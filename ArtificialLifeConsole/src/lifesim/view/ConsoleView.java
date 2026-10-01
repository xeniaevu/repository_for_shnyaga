package lifesim.view;

import lifesim.model.Agent;
import lifesim.model.Environment;
import lifesim.model.Herbivore;
import lifesim.model.Plant;
import lifesim.model.PopulationStats;
import lifesim.model.Predator;
import lifesim.model.Simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * Консольное представление поля.
 *
 * Символ в консоли примерно в 2–3 раза выше, чем шире, поэтому клетка выводится шириной
 * до 3 символов (значок по центру): так сетка получается почти квадратной.
 * Если поле широкое, ширина клетки уменьшается до 2 или 1 символа, чтобы строка
 * не превышала MAX_LINE символов.
 *
 * С цветами (ANSI, 256 цветов): поле на своём тёмном фоне,
 * «·» пусто, «■» растение (чем ярче, тем ближе к размножению), «●» травоядное, «◆» хищник.
 * Без цветов: те же клетки символами . P T X в рамке из + - |.
 */
public class ConsoleView {

    /** Наибольшая длина строки вывода, при которой поле ещё помещается в окне консоли. */
    private static final int MAX_LINE = 130;

    // ---------- коды цветов из палитры 256 цветов ----------
    private static final int SOIL = 235;          // фон поля
    private static final int GRID = 239;          // точка пустой клетки
    private static final int LINE = 241;          // разделитель над строкой численности
    private static final int TEXT = 250;          // подписи
    private static final int HERBIVORE = 223;     // светлый, «шерсть»
    private static final int PREDATOR = 208;      // рыжий
    private static final int WARNING = 203;       // сообщение о вымирании
    /** Растения: от молодого (тёмно-зелёный) до готового к размножению (яркий). */
    private static final int[] PLANT = {58, 64, 70, 76, 112};

    private static final char EMPTY_CELL = '·';
    private static final char PLANT_CELL = '■';
    private static final char HERBIVORE_CELL = '●';
    private static final char PREDATOR_CELL = '◆';

    private static final String RESET = "\u001B[0m";
    private static final String PAD = "  ";       // отступ внутри тёмной карточки слева и справа

    private final boolean colors;

    /** @param colors true — цветной вывод, false — простые символы без цвета */
    public ConsoleView(boolean colors) {
        this.colors = colors;
    }

    /** Вывести поле и численность видов. */
    public void render(Simulation sim) {
        System.out.println(colors ? colored(sim) : plain(sim));
    }

    /** Строка-легенда для подсказки (на тёмной полосе, чтобы читалась и в светлой консоли). */
    public String legend() {
        if (!colors) return "Обозначения: P — растение, T — травоядное, X — хищник, . — пустая клетка.";
        return bg(SOIL) + " " + fg(GRID) + EMPTY_CELL + fg(TEXT) + " пусто   "
                + fg(PLANT[PLANT.length - 1]) + PLANT_CELL + fg(TEXT) + " растение (ярче — ближе к размножению)   "
                + fg(HERBIVORE) + HERBIVORE_CELL + fg(TEXT) + " травоядное   "
                + fg(PREDATOR) + PREDATOR_CELL + fg(TEXT) + " хищник " + RESET;
    }

    /** Текст предупреждения (красным, если цвета включены). */
    public String warning(String text) {
        return colors ? fg(WARNING) + text + RESET : text;
    }

    // ---------- размеры клетки ----------

    /** Ширина клетки в символах: 3, если помещается, иначе 2 или 1. */
    private static int cellWidth(int fieldWidth) {
        int fit = (MAX_LINE - 2 * PAD.length()) / Math.max(1, fieldWidth);
        return Math.max(1, Math.min(3, fit));
    }

    /** Ширина строки поля: при чётной ширине клетки добавляется пробел, чтобы поле стояло по центру. */
    private static int rowWidth(int fieldWidth, int cell) {
        return fieldWidth * cell + (cell % 2 == 0 ? 1 : 0);
    }

    // ---------- цветной вывод ----------

