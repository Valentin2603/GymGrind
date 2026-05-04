package gymgrind.model;

public final class Stats {

    private int strength;
    private int muscle;
    private int stamina;
    private int fatigue;
    private int money;

    public Stats(int strength, int muscle, int stamina, int fatigue, int money) {
        this.strength = strength;
        this.muscle = muscle;
        this.stamina = stamina;
        this.fatigue = fatigue;
        this.money = money;
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

    public int money() {
        return money;
    }

    public int form() {
        return strength + muscle + stamina - fatigue / 2;
    }

    public void addTrainingReward(TrainingReward reward) {
        strength += reward.strength();
        muscle += reward.muscle();
        stamina += reward.stamina();
        fatigue = clamp(fatigue + reward.fatigue(), 0, 100);
    }

    public void reduceFatigue(int amount) {
        fatigue = clamp(fatigue - amount, 0, 100);
    }

    public void reset() {
        strength = 10;
        muscle = 10;
        stamina = 10;
        fatigue = 0;
        money = 0;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
