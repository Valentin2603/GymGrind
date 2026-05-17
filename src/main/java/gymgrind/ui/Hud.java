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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Objects;

public final class Hud extends VBox {

    private static final double HUD_SCALE = 0.62;
    private static final double HUD_WIDTH = 760 * HUD_SCALE;
    private static final double HUD_HEIGHT = 410 * HUD_SCALE;

    private static final double DAY_TEXT_X = 506 * HUD_SCALE;
    private static final double DAY_TEXT_Y = 84 * HUD_SCALE;
    private static final double DAY_TEXT_WIDTH = 118 * HUD_SCALE;
    private static final double DAY_TEXT_HEIGHT = 64 * HUD_SCALE;

    private static final double MONEY_TEXT_X = 506 * HUD_SCALE;
    private static final double MONEY_TEXT_Y = 188 * HUD_SCALE;
    private static final double MONEY_TEXT_WIDTH = 118 * HUD_SCALE;
    private static final double MONEY_TEXT_HEIGHT = 64 * HUD_SCALE;

    private static final double ENERGY_TEXT_X = 506 * HUD_SCALE;
    private static final double ENERGY_TEXT_Y = 292 * HUD_SCALE;
    private static final double ENERGY_TEXT_WIDTH = 118 * HUD_SCALE;
    private static final double ENERGY_TEXT_HEIGHT = 64 * HUD_SCALE;

    private static final double DAY_FONT_SIZE = 52 * HUD_SCALE;
    private static final double MONEY_FONT_SIZE = 50 * HUD_SCALE;
    private static final double ENERGY_FONT_SIZE = 48 * HUD_SCALE;

    private Label hudDayLabel;
    private Label hudMoneyLabel;
    private Label hudEnergyLabel;
    private final VBox statsPanel;
    private final Label dayLabel;
    private final Label strengthLabel;
    private final Label muscleLabel;
    private final Label staminaLabel;
    private final Label availableStaminaLabel;
    private final Label fatigueLabel;
    private final Label formLabel;
    private boolean statsVisible;
    private boolean compactMode;

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

        applyValueText(hudDayLabel, String.format("%02d", calendarState.currentDay()), DAY_FONT_SIZE, 22);
        applyValueText(hudMoneyLabel, Integer.toString(stats.money()), MONEY_FONT_SIZE, 18);
        applyValueText(hudEnergyLabel, availableStamina + "/" + maxAvailableStamina, ENERGY_FONT_SIZE, 16);

        dayLabel.setText("День: " + calendarState.currentDay() + "/" + calendarState.maxDays());
        strengthLabel.setText("Сила: " + stats.strength());
        muscleLabel.setText("Масса: " + stats.muscle());
        staminaLabel.setText("Стат выносливости: " + stats.stamina());
        availableStaminaLabel.setText("Текущая стамина: " + availableStamina + "/" + maxAvailableStamina);
        fatigueLabel.setText("Усталость: " + stats.fatigue());
        formLabel.setText("Форма: " + stats.form());
    }

    public void setCompactMode(boolean compactMode) {
        if (this.compactMode == compactMode) {
            return;
        }

        this.compactMode = compactMode;
        if (compactMode) {
            setStatsVisible(false);
            setScaleX(0.55);
            setScaleY(0.55);
            setTranslateX(HUD_WIDTH * 0.22);
            setTranslateY(-HUD_HEIGHT * 0.22);
            setOpacity(0.92);
        } else {
            setScaleX(1.0);
            setScaleY(1.0);
            setTranslateX(0);
            setTranslateY(0);
            setOpacity(1.0);
        }
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

        hudDayLabel = createHudLabel(
                DAY_TEXT_X,
                DAY_TEXT_Y,
                DAY_TEXT_WIDTH,
                DAY_TEXT_HEIGHT
        );
        hudMoneyLabel = createHudLabel(
                MONEY_TEXT_X,
                MONEY_TEXT_Y,
                MONEY_TEXT_WIDTH,
                MONEY_TEXT_HEIGHT
        );
        hudEnergyLabel = createHudLabel(
                ENERGY_TEXT_X,
                ENERGY_TEXT_Y,
                ENERGY_TEXT_WIDTH,
                ENERGY_TEXT_HEIGHT
        );

        pane.getChildren().addAll(imageView, hudDayLabel, hudMoneyLabel, hudEnergyLabel);
        return pane;
    }

    private void applyValueText(Label label, String text, double baseFontSize, double minimumFontSize) {
        label.setText(text);
        label.setFont(Font.font("Consolas", FontWeight.BLACK, fittedFontSize(text, baseFontSize, minimumFontSize)));
    }

    private double fittedFontSize(String text, double baseFontSize, double minimumFontSize) {
        int length = text.length();
        if (length <= 2) {
            return baseFontSize;
        }
        if (length <= 4) {
            return Math.max(minimumFontSize, baseFontSize - 4);
        }
        if (length <= 6) {
            return Math.max(minimumFontSize, baseFontSize - 10);
        }
        return minimumFontSize;
    }

    private void setStatsVisible(boolean visible) {
        statsVisible = visible;
        statsPanel.setVisible(visible);
        statsPanel.setManaged(visible);
    }

    private Image loadHudImage() {
        return new Image(Objects.requireNonNull(
                Hud.class.getResource("/assets/ui/hud_dynamic_fields/hud_template_empty_values.png"),
                "HUD image is missing"
        ).toExternalForm());
    }

    private Label createHudLabel(double x, double y, double width, double height) {
        Label label = new Label();
        label.setLayoutX(x);
        label.setLayoutY(y);
        label.setPrefSize(width, height);
        label.setMinSize(width, height);
        label.setMaxSize(width, height);
        label.setAlignment(Pos.CENTER_LEFT);
        label.setPadding(new Insets(0, 0, 0, 5 * HUD_SCALE));
        label.setStyle("-fx-text-fill: #F8FAFC;");
        label.setMouseTransparent(true);
        label.setEffect(new DropShadow(0.0, 1.0, 1.0, Color.rgb(0, 0, 0, 0.95)));
        return label;
    }

    private Label createStatLabel() {
        Label label = new Label();
        label.setFont(Font.font("Segoe UI", 12));
        label.setStyle("-fx-text-fill: #E2E8F0;");
        return label;
    }
}
