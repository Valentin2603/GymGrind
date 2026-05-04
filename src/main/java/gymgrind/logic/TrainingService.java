package gymgrind.logic;

import gymgrind.model.FatigueProfile;
import gymgrind.model.MachineType;
import gymgrind.model.MinigameResult;
import gymgrind.model.Player;
import gymgrind.model.TrainingMachine;
import gymgrind.model.TrainingOutcome;
import gymgrind.model.TrainingReward;
import gymgrind.model.TrainingSession;
import gymgrind.model.TrainingTuning;
import gymgrind.model.TrainingWeight;

public final class TrainingService {

    private final SupplementService supplementService;

    public TrainingService(SupplementService supplementService) {
        this.supplementService = supplementService;
    }

    public TrainingSession createSession(Player player, TrainingMachine machine, TrainingWeight weight) {
        FatigueProfile fatigueProfile = FatigueProfile.fromFatigue(player.stats().fatigue());

        double speedMultiplier = fatigueProfile.speedMultiplier() * weight.speedMultiplier();
        boolean hadPreWorkout = player.activeSupplements().has(gymgrind.model.SupplementType.PRE_WORKOUT);
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
                + ", усталость +" + finalReward.fatigue() + ".";

        return new TrainingOutcome(finalReward, message);
    }

    public boolean isSupportedMinigame(MachineType machineType) {
        return machineType == MachineType.TREADMILL || machineType == MachineType.DEADLIFT_PLATFORM;
    }

    private TrainingReward baseReward(MachineType machineType) {
        return switch (machineType) {
            case BENCH_PRESS -> new TrainingReward(4, 2, 0, 15);
            case SQUAT_RACK -> new TrainingReward(3, 4, 0, 20);
            case TREADMILL -> new TrainingReward(0, 0, 4, 10);
            case DEADLIFT_PLATFORM -> new TrainingReward(5, 2, 0, 22);
        };
    }
}
