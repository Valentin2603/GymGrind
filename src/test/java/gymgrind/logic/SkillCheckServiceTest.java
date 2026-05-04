package gymgrind.logic;

import gymgrind.model.MachineType;
import gymgrind.model.Position;
import gymgrind.model.SkillCheckResult;
import gymgrind.model.SkillCheckSession;
import gymgrind.model.TrainingMachine;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SkillCheckServiceTest {

    private final SkillCheckService service = new SkillCheckService();

    @Test
    void startBenchPressSessionRequiresMultipleHits() {
        SkillCheckSession session = service.startSession(machine(MachineType.BENCH_PRESS), 10);

        assertTrue(session.isTimingMode());
        assertEquals(5, session.requiredHits());
        assertEquals(0, session.completedHits());
        assertTrue(session.markerProgress() >= 0.0);
        assertTrue(session.markerProgress() <= 1.0);
        assertTrue(session.successZoneStart() >= 0.0);
        assertTrue(session.successZoneEnd() <= 1.0);
    }

    @Test
    void startSquatSessionUsesSequenceMode() {
        SkillCheckSession session = service.startSession(machine(MachineType.SQUAT_RACK), 10);

        assertTrue(session.isSequenceMode());
        assertEquals(5, session.sequencePrompt().length());
        assertTrue(session.barProgress() > 0.0);
    }

    @Test
    void benchPressGetsFasterWithMoreStrength() {
        SkillCheckSession lowStrength = service.startSession(machine(MachineType.BENCH_PRESS), 10);
        SkillCheckSession highStrength = service.startSession(machine(MachineType.BENCH_PRESS), 30);

        assertTrue(Math.abs(highStrength.markerVelocity()) > Math.abs(lowStrength.markerVelocity()));
    }

    @Test
    void updateBouncesTimingMarkerFromRightEdge() {
        SkillCheckSession session = SkillCheckSession.timingZone(
                machine(MachineType.TREADMILL),
                0.95,
                1.25,
                0.30,
                0.15,
                1,
                0
        );

        service.update(session, 0.10);

        assertTrue(session.markerProgress() < 1.0);
        assertTrue(session.markerVelocity() < 0.0);
    }

    @Test
    void updateDrainsSquatBarOverTime() {
        SkillCheckSession session = SkillCheckSession.sequenceBar(
                machine(MachineType.SQUAT_RACK),
                "ASDFJ",
                0.50,
                0.10,
                0.17,
                0.18
        );

        service.update(session, 1.0);

        assertTrue(session.barProgress() < 0.50);
    }

    @Test
    void correctSquatInputFillsBarAndAdvancesPrompt() {
        SkillCheckSession session = SkillCheckSession.sequenceBar(
                machine(MachineType.SQUAT_RACK),
                "ASDFJ",
                0.50,
                0.10,
                0.17,
                0.18
        );

        String initialPrompt = session.sequencePrompt();
        boolean correct = service.registerSequenceInput(session, 'A');

        assertTrue(correct);
        assertTrue(session.barProgress() > 0.50);
        assertEquals(initialPrompt.substring(1), session.sequencePrompt().substring(0, initialPrompt.length() - 1));
    }

    @Test
    void wrongSquatInputDrainsBarAndKeepsExpectedSymbol() {
        SkillCheckSession session = SkillCheckSession.sequenceBar(
                machine(MachineType.SQUAT_RACK),
                "ASDFJ",
                0.50,
                0.10,
                0.17,
                0.18
        );

        boolean correct = service.registerSequenceInput(session, 'K');

        assertFalse(correct);
        assertTrue(session.barProgress() < 0.50);
        assertEquals('A', session.expectedSequenceSymbol());
    }

    @Test
    void benchPressNeedsFullSeriesBeforeSuccess() {
        SkillCheckSession session = SkillCheckSession.timingZone(
                machine(MachineType.BENCH_PRESS),
                0.50,
                1.20,
                0.40,
                0.19,
                5,
                0
        );

        double initialWidth = session.successZoneWidth();

        assertFalse(service.registerSuccessfulHit(session, 12));
        assertEquals(1, session.completedHits());
        assertFalse(session.isCompleted());
        assertTrue(session.successZoneWidth() < initialWidth);

        assertFalse(service.registerSuccessfulHit(session, 12));
        assertFalse(service.registerSuccessfulHit(session, 12));
        assertFalse(service.registerSuccessfulHit(session, 12));

        assertTrue(service.registerSuccessfulHit(session, 12));
        assertTrue(session.isCompleted());

        SkillCheckResult result = service.resolveSuccess(session);

        assertTrue(result.success());
        assertEquals(2, result.strengthDelta());
        assertEquals(1, result.muscleDelta());
        assertEquals(6, result.fatigueDelta());
    }

    @Test
    void squatFailureUsesSpecificMessage() {
        SkillCheckSession session = SkillCheckSession.sequenceBar(
                machine(MachineType.SQUAT_RACK),
                "ASDFJ",
                0.0,
                0.10,
                0.17,
                0.18
        );

        SkillCheckResult result = service.resolveFailure(session);

        assertFalse(result.success());
        assertTrue(result.message().contains("полоса опустела"));
    }

    private TrainingMachine machine(MachineType machineType) {
        return new TrainingMachine(
                "Test machine",
                machineType,
                new Position(0, 0),
                100,
                40,
                Color.GRAY,
                "placeholder"
        );
    }
}
