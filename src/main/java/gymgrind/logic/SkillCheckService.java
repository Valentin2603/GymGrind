package gymgrind.logic;

import gymgrind.model.MachineType;
import gymgrind.model.SkillCheckResult;
import gymgrind.model.SkillCheckSession;
import gymgrind.model.TrainingMachine;
import javafx.scene.input.KeyCode;

import java.util.concurrent.ThreadLocalRandom;

public final class SkillCheckService {

    private static final double MIN_MARKER_PROGRESS = 0.0;
    private static final double MAX_MARKER_PROGRESS = 1.0;

    private static final int BENCH_PRESS_REQUIRED_HITS = 5;
    private static final double BENCH_PRESS_SUCCESS_ZONE_SHRINK = 0.02;
    private static final double BENCH_PRESS_MIN_SUCCESS_ZONE_WIDTH = 0.11;
    private static final double BENCH_PRESS_STRENGTH_SPEED_FACTOR = 0.025;
    private static final double BENCH_PRESS_MAX_STRENGTH_BONUS = 0.75;

    private static final int SQUAT_PROMPT_LENGTH = 5;
    private static final double SQUAT_START_BAR_PROGRESS = 0.45;
    private static final double SQUAT_DRAIN_PER_SECOND = 0.11;
    private static final double SQUAT_CORRECT_GAIN = 0.17;
    private static final double SQUAT_WRONG_PENALTY = 0.18;
    private static final char[] SQUAT_SYMBOLS = {'A', 'S', 'D', 'F', 'J', 'K', 'L'};

    public SkillCheckSession startSession(TrainingMachine machine, int strength) {
        return switch (machine.machineType()) {
            case SQUAT_RACK -> SkillCheckSession.sequenceBar(
                    machine,
                    randomSequencePrompt(SQUAT_PROMPT_LENGTH),
                    SQUAT_START_BAR_PROGRESS,
                    SQUAT_DRAIN_PER_SECOND,
                    SQUAT_CORRECT_GAIN,
                    SQUAT_WRONG_PENALTY
            );
            default -> startTimingSession(machine, strength);
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
                    6
            );
            case SQUAT_RACK -> new SkillCheckResult(
                    true,
                    session.machine().name() + ": подход дожат. Сила +1, масса +2, выносливость +1, усталость +8.",
                    1,
                    2,
                    1,
                    8
            );
            case TREADMILL -> new SkillCheckResult(
                    true,
                    session.machine().name() + ": темп выдержан. Выносливость +2, усталость +4.",
                    0,
                    0,
                    2,
                    4
            );
            case DEADLIFT_PLATFORM -> new SkillCheckResult(
                    true,
                    session.machine().name() + ": мощный подъём. Сила +2, масса +1, выносливость +1, усталость +9.",
                    2,
                    1,
                    1,
                    9
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
                            + ". Прогресса нет, усталость +2.",
                    0,
                    0,
                    0,
                    2
            );
            case SQUAT_RACK -> new SkillCheckResult(
                    false,
                    session.machine().name() + ": ритм развалился, полоса опустела. Прогресса нет, усталость +2.",
                    0,
                    0,
                    0,
                    2
            );
            case TREADMILL -> new SkillCheckResult(
                    false,
                    session.machine().name() + ": темп сорван. Прогресса нет, усталость +1.",
                    0,
                    0,
                    0,
                    1
            );
            case DEADLIFT_PLATFORM -> new SkillCheckResult(
                    false,
                    session.machine().name() + ": мимо зелёной зоны. Прогресса нет, усталость +2.",
                    0,
                    0,
                    0,
                    2
            );
        };
    }

    public String buildTimingProgressMessage(SkillCheckSession session) {
        if (!session.requiresMultipleHits()) {
            return session.machine().name() + ": попадание засчитано.";
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

    private SkillCheckSession startTimingSession(TrainingMachine machine, int strength) {
        double successZoneWidth = successZoneWidthFor(machine.machineType());
        return SkillCheckSession.timingZone(
                machine,
                randomMarkerProgress(),
                randomMarkerVelocity(machine.machineType(), strength),
                randomSuccessZoneStart(successZoneWidth),
                successZoneWidth,
                requiredHitsFor(machine.machineType()),
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
        if (session.machine().machineType() == MachineType.BENCH_PRESS) {
            double nextWidth = Math.max(
                    BENCH_PRESS_MIN_SUCCESS_ZONE_WIDTH,
                    session.successZoneWidth() - BENCH_PRESS_SUCCESS_ZONE_SHRINK
            );
            session.setSuccessZoneWidth(nextWidth);
        }

        session.setSuccessZoneStart(randomSuccessZoneStart(session.successZoneWidth()));
        session.setMarkerProgress(randomMarkerProgress());
        session.setMarkerVelocity(randomMarkerVelocity(session.machine().machineType(), strength));
    }

    private int requiredHitsFor(MachineType machineType) {
        return switch (machineType) {
            case BENCH_PRESS -> BENCH_PRESS_REQUIRED_HITS;
            default -> 1;
        };
    }

    private double successZoneWidthFor(MachineType machineType) {
        return switch (machineType) {
            case BENCH_PRESS -> 0.19;
            case TREADMILL -> 0.15;
            case DEADLIFT_PLATFORM -> 0.14;
            case SQUAT_RACK -> 0.17;
        };
    }

    private double markerSpeedFor(MachineType machineType, int strength) {
        return switch (machineType) {
            case BENCH_PRESS -> 0.90 + Math.min(BENCH_PRESS_MAX_STRENGTH_BONUS, Math.max(0, strength) * BENCH_PRESS_STRENGTH_SPEED_FACTOR);
            case TREADMILL -> 1.25;
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

    private double randomMarkerVelocity(MachineType machineType, int strength) {
        double markerVelocity = markerSpeedFor(machineType, strength);
        if (ThreadLocalRandom.current().nextBoolean()) {
            markerVelocity *= -1;
        }
        return markerVelocity;
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
