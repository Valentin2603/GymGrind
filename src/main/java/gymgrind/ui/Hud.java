package gymgrind.ui;

import gymgrind.game.CalendarState;
import gymgrind.game.GameState;
import gymgrind.player.Player;
import gymgrind.player.Stats;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Objects;

public final class Hud extends VBox {

    private static final double HUD_WIDTH = 577;
    private static final double HUD_HEIGHT = 97;
    private static final double DAY_TEXT_X = 56;
    private static final double DAY_TEXT_Y = 35;
    private static final double DAY_TEXT_WIDTH = 38;
    private static final double DAY_TEXT_HEIGHT = 28;
    private static final double MONEY_TEXT_X = 196;
    private static final double MONEY_TEXT_Y = 28;
    private static final double MONEY_TEXT_WIDTH = 90;
    private static final double MONEY_TEXT_HEIGHT = 28;
    private static final double STAMINA_FILL_X = 373;
    private static final double STAMINA_FILL_Y = 37;
    private static final double STAMINA_FILL_WIDTH = 82;
    private static final double STAMINA_FILL_HEIGHT = 18;
    private static final double STAMINA_TEXT_X = 491;
    private static final double STAMINA_TEXT_Y = 29;
    private static final double STAMINA_TEXT_WIDTH = 95;
    private static final double STAMINA_TEXT_HEIGHT = 28;

    private Label hudDayLabel;
    private Label hudMoneyLabel;
    private Label hudStaminaLabel;
    private Rectangle staminaFill;
    private final VBox statsPanel;
    private final Label dayLabel;
    private final Label strengthLabel;
    private final Label muscleLabel;
    private final Label staminaLabel;
    private final Label availableStaminaLabel;
    private final Label fatigueLabel;
    private final Label bodyFatLabel;
    private final Label formLabel;
    private boolean statsVisible;

    public Hud() {
        setSpacing(8);
        setAlignment(Pos.TOP_RIGHT);
        setFillWidth(false);
        setPickOnBounds(false);
        setMaxWidth(Region.USE_PREF_SIZE);

        Pane hudFrame = createHudFrame();
        Tooltip.install(hudFrame, new Tooltip("Кликните по HUD, чтобы показать характеристики"));
        hudFrame.setCursor(Cursor.HAND);
        hudFrame.setOnMouseClicked(event -> setStatsVisible(!statsVisible));

        Label title = new Label("Характеристики");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setStyle("-fx-text-fill: #F8FAFC;");

        dayLabel = createStatLabel();
        strengthLabel = createStatLabel();
        muscleLabel = createStatLabel();
        staminaLabel = createStatLabel();
        availableStaminaLabel = createStatLabel();
        fatigueLabel = createStatLabel();
        bodyFatLabel = createStatLabel();
        formLabel = createStatLabel();

        statsPanel = new VBox(
                6,
                title,
                dayLabel,
                strengthLabel,
                muscleLabel,
                staminaLabel,
                availableStaminaLabel,
                fatigueLabel,
                bodyFatLabel,
                formLabel
        );
        statsPanel.setAlignment(Pos.TOP_LEFT);
        statsPanel.setMaxWidth(260);
        statsPanel.setPadding(new Insets(12));
        statsPanel.setStyle("-fx-background-color: rgba(8, 15, 23, 0.92);"
                + "-fx-background-radius: 16;"
                + "-fx-border-color: #7FDBA4;"
                + "-fx-border-radius: 16;"
                + "-fx-border-width: 1.5;");

        getChildren().addAll(hudFrame, statsPanel);
        setStatsVisible(false);
    }

    public void update(Player player, GameState gameState, CalendarState calendarState) {
        boolean visible = gameState != GameState.MENU;
        setVisible(visible);
        setManaged(visible);
        if (!visible) {
            return;
        }

        Stats stats = player.stats();
        int availableStamina = stats.availableStamina();
        int maxAvailableStamina = stats.maxAvailableStamina();
        double fillRatio = maxAvailableStamina == 0 ? 0.0 : (double) availableStamina / maxAvailableStamina;

        hudDayLabel.setText(Integer.toString(calendarState.currentDay()));
        hudMoneyLabel.setText(Integer.toString(stats.money()));
        hudStaminaLabel.setText(availableStamina + "/" + maxAvailableStamina);
        staminaFill.setWidth(STAMINA_FILL_WIDTH * Math.max(0.0, Math.min(1.0, fillRatio)));

        dayLabel.setText("День: " + calendarState.currentDay() + "/" + calendarState.maxDays());
        strengthLabel.setText("Сила: " + stats.strength());
        muscleLabel.setText("Масса: " + stats.muscle());
        staminaLabel.setText("Стат выносливости: " + stats.stamina());
        availableStaminaLabel.setText("Текущая стамина: " + availableStamina + "/" + maxAvailableStamina);
        fatigueLabel.setText("Усталость: " + stats.fatigue());
        bodyFatLabel.setText("% жира: " + stats.bodyFat() + "%");
        formLabel.setText("Форма: " + stats.form());
    }

