package lifesim.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Модель «Искусственной жизни»: среда + список агентов + игровой шаг.
 * На каждом шаге агенты ходят в случайном порядке (чтобы ни один вид
 * не получал постоянного преимущества «первого хода»).
 */
public class Simulation {
    public static final int HISTORY_LIMIT = 600;

    private final SimulationConfig config;
    private final Environment environment;
    private final Random random;
    private final List<Agent> agents = new ArrayList<>();
    private final List<Agent> newborns = new ArrayList<>();
    private final Deque<PopulationStats> history = new ArrayDeque<>();
    private long step;
    private PopulationStats stats;

    public Simulation(SimulationConfig config) {
        this(config, System.nanoTime());
    }

    public Simulation(SimulationConfig config, long seed) {
        this.config = config.copy();
        this.environment = new Environment(this.config.getWidth(), this.config.getHeight());
        this.random = new Random(seed);
        populate();
    }

    /** Начальное случайное размещение агентов. */
    private void populate() {
        List<Position> free = environment.getEmptyCells();
        Collections.shuffle(free, random);
        int i = 0;
        for (int k = 0; k < config.getInitialPlants() && i < free.size(); k++, i++) {
            place(new Plant(config.getPlantInitialEnergy()), free.get(i));
        }
        for (int k = 0; k < config.getInitialHerbivores() && i < free.size(); k++, i++) {
            place(new Herbivore(config.getHerbivoreInitialEnergy()), free.get(i));
        }
        for (int k = 0; k < config.getInitialPredators() && i < free.size(); k++, i++) {
            place(new Predator(config.getPredatorInitialEnergy()), free.get(i));
        }
        updateStats();
    }

    private void place(Agent agent, Position p) {
        environment.setAgent(p.getX(), p.getY(), agent);
        agents.add(agent);
    }

    /** Один виток времени. */
    public void step() {
        List<Agent> order = new ArrayList<>(agents);
        Collections.shuffle(order, random);
        for (Agent agent : order) {
            if (agent.isAlive()) {
                agent.live(this);          // полиморфный вызов
            }
        }
        agents.addAll(newborns);
        newborns.clear();
        agents.removeIf(a -> !a.isAlive());
        step++;
        updateStats();
    }

    /** Появление потомка (вызывается агентом при делении). */
    void spawn(Agent child, int x, int y) {
        environment.setAgent(x, y, child);
        newborns.add(child);
    }

    private void updateStats() {
        int plants = 0, herbivores = 0, predators = 0;
        for (Agent a : agents) {
            if (a instanceof Plant) plants++;
            else if (a instanceof Herbivore) herbivores++;
            else if (a instanceof Predator) predators++;
        }
        stats = new PopulationStats(step, plants, herbivores, predators);
        history.addLast(stats);
        while (history.size() > HISTORY_LIMIT) history.removeFirst();
    }

    /** Жизнь прекратилась, если вымер хотя бы один вид. */
    public boolean isAnySpeciesExtinct() {
        return stats.getPlants() == 0 || stats.getHerbivores() == 0 || stats.getPredators() == 0;
    }

    public SimulationConfig getConfig() { return config; }
    public Environment getEnvironment() { return environment; }
    public Random getRandom() { return random; }
    public long getStep() { return step; }
    public PopulationStats getStats() { return stats; }
    public List<PopulationStats> getHistory() { return new ArrayList<>(history); }
    public List<Agent> getAgents() { return Collections.unmodifiableList(agents); }
}
