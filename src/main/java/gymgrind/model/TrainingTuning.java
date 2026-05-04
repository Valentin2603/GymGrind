package gymgrind.model;

public record TrainingTuning(
        double speedMultiplier,
        double zoneMultiplier,
        int rhythmLength,
        double rhythmTimeSeconds,
        boolean preWorkoutUsed,
        FatigueProfile fatigueProfile
) {
}
