package gymgrind.achievements;

import gymgrind.daily.DailyQuestBonus;
import gymgrind.training.MachineType;

public enum AchievementType {
    BENCH_60("Первый уверенный жим", MachineType.BENCH_PRESS, 60, DailyQuestBonus.money(15)),
    BENCH_80("Жим пошёл вверх", MachineType.BENCH_PRESS, 80, DailyQuestBonus.money(18)),
    BENCH_100("Жим за сотку", MachineType.BENCH_PRESS, 100, new DailyQuestBonus(25, 1, 0, 0, 0)),
    BENCH_120("Грудь включилась", MachineType.BENCH_PRESS, 120, new DailyQuestBonus(30, 1, 0, 0, 0)),
    BENCH_140("Жимовой монстр", MachineType.BENCH_PRESS, 140, new DailyQuestBonus(35, 1, 1, 0, 0)),
    BENCH_160("Стальная скамья", MachineType.BENCH_PRESS, 160, new DailyQuestBonus(42, 1, 1, 0, 0)),
    BENCH_190("Жим элитного уровня", MachineType.BENCH_PRESS, 190, new DailyQuestBonus(55, 2, 1, 0, 0)),
    BENCH_220("Легенда жима", MachineType.BENCH_PRESS, 220, new DailyQuestBonus(70, 2, 2, 0, 0)),

    SQUAT_70("Присед встал на рельсы", MachineType.SQUAT_RACK, 70, DailyQuestBonus.money(15)),
    SQUAT_90("Ноги проснулись", MachineType.SQUAT_RACK, 90, DailyQuestBonus.money(18)),
    SQUAT_110("Сильные ноги", MachineType.SQUAT_RACK, 110, new DailyQuestBonus(25, 0, 1, 0, 0)),
    SQUAT_130("Глубокий рабочий присед", MachineType.SQUAT_RACK, 130, new DailyQuestBonus(30, 0, 1, 1, 0)),
    SQUAT_150("Глубина и мощь", MachineType.SQUAT_RACK, 150, new DailyQuestBonus(35, 1, 1, 0, 0)),
    SQUAT_170("Ноги как стойки", MachineType.SQUAT_RACK, 170, new DailyQuestBonus(42, 1, 1, 1, 0)),
    SQUAT_200("Присед соревновательный", MachineType.SQUAT_RACK, 200, new DailyQuestBonus(60, 1, 2, 1, 0)),
    SQUAT_230("Король глубины", MachineType.SQUAT_RACK, 230, new DailyQuestBonus(75, 2, 2, 1, 0)),

    DEADLIFT_80("Первая серьёзная тяга", MachineType.DEADLIFT_PLATFORM, 80, DailyQuestBonus.money(15)),
    DEADLIFT_100("Тяга стала рабочей", MachineType.DEADLIFT_PLATFORM, 100, DailyQuestBonus.money(18)),
    DEADLIFT_120("Спина держит", MachineType.DEADLIFT_PLATFORM, 120, new DailyQuestBonus(25, 1, 0, 0, 0)),
    DEADLIFT_140("Платформа дрожит", MachineType.DEADLIFT_PLATFORM, 140, new DailyQuestBonus(30, 1, 1, 0, 0)),
    DEADLIFT_160("Король платформы", MachineType.DEADLIFT_PLATFORM, 160, new DailyQuestBonus(35, 2, 0, 0, 0)),
    DEADLIFT_180("Тяжёлая спина", MachineType.DEADLIFT_PLATFORM, 180, new DailyQuestBonus(45, 2, 1, 0, 0)),
    DEADLIFT_205("Тяга почти элита", MachineType.DEADLIFT_PLATFORM, 205, new DailyQuestBonus(60, 2, 1, 1, 0)),
    DEADLIFT_230("Легенда платформы", MachineType.DEADLIFT_PLATFORM, 230, new DailyQuestBonus(75, 3, 1, 1, 0)),

    TREADMILL_70("Кардио разогналось", MachineType.TREADMILL, 70, DailyQuestBonus.money(15)),
    TREADMILL_90("Дыхалка крепнет", MachineType.TREADMILL, 90, DailyQuestBonus.money(18)),
    TREADMILL_105("Сердце мотора", MachineType.TREADMILL, 105, new DailyQuestBonus(25, 0, 0, 1, 0)),
    TREADMILL_120("Интервалы под контролем", MachineType.TREADMILL, 120, new DailyQuestBonus(30, 0, 0, 1, 0)),
    TREADMILL_135("Интервальный зверь", MachineType.TREADMILL, 135, new DailyQuestBonus(35, 0, 0, 2, 0)),
    TREADMILL_155("Кардио машина", MachineType.TREADMILL, 155, new DailyQuestBonus(42, 0, 0, 2, 0)),
    TREADMILL_185("Лёгкие чемпиона", MachineType.TREADMILL, 185, new DailyQuestBonus(55, 0, 0, 3, 0)),
    TREADMILL_220("Марафонский режим", MachineType.TREADMILL, 220, new DailyQuestBonus(70, 0, 0, 4, 0));

    private final String title;
    private final MachineType machineType;
    private final int workingLoadTarget;
    private final DailyQuestBonus bonus;

    AchievementType(String title, MachineType machineType, int workingLoadTarget, DailyQuestBonus bonus) {
        this.title = title;
        this.machineType = machineType;
        this.workingLoadTarget = workingLoadTarget;
        this.bonus = bonus;
    }

    public String title() {
        return title;
    }

    public MachineType machineType() {
        return machineType;
    }

    public int workingLoadTarget() {
        return workingLoadTarget;
    }

    public DailyQuestBonus bonus() {
        return bonus;
    }

    public String displayTitle() {
        String unit = machineType == MachineType.TREADMILL ? "ур." : "кг";
        return title + " (" + workingLoadTarget + " " + unit + ")";
    }
}
