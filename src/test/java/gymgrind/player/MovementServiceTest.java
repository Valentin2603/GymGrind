package gymgrind.player;

import gymgrind.game.InputState;
import gymgrind.gym.CollisionRect;
import gymgrind.gym.GameMap;
import gymgrind.gym.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class MovementServiceTest {

    @Test
    void diagonalMovementSlidesAlongObstacleInsteadOfFreezing() {
        GameMap gameMap = new GameMap(
                "Test",
                "Synthetic map for movement checks.",
                new Position(0, 0),
                200,
                200,
                new Position(40, 20),
                null,
                false,
                new Position(0, 0),
                200,
                200,
                List.of(new CollisionRect(70, 0, 30, 150)),
                List.of()
        );
        Player player = Player.createDefault(gameMap);
        InputState inputState = new InputState();
        MovementService movementService = new MovementService();

        inputState.setRight(true);
        inputState.setDown(true);
        movementService.movePlayer(player, inputState, gameMap, 0.2);

        assertTrue(player.position().x() < 45.0);
        assertTrue(player.position().y() > 50.0);
    }
}
