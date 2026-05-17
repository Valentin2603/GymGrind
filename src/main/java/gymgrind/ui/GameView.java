package gymgrind.ui;

import gymgrind.game.CalendarState;
import gymgrind.game.GameState;
import gymgrind.game.LocationId;
import gymgrind.daily.DailyQuestNotification;
import gymgrind.daily.DailyQuestView;
import gymgrind.player.Player;
import gymgrind.player.PlayerProfile;
import gymgrind.shop.ShopPurchaseResult;
import gymgrind.shop.SupplementType;
import gymgrind.training.TrainingMachine;
import gymgrind.training.TrainingWeight;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class GameView extends StackPane {

    private static final String SHOP_IMAGE_PATH = "/assets/ui/shop/shop_screen.png";
    private static final double SHOP_SOURCE_SIZE = 1254.0;
    private static final double SHOP_SCALE = 0.54;
    private static final double SHOP_WIDTH = SHOP_SOURCE_SIZE * SHOP_SCALE;
    private static final double SHOP_HEIGHT = SHOP_SOURCE_SIZE * SHOP_SCALE;

    private static final double SHOP_MONEY_MASK_X = shopScale(160);
    private static final double SHOP_MONEY_MASK_Y = shopScale(1108);
    private static final double SHOP_MONEY_MASK_WIDTH = shopScale(228);
    private static final double SHOP_MONEY_MASK_HEIGHT = shopScale(78);

    private static final double SHOP_MONEY_TEXT_X = shopScale(182);
    private static final double SHOP_MONEY_TEXT_Y = shopScale(1104);
    private static final double SHOP_MONEY_TEXT_WIDTH = shopScale(208);
    private static final double SHOP_MONEY_TEXT_HEIGHT = shopScale(82);

    private static final double SHOP_CLOSE_X = shopScale(1019);
    private static final double SHOP_CLOSE_Y = shopScale(420);
    private static final double SHOP_CLOSE_WIDTH = shopScale(82);
    private static final double SHOP_CLOSE_HEIGHT = shopScale(82);

    private static final List<ShopSlot> SHOP_SLOTS = List.of(
            new ShopSlot(SupplementType.CREATINE, 108, 554, 236, 270),
            new ShopSlot(SupplementType.PROTEIN, 356, 554, 236, 270),
            new ShopSlot(SupplementType.PRE_WORKOUT, 604, 554, 236, 270),
            new ShopSlot(SupplementType.ENERGY_DRINK, 852, 554, 236, 270),
            new ShopSlot(SupplementType.KNEE_SLEEVES, 108, 826, 236, 270),
            new ShopSlot(SupplementType.HAND_WRAPS, 356, 826, 236, 270),
            new ShopSlot(SupplementType.ELBOW_WRAPS, 604, 826, 236, 270),
            new ShopSlot(SupplementType.RECOVERY_SHOT, 852, 826, 236, 270)
    );

    private final Canvas canvas;
    private final Hud hud;
    private final DailyQuestPanel dailyQuestPanel;
    private final ActiveSupplementsPanel activeSupplementsPanel;
    private final VBox leftHudColumn;
    private final MainMenu mainMenu;
    private final Label interactionPrompt;
    private final Label statusMessage;
    private final StackPane overlayLayer;

    public GameView(double width, double height) {
        setPrefSize(width, height);
        setStyle("-fx-background-color: #101820;");

        canvas = new Canvas(width, height);
        hud = new Hud();
        dailyQuestPanel = new DailyQuestPanel();
        activeSupplementsPanel = new ActiveSupplementsPanel();
        leftHudColumn = new VBox(10, dailyQuestPanel, activeSupplementsPanel);
        leftHudColumn.setMaxWidth(370);
        mainMenu = new MainMenu();
        mainMenu.prefWidthProperty().bind(widthProperty());
        mainMenu.prefHeightProperty().bind(heightProperty());
        interactionPrompt = createMessageLabel("#F8FAFC", "rgba(15, 23, 42, 0.88)");
        statusMessage = createMessageLabel("#E2E8F0", "rgba(15, 23, 42, 0.82)");
        overlayLayer = new StackPane();
        overlayLayer.setVisible(false);
        overlayLayer.setManaged(false);
        overlayLayer.setPickOnBounds(false);

        VBox bottomMessages = new VBox(10, interactionPrompt, statusMessage);
        bottomMessages.setPadding(new Insets(0, 24, 24, 24));
        bottomMessages.setAlignment(Pos.BOTTOM_CENTER);
        bottomMessages.setMouseTransparent(true);

        getChildren().addAll(canvas, leftHudColumn, hud, bottomMessages, overlayLayer, mainMenu);

        StackPane.setAlignment(leftHudColumn, Pos.TOP_LEFT);
        StackPane.setMargin(leftHudColumn, new Insets(16, 0, 0, 16));
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
        activeSupplementsPanel.update(player);
    }

    public void updateDailyQuests(List<DailyQuestView> quests, GameState gameState) {
        boolean visible = gameState != GameState.MENU;
        leftHudColumn.setVisible(visible);
        leftHudColumn.setManaged(visible);
        if (visible) {
            dailyQuestPanel.update(quests);
        }
    }

    public void showDailyQuestCompletion(DailyQuestNotification notification) {
        dailyQuestPanel.showCompletion(notification);
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
                         Function<SupplementType, ShopPurchaseResult> onBuy,
                         Runnable onClose) {
        StackPane root = new StackPane();
        root.setPrefSize(SHOP_WIDTH, SHOP_HEIGHT);
        root.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        root.setPickOnBounds(false);

        Pane panel = new Pane();
        panel.setPrefSize(SHOP_WIDTH, SHOP_HEIGHT);
        panel.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        panel.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        panel.setPickOnBounds(false);

        ImageView background = new ImageView(loadResourceImage(SHOP_IMAGE_PATH));
        background.setFitWidth(SHOP_WIDTH);
        background.setFitHeight(SHOP_HEIGHT);
        background.setPreserveRatio(false);
        background.setSmooth(false);

        Region moneyMask = createOverlayRegion(
                SHOP_MONEY_MASK_X,
                SHOP_MONEY_MASK_Y,
                SHOP_MONEY_MASK_WIDTH,
                SHOP_MONEY_MASK_HEIGHT,
                "-fx-background-color: rgba(21, 18, 19, 0.98);"
                        + "-fx-background-radius: 10;"
        );

        Label moneyLabel = new Label();
        moneyLabel.setLayoutX(SHOP_MONEY_TEXT_X);
        moneyLabel.setLayoutY(SHOP_MONEY_TEXT_Y);
        moneyLabel.setPrefSize(SHOP_MONEY_TEXT_WIDTH, SHOP_MONEY_TEXT_HEIGHT);
        moneyLabel.setMinSize(SHOP_MONEY_TEXT_WIDTH, SHOP_MONEY_TEXT_HEIGHT);
        moneyLabel.setMaxSize(SHOP_MONEY_TEXT_WIDTH, SHOP_MONEY_TEXT_HEIGHT);
        moneyLabel.setAlignment(Pos.CENTER_LEFT);
        moneyLabel.setStyle("-fx-text-fill: #F8E5CC;");
        moneyLabel.setFont(Font.font("Consolas", FontWeight.BLACK, 28));
        moneyLabel.setMouseTransparent(true);

        Label cardTitle = createShopCardLabel("#F8E5CC", 24, true);
        Label boostLabel = createShopCardLabel("#F6B94D", 14, true);
        Label descriptionLabel = createShopCardLabel("#E2E8F0", 13, true);
        Label priceLabel = createShopCardLabel("#F8E5CC", 15, false);
        Label activeLabel = createShopCardLabel("#9FB1C5", 12, true);
        Label feedbackLabel = createShopCardLabel("#F8E5CC", 12, true);
        Label abilitiesListLabel = createShopCardLabel("#E2E8F0", 13, true);

        Label detailsCaption = createShopCardLabel("#9FB1C5", 11, false);
        detailsCaption.setText("ОПИСАНИЕ");

        Label abilitiesTitle = createShopCardLabel("#F8E5CC", 20, false);
        abilitiesTitle.setText("Активные бусты");
        Label abilitiesHint = createShopCardLabel("#9FB1C5", 11, true);
        abilitiesHint.setText("Эффекты на следующую тренировку отображаются здесь.");

        VBox abilitiesCard = new VBox(10, abilitiesTitle, abilitiesHint, abilitiesListLabel);
        abilitiesCard.setAlignment(Pos.TOP_LEFT);
        abilitiesCard.setPrefWidth(250);
        abilitiesCard.setMinWidth(250);
        abilitiesCard.setMaxWidth(250);
        abilitiesCard.setPadding(new Insets(16));
        abilitiesCard.setStyle("-fx-background-color: rgba(8, 12, 18, 0.86);"
                + "-fx-background-radius: 18;"
                + "-fx-border-color: rgba(248, 229, 204, 0.22);"
                + "-fx-border-radius: 18;"
                + "-fx-border-width: 1.4;");
        StackPane.setAlignment(abilitiesCard, Pos.CENTER_LEFT);
        abilitiesCard.setTranslateX(-240);
        abilitiesCard.setTranslateY(34);

        Region accentLine = new Region();
        accentLine.setPrefSize(260, 2);
        accentLine.setMinSize(260, 2);
        accentLine.setMaxSize(260, 2);
        accentLine.setStyle("-fx-background-color: linear-gradient(to right, rgba(246,185,77,1.0), rgba(248,229,204,0.15));"
                + "-fx-background-radius: 99;");

        Button confirmButton = createOverlayButton("Купить", "#D97706");
        confirmButton.setPrefWidth(140);
        Button cancelButton = createOverlayButton("Отмена", "#4B5563");
        cancelButton.setPrefWidth(140);

        HBox actionRow = new HBox(10, confirmButton, cancelButton);
        actionRow.setAlignment(Pos.CENTER);

        VBox detailsCard = new VBox(
                10,
                detailsCaption,
                cardTitle,
                accentLine,
                boostLabel,
                descriptionLabel,
                priceLabel,
                activeLabel,
                actionRow,
                feedbackLabel
        );
        detailsCard.setAlignment(Pos.TOP_LEFT);
        detailsCard.setPrefSize(332, 332);
        detailsCard.setMinSize(332, 332);
        detailsCard.setMaxSize(332, 332);
        detailsCard.setPadding(new Insets(18));
        detailsCard.setVisible(false);
        detailsCard.setManaged(false);
        detailsCard.setMouseTransparent(false);
        detailsCard.setStyle("-fx-background-color: rgba(8, 12, 18, 0.90);"
                + "-fx-background-radius: 20;"
                + "-fx-border-color: rgba(246, 185, 77, 0.96);"
                + "-fx-border-radius: 20;"
                + "-fx-border-width: 2.2;");
        StackPane.setAlignment(detailsCard, Pos.CENTER);

        Region closeHover = createOverlayRegion(
                SHOP_CLOSE_X,
                SHOP_CLOSE_Y,
                SHOP_CLOSE_WIDTH,
                SHOP_CLOSE_HEIGHT,
                "-fx-background-color: rgba(248, 229, 204, 0.12);"
                        + "-fx-border-color: rgba(248, 229, 204, 0.92);"
                        + "-fx-border-width: 2;"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
        );
        closeHover.setVisible(false);

        Button closeButton = createTransparentHotspot(SHOP_CLOSE_X, SHOP_CLOSE_Y, SHOP_CLOSE_WIDTH, SHOP_CLOSE_HEIGHT);

        final SupplementType[] selectedItem = {null};
        Map<SupplementType, Region> selectionFrames = new EnumMap<>(SupplementType.class);

        Runnable refreshMoney = () -> {
            moneyLabel.setText(Integer.toString(player.stats().money()));
            moneyLabel.setFont(Font.font("Consolas", FontWeight.BLACK, fittedMoneyFontSize(player.stats().money())));
        };
        Runnable refreshActive = () -> {
            activeLabel.setText("Активно: " + player.activeSupplements().labels());
            abilitiesListLabel.setText(formatActiveSupplements(player));
        };

        Runnable clearSelection = () -> {
            selectedItem[0] = null;
            detailsCard.setVisible(false);
            detailsCard.setManaged(false);
            feedbackLabel.setText("");
            selectionFrames.values().forEach(frame -> frame.setVisible(false));
        };

        Consumer<SupplementType> openDetails = supplementType -> {
            selectedItem[0] = supplementType;
            cardTitle.setText(supplementType.label());
            boostLabel.setText("Буст: " + supplementType.effect());
            descriptionLabel.setText(supplementType.description());
            priceLabel.setText("Цена: $" + supplementType.price());
            refreshActive.run();
            feedbackLabel.setText("Нажмите кнопку Купить или Отмена, чтобы закрыть описание.");
            detailsCard.setVisible(true);
            detailsCard.setManaged(true);
            selectionFrames.forEach((type, frame) -> frame.setVisible(type == supplementType));
        };

        Runnable performPurchase = () -> {
            SupplementType supplementType = selectedItem[0];
            if (supplementType == null) {
                return;
            }

            ShopPurchaseResult result = onBuy.apply(supplementType);
            refreshMoney.run();
            refreshActive.run();

            if (result.success()) {
                clearSelection.run();
                return;
            }

            feedbackLabel.setText(result.message());
            detailsCard.setVisible(true);
            detailsCard.setManaged(true);
        };

        cancelButton.setOnAction(event -> clearSelection.run());
        confirmButton.setOnAction(event -> performPurchase.run());
        closeButton.setOnAction(event -> onClose.run());
        closeButton.hoverProperty().addListener((obs, oldValue, hovered) -> closeHover.setVisible(hovered));

        panel.getChildren().addAll(background, moneyMask, moneyLabel, closeHover);

        for (ShopSlot shopSlot : SHOP_SLOTS) {
            double iconX = shopScale(shopSlot.x() + 46);
            double iconY = shopScale(shopSlot.y() + 64);
            double iconWidth = shopScale(150);
            double iconHeight = shopScale(146);

            Region selectionFrame = createOverlayRegion(
                    iconX,
                    iconY,
                    iconWidth,
                    iconHeight,
                    "-fx-background-color: rgba(246, 185, 77, 0.12);"
                            + "-fx-border-color: rgba(246, 185, 77, 1.0);"
                            + "-fx-border-width: 3;"
                            + "-fx-border-radius: 10;"
                            + "-fx-background-radius: 10;"
            );
            selectionFrame.setVisible(false);

            selectionFrames.put(shopSlot.type(), selectionFrame);

            Button itemHotspot = createTransparentHotspot(iconX, iconY, iconWidth, iconHeight);
            itemHotspot.setOnAction(event -> openDetails.accept(shopSlot.type()));

            panel.getChildren().addAll(selectionFrame, itemHotspot);
        }

        panel.getChildren().add(closeButton);
        root.getChildren().addAll(panel, abilitiesCard, detailsCard);

        refreshMoney.run();
        refreshActive.run();
        showOverlay(root);
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

    private Button createTransparentHotspot(double x, double y, double width, double height) {
        Button button = new Button();
        button.setLayoutX(x);
        button.setLayoutY(y);
        button.setPrefSize(width, height);
        button.setMinSize(width, height);
        button.setMaxSize(width, height);
        button.setCursor(Cursor.HAND);
        button.setStyle("-fx-background-color: transparent;"
                + "-fx-border-color: transparent;"
                + "-fx-padding: 0;");
        return button;
    }

    private Region createOverlayRegion(double x, double y, double width, double height, String style) {
        Region region = new Region();
        region.setLayoutX(x);
        region.setLayoutY(y);
        region.setPrefSize(width, height);
        region.setMinSize(width, height);
        region.setMaxSize(width, height);
        region.setStyle(style);
        region.setMouseTransparent(true);
        return region;
    }

    private Label createShopCardLabel(String color, double size, boolean wrap) {
        Label label = new Label();
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, size));
        label.setWrapText(wrap);
        label.setStyle("-fx-text-fill: " + color + ";");
        return label;
    }

    private Image loadResourceImage(String resourcePath) {
        return new Image(Objects.requireNonNull(
                GameView.class.getResource(resourcePath),
                "Missing resource: " + resourcePath
        ).toExternalForm());
    }

    private double fittedMoneyFontSize(int money) {
        int length = Integer.toString(money).length();
        if (length <= 4) {
            return 28;
        }
        if (length == 5) {
            return 24;
        }
        return 20;
    }

    private String formatActiveSupplements(Player player) {
        String labels = player.activeSupplements().labels();
        if ("нет".equalsIgnoreCase(labels)) {
            return "Сейчас ничего не активировано.\n\nКупленные бафы на следующую тренировку появятся здесь.";
        }

        return "• " + labels.replace(", ", "\n• ");
    }

    private static double shopScale(double value) {
        return value * SHOP_SCALE;
    }

    private record ShopSlot(SupplementType type, double x, double y, double width, double height) {
    }
}
