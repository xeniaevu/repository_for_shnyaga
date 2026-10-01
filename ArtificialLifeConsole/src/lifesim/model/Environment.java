package lifesim.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Окружающая среда — двумерная дискретная сетка W×H.
 * В каждой клетке находится не более одного агента (null — клетка пуста).
 */
public class Environment {
    private final int width;
    private final int height;
    private final Agent[][] cells;

    public Environment(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Размер поля должен быть положительным");
        }
        this.width = width;
        this.height = height;
        this.cells = new Agent[height][width];
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public boolean isInside(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public boolean isEmpty(int x, int y) {
        return isInside(x, y) && cells[y][x] == null;
    }


    public Agent getAgent(int x, int y) {
        return isInside(x, y) ? cells[y][x] : null;
    }

    public void setAgent(int x, int y, Agent agent) {
        if (!isInside(x, y)) {
            throw new IndexOutOfBoundsException("Клетка вне поля: " + x + ", " + y);
        }
        cells[y][x] = agent;
        if (agent != null) {
            agent.setPosition(x, y);
        }
    }

    /** Перемещение агента в другую (пустую) клетку. */
    public void moveAgent(Agent agent, int newX, int newY) {
        if (!isEmpty(newX, newY)) {
            throw new IllegalStateException("Клетка занята: " + newX + ", " + newY);
        }
        cells[agent.getY()][agent.getX()] = null;
        setAgent(newX, newY, agent);
    }

    /**
     * Список соседних клеток в квадрате (2*radius+1)×(2*radius+1) с центром в (x, y),
     * сама центральная клетка не включается. radius = 2 даёт область видимости 5×5.
     */
    public List<Position> getNeighbors(int x, int y, int radius) {
        List<Position> result = new ArrayList<>();
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (isInside(nx, ny)) {
                    result.add(new Position(nx, ny));
                }
            }
        }
        return result;
    }

    /** Соседи по вертикали/горизонтали (максимум 4 клетки). */
    public List<Position> getOrthogonalNeighbors(int x, int y) {
        List<Position> result = new ArrayList<>(4);
        for (Direction d : Direction.values()) {
            int nx = x + d.getDx();
            int ny = y + d.getDy();
            if (isInside(nx, ny)) {
                result.add(new Position(nx, ny));
            }
        }
        return result;
    }

    /** Свободные соседние клетки по вертикали/горизонтали. */
    public List<Position> getEmptyOrthogonalNeighbors(int x, int y) {
        List<Position> result = new ArrayList<>(4);
        for (Position p : getOrthogonalNeighbors(x, y)) {
            if (cells[p.getY()][p.getX()] == null) {
                result.add(p);
            }
        }
        return result;
    }

    public List<Position> getEmptyCells() {
        List<Position> result = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (cells[y][x] == null) result.add(new Position(x, y));
            }
        }
        return result;
    }

    public void clear() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = null;
            }
        }
    }
}
