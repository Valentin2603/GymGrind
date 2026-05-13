package gymgrind.ui.render;

import gymgrind.game.GameState;
import gymgrind.gym.GameMap;
import gymgrind.gym.objects.GymObject;
import gymgrind.gym.objects.InteractiveZone;
import gymgrind.player.Player;
import gymgrind.player.PlayerDirection;
import gymgrind.player.PlayerProfile;
import gymgrind.training.MachineType;
import gymgrind.training.minigames.SkillCheckResult;
import gymgrind.training.minigames.SkillCheckSession;
import gymgrind.training.TrainingMachine;
import gymgrind.gym.objects.ZoneType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
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
    private static final Color OVERLAY_BACKDROP = Color.color(0.02, 0.04, 0.07, 0.78);
    private static final Color OVERLAY_PANEL = Color.web("#0F172A");
    private static final Color BAR_COLOR = Color.web("#1E293B");
    private static final Color SUCCESS_ZONE = Color.web("#22C55E");
    private static final Color SEQUENCE_BAR_FILL = Color.web("#F59E0B");
    private static final double TILE_SIZE = 64;
    private static final long WALK_FRAME_NANOS = 180_000_000L;

    private final Image floorTile = loadImage("/assets/tiles/floor_tile.png");
    private final Image wallTile = loadImage("/assets/tiles/wall_tile.png");
    private final Map<MachineType, Image> machineImages;
    private final Map<ZoneType, Image> zoneImages;
    private final Map<String, PlayerSpriteSet> playerSpriteSets;

    public GameRenderer() {
        machineImages = new EnumMap<>(MachineType.class);
        machineImages.put(MachineType.BENCH_PRESS, loadImage("/assets/machines/bench_press.png"));
        machineImages.put(MachineType.SQUAT_RACK, loadImage("/assets/machines/squat_rack.png"));
        machineImages.put(MachineType.TREADMILL, loadImage("/assets/machines/treadmill.png"));
        machineImages.put(MachineType.DEADLIFT_PLATFORM, loadImage("/assets/machines/deadlift_platform.png"));

        zoneImages = new EnumMap<>(ZoneType.class);
        zoneImages.put(ZoneType.SHOP, loadImage("/assets/machines/shop_counter.png"));
        zoneImages.put(ZoneType.COMPUTER, loadImage("/assets/machines/shop_counter.png"));
        zoneImages.put(ZoneType.REST, loadImage("/assets/machines/rest_zone.png"));
        zoneImages.put(ZoneType.STAGE, loadImage("/assets/tiles/stage_tile.png"));
        zoneImages.put(ZoneType.COACH, loadImage("/assets/npcs/trainer_npc.png"));

        playerSpriteSets = new HashMap<>();
    }

    public void render(GraphicsContext graphicsContext,
                       GameMap gameMap,
                       Player player,
                       Optional<GymObject> nearbyObject,
                       GameState gameState,
                       Optional<SkillCheckSession> activeSkillCheck,
                       Optional<SkillCheckResult> pendingSuccessResult) {
        double canvasWidth = graphicsContext.getCanvas().getWidth();
        double canvasHeight = graphicsContext.getCanvas().getHeight();

        graphicsContext.setFill(BACKGROUND);
        graphicsContext.fillRect(0, 0, canvasWidth, canvasHeight);

        drawMap(graphicsContext, gameMap);
        drawObjects(graphicsContext, gameMap, nearbyObject);
        drawPlayer(graphicsContext, player);
        drawLegend(graphicsContext, gameState, gameMap);

        if (activeSkillCheck.isPresent()) {
            drawSkillCheckOverlay(graphicsContext, activeSkillCheck.get());
        } else {
            pendingSuccessResult.ifPresent(result -> drawSuccessResultOverlay(graphicsContext, result));
        }
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
                graphicsContext.drawImage(image, gymObject.left(), gymObject.top(), gymObject.width(), gymObject.height());
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
            graphicsContext.fillText(gymObject.name(), gymObject.centerX(), gymObject.centerY() - 6);

            graphicsContext.setFill(SUBTITLE_COLOR);
            graphicsContext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            graphicsContext.fillText(gymObject.shortTypeLabel(), gymObject.centerX(), gymObject.centerY() + 16);
            graphicsContext.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        }
    }

    private void drawPlayer(GraphicsContext graphicsContext, Player player) {
        Image playerImage = playerImageFor(player);
        if (playerImage != null) {
            double renderWidth = player.profile().renderWidth();
            double renderHeight = player.profile().renderHeight();
            double drawX = player.centerX() - renderWidth / 2.0;
            double drawY = player.position().y() + player.height() - renderHeight;
            graphicsContext.drawImage(playerImage, drawX, drawY, renderWidth, renderHeight);
        } else {
            graphicsContext.setFill(PLAYER_COLOR);
            graphicsContext.fillOval(player.position().x(), player.position().y(), player.width(), player.height());
        }
    }

    private Image playerImageFor(Player player) {
        PlayerSpriteSet spriteSet = spriteSetFor(player.profile());
        if (player.isMoving() && shouldShowWalkFrame()) {
            Image walkImage = directionalImage(spriteSet.walkFrames(), player.direction());
            if (walkImage != null) {
                return walkImage;
            }
        }
        return directionalImage(spriteSet.idleFrames(), player.direction());
    }

    private boolean shouldShowWalkFrame() {
        return (System.nanoTime() / WALK_FRAME_NANOS) % 2 == 0;
    }

    private PlayerSpriteSet spriteSetFor(PlayerProfile profile) {
        return playerSpriteSets.computeIfAbsent(profile.id(), ignored -> loadPlayerSpriteSet(profile));
    }

    private PlayerSpriteSet loadPlayerSpriteSet(PlayerProfile profile) {
        Map<PlayerDirection, Image> idleFrames = new EnumMap<>(PlayerDirection.class);
        Map<PlayerDirection, Image> walkFrames = new EnumMap<>(PlayerDirection.class);

        for (PlayerDirection direction : PlayerDirection.values()) {
            idleFrames.put(direction, loadImage(profile.idleSpritePath(direction)));
            walkFrames.put(direction, loadImage(profile.walkSpritePath(direction)));
        }

        return new PlayerSpriteSet(idleFrames, walkFrames);
    }

    private Image directionalImage(Map<PlayerDirection, Image> frames, PlayerDirection direction) {
        Image image = frames.get(direction);
        if (image != null) {
            return image;
        }
        if (direction == PlayerDirection.LEFT) {
            return frames.get(PlayerDirection.RIGHT);
        }
        if (direction == PlayerDirection.RIGHT) {
            return frames.get(PlayerDirection.LEFT);
        }
        return frames.get(PlayerDirection.FRONT);
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
                "Тренировки, вес, усталость, магазин, работа, отдых и выход на сцену",
                gameMap.left(),
                60
        );
        graphicsContext.fillText("WASD/стрелки - ходьба | E - действие | Esc - меню", gameMap.right() - 360, 60);
    }

    private void drawSkillCheckOverlay(GraphicsContext graphicsContext, SkillCheckSession session) {
        if (session.isSequenceMode()) {
            drawSequenceSkillCheckOverlay(graphicsContext, session);
            return;
        }

        drawTimingSkillCheckOverlay(graphicsContext, session);
    }

    private void drawTimingSkillCheckOverlay(GraphicsContext graphicsContext, SkillCheckSession session) {
        double canvasWidth = graphicsContext.getCanvas().getWidth();
        double canvasHeight = graphicsContext.getCanvas().getHeight();
        double panelWidth = 620;
        double panelHeight = 280;
        double panelLeft = (canvasWidth - panelWidth) / 2.0;
        double panelTop = (canvasHeight - panelHeight) / 2.0;
        double barLeft = panelLeft + 44;
        double barTop = panelTop + 175;
        double barWidth = panelWidth - 88;
        double barHeight = 28;
        double successZoneLeft = barLeft + session.successZoneStart() * barWidth;
        double successZoneWidth = session.successZoneWidth() * barWidth;
        double markerX = barLeft + session.markerProgress() * barWidth;

        graphicsContext.save();
        graphicsContext.setFill(OVERLAY_BACKDROP);
        graphicsContext.fillRect(0, 0, canvasWidth, canvasHeight);
        graphicsContext.setFill(OVERLAY_PANEL);
        graphicsContext.fillRoundRect(panelLeft, panelTop, panelWidth, panelHeight, 24, 24);
        graphicsContext.setStroke(BORDER);
        graphicsContext.setLineWidth(3);
        graphicsContext.strokeRoundRect(panelLeft, panelTop, panelWidth, panelHeight, 24, 24);

        graphicsContext.setFill(LABEL_COLOR);
        graphicsContext.setTextAlign(TextAlignment.LEFT);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        graphicsContext.fillText(session.machine().name(), panelLeft + 36, panelTop + 52);

        graphicsContext.setFill(SUBTITLE_COLOR);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        graphicsContext.fillText(session.machine().shortTypeLabel(), panelLeft + 36, panelTop + 80);
        graphicsContext.fillText(progressText(session), panelLeft + 36, panelTop + 104);
        graphicsContext.fillText("Space/Enter/E фиксирует попытку. Esc отменяет подход.", panelLeft + 36, panelTop + 128);

        graphicsContext.setFill(BAR_COLOR);
        graphicsContext.fillRoundRect(barLeft, barTop, barWidth, barHeight, 18, 18);
        graphicsContext.setFill(SUCCESS_ZONE);
        graphicsContext.fillRoundRect(successZoneLeft, barTop, successZoneWidth, barHeight, 18, 18);
        graphicsContext.setStroke(HIGHLIGHT);
        graphicsContext.setLineWidth(5);
        graphicsContext.strokeLine(markerX, barTop - 22, markerX, barTop + barHeight + 22);
        graphicsContext.setFill(HIGHLIGHT);
        graphicsContext.fillOval(markerX - 8, barTop - 34, 16, 16);

        graphicsContext.setFill(SUBTITLE_COLOR);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        graphicsContext.fillText(footerText(session), panelLeft + 36, panelTop + 238);
        graphicsContext.restore();
    }

    private void drawSequenceSkillCheckOverlay(GraphicsContext graphicsContext, SkillCheckSession session) {
        double canvasWidth = graphicsContext.getCanvas().getWidth();
        double canvasHeight = graphicsContext.getCanvas().getHeight();
        double panelWidth = 700;
        double panelHeight = 320;
        double panelLeft = (canvasWidth - panelWidth) / 2.0;
        double panelTop = (canvasHeight - panelHeight) / 2.0;
        double barLeft = panelLeft + 42;
        double barTop = panelTop + 230;
        double barWidth = panelWidth - 84;
        double barHeight = 30;
        double filledWidth = Math.max(0.0, Math.min(1.0, session.barProgress())) * barWidth;

        graphicsContext.save();
        graphicsContext.setFill(OVERLAY_BACKDROP);
        graphicsContext.fillRect(0, 0, canvasWidth, canvasHeight);
        graphicsContext.setFill(OVERLAY_PANEL);
        graphicsContext.fillRoundRect(panelLeft, panelTop, panelWidth, panelHeight, 24, 24);
        graphicsContext.setStroke(BORDER);
        graphicsContext.setLineWidth(3);
        graphicsContext.strokeRoundRect(panelLeft, panelTop, panelWidth, panelHeight, 24, 24);

        graphicsContext.setFill(LABEL_COLOR);
        graphicsContext.setTextAlign(TextAlignment.LEFT);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        graphicsContext.fillText(session.machine().name(), panelLeft + 34, panelTop + 50);

        graphicsContext.setFill(SUBTITLE_COLOR);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        graphicsContext.fillText(session.machine().shortTypeLabel(), panelLeft + 34, panelTop + 78);
        graphicsContext.fillText("Набирайте символы по порядку: время и ошибки срезают шкалу.", panelLeft + 34, panelTop + 104);

        drawSequencePrompt(graphicsContext, session.sequencePrompt(), panelLeft + 34, panelTop + 168);

        graphicsContext.setFill(BAR_COLOR);
        graphicsContext.fillRoundRect(barLeft, barTop, barWidth, barHeight, 18, 18);
        graphicsContext.setFill(SEQUENCE_BAR_FILL);
        graphicsContext.fillRoundRect(barLeft, barTop, filledWidth, barHeight, 18, 18);

        graphicsContext.setFill(LABEL_COLOR);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        graphicsContext.fillText("Прогресс: " + (int) Math.round(session.barProgress() * 100) + "%", barLeft, barTop - 12);

        graphicsContext.setFill(SUBTITLE_COLOR);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        graphicsContext.fillText("Заполните шкалу до конца для успеха. Если она опустеет, подход сорвётся.", panelLeft + 34, panelTop + 286);
        graphicsContext.restore();
    }

    private void drawSuccessResultOverlay(GraphicsContext graphicsContext, SkillCheckResult result) {
        double canvasWidth = graphicsContext.getCanvas().getWidth();
        double canvasHeight = graphicsContext.getCanvas().getHeight();
        double panelWidth = 640;
        double panelHeight = 320;
        double panelLeft = (canvasWidth - panelWidth) / 2.0;
        double panelTop = (canvasHeight - panelHeight) / 2.0;

        graphicsContext.save();
        graphicsContext.setFill(OVERLAY_BACKDROP);
        graphicsContext.fillRect(0, 0, canvasWidth, canvasHeight);
        graphicsContext.setFill(OVERLAY_PANEL);
        graphicsContext.fillRoundRect(panelLeft, panelTop, panelWidth, panelHeight, 24, 24);
        graphicsContext.setStroke(SUCCESS_ZONE);
        graphicsContext.setLineWidth(3);
        graphicsContext.strokeRoundRect(panelLeft, panelTop, panelWidth, panelHeight, 24, 24);

        graphicsContext.setTextAlign(TextAlignment.LEFT);
        graphicsContext.setFill(SUCCESS_ZONE);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.BLACK, 30));
        graphicsContext.fillText("Подход засчитан", panelLeft + 36, panelTop + 56);

        graphicsContext.setFill(SUBTITLE_COLOR);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        graphicsContext.fillText("Результат уже применён к характеристикам персонажа.", panelLeft + 36, panelTop + 86);

        graphicsContext.setFill(LABEL_COLOR);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 17));
        graphicsContext.fillText(result.message(), panelLeft + 36, panelTop + 130, panelWidth - 72);

        graphicsContext.setStroke(BAR_COLOR);
        graphicsContext.setLineWidth(2);
        graphicsContext.strokeLine(panelLeft + 36, panelTop + 172, panelLeft + panelWidth - 36, panelTop + 172);

        graphicsContext.setFill(HIGHLIGHT);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        graphicsContext.fillText("Эффект: " + buildExtendedResultSummary(result), panelLeft + 36, panelTop + 208, panelWidth - 72);

        graphicsContext.setFill(SUBTITLE_COLOR);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        graphicsContext.fillText("Space или Esc закрывает окно и возвращает в зал.", panelLeft + 36, panelTop + 270);
        graphicsContext.restore();
    }

    private void drawSequencePrompt(GraphicsContext graphicsContext, String prompt, double left, double baselineY) {
        String tail = prompt.length() > 1 ? spacedSymbols(prompt.substring(1)) : "";

        graphicsContext.setFill(SUBTITLE_COLOR);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
        graphicsContext.fillText("Сейчас:", left, baselineY - 28);

        graphicsContext.setFill(HIGHLIGHT);
        graphicsContext.setFont(Font.font("Consolas", FontWeight.BLACK, 42));
        graphicsContext.fillText(String.valueOf(prompt.charAt(0)), left, baselineY + 8);

        graphicsContext.setFill(LABEL_COLOR);
        graphicsContext.setFont(Font.font("Consolas", FontWeight.BOLD, 26));
        graphicsContext.fillText(tail, left + 82, baselineY + 2);
    }

    private String spacedSymbols(String text) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < text.length(); index++) {
            if (index > 0) {
                builder.append("   ");
            }
            builder.append(text.charAt(index));
        }
        return builder.toString();
    }

    private String progressText(SkillCheckSession session) {
        if (session.requiresMultipleHits()) {
            return "Повторы: " + session.completedHits() + "/" + session.requiredHits() + ". Зона сужается после каждого успеха.";
        }

        return "Одно точное попадание засчитывает подход.";
    }

    private String footerText(SkillCheckSession session) {
        if (session.requiresMultipleHits()) {
            if (session.machine().machineType() == MachineType.TREADMILL) {
                return "Для беговой нужно выдержать серию интервалов: каждое попадание сужает зелёную зону.";
            }

            return "Нужно собрать все повторы подряд: каждое попадание делает зелёную зону уже.";
        }

        return "Остановите маркер максимально точно, чтобы получить прибавку к характеристикам.";
    }

    private String buildResultSummary(SkillCheckResult result) {
        StringBuilder builder = new StringBuilder();
        appendDelta(builder, "сила", result.strengthDelta());
        appendDelta(builder, "масса", result.muscleDelta());
        appendDelta(builder, "выносливость", result.staminaDelta());
        appendDelta(builder, "усталость", result.fatigueDelta());

        if (builder.isEmpty()) {
            return "без изменений";
        }

        return builder.toString();
    }

    private void appendDelta(StringBuilder builder, String label, int delta) {
        if (delta == 0) {
            return;
        }

        if (!builder.isEmpty()) {
            builder.append(" | ");
        }

        builder.append(label).append(" ");
        if (delta > 0) {
            builder.append("+");
        }
        builder.append(delta);
    }

    private String buildExtendedResultSummary(SkillCheckResult result) {
        StringBuilder builder = new StringBuilder();
        appendDelta(builder, "сила", result.strengthDelta());
        appendDelta(builder, "масса", result.muscleDelta());
        appendDelta(builder, "выносливость", result.staminaDelta());
        appendDelta(builder, "усталость", result.fatigueDelta());
        appendDelta(builder, "% жира", result.bodyFatDelta());

        if (builder.isEmpty()) {
            return "без изменений";
        }

        return builder.toString();
    }

    private record PlayerSpriteSet(Map<PlayerDirection, Image> idleFrames,
                                   Map<PlayerDirection, Image> walkFrames) {
    }
}
