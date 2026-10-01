package lifesim.model;

import java.util.List;

/**
 * Абстрактный логический агент: координаты, энергия, признак жизни.
 * Общий жизненный цикл задан шаблонным методом {@link #live(Simulation)},
 * а конкретное поведение определяют наследники (полиморфизм).
 */
public abstract class Agent {
    private int x;
    private int y;
    private int energy;
    private boolean alive = true;
    private int age;

    protected Agent(int energy) {
        this.energy = energy;
    }

    // ---------- данные ----------
    public int getX() { return x; }
    public int getY() { return y; }
    public Position getPosition() { return new Position(x, y); }
    public int getEnergy() { return energy; }
    public boolean isAlive() { return alive; }
    public int getAge() { return age; }

    /** Координаты меняет только среда (инкапсуляция: доступ на уровне пакета). */
    void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    protected void addEnergy(int delta) { energy += delta; }
    protected void setEnergy(int value) { energy = value; }

    // ---------- жизненный цикл ----------

    /**
     * Один ход агента: действие (зрение → выбор цели → действие),
     * затем трата/выработка энергии, проверка смерти и размножения.
     */
    public final void live(Simulation sim) {
        if (!alive) return;
        age++;
        act(sim);
        metabolize(sim.getConfig());
        if (energy <= 0) {
            die(sim);
            return;
        }
        reproduce(sim);
    }

    /** Действие агента в текущем ходе (у каждого вида своё). */
    protected abstract void act(Simulation sim);

    /** Изменение энергии за ход: для животных отрицательное, для растений положительное. */
    protected abstract int getEnergyChangePerStep(SimulationConfig config);

    protected abstract int getReproductionThreshold(SimulationConfig config);

    protected abstract int getChildEnergy(SimulationConfig config);

    /** Создать потомка того же вида. */
    protected abstract Agent createChild(int energy);

    /** Символ для консольного вывода. */
    public abstract char getSymbol();

    /** Название вида для интерфейса. */
    public abstract String getSpeciesName();

    /** Метаболизм: расход (или выработка) энергии. */
    protected void metabolize(SimulationConfig config) {
        energy += getEnergyChangePerStep(config);
    }

    /** Исчезновение: агент погибает и удаляется из среды. */
    public void die(Simulation sim) {
        if (!alive) return;
        alive = false;
        Environment env = sim.getEnvironment();
        if (env.getAgent(x, y) == this) {
            env.setAgent(x, y, null);
        }
    }

    /**
     * Появление нового агента (деление): если E > порога, в случайной свободной
     * соседней клетке появляется потомок с энергией E_child, а родитель её теряет.
     */
    protected void reproduce(Simulation sim) {
        SimulationConfig config = sim.getConfig();
        int childEnergy = getChildEnergy(config);
        if (energy <= getReproductionThreshold(config) || childEnergy >= energy) return;

        List<Position> free = sim.getEnvironment().getEmptyOrthogonalNeighbors(x, y);
        if (free.isEmpty()) return;

        Position p = free.get(sim.getRandom().nextInt(free.size()));
        energy -= childEnergy;
        sim.spawn(createChild(childEnergy), p.getX(), p.getY());
    }

    @Override
    public String toString() {
        return getSpeciesName() + " " + getPosition() + " E=" + energy;
    }
}
