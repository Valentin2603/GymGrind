package gymgrind.model;

public record TrainingSession(
        TrainingMachine machine,
        TrainingWeight weight,
        TrainingReward reward,
        TrainingTuning tuning
) {
}
