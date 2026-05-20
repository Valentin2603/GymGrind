package gymgrind.shop;

import gymgrind.player.ActiveSupplements;
import gymgrind.training.TrainingReward;

public final class SupplementService {

    public TrainingReward applyRewardBonuses(ActiveSupplements supplements, TrainingReward reward) {
        TrainingReward result = reward;

        if (supplements.has(SupplementType.CREATINE) && result.strength() > 0) {
            result = result.withStrengthMultiplier(1.65);
            supplements.consume(SupplementType.CREATINE);
        }

        if (supplements.has(SupplementType.PROTEIN) && result.muscle() > 0) {
            result = result.withMuscleMultiplier(1.65);
            supplements.consume(SupplementType.PROTEIN);
        }

        if (supplements.has(SupplementType.KNEE_SLEEVES) && result.fatigue() > 0) {
            result = result.withFatigueMultiplier(0.70);
            supplements.consume(SupplementType.KNEE_SLEEVES);
        }

        return result;
    }

    public TrainingReward applyPermanentShotBonus(boolean recoveryShotPurchased, TrainingReward reward) {
        if (!recoveryShotPurchased) {
            return reward;
        }

        return reward
                .withStrengthMultiplier(1.18)
                .withMuscleMultiplier(1.18)
                .withStaminaMultiplier(1.18)
                .withFatigueMultiplier(0.82);
    }

    public double applySpeedBonuses(ActiveSupplements supplements, double speedMultiplier) {
        if (!supplements.has(SupplementType.PRE_WORKOUT)) {
            return speedMultiplier;
        }

        return speedMultiplier * 0.80;
    }

    public double applyZoneBonuses(ActiveSupplements supplements, double zoneMultiplier) {
        if (!supplements.has(SupplementType.HAND_WRAPS)) {
            return zoneMultiplier;
        }

        supplements.consume(SupplementType.HAND_WRAPS);
        return zoneMultiplier * 1.22;
    }

    public double applyPermanentShotSpeedBonus(boolean recoveryShotPurchased, double speedMultiplier) {
        return recoveryShotPurchased ? speedMultiplier * 0.92 : speedMultiplier;
    }

    public double applyPermanentShotZoneBonus(boolean recoveryShotPurchased, double zoneMultiplier) {
        return recoveryShotPurchased ? zoneMultiplier * 1.06 : zoneMultiplier;
    }
}
