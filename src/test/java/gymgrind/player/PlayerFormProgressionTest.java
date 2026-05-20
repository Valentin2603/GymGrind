package gymgrind.player;

import gymgrind.gym.GameMap;
import gymgrind.shop.SupplementType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlayerFormProgressionTest {

    @Test
    void tamikUnlocksSecondFormWhenThresholdsAreMet() {
        Player player = Player.createDefault(GameMap.createHomeLayout());
        player.stats().restoreValues(150, 180, 220, 0, 300, 10.6);

        Optional<PlayerForm> unlockedForm = player.unlockFormAfterSleep();

        assertEquals(Optional.of(PlayerForm.SECOND), unlockedForm);
        assertEquals(PlayerForm.SECOND, player.currentForm());
    }

    @Test
    void steroidFormRequiresShotPurchase() {
        Player player = Player.createDefault(GameMap.createHomeLayout());
        player.stats().restoreValues(270, 320, 255, 0, 300, 9.0);

        Optional<PlayerForm> firstUnlock = player.unlockFormAfterSleep();
        assertEquals(Optional.of(PlayerForm.THIRD), firstUnlock);

        player.recordPurchase(SupplementType.RECOVERY_SHOT);
        Optional<PlayerForm> secondUnlock = player.unlockFormAfterSleep();

        assertEquals(Optional.of(PlayerForm.FOURTH_STEROIDS), secondUnlock);
        assertTrue(player.hasPurchasedSupplement(SupplementType.RECOVERY_SHOT));
    }

    @Test
    void darkDrunUnlocksSecondFormWhenHeCutsFatAndBuildsCardio() {
        Player player = Player.createDefault(GameMap.createHomeLayout());
        player.applyProfile(PlayerProfiles.findById("dark_drun"), GameMap.createHomeLayout());
        player.stats().restoreValues(245, 295, 160, 0, 300, 21.0);

        Optional<PlayerForm> unlockedForm = player.unlockFormAfterSleep();

        assertEquals(Optional.of(PlayerForm.SECOND), unlockedForm);
        assertEquals(PlayerForm.SECOND, player.currentForm());
    }

    @Test
    void darkDrunSteroidFormStillRequiresRecoveryShot() {
        Player player = Player.createDefault(GameMap.createHomeLayout());
        player.applyProfile(PlayerProfiles.findById("dark_drun"), GameMap.createHomeLayout());
        player.stats().restoreValues(325, 395, 200, 0, 300, 18.0);

        Optional<PlayerForm> firstUnlock = player.unlockFormAfterSleep();
        assertEquals(Optional.of(PlayerForm.THIRD), firstUnlock);

        player.recordPurchase(SupplementType.RECOVERY_SHOT);
        Optional<PlayerForm> secondUnlock = player.unlockFormAfterSleep();

        assertEquals(Optional.of(PlayerForm.FOURTH_STEROIDS), secondUnlock);
        assertTrue(player.hasPurchasedSupplement(SupplementType.RECOVERY_SHOT));
    }

    @Test
    void fattyPopkaUnlocksSecondFormMostlyThroughFatLoss() {
        Player player = Player.createDefault(GameMap.createHomeLayout());
        player.applyProfile(PlayerProfiles.findById("fatty_popka"), GameMap.createHomeLayout());
        player.stats().restoreValues(305, 350, 120, 0, 300, 38.0);

        Optional<PlayerForm> unlockedForm = player.unlockFormAfterSleep();

        assertEquals(Optional.of(PlayerForm.SECOND), unlockedForm);
        assertEquals(PlayerForm.SECOND, player.currentForm());
    }

    @Test
    void fattyPopkaCanReachRegularFourthFormWithoutSteroids() {
        Player player = Player.createDefault(GameMap.createHomeLayout());
        player.applyProfile(PlayerProfiles.findById("fatty_popka"), GameMap.createHomeLayout());
        player.stats().restoreValues(370, 430, 185, 0, 300, 19.0);

        Optional<PlayerForm> unlockedForm = player.unlockFormAfterSleep();

        assertEquals(Optional.of(PlayerForm.FOURTH), unlockedForm);
        assertEquals(PlayerForm.FOURTH, player.currentForm());
    }

    @Test
    void fattyPopkaSteroidFormRequiresRecoveryShot() {
        Player player = Player.createDefault(GameMap.createHomeLayout());
        player.applyProfile(PlayerProfiles.findById("fatty_popka"), GameMap.createHomeLayout());
        player.stats().restoreValues(415, 480, 210, 0, 300, 14.5);

        Optional<PlayerForm> firstUnlock = player.unlockFormAfterSleep();
        assertEquals(Optional.of(PlayerForm.FOURTH), firstUnlock);

        player.recordPurchase(SupplementType.RECOVERY_SHOT);
        Optional<PlayerForm> secondUnlock = player.unlockFormAfterSleep();

        assertEquals(Optional.of(PlayerForm.FOURTH_STEROIDS), secondUnlock);
        assertTrue(player.hasPurchasedSupplement(SupplementType.RECOVERY_SHOT));
    }

    @Test
    void tamikCanQualifyForStageWithoutSteroidForm() {
        Player player = Player.createDefault(GameMap.createHomeLayout());
        player.stats().restoreValues(205, 245, 238, 0, 300, 9.6);

        boolean canEnterStage = player.profile()
                .strongestNaturalFormDefinition()
                .orElseThrow()
                .isUnlockedFor(player);

        assertTrue(canEnterStage);
    }

    @Test
    void fattyPopkaNeedsFourthNaturalFormForStage() {
        Player player = Player.createDefault(GameMap.createHomeLayout());
        player.applyProfile(PlayerProfiles.findById("fatty_popka"), GameMap.createHomeLayout());
        player.stats().restoreValues(335, 390, 155, 0, 300, 27.0);

        boolean canEnterStageAtThird = player.profile()
                .strongestNaturalFormDefinition()
                .orElseThrow()
                .isUnlockedFor(player);

        player.stats().restoreValues(370, 430, 185, 0, 300, 19.0);
        boolean canEnterStageAtFourth = player.profile()
                .strongestNaturalFormDefinition()
                .orElseThrow()
                .isUnlockedFor(player);

        assertFalse(canEnterStageAtThird);
        assertTrue(canEnterStageAtFourth);
    }
}
