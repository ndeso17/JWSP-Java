package jwsp.ui.widget;

import jwsp.domain.prayer.PrayerTimeController;
import jwsp.domain.hijri.HijriService;
import jwsp.domain.ramadan.RamadanService;
import jwsp.theme.ThemeManager;
import jwsp.app.ApplicationLifecycleManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class WidgetWindow extends JWindow implements 
        PrayerTimeController.PrayerTimeListener, 
        ThemeManager.ThemeChangeListener {
    
    // Konstanta
    private static final int WIDGET_WIDTH = 300;
    private static final int WIDGET_HEIGHT = 195;
    private static final int MARGIN = 20;
    
    // Theme
    private ThemeManager.Theme theme;
    
    // Components
    private JPanel mainPanel;
    private JLabel lblTime;
    private JLabel lblCity;
    private JLabel lblPrayerStatus;
    private JLabel lblPrayerName;
    private JLabel lblCountdown;
    private JLabel lblHijri;
    private JLabel lblPuasa;
    private JLabel lblRamadhan;
    
    // Context menu
    private JPopupMenu contextMenu;
    
    // References
    private PrayerTimeController controller;
    private Timer clockTimer;
    private Point dragOffset;
    
    public WidgetWindow(PrayerTimeController controller) {
        super();
        
        this.controller = controller;
        this.theme = ThemeManager.getInstance().getCurrentTheme();
        
        initWindow();
        initComponents();
        initContextMenu();
        initDragging();
        positionWidget();
        startClockTimer();
        
        controller.addListener(this);
        ThemeManager.getInstance().addListener(this);
        
        updateDisplay();
    }
    
    private void initWindow() {
        setSize(WIDGET_WIDTH, WIDGET_HEIGHT);
        
        // Background transparan
        setBackground(new Color(0, 0, 0, 0));
        
        // TIDAK menggunakan AlwaysOnTop - widget bisa tertutup window lain
        setAlwaysOnTop(false);
        
        // Tidak focusable - tidak akan mengambil fokus dari aplikasi lain
        setFocusableWindowState(false);
        
        // Tidak auto-request fokus
        setAutoRequestFocus(false);
    }
    
    private void initComponents() {
        mainPanel = new WidgetPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(theme.padding, theme.padding + 4, theme.padding, theme.padding + 4));
        
        // Jam besar
        lblTime = new JLabel("00:00");
        lblTime.setFont(theme.getTimeFont());
        lblTime.setForeground(theme.textPrimary);
        lblTime.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(lblTime);
        
        // Kota
        lblCity = new JLabel("Jakarta (WIB)");
        lblCity.setFont(theme.getSmallFont());
        lblCity.setForeground(theme.textSecondary);
        lblCity.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(lblCity);

        mainPanel.add(Box.createVerticalStrut(6));

        // Hijri Date
        lblHijri = new JLabel("1 Ramadhan 1446 H");
        lblHijri.setFont(new Font(theme.fontFamily, Font.ITALIC, theme.normalSize - 2));
        lblHijri.setForeground(theme.accentOrange);
        lblHijri.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(lblHijri);

        // Ramadhan Countdown
        lblRamadhan = new JLabel("Menuju Ramadhan: --");
        lblRamadhan.setFont(theme.getSmallFont());
        lblRamadhan.setForeground(theme.textSecondary);
        lblRamadhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(lblRamadhan);
        
        mainPanel.add(Box.createVerticalStrut(8));

        // Divider
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(255, 255, 255, 40));
        mainPanel.add(sep);

        mainPanel.add(Box.createVerticalStrut(8));
        
        // Status sholat
        lblPrayerStatus = new JLabel("🕌 SEDANG");
        lblPrayerStatus.setFont(new Font(theme.fontFamily, Font.BOLD, theme.normalSize - 3));
        lblPrayerStatus.setForeground(theme.accent);
        lblPrayerStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(lblPrayerStatus);
        
        // Nama sholat
        lblPrayerName = new JLabel("Maghrib");
        lblPrayerName.setFont(theme.getTitleFont());
        lblPrayerName.setForeground(theme.textPrimary);
        lblPrayerName.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(lblPrayerName);
        
        // Countdown
        lblCountdown = new JLabel("⏭ Isya 19:03");
        lblCountdown.setFont(theme.getSmallFont());
        lblCountdown.setForeground(theme.textSecondary);
        lblCountdown.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(lblCountdown);
        
        // Status puasa (Bottom-most)
        lblPuasa = new JLabel("🌙 Puasa Ramadhan");
        lblPuasa.setFont(theme.getSmallFont());
        lblPuasa.setForeground(theme.accent);
        lblPuasa.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(lblPuasa);
        
        setContentPane(mainPanel);
    }
    
    private void initContextMenu() {
        contextMenu = new JPopupMenu();
        
        // Theme submenu
        JMenu themeMenu = new JMenu("Tema Widget");
        String[] themeNames = ThemeManager.getInstance().getThemeNames();
        for (String themeName : themeNames) {
            JMenuItem item = new JMenuItem(themeName);
            item.addActionListener(e -> ThemeManager.getInstance().setTheme(themeName));
            themeMenu.add(item);
        }
        contextMenu.add(themeMenu);
        
        contextMenu.addSeparator();
        
        // Bring to front
        JMenuItem bringToFront = new JMenuItem("Tampilkan di Depan");
        bringToFront.addActionListener(e -> toFront());
        contextMenu.add(bringToFront);
        
        // Reset position
        JMenuItem resetPos = new JMenuItem("Reset Posisi");
        resetPos.addActionListener(e -> positionWidget());
        contextMenu.add(resetPos);
        
        contextMenu.addSeparator();
        
        // Open settings
        JMenuItem openSettings = new JMenuItem("Buka Pengaturan");
        openSettings.addActionListener(e -> {
            ApplicationLifecycleManager.getInstance().onShowMainWindow();
        });
        contextMenu.add(openSettings);
        
        contextMenu.addSeparator();
        
        // Hide widget
        JMenuItem hideWidget = new JMenuItem("Sembunyikan Widget");
        hideWidget.addActionListener(e -> {
            setVisible(false);
            ApplicationLifecycleManager.getInstance().hideWidget();
        });
        contextMenu.add(hideWidget);
    }
    
    private void initDragging() {
        MouseHandler handler = new MouseHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    private void positionWidget() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(
            GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
        );
        
        int x = screenSize.width - WIDGET_WIDTH - MARGIN - insets.right;
        int y = screenSize.height - WIDGET_HEIGHT - MARGIN - insets.bottom;
        setLocation(x, y);
    }
    
    private void startClockTimer() {
        clockTimer = new Timer(1000, e -> {
            // Update Jam
            String time = controller.getCurrentTimeString();
            lblTime.setText(time);
            
            // Update Ramadhan Countdown live
            RamadanService svc = RamadanService.getInstance();
            if (svc.isRamadhanStarted()) {
                lblRamadhan.setVisible(false);
            } else {
                lblRamadhan.setText(svc.getCountdownText());
                lblRamadhan.setVisible(true);
            }
        });
        clockTimer.start();
    }
    
    private void updateDisplay() {
        PrayerTimeController.PrayerInfo current = controller.getCurrentPrayer();
        PrayerTimeController.PrayerInfo next = controller.getNextPrayer();
        
        lblCity.setText(controller.getKotaName() + " (" + controller.getZonaWaktu() + ")");
        lblHijri.setText(HijriService.getInstance().getHijriDateText());
        
        if (next.state == PrayerTimeController.PrayerState.UPCOMING) {
            lblPrayerStatus.setText("⏳ AKAN DATANG");
            lblPrayerStatus.setForeground(theme.accentOrange);
            lblPrayerName.setText(next.name + " " + next.getTimeString());
            lblCountdown.setText(next.getCountdownLabel());
        } else {
            lblPrayerStatus.setText("🕌 SEDANG");
            lblPrayerStatus.setForeground(theme.accent);
            lblPrayerName.setText(current.name);
            lblCountdown.setText("⏭ " + next.name + " " + next.getTimeString() + 
                " (" + next.getCountdownLabel() + ")");
        }
        
        String puasaStatus = controller.getPuasaStatus();
        if (puasaStatus.contains("Ramadhan")) {
            lblPuasa.setText("🌙 Puasa Ramadhan");
            lblPuasa.setForeground(theme.accentOrange);
            lblPuasa.setVisible(true);
        } else if (puasaStatus.contains("Sunnah")) {
            lblPuasa.setText("☪ " + puasaStatus);
            lblPuasa.setForeground(theme.accent);
            lblPuasa.setVisible(true);
        } else {
            lblPuasa.setVisible(false);
        }

        // Ramadan Countdown (Initial visibility)
        RamadanService svc = RamadanService.getInstance();
        if (svc.isRamadhanStarted()) {
            lblRamadhan.setVisible(false);
        } else {
            lblRamadhan.setText(svc.getCountdownText());
            lblRamadhan.setVisible(true);
        }
    }
    
    public void applyTheme(ThemeManager.Theme newTheme) {
        this.theme = newTheme;
        
        lblTime.setFont(theme.getTimeFont());
        lblTime.setForeground(theme.textPrimary);
        
        lblCity.setFont(theme.getSmallFont());
        lblCity.setForeground(theme.textSecondary);
        
        lblPrayerStatus.setFont(theme.getNormalFont());
        
        lblPrayerName.setFont(theme.getTitleFont());
        lblPrayerName.setForeground(theme.textPrimary);
        
        lblCountdown.setFont(theme.getSmallFont());
        lblCountdown.setForeground(theme.textSecondary);
        
        lblHijri.setFont(new Font(theme.fontFamily, Font.ITALIC, theme.normalSize - 2));
        lblHijri.setForeground(theme.accentOrange);
        
        lblPuasa.setFont(theme.getSmallFont());
        lblRamadhan.setFont(theme.getSmallFont());
        lblRamadhan.setForeground(theme.textSecondary);
        
        // Dynamic Border with extra bottom padding
        mainPanel.setBorder(new EmptyBorder(
            theme.padding, 
            theme.padding + 4, 
            theme.padding + 12, // User requested +8 min, let's do +12 for balance
            theme.padding + 4
        ));
        
        // Transparency
        setBackground(new Color(0,0,0,0)); 
        
        updateDisplay();
        repaint();
    }
    
    @Override
    public void onPrayerTimeUpdate(PrayerTimeController.PrayerInfo current, 
                                    PrayerTimeController.PrayerInfo next, 
                                    String puasaStatus) {
        SwingUtilities.invokeLater(this::updateDisplay);
    }
    
    @Override
    public void onThemeChanged(ThemeManager.Theme newTheme) {
        SwingUtilities.invokeLater(() -> applyTheme(newTheme));
    }
    
    public void cleanup() {
        if (clockTimer != null) {
            clockTimer.stop();
        }
        controller.removeListener(this);
        ThemeManager.getInstance().removeListener(this);
    }
    
    @Override
    public void dispose() {
        cleanup();
        super.dispose();
    }
    private class WidgetPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(theme.getBackgroundWithAlpha());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), theme.cornerRadius, theme.cornerRadius);
            if (theme.backgroundOpacity > 0.1f) {
                g2.setColor(new Color(255, 255, 255, 30));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, theme.cornerRadius, theme.cornerRadius);
            }
            g2.dispose();
        }
    }

    private class MouseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) dragOffset = e.getPoint();
        }
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                contextMenu.show(WidgetWindow.this, e.getX(), e.getY());
            }
        }
        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragOffset != null) {
                Point location = getLocation();
                setLocation(location.x + e.getX() - dragOffset.x, location.y + e.getY() - dragOffset.y);
            }
        }
    }
}
