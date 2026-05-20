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
import java.util.List;
import java.util.Random;

public final class PosingMinigame {

    // Tune competition duration here.
    private static final long DEFAULT_DURATION_MS = 15_000L;
    // Tune which keys can appear in the posing skill-check here.
    private static final List<SkillCheckButton> BUTTON_POOL = SkillCheckButton.all();
    // Tune stage anchor and overall character size here.
    private static final double STAGE_CHARACTER_ANCHOR_X_RATIO = 0.50;
    private static final double STAGE_CHARACTER_ANCHOR_Y_RATIO = 0.715;
    private static final double CHARACTER_SCALE = 0.36;
    private static final double CHARACTER_MAX_WIDTH_RATIO = 0.28;
    private static final double CHARACTER_MAX_HEIGHT_RATIO = 0.60;

    private static final double PROGRESS_GAIN = 6.25;
    private static final double PROGRESS_LOSS = 3.50;
    private static final int POSE_CHANGE_EVERY = 3;
    private static final double TARGET_SUCCESSFUL_PRESSES = 18.0;
    private static final double TARGET_MAX_COMBO = 10.0;
    private static final double TARGET_PROGRESS_TO_PASS = 60.0;
    private static final int DEBUG_BOX_PADDING = 4;

    private final PoseAssetLoader assetLoader;
    private final Random random;
    private final BufferedImage background;

    private CharacterPoseAssets currentCharacterAssets;
    private PoseForm currentForm;
    private int currentPoseIndex;
    private int successfulPresses;
    private int failedPresses;
    private int combo;
    private int maxCombo;
    private double progress;
    private SkillCheckButton currentTargetButton;
    private long elapsedMs;
    private long durationMs;
    private boolean finished;
    private boolean aborted;
    private PerformanceResult result;

    private int stageCharacterAnchorX;
    private int stageCharacterAnchorY;
    private double characterScale;

    private BufferedImage frameBuffer;
    private WritableImage fxFrame;

    public PosingMinigame(PoseAssetLoader assetLoader) {
        this.assetLoader = assetLoader;
        this.random = new Random();
        this.background = assetLoader.loadBackground("sceneBackground.png");
        this.durationMs = DEFAULT_DURATION_MS;
        this.characterScale = CHARACTER_SCALE;
    }

    public void start(Player player, double screenWidth, double screenHeight) {
        this.currentCharacterAssets = assetLoader.loadFor(player.profile());
        this.currentForm = player.currentForm() == PlayerForm.FOURTH_STEROIDS ? PoseForm.STEROID : PoseForm.NATURAL;
        this.currentPoseIndex = 0;
        this.successfulPresses = 0;
        this.failedPresses = 0;
        this.combo = 0;
        this.maxCombo = 0;
        this.progress = 0.0;
        this.elapsedMs = 0L;
        this.finished = false;
        this.aborted = false;
        this.result = null;
        this.currentTargetButton = randomButton(null);
        configureStageAnchor(screenWidth, screenHeight);
    }

    public void update(double deltaSeconds) {
        if (finished) {
            return;
        }

        elapsedMs += Math.max(0L, Math.round(deltaSeconds * 1000.0));
        if (elapsedMs >= durationMs) {
            finish(false);
        }
    }

    public boolean handleKeyPressed(KeyCode keyCode) {
        if (finished) {
            return false;
        }

        if (keyCode == KeyCode.ESCAPE) {
            finish(true);
            return true;
        }

        SkillCheckButton expected = currentTargetButton;
        if (expected == null) {
            return false;
        }

        SkillCheckButton pressed = BUTTON_POOL.stream()
                .filter(button -> button.keyCode() == keyCode)
                .findFirst()
                .orElse(null);

        if (pressed == null) {
            return false;
        }

        if (pressed == expected) {
            successfulPresses++;
            combo++;
            maxCombo = Math.max(maxCombo, combo);
            progress = Math.min(100.0, progress + PROGRESS_GAIN);
            if (successfulPresses % POSE_CHANGE_EVERY == 0) {
                currentPoseIndex = (currentPoseIndex + 1) % 3;
            }
            currentTargetButton = randomButton(expected);
            return true;
        }

        failedPresses++;
        combo = 0;
        progress = Math.max(0.0, progress - PROGRESS_LOSS);
        return true;
    }

    public boolean isFinished() {
        return finished;
    }

    public PerformanceResult getResult() {
        if (result == null) {
            result = buildResult();
        }
        return result;
    }

