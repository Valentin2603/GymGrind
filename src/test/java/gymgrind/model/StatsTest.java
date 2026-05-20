package gymgrind.model;

import gymgrind.player.Stats;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class StatsTest {

    @Test
    void applyDeltasUpdatesStatsAndClampsAtZero() {
        Stats stats = new Stats(10, 8, 6, 2, 1, 14);

        stats.applyDeltas(2, -3, 1, -5, -4, -20);

        assertEquals(12, stats.strength());
        assertEquals(5, stats.muscle());
        assertEquals(7, stats.stamina());
        assertEquals(0, stats.fatigue());
        assertEquals(100, stats.availableStamina());
        assertEquals(100, stats.maxAvailableStamina());
        assertEquals(0, stats.money());
        assertEquals(10.454545454545455, stats.bodyFat());
    }

    @Test
    void availableStaminaTracksFatigue() {
        Stats stats = new Stats(10, 8, 6, 35, 100, 14);

        assertEquals(65, stats.availableStamina());

        stats.reduceFatigue(15);

        assertEquals(80, stats.availableStamina());
    }

    @Test
    void supportsHighBodyFatValuesForHeavyProfiles() {
        Stats stats = new Stats(10, 8, 6, 0, 100, 60);

        assertEquals(60, stats.bodyFat());
    }
}
