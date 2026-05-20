package gymgrind.gym;

import gymgrind.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GameMapCollisionTest {

    @Test
    void bedroomSpawnIsWalkableAndFurnitureIsSolid() {
        GameMap homeMap = GameMap.createHomeLayout();
        Player player = Player.createDefault(homeMap);

        assertTrue(homeMap.allowsMovement(player.footHitbox()));
        assertFalse(homeMap.allowsMovement(new CollisionRect(homeMap.left() + 95, homeMap.top() + 205, 18, 10)));
        assertFalse(homeMap.allowsMovement(new CollisionRect(homeMap.left() + 335, homeMap.top() + 170, 18, 10)));
        assertFalse(homeMap.allowsMovement(new CollisionRect(homeMap.left() + 495, homeMap.top() + 365, 18, 10)));
        assertFalse(homeMap.allowsMovement(new CollisionRect(homeMap.left() + 525, homeMap.top() + 215, 18, 10)));
        assertFalse(homeMap.allowsMovement(new CollisionRect(homeMap.left() + 360, homeMap.top() + 375, 18, 10)));
    }

    @Test
    void competitionStageSpawnIsWalkableAndBlockedZonesAreSolid() {
        GameMap stageMap = CompetitionStageMap.createLayout();
        Player player = Player.createDefault(stageMap);

        assertTrue(stageMap.allowsMovement(player.footHitbox()));
        assertTrue(stageMap.allowsMovement(stageRect(stageMap, 724, 1060, 18, 10)));
        assertFalse(stageMap.allowsMovement(stageRect(stageMap, 720, 250, 18, 10)));
        assertFalse(stageMap.allowsMovement(stageRect(stageMap, 335, 388, 18, 10)));
        assertFalse(stageMap.allowsMovement(stageRect(stageMap, 715, 505, 18, 10)));
        assertFalse(stageMap.allowsMovement(stageRect(stageMap, 285, 735, 18, 10)));
        assertFalse(stageMap.allowsMovement(stageRect(stageMap, 78, 845, 18, 10)));
    }

    private CollisionRect stageRect(GameMap stageMap, double sourceX, double sourceY, double width, double height) {
        double scale = stageMap.height() / 1086.0;
        return new CollisionRect(
                stageMap.left() + sourceX * scale,
                stageMap.top() + sourceY * scale,
                width,
                height
        );
    }
}
