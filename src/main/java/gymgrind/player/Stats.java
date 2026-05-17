package gymgrind.player;

import gymgrind.training.TrainingReward;

public final class Stats {

    private static final double MIN_BODY_FAT = 8.0;
    private static final double MAX_BODY_FAT = 100.0;
    private static final int MAX_FATIGUE = 100;

    private int strength;
    private int muscle;
    private int stamina;
    private int fatigue;
    private int money;
    private double bodyFat;

    private int baseStrength;
    private int baseMuscle;
    private int baseStamina;
    private int baseFatigue;
    private int baseMoney;
    private double baseBodyFat;

    public Stats(int strength, int muscle, int stamina, int fatigue, int money, double bodyFat) {
        configureBaseValues(strength, muscle, stamina, fatigue, money, bodyFat);
        reset();
    }

    public int strength() {
        return strength;
    }

    public int muscle() {
        return muscle;
    }

    public int stamina() {
        return stamina;
    }

    public int fatigue() {
        return fatigue;
    }

    public int availableStamina() {
        return MAX_FATIGUE - fatigue;
    }

    public int maxAvailableStamina() {
        return MAX_FATIGUE;
    }

    public int money() {
        return money;
    }

    public double bodyFat() {
        return bodyFat;
    }

    public int bodyFatPercent() {
        return (int) Math.round(bodyFat);
    }

    public int form() {
        return (int) Math.round((strength + muscle + stamina) / 5.0 - bodyFat);
    }

    public double staminaFatigueMultiplier() {
        double staminaFactor = stamina / (stamina + 260.0);
        return 1.0 - staminaFactor * 0.38;
    }

    public void addTrainingReward(TrainingReward reward) {
        applyDeltas(reward.strength(), reward.muscle(), reward.stamina(), reward.fatigue(), 0, reward.bodyFat());
    }

    public void applyDeltas(int strengthDelta,
                            int muscleDelta,
                            int staminaDelta,
                            int fatigueDelta,
                            int moneyDelta,
                            double bodyFatDelta) {
        strength = Math.max(0, strength + strengthDelta);
        muscle = Math.max(0, muscle + muscleDelta);
        stamina = Math.max(0, stamina + staminaDelta);
        fatigue = clamp(fatigue + fatigueDelta, 0, MAX_FATIGUE);
        money = Math.max(0, money + moneyDelta);
        bodyFat = clamp(bodyFat + effectiveBodyFatDelta(bodyFatDelta), MIN_BODY_FAT, MAX_BODY_FAT);
    }

    public void reduceFatigue(int amount) {
        fatigue = clamp(fatigue - amount, 0, MAX_FATIGUE);
    }

    public boolean spendMoney(int amount) {
        if (money < amount) {
            return false;
        }

        money -= amount;
        return true;
    }

    public void addMoney(int amount) {
        money = Math.max(0, money + amount);
    }

    public void configureBaseValues(int strength, int muscle, int stamina, int fatigue, int money, double bodyFat) {
        baseStrength = Math.max(0, strength);
        baseMuscle = Math.max(0, muscle);
        baseStamina = Math.max(0, stamina);
        baseFatigue = clamp(fatigue, 0, MAX_FATIGUE);
        baseMoney = Math.max(0, money);
        baseBodyFat = clamp(bodyFat, MIN_BODY_FAT, MAX_BODY_FAT);
    }

    public void restoreValues(int strength, int muscle, int stamina, int fatigue, int money, double bodyFat) {
        this.strength = Math.max(0, strength);
        this.muscle = Math.max(0, muscle);
        this.stamina = Math.max(0, stamina);
        this.fatigue = clamp(fatigue, 0, MAX_FATIGUE);
        this.money = Math.max(0, money);
        this.bodyFat = clamp(bodyFat, MIN_BODY_FAT, MAX_BODY_FAT);
    }

    public void reset() {
        strength = baseStrength;
        muscle = baseMuscle;
        stamina = baseStamina;
        fatigue = baseFatigue;
        money = baseMoney;
        bodyFat = baseBodyFat;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private double effectiveBodyFatDelta(double bodyFatDelta) {
        if (bodyFatDelta >= 0) {
            return bodyFatDelta;
        }

        double burnRate = clamp((bodyFat - MIN_BODY_FAT) * 0.075, 0.0, 0.80);
        return bodyFatDelta * burnRate;
    }
}
