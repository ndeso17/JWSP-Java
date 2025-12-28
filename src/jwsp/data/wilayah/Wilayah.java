package jwsp.data.wilayah;

import java.time.LocalDate;
import java.time.ZoneId;

public class Wilayah {
    
    private String id;
    private String provinsi;
    private String nama;
    private TipeWilayah tipe;
    private double latitude;
    private double longitude;
    private ZoneId zonaWaktu;
    
    // Konstruktor lengkap
    public Wilayah(String id, String provinsi, String nama, TipeWilayah tipe, 
                   double latitude, double longitude, String zonaWaktu) {
        this.id = id;
        this.provinsi = provinsi;
        this.nama = nama;
        this.tipe = tipe;
        this.latitude = latitude;
        this.longitude = longitude;
        this.zonaWaktu = ZoneId.of(zonaWaktu);
    }
    
    // Konstruktor dari CSV line
    public static Wilayah fromCSV(String line) {
        String[] parts = line.split(",");
        if (parts.length < 7) return null;
        
        try {
            return new Wilayah(
                parts[0].trim(),                          // id
                parts[1].trim(),                          // provinsi
                parts[2].trim(),                          // nama
                TipeWilayah.fromString(parts[3].trim()),  // tipe
                Double.parseDouble(parts[4].trim()),      // latitude
                Double.parseDouble(parts[5].trim()),      // longitude
                parts[6].trim()                           // zonaWaktu
            );
        } catch (Exception e) {
            System.out.println("[Wilayah] Failed to parse: " + line);
            return null;
        }
    }
    
    // Getters
    
    public String getId() {
        return id;
    }
    
    public String getProvinsi() {
        return provinsi;
    }
    
    public String getNama() {
        return nama;
    }
    
    public TipeWilayah getTipe() {
        return tipe;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public ZoneId getZonaWaktu() {
        return zonaWaktu;
    }
    
    /**
     * Mendapatkan label zona waktu (WIB/WITA/WIT)
     */
    public String getZonaWaktuLabel() {
        String zone = zonaWaktu.getId();
        if (zone.contains("Jakarta")) return "WIB";
        if (zone.contains("Makassar")) return "WITA";
        if (zone.contains("Jayapura")) return "WIT";
        return "WIB";
    }
    
    /**
     * Mendapatkan nama lengkap (dengan tipe)
     */
    public String getNamaLengkap() {
        return tipe.getLabel() + " " + nama;
    }
    
    /**
     * Mendapatkan nama untuk display
     */
    public String getDisplayName() {
        return nama;
    }
    
    @Override
    public String toString() {
        return nama;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Wilayah) {
            return this.id.equals(((Wilayah) obj).id);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
