package gymgrind.minigames;

import gymgrind.model.MinigameResult;
import gymgrind.model.TrainingGrade;
import gymgrind.model.TrainingSession;
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

import java.util.function.Consumer;

public final class PowerMeterMinigame extends VBox {

    private static final double WIDTH = 680;
    private static final double HEIGHT = 170;
    private static final double GAME_SECONDS = 15.0;

    private final TrainingSession session;
    private final Consumer<MinigameResult> onFinish;
    private final Canvas canvas;
    private final AnimationTimer timer;
    private final double zoneWidth;
    private final double drainSpeed;
    private final double pushPower;

    private double marker;
    private double zoneCenter;
    private double score;
    private double elapsedSeconds;
    private double flashSeconds;
    private long lastFrameNanos;
    private boolean finished;

    public PowerMeterMinigame(TrainingSession session, Consumer<MinigameResult> onFinish) {
        this.session = session;
        this.onFinish = onFinish;
        this.canvas = new Canvas(WIDTH, HEIGHT);
        this.zoneWidth = clamp(0.22 * session.tuning().zoneMultiplier(), 0.08, 0.28);
        this.drainSpeed = 0.34 * session.tuning().speedMultiplier();
        this.pushPower = 0.085 / Math.sqrt(session.weight().speedMultiplier());
        this.marker = 0.42;
        this.zoneCenter = 0.64;
        this.score = 45;

        setAlignment(Pos.CENTER);
        setSpacing(14);
        setPadding(new Insets(28));
        setMaxWidth(780);
        setFocusTraversable(true);
        setStyle("-fx-background-color: rgba(8, 15, 23, 0.95);"
                + "-fx-background-radius: 18;"
                + "-fx-border-color: #7FDBA4;"
                + "-fx-border-radius: 18;"
                + "-fx-border-width: 2;");

        getChildren().add(canvas);
        setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                pushMarker();
            }
        });

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                tick(now);
            }
        };

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
        flashSeconds = Math.max(0, flashSeconds - deltaSeconds);

        zoneCenter = 0.62 + Math.sin(elapsedSeconds * 1.15 * session.tuning().speedMultiplier()) * 0.08;
        marker = clamp(marker - drainSpeed * deltaSeconds, 0.0, 1.0);

        if (isInZone()) {
            score += 20 * deltaSeconds;
        } else {
            score -= 24 * deltaSeconds;
            flashSeconds = Math.max(flashSeconds, 0.08);
        }
        score = clamp(score, 0, 100);

        draw();

        if (elapsedSeconds >= GAME_SECONDS) {
            finish();
        }
    }

    private void pushMarker() {
        marker = clamp(marker + pushPower, 0.0, 1.0);
    }

    private void draw() {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.setFill(flashSeconds > 0 ? Color.web("#30131A") : Color.web("#0F172A"));
        graphics.fillRoundRect(0, 0, WIDTH, HEIGHT, 18, 18);

        graphics.setFill(Color.web("#F8FAFC"));
        graphics.setFont(Font.font("Segoe UI", FontWeight.BOLD, 21));
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.fillText("Становая тяга: жмите Space и держите силу в зелёной зоне", WIDTH / 2, 30);

        double barX = 70;
        double barY = 72;
        double barWidth = WIDTH - 140;
        double barHeight = 28;

        graphics.setFill(Color.web("#334155"));
        graphics.fillRoundRect(barX, barY, barWidth, barHeight, 14, 14);

        double zoneX = barX + barWidth * (zoneCenter - zoneWidth / 2);
        double zonePixels = barWidth * zoneWidth;
        graphics.setFill(Color.web("#22C55E"));
        graphics.fillRoundRect(zoneX, barY, zonePixels, barHeight, 14, 14);

        double markerX = barX + barWidth * marker;
        graphics.setFill(isInZone() ? Color.web("#FFFFFF") : Color.web("#F87171"));
        graphics.fillRoundRect(markerX - 6, barY - 18, 12, barHeight + 36, 6, 6);

        graphics.setFill(Color.web("#F8D66D"));
        graphics.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        graphics.fillText("Очки: " + Math.round(score), WIDTH / 2, 128);

        graphics.setFill(Color.web("#94A3B8"));
        graphics.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        graphics.fillText(
                "Время: " + Math.max(0, Math.ceil(GAME_SECONDS - elapsedSeconds))
                        + " | Вес: " + session.weight().label()
                        + " | Усталость: " + session.tuning().fatigueProfile().label()
                        + preWorkoutText(),
                WIDTH / 2,
                153
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
        } else if (score >= 60) {
            grade = TrainingGrade.NORMAL;
        } else {
            grade = TrainingGrade.FAIL;
        }

        onFinish.accept(new MinigameResult(grade, "Очки становой: " + Math.round(score) + "."));
    }

    private boolean isInZone() {
        return Math.abs(marker - zoneCenter) <= zoneWidth / 2.0;
    }

    private String preWorkoutText() {
        return session.tuning().preWorkoutUsed() ? " | Предтрен сработал" : "";
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
