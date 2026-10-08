package mousemaze.training;

import java.util.Random;

import mousemaze.agent.QLearningAgent;
import mousemaze.env.Environment;
import mousemaze.env.RewardConfig;
import mousemaze.model.Maze;

/**
 * Фасад: собирает среду, агента и тренера из лабиринта и настроек.
 * Используется и графическим интерфейсом, и консольной демонстрацией.
 */
public final class Simulation {

    /** Ограничение размера Q-таблицы (≈ 128 МБ), чтобы не исчерпать память. */
    public static final long MAX_Q_ENTRIES = 16_000_000L;

    private Simulation() {
    }

    public static Trainer create(Maze maze, RewardConfig rewards, TrainingConfig config) {
        Environment env = new Environment(maze.copy(), rewards);
        long entries = (long) env.stateCount() * 4;
        if (entries > MAX_Q_ENTRIES) {
            throw new IllegalArgumentException(String.format(
                    "Q-таблица слишком велика (%,d значений). Уменьшите лабиринт или число клеток с водой.",
                    entries));
        }
        Random random = config.seed() == 0 ? new Random() : new Random(config.seed());
        QLearningAgent agent = new QLearningAgent(env.stateCount(), config.alpha(), config.gamma(), random);
        return new Trainer(env, agent, config);
    }
}
