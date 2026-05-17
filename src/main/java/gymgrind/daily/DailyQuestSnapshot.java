package gymgrind.daily;

import gymgrind.player.Player;
import gymgrind.shop.SupplementType;

import java.util.Set;

public record DailyQuestSnapshot(
        int strength,
        int muscle,
        int stamina,
        int fatigue,
        int money,
        int form,
        double bodyFat,
        Set<SupplementType> activeSupplements
) {
    public DailyQuestSnapshot {
        activeSupplements = Set.copyOf(activeSupplements);
    }

    public static DailyQuestSnapshot from(Player player) {
        return new DailyQuestSnapshot(
                player.stats().strength(),
                player.stats().muscle(),
                player.stats().stamina(),
                player.stats().fatigue(),
                player.stats().money(),
                player.stats().form(),
                player.stats().bodyFat(),
                player.activeSupplements().activeTypes()
        );
    }

    public int nextFormDecade() {
        return form / 10 * 10 + 10;
    }
}
