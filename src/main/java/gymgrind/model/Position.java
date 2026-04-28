package gymgrind.model;

public record Position(double x, double y) {

    public Position translate(double dx, double dy) {
        return new Position(x + dx, y + dy);
    }
}
