package antcolony.model;

/**
 * Узел сети («город»), который должен посетить муравей.
 * Координаты заданы в условных единицах поля {@link Graph#WIDTH} x {@link Graph#HEIGHT}.
 */
public class Node {

    private final int id;
    private double x;
    private double y;

    public Node(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    /** Порядковый номер узла (с нуля). */
    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    /** Перемещает узел (используется при перетаскивании мышью — «изменение среды»). */
    public void moveTo(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }

    /** Евклидово расстояние до другого узла. */
    public double distanceTo(Node other) {
        return Math.hypot(x - other.x, y - other.y);
    }

    @Override
    public String toString() {
        return String.valueOf(id + 1);
    }
}
