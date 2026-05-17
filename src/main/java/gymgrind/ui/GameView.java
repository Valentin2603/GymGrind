package gymgrind.ui;

import gymgrind.game.CalendarState;
import gymgrind.game.GameState;
import gymgrind.game.LocationId;
import gymgrind.player.Player;
import gymgrind.player.PlayerProfile;
import gymgrind.shop.SupplementType;
import gymgrind.training.TrainingMachine;
import gymgrind.training.TrainingWeight;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
        mainMenu.prefWidthProperty().bind(widthProperty());
        mainMenu.prefHeightProperty().bind(heightProperty());
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

        StackPane.setAlignment(hud, Pos.TOP_RIGHT);
        StackPane.setMargin(hud, new Insets(16, 16, 0, 0));
        StackPane.setAlignment(bottomMessages, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(overlayLayer, Pos.CENTER);
        StackPane.setAlignment(mainMenu, Pos.CENTER);
    }

    public GraphicsContext getGraphicsContext() {
        return canvas.getGraphicsContext2D();
    }

    public void updateHud(Player player, GameState gameState, CalendarState calendarState) {
        hud.update(player, gameState, calendarState);
    }

    public void setHudCompactMode(boolean compactMode) {
        hud.setCompactMode(compactMode);
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

    public void setOnContinue(Runnable action) {
        mainMenu.setOnContinue(action);
    }

    public void setContinueAvailable(boolean available) {
        mainMenu.setContinueAvailable(available);
    }

    public void setOnExit(Runnable action) {
        mainMenu.setOnExit(action);
    }

    public PlayerProfile selectedProfile() {
        return mainMenu.selectedProfile();
    }

    public void showTrainingSetup(TrainingMachine machine,
                                  String workingLoadLabel,
                                  Function<TrainingWeight, String> weightLabelFactory,
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

        Label subtitle = new Label("Рабочая нагрузка: " + workingLoadLabel
                + ". Лёгкий вес ниже рабочего, тяжёлый выше. Чем больше рабочий вес, тем медленнее растёт прогресс.");
        subtitle.setFont(Font.font("Segoe UI", 15));
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setStyle("-fx-text-fill: #CBD5E1;");

        Button light = createOverlayButton(weightLabelFactory.apply(TrainingWeight.LIGHT), "#22C55E");
        Button medium = createOverlayButton(weightLabelFactory.apply(TrainingWeight.MEDIUM), "#F59E0B");
        Button heavy = createOverlayButton(weightLabelFactory.apply(TrainingWeight.HEAVY), "#EF4444");
        Button cancel = createOverlayButton("Отмена", "#475569");

        light.setOnAction(event -> onWeightSelected.accept(TrainingWeight.LIGHT));
        medium.setOnAction(event -> onWeightSelected.accept(TrainingWeight.MEDIUM));
        heavy.setOnAction(event -> onWeightSelected.accept(TrainingWeight.HEAVY));
        cancel.setOnAction(event -> onCancel.run());

        panel.getChildren().addAll(title, subtitle, light, medium, heavy, cancel);
        showOverlay(panel);
    }

    public void showPauseMenu(Supplier<String> onSave,
                              Runnable onExit,
                              Runnable onResume) {
        VBox panel = new VBox(14);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(420);
        panel.setPadding(new Insets(24));
        panel.setStyle("-fx-background-color: rgba(8, 15, 23, 0.96);"
                + "-fx-background-radius: 18;"
                + "-fx-border-color: #7DD3FC;"
                + "-fx-border-radius: 18;"
                + "-fx-border-width: 2;");

        Label title = new Label("Пауза");
        title.setFont(Font.font("Segoe UI", 26));
        title.setStyle("-fx-text-fill: #F8FAFC; -fx-font-weight: bold;");

        Label feedback = new Label("Игра остановлена. Можно сохраниться или вернуться назад.");
        feedback.setFont(Font.font("Segoe UI", 14));
        feedback.setWrapText(true);
        feedback.setAlignment(Pos.CENTER);
        feedback.setStyle("-fx-text-fill: #CBD5E1;");

        Button save = createOverlayButton("Сохранить игру", "#22C55E");
        Button exit = createOverlayButton("Выйти из игры", "#EF4444");
        Button resume = createOverlayButton("Назад", "#475569");

        save.setOnAction(event -> feedback.setText(onSave.get()));
        exit.setOnAction(event -> onExit.run());
        resume.setOnAction(event -> onResume.run());

        panel.getChildren().addAll(title, feedback, save, exit, resume);
        showOverlay(panel);
    }

    public void showShop(Player player,
                         Function<SupplementType, String> onBuy,
                         Runnable onClose) {
        VBox panel = new VBox(12);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(560);
        panel.setPadding(new Insets(24));
        panel.setStyle("-fx-background-color: rgba(8, 15, 23, 0.94);"
                + "-fx-background-radius: 18;"
                + "-fx-border-color: #F59E0B;"
                + "-fx-border-radius: 18;"
                + "-fx-border-width: 2;");

        Label title = new Label("Магазин добавок");
        title.setFont(Font.font("Segoe UI", 24));
        title.setStyle("-fx-text-fill: #F8FAFC; -fx-font-weight: bold;");

        Label moneyLabel = new Label();
        moneyLabel.setFont(Font.font("Segoe UI", 16));
        moneyLabel.setStyle("-fx-text-fill: #F8D66D;");

        Label activeLabel = new Label();
        activeLabel.setFont(Font.font("Segoe UI", 14));
        activeLabel.setWrapText(true);
        activeLabel.setStyle("-fx-text-fill: #CBD5E1;");

        Label feedbackLabel = new Label("Выберите добавку.");
        feedbackLabel.setFont(Font.font("Segoe UI", 14));
        feedbackLabel.setWrapText(true);
        feedbackLabel.setAlignment(Pos.CENTER);
        feedbackLabel.setStyle("-fx-text-fill: #E2E8F0;");

        Runnable refreshLabels = () -> {
            moneyLabel.setText("Деньги: " + player.stats().money());
            activeLabel.setText("Активные добавки: " + player.activeSupplements().labels());
        };
        refreshLabels.run();

        panel.getChildren().addAll(title, moneyLabel, activeLabel, feedbackLabel);

        for (SupplementType supplementType : SupplementType.values()) {
            Button buyButton = createOverlayButton(
                    supplementType.label() + " - " + supplementType.price() + " | " + supplementType.effect(),
                    "#D97706"
            );
            buyButton.setPrefWidth(500);
            buyButton.setOnAction(event -> {
                feedbackLabel.setText(onBuy.apply(supplementType));
                refreshLabels.run();
            });
            panel.getChildren().add(buyButton);
        }

        Button close = createOverlayButton("Закрыть", "#475569");
        close.setOnAction(event -> onClose.run());
        panel.getChildren().add(close);

        showOverlay(panel);
    }

    public void showLocationMenu(LocationId currentLocation,
                                 List<LocationId> destinations,
                                 Consumer<LocationId> onSelect,
                                 Runnable onClose) {
        VBox panel = new VBox(12);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(460);
        panel.setPadding(new Insets(24));
        panel.setStyle("-fx-background-color: rgba(8, 15, 23, 0.94);"
                + "-fx-background-radius: 18;"
                + "-fx-border-color: #38BDF8;"
                + "-fx-border-radius: 18;"
                + "-fx-border-width: 2;");

        Label title = new Label("Выбор локации");
        title.setFont(Font.font("Segoe UI", 24));
        title.setStyle("-fx-text-fill: #F8FAFC; -fx-font-weight: bold;");

        Label subtitle = new Label("Сейчас: " + currentLocation.displayName());
        subtitle.setFont(Font.font("Segoe UI", 15));
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setStyle("-fx-text-fill: #CBD5E1;");

        panel.getChildren().addAll(title, subtitle);

        if (destinations.isEmpty()) {
            Label emptyLabel = new Label("Других доступных локаций пока нет.");
            emptyLabel.setFont(Font.font("Segoe UI", 14));
            emptyLabel.setStyle("-fx-text-fill: #E2E8F0;");
            panel.getChildren().add(emptyLabel);
        } else {
            for (LocationId destination : destinations) {
                Button button = createOverlayButton(destination.displayName(), "#0EA5E9");
                button.setPrefWidth(320);
                button.setOnAction(event -> onSelect.accept(destination));
                panel.getChildren().add(button);
            }
        }

        Button cancel = createOverlayButton("Отмена", "#475569");
        cancel.setOnAction(event -> onClose.run());
        panel.getChildren().add(cancel);

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
