package gymgrind.game;

import gymgrind.game.GameState;
import gymgrind.game.InputState;
import gymgrind.gym.InteractionService;
import gymgrind.player.MovementService;
import gymgrind.shop.ShopService;
import gymgrind.training.minigames.SkillCheckService;
import gymgrind.shop.SupplementService;
import gymgrind.training.TrainingService;
import gymgrind.training.minigames.PowerMeterMinigame;
import gymgrind.training.minigames.RhythmMinigame;
import gymgrind.gym.GameMap;
import gymgrind.gym.objects.GymObject;
import gymgrind.gym.objects.InteractiveZone;
import gymgrind.training.MachineType;
import gymgrind.training.MinigameResult;
import gymgrind.player.Player;
import gymgrind.shop.ShopPurchaseResult;
import gymgrind.training.minigames.SkillCheckResult;
import gymgrind.training.minigames.SkillCheckSession;
import gymgrind.training.TrainingGrade;
import gymgrind.training.TrainingMachine;
import gymgrind.training.TrainingOutcome;
import gymgrind.training.TrainingSession;
import gymgrind.training.TrainingWeight;
import gymgrind.gym.objects.ZoneType;
import gymgrind.ui.render.GameRenderer;
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
    private final ShopService shopService;
    private final SkillCheckService skillCheckService;
    private final GameRenderer renderer;

    private GameState gameState;
    private Optional<GymObject> nearbyObject;
    private Optional<SkillCheckSession> activeSkillCheck;
    private Optional<TrainingSession> activeTrainingSession;
    private Optional<SkillCheckResult> pendingSuccessResult;
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
        this.shopService = new ShopService();
        this.skillCheckService = new SkillCheckService();
        this.renderer = new GameRenderer();
        this.gameState = GameState.MENU;
        this.nearbyObject = Optional.empty();
        this.activeSkillCheck = Optional.empty();
        this.activeTrainingSession = Optional.empty();
        this.pendingSuccessResult = Optional.empty();
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
        inputState.clear();
        gameState = GameState.PLAYING;
        nearbyObject = Optional.empty();
        activeSkillCheck = Optional.empty();
        activeTrainingSession = Optional.empty();
        pendingSuccessResult = Optional.empty();
        view.hideOverlay();
        statusMessage = "Осмотритесь в зале. Подойдите к объекту и нажмите E.";
        refreshUi();
        view.requestGameFocus();
    }

    private void update(double deltaSeconds) {
        switch (gameState) {
            case PLAYING -> {
                movementService.movePlayer(player, inputState, gameMap, deltaSeconds);
                nearbyObject = interactionService.findNearbyObject(player, gameMap);
            }
            case MINIGAME -> {
                nearbyObject = Optional.empty();
                if (activeSkillCheck.isPresent()) {
                    SkillCheckSession session = activeSkillCheck.get();
                    skillCheckService.update(session, deltaSeconds);
                    if (session.isSequenceMode() && session.isSequenceDepleted()) {
                        finishSkillCheck(skillCheckService.resolveFailure(session));
                        return;
                    }
                }
            }
            case RESULT, SHOP -> nearbyObject = Optional.empty();
            default -> nearbyObject = Optional.empty();
        }

        refreshUi();
    }

    private void render() {
        renderer.render(
                view.getGraphicsContext(),
                gameMap,
                player,
                nearbyObject,
                gameState,
                activeSkillCheck,
                pendingSuccessResult
        );
    }

    private void refreshUi() {
        view.updateHud(player, gameState);
        view.setMainMenuVisible(gameState == GameState.MENU);
        view.setInteractionPrompt(buildPrompt());
        view.setStatusMessage(statusMessage);
    }

    private void handleKeyPressed(KeyCode keyCode) {
        if (gameState == GameState.RESULT) {
            handleResultKeyPressed(keyCode);
            return;
        }

        if (gameState == GameState.MINIGAME) {
            handleMinigameKeyPressed(keyCode);
            return;
        }

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
                    returnToMenu();
                }
            }
            default -> {
            }
        }
    }

    private void handleKeyReleased(KeyCode keyCode) {
        if (gameState == GameState.MINIGAME || gameState == GameState.RESULT) {
            return;
        }

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

        if (gymObject instanceof InteractiveZone zone && zone.zoneType() == ZoneType.SHOP) {
            openShop();
            return;
        }

        statusMessage = gymObject.interact();
        refreshUi();
    }

    private void openShop() {
        inputState.clear();
        gameState = GameState.SHOP;
        statusMessage = "Магазин открыт.";
        view.showShop(
                player,
                supplementType -> {
                    ShopPurchaseResult result = shopService.buy(player, supplementType);
                    statusMessage = result.message();
                    refreshUi();
                    return result.message();
                },
                () -> {
                    gameState = GameState.PLAYING;
                    statusMessage = "Вы вышли из магазина.";
                    view.hideOverlay();
                    refreshUi();
                    view.requestGameFocus();
                }
        );
        refreshUi();
    }

    private void openWeightSelection(TrainingMachine machine) {
        inputState.clear();
        gameState = GameState.MINIGAME;
        activeSkillCheck = Optional.empty();
        activeTrainingSession = Optional.empty();
        pendingSuccessResult = Optional.empty();
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
        view.hideOverlay();
        TrainingSession session = trainingService.createSession(player, machine, weight);
        if (usesSkillCheck(machine.machineType())) {
            startSkillCheck(session);
            return;
        }

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
        if (machineType == MachineType.DEADLIFT_PLATFORM) {
            return new PowerMeterMinigame(session, result -> finishTraining(session, result));
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

    private String buildPrompt() {
        if (gameState == GameState.RESULT && pendingSuccessResult.isPresent()) {
            return "Space или Esc - закрыть окно результата.";
        }

        if (gameState == GameState.MINIGAME && activeSkillCheck.isPresent()) {
            SkillCheckSession session = activeSkillCheck.get();
            if (session.isSequenceMode()) {
                return "Нажимайте буквы из очереди. Esc - отмена подхода.";
            }
            return "Space/Enter/E - зафиксировать попытку, Esc - отмена подхода.";
        }

        return interactionService.buildPrompt(nearbyObject, gameState);
    }

    private void handleMinigameKeyPressed(KeyCode keyCode) {
        if (activeSkillCheck.isEmpty()) {
            if (keyCode == KeyCode.ESCAPE) {
                gameState = GameState.PLAYING;
                statusMessage = "Тренировка отменена.";
                view.hideOverlay();
                refreshUi();
                view.requestGameFocus();
            }
            return;
        }

        if (keyCode == KeyCode.ESCAPE) {
            cancelSkillCheck();
            return;
        }

        SkillCheckSession session = activeSkillCheck.get();
        if (session.isTimingMode()) {
            if (keyCode == KeyCode.SPACE || keyCode == KeyCode.ENTER || keyCode == KeyCode.E) {
                resolveTimingSkillCheck();
            }
            return;
        }

        handleSequenceSkillCheckInput(session, keyCode);
    }

    private void handleSequenceSkillCheckInput(SkillCheckSession session, KeyCode keyCode) {
        Character symbol = skillCheckService.mapSequenceKey(keyCode);
        if (symbol == null) {
            return;
        }

        boolean correct = skillCheckService.registerSequenceInput(session, symbol);
        if (session.isSequenceCompleted()) {
            finishSkillCheck(skillCheckService.resolveSuccess(session));
            return;
        }

        if (session.isSequenceDepleted()) {
            finishSkillCheck(skillCheckService.resolveFailure(session));
            return;
        }

        statusMessage = skillCheckService.buildSequenceProgressMessage(session, correct);
        refreshUi();
    }

    private void startSkillCheck(TrainingSession trainingSession) {
        inputState.clear();
        pendingSuccessResult = Optional.empty();
        activeTrainingSession = Optional.of(trainingSession);
        TrainingMachine machine = trainingSession.machine();
        SkillCheckSession session = skillCheckService.startSession(machine, player.stats().strength());
        activeSkillCheck = Optional.of(session);
        gameState = GameState.MINIGAME;
        statusMessage = buildSkillCheckStartMessage(session) + " Вес: " + trainingSession.weight().label() + ".";
        refreshUi();
    }

    private void resolveTimingSkillCheck() {
        if (activeSkillCheck.isEmpty()) {
            return;
        }

        SkillCheckSession session = activeSkillCheck.get();
        if (!session.isMarkerInsideSuccessZone()) {
            finishSkillCheck(skillCheckService.resolveFailure(session));
            return;
        }

        boolean completed = skillCheckService.registerSuccessfulHit(session, player.stats().strength());
        if (!completed) {
            statusMessage = skillCheckService.buildTimingProgressMessage(session);
            refreshUi();
            return;
        }

        finishSkillCheck(skillCheckService.resolveSuccess(session));
    }

    private void finishSkillCheck(SkillCheckResult result) {
        TrainingSession trainingSession = activeTrainingSession.orElse(null);
        if (trainingSession == null) {
            player.stats().applyDeltas(
                    result.strengthDelta(),
                    result.muscleDelta(),
                    result.staminaDelta(),
                    result.fatigueDelta(),
                    0
            );
            activeSkillCheck = Optional.empty();
            gameState = GameState.PLAYING;
            statusMessage = result.message();
            refreshUi();
            return;
        }

        activeSkillCheck = Optional.empty();
        activeTrainingSession = Optional.empty();
        TrainingGrade grade = result.success() ? TrainingGrade.NORMAL : TrainingGrade.FAIL;
        TrainingOutcome outcome = trainingService.finishTraining(
                player,
                trainingSession,
                new MinigameResult(grade, result.success() ? "Подход выполнен." : "Подход сорван.")
        );
        SkillCheckResult displayResult = new SkillCheckResult(
                result.success(),
                outcome.message(),
                outcome.finalReward().strength(),
                outcome.finalReward().muscle(),
                outcome.finalReward().stamina(),
                outcome.finalReward().fatigue()
        );

        if (player.stats().fatigue() >= 100) {
            pendingSuccessResult = Optional.empty();
            gameState = GameState.LOSE;
            statusMessage = outcome.message() + " Усталость дошла до 100. Вы перетренировались.";
            refreshUi();
            return;
        }

        if (result.success()) {
            openSuccessResult(displayResult);
            return;
        }

        pendingSuccessResult = Optional.empty();
        gameState = GameState.PLAYING;
        nearbyObject = interactionService.findNearbyObject(player, gameMap);
        statusMessage = outcome.message();
        refreshUi();
    }

    private void handleResultKeyPressed(KeyCode keyCode) {
        if (keyCode == KeyCode.SPACE || keyCode == KeyCode.ESCAPE) {
            closeSuccessResult();
        }
    }

    private void openSuccessResult(SkillCheckResult result) {
        pendingSuccessResult = Optional.of(result);
        gameState = GameState.RESULT;
        nearbyObject = Optional.empty();
        statusMessage = result.message();
        refreshUi();
    }

    private void closeSuccessResult() {
        if (pendingSuccessResult.isEmpty()) {
            return;
        }

        SkillCheckResult result = pendingSuccessResult.get();
        pendingSuccessResult = Optional.empty();
        gameState = GameState.PLAYING;
        nearbyObject = interactionService.findNearbyObject(player, gameMap);
        statusMessage = result.message();
        refreshUi();
        view.requestGameFocus();
    }

    private String buildSkillCheckStartMessage(SkillCheckSession session) {
        if (session.isSequenceMode()) {
            return session.machine().name()
                    + ": набирайте символы по порядку. Верные клавиши заполняют шкалу, ошибки срезают прогресс.";
        }

        if (session.requiresMultipleHits()) {
            return session.machine().name()
                    + ": попадите в зелёную зону "
                    + session.requiredHits()
                    + " раз подряд.";
        }

        return session.machine().name() + ": остановите маркер в зелёной зоне, чтобы засчитать подход.";
    }

    private void cancelSkillCheck() {
        if (activeSkillCheck.isEmpty()) {
            return;
        }

        activeSkillCheck = Optional.empty();
        activeTrainingSession = Optional.empty();
        gameState = GameState.PLAYING;
        nearbyObject = interactionService.findNearbyObject(player, gameMap);
        statusMessage = "Подход отменён. Можно попробовать ещё раз.";
        refreshUi();
    }

    private void returnToMenu() {
        inputState.clear();
        activeSkillCheck = Optional.empty();
        activeTrainingSession = Optional.empty();
        pendingSuccessResult = Optional.empty();
        gameState = GameState.MENU;
        statusMessage = "Пауза. Нажмите «Начать», чтобы вернуться в зал.";
        view.hideOverlay();
        refreshUi();
    }

    private boolean usesSkillCheck(MachineType machineType) {
        return machineType == MachineType.BENCH_PRESS || machineType == MachineType.SQUAT_RACK;
    }
}
