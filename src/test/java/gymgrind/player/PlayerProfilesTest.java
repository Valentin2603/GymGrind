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
        assertEquals(210, darkDrun.get().baseStrength());
        assertEquals(245, darkDrun.get().baseMuscle());
        assertEquals(115, darkDrun.get().baseStamina());
        assertEquals(35.0, darkDrun.get().baseBodyFat());
        assertEquals(3, darkDrun.get().formProgression().size());
    }

    @Test
    void containsFattyPopkaProfileWithExpectedBaseStats() {
        Optional<PlayerProfile> fattyPopka = PlayerProfiles.all().stream()
                .filter(profile -> "fatty_popka".equals(profile.id()))
                .findFirst();

        assertTrue(fattyPopka.isPresent());
        assertEquals("Жирная Попка", fattyPopka.get().displayName());
        assertEquals(280, fattyPopka.get().baseStrength());
        assertEquals(315, fattyPopka.get().baseMuscle());
        assertEquals(85, fattyPopka.get().baseStamina());
        assertEquals(60.0, fattyPopka.get().baseBodyFat());
    }
}
