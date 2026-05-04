package gymgrind.model;

public record SkillCheckResult(
        boolean success,
        String message,
        int strengthDelta,
        int muscleDelta,
        int staminaDelta,
        int fatigueDelta
) {
}
