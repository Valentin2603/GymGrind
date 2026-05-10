package gymgrind.model;

public enum MachineType {
    BENCH_PRESS("Силовой тренажёр"),
    SQUAT_RACK("База на ноги"),
    TREADMILL("Кардио зона"),
    DEADLIFT_PLATFORM("Платформа для тяги");

    private final String label;

    MachineType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
