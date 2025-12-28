package jwsp.data;

import jwsp.domain.prayer.JadwalSholat;
import jwsp.domain.hijri.HijriCalendar;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;

public class JadwalPuasa {
    public static final String STATUS_RAMADHAN = "Puasa Ramadhan";
    public static final String STATUS_SUNNAH_SENIN = "Puasa Sunnah Senin";
    public static final String STATUS_SUNNAH_KAMIS = "Puasa Sunnah Kamis";
    public static final String STATUS_TIDAK_PUASA = "Tidak Berpuasa";
    private static final LocalDate RAMADHAN_2025_MULAI = LocalDate.of(2025, 3, 1);
    private static final LocalDate RAMADHAN_2025_SELESAI = LocalDate.of(2025, 3, 30);
    private static final LocalDate RAMADHAN_2026_MULAI = LocalDate.of(2026, 2, 18);
    private static final LocalDate RAMADHAN_2026_SELESAI = LocalDate.of(2026, 3, 19);
    
    private LocalDate tanggal;
    private String statusPuasa;
    private JadwalSholat jadwalSholat;
    
    /**
     * Constructor dengan tanggal dan jadwal sholat
     * @param tanggal Tanggal untuk cek puasa
     * @param jadwalSholat Jadwal sholat untuk waktu imsak/buka
     */
    public JadwalPuasa(LocalDate tanggal, JadwalSholat jadwalSholat) {
        this.tanggal = tanggal;
        this.jadwalSholat = jadwalSholat;
        this.statusPuasa = tentukanStatusPuasa();
    }
    
    /**
     * Constructor dengan jadwal sholat saja (tanggal = hari ini)
     * @param jadwalSholat Jadwal sholat
     */
    public JadwalPuasa(JadwalSholat jadwalSholat) {
        this(LocalDate.now(), jadwalSholat);
    }
    
    /**
     * Menentukan status puasa berdasarkan tanggal
     * @return String status puasa
     */
    private String tentukanStatusPuasa() {
        // Cek apakah dalam bulan Ramadhan
        if (isRamadhan(tanggal)) {
            return STATUS_RAMADHAN;
        }
        
        // Cek apakah hari Senin atau Kamis (puasa sunnah)
        DayOfWeek hariIni = tanggal.getDayOfWeek();
        if (hariIni == DayOfWeek.MONDAY) {
            return STATUS_SUNNAH_SENIN;
        } else if (hariIni == DayOfWeek.THURSDAY) {
            return STATUS_SUNNAH_KAMIS;
        }
        
        return STATUS_TIDAK_PUASA;
    }
    
    /**
     * Mengecek apakah tanggal termasuk bulan Ramadhan
     * @param tanggal Tanggal yang dicek
     * @return true jika dalam Ramadhan
     */
    private boolean isRamadhan(LocalDate tanggal) {
        HijriCalendar.HijriDate h = HijriCalendar.fromLocalDate(tanggal);
        return h.month == 9;
    }

    public HijriCalendar.HijriDate getHijriDate() {
        return HijriCalendar.fromLocalDate(tanggal);
    }
    
    /**
     * Mendapatkan status puasa hari ini
     * @return String status puasa
     */
    public String getStatusPuasa() {
        return statusPuasa;
    }
    
    /**
     * Mengecek apakah hari ini adalah hari puasa (wajib atau sunnah)
     * @return true jika hari puasa
     */
    public boolean isHariPuasa() {
        return !statusPuasa.equals(STATUS_TIDAK_PUASA);
    }
    
    /**
     * Mengecek apakah puasa Ramadhan
     * @return true jika puasa Ramadhan
     */
    public boolean isPuasaRamadhan() {
        return statusPuasa.equals(STATUS_RAMADHAN);
    }
    
    /**
     * Mengecek apakah puasa sunnah
     * @return true jika puasa sunnah
     */
    public boolean isPuasaSunnah() {
        return statusPuasa.equals(STATUS_SUNNAH_SENIN) || 
               statusPuasa.equals(STATUS_SUNNAH_KAMIS);
    }
    
    /**
     * Mendapatkan waktu Imsak dari jadwal sholat
     * @return String waktu Imsak
     */
    public String getWaktuImsak() {
        return jadwalSholat.getWaktuImsak();
    }
    
    /**
     * Mendapatkan waktu buka puasa dari jadwal sholat
     * @return String waktu buka puasa
     */
    public String getWaktuBukaPuasa() {
        return jadwalSholat.getWaktuBukaPuasa();
    }
    
    /**
     * Mendapatkan tanggal dalam format readable
     * @return String tanggal terformat
     */
    public String getTanggalFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
        return tanggal.format(formatter);
    }
    
    /**
     * Mendapatkan nama hari dalam bahasa Indonesia
     * @return String nama hari
     */
    public String getNamaHari() {
        DayOfWeek hari = tanggal.getDayOfWeek();
        switch (hari) {
            case MONDAY: return "Senin";
            case TUESDAY: return "Selasa";
            case WEDNESDAY: return "Rabu";
            case THURSDAY: return "Kamis";
            case FRIDAY: return "Jumat";
            case SATURDAY: return "Sabtu";
            case SUNDAY: return "Minggu";
            default: return "";
        }
    }
    
    /**
     * Mendapatkan deskripsi lengkap status puasa
     * @return String deskripsi
     */
    public String getDeskripsiLengkap() {
        StringBuilder sb = new StringBuilder();
        sb.append("Status: ").append(statusPuasa).append("\n");
        
        if (isHariPuasa()) {
            sb.append("Waktu Imsak: ").append(getWaktuImsak()).append("\n");
            sb.append("Waktu Buka: ").append(getWaktuBukaPuasa());
        } else {
            sb.append("Tidak ada puasa wajib atau sunnah hari ini.\n");
            sb.append("Puasa sunnah dianjurkan pada hari Senin dan Kamis.");
        }
        
        return sb.toString();
    }
    
    /**
     * Refresh status puasa dengan tanggal terkini
     * @param jadwalSholat Jadwal sholat yang sudah diupdate
     */
    public void refresh(JadwalSholat jadwalSholat) {
        this.tanggal = LocalDate.now();
        this.jadwalSholat = jadwalSholat;
        this.statusPuasa = tentukanStatusPuasa();
    }
    
    /**
     * Mendapatkan informasi tentang Ramadhan yang di-hardcode
     * @return String informasi Ramadhan
     */
    public static String getInfoRamadhanHardcoded() {
        StringBuilder sb = new StringBuilder();
        sb.append("CATATAN PENTING:\n");
        sb.append("================\n");
        sb.append("Tanggal Ramadhan dalam aplikasi ini adalah PERKIRAAN.\n\n");
        sb.append("Ramadhan 1446 H (2025):\n");
        sb.append("  Mulai: ").append(RAMADHAN_2025_MULAI).append("\n");
        sb.append("  Selesai: ").append(RAMADHAN_2025_SELESAI).append("\n\n");
        sb.append("Ramadhan 1447 H (2026):\n");
        sb.append("  Mulai: ").append(RAMADHAN_2026_MULAI).append("\n");
        sb.append("  Selesai: ").append(RAMADHAN_2026_SELESAI).append("\n\n");
        sb.append("Tanggal pasti Ramadhan ditentukan berdasarkan\n");
        sb.append("ru'yatul hilal oleh pemerintah/ormas Islam.");
        return sb.toString();
    }
}
