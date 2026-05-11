package gymgrind.game;

public final class CalendarState {

    private int currentDay;
    private final int maxDays;

    public CalendarState(int currentDay, int maxDays) {
        this.currentDay = currentDay;
        this.maxDays = maxDays;
    }

    public static CalendarState createDefault() {
        return new CalendarState(1, 30);
    }

    public int currentDay() {
        return currentDay;
    }

    public int maxDays() {
        return maxDays;
    }

    public int daysLeft() {
        return maxDays - currentDay;
    }

    public boolean isLastDay() {
        return currentDay >= maxDays;
    }

    public void nextDay() {
        if (currentDay < maxDays) {
            currentDay++;
        }
    }

    public void reset() {
        currentDay = 1;
    }
}
