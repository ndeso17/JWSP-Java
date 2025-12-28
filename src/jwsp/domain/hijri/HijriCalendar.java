package jwsp.domain.hijri;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class HijriCalendar {

    public static class HijriDate {
        public final int day;
        public final int month;
        public final int year;
        public final String monthName;

        public HijriDate(int day, int month, int year) {
            this.day = day;
            this.month = month;
            this.year = year;
            this.monthName = getHijriMonthName(month);
        }

        @Override
        public String toString() {
            return String.format("%d %s %d H", day, monthName, year);
        }
    }

    private static final String[] MONTH_NAMES = {
        "Muharram", "Safar", "Rabi'ul Awwal", "Rabi'ul Akhir",
        "Jumadil Awwal", "Jumadil Akhir", "Rajab", "Sya'ban",
        "Ramadhan", "Syawwal", "Dzulqa'dah", "Dzulhijjah"
    };

    public static String getHijriMonthName(int month) {
        if (month < 1 || month > 12) return "Unknown";
        return MONTH_NAMES[month - 1];
    }

    public static HijriDate fromLocalDate(LocalDate date) {

        double jd = getJulianDay(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        
        long l = (long) jd - 1948440 + 10632;
        long n = (long) Math.floor((l - 1) / 10631.0);
        l = l - 10631 * n + 354;
        long j = (long) (Math.floor((10985 - l) / 5316.0) * Math.floor((50 * l) / 17719.0) + Math.floor(l / 5670.0) * Math.floor((43 * l) / 15238.0));
        l = l - (long) (Math.floor((30 - j) / 15.0) * Math.floor((17719 * j) / 50.0) + Math.floor(j / 16.0) * Math.floor((15238 * j) / 43.0)) + 29;
        
        int m = (int) Math.floor((24 * l) / 709.0);
        int d = (int) (l - Math.floor((709 * m) / 24.0));
        int y = (int) (30 * n + j - 30);
        
        return new HijriDate(d, m, y);
    }

    private static double getJulianDay(int year, int month, int day) {
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
}
