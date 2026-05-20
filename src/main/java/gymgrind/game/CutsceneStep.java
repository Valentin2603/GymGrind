package gymgrind.game;

public record CutsceneStep(
        Type type,
        String title,
        String description,
        String judgeKey,
        double durationSeconds,
        boolean requiresPlayerAdvance
) {

    public enum Type {
        ANNOUNCEMENT,
        JUDGE_REVEAL,
        FINALE
    }

    public static CutsceneStep announcement(String title, String description, double durationSeconds) {
        return new CutsceneStep(Type.ANNOUNCEMENT, title, description, null, durationSeconds, false);
    }

    public static CutsceneStep judgeReveal(String judgeKey, String title, String description, double durationSeconds) {
        return new CutsceneStep(Type.JUDGE_REVEAL, title, description, judgeKey, durationSeconds, true);
    }

    public static CutsceneStep finale(String title, String description, double durationSeconds) {
        return new CutsceneStep(Type.FINALE, title, description, null, durationSeconds, false);
    }

    public boolean hasJudge() {
        return judgeKey != null && !judgeKey.isBlank();
    }
}
