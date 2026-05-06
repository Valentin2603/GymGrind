package gymgrind.model;

public enum SupplementType {
    CREATINE("Креатин", 200, "+50% к силе на следующей тренировке"),
    PROTEIN("Протеин", 150, "+50% к массе на следующей тренировке"),
    PRE_WORKOUT("Предтрен", 180, "индикаторы мини-игр медленнее на 20%"),
    ENERGY_DRINK("Энергетик", 100, "сразу снижает усталость на 30");

    private final String label;
    private final int price;
    private final String effect;

    SupplementType(String label, int price, String effect) {
        this.label = label;
        this.price = price;
        this.effect = effect;
    }

    public String label() {
        return label;
    }

    public int price() {
        return price;
    }

    public String effect() {
        return effect;
    }
}
