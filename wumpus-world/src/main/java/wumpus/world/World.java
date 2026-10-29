package wumpus.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Среда «Мир вампуса»: настоящее состояние пещеры, которое агент не видит.
 * Принимает действия агента, возвращает восприятия и считает очки.
 */
public class World {

    public static final int ACTION_COST = 1;      // −1 за любое действие
    public static final int ARROW_COST = 10;      // −10 дополнительно за выстрел
    public static final int DEATH_PENALTY = 1000; // −1000 за смерть
    public static final int GOLD_REWARD = 1000;   // +1000 за выход из пещеры с золотом

    public enum Status {
        PLAYING("Игра идёт"),
        WON("Победа: агент выбрался с золотом"),
        FELL_INTO_PIT("Агент упал в яму"),
        EATEN_BY_WUMPUS("Агента съел вампус"),
        CLIMBED_WITHOUT_GOLD("Агент выбрался без золота");

        public final String text;

        Status(String text) {
            this.text = text;
        }
    }

    private final Grid grid;
    private final double pitProbability;
    private final boolean[][] pits;
    private final Position wumpus;
    private Position gold; // null, когда золото уже подобрано

    private Position agentPos = Grid.START;
    private Direction agentDir = Direction.EAST;
    private boolean hasArrow = true;
    private boolean hasGold = false;
    private boolean wumpusAlive = true;
    private int score = 0;
    private int actionCount = 0;
    private Status status = Status.PLAYING;
    private Percept lastPercept;
    private Action lastAction;

    public World(int width, int height, double pitProbability,
                 Collection<Position> pitCells, Position wumpus, Position gold) {
        this.grid = new Grid(width, height);
        this.pitProbability = pitProbability;
        this.pits = new boolean[width + 1][height + 1];
        for (Position p : pitCells) {
            if (!grid.inside(p) || p.equals(Grid.START)) throw new IllegalArgumentException("Недопустимая яма " + p);
            pits[p.x()][p.y()] = true;
        }
        if (!grid.inside(wumpus) || wumpus.equals(Grid.START)) throw new IllegalArgumentException("Недопустимый вампус");
        if (!grid.inside(gold) || gold.equals(Grid.START)) throw new IllegalArgumentException("Недопустимое золото");
        this.wumpus = wumpus;
        this.gold = gold;
        this.lastPercept = perceive(false, false);
    }

    /**
     * Случайная пещера по правилам книги: вампус и золото — в случайных клетках
     * (кроме [1,1]), в каждой клетке кроме [1,1] яма с вероятностью pitProbability.
     */
    public static World random(int width, int height, double pitProbability, Random rnd) {
        Grid g = new Grid(width, height);
        List<Position> candidates = new ArrayList<>();
        for (Position p : g.cells()) if (!p.equals(Grid.START)) candidates.add(p);
        if (candidates.isEmpty()) throw new IllegalArgumentException("Поле слишком маленькое");

        Position wumpus = candidates.get(rnd.nextInt(candidates.size()));
        Position gold = candidates.get(rnd.nextInt(candidates.size()));
        List<Position> pitCells = new ArrayList<>();
        for (Position p : candidates) if (rnd.nextDouble() < pitProbability) pitCells.add(p);
        return new World(width, height, pitProbability, pitCells, wumpus, gold);
    }

    /** Пещера с рис. 7.2 из книги. */
    public static World bookExample() {
        return new World(4, 4, 0.2,
                List.of(new Position(3, 1), new Position(3, 3), new Position(4, 4)),
                new Position(1, 3),
                new Position(2, 3));
    }

    /** Выполнить действие агента и вернуть новое восприятие. */
    public Percept execute(Action action) {
        if (isOver()) throw new IllegalStateException("Игра уже закончена");
        actionCount++;
        score -= ACTION_COST;
        lastAction = action;
        boolean bump = false;
        boolean scream = false;

        switch (action) {
            case FORWARD -> {
                Position next = agentPos.step(agentDir);
                if (!grid.inside(next)) {
                    bump = true;
                } else {
                    agentPos = next;
                    if (hasPit(next)) {
                        score -= DEATH_PENALTY;
                        status = Status.FELL_INTO_PIT;
                    } else if (wumpusAlive && next.equals(wumpus)) {
                        score -= DEATH_PENALTY;
                        status = Status.EATEN_BY_WUMPUS;
                    }
                }
            }
            case TURN_LEFT -> agentDir = agentDir.left();
            case TURN_RIGHT -> agentDir = agentDir.right();
            case GRAB -> {
                if (gold != null && gold.equals(agentPos)) {
                    hasGold = true;
                    gold = null;
                }
            }
            case SHOOT -> {
                if (hasArrow) {
                    hasArrow = false;
                    score -= ARROW_COST;
                    // Стрела летит по прямой до стены или до вампуса
                    for (Position c = agentPos.step(agentDir); grid.inside(c); c = c.step(agentDir)) {
                        if (wumpusAlive && c.equals(wumpus)) {
                            wumpusAlive = false;
                            scream = true;
                            break;
                        }
                    }
                }
            }
            case CLIMB -> {
                if (agentPos.equals(Grid.START)) {
                    if (hasGold) {
                        score += GOLD_REWARD;
                        status = Status.WON;
                    } else {
                        status = Status.CLIMBED_WITHOUT_GOLD;
                    }
                }
            }
        }
        lastPercept = perceive(bump, scream);
        return lastPercept;
    }

    private Percept perceive(boolean bump, boolean scream) {
        boolean stench = wumpus.equals(agentPos) || Grid.adjacent(wumpus, agentPos);
        boolean breeze = false;
        for (Position n : grid.neighbors(agentPos)) breeze |= hasPit(n);
        boolean glitter = gold != null && gold.equals(agentPos);
        return new Percept(stench, breeze, glitter, bump, scream);
    }

    // ---- геттеры ----

    public Grid grid() { return grid; }
    public double pitProbability() { return pitProbability; }
    public boolean hasPit(Position p) { return pits[p.x()][p.y()]; }
    public Position wumpus() { return wumpus; }
    public Position gold() { return gold; }
    public boolean isWumpusAlive() { return wumpusAlive; }
    public Position agentPosition() { return agentPos; }
    public Direction agentDirection() { return agentDir; }
    public boolean agentHasArrow() { return hasArrow; }
    public boolean agentHasGold() { return hasGold; }
    public int score() { return score; }
    public int actionCount() { return actionCount; }
    public Status status() { return status; }
    public boolean isOver() { return status != Status.PLAYING; }
    public boolean isAgentDead() { return status == Status.FELL_INTO_PIT || status == Status.EATEN_BY_WUMPUS; }
    public Percept lastPercept() { return lastPercept; }
    public Action lastAction() { return lastAction; }
}
