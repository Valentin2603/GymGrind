package gymgrind.player;

public enum PlayerForm {
    BASE("Base"),
    SECOND("Second"),
    THIRD("Third"),
    FOURTH("Fourth"),
    FOURTH_STEROIDS("FourthSteroids");

    private final String displayName;

    PlayerForm(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