    private Pane createHudFrame() {
        Pane pane = new Pane();
        pane.setPrefSize(HUD_WIDTH, HUD_HEIGHT);
        pane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        pane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        pane.setPickOnBounds(true);

        ImageView imageView = new ImageView(loadHudImage());
        imageView.setFitWidth(HUD_WIDTH);
        imageView.setFitHeight(HUD_HEIGHT);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(false);

        Rectangle dayMask = createMask(DAY_TEXT_X, DAY_TEXT_Y, DAY_TEXT_WIDTH, DAY_TEXT_HEIGHT, "#F4ECD3");
        Rectangle moneyMask = createMask(MONEY_TEXT_X, MONEY_TEXT_Y, MONEY_TEXT_WIDTH, MONEY_TEXT_HEIGHT, "#060606");
        Rectangle staminaTextMask = createMask(STAMINA_TEXT_X, STAMINA_TEXT_Y, STAMINA_TEXT_WIDTH, STAMINA_TEXT_HEIGHT, "#060606");
        Rectangle staminaEmptyMask = createMask(STAMINA_FILL_X, STAMINA_FILL_Y, STAMINA_FILL_WIDTH, STAMINA_FILL_HEIGHT, "#4F3D21");

        staminaFill = new Rectangle(STAMINA_FILL_X, STAMINA_FILL_Y, 0, STAMINA_FILL_HEIGHT);
        staminaFill.setArcWidth(3);
        staminaFill.setArcHeight(3);
        staminaFill.setFill(new LinearGradient(
                0,
                0,
                1,
                0,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#FFF39A")),
                new Stop(0.52, Color.web("#F7D83E")),
                new Stop(1.0, Color.web("#DFAF00"))
        ));

        hudDayLabel = createHudLabel(
                DAY_TEXT_X,
                DAY_TEXT_Y,
                DAY_TEXT_WIDTH,
                DAY_TEXT_HEIGHT,
                Pos.CENTER,
                Font.font("Consolas", FontWeight.BLACK, 24),
                "#161616",
                false
        );
        hudMoneyLabel = createHudLabel(
                MONEY_TEXT_X,
                MONEY_TEXT_Y,
                MONEY_TEXT_WIDTH,
                MONEY_TEXT_HEIGHT,
                Pos.CENTER_LEFT,
                Font.font("Consolas", FontWeight.BOLD, 21),
                "#F8FAFC",
                true
        );
        hudStaminaLabel = createHudLabel(
                STAMINA_TEXT_X,
                STAMINA_TEXT_Y,
                STAMINA_TEXT_WIDTH,
                STAMINA_TEXT_HEIGHT,
                Pos.CENTER_LEFT,
                Font.font("Consolas", FontWeight.BOLD, 20),
                "#F8FAFC",
                true
        );

        pane.getChildren().addAll(
                imageView,
                dayMask,
                moneyMask,
                staminaTextMask,
                staminaEmptyMask,
                staminaFill,
                hudDayLabel,
                hudMoneyLabel,
                hudStaminaLabel
        );
        return pane;
    }

    private void setStatsVisible(boolean visible) {
        statsVisible = visible;
        statsPanel.setVisible(visible);
        statsPanel.setManaged(visible);
    }

    private Image loadHudImage() {
        return new Image(Objects.requireNonNull(
                Hud.class.getResource("/assets/ui/hud_variant_8.png"),
                "HUD image is missing"
        ).toExternalForm());
    }

    private Rectangle createMask(double x, double y, double width, double height, String fill) {
        Rectangle rectangle = new Rectangle(x, y, width, height);
        rectangle.setArcWidth(4);
        rectangle.setArcHeight(4);
        rectangle.setFill(Color.web(fill));
        rectangle.setMouseTransparent(true);
        return rectangle;
    }

    private Label createHudLabel(double x,
                                 double y,
                                 double width,
                                 double height,
                                 Pos alignment,
                                 Font font,
                                 String textColor,
                                 boolean applyShadow) {
        Label label = new Label();
        label.setLayoutX(x);
        label.setLayoutY(y);
        label.setPrefSize(width, height);
        label.setMinSize(width, height);
        label.setMaxSize(width, height);
        label.setAlignment(alignment);
        label.setPadding(new Insets(0, 2, 0, 2));
        label.setFont(font);
        label.setStyle("-fx-text-fill: " + textColor + ";");
        label.setMouseTransparent(true);
        if (applyShadow) {
            label.setEffect(new DropShadow(0.0, 1.0, 1.0, Color.rgb(0, 0, 0, 0.9)));
        }
        return label;
    }

    private Label createStatLabel() {
        Label label = new Label();
        label.setFont(Font.font("Segoe UI", 12));
        label.setStyle("-fx-text-fill: #E2E8F0;");
        return label;
    }
}
