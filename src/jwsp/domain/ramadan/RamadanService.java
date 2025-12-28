package jwsp.domain.ramadan;

import jwsp.domain.hijri.HijriCalendar;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RamadanService {
    
    private static RamadanService instance;
    private LocalDate cachedRamadhanStart;
    private LocalDate lastCheckDate;

    private RamadanService() {}

    public static synchronized RamadanService getInstance() {
        if (instance == null) {
            instance = new RamadanService();
        }
        return instance;
    }

    public boolean isRamadhanStarted() {
        LocalDate start = getNextRamadanStart();
        if (start == null) return false;
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime ramadhanTime = start.atTime(LocalTime.of(4, 30));

        return now.isAfter(ramadhanTime) && now.toLocalDate().equals(start);
    }

    public LocalDateTime getNextRamadhan() {
        LocalDate start = getNextRamadanStart();
        if (start == null) return null;
        return start.atTime(LocalTime.of(4, 30));
    }

    public String getCountdownText() {
        LocalDateTime target = getNextRamadhan();
        if (target == null) return "Data tidak tersedia";
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(target)) {
            return "Ramadhan telah tiba!";
        }

        Duration d = Duration.between(now, target);
        long days = d.toDays();
        long hours = d.toHoursPart();
        long minutes = d.toMinutesPart();
        long seconds = d.toSecondsPart();

        StringBuilder sb = new StringBuilder("Menuju Ramadhan: ");
        
        if (d.toHours() < 1) {
            sb.append(minutes).append(" menit ").append(seconds).append(" detik");
        } else if (d.toDays() < 1) {
            sb.append(hours).append(" jam ").append(minutes).append(" menit");
        } else {
            sb.append(days).append(" hari ").append(hours).append(" jam ").append(minutes).append(" menit");
        }
        
        return sb.toString();
    }

    public LocalDate getNextRamadanStart() {
        LocalDate today = LocalDate.now();
        if (cachedRamadhanStart != null && today.equals(lastCheckDate)) {
            return cachedRamadhanStart;
        }

        HijriCalendar.HijriDate hijriToday = HijriCalendar.fromLocalDate(today);
        int targetYear = hijriToday.year;
        
        if (hijriToday.month > 9 || (hijriToday.month == 9 && hijriToday.day > 1)) {
            targetYear++;
        }

        LocalDate checkDate = today.minusDays(30); 
        LocalDate result = null;
        while (true) {
            HijriCalendar.HijriDate h = HijriCalendar.fromLocalDate(checkDate);
            if (h.year == targetYear && h.month == 9) {
                while (h.day > 1) {
                    checkDate = checkDate.minusDays(1);
                    h = HijriCalendar.fromLocalDate(checkDate);
                }
                result = checkDate;
                break;
            }
            checkDate = checkDate.plusDays(10);
            if (checkDate.isAfter(today.plusYears(2))) break;
        }
        
        cachedRamadhanStart = result;
        lastCheckDate = today;
        return result;
    }

    public static class Countdown {
        private final String text;
        public final long days;
        public Countdown(String text, long days) {
            this.text = text;
            this.days = days;
        }
        @Override
        public String toString() { return text; }
    }

    public static Countdown getCountdown() {
        RamadanService svc = getInstance();
        return new Countdown(svc.getCountdownText(), svc.getNextRamadhan() != null ? 
            Duration.between(LocalDateTime.now(), svc.getNextRamadhan()).toDays() : 0);
    }
}
