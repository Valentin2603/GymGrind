package gymgrind.ui;

import gymgrind.game.CalendarState;
import gymgrind.game.GameState;
import gymgrind.player.Player;
import gymgrind.player.Stats;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public final class Hud extends VBox {

    private final Label currentDayLabel;
    private final Label moneyLabel;
    private final Label fatigueLabel;
    private final VBox statsPanel;
    private final Label dayLabel;
    private final Label strengthLabel;
    private final Label muscleLabel;
    private final Label staminaLabel;
    private final Label bodyFatLabel;
    private final Label formLabel;
    private boolean statsVisible;

    public Hud() {
        setSpacing(10);
        setAlignment(Pos.TOP_RIGHT);
        setFillWidth(false);
        setPickOnBounds(false);
        setMaxWidth(Region.USE_PREF_SIZE);

        currentDayLabel = createBadgeLabel();
        moneyLabel = createBadgeLabel();
        fatigueLabel = createBadgeLabel();

        VBox quickInfoBox = new VBox(8, currentDayLabel, moneyLabel, fatigueLabel);
        quickInfoBox.setAlignment(Pos.TOP_RIGHT);
        quickInfoBox.setFillWidth(false);

        Button statsButton = new Button("\uD83D\uDCAA");
        statsButton.setFocusTraversable(false);
        statsButton.setPrefSize(52, 52);
        statsButton.setMinSize(52, 52);
        statsButton.setStyle("-fx-background-color: rgba(8, 15, 23, 0.92);"
                + "-fx-text-fill: #F8FAFC;"
                + "-fx-font-size: 24px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 16;"
                + "-fx-border-color: #7FDBA4;"
                + "-fx-border-radius: 16;"
                + "-fx-border-width: 2;");
        statsButton.setTooltip(new Tooltip("Показать параметры"));
        statsButton.setOnAction(event -> setStatsVisible(!statsVisible));

        HBox headerRow = new HBox(10, quickInfoBox, statsButton);
        headerRow.setAlignment(Pos.TOP_RIGHT);

        Label title = new Label("Параметры");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setStyle("-fx-text-fill: #F8FAFC;");

        dayLabel = createStatLabel();
        strengthLabel = createStatLabel();
        muscleLabel = createStatLabel();
        staminaLabel = createStatLabel();
        bodyFatLabel = createStatLabel();
        formLabel = createStatLabel();

        statsPanel = new VBox(6, title, dayLabel, strengthLabel, muscleLabel, staminaLabel, bodyFatLabel, formLabel);
        statsPanel.setAlignment(Pos.TOP_LEFT);
        statsPanel.setMaxWidth(220);
        statsPanel.setPadding(new Insets(12));
        statsPanel.setStyle("-fx-background-color: rgba(8, 15, 23, 0.92);"
                + "-fx-background-radius: 16;"
                + "-fx-border-color: #7FDBA4;"
                + "-fx-border-radius: 16;"
                + "-fx-border-width: 1.5;");

        getChildren().addAll(headerRow, statsPanel);
        setStatsVisible(false);
    }

    public void update(Player player, GameState gameState, CalendarState calendarState) {
        Stats stats = player.stats();
        currentDayLabel.setText("День: " + calendarState.currentDay() + "/" + calendarState.maxDays());
        moneyLabel.setText("Деньги: " + stats.money());
        fatigueLabel.setText("Усталость: " + stats.fatigue());
        dayLabel.setText("День: " + calendarState.currentDay() + "/" + calendarState.maxDays());
        strengthLabel.setText("Сила: " + stats.strength());
        muscleLabel.setText("Масса: " + stats.muscle());
        staminaLabel.setText("Выносливость: " + stats.stamina());
        bodyFatLabel.setText("% жира: " + stats.bodyFat() + "%");
        formLabel.setText("Форма: " + stats.form());
    }

    private void setStatsVisible(boolean visible) {
        statsVisible = visible;
        statsPanel.setVisible(visible);
        statsPanel.setManaged(visible);
    }

    private Label createBadgeLabel() {
        Label label = new Label();
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        label.setPadding(new Insets(8, 12, 8, 12));
        label.setStyle("-fx-text-fill: #F8FAFC;"
                + "-fx-background-color: rgba(8, 15, 23, 0.92);"
                + "-fx-background-radius: 12;"
                + "-fx-border-color: rgba(125, 219, 164, 0.85);"
                + "-fx-border-radius: 12;"
                + "-fx-border-width: 1.2;");
        return label;
    }

    private Label createStatLabel() {
        Label label = new Label();
        label.setFont(Font.font("Segoe UI", 12));
        label.setStyle("-fx-text-fill: #E2E8F0;");
        return label;
    }
}
