package jwsp.theme;

import jwsp.config.UserPreferences;
import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class ThemeManager {
    private static ThemeManager instance;
    private static final String THEMES_DIR = "themes";
    
    public static class Theme {
        public String name = "Default Dark";
        public String author = "System";
        
        // Colors
        public Color textPrimary = Color.WHITE;
        public Color textSecondary = new Color(200, 200, 200);
        public Color background = new Color(30, 32, 40);
        public Color accent = new Color(76, 175, 80);
        public Color accentOrange = new Color(255, 159, 64);
        
        // Widget specific
        public float backgroundOpacity = 0.85f;
        public int cornerRadius = 16;
        public int padding = 14;
        
        // Fonts
        public String fontFamily = "SansSerif";
        public int titleSize = 18;
        public int normalSize = 14;
        
        // Helpers
        public Color getBackgroundWithAlpha() {
            int alpha = (int) (backgroundOpacity * 255);
            return new Color(background.getRed(), background.getGreen(), background.getBlue(), alpha);
        }

        public Font getTitleFont() {
            return new Font(fontFamily, Font.BOLD, titleSize);
        }

        public Font getNormalFont() {
            return new Font(fontFamily, Font.PLAIN, normalSize);
        }
        
        public Font getTimeFont() {
            return new Font("Monospaced", Font.BOLD, titleSize + 24);
        }

        public Font getSmallFont() {
            return new Font(fontFamily, Font.PLAIN, normalSize - 2);
        }
    }

    private Theme currentTheme;
    private final List<ThemeChangeListener> listeners = new ArrayList<>();
    private final Map<String, Theme> availableThemes = new LinkedHashMap<>();

    public interface ThemeChangeListener {
        void onThemeChanged(Theme newTheme);
    }

    private ThemeManager() {
        new File(THEMES_DIR).mkdirs();
        loadBuiltInThemes();
        loadLocalThemes();
        
        // Persistence
        String savedTheme = UserPreferences.getInstance().getTheme();
        if (savedTheme != null && availableThemes.containsKey(savedTheme)) {
            currentTheme = availableThemes.get(savedTheme);
        } else {
            currentTheme = availableThemes.get("Dark Modern");
        }
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    private void loadBuiltInThemes() {
        // Dark Modern
        Theme dark = new Theme();
        dark.name = "Dark Modern";
        availableThemes.put(dark.name, dark);

        // Light Modern
        Theme light = new Theme();
        light.name = "Light Modern";
        light.background = new Color(245, 245, 250);
        light.textPrimary = new Color(30, 30, 35);
        light.textSecondary = new Color(100, 100, 110);
        light.backgroundOpacity = 0.95f;
        availableThemes.put(light.name, light);
        
        // Glassy
        Theme glass = new Theme();
        glass.name = "Glassy Transparent";
        glass.background = new Color(10, 10, 15);
        glass.backgroundOpacity = 0.4f;
        glass.accent = new Color(0, 200, 255);
        availableThemes.put(glass.name, glass);
    }

    private void loadLocalThemes() {
        File dir = new File(THEMES_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File f : files) {
                try {
                    Theme t = parseJsonTheme(f);
                    if (t != null) availableThemes.put(t.name, t);
                } catch (Exception e) {
                    System.err.println("Failed to load theme " + f.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    public void applyTheme(Theme theme) {
        this.currentTheme = theme;
        UserPreferences.getInstance().setTheme(theme.name);
        for (ThemeChangeListener l : listeners) {
            l.onThemeChanged(theme);
        }
    }

    public void loadThemeFromFile(File file) {
        try {
            Theme t = parseJsonTheme(file);
            if (t != null) {
                availableThemes.put(t.name, t);
                applyTheme(t);
                
                File dest = new File(THEMES_DIR, file.getName());
                if (!dest.exists()) {
                    copyFile(file, dest);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyFile(File src, File dest) throws IOException {
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        }
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public String[] getThemeNames() {
        return availableThemes.keySet().toArray(new String[0]);
    }

    public Theme getTheme(String name) {
        return availableThemes.get(name);
    }

    public void setTheme(String name) {
        Theme t = availableThemes.get(name);
        if (t != null) applyTheme(t);
    }

    public void addListener(ThemeChangeListener l) {
        listeners.add(l);
    }

    public void removeListener(ThemeChangeListener l) {
        listeners.remove(l);
    }

    private Theme parseJsonTheme(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        String json = sb.toString();
        Theme t = new Theme();
        
        t.name = getVal(json, "name", t.name);
        t.author = getVal(json, "author", t.author);
        
        // Colors
        t.textPrimary = parseHex(getVal(json, "textPrimary", null), t.textPrimary);
        t.textSecondary = parseHex(getVal(json, "textSecondary", null), t.textSecondary);
        t.background = parseHex(getVal(json, "background", null), t.background);
        t.accent = parseHex(getVal(json, "accent", null), t.accent);
        
        // Widget
        t.backgroundOpacity = Float.parseFloat(getVal(json, "backgroundOpacity", String.valueOf(t.backgroundOpacity)));
        t.cornerRadius = Integer.parseInt(getVal(json, "cornerRadius", String.valueOf(t.cornerRadius)));
        t.padding = Integer.parseInt(getVal(json, "padding", String.valueOf(t.padding)));
        
        // Font
        t.fontFamily = getVal(json, "family", t.fontFamily);
        t.titleSize = Integer.parseInt(getVal(json, "titleSize", String.valueOf(t.titleSize)));
        t.normalSize = Integer.parseInt(getVal(json, "normalSize", String.valueOf(t.normalSize)));
        
        return t;
    }

    private String getVal(String json, String key, String def) {
        Pattern p = Pattern.compile("\"" + key + "\":\\s*\"?([^,\"}]+)\"?");
        Matcher m = p.matcher(json);
        if (m.find()) return m.group(1).trim();
        return def;
    }

    private Color parseHex(String hex, Color def) {
        if (hex == null) return def;
        try {
            if (hex.startsWith("#")) hex = hex.substring(1);
            return new Color(Integer.parseInt(hex, 16));
        } catch (Exception e) {
            return def;
        }
    }
}
