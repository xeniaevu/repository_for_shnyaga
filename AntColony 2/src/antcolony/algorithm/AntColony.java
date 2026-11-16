package antcolony.algorithm;

import antcolony.model.Graph;
import antcolony.model.Node;
import antcolony.model.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Муравьиная колония — управляет циклами алгоритма «Муравьиная система» (Ant System).
 *
 * Один цикл:
 * 1) муравьи равномерно распределены по узлам;
 * 2) каждый муравей шаг за шагом строит замкнутый маршрут через все узлы;
 * 3) когда все муравьи закончили, обновляется феромон и выполняется испарение;
 * 4) алгоритм запускается повторно.
 */
public class AntColony {

    private final Graph graph;
    private final Parameters params;
    private final Random random;
    private final List<Ant> ants = new ArrayList<Ant>();

    private int iteration;
    private List<Node> bestTour;
    private List<Path> bestTourPaths;
    private double bestLength;
    private double iterationBestLength;
    private double iterationAverageLength;
    private int antsOnBestTour;
    private final List<Double> bestHistory = new ArrayList<Double>();
    private final List<Double> averageHistory = new ArrayList<Double>();

    public AntColony(Graph graph, Parameters params, Random random) {
        this.graph = graph;
        this.params = params;
        this.random = random;
        reset();
    }

    /** Полный перезапуск: феромон сбрасывается, муравьи создаются заново. */
    public void reset() {
        graph.resetPheromone(params.initialPheromone);
        ants.clear();
        for (int i = 0; i < params.antCount; i++) {
            // равномерное распределение муравьёв по узлам
            Node start = graph.getNode(i % graph.size());
            ants.add(new Ant(i, start, graph.size()));
        }
        iteration = 0;
        bestHistory.clear();
        averageHistory.clear();
        iterationBestLength = Double.NaN;
        iterationAverageLength = Double.NaN;
        forgetBestTour();
    }

    /** Забыть лучший маршрут (нужно, когда среда изменилась — узел передвинули). */
    public void forgetBestTour() {
        bestTour = null;
        bestTourPaths = null;
        bestLength = Double.POSITIVE_INFINITY;
        antsOnBestTour = 0;
    }

    /** Все ли муравьи завершили маршрут в текущем цикле. */
    public boolean isIterationComplete() {
        for (Ant ant : ants) {
            if (!ant.isTourComplete()) {
                return false;
            }
        }
        return true;
    }

    /** Все муравьи одновременно делают один шаг (переход в следующий узел). */
    public void stepAnts() {
        for (Ant ant : ants) {
            ant.step(graph, params, random);
        }
    }

    /**
     * Завершение цикла: подсчёт длин маршрутов, обновление и испарение феромона,
     * возврат муравьёв в начальные узлы.
     */
    public void finishIteration() {
        double sum = 0;
        Ant iterationBest = null;
        for (Ant ant : ants) {
            sum += ant.getTourLength();
            if (iterationBest == null || ant.getTourLength() < iterationBest.getTourLength()) {
                iterationBest = ant;
            }
        }
        iterationBestLength = iterationBest.getTourLength();
        iterationAverageLength = sum / ants.size();
        if (iterationBestLength < bestLength - 1e-9) {
            bestLength = iterationBestLength;
            bestTour = new ArrayList<Node>(iterationBest.getTour());
            bestTourPaths = new ArrayList<Path>(iterationBest.getTourPaths());
        }
        antsOnBestTour = 0;
        for (Ant ant : ants) {
            if (Math.abs(ant.getTourLength() - bestLength) <= 1e-9 * bestLength) {
                antsOnBestTour++;
            }
        }

        updatePheromone();

        iteration++;
        bestHistory.add(iterationBestLength);
        averageHistory.add(iterationAverageLength);
        for (Ant ant : ants) {
            ant.reset();
        }
    }

    /** Обновление феромона после того, как все муравьи закончили цикл, затем испарение. */
    private void updatePheromone() {
        for (Ant ant : ants) {
            ant.depositPheromone(params.q);
        }
        if (params.elitist && bestTourPaths != null) {
            // «элитные муравьи»: лучший найденный маршрут подкрепляется дополнительно,
            // как будто по нему прошла ещё одна колония
            double delta = ants.size() * params.q / bestLength;
            for (Path path : bestTourPaths) {
                path.addDeposit(delta);
            }
        }
        for (Path path : graph.getPaths()) {
            path.applyDeposit(params.rho);
            path.evaporate(params.rho);
        }
    }

    /**
     * Продвигает моделирование на один такт (для анимации):
     * либо все муравьи делают шаг, либо — если маршруты завершены — подводится итог цикла.
     *
     * @return true, если на этом такте цикл был завершён
     */
    public boolean advance() {
        if (isIterationComplete()) {
            finishIteration();
            return true;
        }
        stepAnts();
        return false;
    }

    /** Выполняет (или довыполняет) один цикл целиком, без анимации. */
    public void runIteration() {
        while (!isIterationComplete()) {
            stepAnts();
        }
        finishIteration();
    }

    public Graph getGraph() {
        return graph;
    }

    public Parameters getParameters() {
        return params;
    }

    public List<Ant> getAnts() {
        return Collections.unmodifiableList(ants);
    }

    /** Количество завершённых циклов. */
    public int getIteration() {
        return iteration;
    }

    /** Лучший найденный маршрут (последовательность узлов) или null. */
    public List<Node> getBestTour() {
        return bestTour;
    }

    public List<Path> getBestTourPaths() {
        return bestTourPaths;
    }

    public double getBestLength() {
        return bestLength;
    }

    public double getIterationBestLength() {
        return iterationBestLength;
    }

    public double getIterationAverageLength() {
        return iterationAverageLength;
    }

    /** Сколько муравьёв в последнем цикле прошли по лучшему маршруту. */
    public int getAntsOnBestTour() {
        return antsOnBestTour;
    }

    /** Длина лучшего маршрута в каждом цикле. */
    public List<Double> getBestHistory() {
        return Collections.unmodifiableList(bestHistory);
    }

    /** Средняя длина маршрута в каждом цикле. */
    public List<Double> getAverageHistory() {
        return Collections.unmodifiableList(averageHistory);
    }
}
