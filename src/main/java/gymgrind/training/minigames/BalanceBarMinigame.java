package gymgrind.training.minigames;

import gymgrind.training.MinigameResult;
import gymgrind.training.TrainingGrade;
import gymgrind.training.TrainingSession;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public final class BalanceBarMinigame extends VBox {

    private static final double WIDTH = 720;
    private static final double HEIGHT = 210;
    private static final double GAME_SECONDS = 13.0;
    private static final double DANGER_LIMIT = 0.74;

    private final TrainingSession session;
    private final Consumer<MinigameResult> onFinish;
    private final Canvas canvas;
    private final AnimationTimer timer;
    private final double driftPower;
    private final double controlPower;
    private final double safeZone;

    private double barPosition;
    private double barVelocity;
    private double driftDirection;
    private double driftChangeSeconds;
    private double score;
    private double elapsedSeconds;
    private double stableSeconds;
    private double instabilitySum;
    private double flashSeconds;
    private long lastFrameNanos;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean forcedFail;
    private boolean finished;

    public BalanceBarMinigame(TrainingSession session, Consumer<MinigameResult> onFinish) {
        this.session = session;
        this.onFinish = onFinish;
        this.canvas = new Canvas(WIDTH, HEIGHT);
        this.driftPower = 0.86
                * session.tuning().speedMultiplier()
                * (1.0 - session.tuning().muscleBonus() * 0.06)
                * (1.0 + session.tuning().bodyFatLoad() * 0.05);
        this.controlPower = 0.72
                / Math.sqrt(session.weight().speedMultiplier())
                * (1.0 + session.tuning().strengthBonus() * 0.14 + session.tuning().muscleBonus() * 0.04)
                * (1.0 - session.tuning().bodyFatLoad() * 0.04);
        this.safeZone = clamp(
                0.22 * session.tuning().zoneMultiplier() * (1.0 + session.tuning().strengthBonus() * 0.04 - session.tuning().bodyFatLoad() * 0.02),
                0.07,
                0.27
        );
        this.barPosition = ThreadLocalRandom.current().nextDouble(-0.18, 0.18);
        this.score = 48;
        this.driftDirection = randomDrift();

        setAlignment(Pos.CENTER);
        setSpacing(14);
        setPadding(new Insets(28));
        setMaxWidth(820);
        setFocusTraversable(true);
        setStyle("-fx-background-color: rgba(8, 15, 23, 0.95);"
                + "-fx-background-radius: 18;"
                + "-fx-border-color: #7FDBA4;"
                + "-fx-border-radius: 18;"
                + "-fx-border-width: 2;");

        getChildren().add(canvas);
        setOnKeyPressed(event -> handleKey(event.getCode(), true));
        setOnKeyReleased(event -> handleKey(event.getCode(), false));

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                tick(now);
            }
        };

        draw();
        timer.start();
    }

    private void handleKey(KeyCode keyCode, boolean pressed) {
        if (keyCode == KeyCode.A || keyCode == KeyCode.LEFT) {
            leftPressed = pressed;
        }
        if (keyCode == KeyCode.D || keyCode == KeyCode.RIGHT) {
            rightPressed = pressed;
        }
    }

    private void tick(long now) {
        if (lastFrameNanos == 0L) {
            lastFrameNanos = now;
            return;
        }

        double deltaSeconds = (now - lastFrameNanos) / 1_000_000_000.0;
        lastFrameNanos = now;
        elapsedSeconds += deltaSeconds;
        flashSeconds = Math.max(0, flashSeconds - deltaSeconds);
        driftChangeSeconds -= deltaSeconds;

        if (driftChangeSeconds <= 0) {
            driftDirection = randomDrift();
            driftChangeSeconds = ThreadLocalRandom.current().nextDouble(0.20, 0.45);
        }

        double control = 0.0;
        if (leftPressed) {
            control -= controlPower;
        }
        if (rightPressed) {
            control += controlPower;
        }

        double waveDrift = Math.sin(elapsedSeconds * 3.35 * session.tuning().speedMultiplier()) * driftPower * 0.62
                + Math.sin(elapsedSeconds * 6.4) * driftPower * 0.22;
        barVelocity += (driftDirection + waveDrift + control) * deltaSeconds;
        barVelocity *= Math.pow(0.45, deltaSeconds);
        barPosition = clamp(barPosition + barVelocity * deltaSeconds, -1.05, 1.05);

        double distance = Math.abs(barPosition);
        instabilitySum += distance * deltaSeconds;
        if (distance <= safeZone) {
            stableSeconds += deltaSeconds;
            score += 3.1 * deltaSeconds;
        } else {
            score -= 44 * deltaSeconds * session.tuning().speedMultiplier();
            flashSeconds = Math.max(flashSeconds, 0.08);
        }

        score = clamp(score, 0, 100);
        if (distance >= DANGER_LIMIT) {
            forcedFail = true;
            finish();
            return;
        }

        draw();
        if (elapsedSeconds >= GAME_SECONDS) {
            finish();
        }
    }

    private void draw() {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.setFill(flashSeconds > 0 ? Color.web("#32131A") : Color.web("#0F172A"));
        graphics.fillRoundRect(0, 0, WIDTH, HEIGHT, 18, 18);

        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setFill(Color.web("#F8FAFC"));
        graphics.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        graphics.fillText("Жим лёжа: удерживайте штангу в центре", WIDTH / 2, 32);

        graphics.setFill(Color.web("#CBD5E1"));
        graphics.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        graphics.fillText("A/← тянет влево, D/→ тянет вправо. Не выпускайте маркер за красные края.", WIDTH / 2, 56);

        double barX = 76;
        double barY = 94;
        double barWidth = WIDTH - 152;
        double barHeight = 30;
        double centerX = barX + barWidth / 2.0;
        double safePixels = safeZone * barWidth / 2.0;
        double markerX = centerX + barPosition * barWidth / 2.0;

        graphics.setFill(Color.web("#334155"));
        graphics.fillRoundRect(barX, barY, barWidth, barHeight, 15, 15);

        graphics.setFill(Color.web("#7F1D1D"));
        graphics.fillRoundRect(barX, barY, barWidth * 0.04, barHeight, 15, 15);
        graphics.fillRoundRect(barX + barWidth * 0.96, barY, barWidth * 0.04, barHeight, 15, 15);

        graphics.setFill(Color.web("#22C55E"));
        graphics.fillRoundRect(centerX - safePixels, barY, safePixels * 2, barHeight, 15, 15);

        graphics.setStroke(Color.web("#F8D66D"));
        graphics.setLineWidth(3);
        graphics.strokeLine(centerX, barY - 16, centerX, barY + barHeight + 16);

        graphics.setFill(Math.abs(barPosition) <= safeZone ? Color.web("#FFFFFF") : Color.web("#F87171"));
        graphics.fillRoundRect(markerX - 8, barY - 20, 16, barHeight + 40, 8, 8);

        graphics.setFill(Color.web("#F8D66D"));
        graphics.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        graphics.fillText("Стабильность: " + Math.round(score), WIDTH / 2, 154);

        graphics.setFill(Color.web("#94A3B8"));
        graphics.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        graphics.fillText(
                "Время: " + Math.max(0, Math.ceil(GAME_SECONDS - elapsedSeconds))
                        + " | Нагрузка: " + session.weightLabel()
                        + " | Усталость: " + session.tuning().fatigueProfile().label()
                        + preWorkoutText(),
                WIDTH / 2,
                184
        );
    }

    private void finish() {
        if (finished) {
            return;
        }

        finished = true;
        timer.stop();

        double averageInstability = elapsedSeconds <= 0 ? 1.0 : instabilitySum / elapsedSeconds;
        TrainingGrade grade;
        if (forcedFail || score < 55) {
            grade = TrainingGrade.FAIL;
        } else if (score >= 84 && averageInstability <= safeZone * 0.34 && stableSeconds >= GAME_SECONDS * 0.78) {
            grade = TrainingGrade.EXCELLENT;
        } else {
            grade = TrainingGrade.NORMAL;
        }

        String details = forcedFail
                ? "Штанга ушла за безопасный предел."
                : "Стабильность жима: " + Math.round(score) + ".";
        onFinish.accept(new MinigameResult(grade, details));
    }

    private double randomDrift() {
        return ThreadLocalRandom.current().nextDouble(-driftPower, driftPower);
    }

    private String preWorkoutText() {
        return session.tuning().preWorkoutUsed() ? " | Предтрен сработал" : "";
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
