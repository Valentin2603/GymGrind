package gymgrind.player;

import gymgrind.gym.CollisionRect;
import gymgrind.gym.GameMap;
import gymgrind.gym.Position;

public final class Player {

    private static final double DEFAULT_WIDTH = 34;
    private static final double DEFAULT_HEIGHT = 34;
    private static final double DEFAULT_SPEED = 250;
    private static final double FOOT_HITBOX_OFFSET_X = 8;
    private static final double FOOT_HITBOX_OFFSET_Y = 22;
    private static final double FOOT_HITBOX_WIDTH = 18;
    private static final double FOOT_HITBOX_HEIGHT = 10;

    private Position position;
    private PlayerDirection direction;
    private boolean moving;
    private final double width;
    private final double height;
    private final double speed;
    private final Stats stats;
    private final ActiveSupplements activeSupplements;
    private PlayerProfile profile;

    private Player(Position position, double width, double height, double speed, Stats stats, PlayerProfile profile) {
        this.position = position;
        this.direction = PlayerDirection.FRONT;
        this.moving = false;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.stats = stats;
        this.activeSupplements = new ActiveSupplements();
        this.profile = profile;
    }

    public static Player createDefault(GameMap gameMap) {
        PlayerProfile defaultProfile = PlayerProfiles.defaultProfile();
        return new Player(
                gameMap.spawnPoint(),
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                DEFAULT_SPEED,
                new Stats(
                        defaultProfile.baseStrength(),
                        defaultProfile.baseMuscle(),
                        defaultProfile.baseStamina(),
                        defaultProfile.baseFatigue(),
                        defaultProfile.baseMoney(),
                        defaultProfile.baseBodyFat()
                ),
                defaultProfile
        );
    }

    public void reset(GameMap gameMap) {
        position = gameMap.spawnPoint();
        direction = PlayerDirection.FRONT;
        moving = false;
        stats.reset();
        activeSupplements.clear();
    }

    public void moveToSpawn(GameMap gameMap) {
        position = gameMap.spawnPoint();
        direction = PlayerDirection.FRONT;
        moving = false;
    }

    public void applyProfile(PlayerProfile profile, GameMap gameMap) {
        this.profile = profile;
        stats.configureBaseValues(
                profile.baseStrength(),
                profile.baseMuscle(),
                profile.baseStamina(),
                profile.baseFatigue(),
                profile.baseMoney(),
                profile.baseBodyFat()
        );
        reset(gameMap);
    }

    public Position position() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public PlayerDirection direction() {
        return direction;
    }

    public void setDirection(PlayerDirection direction) {
        this.direction = direction;
    }

    public boolean isMoving() {
        return moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    public double speed() {
        return speed;
    }

    public Stats stats() {
        return stats;
    }

    public ActiveSupplements activeSupplements() {
        return activeSupplements;
    }

    public PlayerProfile profile() {
        return profile;
    }

    public double centerX() {
        return position.x() + width / 2.0;
    }

    public double centerY() {
        return position.y() + height / 2.0;
    }

    public CollisionRect footHitbox() {
        return footHitboxAt(position);
    }

    public CollisionRect footHitboxAt(Position position) {
        return new CollisionRect(
                position.x() + FOOT_HITBOX_OFFSET_X,
                position.y() + FOOT_HITBOX_OFFSET_Y,
                FOOT_HITBOX_WIDTH,
                FOOT_HITBOX_HEIGHT
        );
    }
}
