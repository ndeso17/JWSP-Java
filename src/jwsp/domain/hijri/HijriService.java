package jwsp.domain.hijri;

import java.time.LocalDate;

public class HijriService {

    private static HijriService instance;
    private HijriCalendar.HijriDate currentHijriDate;
    private LocalDate lastCachedDate;

    private HijriService() {
        refresh();
    }

    public static synchronized HijriService getInstance() {
        if (instance == null) {
            instance = new HijriService();
        }
        return instance;
    }

    public void refresh() {
        LocalDate today = LocalDate.now();
        if (lastCachedDate == null || !lastCachedDate.equals(today)) {
            currentHijriDate = HijriCalendar.fromLocalDate(today);
            lastCachedDate = today;
        }
    }
    
    public String getHijriDateText() {
        refresh();
        return currentHijriDate.toString();
    }

    public HijriCalendar.HijriDate getHijriDate() {
        refresh();
        return currentHijriDate;
    }
}
