package gymgrind.model;

public final class Player {

    private static final double DEFAULT_WIDTH = 34;
    private static final double DEFAULT_HEIGHT = 34;
    private static final double DEFAULT_SPEED = 250;

    private Position position;
    private final double width;
    private final double height;
    private final double speed;
    private final Stats stats;
    private final ActiveSupplements activeSupplements;

    private Player(Position position, double width, double height, double speed, Stats stats) {
        this.position = position;
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.stats = stats;
        this.activeSupplements = new ActiveSupplements();
    }

    public static Player createDefault(GameMap gameMap) {
        Position spawnPoint = new Position(gameMap.left() + 40, gameMap.bottom() - 90);
        return new Player(spawnPoint, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_SPEED, new Stats(10, 10, 10, 0, 0));
    }

    public void reset(GameMap gameMap) {
        position = new Position(gameMap.left() + 40, gameMap.bottom() - 90);
        stats.reset();
        activeSupplements.clear();
    }

    public Position position() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
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

    public double centerX() {
        return position.x() + width / 2.0;
    }

    public double centerY() {
        return position.y() + height / 2.0;
    }
}
