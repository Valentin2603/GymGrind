package gymgrind.training;

public enum TrainingWeight {
    LIGHT("Лёгкий", 0.82, "-18%", 0.55, 0.70, 0.82, 1.20, -1),
    MEDIUM("Средний", 1.00, "рабочий", 1.00, 1.00, 1.00, 1.00, 0),
    HEAVY("Тяжёлый", 1.18, "+18%", 1.65, 1.75, 1.45, 0.66, 3);

    private final String label;
    private final double loadMultiplier;
    private final String loadHint;
    private final double rewardMultiplier;
    private final double fatigueMultiplier;
    private final double speedMultiplier;
    private final double zoneMultiplier;
    private final int rhythmLengthBonus;

    TrainingWeight(String label,
                   double loadMultiplier,
                   String loadHint,
                   double rewardMultiplier,
                   double fatigueMultiplier,
                   double speedMultiplier,
                   double zoneMultiplier,
                   int rhythmLengthBonus) {
        this.label = label;
        this.loadMultiplier = loadMultiplier;
        this.loadHint = loadHint;
        this.rewardMultiplier = rewardMultiplier;
        this.fatigueMultiplier = fatigueMultiplier;
        this.speedMultiplier = speedMultiplier;
        this.zoneMultiplier = zoneMultiplier;
        this.rhythmLengthBonus = rhythmLengthBonus;
    }

    public String label() {
        return label;
    }

    public double loadMultiplier() {
        return loadMultiplier;
    }

    public String loadHint() {
        return loadHint;
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
