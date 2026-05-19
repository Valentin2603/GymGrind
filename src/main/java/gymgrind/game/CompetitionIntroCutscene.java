package gymgrind.game;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CompetitionIntroCutscene {

    // All editable cutscene content lives here: image paths, names, descriptions,
    // timings, step order and the screen anchors for portraits/text.
    private static final String BACKGROUND_PATH = "/assets/competition_intro/background.png";
    private static final double ANNOUNCEMENT_DURATION = 2.6;
    private static final double JUDGE_REVEAL_DURATION = 3.8;
    private static final double FINALE_DURATION = 2.5;
    private static final double TEXT_BOX_X_RATIO = 0.075;
    private static final double TEXT_BOX_Y_RATIO = 0.72;
    private static final double TEXT_BOX_WIDTH_RATIO = 0.85;
    private static final double TEXT_BOX_HEIGHT_RATIO = 0.21;
    private static final double PORTRAIT_WIDTH_RATIO = 0.42;
    private static final double PORTRAIT_HEIGHT_RATIO = 0.80;
    private static final double LEFT_PORTRAIT_CENTER_X_RATIO = 0.29;
    private static final double RIGHT_PORTRAIT_CENTER_X_RATIO = 0.71;
    private static final double PORTRAIT_CENTER_Y_RATIO = 0.44;
    private static final double ENTRY_SLIDE_DISTANCE_RATIO = 0.18;
    private static final double BOB_DISTANCE_PIXELS = 8.0;
    private static final double BACKDROP_DARKEN_ALPHA = 0.54;
    private static final double REACTION_BLEND_START = 0.72;

    private static final JudgeCard JUDGE_ONE = new JudgeCard(
            "judge_1",
            "Арсен Грей",
            "Судья по эстетике и пропорциям",
            "Бывший абсолютный чемпион. Следит за балансом массы, чистотой линий и общей сценической формой.",
            "/assets/competition_intro/judge_1_normal.png",
            "/assets/competition_intro/judge_1_shocked.png",
            EntrySide.LEFT,
            new Color(244, 190, 92)
    );

    private static final JudgeCard JUDGE_TWO = new JudgeCard(
            "judge_2",
            "Люциан Кроун",
            "Главный судья и самый жёсткий эксперт",
            "Оценивает сухость, глубину сепарации и то, насколько уверенно атлет держит сцену под давлением.",
            "/assets/competition_intro/judge_2_normal.png",
            "/assets/competition_intro/judge_2_shocked.png",
            EntrySide.CENTER,
            new Color(204, 168, 96)
    );

    private static final JudgeCard JUDGE_THREE = new JudgeCard(
            "judge_3",
            "Кай Орин",
            "Судья по подаче и харизме",
            "Смотрит на позинг, контроль корпуса и тот самый вау-эффект, из-за которого зал замирает.",
            "/assets/competition_intro/judge_3_normal.png",
            "/assets/competition_intro/judge_3_shocked.png",
            EntrySide.RIGHT,
            new Color(255, 146, 72)
    );

    private static final List<CutsceneStep> STEPS = List.of(
            CutsceneStep.announcement(
                    "IRON LEGENDS",
                    "Лучшие атлеты добрались до сцены. Судьи занимают места, зал затихает, свет ложится на помост.",
                    ANNOUNCEMENT_DURATION
            ),
            CutsceneStep.judgeReveal(
                    JUDGE_ONE.key(),
                    JUDGE_ONE.name(),
                    JUDGE_ONE.subtitle() + ". " + JUDGE_ONE.description(),
                    JUDGE_REVEAL_DURATION
            ),
            CutsceneStep.judgeReveal(
                    JUDGE_TWO.key(),
                    JUDGE_TWO.name(),
                    JUDGE_TWO.subtitle() + ". " + JUDGE_TWO.description(),
                    JUDGE_REVEAL_DURATION
            ),
            CutsceneStep.judgeReveal(
                    JUDGE_THREE.key(),
                    JUDGE_THREE.name(),
                    JUDGE_THREE.subtitle() + ". " + JUDGE_THREE.description(),
                    JUDGE_REVEAL_DURATION
            ),
            CutsceneStep.finale(
                    "Соревнования начинаются!",
                    "Судьи представлены. Прожекторы наведены на сцену. Пора решать, кто сегодня станет легендой.",
                    FINALE_DURATION
            )
    );

    private final Map<String, JudgeAsset> judgeAssets;
    private final BufferedImage backgroundImage;
    private BufferedImage frameBuffer;
    private WritableImage fxFrame;
    private int stepIndex;
    private double stepElapsedSeconds;
    private double totalElapsedSeconds;
    private boolean finished;

    public CompetitionIntroCutscene() {
        this.backgroundImage = loadBufferedImage(BACKGROUND_PATH);
        this.judgeAssets = loadJudgeAssets();
        this.stepIndex = 0;
        this.stepElapsedSeconds = 0.0;
        this.totalElapsedSeconds = 0.0;
        this.finished = false;
    }

    public void update(double deltaSeconds) {
        if (finished) {
            return;
        }

        totalElapsedSeconds += deltaSeconds;
        stepElapsedSeconds += deltaSeconds;

        while (!finished && stepElapsedSeconds >= currentStep().durationSeconds()) {
            stepElapsedSeconds -= currentStep().durationSeconds();
            stepIndex++;
            if (stepIndex >= STEPS.size()) {
                finished = true;
                stepIndex = STEPS.size() - 1;
                stepElapsedSeconds = STEPS.getLast().durationSeconds();
            }
        }
    }

    public void skip() {
        finished = true;
        stepIndex = STEPS.size() - 1;
        stepElapsedSeconds = STEPS.getLast().durationSeconds();
    }

    public boolean isFinished() {
        return finished;
    }

    public int stepCount() {
        return STEPS.size();
    }

    public double totalDurationSeconds() {
        return STEPS.stream().mapToDouble(CutsceneStep::durationSeconds).sum();
    }

    public void render(GraphicsContext graphicsContext, double width, double height) {
        int frameWidth = Math.max(1, (int) Math.round(width));
        int frameHeight = Math.max(1, (int) Math.round(height));
        ensureFrameBuffer(frameWidth, frameHeight);

        Graphics2D graphics2D = frameBuffer.createGraphics();
        try {
            configureGraphics(graphics2D);
            graphics2D.setColor(Color.BLACK);
            graphics2D.fillRect(0, 0, frameWidth, frameHeight);

            drawCoverBackground(graphics2D, backgroundImage, frameWidth, frameHeight);

            CutsceneStep step = currentStep();
            if (step.type() == CutsceneStep.Type.JUDGE_REVEAL) {
                graphics2D.setColor(new Color(0, 0, 0, (int) Math.round(BACKDROP_DARKEN_ALPHA * 255)));
                graphics2D.fillRect(0, 0, frameWidth, frameHeight);
                drawJudgeReveal(graphics2D, step, frameWidth, frameHeight);
            } else {
                drawAnnouncementBackdrop(graphics2D, frameWidth, frameHeight, step.type() == CutsceneStep.Type.FINALE);
            }

            drawTextBox(graphics2D, step, frameWidth, frameHeight);
            drawSkipHint(graphics2D, frameWidth, frameHeight);
        } finally {
            graphics2D.dispose();
        }

        fxFrame = SwingFXUtils.toFXImage(frameBuffer, fxFrame);
        graphicsContext.drawImage(fxFrame, 0, 0, width, height);
    }

    private CutsceneStep currentStep() {
        return STEPS.get(Math.min(stepIndex, STEPS.size() - 1));
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

    private void drawAnnouncementBackdrop(Graphics2D graphics2D, int width, int height, boolean finale) {
        float alpha = finale ? 0.66f : 0.58f;
        graphics2D.setPaint(new GradientPaint(
                0,
                0,
                new Color(5, 10, 18, Math.round(alpha * 255)),
                0,
                height,
                new Color(6, 14, 24, Math.round((alpha + 0.12f) * 255))
        ));
        graphics2D.fillRect(0, 0, width, height);
    }

    private void drawJudgeReveal(Graphics2D graphics2D, CutsceneStep step, int width, int height) {
        JudgeAsset judgeAsset = judgeAssets.get(step.judgeKey());
        if (judgeAsset == null) {
            return;
        }

        double progress = animationProgress(step);
        double alpha = computeAlpha(progress);
        double bobOffset = Math.sin(totalElapsedSeconds * 3.25) * BOB_DISTANCE_PIXELS;
        double portraitHeight = height * PORTRAIT_HEIGHT_RATIO;
        double portraitWidth = width * PORTRAIT_WIDTH_RATIO;
        double centerX = switch (judgeAsset.card().side()) {
            case LEFT -> width * LEFT_PORTRAIT_CENTER_X_RATIO;
            case CENTER -> width * 0.50;
            case RIGHT -> width * RIGHT_PORTRAIT_CENTER_X_RATIO;
        };
        double centerY = height * PORTRAIT_CENTER_Y_RATIO + bobOffset;
        double targetX = centerX - portraitWidth / 2.0;
        double targetY = centerY - portraitHeight / 2.0;

        double entryDistance = width * ENTRY_SLIDE_DISTANCE_RATIO;
        double startX = switch (judgeAsset.card().side()) {
            case LEFT -> targetX - entryDistance;
            case CENTER -> targetX;
            case RIGHT -> targetX + entryDistance;
        };
        double slideProgress = easeOutCubic(clamp(progress / 0.28));
        double drawX = lerp(startX, targetX, slideProgress);

        drawPortraitShadow(graphics2D, drawX, targetY, portraitWidth, portraitHeight, alpha);
        drawJudgePortrait(graphics2D, judgeAsset, drawX, targetY, portraitWidth, portraitHeight, alpha, progress);
    }

    private void drawPortraitShadow(Graphics2D graphics2D,
                                    double x,
                                    double y,
                                    double width,
                                    double height,
                                    double alpha) {
        graphics2D.setComposite(AlphaComposite.SrcOver.derive((float) (alpha * 0.42)));
        graphics2D.setColor(new Color(0, 0, 0, 180));
        graphics2D.fill(new RoundRectangle2D.Double(x + 18, y + 24, width - 36, height - 48, 32, 32));
        graphics2D.setComposite(AlphaComposite.SrcOver);
    }

    private void drawJudgePortrait(Graphics2D graphics2D,
                                   JudgeAsset judgeAsset,
                                   double x,
                                   double y,
                                   double width,
                                   double height,
                                   double alpha,
                                   double progress) {
        BufferedImage normalImage = judgeAsset.normalImage();
        BufferedImage shockedImage = judgeAsset.shockedImage();
        double reactionBlend = clamp((progress - REACTION_BLEND_START) / (1.0 - REACTION_BLEND_START));

        graphics2D.setComposite(AlphaComposite.SrcOver.derive((float) alpha));
        graphics2D.drawImage(normalImage, (int) Math.round(x), (int) Math.round(y), (int) Math.round(width), (int) Math.round(height), null);

        if (shockedImage != null && reactionBlend > 0.0) {
            graphics2D.setComposite(AlphaComposite.SrcOver.derive((float) (alpha * reactionBlend)));
            graphics2D.drawImage(shockedImage, (int) Math.round(x), (int) Math.round(y), (int) Math.round(width), (int) Math.round(height), null);
        }

        graphics2D.setComposite(AlphaComposite.SrcOver);
    }

    private void drawTextBox(Graphics2D graphics2D, CutsceneStep step, int width, int height) {
        double progress = animationProgress(step);
        double alpha = Math.max(0.88, computeAlpha(progress));
        int boxX = (int) Math.round(width * TEXT_BOX_X_RATIO);
        int boxY = (int) Math.round(height * TEXT_BOX_Y_RATIO);
        int boxWidth = (int) Math.round(width * TEXT_BOX_WIDTH_RATIO);
        int boxHeight = (int) Math.round(height * TEXT_BOX_HEIGHT_RATIO);

        Color accent = accentFor(step);
        graphics2D.setComposite(AlphaComposite.SrcOver.derive((float) alpha));
        graphics2D.setColor(new Color(5, 10, 18, 214));
        graphics2D.fill(new RoundRectangle2D.Double(boxX, boxY, boxWidth, boxHeight, 26, 26));

        graphics2D.setStroke(new BasicStroke(3f));
        graphics2D.setColor(accent);
        graphics2D.draw(new RoundRectangle2D.Double(boxX, boxY, boxWidth, boxHeight, 26, 26));

        graphics2D.setPaint(new GradientPaint(
                boxX,
                boxY,
                accent,
                boxX + boxWidth,
                boxY,
                new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 20)
        ));
        graphics2D.fillRect(boxX + 28, boxY + 58, boxWidth - 56, 3);

        graphics2D.setColor(new Color(248, 250, 252));
        graphics2D.setFont(new Font("Segoe UI", Font.BOLD, 32));
        graphics2D.drawString(step.title(), boxX + 28, boxY + 44);

        graphics2D.setColor(new Color(226, 232, 240));
        graphics2D.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        drawWrappedText(
                graphics2D,
                step.description(),
                boxX + 28,
                boxY + 88,
                boxWidth - 56,
                24
        );

        if (step.type() == CutsceneStep.Type.JUDGE_REVEAL) {
            JudgeAsset judgeAsset = judgeAssets.get(step.judgeKey());
            if (judgeAsset != null) {
                graphics2D.setColor(accent);
                graphics2D.setFont(new Font("Segoe UI", Font.BOLD, 15));
                graphics2D.drawString(judgeAsset.card().subtitle().toUpperCase(), boxX + 28, boxY + boxHeight - 22);
            }
        }

        graphics2D.setComposite(AlphaComposite.SrcOver);
    }

    private void drawSkipHint(Graphics2D graphics2D, int width, int height) {
        String text = "Enter или Space — пропустить вступление";
        graphics2D.setColor(new Color(6, 10, 14, 175));
        graphics2D.fillRoundRect(width - 356, 24, 324, 34, 16, 16);
        graphics2D.setColor(new Color(230, 235, 240));
        graphics2D.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        graphics2D.drawString(text, width - 338, 47);
    }

    private Color accentFor(CutsceneStep step) {
        if (!step.hasJudge()) {
            return step.type() == CutsceneStep.Type.FINALE
                    ? new Color(244, 171, 62)
                    : new Color(120, 196, 255);
        }

        JudgeAsset judgeAsset = judgeAssets.get(step.judgeKey());
        return judgeAsset == null ? new Color(244, 171, 62) : judgeAsset.card().accentColor();
    }

    private double animationProgress(CutsceneStep step) {
        return clamp(stepElapsedSeconds / Math.max(0.001, step.durationSeconds()));
    }

    private double computeAlpha(double progress) {
        double fadeIn = clamp(progress / 0.18);
        double fadeOut = clamp((1.0 - progress) / 0.18);
        return Math.min(fadeIn, fadeOut);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double easeOutCubic(double progress) {
        double inverse = 1.0 - progress;
        return 1.0 - inverse * inverse * inverse;
    }

    private double lerp(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    private void drawWrappedText(Graphics2D graphics2D,
                                 String text,
                                 int x,
                                 int y,
                                 int maxWidth,
                                 int lineHeight) {
        List<String> lines = wrapText(graphics2D, text, maxWidth);
        int lineY = y;
        for (String line : lines) {
            graphics2D.drawString(line, x, lineY);
            lineY += lineHeight;
        }
    }

    private List<String> wrapText(Graphics2D graphics2D, String text, int maxWidth) {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (graphics2D.getFontMetrics().stringWidth(candidate) <= maxWidth) {
                current.setLength(0);
                current.append(candidate);
                continue;
            }

            if (!current.isEmpty()) {
                lines.add(current.toString());
            }
            current.setLength(0);
            current.append(word);
        }

        if (!current.isEmpty()) {
            lines.add(current.toString());
        }

        return lines;
    }

    private void drawCoverBackground(Graphics2D graphics2D, BufferedImage image, int width, int height) {
        double scale = Math.max(width / (double) image.getWidth(), height / (double) image.getHeight());
        int drawWidth = (int) Math.round(image.getWidth() * scale);
        int drawHeight = (int) Math.round(image.getHeight() * scale);
        int drawX = (width - drawWidth) / 2;
        int drawY = (height - drawHeight) / 2;
        graphics2D.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
    }

    private Map<String, JudgeAsset> loadJudgeAssets() {
        Map<String, JudgeAsset> assets = new HashMap<>();
        registerJudge(assets, JUDGE_ONE);
        registerJudge(assets, JUDGE_TWO);
        registerJudge(assets, JUDGE_THREE);
        return assets;
    }

    private void registerJudge(Map<String, JudgeAsset> assets, JudgeCard judgeCard) {
        assets.put(
                judgeCard.key(),
                new JudgeAsset(
                        judgeCard,
                        loadBufferedImage(judgeCard.normalImagePath()),
                        loadBufferedImage(judgeCard.shockedImagePath())
                )
        );
    }

    private BufferedImage loadBufferedImage(String resourcePath) {
        try (InputStream inputStream = CompetitionIntroCutscene.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing cutscene resource: " + resourcePath);
            }
            return ImageIO.read(inputStream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load cutscene resource: " + resourcePath, exception);
        }
    }

    private record JudgeCard(
            String key,
            String name,
            String subtitle,
            String description,
            String normalImagePath,
            String shockedImagePath,
            EntrySide side,
            Color accentColor
    ) {
        private JudgeCard {
            Objects.requireNonNull(key);
            Objects.requireNonNull(name);
            Objects.requireNonNull(subtitle);
            Objects.requireNonNull(description);
            Objects.requireNonNull(normalImagePath);
            Objects.requireNonNull(shockedImagePath);
            Objects.requireNonNull(side);
            Objects.requireNonNull(accentColor);
        }
    }

    private record JudgeAsset(JudgeCard card, BufferedImage normalImage, BufferedImage shockedImage) {
    }

    private enum EntrySide {
        LEFT,
        CENTER,
        RIGHT
    }
}
