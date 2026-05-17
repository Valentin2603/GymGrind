package gymgrind.daily;

public final class DailyQuest {

    private final DailyQuestType type;
    private int progress;
    private boolean completed;

    public DailyQuest(DailyQuestType type) {
        this.type = type;
    }

    public DailyQuest(DailyQuestType type, int progress, boolean completed) {
        this.type = type;
        this.completed = completed;
        setProgress(completed ? type.target() : progress);
    }

    public DailyQuestType type() {
        return type;
    }

    public int progress() {
        return progress;
    }

    public boolean completed() {
        return completed;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(progress, type.target()));
    }

    public boolean markCompleted() {
        if (completed) {
            return false;
        }
        completed = true;
        progress = type.target();
        return true;
    }
}
