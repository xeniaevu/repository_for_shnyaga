package antcolony;

import antcolony.algorithm.AntColony;
import antcolony.algorithm.ExactSolver;
import antcolony.algorithm.Parameters;
import antcolony.model.Graph;
import antcolony.model.Node;
import antcolony.model.Path;

import java.util.Locale;
import java.util.Random;

/**
 * Консольная проверка без графики:
 * 1) пример «два муравья» из методички;
 * 2) запуск алгоритма на случайном графе и сравнение с точным решением.
 */
public class ConsoleDemo {

    public static void main(String[] args) {
        twoAntsExample();
        System.out.println();
        randomGraphRun();
    }

    /** Пример из методички: rho = 0.6, Q = 10, длины путей 20 и 10, начальный феромон 0.1. */
    private static void twoAntsExample() {
        double rho = 0.6;
        double q = 10;
        System.out.println("Пример из методички (два муравья, два пути):");
        double[] lengths = {20, 10};
        for (int i = 0; i < lengths.length; i++) {
            Path path = new Path(new Node(0, 0, 0), new Node(1, lengths[i], 0), 0.1);
            path.addDeposit(q / path.getLength());
            path.applyDeposit(rho);
            double afterUpdate = path.getPheromone();
            path.evaporate(rho);
            System.out.printf(Locale.US,
                    "  муравей A%d: длина %.0f, феромон после обновления %.2f, после испарения %.2f%n",
                    i, lengths[i], afterUpdate, path.getPheromone());
        }
    }

    private static void randomGraphRun() {
        Random random = new Random(2024);
        Graph graph = Graph.createRandom(10, random);
        Parameters params = new Parameters();
        AntColony colony = new AntColony(graph, params, random);
        double exact = ExactSolver.shortestTourLength(graph);

        System.out.println("Случайный граф, 10 узлов, 10 муравьёв:");
        System.out.println("  цикл | лучший в цикле | средний в цикле | муравьёв на лучшем маршруте");
        for (int i = 1; i <= 100; i++) {
            colony.runIteration();
            if (i <= 5 || i % 10 == 0) {
                System.out.printf(Locale.US, "  %4d | %14.1f | %15.1f | %d из %d%n",
                        i, colony.getIterationBestLength(), colony.getIterationAverageLength(),
                        colony.getAntsOnBestTour(), colony.getAnts().size());
            }
        }
        System.out.printf(Locale.US, "  Лучший маршрут муравьёв: %.1f  %s%n",
                colony.getBestLength(), colony.getBestTour());
        System.out.printf(Locale.US, "  Точный оптимум:          %.1f%n", exact);
    }
}
