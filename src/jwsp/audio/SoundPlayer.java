package jwsp.audio;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundPlayer {
    public static final String FILE_SIRINE = "Sirine Buka Puasa Dan Imsak.wav";
    public static final String FILE_TARKHIM_ADZAN = "tarkhim_sebelum_adzan.wav";
    public static final String FILE_TARKHIM_BUKA = "tarkhim_sebelum_buka_puasa.wav";
    
    // Commands
    private static final String CMD_FFPLAY = "ffplay";
    private static final String CMD_APLAY = "aplay";
    
    private final ExecutorService executor;
    private final Set<Process> activeProcesses = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    private String preferredPlayer = null;
    
    public interface PlayCallback {
        void onFinished();
    }
    
    public SoundPlayer() {
        this.executor = Executors.newCachedThreadPool();
        detectPlayer();
    }
    
    private void detectPlayer() {
        if (checkCommand(CMD_FFPLAY)) {
            preferredPlayer = CMD_FFPLAY;
        } else if (checkCommand(CMD_APLAY)) {
            preferredPlayer = CMD_APLAY;
        } else {
            System.err.println("WARNING: No external audio player found (ffplay/aplay). Audio will not play.");
        }
        System.out.println("DEBUG: Detected Audio Player: " + preferredPlayer);
    }
    
    private boolean checkCommand(String cmd) {
        try {
            Process p = new ProcessBuilder(cmd, "--version").start();
            p.waitFor();
            return true;
        } catch (Exception e) {
            return false;
        }
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
        for (Process p : activeProcesses) {
            if (p.isAlive()) {
                p.destroy(); 
                 // Force kill if necessary after delay?
                 // For now, destroy is usually SIGTERM
            }
        }
        activeProcesses.clear();
    }
    
    public void shutdown() {
        stopAll();
        executor.shutdownNow();
    }
    
    private void playFile(File file, PlayCallback callback) {
        if (preferredPlayer == null) {
            if (callback != null) callback.onFinished();
            return;
        }
        
        executor.submit(() -> {
            Process process = null;
            try {
                ProcessBuilder pb;
                if (CMD_FFPLAY.equals(preferredPlayer)) {
                    // ffplay -nodisp -autoexit -hide_banner <file> (removed -loglevel quiet for debug)
                    pb = new ProcessBuilder(CMD_FFPLAY, "-nodisp", "-autoexit", "-hide_banner", file.getAbsolutePath());
                } else {
                    // aplay <file> (removed -q for debug)
                    pb = new ProcessBuilder(CMD_APLAY, file.getAbsolutePath());
                }
                
                System.out.println("DEBUG: Starting audio process: " + String.join(" ", pb.command()));
                
                process = pb.start();
                activeProcesses.add(process);
                
                // Wait for process to finish
                int exitCode = process.waitFor();
                
                if (exitCode != 0) {
                     System.err.println("Audio player exited with code: " + exitCode);
                }
                
            } catch (Exception e) {
                System.err.println("Error executing external audio player: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (process != null) {
                    try {
                        // Capture standard error if process failed
                         java.io.InputStream stderr = process.getErrorStream();
                         java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(stderr));
                         String line;
                         while ((line = reader.readLine()) != null) {
                             System.err.println("[AudioPlayer Stderr] " + line);
                         }
                    } catch (Exception ex) {}
                    
                    activeProcesses.remove(process);
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
