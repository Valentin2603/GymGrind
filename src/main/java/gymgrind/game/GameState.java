package gymgrind.game;

public enum GameState {
    MENU("Меню"),
    PLAYING("Игра"),
    PAUSE("Пауза"),
    MINIGAME("Мини-игра"),
    RESULT("Результат"),фыв
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
