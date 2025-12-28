package jwsp.audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundPlayer {
    public static final String FILE_SIRINE = "Sirine Buka Puasa Dan Imsak.wav";
    public static final String FILE_TARKHIM_ADZAN = "tarkhim_sebelum_adzan.wav";
    public static final String FILE_TARKHIM_BUKA = "tarkhim_sebelum_buka_puasa.wav";
    
    private final ExecutorService executor;
    
    private final Set<Clip> activeClips = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    public interface PlayCallback {
        void onFinished();
    }
    
    public SoundPlayer() {
        this.executor = Executors.newCachedThreadPool();
    }
    
    public void play(String filename, String category) {
        play(filename, category, null);
    }
    
    public void play(String filename, String category, PlayCallback callback) {
        File file = resolveFile(filename, category);
        if (file == null) {
            if (callback != null) callback.onFinished();
            return;
        }
        
        playFile(file, callback);
    }
    
    public void stopAll() {
        for (Clip clip : activeClips) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.close(); 
        }
        activeClips.clear();
    }
    
    public void shutdown() {
        stopAll();
        executor.shutdownNow();
    }
    
    private void playFile(File file, PlayCallback callback) {
        executor.submit(() -> {
            Clip clip = null;
            AudioInputStream audioStream = null;
            
            try {
                audioStream = AudioSystem.getAudioInputStream(file);
                AudioFormat format = audioStream.getFormat();
                DataLine.Info info = new DataLine.Info(Clip.class, format);

                if (!AudioSystem.isLineSupported(info)) {
                    if (callback != null) callback.onFinished();
                    return;
                }
                
                clip = (Clip) AudioSystem.getLine(info);
                
                final Clip finalClip = clip;
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        finalClip.close();
                        activeClips.remove(finalClip);
                        if (callback != null) callback.onFinished();
                    }
                });
                
                clip.open(audioStream);
                activeClips.add(clip);
                
                clip.start();
            } catch (Exception e) {
                if (clip != null) {
                    clip.close();
                    activeClips.remove(clip);
                }
                if (callback != null) callback.onFinished();
            }
        });
    }
    
    private File resolveFile(String filename, String category) {
        File dir;
        switch (category) {
            case "adzan": dir = RuntimePathUtil.getAdzanDir(); break;
            case "sirine": dir = RuntimePathUtil.getSirineDir(); break;
            case "tarkhim": dir = RuntimePathUtil.getTarkhimDir(); break;
            default: return null;
        }
        File file = new File(dir, filename);
        return file.exists() ? file : null;
    }
    
    public static String[] getAvailableAdzanFiles() {
        return SoundPlayerHelper.scanAdzanFiles();
    }
    
    private static class SoundPlayerHelper {
        static String[] scanAdzanFiles() {
            try {
                File dir = RuntimePathUtil.getAdzanDir();
                if (!dir.exists()) return new String[0];
                
                java.util.List<String> files = new java.util.ArrayList<>();
                File[] list = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".wav"));
                
                if (list != null) {
                    for (File f : list) files.add(f.getName());
                }
                java.util.Collections.sort(files);
                return files.toArray(new String[0]);
            } catch (Exception e) {
                return new String[0];
            }
        }
    }
}
