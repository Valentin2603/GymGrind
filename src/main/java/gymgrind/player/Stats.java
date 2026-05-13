package gymgrind.player;

import gymgrind.training.TrainingReward;

public final class Stats {

    private static final int MIN_BODY_FAT = 3;
    private static final int MAX_BODY_FAT = 100;
    private static final int MAX_FATIGUE = 100;

    private int strength;
    private int muscle;
    private int stamina;
    private int fatigue;
    private int money;
    private int bodyFat;

    private int baseStrength;
    private int baseMuscle;
    private int baseStamina;
    private int baseFatigue;
    private int baseMoney;
    private int baseBodyFat;

    public Stats(int strength, int muscle, int stamina, int fatigue, int money, int bodyFat) {
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

    public int bodyFat() {
        return bodyFat;
    }

    public int form() {
        return strength + muscle + stamina - fatigue / 2 - bodyFat;
    }

    public void addTrainingReward(TrainingReward reward) {
        applyDeltas(reward.strength(), reward.muscle(), reward.stamina(), reward.fatigue(), 0, reward.bodyFat());
    }

    public void applyDeltas(int strengthDelta,
                            int muscleDelta,
                            int staminaDelta,
                            int fatigueDelta,
                            int moneyDelta,
                            int bodyFatDelta) {
        strength = Math.max(0, strength + strengthDelta);
        muscle = Math.max(0, muscle + muscleDelta);
        stamina = Math.max(0, stamina + staminaDelta);
        fatigue = clamp(fatigue + fatigueDelta, 0, MAX_FATIGUE);
        money = Math.max(0, money + moneyDelta);
        bodyFat = clamp(bodyFat + bodyFatDelta, MIN_BODY_FAT, MAX_BODY_FAT);
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

    public void configureBaseValues(int strength, int muscle, int stamina, int fatigue, int money, int bodyFat) {
        baseStrength = Math.max(0, strength);
        baseMuscle = Math.max(0, muscle);
        baseStamina = Math.max(0, stamina);
        baseFatigue = clamp(fatigue, 0, MAX_FATIGUE);
        baseMoney = Math.max(0, money);
        baseBodyFat = clamp(bodyFat, MIN_BODY_FAT, MAX_BODY_FAT);
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
}
