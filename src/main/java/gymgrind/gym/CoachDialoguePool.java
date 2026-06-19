package gymgrind.gym;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class CoachDialoguePool {

    private static final List<String> PHRASES = List.of(
            "Ешь нормально, я тебе говорю. Добавь г*вна бл*ть каждый день бл*ть с*ка н*хуй бл*ть. Ты меня слышишь или нет?",
            "Значит утром, 50 грамм овсянки 50 грамм ягод, чтобы было вкусненько. БЛ*ТЬ Н*ХУЙ БЛ* ЕБ*ТЬ. Следующий приём 3 яйца бл*ть н*хуй бл* еб*ть н*хуй и бл*ть н*хуй 50 грамм каши",
            "Он качается бл*ть н*хуй бл*ть с 15 лет бл*ть на химоне бл* с*ка н*хуй. Он жрал метана с 15 лет с*ка н*хуй и вы рассказываете что он за год накачался. ВЫ шо еб*нуЛись? бл*ть вообще н*хуй бл* еб*ть"
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
