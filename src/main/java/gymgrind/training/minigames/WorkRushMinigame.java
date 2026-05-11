package gymgrind.training.minigames;

import gymgrind.training.MinigameResult;
import gymgrind.training.TrainingGrade;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Random;
import java.util.function.Consumer;

public final class WorkRushMinigame extends VBox {

    private static final double GAME_SECONDS = 15.0;
    private static final double ANSWER_SECONDS = 1.25;
    private static final WorkOrder[] ORDERS = {
            new WorkOrder(KeyCode.DIGIT1, KeyCode.NUMPAD1, "1", "протеин"),
            new WorkOrder(KeyCode.DIGIT2, KeyCode.NUMPAD2, "2", "креатин"),
            new WorkOrder(KeyCode.DIGIT3, KeyCode.NUMPAD3, "3", "энергетик")
    };

    private final Consumer<MinigameResult> onFinish;
    private final Label title;
    private final Label orderLabel;
    private final Label infoLabel;
    private final AnimationTimer timer;
    private final Random random;

    private WorkOrder currentOrder;
    private double elapsedSeconds;
    private double orderSeconds;
    private double flashSeconds;
    private double score;
    private int hits;
    private int mistakes;
    private long lastFrameNanos;
    private boolean finished;

    public WorkRushMinigame(Consumer<MinigameResult> onFinish) {
        this.onFinish = onFinish;
        this.random = new Random();
        this.score = 50;

        setAlignment(Pos.CENTER);
        setSpacing(14);
        setPadding(new Insets(28));
        setMaxWidth(720);
        setFocusTraversable(true);
        setStyle(panelStyle(false));

        title = new Label("Работа: быстро выдавайте добавки клиентам");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #F8FAFC;");

        orderLabel = new Label();
        orderLabel.setFont(Font.font("Segoe UI", FontWeight.BLACK, 34));
        orderLabel.setStyle("-fx-text-fill: #F8D66D;");

        infoLabel = new Label();
        infoLabel.setFont(Font.font("Segoe UI", 16));
        infoLabel.setStyle("-fx-text-fill: #CBD5E1;");

        Label controls = new Label("1 - протеин | 2 - креатин | 3 - энергетик");
        controls.setFont(Font.font("Segoe UI", 15));
        controls.setStyle("-fx-text-fill: #94A3B8;");

        getChildren().addAll(title, orderLabel, controls, infoLabel);
        setOnKeyPressed(event -> handleInput(event.getCode()));

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                tick(now);
            }
        };

        nextOrder();
        draw();
        timer.start();
    }

    private void tick(long now) {
        if (lastFrameNanos == 0L) {
            lastFrameNanos = now;
            return;
        }

        double deltaSeconds = (now - lastFrameNanos) / 1_000_000_000.0;
        lastFrameNanos = now;
        elapsedSeconds += deltaSeconds;
        orderSeconds += deltaSeconds;
        flashSeconds = Math.max(0, flashSeconds - deltaSeconds);

        if (orderSeconds >= ANSWER_SECONDS) {
            losePoints(8);
            mistakes++;
            nextOrder();
        }

        draw();
        if (elapsedSeconds >= GAME_SECONDS) {
            finish();
        }
    }

    private void handleInput(KeyCode keyCode) {
        if (finished || !isOrderKey(keyCode)) {
            return;
        }

        if (currentOrder.matches(keyCode)) {
            score = clamp(score + 8, 0, 100);
            hits++;
        } else {
            losePoints(12);
            mistakes++;
        }

        nextOrder();
        draw();
    }

    private void losePoints(int amount) {
        score = clamp(score - amount, 0, 100);
        flashSeconds = 0.18;
    }

    private void nextOrder() {
        currentOrder = ORDERS[random.nextInt(ORDERS.length)];
        orderSeconds = 0;
    }

    private void draw() {
        boolean danger = flashSeconds > 0;
        setTranslateX(danger ? Math.sin(elapsedSeconds * 90) * 8 : 0);
        setStyle(panelStyle(danger));

        orderLabel.setText("Клиент просит: " + currentOrder.name() + "  →  нажмите " + currentOrder.label());
        orderLabel.setStyle("-fx-text-fill: " + (danger ? "#F87171" : "#F8D66D") + ";");
        infoLabel.setText(
                "Очки: " + Math.round(score)
                        + " | Верно: " + hits
                        + " | Ошибки: " + mistakes
                        + " | Время: " + Math.max(0, Math.ceil(GAME_SECONDS - elapsedSeconds))
        );
    }

    private void finish() {
        if (finished) {
            return;
        }

        finished = true;
        timer.stop();

        TrainingGrade grade;
        if (score >= 85) {
            grade = TrainingGrade.EXCELLENT;
        } else if (score >= 58) {
            grade = TrainingGrade.NORMAL;
        } else {
            grade = TrainingGrade.FAIL;
        }

        onFinish.accept(new MinigameResult(
                grade,
                "Очки работы: " + Math.round(score) + ", верно: " + hits + ", ошибок: " + mistakes + "."
        ));
    }

    private boolean isOrderKey(KeyCode keyCode) {
        return keyCode == KeyCode.DIGIT1
                || keyCode == KeyCode.DIGIT2
                || keyCode == KeyCode.DIGIT3
                || keyCode == KeyCode.NUMPAD1
                || keyCode == KeyCode.NUMPAD2
                || keyCode == KeyCode.NUMPAD3;
    }

    private String panelStyle(boolean danger) {
        return "-fx-background-color: " + (danger ? "rgba(55, 18, 28, 0.96)" : "rgba(8, 15, 23, 0.95)") + ";"
                + "-fx-background-radius: 18;"
                + "-fx-border-color: " + (danger ? "#F87171" : "#7FDBA4") + ";"
                + "-fx-border-radius: 18;"
                + "-fx-border-width: 2;";
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private record WorkOrder(KeyCode digit, KeyCode numpad, String label, String name) {

        private boolean matches(KeyCode keyCode) {
            return keyCode == digit || keyCode == numpad;
        }
    }
}
