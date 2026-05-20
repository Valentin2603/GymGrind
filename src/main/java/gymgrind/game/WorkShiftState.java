package gymgrind.game;

import gymgrind.gym.CollisionRect;
import gymgrind.player.Player;

public final class WorkShiftState {

    public static final int TARGET_BOXES = 10;
    public static final int REWARD_MONEY = 200;
    public static final CollisionRect PICKUP_ZONE = new CollisionRect(150, 408, 74, 72);
    public static final CollisionRect DROP_ZONE = new CollisionRect(1046, 250, 90, 98);
    public static final CollisionRect SHIFT_ZONE = new CollisionRect(522, 392, 96, 64);

    private static final double INTERACTION_DISTANCE = 82;

    private boolean active;
    private boolean carryingBox;
    private boolean completed;
    private boolean workerDressed;
    private int deliveredBoxes;

    public void reset() {
        active = false;
        carryingBox = false;
        completed = false;
        workerDressed = false;
        deliveredBoxes = 0;
    }

    public void start() {
        if (!completed) {
            active = true;
            workerDressed = true;
        }
    }

    public boolean endShift(Player player) {
        if (!workerDressed || carryingBox || !isNearShiftZone(player)) {
            return false;
        }

        active = false;
        workerDressed = false;
        return true;
    }

    public boolean active() {
        return active;
    }

    public boolean carryingBox() {
        return carryingBox;
    }

    public boolean workerDressed() {
        return workerDressed;
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

    public boolean isNearShiftZone(Player player) {
        return distanceTo(player, SHIFT_ZONE) <= INTERACTION_DISTANCE;
    }

    public boolean takeBox(Player player) {
        if (!active || !workerDressed || completed || carryingBox || !isNearPickup(player)) {
            return false;
        }

        carryingBox = true;
        return true;
    }

    public boolean deliverBox(Player player) {
        if (!active || !workerDressed || completed || !carryingBox || !isNearDrop(player)) {
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
        if (isNearShiftZone(player)) {
            if (carryingBox) {
                return "Сначала сдайте коробку в отгрузку, потом вернитесь закончить смену.";
            }
            if (workerDressed) {
                return "E - закончить смену и переодеться.";
            }
            if (completed) {
                return "Смена выполнена: 10/10 коробок, награда уже получена.";
            }
            return "E - начать смену и переодеться в рабочую форму.";
        }
        if (completed) {
            if (workerDressed) {
                return "Смена выполнена: 10/10 коробок. Вернитесь в зону под второй полкой, чтобы переодеться.";
            }
            return "Смена выполнена: 10/10 коробок, награда уже получена.";
        }
        if (!active || !workerDressed) {
            return "Подойдите к зоне под второй полкой и нажмите E, чтобы начать смену и переодеться.";
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
