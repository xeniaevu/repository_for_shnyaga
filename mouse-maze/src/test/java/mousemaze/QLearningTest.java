package mousemaze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import mousemaze.agent.QLearningAgent;
import mousemaze.env.Environment;
import mousemaze.env.RewardConfig;
import mousemaze.generator.MazeFileIO;
import mousemaze.generator.MazeGenerator;
import mousemaze.model.Action;
import mousemaze.model.Maze;
import mousemaze.training.EpisodeResult;
import mousemaze.training.Simulation;
import mousemaze.training.Trainer;
import mousemaze.training.TrainingConfig;

/**
 * Проверка на мире 3×3 из пояснительной записки:
 *   A B C
 *   D # E(ток)
 *   F G H(сыр)
 */
class QLearningTest {

    private static final Maze WORLD_3X3 = MazeFileIO.parse(List.of(
            "#####",
            "#S..#",
            "#.#T#",
            "#..C#",
            "#####"));

    @Test
    void bellmanUpdateByHand() {
        Environment env = new Environment(WORLD_3X3.copy(), RewardConfig.defaults());
        QLearningAgent agent = new QLearningAgent(env.stateCount(), 0.5, 0.9, new Random(1));
        int a = env.encode(1, 1, 0);
        int b = env.encode(1, 2, 0);
        int c = env.encode(1, 3, 0);
        int e = env.encode(2, 3, 0);
        int h = env.encode(3, 3, 0);

        agent.learn(a, Action.RIGHT, -1, b, false);
        assertEquals(-0.5, agent.getQ(a, Action.RIGHT), 1e-9);
        agent.learn(c, Action.DOWN, -30, e, false);
        assertEquals(-15, agent.getQ(c, Action.DOWN), 1e-9);
        agent.learn(e, Action.DOWN, 100, h, true);
        assertEquals(50, agent.getQ(e, Action.DOWN), 1e-9);
        // Второй эпизод: ценность сыра «протекает» назад через ловушку.
        agent.learn(c, Action.DOWN, -30, e, false);
        assertEquals(0, agent.getQ(c, Action.DOWN), 1e-9);
    }

    @Test
    void convergesToHandComputedValues() {
        TrainingConfig cfg = new TrainingConfig(3000, 200, 0.5, 0.9, 1.0, 0.3, 0.5, 5);
        Trainer t = Simulation.create(WORLD_3X3, RewardConfig.defaults(), cfg);
        t.train(null, null);
        int a = t.environment().encode(1, 1, 0);
        assertEquals(70.2, t.agent().getQ(a, Action.DOWN), 0.5);   // в обход тока
        assertEquals(55.0, t.agent().getQ(a, Action.RIGHT), 0.5);  // через ток
        EpisodeResult best = t.greedyRun();
        assertTrue(best.reachedCheese());
        assertEquals(4, best.steps());
        assertEquals(0, best.trapHits());
    }

    @Test
    void learnsGeneratedMazes() {
        int reached = 0;
        for (long seed = 1; seed <= 10; seed++) {
            Maze m = new MazeGenerator(new Random(seed)).generate(6, 6, 0, 4, 3);
            TrainingConfig d = TrainingConfig.defaults();
            TrainingConfig cfg = new TrainingConfig(d.episodes(), d.maxSteps(), d.alpha(), d.gamma(),
                    d.epsilonStart(), d.epsilonEnd(), d.explorationShare(), seed);
            Trainer t = Simulation.create(m, RewardConfig.defaults(), cfg);
            List<EpisodeResult> h = t.train(null, null);
            if (t.greedyRun().reachedCheese()) {
                reached++;
            }
            assertTrue(h.get(h.size() - 1).totalReward() > h.get(0).totalReward());
        }
        assertTrue(reached >= 9, "мышь нашла сыр только в " + reached + " из 10 лабиринтов");
    }
}
