package gymgrind.model;

public record TrainingReward(int strength, int muscle, int stamina, int fatigue) {

    public TrainingReward multiply(double rewardMultiplier, double fatigueMultiplier) {
        return new TrainingReward(
                round(strength * rewardMultiplier),
                round(muscle * rewardMultiplier),
                round(stamina * rewardMultiplier),
                Math.max(1, round(fatigue * fatigueMultiplier))
        );
    }

    public TrainingReward withStrengthMultiplier(double multiplier) {
        return new TrainingReward(round(strength * multiplier), muscle, stamina, fatigue);
    }

    public TrainingReward withMuscleMultiplier(double multiplier) {
        return new TrainingReward(strength, round(muscle * multiplier), stamina, fatigue);
    }

    public TrainingReward scaleProgress(double multiplier) {
        return new TrainingReward(
                Math.max(0, round(strength * multiplier)),
                Math.max(0, round(muscle * multiplier)),
                Math.max(0, round(stamina * multiplier)),
                fatigue
        );
    }

    private int round(double value) {
        return (int) Math.round(value);
    }
}
