package gymgrind.game;

import gymgrind.gym.CollisionRect;
import gymgrind.player.Player;

public final class WorkShiftState {

    public static final int TARGET_BOXES = 10;
    public static final int REWARD_MONEY = 220;
    public static final CollisionRect PICKUP_ZONE = new CollisionRect(150, 408, 74, 72);
    public static final CollisionRect DROP_ZONE = new CollisionRect(1060, 250, 130, 98);

    private static final double INTERACTION_DISTANCE = 82;

    private boolean active;
    private boolean carryingBox;
    private boolean completed;
    private int deliveredBoxes;

    public void reset() {
        active = false;
        carryingBox = false;
        completed = false;
        deliveredBoxes = 0;
    }

    public void start() {
        if (!completed) {
            active = true;
        }
    }

    public boolean active() {
        return active;
    }

    public boolean carryingBox() {
        return carryingBox;
    }

    public boolean completed() {
        return completed;
    }

    public int deliveredBoxes() {
        return deliveredBoxes;
    }

    public int boxesLeftAtPickup() {
        return Math.max(0, TARGET_BOXES - deliveredBoxes - (carryingBox ? 1 : 0));
    }

    public boolean isNearPickup(Player player) {
        return distanceTo(player, PICKUP_ZONE) <= INTERACTION_DISTANCE;
    }

    public boolean isNearDrop(Player player) {
        return distanceTo(player, DROP_ZONE) <= INTERACTION_DISTANCE;
    }

    public boolean takeBox(Player player) {
        if (!active || completed || carryingBox || !isNearPickup(player)) {
            return false;
        }

        carryingBox = true;
        return true;
    }

    public boolean deliverBox(Player player) {
        if (!active || completed || !carryingBox || !isNearDrop(player)) {
            return false;
        }

        carryingBox = false;
        deliveredBoxes++;
        if (deliveredBoxes >= TARGET_BOXES) {
            completed = true;
            active = false;
        }
        return true;
    }

    public String prompt(Player player) {
        if (completed) {
            return "Смена выполнена: 10/10 коробок, награда уже получена.";
        }
        if (!active) {
            return "Подойдите к зоне приемки и нажмите E, чтобы начать складскую смену.";
        }
        if (!carryingBox && isNearPickup(player)) {
            return "E - взять коробку. Прогресс: " + deliveredBoxes + "/" + TARGET_BOXES + ".";
        }
        if (carryingBox && isNearDrop(player)) {
            return "E - сдать коробку в отгрузку. Прогресс: " + deliveredBoxes + "/" + TARGET_BOXES + ".";
        }
        if (carryingBox) {
            return "Несите коробку в зеленую зону отгрузки, обходя полки.";
        }
        return "Идите к приемке за следующей коробкой. Прогресс: " + deliveredBoxes + "/" + TARGET_BOXES + ".";
    }

    private double distanceTo(Player player, CollisionRect zone) {
        double zoneCenterX = zone.left() + zone.width() / 2.0;
        double zoneCenterY = zone.top() + zone.height() / 2.0;
        return Math.hypot(player.centerX() - zoneCenterX, player.centerY() - zoneCenterY);
    }
}
