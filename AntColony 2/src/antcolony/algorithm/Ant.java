package antcolony.algorithm;

import antcolony.model.Graph;
import antcolony.model.Node;
import antcolony.model.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Муравей. За один цикл обходит все узлы графа по одному разу и возвращается в начальный узел.
 * Умеет выбирать следующий путь, перемещаться и откладывать феромон на пройденном маршруте.
 */
public class Ant {

    private final int id;
    private final Node startNode;
    private Node currentNode;
    private Node previousNode;
    private final boolean[] visited;
    private final List<Node> tour = new ArrayList<Node>();
    private final List<Path> tourPaths = new ArrayList<Path>();
    private double tourLength;

    public Ant(int id, Node startNode, int nodeCount) {
        this.id = id;
        this.startNode = startNode;
        this.visited = new boolean[nodeCount];
        reset();
    }

    /** Возвращает муравья в начальный узел и стирает его память о маршруте. */
    public void reset() {
        java.util.Arrays.fill(visited, false);
        tour.clear();
        tourPaths.clear();
        tourLength = 0;
        currentNode = startNode;
        previousNode = startNode;
        visited[startNode.getId()] = true;
        tour.add(startNode);
    }

    /**
     * Один шаг муравья: выбор следующего узла и переход в него.
     *
     * @return false, если маршрут уже завершён и шаг не сделан
     */
    public boolean step(Graph graph, Parameters params, Random random) {
        if (isTourComplete()) {
            return false;
        }
        moveTo(chooseNextNode(graph, params, random), graph);
        return true;
    }

    /**
     * Выбор следующего узла. Вероятность перехода из узла r в непосещённый узел u:
     * <pre>
     *            tau(r,u)^alpha * eta(r,u)^beta
     * P(r,u) = ----------------------------------- ,   eta(r,u) = 1 / d(r,u)
     *          сумма по всем непосещённым узлам
     * </pre>
     * Узел выбирается случайно пропорционально этой вероятности («рулетка»).
     * Когда все узлы посещены, муравей возвращается в начальный узел.
     */
    public Node chooseNextNode(Graph graph, Parameters params, Random random) {
        List<Node> candidates = new ArrayList<Node>();
        for (Node node : graph.getNodes()) {
            if (!visited[node.getId()]) {
                candidates.add(node);
            }
        }
        if (candidates.isEmpty()) {
            return startNode;
        }

        double[] weights = new double[candidates.size()];
        double total = 0;
        for (int i = 0; i < weights.length; i++) {
            Path path = graph.getPath(currentNode, candidates.get(i));
            weights[i] = Math.pow(path.getPheromone(), params.alpha)
                    * Math.pow(path.getVisibility(), params.beta);
            total += weights[i];
        }
        if (!(total > 0) || Double.isInfinite(total)) {
            // вырожденный случай: выбираем равновероятно
            return candidates.get(random.nextInt(candidates.size()));
        }

        double threshold = random.nextDouble() * total;
        double accumulated = 0;
        for (int i = 0; i < weights.length; i++) {
            accumulated += weights[i];
            if (threshold < accumulated) {
                return candidates.get(i);
            }
        }
        return candidates.get(candidates.size() - 1);
    }

    /** Перемещение муравья в соседний узел по соединяющему их пути. */
    public void moveTo(Node next, Graph graph) {
        Path path = graph.getPath(currentNode, next);
        tourPaths.add(path);
        tourLength += path.getLength();
        previousNode = currentNode;
        currentNode = next;
        visited[next.getId()] = true;
        tour.add(next);
    }

    /** Маршрут завершён, когда муравей посетил все узлы и вернулся в начало. */
    public boolean isTourComplete() {
        return tourPaths.size() == visited.length;
    }

    /** Муравей оставляет феромон на каждом пути своего маршрута: delta tau = Q / L. */
    public void depositPheromone(double q) {
        double delta = q / tourLength;
        for (Path path : tourPaths) {
            path.addDeposit(delta);
        }
    }

    public int getId() {
        return id;
    }

    public Node getStartNode() {
        return startNode;
    }

    /** Узел, в который муравей сейчас идёт (или в котором стоит). */
    public Node getCurrentNode() {
        return currentNode;
    }

    /** Узел, из которого муравей вышел на последнем шаге. */
    public Node getPreviousNode() {
        return previousNode;
    }

    /** Длина пройденного маршрута L. */
    public double getTourLength() {
        return tourLength;
    }

    public List<Node> getTour() {
        return Collections.unmodifiableList(tour);
    }

    public List<Path> getTourPaths() {
        return Collections.unmodifiableList(tourPaths);
    }
}
