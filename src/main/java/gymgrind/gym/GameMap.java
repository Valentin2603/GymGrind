package gymgrind.gym;

import gymgrind.gym.objects.GymObject;
import gymgrind.gym.objects.InteractiveZone;
import gymgrind.gym.objects.WarehouseProp;
import gymgrind.gym.objects.ZoneType;
import gymgrind.training.MachineType;
import gymgrind.training.TrainingMachine;
import javafx.scene.paint.Color;

import java.util.List;

public final class GameMap {

    private static final Position DEFAULT_ORIGIN = new Position(40, 90);
    private static final double DEFAULT_WIDTH = 1200;
    private static final double DEFAULT_HEIGHT = 590;

    private static final double BEDROOM_SOURCE_SIZE = 1254;
    private static final Position HOME_ORIGIN = new Position(345, 90);
    private static final double HOME_WIDTH = 590;
    private static final double HOME_HEIGHT = 590;
    private static final double HOME_SCALE = HOME_WIDTH / BEDROOM_SOURCE_SIZE;
    private static final String BEDROOM_BACKGROUND_PATH = "/assets/rooms/bedroom.png";

    private final String name;
    private final String description;
    private final Position origin;
    private final double width;
    private final double height;
    private final Position spawnPoint;
    private final String backgroundImagePath;
    private final boolean hideEmbeddedObjectMarkers;
    private final Position walkAreaOrigin;
    private final double walkAreaWidth;
    private final double walkAreaHeight;
    private final List<CollisionRect> collisionAreas;
    private final List<GymObject> objects;

    public GameMap(String name,
                   String description,
                   Position origin,
                   double width,
                   double height,
                   Position spawnPoint,
                   List<GymObject> objects) {
        this(
                name,
                description,
                origin,
                width,
                height,
                spawnPoint,
                null,
                false,
                origin,
                width,
                height,
                List.of(),
                objects
        );
    }

    public GameMap(String name,
                   String description,
                   Position origin,
                   double width,
                   double height,
                   Position spawnPoint,
                   String backgroundImagePath,
                   boolean hideEmbeddedObjectMarkers,
                   Position walkAreaOrigin,
                   double walkAreaWidth,
                   double walkAreaHeight,
                   List<GymObject> objects) {
        this(
                name,
                description,
                origin,
                width,
                height,
                spawnPoint,
                backgroundImagePath,
                hideEmbeddedObjectMarkers,
                walkAreaOrigin,
                walkAreaWidth,
                walkAreaHeight,
                List.of(),
                objects
        );
    }

    public GameMap(String name,
                   String description,
                   Position origin,
                   double width,
                   double height,
                   Position spawnPoint,
                   String backgroundImagePath,
                   boolean hideEmbeddedObjectMarkers,
                   Position walkAreaOrigin,
                   double walkAreaWidth,
                   double walkAreaHeight,
                   List<CollisionRect> collisionAreas,
                   List<GymObject> objects) {
        this.name = name;
        this.description = description;
        this.origin = origin;
        this.width = width;
        this.height = height;
        this.spawnPoint = spawnPoint;
        this.backgroundImagePath = backgroundImagePath;
        this.hideEmbeddedObjectMarkers = hideEmbeddedObjectMarkers;
        this.walkAreaOrigin = walkAreaOrigin;
        this.walkAreaWidth = walkAreaWidth;
        this.walkAreaHeight = walkAreaHeight;
        this.collisionAreas = List.copyOf(collisionAreas);
        this.objects = List.copyOf(objects);
    }

