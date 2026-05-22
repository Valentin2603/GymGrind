package gymgrind.gym;

import gymgrind.game.GameState;
import gymgrind.gym.objects.GymObject;
import gymgrind.gym.objects.InteractiveZone;
import gymgrind.gym.objects.ZoneType;
import gymgrind.player.Player;

import java.util.Comparator;
import java.util.Optional;

public final class InteractionService {

    private static final double INTERACTION_DISTANCE = 92;

    public Optional<GymObject> findNearbyObject(Player player, GameMap gameMap) {
        return gameMap.objects().stream()
                .filter(GymObject::isInteractive)
                .filter(object -> distanceBetween(player, object) <= INTERACTION_DISTANCE)
                .min(Comparator.comparingDouble(object -> distanceBetween(player, object)));
    }

    public String buildPrompt(Optional<GymObject> nearbyObject, GameState gameState) {
        if (gameState != GameState.PLAYING && gameState != GameState.COMPETITION) {
            return "";
        }

        return nearbyObject
                .map(object -> "Нажмите E: " + promptLabel(object))
                .orElse("Подойдите ближе к объекту, двери или тренажёру.");
    }

    private String promptLabel(GymObject object) {
        if (object instanceof InteractiveZone zone && zone.zoneType() == ZoneType.COACH) {
            return "Duhota";
        }
        return object.name();
    }

    private double distanceBetween(Player player, GymObject gymObject) {
        double dx = player.centerX() - gymObject.centerX();
        double dy = player.centerY() - gymObject.centerY();
        return Math.hypot(dx, dy);
    }
}
