package gymgrind.model;

public final class SkillCheckSession {

    private final TrainingMachine machine;
    private final SkillCheckMode mode;
    private final int requiredHits;
    private double successZoneStart;
    private double successZoneWidth;
    private double markerProgress;
    private double markerVelocity;
    private int completedHits;
    private String sequencePrompt;
    private double barProgress;
    private double drainPerSecond;
    private double correctGain;
    private double wrongPenalty;

    private SkillCheckSession(TrainingMachine machine,
                              SkillCheckMode mode,
                              int requiredHits,
                              double successZoneStart,
                              double successZoneWidth,
                              double markerProgress,
                              double markerVelocity,
                              int completedHits,
                              String sequencePrompt,
                              double barProgress,
                              double drainPerSecond,
                              double correctGain,
                              double wrongPenalty) {
        this.machine = machine;
        this.mode = mode;
        this.requiredHits = requiredHits;
        this.successZoneStart = successZoneStart;
        this.successZoneWidth = successZoneWidth;
        this.markerProgress = markerProgress;
        this.markerVelocity = markerVelocity;
        this.completedHits = completedHits;
        this.sequencePrompt = sequencePrompt;
        this.barProgress = barProgress;
        this.drainPerSecond = drainPerSecond;
        this.correctGain = correctGain;
        this.wrongPenalty = wrongPenalty;
    }

    public static SkillCheckSession timingZone(TrainingMachine machine,
                                               double markerProgress,
                                               double markerVelocity,
                                               double successZoneStart,
                                               double successZoneWidth,
                                               int requiredHits,
                                               int completedHits) {
        return new SkillCheckSession(
                machine,
                SkillCheckMode.TIMING_ZONE,
                requiredHits,
                successZoneStart,
                successZoneWidth,
                markerProgress,
                markerVelocity,
                completedHits,
                "",
                0.0,
                0.0,
                0.0,
                0.0
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
                0.0,
                0.0,
                0.0,
                0.0,
                0,
                sequencePrompt,
                barProgress,
                drainPerSecond,
                correctGain,
                wrongPenalty
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

    public int completedHits() {
        return completedHits;
    }

    public int remainingHits() {
        return Math.max(0, requiredHits - completedHits);
    }

    public boolean requiresMultipleHits() {
        return requiredHits > 1;
    }

    public void registerSuccessfulHit() {
        completedHits++;
    }

    public boolean isCompleted() {
        return completedHits >= requiredHits;
    }

    public boolean isMarkerInsideSuccessZone() {
        return markerProgress >= successZoneStart && markerProgress <= successZoneEnd();
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

    public boolean isSequenceCompleted() {
        return barProgress >= 1.0;
    }

    public boolean isSequenceDepleted() {
        return barProgress <= 0.0;
    }
}
