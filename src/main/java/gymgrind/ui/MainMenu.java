package gymgrind.ui;

import gymgrind.player.PlayerProfile;
import gymgrind.player.PlayerProfiles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MainMenu extends StackPane {

    private static final String[] BUNDLED_SPLASH_RESOURCES = {
            "/assets/ui/player_splash.png",
            "/assets/ui/player_splash.jpg",
            "/assets/ui/player_splash.jpeg"
    };
    private static final String[] LOCAL_SPLASH_FILES = {
            "player_splash.png",
            "player_splash.jpg",
            "player_splash.jpeg"
    };

    private final Button startButton;
    private final Button continueButton;
    private final Button exitButton;
    private final Map<PlayerProfile, Button> profileButtons;
    private PlayerProfile selectedProfile;

    public MainMenu() {
        setMinSize(0, 0);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        setStyle("-fx-background-color: #020617;");

        Region backgroundView = createBackgroundLayer();

        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(2, 6, 23, 0.28), rgba(2, 6, 23, 0.78) 52%, rgba(2, 6, 23, 0.95));");
        overlay.prefWidthProperty().bind(widthProperty());
        overlay.prefHeightProperty().bind(heightProperty());

        profileButtons = new LinkedHashMap<>();

        VBox copyBlock = new VBox(16);
        copyBlock.setAlignment(Pos.CENTER_LEFT);
        copyBlock.setFillWidth(true);
        copyBlock.setPrefWidth(560);
        copyBlock.setMaxWidth(560);
        copyBlock.setMaxHeight(Region.USE_PREF_SIZE);
        copyBlock.setPadding(new Insets(28, 32, 28, 32));
        copyBlock.setStyle("-fx-background-color: rgba(2, 6, 23, 0.68);"
                + "-fx-background-radius: 26;"
                + "-fx-border-color: rgba(125, 211, 252, 0.22);"
                + "-fx-border-radius: 26;"
                + "-fx-border-width: 1.5;");

        Label eyebrow = new Label("GYMGRIND");
        eyebrow.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        eyebrow.setStyle("-fx-text-fill: #7DD3FC;");

        Label title = new Label("ТВОЯ ДОРОГА\nК СЦЕНЕ");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 42));
        title.setStyle("-fx-text-fill: #F8FAFC;");
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);

        Label subtitle = new Label(
                "Перед стартом выбери героя. Теперь в меню есть разные стили старта: быстрый сухой новичок и тяжёлый силовик."
        );
        subtitle.setFont(Font.font("Segoe UI", 18));
        subtitle.setStyle("-fx-text-fill: #CBD5E1;");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(Double.MAX_VALUE);

        Label chooseLabel = new Label("Выбор героя");
        chooseLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        chooseLabel.setStyle("-fx-text-fill: #F8FAFC;");

        VBox profileList = new VBox(12);
        profileList.setFillWidth(true);
        for (PlayerProfile profile : PlayerProfiles.all()) {
            Button button = createProfileButton(profile);
            profileButtons.put(profile, button);
            profileList.getChildren().add(button);
        }

        Label controls = new Label("Управление: WASD или стрелки - движение, E - действие, Esc - меню");
        controls.setFont(Font.font("Segoe UI", 14));
        controls.setStyle("-fx-text-fill: #94A3B8;");
        controls.setWrapText(true);
        controls.setMaxWidth(Double.MAX_VALUE);

        startButton = new Button("Начать игру");
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setPrefHeight(52);
        startButton.setStyle(actionButtonStyle("#22C55E", "#14532D"));

        continueButton = new Button("Продолжить");
        continueButton.setMaxWidth(Double.MAX_VALUE);
        continueButton.setPrefHeight(52);
        continueButton.setStyle(actionButtonStyle("#0EA5E9", "#075985"));

        exitButton = new Button("Выход");
        exitButton.setMaxWidth(Double.MAX_VALUE);
        exitButton.setPrefHeight(52);
        exitButton.setStyle(actionButtonStyle("#EF4444", "#7F1D1D"));

        VBox buttons = new VBox(12, startButton, continueButton, exitButton);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setFillWidth(true);

        copyBlock.getChildren().addAll(eyebrow, title, subtitle, chooseLabel, profileList, controls, buttons);
        getChildren().addAll(backgroundView, overlay, copyBlock);

        StackPane.setAlignment(copyBlock, Pos.CENTER_LEFT);
        StackPane.setMargin(copyBlock, new Insets(32, 32, 32, 32));

        selectProfile(PlayerProfiles.defaultProfile());
    }

    public void setOnStart(Runnable action) {
        startButton.setOnAction(event -> action.run());
    }

    public void setOnContinue(Runnable action) {
        continueButton.setOnAction(event -> action.run());
    }

    public void setContinueAvailable(boolean available) {
        continueButton.setDisable(!available);
        continueButton.setText(available ? "Продолжить" : "Продолжить (нет сохранения)");
    }

    public void setOnExit(Runnable action) {
        exitButton.setOnAction(event -> action.run());
    }

    public PlayerProfile selectedProfile() {
        return selectedProfile != null ? selectedProfile : PlayerProfiles.defaultProfile();
    }

    private Button createProfileButton(PlayerProfile profile) {
        Button button = new Button();
        button.setAlignment(Pos.CENTER_LEFT);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setGraphic(buildProfileGraphic(profile));
        button.setOnAction(event -> selectProfile(profile));
        button.setStyle(profileButtonStyle(false));
        return button;
    }

    private HBox buildProfileGraphic(PlayerProfile profile) {
        HBox root = new HBox(16);
        root.setAlignment(Pos.CENTER_LEFT);

        ImageView preview = buildPreview(profile.previewSpritePath());
        VBox textBlock = new VBox(6);
        textBlock.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textBlock, Priority.ALWAYS);

        Label name = new Label(profile.displayName());
        name.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        name.setStyle("-fx-text-fill: #F8FAFC;");

        Label description = new Label(profile.description());
        description.setFont(Font.font("Segoe UI", 13));
        description.setStyle("-fx-text-fill: #CBD5E1;");
        description.setWrapText(true);
        description.setMaxWidth(360);

        Label stats = new Label(
                "Сила: " + profile.baseStrength()
                        + " | Масса: " + profile.baseMuscle()
                        + " | Выносливость: " + profile.baseStamina()
        );
        stats.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        stats.setStyle("-fx-text-fill: #F8FAFC;");
        stats.setWrapText(true);

        textBlock.getChildren().addAll(name, description, stats);
        root.getChildren().addAll(preview, textBlock);
        return root;
    }

    private ImageView buildPreview(String previewPath) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(82);
        imageView.setFitHeight(82);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        URL resource = MainMenu.class.getResource(previewPath);
        if (resource != null) {
            imageView.setImage(new Image(resource.toExternalForm()));
        }
        return imageView;
    }

    private void selectProfile(PlayerProfile profile) {
        selectedProfile = profile;
        startButton.setDisable(selectedProfile == null);
        for (Map.Entry<PlayerProfile, Button> entry : profileButtons.entrySet()) {
            boolean selected = entry.getKey().equals(selectedProfile);
            entry.getValue().setStyle(profileButtonStyle(selected));
        }
    }

    private Region createBackgroundLayer() {
        Region backgroundLayer = new Region();
        backgroundLayer.setMinSize(0, 0);
        backgroundLayer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        backgroundLayer.prefWidthProperty().bind(widthProperty());
        backgroundLayer.prefHeightProperty().bind(heightProperty());
        BackgroundFill fallbackFill = new BackgroundFill(Color.web("#020617"), null, null);

        Image splashImage = loadSplashImage();
        if (splashImage != null) {
            BackgroundSize coverSize = new BackgroundSize(100, 100, true, true, false, true);
            BackgroundImage backgroundImage = new BackgroundImage(
                    splashImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    coverSize
            );
            backgroundLayer.setBackground(new Background(
                    new BackgroundFill[]{new BackgroundFill(Color.web("#0F172A"), null, null)},
                    new BackgroundImage[]{backgroundImage}
            ));
            return backgroundLayer;
        }

        backgroundLayer.setBackground(new Background(fallbackFill));
        return backgroundLayer;
    }

    private Image loadSplashImage() {
        String explicitPath = System.getProperty("gymgrind.start.image");
        if (explicitPath != null && !explicitPath.isBlank()) {
            Image image = loadImageFromString(explicitPath);
            if (image != null) {
                return image;
            }
        }

        for (String localFile : LOCAL_SPLASH_FILES) {
            Image image = loadImageFromPath(Path.of(localFile));
            if (image != null) {
                return image;
            }
        }

        for (String resourcePath : BUNDLED_SPLASH_RESOURCES) {
            URL resource = MainMenu.class.getResource(resourcePath);
            if (resource != null) {
                return new Image(resource.toExternalForm());
            }
        }

        return null;
    }

    private Image loadImageFromString(String pathValue) {
        try {
            return loadImageFromPath(Path.of(pathValue));
        } catch (InvalidPathException ignored) {
            return null;
        }
    }

    private Image loadImageFromPath(Path path) {
        if (!Files.isRegularFile(path)) {
            return null;
        }
        return new Image(path.toUri().toString());
    }

    private String actionButtonStyle(String accentColor, String borderColor) {
        return "-fx-background-color: " + accentColor + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 16px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 14;"
                + "-fx-border-radius: 14;"
                + "-fx-border-width: 1.5;"
                + "-fx-border-color: " + borderColor + ";"
                + "-fx-padding: 12 18 12 18;";
    }

    private String profileButtonStyle(boolean selected) {
        String borderColor = selected ? "#7FDBA4" : "rgba(148, 163, 184, 0.28)";
        String background = selected ? "rgba(15, 23, 42, 0.92)" : "rgba(15, 23, 42, 0.62)";
        return "-fx-background-color: " + background + ";"
                + "-fx-background-radius: 18;"
                + "-fx-border-color: " + borderColor + ";"
                + "-fx-border-radius: 18;"
                + "-fx-border-width: 1.6;"
                + "-fx-padding: 12 14 12 14;";
    }
}
