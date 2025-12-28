package jwsp.data.wilayah;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class WilayahData {
    
    // Singleton
    private static WilayahData instance;
    
    // Data storage
    private List<Wilayah> allWilayah;
    private Map<String, List<Wilayah>> byProvinsi;
    private Map<String, Wilayah> byId;
    private Map<String, Wilayah> byNama;
    
    // Daftar provinsi (sorted)
    private String[] daftarProvinsi;
    
    // Data file
    private static final String DATA_FILE = "data/wilayah.csv";
    
    private WilayahData() {
        allWilayah = new ArrayList<>();
        byProvinsi = new LinkedHashMap<>();
        byId = new HashMap<>();
        byNama = new HashMap<>();
        
        loadData();
        buildIndexes();
    }
    
    public static synchronized WilayahData getInstance() {
        if (instance == null) {
            instance = new WilayahData();
        }
        return instance;
    }
    
    /**
     * Load data dari CSV file
     */
    private void loadData() {
        String[] paths = {DATA_FILE, "src/" + DATA_FILE};
        InputStream is = null;
        
        // Coba dari classpath
        is = getClass().getClassLoader().getResourceAsStream(DATA_FILE);
        
        // Coba dari file system
        if (is == null) {
            for (String path : paths) {
                File file = new File(path);
                if (file.exists()) {
                    try {
                        is = new FileInputStream(file);
                        break;
                    } catch (Exception e) {
                        // Continue to next path
                    }
                }
            }
        }
        
        if (is == null) {
            loadDefaultData();
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                Wilayah w = Wilayah.fromCSV(line);
                if (w != null) {
                    allWilayah.add(w);
                    count++;
                }
            }
        } catch (IOException e) {
            loadDefaultData();
        }
    }
    
    private void loadDefaultData() {
        // Minimal data untuk fallback
        String[] defaultData = {
            "3101,DKI Jakarta,Jakarta Pusat,KOTA,-6.1864,106.8340,Asia/Jakarta",
            "3102,DKI Jakarta,Jakarta Selatan,KOTA,-6.2615,106.8106,Asia/Jakarta",
            "3201,Jawa Barat,Bandung,KOTA,-6.9175,107.6191,Asia/Jakarta",
            "3202,Jawa Barat,Bekasi,KOTA,-6.2349,106.9896,Asia/Jakarta",
            "3301,Jawa Tengah,Semarang,KOTA,-6.9666,110.4196,Asia/Jakarta",
            "3401,DI Yogyakarta,Yogyakarta,KOTA,-7.7956,110.3695,Asia/Jakarta",
            "3501,Jawa Timur,Surabaya,KOTA,-7.2575,112.7521,Asia/Jakarta"
        };
        
        for (String line : defaultData) {
            Wilayah w = Wilayah.fromCSV(line);
            if (w != null) {
                allWilayah.add(w);
            }
        }
    }
    
    private void buildIndexes() {
        // Group by provinsi
        byProvinsi.clear();
        for (Wilayah w : allWilayah) {
            byProvinsi.computeIfAbsent(w.getProvinsi(), k -> new ArrayList<>()).add(w);
            byId.put(w.getId(), w);
            byNama.put(w.getNama().toLowerCase(), w);
        }
        
        // Build provinsi list (sorted)
        Set<String> provinsiSet = new LinkedHashSet<>(byProvinsi.keySet());
        daftarProvinsi = provinsiSet.toArray(new String[0]);
    }

    public String[] getDaftarProvinsi() {
        return daftarProvinsi;
    }
    
    public List<Wilayah> getWilayahByProvinsi(String provinsi) {
        return byProvinsi.getOrDefault(provinsi, new ArrayList<>());
    }
    
    public List<Wilayah> getWilayahByProvinsiAndTipe(String provinsi, TipeWilayah tipe) {
        List<Wilayah> list = byProvinsi.getOrDefault(provinsi, new ArrayList<>());
        if (tipe == null) return list;
        
        return list.stream()
            .filter(w -> w.getTipe() == tipe)
            .collect(Collectors.toList());
    }
    
    public String[] getWilayahNames(String provinsi, TipeWilayah tipe) {
        List<Wilayah> list;
        if (tipe == null) {
            list = getWilayahByProvinsi(provinsi);
        } else {
            list = getWilayahByProvinsiAndTipe(provinsi, tipe);
        }
        
        return list.stream()
            .map(Wilayah::getNama)
            .toArray(String[]::new);
    }
    
    public Wilayah getById(String id) {
        return byId.get(id);
    }
    
    public Wilayah getByNama(String nama) {
        Wilayah w = byNama.get(nama.toLowerCase());
        if (w != null) return w;

        for (Wilayah wilayah : allWilayah) {
            if (wilayah.getNama().equalsIgnoreCase(nama)) {
                return wilayah;
            }
        }
        return null;
    }
    
    public Wilayah getByNamaInProvinsi(String nama, String provinsi) {
        List<Wilayah> list = byProvinsi.get(provinsi);
        if (list == null) return null;
        
        for (Wilayah w : list) {
            if (w.getNama().equalsIgnoreCase(nama)) {
                return w;
            }
        }
        return null;
    }
    
    public boolean isProvinsiValid(String provinsi) {
        return byProvinsi.containsKey(provinsi);
    }
    
    public boolean isWilayahValid(String nama) {
        return getByNama(nama) != null;
    }
    
    public String getDefaultProvinsi() {
        if (daftarProvinsi.length > 0) {
            // Cari DKI Jakarta
            for (String p : daftarProvinsi) {
                if (p.contains("Jakarta")) return p;
            }
            return daftarProvinsi[0];
        }
        return "DKI Jakarta";
    }

    public Wilayah getDefaultWilayah(String provinsi) {
        List<Wilayah> list = byProvinsi.get(provinsi);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        // Ultimate fallback
        return allWilayah.isEmpty() ? null : allWilayah.get(0);
    }

    public int getTotalWilayah() {
        return allWilayah.size();
    }
    
    public void printStats() {}
}
