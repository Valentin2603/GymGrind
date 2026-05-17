package gymgrind.shop;

import gymgrind.player.ActiveSupplements;
import gymgrind.training.TrainingReward;

public final class SupplementService {

    public TrainingReward applyRewardBonuses(ActiveSupplements supplements, TrainingReward reward) {
        TrainingReward result = reward;

        if (supplements.has(SupplementType.CREATINE) && result.strength() > 0) {
            result = result.withStrengthMultiplier(1.5);
            supplements.consume(SupplementType.CREATINE);
        }

        if (supplements.has(SupplementType.ELBOW_WRAPS) && result.strength() > 0) {
            result = result.withStrengthMultiplier(1.25);
            supplements.consume(SupplementType.ELBOW_WRAPS);
        }

        if (supplements.has(SupplementType.PROTEIN) && result.muscle() > 0) {
            result = result.withMuscleMultiplier(1.5);
            supplements.consume(SupplementType.PROTEIN);
        }

        if (supplements.has(SupplementType.KNEE_SLEEVES) && result.fatigue() > 0) {
            result = result.withFatigueMultiplier(0.80);
            supplements.consume(SupplementType.KNEE_SLEEVES);
        }

        return result;
    }

    public double applySpeedBonuses(ActiveSupplements supplements, double speedMultiplier) {
        if (!supplements.has(SupplementType.PRE_WORKOUT)) {
            return speedMultiplier;
        }

        supplements.consume(SupplementType.PRE_WORKOUT);
        return speedMultiplier * 0.80;
    }

    public double applyZoneBonuses(ActiveSupplements supplements, double zoneMultiplier) {
        if (!supplements.has(SupplementType.HAND_WRAPS)) {
            return zoneMultiplier;
        }

        supplements.consume(SupplementType.HAND_WRAPS);
        return zoneMultiplier * 1.15;
    }
}
