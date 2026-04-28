package gymgrind.ui;

import gymgrind.GameState;
import gymgrind.model.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public final class GameView extends StackPane {

    private final Canvas canvas;
    private final Hud hud;
    private final MainMenu mainMenu;
    private final Label interactionPrompt;
    private final Label statusMessage;

    public GameView(double width, double height) {
        setPrefSize(width, height);
        setStyle("-fx-background-color: #101820;");

        canvas = new Canvas(width, height);
        hud = new Hud();
        mainMenu = new MainMenu();
        interactionPrompt = createMessageLabel("#F8FAFC", "rgba(15, 23, 42, 0.88)");
        statusMessage = createMessageLabel("#E2E8F0", "rgba(15, 23, 42, 0.82)");

        VBox bottomMessages = new VBox(10, interactionPrompt, statusMessage);
        bottomMessages.setPadding(new Insets(0, 24, 24, 24));
        bottomMessages.setAlignment(Pos.BOTTOM_CENTER);
        bottomMessages.setMouseTransparent(true);

        getChildren().addAll(canvas, hud, bottomMessages, mainMenu);

        StackPane.setAlignment(hud, Pos.TOP_LEFT);
        StackPane.setMargin(hud, new Insets(20, 0, 0, 20));
        StackPane.setAlignment(bottomMessages, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(mainMenu, Pos.CENTER);
    }

    public GraphicsContext getGraphicsContext() {
        return canvas.getGraphicsContext2D();
    }

    public void updateHud(Player player, GameState gameState) {
        hud.update(player, gameState);
    }

    public void setInteractionPrompt(String text) {
        interactionPrompt.setText(text);
        interactionPrompt.setVisible(!text.isBlank());
    }

    public void setStatusMessage(String text) {
        statusMessage.setText(text);
        statusMessage.setVisible(!text.isBlank());
    }

    public void setMainMenuVisible(boolean visible) {
        mainMenu.setVisible(visible);
        mainMenu.setManaged(visible);
    }

    public void setOnStart(Runnable action) {
        mainMenu.setOnStart(action);
    }

    public void setOnExit(Runnable action) {
        mainMenu.setOnExit(action);
    }

    public void requestGameFocus() {
        requestFocus();
    }

    private Label createMessageLabel(String textColor, String backgroundColor) {
        Label label = new Label();
        label.setFont(Font.font("Segoe UI", 16));
        label.setWrapText(true);
        label.setMaxWidth(760);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-text-fill: " + textColor + ";"
                + "-fx-background-color: " + backgroundColor + ";"
                + "-fx-background-radius: 14;"
                + "-fx-padding: 10 16 10 16;");
        return label;
    }
}
