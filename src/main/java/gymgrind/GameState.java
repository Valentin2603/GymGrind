package gymgrind;

public enum GameState {
    MENU("Меню"),
    PLAYING("Игра"),
    MINIGAME("Мини-игра"),
    SHOP("Магазин"),
    DIALOGUE("Диалог"),
    WIN("Победа"),
    LOSE("Поражение");

    private final String title;

    GameState(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
