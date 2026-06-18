package gymgrind.ui;

import gymgrind.achievements.AchievementType;
import gymgrind.game.CalendarState;
import gymgrind.game.GameState;
import gymgrind.player.Player;
import gymgrind.player.Stats;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Set;

public final class Hud extends VBox {

    private static final double SUMMARY_HEIGHT = 62;
    private static final double SUMMARY_WIDTH = 530;
    private static final double DETAILS_WIDTH = 350;
    private static final double DETAILS_MAX_HEIGHT = 470;

    private final Label dayValueLabel;
    private final Label moneyValueLabel;
    private final Label energyValueLabel;
    private final ToggleButton statsButton;
    private final ToggleButton achievementsButton;
    private final VBox statsPanel;
    private final VBox achievementsPanel;
    private final Label achievementsProgressLabel;
    private final VBox achievementsList;
    private boolean compactMode;
    private final StatRow strengthRow;
    private final StatRow muscleRow;
    private final StatRow staminaRow;
    private final StatRow availableStaminaRow;
    private final StatRow fatigueRow;
    private final StatRow bodyFatRow;
    private final StatRow formRow;

    public Hud() {
        setSpacing(8);
        setAlignment(Pos.TOP_RIGHT);
        setFillWidth(false);
        setPickOnBounds(false);
        setMaxWidth(Region.USE_PREF_SIZE);

        dayValueLabel = createSummaryValueLabel();
        moneyValueLabel = createSummaryValueLabel();
        energyValueLabel = createSummaryValueLabel();

        HBox summaryBar = new HBox(8,
                createSummaryChip("День", dayValueLabel, "#F6B94D"),
                createSummaryChip("Деньги", moneyValueLabel, "#7FDBA4"),
                createSummaryChip("Энергия", energyValueLabel, "#F8D66D")
        );
        summaryBar.setAlignment(Pos.CENTER_RIGHT);
        summaryBar.setPrefSize(SUMMARY_WIDTH, SUMMARY_HEIGHT);
        summaryBar.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        summaryBar.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        summaryBar.setPadding(new Insets(8));
        summaryBar.setStyle(hudFrameStyle());
        summaryBar.setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.48)));

        statsButton = createHudButton("Характеристики");
        achievementsButton = createHudButton("Достижения");
        Tooltip.install(statsButton, new Tooltip("Показать характеристики"));
        Tooltip.install(achievementsButton, new Tooltip("Показать достижения"));
        statsButton.setOnAction(event -> showPanel(statsButton.isSelected(), false));
        achievementsButton.setOnAction(event -> showPanel(false, achievementsButton.isSelected()));

        HBox buttonRow = new HBox(8, statsButton, achievementsButton);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        strengthRow = createStatRow("Сила", "#F6B94D");
        muscleRow = createStatRow("Масса", "#7DD3FC");
        staminaRow = createStatRow("Выносливость", "#7FDBA4");
        availableStaminaRow = createStatRow("Энергия", "#F8D66D");
        fatigueRow = createStatRow("Усталость", "#F97316");
        bodyFatRow = createStatRow("Жир", "#FDBA74");
        formRow = createStatRow("Форма", "#86EFAC");

        statsPanel = createDetailsPanel("Характеристики");
        statsPanel.getChildren().addAll(
                strengthRow.root(),
                muscleRow.root(),
                staminaRow.root(),
                availableStaminaRow.root(),
                fatigueRow.root(),
                bodyFatRow.root(),
                formRow.root()
        );

        achievementsProgressLabel = createStatLabel();
        achievementsProgressLabel.setStyle("-fx-text-fill: #F8D66D;");
        achievementsList = new VBox(6);
        achievementsList.setFillWidth(true);

        ScrollPane achievementsScroll = new ScrollPane(achievementsList);
        achievementsScroll.setFitToWidth(true);
        achievementsScroll.setMaxHeight(390);
        achievementsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        achievementsScroll.setStyle("-fx-background: transparent;"
                + "-fx-background-color: transparent;"
                + "-fx-control-inner-background: transparent;"
                + "-fx-padding: 0;");

        achievementsPanel = createDetailsPanel("Достижения");
        achievementsPanel.getChildren().addAll(achievementsProgressLabel, achievementsScroll);

        getChildren().addAll(summaryBar, buttonRow, statsPanel, achievementsPanel);
        showPanel(false, false);
    }

    public void update(Player player,
                       GameState gameState,
                       CalendarState calendarState,
                       Set<AchievementType> completedAchievements) {
        boolean visible = gameState != GameState.MENU && gameState != GameState.COMPETITION_INTRO;
        setVisible(visible);
        setManaged(visible);
        if (!visible) {
            return;
        }

        Stats stats = player.stats();
        int availableStamina = stats.availableStamina();
        int maxAvailableStamina = stats.maxAvailableStamina();

        applyValueText(dayValueLabel, Integer.toString(calendarState.currentDay()), 23, 17);
        applyValueText(moneyValueLabel, Integer.toString(stats.money()), 23, 15);
        applyValueText(energyValueLabel, availableStamina + "/" + maxAvailableStamina, 21, 14);

        updateStatRow(strengthRow, Integer.toString(stats.strength()), stats.strength(), 500);
        updateStatRow(muscleRow, Integer.toString(stats.muscle()), stats.muscle(), 500);
        updateStatRow(staminaRow, Integer.toString(stats.stamina()), stats.stamina(), 500);
        updateStatRow(availableStaminaRow, availableStamina + "/" + maxAvailableStamina, availableStamina, maxAvailableStamina);
        updateStatRow(fatigueRow, stats.fatigue() + "%", stats.fatigue(), 100);
        updateStatRow(bodyFatRow, stats.bodyFatPercent() + "%", stats.bodyFatPercent(), 50);
        updateStatRow(formRow, Integer.toString(stats.form()), stats.form(), 150);

        updateAchievements(completedAchievements);
    }

    public void setCompactMode(boolean compactMode) {
        if (this.compactMode == compactMode) {
            return;
        }

        this.compactMode = compactMode;
        if (compactMode) {
            showPanel(false, false);
            setScaleX(0.78);
            setScaleY(0.78);
            setTranslateX(54);
            setTranslateY(-14);
            setOpacity(0.94);
        } else {
            setScaleX(1.0);
            setScaleY(1.0);
            setTranslateX(0);
            setTranslateY(0);
            setOpacity(1.0);
        }
    }

    private void updateAchievements(Set<AchievementType> completedAchievements) {
        achievementsList.getChildren().clear();

        int completedCount = completedAchievements == null ? 0 : completedAchievements.size();
        achievementsProgressLabel.setText("Выполнено: " + completedCount + " / " + AchievementType.values().length);

        for (AchievementType achievement : AchievementType.values()) {
            boolean completed = completedAchievements != null && completedAchievements.contains(achievement);
            achievementsList.getChildren().add(createAchievementRow(achievement, completed));
        }
    }

    private VBox createAchievementRow(AchievementType achievement, boolean completed) {
        VBox row = new VBox(2);
        row.setPadding(new Insets(7, 9, 7, 9));
        row.setStyle(completed
                ? "-fx-background-color: rgba(127, 219, 164, 0.16); -fx-background-radius: 8;"
                : "-fx-background-color: rgba(248, 229, 204, 0.07); -fx-background-radius: 8;");

        Label title = new Label((completed ? "[OK] " : "[ ] ") + achievement.displayTitle());
        title.setWrapText(true);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        title.setStyle(completed ? "-fx-text-fill: #9EF6BE;" : "-fx-text-fill: #F8E5CC;");

        Label bonus = new Label("Бонус: " + achievement.bonus().description());
        bonus.setWrapText(true);
        bonus.setFont(Font.font("Segoe UI", 11));
        bonus.setStyle("-fx-text-fill: #F8D66D;");

        row.getChildren().addAll(title, bonus);
        return row;
    }

    private void showPanel(boolean statsVisible, boolean achievementsVisible) {
        statsButton.setSelected(statsVisible);
        achievementsButton.setSelected(achievementsVisible);
        statsPanel.setVisible(statsVisible);
        statsPanel.setManaged(statsVisible);
        achievementsPanel.setVisible(achievementsVisible);
        achievementsPanel.setManaged(achievementsVisible);
    }

    private HBox createSummaryChip(String title, Label value, String accentColor) {
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        titleLabel.setStyle("-fx-text-fill: " + accentColor + ";");

        VBox text = new VBox(1, titleLabel, value);
        text.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(text, Priority.ALWAYS);

        HBox chip = new HBox(text);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPrefSize(164, 46);
        chip.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        chip.setPadding(new Insets(5, 10, 5, 10));
        chip.setStyle("-fx-background-color: rgba(24, 17, 12, 0.92);"
                + "-fx-background-radius: 10;"
                + "-fx-border-color: rgba(246, 185, 77, 0.45);"
                + "-fx-border-radius: 10;"
                + "-fx-border-width: 1.2;");
        return chip;
    }

    private Label createSummaryValueLabel() {
        Label label = new Label();
        label.setAlignment(Pos.CENTER_LEFT);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-text-fill: #F8FAFC;");
        label.setEffect(new DropShadow(0.0, 1.0, 1.0, Color.rgb(0, 0, 0, 0.95)));
        return label;
    }

    private ToggleButton createHudButton(String text) {
        ToggleButton button = new ToggleButton(text);
        button.setCursor(Cursor.HAND);
        button.setFocusTraversable(false);
        button.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) {
                event.consume();
            }
        });
        button.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.SPACE) {
                event.consume();
            }
        });
        button.setPrefWidth(154);
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        button.setStyle("-fx-background-color: rgba(24, 17, 12, 0.94);"
                + "-fx-text-fill: #F8E5CC;"
                + "-fx-background-radius: 9;"
                + "-fx-border-color: rgba(246, 185, 77, 0.70);"
                + "-fx-border-radius: 9;"
                + "-fx-border-width: 1.3;"
                + "-fx-padding: 7 10 7 10;");
        return button;
    }

    private VBox createDetailsPanel(String titleText) {
        Label title = new Label(titleText);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        title.setStyle("-fx-text-fill: #F8E5CC;");

        VBox panel = new VBox(7, title);
        panel.setAlignment(Pos.TOP_LEFT);
        panel.setPrefWidth(DETAILS_WIDTH);
        panel.setMaxWidth(DETAILS_WIDTH);
        panel.setMaxHeight(DETAILS_MAX_HEIGHT);
        panel.setPadding(new Insets(12));
        panel.setStyle(hudFrameStyle());
        panel.setEffect(new DropShadow(16, Color.rgb(0, 0, 0, 0.45)));
        return panel;
    }

    private void applyValueText(Label label, String text, double baseFontSize, double minimumFontSize) {
        label.setText(text);
        label.setFont(Font.font("Consolas", FontWeight.BLACK, fittedFontSize(text, baseFontSize, minimumFontSize)));
    }

    private double fittedFontSize(String text, double baseFontSize, double minimumFontSize) {
        int length = text.length();
        if (length <= 3) {
            return baseFontSize;
        }
        if (length <= 5) {
            return Math.max(minimumFontSize, baseFontSize - 3);
        }
        if (length <= 7) {
            return Math.max(minimumFontSize, baseFontSize - 6);
        }
        return minimumFontSize;
    }

    private Label createStatLabel() {
        Label label = new Label();
        label.setWrapText(true);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        label.setStyle("-fx-text-fill: #E2E8F0;");
        return label;
    }

    private StatRow createStatRow(String titleText, String fillColor) {
        Label title = new Label(titleText);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        title.setStyle("-fx-text-fill: #F8E5CC;");

        Label value = new Label();
        value.setFont(Font.font("Consolas", FontWeight.BLACK, 15));
        value.setStyle("-fx-text-fill: #FFFFFF;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(8, title, spacer, value);
        header.setAlignment(Pos.CENTER_LEFT);

        Region track = new Region();
        track.setPrefSize(300, 8);
        track.setMinSize(300, 8);
        track.setMaxSize(300, 8);
        track.setStyle("-fx-background-color: rgba(248, 229, 204, 0.16);"
                + "-fx-background-radius: 99;"
                + "-fx-border-color: rgba(246, 185, 77, 0.25);"
                + "-fx-border-radius: 99;"
                + "-fx-border-width: 1;");

        Region fill = new Region();
        fill.setPrefSize(0, 8);
        fill.setMinHeight(8);
        fill.setMaxHeight(8);
        fill.setStyle("-fx-background-color: " + fillColor + ";"
                + "-fx-background-radius: 99;");

        HBox bar = new HBox(fill);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPrefSize(300, 8);
        bar.setMinSize(300, 8);
        bar.setMaxSize(300, 8);
        bar.setStyle(track.getStyle());

        VBox root = new VBox(5, header, bar);
        root.setPadding(new Insets(7, 9, 8, 9));
        root.setStyle("-fx-background-color: rgba(248, 229, 204, 0.075);"
                + "-fx-background-radius: 9;");
        return new StatRow(root, value, fill);
    }

    private void updateStatRow(StatRow row, String valueText, double value, double maxValue) {
        row.value().setText(valueText);
        double ratio = maxValue <= 0 ? 0 : Math.max(0, Math.min(1, value / maxValue));
        row.fill().setPrefWidth(300 * ratio);
    }

    private String hudFrameStyle() {
        return "-fx-background-color: linear-gradient(to bottom, rgba(48, 27, 13, 0.96), rgba(20, 13, 8, 0.96));"
                + "-fx-background-radius: 12;"
                + "-fx-border-color: #B76B2A;"
                + "-fx-border-radius: 12;"
                + "-fx-border-width: 2;";
    }

    private record StatRow(VBox root, Label value, Region fill) {}
}

