package gymgrind.training;

import gymgrind.player.Player;
import gymgrind.player.Stats;
import gymgrind.shop.SupplementService;
import gymgrind.shop.SupplementType;

public final class TrainingService {

    private static final double STAMINA_COST_MULTIPLIER = 1.3;
    private static final int TAMIK_BASE_STRENGTH = 115;
    private static final double WORKING_LOAD_CURVE_SCALE = 85.0;
    private static final int MIN_WORKING_LOAD = 30;
    private static final int MAX_WORKING_LOAD = 90;

    private final SupplementService supplementService;

    public TrainingService(SupplementService supplementService) {
        this.supplementService = supplementService;
    }

    public TrainingSession createSession(Player player, TrainingMachine machine, TrainingWeight weight) {
        Stats stats = player.stats();
        FatigueProfile fatigueProfile = FatigueProfile.fromFatigue(stats.fatigue());
        double strengthBonus = statBonus(stats.strength());
        double muscleBonus = statBonus(stats.muscle());
        double staminaBonus = statBonus(stats.stamina());
        double bodyFatLoad = bodyFatLoad(stats.bodyFat());
        double muscleLoad = muscleLoad(stats.muscle());
        double cardioLoad = machine.machineType() == MachineType.TREADMILL ? muscleLoad : 0.0;
        double squatLoad = machine.machineType() == MachineType.SQUAT_RACK ? muscleLoad * 0.35 : 0.0;
        int workingLoad = workingLoad(player, machine);
        int selectedLoad = selectedLoad(player, machine, weight);
        double progressSlowdown = progressSlowdown(workingLoad);

        double speedMultiplier = fatigueProfile.speedMultiplier()
                * weight.speedMultiplier()
                * (1.0 + bodyFatLoad * 0.26 + cardioLoad * 0.16 + squatLoad * 0.08)
                * (1.0 - staminaBonus * 0.09);
        boolean hadPreWorkout = player.activeSupplements().has(SupplementType.PRE_WORKOUT);
        speedMultiplier = supplementService.applySpeedBonuses(player.activeSupplements(), speedMultiplier);

        double zoneMultiplier = fatigueProfile.zoneMultiplier()
                * weight.zoneMultiplier()
                * clamp(1.0 - bodyFatLoad * 0.10 - cardioLoad * 0.10 - squatLoad * 0.05 + staminaBonus * 0.06, 0.74, 1.10);
        zoneMultiplier = supplementService.applyZoneBonuses(player.activeSupplements(), zoneMultiplier);
        int bodyLoadRhythmPenalty = Math.max(0, (int) Math.round(bodyFatLoad * 1.2 + cardioLoad * 1.3 + squatLoad * 0.8 - staminaBonus * 1.0));
        int rhythmLength = Math.max(4, 6 + fatigueProfile.rhythmLengthBonus() + weight.rhythmLengthBonus() + bodyLoadRhythmPenalty);
        double rhythmTime = 15.0;

        TrainingReward reward = baseReward(machine.machineType()).multiply(
                weight.rewardMultiplier() * progressSlowdown,
                weight.fatigueMultiplier()
                        * STAMINA_COST_MULTIPLIER
                        * stats.staminaFatigueMultiplier()
                        * (1.0 + bodyFatLoad * 0.12 + cardioLoad * 0.08)
        );
        reward = supplementService.applyRewardBonuses(player.activeSupplements(), reward);

        TrainingTuning tuning = new TrainingTuning(
                speedMultiplier,
                zoneMultiplier,
                rhythmLength,
                rhythmTime,
                hadPreWorkout,
                strengthBonus,
                muscleBonus,
                staminaBonus,
                bodyFatLoad,
                muscleLoad,
                fatigueProfile
        );

        return new TrainingSession(machine, weight, workingLoad, selectedLoad, loadUnit(machine.machineType()), reward, tuning);
    }

    public String workingLoadLabel(Player player, TrainingMachine machine) {
        return workingLoadValue(player, machine) + " " + loadUnit(machine.machineType());
    }

    public int workingLoadValue(Player player, TrainingMachine machine) {
        return workingLoad(player, machine);
    }

    public String weightChoiceLabel(Player player, TrainingMachine machine, TrainingWeight weight) {
        return weight.label()
                + " - "
                + selectedLoad(player, machine, weight)
                + " "
                + loadUnit(machine.machineType())
                + " ("
                + weight.loadHint()
                + ")";
    }

    public TrainingOutcome finishTraining(Player player, TrainingSession session, MinigameResult result) {
        TrainingReward finalReward = session.reward().scaleProgress(result.grade().rewardMultiplier());
        player.stats().addTrainingReward(finalReward);
        return new TrainingOutcome(finalReward, buildResultDescription(session, result));
    }

    public boolean isSupportedMinigame(MachineType machineType) {
        return machineType == MachineType.BENCH_PRESS
                || machineType == MachineType.DEADLIFT_PLATFORM;
    }

