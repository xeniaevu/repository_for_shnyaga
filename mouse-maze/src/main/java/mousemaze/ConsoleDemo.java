package mousemaze;

import java.util.List;
import java.util.Random;

import mousemaze.env.Environment;
import mousemaze.env.RewardConfig;
import mousemaze.generator.MazeGenerator;
import mousemaze.model.Maze;
import mousemaze.model.Position;
import mousemaze.training.EpisodeResult;
import mousemaze.training.Simulation;
import mousemaze.training.Trainer;
import mousemaze.training.TrainingConfig;

/**
 * Консольная демонстрация без графики: генерирует лабиринт, обучает мышь
 * и печатает рост суммы выигрыша и найденный путь.
 *
 * Аргументы (необязательные): комнат_по_вертикали комнат_по_горизонтали эпизодов зерно
 */
public final class ConsoleDemo {

    private ConsoleDemo() {
    }

    public static void main(String[] args) {
        int rows = args.length > 0 ? Integer.parseInt(args[0]) : 6;
        int cols = args.length > 1 ? Integer.parseInt(args[1]) : 6;
        int episodes = args.length > 2 ? Integer.parseInt(args[2]) : 500;
        long seed = args.length > 3 ? Long.parseLong(args[3]) : 42;

        Maze maze = new MazeGenerator(new Random(seed)).generate(rows, cols, 0.0, 4, 3);
        System.out.println("Лабиринт (" + maze.rows() + "×" + maze.cols() + " клеток), кратчайший путь: "
                + maze.shortestPathLength() + " шагов");
        System.out.println(maze);

        TrainingConfig defaults = TrainingConfig.defaults();
        TrainingConfig config = new TrainingConfig(episodes, defaults.maxSteps(), defaults.alpha(),
                defaults.gamma(), defaults.epsilonStart(), defaults.epsilonEnd(), defaults.explorationShare(), seed);
        Trainer trainer = Simulation.create(maze, RewardConfig.defaults(), config);
        List<EpisodeResult> history = trainer.train(null, null);

        System.out.println("Эпизод |    ε  | шагов | сумма выигрыша | сыр");
        for (EpisodeResult r : history) {
            if (r.episode() == 1 || r.episode() % 50 == 0) {
                System.out.printf("%6d | %5.2f | %5d | %14.1f | %s%n",
                        r.episode(), r.epsilon(), r.steps(), r.totalReward(), r.reachedCheese() ? "да" : "нет");
            }
        }

        EpisodeResult best = trainer.greedyRun();
        System.out.printf("%nПосле обучения (ε = 0): %d шагов, сумма %.1f, сыр %s, выпито воды %d, ударов током %d%n",
                best.steps(), best.totalReward(), best.reachedCheese() ? "найден" : "НЕ найден",
                best.waterDrunk(), best.trapHits());
        System.out.println(renderPath(maze, trainer.environment(), best));
    }

    /** Рисует лабиринт с путём мыши, отмеченным символом '*'. */
    static String renderPath(Maze maze, Environment env, EpisodeResult result) {
        char[][] grid = new char[maze.rows()][];
        String[] lines = maze.toText().split("\n");
        for (int r = 0; r < maze.rows(); r++) {
            grid[r] = lines[r].toCharArray();
        }
        for (int state : result.states()) {
            Position p = env.positionOf(state);
            if (grid[p.row()][p.col()] == '.') {
                grid[p.row()][p.col()] = '*';
            }
        }
        StringBuilder sb = new StringBuilder();
        for (char[] row : grid) {
            sb.append(row).append('\n');
        }
        return sb.toString();
    }
}
