package gymgrind.gym;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CoachDialoguePoolTest {

    @Test
    void keepsAllCoachPhrasesInOnePlace() {
        assertEquals(3, CoachDialoguePool.phrases().size());
        assertTrue(CoachDialoguePool.phrases().contains(
                "Ешь нормально, я тебе говорю. Дообавь говна блять каждый день блять сука нахуй блять. Ты меня слышишь или нет?"
        ));
    }

    @Test
    void servesOnlyConfiguredPhrases() {
        CoachDialoguePool pool = new CoachDialoguePool();
        Set<String> seen = new HashSet<>();

        for (int index = 0; index < 12; index++) {
            String phrase = pool.nextPhrase();
            assertTrue(CoachDialoguePool.phrases().contains(phrase));
            seen.add(phrase);
        }

        assertEquals(CoachDialoguePool.phrases().size(), seen.size());
    }
}
