package gymgrind.training;

public record TrainingReward(int strength, int muscle, int stamina, int fatigue, double bodyFat) {

    public TrainingReward multiply(double rewardMultiplier, double fatigueMultiplier) {
        return new TrainingReward(
                round(strength * rewardMultiplier),
                round(muscle * rewardMultiplier),
                round(stamina * rewardMultiplier),
                Math.max(1, (int) Math.ceil(fatigue * fatigueMultiplier)),
                bodyFat * rewardMultiplier
        );
    }

    public TrainingReward withStrengthMultiplier(double multiplier) {
        return new TrainingReward(round(strength * multiplier), muscle, stamina, fatigue, bodyFat);
    }

    public TrainingReward withMuscleMultiplier(double multiplier) {
        return new TrainingReward(strength, round(muscle * multiplier), stamina, fatigue, bodyFat);
    }

    public TrainingReward withFatigueMultiplier(double multiplier) {
        if (fatigue <= 0) {
            return this;
        }
        return new TrainingReward(strength, muscle, stamina, Math.max(1, (int) Math.ceil(fatigue * multiplier)), bodyFat);
    }

    public TrainingReward scaleProgress(double multiplier) {
        return new TrainingReward(
                Math.max(0, round(strength * multiplier)),
                Math.max(0, round(muscle * multiplier)),
                Math.max(0, round(stamina * multiplier)),
                fatigue,
                bodyFat * multiplier
        );
    }

    private int round(double value) {
        return (int) Math.round(value);
    }
}
