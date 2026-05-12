package gymgrind.training;

import gymgrind.player.Player;
import gymgrind.shop.SupplementService;
import gymgrind.shop.SupplementType;

public final class TrainingService {

    private final SupplementService supplementService;

    public TrainingService(SupplementService supplementService) {
        this.supplementService = supplementService;
    }

    public TrainingSession createSession(Player player, TrainingMachine machine, TrainingWeight weight) {
        FatigueProfile fatigueProfile = FatigueProfile.fromFatigue(player.stats().fatigue());

        double speedMultiplier = fatigueProfile.speedMultiplier() * weight.speedMultiplier();
        boolean hadPreWorkout = player.activeSupplements().has(SupplementType.PRE_WORKOUT);
        speedMultiplier = supplementService.applySpeedBonuses(player.activeSupplements(), speedMultiplier);

        double zoneMultiplier = fatigueProfile.zoneMultiplier() * weight.zoneMultiplier();
        int rhythmLength = Math.max(4, 6 + fatigueProfile.rhythmLengthBonus() + weight.rhythmLengthBonus());
        double rhythmTime = 15.0;

        TrainingReward reward = baseReward(machine.machineType()).multiply(
                weight.rewardMultiplier(),
                weight.fatigueMultiplier()
        );
        reward = supplementService.applyRewardBonuses(player.activeSupplements(), reward);

        TrainingTuning tuning = new TrainingTuning(
                speedMultiplier,
                zoneMultiplier,
                rhythmLength,
                rhythmTime,
                hadPreWorkout,
                fatigueProfile
        );

        return new TrainingSession(machine, weight, reward, tuning);
    }

    public TrainingOutcome finishTraining(Player player, TrainingSession session, MinigameResult result) {
        TrainingReward finalReward = session.reward().scaleProgress(result.grade().rewardMultiplier());
        player.stats().addTrainingReward(finalReward);

        String message = session.machine().name()
                + ": " + result.grade().label()
                + ". " + result.details()
                + " Получено: сила +" + finalReward.strength()
                + ", масса +" + finalReward.muscle()
                + ", выносливость +" + finalReward.stamina()
                + ", усталость +" + finalReward.fatigue()
                + bodyFatSuffix(finalReward) + ".";

        return new TrainingOutcome(finalReward, message);
    }

    public boolean isSupportedMinigame(MachineType machineType) {
        return machineType == MachineType.BENCH_PRESS
                || machineType == MachineType.DEADLIFT_PLATFORM;
    }

    private TrainingReward baseReward(MachineType machineType) {
        return switch (machineType) {
            case BENCH_PRESS -> new TrainingReward(4, 2, 0, 15, 0);
            case SQUAT_RACK -> new TrainingReward(3, 4, 1, 20, 0);
            case TREADMILL -> new TrainingReward(0, 0, 5, 10, -2);
            case DEADLIFT_PLATFORM -> new TrainingReward(5, 2, 0, 22, 0);
        };
    }

    private String bodyFatSuffix(TrainingReward reward) {
        if (reward.bodyFat() == 0) {
            return "";
        }
        return ", % жира " + reward.bodyFat() + "%";
    }
}
