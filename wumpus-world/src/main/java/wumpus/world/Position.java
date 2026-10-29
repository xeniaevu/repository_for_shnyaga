package wumpus.world;

/** Клетка пещеры [x,y]. Нумерация с единицы, [1,1] — левый нижний угол. */
public record Position(int x, int y) {

    /** Соседняя клетка в заданном направлении (может оказаться за пределами поля). */
    public Position step(Direction d) {
        return new Position(x + d.dx, y + d.dy);
    }

    @Override
    public String toString() {
        return "[" + x + "," + y + "]";
    }
}
