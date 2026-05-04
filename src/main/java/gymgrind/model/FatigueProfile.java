package gymgrind.model;

public enum FatigueProfile {
    NORMAL("Обычная", 1.00, 1.00, 0),
    TIRED("Быстрее", 1.25, 1.00, 1),
    EXHAUSTED("Уже тяжело", 1.35, 0.70, 1),
    DANGER("Почти провал", 1.65, 0.50, 2);

    private final String label;
    private final double speedMultiplier;
    private final double zoneMultiplier;
    private final int rhythmLengthBonus;

    FatigueProfile(String label, double speedMultiplier, double zoneMultiplier, int rhythmLengthBonus) {
        this.label = label;
        this.speedMultiplier = speedMultiplier;
        this.zoneMultiplier = zoneMultiplier;
        this.rhythmLengthBonus = rhythmLengthBonus;
    }

    public static FatigueProfile fromFatigue(int fatigue) {
        if (fatigue <= 30) {
            return NORMAL;
        }
        if (fatigue <= 60) {
            return TIRED;
        }
        if (fatigue <= 80) {
            return EXHAUSTED;
        }
        return DANGER;
    }

    public String label() {
        return label;
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
