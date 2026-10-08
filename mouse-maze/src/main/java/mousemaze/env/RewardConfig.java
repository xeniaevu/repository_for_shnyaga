package mousemaze.env;

/**
 * Величины наград. Все числа задаются положительными, знак ставит среда.
 *
 * @param cheese      +Z — сыр, конец эпизода
 * @param water       +x — вода (засчитывается один раз за эпизод в каждой клетке)
 * @param trap        −y — удар током
 * @param stepCost    штраф за обычный шаг (заставляет искать короткий путь)
 * @param wallPenalty штраф за попытку пройти в стену (мышь остаётся на месте)
 */
public record RewardConfig(double cheese, double water, double trap, double stepCost, double wallPenalty) {

    public static RewardConfig defaults() {
        return new RewardConfig(100, 10, 30, 1, 5);
    }
}
