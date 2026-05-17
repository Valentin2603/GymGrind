package gymgrind.daily;

import gymgrind.player.Player;

public enum DailyQuestType {
    TWO_TRAININGS("Сделать 2 любые тренировки", 2, DailyQuestBonus.money(60)),
    THREE_TRAININGS("Сделать 3 любые тренировки", 3, DailyQuestBonus.money(90)),
    LIGHT_TRAINING("Сделать тренировку на лёгком весе", 1, DailyQuestBonus.money(45)),
    MEDIUM_TRAINING("Сделать тренировку на среднем весе", 1, DailyQuestBonus.money(55)),
    HEAVY_TRAINING("Сделать тренировку на тяжёлом весе", 1, DailyQuestBonus.money(85)),
    THREE_DIFFERENT_TRAININGS("Сделать 3 разные тренировки", 3, new DailyQuestBonus(90, 0, 0, 2, 0)),
    ALL_FOUR_TRAININGS("Сделать все 4 упражнения", 4, new DailyQuestBonus(140, 0, 0, 3, 0)),
    NORMAL_OR_BETTER("Получить NORMAL или лучше", 1, DailyQuestBonus.money(50)),
    EXCELLENT_ANY("Получить EXCELLENT", 1, DailyQuestBonus.money(110)),
    EXCELLENT_TWO("Получить EXCELLENT 2 раза", 2, DailyQuestBonus.money(170)),
    NO_FAIL_TRAINING("Пройти тренировку без FAIL", 1, DailyQuestBonus.money(60)),
    HEAVY_NO_FAIL("Сделать тяжёлую тренировку без FAIL", 1, new DailyQuestBonus(120, 2, 0, 0, 0)),

    FORM_PLUS_10("Улучшить форму на +10", 10, DailyQuestBonus.money(80)),
    FORM_PLUS_20("Улучшить форму на +20", 20, new DailyQuestBonus(150, 1, 1, 1, 0)),
    STRENGTH_PLUS_15("Получить +15 силы", 15, DailyQuestBonus.money(70)),
    MUSCLE_PLUS_15("Получить +15 массы", 15, DailyQuestBonus.money(70)),
    STAMINA_PLUS_15("Получить +15 выносливости", 15, DailyQuestBonus.money(70)),
    ANY_STAT_PLUS_20("Поднять любой стат на +20", 20, DailyQuestBonus.money(100)),
    WORKING_LOAD_UP("Поднять рабочий вес", 1, new DailyQuestBonus(130, 2, 0, 0, 0)),
    FAT_REDUCED("Снизить скрытый жир", 1, new DailyQuestBonus(90, 0, 0, 2, 0)),
    FORM_NEXT_TEN("Довести форму до нового десятка", 1, DailyQuestBonus.money(70)),

    END_FATIGUE_UNDER_40("Закончить день с усталостью ниже 40", 1, DailyQuestBonus.money(70), true, null, false),
    END_FATIGUE_UNDER_25("Закончить день с усталостью ниже 25", 1, new DailyQuestBonus(110, 0, 0, 2, 0), true, null, false),
    REST_ONCE("Отдохнуть хотя бы 1 раз", 1, DailyQuestBonus.money(40)),
    RESTORE_30_FATIGUE("Восстановить 30+ усталости", 30, DailyQuestBonus.money(80)),
    MAX_FATIGUE_UNDER_70("Не довести усталость выше 70", 1, DailyQuestBonus.money(90), true, null, false),
    NO_OVERTRAINING_DAY("Провести день без перетренированности", 1, DailyQuestBonus.money(60), true, null, false),
    HIGH_FATIGUE_NO_FAIL("Тренировка при усталости выше 60 без FAIL", 1, DailyQuestBonus.money(120)),

