package wumpus.world;

/** Направление взгляда агента. Порядок важен: поворот налево — следующий элемент. */
public enum Direction {
    EAST(1, 0, "восток"),
    NORTH(0, 1, "север"),
    WEST(-1, 0, "запад"),
    SOUTH(0, -1, "юг");

    public final int dx;
    public final int dy;
    public final String nameRu;

    Direction(int dx, int dy, String nameRu) {
        this.dx = dx;
        this.dy = dy;
        this.nameRu = nameRu;
    }

    public Direction left() {
        return values()[(ordinal() + 1) % 4];
    }

    public Direction right() {
        return values()[(ordinal() + 3) % 4];
    }

    /** Направление из клетки from в соседнюю клетку to, либо null, если они не соседние. */
    public static Direction between(Position from, Position to) {
        for (Direction d : values()) {
            if (from.step(d).equals(to)) return d;
        }
        return null;
    }
}
