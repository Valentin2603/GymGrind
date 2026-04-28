package gymgrind;

import gymgrind.logic.InteractionService;
import gymgrind.logic.MovementService;
import gymgrind.model.GameMap;
import gymgrind.model.GymObject;
import gymgrind.model.Player;
import gymgrind.ui.GameView;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.util.Optional;

public final class GameController {

    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 720;

    private final Stage stage;
    private final GameView view;
    private final GameMap gameMap;
    private final Player player;
    private final InputState inputState;
    private final MovementService movementService;
    private final InteractionService interactionService;
    private final GameRenderer renderer;

    private GameState gameState;
    private Optional<GymObject> nearbyObject;
    private String statusMessage;
    private AnimationTimer gameLoop;

    public GameController(Stage stage) {
        this.stage = stage;
        this.view = new GameView(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.gameMap = GameMap.createWeekOneLayout();
        this.player = Player.createDefault(gameMap);
        this.inputState = new InputState();
        this.movementService = new MovementService();
        this.interactionService = new InteractionService();
        this.renderer = new GameRenderer();
        this.gameState = GameState.MENU;
        this.nearbyObject = Optional.empty();
        this.statusMessage = "Нажмите «Начать», чтобы войти в зал.";
    }

    public Scene createScene() {
        Scene scene = new Scene(view, WINDOW_WIDTH, WINDOW_HEIGHT);

        scene.setOnKeyPressed(event -> handleKeyPressed(event.getCode()));
        scene.setOnKeyReleased(event -> handleKeyReleased(event.getCode()));

        view.setOnStart(this::startNewRun);
        view.setOnExit(stage::close);
        refreshUi();

        return scene;
    }

    public void start() {
        gameLoop = new AnimationTimer() {
            private long lastFrameNanos = -1L;

            @Override
            public void handle(long now) {
                if (lastFrameNanos < 0L) {
                    lastFrameNanos = now;
                }

                double deltaSeconds = (now - lastFrameNanos) / 1_000_000_000.0;
                lastFrameNanos = now;

                update(deltaSeconds);
                render();
            }
        };
        gameLoop.start();
    }

    private void startNewRun() {
        player.reset(gameMap);
        gameState = GameState.PLAYING;
        statusMessage = "Осмотритесь в зале. Подойдите к объекту и нажмите E.";
        refreshUi();
        view.requestGameFocus();
    }

    private void update(double deltaSeconds) {
        if (gameState == GameState.PLAYING) {
            movementService.movePlayer(player, inputState, gameMap, deltaSeconds);
            nearbyObject = interactionService.findNearbyObject(player, gameMap);
        } else {
            nearbyObject = Optional.empty();
        }

        refreshUi();
    }

    private void render() {
        renderer.render(view.getGraphicsContext(), gameMap, player, nearbyObject, gameState);
    }

    private void refreshUi() {
        view.updateHud(player, gameState);
        view.setMainMenuVisible(gameState == GameState.MENU);
        view.setInteractionPrompt(interactionService.buildPrompt(nearbyObject, gameState));
        view.setStatusMessage(statusMessage);
    }

    private void handleKeyPressed(KeyCode keyCode) {
        switch (keyCode) {
            case W, UP -> inputState.setUp(true);
            case S, DOWN -> inputState.setDown(true);
            case A, LEFT -> inputState.setLeft(true);
            case D, RIGHT -> inputState.setRight(true);
            case E -> tryInteract();
            case ENTER -> {
                if (gameState == GameState.MENU) {
                    startNewRun();
                }
            }
            case ESCAPE -> {
                if (gameState == GameState.PLAYING) {
                    gameState = GameState.MENU;
                    statusMessage = "Пауза. Нажмите «Начать», чтобы вернуться в зал.";
                    refreshUi();
                }
            }
            default -> {
            }
        }
    }

    private void handleKeyReleased(KeyCode keyCode) {
        switch (keyCode) {
            case W, UP -> inputState.setUp(false);
            case S, DOWN -> inputState.setDown(false);
            case A, LEFT -> inputState.setLeft(false);
            case D, RIGHT -> inputState.setRight(false);
            default -> {
            }
        }
    }

    private void tryInteract() {
        if (gameState != GameState.PLAYING || nearbyObject.isEmpty()) {
            return;
        }

        GymObject gymObject = nearbyObject.get();
        statusMessage = gymObject.interact();
        refreshUi();
    }
}