    private TrainingReward baseReward(MachineType machineType) {
        return switch (machineType) {
            case BENCH_PRESS -> new TrainingReward(10, 6, 0, 9, 0);
            case SQUAT_RACK -> new TrainingReward(8, 10, 3, 11, 0);
            case TREADMILL -> new TrainingReward(0, 0, 12, 7, -2);
            case DEADLIFT_PLATFORM -> new TrainingReward(14, 5, 0, 13, 0);
        };
    }

    private String buildResultDescription(TrainingSession session, MinigameResult result) {
        String base = switch (session.machine().machineType()) {
            case BENCH_PRESS -> switch (result.grade()) {
                case EXCELLENT -> "Жим получился мощным и ровным: штанга почти не гуляла.";
                case NORMAL -> "Жим засчитан: подход рабочий, но штангу немного водило.";
                case FAIL -> "Жим вышел тяжёлым: контроль штанги сорвался, прогресс снижен.";
            };
            case SQUAT_RACK -> switch (result.grade()) {
                case EXCELLENT -> "Присед отличный: ритм, глубина и техника удержаны почти без ошибок.";
                case NORMAL -> "Присед засчитан: подход дожат, но техника была не идеально стабильной.";
                case FAIL -> "Присед сорвался: ритм потерян, поэтому прогресс сильно снижен.";
            };
            case TREADMILL -> switch (result.grade()) {
                case EXCELLENT -> "Беговая прошла отлично: интервалы выдержаны в хорошем темпе.";
                case NORMAL -> "Беговая засчитана: темп удержан, но были неточные интервалы.";
                case FAIL -> "Беговая далась тяжело: темп часто сбивался, прогресс снижен.";
            };
            case DEADLIFT_PLATFORM -> switch (result.grade()) {
                case EXCELLENT -> "Становая отличная: сила держалась в рабочей зоне почти весь подход.";
                case NORMAL -> "Становая засчитана: подъём выполнен, но мощность плавала.";
                case FAIL -> "Становая сорвалась: сила ушла мимо рабочей зоны, прогресс снижен.";
            };
        };

        if (result.details() == null || result.details().isBlank()) {
            return session.machine().name() + ": " + result.grade().label() + ". " + base;
        }

        return session.machine().name()
                + ": "
                + result.grade().label()
                + ". "
                + base
                + " "
                + result.details();
    }

    private double statBonus(int stat) {
        return stat / (stat + 260.0);
    }

    private int workingLoad(Player player, TrainingMachine machine) {
        if (machine.machineType() == MachineType.TREADMILL) {
            return cardioLoad(player.stats().stamina());
        }

        int baseLoad = baseWorkingLoad(machine.machineType());
        double extraStrength = Math.max(0, player.stats().strength() - TAMIK_BASE_STRENGTH);
        double curvedProgress = 1.0 - Math.exp(-extraStrength / WORKING_LOAD_CURVE_SCALE);
        int load = (int) Math.round(baseLoad + (MAX_WORKING_LOAD - baseLoad) * curvedProgress);
        return clamp(load, MIN_WORKING_LOAD, MAX_WORKING_LOAD);
    }

    private int selectedLoad(Player player, TrainingMachine machine, TrainingWeight weight) {
        return clamp(
                (int) Math.round(workingLoad(player, machine) * weight.loadMultiplier()),
                MIN_WORKING_LOAD,
                MAX_WORKING_LOAD
        );
    }

    private double progressSlowdown(int workingLoad) {
        double progress = (workingLoad - MIN_WORKING_LOAD) / (double) (MAX_WORKING_LOAD - MIN_WORKING_LOAD);
        return clamp(1.06 - progress * 0.34, 0.72, 1.06);
    }

    private int baseWorkingLoad(MachineType machineType) {
        return switch (machineType) {
            case BENCH_PRESS -> 40;
            case SQUAT_RACK -> 50;
            case DEADLIFT_PLATFORM -> 60;
            case TREADMILL -> 45;
        };
    }

    private int cardioLoad(int stamina) {
        double curvedProgress = 1.0 - Math.exp(-stamina / 260.0);
        int load = (int) Math.round(MIN_WORKING_LOAD + (MAX_WORKING_LOAD - MIN_WORKING_LOAD) * curvedProgress);
        return clamp(load, MIN_WORKING_LOAD, MAX_WORKING_LOAD);
    }

    private String loadUnit(MachineType machineType) {
        return machineType == MachineType.TREADMILL ? "ур." : "кг";
    }

    private double bodyFatLoad(double bodyFat) {
        return clamp((bodyFat - 14.0) / 56.0, 0.0, 1.0);
    }

    private double muscleLoad(int muscle) {
        return clamp((muscle - 170.0) / 180.0, 0.0, 1.0);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}
