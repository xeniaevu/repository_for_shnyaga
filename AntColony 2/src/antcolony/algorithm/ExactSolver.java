package antcolony.algorithm;

import antcolony.model.Graph;

import java.util.Arrays;

/**
 * Точное решение задачи коммивояжёра методом динамического программирования
 * (алгоритм Хелда — Карпа). Нужен только для проверки: позволяет убедиться,
 * что маршрут, найденный муравьями, действительно кратчайший.
 */
public final class ExactSolver {

    /** Максимальное число узлов, для которого точное решение считается быстро. */
    public static final int MAX_NODES = 15;

    private ExactSolver() {
    }

    /** @return длина кратчайшего замкнутого маршрута или NaN, если узлов слишком много */
    public static double shortestTourLength(Graph graph) {
        int n = graph.size();
        if (n > MAX_NODES) {
            return Double.NaN;
        }
        if (n < 2) {
            return 0;
        }
        double[][] d = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                d[i][j] = graph.getNode(i).distanceTo(graph.getNode(j));
            }
        }
        // best[mask][j] — кратчайший путь из узла 0 через множество узлов mask с концом в j
        int full = 1 << n;
        double[][] best = new double[full][n];
        for (double[] row : best) {
            Arrays.fill(row, Double.POSITIVE_INFINITY);
        }
        best[1][0] = 0;
        for (int mask = 1; mask < full; mask += 2) {
            for (int last = 0; last < n; last++) {
                double current = best[mask][last];
                if (current == Double.POSITIVE_INFINITY) {
                    continue;
                }
                for (int next = 1; next < n; next++) {
                    if ((mask & (1 << next)) != 0) {
                        continue;
                    }
                    int nextMask = mask | (1 << next);
                    double candidate = current + d[last][next];
                    if (candidate < best[nextMask][next]) {
                        best[nextMask][next] = candidate;
                    }
                }
            }
        }
        double result = Double.POSITIVE_INFINITY;
        for (int last = 1; last < n; last++) {
            result = Math.min(result, best[full - 1][last] + d[last][0]);
        }
        return result;
    }
}
