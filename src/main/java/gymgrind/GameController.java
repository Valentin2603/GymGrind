package gymgrind;

import gymgrind.logic.InteractionService;
import gymgrind.logic.MovementService;
import gymgrind.logic.SupplementService;
import gymgrind.logic.TrainingService;
import gymgrind.minigames.PowerMeterMinigame;
import gymgrind.minigames.RhythmMinigame;
import gymgrind.model.GameMap;
import gymgrind.model.GymObject;
import gymgrind.model.MachineType;
import gymgrind.model.MinigameResult;
import gymgrind.model.Player;
import gymgrind.model.TrainingMachine;
import gymgrind.model.TrainingOutcome;
import gymgrind.model.TrainingSession;
import gymgrind.model.TrainingWeight;
import gymgrind.ui.GameView;
import javafx.animation.AnimationTimer;
import javafx.scene.Node;
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
    private final TrainingService trainingService;
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
        this.trainingService = new TrainingService(new SupplementService());
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
                if (gameState == GameState.MENU || gameState == GameState.LOSE) {
                    startNewRun();
                }
            }
            case ESCAPE -> {
                if (gameState != GameState.MENU) {
                    gameState = GameState.MENU;
                    statusMessage = "Пауза. Нажмите «Начать», чтобы вернуться в зал.";
                    view.hideOverlay();
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

        if (gymObject instanceof TrainingMachine trainingMachine) {
            openWeightSelection(trainingMachine);
            return;
        }

        statusMessage = gymObject.interact();
        refreshUi();
    }

    private void openWeightSelection(TrainingMachine machine) {
        if (!trainingService.isSupportedMinigame(machine.machineType())) {
            statusMessage = machine.name() + ": мини-игра будет добавлена позже.";
            refreshUi();
            return;
        }

        inputState.clear();
        gameState = GameState.MINIGAME;
        statusMessage = "Выберите вес для тренировки.";
        view.showTrainingSetup(
                machine,
                weight -> startTraining(machine, weight),
                () -> {
                    gameState = GameState.PLAYING;
                    statusMessage = "Тренировка отменена.";
                    view.hideOverlay();
                    refreshUi();
                    view.requestGameFocus();
                }
        );
        refreshUi();
    }

    private void startTraining(TrainingMachine machine, TrainingWeight weight) {
        TrainingSession session = trainingService.createSession(player, machine, weight);
        Node minigame = createMinigame(session);
        statusMessage = "Тренировка началась: " + machine.name() + ", вес: " + weight.label() + ".";
        view.showOverlay(minigame);
        refreshUi();
        minigame.requestFocus();
    }

    private Node createMinigame(TrainingSession session) {
        MachineType machineType = session.machine().machineType();
        if (machineType == MachineType.DEADLIFT_PLATFORM) {
            return new PowerMeterMinigame(session, result -> finishTraining(session, result));
        }
        if (machineType == MachineType.TREADMILL) {
            return new RhythmMinigame(session, result -> finishTraining(session, result));
        }
        throw new IllegalArgumentException("Unsupported minigame: " + machineType);
    }

    private void finishTraining(TrainingSession session, MinigameResult result) {
        TrainingOutcome outcome = trainingService.finishTraining(player, session, result);
        statusMessage = outcome.message();
        view.hideOverlay();

        if (player.stats().fatigue() >= 100) {
            gameState = GameState.LOSE;
            statusMessage += " Усталость дошла до 100. Вы перетренировались.";
        } else {
            gameState = GameState.PLAYING;
        }

        refreshUi();
        view.requestGameFocus();
    }
}
