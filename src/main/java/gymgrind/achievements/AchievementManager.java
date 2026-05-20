package gymgrind.achievements;

import gymgrind.daily.DailyQuestNotification;
import gymgrind.player.Player;
import gymgrind.training.MachineType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AchievementManager {

    private final Set<AchievementType> completed = EnumSet.noneOf(AchievementType.class);

    public void reset() {
        completed.clear();
    }

    public void restore(Set<AchievementType> achievements) {
        completed.clear();
        if (achievements != null) {
            completed.addAll(achievements);
        }
    }

    public Set<AchievementType> completedAchievements() {
        return completed.isEmpty() ? EnumSet.noneOf(AchievementType.class) : EnumSet.copyOf(completed);
    }

    public List<DailyQuestNotification> checkWorkingLoads(Player player, Map<MachineType, Integer> workingLoads) {
        List<DailyQuestNotification> notifications = new ArrayList<>();
        for (AchievementType achievement : AchievementType.values()) {
            if (completed.contains(achievement)) {
                continue;
            }

            int currentLoad = workingLoads.getOrDefault(achievement.machineType(), 0);
            if (currentLoad < achievement.workingLoadTarget()) {
                continue;
            }

            completed.add(achievement);
            achievement.bonus().apply(player);
            notifications.add(new DailyQuestNotification(
                    "Достижение: " + achievement.displayTitle(),
                    achievement.bonus().description()
            ));
        }
        return notifications;
    }

    public static Map<MachineType, Integer> emptyWorkingLoads() {
        return new EnumMap<>(MachineType.class);
    }
}
