package gymgrind.game;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public final class CompetitionResultScreen {

    private final PerformanceResult result;
    private final String playerName;
    private BufferedImage frameBuffer;
    private WritableImage fxFrame;

    public CompetitionResultScreen(String playerName, PerformanceResult result) {
        this.playerName = playerName;
        this.result = result;
    }

    public void render(GraphicsContext graphicsContext, double width, double height) {
        int frameWidth = Math.max(1, (int) Math.round(width));
        int frameHeight = Math.max(1, (int) Math.round(height));
        ensureFrameBuffer(frameWidth, frameHeight);

        Graphics2D graphics2D = frameBuffer.createGraphics();
        try {
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics2D.setPaint(new GradientPaint(0, 0, new Color(8, 14, 22), 0, frameHeight, new Color(16, 28, 42)));
            graphics2D.fillRect(0, 0, frameWidth, frameHeight);

            int panelWidth = 760;
            int panelHeight = 420;
            int panelX = (frameWidth - panelWidth) / 2;
            int panelY = (frameHeight - panelHeight) / 2;
            graphics2D.setColor(new Color(5, 10, 18, 232));
            graphics2D.fill(new RoundRectangle2D.Double(panelX, panelY, panelWidth, panelHeight, 34, 34));
            graphics2D.setColor(new Color(244, 171, 62));
            graphics2D.setStroke(new BasicStroke(3f));
            graphics2D.draw(new RoundRectangle2D.Double(panelX, panelY, panelWidth, panelHeight, 34, 34));

            graphics2D.setColor(new Color(248, 250, 252));
            graphics2D.setFont(new Font("Segoe UI", Font.BOLD, 42));
            graphics2D.drawString("Итоги соревнования", panelX + 34, panelY + 58);
            graphics2D.setFont(new Font("Segoe UI", Font.PLAIN, 22));
            graphics2D.drawString(playerName + " завершил выход на сцену.", panelX + 34, panelY + 96);

            graphics2D.setFont(new Font("Segoe UI", Font.BOLD, 34));
            graphics2D.setColor(new Color(244, 171, 62));
            graphics2D.drawString(String.format("Итог: %.1f / 10", result.totalScore()), panelX + 34, panelY + 152);
            graphics2D.setFont(new Font("Segoe UI", Font.PLAIN, 24));
            graphics2D.setColor(new Color(226, 232, 240));
            graphics2D.drawString(result.rankLabel(), panelX + 34, panelY + 188);

            drawScoreLine(graphics2D, panelX + 34, panelY + 246, "Техника", result.techniqueScore());
            drawScoreLine(graphics2D, panelX + 34, panelY + 288, "Харизма", result.charismaScore());
            drawScoreLine(graphics2D, panelX + 34, panelY + 330, "Сила подачи", result.powerScore());

            graphics2D.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            graphics2D.drawString("Успехи: " + result.successfulPresses()
                    + "   Ошибки: " + result.failedPresses()
                    + "   Макс комбо: " + result.maxCombo(), panelX + 34, panelY + 378);
            graphics2D.drawString("Space / Enter / клик - вернуться на сцену", panelX + 34, panelY + 410);
        } finally {
            graphics2D.dispose();
        }

        fxFrame = SwingFXUtils.toFXImage(frameBuffer, fxFrame);
        graphicsContext.drawImage(fxFrame, 0, 0, width, height);
    }

    private void drawScoreLine(Graphics2D graphics2D, int x, int y, String label, double score) {
        graphics2D.setColor(new Color(226, 232, 240));
        graphics2D.drawString(label + ": " + String.format("%.1f", score), x, y);
        graphics2D.setColor(new Color(31, 41, 55));
        graphics2D.fillRoundRect(x + 220, y - 18, 360, 16, 14, 14);
        graphics2D.setPaint(new GradientPaint(x + 220, y - 18, new Color(34, 197, 94), x + 580, y - 18, new Color(244, 171, 62)));
        graphics2D.fillRoundRect(x + 220, y - 18, (int) Math.round(360 * (score / 10.0)), 16, 14, 14);
    }

    private void ensureFrameBuffer(int width, int height) {
        if (frameBuffer != null && frameBuffer.getWidth() == width && frameBuffer.getHeight() == height) {
            return;
        }
        frameBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        fxFrame = null;
    }
}
