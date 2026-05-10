package gymgrind.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class StatsTest {

    @Test
    void applyDeltasUpdatesStatsAndClampsAtZero() {
        Stats stats = new Stats(10, 8, 6, 2, 1);

        stats.applyDeltas(2, -3, 1, -5, -4);

        assertEquals(12, stats.strength());
        assertEquals(5, stats.muscle());
        assertEquals(7, stats.stamina());
        assertEquals(0, stats.fatigue());
        assertEquals(0, stats.money());
    }
}
