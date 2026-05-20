package gymgrind.game;

public enum GameState {
    MENU("Меню"),
    PLAYING("Игра"),
    PAUSE("Пауза"),
    MINIGAME("Мини-игра"),
    COMPETITION_INTRO("Заставка"),
    COMPETITION("Соревнование"),
    RESULT("Результат"),
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
