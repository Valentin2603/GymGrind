package gymgrind.ui.render;

import gymgrind.game.GameState;
import gymgrind.game.WorkShiftState;
import gymgrind.gym.CollisionRect;
import gymgrind.gym.GameMap;
import gymgrind.gym.objects.GymObject;
import gymgrind.gym.objects.InteractiveZone;
import gymgrind.gym.objects.WarehouseProp;
import gymgrind.player.Player;
import gymgrind.player.PlayerDirection;
import gymgrind.player.PlayerForm;
import gymgrind.player.PlayerProfile;
import gymgrind.training.MachineType;
import gymgrind.training.TrainingGrade;
import gymgrind.training.minigames.SkillCheckResult;
import gymgrind.training.minigames.SkillCheckSession;
import gymgrind.training.TrainingMachine;
import gymgrind.gym.objects.ZoneType;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
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
    private static final double WORK_UNIFORM_RENDER_SCALE = 0.90;
    private static final double WORK_UNIFORM_WIDTH_SCALE = 0.75;
    private static final int WORK_UNIFORM_FRAME_WIDTH = 380;
    private static final int WORK_UNIFORM_FRAME_HEIGHT = 627;
    private static final int WORK_UNIFORM_CONTENT_HEIGHT = 608;
    private static final int WORK_UNIFORM_MAX_CONTENT_WIDTH = 300;

    private final Image floorTile = loadImage("/assets/tiles/floor_tile.png");
    private final Image wallTile = loadImage("/assets/tiles/wall_tile.png");
    private final Image workBoxImage = loadImage("/assets/work/box.png");
    private final Image pickupRackImage = loadImage("/assets/work/pickup_rack.png");
    private final Image warehouseWorkbenchImage = loadImage("/assets/work/source/5.png");
    private final Image warehouseBinsImage = loadImage("/assets/work/source/4.png");
    private final Image warehousePalletJackImage = loadImage("/assets/work/source/3.png");
    private final Image warehousePalletImage = loadImage("/assets/work/source/2.png");
    private final Image gymRoomImage = loadImage("/assets/rooms/gym_room.png");
    private final Image gymBenchImage = loadImage("/assets/gym/bench.png");
    private final Image gymDumbbellRackImage = loadImage("/assets/gym/dumbbell_rack.png");
    private final Image gymKettlebellRackImage = loadImage("/assets/gym/kettlebell_rack.png");
    private final Image gymLockerImage = loadImage("/assets/gym/locker.png");
    private final Image gymMatsImage = loadImage("/assets/gym/mats.png");
    private final Image gymWaterCoolerImage = loadImage("/assets/gym/water_cooler.png");
    private final Map<MachineType, Image> machineImages;
    private final Map<ZoneType, Image> zoneImages;
    private final Map<String, Image> mapBackgrounds;
    private final Map<String, PlayerSpriteSet> playerSpriteSets;
    private final Map<String, DirectionalSpriteSet> playerCarrySpriteSets;
    private final Map<String, PlayerSpriteSet> workUniformSpriteSets;
    private boolean debugCollisions;

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

        mapBackgrounds = new HashMap<>();
        playerSpriteSets = new HashMap<>();
        playerCarrySpriteSets = new HashMap<>();
        workUniformSpriteSets = new HashMap<>();
    }

    public void render(GraphicsContext graphicsContext,
                       GameMap gameMap,
                       Player player,
                       Optional<GymObject> nearbyObject,
                       GameState gameState,
                       Optional<SkillCheckSession> activeSkillCheck,
                       Optional<SkillCheckResult> pendingSuccessResult,
                       Optional<WorkShiftState> workShiftState,
                       Optional<String> coachSpeechText,
                       double fadeOverlayAlpha) {
        double canvasWidth = graphicsContext.getCanvas().getWidth();
        double canvasHeight = graphicsContext.getCanvas().getHeight();

        graphicsContext.setFill(BACKGROUND);
        graphicsContext.fillRect(0, 0, canvasWidth, canvasHeight);

        drawMap(graphicsContext, gameMap);
        workShiftState.ifPresent(state -> drawWorkShiftFloor(graphicsContext, state));
        if (isGymMap(gameMap)) {
            drawGymObjects(graphicsContext, gameMap, nearbyObject, player, false);
        } else {
            drawObjects(graphicsContext, gameMap, nearbyObject);
        }
        workShiftState.ifPresent(state -> drawWorkRacks(graphicsContext));
        boolean carryingWorkBox = workShiftState.map(WorkShiftState::carryingBox).orElse(false);
        boolean wearingWorkUniform = workShiftState.map(WorkShiftState::workerDressed).orElse(false);
        boolean renderedCarrySprite = drawPlayer(graphicsContext, player, carryingWorkBox, wearingWorkUniform);
        if (isGymMap(gameMap)) {
            drawGymObjects(graphicsContext, gameMap, nearbyObject, player, true);
        }
        workShiftState.ifPresent(state -> drawCarriedWorkBox(graphicsContext, player, state, renderedCarrySprite));
        drawCoachSpeechBubble(graphicsContext, gameMap, coachSpeechText);
        drawDebugCollisions(graphicsContext, gameMap, player);
        drawLegend(graphicsContext, gameState, gameMap);
        workShiftState.ifPresent(state -> drawWorkShiftHud(graphicsContext, gameMap, state));

        if (activeSkillCheck.isPresent()) {
            drawSkillCheckOverlay(graphicsContext, activeSkillCheck.get());
        } else {
            pendingSuccessResult.ifPresent(result -> drawSuccessResultOverlay(graphicsContext, result));
        }

        drawFadeOverlay(graphicsContext, fadeOverlayAlpha);
    }

    private void drawFadeOverlay(GraphicsContext graphicsContext, double alpha) {
        if (alpha <= 0.0) {
            return;
        }
        double overlayAlpha = clamp(alpha, 0.0, 1.0);
        graphicsContext.setFill(Color.color(0, 0, 0, overlayAlpha));
        graphicsContext.fillRect(
                0,
                0,
                graphicsContext.getCanvas().getWidth(),
                graphicsContext.getCanvas().getHeight()
        );
    }

    private void drawMap(GraphicsContext graphicsContext, GameMap gameMap) {
        Image mapBackground = backgroundImageFor(gameMap);
        if (mapBackground != null) {
            graphicsContext.drawImage(mapBackground, gameMap.left(), gameMap.top(), gameMap.width(), gameMap.height());
            return;
        }

        if (isWarehouseMap(gameMap)) {
            drawWarehouseMap(graphicsContext, gameMap);
            return;
        }

        if (isGymMap(gameMap)) {
            drawGymMap(graphicsContext, gameMap);
            return;
        }

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
            boolean hideMarker = gameMap.hideEmbeddedObjectMarkers() && gymObject instanceof InteractiveZone;
            boolean nearby = nearbyObject.filter(object -> object == gymObject).isPresent();
            if (hideMarker && !nearby) {
                continue;
            }
            if (isWarehouseMap(gameMap)
                    && gymObject instanceof InteractiveZone interactiveZone
                    && (interactiveZone.zoneType() == ZoneType.WORK || interactiveZone.zoneType() == ZoneType.DOOR)) {
                if (nearby && interactiveZone.zoneType() == ZoneType.WORK) {
                    graphicsContext.setStroke(HIGHLIGHT);
                    graphicsContext.setLineWidth(3);
                    graphicsContext.strokeRect(
                            gymObject.left(),
                            gymObject.top(),
                            gymObject.width(),
                            gymObject.height()
                    );
                }
                continue;
            }
            if (isGymMap(gameMap)
                    && gymObject instanceof InteractiveZone interactiveZone
                    && interactiveZone.zoneType() == ZoneType.DOOR) {
                continue;
            }

            Image image = imageFor(gymObject);
            if (hideMarker) {
                graphicsContext.setStroke(HIGHLIGHT);
                graphicsContext.setLineWidth(3);
                graphicsContext.strokeRoundRect(
                        gymObject.left(),
                        gymObject.top(),
                        gymObject.width(),
                        gymObject.height(),
                        18,
                        18
                );
            } else if (gymObject instanceof WarehouseProp) {
                drawWarehouseProp(graphicsContext, gymObject);
            } else if (image == null) {
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

            if (nearby && !(gymObject instanceof WarehouseProp)) {
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

            boolean hideGymAssetLabel = isGymMap(gameMap) && image != null && !nearby;
            if (!(gymObject instanceof WarehouseProp) && !hideGymAssetLabel && (!hideMarker || nearby)) {
                graphicsContext.setFill(LABEL_COLOR);
                graphicsContext.fillText(gymObject.name(), gymObject.centerX(), gymObject.centerY() - 6);

                graphicsContext.setFill(SUBTITLE_COLOR);
                graphicsContext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
                graphicsContext.fillText(gymObject.shortTypeLabel(), gymObject.centerX(), gymObject.centerY() + 16);
                graphicsContext.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            }
        }
    }

    private void drawGymObjects(GraphicsContext graphicsContext,
                                GameMap gameMap,
                                Optional<GymObject> nearbyObject,
                                Player player,
                                boolean foreground) {
        graphicsContext.setTextAlign(TextAlignment.CENTER);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        for (GymObject gymObject : gameMap.objects()) {
            if (gymObject instanceof InteractiveZone interactiveZone
                    && interactiveZone.zoneType() == ZoneType.DOOR) {
                continue;
            }

            Image image = imageFor(gymObject);
            double playerDepthY = player.position().y() + player.height();
            boolean drawInForeground = !(gymObject instanceof TrainingMachine trainingMachine
                    && trainingMachine.machineType() == MachineType.DEADLIFT_PLATFORM)
                    && gymObject.bottom() > playerDepthY;
            if (drawInForeground != foreground) {
                continue;
            }

            boolean nearby = nearbyObject.filter(object -> object == gymObject).isPresent();
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

            if (nearby) {
                double highlightInset = 6;
                double highlightWidth = Math.max(0, gymObject.width() - highlightInset * 2);
                double highlightHeight = Math.max(0, gymObject.height() - highlightInset * 2);
                graphicsContext.setStroke(HIGHLIGHT);
                graphicsContext.setLineWidth(2);
                graphicsContext.strokeRoundRect(
                        gymObject.left() + highlightInset,
                        gymObject.top() + highlightInset,
                        highlightWidth,
                        highlightHeight,
                        10,
                        10
                );
            }
        }
    }

    private boolean drawPlayer(GraphicsContext graphicsContext,
                               Player player,
                               boolean carryingWorkBox,
                               boolean wearingWorkUniform) {
        Image playerImage = playerImageFor(player, carryingWorkBox, wearingWorkUniform);
        if (playerImage != null) {
            double renderScale = wearingWorkUniform ? WORK_UNIFORM_RENDER_SCALE : 1.0;
            double widthScale = wearingWorkUniform ? WORK_UNIFORM_WIDTH_SCALE : 1.0;
            double renderWidth = player.profile().renderWidth() * renderScale * widthScale;
            double renderHeight = player.profile().renderHeight() * renderScale;
            double drawX = player.centerX() - renderWidth / 2.0;
            double drawY = player.position().y() + player.height() - renderHeight;
            graphicsContext.setImageSmoothing(false);
            graphicsContext.drawImage(playerImage, drawX, drawY, renderWidth, renderHeight);
            return carryingWorkBox
                    && ((wearingWorkUniform && workUniformSpriteSetFor(player, true) != null) || carryImageFor(player) != null);
        } else {
            graphicsContext.setFill(PLAYER_COLOR);
            graphicsContext.fillOval(player.position().x(), player.position().y(), player.width(), player.height());
            return false;
        }
    }

    private void drawCoachSpeechBubble(GraphicsContext graphicsContext,
                                       GameMap gameMap,
                                       Optional<String> coachSpeechText) {
        if (coachSpeechText.isEmpty() || coachSpeechText.get().isBlank()) {
            return;
        }

        Optional<InteractiveZone> coachZone = gameMap.objects().stream()
                .filter(InteractiveZone.class::isInstance)
                .map(InteractiveZone.class::cast)
                .filter(zone -> zone.zoneType() == ZoneType.COACH)
                .findFirst();
        if (coachZone.isEmpty()) {
            return;
        }

        Font bubbleFont = Font.font("Segoe UI", FontWeight.BOLD, 13);
        double maxTextWidth = 360;
        List<String> lines = wrapText(coachSpeechText.get(), bubbleFont, maxTextWidth);
        if (lines.isEmpty()) {
            return;
        }

        double lineHeight = 18;
        double paddingX = 18;
        double paddingY = 14;
        double maxLineWidth = lines.stream()
                .mapToDouble(line -> measureTextWidth(line, bubbleFont))
                .max()
                .orElse(180);

        double bubbleWidth = Math.max(240, Math.min(maxTextWidth + paddingX * 2, maxLineWidth + paddingX * 2));
        double bubbleHeight = lines.size() * lineHeight + paddingY * 2;
        double anchorX = coachZone.get().centerX();
        double bubbleLeft = clamp(anchorX - bubbleWidth / 2.0, 20, graphicsContext.getCanvas().getWidth() - bubbleWidth - 20);
        double bubbleTop = Math.max(20, coachZone.get().top() - bubbleHeight - 30);
        double tailTipX = clamp(anchorX, bubbleLeft + 36, bubbleLeft + bubbleWidth - 36);
        double tailTipY = coachZone.get().top() + 8;
        double tailBaseY = bubbleTop + bubbleHeight - 4;

        graphicsContext.save();
        graphicsContext.setTextAlign(TextAlignment.LEFT);
        graphicsContext.setTextBaseline(VPos.TOP);
        graphicsContext.setFont(bubbleFont);

        graphicsContext.setFill(Color.color(0.03, 0.05, 0.08, 0.26));
        graphicsContext.fillRoundRect(bubbleLeft + 5, bubbleTop + 6, bubbleWidth, bubbleHeight, 18, 18);
        fillTriangle(graphicsContext, tailTipX + 5, tailTipY + 6, tailTipX - 13 + 5, tailBaseY + 6, tailTipX + 13 + 5, tailBaseY + 6);

        graphicsContext.setFill(Color.color(0.98, 0.98, 0.99, 0.97));
        graphicsContext.fillRoundRect(bubbleLeft, bubbleTop, bubbleWidth, bubbleHeight, 18, 18);
        fillTriangle(graphicsContext, tailTipX, tailTipY, tailTipX - 13, tailBaseY, tailTipX + 13, tailBaseY);

        graphicsContext.setStroke(Color.web("#1F2937"));
        graphicsContext.setLineWidth(2);
        graphicsContext.strokeRoundRect(bubbleLeft, bubbleTop, bubbleWidth, bubbleHeight, 18, 18);
        strokeTriangle(graphicsContext, tailTipX, tailTipY, tailTipX - 13, tailBaseY, tailTipX + 13, tailBaseY);

        graphicsContext.setFill(Color.web("#111827"));
        for (int index = 0; index < lines.size(); index++) {
            graphicsContext.fillText(
                    lines.get(index),
                    bubbleLeft + paddingX,
                    bubbleTop + paddingY + index * lineHeight
            );
        }
        graphicsContext.restore();
    }

    private Image playerImageFor(Player player, boolean carryingWorkBox, boolean wearingWorkUniform) {
        if (wearingWorkUniform) {
            PlayerSpriteSet spriteSet = workUniformSpriteSetFor(player, carryingWorkBox);
            if (spriteSet != null) {
                return playerImageFromSpriteSet(player, spriteSet);
            }
        }

        if (carryingWorkBox) {
            Image carryImage = carryImageFor(player);
            if (carryImage != null) {
                return carryImage;
            }
        }

        return playerImageFromSpriteSet(player, spriteSetFor(player));
    }

    private Image playerImageFromSpriteSet(Player player, PlayerSpriteSet spriteSet) {
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

    private PlayerSpriteSet spriteSetFor(Player player) {
        String cacheKey = player.profile().id() + ":" + player.currentForm().name();
        return playerSpriteSets.computeIfAbsent(cacheKey, ignored -> loadPlayerSpriteSet(player.profile(), player.currentForm()));
    }

    private Image carryImageFor(Player player) {
        DirectionalSpriteSet spriteSet = carrySpriteSetFor(player.profile());
        if (player.isMoving() && shouldShowWalkFrame()) {
            Image stepImage = directionalImage(spriteSet.stepFrames(), player.direction());
            if (stepImage != null) {
                return stepImage;
            }
        }
        return directionalImage(spriteSet.frames(), player.direction());
    }

    private DirectionalSpriteSet carrySpriteSetFor(PlayerProfile profile) {
        return playerCarrySpriteSets.computeIfAbsent(profile.id(), ignored -> loadCarrySpriteSet(profile));
    }

    private PlayerSpriteSet workUniformSpriteSetFor(Player player, boolean carryingBox) {
        String sheetPath = workUniformSheetPath(player.profile(), player.currentForm(), carryingBox);
        String referenceSheetPath = workUniformSheetPath(player.profile(), player.currentForm(), false);
        String cacheKey = player.profile().id() + ":" + sheetPath;
        return workUniformSpriteSets.computeIfAbsent(
                cacheKey,
                ignored -> loadWorkUniformSpriteSet(loadImage(sheetPath), loadImage(referenceSheetPath), player.profile().id())
        );
    }

    private String workUniformSheetPath(PlayerProfile profile, PlayerForm form, boolean carryingBox) {
        String spriteNumber = switch (profile.id()) {
            case "street_rookie" -> switch (form) {
                case BASE -> "10";
                case SECOND -> "9";
                case THIRD -> "11";
                case FOURTH, FOURTH_STEROIDS -> "8";
            };
            case "fatty_popka" -> switch (form) {
                case BASE -> "12";
                case SECOND -> "5";
                case THIRD -> "7";
                case FOURTH -> "4";
                case FOURTH_STEROIDS -> "6";
            };
            case "dark_drun" -> switch (form) {
                case BASE -> "13";
                case SECOND -> "1";
                case THIRD -> "3";
                case FOURTH, FOURTH_STEROIDS -> "2";
            };
            default -> "1";
        };
        return "/assets/characters/" + spriteNumber + (carryingBox ? "_box" : "") + ".png";
    }

    private PlayerSpriteSet loadWorkUniformSpriteSet(Image sheet, Image referenceSheet, String profileId) {
        if (sheet == null || sheet.getPixelReader() == null || sheet.getWidth() <= 0 || sheet.getHeight() <= 0) {
            return null;
        }
        if (referenceSheet == null || referenceSheet.getPixelReader() == null) {
            referenceSheet = sheet;
        }

        int frameWidth = Math.max(1, (int) Math.floor(sheet.getWidth() / 4.0));
        int frameHeight = Math.max(1, (int) Math.floor(sheet.getHeight() / 2.0));
        int referenceFrameWidth = Math.max(1, (int) Math.floor(referenceSheet.getWidth() / 4.0));
        int referenceFrameHeight = Math.max(1, (int) Math.floor(referenceSheet.getHeight() / 2.0));
        Map<PlayerDirection, Image> idleFrames = new EnumMap<>(PlayerDirection.class);
        Map<PlayerDirection, Image> walkFrames = new EnumMap<>(PlayerDirection.class);

        Image frontIdle = cropFrame(sheet, referenceSheet, 0, 0, frameWidth, frameHeight, referenceFrameWidth, referenceFrameHeight);
        Image frontWalk = cropFrame(sheet, referenceSheet, 1, 0, frameWidth, frameHeight, referenceFrameWidth, referenceFrameHeight);
        Image backIdle = cropFrame(sheet, referenceSheet, 0, 1, frameWidth, frameHeight, referenceFrameWidth, referenceFrameHeight);
        Image backWalk = cropFrame(sheet, referenceSheet, 1, 1, frameWidth, frameHeight, referenceFrameWidth, referenceFrameHeight);
        idleFrames.put(PlayerDirection.FRONT, frontIdle);
        walkFrames.put(PlayerDirection.FRONT, frontWalk);
        Image sideIdle = cropFrame(sheet, referenceSheet, 2, 0, frameWidth, frameHeight, referenceFrameWidth, referenceFrameHeight);
        Image sideWalk = cropFrame(sheet, referenceSheet, 3, 0, frameWidth, frameHeight, referenceFrameWidth, referenceFrameHeight);
        if ("dark_drun".equals(profileId)) {
            idleFrames.put(PlayerDirection.RIGHT, mirrorImage(sideIdle));
            walkFrames.put(PlayerDirection.RIGHT, mirrorImage(sideWalk));
            idleFrames.put(PlayerDirection.LEFT, sideIdle);
            walkFrames.put(PlayerDirection.LEFT, sideWalk);
        } else {
            idleFrames.put(PlayerDirection.RIGHT, sideIdle);
            walkFrames.put(PlayerDirection.RIGHT, sideWalk);
            idleFrames.put(PlayerDirection.LEFT, mirrorImage(sideIdle));
            walkFrames.put(PlayerDirection.LEFT, mirrorImage(sideWalk));
        }
        idleFrames.put(PlayerDirection.BACK, backIdle);
        walkFrames.put(PlayerDirection.BACK, backWalk);

        return new PlayerSpriteSet(idleFrames, walkFrames);
    }

    private Image cropFrame(Image sheet,
                            Image referenceSheet,
                            int column,
                            int row,
                            int frameWidth,
                            int frameHeight,
                            int referenceFrameWidth,
                            int referenceFrameHeight) {
        int x = column * frameWidth;
        int y = row * frameHeight;
        int width = Math.max(1, Math.min(frameWidth, (int) sheet.getWidth() - x));
        int height = Math.max(1, Math.min(frameHeight, (int) sheet.getHeight() - y));
        boolean[] backgroundMask = backgroundMask(sheet, x, y, width, height);
        removeSmallForegroundArtifacts(backgroundMask, width, height);
        FrameBounds sourceBounds = foregroundBounds(backgroundMask, width, height);

        if (sourceBounds.isEmpty()) {
            return new WritableImage(WORK_UNIFORM_FRAME_WIDTH, WORK_UNIFORM_FRAME_HEIGHT);
        }

        FrameBounds referenceBounds = referenceBounds(
                referenceSheet,
                column,
                row,
                referenceFrameWidth,
                referenceFrameHeight
        );
        if (referenceBounds.isEmpty()) {
            referenceBounds = sourceBounds;
        }

        int sourceWidth = sourceBounds.width();
        int sourceHeight = sourceBounds.height();
        int scaleWidth = Math.max(referenceBounds.width(), sourceWidth);
        int scaleHeight = Math.max(referenceBounds.height(), sourceHeight);
        double scale = Math.min(
                WORK_UNIFORM_CONTENT_HEIGHT / (double) scaleHeight,
                WORK_UNIFORM_MAX_CONTENT_WIDTH / (double) scaleWidth
        );
        int targetWidth = Math.max(1, (int) Math.round(sourceWidth * scale));
        int targetHeight = Math.max(1, (int) Math.round(sourceHeight * scale));
        int targetX = Math.max(0, (WORK_UNIFORM_FRAME_WIDTH - targetWidth) / 2);
        int targetY = Math.max(0, WORK_UNIFORM_FRAME_HEIGHT - targetHeight - 18);

        WritableImage image = new WritableImage(WORK_UNIFORM_FRAME_WIDTH, WORK_UNIFORM_FRAME_HEIGHT);
        for (int targetPixelY = 0; targetPixelY < targetHeight; targetPixelY++) {
            int sourcePixelY = sourceBounds.minY() + Math.min(sourceHeight - 1, (int) Math.floor(targetPixelY / scale));
            for (int targetPixelX = 0; targetPixelX < targetWidth; targetPixelX++) {
                int sourcePixelX = sourceBounds.minX() + Math.min(sourceWidth - 1, (int) Math.floor(targetPixelX / scale));
                int finalX = targetX + targetPixelX;
                int finalY = targetY + targetPixelY;
                if (finalX < 0 || finalY < 0 || finalX >= WORK_UNIFORM_FRAME_WIDTH || finalY >= WORK_UNIFORM_FRAME_HEIGHT) {
                    continue;
                }
                Color color = sheet.getPixelReader().getColor(x + sourcePixelX, y + sourcePixelY);
                if (backgroundMask[sourcePixelY * width + sourcePixelX]) {
                    color = Color.TRANSPARENT;
                }
                image.getPixelWriter().setColor(finalX, finalY, color);
            }
        }
        return image;
    }

    private FrameBounds referenceBounds(Image sheet, int column, int row, int frameWidth, int frameHeight) {
        int x = column * frameWidth;
        int y = row * frameHeight;
        int width = Math.max(1, Math.min(frameWidth, (int) sheet.getWidth() - x));
        int height = Math.max(1, Math.min(frameHeight, (int) sheet.getHeight() - y));
        boolean[] mask = backgroundMask(sheet, x, y, width, height);
        removeSmallForegroundArtifacts(mask, width, height);
        return foregroundBounds(mask, width, height);
    }

    private FrameBounds foregroundBounds(boolean[] backgroundMask, int width, int height) {
        int minX = width;
        int minY = height;
        int maxX = -1;
        int maxY = -1;

        for (int frameY = 0; frameY < height; frameY++) {
            for (int frameX = 0; frameX < width; frameX++) {
                if (!backgroundMask[frameY * width + frameX]) {
                    minX = Math.min(minX, frameX);
                    minY = Math.min(minY, frameY);
                    maxX = Math.max(maxX, frameX);
                    maxY = Math.max(maxY, frameY);
                }
            }
        }

        return new FrameBounds(minX, minY, maxX, maxY);
    }

    private void removeSmallForegroundArtifacts(boolean[] backgroundMask, int width, int height) {
        boolean[] visited = new boolean[backgroundMask.length];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        List<Integer> component = new ArrayList<>();
        int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int startIndex = y * width + x;
                if (backgroundMask[startIndex] || visited[startIndex]) {
                    continue;
                }

                component.clear();
                visited[startIndex] = true;
                queue.addLast(new int[]{x, y});
                while (!queue.isEmpty()) {
                    int[] point = queue.removeFirst();
                    int index = point[1] * width + point[0];
                    component.add(index);

                    for (int[] offset : offsets) {
                        int nextX = point[0] + offset[0];
                        int nextY = point[1] + offset[1];
                        if (nextX < 0 || nextY < 0 || nextX >= width || nextY >= height) {
                            continue;
                        }
                        int nextIndex = nextY * width + nextX;
                        if (backgroundMask[nextIndex] || visited[nextIndex]) {
                            continue;
                        }
                        visited[nextIndex] = true;
                        queue.addLast(new int[]{nextX, nextY});
                    }
                }

                if (component.size() < 900) {
                    for (int index : component) {
                        backgroundMask[index] = true;
                    }
                }
            }
        }
    }

    private boolean[] backgroundMask(Image sheet, int x, int y, int width, int height) {
        boolean[] mask = new boolean[width * height];
        Color[] edgeColors = {
                sheet.getPixelReader().getColor(x, y),
                sheet.getPixelReader().getColor(x + width - 1, y),
                sheet.getPixelReader().getColor(x, y + height - 1),
                sheet.getPixelReader().getColor(x + width - 1, y + height - 1)
        };
        ArrayDeque<int[]> queue = new ArrayDeque<>();

        for (int px = 0; px < width; px++) {
            enqueueBackgroundPixel(sheet, x, y, width, height, edgeColors, mask, queue, px, 0);
            enqueueBackgroundPixel(sheet, x, y, width, height, edgeColors, mask, queue, px, height - 1);
        }
        for (int py = 1; py < height - 1; py++) {
            enqueueBackgroundPixel(sheet, x, y, width, height, edgeColors, mask, queue, 0, py);
            enqueueBackgroundPixel(sheet, x, y, width, height, edgeColors, mask, queue, width - 1, py);
        }

        int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            int[] point = queue.removeFirst();
            for (int[] offset : offsets) {
                int nextX = point[0] + offset[0];
                int nextY = point[1] + offset[1];
                enqueueBackgroundPixel(sheet, x, y, width, height, edgeColors, mask, queue, nextX, nextY);
            }
        }

        return mask;
    }

    private void enqueueBackgroundPixel(Image sheet,
                                        int sourceX,
                                        int sourceY,
                                        int width,
                                        int height,
                                        Color[] edgeColors,
                                        boolean[] mask,
                                        ArrayDeque<int[]> queue,
                                        int x,
                                        int y) {
        if (x < 0 || y < 0 || x >= width || y >= height || mask[y * width + x]) {
            return;
        }
        Color color = sheet.getPixelReader().getColor(sourceX + x, sourceY + y);
        if (!isBackgroundColor(color, edgeColors)) {
            return;
        }
        mask[y * width + x] = true;
        queue.addLast(new int[]{x, y});
    }

    private boolean isBackgroundColor(Color color, Color[] edgeColors) {
        if (color.getOpacity() <= 0.05) {
            return true;
        }
        for (Color edgeColor : edgeColors) {
            double edgeBrightness = (edgeColor.getRed() + edgeColor.getGreen() + edgeColor.getBlue()) / 3.0;
            double threshold = edgeBrightness < 0.12 ? 0.04 : 0.17;
            if (colorDistance(color, edgeColor) < threshold) {
                return true;
            }
        }
        double brightness = (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
        double channelSpread = Math.max(color.getRed(), Math.max(color.getGreen(), color.getBlue()))
                - Math.min(color.getRed(), Math.min(color.getGreen(), color.getBlue()));
        return brightness > 0.86 && channelSpread < 0.12;
    }

    private double colorDistance(Color first, Color second) {
        double red = first.getRed() - second.getRed();
        double green = first.getGreen() - second.getGreen();
        double blue = first.getBlue() - second.getBlue();
        return Math.sqrt(red * red + green * green + blue * blue);
    }

    private Image mirrorImage(Image image) {
        if (image == null || image.getPixelReader() == null) {
            return image;
        }
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        WritableImage mirrored = new WritableImage(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                mirrored.getPixelWriter().setColor(width - x - 1, y, image.getPixelReader().getColor(x, y));
            }
        }
        return mirrored;
    }

    private PlayerSpriteSet loadPlayerSpriteSet(PlayerProfile profile, gymgrind.player.PlayerForm form) {
        Map<PlayerDirection, Image> idleFrames = new EnumMap<>(PlayerDirection.class);
        Map<PlayerDirection, Image> walkFrames = new EnumMap<>(PlayerDirection.class);

        for (PlayerDirection direction : PlayerDirection.values()) {
            idleFrames.put(direction, loadImage(profile.idleSpritePath(direction, form)));
            walkFrames.put(direction, loadImage(profile.walkSpritePath(direction, form)));
        }

        return new PlayerSpriteSet(idleFrames, walkFrames);
    }

    private DirectionalSpriteSet loadCarrySpriteSet(PlayerProfile profile) {
        Map<PlayerDirection, Image> frames = new EnumMap<>(PlayerDirection.class);
        Map<PlayerDirection, Image> stepFrames = new EnumMap<>(PlayerDirection.class);

        for (PlayerDirection direction : PlayerDirection.values()) {
            frames.put(direction, loadImage(carrySpritePath(profile, direction)));
            stepFrames.put(direction, loadImage(carryStepSpritePath(profile, direction)));
        }

        return new DirectionalSpriteSet(frames, stepFrames);
    }

    private String carrySpritePath(PlayerProfile profile, PlayerDirection direction) {
        return profile.idleSpritePath(direction).replace("_idle_", "_carry_");
    }

    private String carryStepSpritePath(PlayerProfile profile, PlayerDirection direction) {
        return profile.idleSpritePath(direction).replace("_idle_", "_carry_").replace(".png", "_step.png");
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

    private void drawGymMap(GraphicsContext graphicsContext, GameMap gameMap) {
        double left = gameMap.left();
        double top = gameMap.top();
        double width = gameMap.width();
        double height = gameMap.height();

        if (gymRoomImage != null) {
            graphicsContext.drawImage(gymRoomImage, left, top, width, height);
        } else {
            graphicsContext.setFill(Color.web("#050507"));
            graphicsContext.fillRect(left - 26, top - 26, width + 52, height + 52);
            drawGymWoodFloor(graphicsContext, left, top, width, height);
            drawGymBackWall(graphicsContext, left + 34, top + 24, width - 68, 112);
            drawGymBorder(graphicsContext, left, top, width, height);
            drawGymTopDoor(graphicsContext, left + width / 2.0 - 54, top - 6);
            drawGymBottomDoor(graphicsContext, left + width / 2.0 - 62, top + height - 12);
            drawGymImageOrFallback(graphicsContext, gymWaterCoolerImage, left + 54, top + 70, 72, 158,
                    () -> drawGymWaterCooler(graphicsContext, left + 64, top + 74));
            drawGymImageOrFallback(graphicsContext, gymLockerImage, left + width - 314, top + 62, 214, 144,
                    () -> drawGymLockers(graphicsContext, left + width - 292, top + 76));
        }
        drawGymRubberZone(graphicsContext, left + 46, top + 144, 360, 382);
        drawGymImageOrFallback(graphicsContext, gymDumbbellRackImage, left + 638, top + 176, 184, 124,
                () -> drawDumbbellRack(graphicsContext, left + 650, top + 172));
        drawGymImageOrFallback(graphicsContext, gymBenchImage, left + 480, top + 226, 58, 144,
                () -> drawGymBenchFallback(graphicsContext, left + 480, top + 226));
        drawGymImageOrFallback(graphicsContext, gymBenchImage, left + 570, top + 226, 58, 144,
                () -> drawGymBenchFallback(graphicsContext, left + 570, top + 226));
        drawGymImageOrFallback(graphicsContext, gymMatsImage, left + width - 320, top + height - 182, 152, 104,
                () -> drawGymMats(graphicsContext, left + width - 310, top + height - 182));
        drawGymImageOrFallback(graphicsContext, gymKettlebellRackImage, left + width - 128, top + height - 236, 64, 130,
                () -> drawKettlebellStand(graphicsContext, left + width - 128, top + height - 232));
    }

    private void drawGymImageOrFallback(GraphicsContext graphicsContext,
                                        Image image,
                                        double x,
                                        double y,
                                        double width,
                                        double height,
                                        Runnable fallback) {
        if (image != null) {
            graphicsContext.drawImage(image, x, y, width, height);
            return;
        }
        fallback.run();
    }

    private void drawGymWoodFloor(GraphicsContext graphicsContext, double left, double top, double width, double height) {
        graphicsContext.setFill(Color.web("#9B5A24"));
        graphicsContext.fillRect(left, top, width, height);
        graphicsContext.setStroke(Color.color(0.38, 0.20, 0.08, 0.48));
        graphicsContext.setLineWidth(2);
        double plankHeight = 24;
        for (double y = top; y < top + height; y += plankHeight) {
            graphicsContext.strokeLine(left, y, left + width, y);
            double offset = Math.floor((y - top) / plankHeight) % 2 == 0 ? 0 : 78;
            for (double x = left + offset; x < left + width; x += 156) {
                graphicsContext.strokeLine(x, y, x, Math.min(y + plankHeight, top + height));
            }
        }
        graphicsContext.setFill(Color.color(1.0, 0.68, 0.30, 0.08));
        graphicsContext.fillRect(left + 22, top + 138, width - 44, height - 170);
    }

    private void drawGymRubberZone(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        graphicsContext.setFill(Color.web("#262A2B"));
        graphicsContext.fillRect(x, y, width, height);
        graphicsContext.setStroke(Color.web("#393F40"));
        graphicsContext.setLineWidth(2);
        for (double tileX = x; tileX <= x + width; tileX += 72) {
            graphicsContext.strokeLine(tileX, y, tileX, y + height);
        }
        for (double tileY = y; tileY <= y + height; tileY += 72) {
            graphicsContext.strokeLine(x, tileY, x + width, tileY);
        }
        graphicsContext.setStroke(Color.web("#161819"));
        graphicsContext.setLineWidth(4);
        graphicsContext.strokeRect(x, y, width, height);
    }

    private void drawGymBackWall(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        graphicsContext.setFill(Color.web("#303036"));
        graphicsContext.fillRect(x, y, width, height);
        graphicsContext.setFill(Color.web("#4A4B4B"));
        graphicsContext.fillRect(x + 28, y + 38, width - 56, height - 54);
        graphicsContext.setStroke(Color.web("#24242A"));
        graphicsContext.setLineWidth(3);
        for (double brickY = y + 10; brickY < y + height; brickY += 22) {
            graphicsContext.strokeLine(x, brickY, x + width, brickY);
            double offset = Math.floor((brickY - y) / 22) % 2 == 0 ? 0 : 42;
            for (double brickX = x + offset; brickX < x + width; brickX += 84) {
                graphicsContext.strokeLine(brickX, brickY, brickX, brickY + 22);
            }
        }
        graphicsContext.setFill(Color.web("#1C1B35"));
        graphicsContext.fillRect(x, y + height - 20, width, 20);
    }

    private void drawGymBorder(GraphicsContext graphicsContext, double left, double top, double width, double height) {
        graphicsContext.setFill(Color.web("#E7E4F6"));
        graphicsContext.fillRect(left - 10, top - 10, width + 20, 28);
        graphicsContext.fillRect(left - 10, top + height - 18, width + 20, 28);
        graphicsContext.fillRect(left - 10, top - 10, 28, height + 20);
        graphicsContext.fillRect(left + width - 18, top - 10, 28, height + 20);
        graphicsContext.setStroke(Color.web("#9C9BC4"));
        graphicsContext.setLineWidth(2);
        for (double x = left - 10; x <= left + width + 10; x += 24) {
            graphicsContext.strokeLine(x, top - 10, x, top + 18);
            graphicsContext.strokeLine(x, top + height - 18, x, top + height + 10);
        }
        for (double y = top - 10; y <= top + height + 10; y += 24) {
            graphicsContext.strokeLine(left - 10, y, left + 18, y);
            graphicsContext.strokeLine(left + width - 18, y, left + width + 10, y);
        }
        graphicsContext.setStroke(Color.web("#1D1B36"));
        graphicsContext.setLineWidth(10);
        graphicsContext.strokeRect(left + 14, top + 16, width - 28, height - 32);
    }

    private void drawGymTopDoor(GraphicsContext graphicsContext, double x, double y) {
        graphicsContext.setFill(Color.web("#1A1110"));
        graphicsContext.fillRect(x - 10, y, 128, 92);
        graphicsContext.setFill(Color.web("#7A3F17"));
        graphicsContext.fillRect(x + 12, y + 18, 84, 70);
        graphicsContext.setFill(Color.web("#A5652A"));
        graphicsContext.fillRect(x + 20, y + 24, 30, 58);
        graphicsContext.fillRect(x + 54, y + 24, 30, 58);
    }

    private void drawGymBottomDoor(GraphicsContext graphicsContext, double x, double y) {
        graphicsContext.setFill(Color.web("#3D3D38"));
        graphicsContext.fillRect(x, y, 124, 38);
        graphicsContext.setStroke(Color.web("#6B665B"));
        graphicsContext.setLineWidth(3);
        graphicsContext.strokeLine(x + 18, y, x + 18, y + 38);
        graphicsContext.strokeLine(x + 62, y, x + 62, y + 38);
        graphicsContext.strokeLine(x + 106, y, x + 106, y + 38);
    }

    private void drawGymWaterCooler(GraphicsContext graphicsContext, double x, double y) {
        graphicsContext.setFill(Color.web("#D8DEE5"));
        graphicsContext.fillRect(x, y + 38, 34, 54);
        graphicsContext.setFill(Color.web("#4EA7E8"));
        graphicsContext.fillOval(x + 3, y, 28, 46);
        graphicsContext.setFill(Color.web("#1B5E8C"));
        graphicsContext.fillRect(x + 45, y + 54, 34, 38);
    }

    private void drawGymBenchFallback(GraphicsContext graphicsContext, double x, double y) {
        graphicsContext.setFill(Color.web("#A65E23"));
        graphicsContext.fillRect(x + 14, y + 24, 50, 118);
        graphicsContext.setStroke(Color.web("#161719"));
        graphicsContext.setLineWidth(4);
        graphicsContext.strokeRect(x + 14, y + 24, 50, 118);
        graphicsContext.setFill(Color.web("#2B2F31"));
        graphicsContext.fillRect(x + 10, y + 142, 58, 12);
        graphicsContext.fillRect(x + 18, y + 154, 12, 28);
        graphicsContext.fillRect(x + 48, y + 154, 12, 28);
    }

    private void drawGymLockers(GraphicsContext graphicsContext, double x, double y) {
        graphicsContext.setFill(Color.web("#2D2F31"));
        graphicsContext.fillRect(x, y, 172, 88);
        graphicsContext.setStroke(Color.web("#0E0F10"));
        graphicsContext.setLineWidth(3);
        graphicsContext.strokeRect(x, y, 172, 88);
        for (int i = 0; i < 3; i++) {
            double doorX = x + 8 + i * 54;
            graphicsContext.setFill(i == 2 ? Color.web("#333638") : Color.web("#3C3F41"));
            graphicsContext.fillRect(doorX, y + 8, 48, 72);
            graphicsContext.strokeRect(doorX, y + 8, 48, 72);
            graphicsContext.strokeLine(doorX + 14, y + 28, doorX + 30, y + 28);
            graphicsContext.strokeLine(doorX + 14, y + 34, doorX + 30, y + 34);
        }
        graphicsContext.setFill(Color.web("#2D7E35"));
        graphicsContext.fillOval(x + 194, y + 36, 36, 44);
        graphicsContext.setFill(Color.web("#3D3024"));
        graphicsContext.fillRect(x + 204, y + 72, 18, 22);
    }

    private void drawDumbbellRack(GraphicsContext graphicsContext, double x, double y) {
        graphicsContext.setFill(Color.web("#2C2117"));
        graphicsContext.fillRect(x, y + 66, 170, 12);
        graphicsContext.fillRect(x, y + 26, 170, 12);
        for (int row = 0; row < 2; row++) {
            for (int i = 0; i < 7; i++) {
                double cx = x + 18 + i * 22;
                double cy = y + 12 + row * 40;
                graphicsContext.setFill(Color.web("#101112"));
                graphicsContext.fillOval(cx, cy, 18, 18);
                graphicsContext.setFill(Color.web("#3A3D40"));
                graphicsContext.fillRect(cx + 5, cy + 7, 8, 4);
            }
        }
    }

    private void drawGymMats(GraphicsContext graphicsContext, double x, double y) {
        graphicsContext.setFill(Color.web("#174A8A"));
        graphicsContext.fillRect(x, y, 76, 94);
        graphicsContext.setFill(Color.web("#1D5EA8"));
        graphicsContext.fillRect(x + 82, y, 76, 94);
        graphicsContext.setStroke(Color.web("#0B2445"));
        graphicsContext.setLineWidth(3);
        graphicsContext.strokeRect(x, y, 76, 94);
        graphicsContext.strokeRect(x + 82, y, 76, 94);
    }

    private void drawKettlebellStand(GraphicsContext graphicsContext, double x, double y) {
        graphicsContext.setFill(Color.web("#17191A"));
        graphicsContext.fillRect(x + 8, y, 56, 146);
        graphicsContext.setStroke(Color.web("#4B4F52"));
        graphicsContext.setLineWidth(4);
        graphicsContext.strokeRect(x + 8, y, 56, 146);
        Color[] colors = {Color.web("#C66A20"), Color.web("#1E61A8"), Color.web("#161719")};
        for (int i = 0; i < 3; i++) {
            graphicsContext.setFill(colors[i]);
            graphicsContext.fillOval(x + 22, y + 16 + i * 42, 28, 28);
            graphicsContext.setStroke(Color.web("#0B0D0E"));
            graphicsContext.setLineWidth(3);
            graphicsContext.strokeOval(x + 22, y + 16 + i * 42, 28, 28);
        }
    }

    private void drawWarehouseMap(GraphicsContext graphicsContext, GameMap gameMap) {
        double left = gameMap.left();
        double top = gameMap.top();
        double width = gameMap.width();
        double height = gameMap.height();

        graphicsContext.setFill(Color.web("#050505"));
        graphicsContext.fillRect(left - 28, top - 28, width + 56, height + 56);

        graphicsContext.setFill(Color.web("#595348"));
        graphicsContext.fillRect(left, top, width, height);

        graphicsContext.setStroke(Color.web("#625B50"));
        graphicsContext.setLineWidth(1);
        for (double x = left; x <= left + width; x += 32) {
            graphicsContext.strokeLine(x, top, x, top + height);
        }
        for (double y = top; y <= top + height; y += 32) {
            graphicsContext.strokeLine(left, y, left + width, y);
        }

        drawWarehouseFloorNoise(graphicsContext, left, top, width, height);
        drawSafetyStripes(graphicsContext, left + 86, top + 340, 150, 88);
        drawSafetyStripes(graphicsContext, left + 456, top + 374, 180, 86);
        drawSafetyStripes(graphicsContext, left + 830, top + 112, 180, 70);
        drawWarehouseWoodWall(graphicsContext, left + 14, top + 12, width - 28, 118);
        drawWarehouseBorder(graphicsContext, left, top, width, height);
        drawWarehouseAsset(graphicsContext, warehouseWorkbenchImage, left + 38, top + 42, 176, 128);
        drawWarehouseAsset(graphicsContext, warehouseBinsImage, left + 616, top + 68, 118, 105);
        drawWarehouseAsset(graphicsContext, warehousePalletJackImage, left + 32, top + 416, 150, 115);
        drawWarehouseAsset(graphicsContext, warehousePalletImage, left + 892, top + 346, 145, 121);
        drawHangingLamp(graphicsContext, left + 528, top + 26);
        drawHangingLamp(graphicsContext, left + 700, top + 26);
        drawWarehouseDoor(graphicsContext, left + 512, top + height - 70);
    }

    private void drawWarehouseFloorNoise(GraphicsContext graphicsContext, double left, double top, double width, double height) {
        graphicsContext.setFill(Color.color(0.13, 0.12, 0.10, 0.22));
        for (int index = 0; index < 90; index++) {
            double x = left + 24 + Math.floorMod(index * 47, (int) width - 48);
            double y = top + 42 + Math.floorMod(index * 31, (int) height - 84);
            graphicsContext.fillRect(x, y, 2 + index % 4, 2);
        }
    }

    private void drawWarehouseAsset(GraphicsContext graphicsContext,
                                    Image image,
                                    double x,
                                    double y,
                                    double width,
                                    double height) {
        if (image == null) {
            return;
        }

        graphicsContext.setFill(Color.color(0.02, 0.02, 0.02, 0.24));
        graphicsContext.fillOval(x + width * 0.16, y + height * 0.78, width * 0.68, height * 0.12);
        graphicsContext.drawImage(image, x, y, width, height);
    }

    private void drawSafetyStripes(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        graphicsContext.setStroke(Color.color(0.95, 0.58, 0.13, 0.55));
        graphicsContext.setLineWidth(3);
        graphicsContext.strokeRect(x, y, width, height);
        for (double offset = -height; offset < width; offset += 22) {
            graphicsContext.strokeLine(x + offset, y + height, x + offset + height, y);
        }
    }

    private void drawWarehouseWoodWall(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        graphicsContext.setFill(Color.web("#6E381C"));
        graphicsContext.fillRect(x, y, width, height);
        graphicsContext.setFill(Color.web("#8E4A22"));
        graphicsContext.fillRect(x, y, width, 16);
        graphicsContext.fillRect(x, y + height - 14, width, 14);

        graphicsContext.setStroke(Color.web("#3C2012"));
        graphicsContext.setLineWidth(2);
        for (double boardX = x + 18; boardX < x + width; boardX += 24) {
            graphicsContext.strokeLine(boardX, y + 16, boardX, y + height - 14);
        }
        graphicsContext.setFill(Color.color(1.0, 0.78, 0.38, 0.12));
        graphicsContext.fillOval(x + width * 0.42, y + 10, 150, 78);
        graphicsContext.fillOval(x + width * 0.58, y + 10, 150, 78);
    }

    private void drawWarehouseBorder(GraphicsContext graphicsContext, double left, double top, double width, double height) {
        graphicsContext.setFill(Color.web("#5B2F18"));
        graphicsContext.fillRect(left - 6, top - 6, width + 12, 18);
        graphicsContext.fillRect(left - 6, top + height - 12, width + 12, 18);
        graphicsContext.fillRect(left - 6, top - 6, 18, height + 12);
        graphicsContext.fillRect(left + width - 12, top - 6, 18, height + 12);

        graphicsContext.setStroke(Color.web("#E58A30"));
        graphicsContext.setLineWidth(3);
        graphicsContext.strokeRect(left - 6, top - 6, width + 12, height + 12);

        graphicsContext.setStroke(Color.web("#6B3A1F"));
        graphicsContext.setLineWidth(2);
        for (double x = left + 28; x < left + width - 28; x += 54) {
            graphicsContext.strokeLine(x, top - 1, x + 28, top + 12);
            graphicsContext.strokeLine(x, top + height + 1, x + 28, top + height - 12);
        }
    }

    private void drawWarehouseOffice(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        graphicsContext.setFill(Color.web("#5E371F"));
        graphicsContext.fillRoundRect(x, y, width, height, 10, 10);
        graphicsContext.setFill(Color.web("#88C6D8"));
        graphicsContext.fillRect(x + 18, y + 18, width - 36, 44);
        graphicsContext.setStroke(Color.web("#153041"));
        graphicsContext.setLineWidth(3);
        graphicsContext.strokeRect(x + 18, y + 18, width - 36, 44);
        graphicsContext.setFill(Color.web("#D5A55A"));
        graphicsContext.fillRect(x + 20, y + 76, width - 40, height - 92);
        drawOrangeCarton(graphicsContext, x + width - 62, y + 86, 34, 28);
    }

    private void drawWorkbench(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        graphicsContext.setFill(Color.web("#5A321B"));
        graphicsContext.fillRect(x + 8, y + height - 10, width - 16, 12);
        graphicsContext.setFill(Color.web("#9B5A2B"));
        graphicsContext.fillRoundRect(x, y, width, height, 8, 8);
        graphicsContext.setFill(Color.web("#C98543"));
        graphicsContext.fillRect(x + 10, y + 8, width - 20, 16);
        drawWrappedPallet(graphicsContext, x + 78, y + 30, 34, 24);
        drawOrangeCarton(graphicsContext, x + 142, y + 28, 34, 26);
        graphicsContext.setFill(Color.web("#77A7C8"));
        graphicsContext.fillRect(x + width - 32, y + 24, 11, 26);
    }

    private void drawHangingLamp(GraphicsContext graphicsContext, double x, double y) {
        graphicsContext.setStroke(Color.web("#21160F"));
        graphicsContext.setLineWidth(3);
        graphicsContext.strokeLine(x, y, x, y + 34);
        graphicsContext.setFill(Color.web("#1B1715"));
        graphicsContext.fillOval(x - 18, y + 30, 36, 18);
        graphicsContext.setFill(Color.color(1.0, 0.78, 0.35, 0.30));
        graphicsContext.fillOval(x - 38, y + 38, 76, 38);
        graphicsContext.setFill(Color.web("#FFD36E"));
        graphicsContext.fillOval(x - 7, y + 40, 14, 10);
    }

    private void drawPalletCluster(GraphicsContext graphicsContext, double x, double y) {
        drawWoodPallet(graphicsContext, x, y + 38, 132, 30);
        drawOrangeCarton(graphicsContext, x + 12, y, 42, 40);
        drawOrangeCarton(graphicsContext, x + 54, y + 12, 40, 28);
        drawWrappedPallet(graphicsContext, x + 94, y - 4, 34, 44);
    }

    private void drawWoodPallet(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        graphicsContext.setFill(Color.web("#6E3B1E"));
        graphicsContext.fillRect(x, y, width, height);
        graphicsContext.setStroke(Color.web("#B46D35"));
        graphicsContext.setLineWidth(3);
        for (double stripX = x + 8; stripX < x + width; stripX += 24) {
            graphicsContext.strokeLine(stripX, y + 4, stripX, y + height - 4);
        }
    }

    private void drawBarrelStack(GraphicsContext graphicsContext, double x, double y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 2; column++) {
                drawBarrel(graphicsContext, x + column * 24, y + row * 25, 24, 24);
            }
        }
    }

    private void drawWarehouseDoor(GraphicsContext graphicsContext, double x, double y) {
        double passageWidth = 118;
        double passageHeight = 54;
        double passageX = x - 14;
        double passageY = y + 40;

        graphicsContext.setFill(Color.web("#050505"));
        graphicsContext.fillRect(passageX - 10, passageY + passageHeight - 2, passageWidth + 20, 16);

        graphicsContext.setFill(Color.web("#595348"));
        graphicsContext.fillRoundRect(passageX, passageY, passageWidth, passageHeight, 6, 6);

        graphicsContext.setStroke(Color.web("#625B50"));
        graphicsContext.setLineWidth(1);
        for (double tileX = passageX + 18; tileX < passageX + passageWidth; tileX += 32) {
            graphicsContext.strokeLine(tileX, passageY + 2, tileX, passageY + passageHeight - 2);
        }
        for (double tileY = passageY + 16; tileY < passageY + passageHeight; tileY += 32) {
            graphicsContext.strokeLine(passageX + 2, tileY, passageX + passageWidth - 2, tileY);
        }

        graphicsContext.setStroke(Color.web("#E58A30"));
        graphicsContext.setLineWidth(3);
        graphicsContext.strokeLine(passageX, passageY, passageX, passageY + passageHeight - 4);
        graphicsContext.strokeLine(passageX + passageWidth, passageY, passageX + passageWidth, passageY + passageHeight - 4);
        graphicsContext.strokeLine(passageX, passageY + passageHeight - 4, passageX + passageWidth, passageY + passageHeight - 4);

        graphicsContext.setFill(Color.color(0.95, 0.58, 0.13, 0.18));
        graphicsContext.fillRect(passageX + 8, passageY + 8, passageWidth - 16, 12);
        graphicsContext.setStroke(Color.color(0.95, 0.58, 0.13, 0.48));
        graphicsContext.setLineWidth(2);
        for (double stripeX = passageX + 10; stripeX < passageX + passageWidth - 18; stripeX += 18) {
            graphicsContext.strokeLine(stripeX, passageY + 20, stripeX + 14, passageY + 8);
        }
    }

    private void drawWarehouseBlueStacks(GraphicsContext graphicsContext, double x, double y, int columns, int rows) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                drawBlueCrate(graphicsContext, x + column * 20, y + row * 17, 18, 14);
            }
        }
    }

    private void drawBlueCrate(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        graphicsContext.setFill(Color.web("#0F5FB8"));
        graphicsContext.fillRect(x, y, width, height);
        graphicsContext.setStroke(Color.web("#07366C"));
        graphicsContext.setLineWidth(2);
        graphicsContext.strokeRect(x, y, width, height);
        graphicsContext.setStroke(Color.web("#2A88E8"));
        graphicsContext.setLineWidth(1);
        graphicsContext.strokeLine(x + 3, y + height / 2.0, x + width - 3, y + height / 2.0);
    }

    private void drawWarehouseProp(GraphicsContext graphicsContext, GymObject gymObject) {
        if (pickupRackImage != null) {
            graphicsContext.setFill(Color.color(0.02, 0.02, 0.02, 0.28));
            graphicsContext.fillOval(
                    gymObject.left() + gymObject.width() * 0.08,
                    gymObject.bottom() - 8,
                    gymObject.width() * 0.84,
                    16
            );
            graphicsContext.drawImage(
                    pickupRackImage,
                    gymObject.left(),
                    gymObject.top(),
                    gymObject.width(),
                    gymObject.height()
            );
            return;
        }

        graphicsContext.setFill(Color.color(0.02, 0.02, 0.02, 0.32));
        graphicsContext.fillRect(gymObject.left() + 8, gymObject.bottom() - 4, gymObject.width(), 12);

        graphicsContext.setFill(Color.web("#2F3740"));
        graphicsContext.fillRect(gymObject.left(), gymObject.top(), 10, gymObject.height());
        graphicsContext.fillRect(gymObject.right() - 10, gymObject.top(), 10, gymObject.height());
        graphicsContext.fillRect(gymObject.centerX() - 5, gymObject.top(), 10, gymObject.height());

        graphicsContext.setFill(Color.web("#5A321B"));
        for (double shelfY = gymObject.top() + 4; shelfY < gymObject.bottom(); shelfY += 56) {
            graphicsContext.fillRect(gymObject.left() + 8, shelfY, gymObject.width() - 16, 8);
        }

        graphicsContext.setStroke(Color.web("#1B2930"));
        graphicsContext.setLineWidth(5);
        for (double braceTop = gymObject.top() + 8; braceTop < gymObject.bottom() - 42; braceTop += 56) {
            double braceBottom = Math.min(braceTop + 52, gymObject.bottom() - 8);
            graphicsContext.strokeLine(gymObject.left() + 10, braceTop, gymObject.right() - 10, braceBottom);
            graphicsContext.strokeLine(gymObject.right() - 10, braceTop, gymObject.left() + 10, braceBottom);
        }
        graphicsContext.setStroke(Color.web("#43535C"));
        graphicsContext.setLineWidth(2);
        for (double braceTop = gymObject.top() + 8; braceTop < gymObject.bottom() - 42; braceTop += 56) {
            double braceBottom = Math.min(braceTop + 52, gymObject.bottom() - 8);
            graphicsContext.strokeLine(gymObject.left() + 11, braceTop, gymObject.right() - 11, braceBottom);
            graphicsContext.strokeLine(gymObject.right() - 11, braceTop, gymObject.left() + 11, braceBottom);
        }

        graphicsContext.setFill(Color.web("#17232A"));
        for (double holeY = gymObject.top() + 16; holeY < gymObject.bottom() - 12; holeY += 18) {
            graphicsContext.fillRect(gymObject.left() + 3, holeY, 4, 5);
            graphicsContext.fillRect(gymObject.right() - 7, holeY, 4, 5);
        }

        for (double y = gymObject.top() + 14; y < gymObject.bottom() - 20; y += 50) {
            int row = (int) Math.round((y - gymObject.top()) / 50.0);
            for (double x = gymObject.left() + 18; x < gymObject.right() - 28; x += 32) {
                int column = (int) Math.round((x - gymObject.left()) / 28.0);
                int variant = Math.floorMod(row + column, 4);
                if (variant == 0) {
                    drawOrangeCarton(graphicsContext, x, y - 2, 28, 28);
                } else if (variant == 1) {
                    drawWrappedPallet(graphicsContext, x, y + 2, 28, 24);
                } else if (variant == 2) {
                    drawWoodCrate(graphicsContext, x - 1, y + 2, 30, 24);
                } else {
                    drawBarrel(graphicsContext, x + 4, y + 1, 22, 24);
                }
            }
        }
    }

    private void drawOrangeCarton(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        graphicsContext.setFill(Color.web("#B76B2E"));
        graphicsContext.fillRect(x, y, width, height);
        graphicsContext.setFill(Color.web("#D89A55"));
        graphicsContext.fillRect(x + 3, y + 3, width - 6, Math.max(4, height * 0.22));
        graphicsContext.setStroke(Color.web("#F2C080"));
        graphicsContext.setLineWidth(2);
        graphicsContext.strokeLine(x + width / 2.0, y + 3, x + width / 2.0, y + height - 3);
        graphicsContext.setStroke(Color.web("#6B341C"));
        graphicsContext.setLineWidth(2);
        graphicsContext.strokeRect(x, y, width, height);
    }

    private void drawWrappedPallet(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        graphicsContext.setFill(Color.web("#6EA7D8"));
        graphicsContext.fillRect(x, y, width, height);
        graphicsContext.setFill(Color.web("#B7D7EA"));
        graphicsContext.fillRect(x + 3, y + 3, width - 6, 4);
        graphicsContext.fillRect(x + 3, y + 11, width - 6, 3);
        graphicsContext.setStroke(Color.web("#24517A"));
        graphicsContext.setLineWidth(2);
        graphicsContext.strokeRect(x, y, width, height);
    }

    private void drawBarrel(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        graphicsContext.setFill(Color.web("#8A4B24"));
        graphicsContext.fillOval(x, y, width, height);
        graphicsContext.setFill(Color.web("#B56D35"));
        graphicsContext.fillOval(x + 3, y + 3, width - 6, height - 6);
        graphicsContext.setStroke(Color.web("#4D2616"));
        graphicsContext.setLineWidth(2);
        graphicsContext.strokeOval(x, y, width, height);
        graphicsContext.strokeLine(x + 3, y + height / 2.0, x + width - 3, y + height / 2.0);
    }

    private void drawWoodCrate(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        graphicsContext.setFill(Color.web("#7A421F"));
        graphicsContext.fillRect(x, y, width, height);
        graphicsContext.setStroke(Color.web("#B46D35"));
        graphicsContext.setLineWidth(2);
        graphicsContext.strokeRect(x, y, width, height);
        graphicsContext.strokeLine(x + 4, y + 4, x + width - 4, y + height - 4);
        graphicsContext.strokeLine(x + width - 4, y + 4, x + 4, y + height - 4);
    }

    private void drawWorkShiftFloor(GraphicsContext graphicsContext, WorkShiftState state) {
        String shiftLabel = state.workerDressed() ? "ФИНИШ" : "СМЕНА";
        drawWorkZone(graphicsContext, WorkShiftState.SHIFT_ZONE, shiftLabel, Color.web("#38BDF8"));
        drawWorkZone(graphicsContext, WorkShiftState.PICKUP_ZONE, "ПРИЕМКА", Color.web("#B7791F"));
        drawWorkZone(graphicsContext, WorkShiftState.DROP_ZONE, "ОТГРУЗКА", Color.web("#22C55E"));
    }

    private void drawWorkRacks(GraphicsContext graphicsContext) {
        drawWorkRack(graphicsContext, WorkShiftState.PICKUP_ZONE.left() - 74, WorkShiftState.PICKUP_ZONE.top() - 108, 70, 190);
        drawWorkRack(graphicsContext, WorkShiftState.DROP_ZONE.right() + 8, WorkShiftState.DROP_ZONE.top() - 34, 74, 184);
    }

    private void drawWorkRack(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        if (pickupRackImage != null) {
            graphicsContext.drawImage(pickupRackImage, x, y, width, height);
            return;
        }

        graphicsContext.setFill(Color.web("#263A42"));
        graphicsContext.fillRect(x + 8, y, width - 16, height);
        graphicsContext.setStroke(Color.web("#50636B"));
        graphicsContext.setLineWidth(4);
        graphicsContext.strokeRect(x + 8, y, width - 16, height);
        drawOrangeCarton(graphicsContext, x + 28, y + 22, 28, 24);
        drawOrangeCarton(graphicsContext, x + 58, y + 22, 28, 24);
        drawWrappedPallet(graphicsContext, x + 30, y + 78, 46, 30);
        drawOrangeCarton(graphicsContext, x + 48, y + 130, 38, 30);
    }

    private void drawPickupBoxBin(GraphicsContext graphicsContext, int boxesLeft) {
        CollisionRect zone = WorkShiftState.PICKUP_ZONE;
        double x = zone.left() + 20;
        double y = zone.top() + 40;
        double width = zone.width() - 40;
        double height = zone.height() - 52;

        graphicsContext.setFill(Color.web("#5A321B"));
        graphicsContext.fillRect(x, y, width, height);
        graphicsContext.setFill(Color.web("#8B5429"));
        graphicsContext.fillRect(x + 5, y + 6, width - 10, height - 11);
        graphicsContext.setStroke(Color.web("#2B170D"));
        graphicsContext.setLineWidth(3);
        graphicsContext.strokeRect(x, y, width, height);

        int visibleBoxes = Math.min(12, Math.max(0, boxesLeft));
        for (int index = 0; index < visibleBoxes; index++) {
            double boxX = x + 10 + (index % 4) * 23;
            double boxY = y + 7 + (index / 4) * 11 + (index % 2) * 2;
            drawTinyCarton(graphicsContext, boxX, boxY, 20, 14);
        }
    }

    private void drawTinyCarton(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        graphicsContext.setFill(Color.web("#B76B2E"));
        graphicsContext.fillRect(x, y, width, height);
        graphicsContext.setFill(Color.web("#D89A55"));
        graphicsContext.fillRect(x + 2, y + 2, width - 4, 4);
        graphicsContext.setStroke(Color.web("#6B341C"));
        graphicsContext.setLineWidth(1.5);
        graphicsContext.strokeRect(x, y, width, height);
    }

    private void drawWorkZone(GraphicsContext graphicsContext, CollisionRect zone, String label, Color color) {
        graphicsContext.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.25));
        graphicsContext.fillRect(zone.x(), zone.y(), zone.width(), zone.height());
        graphicsContext.setStroke(color);
        graphicsContext.setLineWidth(3);
        graphicsContext.strokeRect(zone.x(), zone.y(), zone.width(), zone.height());

        graphicsContext.setFill(Color.web("#F8FAFC"));
        graphicsContext.setTextAlign(TextAlignment.CENTER);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        graphicsContext.fillText(label, zone.x() + zone.width() / 2.0, zone.y() + 24);
    }

    private void drawCarriedWorkBox(GraphicsContext graphicsContext,
                                    Player player,
                                    WorkShiftState state,
                                    boolean renderedCarrySprite) {
        if (!state.carryingBox() || renderedCarrySprite) {
            return;
        }

        double boxSize = 42;
        double drawX = player.centerX() - boxSize / 2.0;
        double drawY = player.position().y();

        switch (player.direction()) {
            case BACK -> drawY = player.position().y() - boxSize * 0.55;
            case FRONT -> drawY = player.position().y() + player.height() * 0.06;
            case LEFT -> {
                drawX = player.position().x() - boxSize * 0.68;
                drawY = player.position().y() - player.height() * 0.08;
            }
            case RIGHT -> {
                drawX = player.position().x() + player.width() - boxSize * 0.32;
                drawY = player.position().y() - player.height() * 0.08;
            }
        }

        drawWorkBox(graphicsContext, drawX, drawY, boxSize, boxSize);
    }

    private void drawWorkBox(GraphicsContext graphicsContext, double x, double y, double width, double height) {
        if (workBoxImage != null) {
            graphicsContext.drawImage(workBoxImage, x, y, width, height);
            return;
        }

        graphicsContext.setFill(Color.web("#0B0F14"));
        graphicsContext.fillRect(x, y, width, height);
        graphicsContext.setFill(Color.web("#2C1A13"));
        graphicsContext.fillRect(x + 4, y + 4, width - 8, height - 8);
        graphicsContext.setFill(Color.web("#D89458"));
        graphicsContext.fillRect(x + 8, y + 8, width - 16, height / 4.0);
        graphicsContext.setStroke(Color.web("#F5C48D"));
        graphicsContext.setLineWidth(3);
        graphicsContext.strokeLine(x + width * 0.36, y + 8, x + width * 0.72, y + height - 8);
    }

    private void drawWorkShiftHud(GraphicsContext graphicsContext, GameMap gameMap, WorkShiftState state) {
        graphicsContext.setFill(Color.color(0.02, 0.04, 0.07, 0.82));
        graphicsContext.fillRoundRect(gameMap.left() + 18, gameMap.bottom() - 58, 360, 42, 10, 10);
        graphicsContext.setFill(HIGHLIGHT);
        graphicsContext.setTextAlign(TextAlignment.LEFT);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        graphicsContext.fillText(
                "Работа: " + state.deliveredBoxes() + "/" + WorkShiftState.TARGET_BOXES
                        + " коробок | награда " + WorkShiftState.REWARD_MONEY,
                gameMap.left() + 34,
                gameMap.bottom() - 32
        );
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

    private boolean isWarehouseMap(GameMap gameMap) {
        return "Работа".equals(gameMap.name());
    }

    private boolean isGymMap(GameMap gameMap) {
        return "Зал".equals(gameMap.name());
    }

    private Image backgroundImageFor(GameMap gameMap) {
        if (!gameMap.hasBackgroundImage()) {
            return null;
        }
        return mapBackgrounds.computeIfAbsent(gameMap.backgroundImagePath(), this::loadImage);
    }

    private List<String> wrapText(String text, Font font, double maxWidth) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            return List.of();
        }

        String[] words = trimmed.split("\\s+");
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            String candidate = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (currentLine.length() == 0 || measureTextWidth(candidate, font) <= maxWidth) {
                currentLine.setLength(0);
                currentLine.append(candidate);
                continue;
            }

            lines.add(currentLine.toString());
            currentLine.setLength(0);
            currentLine.append(word);
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    private double measureTextWidth(String text, Font font) {
        Text helper = new Text(text);
        helper.setFont(font);
        return helper.getLayoutBounds().getWidth();
    }

    private void fillTriangle(GraphicsContext graphicsContext,
                              double x1,
                              double y1,
                              double x2,
                              double y2,
                              double x3,
                              double y3) {
        graphicsContext.fillPolygon(
                new double[]{x1, x2, x3},
                new double[]{y1, y2, y3},
                3
        );
    }

    private void strokeTriangle(GraphicsContext graphicsContext,
                                double x1,
                                double y1,
                                double x2,
                                double y2,
                                double x3,
                                double y3) {
        graphicsContext.strokePolygon(
                new double[]{x1, x2, x3},
                new double[]{y1, y2, y3},
                3
        );
    }

    private double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private Image loadImage(String resourcePath) {
        InputStream inputStream = GameRenderer.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            return null;
        }
        return new Image(inputStream);
    }

    public boolean toggleDebugCollisions() {
        debugCollisions = !debugCollisions;
        return debugCollisions;
    }

    private void drawDebugCollisions(GraphicsContext graphicsContext, GameMap gameMap, Player player) {
        if (!debugCollisions) {
            return;
        }

        graphicsContext.save();

        graphicsContext.setStroke(Color.color(0.25, 0.75, 1.0, 0.95));
        graphicsContext.setLineWidth(2);
        graphicsContext.strokeRect(
                gameMap.walkLeft(),
                gameMap.walkTop(),
                gameMap.walkRight() - gameMap.walkLeft(),
                gameMap.walkBottom() - gameMap.walkTop()
        );

        graphicsContext.setFill(Color.color(1.0, 0.2, 0.2, 0.28));
        graphicsContext.setStroke(Color.color(1.0, 0.25, 0.25, 0.9));
        for (CollisionRect collisionRect : gameMap.collisionAreas()) {
            graphicsContext.fillRect(collisionRect.x(), collisionRect.y(), collisionRect.width(), collisionRect.height());
            graphicsContext.strokeRect(collisionRect.x(), collisionRect.y(), collisionRect.width(), collisionRect.height());
        }

        CollisionRect playerHitbox = player.footHitbox();
        graphicsContext.setFill(Color.color(0.15, 0.85, 0.35, 0.35));
        graphicsContext.setStroke(Color.color(0.25, 1.0, 0.45, 0.95));
        graphicsContext.fillRect(playerHitbox.x(), playerHitbox.y(), playerHitbox.width(), playerHitbox.height());
        graphicsContext.strokeRect(playerHitbox.x(), playerHitbox.y(), playerHitbox.width(), playerHitbox.height());

        graphicsContext.restore();
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
        double panelHeight = 330;
        double panelLeft = (canvasWidth - panelWidth) / 2.0;
        double panelTop = (canvasHeight - panelHeight) / 2.0;
        double barLeft = panelLeft + 44;
        double barTop = panelTop + 205;
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
        graphicsContext.fillText("Нажмите показанную клавишу, когда маркер в зелёной зоне. Ошибки считаются, но не обрывают игру.", panelLeft + 36, panelTop + 128);

        graphicsContext.setFill(HIGHLIGHT);
        graphicsContext.setFont(Font.font("Consolas", FontWeight.BLACK, 44));
        graphicsContext.fillText("Клавиша: " + session.expectedTimingKey().getName(), panelLeft + 36, panelTop + 174);

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
        graphicsContext.fillText(footerText(session), panelLeft + 36, panelTop + 288);
        graphicsContext.restore();
    }

    private void drawResultGradeAnimation(GraphicsContext graphicsContext,
                                          TrainingGrade grade,
                                          double panelLeft,
                                          double panelTop,
                                          double panelWidth,
                                          double panelHeight,
                                          double pulse) {
        Color color = gradeColor(grade);
        double centerX = panelLeft + panelWidth / 2.0;
        double centerY = panelTop + panelHeight / 2.0;
        graphicsContext.save();
        switch (grade) {
            case EXCELLENT -> {
                graphicsContext.setStroke(Color.web("#67E8F9", 0.42 + pulse * 0.24));
                graphicsContext.setLineWidth(4);
                for (int index = 0; index < 4; index++) {
                    double radius = 230 + index * 26 + pulse * 18;
                    graphicsContext.strokeOval(centerX - radius / 2, centerY - radius / 2, radius, radius);
                }
                graphicsContext.setFill(Color.web("#F8D66D", 0.72));
                for (int index = 0; index < 10; index++) {
                    double angle = index * Math.PI * 0.2 + pulse;
                    double x = centerX + Math.cos(angle) * (290 + pulse * 20);
                    double y = centerY + Math.sin(angle) * (170 + pulse * 12);
                    graphicsContext.fillOval(x - 4, y - 4, 8, 8);
                }
            }
            case GOOD -> {
                graphicsContext.setStroke(Color.web("#22C55E", 0.34 + pulse * 0.20));
                graphicsContext.setLineWidth(5);
                graphicsContext.strokeRoundRect(panelLeft - 18 - pulse * 8, panelTop - 18 - pulse * 8,
                        panelWidth + 36 + pulse * 16, panelHeight + 36 + pulse * 16, 34, 34);
            }
            case NORMAL -> {
                graphicsContext.setFill(Color.web("#F8D66D", 0.18 + pulse * 0.16));
                graphicsContext.fillRoundRect(panelLeft - 20, panelTop + panelHeight - 34,
                        panelWidth + 40, 20 + pulse * 8, 20, 20);
            }
            case WEAK -> {
                graphicsContext.setFill(Color.web("#FB923C", 0.18 + pulse * 0.18));
                for (int index = 0; index < 5; index++) {
                    graphicsContext.fillOval(panelLeft + 80 + index * 118, panelTop - 20 + pulse * 10,
                            18 + pulse * 8, 18 + pulse * 8);
                }
            }
            case FAIL -> {
                graphicsContext.setStroke(Color.web("#F87171", 0.34 + pulse * 0.22));
                graphicsContext.setLineWidth(5);
                graphicsContext.strokeLine(panelLeft - 28, panelTop + 18, panelLeft + panelWidth + 28, panelTop + panelHeight - 18);
                graphicsContext.strokeLine(panelLeft + 24, panelTop + panelHeight + 22, panelLeft + panelWidth - 24, panelTop - 22);
            }
        }
        graphicsContext.setFill(color.deriveColor(0, 1, 1, 0.12 + pulse * 0.10));
        graphicsContext.fillRoundRect(panelLeft - 10, panelTop - 10, panelWidth + 20, panelHeight + 20, 32, 32);
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
        double panelHeight = 370;
        double panelLeft = (canvasWidth - panelWidth) / 2.0;
        double panelTop = (canvasHeight - panelHeight) / 2.0;
        Color gradeColor = gradeColor(result.grade());
        double pulse = 0.72 + Math.sin(System.nanoTime() / 180_000_000.0) * 0.18;

        graphicsContext.save();
        graphicsContext.setFill(OVERLAY_BACKDROP);
        graphicsContext.fillRect(0, 0, canvasWidth, canvasHeight);
        drawResultGradeAnimation(graphicsContext, result.grade(), panelLeft, panelTop, panelWidth, panelHeight, pulse);
        graphicsContext.setFill(OVERLAY_PANEL);
        graphicsContext.fillRoundRect(panelLeft, panelTop, panelWidth, panelHeight, 24, 24);
        graphicsContext.setStroke(gradeColor);
        graphicsContext.setLineWidth(3);
        graphicsContext.strokeRoundRect(panelLeft, panelTop, panelWidth, panelHeight, 24, 24);

        graphicsContext.setTextAlign(TextAlignment.LEFT);
        graphicsContext.setFill(gradeColor);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.BLACK, 30));
        graphicsContext.fillText("Результат: " + result.grade().label(), panelLeft + 36, panelTop + 56);

        graphicsContext.setFill(SUBTITLE_COLOR);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        graphicsContext.fillText("Статистика уже применена к персонажу.", panelLeft + 36, panelTop + 86);

        graphicsContext.setFill(LABEL_COLOR);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 17));
        graphicsContext.fillText(result.message(), panelLeft + 36, panelTop + 130, panelWidth - 72);

        graphicsContext.setStroke(BAR_COLOR);
        graphicsContext.setLineWidth(2);
        graphicsContext.strokeLine(panelLeft + 36, panelTop + 172, panelLeft + panelWidth - 36, panelTop + 172);

        drawResultStats(graphicsContext, result, panelLeft + 36, panelTop + 196, panelWidth - 72, pulse);

        graphicsContext.setFill(SUBTITLE_COLOR);
        graphicsContext.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        graphicsContext.fillText("Space или Esc закрывает окно и возвращает в зал.", panelLeft + 36, panelTop + 330);
        graphicsContext.restore();
    }

    private void drawResultStats(GraphicsContext graphicsContext,
                                 SkillCheckResult result,
                                 double left,
                                 double top,
                                 double width,
                                 double pulse) {
        String[] stats = buildResultStats(result);
        double chipWidth = width / 3.0 - 10;
        double chipHeight = 34;

        for (int index = 0; index < stats.length; index++) {
            double x = left + (index % 3) * (chipWidth + 15);
            double y = top + (index / 3) * (chipHeight + 10);
            graphicsContext.setGlobalAlpha(index == 0 ? 1.0 : pulse);
            graphicsContext.setFill(Color.web("#1E293B"));
            graphicsContext.fillRoundRect(x, y, chipWidth, chipHeight, 14, 14);
            graphicsContext.setGlobalAlpha(1.0);
            graphicsContext.setFill(HIGHLIGHT);
            graphicsContext.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            graphicsContext.fillText(stats[index], x + 12, y + 22, chipWidth - 24);
        }
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
            if (session.machine().machineType() == MachineType.TREADMILL) {
                return "Попадания: " + session.completedHits()
                        + "/" + session.requiredHits()
                        + " | Попытки: " + session.timingAttempts()
                        + "/" + session.maxAttempts()
                        + " | Ошибки: " + session.missedAttempts();
            }

            return "Повторы: " + session.completedHits() + "/" + session.requiredHits() + ". Зона сужается после каждого успеха.";
        }

        return "Одно точное попадание засчитывает подход.";
    }

    private String footerText(SkillCheckSession session) {
        if (session.requiresMultipleHits()) {
            if (session.machine().machineType() == MachineType.TREADMILL) {
                return "Беговая оценивается по количеству попаданий. Промах не обрывает игру, но портит итоговую оценку.";
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
        appendDelta(builder, "деньги", result.moneyDelta());

        if (builder.isEmpty()) {
            return "без изменений";
        }

        return builder.toString();
    }

    private String[] buildResultStats(SkillCheckResult result) {
        return new String[]{
                formatDelta("Сила", result.strengthDelta()),
                formatDelta("Масса", result.muscleDelta()),
                formatDelta("Выносл.", result.staminaDelta()),
                formatDelta("Устал.", result.fatigueDelta()),
                formatDelta("Деньги", result.moneyDelta()),
                formatFatDelta("Жир", result.bodyFatDelta())
        };
    }

    private String formatDelta(String label, int delta) {
        if (delta == 0) {
            return label + " 0";
        }
        return label + " " + (delta > 0 ? "+" : "") + delta;
    }

    private String formatFatDelta(String label, double delta) {
        if (Math.abs(delta) < 0.05) {
            return label + " 0%";
        }
        String sign = delta > 0 ? "+" : "";
        return label + " " + sign + String.format(java.util.Locale.US, "%.1f", delta) + "%";
    }

    private Color gradeColor(TrainingGrade grade) {
        return switch (grade) {
            case EXCELLENT -> Color.web("#67E8F9");
            case GOOD -> SUCCESS_ZONE;
            case NORMAL -> HIGHLIGHT;
            case WEAK -> Color.web("#FB923C");
            case FAIL -> Color.web("#F87171");
        };
    }

    private record PlayerSpriteSet(Map<PlayerDirection, Image> idleFrames,
                                   Map<PlayerDirection, Image> walkFrames) {
    }

    private record DirectionalSpriteSet(Map<PlayerDirection, Image> frames,
                                        Map<PlayerDirection, Image> stepFrames) {
    }

    private record FrameBounds(int minX, int minY, int maxX, int maxY) {
        private boolean isEmpty() {
            return maxX < minX || maxY < minY;
        }

        private int width() {
            return maxX - minX + 1;
        }

        private int height() {
            return maxY - minY + 1;
        }
    }
}
