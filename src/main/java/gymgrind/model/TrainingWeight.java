package gymgrind.model;

public enum TrainingWeight {
    LIGHT("Лёгкий", 0.65, 0.70, 0.85, 1.15, -1),
    MEDIUM("Средний", 1.00, 1.00, 1.00, 1.00, 0),
    HEAVY("Тяжёлый", 1.45, 1.55, 1.25, 0.80, 1);

    private final String label;
    private final double rewardMultiplier;
    private final double fatigueMultiplier;
    private final double speedMultiplier;
    private final double zoneMultiplier;
    private final int rhythmLengthBonus;

    TrainingWeight(String label,
                   double rewardMultiplier,
                   double fatigueMultiplier,
                   double speedMultiplier,
                   double zoneMultiplier,
                   int rhythmLengthBonus) {
        this.label = label;
        this.rewardMultiplier = rewardMultiplier;
        this.fatigueMultiplier = fatigueMultiplier;
        this.speedMultiplier = speedMultiplier;
        this.zoneMultiplier = zoneMultiplier;
        this.rhythmLengthBonus = rhythmLengthBonus;
    }

    public String label() {
        return label;
    }

    public double rewardMultiplier() {
        return rewardMultiplier;
    }

    public double fatigueMultiplier() {
        return fatigueMultiplier;
    }

    public double speedMultiplier() {
        return speedMultiplier;
    }

    public double zoneMultiplier() {
        return zoneMultiplier;
    }

    public int rhythmLengthBonus() {
        return rhythmLengthBonus;
    }
}
