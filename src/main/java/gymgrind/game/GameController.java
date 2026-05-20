package gymgrind.game;

import gymgrind.achievements.AchievementManager;
import gymgrind.gym.CoachDialoguePool;
import gymgrind.gym.GameMap;
import gymgrind.gym.InteractionService;
import gymgrind.gym.Position;
import gymgrind.daily.DailyQuestManager;
import gymgrind.daily.DailyQuestNotification;
import gymgrind.daily.DailyQuestSnapshot;
import gymgrind.gym.objects.GymObject;
import gymgrind.gym.objects.InteractiveZone;
import gymgrind.gym.objects.ZoneType;
import gymgrind.player.MovementService;
import gymgrind.player.Player;
import gymgrind.player.PlayerForm;
import gymgrind.player.PlayerFormDefinition;
import gymgrind.player.PlayerProfile;
import gymgrind.player.PlayerProfiles;
import gymgrind.player.Stats;
import gymgrind.save.SaveData;
import gymgrind.save.SaveService;
import gymgrind.shop.ShopPurchaseResult;
import gymgrind.shop.ShopService;
import gymgrind.shop.SupplementService;
import gymgrind.training.MachineType;
import gymgrind.training.MinigameResult;
import gymgrind.training.TrainingGrade;
import gymgrind.training.TrainingMachine;
import gymgrind.training.TrainingOutcome;
import gymgrind.training.TrainingService;
import gymgrind.training.TrainingSession;
import gymgrind.training.TrainingWeight;
import gymgrind.training.minigames.BalanceBarMinigame;
import gymgrind.training.minigames.PowerMeterMinigame;
import gymgrind.training.minigames.SkillCheckResult;
import gymgrind.training.minigames.SkillCheckService;
import gymgrind.training.minigames.SkillCheckSession;
import gymgrind.ui.GameView;
import gymgrind.ui.render.GameRenderer;
import gymgrind.ui.tutorial.TutorialOverlay;
import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.List;
import java.util.Map;

public final class GameController {

    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 720;
    private static final double ACTIVITY_STAMINA_COST_MULTIPLIER = 1.3;
    private static final double COACH_SPEECH_DURATION_SECONDS = 9.0;
    private static final double CLOTHES_CHANGE_FADE_SECONDS = 0.95;
    private static final boolean SHOW_COACH_SPEECH = false;

    private final Stage stage;
    private final GameView view;
    private final LocationManager locationManager;
    private final Player player;
    private final InputState inputState;
    private final MovementService movementService;
    private final InteractionService interactionService;
    private final TrainingService trainingService;
    private final ShopService shopService;
    private final SkillCheckService skillCheckService;
    private final GameRenderer renderer;
    private final CalendarState calendarState;
    private final SaveService saveService;
    private final CoachDialoguePool coachDialoguePool;
    private final PoseAssetLoader poseAssetLoader;

    private final WorkShiftState workShiftState;

    private final DailyQuestManager dailyQuestManager;
    private final AchievementManager achievementManager;


    private GameState gameState;
    private Optional<GymObject> nearbyObject;
    private Optional<SkillCheckSession> activeSkillCheck;
    private Optional<TrainingSession> activeTrainingSession;
    private Optional<DailyQuestSnapshot> activeTrainingStartSnapshot;
    private Optional<SkillCheckResult> pendingSuccessResult;
    private Optional<CompetitionIntroCutscene> activeCompetitionIntro;
    private Optional<PosingMinigame> activePosingMinigame;
    private Optional<JudgeResultsCutscene> activeJudgeResultsCutscene;
    private Optional<CompetitionResultScreen> activeCompetitionResultScreen;
    private Optional<PerformanceResult> latestCompetitionPerformance;
    private String statusMessage;
    private String coachSpeechText;
    private double coachSpeechTimeLeft;
    private double clothesChangeFadeTimeLeft;
    private AnimationTimer gameLoop;