    EARN_100("Заработать 100 денег", 100, DailyQuestBonus.money(50)),
    EARN_200("Заработать 200 денег", 200, DailyQuestBonus.money(90)),
    WORK_ONCE("Выполнить подработку", 1, DailyQuestBonus.money(40)),
    WORK_NORMAL_OR_BETTER("Подработка на NORMAL или лучше", 1, DailyQuestBonus.money(80)),
    BUY_ANY_SUPPLEMENT("Купить любую добавку", 1, DailyQuestBonus.money(50)),
    BUY_PROTEIN("Купить протеин", 1, new DailyQuestBonus(0, 0, 2, 0, 0)),
    BUY_CREATINE("Купить креатин", 1, new DailyQuestBonus(0, 2, 0, 0, 0)),
    BUY_PRE_WORKOUT("Купить предтрен", 1, new DailyQuestBonus(0, 0, 0, 2, 0)),
    BUY_ENERGY("Купить энергетик", 1, new DailyQuestBonus(0, 0, 0, 0, 10)),
    USE_SUPPLEMENT("Использовать добавку в тренировке", 1, DailyQuestBonus.money(120)),
    BUY_AND_TRAIN("Купить добавку и провести тренировку", 1, DailyQuestBonus.money(100)),

    TRAIN_AND_WORK("Сделать тренировку и подработку", 1, DailyQuestBonus.money(90)),
    CARDIO_AFTER_STRENGTH("Сделать кардио после силовой", 1, new DailyQuestBonus(70, 0, 0, 1, 0)),
    STRENGTH_AFTER_CARDIO("Сделать силовую после беговой", 1, new DailyQuestBonus(70, 1, 0, 0, 0)),
    NO_SPEND_DAY("Не потратить деньги за день", 1, DailyQuestBonus.money(100), true, null, false),
    MONEY_300("Закончить день с деньгами 300+", 1, DailyQuestBonus.money(80), true, null, false),

    TAMIK_TREADMILL_EXCELLENT("Тамик: беговая на EXCELLENT", 1, new DailyQuestBonus(150, 0, 0, 3, 0), false, "street_rookie", false),
    TAMIK_HEAVY_STRENGTH("Тамик: тяжёлая силовая тренировка", 1, new DailyQuestBonus(120, 3, 0, 0, 0), false, "street_rookie", false),
    DRUN_TREADMILL_NO_FAIL("Друн: беговая без провала", 1, new DailyQuestBonus(140, 0, 0, 3, 0), false, "dark_drun", false),
    DRUN_FATIGUE_UNDER_40("Друн: снизить усталость ниже 40", 1, DailyQuestBonus.money(100), false, "dark_drun", false),
    DRUN_FAT_REDUCED("Друн: снизить процент жира", 1, new DailyQuestBonus(130, 0, 0, 3, 0), false, "dark_drun", false),
    FATTY_CARDIO("Жирная Попка: сделать кардио", 1, new DailyQuestBonus(120, 0, 0, 2, 0), false, "fatty_popka", false),
    FATTY_FAT_REDUCED("Жирная Попка: снизить процент жира", 1, new DailyQuestBonus(150, 0, 0, 4, 0), false, "fatty_popka", false),
    FATTY_TWO_NO_FAIL("Жирная Попка: 2 тренировки без FAIL", 2, DailyQuestBonus.money(160), false, "fatty_popka", false),

    FORM_300("Достичь формы 300+", 1, DailyQuestBonus.money(150), false, null, true),
    FORM_400("Достичь формы 400+", 1, DailyQuestBonus.money(220), false, null, true),
    STAGE_FATIGUE_UNDER_60("Выйти на сцену с усталостью ниже 60", 1, DailyQuestBonus.money(200), false, null, true),
    STAGE_AFTER_PURCHASE("Выйти на сцену после покупки добавки", 1, DailyQuestBonus.money(160), false, null, true),
    STAGE_AFTER_NO_FAIL_DAY("Выйти на сцену после дня без провалов", 1, DailyQuestBonus.money(180), false, null, true),
    STAGE_READY("Подготовиться к сцене: форма 320+ и усталость ниже 50", 1, DailyQuestBonus.money(250), false, null, true),
    COMPLETE_DAILY_GOALS("Закрыть все ежедневные задания", 3, DailyQuestBonus.money(300));

    private final String title;
    private final int target;
    private final DailyQuestBonus bonus;
    private final boolean endOfDayOnly;
    private final String profileId;
    private final boolean stageGoal;

    DailyQuestType(String title, int target, DailyQuestBonus bonus) {
        this(title, target, bonus, false, null, false);
    }

