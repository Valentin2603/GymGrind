package gymgrind.shop;

public enum SupplementType {
    CREATINE(
            "Креатин",
            120,
            "+50% к силе на следующей тренировке",
            "Базовая добавка для тяжёлых подходов. Усиливает прирост силы на следующей тренировке, где есть силовая награда.",
            false
    ),
    PROTEIN(
            "Протеин",
            180,
            "+50% к массе на следующей тренировке",
            "Сывороточный протеин помогает быстрее добрать массу. Бафф срабатывает на следующей тренировке, где есть прирост мышц.",
            false
    ),
    PRE_WORKOUT(
            "Предтрен",
            150,
            "Следующая мини-игра медленнее на 20%",
            "Заряжает перед подходом и даёт больше времени на реакцию. На следующей тренировке скорость мини-игры станет ниже.",
            false
    ),
    ENERGY_DRINK(
            "Энергетик",
            80,
            "Сразу снимает 30 усталости",
            "Быстрый разовый буст, если нужно успеть ещё одно действие в текущий день. Эффект применяется сразу после покупки.",
            true
    ),
    KNEE_SLEEVES(
            "Наколенники",
            100,
            "Следующая тренировка даёт на 20% меньше усталости",
            "Поддерживают колени и немного разгружают ноги. На следующей тренировке персонаж получит меньше усталости.",
            false
    ),
    HAND_WRAPS(
            "Кистевые бинты",
            70,
            "Следующая зона успеха шире на 15%",
            "Фиксируют кисти и помогают точнее вести движение. На следующей тренировке безопасная зона в мини-игре станет больше.",
            false
    ),
    ELBOW_WRAPS(
            "Локтевые бинты",
            80,
            "+25% к силе на следующей тренировке",
            "Дают дополнительную жёсткость в жимовых движениях. На следующей тренировке увеличат прирост силы поверх базовой награды.",
            false
    ),
    RECOVERY_SHOT(
            "Восстанавливающий укол",
            120,
            "Сразу: выносливость +2 и усталость -15",
            "Экстренный вариант для тяжёлого дня. Немного поднимает стат выносливости и сразу снимает часть накопленной усталости.",
            true
    );

    private final String label;
    private final int price;
    private final String effect;
    private final String description;
    private final boolean instantUse;

    SupplementType(String label, int price, String effect, String description, boolean instantUse) {
        this.label = label;
        this.price = price;
        this.effect = effect;
        this.description = description;
        this.instantUse = instantUse;
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

    public String description() {
        return description;
    }

    public boolean instantUse() {
        return instantUse;
    }
}
