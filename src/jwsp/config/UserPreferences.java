package jwsp.config;

import java.util.prefs.Preferences;

public class UserPreferences {
    private static UserPreferences instance;
    private Preferences prefs;
    private static final String KEY_PROVINCE = "selectedProvince";
    private static final String KEY_CITY = "selectedCity";
    private static final String KEY_ADZAN = "selectedAdzan";
    private static final String KEY_WIDGET_ENABLED = "widgetEnabled";
    private static final String KEY_TRAY_ENABLED = "trayEnabled";
    private static final String KEY_THEME = "selectedTheme";
    private static final String DEFAULT_PROVINCE = "DKI Jakarta";
    private static final String DEFAULT_CITY = "Jakarta";
    private static final String DEFAULT_ADZAN = "Adzan Makkah.mp3";
    private static final boolean DEFAULT_WIDGET = false;
    private static final boolean DEFAULT_TRAY = true;

    private UserPreferences() {
        prefs = Preferences.userNodeForPackage(UserPreferences.class);
        System.out.println("[UserPreferences] Initialized at: " + prefs.absolutePath());
    }

    public static synchronized UserPreferences getInstance() {
        if (instance == null) {
            instance = new UserPreferences();
        }
        return instance;
    }
    
    public String getProvince() {
        return prefs.get(KEY_PROVINCE, DEFAULT_PROVINCE);
    }
    
    public void setProvince(String province) {
        prefs.put(KEY_PROVINCE, province);
        System.out.println("[UserPreferences] Province saved: " + province);
    }
    
    public String getCity() {
        return prefs.get(KEY_CITY, DEFAULT_CITY);
    }
    
    public void setCity(String city) {
        prefs.put(KEY_CITY, city);
        System.out.println("[UserPreferences] City saved: " + city);
    }
    
    public String getAdzan() {
        return prefs.get(KEY_ADZAN, DEFAULT_ADZAN);
    }
    
    public void setAdzan(String adzan) {
        prefs.put(KEY_ADZAN, adzan);
    }
    
    public boolean isWidgetEnabled() {
        return prefs.getBoolean(KEY_WIDGET_ENABLED, DEFAULT_WIDGET);
    }
    
    public void setWidgetEnabled(boolean enabled) {
        prefs.putBoolean(KEY_WIDGET_ENABLED, enabled);
    }
    
    public boolean isTrayEnabled() {
        return prefs.getBoolean(KEY_TRAY_ENABLED, DEFAULT_TRAY);
    }
    
    public void setTrayEnabled(boolean enabled) {
        prefs.putBoolean(KEY_TRAY_ENABLED, enabled);
    }
    
    public String getTheme() {
        return prefs.get(KEY_THEME, "Dark Transparent");
    }
    
    public void setTheme(String theme) {
        prefs.put(KEY_THEME, theme);
    }
    
    public void saveAll(String province, String city, String adzan, 
                        boolean widget, boolean tray) {
        setProvince(province);
        setCity(city);
        setAdzan(adzan);
        setWidgetEnabled(widget);
        setTrayEnabled(tray);
    }
    
    public void resetToDefaults() {
        setProvince(DEFAULT_PROVINCE);
        setCity(DEFAULT_CITY);
        setAdzan(DEFAULT_ADZAN);
        setWidgetEnabled(DEFAULT_WIDGET);
        setTrayEnabled(DEFAULT_TRAY);
    }

    public void flush() {
        try {
            prefs.flush();
        } catch (Exception e) {
        }
    }

    public void printAll() {
        
    }
}
