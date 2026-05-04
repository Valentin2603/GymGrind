package gymgrind.logic;

import gymgrind.model.ActiveSupplements;
import gymgrind.model.SupplementType;
import gymgrind.model.TrainingReward;

public final class SupplementService {

    public TrainingReward applyRewardBonuses(ActiveSupplements supplements, TrainingReward reward) {
        TrainingReward result = reward;

        if (supplements.has(SupplementType.CREATINE)) {
            result = result.withStrengthMultiplier(1.5);
            supplements.consume(SupplementType.CREATINE);
        }

        if (supplements.has(SupplementType.PROTEIN)) {
            result = result.withMuscleMultiplier(1.5);
            supplements.consume(SupplementType.PROTEIN);
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
}
