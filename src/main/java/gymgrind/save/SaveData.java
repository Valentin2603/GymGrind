package gymgrind.save;

import gymgrind.game.LocationId;
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
        DailyQuestSaveData dailyQuests
) {
    public SaveData {
        activeSupplements = Set.copyOf(activeSupplements);
        dailyQuests = dailyQuests == null ? DailyQuestSaveData.empty() : dailyQuests;
    }
}
