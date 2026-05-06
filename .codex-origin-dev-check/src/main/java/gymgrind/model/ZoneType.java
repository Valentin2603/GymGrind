package gymgrind.model;

public enum ZoneType {
    SHOP("Экономика"),
    STAGE("Финальная цель"),
    COACH("Подсказки"),
    REST("Восстановление"),
    WORK("Заработок");

    private final String label;

    ZoneType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
