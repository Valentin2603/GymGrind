package gymgrind.player;

import gymgrind.game.InputState;
import gymgrind.gym.GameMap;
import gymgrind.gym.Position;

public final class MovementService {

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

        double nextX = clamp(
                player.position().x() + stepX,
                gameMap.left() + 12,
                gameMap.right() - player.width() - 12
        );
        double nextY = clamp(
                player.position().y() + stepY,
                gameMap.top() + 12,
                gameMap.bottom() - player.height() - 12
        );

        player.setPosition(new Position(nextX, nextY));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private PlayerDirection directionFor(double dx, double dy) {
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx < 0 ? PlayerDirection.LEFT : PlayerDirection.RIGHT;
        }
        return dy < 0 ? PlayerDirection.BACK : PlayerDirection.FRONT;
    }
}
