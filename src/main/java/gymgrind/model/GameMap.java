package gymgrind.model;

import javafx.scene.paint.Color;

import java.util.List;

public final class GameMap {

    private final Position origin;
    private final double width;
    private final double height;
    private final List<GymObject> objects;

    public GameMap(Position origin, double width, double height, List<GymObject> objects) {
        this.origin = origin;
        this.width = width;
        this.height = height;
        this.objects = List.copyOf(objects);
    }

    public static GameMap createWeekOneLayout() {
        return new GameMap(
                new Position(40, 90),
                1200,
                590,
                List.of(
                        new TrainingMachine(
                                "Жим лёжа",
                                MachineType.BENCH_PRESS,
                                new Position(110, 145),
                                170,
                                80,
                                Color.web("#2D6A4F"),
                                "Жим лёжа: здесь будет мини-игра на быстрые нажатия пробела."
                        ),
                        new TrainingMachine(
                                "Присед",
                                MachineType.SQUAT_RACK,
                                new Position(330, 145),
                                170,
                                80,
                                Color.web("#1D7874"),
                                "Присед: сюда позже подключим мини-игру, где будут skill checks."
                        ),
                        new TrainingMachine(
                                "Беговая дорожка",
                                MachineType.TREADMILL,
                                new Position(560, 145),
                                190,
                                80,
                                Color.web("#3A86FF"),
                                "Беговая дорожка: зона под кардио-тренировку и развитие выносливости."
                        ),
                        new TrainingMachine(
                                "Становая",
                                MachineType.DEADLIFT_PLATFORM,
                                new Position(800, 145),
                                170,
                                80,
                                Color.web("#6D597A"),
                                "Становая тяга: пока заглушка, добавим мини-игру."
                        ),
                        new InteractiveZone(
                                "Магазин",
                                ZoneType.SHOP,
                                new Position(1020, 135),
                                165,
                                110,
                                Color.web("#D97706"),
                                "Магазин: скоро здесь откроется покупка добавок."
                        ),
                        new InteractiveZone(
                                "Работа",
                                ZoneType.WORK,
                                new Position(130, 355),
                                170,
                                100,
                                Color.web("#8B5CF6"),
                                "Работа: сюда подключим мини-игру для заработка денег."
                        ),
                        new InteractiveZone(
                                "Тренер",
                                ZoneType.COACH,
                                new Position(365, 340),
                                140,
                                130,
                                Color.web("#DB2777"),
                                "Тренер: 'Следи за усталостью и не выходи на сцену слишком рано.'"
                        ),
                        new InteractiveZone(
                                "Отдых",
                                ZoneType.REST,
                                new Position(565, 350),
                                170,
                                100,
                                Color.web("#14B8A6"),
                                "Зона отдыха: позже здесь будет восстановление усталости."
                        ),
                        new InteractiveZone(
                                "Сцена",
                                ZoneType.STAGE,
                                new Position(815, 325),
                                285,
                                145,
                                Color.web("#B91C1C"),
                                "Сцена: финальная проверка формы появится после системы статов и победы."
                        )
                )
        );
    }

    public Position origin() {
        return origin;
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    public double left() {
        return origin.x();
    }

    public double top() {
        return origin.y();
    }

    public double right() {
        return origin.x() + width;
    }

    public double bottom() {
        return origin.y() + height;
    }

    public List<GymObject> objects() {
        return objects;
    }
}
