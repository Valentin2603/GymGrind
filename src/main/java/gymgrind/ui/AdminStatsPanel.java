package gymgrind.ui;

import gymgrind.game.CalendarState;
import gymgrind.player.Player;
import gymgrind.player.Stats;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.function.Function;

public final class AdminStatsPanel extends VBox {

    private final Player player;
    private final CalendarState calendarState;
    private final TextField strengthField;
    private final TextField muscleField;
    private final TextField staminaField;
    private final TextField fatigueField;
    private final TextField moneyField;
    private final TextField bodyFatField;
    private final TextField dayField;
    private final Label feedback;
    private final Label currentForm;

    public AdminStatsPanel(Player player,
                           CalendarState calendarState,
                           Function<AdminStatValues, String> onApply,
                           Function<AdminStatValues, String> onApplyAndWake,
                           Runnable onClose) {
        this.player = player;
        this.calendarState = calendarState;
        this.strengthField = createField();
        this.muscleField = createField();
        this.staminaField = createField();
        this.fatigueField = createField();
        this.moneyField = createField();
        this.bodyFatField = createField();
        this.dayField = createField();
        this.feedback = new Label("F10 открывает эту панель. Для смены формы нажмите «Применить и сон».");
        this.currentForm = new Label();

        setAlignment(Pos.CENTER);
        setSpacing(14);
        setMaxWidth(620);
        setPadding(new Insets(26));
        setFocusTraversable(true);
        setStyle("-fx-background-color: rgba(7, 12, 20, 0.97);"
                + "-fx-background-radius: 22;"
                + "-fx-border-color: #38BDF8;"
                + "-fx-border-radius: 22;"
                + "-fx-border-width: 2;");

        Label title = new Label("Админ-панель для тестов");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setStyle("-fx-text-fill: #F8FAFC;");

        Label subtitle = new Label("Быстро меняет статы для показа. Сон проверяет условия новой формы персонажа.");
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setStyle("-fx-text-fill: #CBD5E1;");

        currentForm.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        currentForm.setStyle("-fx-text-fill: #7DD3FC;");

        GridPane grid = createGrid();
        addRow(grid, 0, "Сила", strengthField);
        addRow(grid, 1, "Масса", muscleField);
        addRow(grid, 2, "Выносливость", staminaField);
        addRow(grid, 3, "Усталость", fatigueField);
        addRow(grid, 4, "Деньги", moneyField);
        addRow(grid, 5, "Жир %", bodyFatField);
        addRow(grid, 6, "День", dayField);

        HBox quickStats = new HBox(8,
                quickButton("+25 сила", () -> addInt(strengthField, 25)),
                quickButton("+25 масса", () -> addInt(muscleField, 25)),
                quickButton("+20 вын.", () -> addInt(staminaField, 20)),
                quickButton("-5% жир", () -> addDouble(bodyFatField, -5.0)),
                quickButton("+500 денег", () -> addInt(moneyField, 500))
        );
        quickStats.setAlignment(Pos.CENTER);

        Button apply = createButton("Применить", "#22C55E");
        Button applyAndWake = createButton("Применить и сон", "#F59E0B");
        Button close = createButton("Закрыть", "#475569");

        apply.setOnAction(event -> applyValues(onApply));
        applyAndWake.setOnAction(event -> applyValues(onApplyAndWake));
        close.setOnAction(event -> onClose.run());

        HBox buttons = new HBox(10, apply, applyAndWake, close);
        buttons.setAlignment(Pos.CENTER);

        feedback.setWrapText(true);
        feedback.setAlignment(Pos.CENTER);
        feedback.setFont(Font.font("Segoe UI", 13));
        feedback.setStyle("-fx-text-fill: #F8D66D;");

        refreshFields();
        getChildren().addAll(title, subtitle, currentForm, grid, quickStats, buttons, feedback);
    }

    private void applyValues(Function<AdminStatValues, String> action) {
        try {
            String message = action.apply(readValues());
            refreshFields();
            feedback.setText(message);
        } catch (NumberFormatException exception) {
            feedback.setText("Проверьте числа в полях. Жир можно писать дробным, например 18.5.");
        }
    }

    private AdminStatValues readValues() {
        return new AdminStatValues(
                parseInt(strengthField),
                parseInt(muscleField),
                parseInt(staminaField),
                parseInt(fatigueField),
                parseInt(moneyField),
                parseDouble(bodyFatField),
                parseInt(dayField)
        );
    }

    private void refreshFields() {
        Stats stats = player.stats();
        strengthField.setText(Integer.toString(stats.strength()));
        muscleField.setText(Integer.toString(stats.muscle()));
        staminaField.setText(Integer.toString(stats.stamina()));
        fatigueField.setText(Integer.toString(stats.fatigue()));
        moneyField.setText(Integer.toString(stats.money()));
        bodyFatField.setText(String.format(java.util.Locale.US, "%.1f", stats.bodyFat()));
        dayField.setText(Integer.toString(calendarState.currentDay()));
        currentForm.setText("Персонаж: " + player.profile().displayName()
                + " | форма: " + player.currentForm().displayName()
                + " | рейтинг: " + stats.form());
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        return grid;
    }

    private void addRow(GridPane grid, int row, String labelText, TextField field) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        label.setStyle("-fx-text-fill: #E2E8F0;");
        grid.add(label, 0, row);
        grid.add(field, 1, row);
    }

    private TextField createField() {
        TextField field = new TextField();
        field.setPrefWidth(170);
        field.setStyle("-fx-background-color: #111827;"
                + "-fx-background-radius: 10;"
                + "-fx-border-color: #334155;"
                + "-fx-border-radius: 10;"
                + "-fx-text-fill: #F8FAFC;"
                + "-fx-prompt-text-fill: #64748B;");
        return field;
    }

    private Button quickButton(String text, Runnable action) {
        Button button = createButton(text, "#0EA5E9");
        button.setOnAction(event -> action.run());
        return button;
    }

    private Button createButton(String text, String color) {
        Button button = new Button(text);
        button.setMinWidth(112);
        button.setCursor(javafx.scene.Cursor.HAND);
        button.setStyle("-fx-background-color: " + color + ";"
                + "-fx-background-radius: 12;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-padding: 10 14;");
        return button;
    }

    private void addInt(TextField field, int delta) {
        field.setText(Integer.toString(Math.max(0, parseInt(field) + delta)));
    }

    private void addDouble(TextField field, double delta) {
        double value = Math.max(8.0, parseDouble(field) + delta);
        field.setText(String.format(java.util.Locale.US, "%.1f", value));
    }

    private int parseInt(TextField field) {
        return Integer.parseInt(field.getText().trim());
    }

    private double parseDouble(TextField field) {
        return Double.parseDouble(field.getText().trim().replace(',', '.'));
    }
}