    private String colored(Simulation sim) {
        Environment env = sim.getEnvironment();
        int w = env.getWidth();
        int h = env.getHeight();
        int cell = cellWidth(w);
        int left = (cell - 1) / 2;                    // пробелы слева и справа от значка
        int right = cell - 1 - left;
        int threshold = Math.max(1, sim.getConfig().getPlantReproductionThreshold());

        List<String> status = statusLines(sim.getStats(), rowWidth(w, cell));
        int inner = Math.max(rowWidth(w, cell), maxVisible(status));
        int shiftLeft = (inner - rowWidth(w, cell)) / 2;
        int shiftRight = inner - rowWidth(w, cell) - shiftLeft;

        StringBuilder sb = new StringBuilder(w * h * (cell + 3));
        sb.append(blankLine(inner));
        for (int y = 0; y < h; y++) {
            sb.append(bg(SOIL)).append(PAD).append(" ".repeat(shiftLeft));
            if (cell % 2 == 0) sb.append(' ');
            int current = -1;                          // код цвета пишем, только когда он меняется
            for (int x = 0; x < w; x++) {
                Agent a = env.getAgent(x, y);
                int color;
                char mark;
                if (a instanceof Plant) {
                    int shade = (int) Math.round((double) a.getEnergy() / threshold * (PLANT.length - 1));
                    color = PLANT[Math.max(0, Math.min(PLANT.length - 1, shade))];
                    mark = PLANT_CELL;
                } else if (a instanceof Herbivore) {
                    color = HERBIVORE;
                    mark = HERBIVORE_CELL;
                } else if (a instanceof Predator) {
                    color = PREDATOR;
                    mark = PREDATOR_CELL;
                } else {
                    color = GRID;
                    mark = EMPTY_CELL;
                }
                if (color != current) {
                    sb.append(fg(color));
                    current = color;
                }
                sb.append(" ".repeat(left)).append(mark).append(" ".repeat(right));
            }
            sb.append(" ".repeat(shiftRight)).append(PAD).append(RESET).append('\n');
        }
        sb.append(bg(SOIL)).append(fg(LINE)).append(PAD).append("─".repeat(inner)).append(PAD)
                .append(RESET).append('\n');
        for (String line : status) {
            sb.append(bg(SOIL)).append(PAD).append(line)
                    .append(" ".repeat(inner - visible(line))).append(PAD).append(RESET).append('\n');
        }
        sb.append(blankLine(inner));
        return sb.toString();
    }

    private static String blankLine(int inner) {
        return bg(SOIL) + " ".repeat(inner + 2 * PAD.length()) + RESET + "\n";
    }

    /**
     * Номер шага и численность видов (значки — те же, что на поле).
     * Если поле узкое, части переносятся на следующие строки.
     */
    private List<String> statusLines(PopulationStats s, int width) {
        String[] parts = {
                fg(TEXT) + "Шаг " + s.getStep(),
                fg(PLANT[PLANT.length - 1]) + PLANT_CELL + fg(TEXT) + " Растения " + s.getPlants(),
                fg(HERBIVORE) + HERBIVORE_CELL + fg(TEXT) + " Травоядные " + s.getHerbivores(),
                fg(PREDATOR) + PREDATOR_CELL + fg(TEXT) + " Хищники " + s.getPredators(),
        };
        String gap = "   ";
        List<String> lines = new ArrayList<>();
        String line = "";
        for (String part : parts) {
            if (line.isEmpty()) {
                line = part;
            } else if (visible(line) + gap.length() + visible(part) <= width) {
                line += gap + part;
            } else {
                lines.add(line);
                line = part;
            }
        }
        lines.add(line);
        return lines;
    }

    /** Видимая длина строки (коды цвета места не занимают). */
    private static int visible(String text) {
        return text.replaceAll("\u001B\\[[0-9;]*m", "").length();
    }

    private static int maxVisible(List<String> lines) {
        int max = 0;
        for (String l : lines) max = Math.max(max, visible(l));
        return max;
    }

    private static String fg(int color) { return "\u001B[38;5;" + color + "m"; }
    private static String bg(int color) { return "\u001B[48;5;" + color + "m"; }

    // ---------- вывод без цветов ----------

    private String plain(Simulation sim) {
        Environment env = sim.getEnvironment();
        int w = env.getWidth();
        int cell = cellWidth(w);
        int left = (cell - 1) / 2;
        int right = cell - 1 - left;
        int inner = rowWidth(w, cell);

        StringBuilder sb = new StringBuilder();
        sb.append('+').append("-".repeat(inner)).append("+\n");
        for (int y = 0; y < env.getHeight(); y++) {
            sb.append('|');
            if (cell % 2 == 0) sb.append(' ');
            for (int x = 0; x < w; x++) {
                Agent a = env.getAgent(x, y);
                sb.append(" ".repeat(left)).append(a == null ? '.' : a.getSymbol()).append(" ".repeat(right));
            }
            sb.append("|\n");
        }
        sb.append('+').append("-".repeat(inner)).append("+\n");
        sb.append(sim.getStats());
        return sb.toString();
    }
}
