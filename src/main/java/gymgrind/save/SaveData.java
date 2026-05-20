package gymgrind.save;

import gymgrind.achievements.AchievementType;
import gymgrind.game.LocationId;
import gymgrind.player.PlayerForm;
import gymgrind.daily.DailyQuestSaveData;
import gymgrind.shop.SupplementType;

import java.util.Set;

public record SaveData(
        String profileId,
        LocationId locationId,
        double playerX,
        double playerY,
        int currentDay,
        int strength,
        int muscle,
        int stamina,
        int fatigue,
        int money,
        double bodyFat,
        Set<SupplementType> activeSupplements,
        PlayerForm currentForm,
        Set<SupplementType> purchasedSupplements,
        Set<AchievementType> completedAchievements,
        DailyQuestSaveData dailyQuests
) {
    public SaveData {
        activeSupplements = activeSupplements == null ? Set.of() : Set.copyOf(activeSupplements);
        currentForm = currentForm == null ? PlayerForm.BASE : currentForm;
        purchasedSupplements = purchasedSupplements == null ? Set.of() : Set.copyOf(purchasedSupplements);
        completedAchievements = completedAchievements == null ? Set.of() : Set.copyOf(completedAchievements);
        dailyQuests = dailyQuests == null ? DailyQuestSaveData.empty() : dailyQuests;
    }
}
