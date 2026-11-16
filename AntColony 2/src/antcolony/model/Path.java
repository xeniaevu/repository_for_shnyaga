package antcolony.model;

/**
 * Путь (ребро графа), соединяющий два узла.
 * Хранит количество феромона tau и феромон, отложенный муравьями за текущий цикл.
 * Граф двунаправленный, поэтому путь a-b и путь b-a — один и тот же объект.
 */
public class Path {

    /** Нижняя граница феромона, чтобы вероятность выбора пути никогда не стала нулевой. */
    public static final double MIN_PHEROMONE = 1e-12;

    private final Node first;
    private final Node second;
    private double pheromone;
    private double deposit;

    public Path(Node first, Node second, double initialPheromone) {
        this.first = first;
        this.second = second;
        this.pheromone = initialPheromone;
    }

    public Node getFirst() {
        return first;
    }

    public Node getSecond() {
        return second;
    }

    /** Вес ребра d(r,u) — расстояние между узлами. Считается по текущим координатам узлов. */
    public double getLength() {
        return first.distanceTo(second);
    }

    /** Видимость eta(r,u) = 1 / d(r,u). */
    public double getVisibility() {
        return 1.0 / Math.max(getLength(), 1e-6);
    }

    /** Интенсивность феромона tau(r,u). */
    public double getPheromone() {
        return pheromone;
    }

    public void setPheromone(double value) {
        this.pheromone = Math.max(value, MIN_PHEROMONE);
        this.deposit = 0;
    }

    /** Муравей, прошедший по пути, добавляет свою долю феромона: delta tau = Q / L. */
    public void addDeposit(double delta) {
        deposit += delta;
    }

    /**
     * Обновление феромона в конце цикла (после того, как все муравьи завершили маршрут):
     * <pre>tau = tau + rho * (сумма delta tau)</pre>
     * Соответствует примеру из методички: 0.1 + (0.5 * 0.6) = 0.4.
     */
    public void applyDeposit(double rho) {
        pheromone += rho * deposit;
        deposit = 0;
    }

    /**
     * Испарение феромона:
     * <pre>tau = tau * (1 - rho)</pre>
     * Соответствует примеру из методички: 0.4 * (1 - 0.6) = 0.16.
     */
    public void evaporate(double rho) {
        pheromone = Math.max(pheromone * (1.0 - rho), MIN_PHEROMONE);
    }

    /** Возвращает противоположный конец пути. */
    public Node other(Node node) {
        return node == first ? second : first;
    }

    @Override
    public String toString() {
        return first + "-" + second;
    }
}
