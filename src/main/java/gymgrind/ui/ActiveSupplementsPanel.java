package gymgrind.ui;

import gymgrind.player.Player;
import gymgrind.shop.SupplementType;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public final class ActiveSupplementsPanel extends VBox {

    private final Label listLabel;

    public ActiveSupplementsPanel() {
        setSpacing(8);
        setPadding(new Insets(12));
        setMaxWidth(370);
        setStyle("-fx-background-color: rgba(8, 15, 23, 0.92);"
                + "-fx-background-radius: 16;"
                + "-fx-border-color: #7FDBA4;"
                + "-fx-border-radius: 16;"
                + "-fx-border-width: 1.5;");

        Label title = new Label("Активные добавки");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setStyle("-fx-text-fill: #F8FAFC;");

        listLabel = new Label();
        listLabel.setWrapText(true);
        listLabel.setFont(Font.font("Segoe UI", 12));
        listLabel.setStyle("-fx-text-fill: #CBD5E1;");

        getChildren().addAll(title, listLabel);
    }

    public void update(Player player) {
        if (player.activeSupplements().activeTypes().isEmpty()) {
            listLabel.setText("Нет активных добавок.");
            listLabel.setStyle("-fx-text-fill: #94A3B8;");
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (SupplementType type : player.activeSupplements().activeTypes()) {
            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            builder.append("- ")
                    .append(type.label())
                    .append(": ")
                    .append(type.effect());
        }
        listLabel.setText(builder.toString());
        listLabel.setStyle("-fx-text-fill: #E2E8F0;");
    }
}
