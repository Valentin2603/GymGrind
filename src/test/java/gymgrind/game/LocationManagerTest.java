package gymgrind.game;

import gymgrind.gym.GameMap;
import gymgrind.gym.objects.InteractiveZone;
import gymgrind.gym.objects.ZoneType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LocationManagerTest {

    @Test
    void startsAtHomeAndExcludesCurrentLocationFromDoorMenu() {
        LocationManager manager = new LocationManager();

        assertEquals(LocationId.HOME, manager.currentLocation());
        assertEquals(List.of(LocationId.GYM, LocationId.WORK, LocationId.STAGE), manager.availableDestinations());
    }

    @Test
    void travelingUpdatesCurrentLocationAndAvailableDestinations() {
        LocationManager manager = new LocationManager();

        GameMap map = manager.travelTo(LocationId.GYM);

        assertEquals("Зал", map.name());
        assertEquals(LocationId.GYM, manager.currentLocation());
        assertEquals(List.of(LocationId.HOME, LocationId.WORK, LocationId.STAGE), manager.availableDestinations());
    }

    @Test
    void mapsContainExpectedInteractiveZones() {
        LocationManager manager = new LocationManager();

        List<ZoneType> homeZones = extractZoneTypes(manager.mapFor(LocationId.HOME));
        List<ZoneType> gymZones = extractZoneTypes(manager.mapFor(LocationId.GYM));

        assertTrue(homeZones.containsAll(List.of(ZoneType.BED, ZoneType.COMPUTER, ZoneType.DOOR)));
        assertTrue(gymZones.contains(ZoneType.DOOR));
        assertFalse(gymZones.contains(ZoneType.SHOP));
        assertFalse(gymZones.contains(ZoneType.WORK));
        assertFalse(gymZones.contains(ZoneType.STAGE));
        assertFalse(gymZones.contains(ZoneType.REST));
    }

    private List<ZoneType> extractZoneTypes(GameMap gameMap) {
        return gameMap.objects().stream()
                .filter(InteractiveZone.class::isInstance)
                .map(InteractiveZone.class::cast)
                .map(InteractiveZone::zoneType)
                .toList();
    }
}
