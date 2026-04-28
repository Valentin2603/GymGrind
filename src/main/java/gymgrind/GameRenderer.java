package gymgrind;

import gymgrind.model.GameMap;
import gymgrind.model.GymObject;
import gymgrind.model.Player;
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
                "Неделя 1: движение, границы карты, интерактивные зоны и меню",
                gameMap.left(),
                60
        );
        graphicsContext.fillText("Режим: " + gameState.title(), gameMap.right() - 185, 38);
        graphicsContext.fillText("WASD/стрелки - ходьба | E - действие | Esc - меню", gameMap.right() - 360, 60);
    }
}
