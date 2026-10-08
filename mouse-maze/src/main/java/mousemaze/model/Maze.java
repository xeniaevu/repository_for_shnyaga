package mousemaze.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Лабиринт — прямоугольная сетка клеток. Стены тоже являются клетками,
 * поэтому каждый ход мыши — это ровно одна клетка.
 * Старт и сыр в лабиринте единственные.
 */
public class Maze {

    private final int rows;
    private final int cols;
    private final CellType[][] cells;
    private Position start;
    private Position cheese;

    /** Создаёт поле, целиком заполненное указанным типом. */
    public Maze(int rows, int cols, CellType fill) {
        if (rows < 3 || cols < 3) {
            throw new IllegalArgumentException("Лабиринт должен быть не меньше 3×3");
        }
        this.rows = rows;
        this.cols = cols;
        this.cells = new CellType[rows][cols];
        for (CellType[] row : cells) {
            Arrays.fill(row, fill);
        }
    }

    /** Пустое поле с рамкой из стен — заготовка для ручного редактирования. */
    public static Maze blankWithBorder(int rows, int cols) {
        Maze m = new Maze(rows, cols, CellType.EMPTY);
        for (int r = 0; r < rows; r++) {
            m.cells[r][0] = CellType.WALL;
            m.cells[r][cols - 1] = CellType.WALL;
        }
        for (int c = 0; c < cols; c++) {
            m.cells[0][c] = CellType.WALL;
            m.cells[rows - 1][c] = CellType.WALL;
        }
        return m;
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public Position start() {
        return start;
    }

    public Position cheese() {
        return cheese;
    }

    public boolean inBounds(Position p) {
        return p.row() >= 0 && p.row() < rows && p.col() >= 0 && p.col() < cols;
    }

    public CellType get(Position p) {
        return cells[p.row()][p.col()];
    }

    public CellType get(int row, int col) {
        return cells[row][col];
    }

    /** Клетка внутри поля и не стена. */
    public boolean isPassable(Position p) {
        return inBounds(p) && get(p).isPassable();
    }

    /**
     * Устанавливает тип клетки. Старт и сыр могут быть только в одном месте:
     * при повторной установке старая клетка становится проходом.
     */
    public void set(Position p, CellType type) {
        CellType old = get(p);
        if (old == CellType.START) {
            start = null;
        }
        if (old == CellType.CHEESE) {
            cheese = null;
        }
        if (type == CellType.START) {
            if (start != null) {
                cells[start.row()][start.col()] = CellType.EMPTY;
            }
            start = p;
        }
        if (type == CellType.CHEESE) {
            if (cheese != null) {
                cells[cheese.row()][cheese.col()] = CellType.EMPTY;
            }
            cheese = p;
        }
        cells[p.row()][p.col()] = type;
    }

    public void set(int row, int col, CellType type) {
        set(new Position(row, col), type);
    }

    /** Все клетки указанного типа в порядке обхода строк. */
    public List<Position> positionsOf(CellType type) {
        List<Position> list = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c] == type) {
                    list.add(new Position(r, c));
                }
            }
        }
        return list;
    }

    public int count(CellType type) {
        return positionsOf(type).size();
    }

    /**
     * Длина кратчайшего пути от старта до сыра (поиск в ширину, ловушки проходимы).
     * Возвращает -1, если пути нет или старт/сыр не заданы.
     */
    public int shortestPathLength() {
        if (start == null || cheese == null) {
            return -1;
        }
        int[][] dist = new int[rows][cols];
        for (int[] d : dist) {
            Arrays.fill(d, -1);
        }
        ArrayDeque<Position> queue = new ArrayDeque<>();
        queue.add(start);
        dist[start.row()][start.col()] = 0;
        while (!queue.isEmpty()) {
            Position p = queue.poll();
            if (p.equals(cheese)) {
                return dist[p.row()][p.col()];
            }
            for (Action a : Action.ALL) {
                Position n = p.move(a);
                if (isPassable(n) && dist[n.row()][n.col()] < 0) {
                    dist[n.row()][n.col()] = dist[p.row()][p.col()] + 1;
                    queue.add(n);
                }
            }
        }
        return -1;
    }

    /**
     * Проверяет, что по лабиринту можно учиться. Возвращает текст ошибки или null.
     */
    public String validate() {
        if (start == null) {
            return "Не задан старт (S).";
        }
        if (cheese == null) {
            return "Не задан сыр (C).";
        }
        if (shortestPathLength() < 0) {
            return "Сыр недостижим из стартовой клетки.";
        }
        return null;
    }

    public Maze copy() {
        Maze m = new Maze(rows, cols, CellType.EMPTY);
        for (int r = 0; r < rows; r++) {
            m.cells[r] = cells[r].clone();
        }
        m.start = start;
        m.cheese = cheese;
        return m;
    }

    /** Текстовое представление в формате файла схемы. */
    public String toText() {
        StringBuilder sb = new StringBuilder();
        for (CellType[] row : cells) {
            for (CellType t : row) {
                sb.append(t.symbol());
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toText();
    }
}
