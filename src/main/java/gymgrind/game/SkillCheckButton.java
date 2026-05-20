package gymgrind.game;

import javafx.scene.input.KeyCode;

import java.util.List;

public enum SkillCheckButton {
    A(KeyCode.A, "A"),
    S(KeyCode.S, "S"),
    D(KeyCode.D, "D"),
    SPACE(KeyCode.SPACE, "SPACE");

    private static final List<SkillCheckButton> ALL = List.of(values());

    private final KeyCode keyCode;
    private final String label;

    SkillCheckButton(KeyCode keyCode, String label) {
        this.keyCode = keyCode;
        this.label = label;
    }

    public KeyCode keyCode() {
        return keyCode;
    }

    public String label() {
        return label;
    }

    public static List<SkillCheckButton> all() {
        return ALL;
    }
}
