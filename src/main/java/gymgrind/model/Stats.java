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

    public void applyDeltas(int strengthDelta, int muscleDelta, int staminaDelta, int fatigueDelta, int moneyDelta) {
        strength = Math.max(0, strength + strengthDelta);
        muscle = Math.max(0, muscle + muscleDelta);
        stamina = Math.max(0, stamina + staminaDelta);
        fatigue = Math.max(0, fatigue + fatigueDelta);
        money = Math.max(0, money + moneyDelta);
    }

    public void reset() {
        strength = 10;
        muscle = 10;
        stamina = 10;
        fatigue = 0;
        money = 0;
    }
}
