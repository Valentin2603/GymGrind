package gymgrind.player;

import gymgrind.shop.SupplementType;

public record PlayerFormDefinition(
        PlayerForm form,
        String spritePrefix,
        int minStrength,
        int minMuscle,
        int minStamina,
        double maxBodyFat,
        SupplementType requiredPurchasedSupplement
) {

    public boolean isUnlockedFor(Player player) {
        Stats stats = player.stats();
        return stats.strength() >= minStrength
                && stats.muscle() >= minMuscle
                && stats.stamina() >= minStamina
                && stats.bodyFat() <= maxBodyFat
                && (requiredPurchasedSupplement == null
                || player.hasPurchasedSupplement(requiredPurchasedSupplement));
    }
}
