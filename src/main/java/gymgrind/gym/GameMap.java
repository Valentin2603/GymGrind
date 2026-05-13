package gymgrind.gym;

import gymgrind.gym.objects.GymObject;
import gymgrind.gym.objects.InteractiveZone;
import gymgrind.gym.objects.ZoneType;
import gymgrind.training.MachineType;
import gymgrind.training.TrainingMachine;
import javafx.scene.paint.Color;

import java.util.List;

public final class GameMap {

    private static final Position DEFAULT_ORIGIN = new Position(40, 90);
    private static final double DEFAULT_WIDTH = 1200;
    private static final double DEFAULT_HEIGHT = 590;

    private final String name;
    private final String description;
    private final Position origin;
    private final double width;
    private final double height;
    private final Position spawnPoint;
    private final List<GymObject> objects;

    public GameMap(String name,
                   String description,
                   Position origin,
                   double width,
                   double height,
                   Position spawnPoint,
                   List<GymObject> objects) {
        this.name = name;
        this.description = description;
        this.origin = origin;
        this.width = width;
        this.height = height;
        this.spawnPoint = spawnPoint;
        this.objects = List.copyOf(objects);
    }

    public static GameMap createHomeLayout() {
        return new GameMap(
                "Комната игрока",
                "Домашняя база: здесь можно поспать, открыть магазин на компьютере и выйти в другие локации.",
                DEFAULT_ORIGIN,
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                new Position(140, 560),
                List.of(
                        new InteractiveZone(
                                "Кровать",
                                ZoneType.BED,
                                new Position(120, 180),
                                260,
                                130,
                                Color.web("#C084FC"),
                                "Кровать: восстановит силы и переведёт игру на следующий день."
                        ),
                        new InteractiveZone(
                                "Компьютер",
                                ZoneType.COMPUTER,
                                new Position(790, 170),
                                220,
                                140,
                                Color.web("#38BDF8"),
                                "Компьютер: здесь можно открыть магазин добавок."
                        ),
                        new InteractiveZone(
                                "Дверь",
                                ZoneType.DOOR,
                                new Position(1035, 255),
                                120,
                                180,
                                Color.web("#F59E0B"),
                                "Дверь: отсюда можно перейти в другие локации."
                        )
                )
        );
    }

    public static GameMap createGymLayout() {
        return new GameMap(
                "Зал",
                "Тренировочная локация с существующими тренажёрами. Магазин и переходы вынесены домой.",
                DEFAULT_ORIGIN,
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                new Position(110, 560),
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
                                "Присед: сюда позже подключим мини-игру со skill checks."
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
                                "Становая тяга: пока заглушка, позже добавим полноценную мини-игру."
                        ),
                        new InteractiveZone(
                                "Тренер",
                                ZoneType.COACH,
                                new Position(390, 350),
                                96,
                                96,
                                Color.web("#DB2777"),
                                "Тренер: 'Следи за усталостью и грамотно распределяй тренировки по дням.'"
                        ),
                        new InteractiveZone(
                                "Дверь",
                                ZoneType.DOOR,
                                new Position(1035, 300),
                                120,
                                180,
                                Color.web("#F59E0B"),
                                "Дверь: отсюда можно перейти в другие локации."
                        )
                )
        );
    }

    public static GameMap createWorkLayout() {
        return new GameMap(
                "Работа",
                "Отдельная локация под будущую систему заработка. Пока здесь только точка перехода.",
                DEFAULT_ORIGIN,
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                new Position(140, 560),
                List.of(
                        new InteractiveZone(
                                "Дверь",
                                ZoneType.DOOR,
                                new Position(1035, 255),
                                120,
                                180,
                                Color.web("#F59E0B"),
                                "Дверь: отсюда можно перейти в другие локации."
                        )
                )
        );
    }

    public static GameMap createStageLayout() {
        return new GameMap(
                "Сцена",
                "Отдельная локация под будущие выступления. Пока здесь только точка перехода.",
                DEFAULT_ORIGIN,
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                new Position(140, 560),
                List.of(
                        new InteractiveZone(
                                "Дверь",
                                ZoneType.DOOR,
                                new Position(1035, 255),
                                120,
                                180,
                                Color.web("#F59E0B"),
                                "Дверь: отсюда можно перейти в другие локации."
                        )
                )
        );
    }

    public static GameMap createWeekOneLayout() {
        return createGymLayout();
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
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

    public Position spawnPoint() {
        return spawnPoint;
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
