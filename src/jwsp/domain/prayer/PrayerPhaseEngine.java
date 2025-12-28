package jwsp.domain.prayer;

import java.time.Duration;
import java.time.LocalTime;

public class PrayerPhaseEngine {

    public static class PhaseResult {
        public final PrayerPhase currentPhase;
        public final String nextObligatoryName;
        public final LocalTime nextObligatoryTime;
        public final long minutesToNext;

        public PhaseResult(PrayerPhase currentPhase, String nextName, LocalTime nextTime, long minutes) {
            this.currentPhase = currentPhase;
            this.nextObligatoryName = nextName;
            this.nextObligatoryTime = nextTime;
            this.minutesToNext = minutes;
        }
    }

    public static PhaseResult resolve(LocalTime now, PrayerSchedule schedule) {
        if (schedule == null) return null;

        LocalTime fajr = schedule.getTime("fajr");
        LocalTime sunrise = schedule.getTime("sunrise");
        LocalTime dhuha = schedule.getTime("dhuha");
        LocalTime dhuhr = schedule.getTime("dhuhr");
        LocalTime ashar = schedule.getTime("asr");
        LocalTime maghrib = schedule.getTime("maghrib");
        LocalTime isya = schedule.getTime("isya");

        // Determine current phase
        PrayerPhase current = PrayerPhase.NONE_GAP;
        String nextWajib = "";
        LocalTime nextTime = null;

        if (isBetween(now, fajr, sunrise)) {
            current = PrayerPhase.SHUBUH;
            nextWajib = "Dzuhur";
            nextTime = dhuhr;
        } else if (isBetween(now, sunrise, dhuha)) {
            current = PrayerPhase.NONE_GAP; // "Tunggu Dhuha"
            nextWajib = "Dzuhur";
            nextTime = dhuhr;
        } else if (isBetween(now, dhuha, dhuhr)) {
            current = PrayerPhase.DHUHA;
            nextWajib = "Dzuhur";
            nextTime = dhuhr;
        } else if (isBetween(now, dhuhr, ashar)) {
            current = PrayerPhase.DHUHUR;
            nextWajib = "Ashar";
            nextTime = ashar;
        } else if (isBetween(now, ashar, maghrib)) {
            current = PrayerPhase.ASHAR;
            nextWajib = "Maghrib";
            nextTime = maghrib;
        } else if (isBetween(now, maghrib, isya)) {
            current = PrayerPhase.MAGHRIB;
            nextWajib = "Isya";
            nextTime = isya;
        } else {
            current = PrayerPhase.ISYA;
            nextWajib = "Subuh";
            nextTime = fajr; 
        }

        long minutes = calculateMinutes(now, nextTime);

        return new PhaseResult(current, nextWajib, nextTime, minutes);
    }

    private static boolean isBetween(LocalTime now, LocalTime start, LocalTime end) {
        if (start == null || end == null) return false;
        return !now.isBefore(start) && now.isBefore(end);
    }

    private static long calculateMinutes(LocalTime now, LocalTime target) {
        if (target == null) return 0;
        if (target.isBefore(now)) {
            return Duration.between(now, LocalTime.MAX).toMinutes() + 
                   Duration.between(LocalTime.MIN, target).toMinutes() + 1;
        } else {
            return Duration.between(now, target).toMinutes();
        }
    }
}
