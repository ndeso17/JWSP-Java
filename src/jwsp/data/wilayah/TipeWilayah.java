package jwsp.data.wilayah;

public enum TipeWilayah {
    KOTA("Kota"),
    KABUPATEN("Kabupaten");
    
    private final String label;
    
    TipeWilayah(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
    
    @Override
    public String toString() {
        return label;
    }
    
    public static TipeWilayah fromString(String text) {
        for (TipeWilayah t : TipeWilayah.values()) {
            if (t.name().equalsIgnoreCase(text) || t.label.equalsIgnoreCase(text)) {
                return t;
            }
        }
        return KABUPATEN;
    }
}
