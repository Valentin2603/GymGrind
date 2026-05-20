package gymgrind.training.minigames;

import gymgrind.training.TrainingMachine;
import javafx.scene.input.KeyCode;

public final class SkillCheckSession {

    private final TrainingMachine machine;
    private final SkillCheckMode mode;
    private final int requiredHits;
    private final int maxAttempts;
    private double successZoneStart;
    private double successZoneWidth;
    private double markerProgress;
    private double markerVelocity;
    private double markerSpeedMultiplier;
    private double successZoneShrink;
    private double minSuccessZoneWidth;
    private int completedHits;
    private int timingAttempts;
    private KeyCode expectedTimingKey;
    private String sequencePrompt;
    private double barProgress;
    private double drainPerSecond;
    private double correctGain;
    private double wrongPenalty;
    private int sequenceCorrectInputs;
    private int sequenceWrongInputs;

    private SkillCheckSession(TrainingMachine machine,
                              SkillCheckMode mode,
                              int requiredHits,
                              int maxAttempts,
                              double successZoneStart,
                              double successZoneWidth,
                              double markerProgress,
                              double markerVelocity,
                              double markerSpeedMultiplier,
                              double successZoneShrink,
                              double minSuccessZoneWidth,
                              int completedHits,
                              int timingAttempts,
                              KeyCode expectedTimingKey,
                              String sequencePrompt,
                              double barProgress,
                              double drainPerSecond,
                              double correctGain,
                              double wrongPenalty,
                              int sequenceCorrectInputs,
                              int sequenceWrongInputs) {
        this.machine = machine;
        this.mode = mode;
        this.requiredHits = requiredHits;
        this.maxAttempts = maxAttempts;
        this.successZoneStart = successZoneStart;
        this.successZoneWidth = successZoneWidth;
        this.markerProgress = markerProgress;
        this.markerVelocity = markerVelocity;
        this.markerSpeedMultiplier = markerSpeedMultiplier;
        this.successZoneShrink = successZoneShrink;
        this.minSuccessZoneWidth = minSuccessZoneWidth;
        this.completedHits = completedHits;
        this.timingAttempts = timingAttempts;
        this.expectedTimingKey = expectedTimingKey;
        this.sequencePrompt = sequencePrompt;
        this.barProgress = barProgress;
        this.drainPerSecond = drainPerSecond;
        this.correctGain = correctGain;
        this.wrongPenalty = wrongPenalty;
        this.sequenceCorrectInputs = sequenceCorrectInputs;
        this.sequenceWrongInputs = sequenceWrongInputs;
    }

    public static SkillCheckSession timingZone(TrainingMachine machine,
                                               double markerProgress,
                                               double markerVelocity,
                                               double successZoneStart,
                                               double successZoneWidth,
                                               double markerSpeedMultiplier,
                                               double successZoneShrink,
                                               double minSuccessZoneWidth,
                                               int requiredHits,
                                               int maxAttempts,
                                               int completedHits,
                                               int timingAttempts,
                                               KeyCode expectedTimingKey) {
        return new SkillCheckSession(
                machine,
                SkillCheckMode.TIMING_ZONE,
                requiredHits,
                maxAttempts,
                successZoneStart,
                successZoneWidth,
                markerProgress,
                markerVelocity,
                markerSpeedMultiplier,
                successZoneShrink,
                minSuccessZoneWidth,
                completedHits,
                timingAttempts,
                expectedTimingKey,
                "",
                0.0,
                0.0,
                0.0,
                0.0,
                0,
                0
        );
    }

    public static SkillCheckSession sequenceBar(TrainingMachine machine,
                                                String sequencePrompt,
                                                double barProgress,
                                                double drainPerSecond,
                                                double correctGain,
                                                double wrongPenalty) {
        return new SkillCheckSession(
                machine,
                SkillCheckMode.SEQUENCE_BAR,
                0,
                0,
                0.0,
                0.0,
                0.0,
                0.0,
                1.0,
                0.0,
                0.0,
                0,
                0,
                KeyCode.SPACE,
                sequencePrompt,
                barProgress,
                drainPerSecond,
                correctGain,
                wrongPenalty,
                0,
                0
        );
    }

