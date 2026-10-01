package lifesim.model;

import java.util.ArrayList;
import java.util.List;

public class Herbivore extends Animal {

    public Herbivore(int energy) {
        super(energy);
    }

    @Override
    protected void act(Simulation sim) {
        List<Agent> seen = look(sim);
        List<Predator> predators = filter(seen, Predator.class);

        if (!predators.isEmpty()) {
            flee(sim, predators);
            return;
        }

        Plant adjacent = findAdjacent(sim, Plant.class);
        if (adjacent != null) {
            eat(sim, adjacent, sim.getConfig().getHerbivoreFoodGain());
            return;
        }

        Plant target = nearest(filter(seen, Plant.class), sim.getRandom());
        if (target != null && stepTowards(sim, target.getPosition())) {
            return;
        }
        wander(sim);
    }

    /**
     * Бегство. Оцениваются все свободные соседние клетки (и вариант «остаться»):
     * 1) максимизируется расстояние до ближайшего видимого хищника;
     * 2) при равенстве — сумма расстояний до всех видимых хищников;
     * 3) если несколько ходов одинаково хороши — выбирается случайный.
     * Расстояние манхэттенское: хищник ходит и атакует только по вертикали/горизонтали,
     * поэтому отход назад по линии и отход вбок одинаково увеличивают дистанцию.
     */
    private void flee(Simulation sim, List<Predator> predators) {
        Environment env = sim.getEnvironment();
        Position here = getPosition();
        int[] stayScore = score(here, predators);

        List<Position> best = new ArrayList<>();
        int[] bestScore = null;
        for (Position p : env.getEmptyOrthogonalNeighbors(getX(), getY())) {
            int[] s = score(p, predators);
            int cmp = bestScore == null ? 1 : compare(s, bestScore);
            if (cmp > 0) {
                bestScore = s;
                best.clear();
                best.add(p);
            } else if (cmp == 0) {
                best.add(p);
            }
        }

        // Если ни один ход не лучше, чем стоять на месте, — остаёмся.
        if (best.isEmpty() || compare(bestScore, stayScore) < 0) return;
        moveTo(sim, best.get(sim.getRandom().nextInt(best.size())));
    }

    /** [минимальное расстояние до хищника, сумма расстояний]. */
    private static int[] score(Position p, List<Predator> predators) {
        int min = Integer.MAX_VALUE;
        int sum = 0;
        for (Predator pr : predators) {
            int d = p.manhattan(pr.getX(), pr.getY());
            min = Math.min(min, d);
            sum += d;
        }
        return new int[]{min, sum};
    }

    private static int compare(int[] a, int[] b) {
        if (a[0] != b[0]) return Integer.compare(a[0], b[0]);
        return Integer.compare(a[1], b[1]);
    }

    @Override
    protected int getEnergyChangePerStep(SimulationConfig config) {
        return -config.getHerbivoreMetabolism();
    }

    @Override
    protected int getReproductionThreshold(SimulationConfig config) {
        return config.getHerbivoreReproductionThreshold();
    }

    @Override
    protected int getChildEnergy(SimulationConfig config) {
        return config.getHerbivoreChildEnergy();
    }

    @Override
    protected Agent createChild(int energy) {
        return new Herbivore(energy);
    }

    @Override
    public char getSymbol() { return 'T'; }

    @Override
    public String getSpeciesName() { return "Травоядное"; }
}
