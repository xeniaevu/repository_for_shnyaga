package lifesim.model;

/** Снимок численности популяций на определённом шаге. */
public final class PopulationStats {
    private final long step;
    private final int plants;
    private final int herbivores;
    private final int predators;

    public PopulationStats(long step, int plants, int herbivores, int predators) {
        this.step = step;
        this.plants = plants;
        this.herbivores = herbivores;
        this.predators = predators;
    }

    public long getStep() { return step; }
    public int getPlants() { return plants; }
    public int getHerbivores() { return herbivores; }
    public int getPredators() { return predators; }

    @Override
    public String toString() {
        return String.format("Шаг %d | Растения (P): %d | Травоядные (T): %d | Хищники (X): %d",
                step, plants, herbivores, predators);
    }
}
