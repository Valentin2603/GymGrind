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
        setSpacing(6);
        setPadding(new Insets(9));
        setMaxWidth(300);
        setStyle(hudPanelStyle());

        Label title = new Label("Активные добавки");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setStyle("-fx-text-fill: #F8E5CC;");

        listLabel = new Label();
        listLabel.setWrapText(true);
        listLabel.setFont(Font.font("Segoe UI", 10.5));
        listLabel.setStyle("-fx-text-fill: #CBD5E1;");

        getChildren().addAll(title, listLabel);
    }

    public void update(Player player) {
        if (player.activeSupplements().activeTypes().isEmpty()) {
            if (player.hasPurchasedSupplement(SupplementType.RECOVERY_SHOT)) {
                listLabel.setText("- Шприц прогресса: постоянный буст к статам, усталости и мини-играм.");
                listLabel.setStyle("-fx-text-fill: #F8E5CC;");
                return;
            }
            listLabel.setText("Нет активных добавок.");
            listLabel.setStyle("-fx-text-fill: #D2B48C;");
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
        if (player.hasPurchasedSupplement(SupplementType.RECOVERY_SHOT)) {
            builder.append('\n').append("- Шприц прогресса: постоянный буст.");
        }
        listLabel.setText(builder.toString());
        listLabel.setStyle("-fx-text-fill: #F8E5CC;");
    }

    private String hudPanelStyle() {
        return "-fx-background-color: linear-gradient(to bottom, rgba(48, 27, 13, 0.96), rgba(20, 13, 8, 0.96));"
                + "-fx-background-radius: 12;"
                + "-fx-border-color: #B76B2A;"
                + "-fx-border-radius: 12;"
                + "-fx-border-width: 2;";
    }
}
