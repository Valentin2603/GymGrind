package gymgrind.training;

public record TrainingTuning(
        double speedMultiplier,
        double zoneMultiplier,
        int rhythmLength,
        double rhythmTimeSeconds,
        boolean preWorkoutUsed,
        FatigueProfile fatigueProfile
) {
}