    public GameController(Stage stage) {
        this.stage = stage;
        this.view = new GameView(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.locationManager = new LocationManager();
        this.player = Player.createDefault(currentMap());
        this.inputState = new InputState();
        this.movementService = new MovementService();
        this.interactionService = new InteractionService();
        this.trainingService = new TrainingService(new SupplementService());
        this.shopService = new ShopService();
        this.skillCheckService = new SkillCheckService();
        this.renderer = new GameRenderer();
        this.calendarState = CalendarState.createDefault();
        this.saveService = new SaveService();
        this.coachDialoguePool = new CoachDialoguePool();
        this.poseAssetLoader = new PoseAssetLoader();

        this.workShiftState = new WorkShiftState();

        this.dailyQuestManager = new DailyQuestManager();
        this.achievementManager = new AchievementManager();

        this.gameState = GameState.MENU;
        this.nearbyObject = Optional.empty();
        this.activeSkillCheck = Optional.empty();
        this.activeTrainingSession = Optional.empty();
        this.activeTrainingStartSnapshot = Optional.empty();
        this.pendingSuccessResult = Optional.empty();
        this.activeCompetitionIntro = Optional.empty();
        this.activePosingMinigame = Optional.empty();
        this.activeJudgeResultsCutscene = Optional.empty();
        this.activeCompetitionResultScreen = Optional.empty();
        this.latestCompetitionPerformance = Optional.empty();
        this.coachSpeechText = "";
        this.coachSpeechTimeLeft = 0.0;
        this.clothesChangeFadeTimeLeft = 0.0;
        this.statusMessage = "РќР°Р¶РјРёС‚Рµ В«РќР°С‡Р°С‚СЊВ», С‡С‚РѕР±С‹ РЅР°С‡Р°С‚СЊ РґРµРЅСЊ РІ РєРѕРјРЅР°С‚Рµ РёРіСЂРѕРєР°.";
    }

    public Scene createScene() {
        Scene scene = new Scene(view, WINDOW_WIDTH, WINDOW_HEIGHT);

        scene.setOnKeyPressed(event -> handleKeyPressed(event.getCode()));
        scene.setOnKeyReleased(event -> handleKeyReleased(event.getCode()));
        scene.setOnMousePressed(event -> handleMousePressed());

        view.setOnStart(this::showTutorialBeforeStart);
        view.setOnContinue(this::loadSavedRun);
        view.setOnExit(stage::close);
        refreshContinueButton();
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
        locationManager.reset();
        player.applyProfile(view.selectedProfile(), currentMap());
        calendarState.reset();
        inputState.clear();
        gameState = explorationStateForCurrentLocation();
        nearbyObject = Optional.empty();
        activeSkillCheck = Optional.empty();
        activeTrainingSession = Optional.empty();
        activeTrainingStartSnapshot = Optional.empty();
        pendingSuccessResult = Optional.empty();
        clearCompetitionFlow();
        clearCoachSpeech();

        workShiftState.reset();

        dailyQuestManager.startNewDay(player, calendarState.currentDay());
        achievementManager.reset();

        view.hideOverlay();
        view.hideTutorial();
        statusMessage = "Р’С‹ РґРѕРјР°. РџРѕРґРѕР№РґРёС‚Рµ Рє РєСЂРѕРІР°С‚Рё, РєРѕРјРїСЊСЋС‚РµСЂСѓ РёР»Рё РґРІРµСЂРё Рё РЅР°Р¶РјРёС‚Рµ E.";
        refreshUi();
        view.requestGameFocus();
    }

    private void showTutorialBeforeStart() {
        inputState.clear();
        PlayerProfile selectedProfile = view.selectedProfile();

        TutorialOverlay tutorialOverlay = new TutorialOverlay(
                selectedProfile,
                () -> {
                    view.hideTutorial();
                    startNewRun();
                },
                () -> {
                    view.hideTutorial();
                    startNewRun();
                },
                () -> {
                    view.hideTutorial();
                    refreshUi();
                }
        );

        view.showTutorial(tutorialOverlay);
    }

    private void loadSavedRun() {
        Optional<SaveData> saveData = saveService.load();
        if (saveData.isEmpty()) {
            statusMessage = "РЎРѕС…СЂР°РЅРµРЅРёРµ РЅРµ РЅР°Р№РґРµРЅРѕ РёР»Рё РїРѕРІСЂРµР¶РґРµРЅРѕ.";
            refreshContinueButton();
            refreshUi();
            return;
        }

        applySaveData(saveData.get());
        gameState = explorationStateForCurrentLocation();
        inputState.clear();
        nearbyObject = Optional.empty();
        activeSkillCheck = Optional.empty();
        activeTrainingSession = Optional.empty();
        activeTrainingStartSnapshot = Optional.empty();
        pendingSuccessResult = Optional.empty();
        clearCompetitionFlow();
        clearCoachSpeech();

        workShiftState.reset();

        if (!dailyQuestManager.restore(player, saveData.get().dailyQuests())) {
            dailyQuestManager.startNewDay(player, calendarState.currentDay());
        }
        achievementManager.restore(saveData.get().completedAchievements());

        view.hideOverlay();
        statusMessage = "РЎРѕС…СЂР°РЅРµРЅРёРµ Р·Р°РіСЂСѓР¶РµРЅРѕ. Р”РµРЅСЊ " + calendarState.currentDay()
                + "/" + calendarState.maxDays() + ".";
        refreshUi();
        view.requestGameFocus();
    }

    private void applySaveData(SaveData saveData) {
        LocationId locationId = saveData.locationId();
        locationManager.travelTo(locationId);

        PlayerProfile profile = PlayerProfiles.findById(saveData.profileId());
        player.applyProfile(profile, currentMap());
        player.setPosition(new Position(saveData.playerX(), saveData.playerY()));
        player.stats().restoreValues(
                saveData.strength(),
                saveData.muscle(),
                saveData.stamina(),
                saveData.fatigue(),
                saveData.money(),
                saveData.bodyFat()
        );
        player.activeSupplements().restore(saveData.activeSupplements());
        player.restoreProgress(saveData.currentForm(), saveData.purchasedSupplements());
        calendarState.setCurrentDay(saveData.currentDay());
    }

    private String saveCurrentRunFromPause() {
        boolean saved = saveCurrentRun();
        statusMessage = saved
                ? "РРіСЂР° СЃРѕС…СЂР°РЅРµРЅР°: " + saveService.savePath()
                : "РќРµ СѓРґР°Р»РѕСЃСЊ СЃРѕС…СЂР°РЅРёС‚СЊ РёРіСЂСѓ.";
        refreshUi();
        return statusMessage;
    }

    private boolean saveCurrentRun() {
        if (!canSaveCurrentRun()) {
            return false;
        }

        boolean saved = saveService.save(createSaveData());
        refreshContinueButton();
        return saved;
    }

    private void refreshContinueButton() {
        Optional<SaveData> saveData = saveService.load();
        if (saveData.isEmpty()) {
            view.setContinueAvailable(false);
            return;
        }

        SaveData data = saveData.get();
        PlayerProfile profile = PlayerProfiles.findById(data.profileId());
        Stats savedStats = new Stats(
                data.strength(),
                data.muscle(),
                data.stamina(),
                data.fatigue(),
                data.money(),
                data.bodyFat()
        );
        view.setContinueAvailable(true, "РџСЂРѕРґРѕР»Р¶РёС‚СЊ: " + profile.displayName() + " | С„РѕСЂРјР° " + savedStats.form());
    }

    private boolean canSaveCurrentRun() {
        return gameState != GameState.MENU
                && gameState != GameState.WIN
                && gameState != GameState.LOSE;
    }

    private SaveData createSaveData() {
        Stats stats = player.stats();
        return new SaveData(
                player.profile().id(),
                locationManager.currentLocation(),
                player.position().x(),
                player.position().y(),
                calendarState.currentDay(),
                stats.strength(),
                stats.muscle(),
                stats.stamina(),
                stats.fatigue(),
                stats.money(),
                stats.bodyFat(),
                player.activeSupplements().activeTypes(),
                player.currentForm(),
                player.purchasedSupplements(),
                achievementManager.completedAchievements(),
                dailyQuestManager.saveData()
        );
    }

    private void update(double deltaSeconds) {
        updateCoachSpeech(deltaSeconds);
        updateClothesChangeFade(deltaSeconds);

        switch (gameState) {
            case PLAYING, COMPETITION -> {
                movementService.movePlayer(player, inputState, currentMap(), deltaSeconds);
                nearbyObject = interactionService.findNearbyObject(player, currentMap());
            }
            case COMPETITION_INTRO -> {
                nearbyObject = Optional.empty();
                if (activeCompetitionIntro.isPresent()) {
                    CompetitionIntroCutscene cutscene = activeCompetitionIntro.get();
                    cutscene.update(deltaSeconds);
                    if (cutscene.isFinished()) {
                        finishCompetitionIntro();
                        return;
                    }
                }
            }
            case POSING_MINIGAME -> {
                nearbyObject = Optional.empty();
                if (activePosingMinigame.isPresent()) {
                    PosingMinigame minigame = activePosingMinigame.get();
                    minigame.update(deltaSeconds);
                    if (minigame.isFinished()) {
                        finishPosingMinigame();
                        return;
                    }
                }
            }
            case JUDGE_RESULTS -> {
                nearbyObject = Optional.empty();
                if (activeJudgeResultsCutscene.isPresent()) {
                    JudgeResultsCutscene cutscene = activeJudgeResultsCutscene.get();
                    cutscene.update(deltaSeconds);
                    if (cutscene.isFinished()) {
                        finishJudgeResultsCutscene();
                        return;
                    }
                }
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
            case RESULT, SHOP, DIALOGUE, COMPETITION_RESULT -> nearbyObject = Optional.empty();
            default -> nearbyObject = Optional.empty();
        }

        refreshUi();
    }

    private void render() {
        if (gameState == GameState.COMPETITION_INTRO && activeCompetitionIntro.isPresent()) {
            activeCompetitionIntro.get().render(
                    view.getGraphicsContext(),
                    view.getGraphicsContext().getCanvas().getWidth(),
                    view.getGraphicsContext().getCanvas().getHeight()
            );
            return;
        }

        if (gameState == GameState.POSING_MINIGAME && activePosingMinigame.isPresent()) {
            activePosingMinigame.get().render(
                    view.getGraphicsContext(),
                    view.getGraphicsContext().getCanvas().getWidth(),
                    view.getGraphicsContext().getCanvas().getHeight(),
                    renderer.debugCollisionsEnabled()
            );
            return;
        }

        if (gameState == GameState.JUDGE_RESULTS && activeJudgeResultsCutscene.isPresent()) {
            activeJudgeResultsCutscene.get().render(
                    view.getGraphicsContext(),
                    view.getGraphicsContext().getCanvas().getWidth(),
                    view.getGraphicsContext().getCanvas().getHeight()
            );
            return;
        }

        if (gameState == GameState.COMPETITION_RESULT && activeCompetitionResultScreen.isPresent()) {
            activeCompetitionResultScreen.get().render(
                    view.getGraphicsContext(),
                    view.getGraphicsContext().getCanvas().getWidth(),
                    view.getGraphicsContext().getCanvas().getHeight()
            );
            return;
        }

        renderer.render(
                view.getGraphicsContext(),
                currentMap(),
                player,
                nearbyObject,
                gameState,
                activeSkillCheck,
                pendingSuccessResult,
                workShiftForRender(),
                coachSpeech(),
                clothesChangeFadeAlpha()
        );
    }

    private void refreshUi() {
        view.updateHud(player, gameState, calendarState);

        view.setHudCompactMode(false);

        view.updateDailyQuests(dailyQuestManager.views(), gameState);

        view.setMainMenuVisible(gameState == GameState.MENU);
        view.setInteractionPrompt(buildPrompt());
        view.setStatusMessage(statusMessage);
    }

    private void showQuestNotifications(List<DailyQuestNotification> notifications) {
        for (DailyQuestNotification notification : notifications) {
            view.showDailyQuestCompletion(notification);
        }
    }

    private void checkWorkingLoadAchievements() {
        showQuestNotifications(achievementManager.checkWorkingLoads(player, currentWorkingLoads()));
    }

    private Map<MachineType, Integer> currentWorkingLoads() {
        Map<MachineType, Integer> workingLoads = AchievementManager.emptyWorkingLoads();
        for (GymObject object : currentMap().objects()) {
            if (object instanceof TrainingMachine machine) {
                workingLoads.put(machine.machineType(), trainingService.workingLoadValue(player, machine));
            }
        }
        return workingLoads;
    }

    private void handleKeyPressed(KeyCode keyCode) {
        if (keyCode == KeyCode.F3) {
            boolean debugEnabled = renderer.toggleDebugCollisions();
            statusMessage = debugEnabled
                    ? "Р РµР¶РёРј РѕС‚Р»Р°РґРєРё РєРѕР»Р»РёР·РёР№ РІРєР»СЋС‡С‘РЅ."
                    : "Р РµР¶РёРј РѕС‚Р»Р°РґРєРё РєРѕР»Р»РёР·РёР№ РІС‹РєР»СЋС‡РµРЅ.";
            refreshUi();
            return;
        }

        if (gameState == GameState.COMPETITION_INTRO) {
            if (keyCode == KeyCode.ENTER) {
                skipCompetitionIntro();
            } else if (keyCode == KeyCode.SPACE) {
                advanceCompetitionIntro();
            }
            return;
        }

        if (gameState == GameState.POSING_MINIGAME) {
            handlePosingMinigameKeyPressed(keyCode);
            return;
        }

        if (gameState == GameState.JUDGE_RESULTS) {
            if (keyCode == KeyCode.ENTER) {
                skipJudgeResultsCutscene();
            } else if (keyCode == KeyCode.SPACE) {
                advanceJudgeResultsCutscene();
            }
            return;
        }

        if (gameState == GameState.COMPETITION_RESULT) {
            if (keyCode == KeyCode.ENTER
                    || keyCode == KeyCode.SPACE
                    || keyCode == KeyCode.ESCAPE) {
                closeCompetitionResult();
            }
            return;
        }

        if (gameState == GameState.PAUSE) {
            if (keyCode == KeyCode.ESCAPE) {
                closePauseMenu();
            }
            return;
        }

        if (gameState == GameState.RESULT) {
            handleResultKeyPressed(keyCode);
            return;
        }

        if (gameState == GameState.MINIGAME) {
            handleMinigameKeyPressed(keyCode);
            return;
        }

        if (gameState == GameState.DIALOGUE) {
            handleDialogueKeyPressed(keyCode);
            return;
        }

        if (gameState == GameState.SHOP) {
            handleShopKeyPressed(keyCode);
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
                    showTutorialBeforeStart();
                } else if (gameState == GameState.LOSE || gameState == GameState.WIN) {
                    startNewRun();
                }
            }
            case ESCAPE -> {
                if (isExplorationState()) {
                    openPauseMenu();
                }
            }
            default -> {
            }
        }
    }

