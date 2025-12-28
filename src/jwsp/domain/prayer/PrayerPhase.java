package jwsp.domain.prayer;

public enum PrayerPhase {
    SHUBUH("Shubuh", true),
    DHUHA("Dhuha", false),
    DHUHUR("Dhuhur", true),
    ASHAR("Ashar", true),
    MAGHRIB("Maghrib", true),
    ISYA("Isya", true),
    NONE_GAP("Tunggu", false);

    private final String label;
    private final boolean wajib;

    PrayerPhase(String label, boolean wajib) {
        this.label = label;
        this.wajib = wajib;
    }

    public String getLabel() {
        return label;
    }

    public boolean isWajib() {
        return wajib;
    }

    public String getStatusText() {
        return String.format("%s (%s)", label, wajib ? "Wajib" : "Sunnah");
    }
}
