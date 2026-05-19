package gymgrind.ui.tutorial;

import java.util.List;

public record TutorialSlide(
        String eyebrow,
        String title,
        String description,
        List<String> bullets,
        String imagePath,
        TutorialAnimationType animationType
) {
    public TutorialSlide {
        bullets = List.copyOf(bullets);
    }
}
