package mousemaze.generator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import mousemaze.model.Action;
import mousemaze.model.CellType;
import mousemaze.model.Maze;
import mousemaze.model.Position;

/**
 * Генератор лабиринта методом поиска в глубину с откатом (recursive backtracker).
 *
 * Лабиринт из R×C «комнат» хранится в сетке (2R+1)×(2C+1):
 * комнаты стоят в клетках с нечётными координатами, между ними — стены,
 * которые алгоритм «пробивает», переходя к непосещённому соседу.
 *
 * Рекурсия заменена явным стеком, чтобы большие лабиринты не вызывали
 * StackOverflowError.
 */
public class MazeGenerator {

    private final Random random;

    public MazeGenerator(Random random) {
        this.random = random;
    }

    /**
     * @param roomRows     число комнат по вертикали
     * @param roomCols     число комнат по горизонтали
     * @param loopFraction доля внутренних стен, которые сносятся после DFS (0 = идеальный лабиринт)
     * @param waterCount   сколько клеток с водой расставить
     * @param trapCount    сколько ловушек расставить
     */
    public Maze generate(int roomRows, int roomCols, double loopFraction, int waterCount, int trapCount) {
        if (roomRows < 1 || roomCols < 1) {
            throw new IllegalArgumentException("Размер должен быть не меньше 1×1 комнаты");
        }
        Maze maze = new Maze(2 * roomRows + 1, 2 * roomCols + 1, CellType.WALL);
        carveByDfs(maze, new Position(1, 1));
        if (loopFraction > 0) {
            addLoops(maze, loopFraction);
        }
        // Сыр — в левом верхнем углу, мышь — в правом нижнем (как на картинке в задании).
        maze.set(1, 1, CellType.CHEESE);
        maze.set(maze.rows() - 2, maze.cols() - 2, CellType.START);
        placeItems(maze, waterCount, trapCount);
        return maze;
    }

    /** Шаги 1–5 со слайда: посетить, пойти к непосещённому соседу, при тупике — откат. */
    private void carveByDfs(Maze maze, Position from) {
        boolean[][] visited = new boolean[maze.rows()][maze.cols()];
        Deque<Position> stack = new ArrayDeque<>();
        visited[from.row()][from.col()] = true;
        maze.set(from, CellType.EMPTY);
        stack.push(from);

        List<Action> directions = new ArrayList<>(List.of(Action.ALL));
        while (!stack.isEmpty()) {
            Position current = stack.peek();
            Collections.shuffle(directions, random);
            Position next = null;
            Position wall = null;
            for (Action a : directions) {
                Position candidate = new Position(current.row() + 2 * a.dRow(), current.col() + 2 * a.dCol());
                if (isInnerRoom(maze, candidate) && !visited[candidate.row()][candidate.col()]) {
                    next = candidate;
                    wall = current.move(a);
                    break;
                }
            }
            if (next == null) {
                stack.pop(); // все соседи посещены — откат
                continue;
            }
            maze.set(wall, CellType.EMPTY);
            maze.set(next, CellType.EMPTY);
            visited[next.row()][next.col()] = true;
            stack.push(next);
        }
    }

    private boolean isInnerRoom(Maze maze, Position p) {
        return p.row() > 0 && p.col() > 0 && p.row() < maze.rows() - 1 && p.col() < maze.cols() - 1;
    }

    /**
     * Сносит часть стен, разделяющих две комнаты. Появляются циклы,
     * т.е. несколько маршрутов от старта к сыру.
     */
    private void addLoops(Maze maze, double fraction) {
        List<Position> candidates = new ArrayList<>();
        for (int r = 1; r < maze.rows() - 1; r++) {
            for (int c = 1; c < maze.cols() - 1; c++) {
                if (maze.get(r, c) != CellType.WALL) {
                    continue;
                }
                boolean horizontalGap = r % 2 == 1 && c % 2 == 0
                        && maze.get(r, c - 1).isPassable() && maze.get(r, c + 1).isPassable();
                boolean verticalGap = r % 2 == 0 && c % 2 == 1
                        && maze.get(r - 1, c).isPassable() && maze.get(r + 1, c).isPassable();
                if (horizontalGap || verticalGap) {
                    candidates.add(new Position(r, c));
                }
            }
        }
        Collections.shuffle(candidates, random);
        int toRemove = (int) Math.round(candidates.size() * Math.min(1.0, fraction));
        for (int i = 0; i < toRemove; i++) {
            maze.set(candidates.get(i), CellType.EMPTY);
        }
    }

    /** Расставляет воду и ловушки по случайным пустым «комнатам». */
    private void placeItems(Maze maze, int waterCount, int trapCount) {
        List<Position> free = new ArrayList<>();
        for (int r = 1; r < maze.rows(); r += 2) {
            for (int c = 1; c < maze.cols(); c += 2) {
                if (maze.get(r, c) == CellType.EMPTY) {
                    free.add(new Position(r, c));
                }
            }
        }
        Collections.shuffle(free, random);
        int i = 0;
        for (int w = 0; w < waterCount && i < free.size(); w++) {
            maze.set(free.get(i++), CellType.WATER);
        }
        for (int t = 0; t < trapCount && i < free.size(); t++) {
            maze.set(free.get(i++), CellType.TRAP);
        }
    }
}
