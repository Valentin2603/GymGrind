package gymgrind.game;

import gymgrind.gym.GameMap;
import gymgrind.player.Player;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PosingMinigameTest {

    @Test
    void minigameBuildsScoresInExpectedRange() {
        PosingMinigame minigame = new PosingMinigame(new PoseAssetLoader());
        Player player = Player.createDefault(GameMap.createHomeLayout());
        minigame.start(player, 1280, 720);

        for (int index = 0; index < 12; index++) {
            KeyCode expected = minigame.currentTargetButton().keyCode();
            minigame.handleKeyPressed(expected);
        }
        minigame.update(20.0);

        assertTrue(minigame.isFinished());
        PerformanceResult result = minigame.getResult();
        assertTrue(result.techniqueScore() >= 0.0 && result.techniqueScore() <= 10.0);
        assertTrue(result.charismaScore() >= 0.0 && result.charismaScore() <= 10.0);
        assertTrue(result.powerScore() >= 0.0 && result.powerScore() <= 10.0);
        assertTrue(result.totalScore() >= 0.0 && result.totalScore() <= 10.0);
        assertTrue(result.successfulPresses() >= 12);
    }

    @Test
    void escapeFinishesMinigameEarly() {
        PosingMinigame minigame = new PosingMinigame(new PoseAssetLoader());
        Player player = Player.createDefault(GameMap.createHomeLayout());
        minigame.start(player, 1280, 720);

        minigame.handleKeyPressed(KeyCode.ESCAPE);

        assertTrue(minigame.isFinished());
        assertFalse(minigame.getResult().passed());
    }
}
