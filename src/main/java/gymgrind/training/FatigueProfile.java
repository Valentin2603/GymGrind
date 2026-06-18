package gymgrind.training;

public enum FatigueProfile {
    NORMAL("Обычная", 1.08, 0.98, 0),
    TIRED("Быстрее", 1.22, 0.92, 1),
    EXHAUSTED("Уже тяжело", 1.34, 0.76, 1),
    DANGER("Почти провал", 1.56, 0.58, 2);

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
        if (fatigue <= 40) {
            return NORMAL;
        }
        if (fatigue <= 70) {
            return TIRED;
        }
        if (fatigue < 90) {
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
