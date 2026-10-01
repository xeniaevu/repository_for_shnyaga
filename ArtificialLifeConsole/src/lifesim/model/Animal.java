package lifesim.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Общая часть подвижных агентов: зрение 5×5, движение на 1 клетку
 * по вертикали/горизонтали, поедание (со вставанием на клетку жертвы).
 */
public abstract class Animal extends Agent {
    /** Радиус зрения: 2 клетки во все стороны → квадрат 5×5 с агентом в центре. */
    public static final int VISION_RADIUS = 2;

    protected Animal(int energy) {
        super(energy);
    }

    /** Зрение: все агенты в квадрате 5×5 вокруг. */
    protected List<Agent> look(Simulation sim) {
        Environment env = sim.getEnvironment();
        List<Agent> seen = new ArrayList<>();
        for (Position p : env.getNeighbors(getX(), getY(), VISION_RADIUS)) {
            Agent a = env.getAgent(p.getX(), p.getY());
            if (a != null && a.isAlive()) {
                seen.add(a);
            }
        }
        return seen;
    }

    /** Отбор увиденных агентов заданного вида. */
    protected static <T extends Agent> List<T> filter(List<Agent> seen, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Agent a : seen) {
            if (type.isInstance(a)) result.add(type.cast(a));
        }
        return result;
    }

    /** Агент заданного вида в соседней (не диагональной) клетке, или null. */
    protected <T extends Agent> T findAdjacent(Simulation sim, Class<T> type) {
        Environment env = sim.getEnvironment();
        List<T> found = new ArrayList<>();
        for (Position p : env.getOrthogonalNeighbors(getX(), getY())) {
            Agent a = env.getAgent(p.getX(), p.getY());
            if (type.isInstance(a) && a.isAlive()) found.add(type.cast(a));
        }
        return found.isEmpty() ? null : found.get(sim.getRandom().nextInt(found.size()));
    }

    /** Ближайший (по числу ходов) агент из списка; при равенстве — случайный. */
    protected <T extends Agent> T nearest(List<T> agents, Random random) {
        List<T> best = new ArrayList<>();
        int bestDist = Integer.MAX_VALUE;
        for (T a : agents) {
            int d = a.getPosition().manhattan(getX(), getY());
            if (d < bestDist) {
                bestDist = d;
                best.clear();
                best.add(a);
            } else if (d == bestDist) {
                best.add(a);
            }
        }
        return best.isEmpty() ? null : best.get(random.nextInt(best.size()));
    }

    /** Движение в свободную соседнюю клетку. */
    protected void moveTo(Simulation sim, Position p) {
        sim.getEnvironment().moveAgent(this, p.getX(), p.getY());
    }

    /**
     * Поедание: жертва исчезает, хищник/травоядное получает энергию
     * и в этот же ход встаёт на её клетку (как взятие в шахматах).
     */
    protected void eat(Simulation sim, Agent food, int energyGain) {
        int fx = food.getX();
        int fy = food.getY();
        food.die(sim);
        addEnergy(energyGain);
        sim.getEnvironment().moveAgent(this, fx, fy);
    }

    /**
     * Шаг к цели по свободным клеткам: выбирается ход, максимально сокращающий
     * расстояние (при нескольких равных — случайный). Возвращает false,
     * если приблизиться нельзя (путь перекрыт).
     */
    protected boolean stepTowards(Simulation sim, Position target) {
        List<Position> free = sim.getEnvironment().getEmptyOrthogonalNeighbors(getX(), getY());
        int current = target.manhattan(getX(), getY());
        List<Position> best = new ArrayList<>();
        int bestDist = current;
        for (Position p : free) {
            int d = p.manhattan(target);
            if (d < bestDist) {
                bestDist = d;
                best.clear();
                best.add(p);
            } else if (d == bestDist && d < current) {
                best.add(p);
            }
        }
        if (best.isEmpty()) return false;
        moveTo(sim, best.get(sim.getRandom().nextInt(best.size())));
        return true;
    }

    /** Случайное блуждание: ход в случайную свободную соседнюю клетку. */
    protected void wander(Simulation sim) {
        List<Position> free = sim.getEnvironment().getEmptyOrthogonalNeighbors(getX(), getY());
        if (!free.isEmpty()) {
            moveTo(sim, free.get(sim.getRandom().nextInt(free.size())));
        }
    }
}
