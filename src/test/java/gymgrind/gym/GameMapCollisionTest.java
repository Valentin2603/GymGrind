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
}
