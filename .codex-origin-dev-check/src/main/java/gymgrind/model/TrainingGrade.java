package gymgrind.model;

public enum TrainingGrade {
    FAIL("Провал", 0.45),
    NORMAL("Нормально", 1.00),
    EXCELLENT("Отлично", 1.35);

    private final String label;
    private final double rewardMultiplier;

    TrainingGrade(String label, double rewardMultiplier) {
        this.label = label;
        this.rewardMultiplier = rewardMultiplier;
    }

    public String label() {
        return label;
    }

    public double rewardMultiplier() {
        return rewardMultiplier;
    }
}
