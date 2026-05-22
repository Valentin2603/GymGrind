package gymgrind.ui;

import gymgrind.daily.DailyQuestNotification;
import gymgrind.daily.DailyQuestView;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.List;

public final class DailyQuestPanel extends VBox {

    private final VBox questList;
    private final Label notificationLabel;

    public DailyQuestPanel() {
        setSpacing(6);
        setPadding(new Insets(9));
        setMaxWidth(300);
        setPrefHeight(252);
        setMaxHeight(270);
        setStyle(hudPanelStyle());

        Label title = new Label("Ежедневные цели");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setStyle("-fx-text-fill: #F8E5CC;");

        questList = new VBox(4);
        questList.setMaxHeight(198);

        notificationLabel = new Label();
        notificationLabel.setWrapText(true);
        notificationLabel.setVisible(false);
        notificationLabel.setManaged(false);
        notificationLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        notificationLabel.setStyle("-fx-text-fill: #0F172A;"
                + "-fx-background-color: #7FDBA4;"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 7 9 7 9;");

        getChildren().addAll(title, questList, notificationLabel);
    }

    public void update(List<DailyQuestView> quests) {
        questList.getChildren().clear();
        for (DailyQuestView quest : quests) {
            VBox row = new VBox(1);
            row.setPadding(new Insets(5, 7, 5, 7));
            row.setStyle(quest.completed()
                    ? "-fx-background-color: rgba(127, 219, 164, 0.16); -fx-background-radius: 9;"
                    : "-fx-background-color: rgba(248, 229, 204, 0.075); -fx-background-radius: 9;");

            Label title = new Label((quest.completed() ? "[OK] " : "- ")
                    + quest.title()
                    + " — "
                    + quest.progressText());
            title.setWrapText(true);
            title.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 10.5));
            title.setStyle(quest.completed()
                    ? "-fx-text-fill: #9EF6BE;"
                    : "-fx-text-fill: #F8E5CC;");

            Label bonus = new Label(quest.bonusText());
            bonus.setWrapText(true);
            bonus.setFont(Font.font("Segoe UI", 10));
            bonus.setStyle("-fx-text-fill: #F8D66D;");

            row.getChildren().addAll(title, bonus);
            questList.getChildren().add(row);
        }
    }

    public void showCompletion(DailyQuestNotification notification) {
        notificationLabel.setText("Цель выполнена: " + notification.title()
                + "\nБонус: " + notification.bonusText());
        notificationLabel.setOpacity(0);
        notificationLabel.setScaleX(0.94);
        notificationLabel.setScaleY(0.94);
        notificationLabel.setVisible(true);
        notificationLabel.setManaged(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), notificationLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition pop = new ScaleTransition(Duration.millis(180), notificationLabel);
        pop.setFromX(0.94);
        pop.setFromY(0.94);
        pop.setToX(1.0);
        pop.setToY(1.0);

        PauseTransition pause = new PauseTransition(Duration.seconds(2.0));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(450), notificationLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> {
            notificationLabel.setVisible(false);
            notificationLabel.setManaged(false);
        });

        new SequentialTransition(fadeIn, pop, pause, fadeOut).play();
    }

    private String hudPanelStyle() {
        return "-fx-background-color: linear-gradient(to bottom, rgba(48, 27, 13, 0.96), rgba(20, 13, 8, 0.96));"
                + "-fx-background-radius: 12;"
                + "-fx-border-color: #B76B2A;"
                + "-fx-border-radius: 12;"
                + "-fx-border-width: 2;";
    }
}
