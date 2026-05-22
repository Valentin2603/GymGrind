package gymgrind.shop;

public enum SupplementType {
    CREATINE(
            "Креатин",
            150,
            "+65% к силе на следующей тренировке",
            "Базовая добавка для тяжёлых подходов. Усиливает прирост силы на следующей тренировке, где есть силовая награда.",
            false
    ),
    PROTEIN(
            "Протеин",
            150,
            "+65% к массе на следующей тренировке",
            "Сывороточный протеин помогает быстрее добрать массу. Бафф срабатывает на следующей тренировке, где есть прирост мышц.",
            false
    ),
    PRE_WORKOUT(
            "Предтрен",
            300,
            "До конца дня мини-игры медленнее на 20%",
            "Дневной буст концентрации. После покупки все мини-игры текущего дня становятся медленнее на 20%, поэтому тяжёлые подходы легче контролировать.",
            false
    ),
    ENERGY_DRINK(
            "Энергетик",
            150,
            "Сразу снимает 35 усталости",
            "Быстрый разовый буст, если нужно успеть ещё одно действие в текущий день. Эффект применяется сразу после покупки.",
            true
    ),
    KNEE_SLEEVES(
            "Наколенники",
            240,
            "Следующая тренировка даёт на 30% меньше усталости",
            "Поддерживают колени и немного разгружают ноги. На следующей тренировке персонаж получит меньше усталости.",
            false
    ),
    HAND_WRAPS(
            "Кистевые бинты",
            100,
            "Следующая зона успеха шире на 22%",
            "Фиксируют кисти и помогают точнее вести движение. На следующей тренировке безопасная зона в мини-игре станет больше.",
            false
    ),
    ELBOW_WRAPS(
            "Локтевые бинты",
            100,
            "Следующий жим легче удерживать",
            "Фиксируют локти и помогают именно в жиме лёжа: штанга меньше гуляет, а безопасная зона контроля становится чуть шире.",
            false
    ),
    RECOVERY_SHOT(
            "Шприц прогресса",
            1000,
            "Постоянный буст и новая форма",
            "Очень дорогой рискованный буст. Навсегда улучшает прирост силы, массы и выносливости, снижает усталость после тренировок и немного облегчает мини-игры. Также открывает особое состояние персонажа.",
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
