package gymgrind.ui;

import gymgrind.GameState;
import gymgrind.model.Player;
import gymgrind.model.Stats;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public final class Hud extends VBox {

    private final Label stateLabel;
    private final Label strengthLabel;
    private final Label muscleLabel;
    private final Label staminaLabel;
    private final Label fatigueLabel;
    private final Label moneyLabel;
    private final Label formLabel;

    public Hud() {
        setSpacing(8);
        setPadding(new Insets(18));
        setMaxWidth(230);
        setStyle("-fx-background-color: rgba(8, 15, 23, 0.88);"
                + "-fx-background-radius: 20;"
                + "-fx-border-color: #7FDBA4;"
                + "-fx-border-radius: 20;");

        Label title = new Label("Параметры");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 19));
        title.setStyle("-fx-text-fill: #F8FAFC;");

        stateLabel = createLabel();
        strengthLabel = createLabel();
        muscleLabel = createLabel();
        staminaLabel = createLabel();
        fatigueLabel = createLabel();
        moneyLabel = createLabel();
        formLabel = createLabel();

        getChildren().addAll(
                title,
                stateLabel,
                strengthLabel,
                muscleLabel,
                staminaLabel,
                fatigueLabel,
                moneyLabel,
                formLabel
        );
    }

    public void update(Player player, GameState gameState) {
        Stats stats = player.stats();
        stateLabel.setText("Режим: " + gameState.title());
        strengthLabel.setText("Сила: " + stats.strength());
        muscleLabel.setText("Масса: " + stats.muscle());
        staminaLabel.setText("Выносливость: " + stats.stamina());
        fatigueLabel.setText("Усталость: " + stats.fatigue());
        moneyLabel.setText("Деньги: " + stats.money());
        formLabel.setText("Форма: " + stats.form());
    }

    private Label createLabel() {
        Label label = new Label();
        label.setFont(Font.font("Segoe UI", 15));
        label.setStyle("-fx-text-fill: #E2E8F0;");
        return label;
    }
}
