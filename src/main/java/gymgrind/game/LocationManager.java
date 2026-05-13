package gymgrind.game;

import gymgrind.gym.GameMap;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class LocationManager {

    private final Map<LocationId, GameMap> locations;
    private LocationId currentLocation;

    public LocationManager() {
        locations = new EnumMap<>(LocationId.class);
        locations.put(LocationId.HOME, GameMap.createHomeLayout());
        locations.put(LocationId.GYM, GameMap.createGymLayout());
        locations.put(LocationId.WORK, GameMap.createWorkLayout());
        locations.put(LocationId.STAGE, GameMap.createStageLayout());

        if (locations.size() != LocationId.values().length) {
            throw new IllegalStateException("Each LocationId must have a registered GameMap");
        }

        currentLocation = LocationId.HOME;
    }

    public void reset() {
        currentLocation = LocationId.HOME;
    }

    public LocationId currentLocation() {
        return currentLocation;
    }

    public GameMap currentMap() {
        return mapFor(currentLocation);
    }

    public GameMap mapFor(LocationId locationId) {
        GameMap map = locations.get(locationId);
        if (map == null) {
            throw new IllegalArgumentException("Unknown location: " + locationId);
        }
        return map;
    }

    public List<LocationId> availableDestinations() {
        return locations.keySet().stream()
                .filter(locationId -> locationId != currentLocation)
                .toList();
    }

    public GameMap travelTo(LocationId locationId) {
        currentLocation = locationId;
        return currentMap();
    }
}
