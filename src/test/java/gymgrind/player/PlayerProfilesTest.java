package gymgrind.player;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PlayerProfilesTest {

    @Test
    void containsDarkDrunProfileWithExpectedBaseStats() {
        Optional<PlayerProfile> darkDrun = PlayerProfiles.all().stream()
                .filter(profile -> "dark_drun".equals(profile.id()))
                .findFirst();

        assertTrue(darkDrun.isPresent());
        assertEquals("Тёмный друн", darkDrun.get().displayName());
        assertEquals(12, darkDrun.get().baseStrength());
        assertEquals(84, darkDrun.get().baseMuscle());
        assertEquals(6, darkDrun.get().baseStamina());
        assertEquals(35, darkDrun.get().baseBodyFat());
    }
}