    public void render(GraphicsContext graphicsContext, double width, double height, boolean debugCollisions) {
        int frameWidth = Math.max(1, (int) Math.round(width));
        int frameHeight = Math.max(1, (int) Math.round(height));
        ensureFrameBuffer(frameWidth, frameHeight);
        configureStageAnchor(width, height);

        Graphics2D graphics2D = frameBuffer.createGraphics();
        try {
            configureGraphics(graphics2D);
            graphics2D.setColor(Color.BLACK);
            graphics2D.fillRect(0, 0, frameWidth, frameHeight);

            drawCoverBackground(graphics2D, background, frameWidth, frameHeight);
            drawCharacterCenteredOnStage(graphics2D, currentPoseImage());
            drawHud(graphics2D, frameWidth, frameHeight);
            if (debugCollisions) {
                drawDebug(graphics2D, currentPoseImage(), frameWidth, frameHeight);
            }
        } finally {
            graphics2D.dispose();
        }

        fxFrame = SwingFXUtils.toFXImage(frameBuffer, fxFrame);
        graphicsContext.drawImage(fxFrame, 0, 0, width, height);
    }

    public SkillCheckButton currentTargetButton() {
        return currentTargetButton;
    }

    public double progress() {
        return progress;
    }

    public long remainingMs() {
        return Math.max(0L, durationMs - elapsedMs);
    }

    private void finish(boolean abortedByPlayer) {
        this.aborted = abortedByPlayer;
        this.finished = true;
        this.result = buildResult();
    }

    private PerformanceResult buildResult() {
        double totalAttempts = Math.max(1.0, successfulPresses + failedPresses);
        double accuracy = successfulPresses / totalAttempts;
        // Adjust competition formula here if you want a different scoring balance.
        double techniqueScore = accuracy * 10.0;
        double charismaScore = Math.min(10.0, (maxCombo / TARGET_MAX_COMBO) * 10.0);
        double powerScore = Math.min(10.0, (successfulPresses / TARGET_SUCCESSFUL_PRESSES) * 10.0);
        double totalScore = (techniqueScore + charismaScore + powerScore) / 3.0;
        boolean passed = progress >= TARGET_PROGRESS_TO_PASS && !aborted;
        return new PerformanceResult(
                techniqueScore,
                charismaScore,
                powerScore,
                totalScore,
                successfulPresses,
                failedPresses,
                maxCombo,
                progress,
                passed
        );
    }

    private BufferedImage currentPoseImage() {
        return currentCharacterAssets.forForm(currentForm).pose(currentPoseIndex);
    }

    private SkillCheckButton randomButton(SkillCheckButton previous) {
        SkillCheckButton next = previous;
        while (next == previous) {
            next = BUTTON_POOL.get(random.nextInt(BUTTON_POOL.size()));
        }
        return next;
    }

    private void drawHud(Graphics2D graphics2D, int width, int height) {
        int panelWidth = 420;
        int panelHeight = 208;
        int panelX = width - panelWidth - 28;
        int panelY = 28;

        graphics2D.setColor(new Color(5, 10, 18, 216));
        graphics2D.fill(new RoundRectangle2D.Double(panelX, panelY, panelWidth, panelHeight, 28, 28));
        graphics2D.setColor(new Color(248, 190, 84));
        graphics2D.setStroke(new BasicStroke(3f));
        graphics2D.draw(new RoundRectangle2D.Double(panelX, panelY, panelWidth, panelHeight, 28, 28));

        graphics2D.setColor(new Color(248, 250, 252));
        graphics2D.setFont(new Font("Segoe UI", Font.BOLD, 28));
        graphics2D.drawString("Позирование", panelX + 24, panelY + 40);

        graphics2D.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        graphics2D.setColor(new Color(220, 226, 235));
        graphics2D.drawString("Кнопка: " + currentTargetButton.label(), panelX + 24, panelY + 78);
        graphics2D.drawString("Комбо: " + combo + "   Макс: " + maxCombo, panelX + 24, panelY + 108);
        graphics2D.drawString("Успехи: " + successfulPresses + "   Ошибки: " + failedPresses, panelX + 24, panelY + 138);
        graphics2D.drawString("Время: " + formatSeconds(remainingMs()), panelX + 24, panelY + 168);

        int barX = panelX + 24;
        int barY = panelY + 180;
        int barWidth = panelWidth - 48;
        int barHeight = 16;
        graphics2D.setColor(new Color(31, 41, 55, 220));
        graphics2D.fillRoundRect(barX, barY, barWidth, barHeight, 16, 16);
        graphics2D.setPaint(new GradientPaint(
                barX,
                barY,
                new Color(34, 197, 94),
                barX + barWidth,
                barY,
                new Color(250, 204, 21)
        ));
        graphics2D.fillRoundRect(barX, barY, (int) Math.round(barWidth * (progress / 100.0)), barHeight, 16, 16);
        graphics2D.setColor(new Color(248, 250, 252));
        graphics2D.setFont(new Font("Segoe UI", Font.BOLD, 16));
        graphics2D.drawString("Прогресс: " + Math.round(progress) + "%", barX, barY - 6);

        int promptBoxX = 28;
        int promptBoxY = height - 120;
        int promptBoxWidth = 360;
        int promptBoxHeight = 84;
        graphics2D.setColor(new Color(5, 10, 18, 210));
        graphics2D.fillRoundRect(promptBoxX, promptBoxY, promptBoxWidth, promptBoxHeight, 24, 24);
        graphics2D.setColor(new Color(120, 196, 255));
        graphics2D.drawRoundRect(promptBoxX, promptBoxY, promptBoxWidth, promptBoxHeight, 24, 24);
        graphics2D.setColor(new Color(248, 250, 252));
        graphics2D.setFont(new Font("Segoe UI", Font.BOLD, 22));
        graphics2D.drawString("Жми: " + currentTargetButton.label(), promptBoxX + 22, promptBoxY + 34);
        graphics2D.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        graphics2D.drawString("Правильные попадания качают прогресс и меняют позу.", promptBoxX + 22, promptBoxY + 58);
        graphics2D.drawString("Esc - закончить досрочно и перейти к оценкам.", promptBoxX + 22, promptBoxY + 80);
    }