    DailyQuestType(String title,
                   int target,
                   DailyQuestBonus bonus,
                   boolean endOfDayOnly,
                   String profileId,
                   boolean stageGoal) {
        this.title = title;
        this.target = target;
        this.bonus = bonus;
        this.endOfDayOnly = endOfDayOnly;
        this.profileId = profileId;
        this.stageGoal = stageGoal;
    }

    public String title() {
        return title;
    }

    public int target() {
        return target;
    }

    public DailyQuestBonus bonus() {
        return bonus;
    }

    public boolean endOfDayOnly() {
        return endOfDayOnly;
    }

    public boolean isEligible(Player player, int day) {
        if (this == COMPLETE_DAILY_GOALS || this == REST_ONCE || this == RESTORE_30_FATIGUE || isSimpleNoFailGoal()) {
            return false;
        }
        if (profileId != null && !profileId.equals(player.profile().id())) {
            return false;
        }
        return !stageGoal || day >= 7 || player.stats().form() >= 280;
    }

    private boolean isSimpleNoFailGoal() {
        return switch (this) {
            case NORMAL_OR_BETTER,
                 NO_FAIL_TRAINING,
                 HEAVY_NO_FAIL,
                 HIGH_FATIGUE_NO_FAIL,
                 WORK_NORMAL_OR_BETTER,
                 DRUN_TREADMILL_NO_FAIL,
                 FATTY_TWO_NO_FAIL,
                 STAGE_AFTER_NO_FAIL_DAY -> true;
            default -> false;
        };
    }

    public boolean finalGoal() {
        return this == COMPLETE_DAILY_GOALS;
    }

    public DailyQuestGroup group() {
        return switch (this) {
            case TWO_TRAININGS, THREE_TRAININGS, LIGHT_TRAINING, MEDIUM_TRAINING, HEAVY_TRAINING,
                 THREE_DIFFERENT_TRAININGS, ALL_FOUR_TRAININGS, NORMAL_OR_BETTER, EXCELLENT_ANY,
                 EXCELLENT_TWO, NO_FAIL_TRAINING, HEAVY_NO_FAIL -> DailyQuestGroup.TRAINING;
            case FORM_PLUS_10, FORM_PLUS_20, STRENGTH_PLUS_15, MUSCLE_PLUS_15, STAMINA_PLUS_15,
                 ANY_STAT_PLUS_20, WORKING_LOAD_UP, FAT_REDUCED, FORM_NEXT_TEN -> DailyQuestGroup.PROGRESS;
            case END_FATIGUE_UNDER_40, END_FATIGUE_UNDER_25, REST_ONCE, RESTORE_30_FATIGUE,
                 MAX_FATIGUE_UNDER_70, NO_OVERTRAINING_DAY, HIGH_FATIGUE_NO_FAIL -> DailyQuestGroup.RECOVERY;
            case EARN_100, EARN_200, WORK_ONCE, WORK_NORMAL_OR_BETTER, BUY_ANY_SUPPLEMENT,
                 BUY_PROTEIN, BUY_CREATINE, BUY_PRE_WORKOUT, BUY_ENERGY, USE_SUPPLEMENT,
                 BUY_AND_TRAIN -> DailyQuestGroup.ECONOMY;
            case TRAIN_AND_WORK, CARDIO_AFTER_STRENGTH, STRENGTH_AFTER_CARDIO, NO_SPEND_DAY,
                 MONEY_300 -> DailyQuestGroup.BALANCE;
            case TAMIK_TREADMILL_EXCELLENT, TAMIK_HEAVY_STRENGTH, DRUN_TREADMILL_NO_FAIL,
                 DRUN_FATIGUE_UNDER_40, DRUN_FAT_REDUCED, FATTY_CARDIO, FATTY_FAT_REDUCED,
                 FATTY_TWO_NO_FAIL -> DailyQuestGroup.CHARACTER;
            case FORM_300, FORM_400, STAGE_FATIGUE_UNDER_60, STAGE_AFTER_PURCHASE,
                 STAGE_AFTER_NO_FAIL_DAY, STAGE_READY -> DailyQuestGroup.STAGE;
            case COMPLETE_DAILY_GOALS -> DailyQuestGroup.FINAL;
        };
    }

    public enum DailyQuestGroup {
        TRAINING,
        PROGRESS,
        RECOVERY,
        ECONOMY,
        BALANCE,
        CHARACTER,
        STAGE,
        FINAL
    }
}
