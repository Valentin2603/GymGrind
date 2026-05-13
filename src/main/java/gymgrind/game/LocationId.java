package gymgrind.game;

public enum LocationId {
    HOME("Дом"),
    GYM("Зал"),
    WORK("Работа"),
    STAGE("Сцена");

    private final String displayName;

    LocationId(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
