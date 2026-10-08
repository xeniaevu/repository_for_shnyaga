package mousemaze.model;

/**
 * Тип клетки лабиринта. Символ используется в текстовом формате схемы.
 */
public enum CellType {
    EMPTY('.', "Проход"),
    WALL('#', "Стена"),
    START('S', "Старт (мышь)"),
    CHEESE('C', "Сыр (+Z)"),
    WATER('W', "Вода (+x)"),
    TRAP('T', "Ток (−y)");

    private final char symbol;
    private final String title;

    CellType(char symbol, String title) {
        this.symbol = symbol;
        this.title = title;
    }

    public char symbol() {
        return symbol;
    }

    public String title() {
        return title;
    }

    public boolean isPassable() {
        return this != WALL;
    }

    public static CellType fromSymbol(char c) {
        for (CellType t : values()) {
            if (t.symbol == Character.toUpperCase(c)) {
                return t;
            }
        }
        if (c == ' ') {
            return EMPTY;
        }
        throw new IllegalArgumentException("Неизвестный символ клетки: '" + c + "'");
    }

    @Override
    public String toString() {
        return title;
    }
}
