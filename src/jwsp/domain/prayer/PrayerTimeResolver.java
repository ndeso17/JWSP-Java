package jwsp.domain.prayer;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class PrayerTimeResolver {

    public enum PrayerType {
        SUBUH("Subuh"),
        DZUHUR("Dzuhur"),
        ASHAR("Ashar"),
        MAGHRIB("Maghrib"),
        ISYA("Isya");

        private final String label;

        PrayerType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static class PrayerStateResult {
        public PrayerType currentPrayer;
        public PrayerType nextPrayer;
        public LocalTime currentPrayerTime;
        public LocalTime nextPrayerTime;
        public Duration timeToNext;
        public Duration timeSinceCurrent;
        
        public String toString() {
            return String.format("Current: %s (%s) | Next: %s (%s) | ToNext: %dm", 
                currentPrayer, currentPrayerTime, nextPrayer, nextPrayerTime, timeToNext.toMinutes());
        }
    }

    /**
     * Resolve status sholat berdasarkan waktu saat ini dan jadwal
     * @param now Waktu saat ini
     * @param jadwalSholat Object JadwalSholat yang berisi waktu-waktu sholat
     * @return PrayerStateResult
     */
    public static PrayerStateResult resolve(LocalTime now, JadwalSholat jadwalSholat) {
        LocalTime subuh = jadwalSholat.getWaktu(0);
        LocalTime dzuhur = jadwalSholat.getWaktu(1);
        LocalTime ashar = jadwalSholat.getWaktu(2);
        LocalTime maghrib = jadwalSholat.getWaktu(3);
        LocalTime isya = jadwalSholat.getWaktu(4);

        PrayerStateResult result = new PrayerStateResult();

        // 1. Cek Interval SUBUH: Subuh <= now < Dzuhur
        if (isBetween(now, subuh, dzuhur)) {
            result.currentPrayer = PrayerType.SUBUH;
            result.nextPrayer = PrayerType.DZUHUR;
            result.currentPrayerTime = subuh;
            result.nextPrayerTime = dzuhur;
        }
        // 2. Cek Interval DZUHUR: Dzuhur <= now < Ashar
        else if (isBetween(now, dzuhur, ashar)) {
            result.currentPrayer = PrayerType.DZUHUR;
            result.nextPrayer = PrayerType.ASHAR;
            result.currentPrayerTime = dzuhur;
            result.nextPrayerTime = ashar;
        }
        // 3. Cek Interval ASHAR: Ashar <= now < Maghrib
        else if (isBetween(now, ashar, maghrib)) {
            result.currentPrayer = PrayerType.ASHAR;
            result.nextPrayer = PrayerType.MAGHRIB;
            result.currentPrayerTime = ashar;
            result.nextPrayerTime = maghrib;
        }
        // 4. Cek Interval MAGHRIB: Maghrib <= now < Isya
        else if (isBetween(now, maghrib, isya)) {
            result.currentPrayer = PrayerType.MAGHRIB;
            result.nextPrayer = PrayerType.ISYA;
            result.currentPrayerTime = maghrib;
            result.nextPrayerTime = isya;
        }
        // 5. Cek Interval ISYA: Isya <= now OR now < Subuh
        else {
            result.currentPrayer = PrayerType.ISYA;
            result.nextPrayer = PrayerType.SUBUH;
            result.currentPrayerTime = isya;
            result.nextPrayerTime = subuh;
        }

        // Kalkulasi durasi
        result.timeToNext = calculateDuration(now, result.nextPrayerTime);
        result.timeSinceCurrent = calculateDuration(result.currentPrayerTime, now);

        return result;
    }

    private static boolean isBetween(LocalTime time, LocalTime start, LocalTime end) {
        return !time.isBefore(start) && time.isBefore(end);
    }

    private static Duration calculateDuration(LocalTime start, LocalTime end) {
        if (end.isBefore(start)) {
            // Lintas hari (contoh: 20:00 ke 04:00)
            long minutes = Duration.between(start, LocalTime.MAX).toMinutes() + 
                           Duration.between(LocalTime.MIN, end).toMinutes() + 1; // +1 untuk menit transisi 23:59 -> 00:00
            return Duration.ofMinutes(minutes);
        } else {
            return Duration.between(start, end);
        }
    }
}
