package gymgrind.daily;

import gymgrind.player.Player;
import gymgrind.shop.SupplementType;
import gymgrind.training.MachineType;
import gymgrind.training.TrainingGrade;
import gymgrind.training.TrainingSession;
import gymgrind.training.TrainingWeight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class DailyQuestManager {

    private static final int QUESTS_PER_DAY = 4;

    private final List<DailyQuest> activeQuests = new ArrayList<>();
    private DailyProgress progress;

    public void startNewDay(Player player, int day) {
        progress = DailyProgress.start(player);
        activeQuests.clear();

        List<DailyQuestType> pool = Arrays.stream(DailyQuestType.values())
                .filter(type -> type.isEligible(player, day))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        Collections.shuffle(pool, new Random(day * 97L + player.profile().id().hashCode()));

        for (DailyQuestType type : pool.stream().limit(QUESTS_PER_DAY).toList()) {
            activeQuests.add(new DailyQuest(type));
        }
    }

    public List<DailyQuestView> views() {
        return activeQuests.stream()
                .map(quest -> new DailyQuestView(
                        quest.type().title(),
                        progressText(quest),
                        quest.completed()
                ))
                .toList();
    }

    public List<DailyQuestNotification> onTraining(Player player,
                                                   TrainingSession session,
                                                   TrainingGrade grade,
                                                   DailyQuestSnapshot before,
                                                   int workingLoadAfter) {
        boolean isFail = grade == TrainingGrade.FAIL;
        boolean isNormalOrBetter = grade != TrainingGrade.FAIL;
        boolean isExcellent = grade == TrainingGrade.EXCELLENT;
        boolean isHeavy = session.weight() == TrainingWeight.HEAVY;
        boolean isCardio = session.machine().machineType() == MachineType.TREADMILL;
        boolean isStrengthTraining = !isCardio;
        boolean supplementUsed = before.activeSupplements().size() > player.activeSupplements().activeTypes().size();

        progress.trainingCount++;
        progress.trainedMachines.add(session.machine().machineType());
        progress.maxFatigue = Math.max(progress.maxFatigue, player.stats().fatigue());
        progress.minFatigue = Math.min(progress.minFatigue, player.stats().fatigue());
        progress.failedTrainingToday |= isFail;
        progress.overtrainedToday |= player.stats().fatigue() >= 100;

        if (isExcellent) {
            progress.excellentCount++;
        }
        if (isNormalOrBetter) {
            progress.noFailTrainingCount++;
        }
        if (isHeavy && isNormalOrBetter) {
            progress.heavyNoFailCount++;
        }
        if (isHeavy) {
            progress.heavyTrainingCount++;
        }
        if (isHeavy && isStrengthTraining) {
            progress.heavyStrengthTrainingCount++;
        }
        if (session.weight() == TrainingWeight.LIGHT) {
            progress.lightTrainingCount++;
        }
        if (session.weight() == TrainingWeight.MEDIUM) {
            progress.mediumTrainingCount++;
        }
        if (isCardio) {
            progress.cardioCount++;
        }
        if (isStrengthTraining) {
            progress.strengthTrainingCount++;
        }
        if (progress.lastTrainingWasStrength && isCardio) {
            progress.cardioAfterStrength = true;
        }
        if (progress.lastTrainingWasCardio && isStrengthTraining) {
            progress.strengthAfterCardio = true;
        }
        progress.lastTrainingWasCardio = isCardio;
        progress.lastTrainingWasStrength = isStrengthTraining;
        if (before.fatigue() > 60 && isNormalOrBetter) {
            progress.highFatigueNoFail = true;
        }
        if (supplementUsed) {
            progress.usedSupplement = true;
        }
        if (progress.boughtSupplement && progress.trainingCount > 0) {
            progress.boughtAndTrained = true;
        }
        progress.lastTamikTreadmillExcellent |= progress.profileId.equals("street_rookie") && isCardio && isExcellent;
        progress.drunTreadmillNoFail |= progress.profileId.equals("dark_drun") && isCardio && isNormalOrBetter;

        progress.strengthGained = player.stats().strength() - progress.startStrength;
        progress.muscleGained = player.stats().muscle() - progress.startMuscle;
        progress.staminaGained = player.stats().stamina() - progress.startStamina;
        progress.formGained = player.stats().form() - progress.startForm;
        progress.anyStatGained = Math.max(progress.strengthGained, Math.max(progress.muscleGained, progress.staminaGained));
        progress.fatReduced = progress.fatReduced || player.stats().bodyFat() < before.bodyFat();
        progress.formNextTen = progress.formNextTen || player.stats().form() >= progress.nextFormDecade;
        progress.workingLoadUp = progress.workingLoadUp || workingLoadAfter > session.workingLoad();

        return checkActiveQuests(player, false);
    }

    public List<DailyQuestNotification> onWork(Player player, TrainingGrade grade, int moneyDelta) {
        progress.workCount++;
        progress.moneyEarned += moneyDelta;
        progress.maxFatigue = Math.max(progress.maxFatigue, player.stats().fatigue());
        progress.minFatigue = Math.min(progress.minFatigue, player.stats().fatigue());
        progress.workNormalOrBetter |= grade != TrainingGrade.FAIL;
        progress.overtrainedToday |= player.stats().fatigue() >= 100;
        return checkActiveQuests(player, false);
    }

    public List<DailyQuestNotification> onPurchase(Player player, SupplementType supplementType) {
        progress.boughtSupplement = true;
        progress.spentMoney = true;
        progress.boughtProtein |= supplementType == SupplementType.PROTEIN;
        progress.boughtCreatine |= supplementType == SupplementType.CREATINE;
        progress.boughtPreWorkout |= supplementType == SupplementType.PRE_WORKOUT;
        progress.boughtEnergy |= supplementType == SupplementType.ENERGY_DRINK;
        if (progress.trainingCount > 0) {
            progress.boughtAndTrained = true;
        }
        return checkActiveQuests(player, false);
    }

    public List<DailyQuestNotification> onRest(Player player, int restoredFatigue) {
        progress.restCount++;
        progress.restoredFatigue += restoredFatigue;
        progress.minFatigue = Math.min(progress.minFatigue, player.stats().fatigue());
        return checkActiveQuests(player, false);
    }

    public List<DailyQuestNotification> onDayEnd(Player player) {
        progress.minFatigue = Math.min(progress.minFatigue, player.stats().fatigue());
        return checkActiveQuests(player, true);
    }

    public List<DailyQuestNotification> onStage(Player player) {
        progress.stageAttempted = true;
        return checkActiveQuests(player, false);
    }

    private List<DailyQuestNotification> checkActiveQuests(Player player, boolean endOfDay) {
        List<DailyQuestNotification> notifications = new ArrayList<>();
        for (DailyQuest quest : activeQuests) {
            if (quest.completed()) {
                continue;
            }
            if (quest.type().endOfDayOnly() && !endOfDay) {
                continue;
            }

            int value = valueFor(quest.type(), player);
            quest.setProgress(value);
            if (value >= quest.type().target() && quest.markCompleted()) {
                quest.type().bonus().apply(player);
                notifications.add(new DailyQuestNotification(
                        quest.type().title(),
                        quest.type().bonus().description()
                ));
            }
        }
        return notifications;
    }

    private int valueFor(DailyQuestType type, Player player) {
        return switch (type) {
            case TWO_TRAININGS, THREE_TRAININGS -> progress.trainingCount;
            case LIGHT_TRAINING -> progress.lightTrainingCount;
            case MEDIUM_TRAINING -> progress.mediumTrainingCount;
            case HEAVY_TRAINING -> progress.heavyTrainingCount;
            case THREE_DIFFERENT_TRAININGS, ALL_FOUR_TRAININGS -> progress.trainedMachines.size();
            case NORMAL_OR_BETTER, NO_FAIL_TRAINING -> progress.noFailTrainingCount;
            case EXCELLENT_ANY, EXCELLENT_TWO -> progress.excellentCount;
            case HEAVY_NO_FAIL -> progress.heavyNoFailCount;
            case FORM_PLUS_10, FORM_PLUS_20 -> progress.formGained;
            case STRENGTH_PLUS_15 -> progress.strengthGained;
            case MUSCLE_PLUS_15 -> progress.muscleGained;
            case STAMINA_PLUS_15 -> progress.staminaGained;
            case ANY_STAT_PLUS_20 -> progress.anyStatGained;
            case WORKING_LOAD_UP -> bool(progress.workingLoadUp);
            case FAT_REDUCED -> bool(progress.fatReduced);
            case FORM_NEXT_TEN -> bool(progress.formNextTen);
            case END_FATIGUE_UNDER_40 -> bool(player.stats().fatigue() < 40);
            case END_FATIGUE_UNDER_25 -> bool(player.stats().fatigue() < 25);
            case REST_ONCE -> progress.restCount;
            case RESTORE_30_FATIGUE -> progress.restoredFatigue;
            case MAX_FATIGUE_UNDER_70 -> bool(progress.maxFatigue <= 70);
            case NO_OVERTRAINING_DAY -> bool(!progress.overtrainedToday);
            case HIGH_FATIGUE_NO_FAIL -> bool(progress.highFatigueNoFail);
            case EARN_100, EARN_200 -> progress.moneyEarned;
            case WORK_ONCE -> progress.workCount;
            case WORK_NORMAL_OR_BETTER -> bool(progress.workNormalOrBetter);
            case BUY_ANY_SUPPLEMENT -> bool(progress.boughtSupplement);
            case BUY_PROTEIN -> bool(progress.boughtProtein);
            case BUY_CREATINE -> bool(progress.boughtCreatine);
            case BUY_PRE_WORKOUT -> bool(progress.boughtPreWorkout);
            case BUY_ENERGY -> bool(progress.boughtEnergy);
            case USE_SUPPLEMENT -> bool(progress.usedSupplement);
            case BUY_AND_TRAIN -> bool(progress.boughtAndTrained);
            case TRAIN_AND_WORK -> bool(progress.trainingCount > 0 && progress.workCount > 0);
            case CARDIO_AFTER_STRENGTH -> bool(progress.cardioAfterStrength);
            case STRENGTH_AFTER_CARDIO -> bool(progress.strengthAfterCardio);
            case NO_SPEND_DAY -> bool(!progress.spentMoney);
            case MONEY_300 -> bool(player.stats().money() >= 300);
            case TAMIK_TREADMILL_EXCELLENT -> bool(progress.profileId.equals("street_rookie")
                    && progress.lastTamikTreadmillExcellent);
            case TAMIK_HEAVY_STRENGTH -> bool(progress.profileId.equals("street_rookie")
                    && progress.heavyStrengthTrainingCount > 0);
            case DRUN_TREADMILL_NO_FAIL -> bool(progress.profileId.equals("dark_drun")
                    && progress.drunTreadmillNoFail);
            case DRUN_FATIGUE_UNDER_40 -> bool(progress.profileId.equals("dark_drun")
                    && player.stats().fatigue() < 40);
            case FATTY_CARDIO -> bool(progress.profileId.equals("fatty_popka") && progress.cardioCount > 0);
            case FATTY_TWO_NO_FAIL -> progress.profileId.equals("fatty_popka") ? progress.noFailTrainingCount : 0;
            case FORM_300 -> bool(player.stats().form() >= 300);
            case FORM_400 -> bool(player.stats().form() >= 400);
            case STAGE_FATIGUE_UNDER_60 -> bool(progress.stageAttempted && player.stats().fatigue() < 60);
            case STAGE_AFTER_PURCHASE -> bool(progress.stageAttempted && progress.boughtSupplement);
            case STAGE_AFTER_NO_FAIL_DAY -> bool(progress.stageAttempted && !progress.failedTrainingToday);
            case STAGE_READY -> bool(player.stats().form() >= 320 && player.stats().fatigue() < 50);
        };
    }

    private String progressText(DailyQuest quest) {
        if (quest.completed()) {
            return "готово";
        }
        if (quest.type().endOfDayOnly()) {
            return "проверка в конце дня";
        }
        return quest.progress() + "/" + quest.type().target();
    }

    private int bool(boolean value) {
        return value ? 1 : 0;
    }

    private static final class DailyProgress {
        private final String profileId;
        private final int startStrength;
        private final int startMuscle;
        private final int startStamina;
        private final int startForm;
        private final int nextFormDecade;
        private int trainingCount;
        private int lightTrainingCount;
        private int mediumTrainingCount;
        private int heavyTrainingCount;
        private int heavyNoFailCount;
        private int excellentCount;
        private int noFailTrainingCount;
        private int workCount;
        private int moneyEarned;
        private int restCount;
        private int restoredFatigue;
        private int maxFatigue;
        private int minFatigue;
        private int strengthGained;
        private int muscleGained;
        private int staminaGained;
        private int formGained;
        private int anyStatGained;
        private int cardioCount;
        private int strengthTrainingCount;
        private int heavyStrengthTrainingCount;
        private boolean failedTrainingToday;
        private boolean overtrainedToday;
        private boolean spentMoney;
        private boolean boughtSupplement;
        private boolean boughtProtein;
        private boolean boughtCreatine;
        private boolean boughtPreWorkout;
        private boolean boughtEnergy;
        private boolean boughtAndTrained;
        private boolean usedSupplement;
        private boolean workNormalOrBetter;
        private boolean workingLoadUp;
        private boolean fatReduced;
        private boolean formNextTen;
        private boolean highFatigueNoFail;
        private boolean cardioAfterStrength;
        private boolean strengthAfterCardio;
        private boolean lastTrainingWasCardio;
        private boolean lastTrainingWasStrength;
        private boolean lastTamikTreadmillExcellent;
        private boolean drunTreadmillNoFail;
        private boolean stageAttempted;
        private final Set<MachineType> trainedMachines = EnumSet.noneOf(MachineType.class);

        private DailyProgress(Player player) {
            profileId = player.profile().id();
            startStrength = player.stats().strength();
            startMuscle = player.stats().muscle();
            startStamina = player.stats().stamina();
            startForm = player.stats().form();
            nextFormDecade = startForm / 10 * 10 + 10;
            maxFatigue = player.stats().fatigue();
            minFatigue = player.stats().fatigue();
        }

        private static DailyProgress start(Player player) {
            return new DailyProgress(player);
        }

    }
}