    public TrainingMachine machine() {
        return machine;
    }

    public SkillCheckMode mode() {
        return mode;
    }

    public boolean isTimingMode() {
        return mode == SkillCheckMode.TIMING_ZONE;
    }

    public boolean isSequenceMode() {
        return mode == SkillCheckMode.SEQUENCE_BAR;
    }

    public double markerProgress() {
        return markerProgress;
    }

    public void setMarkerProgress(double markerProgress) {
        this.markerProgress = markerProgress;
    }

    public double markerVelocity() {
        return markerVelocity;
    }

    public void setMarkerVelocity(double markerVelocity) {
        this.markerVelocity = markerVelocity;
    }

    public double markerSpeedMultiplier() {
        return markerSpeedMultiplier;
    }

    public double successZoneShrink() {
        return successZoneShrink;
    }

    public double minSuccessZoneWidth() {
        return minSuccessZoneWidth;
    }

    public double successZoneStart() {
        return successZoneStart;
    }

    public void setSuccessZoneStart(double successZoneStart) {
        this.successZoneStart = successZoneStart;
    }

    public double successZoneWidth() {
        return successZoneWidth;
    }

    public void setSuccessZoneWidth(double successZoneWidth) {
        this.successZoneWidth = successZoneWidth;
    }

    public double successZoneEnd() {
        return successZoneStart + successZoneWidth;
    }

    public int requiredHits() {
        return requiredHits;
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public int completedHits() {
        return completedHits;
    }

    public int timingAttempts() {
        return timingAttempts;
    }

    public int missedAttempts() {
        return Math.max(0, timingAttempts - completedHits);
    }

    public int remainingHits() {
        return Math.max(0, requiredHits - completedHits);
    }

    public int remainingAttempts() {
        return Math.max(0, maxAttempts - timingAttempts);
    }

    public boolean requiresMultipleHits() {
        return requiredHits > 1;
    }

    public void registerSuccessfulHit() {
        completedHits++;
    }

    public void registerTimingAttempt(boolean hit) {
        timingAttempts++;
        if (hit) {
            completedHits++;
        }
    }

    public boolean isCompleted() {
        return completedHits >= requiredHits;
    }

    public boolean isTimingCompleted() {
        return timingAttempts >= maxAttempts;
    }

    public boolean isMarkerInsideSuccessZone() {
        return markerProgress >= successZoneStart && markerProgress <= successZoneEnd();
    }

    public KeyCode expectedTimingKey() {
        return expectedTimingKey;
    }

    public void setExpectedTimingKey(KeyCode expectedTimingKey) {
        this.expectedTimingKey = expectedTimingKey;
    }

    public String sequencePrompt() {
        return sequencePrompt;
    }

    public void setSequencePrompt(String sequencePrompt) {
        this.sequencePrompt = sequencePrompt;
    }

    public char expectedSequenceSymbol() {
        return sequencePrompt.charAt(0);
    }

    public void advanceSequence(char nextSymbol) {
        sequencePrompt = sequencePrompt.substring(1) + nextSymbol;
    }

    public double barProgress() {
        return barProgress;
    }

    public void setBarProgress(double barProgress) {
        this.barProgress = barProgress;
    }

    public double drainPerSecond() {
        return drainPerSecond;
    }

    public double correctGain() {
        return correctGain;
    }

    public double wrongPenalty() {
        return wrongPenalty;
    }

    public void registerSequenceCorrectInput() {
        sequenceCorrectInputs++;
    }

    public void registerSequenceWrongInput() {
        sequenceWrongInputs++;
    }

    public int sequenceCorrectInputs() {
        return sequenceCorrectInputs;
    }

    public int sequenceWrongInputs() {
        return sequenceWrongInputs;
    }

    public int sequenceInputs() {
        return sequenceCorrectInputs + sequenceWrongInputs;
    }

    public boolean isSequenceCompleted() {
        return barProgress >= 1.0;
    }

    public boolean isSequenceDepleted() {
        return barProgress <= 0.0;
    }
}
