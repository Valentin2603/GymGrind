package gymgrind.logic;

import gymgrind.GameState;
import gymgrind.model.GameMap;
import gymgrind.model.GymObject;
import gymgrind.model.Player;

import java.util.Comparator;
import java.util.Optional;

public final class InteractionService {

    private static final double INTERACTION_DISTANCE = 92;

    public Optional<GymObject> findNearbyObject(Player player, GameMap gameMap) {
        return gameMap.objects().stream()
                .filter(object -> distanceBetween(player, object) <= INTERACTION_DISTANCE)
                .min(Comparator.comparingDouble(object -> distanceBetween(player, object)));
    }

    public String buildPrompt(Optional<GymObject> nearbyObject, GameState gameState) {
        if (gameState != GameState.PLAYING) {
            return "";
        }

        return nearbyObject
                .map(object -> "Нажмите E: " + object.name())
                .orElse("Подойдите ближе к тренажёру или зоне.");
    }

    private double distanceBetween(Player player, GymObject gymObject) {
        double dx = player.centerX() - gymObject.centerX();
        double dy = player.centerY() - gymObject.centerY();
        return Math.hypot(dx, dy);
    }
}
