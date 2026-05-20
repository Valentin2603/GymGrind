package gymgrind.training;

public enum MachineType {
    BENCH_PRESS("Жим лёжа"),
    SQUAT_RACK("Присед"),
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
