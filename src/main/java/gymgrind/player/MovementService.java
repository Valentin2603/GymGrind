package gymgrind.player;

import gymgrind.game.InputState;
import gymgrind.gym.GameMap;
import gymgrind.gym.Position;

public final class MovementService {

    private static final double COLLISION_STEP = 4.0;

    public void movePlayer(Player player, InputState inputState, GameMap gameMap, double deltaSeconds) {
        double dx = 0;
        double dy = 0;

        if (inputState.isUp()) {
            dy -= 1;
        }
        if (inputState.isDown()) {
            dy += 1;
        }
        if (inputState.isLeft()) {
            dx -= 1;
        }
        if (inputState.isRight()) {
            dx += 1;
        }

        if (dx == 0 && dy == 0) {
            player.setMoving(false);
            return;
        }

        player.setMoving(true);
        player.setDirection(directionFor(dx, dy));

        double length = Math.hypot(dx, dy);
        double stepX = dx / length * player.speed() * deltaSeconds;
        double stepY = dy / length * player.speed() * deltaSeconds;

        Position afterHorizontalMove = moveAlongAxis(player, gameMap, player.position(), stepX, true);
        Position finalPosition = moveAlongAxis(player, gameMap, afterHorizontalMove, stepY, false);
        player.setPosition(finalPosition);
    }

    private Position moveAlongAxis(Player player,
                                   GameMap gameMap,
                                   Position start,
                                   double delta,
                                   boolean horizontal) {
        if (delta == 0) {
            return start;
        }

        int steps = Math.max(1, (int) Math.ceil(Math.abs(delta) / COLLISION_STEP));
        double partialDelta = delta / steps;
        Position current = start;

        for (int index = 0; index < steps; index++) {
            Position candidate = horizontal
                    ? current.translate(partialDelta, 0)
                    : current.translate(0, partialDelta);

            if (!gameMap.allowsMovement(player.footHitboxAt(candidate))) {
                break;
            }

            current = candidate;
        }

        return current;
    }

    private PlayerDirection directionFor(double dx, double dy) {
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx < 0 ? PlayerDirection.LEFT : PlayerDirection.RIGHT;
        }
        return dy < 0 ? PlayerDirection.BACK : PlayerDirection.FRONT;
    }
}
