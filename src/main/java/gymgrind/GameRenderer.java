package gymgrind;

import gymgrind.model.GameMap;
import gymgrind.model.GymObject;
import gymgrind.model.Player;
import gymgrind.model.SkillCheckResult;
import gymgrind.model.SkillCheckSession;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Optional;

public final class GameRenderer {

    private static final Color BACKGROUND = Color.web("#101820");
    private static final Color FLOOR = Color.web("#1B2838");
    private static final Color FLOOR_GRID = Color.web("#213247");
    private static final Color BORDER = Color.web("#7FDBA4");
    private static final Color PLAYER_COLOR = Color.web("#F8FAFC");
    private static final Color PLAYER_OUTLINE = Color.web("#49C16D");
    private static final Color LABEL_COLOR = Color.web("#E2E8F0");
    private static final Color SUBTITLE_COLOR = Color.web("#94A3B8");
    private static final Color HIGHLIGHT = Color.web("#F8D66D");
    private static final Color OVERLAY_BACKDROP = Color.color(0.02, 0.04, 0.07, 0.78);
    private static final Color OVERLAY_PANEL = Color.web("#0F172A");
    private static final Color BAR_COLOR = Color.web("#1E293B");
    private static final Color SUCCESS_ZONE = Color.web("#22C55E");
    private static final Color SEQUENCE_BAR_FILL = Color.web("#F59E0B");

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

        graphicsContext.setStroke(FLOOR_GRID);
        graphicsContext.setLineWidth(1);
        for (double x = gameMap.left() + 30; x < gameMap.right(); x += 60) {
            graphicsContext.strokeLine(x, gameMap.top() + 10, x, gameMap.bottom() - 10);
        }
        for (double y = gameMap.top() + 30; y < gameMap.bottom(); y += 60) {
            graphicsContext.strokeLine(gameMap.left() + 10, y, gameMap.right() - 10, y);
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
            graphicsContext.setFill(gymObject.color());
            graphicsContext.fillRoundRect(
                    gymObject.left(),
                    gymObject.top(),
                    gymObject.width(),
                    gymObject.height(),
                    20,
                    20
            );

            Color outline = nearbyObject.filter(object -> object == gymObject).isPresent() ? HIGHLIGHT : BORDER;
            graphicsContext.setStroke(outline);
            graphicsContext.setLineWidth(3);
            graphicsContext.strokeRoundRect(
                    gymObject.left(),
                    gymObject.top(),
                    gymObject.width(),
                    gymObject.height(),
                    20,
                    20
            );

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
        graphicsContext.setFill(PLAYER_COLOR);
        graphicsContext.fillOval(player.position().x(), player.position().y(), player.width(), player.height());

        graphicsContext.setStroke(PLAYER_OUTLINE);
        graphicsContext.setLineWidth(3);
        graphicsContext.strokeOval(player.position().x(), player.position().y(), player.width(), player.height());
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
                "Неделя 1: движение, границы карты, интерактивные зоны, меню и первые скилл-чеки",
                gameMap.left(),
                60
        );
        graphicsContext.fillText("Режим: " + gameState.title(), gameMap.right() - 185, 38);
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
        graphicsContext.fillText("Эффект: " + buildResultSummary(result), panelLeft + 36, panelTop + 208, panelWidth - 72);

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
            return "Повторы: " + session.completedHits() + "/" + session.requiredHits() + ". Зона сужается, сила ускоряет маркер.";
        }

        return "Одно точное попадание засчитывает подход.";
    }

    private String footerText(SkillCheckSession session) {
        if (session.requiresMultipleHits()) {
            return "Для жима лёжа нужно собрать все 5 повторов подряд: каждое попадание делает зелёную зону уже.";
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
}
