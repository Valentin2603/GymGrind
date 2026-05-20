package gymgrind.daily;

import gymgrind.training.MachineType;

import java.util.List;
import java.util.Set;

public record DailyQuestSaveData(
        List<QuestEntry> quests,
        ProgressData progress
) {
    public DailyQuestSaveData {
        quests = List.copyOf(quests);
    }

    public static DailyQuestSaveData empty() {
        return new DailyQuestSaveData(List.of(), null);
    }

    public boolean hasData() {
        return progress != null && !quests.isEmpty();
    }

    public record QuestEntry(
            DailyQuestType type,
            int progress,
            boolean completed
    ) {
    }

    public record ProgressData(
            String profileId,
            int startStrength,
            int startMuscle,
            int startStamina,
            int startForm,
            int nextFormDecade,
            int trainingCount,
            int lightTrainingCount,
            int mediumTrainingCount,
            int heavyTrainingCount,
            int heavyNoFailCount,
            int excellentCount,
            int noFailTrainingCount,
            int workCount,
            int moneyEarned,
            int restCount,
            int restoredFatigue,
            int maxFatigue,
            int minFatigue,
            int strengthGained,
            int muscleGained,
            int staminaGained,
            int formGained,
            int anyStatGained,
            int cardioCount,
            int strengthTrainingCount,
            int heavyStrengthTrainingCount,
            boolean failedTrainingToday,
            boolean overtrainedToday,
            boolean spentMoney,
            boolean boughtSupplement,
            boolean boughtProtein,
            boolean boughtCreatine,
            boolean boughtPreWorkout,
            boolean boughtEnergy,
            boolean boughtAndTrained,
            boolean usedSupplement,
            boolean workNormalOrBetter,
            boolean workingLoadUp,
            boolean fatReduced,
            boolean formNextTen,
            boolean highFatigueNoFail,
            boolean cardioAfterStrength,
            boolean strengthAfterCardio,
            boolean lastTrainingWasCardio,
            boolean lastTrainingWasStrength,
            boolean lastTamikTreadmillExcellent,
            boolean drunTreadmillNoFail,
            boolean drunFatigueReducedUnder40,
            boolean stageAttempted,
            Set<MachineType> trainedMachines
    ) {
        public ProgressData {
            trainedMachines = Set.copyOf(trainedMachines);
        }
    }
}
