package gymgrind.ui;

import gymgrind.GameState;
import gymgrind.model.Player;
import gymgrind.model.TrainingMachine;
import gymgrind.model.TrainingWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.function.Consumer;

public final class GameView extends StackPane {

    private final Canvas canvas;
    private final Hud hud;
    private final MainMenu mainMenu;
    private final Label interactionPrompt;
    private final Label statusMessage;
    private final StackPane overlayLayer;

    public GameView(double width, double height) {
        setPrefSize(width, height);
        setStyle("-fx-background-color: #101820;");

        canvas = new Canvas(width, height);
        hud = new Hud();
        mainMenu = new MainMenu();
        interactionPrompt = createMessageLabel("#F8FAFC", "rgba(15, 23, 42, 0.88)");
        statusMessage = createMessageLabel("#E2E8F0", "rgba(15, 23, 42, 0.82)");
        overlayLayer = new StackPane();
        overlayLayer.setVisible(false);
        overlayLayer.setManaged(false);

        VBox bottomMessages = new VBox(10, interactionPrompt, statusMessage);
        bottomMessages.setPadding(new Insets(0, 24, 24, 24));
        bottomMessages.setAlignment(Pos.BOTTOM_CENTER);
        bottomMessages.setMouseTransparent(true);

        getChildren().addAll(canvas, hud, bottomMessages, overlayLayer, mainMenu);

        StackPane.setAlignment(hud, Pos.TOP_LEFT);
        StackPane.setMargin(hud, new Insets(20, 0, 0, 20));
        StackPane.setAlignment(bottomMessages, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(overlayLayer, Pos.CENTER);
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

    public void showTrainingSetup(TrainingMachine machine,
                                  Consumer<TrainingWeight> onWeightSelected,
                                  Runnable onCancel) {
        VBox panel = new VBox(14);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(440);
        panel.setPadding(new Insets(24));
        panel.setStyle("-fx-background-color: rgba(8, 15, 23, 0.94);"
                + "-fx-background-radius: 18;"
                + "-fx-border-color: #7FDBA4;"
                + "-fx-border-radius: 18;"
                + "-fx-border-width: 2;");

        Label title = new Label(machine.name());
        title.setFont(Font.font("Segoe UI", 24));
        title.setStyle("-fx-text-fill: #F8FAFC; -fx-font-weight: bold;");

        Label subtitle = new Label("Выберите вес. Чем тяжелее вес, тем выше награда и сложность.");
        subtitle.setFont(Font.font("Segoe UI", 15));
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setStyle("-fx-text-fill: #CBD5E1;");

        Button light = createOverlayButton("Лёгкий вес", "#22C55E");
        Button medium = createOverlayButton("Средний вес", "#F59E0B");
        Button heavy = createOverlayButton("Тяжёлый вес", "#EF4444");
        Button cancel = createOverlayButton("Отмена", "#475569");

        light.setOnAction(event -> onWeightSelected.accept(TrainingWeight.LIGHT));
        medium.setOnAction(event -> onWeightSelected.accept(TrainingWeight.MEDIUM));
        heavy.setOnAction(event -> onWeightSelected.accept(TrainingWeight.HEAVY));
        cancel.setOnAction(event -> onCancel.run());

        panel.getChildren().addAll(title, subtitle, light, medium, heavy, cancel);
        showOverlay(panel);
    }

    public void showOverlay(Node node) {
        overlayLayer.getChildren().setAll(node);
        overlayLayer.setVisible(true);
        overlayLayer.setManaged(true);
        Platform.runLater(node::requestFocus);
    }

    public void hideOverlay() {
        overlayLayer.getChildren().clear();
        overlayLayer.setVisible(false);
        overlayLayer.setManaged(false);
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

    private Button createOverlayButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefWidth(260);
        button.setStyle("-fx-background-color: " + color + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 15px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 10 16 10 16;");
        return button;
    }
}
