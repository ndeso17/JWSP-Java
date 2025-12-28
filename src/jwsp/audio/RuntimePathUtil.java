package jwsp.audio;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RuntimePathUtil {

    private static final String AUDIO_DIR = "audio";
    
    public static String getBaseDir() {
        return System.getProperty("user.dir");
    }
    
    /**
     * Resolve path ke file audio
     * @param subPath subpath relatif dari folder audio/
     * @return File object yang menunjuk ke audio file
     */
    public static File getAudioFile(String subPath) {
        Path path = Paths.get(getBaseDir(), AUDIO_DIR, subPath);
        File file = path.toFile();
        
        if (file.exists()) {
            return file;
        }

        File devFile = Paths.get(getBaseDir(), "src", AUDIO_DIR, subPath).toFile();
        if (devFile.exists()) {
            return devFile;
        }
        
        return file;
    }
 
    public static File getAdzanDir() {
        return getAudioFile("adzan");
    }
    
    public static File getSirineDir() {
        return getAudioFile("sirine");
    }
    
    public static File getTarkhimDir() {
        return getAudioFile("tarkhim");
    }
}
