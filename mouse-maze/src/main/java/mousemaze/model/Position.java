package mousemaze.model;

/**
 * Координаты клетки (строка, столбец). Неизменяемый объект.
 */
public record Position(int row, int col) {

    public Position move(Action action) {
        return new Position(row + action.dRow(), col + action.dCol());
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
