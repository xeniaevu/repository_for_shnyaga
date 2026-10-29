package wumpus.world;

import java.util.ArrayList;
import java.util.List;

/** Геометрия пещеры: размеры, соседство клеток. Её знают и среда, и агент. */
public final class Grid {

    public static final Position START = new Position(1, 1);

    public final int width;
    public final int height;

    public Grid(int width, int height) {
        if (width < 1 || height < 1) throw new IllegalArgumentException("Размер поля должен быть положительным");
        this.width = width;
        this.height = height;
    }

    public boolean inside(Position p) {
        return p.x() >= 1 && p.x() <= width && p.y() >= 1 && p.y() <= height;
    }

    /** Соседи по горизонтали и вертикали (не по диагонали). */
    public List<Position> neighbors(Position p) {
        List<Position> result = new ArrayList<>(4);
        for (Direction d : Direction.values()) {
            Position q = p.step(d);
            if (inside(q)) result.add(q);
        }
        return result;
    }

    public static boolean adjacent(Position a, Position b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y()) == 1;
    }

    /** Все клетки поля. */
    public List<Position> cells() {
        List<Position> result = new ArrayList<>(width * height);
        for (int y = 1; y <= height; y++) {
            for (int x = 1; x <= width; x++) result.add(new Position(x, y));
        }
        return result;
    }
}
