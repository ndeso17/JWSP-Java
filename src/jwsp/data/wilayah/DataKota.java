package jwsp.data.wilayah;

import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class DataKota {
    public static class Kota {
        public String nama;
        public ZoneId zona;
        public int[] offset;
        
        public Kota(String nama, String zonaId, int[] offset) {
            this.nama = nama;
            this.zona = ZoneId.of(zonaId);
            this.offset = offset;
        }
    }
    
    // Map provinsi → list kota
    private static final Map<String, List<Kota>> PROVINSI_KOTA = new LinkedHashMap<>();
    
    // Daftar nama provinsi (untuk combo box)
    public static final String[] DAFTAR_PROVINSI;
    
    // Legacy: Daftar nama kota (untuk backward compatibility)
    public static final String[] DAFTAR_KOTA;
    
    static {
        // === WIB (UTC+7) ===
        
        // DKI Jakarta
        PROVINSI_KOTA.put("DKI Jakarta", Arrays.asList(
            new Kota("Jakarta", "Asia/Jakarta", new int[]{0, 0, 0, 0, 0}),
            new Kota("Jakarta Pusat", "Asia/Jakarta", new int[]{0, 0, 0, 0, 0}),
            new Kota("Jakarta Selatan", "Asia/Jakarta", new int[]{0, 0, 0, 0, 0}),
            new Kota("Jakarta Timur", "Asia/Jakarta", new int[]{0, 0, 0, 0, 0})
        ));
        
        // Jawa Barat
        PROVINSI_KOTA.put("Jawa Barat", Arrays.asList(
            new Kota("Bandung", "Asia/Jakarta", new int[]{2, 3, 3, 3, 3}),
            new Kota("Bekasi", "Asia/Jakarta", new int[]{-1, -1, -1, -1, -1}),
            new Kota("Depok", "Asia/Jakarta", new int[]{0, 0, 0, 0, 0}),
            new Kota("Bogor", "Asia/Jakarta", new int[]{1, 1, 1, 1, 1}),
            new Kota("Cirebon", "Asia/Jakarta", new int[]{-5, -5, -5, -5, -5}),
            new Kota("Tasikmalaya", "Asia/Jakarta", new int[]{0, 0, 0, 0, 0})
        ));
        
        // Jawa Tengah
        PROVINSI_KOTA.put("Jawa Tengah", Arrays.asList(
            new Kota("Semarang", "Asia/Jakarta", new int[]{-15, -15, -15, -15, -15}),
            new Kota("Solo", "Asia/Jakarta", new int[]{-18, -18, -18, -18, -18}),
            new Kota("Purwokerto", "Asia/Jakarta", new int[]{-8, -8, -8, -8, -8}),
            new Kota("Pekalongan", "Asia/Jakarta", new int[]{-12, -12, -12, -12, -12}),
            new Kota("Tegal", "Asia/Jakarta", new int[]{-8, -8, -8, -8, -8})
        ));
        
        // DI Yogyakarta
        PROVINSI_KOTA.put("DI Yogyakarta", Arrays.asList(
            new Kota("Yogyakarta", "Asia/Jakarta", new int[]{-10, -10, -10, -10, -10}),
            new Kota("Sleman", "Asia/Jakarta", new int[]{-10, -10, -10, -10, -10}),
            new Kota("Bantul", "Asia/Jakarta", new int[]{-10, -10, -10, -10, -10})
        ));
        
        // Jawa Timur
        PROVINSI_KOTA.put("Jawa Timur", Arrays.asList(
            new Kota("Surabaya", "Asia/Jakarta", new int[]{-25, -25, -25, -25, -25}),
            new Kota("Malang", "Asia/Jakarta", new int[]{-23, -23, -23, -23, -23}),
            new Kota("Sidoarjo", "Asia/Jakarta", new int[]{-25, -25, -25, -25, -25}),
            new Kota("Kediri", "Asia/Jakarta", new int[]{-22, -22, -22, -22, -22}),
            new Kota("Jember", "Asia/Jakarta", new int[]{-28, -28, -28, -28, -28})
        ));
        
        // Sumatera Utara
        PROVINSI_KOTA.put("Sumatera Utara", Arrays.asList(
            new Kota("Medan", "Asia/Jakarta", new int[]{30, 30, 30, 30, 30}),
            new Kota("Binjai", "Asia/Jakarta", new int[]{30, 30, 30, 30, 30}),
            new Kota("Pematangsiantar", "Asia/Jakarta", new int[]{28, 28, 28, 28, 28})
        ));
        
        // Sumatera Selatan
        PROVINSI_KOTA.put("Sumatera Selatan", Arrays.asList(
            new Kota("Palembang", "Asia/Jakarta", new int[]{15, 15, 15, 15, 15}),
            new Kota("Lubuklinggau", "Asia/Jakarta", new int[]{12, 12, 12, 12, 12})
        ));
        
        // Riau
        PROVINSI_KOTA.put("Riau", Arrays.asList(
            new Kota("Pekanbaru", "Asia/Jakarta", new int[]{22, 22, 22, 22, 22}),
            new Kota("Dumai", "Asia/Jakarta", new int[]{24, 24, 24, 24, 24})
        ));
        
        // Kalimantan Barat
        PROVINSI_KOTA.put("Kalimantan Barat", Arrays.asList(
            new Kota("Pontianak", "Asia/Jakarta", new int[]{20, 20, 20, 20, 20}),
            new Kota("Singkawang", "Asia/Jakarta", new int[]{22, 22, 22, 22, 22})
        ));
        
        // === WITA (UTC+8) ===
        
        // Sulawesi Selatan
        PROVINSI_KOTA.put("Sulawesi Selatan", Arrays.asList(
            new Kota("Makassar", "Asia/Makassar", new int[]{-20, -20, -20, -20, -20}),
            new Kota("Parepare", "Asia/Makassar", new int[]{-18, -18, -18, -18, -18})
        ));
        
        // Sulawesi Utara
        PROVINSI_KOTA.put("Sulawesi Utara", Arrays.asList(
            new Kota("Manado", "Asia/Makassar", new int[]{-35, -35, -35, -35, -35}),
            new Kota("Bitung", "Asia/Makassar", new int[]{-36, -36, -36, -36, -36})
        ));
        
        // Bali
        PROVINSI_KOTA.put("Bali", Arrays.asList(
            new Kota("Denpasar", "Asia/Makassar", new int[]{-30, -30, -30, -30, -30}),
            new Kota("Singaraja", "Asia/Makassar", new int[]{-30, -30, -30, -30, -30}),
            new Kota("Ubud", "Asia/Makassar", new int[]{-30, -30, -30, -30, -30})
        ));
        
        // Kalimantan Timur
        PROVINSI_KOTA.put("Kalimantan Timur", Arrays.asList(
            new Kota("Balikpapan", "Asia/Makassar", new int[]{-25, -25, -25, -25, -25}),
            new Kota("Samarinda", "Asia/Makassar", new int[]{-23, -23, -23, -23, -23})
        ));
        
        // Nusa Tenggara Barat
        PROVINSI_KOTA.put("Nusa Tenggara Barat", Arrays.asList(
            new Kota("Mataram", "Asia/Makassar", new int[]{-32, -32, -32, -32, -32}),
            new Kota("Bima", "Asia/Makassar", new int[]{-36, -36, -36, -36, -36})
        ));
        
        // === WIT (UTC+9) ===
        
        // Papua
        PROVINSI_KOTA.put("Papua", Arrays.asList(
            new Kota("Jayapura", "Asia/Jayapura", new int[]{-40, -40, -40, -40, -40}),
            new Kota("Sorong", "Asia/Jayapura", new int[]{-30, -30, -30, -30, -30})
        ));
        
        // Maluku
        PROVINSI_KOTA.put("Maluku", Arrays.asList(
            new Kota("Ambon", "Asia/Jayapura", new int[]{-30, -30, -30, -30, -30}),
            new Kota("Tual", "Asia/Jayapura", new int[]{-35, -35, -35, -35, -35})
        ));
        
        // Build daftar provinsi
        DAFTAR_PROVINSI = PROVINSI_KOTA.keySet().toArray(new String[0]);
        
        // Build daftar kota (legacy)
        List<String> semuaKota = new ArrayList<>();
        for (List<Kota> kotaList : PROVINSI_KOTA.values()) {
            for (Kota k : kotaList) {
                if (!semuaKota.contains(k.nama)) {
                    semuaKota.add(k.nama);
                }
            }
        }
        DAFTAR_KOTA = semuaKota.toArray(new String[0]);
    }
    
    /**
     * Mendapatkan daftar kota untuk provinsi tertentu
     */
    public static String[] getKotaByProvinsi(String provinsi) {
        List<Kota> kotaList = PROVINSI_KOTA.get(provinsi);
        if (kotaList == null) return new String[0];
        
        String[] result = new String[kotaList.size()];
        for (int i = 0; i < kotaList.size(); i++) {
            result[i] = kotaList.get(i).nama;
        }
        return result;
    }
    
    /**
     * Mendapatkan provinsi untuk kota tertentu
     */
    public static String getProvinsiByKota(String namaKota) {
        for (Map.Entry<String, List<Kota>> entry : PROVINSI_KOTA.entrySet()) {
            for (Kota k : entry.getValue()) {
                if (k.nama.equals(namaKota)) {
                    return entry.getKey();
                }
            }
        }
        return DAFTAR_PROVINSI[0]; // Default
    }
    
    /**
     * Mendapatkan data kota lengkap
     */
    public static Kota getKotaData(String namaKota) {
        for (List<Kota> kotaList : PROVINSI_KOTA.values()) {
            for (Kota k : kotaList) {
                if (k.nama.equals(namaKota)) {
                    return k;
                }
            }
        }
        // Default ke Jakarta
        return PROVINSI_KOTA.get("DKI Jakarta").get(0);
    }
    
    /**
     * Mendapatkan zona waktu untuk kota tertentu
     */
    public static ZoneId getZonaWaktu(String namaKota) {
        Kota kota = getKotaData(namaKota);
        return kota.zona;
    }
    
    /**
     * Mendapatkan keterangan zona waktu (WIB/WITA/WIT)
     */
    public static String getKeteranganZona(String namaKota) {
        Kota kota = getKotaData(namaKota);
        String zonaString = kota.zona.toString();
        
        if (zonaString.contains("Jakarta")) {
            return "WIB";
        } else if (zonaString.contains("Makassar")) {
            return "WITA";
        } else if (zonaString.contains("Jayapura")) {
            return "WIT";
        }
        return "WIB";
    }
    
    /**
     * Mendapatkan offset waktu sholat untuk kota tertentu
     */
    public static int[] getOffset(String namaKota) {
        Kota kota = getKotaData(namaKota);
        return kota.offset;
    }
    
    /**
     * Mendapatkan waktu referensi dasar (Jakarta)
     */
    public static int[] getWaktuReferensi() {
        // Waktu dalam menit dari tengah malam
        // Subuh: 04:30, Dzuhur: 12:00, Ashar: 15:15, Maghrib: 18:00, Isya: 19:15
        return new int[]{270, 720, 915, 1080, 1155};
    }
    
    /**
     * Cek apakah kota valid
     */
    public static boolean isKotaValid(String namaKota) {
        for (List<Kota> kotaList : PROVINSI_KOTA.values()) {
            for (Kota k : kotaList) {
                if (k.nama.equals(namaKota)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Cek apakah provinsi valid
     */
    public static boolean isProvinsiValid(String provinsi) {
        return PROVINSI_KOTA.containsKey(provinsi);
    }
}
