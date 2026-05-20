package gymgrind.ui.tutorial;

import gymgrind.player.PlayerProfile;
import gymgrind.player.PlayerProfiles;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class TutorialOverlay extends StackPane {

    private static final double CARD_WIDTH = 1120;
    private static final double CARD_HEIGHT = 660;
    private static final double PROGRESS_WIDTH = 250;

    private final PlayerProfile selectedProfile;
    private final Runnable onFinish;
    private final Runnable onSkip;
    private final Runnable onCancel;
    private final List<TutorialSlide> slides;
    private final List<Animation> runningDecorations;
    private final List<Region> progressDots;

    private final StackPane mainCard;
    private final StackPane visualPane;
    private final Pane animationLayer;
    private final StackPane characterSlot;
    private final VBox textPane;
    private final Label eyebrowLabel;
    private final Label titleLabel;
    private final Label descriptionLabel;
    private final Label profileBadge;
    private final VBox bulletsBox;
    private final Label progressLabel;
    private final Region progressFill;
    private final Button backButton;
    private final Button nextButton;
    private final Button skipButton;
    private final Button closeButton;

    private int currentIndex;
    private int slideDirection;
    private boolean animating;

    public TutorialOverlay(PlayerProfile selectedProfile, Runnable onFinish, Runnable onSkip, Runnable onCancel) {
        this.selectedProfile = selectedProfile == null ? PlayerProfiles.defaultProfile() : selectedProfile;
        this.onFinish = onFinish;
        this.onSkip = onSkip;
        this.onCancel = onCancel;
        this.slides = TutorialSlidesFactory.create(this.selectedProfile);
        this.runningDecorations = new ArrayList<>();
        this.progressDots = new ArrayList<>();
        this.slideDirection = -1;

        setFocusTraversable(true);
        setPickOnBounds(true);
        setStyle("-fx-background-color: rgba(2, 6, 23, 0.78);");

        Region dimGradient = new Region();
        dimGradient.setStyle("-fx-background-color: radial-gradient(center 50% 46%, radius 78%, rgba(34, 211, 238, 0.14), rgba(2, 6, 23, 0.88));");
        dimGradient.prefWidthProperty().bind(widthProperty());
        dimGradient.prefHeightProperty().bind(heightProperty());

        mainCard = new StackPane();
        mainCard.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        mainCard.setMaxSize(CARD_WIDTH, CARD_HEIGHT);
        mainCard.setPadding(new Insets(16));
        mainCard.setStyle("-fx-background-color: transparent;");

        visualPane = new StackPane();
        visualPane.setPrefSize(500, 630);
        visualPane.setMaxSize(500, 630);
        visualPane.setStyle("-fx-background-color: radial-gradient(center 56% 48%, radius 64%, rgba(34, 211, 238, 0.20), rgba(15, 23, 42, 0.0));");

        animationLayer = new Pane();
        animationLayer.setMouseTransparent(true);
        animationLayer.setPrefSize(500, 630);
        animationLayer.setMaxSize(500, 630);

        characterSlot = new StackPane();
        characterSlot.setPrefSize(430, 580);
        characterSlot.setMaxSize(430, 580);
        StackPane.setAlignment(characterSlot, Pos.CENTER);
        characterSlot.setTranslateX(38);

        Label visualHint = new Label("Туториал можно пропустить — все механики доступны в игре.");
        visualHint.setWrapText(true);
        visualHint.setMaxWidth(360);
        visualHint.setAlignment(Pos.CENTER);
        visualHint.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        visualHint.setStyle("-fx-text-fill: rgba(226, 232, 240, 0.72);"
                + "-fx-background-color: rgba(2, 6, 23, 0.52);"
                + "-fx-background-radius: 16;"
                + "-fx-padding: 9 12 9 12;");
        StackPane.setAlignment(visualHint, Pos.BOTTOM_CENTER);
        StackPane.setMargin(visualHint, new Insets(0, 26, 14, 26));

        visualPane.getChildren().addAll(animationLayer, characterSlot, visualHint);

        textPane = new VBox(12);
        textPane.setAlignment(Pos.TOP_LEFT);
        textPane.setPrefSize(700, 610);
        textPane.setMaxSize(700, 610);
        textPane.setPadding(new Insets(26, 32, 22, 32));
        textPane.setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(15, 23, 42, 0.98), rgba(8, 13, 24, 0.98));"
                + "-fx-background-radius: 30;"
                + "-fx-border-color: rgba(125, 211, 252, 0.72);"
                + "-fx-border-radius: 30;"
                + "-fx-border-width: 1.8;");

        eyebrowLabel = new Label();
        eyebrowLabel.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 14));
        eyebrowLabel.setStyle("-fx-text-fill: #67E8F9; -fx-letter-spacing: 0.08em;");
        eyebrowLabel.setMinHeight(Region.USE_PREF_SIZE);

        titleLabel = new Label();
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(636);
        titleLabel.setMinHeight(Region.USE_PREF_SIZE);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 29));
        titleLabel.setStyle("-fx-text-fill: #F8FAFC;");

        descriptionLabel = new Label();
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(636);
        descriptionLabel.setMinHeight(Region.USE_PREF_SIZE);
        descriptionLabel.setFont(Font.font("Segoe UI", 15));
        descriptionLabel.setStyle("-fx-text-fill: #CBD5E1; -fx-line-spacing: 3px;");

        profileBadge = new Label();
        profileBadge.setWrapText(true);
        profileBadge.setMaxWidth(636);
        profileBadge.setMinHeight(Region.USE_PREF_SIZE);
        profileBadge.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        profileBadge.setStyle("-fx-text-fill: #DCFCE7;"
                + "-fx-background-color: rgba(34, 197, 94, 0.14);"
                + "-fx-background-radius: 14;"
                + "-fx-border-color: rgba(34, 197, 94, 0.34);"
                + "-fx-border-radius: 14;"
                + "-fx-padding: 10 12 10 12;");

        bulletsBox = new VBox(8);
        bulletsBox.setFillWidth(true);
        bulletsBox.setMinHeight(Region.USE_PREF_SIZE);

        HBox dotsRow = new HBox(7);
        dotsRow.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < slides.size(); i++) {
            Region dot = new Region();
            dot.setPrefSize(10, 10);
            dot.setMinSize(10, 10);
            dot.setMaxSize(10, 10);
            dot.setStyle("-fx-background-radius: 99;");
            progressDots.add(dot);
            dotsRow.getChildren().add(dot);
        }

        progressLabel = new Label();
        progressLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        progressLabel.setStyle("-fx-text-fill: #94A3B8;");

        StackPane progressTrack = new StackPane();
        progressTrack.setAlignment(Pos.CENTER_LEFT);
        progressTrack.setPrefSize(PROGRESS_WIDTH, 8);
        progressTrack.setMaxSize(PROGRESS_WIDTH, 8);
        Region track = new Region();
        track.setPrefSize(PROGRESS_WIDTH, 8);
        track.setMaxSize(PROGRESS_WIDTH, 8);
        track.setStyle("-fx-background-color: rgba(148, 163, 184, 0.20); -fx-background-radius: 99;");
        progressFill = new Region();
        progressFill.setPrefSize(0, 8);
        progressFill.setMaxHeight(8);
        progressFill.setStyle("-fx-background-color: linear-gradient(to right, #22C55E, #22D3EE); -fx-background-radius: 99;");
        progressTrack.getChildren().addAll(track, progressFill);

        VBox progressBlock = new VBox(8, progressLabel, progressTrack, dotsRow);
        progressBlock.setAlignment(Pos.CENTER_LEFT);
        progressBlock.setMinHeight(Region.USE_PREF_SIZE);

        backButton = createNeonButton("Назад", "#475569");
        nextButton = createNeonButton("Далее", "#22C55E");
        skipButton = createNeonButton("Пропустить", "#0EA5E9");

        backButton.setOnAction(event -> goToSlide(currentIndex - 1));
        nextButton.setOnAction(event -> {
            if (currentIndex == slides.size() - 1) {
                finishTutorial();
            } else {
                goToSlide(currentIndex + 1);
            }
        });
        skipButton.setOnAction(event -> skipTutorial());

        HBox buttons = new HBox(12, backButton, skipButton, nextButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        spacer.setMinHeight(4);

        textPane.getChildren().addAll(
                eyebrowLabel,
                titleLabel,
                descriptionLabel,
                profileBadge,
                bulletsBox,
                spacer,
                progressBlock,
                buttons
        );

        closeButton = createCloseButton();
        closeButton.setOnAction(event -> cancelTutorial());
        StackPane.setAlignment(closeButton, Pos.TOP_RIGHT);
        StackPane.setMargin(closeButton, new Insets(12));

        mainCard.getChildren().addAll(visualPane, textPane, closeButton);
        StackPane.setAlignment(visualPane, Pos.CENTER_RIGHT);
        StackPane.setMargin(visualPane, new Insets(0, 4, 0, 0));
        StackPane.setAlignment(textPane, Pos.CENTER_LEFT);
        StackPane.setMargin(textPane, new Insets(0, 0, 0, 22));

        getChildren().addAll(dimGradient, mainCard);
        StackPane.setAlignment(mainCard, Pos.CENTER);

        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                cancelTutorial();
            }
        });

        updateSlide();
        Platform.runLater(() -> {
            requestFocus();
            animateSlideIn();
        });
    }

    private void goToSlide(int targetIndex) {
        if (animating || targetIndex < 0 || targetIndex >= slides.size() || targetIndex == currentIndex) {
            return;
        }

        slideDirection = targetIndex > currentIndex ? -1 : 1;
        animateSlideOutThen(() -> {
            currentIndex = targetIndex;
            updateSlide();
            animateSlideIn();
        });
    }

    private void finishTutorial() {
        stopDecorations();
        onFinish.run();
    }

    private void skipTutorial() {
        stopDecorations();
        onSkip.run();
    }

    private void cancelTutorial() {
        stopDecorations();
        onCancel.run();
    }

    private void animateSlideIn() {
        animating = true;
        mainCard.setOpacity(0);
        mainCard.setTranslateX(-slideDirection * 78);
        mainCard.setScaleX(0.985);
        mainCard.setScaleY(0.985);

        FadeTransition fade = new FadeTransition(Duration.millis(270), mainCard);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition move = new TranslateTransition(Duration.millis(300), mainCard);
        move.setToX(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), mainCard);
        scale.setToX(1);
        scale.setToY(1);

        ParallelTransition transition = new ParallelTransition(fade, move, scale);
        transition.setOnFinished(event -> animating = false);
        transition.play();
    }

    private void animateSlideOutThen(Runnable action) {
        animating = true;

        FadeTransition fade = new FadeTransition(Duration.millis(210), mainCard);
        fade.setToValue(0);

        TranslateTransition move = new TranslateTransition(Duration.millis(230), mainCard);
        move.setToX(slideDirection * 78);

        ScaleTransition scale = new ScaleTransition(Duration.millis(230), mainCard);
        scale.setToX(0.985);
        scale.setToY(0.985);

        ParallelTransition transition = new ParallelTransition(fade, move, scale);
        transition.setOnFinished(event -> action.run());
        transition.play();
    }

    private void updateSlide() {
        stopDecorations();

        TutorialSlide slide = slides.get(currentIndex);
        eyebrowLabel.setText(slide.eyebrow());
        titleLabel.setText(slide.title());
        descriptionLabel.setText(slide.description());
        profileBadge.setText("Выбран: " + selectedProfile.displayName()
                + "   |   Сила: " + selectedProfile.baseStrength()
                + "   Масса: " + selectedProfile.baseMuscle()
                + "   Выносл.: " + selectedProfile.baseStamina()
                + "   Жир: " + Math.round(selectedProfile.baseBodyFat()) + "%");

        bulletsBox.getChildren().clear();
        for (String bullet : slide.bullets()) {
            bulletsBox.getChildren().add(createBullet(bullet));
        }

        Image image = loadImageSafely(slide.imagePath());
        if (image == null) {
            characterSlot.getChildren().setAll(createFallbackCharacter());
        } else {
            ImageView characterImageView = new ImageView(image);
            characterImageView.setFitWidth(430);
            characterImageView.setFitHeight(610);
            characterImageView.setPreserveRatio(true);
            characterImageView.setSmooth(true);
            characterSlot.getChildren().setAll(characterImageView);
        }

        updateProgress();
        updateButtons();
        createAnimation(slide.animationType());
    }

    private Image loadImageSafely(String path) {
        URL resource = TutorialOverlay.class.getResource(path);
        if (resource == null) {
            return null;
        }

        Image image = new Image(resource.toExternalForm());
        return image.isError() ? null : image;
    }

    private Node createFallbackCharacter() {
        StackPane fallback = new StackPane();
        fallback.setPrefSize(390, 500);
        fallback.setMaxSize(390, 500);

        Circle glow = new Circle(118);
        glow.setFill(Color.web("#22D3EE", 0.13));
        glow.setStroke(Color.web("#22C55E", 0.55));
        glow.setStrokeWidth(3);

        Circle head = new Circle(46);
        head.setFill(Color.web("#38BDF8", 0.82));
        head.setTranslateY(-98);

        Rectangle body = new Rectangle(120, 170);
        body.setArcWidth(46);
        body.setArcHeight(46);
        body.setFill(Color.web("#22C55E", 0.72));
        body.setTranslateY(22);

        Label missing = new Label("ассет не найден");
        missing.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        missing.setStyle("-fx-text-fill: #E0F2FE;"
                + "-fx-background-color: rgba(2, 6, 23, 0.72);"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 8 12 8 12;");
        missing.setTranslateY(142);

        fallback.getChildren().addAll(glow, body, head, missing);
        return fallback;
    }

    private Button createNeonButton(String text, String accentColor) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setMinWidth(132);
        button.setPrefHeight(44);
        button.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 14));
        setButtonStyle(button, accentColor, false);

        button.setOnMouseEntered(event -> {
            if (!button.isDisabled()) {
                button.setScaleX(1.035);
                button.setScaleY(1.035);
                setButtonStyle(button, accentColor, true);
            }
        });
        button.setOnMouseExited(event -> {
            button.setScaleX(1);
            button.setScaleY(1);
            setButtonStyle(button, accentColor, false);
        });

        return button;
    }

    private Button createCloseButton() {
        Button button = createNeonButton("×", "#EF4444");
        button.setMinWidth(42);
        button.setPrefWidth(42);
        button.setPrefHeight(38);
        button.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 18));
        return button;
    }

    private void setButtonStyle(Button button, String accentColor, boolean hovered) {
        String background = hovered ? accentColor : "rgba(15, 23, 42, 0.88)";
        String border = hovered ? "#E0F2FE" : accentColor;
        button.setStyle("-fx-background-color: " + background + ";"
                + "-fx-text-fill: #F8FAFC;"
                + "-fx-background-radius: 14;"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-radius: 14;"
                + "-fx-border-width: 1.6;"
                + "-fx-padding: 10 16 10 16;");
    }

    private HBox createBullet(String text) {
        Circle marker = new Circle(4.5, Color.web("#22C55E"));
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(604);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        label.setStyle("-fx-text-fill: #E2E8F0;");

        HBox row = new HBox(10, marker, label);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(0, 0, 0, 2));
        return row;
    }

    private void updateProgress() {
        int slideNumber = currentIndex + 1;
        progressLabel.setText("Прогресс: " + slideNumber + "/" + slides.size());
        progressFill.setPrefWidth(PROGRESS_WIDTH * slideNumber / slides.size());

        for (int i = 0; i < progressDots.size(); i++) {
            boolean active = i <= currentIndex;
            progressDots.get(i).setStyle("-fx-background-radius: 99;"
                    + "-fx-background-color: " + (active ? "#22D3EE" : "rgba(148, 163, 184, 0.30)") + ";");
        }
    }

    private void updateButtons() {
        backButton.setDisable(currentIndex == 0);
        backButton.setOpacity(currentIndex == 0 ? 0.45 : 1.0);
        nextButton.setText(currentIndex == slides.size() - 1 ? "Начать путь" : "Далее");
    }

    private void createAnimation(TutorialAnimationType animationType) {
        resetAnimatedNodes();
        animationLayer.getChildren().clear();

        switch (animationType) {
            case IDLE_BOUNCE -> createIdleBounce();
            case TRAINING_PULSE -> createTrainingPulse();
            case SHOP_FLOAT -> createShopFloat();
            case REST_DAY_SWAP -> createRestDaySwap();
            case COACH_TALK -> createCoachTalk();
            case STAGE_SPOTLIGHT -> createStageSpotlight();
        }
    }

    private void createIdleBounce() {
        Circle aura = new Circle(138);
        aura.setCenterX(215);
        aura.setCenterY(260);
        aura.setFill(Color.web("#22D3EE", 0.08));
        aura.setStroke(Color.web("#22C55E", 0.42));
        aura.setStrokeWidth(2);
        animationLayer.getChildren().add(aura);

        TranslateTransition bounce = new TranslateTransition(Duration.seconds(1.5), characterSlot);
        bounce.setFromY(0);
        bounce.setToY(-14);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(Animation.INDEFINITE);
        playDecoration(bounce);
    }

    private void createTrainingPulse() {
        Circle pulse = new Circle(120);
        pulse.setCenterX(215);
        pulse.setCenterY(265);
        pulse.setFill(Color.TRANSPARENT);
        pulse.setStroke(Color.web("#22C55E", 0.70));
        pulse.setStrokeWidth(4);

        Region barTrack = new Region();
        barTrack.setLayoutX(105);
        barTrack.setLayoutY(440);
        barTrack.setPrefSize(220, 10);
        barTrack.setStyle("-fx-background-color: rgba(148, 163, 184, 0.22); -fx-background-radius: 99;");

        Region barFill = new Region();
        barFill.setLayoutX(105);
        barFill.setLayoutY(440);
        barFill.setPrefSize(55, 10);
        barFill.setStyle("-fx-background-color: linear-gradient(to right, #22C55E, #22D3EE); -fx-background-radius: 99;");

        animationLayer.getChildren().addAll(pulse, barTrack, barFill);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(1.05), pulse);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.26);
        scale.setToY(1.26);
        scale.setAutoReverse(true);
        scale.setCycleCount(Animation.INDEFINITE);

        FadeTransition fade = new FadeTransition(Duration.seconds(1.05), pulse);
        fade.setFromValue(0.86);
        fade.setToValue(0.18);
        fade.setAutoReverse(true);
        fade.setCycleCount(Animation.INDEFINITE);

        Timeline bar = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(barFill.prefWidthProperty(), 45)),
                new KeyFrame(Duration.seconds(0.72), new KeyValue(barFill.prefWidthProperty(), 220)),
                new KeyFrame(Duration.seconds(1.15), new KeyValue(barFill.prefWidthProperty(), 72))
        );
        bar.setCycleCount(Animation.INDEFINITE);

        playDecoration(new ParallelTransition(scale, fade));
        playDecoration(bar);
    }

    private void createShopFloat() {
        addFloatingBadge("$", 86, 108, "#FACC15", 0.0);
        addFloatingBadge("+", 306, 130, "#22C55E", 0.25);
        addFloatingBadge("BOOST", 250, 406, "#22D3EE", 0.5);

        TranslateTransition floatCharacter = new TranslateTransition(Duration.seconds(1.8), characterSlot);
        floatCharacter.setFromY(0);
        floatCharacter.setToY(-10);
        floatCharacter.setAutoReverse(true);
        floatCharacter.setCycleCount(Animation.INDEFINITE);
        playDecoration(floatCharacter);
    }

    private void createRestDaySwap() {
        Label dayBadge = new Label("День +1");
        dayBadge.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 18));
        dayBadge.setStyle("-fx-text-fill: #ECFEFF;"
                + "-fx-background-color: rgba(14, 165, 233, 0.82);"
                + "-fx-background-radius: 16;"
                + "-fx-padding: 9 16 9 16;");
        dayBadge.setLayoutX(34);
        dayBadge.setLayoutY(54);

        Circle recoveryGlow = new Circle(132);
        recoveryGlow.setCenterX(215);
        recoveryGlow.setCenterY(270);
        recoveryGlow.setFill(Color.web("#22C55E", 0.10));
        recoveryGlow.setStroke(Color.web("#A7F3D0", 0.42));
        recoveryGlow.setStrokeWidth(3);

        animationLayer.getChildren().addAll(recoveryGlow, dayBadge);

        Timeline glow = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(recoveryGlow.opacityProperty(), 0.35),
                        new KeyValue(dayBadge.translateYProperty(), 18),
                        new KeyValue(dayBadge.opacityProperty(), 0.3)),
                new KeyFrame(Duration.seconds(0.7),
                        new KeyValue(recoveryGlow.opacityProperty(), 1.0),
                        new KeyValue(dayBadge.translateYProperty(), 0),
                        new KeyValue(dayBadge.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(1.5),
                        new KeyValue(recoveryGlow.opacityProperty(), 0.42),
                        new KeyValue(dayBadge.translateYProperty(), -5),
                        new KeyValue(dayBadge.opacityProperty(), 0.72))
        );
        glow.setCycleCount(Animation.INDEFINITE);
        glow.setAutoReverse(true);
        playDecoration(glow);
    }

    private void createCoachTalk() {
        Label bubble = new Label("Совет Духоты");
        bubble.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 16));
        bubble.setStyle("-fx-text-fill: #F8FAFC;"
                + "-fx-background-color: rgba(15, 23, 42, 0.88);"
                + "-fx-background-radius: 18;"
                + "-fx-border-color: rgba(125, 211, 252, 0.7);"
                + "-fx-border-radius: 18;"
                + "-fx-padding: 10 16 10 16;");
        bubble.setLayoutX(244);
        bubble.setLayoutY(76);
        animationLayer.getChildren().add(bubble);

        FadeTransition fade = new FadeTransition(Duration.seconds(1.1), bubble);
        fade.setFromValue(0.4);
        fade.setToValue(1.0);
        fade.setAutoReverse(true);
        fade.setCycleCount(Animation.INDEFINITE);
        playDecoration(fade);
    }

    private void createStageSpotlight() {
        Polygon leftBeam = new Polygon(72, 0, 168, 0, 250, 470, 120, 470);
        leftBeam.setFill(Color.web("#22D3EE", 0.11));
        Polygon rightBeam = new Polygon(260, 0, 360, 0, 315, 470, 185, 470);
        rightBeam.setFill(Color.web("#A7F3D0", 0.10));

        Rectangle stage = new Rectangle(250, 28);
        stage.setArcWidth(22);
        stage.setArcHeight(22);
        stage.setFill(Color.web("#0F172A", 0.86));
        stage.setStroke(Color.web("#22D3EE", 0.45));
        stage.setLayoutX(90);
        stage.setLayoutY(460);

        Label judges = new Label("Судьи смотрят на форму");
        judges.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        judges.setStyle("-fx-text-fill: #E0F2FE;"
                + "-fx-background-color: rgba(2, 6, 23, 0.66);"
                + "-fx-background-radius: 13;"
                + "-fx-padding: 7 12 7 12;");
        judges.setLayoutX(118);
        judges.setLayoutY(414);

        for (int i = 0; i < 14; i++) {
            Circle spectator = new Circle(4, Color.web(i % 2 == 0 ? "#22C55E" : "#38BDF8", 0.6));
            spectator.setCenterX(76 + i * 22);
            spectator.setCenterY(502 + (i % 3) * 7);
            animationLayer.getChildren().add(spectator);
        }

        animationLayer.getChildren().addAll(leftBeam, rightBeam, stage, judges);

        Timeline spotlight = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(leftBeam.opacityProperty(), 0.35),
                        new KeyValue(rightBeam.opacityProperty(), 0.82),
                        new KeyValue(judges.opacityProperty(), 0.65)),
                new KeyFrame(Duration.seconds(1.3),
                        new KeyValue(leftBeam.opacityProperty(), 0.86),
                        new KeyValue(rightBeam.opacityProperty(), 0.36),
                        new KeyValue(judges.opacityProperty(), 1.0))
        );
        spotlight.setAutoReverse(true);
        spotlight.setCycleCount(Animation.INDEFINITE);
        playDecoration(spotlight);
    }

    private void addFloatingBadge(String text, double x, double y, String color, double delaySeconds) {
        Label badge = new Label(text);
        badge.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, text.length() > 2 ? 15 : 24));
        badge.setStyle("-fx-text-fill: " + color + ";"
                + "-fx-background-color: rgba(2, 6, 23, 0.68);"
                + "-fx-background-radius: 18;"
                + "-fx-border-color: rgba(226, 232, 240, 0.18);"
                + "-fx-border-radius: 18;"
                + "-fx-padding: 8 13 8 13;");
        badge.setLayoutX(x);
        badge.setLayoutY(y);
        animationLayer.getChildren().add(badge);

        Timeline floatLoop = new Timeline(
                new KeyFrame(Duration.seconds(delaySeconds),
                        new KeyValue(badge.translateYProperty(), 18),
                        new KeyValue(badge.opacityProperty(), 0.35)),
                new KeyFrame(Duration.seconds(delaySeconds + 0.85),
                        new KeyValue(badge.translateYProperty(), -4),
                        new KeyValue(badge.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(delaySeconds + 1.7),
                        new KeyValue(badge.translateYProperty(), 18),
                        new KeyValue(badge.opacityProperty(), 0.35))
        );
        floatLoop.setCycleCount(Animation.INDEFINITE);
        playDecoration(floatLoop);
    }

    private void resetAnimatedNodes() {
        characterSlot.setTranslateX(38);
        characterSlot.setTranslateY(0);
        characterSlot.setScaleX(1);
        characterSlot.setScaleY(1);
        characterSlot.setOpacity(1);
        characterSlot.setRotate(0);
    }

    private void stopDecorations() {
        for (Animation animation : runningDecorations) {
            animation.stop();
        }
        runningDecorations.clear();
    }

    private void playDecoration(Animation animation) {
        runningDecorations.add(animation);
        animation.play();
    }
}
