package gymgrind.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompetitionIntroCutsceneTest {

    @Test
    void cutsceneContainsExpectedNumberOfSteps() {
        CompetitionIntroCutscene cutscene = new CompetitionIntroCutscene();

        assertEquals(5, cutscene.stepCount());
        assertTrue(cutscene.totalDurationSeconds() > 10.0);
    }

    @Test
    void cutsceneFinishesAfterFullDuration() {
        CompetitionIntroCutscene cutscene = new CompetitionIntroCutscene();

        cutscene.update(cutscene.totalDurationSeconds() + 0.25);

        assertTrue(cutscene.isFinished());
    }

    @Test
    void cutsceneCanBeSkippedImmediately() {
        CompetitionIntroCutscene cutscene = new CompetitionIntroCutscene();

        assertFalse(cutscene.isFinished());
        cutscene.skip();

        assertTrue(cutscene.isFinished());
    }
}
