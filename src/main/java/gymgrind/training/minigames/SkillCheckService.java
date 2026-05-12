package gymgrind.training.minigames;

import gymgrind.training.MachineType;
import gymgrind.training.TrainingMachine;
import gymgrind.training.TrainingSession;
import gymgrind.training.TrainingTuning;
import javafx.scene.input.KeyCode;

import java.util.concurrent.ThreadLocalRandom;

public final class SkillCheckService {

    private static final double MIN_MARKER_PROGRESS = 0.0;
    private static final double MAX_MARKER_PROGRESS = 1.0;

    private static final int TREADMILL_REQUIRED_HITS = 6;
    private static final double TREADMILL_SUCCESS_ZONE_SHRINK = 0.018;
    private static final double TREADMILL_MIN_SUCCESS_ZONE_WIDTH = 0.09;

    private static final int SQUAT_PROMPT_LENGTH = 7;
    private static final double SQUAT_START_BAR_PROGRESS = 0.45;
    private static final double SQUAT_DRAIN_PER_SECOND = 0.14;
    private static final double SQUAT_CORRECT_GAIN = 0.15;
    private static final double SQUAT_WRONG_PENALTY = 0.22;
    private static final char[] SQUAT_SYMBOLS = {'A', 'S', 'D', 'F', 'J', 'K', 'L'};

    public SkillCheckSession startSession(TrainingSession trainingSession, int strength) {
        TrainingMachine machine = trainingSession.machine();
        TrainingTuning tuning = trainingSession.tuning();
        return switch (machine.machineType()) {
            case SQUAT_RACK -> SkillCheckSession.sequenceBar(
                    machine,
                    randomSequencePrompt(Math.max(SQUAT_PROMPT_LENGTH, tuning.rhythmLength())),
                    SQUAT_START_BAR_PROGRESS,
                    SQUAT_DRAIN_PER_SECOND * tuning.speedMultiplier(),
                    SQUAT_CORRECT_GAIN,
                    SQUAT_WRONG_PENALTY * Math.sqrt(tuning.speedMultiplier())
            );
            default -> startTimingSession(trainingSession, strength);
        };
    }

    public void update(SkillCheckSession session, double deltaSeconds) {
        if (session.isTimingMode()) {
            updateTimingSession(session, deltaSeconds);
            return;
        }

        double nextBarProgress = Math.max(0.0, session.barProgress() - session.drainPerSecond() * deltaSeconds);
        session.setBarProgress(nextBarProgress);
    }

    public boolean registerSuccessfulHit(SkillCheckSession session, int strength) {
        session.registerSuccessfulHit();
        if (session.isCompleted()) {
            return true;
        }

        prepareNextTimingHit(session, strength);
        return false;
    }

    public Character mapSequenceKey(KeyCode keyCode) {
        return switch (keyCode) {
            case A -> 'A';
            case S -> 'S';
            case D -> 'D';
            case F -> 'F';
            case J -> 'J';
            case K -> 'K';
            case L -> 'L';
            default -> null;
        };
    }

    public boolean registerSequenceInput(SkillCheckSession session, char inputSymbol) {
        if (inputSymbol == session.expectedSequenceSymbol()) {
            double nextBarProgress = Math.min(1.0, session.barProgress() + session.correctGain());
            session.setBarProgress(nextBarProgress);
            if (!session.isSequenceCompleted()) {
                session.advanceSequence(randomSequenceSymbol());
            }
            return true;
        }

        double nextBarProgress = Math.max(0.0, session.barProgress() - session.wrongPenalty());
        session.setBarProgress(nextBarProgress);
        return false;
    }

    public SkillCheckResult resolveSuccess(SkillCheckSession session) {
        return switch (session.machine().machineType()) {
            case BENCH_PRESS -> new SkillCheckResult(
                    true,
                    session.machine().name() + ": серия собрана. Сила +2, масса +1, усталость +6.",
                    2,
                    1,
                    0,
                    6,
                    0
            );
            case SQUAT_RACK -> new SkillCheckResult(
                    true,
                    session.machine().name() + ": подход дожат. Сила +1, масса +2, выносливость +1, усталость +8.",
                    1,
                    2,
                    1,
                    8,
                    0
            );
            case TREADMILL -> new SkillCheckResult(
                    true,
                    session.machine().name() + ": интервальный бег выдержан. Выносливость +4, усталость +7, % жира -2.",
                    0,
                    0,
                    4,
                    7,
                    -2
            );
            case DEADLIFT_PLATFORM -> new SkillCheckResult(
                    true,
                    session.machine().name() + ": мощный подъём. Сила +2, масса +1, выносливость +1, усталость +9.",
                    2,
                    1,
                    1,
                    9,
                    0
            );
        };
    }

    public SkillCheckResult resolveFailure(SkillCheckSession session) {
        return switch (session.machine().machineType()) {
            case BENCH_PRESS -> new SkillCheckResult(
                    false,
                    session.machine().name()
                            + ": серия сорвана на "
                            + session.completedHits()
                            + "/"
                            + session.requiredHits()
                            + ". Прогресса нет, усталость +5.",
                    0,
                    0,
                    0,
                    5,
                    0
            );
            case SQUAT_RACK -> new SkillCheckResult(
                    false,
                    session.machine().name() + ": ритм развалился, полоса опустела. Прогресса нет, усталость +7.",
                    0,
                    0,
                    0,
                    7,
                    0
            );
            case TREADMILL -> new SkillCheckResult(
                    false,
                    session.machine().name()
                            + ": темп сорван на "
                            + session.completedHits()
                            + "/"
                            + session.requiredHits()
                            + ". Прогресса нет, усталость +2.",
                    0,
                    0,
                    0,
                    2,
                    0
            );
            case DEADLIFT_PLATFORM -> new SkillCheckResult(
                    false,
                    session.machine().name() + ": мимо зелёной зоны. Прогресса нет, усталость +7.",
                    0,
                    0,
                    0,
                    7,
                    0
            );
        };
    }

