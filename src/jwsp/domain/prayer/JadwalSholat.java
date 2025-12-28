package jwsp.domain.prayer;

import jwsp.data.wilayah.Wilayah;
import jwsp.data.wilayah.WilayahData;
import jwsp.data.wilayah.DataKota;
import jwsp.api.JadwalSholatService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class JadwalSholat {
    
    // Nama-nama waktu sholat
    public static final String[] NAMA_SHOLAT = {
        "Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya"
    };
    
    // Angle constants (ISNA method)
    private static final double FAJR_ANGLE = -18.0;
    private static final double SUNRISE_ANGLE = -0.833;
    private static final double MAGHRIB_ANGLE = -0.833;
    private static final double ISHA_ANGLE = -17.0;
    
    private Wilayah wilayah;
    private LocalDate tanggal;
    private LocalTime[] waktuSholat;
    private double latitude;
    private double longitude;
    private ZoneId zonaWaktu;
    private LocalTime sunriseTime;
    private LocalTime dhuhaTime;
    
    private String namaKota;
    
    public JadwalSholat(Wilayah wilayah) {
        this.wilayah = wilayah;
        this.namaKota = wilayah.getNama();
        this.latitude = wilayah.getLatitude();
        this.longitude = wilayah.getLongitude();
        this.zonaWaktu = wilayah.getZonaWaktu();
        this.tanggal = LocalDate.now(zonaWaktu);
        this.waktuSholat = new LocalTime[5];
        
        if (!tryApiSchedule()) {
            hitungJadwal();
        }
    }
    
    public JadwalSholat(String namaKota) {
        this.namaKota = namaKota;
        
        // Coba cari di WilayahData
        Wilayah w = WilayahData.getInstance().getByNama(namaKota);
        if (w != null) {
            this.wilayah = w;
            this.latitude = w.getLatitude();
            this.longitude = w.getLongitude();
            this.zonaWaktu = w.getZonaWaktu();
        } else {
            // Fallback ke DataKota lama
            this.wilayah = null;
            this.latitude = -6.2; // Default Jakarta
            this.longitude = 106.8;
            this.zonaWaktu = DataKota.getZonaWaktu(namaKota);
        }
        
        this.tanggal = LocalDate.now(zonaWaktu);
        this.waktuSholat = new LocalTime[5];
        if (!tryApiSchedule()) {
            hitungJadwal();
        }
    }
    
    public JadwalSholat(String namaKota, LocalDate tanggal) {
        this(namaKota);
        this.tanggal = tanggal;
        if (!tryApiSchedule()) {
            hitungJadwal();
        }
    }
    
    private boolean tryApiSchedule() {
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            return false; // Don't block EDT
        }
        if (wilayah == null) return false;
        
        try {
            PrayerSchedule schedule = JadwalSholatService.getInstance().getSchedule(wilayah.getId(), tanggal);
            if (schedule != null) {
                waktuSholat[0] = schedule.getTime("fajr");
                waktuSholat[1] = schedule.getTime("dhuhr");
                waktuSholat[2] = schedule.getTime("asr");
                waktuSholat[3] = schedule.getTime("maghrib");
                waktuSholat[4] = schedule.getTime("isya");
                this.sunriseTime = schedule.getTime("sunrise");
                this.dhuhaTime = schedule.getTime("dhuha");
                
                // Validate
                for (LocalTime t : waktuSholat) {
                    if (t == null) return false;
                }
                return true;
            }
        } catch (Exception e) {
            // API fetch failed
        }
        return false;
    }

    private void hitungJadwal() {
        // Hitung Julian Day
        int year = tanggal.getYear();
        int month = tanggal.getMonthValue();
        int day = tanggal.getDayOfMonth();
        
        double jd = julianDay(year, month, day);
        
        // Timezone offset in hours
        int tzOffset = getTimezoneOffset();
        
        // Calculate prayer times
        double[] times = calculatePrayerTimes(jd, latitude, longitude, tzOffset);
        
        // Convert to LocalTime
        waktuSholat[0] = doubleToTime(times[0]); // Subuh
        waktuSholat[1] = doubleToTime(times[1]); // Dzuhur
        waktuSholat[2] = doubleToTime(times[2]); // Ashar
        waktuSholat[3] = doubleToTime(times[3]); // Maghrib
        waktuSholat[4] = doubleToTime(times[4]); // Isya
        
        // Sunrise and Dhuha (Manual Fallback)
        this.sunriseTime = doubleToTime(times[5]); // times[5] will hold sunrise
        this.dhuhaTime = sunriseTime.plusMinutes(15);
    }

    private double julianDay(int year, int month, int day) {
        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        int a = year / 100;
        int b = 2 - a + a / 4;
        return Math.floor(365.25 * (year + 4716)) + 
               Math.floor(30.6001 * (month + 1)) + 
               day + b - 1524.5;
    }
    
    private double[] calculatePrayerTimes(double jd, double lat, double lng, int tzOffset) {
        double[] times = new double[6];
        
        // Sun declination for this day
        double D = jd - 2451545.0;
        double g = fixAngle(357.529 + 0.98560028 * D);
        double q = fixAngle(280.459 + 0.98564736 * D);
        double L = fixAngle(q + 1.915 * dSin(g) + 0.020 * dSin(2 * g));
        double e = 23.439 - 0.00000036 * D;
        double RA = dArctan2(dCos(e) * dSin(L), dCos(L)) / 15.0;
        RA = fixHour(RA);
        double decl = dArcsin(dSin(e) * dSin(L));
        double eqt = q / 15.0 - RA;
        
        // Calculate Dhuhr time
        double dhuhr = 12 + tzOffset - lng / 15.0 - eqt;
        
        // Calculate prayer times
        times[1] = dhuhr; // Dzuhur
        times[0] = dhuhr - hourAngle(lat, decl, FAJR_ANGLE) / 15.0; // Subuh
        double sunrise = dhuhr - hourAngle(lat, decl, SUNRISE_ANGLE) / 15.0;
        times[2] = dhuhr + asarFactor(lat, decl); // Ashar
        times[3] = dhuhr + hourAngle(lat, decl, MAGHRIB_ANGLE) / 15.0; // Maghrib
        times[4] = dhuhr + hourAngle(lat, decl, ISHA_ANGLE) / 15.0; // Isya
        times[5] = sunrise;
        
        return times;
    }

    private double hourAngle(double lat, double decl, double angle) {
        double cosHa = (dSin(angle) - dSin(lat) * dSin(decl)) / 
                       (dCos(lat) * dCos(decl));
        if (cosHa < -1) cosHa = -1;
        if (cosHa > 1) cosHa = 1;
        return dArccos(cosHa);
    }

    private double asarFactor(double lat, double decl) {
        double factor = 1; // Standard
        double angle = dArccot(factor + dTan(Math.abs(lat - decl)));
        return hourAngle(lat, decl, angle) / 15.0;
    }
    
    private int getTimezoneOffset() {
        String zone = zonaWaktu.getId();
        if (zone.contains("Jakarta")) return 7;
        if (zone.contains("Makassar")) return 8;
        if (zone.contains("Jayapura")) return 9;
        return 7; // Default WIB
    }
    
    private LocalTime doubleToTime(double hours) {
        hours = fixHour(hours);
        int h = (int) Math.floor(hours);
        int m = (int) Math.floor((hours - h) * 60);
        if (h < 0) h = 0;
        if (h > 23) h = 23;
        if (m < 0) m = 0;
        if (m > 59) m = 59;
        return LocalTime.of(h, m);
    }
    
    private double fixAngle(double a) {
        a = a - 360.0 * Math.floor(a / 360.0);
        return a < 0 ? a + 360.0 : a;
    }
    
    private double fixHour(double a) {
        a = a - 24.0 * Math.floor(a / 24.0);
        return a < 0 ? a + 24.0 : a;
    }
    
    private double dSin(double d) { return Math.sin(Math.toRadians(d)); }
    private double dCos(double d) { return Math.cos(Math.toRadians(d)); }
    private double dTan(double d) { return Math.tan(Math.toRadians(d)); }
    private double dArcsin(double x) { return Math.toDegrees(Math.asin(x)); }
    private double dArccos(double x) { return Math.toDegrees(Math.acos(x)); }
    private double dArctan2(double y, double x) { return Math.toDegrees(Math.atan2(y, x)); }
    private double dArccot(double x) { return Math.toDegrees(Math.atan(1.0 / x)); }
    
    public String getWaktuString(int index) {
        if (index < 0 || index >= 5) return "--:--";
        return waktuSholat[index].format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    public LocalTime getWaktu(int index) {
        if (index < 0 || index >= 5) return LocalTime.MIDNIGHT;
        return waktuSholat[index];
    }
    
    public String getWaktuImsak() {
        LocalTime imsak = waktuSholat[0].minusMinutes(10);
        return imsak.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getWaktuSunrise() {
        if (sunriseTime == null) return "--:--";
        return sunriseTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getWaktuDhuha() {
        if (dhuhaTime == null) return "--:--";
        return dhuhaTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    public LocalTime getImsakTime() {
        return waktuSholat[0].minusMinutes(10);
    }
    
    public String getWaktuBukaPuasa() {
        return getWaktuString(3);
    }
    
    public LocalTime getBukaPuasaTime() {
        return waktuSholat[3];
    }
    
    public String getNamaKota() {
        return namaKota;
    }
    
    public Wilayah getWilayah() {
        return wilayah;
    }
    
    public LocalDate getTanggal() {
        return tanggal;
    }
    
    public String getZonaWaktuString() {
        if (wilayah != null) {
            return wilayah.getZonaWaktuLabel();
        }
        return DataKota.getKeteranganZona(namaKota);
    }
    
    public String getTanggalFormatted() {
        return tanggal.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));
    }
    
    public String[] getSemuaJadwal() {
        String[] hasil = new String[5];
        for (int i = 0; i < 5; i++) {
            hasil[i] = NAMA_SHOLAT[i] + ": " + getWaktuString(i);
        }
        return hasil;
    }
    
    public void refresh() {
        this.tanggal = LocalDate.now(zonaWaktu);
        hitungJadwal();
    }
    
    public void setKota(String namaKota) {
        this.namaKota = namaKota;
        
        Wilayah w = WilayahData.getInstance().getByNama(namaKota);
        if (w != null) {
            this.wilayah = w;
            this.latitude = w.getLatitude();
            this.longitude = w.getLongitude();
            this.zonaWaktu = w.getZonaWaktu();
        } else {
            this.wilayah = null;
            this.zonaWaktu = DataKota.getZonaWaktu(namaKota);
        }
        
        this.tanggal = LocalDate.now(zonaWaktu);
        hitungJadwal();
    }
    
    public void setWilayah(Wilayah wilayah) {
        this.wilayah = wilayah;
        this.namaKota = wilayah.getNama();
        this.latitude = wilayah.getLatitude();
        this.longitude = wilayah.getLongitude();
        this.zonaWaktu = wilayah.getZonaWaktu();
        this.tanggal = LocalDate.now(zonaWaktu);
        hitungJadwal();
    }
}
