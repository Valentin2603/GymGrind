package gymgrind.model;

import javafx.scene.paint.Color;

public abstract class GymObject {

    private final String name;
    private final Position position;
    private final double width;
    private final double height;
    private final Color color;

    protected GymObject(String name, Position position, double width, double height, Color color) {
        this.name = name;
        this.position = position;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    public String name() {
        return name;
    }

    public Position position() {
        return position;
    }

    public double width() {
        return width;
    }

    public double height() {
        return height;
    }

    public Color color() {
        return color;
    }

    public double left() {
        return position.x();
    }

    public double top() {
        return position.y();
    }

    public double right() {
        return position.x() + width;
    }

    public double bottom() {
        return position.y() + height;
    }

    public double centerX() {
        return position.x() + width / 2.0;
    }

    public double centerY() {
        return position.y() + height / 2.0;
    }

    public abstract String shortTypeLabel();

    public abstract String interact();
}
