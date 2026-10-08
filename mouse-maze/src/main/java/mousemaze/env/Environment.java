package mousemaze.env;

import java.util.List;

import mousemaze.model.Action;
import mousemaze.model.CellType;
import mousemaze.model.Maze;
import mousemaze.model.Position;

/**
 * Среда — лабиринт с правилами наград. Только среда знает карту;
 * мышь (агент) получает от неё лишь номер состояния и награду.
 *
 * Состояние = (строка, столбец, маска выпитой воды).
 * Маска нужна, чтобы процесс оставался марковским: без неё мышь
 * не отличает полную лужу от уже выпитой и начинает бегать к пустой.
 *
 * Номер состояния: state = mask * (rows * cols) + row * cols + col.
 */
public class Environment {

    /** Каждая лужа удваивает число состояний, поэтому их число ограничено. */
    public static final int MAX_WATER = 10;

    private final Maze maze;
    private final RewardConfig rewards;
    private final int cellCount;
    private final int[][] waterIndex;
    private final int waterCount;

    private Position position;
    private int drunkMask;
    private boolean done;

    public Environment(Maze maze, RewardConfig rewards) {
        String error = maze.validate();
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        this.maze = maze;
        this.rewards = rewards;
        this.cellCount = maze.rows() * maze.cols();
        this.waterIndex = new int[maze.rows()][maze.cols()];
        List<Position> waters = maze.positionsOf(CellType.WATER);
        if (waters.size() > MAX_WATER) {
            throw new IllegalArgumentException(
                    "Слишком много клеток с водой: " + waters.size() + " (максимум " + MAX_WATER + ")");
        }
        for (int[] row : waterIndex) {
            java.util.Arrays.fill(row, -1);
        }
        for (int i = 0; i < waters.size(); i++) {
            Position p = waters.get(i);
            waterIndex[p.row()][p.col()] = i;
        }
        this.waterCount = waters.size();
        reset();
    }

    /** Начало эпизода: мышь на старте, вся вода снова на месте. */
    public int reset() {
        position = maze.start();
        drunkMask = 0;
        done = false;
        return currentState();
    }

    /** Мышь делает ход, среда сообщает новое состояние и величину выигрыша или проигрыша. */
    public StepResult step(Action action) {
        if (done) {
            throw new IllegalStateException("Эпизод уже закончен, вызовите reset()");
        }
        Position next = position.move(action);
        double reward;
        StepEvent event;

        if (!maze.isPassable(next)) {
            next = position;
            reward = -rewards.wallPenalty();
            event = StepEvent.WALL;
        } else {
            switch (maze.get(next)) {
                case CHEESE -> {
                    reward = rewards.cheese();
                    event = StepEvent.CHEESE;
                    done = true;
                }
                case WATER -> {
                    int bit = 1 << waterIndex[next.row()][next.col()];
                    if ((drunkMask & bit) == 0) {
                        drunkMask |= bit;
                        reward = rewards.water();
                        event = StepEvent.WATER;
                    } else {
                        reward = -rewards.stepCost();
                        event = StepEvent.WATER_ALREADY_DRUNK;
                    }
                }
                case TRAP -> {
                    reward = -rewards.trap();
                    event = StepEvent.TRAP;
                }
                default -> {
                    reward = -rewards.stepCost();
                    event = StepEvent.MOVE;
                }
            }
        }
        position = next;
        return new StepResult(currentState(), reward, done, event);
    }

    public int currentState() {
        return encode(position.row(), position.col(), drunkMask);
    }

    public int encode(int row, int col, int mask) {
        return mask * cellCount + row * maze.cols() + col;
    }

    public Position positionOf(int state) {
        int cell = state % cellCount;
        return new Position(cell / maze.cols(), cell % maze.cols());
    }

    public int maskOf(int state) {
        return state / cellCount;
    }

    public int stateCount() {
        return cellCount * (1 << waterCount);
    }

    public int waterCount() {
        return waterCount;
    }

    public Position position() {
        return position;
    }

    public int drunkMask() {
        return drunkMask;
    }

    public boolean isDone() {
        return done;
    }

    public Maze maze() {
        return maze;
    }

    public RewardConfig rewards() {
        return rewards;
    }
}
