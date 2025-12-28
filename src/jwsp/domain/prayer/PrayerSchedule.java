package jwsp.domain.prayer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class PrayerSchedule {
    private final Map<String, LocalTime> times = new HashMap<>();
    private final String date;
    private final String cityId;

    public PrayerSchedule(String cityId, String date) {
        this.cityId = cityId;
        this.date = date;
    }

    public void setTime(String name, String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return;
        try {
            // API usually returns HH:mm
            String key = name.toLowerCase();
            if (key.equals("subuh")) key = "fajr";
            if (key.equals("dzuhur")) key = "dhuhr";
            if (key.equals("terbit")) key = "sunrise";
            
            times.put(key, LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm")));
        } catch (Exception e) {
            System.err.println("Error parsing time for " + name + ": " + timeStr);
        }
    }

    public LocalTime getTime(String name) {
        return times.get(name.toLowerCase());
    }

    public String getDate() {
        return date;
    }

    public String getCityId() {
        return cityId;
    }

    @Override
    public String toString() {
        return "PrayerSchedule{" +
                "cityId='" + cityId + '\'' +
                ", date='" + date + '\'' +
                ", times=" + times +
                '}';
    }
}
