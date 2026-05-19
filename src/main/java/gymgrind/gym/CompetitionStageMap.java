package gymgrind.gym;

import gymgrind.gym.objects.InteractiveZone;
import gymgrind.gym.objects.ZoneType;
import javafx.scene.paint.Color;

import java.util.List;

public final class CompetitionStageMap {

    private static final double SOURCE_WIDTH = 1448;
    private static final double SOURCE_HEIGHT = 1086;
    private static final Position ORIGIN = new Position(240, 90);
    private static final double HEIGHT = 600;
    private static final double SCALE = HEIGHT / SOURCE_HEIGHT;
    private static final double WIDTH = SOURCE_WIDTH * SCALE;
    private static final String BACKGROUND_IMAGE_PATH = "/assets/rooms/competition_stage.png";

    private CompetitionStageMap() {
    }

    public static GameMap createLayout() {
        return new GameMap(
                "Сцена соревнований",
                "Локация чемпионата по бодибилдингу с центральными проходами, дверью снизу и ручной разметкой коллизий.",
                ORIGIN,
                WIDTH,
                HEIGHT,
                spawn(724, 1068),
                BACKGROUND_IMAGE_PATH,
                true,
                ORIGIN,
                WIDTH,
                HEIGHT,
                collisionAreas(),
                List.of(
                        new InteractiveZone(
                                "Дверь",
                                ZoneType.DOOR,
                                position(626, 1038),
                                scale(196),
                                scale(48),
                                Color.web("#F59E0B"),
                                "Дверь: отсюда можно перейти в другие локации."
                        )
                )
        );
    }

    private static List<CollisionRect> collisionAreas() {
        return List.of(
                // Outer walls, banners, speaker towers, plants and the packed side audience.
                collision(0, 0, 180, 288),
                collision(1268, 0, 180, 288),
                collision(0, 255, 139, 335),
                collision(1308, 247, 140, 345),
                collision(0, 542, 152, 192),
                collision(1292, 542, 156, 190),
                collision(0, 725, 143, 361),
                collision(1305, 723, 143, 363),
                collision(148, 0, 1152, 125),

                // Main competition stage and both staircases.
                collision(220, 150, 1005, 209),
                collision(319, 356, 63, 82),
                collision(1068, 356, 63, 82),

                // Judges table, chairs, trophy stand and decorative plants.
                collision(532, 463, 387, 86),
                collision(533, 548, 96, 103),
                collision(657, 556, 111, 106),
                collision(810, 548, 103, 103),
                collision(654, 609, 143, 79),
                collision(480, 601, 55, 64),
                collision(919, 601, 55, 64),

                // Audience blocks together with their rope barriers.
                collision(185, 657, 319, 182),
                collision(565, 656, 319, 183),
                collision(948, 657, 323, 182),
                collision(185, 842, 319, 195),
                collision(565, 842, 319, 195),
                collision(948, 842, 323, 195),

                // Bottom wall segments keep the middle gap open for the exit door.
                collision(0, 1018, 570, 68),
                collision(878, 1018, 570, 68)
        );
    }

    private static Position position(double sourceX, double sourceY) {
        return ORIGIN.translate(scale(sourceX), scale(sourceY));
    }

    private static Position spawn(double sourceCenterX, double sourceFeetY) {
        return new Position(
                ORIGIN.x() + scale(sourceCenterX) - 17,
                ORIGIN.y() + scale(sourceFeetY) - 32
        );
    }

    private static CollisionRect collision(double sourceX, double sourceY, double sourceWidth, double sourceHeight) {
        return new CollisionRect(
                ORIGIN.x() + scale(sourceX),
                ORIGIN.y() + scale(sourceY),
                scale(sourceWidth),
                scale(sourceHeight)
        );
    }

    private static double scale(double sourceValue) {
        return sourceValue * SCALE;
    }
}
