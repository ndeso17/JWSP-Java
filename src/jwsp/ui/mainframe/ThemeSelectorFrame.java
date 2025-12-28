package jwsp.ui.mainframe;

import jwsp.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class ThemeSelectorFrame extends JFrame {
    
    private final JList<String> themeList;
    private final DefaultListModel<String> listModel;
    private final ThemeManager manager;

    public ThemeSelectorFrame() {
        super("Theme Manager");
        this.manager = ThemeManager.getInstance();
        this.listModel = new DefaultListModel<>();
        this.themeList = new JList<>(listModel);
        
        setupUI();
        refreshList();
        
        setSize(400, 500);
        setLocationRelativeTo(null);
    }

    private void setupUI() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(new EmptyBorder(20, 20, 20, 20));
        main.setBackground(new Color(240, 240, 245));

        // Header
        JLabel header = new JLabel("Custom Themes");
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        main.add(header, BorderLayout.NORTH);

        // List
        themeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        themeList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        themeList.setFixedCellHeight(35);
        main.add(new JScrollPane(themeList), BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));
        buttons.setOpaque(false);

        JButton btnApply = new JButton("Apply Selected");
        btnApply.addActionListener(e -> {
            String selected = themeList.getSelectedValue();
            if (selected != null) {
                manager.setTheme(selected);
            }
        });
        
        // Let's refine the Manager API slightly to support getByName or just pass name to apply
        // Wait, I used applyTheme(Theme t) in my new Manager. I need to expose getTheme(String name).

        JButton btnLoad = new JButton("Load JSON...");
        btnLoad.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON Themes", "json"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                manager.loadThemeFromFile(file);
                refreshList();
            }
        });

        buttons.add(btnLoad);
        buttons.add(btnApply);
        main.add(buttons, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private void refreshList() {
        listModel.clear();
        for (String name : manager.getThemeNames()) {
            listModel.addElement(name);
        }
    }
}
