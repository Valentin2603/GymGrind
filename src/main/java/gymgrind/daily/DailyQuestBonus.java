package gymgrind.daily;

import gymgrind.player.Player;

public record DailyQuestBonus(
        int money,
        int strength,
        int muscle,
        int stamina,
        int fatigueRecovery
) {
    public static DailyQuestBonus money(int money) {
        return new DailyQuestBonus(money, 0, 0, 0, 0);
    }

    public void apply(Player player) {
        player.stats().applyDeltas(strength, muscle, stamina, 0, money, 0);
        if (fatigueRecovery > 0) {
            player.stats().reduceFatigue(fatigueRecovery);
        }
    }

    public String description() {
        StringBuilder builder = new StringBuilder();
        append(builder, "деньги", money);
        append(builder, "сила", strength);
        append(builder, "масса", muscle);
        append(builder, "выносливость", stamina);
        append(builder, "усталость", -fatigueRecovery);
        return builder.isEmpty() ? "без бонуса" : builder.toString();
    }

    private void append(StringBuilder builder, String label, int value) {
        if (value == 0) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(", ");
        }
        builder.append(label).append(" ");
        if (value > 0) {
            builder.append("+");
        }
        builder.append(value);
    }
}
