package mousemaze.agent;

import java.util.Random;

import mousemaze.model.Action;

/**
 * Агент (мышь), обучающийся методом Q-learning.
 *
 * Q-таблица хранит для каждого состояния s и действия a оценку Q(s, a) —
 * насколько выгодно сделать a в s с учётом всех будущих наград.
 * В начале таблица заполнена нулями: мышь ничего не знает о лабиринте.
 *
 * Агент ничего не знает о стенах и клетках — только номера состояний и награды.
 */
public class QLearningAgent {

    private final int stateCount;
    /** Q-таблица в плоском массиве: q[s * 4 + a]. */
    private final double[] q;
    private final double alpha;
    private final double gamma;
    private final Random random;

    /**
     * @param stateCount число состояний
     * @param alpha      скорость обучения α ∈ (0, 1]
     * @param gamma      коэффициент дисконтирования γ ∈ [0, 1]
     */
    public QLearningAgent(int stateCount, double alpha, double gamma, Random random) {
        if (alpha <= 0 || alpha > 1) {
            throw new IllegalArgumentException("α должна быть в (0, 1]");
        }
        if (gamma < 0 || gamma > 1) {
            throw new IllegalArgumentException("γ должна быть в [0, 1]");
        }
        this.stateCount = stateCount;
        this.q = new double[stateCount * Action.COUNT];
        this.alpha = alpha;
        this.gamma = gamma;
        this.random = random;
    }

    /**
     * ε-жадный выбор: с вероятностью ε — случайное действие (исследование),
     * иначе — лучшее по Q-таблице (использование опыта).
     */
    public Action chooseAction(int state, double epsilon) {
        if (random.nextDouble() < epsilon) {
            return Action.ALL[random.nextInt(Action.COUNT)];
        }
        return bestAction(state);
    }

    /** Действие с максимальным Q. При равенстве выбирается случайное из лучших. */
    public Action bestAction(int state) {
        int base = state * Action.COUNT;
        double best = Double.NEGATIVE_INFINITY;
        int bestCount = 0;
        int chosen = 0;
        for (int a = 0; a < Action.COUNT; a++) {
            double v = q[base + a];
            if (v > best) {
                best = v;
                bestCount = 1;
                chosen = a;
            } else if (v == best) {
                bestCount++;
                if (random.nextInt(bestCount) == 0) { // равновероятный выбор среди равных
                    chosen = a;
                }
            }
        }
        return Action.ALL[chosen];
    }

    /**
     * Обновление по уравнению Беллмана:
     * Q(s,a) ← Q(s,a) + α · [ r + γ · max Q(s',a') − Q(s,a) ].
     * Для конечного состояния будущих наград нет, поэтому max Q(s',a') = 0.
     *
     * @return ошибка прогноза (TD-ошибка) — выражение в квадратных скобках
     */
    public double learn(int state, Action action, double reward, int nextState, boolean terminal) {
        int idx = state * Action.COUNT + action.ordinal();
        double future = terminal ? 0.0 : maxQ(nextState);
        double tdError = reward + gamma * future - q[idx];
        q[idx] += alpha * tdError;
        return tdError;
    }

    public double maxQ(int state) {
        int base = state * Action.COUNT;
        double m = q[base];
        for (int a = 1; a < Action.COUNT; a++) {
            m = Math.max(m, q[base + a]);
        }
        return m;
    }

    public double getQ(int state, Action action) {
        return q[state * Action.COUNT + action.ordinal()];
    }

    /** Мышь уже что-то знает об этом состоянии (хотя бы одно значение не ноль). */
    public boolean isVisited(int state) {
        int base = state * Action.COUNT;
        for (int a = 0; a < Action.COUNT; a++) {
            if (q[base + a] != 0.0) {
                return true;
            }
        }
        return false;
    }

    public int stateCount() {
        return stateCount;
    }

    public double alpha() {
        return alpha;
    }

    public double gamma() {
        return gamma;
    }
}
