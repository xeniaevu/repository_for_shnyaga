package wumpus;

import wumpus.agent.Agent;
import wumpus.world.Action;
import wumpus.world.Percept;
import wumpus.world.World;

import java.util.function.Consumer;

/** Связывает среду и агента: игровой цикл «действие → восприятие». */
public class Game {

    private final World world;
    private final Agent agent;
    private final Consumer<String> log;

    public Game(World world, double maxRisk, Consumer<String> log) {
        this.world = world;
        this.log = log != null ? log : s -> { };
        this.agent = new Agent(world.grid(), world.pitProbability(), maxRisk, this.log);
        agent.start(world.lastPercept());
    }

    /** Один шаг: агент выбирает действие, среда его исполняет. */
    public void step() {
        if (world.isOver()) return;
        Action action = agent.nextAction();
        Percept percept = world.execute(action);

        String where = action == Action.FORWARD && !percept.bump()
                ? " → " + world.agentPosition() : "";
        log.accept(String.format("#%d %s%s  %s", world.actionCount(), action.title, where, percept));

        if (world.isAgentDead()) {
            log.accept("✖ " + world.status().text + ". Счёт: " + world.score());
            return;
        }
        agent.afterAction(action, percept);
        if (world.isOver()) log.accept("■ " + world.status().text + ". Счёт: " + world.score());
    }

    /** Доиграть до конца (с ограничением на число действий на всякий случай). */
    public void runToEnd(int maxActions) {
        while (!world.isOver() && world.actionCount() < maxActions) step();
    }

    public World world() { return world; }
    public Agent agent() { return agent; }
    public boolean isOver() { return world.isOver(); }
}
