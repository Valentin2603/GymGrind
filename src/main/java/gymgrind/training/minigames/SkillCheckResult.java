package gymgrind.training.minigames;

import gymgrind.training.TrainingGrade;

public record SkillCheckResult(
        boolean success,
        TrainingGrade grade,
        String message,
        int strengthDelta,
        int muscleDelta,
        int staminaDelta,
        int fatigueDelta,
        int moneyDelta,
        double bodyFatDelta
) {
    public SkillCheckResult(boolean success,
                            String message,
                            int strengthDelta,
                            int muscleDelta,
                            int staminaDelta,
                            int fatigueDelta,
                            double bodyFatDelta) {
        this(
                success,
                success ? TrainingGrade.NORMAL : TrainingGrade.FAIL,
                message,
                strengthDelta,
                muscleDelta,
                staminaDelta,
                fatigueDelta,
                0,
                bodyFatDelta
        );
    }

    public SkillCheckResult(boolean success,
                            TrainingGrade grade,
                            String message,
                            int strengthDelta,
                            int muscleDelta,
                            int staminaDelta,
                            int fatigueDelta,
                            double bodyFatDelta) {
        this(success, grade, message, strengthDelta, muscleDelta, staminaDelta, fatigueDelta, 0, bodyFatDelta);
    }
}
