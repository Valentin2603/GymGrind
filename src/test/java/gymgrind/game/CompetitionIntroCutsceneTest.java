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
        assertTrue(cutscene.totalDurationSeconds() > 5.0);
    }

    @Test
    void cutsceneWaitsForManualAdvanceOnJudgeSteps() {
        CompetitionIntroCutscene cutscene = new CompetitionIntroCutscene();

        cutscene.update(cutscene.totalDurationSeconds() + 0.25);

        assertFalse(cutscene.isFinished());
    }

    @Test
    void cutsceneFinishesAfterAdvancingJudgeSteps() {
        CompetitionIntroCutscene cutscene = new CompetitionIntroCutscene();

        cutscene.update(3.0);
        cutscene.update(1.25);
        cutscene.advance();
        cutscene.update(1.25);
        cutscene.advance();
        cutscene.update(1.25);
        cutscene.advance();
        cutscene.update(3.0);

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
