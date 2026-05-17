package gymgrind.daily;

public record DailyQuestView(
        String title,
        String progressText,
        String bonusText,
        boolean completed
) {
}
