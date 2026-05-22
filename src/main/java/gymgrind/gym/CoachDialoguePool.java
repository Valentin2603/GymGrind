package gymgrind.gym;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class CoachDialoguePool {

    private static final List<String> PHRASES = List.of(
            "Ешь нормально, я тебе говорю. Добавь говна блять каждый день блять сука нахуй блять. Ты меня слышишь или нет?",
            "Значит утром, 50 грамм овсянки 50 грамм ягод, чтобы было вкусненько. БЛЯТЬ НАХУЙ БЛЯ ЕБАТЬ. Следующий приём 3 яйца блять нахуй бля ебать нахуй и блять нахуй 50 грамм каши",
            "Он качается блять нахуй блять с 15 лет блять на химоне бля сука нахуй. Он жрал метана с 15 лет сука нахуй и вы рассказываете что он за год накачался. ВЫ шо ебануЛись? блять вообще нахуй бля ебать"
    );

    private final Deque<String> remainingPhrases = new ArrayDeque<>();

    public String nextPhrase() {
        if (remainingPhrases.isEmpty()) {
            refill();
        }
        return remainingPhrases.removeFirst();
    }

    public static List<String> phrases() {
        return PHRASES;
    }

    private void refill() {
        List<String> shuffled = new ArrayList<>(PHRASES);
        Collections.shuffle(shuffled, ThreadLocalRandom.current());
        remainingPhrases.addAll(shuffled);
    }
}
