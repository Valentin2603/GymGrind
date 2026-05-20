package gymgrind.training;

public record TrainingSession(
        TrainingMachine machine,
        TrainingWeight weight,
        int workingLoad,
        int selectedLoad,
        String loadUnit,
        TrainingReward reward,
        TrainingTuning tuning
) {
    public String selectedLoadLabel() {
        return selectedLoad + " " + loadUnit;
    }

    public String workingLoadLabel() {
        return workingLoad + " " + loadUnit;
    }

    public String weightLabel() {
        return weight.label() + " (" + selectedLoadLabel() + ")";
    }
}
