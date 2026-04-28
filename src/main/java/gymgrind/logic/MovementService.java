package gymgrind.logic;

import gymgrind.InputState;
import gymgrind.model.GameMap;
import gymgrind.model.Player;
import gymgrind.model.Position;

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
            return;
        }

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
}
