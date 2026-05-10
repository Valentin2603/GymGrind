package gymgrind.training.minigames;

public record SkillCheckResult(
        boolean success,
        String message,
        int strengthDelta,
        int muscleDelta,
        int staminaDelta,
        int fatigueDelta
) {
}