    private void handleKeyReleased(KeyCode keyCode) {
        if (gameState == GameState.MINIGAME
                || gameState == GameState.RESULT
                || gameState == GameState.DIALOGUE
                || gameState == GameState.COMPETITION_INTRO
                || gameState == GameState.POSING_MINIGAME
                || gameState == GameState.JUDGE_RESULTS
                || gameState == GameState.COMPETITION_RESULT) {
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

    private void handleMousePressed() {
        if (gameState == GameState.COMPETITION_INTRO) {
            advanceCompetitionIntro();
        } else if (gameState == GameState.JUDGE_RESULTS) {
            advanceJudgeResultsCutscene();
        } else if (gameState == GameState.COMPETITION_RESULT) {
            closeCompetitionResult();
        }
    }

    private void tryInteract() {
        if (!isExplorationState() || nearbyObject.isEmpty()) {
            if (isExplorationState() && tryWorkInteraction()) {
                return;
            }
            return;
        }

        if (tryWorkInteraction()) {
            return;
        }

        GymObject gymObject = nearbyObject.get();
        if (gymObject instanceof TrainingMachine trainingMachine) {
            if (isTooTiredForMinigame()) {
                statusMessage = "РЈСЃС‚Р°Р»РѕСЃС‚СЊ 100. Р’С‹ РјРѕР¶РµС‚Рµ С‚РѕР»СЊРєРѕ РјРµРґР»РµРЅРЅРѕ С…РѕРґРёС‚СЊ, РѕС‚РґС‹С…Р°С‚СЊ РёР»Рё СЃРїР°С‚СЊ. РўСЂРµРЅРёСЂРѕРІРєРё РЅРµРґРѕСЃС‚СѓРїРЅС‹.";
                refreshUi();
                return;
            }

            openWeightSelection(trainingMachine);
            return;
        }

        if (gymObject instanceof InteractiveZone zone) {
            switch (zone.zoneType()) {
                case COMPUTER, SHOP -> openShop();
                case BED -> sleepAtHome();
                case COACH -> talkToCoach();
                case DOOR -> openLocationMenu();
                case REST -> rest();
                case STAGE -> tryStage();
                case WORK -> startWork();
                default -> {
                    statusMessage = gymObject.interact();
                    refreshUi();
                }
            }
            return;
        }

        statusMessage = gymObject.interact();
        refreshUi();
    }

    private void openShop() {
        inputState.clear();
        gameState = GameState.SHOP;
        statusMessage = "РњР°РіР°Р·РёРЅ РѕС‚РєСЂС‹С‚.";
        view.showShop(
                player,
                supplementType -> {
                    ShopPurchaseResult result = shopService.buy(player, supplementType);
                    statusMessage = result.message();
                    if (result.success()) {
                        showQuestNotifications(dailyQuestManager.onPurchase(player, supplementType));
                    }
                    refreshUi();
                    return result;
                },
                this::closeShop
        );
        refreshUi();
    }

    private void closeShop() {
        view.hideOverlay();
        gameState = explorationStateForCurrentLocation();
        statusMessage = "Р’С‹ Р·Р°РєСЂС‹Р»Рё РјР°РіР°Р·РёРЅ.";
        refreshUi();
        view.requestGameFocus();
    }

    private void openLocationMenu() {
        if (locationManager.currentLocation() == LocationId.WORK && workShiftState.workerDressed()) {
            statusMessage = "РЎРЅР°С‡Р°Р»Р° Р·Р°РєРѕРЅС‡РёС‚Рµ СЃРјРµРЅСѓ Рё РїРµСЂРµРѕРґРµРЅСЊС‚РµСЃСЊ РІ РѕР±С‹С‡РЅСѓСЋ РѕРґРµР¶РґСѓ.";
            refreshUi();
            return;
        }

        inputState.clear();
        gameState = GameState.DIALOGUE;
        statusMessage = "Р’С‹Р±РµСЂРёС‚Рµ Р»РѕРєР°С†РёСЋ РґР»СЏ РїРµСЂРµС…РѕРґР°.";
        view.showLocationMenu(
                locationManager.currentLocation(),
                locationManager.availableDestinations(),
                this::handleLocationSelection,
                this::closeLocationMenu
        );
        refreshUi();
    }

    private void closeLocationMenu() {
        if (gameState != GameState.DIALOGUE) {
            return;
        }

        view.hideOverlay();
        gameState = explorationStateForCurrentLocation();
        nearbyObject = interactionService.findNearbyObject(player, currentMap());
        statusMessage = "РџРµСЂРµС…РѕРґ РѕС‚РјРµРЅС‘РЅ.";
        refreshUi();
        view.requestGameFocus();
    }

    private void travelToLocation(LocationId locationId) {
        inputState.clear();
        activeSkillCheck = Optional.empty();
        activeTrainingSession = Optional.empty();
        activeTrainingStartSnapshot = Optional.empty();
        pendingSuccessResult = Optional.empty();
        clearCompetitionFlow();
        clearCoachSpeech();
        view.hideOverlay();

        GameMap destinationMap = locationManager.travelTo(locationId);
        player.moveToSpawn(destinationMap);
        if (locationId != LocationId.WORK) {
            workShiftState.reset();
        }
        gameState = explorationStateFor(locationId);
        nearbyObject = Optional.empty();
        statusMessage = "Р’С‹ РїРµСЂРµС€Р»Рё РІ Р»РѕРєР°С†РёСЋ: " + locationId.displayName() + ".";
        refreshUi();
        view.requestGameFocus();
    }

    private void handleLocationSelection(LocationId locationId) {
        if (locationId == LocationId.STAGE) {
            tryOpenCompetitionStage();
            return;
        }

        travelToLocation(locationId);
    }

    private void tryOpenCompetitionStage() {
        Optional<PlayerFormDefinition> naturalStageRequirement = player.profile().strongestNaturalFormDefinition();
        if (naturalStageRequirement.isEmpty() || !naturalStageRequirement.get().isUnlockedFor(player)) {
            statusMessage = "РќР° СЃС†РµРЅСѓ РµС‰С‘ СЂР°РЅРѕ. РЎРЅР°С‡Р°Р»Р° РґРѕРІРµРґРё РїРµСЂСЃРѕРЅР°Р¶Р° РґРѕ РµРіРѕ РїРѕСЃР»РµРґРЅРµР№ РЅР°С‚СѓСЂР°Р»СЊРЅРѕР№ С„РѕСЂРјС‹.";
            view.showStackedMessageDialog(
                    "РџРѕРєР° Р Р°РЅРѕ",
                    "Р”Р»СЏ РІС‹С…РѕРґР° РЅР° СЃС†РµРЅСѓ РїРµСЂСЃРѕРЅР°Р¶ РґРѕР»Р¶РµРЅ РґРѕС‚СЏРЅСѓС‚СЊСЃСЏ РґРѕ СЃРІРѕРµР№ РїРѕСЃР»РµРґРЅРµР№ РЅР°С‚СѓСЂР°Р»СЊРЅРѕР№ С„РѕСЂРјС‹. "
                            + "РџРѕРєР° С…Р°СЂР°РєС‚РµСЂРёСЃС‚РёРє РЅРµРґРѕСЃС‚Р°С‚РѕС‡РЅРѕ.",
                    "РџРѕРЅСЏР»",
                    () -> {
                        statusMessage = "Р’С‹Р±РµСЂРёС‚Рµ Р»РѕРєР°С†РёСЋ РґР»СЏ РїРµСЂРµС…РѕРґР°.";
                        refreshUi();
                    }
            );
            refreshUi();
            return;
        }

        statusMessage = "Р¤РѕСЂРјР° РїРѕРґС…РѕРґРёС‚. РўС‹ СѓРІРµСЂРµРЅ, С‡С‚Рѕ С…РѕС‡РµС€СЊ РІС‹Р№С‚Рё РЅР° СЃС†РµРЅСѓ?";
        view.showConfirmationDialog(
                "Р’С‹С…РѕРґ РќР° РЎС†РµРЅСѓ",
                "РџРµСЂСЃРѕРЅР°Р¶ СѓР¶Рµ РґРѕС‚СЏРіРёРІР°РµС‚ РґРѕ РїРѕСЃР»РµРґРЅРµР№ РЅР°С‚СѓСЂР°Р»СЊРЅРѕР№ С„РѕСЂРјС‹. РўРѕС‡РЅРѕ РёРґС‘Рј РЅР° СЃС†РµРЅСѓ СЃРѕСЂРµРІРЅРѕРІР°РЅРёР№?",
                "Р”Р°, РІС‹Р№С‚Рё",
                "РќРµС‚, РЅР°Р·Р°Рґ",
                this::confirmCompetitionStageTravel,
                this::openLocationMenu
        );
        refreshUi();
    }

    private void confirmCompetitionStageTravel() {
        showQuestNotifications(dailyQuestManager.onStage(player));
        travelToLocation(LocationId.STAGE);
        startCompetitionIntro();
    }

    private void startCompetitionIntro() {
        inputState.clear();
        nearbyObject = Optional.empty();
        activePosingMinigame = Optional.empty();
        activeJudgeResultsCutscene = Optional.empty();
        activeCompetitionResultScreen = Optional.empty();
        latestCompetitionPerformance = Optional.empty();
        activeCompetitionIntro = Optional.of(new CompetitionIntroCutscene());
        gameState = GameState.COMPETITION_INTRO;
        statusMessage = "";
        refreshUi();
        view.requestGameFocus();
    }

    private void finishCompetitionIntro() {
        activeCompetitionIntro = Optional.empty();
        startPosingMinigame();
    }

    private void startPosingMinigame() {
        inputState.clear();
        nearbyObject = Optional.empty();
        PosingMinigame minigame = new PosingMinigame(poseAssetLoader);
        minigame.start(
                player,
                view.getGraphicsContext().getCanvas().getWidth(),
                view.getGraphicsContext().getCanvas().getHeight()
        );
        activePosingMinigame = Optional.of(minigame);
        gameState = GameState.POSING_MINIGAME;
        statusMessage = "";
        refreshUi();
        view.requestGameFocus();
    }

    private void finishPosingMinigame() {
        if (activePosingMinigame.isEmpty()) {
            return;
        }

        PerformanceResult result = activePosingMinigame.get().getResult();
        activePosingMinigame = Optional.empty();
        latestCompetitionPerformance = Optional.of(result);
        startJudgeResultsCutscene(result);
    }

    private void startJudgeResultsCutscene(PerformanceResult result) {
        inputState.clear();
        nearbyObject = Optional.empty();
        activeJudgeResultsCutscene = Optional.of(new JudgeResultsCutscene(result));
        gameState = GameState.JUDGE_RESULTS;
        statusMessage = "";
        refreshUi();
        view.requestGameFocus();
    }

    private void advanceJudgeResultsCutscene() {
        if (activeJudgeResultsCutscene.isEmpty()) {
            return;
        }

        JudgeResultsCutscene cutscene = activeJudgeResultsCutscene.get();
        cutscene.advance();
        if (cutscene.isFinished()) {
            finishJudgeResultsCutscene();
            return;
        }

        refreshUi();
    }

    private void skipJudgeResultsCutscene() {
        if (activeJudgeResultsCutscene.isEmpty()) {
            return;
        }

        activeJudgeResultsCutscene.get().skip();
        finishJudgeResultsCutscene();
    }

    private void finishJudgeResultsCutscene() {
        activeJudgeResultsCutscene = Optional.empty();
        PerformanceResult result = latestCompetitionPerformance.orElseGet(() -> new PerformanceResult(0, 0, 0, 0, 0, 0, 0, 0, false));
        activeCompetitionResultScreen = Optional.of(new CompetitionResultScreen(player.profile().displayName(), result));
        gameState = GameState.COMPETITION_RESULT;
        statusMessage = "";
        refreshUi();
        view.requestGameFocus();
    }

    private void closeCompetitionResult() {
        activeCompetitionResultScreen = Optional.empty();
        gameState = GameState.COMPETITION;
        nearbyObject = interactionService.findNearbyObject(player, currentMap());
        PerformanceResult result = latestCompetitionPerformance.orElse(null);
        statusMessage = result == null
                ? "РЎРѕСЂРµРІРЅРѕРІР°РЅРёРµ Р·Р°РІРµСЂС€РµРЅРѕ."
                : "РЎРѕСЂРµРІРЅРѕРІР°РЅРёРµ Р·Р°РІРµСЂС€РµРЅРѕ. РС‚РѕРіРѕРІС‹Р№ Р±Р°Р»Р»: " + result.totalScore() + "/10.";
        refreshUi();
        view.requestGameFocus();
    }

    private void advanceCompetitionIntro() {
        if (activeCompetitionIntro.isEmpty()) {
            return;
        }

        CompetitionIntroCutscene cutscene = activeCompetitionIntro.get();
        cutscene.advance();
        if (cutscene.isFinished()) {
            finishCompetitionIntro();
            return;
        }

        refreshUi();
    }

    private void skipCompetitionIntro() {
        if (activeCompetitionIntro.isEmpty()) {
            return;
        }

        activeCompetitionIntro.get().skip();
        finishCompetitionIntro();
    }

    private void handlePosingMinigameKeyPressed(KeyCode keyCode) {
        if (activePosingMinigame.isEmpty()) {
            return;
        }

        boolean consumed = activePosingMinigame.get().handleKeyPressed(keyCode);
        if (activePosingMinigame.get().isFinished()) {
            finishPosingMinigame();
            return;
        }

        if (consumed) {
            refreshUi();
        }
    }

    private void talkToCoach() {
        coachSpeechText = coachDialoguePool.nextPhrase();
        coachSpeechTimeLeft = COACH_SPEECH_DURATION_SECONDS;
        statusMessage = "РўСЂРµРЅРµСЂ РґРµР»РёС‚СЃСЏ СЃРѕРІРµС‚РѕРј.";
        refreshUi();
    }

    private void sleepAtHome() {
        // Form unlocks are checked only after a real sleep at home.
        advanceDayWithFatigueRecovery(player.stats().fatigue(), "Р’С‹ РІС‹СЃРїР°Р»РёСЃСЊ РґРѕРјР°.");
        if (gameState == GameState.LOSE) {
            return;
        }

        Optional<PlayerForm> unlockedForm = player.unlockFormAfterSleep();
        unlockedForm.ifPresent(form -> statusMessage += " РћС‚РєСЂС‹С‚Р° РЅРѕРІР°СЏ С„РѕСЂРјР°: " + form.displayName() + ".");
        refreshUi();
    }

    private void rest() {
        advanceDayWithFatigueRecovery(35, "Р’С‹ РѕС‚РґРѕС…РЅСѓР»Рё.");
    }

    private void advanceDayWithFatigueRecovery(int fatigueRecovery, String actionText) {
        if (calendarState.isLastDay()) {
            gameState = GameState.LOSE;
            statusMessage = "Р”РЅРё РїРѕРґРіРѕС‚РѕРІРєРё Р·Р°РєРѕРЅС‡РёР»РёСЃСЊ. Р’С‹ РЅРµ СѓСЃРїРµР»Рё РІС‹Р№С‚Рё РЅР° СЃС†РµРЅСѓ.";
            refreshUi();
            return;
        }

        int fatigueBefore = player.stats().fatigue();
        player.stats().reduceFatigue(fatigueRecovery);
        int restored = fatigueBefore - player.stats().fatigue();
        showQuestNotifications(dailyQuestManager.onRest(player, restored));
        showQuestNotifications(dailyQuestManager.onDayEnd(player));
        player.activeSupplements().clearDayLongEffects();
        calendarState.nextDay();
        dailyQuestManager.startNewDay(player, calendarState.currentDay());

        statusMessage = actionText + " РЈСЃС‚Р°Р»РѕСЃС‚СЊ -" + restored
                + ". РќР°СЃС‚СѓРїРёР» РґРµРЅСЊ " + calendarState.currentDay()
                + "/" + calendarState.maxDays() + ".";
        refreshUi();
    }

    private void tryStage() {
        int form = player.stats().form();
        int fatigue = player.stats().fatigue();
        showQuestNotifications(dailyQuestManager.onStage(player));

        if (form >= 100 && fatigue < 80) {
            gameState = GameState.WIN;
            statusMessage = "РџРѕР±РµРґР°! Р’С‹ РІС‹С€Р»Рё РЅР° СЃС†РµРЅСѓ. Р¤РѕСЂРјР°: "
                    + form + ", СѓСЃС‚Р°Р»РѕСЃС‚СЊ: " + fatigue + ". Enter - РЅР°С‡Р°С‚СЊ Р·Р°РЅРѕРІРѕ.";
        } else {
            gameState = GameState.LOSE;
            statusMessage = "РџРѕСЂР°Р¶РµРЅРёРµ. Р’С‹ РІС‹С€Р»Рё РЅР° СЃС†РµРЅСѓ СЃР»РёС€РєРѕРј СЂР°РЅРѕ. Р¤РѕСЂРјР°: "
                    + form + ", СѓСЃС‚Р°Р»РѕСЃС‚СЊ: " + fatigue + ". Enter - РЅР°С‡Р°С‚СЊ Р·Р°РЅРѕРІРѕ.";
        }

        inputState.clear();
        refreshUi();
    }

    private void startWork() {
        statusMessage = "РЎРЅР°С‡Р°Р»Р° РЅР°С‡РЅРёС‚Рµ СЃРјРµРЅСѓ РІ Р·РѕРЅРµ РїРѕРґ РІС‚РѕСЂРѕР№ Р±РѕР»СЊС€РѕР№ РїРѕР»РєРѕР№.";
        refreshUi();
    }

    private boolean tryWorkInteraction() {
        if (locationManager.currentLocation() != LocationId.WORK) {
            return false;
        }

        if (workShiftState.isNearShiftZone(player)) {
            if (workShiftState.carryingBox()) {
                statusMessage = "РЎРЅР°С‡Р°Р»Р° СЃРґР°Р№С‚Рµ РєРѕСЂРѕР±РєСѓ РІ РѕС‚РіСЂСѓР·РєСѓ, РїРѕС‚РѕРј РІРµСЂРЅРёС‚РµСЃСЊ Р·Р°РєРѕРЅС‡РёС‚СЊ СЃРјРµРЅСѓ.";
            } else if (workShiftState.workerDressed()) {
                workShiftState.endShift(player);
                startClothesChangeFade();
                statusMessage = "РЎРјРµРЅР° Р·Р°РєРѕРЅС‡РµРЅР°, РІС‹ РїРµСЂРµРѕРґРµР»РёСЃСЊ РѕР±СЂР°С‚РЅРѕ.";
            } else if (workShiftState.completed()) {
                statusMessage = "РЎРєР»Р°РґСЃРєР°СЏ СЃРјРµРЅР° СѓР¶Рµ РІС‹РїРѕР»РЅРµРЅР°: 10/10 РєРѕСЂРѕР±РѕРє, РЅР°РіСЂР°РґР° РїРѕР»СѓС‡РµРЅР°.";
            } else {
                workShiftState.start();
                startClothesChangeFade();
                statusMessage = "Р’С‹ РїРµСЂРµРѕРґРµР»РёСЃСЊ РІ СЂР°Р±РѕС‡СѓСЋ С„РѕСЂРјСѓ. Р’РѕР·СЊРјРёС‚Рµ РєРѕСЂРѕР±РєСѓ РІ РїСЂРёРµРјРєРµ Рё РѕС‚РЅРµСЃРёС‚Рµ 10 РєРѕСЂРѕР±РѕРє РІ РѕС‚РіСЂСѓР·РєСѓ.";
            }
            refreshUi();
            return true;
        }

        if (!workShiftState.active() || !workShiftState.workerDressed()) {
            return false;
        }

        if (workShiftState.takeBox(player)) {
            statusMessage = "РљРѕСЂРѕР±РєР° РІР·СЏС‚Р°. РќРµСЃРёС‚Рµ РµРµ РІ Р·РµР»РµРЅСѓСЋ Р·РѕРЅСѓ РѕС‚РіСЂСѓР·РєРё, РѕР±С…РѕРґСЏ РїРѕР»РєРё.";
            refreshUi();
            return true;
        }

        if (!workShiftState.deliverBox(player)) {
            return false;
        }

        if (workShiftState.completed()) {
            player.stats().addMoney(WorkShiftState.REWARD_MONEY);
            showQuestNotifications(dailyQuestManager.onWork(
                    player,
                    TrainingGrade.EXCELLENT,
                    WorkShiftState.REWARD_MONEY
            ));
            statusMessage = "РЎРєР»Р°РґСЃРєР°СЏ СЃРјРµРЅР° Р·Р°РІРµСЂС€РµРЅР°: 10/10 РєРѕСЂРѕР±РѕРє. Р”РµРЅСЊРіРё +"
                    + WorkShiftState.REWARD_MONEY + ".";
        } else {
            statusMessage = "РљРѕСЂРѕР±РєР° РґРѕСЃС‚Р°РІР»РµРЅР°: " + workShiftState.deliveredBoxes()
                    + "/" + WorkShiftState.TARGET_BOXES + ". Р’РѕР·РІСЂР°С‰Р°Р№С‚РµСЃСЊ Рє РїСЂРёРµРјРєРµ.";
        }

        refreshUi();
        return true;
    }

    private void openWeightSelection(TrainingMachine machine) {
        inputState.clear();
        gameState = GameState.MINIGAME;
        activeSkillCheck = Optional.empty();
        activeTrainingSession = Optional.empty();
        activeTrainingStartSnapshot = Optional.empty();
        pendingSuccessResult = Optional.empty();
        statusMessage = "Р’С‹Р±РµСЂРёС‚Рµ РІРµСЃ РґР»СЏ С‚СЂРµРЅРёСЂРѕРІРєРё.";
        view.showTrainingSetup(
                machine,
                trainingService.workingLoadLabel(player, machine),
                weight -> trainingService.weightChoiceLabel(player, machine, weight),
                weight -> startTraining(machine, weight),
                () -> {
                    gameState = GameState.PLAYING;
                    statusMessage = "РўСЂРµРЅРёСЂРѕРІРєР° РѕС‚РјРµРЅРµРЅР°.";
                    activeTrainingStartSnapshot = Optional.empty();
                    view.hideOverlay();
                    refreshUi();
                    view.requestGameFocus();
                }
        );
        refreshUi();
    }

    private int scaledActivityFatigue(int baseFatigue) {
        return Math.max(1, (int) Math.ceil(baseFatigue * ACTIVITY_STAMINA_COST_MULTIPLIER));
    }

    private void startTraining(TrainingMachine machine, TrainingWeight weight) {
        view.hideOverlay();
        activeTrainingStartSnapshot = Optional.of(DailyQuestSnapshot.from(player));
        TrainingSession session = trainingService.createSession(player, machine, weight);
        if (usesSkillCheck(machine.machineType())) {
            startSkillCheck(session);
            return;
        }

        Node minigame = createMinigame(session);
        statusMessage = "РўСЂРµРЅРёСЂРѕРІРєР° РЅР°С‡Р°Р»Р°СЃСЊ: " + machine.name() + ", РЅР°РіСЂСѓР·РєР°: " + session.weightLabel() + ".";
        view.showOverlay(minigame);
        refreshUi();
        minigame.requestFocus();
    }

    private Node createMinigame(TrainingSession session) {
        MachineType machineType = session.machine().machineType();
        if (machineType == MachineType.BENCH_PRESS) {
            return new BalanceBarMinigame(session, result -> finishTraining(session, result));
        }
        if (machineType == MachineType.DEADLIFT_PLATFORM) {
            return new PowerMeterMinigame(session, result -> finishTraining(session, result));
        }
        throw new IllegalArgumentException("Unsupported minigame: " + machineType);
    }

    private void finishTraining(TrainingSession session, MinigameResult result) {
        DailyQuestSnapshot startSnapshot = activeTrainingStartSnapshot.orElse(DailyQuestSnapshot.from(player));
        activeTrainingStartSnapshot = Optional.empty();
        TrainingOutcome outcome = trainingService.finishTraining(player, session, result);
        showQuestNotifications(dailyQuestManager.onTraining(
                player,
                session,
                result.grade(),
                startSnapshot,
                trainingService.workingLoadValue(player, session.machine())
        ));
        checkWorkingLoadAchievements();
        statusMessage = outcome.message();
        view.hideOverlay();

        if (player.stats().fatigue() >= 100) {
            statusMessage += " РЈСЃС‚Р°Р»РѕСЃС‚СЊ РґРѕСЃС‚РёРіР»Р° 100: РґР°Р»СЊС€Рµ С‚СЂРµРЅРёСЂРѕРІР°С‚СЊСЃСЏ РЅРµР»СЊР·СЏ, РЅСѓР¶РЅРѕ РІРѕСЃСЃС‚Р°РЅРѕРІРёС‚СЊСЃСЏ.";
        }

        openTrainingResult(result.grade() != TrainingGrade.FAIL, result.grade(), outcome);

        refreshUi();
        view.requestGameFocus();
    }

    private String buildPrompt() {
        if (gameState == GameState.COMPETITION_INTRO
                || gameState == GameState.POSING_MINIGAME
                || gameState == GameState.JUDGE_RESULTS
                || gameState == GameState.COMPETITION_RESULT) {
            return "";
        }

        if (gameState == GameState.RESULT && pendingSuccessResult.isPresent()) {
            return "Space РёР»Рё Esc - Р·Р°РєСЂС‹С‚СЊ РѕРєРЅРѕ СЂРµР·СѓР»СЊС‚Р°С‚Р°.";
        }

        if (gameState == GameState.MINIGAME && activeSkillCheck.isPresent()) {
            SkillCheckSession session = activeSkillCheck.get();
            if (session.isSequenceMode()) {
                return "РќР°Р¶РёРјР°Р№С‚Рµ Р±СѓРєРІС‹ РёР· РѕС‡РµСЂРµРґРё. Esc - РѕС‚РјРµРЅР° РїРѕРґС…РѕРґР°.";
            }
            return "РќР°Р¶РјРёС‚Рµ РїРѕРєР°Р·Р°РЅРЅСѓСЋ РєР»Р°РІРёС€Сѓ РІ Р·РµР»С‘РЅРѕР№ Р·РѕРЅРµ. Esc - РѕС‚РјРµРЅР° РїРѕРґС…РѕРґР°.";
        }

        if (gameState == GameState.DIALOGUE) {
            return "Р’С‹Р±РµСЂРёС‚Рµ Р»РѕРєР°С†РёСЋ РјС‹С€СЊСЋ РёР»Рё РЅР°Р¶РјРёС‚Рµ Esc РґР»СЏ РѕС‚РјРµРЅС‹.";
        }

        if (gameState == GameState.PLAYING && locationManager.currentLocation() == LocationId.WORK) {
            return workShiftState.prompt(player);
        }

        return interactionService.buildPrompt(nearbyObject, gameState);
    }

    private Optional<WorkShiftState> workShiftForRender() {
        if (locationManager.currentLocation() == LocationId.WORK) {
            return Optional.of(workShiftState);
        }
        return Optional.empty();
    }

    private void handleDialogueKeyPressed(KeyCode keyCode) {
        if (keyCode == KeyCode.ESCAPE) {
            closeLocationMenu();
        }
    }

    private void handleShopKeyPressed(KeyCode keyCode) {
        if (keyCode == KeyCode.ESCAPE) {
            closeShop();
        }
    }

    private void handleMinigameKeyPressed(KeyCode keyCode) {
        if (activeSkillCheck.isEmpty()) {
            if (keyCode == KeyCode.ESCAPE) {
                gameState = GameState.PLAYING;
                statusMessage = "Р”РµР№СЃС‚РІРёРµ РѕС‚РјРµРЅРµРЅРѕ.";
                activeTrainingStartSnapshot = Optional.empty();
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
            if (skillCheckService.isTimingInputKey(keyCode)) {
                resolveTimingSkillCheck(keyCode);
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
        SkillCheckSession session = skillCheckService.startSession(trainingSession, player.stats().strength());
        activeSkillCheck = Optional.of(session);
        gameState = GameState.MINIGAME;
        statusMessage = buildSkillCheckStartMessage(session) + " РќР°РіСЂСѓР·РєР°: " + trainingSession.weightLabel() + ".";
        refreshUi();
    }

    private void resolveTimingSkillCheck(KeyCode keyCode) {
        if (activeSkillCheck.isEmpty()) {
            return;
        }

        SkillCheckSession session = activeSkillCheck.get();
        boolean hit = skillCheckService.isExpectedTimingKey(session, keyCode)
                && session.isMarkerInsideSuccessZone();

        boolean completed = skillCheckService.registerTimingAttempt(session, hit, player.stats().strength());
        if (!completed) {
            statusMessage = skillCheckService.buildTimingProgressMessage(session);
            refreshUi();
            return;
        }

        finishSkillCheck(skillCheckService.resolveTimingResult(session));
    }

    private void finishSkillCheck(SkillCheckResult result) {
        TrainingSession trainingSession = activeTrainingSession.orElse(null);
        if (trainingSession == null) {
            player.stats().applyDeltas(
                    result.strengthDelta(),
                    result.muscleDelta(),
                    result.staminaDelta(),
                    result.fatigueDelta(),
                    result.moneyDelta(),
                    result.bodyFatDelta()
            );
            activeSkillCheck = Optional.empty();
            gameState = GameState.PLAYING;
            statusMessage = result.message();
            refreshUi();
            return;
        }

        activeSkillCheck = Optional.empty();
        activeTrainingSession = Optional.empty();
        DailyQuestSnapshot startSnapshot = activeTrainingStartSnapshot.orElse(DailyQuestSnapshot.from(player));
        activeTrainingStartSnapshot = Optional.empty();
        TrainingGrade grade = result.grade();
        String details = result.message();
        String machinePrefix = trainingSession.machine().name() + ": ";
        if (details.startsWith(machinePrefix)) {
            details = details.substring(machinePrefix.length());
        }
        TrainingOutcome outcome = trainingService.finishTraining(
                player,
                trainingSession,
                new MinigameResult(grade, details)
        );
        showQuestNotifications(dailyQuestManager.onTraining(
                player,
                trainingSession,
                grade,
                startSnapshot,
                trainingService.workingLoadValue(player, trainingSession.machine())
        ));
        checkWorkingLoadAchievements();
        SkillCheckResult displayResult = new SkillCheckResult(
                grade != TrainingGrade.FAIL,
                grade,
                outcome.message(),
                outcome.finalReward().strength(),
                outcome.finalReward().muscle(),
                outcome.finalReward().stamina(),
                outcome.finalReward().fatigue(),
                outcome.finalReward().bodyFat()
        );

        if (player.stats().fatigue() >= 100) {
            statusMessage = outcome.message() + " РЈСЃС‚Р°Р»РѕСЃС‚СЊ РґРѕСЃС‚РёРіР»Р° 100: С‚СЂРµРЅРёСЂРѕРІРєРё Р·Р°Р±Р»РѕРєРёСЂРѕРІР°РЅС‹ РґРѕ РІРѕСЃСЃС‚Р°РЅРѕРІР»РµРЅРёСЏ.";
            displayResult = new SkillCheckResult(
                    grade != TrainingGrade.FAIL,
                    grade,
                    statusMessage,
                    outcome.finalReward().strength(),
                    outcome.finalReward().muscle(),
                    outcome.finalReward().stamina(),
                    outcome.finalReward().fatigue(),
                    outcome.finalReward().bodyFat()
            );
        }

        openSuccessResult(displayResult);
    }

    private void handleResultKeyPressed(KeyCode keyCode) {
        if (keyCode == KeyCode.SPACE || keyCode == KeyCode.ESCAPE) {
            closeSuccessResult();
        }
    }

    private void openTrainingResult(boolean success, TrainingGrade grade, TrainingOutcome outcome) {
        String message = statusMessage == null || statusMessage.isBlank()
                ? outcome.message()
                : statusMessage;
        openSuccessResult(new SkillCheckResult(
                success,
                grade,
                message,
                outcome.finalReward().strength(),
                outcome.finalReward().muscle(),
                outcome.finalReward().stamina(),
                outcome.finalReward().fatigue(),
                outcome.finalReward().bodyFat()
        ));
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
        nearbyObject = interactionService.findNearbyObject(player, currentMap());
        statusMessage = result.message();
        refreshUi();
        view.requestGameFocus();
    }

    private String buildSkillCheckStartMessage(SkillCheckSession session) {
        if (session.isSequenceMode()) {
            return session.machine().name()
                    + ": РЅР°Р±РёСЂР°Р№С‚Рµ СЃРёРјРІРѕР»С‹ РїРѕ РїРѕСЂСЏРґРєСѓ. Р’РµСЂРЅС‹Рµ РєР»Р°РІРёС€Рё Р·Р°РїРѕР»РЅСЏСЋС‚ С€РєР°Р»Сѓ, РѕС€РёР±РєРё СЃСЂРµР·Р°СЋС‚ РїСЂРѕРіСЂРµСЃСЃ.";
        }

        if (session.requiresMultipleHits()) {
            if (session.machine().machineType() == MachineType.TREADMILL) {
                return session.machine().name()
                        + ": РЅР°Р¶РёРјР°Р№С‚Рµ РїРѕРєР°Р·Р°РЅРЅС‹Рµ РєР»Р°РІРёС€Рё РІ Р·РµР»С‘РЅРѕР№ Р·РѕРЅРµ. Р¦РµР»СЊ: "
                        + session.requiredHits()
                        + " РїРѕРїР°РґР°РЅРёР№ РёР· "
                        + session.maxAttempts()
                        + " РїРѕРїС‹С‚РѕРє.";
            }

            return session.machine().name()
                    + ": РїРѕРїР°РґРёС‚Рµ РІ Р·РµР»С‘РЅСѓСЋ Р·РѕРЅСѓ "
                    + session.requiredHits()
                    + " СЂР°Р· РїРѕРґСЂСЏРґ.";
        }

        return session.machine().name() + ": РѕСЃС‚Р°РЅРѕРІРёС‚Рµ РјР°СЂРєРµСЂ РІ Р·РµР»С‘РЅРѕР№ Р·РѕРЅРµ, С‡С‚РѕР±С‹ Р·Р°СЃС‡РёС‚Р°С‚СЊ РїРѕРґС…РѕРґ.";
    }

    private void cancelSkillCheck() {
        if (activeSkillCheck.isEmpty()) {
            return;
        }

        activeSkillCheck = Optional.empty();
        activeTrainingSession = Optional.empty();
        activeTrainingStartSnapshot = Optional.empty();
        gameState = GameState.PLAYING;
        nearbyObject = interactionService.findNearbyObject(player, currentMap());
        statusMessage = "РџРѕРґС…РѕРґ РѕС‚РјРµРЅС‘РЅ. РњРѕР¶РЅРѕ РїРѕРїСЂРѕР±РѕРІР°С‚СЊ РµС‰С‘ СЂР°Р·.";
        refreshUi();
    }

    private void openPauseMenu() {
        inputState.clear();
        gameState = GameState.PAUSE;
        statusMessage = "РџР°СѓР·Р°. РњРѕР¶РЅРѕ СЃРѕС…СЂР°РЅРёС‚СЊСЃСЏ, РІРµСЂРЅСѓС‚СЊСЃСЏ Рє РІС‹Р±РѕСЂСѓ РїРµСЂСЃРѕРЅР°Р¶Р° РёР»Рё РїСЂРѕРґРѕР»Р¶РёС‚СЊ.";
        view.showPauseMenu(
                this::saveCurrentRunFromPause,
                this::returnToMainMenu,
                this::closePauseMenu
        );
        refreshUi();
    }

    private void returnToMainMenu() {
        inputState.clear();
        activeSkillCheck = Optional.empty();
        activeTrainingSession = Optional.empty();
        activeTrainingStartSnapshot = Optional.empty();
        pendingSuccessResult = Optional.empty();
        nearbyObject = Optional.empty();
        clearCompetitionFlow();
        clearCoachSpeech();
        gameState = GameState.MENU;
        statusMessage = "Р’С‹ РІРµСЂРЅСѓР»РёСЃСЊ Рє РІС‹Р±РѕСЂСѓ РїРµСЂСЃРѕРЅР°Р¶Р°. РЎРѕС…СЂР°РЅРµРЅРёРµ РІС‹РїРѕР»РЅСЏРµС‚СЃСЏ С‚РѕР»СЊРєРѕ РїРѕ РєРЅРѕРїРєРµ РІ РїР°СѓР·Рµ.";
        view.hideOverlay();
        view.hideTutorial();
        refreshContinueButton();
        refreshUi();
    }

    private void closePauseMenu() {
        inputState.clear();
        gameState = explorationStateForCurrentLocation();
        nearbyObject = interactionService.findNearbyObject(player, currentMap());
        statusMessage = "РРіСЂР° РїСЂРѕРґРѕР»Р¶РµРЅР°.";
        view.hideOverlay();
        refreshUi();
        view.requestGameFocus();
    }

    private boolean usesSkillCheck(MachineType machineType) {
        return !trainingService.isSupportedMinigame(machineType);
    }

    private boolean isTooTiredForMinigame() {
        return player.stats().fatigue() >= 100;
    }

    private Optional<String> coachSpeech() {
        if (!SHOW_COACH_SPEECH) {
            return Optional.empty();
        }
        if (coachSpeechText == null || coachSpeechText.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(coachSpeechText);
    }

    private void updateCoachSpeech(double deltaSeconds) {
        if (coachSpeechText == null || coachSpeechText.isBlank()) {
            return;
        }

        coachSpeechTimeLeft -= deltaSeconds;
        if (coachSpeechTimeLeft <= 0.0) {
            clearCoachSpeech();
        }
    }

    private void startClothesChangeFade() {
        clothesChangeFadeTimeLeft = CLOTHES_CHANGE_FADE_SECONDS;
    }

    private void updateClothesChangeFade(double deltaSeconds) {
        if (clothesChangeFadeTimeLeft <= 0) {
            return;
        }
        clothesChangeFadeTimeLeft = Math.max(0, clothesChangeFadeTimeLeft - deltaSeconds);
    }

    private double clothesChangeFadeAlpha() {
        if (clothesChangeFadeTimeLeft <= 0) {
            return 0;
        }
        double progress = clothesChangeFadeTimeLeft / CLOTHES_CHANGE_FADE_SECONDS;
        return progress * 0.96;
    }

    private void clearCoachSpeech() {
        coachSpeechText = "";
        coachSpeechTimeLeft = 0.0;
    }

    private boolean isExplorationState() {
        return gameState == GameState.PLAYING || gameState == GameState.COMPETITION;
    }

    private void clearCompetitionFlow() {
        activeCompetitionIntro = Optional.empty();
        activePosingMinigame = Optional.empty();
        activeJudgeResultsCutscene = Optional.empty();
        activeCompetitionResultScreen = Optional.empty();
        latestCompetitionPerformance = Optional.empty();
    }

    private GameState explorationStateForCurrentLocation() {
        return explorationStateFor(locationManager.currentLocation());
    }

    private GameState explorationStateFor(LocationId locationId) {
        return locationId == LocationId.STAGE ? GameState.COMPETITION : GameState.PLAYING;
    }

    private GameMap currentMap() {
        return locationManager.currentMap();
    }
}
