package gymgrind.logic;

import gymgrind.model.Player;
import gymgrind.model.ShopPurchaseResult;
import gymgrind.model.SupplementType;

public final class ShopService {

    public ShopPurchaseResult buy(Player player, SupplementType supplementType) {
        if (!player.stats().spendMoney(supplementType.price())) {
            return new ShopPurchaseResult(false, "Не хватает денег на " + supplementType.label() + ".");
        }

        if (supplementType == SupplementType.ENERGY_DRINK) {
            player.stats().reduceFatigue(30);
            return new ShopPurchaseResult(true, "Куплен энергетик. Усталость снижена на 30.");
        }

        player.activeSupplements().activate(supplementType);
        return new ShopPurchaseResult(true, "Куплено: " + supplementType.label() + ". Эффект сработает на следующей тренировке.");
    }
}
