package wumpus.agent;

import wumpus.world.Action;
import wumpus.world.Direction;
import wumpus.world.Grid;
import wumpus.world.Position;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Поиск кратчайшего пути с учётом поворотов. Состояние = (клетка, направление),
 * каждое действие (Forward, TurnLeft, TurnRight) стоит 1 очко, поэтому хватает
 * поиска в ширину. Ходить можно только по клеткам, для которых passable == true.
 */
final class PathFinder {

    static final int UNREACHABLE = Integer.MAX_VALUE;

    private final Grid grid;
    private final int[][][] dist;
    private final int[][][] prev; // закодированное предыдущее состояние
    private final Action[][][] via;

    PathFinder(Grid grid, Position start, Direction startDir, Predicate<Position> passable) {
        this.grid = grid;
        int w = grid.width + 1, h = grid.height + 1;
        dist = new int[w][h][4];
        prev = new int[w][h][4];
        via = new Action[w][h][4];
        for (int[][] a : dist) for (int[] b : a) java.util.Arrays.fill(b, UNREACHABLE);

        ArrayDeque<int[]> queue = new ArrayDeque<>();
        dist[start.x()][start.y()][startDir.ordinal()] = 0;
        prev[start.x()][start.y()][startDir.ordinal()] = -1;
        queue.add(new int[]{start.x(), start.y(), startDir.ordinal()});

        while (!queue.isEmpty()) {
            int[] s = queue.poll();
            int x = s[0], y = s[1], d = s[2];
            int nd = dist[x][y][d] + 1;
            Direction dir = Direction.values()[d];

            relax(queue, x, y, dir.left().ordinal(), nd, encode(x, y, d), Action.TURN_LEFT);
            relax(queue, x, y, dir.right().ordinal(), nd, encode(x, y, d), Action.TURN_RIGHT);
            Position next = new Position(x, y).step(dir);
            if (grid.inside(next) && passable.test(next)) {
                relax(queue, next.x(), next.y(), d, nd, encode(x, y, d), Action.FORWARD);
            }
        }
    }

    private void relax(ArrayDeque<int[]> q, int x, int y, int d, int nd, int from, Action a) {
        if (dist[x][y][d] != UNREACHABLE) return;
        dist[x][y][d] = nd;
        prev[x][y][d] = from;
        via[x][y][d] = a;
        q.add(new int[]{x, y, d});
    }

    private int encode(int x, int y, int d) {
        return (x * (grid.height + 1) + y) * 4 + d;
    }

    /** Стоимость попасть в клетку p, смотря в направлении d. */
    int cost(Position p, Direction d) {
        return dist[p.x()][p.y()][d.ordinal()];
    }

    /** Стоимость попасть в клетку p с любым направлением. */
    int cost(Position p) {
        int best = UNREACHABLE;
        for (int d = 0; d < 4; d++) best = Math.min(best, dist[p.x()][p.y()][d]);
        return best;
    }

    /** Последовательность действий до клетки p (d == null — направление любое). */
    List<Action> actionsTo(Position p, Direction d) {
        int bestD = -1;
        if (d != null) {
            bestD = d.ordinal();
        } else {
            for (int k = 0; k < 4; k++) {
                if (dist[p.x()][p.y()][k] != UNREACHABLE
                        && (bestD < 0 || dist[p.x()][p.y()][k] < dist[p.x()][p.y()][bestD])) bestD = k;
            }
        }
        if (bestD < 0 || dist[p.x()][p.y()][bestD] == UNREACHABLE) return null;

        List<Action> actions = new ArrayList<>();
        int x = p.x(), y = p.y(), dd = bestD;
        while (prev[x][y][dd] != -1) {
            actions.add(via[x][y][dd]);
            int code = prev[x][y][dd];
            dd = code % 4;
            int xy = code / 4;
            x = xy / (grid.height + 1);
            y = xy % (grid.height + 1);
        }
        Collections.reverse(actions);
        return actions;
    }
}