    public String buildTimingProgressMessage(SkillCheckSession session) {
        if (!session.requiresMultipleHits()) {
            return session.machine().name() + ": попадание засчитано.";
        }

        if (session.machine().machineType() == MachineType.TREADMILL) {
            return session.machine().name()
                    + ": интервал "
                    + session.completedHits()
                    + "/"
                    + session.requiredHits()
                    + ". Ещё "
                    + session.remainingHits()
                    + " точных интервалов. Зона стала уже.";
        }

        return session.machine().name()
                + ": "
                + session.completedHits()
                + "/"
                + session.requiredHits()
                + ". Ещё "
                + session.remainingHits()
                + " точных повторов. Зона стала уже.";
    }

    public String buildSequenceProgressMessage(SkillCheckSession session, boolean correct) {
        int percent = (int) Math.round(session.barProgress() * 100);
        if (correct) {
            return session.machine().name()
                    + ": верно. Следующий символ "
                    + session.expectedSequenceSymbol()
                    + ". Полоса "
                    + percent
                    + "%.";
        }

        return session.machine().name()
                + ": ошибка. Нужен символ "
                + session.expectedSequenceSymbol()
                + ". Полоса "
                + percent
                + "%.";
    }

    private SkillCheckSession startTimingSession(TrainingSession trainingSession, int strength) {
        TrainingMachine machine = trainingSession.machine();
        TrainingTuning tuning = trainingSession.tuning();
        double successZoneWidth = clamp(
                successZoneWidthFor(machine.machineType()) * tuning.zoneMultiplier(),
                0.08,
                0.24
        );
        double markerSpeedMultiplier = tuning.speedMultiplier();
        return SkillCheckSession.timingZone(
                machine,
                randomMarkerProgress(),
                randomMarkerVelocity(machine.machineType(), strength, markerSpeedMultiplier),
                randomSuccessZoneStart(successZoneWidth),
                successZoneWidth,
                markerSpeedMultiplier,
                TREADMILL_SUCCESS_ZONE_SHRINK,
                TREADMILL_MIN_SUCCESS_ZONE_WIDTH * tuning.zoneMultiplier(),
                requiredHitsFor(machine.machineType(), tuning),
                0
        );
    }

    private void updateTimingSession(SkillCheckSession session, double deltaSeconds) {
        double nextMarkerProgress = session.markerProgress() + session.markerVelocity() * deltaSeconds;
        double nextMarkerVelocity = session.markerVelocity();

        while (nextMarkerProgress < MIN_MARKER_PROGRESS || nextMarkerProgress > MAX_MARKER_PROGRESS) {
            if (nextMarkerProgress < MIN_MARKER_PROGRESS) {
                nextMarkerProgress = -nextMarkerProgress;
                nextMarkerVelocity = Math.abs(nextMarkerVelocity);
            } else {
                nextMarkerProgress = 2 - nextMarkerProgress;
                nextMarkerVelocity = -Math.abs(nextMarkerVelocity);
            }
        }

        session.setMarkerProgress(nextMarkerProgress);
        session.setMarkerVelocity(nextMarkerVelocity);
    }

    private void prepareNextTimingHit(SkillCheckSession session, int strength) {
        if (session.machine().machineType() == MachineType.TREADMILL) {
            double nextWidth = Math.max(
                    session.minSuccessZoneWidth(),
                    session.successZoneWidth() - session.successZoneShrink()
            );
            session.setSuccessZoneWidth(nextWidth);
        }

        session.setSuccessZoneStart(randomSuccessZoneStart(session.successZoneWidth()));
        session.setMarkerProgress(randomMarkerProgress());
        session.setMarkerVelocity(randomMarkerVelocity(
                session.machine().machineType(),
                strength,
                session.markerSpeedMultiplier()
        ));
    }

    private int requiredHitsFor(MachineType machineType, TrainingTuning tuning) {
        return switch (machineType) {
            case TREADMILL -> Math.max(TREADMILL_REQUIRED_HITS, tuning.rhythmLength());
            default -> 1;
        };
    }

    private double successZoneWidthFor(MachineType machineType) {
        return switch (machineType) {
            case BENCH_PRESS -> 0.19;
            case TREADMILL -> 0.16;
            case DEADLIFT_PLATFORM -> 0.14;
            case SQUAT_RACK -> 0.17;
        };
    }

    private double markerSpeedFor(MachineType machineType, int strength) {
        return switch (machineType) {
            case BENCH_PRESS -> 1.00;
            case TREADMILL -> 1.55;
            case DEADLIFT_PLATFORM -> 1.12;
            case SQUAT_RACK -> 1.00;
        };
    }

    private double randomSuccessZoneStart(double successZoneWidth) {
        return ThreadLocalRandom.current().nextDouble(0.12, 0.88 - successZoneWidth);
    }

    private double randomMarkerProgress() {
        return ThreadLocalRandom.current().nextDouble(0.08, 0.92);
    }

    private double randomMarkerVelocity(MachineType machineType, int strength, double speedMultiplier) {
        double markerVelocity = markerSpeedFor(machineType, strength) * speedMultiplier;
        if (ThreadLocalRandom.current().nextBoolean()) {
            markerVelocity *= -1;
        }
        return markerVelocity;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    private String randomSequencePrompt(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            builder.append(randomSequenceSymbol());
        }
        return builder.toString();
    }

    private char randomSequenceSymbol() {
        int index = ThreadLocalRandom.current().nextInt(SQUAT_SYMBOLS.length);
        return SQUAT_SYMBOLS[index];
    }
}
