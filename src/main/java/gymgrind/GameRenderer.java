package gymgrind;

import gymgrind.model.GameMap;
import gymgrind.model.GymObject;
import gymgrind.model.InteractiveZone;
import gymgrind.model.MachineType;
import gymgrind.model.Player;
import gymgrind.model.PlayerDirection;
import gymgrind.model.TrainingMachine;
import gymgrind.model.ZoneType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public final class GameRenderer {

    private static final Color BACKGROUND = Color.web("#101820");
    private static final Color FLOOR = Color.web("#1B2838");
    private static final Color FLOOR_GRID = Color.web("#213247");
    private static final Color BORDER = Color.web("#7FDBA4");
    private static final Color PLAYER_COLOR = Color.web("#F8FAFC");
    private static final Color LABEL_COLOR = Color.web("#E2E8F0");
    private static final Color SUBTITLE_COLOR = Color.web("#94A3B8");
    private static final Color HIGHLIGHT = Color.web("#F8D66D");
    private static final double TILE_SIZE = 64;
    private static final long WALK_FRAME_NANOS = 180_000_000L;

    private final Image floorTile = loadImage("/assets/tiles/floor_tile.png");
    private final Image wallTile = loadImage("/assets/tiles/wall_tile.png");
    private final Map<MachineType, Image> machineImages;
    private final Map<ZoneType, Image> zoneImages;
    private final Map<PlayerDirection, Image> playerIdleImages;
    private final Map<PlayerDirection, Image> playerWalkImages;

    public GameRenderer() {
        machineImages = new EnumMap<>(MachineType.class);
        machineImages.put(MachineType.BENCH_PRESS, loadImage("/assets/machines/bench_press.png"));
        machineImages.put(MachineType.SQUAT_RACK, loadImage("/assets/machines/squat_rack.png"));
        machineImages.put(MachineType.TREADMILL, loadImage("/assets/machines/treadmill.png"));
        machineImages.put(MachineType.DEADLIFT_PLATFORM, loadImage("/assets/machines/deadlift_platform.png"));

        zoneImages = new EnumMap<>(ZoneType.class);
        zoneImages.put(ZoneType.SHOP, loadImage("/assets/machines/shop_counter.png"));
        zoneImages.put(ZoneType.REST, loadImage("/assets/machines/rest_zone.png"));
        zoneImages.put(ZoneType.STAGE, loadImage("/assets/tiles/stage_tile.png"));
        zoneImages.put(ZoneType.COACH, loadImage("/assets/npcs/trainer_npc.png"));

        playerIdleImages = new EnumMap<>(PlayerDirection.class);
        playerIdleImages.put(PlayerDirection.FRONT, loadImage("/assets/characters/player_idle_front.png"));
        playerIdleImages.put(PlayerDirection.BACK, loadImage("/assets/characters/player_idle_back.png"));
        playerIdleImages.put(PlayerDirection.LEFT, loadImage("/assets/characters/player_idle_left.png"));
        playerIdleImages.put(PlayerDirection.RIGHT, loadImage("/assets/characters/player_idle_right.png"));

        playerWalkImages = new EnumMap<>(PlayerDirection.class);
        playerWalkImages.put(PlayerDirection.FRONT, loadImage("/assets/characters/player_walk_front.png"));
        playerWalkImages.put(PlayerDirection.BACK, loadImage("/assets/characters/player_walk_back.png"));
        playerWalkImages.put(PlayerDirection.LEFT, loadImage("/assets/characters/player_walk_left.png"));
        playerWalkImages.put(PlayerDirection.RIGHT, loadImage("/assets/characters/player_walk_right.png"));
    }

    public void render(GraphicsContext graphicsContext,
                       GameMap gameMap,
                       Player player,
                       Optional<GymObject> nearbyObject,
                       GameState gameState) {
        double canvasWidth = graphicsContext.getCanvas().getWidth();
        double canvasHeight = graphicsContext.getCanvas().getHeight();

        graphicsContext.setFill(BACKGROUND);
        graphicsContext.fillRect(0, 0, canvasWidth, canvasHeight);

        drawMap(graphicsContext, gameMap);
        drawObjects(graphicsContext, gameMap, nearbyObject);
        drawPlayer(graphicsContext, player);
        drawLegend(graphicsContext, gameState, gameMap);
    }

    private void drawMap(GraphicsContext graphicsContext, GameMap gameMap) {
        graphicsContext.setFill(FLOOR);
        graphicsContext.fillRoundRect(gameMap.left(), gameMap.top(), gameMap.width(), gameMap.height(), 28, 28);

        if (floorTile != null) {
            for (double y = gameMap.top(); y < gameMap.bottom(); y += TILE_SIZE) {
                for (double x = gameMap.left(); x < gameMap.right(); x += TILE_SIZE) {
                    double width = Math.min(TILE_SIZE, gameMap.right() - x);
                    double height = Math.min(TILE_SIZE, gameMap.bottom() - y);
                    graphicsContext.drawImage(floorTile, 0, 0, width, height, x, y, width, height);
                }
            }
        } else {
            graphicsContext.setStroke(FLOOR_GRID);
            graphicsContext.setLineWidth(1);
            for (double x = gameMap.left() + 30; x < gameMap.right(); x += 60) {
                graphicsContext.strokeLine(x, gameMap.top() + 10, x, gameMap.bottom() - 10);
            }
            for (double y = gameMap.top() + 30; y < gameMap.bottom(); y += 60) {
                graphicsContext.strokeLine(gameMap.left() + 10, y, gameMap.right() - 10, y);
            }
        }

        if (wallTile != null) {
            for (double x = gameMap.left(); x < gameMap.right(); x += TILE_SIZE) {
                graphicsContext.drawImage(wallTile, x, gameMap.top() - 16, TILE_SIZE, 32);
                graphicsContext.drawImage(wallTile, x, gameMap.bottom() - 16, TILE_SIZE, 32);
            }
            for (double y = gameMap.top(); y < gameMap.bottom(); y += TILE_SIZE) {
                graphicsContext.drawImage(wallTile, gameMap.left() - 16, y, 32, TILE_SIZE);
                graphicsContext.drawImage(wallTile, gameMap.right() - 16, y, 32, TILE_SIZE);
            }
        }

        graphicsContext.setStroke(BORDER);
        graphicsContext.setLineWidth(4);
        graphicsContext.strokeRoundRect(gameMap.left(), gameMap.top(), gameMap.width(), gameMap.height(), 28, 28);
    }

    private void drawObjects(GraphicsContext graphicsContext,
                             GameMap gameMap,
                             Optional<GymObject> nearbyObject) {
        graphicsContext.setTextAlign(TextAlignment.CENTER);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        for (GymObject gymObject : gameMap.objects()) {
            Image image = imageFor(gymObject);
            if (image == null) {
                graphicsContext.setFill(gymObject.color());
                graphicsContext.fillRoundRect(
                        gymObject.left(),
                        gymObject.top(),
                        gymObject.width(),
                        gymObject.height(),
                        20,
                        20
                );
            } else {
                graphicsContext.drawImage(
                        image,
                        gymObject.left(),
                        gymObject.top(),
                        gymObject.width(),
                        gymObject.height()
                );
            }

            if (nearbyObject.filter(object -> object == gymObject).isPresent()) {
                graphicsContext.setStroke(HIGHLIGHT);
                graphicsContext.setLineWidth(3);
                graphicsContext.strokeRoundRect(
                        gymObject.left(),
                        gymObject.top(),
                        gymObject.width(),
                        gymObject.height(),
                        20,
                        20
                );
            }

            graphicsContext.setFill(LABEL_COLOR);
            graphicsContext.fillText(
                    gymObject.name(),
                    gymObject.centerX(),
                    gymObject.centerY() - 6
            );

            graphicsContext.setFill(SUBTITLE_COLOR);
            graphicsContext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            graphicsContext.fillText(
                    gymObject.shortTypeLabel(),
                    gymObject.centerX(),
                    gymObject.centerY() + 16
            );
            graphicsContext.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        }
    }

    private void drawPlayer(GraphicsContext graphicsContext, Player player) {
        Image playerImage = playerImageFor(player);
        if (playerImage != null) {
            graphicsContext.drawImage(
                    playerImage,
                    player.position().x() - 15,
                    player.position().y() - 24,
                    64,
                    64
            );
        } else {
            graphicsContext.setFill(PLAYER_COLOR);
            graphicsContext.fillOval(player.position().x(), player.position().y(), player.width(), player.height());
        }
    }

    private Image playerImageFor(Player player) {
        if (player.isMoving() && shouldShowWalkFrame()) {
            Image walkImage = playerWalkImages.get(player.direction());
            if (walkImage != null) {
                return walkImage;
            }
        }
        return playerIdleImages.get(player.direction());
    }

    private boolean shouldShowWalkFrame() {
        return (System.nanoTime() / WALK_FRAME_NANOS) % 2 == 0;
    }

    private Image imageFor(GymObject gymObject) {
        if (gymObject instanceof TrainingMachine trainingMachine) {
            return machineImages.get(trainingMachine.machineType());
        }
        if (gymObject instanceof InteractiveZone interactiveZone) {
            return zoneImages.get(interactiveZone.zoneType());
        }
        return null;
    }

    private Image loadImage(String resourcePath) {
        InputStream inputStream = GameRenderer.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            return null;
        }
        return new Image(inputStream);
    }

    private void drawLegend(GraphicsContext graphicsContext,
                            GameState gameState,
                            GameMap gameMap) {
        graphicsContext.setFill(LABEL_COLOR);
        graphicsContext.setTextAlign(TextAlignment.LEFT);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        graphicsContext.fillText("Путь к сцене", gameMap.left(), 38);

        graphicsContext.setFill(SUBTITLE_COLOR);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        graphicsContext.fillText(
                "Неделя 1: движение, границы карты, интерактивные зоны и меню",
                gameMap.left(),
                60
        );
        graphicsContext.fillText("Режим: " + gameState.title(), gameMap.right() - 185, 38);
        graphicsContext.fillText("WASD/стрелки - ходьба | E - действие | Esc - меню", gameMap.right() - 360, 60);
    }
}
