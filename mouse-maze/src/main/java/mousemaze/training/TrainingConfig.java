package mousemaze.training;

/**
 * Параметры обучения.
 *
 * @param episodes         число эпизодов (эпох)
 * @param maxSteps         лимит шагов в эпизоде, чтобы мышь не бродила бесконечно
 * @param alpha            скорость обучения α
 * @param gamma            коэффициент дисконтирования γ
 * @param epsilonStart     доля случайных ходов в начале (обычно 1 — полный хаос)
 * @param epsilonEnd       минимальная доля случайных ходов
 * @param explorationShare за какую долю эпизодов ε линейно падает от начального до минимального
 * @param seed             зерно генератора случайных чисел (0 — случайное)
 */
public record TrainingConfig(int episodes, int maxSteps, double alpha, double gamma,
                             double epsilonStart, double epsilonEnd, double explorationShare, long seed) {

    /** Значения, подобранные симуляцией: на лабиринте 6×6 мышь находит сыр почти всегда. */
    public static TrainingConfig defaults() {
        return new TrainingConfig(500, 800, 0.5, 0.99, 1.0, 0.05, 0.7, 0);
    }

    /** Линейное уменьшение ε: хаос → поиск пути → использование опыта. */
    public double epsilonAt(int episode) {
        double decayEpisodes = Math.max(1.0, explorationShare * episodes);
        double t = Math.min(1.0, (episode - 1) / decayEpisodes);
        return epsilonStart + (epsilonEnd - epsilonStart) * t;
    }
}
