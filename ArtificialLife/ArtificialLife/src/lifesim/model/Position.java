package lifesim.model;

import java.util.Objects;

/** Неизменяемая пара координат клетки (x — столбец, y — строка). */
public final class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    /**
     * Манхэттенское расстояние. Так как ходить можно только по вертикали/горизонтали,
     * оно равно минимальному числу ходов между клетками (без учёта препятствий).
     */
    public int manhattan(Position other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }

    public int manhattan(int ox, int oy) {
        return Math.abs(x - ox) + Math.abs(y - oy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode() { return Objects.hash(x, y); }

    @Override
    public String toString() { return "(" + x + ", " + y + ")"; }
}
