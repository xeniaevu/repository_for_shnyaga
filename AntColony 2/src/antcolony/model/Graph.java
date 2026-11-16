package antcolony.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Среда для муравьёв — полный двунаправленный граф:
 * каждый узел соединён путём с каждым другим узлом.
 */
public class Graph {

    /** Размеры условного поля, на котором расположены узлы. */
    public static final double WIDTH = 1000;
    public static final double HEIGHT = 700;
    /** Отступ узлов от края поля. */
    public static final double MARGIN = 60;

    private final List<Node> nodes;
    private final List<Path> paths = new ArrayList<Path>();
    private final Path[][] matrix;

    public Graph(List<Node> nodes) {
        this.nodes = new ArrayList<Node>(nodes);
        int n = nodes.size();
        matrix = new Path[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Path path = new Path(nodes.get(i), nodes.get(j), 0);
                paths.add(path);
                matrix[i][j] = path;
                matrix[j][i] = path;
            }
        }
    }

    /** Узлы расположены по окружности: кратчайший маршрут очевиден — обход по контуру. */
    public static Graph createCircle(int nodeCount) {
        List<Node> nodes = new ArrayList<Node>();
        double radius = HEIGHT / 2 - MARGIN;
        for (int i = 0; i < nodeCount; i++) {
            double angle = 2 * Math.PI * i / nodeCount - Math.PI / 2;
            nodes.add(new Node(i,
                    WIDTH / 2 + radius * Math.cos(angle),
                    HEIGHT / 2 + radius * Math.sin(angle)));
        }
        return new Graph(nodes);
    }

    /** Узлы расположены случайно, но не слишком близко друг к другу. */
    public static Graph createRandom(int nodeCount, Random random) {
        List<Node> nodes = new ArrayList<Node>();
        double minDistance = Math.min(110, 600 / Math.sqrt(nodeCount));
        for (int i = 0; i < nodeCount; i++) {
            Node candidate = null;
            for (int attempt = 0; attempt < 500; attempt++) {
                candidate = new Node(i,
                        MARGIN + random.nextDouble() * (WIDTH - 2 * MARGIN),
                        MARGIN + random.nextDouble() * (HEIGHT - 2 * MARGIN));
                boolean farEnough = true;
                for (Node node : nodes) {
                    if (node.distanceTo(candidate) < minDistance) {
                        farEnough = false;
                        break;
                    }
                }
                if (farEnough) {
                    break;
                }
            }
            nodes.add(candidate);
        }
        return new Graph(nodes);
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public List<Path> getPaths() {
        return Collections.unmodifiableList(paths);
    }

    public int size() {
        return nodes.size();
    }

    public Node getNode(int index) {
        return nodes.get(index);
    }

    /** Путь между двумя узлами. */
    public Path getPath(Node a, Node b) {
        return matrix[a.getId()][b.getId()];
    }

    /** Задаёт одинаковое начальное количество феромона на всех путях. */
    public void resetPheromone(double value) {
        for (Path path : paths) {
            path.setPheromone(value);
        }
    }

    /** Суммарное количество феромона на всех путях. */
    public double getTotalPheromone() {
        double sum = 0;
        for (Path path : paths) {
            sum += path.getPheromone();
        }
        return sum;
    }
}
