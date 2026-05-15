package gymgrind.training;

public record TrainingTuning(
        double speedMultiplier,
        double zoneMultiplier,
        int rhythmLength,
        double rhythmTimeSeconds,
        boolean preWorkoutUsed,
        double strengthBonus,
        double muscleBonus,
        double staminaBonus,
        double bodyFatLoad,
        double muscleLoad,
        FatigueProfile fatigueProfile
) {
}
