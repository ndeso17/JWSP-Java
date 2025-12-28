package jwsp.app;

import jwsp.domain.prayer.PrayerTimeController;
import jwsp.domain.prayer.JadwalSholat;
import jwsp.domain.hijri.HijriService;
import jwsp.domain.ramadan.RamadanService;
import jwsp.data.wilayah.Wilayah;
import jwsp.data.wilayah.WilayahData;
import jwsp.data.wilayah.DataKota;
import jwsp.data.JadwalPuasa;
import jwsp.config.UserPreferences;
import jwsp.audio.SoundPlayer;
import jwsp.theme.ThemeManager;
import jwsp.ui.mainframe.MainFrame;
import jwsp.ui.widget.WidgetWindow;
import jwsp.ui.tray.TrayHandler;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Timer;

public class ApplicationLifecycleManager implements TrayHandler.TrayCallback {
    
    private static ApplicationLifecycleManager instance;
    
    private PrayerTimeController controller;
    private SoundPlayer soundPlayer;
    private ThemeManager themeManager;
    private UserPreferences prefs;
    
    private MainFrame mainFrame;
    private WidgetWindow widgetWindow;
    private TrayHandler trayHandler;
    
    private Timer updateTimer;
    private static final int UPDATE_INTERVAL = 1000;

    private String lastProcessedMinute = "";

    private String selectedAdzanFile;
    
    private ApplicationLifecycleManager() {
    }
    
    public static synchronized ApplicationLifecycleManager getInstance() {
        if (instance == null) instance = new ApplicationLifecycleManager();
        return instance;
    }
    
    public void initialize() {
        
        prefs = UserPreferences.getInstance();
        soundPlayer = new SoundPlayer();
        
        try {
            java.net.URL iconUrl = getClass().getResource("/jwsp/app/resources/icon.png");
            if (iconUrl != null) {
                Image icon = new ImageIcon(iconUrl).getImage();
                if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
                    Taskbar.getTaskbar().setIconImage(icon);
                }
            }
        } catch (Exception e) {}

        selectedAdzanFile = prefs.getAdzan();
        themeManager = ThemeManager.getInstance();
        
        String city = prefs.getCity();
        Wilayah w = WilayahData.getInstance().getByNama(city);
        if (w == null) w = WilayahData.getInstance().getDefaultWilayah("DKI Jakarta");
        
        JadwalSholat jadwal = new JadwalSholat(w);
        controller = new PrayerTimeController(jadwal, new JadwalPuasa(jadwal));
        
        mainFrame = new MainFrame();
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        
        mainFrame.setVisible(true);

        initializeTray();
        if (prefs.isWidgetEnabled()) showWidget();
        
        startUpdateTimer();

