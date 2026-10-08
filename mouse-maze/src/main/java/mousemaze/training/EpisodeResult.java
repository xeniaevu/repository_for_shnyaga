package mousemaze.training;

/**
 * Итог одного эпизода. Траектория хранится как номера состояний,
 * поэтому по ней можно восстановить и клетку, и какую воду мышь уже выпила.
 *
 * @param episode     номер эпизода (0 — контрольный прогон без случайности)
 * @param epsilon     доля случайных ходов в этом эпизоде
 * @param totalReward сумма выигрыша/проигрыша за эпизод
 * @param states      states[0] — старт, states[i] — состояние после i-го шага
 * @param rewards     rewards[i] — награда за (i+1)-й шаг
 */
public record EpisodeResult(int episode, double epsilon, double totalReward, boolean reachedCheese,
                            int waterDrunk, int trapHits, int wallHits,
                            int[] states, double[] rewards) {

    public int steps() {
        return rewards.length;
    }

    /** Сумма наград за первые k шагов — для анимации. */
    public double rewardUpTo(int k) {
        double sum = 0;
        for (int i = 0; i < k && i < rewards.length; i++) {
            sum += rewards[i];
        }
        return sum;
    }
}
