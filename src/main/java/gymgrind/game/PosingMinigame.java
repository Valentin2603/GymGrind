package gymgrind.game;

import gymgrind.player.Player;
import gymgrind.player.PlayerForm;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class PosingMinigame {

    private static final long ROUND_DURATION_MS = 5_000L;
    private static final int TOTAL_ROUNDS = 6;

    private static final List<SkillCheckButton> BUTTON_POOL = SkillCheckButton.all();

    private static final double STAGE_CHARACTER_ANCHOR_X_RATIO = 0.50;
    private static final double STAGE_CHARACTER_ANCHOR_Y_RATIO = 0.715;

    private static final double CHARACTER_SCALE = 0.36;
    private static final double CHARACTER_MAX_WIDTH_RATIO = 0.28;
    private static final double CHARACTER_MAX_HEIGHT_RATIO = 0.60;

    // Meter tuning
    private static final double METER_DECAY_PER_SECOND = 22.0;
    private static final double MIN_GAIN = 1.5;

    // Zones
    private static final double GOOD_ZONE_START = 45.0;
    private static final double PERFECT_ZONE_START = 70.0;
    private static final double PERFECT_ZONE_END = 84.0;

    private final PoseAssetLoader assetLoader;
    private final Random random;

    private final BufferedImage background;

    private CharacterPoseAssets currentCharacterAssets;
    private PoseForm currentForm;

    private int currentPoseIndex;

    private SkillCheckButton currentTargetButton;

    private int currentRound;
    private long roundElapsedMs;

    private boolean finished;

    private double poseMeter;

    private double timeInGoodZone;
    private double timeInPerfectZone;
    private double timeInOverspamZone;
    private double roundTimeInGoodZone;
    private double roundTimeInPerfectZone;
    private double roundTimeInOverspamZone;

    private int successfulPresses;
    private int failedPresses;

    private int combo;
    private int maxCombo;

    private final List<Double> roundScores;

    private PerformanceResult result;

    private int stageCharacterAnchorX;
    private int stageCharacterAnchorY;

    private BufferedImage frameBuffer;
    private WritableImage fxFrame;

    public PosingMinigame(PoseAssetLoader assetLoader) {
        this.assetLoader = assetLoader;
        this.random = new Random();
        this.background = assetLoader.loadBackground("sceneBackground.png");
        this.roundScores = new ArrayList<>();
    }

    public void start(Player player, double screenWidth, double screenHeight) {

        this.currentCharacterAssets = assetLoader.loadFor(player.profile());

        this.currentForm = player.currentForm() == PlayerForm.FOURTH_STEROIDS
                ? PoseForm.STEROID
                : PoseForm.NATURAL;

        this.currentPoseIndex = 0;

        this.currentRound = 1;
        this.roundElapsedMs = 0L;

        this.finished = false;

        this.poseMeter = 0.0;

        this.timeInGoodZone = 0.0;
        this.timeInPerfectZone = 0.0;
        this.timeInOverspamZone = 0.0;
        resetRoundZoneTimes();

        this.successfulPresses = 0;
        this.failedPresses = 0;

        this.combo = 0;
        this.maxCombo = 0;

        this.roundScores.clear();

        this.result = null;

        this.currentTargetButton = randomButton(null);

        configureStageAnchor(screenWidth, screenHeight);
    }

    public void update(double deltaSeconds) {

        if (finished) {
            return;
        }

        roundElapsedMs += Math.round(deltaSeconds * 1000.0);

        updateMeter(deltaSeconds);

        trackZoneTimes(deltaSeconds);

        if (roundElapsedMs >= ROUND_DURATION_MS) {
            finishRound();
        }
    }

    private void updateMeter(double deltaSeconds) {

        poseMeter -= METER_DECAY_PER_SECOND * deltaSeconds;

        poseMeter = Math.max(0.0, Math.min(100.0, poseMeter));
    }

    private void trackZoneTimes(double deltaSeconds) {

        if (poseMeter >= PERFECT_ZONE_START && poseMeter <= PERFECT_ZONE_END) {
            timeInPerfectZone += deltaSeconds;
            roundTimeInPerfectZone += deltaSeconds;
        } else if (poseMeter >= GOOD_ZONE_START && poseMeter < PERFECT_ZONE_START) {
            timeInGoodZone += deltaSeconds;
            roundTimeInGoodZone += deltaSeconds;
        } else if (poseMeter > PERFECT_ZONE_END) {
            timeInOverspamZone += deltaSeconds;
            roundTimeInOverspamZone += deltaSeconds;
        }
    }

    public boolean handleKeyPressed(KeyCode keyCode) {

        if (finished) {
            return false;
        }

        if (keyCode == KeyCode.ESCAPE) {
            finishCompetition();
            return true;
        }

        SkillCheckButton pressedButton = BUTTON_POOL.stream()
                .filter(button -> button.keyCode() == keyCode)
                .findFirst()
                .orElse(null);

        if (pressedButton == null) {
            return false;
        }

        if (pressedButton == currentTargetButton) {

            successfulPresses++;

            combo++;
            maxCombo = Math.max(maxCombo, combo);

            double gain = 7.0 - (poseMeter * 0.045);

            poseMeter += Math.max(MIN_GAIN, gain);

            poseMeter = Math.min(100.0, poseMeter);

            return true;
        }

        failedPresses++;

        combo = 0;

        poseMeter -= 6.0;

        poseMeter = Math.max(0.0, poseMeter);

        return true;
    }

    private void finishRound() {

        double roundScore =
                (roundTimeInPerfectZone * 2.2)
                        + (roundTimeInGoodZone * 1.0)
                        - (roundTimeInOverspamZone * 1.5);

        roundScore = Math.max(0.0, Math.min(10.0, roundScore));

        roundScores.add(roundScore);

        currentPoseIndex = (currentPoseIndex + 1) % 3;

        currentTargetButton = randomButton(currentTargetButton);

        if (currentRound >= TOTAL_ROUNDS) {
            finishCompetition();
            return;
        }

        currentRound++;

        roundElapsedMs = 0L;

        poseMeter = 0.0;
        resetRoundZoneTimes();
    }

    private void finishCompetition() {

        finished = true;

        result = buildResult();
    }

    private PerformanceResult buildResult() {

        double averageRoundScore = roundScores.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double completedRatio = roundScores.size() / (double) TOTAL_ROUNDS;
        int totalPresses = successfulPresses + failedPresses;
        double accuracyScore = totalPresses == 0
                ? 0.0
                : (successfulPresses / (double) totalPresses) * 10.0;
        double comboScore = Math.min(10.0, maxCombo * 0.42);
        double controlPenalty = Math.min(2.0, timeInOverspamZone * 0.10);
        double baseScore = (averageRoundScore * 0.55)
                + (accuracyScore * 0.25)
                + (comboScore * 0.20);
        baseScore = (baseScore - controlPenalty) * (0.65 + completedRatio * 0.35);

        double technique = balancedJudgeScore(
                baseScore,
                (averageRoundScore - 5.0) * 0.10
        );
        double stagePresence = balancedJudgeScore(
                baseScore,
                (comboScore - 5.0) * 0.11
        );
        double muscleControl = balancedJudgeScore(
                baseScore,
                (accuracyScore - 5.0) * 0.10 - controlPenalty * 0.20
        );

        double total =
                (technique + stagePresence + muscleControl) / 3.0;

        return new PerformanceResult(
                technique,
                stagePresence,
                muscleControl,
                total,

                successfulPresses,
                failedPresses,
                maxCombo,

                averageRoundScore * 10.0,

                total >= 5.0
        );
    }

    private double balancedJudgeScore(double baseScore, double modifier) {
        return Math.max(0.0, Math.min(9.2, baseScore + modifier));
    }

    private void resetRoundZoneTimes() {
        roundTimeInGoodZone = 0.0;
        roundTimeInPerfectZone = 0.0;
        roundTimeInOverspamZone = 0.0;
    }

    public void render(GraphicsContext graphicsContext,
                       double width,
                       double height,
                       boolean debugCollisions) {

        int frameWidth = Math.max(1, (int) Math.round(width));
        int frameHeight = Math.max(1, (int) Math.round(height));

        ensureFrameBuffer(frameWidth, frameHeight);

        configureStageAnchor(width, height);

        Graphics2D g = frameBuffer.createGraphics();

        try {

            configureGraphics(g);

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, frameWidth, frameHeight);

            drawCoverBackground(g, background, frameWidth, frameHeight);

            drawCharacter(g, currentPoseImage());

            drawHud(g, frameWidth, frameHeight);

        } finally {
            g.dispose();
        }

        fxFrame = SwingFXUtils.toFXImage(frameBuffer, fxFrame);

        graphicsContext.drawImage(fxFrame, 0, 0, width, height);
    }

    private void drawHud(Graphics2D g, int width, int height) {

        int panelX = width - 390;
        int panelY = 30;

        int panelWidth = 340;
        int panelHeight = 240;

        g.setColor(new Color(5, 10, 18, 220));

        g.fillRoundRect(
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                28,
                28
        );

        g.setColor(new Color(244, 171, 62));

        g.setStroke(new BasicStroke(3f));

        g.drawRoundRect(
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                28,
                28
        );

        g.setColor(Color.WHITE);

        g.setFont(new Font("Segoe UI", Font.BOLD, 28));

        g.drawString("Позирование", panelX + 20, panelY + 38);

        g.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        g.drawString(
                "Раунд: " + currentRound + "/" + TOTAL_ROUNDS,
                panelX + 20,
                panelY + 72
        );

        g.drawString(
                "Кнопка: " + currentTargetButton.label(),
                panelX + 20,
                panelY + 102
        );

        g.drawString(
                "Комбо: " + combo,
                panelX + 20,
                panelY + 132
        );

        g.drawString(
                "Время: " + remainingSeconds(),
                panelX + 20,
                panelY + 162
        );

        drawVerticalMeter(g, panelX + 250, panelY + 30);
    }

    private void drawVerticalMeter(Graphics2D g, int x, int y) {

        int meterWidth = 44;
        int meterHeight = 170;

        g.setColor(new Color(25, 25, 30));

        g.fillRoundRect(x, y, meterWidth, meterHeight, 16, 16);

        drawZone(g, x, y, meterWidth, meterHeight,
                0, 45,
                new Color(100, 100, 100));

        drawZone(g, x, y, meterWidth, meterHeight,
                45, 70,
                new Color(60, 180, 90));

        drawZone(g, x, y, meterWidth, meterHeight,
                70, 84,
                new Color(30, 255, 120));

        drawZone(g, x, y, meterWidth, meterHeight,
                84, 100,
                new Color(220, 50, 50));

        int markerY =
                y + meterHeight
                        - (int) ((poseMeter / 100.0) * meterHeight);

        g.setColor(Color.WHITE);

        g.fillRoundRect(
                x - 6,
                markerY - 4,
                meterWidth + 12,
                8,
                8,
                8
        );
    }

    private void drawZone(Graphics2D g,
                          int x,
                          int y,
                          int width,
                          int height,
                          double start,
                          double end,
                          Color color) {

        int startY =
                y + height
                        - (int) ((end / 100.0) * height);

        int zoneHeight =
                (int) (((end - start) / 100.0) * height);

        g.setColor(color);

        g.fillRect(
                x,
                startY,
                width,
                zoneHeight
        );
    }

    private void drawCharacter(Graphics2D g,
                               BufferedImage poseImage) {

        double scale = resolveCharacterScale(
                poseImage,
                frameBuffer.getWidth(),
                frameBuffer.getHeight()
        );

        int drawWidth =
                (int) Math.round(poseImage.getWidth() * scale);

        int drawHeight =
                (int) Math.round(poseImage.getHeight() * scale);

        int drawX =
                stageCharacterAnchorX - drawWidth / 2;

        int drawY =
                stageCharacterAnchorY - drawHeight;

        g.setComposite(
                AlphaComposite.SrcOver.derive(0.30f)
        );

        g.setColor(new Color(0, 0, 0, 180));

        g.fillOval(
                drawX + drawWidth / 4,
                stageCharacterAnchorY - 16,
                drawWidth / 2,
                30
        );

        g.setComposite(AlphaComposite.SrcOver);

        g.drawImage(
                poseImage,
                drawX,
                drawY,
                drawWidth,
                drawHeight,
                null
        );
    }

    private BufferedImage currentPoseImage() {

        return currentCharacterAssets
                .forForm(currentForm)
                .pose(currentPoseIndex);
    }

    private SkillCheckButton randomButton(SkillCheckButton previous) {

        SkillCheckButton next = previous;

        while (next == previous) {
            next = BUTTON_POOL.get(
                    random.nextInt(BUTTON_POOL.size())
            );
        }

        return next;
    }

    private String remainingSeconds() {

        long remaining =
                Math.max(0L, ROUND_DURATION_MS - roundElapsedMs);

        return String.format("%.1f", remaining / 1000.0);
    }

    public boolean isFinished() {
        return finished;
    }

    public PerformanceResult getResult() {
        return result;
    }

    SkillCheckButton currentTargetButton() {
        return currentTargetButton;
    }

    private void configureStageAnchor(double screenWidth,
                                      double screenHeight) {

        stageCharacterAnchorX =
                (int) Math.round(
                        screenWidth * STAGE_CHARACTER_ANCHOR_X_RATIO
                );

        stageCharacterAnchorY =
                (int) Math.round(
                        screenHeight * STAGE_CHARACTER_ANCHOR_Y_RATIO
                );
    }

    private double resolveCharacterScale(BufferedImage poseImage,
                                         int screenWidth,
                                         int screenHeight) {

        double widthScale =
                (screenWidth * CHARACTER_MAX_WIDTH_RATIO)
                        / poseImage.getWidth();

        double heightScale =
                (screenHeight * CHARACTER_MAX_HEIGHT_RATIO)
                        / poseImage.getHeight();

        return Math.min(
                CHARACTER_SCALE,
                Math.min(widthScale, heightScale)
        );
    }

    private void ensureFrameBuffer(int width, int height) {

        if (frameBuffer != null
                && frameBuffer.getWidth() == width
                && frameBuffer.getHeight() == height) {
            return;
        }

        frameBuffer = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB
        );

        fxFrame = null;
    }

    private void configureGraphics(Graphics2D g) {

        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );

        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
    }

    private void drawCoverBackground(Graphics2D graphics2D,
                                     BufferedImage image,
                                     int width,
                                     int height) {

        double scale = Math.max(
                width / (double) image.getWidth(),
                height / (double) image.getHeight()
        );

        int drawWidth =
                (int) Math.round(image.getWidth() * scale);

        int drawHeight =
                (int) Math.round(image.getHeight() * scale);

        int drawX = (width - drawWidth) / 2;
        int drawY = (height - drawHeight) / 2;

        graphics2D.drawImage(
                image,
                drawX,
                drawY,
                drawWidth,
                drawHeight,
                null
        );
    }
}
