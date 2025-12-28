package jwsp.api;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;

public class OfflineCacheService {

    private static OfflineCacheService instance;
    private static final String CACHE_DIR = "cache/prayer";

    private OfflineCacheService() {
        try {
            Files.createDirectories(Paths.get(CACHE_DIR));
        } catch (IOException e) {
            // Error creating cache folder
        }
    }

    public static synchronized OfflineCacheService getInstance() {
        if (instance == null) {
            instance = new OfflineCacheService();
        }
        return instance;
    }

    public void save(String cityId, LocalDate date, String json) {
        if (json == null || json.isEmpty()) return;
        
        File file = getCacheFile(cityId, date);
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.print(json);
        } catch (IOException e) {
            // Save failed
        }
    }

    public String load(String cityId, LocalDate date) {
        File file = getCacheFile(cityId, date);
        if (!file.exists()) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private File getCacheFile(String cityId, LocalDate date) {
        String fileName = String.format("prayer_%s_%s.json", cityId, date.toString());
        return new File(CACHE_DIR, fileName);
    }
}
