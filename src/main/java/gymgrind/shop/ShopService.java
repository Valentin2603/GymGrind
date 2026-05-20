package gymgrind.shop;

import gymgrind.player.Player;

public final class ShopService {

    public ShopPurchaseResult buy(Player player, SupplementType supplementType) {
        if (!supplementType.instantUse() && player.activeSupplements().has(supplementType)) {
            return new ShopPurchaseResult(false, supplementType.label() + " уже активен.");
        }

        if (!player.stats().spendMoney(supplementType.price())) {
            return new ShopPurchaseResult(false, "Не хватает денег на " + supplementType.label() + ".");
        }

        player.recordPurchase(supplementType);

        return switch (supplementType) {
            case ENERGY_DRINK -> {
                player.stats().reduceFatigue(35);
                yield new ShopPurchaseResult(true, "Энергетик куплен. Усталость снижена на 35.");
            }
            case RECOVERY_SHOT -> {
                player.stats().applyDeltas(6, 6, 6, -35, 0, 0);
                yield new ShopPurchaseResult(true, "Шприц прогресса куплен. Постоянный буст активирован, усталость -35, все статы +6.");
            }
            default -> {
                player.activeSupplements().activate(supplementType);
                yield new ShopPurchaseResult(true,
                        "Куплено: " + supplementType.label() + ". Эффект сохранён на следующую тренировку.");
            }
        };
    }
}
