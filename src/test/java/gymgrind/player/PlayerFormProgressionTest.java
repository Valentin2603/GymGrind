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

    @Test
    void darkDrunUnlocksSecondFormWhenHeCutsFatAndBuildsCardio() {
        Player player = Player.createDefault(GameMap.createHomeLayout());
        player.applyProfile(PlayerProfiles.findById("dark_drun"), GameMap.createHomeLayout());
        player.stats().restoreValues(230, 270, 145, 0, 300, 24.0);

        Optional<PlayerForm> unlockedForm = player.unlockFormAfterSleep();

        assertEquals(Optional.of(PlayerForm.SECOND), unlockedForm);
        assertEquals(PlayerForm.SECOND, player.currentForm());
    }

    @Test
    void darkDrunSteroidFormStillRequiresRecoveryShot() {
        Player player = Player.createDefault(GameMap.createHomeLayout());
        player.applyProfile(PlayerProfiles.findById("dark_drun"), GameMap.createHomeLayout());
        player.stats().restoreValues(280, 335, 195, 0, 300, 14.0);

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
        player.stats().restoreValues(285, 320, 110, 0, 300, 48.0);

        Optional<PlayerForm> unlockedForm = player.unlockFormAfterSleep();

        assertEquals(Optional.of(PlayerForm.SECOND), unlockedForm);
        assertEquals(PlayerForm.SECOND, player.currentForm());
    }

    @Test
    void fattyPopkaCanReachRegularFourthFormWithoutSteroids() {
        Player player = Player.createDefault(GameMap.createHomeLayout());
        player.applyProfile(PlayerProfiles.findById("fatty_popka"), GameMap.createHomeLayout());
        player.stats().restoreValues(315, 355, 160, 0, 300, 21.5);

        Optional<PlayerForm> unlockedForm = player.unlockFormAfterSleep();

        assertEquals(Optional.of(PlayerForm.FOURTH), unlockedForm);
        assertEquals(PlayerForm.FOURTH, player.currentForm());
    }

    @Test
    void fattyPopkaSteroidFormRequiresRecoveryShot() {
        Player player = Player.createDefault(GameMap.createHomeLayout());
        player.applyProfile(PlayerProfiles.findById("fatty_popka"), GameMap.createHomeLayout());
        player.stats().restoreValues(335, 390, 180, 0, 300, 15.0);

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
        player.stats().restoreValues(200, 225, 245, 0, 300, 9.6);

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
        player.stats().restoreValues(295, 330, 135, 0, 300, 34.0);

        boolean canEnterStageAtThird = player.profile()
                .strongestNaturalFormDefinition()
                .orElseThrow()
                .isUnlockedFor(player);

        player.stats().restoreValues(310, 350, 155, 0, 300, 22.0);
        boolean canEnterStageAtFourth = player.profile()
                .strongestNaturalFormDefinition()
                .orElseThrow()
                .isUnlockedFor(player);

        assertFalse(canEnterStageAtThird);
        assertTrue(canEnterStageAtFourth);
    }
}
