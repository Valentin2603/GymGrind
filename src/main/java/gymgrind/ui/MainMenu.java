package gymgrind.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public final class MainMenu extends VBox {

    private final Button startButton;
    private final Button exitButton;

    public MainMenu() {
        setSpacing(16);
        setAlignment(Pos.CENTER);
        setMaxWidth(460);
        setPadding(new Insets(28));
        setStyle("-fx-background-color: rgba(8, 15, 23, 0.90);"
                + "-fx-background-radius: 24;"
                + "-fx-border-color: #7FDBA4;"
                + "-fx-border-radius: 24;"
                + "-fx-border-width: 2;");

        Label title = new Label("Путь к сцене");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 28));
        title.setStyle("-fx-text-fill: #F8FAFC;");

        Label subtitle = new Label("Симулятор качка\nДвижение, карта, границы, меню и интерактивные зоны.");
        subtitle.setFont(Font.font("Segoe UI", 15));
        subtitle.setStyle("-fx-text-fill: #CBD5E1;");
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);

        Label controls = new Label("Управление: WASD или стрелки - движение, E - действие, Esc - меню");
        controls.setFont(Font.font("Segoe UI", 14));
        controls.setStyle("-fx-text-fill: #94A3B8;");
        controls.setWrapText(true);

        startButton = new Button("Начать");
        startButton.setPrefWidth(220);
        startButton.setStyle(buttonStyle("#22C55E"));

        exitButton = new Button("Выход");
        exitButton.setPrefWidth(220);
        exitButton.setStyle(buttonStyle("#EF4444"));

        getChildren().addAll(title, subtitle, controls, startButton, exitButton);
    }

    public void setOnStart(Runnable action) {
        startButton.setOnAction(event -> action.run());
    }

    public void setOnExit(Runnable action) {
        exitButton.setOnAction(event -> action.run());
    }

    private String buttonStyle(String accentColor) {
        return "-fx-background-color: " + accentColor + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 16px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 14;"
                + "-fx-padding: 12 18 12 18;";
    }
}