        final Wilayah finalW = w;
        new Thread(() -> {
            try {
                JadwalSholat apiJadwal = new JadwalSholat(finalW);
                SwingUtilities.invokeLater(() -> {
                    controller.setJadwalSholat(apiJadwal);
                    controller.setJadwalPuasa(new JadwalPuasa(apiJadwal));
                    if (mainFrame != null) mainFrame.updateDisplay();
                });
            } catch (Exception e) {
            }
        }).start();
    }
    
    private void startUpdateTimer() {
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TickTask(), 0, UPDATE_INTERVAL);
    }

    private class TickTask extends java.util.TimerTask {
        @Override
        public void run() {
            tick();
        }
    }
    
    private void tick() {
        HijriService.getInstance().refresh();
        
        controller.update();
        
        JadwalSholat jadwal = controller.getJadwalSholat();
        LocalTime now = LocalTime.now(DataKota.getZonaWaktu(jadwal.getNamaKota()));
        String currentMinuteStr = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        
        if (currentMinuteStr.equals(lastProcessedMinute)) {
            return;
        }
        lastProcessedMinute = currentMinuteStr;
        
        checkTarkhim(jadwal, currentMinuteStr, now);
        
        checkAdzan(jadwal, currentMinuteStr, now);

        checkImsak(jadwal, currentMinuteStr);
    }
    
    // === LOGIC EVENT ===
    
    private void checkTarkhim(JadwalSholat jadwal, String currentTime, LocalTime now) {
        // Cek H-10 Menit untuk setiap waktu sholat
        // Logic: Iterate 5 waktu, kurangi 10 menit, cek equals
        
        for (int i = 0; i < 5; i++) {
            LocalTime sholatTime = jadwal.getWaktu(i);
            String tarkhimTime = sholatTime.minusMinutes(10).format(DateTimeFormatter.ofPattern("HH:mm"));
            
            if (currentTime.equals(tarkhimTime)) {
                String namaSholat = JadwalSholat.NAMA_SHOLAT[i];
                
                // Show Notif
                showTrayNotification("Menuju " + namaSholat, "10 Menit lagi masuk waktu " + namaSholat);
                
                // Play Audio
                if (i == 0) { // Subuh
                    soundPlayer.play(SoundPlayer.FILE_TARKHIM_ADZAN, "tarkhim");
                } else if (i == 3) { // Maghrib
                    // Cek puasa
                    if (controller.getJadwalPuasa().isPuasaRamadhan()) {
                        soundPlayer.play(SoundPlayer.FILE_TARKHIM_BUKA, "tarkhim");
                    } else {
                        soundPlayer.play(SoundPlayer.FILE_TARKHIM_ADZAN, "tarkhim");
                    }
                } else {
                    // Waktu lain trigger tarkhim standar
                     soundPlayer.play(SoundPlayer.FILE_TARKHIM_ADZAN, "tarkhim");
                }
            }
        }
    }
    
    private void checkAdzan(JadwalSholat jadwal, String currentTime, LocalTime now) {
        for (int i = 0; i < 5; i++) {
            String sholatTime = jadwal.getWaktuString(i);
            
            if (currentTime.equals(sholatTime)) {
                String namaSholat = JadwalSholat.NAMA_SHOLAT[i];
                
                soundPlayer.stopAll();
                
                showTrayNotification("Waktu " + namaSholat, "Telah masuk waktu " + namaSholat);
                
                // Special Case: Maghrib + Ramadhan
                if (i == 3 && controller.getJadwalPuasa().isPuasaRamadhan()) {
                     triggerMaghribRamadhanParams();
                } else {
                     // Normal Adzan
                     playAdzanStandard();
                }
            }
        }
    }
    
    private void checkImsak(JadwalSholat jadwal, String currentTime) {
        if (controller.getJadwalPuasa().isPuasaRamadhan()) {
            if (currentTime.equals(jadwal.getWaktuImsak())) {
                showTrayNotification("IMSAK", "Waktu Imsak telah tiba");
                soundPlayer.play(SoundPlayer.FILE_SIRINE, "sirine");
            }
        }
    }
    
    // === AUDIO TRIGGERS ===
    
    private void playAdzanStandard() {
        if (selectedAdzanFile != null && !selectedAdzanFile.equals("Tidak ada")) {
            soundPlayer.play(selectedAdzanFile, "adzan");
        }
    }
    
    private void triggerMaghribRamadhanParams() {
        soundPlayer.play(SoundPlayer.FILE_SIRINE, "sirine", () -> {
            // Callback saat sirine selesai -> Play Adzan
            playAdzanStandard();
        });
    }
    
    // === Standard Lifecycle Methods ===
    
    private void initializeTray() {
        if (TrayHandler.isSystemTraySupported()) {
            trayHandler = new TrayHandler(controller, this);
            trayHandler.initialize();
        }
    }
    
    public void showWidget() {
        if (widgetWindow == null) widgetWindow = new WidgetWindow(controller);
        widgetWindow.setVisible(true);
        prefs.setWidgetEnabled(true);
        if (trayHandler != null) trayHandler.setWidgetEnabled(true);
    }
    
    public void hideWidget() {
        if (widgetWindow != null) widgetWindow.setVisible(false);
        prefs.setWidgetEnabled(false);
        if (trayHandler != null) trayHandler.setWidgetEnabled(false);
    }
    
    private void showTrayNotification(String title, String message) {
        if (trayHandler != null) trayHandler.showNotification(title, message, TrayIcon.MessageType.INFO);
    }
    
    public void shutdown() {
        if (updateTimer != null) updateTimer.cancel();
        if (soundPlayer != null) soundPlayer.shutdown();
        if (mainFrame != null) mainFrame.dispose();
        if (trayHandler != null) trayHandler.remove();
        System.exit(0);
    }
    
    public PrayerTimeController getController() { return controller; }
    public SoundPlayer getSoundPlayer() { return soundPlayer; }
    public ThemeManager getThemeManager() { return themeManager; }
    public String getSelectedAdzanFile() { return selectedAdzanFile; }
    
    public void setSelectedAdzanFile(String adzan) {
        this.selectedAdzanFile = adzan;
        prefs.setAdzan(adzan);
    }
    
    public void updateCity(String province, String city) {
        prefs.setProvince(province);
        prefs.setCity(city);
        JadwalSholat js = new JadwalSholat(city);
        controller.setJadwalSholat(js);
        controller.setJadwalPuasa(new JadwalPuasa(js));
        lastProcessedMinute = ""; // Reset lock biar update langsung terasa jika menit sama
    }
    
    public void updateWilayah(Wilayah w) {
        prefs.setProvince(w.getProvinsi());
        prefs.setCity(w.getNama());
        JadwalSholat js = new JadwalSholat(w);
        controller.setJadwalSholat(js);
        controller.setJadwalPuasa(new JadwalPuasa(js));
        lastProcessedMinute = "";
        controller.update();
    }
    
    // TrayCallback
    @Override 
    public void onShowMainWindow() { 
        if (mainFrame != null) {
            SwingUtilities.invokeLater(() -> {
                mainFrame.setVisible(true);
                mainFrame.toFront();
                mainFrame.repaint();
            });
        }
    }
    
    @Override public void onToggleWidget() { if (prefs.isWidgetEnabled()) hideWidget(); else showWidget(); }
    @Override public void onToggleTray() {}
    @Override public void onExit() { shutdown(); }
    public void setMainFrame(MainFrame f) { this.mainFrame = f; }
}