    private void drawCharacterCenteredOnStage(Graphics2D graphics2D, BufferedImage poseImage) {
        double resolvedScale = resolveCharacterScale(poseImage, frameBuffer.getWidth(), frameBuffer.getHeight());
        int drawWidth = (int) Math.round(poseImage.getWidth() * resolvedScale);
        int drawHeight = (int) Math.round(poseImage.getHeight() * resolvedScale);
        int drawX = stageCharacterAnchorX - drawWidth / 2;
        int drawY = stageCharacterAnchorY - drawHeight;

        graphics2D.setComposite(AlphaComposite.SrcOver.derive(0.32f));
        graphics2D.setColor(new Color(0, 0, 0, 180));
        graphics2D.fillOval(drawX + drawWidth / 4, stageCharacterAnchorY - 16, drawWidth / 2, 30);
        graphics2D.setComposite(AlphaComposite.SrcOver);
        graphics2D.drawImage(poseImage, drawX, drawY, drawWidth, drawHeight, null);
    }

    private void drawDebug(Graphics2D graphics2D, BufferedImage poseImage, int width, int height) {
        double resolvedScale = resolveCharacterScale(poseImage, width, height);
        int drawWidth = (int) Math.round(poseImage.getWidth() * resolvedScale);
        int drawHeight = (int) Math.round(poseImage.getHeight() * resolvedScale);
        int drawX = stageCharacterAnchorX - drawWidth / 2;
        int drawY = stageCharacterAnchorY - drawHeight;

        graphics2D.setColor(new Color(34, 197, 94, 120));
        graphics2D.fillRect(drawX, drawY, drawWidth, drawHeight);
        graphics2D.setColor(new Color(34, 197, 94));
        graphics2D.drawRect(drawX, drawY, drawWidth, drawHeight);

        graphics2D.setColor(new Color(96, 165, 250, 180));
        graphics2D.fillOval(stageCharacterAnchorX - DEBUG_BOX_PADDING, stageCharacterAnchorY - DEBUG_BOX_PADDING, DEBUG_BOX_PADDING * 2, DEBUG_BOX_PADDING * 2);
        graphics2D.setFont(new Font("Consolas", Font.PLAIN, 14));
        graphics2D.drawString("anchorX=" + stageCharacterAnchorX + " anchorY=" + stageCharacterAnchorY, 18, height - 18);
    }

    private void configureStageAnchor(double screenWidth, double screenHeight) {
        stageCharacterAnchorX = (int) Math.round(screenWidth * STAGE_CHARACTER_ANCHOR_X_RATIO);
        stageCharacterAnchorY = (int) Math.round(screenHeight * STAGE_CHARACTER_ANCHOR_Y_RATIO);
    }

    private double resolveCharacterScale(BufferedImage poseImage, int screenWidth, int screenHeight) {
        double widthScale = (screenWidth * CHARACTER_MAX_WIDTH_RATIO) / poseImage.getWidth();
        double heightScale = (screenHeight * CHARACTER_MAX_HEIGHT_RATIO) / poseImage.getHeight();
        return Math.min(characterScale, Math.min(widthScale, heightScale));
    }

    private void ensureFrameBuffer(int width, int height) {
        if (frameBuffer != null && frameBuffer.getWidth() == width && frameBuffer.getHeight() == height) {
            return;
        }
        frameBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        fxFrame = null;
    }

    private void configureGraphics(Graphics2D graphics2D) {
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private void drawCoverBackground(Graphics2D graphics2D, BufferedImage image, int width, int height) {
        double scale = Math.max(width / (double) image.getWidth(), height / (double) image.getHeight());
        int drawWidth = (int) Math.round(image.getWidth() * scale);
        int drawHeight = (int) Math.round(image.getHeight() * scale);
        int drawX = (width - drawWidth) / 2;
        int drawY = (height - drawHeight) / 2;
        graphics2D.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
    }

    private String formatSeconds(long remainingMs) {
        return String.format("%.1f сек", remainingMs / 1000.0);
    }
}
