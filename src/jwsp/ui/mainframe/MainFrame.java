package jwsp.ui.mainframe;

import jwsp.app.ApplicationLifecycleManager;
import jwsp.config.UserPreferences;
import jwsp.data.wilayah.WilayahData;
import jwsp.data.wilayah.Wilayah;
import jwsp.data.wilayah.TipeWilayah;
import jwsp.data.JadwalPuasa;
import jwsp.domain.prayer.PrayerTimeController;
import jwsp.domain.prayer.JadwalSholat;
import jwsp.domain.hijri.HijriService;
import jwsp.domain.ramadan.RamadanService;
import jwsp.theme.ThemeManager;
import jwsp.audio.SoundPlayer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.Timer;

public class MainFrame extends JFrame implements 
    PrayerTimeController.PrayerTimeListener,
    ThemeManager.ThemeChangeListener {
    
    // === DARK THEME COLORS ===
    private static final Color BG_DARK = new Color(30, 32, 40);
    private static final Color BG_PANEL = new Color(42, 45, 55);
    private static final Color BG_INPUT = new Color(55, 58, 68);
    private static final Color TEXT_PRIMARY = new Color(240, 242, 245);
    private static final Color TEXT_SECONDARY = new Color(170, 175, 185);
    private static final Color TEXT_MUTED = new Color(120, 125, 135);
    private static final Color ACCENT_GREEN = new Color(76, 217, 100);
    private static final Color ACCENT_ORANGE = new Color(255, 159, 64);
    private static final Color ACCENT_BLUE = new Color(90, 200, 250);
    private static final Color BORDER_COLOR = new Color(65, 70, 80);
    
    // Konstanta
    private static final int WINDOW_WIDTH = 640;
    private static final int WINDOW_HEIGHT = 680;
    private static final int PADDING = 18;
    
    // GUI Components
    private JComboBox<String> comboProvinsi;
    private JComboBox<String> comboTipe;
    private JComboBox<String> comboWilayah;
    private JComboBox<String> comboAdzan;
    private JComboBox<String> comboTheme;
    private JButton btnRefresh;
    private JLabel lblTanggal;
    private JLabel lblJamSekarang;
    private JLabel lblZonaWaktu;
    private JLabel lblStatusNotif;
    private JLabel lblPlayerStatus;
    private JLabel lblWilayahInfo;
    private JPanel mainPanel;
    
    private JCheckBox chkWidget;
    private JCheckBox chkTray;
    
    private JLabel lblSubuh, lblDzuhur, lblAshar, lblMaghrib, lblIsya;
    private JLabel lblSunrise, lblDhuha;
    private JLabel lblStatusPuasa, lblImsak, lblBukaPuasa, lblInfoPuasa;
    private JLabel lblHijri, lblCountdown;
    
    // References
    private ApplicationLifecycleManager lifecycle;
    private UserPreferences prefs;
    private WilayahData wilayahData;
    private Timer clockTimer;
    private boolean isUpdatingCombos = false;
    
    // Current selection
    private Wilayah currentWilayah;
    
    public MainFrame() {
        lifecycle = ApplicationLifecycleManager.getInstance();
        prefs = UserPreferences.getInstance();
        wilayahData = WilayahData.getInstance();
        
        loadIcon();
        
        initComponents();
        loadPreferences();
        updateDisplay();
        startClockTimer();
        
        lifecycle.getController().addListener(this);
        ThemeManager.getInstance().addListener(this);
        lifecycle.setMainFrame(this);
        
        // Print stats
        wilayahData.printStats();
    }
    
    private void initComponents() {
        setTitle("Jadwal Waktu Sholat & Puasa");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        addWindowListener(new WindowHandler());
        
        mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        mainPanel.setBackground(BG_DARK);
        
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(createSholatPanel());
        centerPanel.add(createPuasaPanel());
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }

    private void loadIcon() {
        try {
            java.net.URL iconUrl = getClass().getResource("/jwsp/app/resources/icon.png");
            if (iconUrl != null) {
                setIconImage(new ImageIcon(iconUrl).getImage());
            }
        } catch (Exception e) {}
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 12, 0));
        
        // Row 1: Date & Time
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        lblTanggal = new JLabel();
        lblTanggal.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTanggal.setForeground(TEXT_PRIMARY);
        row1.add(lblTanggal, BorderLayout.WEST);
        
        lblJamSekarang = new JLabel();
        lblJamSekarang.setFont(new Font("Monospaced", Font.BOLD, 22));
        lblJamSekarang.setForeground(ACCENT_BLUE);
        lblJamSekarang.setHorizontalAlignment(SwingConstants.RIGHT);
        row1.add(lblJamSekarang, BorderLayout.EAST);
        
        panel.add(row1);
        panel.add(Box.createVerticalStrut(4));
        
        // Row 2: Status
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        lblStatusNotif = new JLabel("🔔 Notifikasi Aktif");
        lblStatusNotif.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblStatusNotif.setForeground(ACCENT_GREEN);
        row2.add(lblStatusNotif, BorderLayout.WEST);
        
        lblHijri = new JLabel();
        lblHijri.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblHijri.setForeground(ACCENT_ORANGE);
        lblHijri.setHorizontalAlignment(SwingConstants.CENTER);
        row2.add(lblHijri, BorderLayout.CENTER);
        
        lblPlayerStatus = new JLabel("🔊 Java Sound (Native)");
        lblPlayerStatus.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblPlayerStatus.setForeground(ACCENT_GREEN);
        row2.add(lblPlayerStatus, BorderLayout.EAST);
        
        panel.add(row2);
        panel.add(Box.createVerticalStrut(4));

        // New Row for Countdown
        JPanel rowCountdown = new JPanel(new BorderLayout());
        rowCountdown.setOpaque(false);
        lblCountdown = new JLabel();
        lblCountdown.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblCountdown.setForeground(TEXT_SECONDARY);
        lblCountdown.setHorizontalAlignment(SwingConstants.CENTER);
        rowCountdown.add(lblCountdown, BorderLayout.CENTER);
        panel.add(rowCountdown);
        
        panel.add(Box.createVerticalStrut(8));
        
        // Row 3: Provinsi
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        row3.setOpaque(false);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        row3.add(createLabel("Provinsi:"));
        comboProvinsi = createCombo(wilayahData.getDaftarProvinsi(), 160);
        comboProvinsi.addActionListener(e -> onProvinsiChanged());
        row3.add(comboProvinsi);
        
        row3.add(Box.createHorizontalStrut(10));
        row3.add(createLabel("Tipe:"));
        comboTipe = createCombo(new String[]{"Semua", "Kota", "Kabupaten"}, 90);
        comboTipe.addActionListener(e -> onTipeChanged());
        row3.add(comboTipe);
        
        panel.add(row3);
        panel.add(Box.createVerticalStrut(4));
        
        // Row 4: Wilayah
        JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        row4.setOpaque(false);
        row4.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        row4.add(createLabel("Wilayah:"));
        comboWilayah = createCombo(new String[]{"Jakarta Pusat"}, 180);
        comboWilayah.addActionListener(e -> onWilayahChanged());
        row4.add(comboWilayah);
        
        lblZonaWaktu = new JLabel("(WIB)");
        lblZonaWaktu.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblZonaWaktu.setForeground(TEXT_MUTED);
        row4.add(lblZonaWaktu);
        
        lblWilayahInfo = new JLabel("");
        lblWilayahInfo.setFont(new Font("SansSerif", Font.ITALIC, 10));
        lblWilayahInfo.setForeground(ACCENT_BLUE);
        row4.add(lblWilayahInfo);
        
        panel.add(row4);
        panel.add(Box.createVerticalStrut(4));
        
        // Row 5: Adzan & Theme
        JPanel row5 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        row5.setOpaque(false);
        row5.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        row5.add(createLabel("Adzan:"));
        String[] adzanFiles = SoundPlayer.getAvailableAdzanFiles();
        comboAdzan = createCombo(adzanFiles.length > 0 ? adzanFiles : new String[]{"Tidak ada"}, 160);
        comboAdzan.addActionListener(e -> onAdzanChanged());
        row5.add(comboAdzan);
        
        row5.add(Box.createHorizontalStrut(10));
        
        row5.add(createLabel("Theme:"));
        comboTheme = createCombo(ThemeManager.getInstance().getThemeNames(), 140);
        comboTheme.addActionListener(e -> onThemeChanged());
        row5.add(comboTheme);
        
        panel.add(row5);
        panel.add(Box.createVerticalStrut(4));
        
        // Row 6: Refresh button
        JPanel row6 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        row6.setOpaque(false);
        row6.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        btnRefresh = new JButton("🔄 Refresh Jadwal");
        btnRefresh.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnRefresh.setForeground(TEXT_PRIMARY);
        btnRefresh.setBackground(BG_INPUT);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        btnRefresh.addActionListener(e -> onRefreshClicked());
        row6.add(btnRefresh);
        
        panel.add(row6);
        
        return panel;
    }
    
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }
    
    private JComboBox<String> createCombo(String[] items, int width) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        combo.setPreferredSize(new Dimension(width, 26));
        combo.setBackground(BG_INPUT);
        combo.setForeground(TEXT_PRIMARY);
        return combo;
    }
    
    private JPanel createSholatPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_GREEN, 2),
            new EmptyBorder(14, 18, 14, 18)
        ));
        
        JLabel title = new JLabel("Jadwal Waktu Sholat");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(ACCENT_GREEN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(12));
        
        lblSubuh = addTimeRow(panel, "Subuh", ACCENT_GREEN);
        panel.add(Box.createVerticalStrut(8));
        lblSunrise = addTimeRow(panel, "Terbit", TEXT_MUTED);
        panel.add(Box.createVerticalStrut(8));
        lblDhuha = addTimeRow(panel, "Dhuha", ACCENT_ORANGE);
        panel.add(Box.createVerticalStrut(8));
        lblDzuhur = addTimeRow(panel, "Dzuhur", TEXT_PRIMARY);
        panel.add(Box.createVerticalStrut(8));
        lblAshar = addTimeRow(panel, "Ashar", TEXT_PRIMARY);
        panel.add(Box.createVerticalStrut(8));
        lblMaghrib = addTimeRow(panel, "Maghrib", ACCENT_ORANGE);
        panel.add(Box.createVerticalStrut(8));
        lblIsya = addTimeRow(panel, "Isya", TEXT_PRIMARY);
        
        return panel;
    }
    
    private JLabel addTimeRow(JPanel parent, String name, Color timeColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        
        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblName.setForeground(TEXT_SECONDARY);
        row.add(lblName, BorderLayout.WEST);
        
        JLabel lblTime = new JLabel("--:--");
        lblTime.setFont(new Font("Monospaced", Font.BOLD, 17));
        lblTime.setForeground(timeColor);
        lblTime.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(lblTime, BorderLayout.EAST);
        
        parent.add(row);
        return lblTime;
    }
    
    private JPanel createPuasaPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_ORANGE, 2),
            new EmptyBorder(14, 18, 14, 18)
        ));
        
        JLabel title = new JLabel("Informasi Puasa");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(ACCENT_ORANGE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(12));
        
        JPanel statusRow = new JPanel(new BorderLayout());
        statusRow.setOpaque(false);
        statusRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblStatus.setForeground(TEXT_SECONDARY);
        statusRow.add(lblStatus, BorderLayout.WEST);
        lblStatusPuasa = new JLabel("--");
        lblStatusPuasa.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblStatusPuasa.setHorizontalAlignment(SwingConstants.RIGHT);
        statusRow.add(lblStatusPuasa, BorderLayout.EAST);
        panel.add(statusRow);
        
        panel.add(Box.createVerticalStrut(10));
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(10));
        
        lblImsak = addTimeRow(panel, "Waktu Imsak", new Color(175, 130, 255));
        panel.add(Box.createVerticalStrut(8));
        lblBukaPuasa = addTimeRow(panel, "Buka Puasa", ACCENT_ORANGE);
        panel.add(Box.createVerticalStrut(12));
        
        lblInfoPuasa = new JLabel("<html><center><i>Info puasa</i></center></html>");
        lblInfoPuasa.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblInfoPuasa.setForeground(TEXT_MUTED);
        lblInfoPuasa.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblInfoPuasa);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));
        
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        optionsPanel.setBackground(BG_PANEL);
        optionsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(6, 15, 6, 15)
        ));
        
        chkWidget = new JCheckBox("Widget Desktop");
        styleCheckbox(chkWidget);
        chkWidget.addActionListener(e -> onToggleWidget());
        optionsPanel.add(chkWidget);
        
        JButton btnThemes = new JButton("Tema & Penampilan");
        btnThemes.setFont(new Font("SansSerif", Font.BOLD, 10));
        btnThemes.addActionListener(e -> new ThemeSelectorFrame().setVisible(true));
        optionsPanel.add(btnThemes);

        chkTray = new JCheckBox("System Tray");
        styleCheckbox(chkTray);
        chkTray.setSelected(true);
        optionsPanel.add(chkTray);
        
        panel.add(optionsPanel, BorderLayout.CENTER);
        
        JLabel note = new JLabel(
            "<html><center><small>Menutup jendela menyembunyikan aplikasi. Gunakan menu tray untuk keluar.</small></center></html>"
        );
        note.setFont(new Font("SansSerif", Font.PLAIN, 9));
        note.setForeground(TEXT_MUTED);
        note.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(note, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void styleCheckbox(JCheckBox chk) {
        chk.setFont(new Font("SansSerif", Font.PLAIN, 12));
        chk.setForeground(TEXT_PRIMARY);
        chk.setOpaque(false);
        chk.setFocusPainted(false);
    }
    
    private void loadPreferences() {
        isUpdatingCombos = true;
        
        // Load saved province and city
        String savedProvince = prefs.getProvince();
        String savedCity = prefs.getCity();
        
        // Set province
        if (wilayahData.isProvinsiValid(savedProvince)) {
            comboProvinsi.setSelectedItem(savedProvince);
        }
        updateWilayahCombo();
        
        // Set city/wilayah
        if (wilayahData.isWilayahValid(savedCity)) {
            comboWilayah.setSelectedItem(savedCity);
        }
        
        // Set current wilayah
        String selectedWilayah = (String) comboWilayah.getSelectedItem();
        if (selectedWilayah != null) {
            currentWilayah = wilayahData.getByNamaInProvinsi(
                selectedWilayah, (String) comboProvinsi.getSelectedItem()
            );
        }
        
        // Adzan
        String savedAdzan = prefs.getAdzan();
        for (int i = 0; i < comboAdzan.getItemCount(); i++) {
            if (comboAdzan.getItemAt(i).equals(savedAdzan)) {
                comboAdzan.setSelectedIndex(i);
                break;
            }
        }
        
        // Theme
        String savedTheme = prefs.getTheme();
        for (int i = 0; i < comboTheme.getItemCount(); i++) {
            if (comboTheme.getItemAt(i).equals(savedTheme)) {
                comboTheme.setSelectedIndex(i);
                break;
            }
        }
        
        // Widget & Tray
        chkWidget.setSelected(prefs.isWidgetEnabled());
        chkTray.setSelected(prefs.isTrayEnabled());
        
        isUpdatingCombos = false;
    }
    
    private void updateWilayahCombo() {
        String provinsi = (String) comboProvinsi.getSelectedItem();
        if (provinsi == null) return;
        
        TipeWilayah tipe = null;
        String tipeStr = (String) comboTipe.getSelectedItem();
        if ("Kota".equals(tipeStr)) {
            tipe = TipeWilayah.KOTA;
        } else if ("Kabupaten".equals(tipeStr)) {
            tipe = TipeWilayah.KABUPATEN;
        }
        
        String[] wilayahNames = wilayahData.getWilayahNames(provinsi, tipe);
        comboWilayah.setModel(new DefaultComboBoxModel<>(wilayahNames));
    }
    
    private void startClockTimer() {
        clockTimer = new Timer(1000, e -> {
            // Update Jam
            PrayerTimeController controller = lifecycle.getController();
            if (controller != null) {
                String time = controller.getCurrentTimeString();
                lblJamSekarang.setText(time);
            }
            
            // Update Ramadhan Countdown live
            RamadanService svc = RamadanService.getInstance();
            if (svc.isRamadhanStarted()) {
                lblCountdown.setVisible(false);
            } else {
                lblCountdown.setText(svc.getCountdownText());
                lblCountdown.setVisible(true);
            }
        });
        clockTimer.start();
    }
    
    public void updateDisplay() {
        PrayerTimeController controller = lifecycle.getController();
        if (controller == null) return;
        
        JadwalSholat jadwal = controller.getJadwalSholat();
        JadwalPuasa puasa = controller.getJadwalPuasa();
        ThemeManager.Theme theme = ThemeManager.getInstance().getCurrentTheme();

        // Apply Theme Colors
        if (mainPanel != null) mainPanel.setBackground(theme.background);
        getContentPane().setBackground(theme.background);
        
        lblJamSekarang.setForeground(theme.textPrimary);
        lblTanggal.setForeground(theme.textSecondary);
        lblHijri.setForeground(theme.accentOrange);
        lblStatusNotif.setForeground(theme.textPrimary);
        lblCountdown.setForeground(theme.textSecondary);

        // Date & Hijri
        LocalDate today = LocalDate.now(
            currentWilayah != null ? currentWilayah.getZonaWaktu() : 
            java.time.ZoneId.of("Asia/Jakarta")
        );
        lblTanggal.setText(today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")));
        lblHijri.setText(HijriService.getInstance().getHijriDateText());
        lblZonaWaktu.setText("(" + jadwal.getZonaWaktuString() + ")");
        
        // Status header
        PrayerTimeController.PrayerInfo current = controller.getCurrentPrayer();
        lblStatusNotif.setText("🕌 " + current.name);
        lblStatusNotif.setForeground(current.name.contains("Wajib") ? ACCENT_GREEN : ACCENT_ORANGE);
        
        // Countdown (Initial visibility)
        RamadanService svc = RamadanService.getInstance();
        if (svc.isRamadhanStarted()) {
            lblCountdown.setVisible(false);
        } else {
            lblCountdown.setText(svc.getCountdownText());
            lblCountdown.setVisible(true);
        }
        
        // Wilayah info
        if (currentWilayah != null) {
            lblWilayahInfo.setText(String.format("%.2f°, %.2f°", 
                currentWilayah.getLatitude(), currentWilayah.getLongitude()));
        }
        
        // Prayer times
        lblSubuh.setText(jadwal.getWaktuString(0));
        lblSunrise.setText(jadwal.getWaktuSunrise());
        lblDhuha.setText(jadwal.getWaktuDhuha());
        lblDzuhur.setText(jadwal.getWaktuString(1));
        lblAshar.setText(jadwal.getWaktuString(2));
        lblMaghrib.setText(jadwal.getWaktuString(3));
        lblIsya.setText(jadwal.getWaktuString(4));
        
        // Fasting info
        String status = puasa.getStatusPuasa();
        lblStatusPuasa.setText(status);
        
        if (puasa.isPuasaRamadhan()) {
            lblStatusPuasa.setForeground(ACCENT_GREEN);
            lblInfoPuasa.setText("<html><center><i>Bulan Ramadhan</i></center></html>");
        } else if (puasa.isPuasaSunnah()) {
            lblStatusPuasa.setForeground(ACCENT_BLUE);
            lblInfoPuasa.setText("<html><center><i>" + puasa.getNamaHari() + "</i></center></html>");
        } else {
            lblStatusPuasa.setForeground(TEXT_MUTED);
            lblInfoPuasa.setText("<html><center><i>Tidak ada puasa</i></center></html>");
        }
        
        lblImsak.setText(puasa.getWaktuImsak());
        lblBukaPuasa.setText(puasa.getWaktuBukaPuasa());
    }
    
    // === Event Handlers ===
    
    private void onProvinsiChanged() {
        if (isUpdatingCombos) return;
        isUpdatingCombos = true;
        updateWilayahCombo();
        isUpdatingCombos = false;
        onWilayahChanged();
    }
    
    private void onTipeChanged() {
        if (isUpdatingCombos) return;
        isUpdatingCombos = true;
        updateWilayahCombo();
        isUpdatingCombos = false;
        onWilayahChanged();
    }
    
    private void onWilayahChanged() {
        if (isUpdatingCombos) return;
        
        String wilayahNama = (String) comboWilayah.getSelectedItem();
        String provinsi = (String) comboProvinsi.getSelectedItem();
        if (wilayahNama == null || provinsi == null) return;
        
        // Get wilayah object
        currentWilayah = wilayahData.getByNamaInProvinsi(wilayahNama, provinsi);
        if (currentWilayah == null) {
            currentWilayah = wilayahData.getByNama(wilayahNama);
        }
        
        if (currentWilayah != null) {
            // Update lifecycle manager
            lifecycle.updateWilayah(currentWilayah);
            
            // Save preferences
            prefs.setProvince(provinsi);
            prefs.setCity(wilayahNama);
            
            // Update display
            updateDisplay();
        }
    }
    
    private void onAdzanChanged() {
        String adzan = (String) comboAdzan.getSelectedItem();
        if (adzan != null) {
            lifecycle.setSelectedAdzanFile(adzan);
        }
    }
    
    private void onThemeChanged() {
        String theme = (String) comboTheme.getSelectedItem();
        if (theme != null) {
            ThemeManager.getInstance().setTheme(theme);
        }
    }
    
    private void onRefreshClicked() {
        PrayerTimeController controller = lifecycle.getController();
        if (controller != null) {
            controller.getJadwalSholat().refresh();
            controller.update();
        }
        updateDisplay();
        JOptionPane.showMessageDialog(this, "Jadwal berhasil di-refresh!", 
            "Refresh", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void onToggleWidget() {
        if (chkWidget.isSelected()) {
            lifecycle.showWidget();
        } else {
            lifecycle.hideWidget();
        }
    }
    
    public void cleanup() {
        if (clockTimer != null) {
            clockTimer.stop();
        }
    }
    
    @Override
    public void onPrayerTimeUpdate(PrayerTimeController.PrayerInfo current, PrayerTimeController.PrayerInfo next, String puasaStatus) {
        SwingUtilities.invokeLater(this::updateDisplay);
    }

    @Override
    public void onThemeChanged(ThemeManager.Theme newTheme) {
        SwingUtilities.invokeLater(this::updateDisplay);
    }

    @Override
    public void dispose() {
        if (lifecycle.getController() != null) {
            lifecycle.getController().removeListener(this);
        }
        ThemeManager.getInstance().removeListener(this);
        cleanup();
        super.dispose();
    }
    private class WindowHandler extends WindowAdapter {
        @Override
        public void windowIconified(WindowEvent e) {
            if (chkTray != null && chkTray.isSelected()) {
                setVisible(false);
            }
        }
    }
}
