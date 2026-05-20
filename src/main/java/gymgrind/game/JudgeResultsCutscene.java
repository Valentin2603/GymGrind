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
import java.util.List;

public final class JudgeResultsCutscene {

    private static final String BACKGROUND_PATH = "/assets/competition_intro/background.png";
    private static final double INTRO_DURATION = 1.6;
    private static final List<JudgeScoreCard> JUDGES = List.of(
            new JudgeScoreCard("Роман", "/assets/competition_intro/judge_1_normal.png", "/assets/competition_intro/judge_1_shocked.png", new Color(244, 190, 92)),
            new JudgeScoreCard("Валентин", "/assets/competition_intro/judge_2_normal.png", "/assets/competition_intro/judge_2_shocked.png", new Color(204, 168, 96)),
            new JudgeScoreCard("Никита", "/assets/competition_intro/judge_3_normal.png", "/assets/competition_intro/judge_3_shocked.png", new Color(255, 146, 72))
    );

    private final BufferedImage background;
    private final List<RenderedJudgeScore> renderedScores;
    private final PerformanceResult result;

    private BufferedImage frameBuffer;
    private WritableImage fxFrame;
    private int stepIndex;
    private double stepElapsedSeconds;
    private boolean finished;

    public JudgeResultsCutscene(PerformanceResult result) {
        this.result = result;
        this.background = loadBufferedImage(BACKGROUND_PATH);
        this.renderedScores = List.of(
                new RenderedJudgeScore(JUDGES.get(0), result.techniqueScore(), result.techniqueScore() >= 7.5 ? "Идеальная линия." : "Есть над чем шлифовать."),
                new RenderedJudgeScore(JUDGES.get(1), result.powerScore(), result.powerScore() >= 7.5 ? "Сила сцены чувствуется." : "Напор пока слабоват."),
                new RenderedJudgeScore(JUDGES.get(2), result.charismaScore(), result.charismaScore() >= 7.5 ? "Зал тебя запомнил." : "Харизму ещё надо раскрыть.")
        );
    }

    public void update(double deltaSeconds) {
        if (finished) {
            return;
        }
        if (stepIndex == 0) {
            stepElapsedSeconds += deltaSeconds;
        }
    }

    public void advance() {
        if (finished) {
            return;
        }
        if (stepIndex == 0 && stepElapsedSeconds < INTRO_DURATION) {
            return;
        }
        stepIndex++;
        stepElapsedSeconds = 0.0;
        if (stepIndex > renderedScores.size()) {
            finished = true;
        }
    }

    public void skip() {
        finished = true;
    }

    public boolean isFinished() {
        return finished;
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
            drawCoverBackground(graphics2D, background, frameWidth, frameHeight);
            graphics2D.setColor(new Color(0, 0, 0, 155));
            graphics2D.fillRect(0, 0, frameWidth, frameHeight);

            if (stepIndex == 0) {
                drawIntroOverlay(graphics2D, frameWidth, frameHeight);
            } else if (stepIndex <= renderedScores.size()) {
                drawJudgeScoreOverlay(graphics2D, renderedScores.get(stepIndex - 1), frameWidth, frameHeight);
            } else {
                drawSummaryOverlay(graphics2D, frameWidth, frameHeight);
            }
        } finally {
            graphics2D.dispose();
        }

