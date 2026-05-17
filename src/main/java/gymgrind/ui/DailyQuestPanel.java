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
        setSpacing(8);
        setPadding(new Insets(12));
        setMaxWidth(370);
        setPrefHeight(315);
        setMaxHeight(330);
        setStyle("-fx-background-color: rgba(8, 15, 23, 0.92);"
                + "-fx-background-radius: 16;"
                + "-fx-border-color: #7FDBA4;"
                + "-fx-border-radius: 16;"
                + "-fx-border-width: 1.5;");

        Label title = new Label("Ежедневные цели");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setStyle("-fx-text-fill: #F8FAFC;");

        questList = new VBox(6);
        questList.setMaxHeight(245);

        notificationLabel = new Label();
        notificationLabel.setWrapText(true);
        notificationLabel.setVisible(false);
        notificationLabel.setManaged(false);
        notificationLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
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
            row.setPadding(new Insets(6, 8, 6, 8));
            row.setStyle(quest.completed()
                    ? "-fx-background-color: rgba(34, 197, 94, 0.12); -fx-background-radius: 10;"
                    : "-fx-background-color: rgba(148, 163, 184, 0.08); -fx-background-radius: 10;");

            Label title = new Label((quest.completed() ? "[OK] " : "- ")
                    + quest.title()
                    + " — "
                    + quest.progressText());
            title.setWrapText(true);
            title.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
            title.setStyle(quest.completed()
                    ? "-fx-text-fill: #86EFAC;"
                    : "-fx-text-fill: #E2E8F0;");

            Label bonus = new Label(quest.bonusText());
            bonus.setWrapText(true);
            bonus.setFont(Font.font("Segoe UI", 11));
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
}