    public static GameMap createHomeLayout() {
        return new GameMap(
                "Спальная комната",
                "Домашняя спальня с кроватью, компьютером и выходом из комнаты.",
                HOME_ORIGIN,
                HOME_WIDTH,
                HOME_HEIGHT,
                homeSpawn(600, 1080),
                BEDROOM_BACKGROUND_PATH,
                true,
                HOME_ORIGIN,
                HOME_WIDTH,
                HOME_HEIGHT,
                homeCollisionAreas(),
                List.of(
                        new InteractiveZone(
                                "Кровать",
                                ZoneType.BED,
                                homePosition(155, 505),
                                homeScale(335),
                                homeScale(270),
                                Color.web("#C084FC"),
                                "Кровать: восстановит силы и переведёт игру на следующий день."
                        ),
                        new InteractiveZone(
                                "Компьютер",
                                ZoneType.COMPUTER,
                                homePosition(535, 380),
                                homeScale(285),
                                homeScale(235),
                                Color.web("#38BDF8"),
                                "Компьютер: здесь можно открыть магазин добавок."
                        ),
                        new InteractiveZone(
                                "Дверь",
                                ZoneType.DOOR,
                                homePosition(150, 1080),
                                homeScale(250),
                                homeScale(130),
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
                new Position(640, 600),
                null,
                false,
                new Position(DEFAULT_ORIGIN.x() + 44, DEFAULT_ORIGIN.y() + 116),
                DEFAULT_WIDTH - 88,
                DEFAULT_HEIGHT - 146,
                List.of(
                        new CollisionRect(40, 90, 1200, 116),
                        new CollisionRect(40, 90, 44, 590),
                        new CollisionRect(1196, 90, 44, 590),
                        new CollisionRect(40, 650, 490, 30),
                        new CollisionRect(750, 650, 490, 30),
                        new CollisionRect(76, 156, 80, 150),
                        new CollisionRect(916, 152, 260, 112),
                        new CollisionRect(116, 246, 104, 106),
                        new CollisionRect(260, 246, 116, 104),
                        new CollisionRect(894, 330, 92, 146),
                        new CollisionRect(194, 508, 144, 56),
                        new CollisionRect(674, 276, 176, 96),
                        new CollisionRect(525, 334, 44, 112),
                        new CollisionRect(615, 334, 44, 112),
                        new CollisionRect(934, 516, 136, 76),
                        new CollisionRect(1128, 460, 50, 102)
                ),
                List.of(
                        new TrainingMachine(
                                "Жим лёжа",
                                MachineType.BENCH_PRESS,
                                new Position(108, 232),
                                120,
                                122,
                                Color.web("#2D6A4F"),
                                "Жим лёжа: здесь будет мини-игра на быстрые нажатия пробела."
                        ),
                        new TrainingMachine(
                                "Присед",
                                MachineType.SQUAT_RACK,
                                new Position(250, 232),
                                136,
                                130,
                                Color.web("#1D7874"),
                                "Присед: сюда позже подключим мини-игру со skill checks."
                        ),
                        new TrainingMachine(
                                "Беговая дорожка",
                                MachineType.TREADMILL,
                                new Position(892, 318),
                                104,
                                168,
                                Color.web("#3A86FF"),
                                "Беговая дорожка: зона под кардио-тренировку и развитие выносливости."
                        ),
                        new TrainingMachine(
                                "Становая",
                                MachineType.DEADLIFT_PLATFORM,
                                new Position(184, 500),
                                164,
                                82,
                                Color.web("#6D597A"),
                                "Становая тяга: пока заглушка, позже добавим полноценную мини-игру."
                        ),
                        new InteractiveZone(
                                "Дверь",
                                ZoneType.DOOR,
                                new Position(552, 622),
                                176,
                                58,
                                Color.web("#F59E0B"),
                                "Дверь: отсюда можно перейти в другие локации."
                        )
                )
        );
    }

    public static GameMap createWorkLayout() {
        return new GameMap(
                "Работа",
                "Складская подработка: берите коробки и относите их в зону доставки.",
                DEFAULT_ORIGIN,
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                new Position(592, 622),
                null,
                false,
                DEFAULT_ORIGIN,
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                List.of(
                        new CollisionRect(40, 90, 1200, 130),
                        new CollisionRect(40, 90, 18, 590),
                        new CollisionRect(1222, 90, 18, 590),
                        new CollisionRect(40, 660, 490, 20),
                        new CollisionRect(750, 660, 490, 20),
                        new CollisionRect(78, 132, 176, 128),
                        new CollisionRect(656, 158, 118, 105),
                        new CollisionRect(72, 506, 150, 115),
                        new CollisionRect(932, 436, 145, 121),
                        new CollisionRect(76, 300, 70, 190),
                        new CollisionRect(1144, 216, 74, 184),
                        new CollisionRect(265, 155, 130, 330),
                        new CollisionRect(510, 90, 120, 285),
                        new CollisionRect(745, 305, 130, 250)
                ),
                List.of(
                        new InteractiveZone(
                                "Приемка",
                                ZoneType.WORK,
                                new Position(150, 408),
                                74,
                                72,
                                Color.web("#B7791F"),
                                "Приемка: начните смену и берите коробки отсюда."
                        ),
                        new WarehouseProp(
                                "Полки",
                                "Товары",
                                new Position(265, 155),
                                130,
                                330,
                                Color.web("#8B5A2B")
                        ),
                        new WarehouseProp(
                                "Полки",
                                "Товары",
                                new Position(510, 90),
                                120,
                                285,
                                Color.web("#9A6737")
                        ),
                        new WarehouseProp(
                                "Полки",
                                "Товары",
                                new Position(745, 305),
                                130,
                                250,
                                Color.web("#8B5A2B")
                        ),
                        new InteractiveZone(
                                "Дверь",
                                ZoneType.DOOR,
                                new Position(552, 612),
                                120,
                                64,
                                Color.web("#F59E0B"),
                                "Дверь: отсюда можно перейти в другие локации."
                        )
                )
        );
    }

    public static GameMap createStageLayout() {
        // Legacy entry point: keep old callers on the new competition stage layout.
        if (CompetitionStageMap.class != null) {
            return CompetitionStageMap.createLayout();
        }
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

    public String backgroundImagePath() {
        return backgroundImagePath;
    }

    public boolean hasBackgroundImage() {
        return backgroundImagePath != null && !backgroundImagePath.isBlank();
    }

    public boolean hideEmbeddedObjectMarkers() {
        return hideEmbeddedObjectMarkers;
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

    public double walkLeft() {
        return walkAreaOrigin.x();
    }

    public double walkTop() {
        return walkAreaOrigin.y();
    }

    public double walkRight() {
        return walkAreaOrigin.x() + walkAreaWidth;
    }

    public double walkBottom() {
        return walkAreaOrigin.y() + walkAreaHeight;
    }

    public CollisionRect walkBounds() {
        return new CollisionRect(walkLeft(), walkTop(), walkAreaWidth, walkAreaHeight);
    }

    public List<CollisionRect> collisionAreas() {
        return collisionAreas;
    }

    public boolean hasCollisionAreas() {
        return !collisionAreas.isEmpty();
    }

    public boolean allowsMovement(CollisionRect hitbox) {
        return walkBounds().contains(hitbox)
                && collisionAreas.stream().noneMatch(area -> area.intersects(hitbox));
    }

    public List<GymObject> objects() {
        return objects;
    }

    private static List<CollisionRect> homeCollisionAreas() {
        return List.of(
                homeCollision(0, 0, 1254, 70),
                homeCollision(0, 0, 60, 1254),
                homeCollision(1190, 0, 64, 1254),
                homeCollision(0, 1190, 140, 64),
                homeCollision(420, 1190, 834, 64),
                homeCollision(145, 270, 350, 525),
                homeCollision(520, 300, 320, 180),
                homeCollision(604, 430, 132, 170),
                homeCollision(850, 275, 300, 180),
                homeCollision(1080, 360, 110, 255),
                homeCollision(960, 650, 170, 330),
                homeCollision(660, 690, 240, 260),
                homeCollision(70, 350, 90, 130)
        );
    }

    private static Position homePosition(double sourceX, double sourceY) {
        return HOME_ORIGIN.translate(homeScale(sourceX), homeScale(sourceY));
    }

    private static Position homeSpawn(double sourceCenterX, double sourceFeetY) {
        return new Position(
                HOME_ORIGIN.x() + homeScale(sourceCenterX) - 17,
                HOME_ORIGIN.y() + homeScale(sourceFeetY) - 32
        );
    }

    private static CollisionRect homeCollision(double sourceX, double sourceY, double sourceWidth, double sourceHeight) {
        return new CollisionRect(
                HOME_ORIGIN.x() + homeScale(sourceX),
                HOME_ORIGIN.y() + homeScale(sourceY),
                homeScale(sourceWidth),
                homeScale(sourceHeight)
        );
    }

    private static double homeScale(double sourceValue) {
        return sourceValue * HOME_SCALE;
    }
}