        fxFrame = SwingFXUtils.toFXImage(frameBuffer, fxFrame);
        graphicsContext.drawImage(fxFrame, 0, 0, width, height);
    }

    private void drawIntroOverlay(Graphics2D graphics2D, int width, int height) {
        drawCenteredPanel(graphics2D, width, height, 640, 210, new Color(120, 196, 255));
        graphics2D.setColor(new Color(248, 250, 252));
        graphics2D.setFont(new Font("Segoe UI", Font.BOLD, 34));
        graphics2D.drawString("Судьи берут листки", width / 2 - 180, height / 2 - 14);
        graphics2D.setFont(new Font("Segoe UI", Font.PLAIN, 19));
        graphics2D.drawString("Сейчас каждый судья выставит свою оценку за выступление.", width / 2 - 250, height / 2 + 22);
        graphics2D.drawString("После вступления жми пробел или кликай, чтобы листать оценки.", width / 2 - 258, height / 2 + 54);
    }

    private void drawJudgeScoreOverlay(Graphics2D graphics2D, RenderedJudgeScore judgeScore, int width, int height) {
        BufferedImage portrait = judgeScore.score() >= 7.5 ? judgeScore.shockedImage() : judgeScore.normalImage();
        double portraitScale = Math.min((width * 0.34) / portrait.getWidth(), (height * 0.76) / portrait.getHeight());
        int portraitWidth = (int) Math.round(portrait.getWidth() * portraitScale);
        int portraitHeight = (int) Math.round(portrait.getHeight() * portraitScale);
        int portraitX = 72;
        int portraitY = height / 2 - portraitHeight / 2;

        graphics2D.setComposite(AlphaComposite.SrcOver.derive(0.32f));
        graphics2D.setColor(Color.BLACK);
        graphics2D.fillRoundRect(portraitX + 20, portraitY + 24, portraitWidth - 40, portraitHeight - 40, 28, 28);
        graphics2D.setComposite(AlphaComposite.SrcOver);
        graphics2D.drawImage(portrait, portraitX, portraitY, portraitWidth, portraitHeight, null);

        int panelX = width - 560;
        int panelY = 92;
        int panelWidth = 468;
        int panelHeight = height - 184;
        graphics2D.setColor(new Color(8, 14, 22, 226));
        graphics2D.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 28, 28);
        graphics2D.setColor(judgeScore.card().accent());
        graphics2D.setStroke(new BasicStroke(3f));
        graphics2D.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 28, 28);

        graphics2D.setFont(new Font("Segoe UI", Font.BOLD, 30));
        graphics2D.setColor(new Color(248, 250, 252));
        graphics2D.drawString(judgeScore.card().name(), panelX + 28, panelY + 42);
        graphics2D.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        graphics2D.setColor(new Color(226, 232, 240));
        graphics2D.drawString("Листок судьи", panelX + 28, panelY + 70);

        graphics2D.setPaint(new GradientPaint(
                panelX + 28,
                panelY + 92,
                judgeScore.card().accent(),
                panelX + panelWidth - 28,
                panelY + 92,
                new Color(judgeScore.card().accent().getRed(), judgeScore.card().accent().getGreen(), judgeScore.card().accent().getBlue(), 0)
        ));
        graphics2D.fillRect(panelX + 28, panelY + 88, panelWidth - 56, 3);

        graphics2D.setFont(new Font("Consolas", Font.BOLD, 24));
        graphics2D.setColor(judgeScore.card().accent());
        graphics2D.drawString(String.format("Оценка: %.1f / 10", judgeScore.score()), panelX + 28, panelY + 136);

        graphics2D.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        graphics2D.setColor(new Color(235, 241, 248));
        graphics2D.drawString(judgeScore.reaction(), panelX + 28, panelY + 176);

        graphics2D.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        graphics2D.drawString(String.format("Техника: %.1f", result.techniqueScore()), panelX + 28, panelY + 234);
        graphics2D.drawString(String.format("Харизма: %.1f", result.charismaScore()), panelX + 28, panelY + 266);
        graphics2D.drawString(String.format("Сила подачи: %.1f", result.powerScore()), panelX + 28, panelY + 298);
        graphics2D.drawString("Комбо: " + result.maxCombo(), panelX + 28, panelY + 330);
        graphics2D.drawString("Успешных нажатий: " + result.successfulPresses(), panelX + 28, panelY + 362);

        graphics2D.setFont(new Font("Segoe UI", Font.BOLD, 16));
        graphics2D.setColor(new Color(248, 250, 252));
        graphics2D.drawString("Пробел или клик - следующий листок | Enter - пропустить оценки", panelX + 28, panelY + panelHeight - 26);
    }

    private void drawSummaryOverlay(Graphics2D graphics2D, int width, int height) {
        drawCenteredPanel(graphics2D, width, height, 700, 260, new Color(244, 171, 62));
        graphics2D.setColor(new Color(248, 250, 252));
        graphics2D.setFont(new Font("Segoe UI", Font.BOLD, 34));
        graphics2D.drawString("Общий вердикт судей", width / 2 - 176, height / 2 - 44);
        graphics2D.setFont(new Font("Segoe UI", Font.BOLD, 28));
        graphics2D.setColor(new Color(244, 171, 62));
        graphics2D.drawString(String.format("Итоговый балл: %.1f / 10", result.totalScore()), width / 2 - 156, height / 2);
        graphics2D.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        graphics2D.setColor(new Color(226, 232, 240));
        graphics2D.drawString(result.summary(), width / 2 - 250, height / 2 + 38);
        graphics2D.drawString("Пробел или клик - перейти к итоговому экрану", width / 2 - 210, height / 2 + 74);
    }

    private void drawCenteredPanel(Graphics2D graphics2D, int width, int height, int panelWidth, int panelHeight, Color accent) {
        int panelX = (width - panelWidth) / 2;
        int panelY = (height - panelHeight) / 2;
        graphics2D.setColor(new Color(8, 14, 22, 226));
        graphics2D.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 28, 28);
        graphics2D.setColor(accent);
        graphics2D.setStroke(new BasicStroke(3f));
        graphics2D.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 28, 28);
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

    private BufferedImage loadBufferedImage(String resourcePath) {
        try (InputStream inputStream = JudgeResultsCutscene.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing judge results resource: " + resourcePath);
            }
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IllegalStateException("Unreadable judge results resource: " + resourcePath);
            }
            return image;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load judge results resource: " + resourcePath, exception);
        }
    }

    private record JudgeScoreCard(String name, String normalImagePath, String shockedImagePath, Color accent) {
    }

    private record RenderedJudgeScore(
            JudgeScoreCard card,
            double score,
            String reaction,
            BufferedImage normalImage,
            BufferedImage shockedImage
    ) {
        private RenderedJudgeScore(JudgeScoreCard card, double score, String reaction) {
            this(
                    card,
                    score,
                    reaction,
                    load(card.normalImagePath),
                    load(card.shockedImagePath)
            );
        }

        private static BufferedImage load(String path) {
            try (InputStream inputStream = JudgeResultsCutscene.class.getResourceAsStream(path)) {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing judge image: " + path);
                }
                BufferedImage image = ImageIO.read(inputStream);
                if (image == null) {
                    throw new IllegalStateException("Unreadable judge image: " + path);
                }
                return PoseAssetLoader.preparePoseImage(image);
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to load judge image: " + path, exception);
            }
        }
    }
}
