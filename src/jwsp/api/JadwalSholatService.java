package jwsp.api;

import jwsp.domain.prayer.PrayerSchedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class JadwalSholatService {

    private static JadwalSholatService instance;
    private final ApiService apiService;
    private PrayerSchedule currentSchedule;
    private final Map<String, PrayerSchedule> cache = new HashMap<>();

    private JadwalSholatService() {
        this.apiService = new ApiService();
    }

    public static synchronized JadwalSholatService getInstance() {
        if (instance == null) {
            instance = new JadwalSholatService();
        }
        return instance;
    }

    public PrayerSchedule getSchedule(String cityId, LocalDate date) {
        String key = cityId + "_" + date.toString();
        
        // 1. Try API Layer
        String json = apiService.fetchRawJson(cityId, date.toString());
        if (json != null) {
            OfflineCacheService.getInstance().save(cityId, date, json);
            PrayerSchedule ps = apiService.parsePrayerSchedule(cityId, date.toString(), json);
            if (ps != null) {
                cache.put(key, ps);
                currentSchedule = ps;
                return ps;
            }
        }

        // 2. Try Cache Layer
        json = OfflineCacheService.getInstance().load(cityId, date);
        if (json != null) {
            PrayerSchedule ps = apiService.parsePrayerSchedule(cityId, date.toString(), json);
            if (ps != null) {
                cache.put(key, ps);
                currentSchedule = ps;
                return ps;
            }
        }

        return null; // Layer 3 (Manual) will be handled by JadwalSholat
    }

    public PrayerSchedule getCurrentSchedule() {
        return currentSchedule;
    }
}
