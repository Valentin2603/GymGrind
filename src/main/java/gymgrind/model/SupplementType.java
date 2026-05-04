package gymgrind.model;

public enum SupplementType {
    CREATINE("Креатин", 200),
    PROTEIN("Протеин", 150),
    PRE_WORKOUT("Предтрен", 180),
    ENERGY_DRINK("Энергетик", 100);

    private final String label;
    private final int price;

    SupplementType(String label, int price) {
        this.label = label;
        this.price = price;
    }

    public String label() {
        return label;
    }

    public int price() {
        return price;
    }
}
