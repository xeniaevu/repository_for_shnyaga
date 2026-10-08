package mousemaze.model;

/**
 * Действие мыши: шаг в одну из четырёх сторон.
 * Порядковый номер (ordinal) — это номер столбца в Q-таблице.
 */
public enum Action {
    UP(-1, 0, "↑", "вверх"),
    DOWN(1, 0, "↓", "вниз"),
    LEFT(0, -1, "←", "влево"),
    RIGHT(0, 1, "→", "вправо");

    /** Кэш values(), чтобы не создавать массив на каждом шаге обучения. */
    public static final Action[] ALL = values();
    public static final int COUNT = ALL.length;

    private final int dRow;
    private final int dCol;
    private final String arrow;
    private final String title;

    Action(int dRow, int dCol, String arrow, String title) {
        this.dRow = dRow;
        this.dCol = dCol;
        this.arrow = arrow;
        this.title = title;
    }

    public int dRow() {
        return dRow;
    }

    public int dCol() {
        return dCol;
    }

    public String arrow() {
        return arrow;
    }

    public String title() {
        return title;
    }
}
