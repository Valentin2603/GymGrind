package gymgrind.gym;

public record CollisionRect(double x, double y, double width, double height) {

    public double left() {
        return x;
    }

    public double top() {
        return y;
    }

    public double right() {
        return x + width;
    }

    public double bottom() {
        return y + height;
    }

    public CollisionRect translate(double dx, double dy) {
        return new CollisionRect(x + dx, y + dy, width, height);
    }

    public boolean contains(CollisionRect other) {
        return other.left() >= left()
                && other.top() >= top()
                && other.right() <= right()
                && other.bottom() <= bottom();
    }

    public boolean intersects(CollisionRect other) {
        return other.left() < right()
                && other.right() > left()
                && other.top() < bottom()
                && other.bottom() > top();
    }
}
