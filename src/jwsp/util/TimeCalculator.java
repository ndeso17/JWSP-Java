package jwsp.util;

import jwsp.domain.prayer.PrayerSchedule;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class TimeCalculator {

    public static class PrayerTiming {
        public final String currentName;
        public final String nextName;
        public final LocalTime nextTime;
        public final long minutesToNext;

        public PrayerTiming(String currentName, String nextName, LocalTime nextTime, long minutesToNext) {
            this.currentName = currentName;
            this.nextName = nextName;
            this.nextTime = nextTime;
            this.minutesToNext = minutesToNext;
        }
    }

    private static final String[] PRAYER_NAMES = {"Imsak", "Subuh", "Dzuhur", "Ashar", "Maghrib", "Isya"};
    private static final String[] API_KEYS = {"imsak", "fajr", "dhuhr", "asr", "maghrib", "isya"};

    public static PrayerTiming calculate(LocalTime now, PrayerSchedule schedule) {
        if (schedule == null) return null;

        int nextIndex = -1;
        for (int i = 0; i < API_KEYS.length; i++) {
            LocalTime t = schedule.getTime(API_KEYS[i]);
            if (t != null && now.isBefore(t)) {
                nextIndex = i;
                break;
            }
        }

        String current;
        String next;
        LocalTime nextTime;

        if (nextIndex == -1) {
            // After Isya, next is Imsak (next day)
            current = "Isya";
            next = "Imsak";
            nextTime = schedule.getTime("imsak"); // This should ideally be next day's imsak
        } else {
            next = PRAYER_NAMES[nextIndex];
            nextTime = schedule.getTime(API_KEYS[nextIndex]);
            current = (nextIndex == 0) ? "Isya" : PRAYER_NAMES[nextIndex - 1];
        }

        long minutes = 0;
        if (nextTime != null) {
            if (nextTime.isBefore(now)) {
                // Next is tomorrow
                minutes = Duration.between(now, LocalTime.MAX).toMinutes() + Duration.between(LocalTime.MIN, nextTime).toMinutes();
            } else {
                minutes = Duration.between(now, nextTime).toMinutes();
            }
        }

        return new PrayerTiming(current, next, nextTime, minutes);
    }
}
