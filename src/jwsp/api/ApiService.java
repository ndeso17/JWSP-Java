package jwsp.api;

import jwsp.domain.prayer.PrayerSchedule;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiService {

    private static final String BASE_URL_CUACA = "https://weather-api-tau-six.vercel.app";
    private static final String BASE_URL_JADWAL = "https://api.myquran.com/v2/sholat/jadwal";

    /**
     * Fetch prayer schedule for a specific city and date
     */
    public PrayerSchedule fetchPrayerSchedule(String cityId, String date) {
        String json = fetchRawJson(cityId, date);
        if (json == null) return null;
        return parsePrayerSchedule(cityId, date, json);
    }

    public String fetchRawJson(String cityId, String date) {
        String urlString = String.format("%s/%s/%s", BASE_URL_JADWAL, cityId, date);
        return makeHttpRequest(urlString);
    }

    public PrayerSchedule parsePrayerSchedule(String cityId, String date, String json) {
        // Simple parsing for "jadwal": { ... }
        String jadwalPart = findJsonSection(json, "\"jadwal\":");
        if (jadwalPart == null) return null;

        PrayerSchedule schedule = new PrayerSchedule(cityId, date);
        schedule.setTime("imsak", findJsonValue(jadwalPart, "\"imsak\""));
        schedule.setTime("fajr", findJsonValue(jadwalPart, "\"subuh\""));
        schedule.setTime("sunrise", findJsonValue(jadwalPart, "\"terbit\""));
        schedule.setTime("dhuha", findJsonValue(jadwalPart, "\"dhuha\""));
        schedule.setTime("dhuhr", findJsonValue(jadwalPart, "\"dzuhur\""));
        schedule.setTime("asr", findJsonValue(jadwalPart, "\"ashar\""));
        schedule.setTime("maghrib", findJsonValue(jadwalPart, "\"maghrib\""));
        schedule.setTime("isya", findJsonValue(jadwalPart, "\"isya\""));

        return schedule;
    }

    /**
     * Fetch provinces from the weather API
     */
    public List<Map<String, String>> fetchProvinces() {
        String urlString = BASE_URL_CUACA + "/cuaca/getIdProvince";
        String json = makeHttpRequest(urlString);
        return parseJsonList(json);
    }

    /**
     * Fetch cities/kabupaten by province ID
     */
    public List<Map<String, String>> fetchCities(String provinceId) {
        String urlString = BASE_URL_CUACA + "/cuaca/getIdKabupaten/" + provinceId;
        String json = makeHttpRequest(urlString);
        return parseJsonList(json);
    }

    private String makeHttpRequest(String urlString) {
        HttpURLConnection conn = null;
        try {
            URL url = URI.create(urlString).toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != 200) {
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            return content.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // --- Primitive JSON parsing helpers ---

    private String findJsonSection(String json, String key) {
        int index = json.indexOf(key);
        if (index == -1) return null;
        
        int start = json.indexOf("{", index);
        if (start == -1) return null;
        
        int end = findClosingBrace(json, start);
        if (end == -1) return null;
        
        return json.substring(start, end + 1);
    }

    private int findClosingBrace(String json, int start) {
        int depth = 0;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private String findJsonValue(String json, String key) {
        int index = json.indexOf(key);
        if (index == -1) return null;
        
        int colon = json.indexOf(":", index);
        if (colon == -1) return null;
        
        int startQuote = json.indexOf("\"", colon);
        if (startQuote == -1) return null;
        
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote == -1) return null;
        
        return json.substring(startQuote + 1, endQuote);
    }

    private List<Map<String, String>> parseJsonList(String json) {
        List<Map<String, String>> result = new ArrayList<>();
        if (json == null) return result;

        // Assume format is like [{...}, {...}]
        int lastPos = 0;
        while ((lastPos = json.indexOf("{", lastPos)) != -1) {
            int end = findClosingBrace(json, lastPos);
            if (end == -1) break;
            
            String objectJson = json.substring(lastPos, end + 1);
            Map<String, String> map = new HashMap<>();
            
            // Very naive loop to extract key-value pairs
            // This assumes simple objects with string values
            int pairPos = 0;
            while ((pairPos = objectJson.indexOf("\"", pairPos)) != -1) {
                int keyEnd = objectJson.indexOf("\"", pairPos + 1);
                if (keyEnd == -1) break;
                String k = objectJson.substring(pairPos + 1, keyEnd);
                
                String v = findJsonValue(objectJson, "\"" + k + "\"");
                if (v != null) {
                    map.put(k, v);
                }
                pairPos = objectJson.indexOf(",", keyEnd);
                if (pairPos == -1) break;
            }
            
            result.add(map);
            lastPos = end + 1;
        }

        return result;
    }
}
