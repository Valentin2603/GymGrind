package gymgrind.game;

public record CutsceneStep(
        Type type,
        String title,
        String description,
        String judgeKey,
        double durationSeconds
) {

    public enum Type {
        ANNOUNCEMENT,
        JUDGE_REVEAL,
        FINALE
    }

    public static CutsceneStep announcement(String title, String description, double durationSeconds) {
        return new CutsceneStep(Type.ANNOUNCEMENT, title, description, null, durationSeconds);
    }

    public static CutsceneStep judgeReveal(String judgeKey, String title, String description, double durationSeconds) {
        return new CutsceneStep(Type.JUDGE_REVEAL, title, description, judgeKey, durationSeconds);
    }

    public static CutsceneStep finale(String title, String description, double durationSeconds) {
        return new CutsceneStep(Type.FINALE, title, description, null, durationSeconds);
    }

    public boolean hasJudge() {
        return judgeKey != null && !judgeKey.isBlank();
    }
}
