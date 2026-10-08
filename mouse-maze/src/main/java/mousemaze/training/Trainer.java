package mousemaze.training;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

import mousemaze.agent.QLearningAgent;
import mousemaze.env.Environment;
import mousemaze.env.StepResult;
import mousemaze.model.Action;

/**
 * Цикл обучения: много эпизодов подряд, после каждого мышь возвращается
 * на старт, а Q-таблица (опыт) сохраняется.
 */
public class Trainer {

    private final Environment env;
    private final QLearningAgent agent;
    private final TrainingConfig config;

    public Trainer(Environment env, QLearningAgent agent, TrainingConfig config) {
        this.env = env;
        this.agent = agent;
        this.config = config;
    }

    /**
     * Проводит все эпизоды обучения.
     *
     * @param listener  уведомляется после каждого эпизода (может быть null)
     * @param cancelled позволяет прервать обучение (может быть null)
     */
    public List<EpisodeResult> train(TrainingListener listener, BooleanSupplier cancelled) {
        List<EpisodeResult> history = new ArrayList<>(config.episodes());
        for (int ep = 1; ep <= config.episodes(); ep++) {
            if (cancelled != null && cancelled.getAsBoolean()) {
                break;
            }
            EpisodeResult result = runEpisode(ep, config.epsilonAt(ep), true);
            history.add(result);
            if (listener != null) {
                listener.onEpisodeFinished(result, config.episodes());
            }
        }
        return history;
    }

    /** Контрольный прогон после обучения: ε = 0, таблица не меняется. Это найденный путь. */
    public EpisodeResult greedyRun() {
        return runEpisode(0, 0.0, false);
    }

    /** Один эпизод: от старта до сыра или до лимита шагов. */
    public EpisodeResult runEpisode(int episodeNumber, double epsilon, boolean learn) {
        int state = env.reset();
        int[] states = new int[config.maxSteps() + 1];
        double[] rewards = new double[config.maxSteps()];
        states[0] = state;
        int steps = 0;
        double total = 0;
        int water = 0;
        int traps = 0;
        int walls = 0;
        boolean done = false;

        while (!done && steps < config.maxSteps()) {
            Action action = agent.chooseAction(state, epsilon);
            StepResult r = env.step(action);
            if (learn) {
                agent.learn(state, action, r.reward(), r.nextState(), r.done());
            }
            switch (r.event()) {
                case WATER -> water++;
                case TRAP -> traps++;
                case WALL -> walls++;
                default -> { }
            }
            total += r.reward();
            rewards[steps] = r.reward();
            steps++;
            states[steps] = r.nextState();
            state = r.nextState();
            done = r.done();
        }
        return new EpisodeResult(episodeNumber, epsilon, total, done, water, traps, walls,
                Arrays.copyOf(states, steps + 1), Arrays.copyOf(rewards, steps));
    }

    public Environment environment() {
        return env;
    }

    public QLearningAgent agent() {
        return agent;
    }

    public TrainingConfig config() {
        return config;
    }
}
