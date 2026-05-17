package gymgrind.player;

import gymgrind.gym.GameMap;
import gymgrind.shop.SupplementType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlayerFormProgressionTest {

    @Test
    void tamikUnlocksSecondFormWhenThresholdsAreMet() {
        Player player = Player.createDefault(GameMap.createHomeLayout());
        player.stats().restoreValues(150, 175, 235, 0, 300, 10.6);

        Optional<PlayerForm> unlockedForm = player.unlockFormAfterSleep();

        assertEquals(Optional.of(PlayerForm.SECOND), unlockedForm);
        assertEquals(PlayerForm.SECOND, player.currentForm());
    }

    @Test
    void steroidFormRequiresShotPurchase() {
        Player player = Player.createDefault(GameMap.createHomeLayout());
        player.stats().restoreValues(260, 300, 260, 0, 300, 9.0);

        Optional<PlayerForm> firstUnlock = player.unlockFormAfterSleep();
        assertEquals(Optional.of(PlayerForm.THIRD), firstUnlock);

        player.recordPurchase(SupplementType.RECOVERY_SHOT);
        Optional<PlayerForm> secondUnlock = player.unlockFormAfterSleep();

        assertEquals(Optional.of(PlayerForm.FOURTH_STEROIDS), secondUnlock);
        assertTrue(player.hasPurchasedSupplement(SupplementType.RECOVERY_SHOT));
    }
}
