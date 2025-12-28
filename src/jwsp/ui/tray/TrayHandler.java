package jwsp.ui.tray;

import jwsp.domain.prayer.PrayerTimeController;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class TrayHandler implements PrayerTimeController.PrayerTimeListener {
    
    // System tray components
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private PopupMenu popupMenu;
    
    // Live info menu item
    private MenuItem nextPrayerItem;
    
    // Controller & Callback
    private PrayerTimeController controller;
    
    public interface TrayCallback {
        void onShowMainWindow();
        void onToggleWidget();
        void onToggleTray();
        void onExit();
    }
    
    private TrayCallback callback;
    
    // State
    private boolean isWidgetEnabled = false;
    
    public TrayHandler(PrayerTimeController controller, TrayCallback callback) {
        this.controller = controller;
        this.callback = callback;
    }
    
    public boolean initialize() {
        if (!SystemTray.isSupported()) {
            return false;
        }
        
        systemTray = SystemTray.getSystemTray();
        
        // Buat ikon minimal (hampir transparan)
        Image icon = createMinimalIcon();
        
        // Buat popup menu dengan info sholat
        popupMenu = createPopupMenu();
        
        // Buat tray icon
        trayIcon = new TrayIcon(icon, getTooltipText(), popupMenu);
        trayIcon.setImageAutoSize(true);
        
        // Double click untuk show main window
        trayIcon.addActionListener(e -> {
            if (callback != null) {
                callback.onShowMainWindow();
            }
        });
        
        try {
            systemTray.add(trayIcon);
            
            // Register sebagai listener
            controller.addListener(this);
            
            // Initial update
            updateNextPrayerInfo();
            
            return true;
        } catch (AWTException e) {
            return false;
        }
    }

    private Image createMinimalIcon() {
        int size = 16;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Ikon masjid minimalis
        // Background: lingkaran hijau gelap
        g2.setColor(new Color(45, 95, 65)); // Hijau tua subtle
        g2.fillOval(0, 0, size, size);
        
        // Kubah putih kecil
        g2.setColor(new Color(220, 220, 220));
        g2.fillArc(4, 5, 8, 6, 0, 180);
        
        // Badan masjid
        g2.fillRect(5, 9, 6, 4);
        
        g2.dispose();
        return image;
    }
    
    private PopupMenu createPopupMenu() {
        PopupMenu popup = new PopupMenu();
        
        // === MENU ITEM INFORMASI SHOLAT (LIVE UPDATE) ===
        nextPrayerItem = new MenuItem("🕌 Memuat info...");
        nextPrayerItem.setEnabled(false); // Tidak bisa diklik
        popup.add(nextPrayerItem);
        
        popup.addSeparator();
        
        // === KONTROL ===
        
        // Toggle Widget
        CheckboxMenuItem widgetItem = new CheckboxMenuItem("Widget Desktop", isWidgetEnabled);
        widgetItem.addItemListener(e -> {
            isWidgetEnabled = widgetItem.getState();
            if (callback != null) callback.onToggleWidget();
        });
        popup.add(widgetItem);
        
        // Buka Pengaturan
        MenuItem settingsItem = new MenuItem("Buka Pengaturan");
        settingsItem.addActionListener(e -> {
            if (callback != null) callback.onShowMainWindow();
        });
        popup.add(settingsItem);
        
        popup.addSeparator();
        
        // Keluar
        MenuItem exitItem = new MenuItem("Keluar");
        exitItem.addActionListener(e -> {
            if (callback != null) callback.onExit();
        });
        popup.add(exitItem);
        
        return popup;
    }
    
    /**
     * Update informasi sholat di menu item
     */
    public void updateNextPrayerInfo() {
        if (controller == null || nextPrayerItem == null) return;
        
        PrayerTimeController.PrayerInfo current = controller.getCurrentPrayer();
        PrayerTimeController.PrayerInfo next = controller.getNextPrayer();
        
        String label;
        
        if (next.state == PrayerTimeController.PrayerState.UPCOMING) {
            // Dalam 20 menit, tampilkan countdown
            label = "⏳ " + next.name + " • " + next.getTimeString() + 
                    " (" + next.minutesRemaining + " menit)";
        } else {
            // Tampilkan waktu sholat berikutnya
            label = "🕌 " + current.name + " (sedang) | ⏭ " + 
                    next.name + " " + next.getTimeString();
        }
        
        // Update menu item
        nextPrayerItem.setLabel(label);
        
        // Update tooltip
        updateTooltip();
    }
    
    private String getTooltipText() {
        if (controller == null) return "Jadwal Sholat";
        
        StringBuilder sb = new StringBuilder();
        sb.append("Jadwal Sholat - ").append(controller.getKotaName()).append("\n");
        
        PrayerTimeController.PrayerInfo current = controller.getCurrentPrayer();
        PrayerTimeController.PrayerInfo next = controller.getNextPrayer();
        
        if (next.state == PrayerTimeController.PrayerState.UPCOMING) {
            sb.append("⏳ ").append(next.name).append(" dalam ")
              .append(next.minutesRemaining).append(" menit\n");
        } else {
            sb.append("🕌 ").append(current.name).append(" (sedang)\n");
        }
        
        sb.append("⏭ ").append(next.name).append(" ")
          .append(next.getTimeString());
        
        // Limit tooltip (max ~127 chars)
        String tooltip = sb.toString();
        if (tooltip.length() > 120) {
            tooltip = tooltip.substring(0, 117) + "...";
        }
        
        return tooltip;
    }

    private void updateTooltip() {
        if (trayIcon != null) {
            trayIcon.setToolTip(getTooltipText());
        }
    }
    
    @Override
    public void onPrayerTimeUpdate(PrayerTimeController.PrayerInfo current, 
                                    PrayerTimeController.PrayerInfo next, 
                                    String puasaStatus) {
        // Update menu item dan tooltip
        SwingUtilities.invokeLater(this::updateNextPrayerInfo);
    }
    
    /**
     * Tampilkan notifikasi
     */
    public void showNotification(String title, String message, TrayIcon.MessageType type) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, type);
        }
    }
    
    public void setWidgetEnabled(boolean enabled) {
        this.isWidgetEnabled = enabled;
        updateWidgetCheckbox();
    }
    
    public void setTrayEnabled(boolean enabled) {
        // Tray selalu aktif
    }
    
    private void updateWidgetCheckbox() {
        if (popupMenu == null) return;
        
        for (int i = 0; i < popupMenu.getItemCount(); i++) {
            MenuItem item = popupMenu.getItem(i);
            if (item instanceof CheckboxMenuItem) {
                CheckboxMenuItem checkbox = (CheckboxMenuItem) item;
                if (checkbox.getLabel().contains("Widget")) {
                    checkbox.setState(isWidgetEnabled);
                }
            }
        }
    }
    
    public void remove() {
        if (systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon);
            controller.removeListener(this);
        }
    }
    
    public boolean isActive() {
        return trayIcon != null;
    }
    
    public static boolean isSystemTraySupported() {
        return SystemTray.isSupported();
    }
}
