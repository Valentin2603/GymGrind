package gymgrind.training;

public record TrainingSession(
        TrainingMachine machine,
        TrainingWeight weight,
        TrainingReward reward,
        TrainingTuning tuning
) {
}
