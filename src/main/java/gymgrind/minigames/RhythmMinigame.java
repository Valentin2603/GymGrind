package gymgrind.minigames;

import gymgrind.model.MinigameResult;
import gymgrind.model.TrainingGrade;
import gymgrind.model.TrainingSession;
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

public final class RhythmMinigame extends VBox {

    private static final KeyCode[] KEYS = {KeyCode.W, KeyCode.A, KeyCode.S, KeyCode.D};
    private static final double GAME_SECONDS = 15.0;

    private final TrainingSession session;
    private final Consumer<MinigameResult> onFinish;
    private final Label title;
    private final Label keyLabel;
    private final Label infoLabel;
    private final AnimationTimer timer;
    private final Random random;
    private final double answerTimeSeconds;

    private KeyCode currentKey;
    private double elapsedSeconds;
    private double keyElapsedSeconds;
    private double score;
    private int hits;
    private int mistakes;
    private double flashSeconds;
    private long lastFrameNanos;
    private boolean finished;

    public RhythmMinigame(TrainingSession session, Consumer<MinigameResult> onFinish) {
        this.session = session;
        this.onFinish = onFinish;
        this.random = new Random();
        this.answerTimeSeconds = clamp(1.25 / session.tuning().speedMultiplier(), 0.45, 1.35);
        this.score = 50;

        setAlignment(Pos.CENTER);
        setSpacing(14);
        setPadding(new Insets(28));
        setMaxWidth(760);
        setFocusTraversable(true);
        setStyle("-fx-background-color: rgba(8, 15, 23, 0.95);"
                + "-fx-background-radius: 18;"
                + "-fx-border-color: #7FDBA4;"
                + "-fx-border-radius: 18;"
                + "-fx-border-width: 2;");

        title = new Label("Беговая дорожка: нажимайте правильные клавиши WASD");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #F8FAFC;");

        keyLabel = new Label();
        keyLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 70));
        keyLabel.setStyle("-fx-text-fill: #F8FAFC;");

        infoLabel = new Label();
        infoLabel.setFont(Font.font("Segoe UI", 16));
        infoLabel.setStyle("-fx-text-fill: #CBD5E1;");

        getChildren().addAll(title, keyLabel, infoLabel);
        setOnKeyPressed(event -> handleInput(event.getCode()));

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                tick(now);
            }
        };

        nextKey();
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
        keyElapsedSeconds += deltaSeconds;
        flashSeconds = Math.max(0, flashSeconds - deltaSeconds);

        if (keyElapsedSeconds >= answerTimeSeconds) {
            losePoints(8);
            mistakes++;
            nextKey();
        }

        draw();

        if (elapsedSeconds >= GAME_SECONDS) {
            finish();
        }
    }

    private void handleInput(KeyCode keyCode) {
        if (finished || !isRhythmKey(keyCode)) {
            return;
        }

        if (keyCode == currentKey) {
            score = clamp(score + 7, 0, 100);
            hits++;
            nextKey();
        } else {
            losePoints(10);
            mistakes++;
            nextKey();
        }

        draw();
    }

    private void losePoints(int amount) {
        score = clamp(score - amount, 0, 100);
        flashSeconds = 0.18;
        setTranslateX(flashSeconds > 0 ? -8 : 0);
    }

    private void nextKey() {
        currentKey = KEYS[random.nextInt(KEYS.length)];
        keyElapsedSeconds = 0;
        setTranslateX(0);
    }

    private void draw() {
        double progress = clamp(keyElapsedSeconds / answerTimeSeconds, 0, 1);
        String color = flashSeconds > 0 ? "#F87171" : progress > 0.72 ? "#F8D66D" : "#F8FAFC";
        setTranslateX(flashSeconds > 0 ? Math.sin(elapsedSeconds * 80) * 8 : 0);
        setStyle("-fx-background-color: " + (flashSeconds > 0 ? "rgba(55, 18, 28, 0.96)" : "rgba(8, 15, 23, 0.95)") + ";"
                + "-fx-background-radius: 18;"
                + "-fx-border-color: " + (flashSeconds > 0 ? "#F87171" : "#7FDBA4") + ";"
                + "-fx-border-radius: 18;"
                + "-fx-border-width: 2;");

        keyLabel.setText(currentKey.getName());
        keyLabel.setStyle("-fx-text-fill: " + color + ";");

        infoLabel.setText(
                "Очки: " + Math.round(score)
                        + " | Попадания: " + hits
                        + " | Ошибки: " + mistakes
                        + " | Время: " + Math.max(0, Math.ceil(GAME_SECONDS - elapsedSeconds))
                        + " | Вес: " + session.weight().label()
                        + " | Усталость: " + session.tuning().fatigueProfile().label()
                        + preWorkoutText()
        );
    }

    private void finish() {
        if (finished) {
            return;
        }

        finished = true;
        timer.stop();

        TrainingGrade grade;
        if (score >= 75) {
            grade = TrainingGrade.EXCELLENT;
        } else if (score >= 45) {
            grade = TrainingGrade.NORMAL;
        } else {
            grade = TrainingGrade.FAIL;
        }

        onFinish.accept(new MinigameResult(
                grade,
                "Очки бега: " + Math.round(score) + ", попаданий: " + hits + ", ошибок: " + mistakes + "."
        ));
    }

    private boolean isRhythmKey(KeyCode keyCode) {
        return keyCode == KeyCode.W || keyCode == KeyCode.A || keyCode == KeyCode.S || keyCode == KeyCode.D;
    }

    private String preWorkoutText() {
        return session.tuning().preWorkoutUsed() ? " | Предтрен сработал" : "";
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
