package gymgrind;

import gymgrind.logic.InteractionService;
import gymgrind.logic.MovementService;
import gymgrind.logic.SkillCheckService;
import gymgrind.model.GameMap;
import gymgrind.model.GymObject;
import gymgrind.model.Player;
import gymgrind.model.SkillCheckResult;
import gymgrind.model.SkillCheckSession;
import gymgrind.model.TrainingMachine;
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
    private final SkillCheckService skillCheckService;
    private final GameRenderer renderer;

    private GameState gameState;
    private Optional<GymObject> nearbyObject;
    private Optional<SkillCheckSession> activeSkillCheck;
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
        this.skillCheckService = new SkillCheckService();
        this.renderer = new GameRenderer();
        this.gameState = GameState.MENU;
        this.nearbyObject = Optional.empty();
        this.activeSkillCheck = Optional.empty();
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
        inputState.reset();
        gameState = GameState.PLAYING;
        activeSkillCheck = Optional.empty();
        pendingSuccessResult = Optional.empty();
        statusMessage = "Осмотритесь в зале. На тренажёрах запускаются скилл-чеки.";
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
            case RESULT -> nearbyObject = Optional.empty();
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
                if (gameState == GameState.MENU) {
                    startNewRun();
                }
            }
            case ESCAPE -> {
                if (gameState == GameState.PLAYING) {
                    inputState.reset();
                    activeSkillCheck = Optional.empty();
                    pendingSuccessResult = Optional.empty();
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
        if (gymObject instanceof TrainingMachine machine) {
            startSkillCheck(machine);
            return;
        }

        statusMessage = gymObject.interact();
        refreshUi();
    }

    private String buildPrompt() {
        if (gameState == GameState.RESULT && pendingSuccessResult.isPresent()) {
            return "Space или Esc - закрыть окно результата.";
        }

        if (gameState != GameState.MINIGAME || activeSkillCheck.isEmpty()) {
            return interactionService.buildPrompt(nearbyObject, gameState);
        }

        SkillCheckSession session = activeSkillCheck.get();
        if (session.isSequenceMode()) {
            return "Нажимайте буквы из очереди. Esc - отмена подхода.";
        }

        return "Space/Enter/E - зафиксировать попытку, Esc - отмена подхода.";
    }

    private void handleMinigameKeyPressed(KeyCode keyCode) {
        if (keyCode == KeyCode.ESCAPE) {
            cancelSkillCheck();
            return;
        }

        if (activeSkillCheck.isEmpty()) {
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

    private void startSkillCheck(TrainingMachine machine) {
        inputState.reset();
        pendingSuccessResult = Optional.empty();
        SkillCheckSession session = skillCheckService.startSession(machine, player.stats().strength());
        activeSkillCheck = Optional.of(session);
        gameState = GameState.MINIGAME;
        statusMessage = buildSkillCheckStartMessage(session);
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
        player.stats().applyDeltas(
                result.strengthDelta(),
                result.muscleDelta(),
                result.staminaDelta(),
                result.fatigueDelta(),
                0
        );

        activeSkillCheck = Optional.empty();
        if (result.success()) {
            openSuccessResult(result);
            return;
        }

        pendingSuccessResult = Optional.empty();
        gameState = GameState.PLAYING;
        nearbyObject = interactionService.findNearbyObject(player, gameMap);
        statusMessage = result.message();
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
                    + ": набирайте символы по порядку. Полоса снизу тает со временем: верные клавиши заполняют её, ошибки срезают прогресс.";
        }

        if (session.requiresMultipleHits()) {
            return session.machine().name()
                    + ": попадите в зелёную зону "
                    + session.requiredHits()
                    + " раз подряд. После каждого успеха зона сужается, а сила ускоряет маркер.";
        }

        return session.machine().name() + ": остановите маркер в зелёной зоне, чтобы засчитать подход.";
    }

    private void cancelSkillCheck() {
        if (activeSkillCheck.isEmpty()) {
            return;
        }

        activeSkillCheck = Optional.empty();
        gameState = GameState.PLAYING;
        nearbyObject = interactionService.findNearbyObject(player, gameMap);
        statusMessage = "Подход отменён. Можно попробовать ещё раз.";
        refreshUi();
    }
}
