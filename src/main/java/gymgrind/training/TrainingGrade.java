package gymgrind.training;

public enum TrainingGrade {
    FAIL("Провал", 0.15),
    WEAK("Слабо", 0.35),
    NORMAL("Нормально", 0.70),
    GOOD("Хорошо", 1.00),
    EXCELLENT("Отлично", 1.28);

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

    public boolean atLeast(TrainingGrade grade) {
        return ordinal() >= grade.ordinal();
    }

    public boolean failed() {
        return this == FAIL;
    }
}
