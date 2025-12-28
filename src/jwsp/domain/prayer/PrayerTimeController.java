package jwsp.domain.prayer;

import jwsp.data.wilayah.DataKota;
import jwsp.api.JadwalSholatService;
import jwsp.data.JadwalPuasa;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PrayerTimeController {
    
    // Konstanta
    private static final int UPCOMING_THRESHOLD_MINUTES = 20;
    
    // State enum
    public enum PrayerState {
        CURRENT,    // Sedang berlangsung
        UPCOMING,   // Akan datang (dalam 20 menit)
        WAITING     // Menunggu (lebih dari 20 menit)
    }
    
    // Data class untuk prayer info
    public static class PrayerInfo {
        public String name;
        public LocalTime time;
        public PrayerState state;
        public int minutesRemaining;
        
        public PrayerInfo(String name, LocalTime time, PrayerState state, int minutesRemaining) {
            this.name = name;
            this.time = time;
            this.state = state;
            this.minutesRemaining = minutesRemaining;
        }
        
        public String getTimeString() {
            return time.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        
        public String getStateLabel() {
            switch (state) {
                case CURRENT: return "SEDANG";
                case UPCOMING: return "AKAN DATANG";
                default: return "";
            }
        }
        
        public String getCountdownLabel() {
            if (minutesRemaining <= 0) return "Sekarang";
            if (minutesRemaining < 60) return minutesRemaining + " menit lagi";
            int hours = minutesRemaining / 60;
            int mins = minutesRemaining % 60;
            return hours + " jam " + mins + " menit lagi";
        }
    }
    
    // Listener interface
    public interface PrayerTimeListener {
        void onPrayerTimeUpdate(PrayerInfo current, PrayerInfo next, String puasaStatus);
    }
    
    // Fields
    private JadwalSholat jadwalSholat;
    private JadwalPuasa jadwalPuasa;
    private List<PrayerTimeListener> listeners;
    private PrayerInfo currentPrayer;
    private PrayerInfo nextPrayer;
    private String puasaStatus;
    
    /**
     * Constructor
     */
    public PrayerTimeController(JadwalSholat jadwalSholat, JadwalPuasa jadwalPuasa) {
        this.jadwalSholat = jadwalSholat;
        this.jadwalPuasa = jadwalPuasa;
        this.listeners = new ArrayList<>();
        update();
    }
    
    /**
     * Menambah listener
     */
    public void addListener(PrayerTimeListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Menghapus listener
     */
    public void removeListener(PrayerTimeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Update state berdasarkan waktu saat ini
     */
    public void update() {
        ZoneId zone = (jadwalSholat.getWilayah() != null) ? 
                      jadwalSholat.getWilayah().getZonaWaktu() : 
                      DataKota.getZonaWaktu(jadwalSholat.getNamaKota());
        LocalTime now = LocalTime.now(zone);
        
        PrayerSchedule schedule = JadwalSholatService.getInstance().getCurrentSchedule();
        if (schedule == null) {
            schedule = new PrayerSchedule(jadwalSholat.getNamaKota(), LocalDate.now().toString());
            schedule.setTime("fajr", jadwalSholat.getWaktuString(0));
            schedule.setTime("sunrise", jadwalSholat.getWaktuSunrise());
            schedule.setTime("dhuha", jadwalSholat.getWaktuDhuha());
            schedule.setTime("dhuhr", jadwalSholat.getWaktuString(1));
            schedule.setTime("asr", jadwalSholat.getWaktuString(2));
            schedule.setTime("maghrib", jadwalSholat.getWaktuString(3));
            schedule.setTime("isya", jadwalSholat.getWaktuString(4));
        }

        // 2. Resolve using Phase Engine
        PrayerPhaseEngine.PhaseResult result = PrayerPhaseEngine.resolve(now, schedule);

        if (result != null) {
            String currentStatus = result.currentPhase.getStatusText();
            if (result.currentPhase == PrayerPhase.NONE_GAP) {
                currentStatus = "Akan Datang Dhuha (Sunnah)";
            }

            currentPrayer = new PrayerInfo(
                currentStatus,
                null,
                PrayerState.CURRENT,
                0
            );

            PrayerState nextState = (result.minutesToNext <= UPCOMING_THRESHOLD_MINUTES) ? 
                                    PrayerState.UPCOMING : PrayerState.WAITING;

            nextPrayer = new PrayerInfo(
                result.nextObligatoryName,
                result.nextObligatoryTime,
                nextState,
                (int) result.minutesToNext
            );
        }
        
        puasaStatus = jadwalPuasa.getStatusPuasa();

        notifyListeners();
    }
    
    private void notifyListeners() {
        for (PrayerTimeListener listener : listeners) {
            listener.onPrayerTimeUpdate(currentPrayer, nextPrayer, puasaStatus);
        }
    }
    
    
    public PrayerInfo getCurrentPrayer() {
        return currentPrayer;
    }
    
    public PrayerInfo getNextPrayer() {
        return nextPrayer;
    }
    
    public String getPuasaStatus() {
        return puasaStatus;
    }
    
    public String getKotaName() {
        return jadwalSholat.getNamaKota();
    }
    
    public String getZonaWaktu() {
        return jadwalSholat.getZonaWaktuString();
    }
    
    public JadwalSholat getJadwalSholat() {
        return jadwalSholat;
    }
    
    public JadwalPuasa getJadwalPuasa() {
        return jadwalPuasa;
    }
    
    public void setJadwalSholat(JadwalSholat jadwalSholat) {
        this.jadwalSholat = jadwalSholat;
        update();
    }
    
    public void setJadwalPuasa(JadwalPuasa jadwalPuasa) {
        this.jadwalPuasa = jadwalPuasa;
        update();
    }
    
    public String getCurrentTimeString() {
        ZoneId zone = DataKota.getZonaWaktu(jadwalSholat.getNamaKota());
        return LocalTime.now(zone).format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    public String getTooltipText() {
        StringBuilder sb = new StringBuilder();
        
        if (nextPrayer.state == PrayerState.UPCOMING) {
            sb.append("⏳ ").append(nextPrayer.name).append(" (").append(nextPrayer.getCountdownLabel()).append(")\n");
        } else {
            sb.append("🕌 ").append(currentPrayer.name).append(" (sedang)\n");
        }
        
        sb.append("⏭ ").append(nextPrayer.name).append(" ")
          .append(nextPrayer.getTimeString());
        
        if (nextPrayer.minutesRemaining > 0) {
            sb.append(" (").append(nextPrayer.getCountdownLabel()).append(")");
        }
        
        return sb.toString();
    }
    
    public String getShortInfo() {
        if (nextPrayer.state == PrayerState.UPCOMING) {
            return nextPrayer.name + " dalam " + nextPrayer.minutesRemaining + " menit";
        } else {
            return currentPrayer.name + " (sedang)";
        }
    }
}
