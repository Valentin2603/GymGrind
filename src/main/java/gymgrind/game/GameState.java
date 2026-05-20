package gymgrind.game;

public enum GameState {
    MENU("Меню"),
    PLAYING("Игра"),
    COMPETITION_INTRO("Вступление"),
    COMPETITION("Соревнование"),
    POSING_MINIGAME("Позирование"),
    JUDGE_RESULTS("Оценки судей"),
    COMPETITION_RESULT("Итоги соревнования"),
    PAUSE("Пауза"),
    MINIGAME("Мини-игра"),
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
