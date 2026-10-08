package mousemaze.ui;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import mousemaze.agent.QLearningAgent;
import mousemaze.env.Environment;
import mousemaze.model.Action;
import mousemaze.model.CellType;
import mousemaze.model.Maze;
import mousemaze.model.Position;
import mousemaze.training.EpisodeResult;

/**
 * Отрисовка лабиринта: клетки с сеткой (одна клетка = один шаг),
 * сыр, вода, ловушки, мышь, траектория эпизода и стрелки выученной политики.
 * Холст подстраивается под размер окна.
 */
public class MazeCanvas extends Region {

    private static final Color WALL = Color.web("#3C3489");
    private static final Color FLOOR = Color.web("#EEEDFE");
    private static final Color GRID = Color.web("#534AB7", 0.22);
    private static final Color DRUNK_FLOOR = Color.web("#DCE9F7");
    private static final Color CHEESE = Color.web("#EF9F27");
    private static final Color CHEESE_HOLE = Color.web("#BA7517");
    private static final Color WATER = Color.web("#378ADD");
    private static final Color TRAP = Color.web("#E24B4A");
    private static final Color MOUSE = Color.web("#888780");
    private static final Color MOUSE_EAR = Color.web("#F4C0D1");
    private static final Color PATH = Color.web("#D85A30", 0.45);
    private static final Color BEST_PATH = Color.web("#1D9E75", 0.8);
    private static final Color HOVER = Color.web("#D85A30");

    private final Canvas canvas = new Canvas();

    private Maze maze;
    private Environment env;
    private QLearningAgent agent;
    private EpisodeResult episode;
    private int shownSteps;
    private boolean bestPathStyle;
    private boolean showPolicy;
    private boolean showStepNumbers;
    private Position hover;

    private double cell;
    private double offsetX;
    private double offsetY;

    private BiConsumer<Position, MouseButton> onCellPaint;
    private Consumer<Position> onCellHover;

    public MazeCanvas() {
        getChildren().add(canvas);
        setMinSize(200, 200);
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::paintEvent);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            paintEvent(e);
            hoverEvent(e);
        });
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, this::hoverEvent);
        canvas.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            hover = null;
            if (onCellHover != null) {
                onCellHover.accept(null);
            }
            redraw();
        });
    }

    @Override
    protected void layoutChildren() {
        double left = snappedLeftInset();
        double top = snappedTopInset();
        canvas.relocate(left, top);
        canvas.setWidth(Math.max(0, getWidth() - left - snappedRightInset()));
        canvas.setHeight(Math.max(0, getHeight() - top - snappedBottomInset()));
        redraw();
    }

    // ---------- данные для отрисовки ----------

    public void setMaze(Maze maze) {
        this.maze = maze;
        this.episode = null;
        this.env = null;
        this.agent = null;
        redraw();
    }

    public void setModel(Environment env, QLearningAgent agent) {
        this.env = env;
        this.agent = agent;
        redraw();
    }

    /** Показать эпизод целиком или его первые {@code steps} шагов. */
    public void showEpisode(EpisodeResult episode, int steps, boolean bestPathStyle) {
        this.episode = episode;
        this.shownSteps = episode == null ? 0 : Math.min(steps, episode.steps());
        this.bestPathStyle = bestPathStyle;
        redraw();
    }

    public void setShowPolicy(boolean value) {
        showPolicy = value;
        redraw();
    }

    public void setShowStepNumbers(boolean value) {
        showStepNumbers = value;
        redraw();
    }

    public void setOnCellPaint(BiConsumer<Position, MouseButton> handler) {
        this.onCellPaint = handler;
    }

    public void setOnCellHover(Consumer<Position> handler) {
        this.onCellHover = handler;
    }

    /** Маска выпитой воды в показанный момент эпизода (0, если эпизод не выбран). */
    public int currentMask() {
        if (episode == null || env == null) {
            return 0;
        }
        return env.maskOf(episode.states()[shownSteps]);
    }

    public Canvas canvas() {
        return canvas;
    }

    // ---------- мышь ----------

    private Position cellAt(double x, double y) {
        if (maze == null || cell <= 0) {
            return null;
        }
        int c = (int) Math.floor((x - offsetX) / cell);
        int r = (int) Math.floor((y - offsetY) / cell);
        Position p = new Position(r, c);
        return maze.inBounds(p) ? p : null;
    }

    private void paintEvent(MouseEvent e) {
        Position p = cellAt(e.getX(), e.getY());
        if (p != null && onCellPaint != null) {
            onCellPaint.accept(p, e.getButton());
        }
    }

    private void hoverEvent(MouseEvent e) {
        Position p = cellAt(e.getX(), e.getY());
        if (p == null ? hover != null : !p.equals(hover)) {
            hover = p;
            if (onCellHover != null) {
                onCellHover.accept(p);
            }
            redraw();
        }
    }

    // ---------- рисование ----------

    public void redraw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        g.clearRect(0, 0, w, h);
        if (maze == null) {
            return;
        }
        cell = Math.floor(Math.min(w / maze.cols(), h / maze.rows()));
        if (cell < 2) {
            return;
        }
        offsetX = Math.floor((w - cell * maze.cols()) / 2);
        offsetY = Math.floor((h - cell * maze.rows()) / 2);

        int mask = currentMask();
        drawCells(g, mask);
        drawGrid(g);
        if (showPolicy) {
            drawPolicy(g, mask);
        }
        drawPath(g);
        drawItems(g, mask);
        drawMouse(g);
        if (showStepNumbers) {
            drawStepNumbers(g);
        }
        if (hover != null) {
            g.setStroke(HOVER);
            g.setLineWidth(2);
            g.strokeRect(x(hover.col()) + 1, y(hover.row()) + 1, cell - 2, cell - 2);
        }
    }

    private double x(int col) {
        return offsetX + col * cell;
    }

    private double y(int row) {
        return offsetY + row * cell;
    }

    private double cx(int col) {
        return x(col) + cell / 2;
    }

    private double cy(int row) {
        return y(row) + cell / 2;
    }

    private void drawCells(GraphicsContext g, int mask) {
        for (int r = 0; r < maze.rows(); r++) {
            for (int c = 0; c < maze.cols(); c++) {
                CellType t = maze.get(r, c);
                g.setFill(t == CellType.WALL ? WALL : FLOOR);
                if (t == CellType.WATER && isDrunk(r, c, mask)) {
                    g.setFill(DRUNK_FLOOR);
                }
                g.fillRect(x(c), y(r), cell + 0.5, cell + 0.5);
            }
        }
    }

    private void drawGrid(GraphicsContext g) {
        if (cell < 6) {
            return;
        }
        g.setStroke(GRID);
        g.setLineWidth(1);
        for (int r = 0; r <= maze.rows(); r++) {
            g.strokeLine(offsetX, y(r) + 0.5, offsetX + maze.cols() * cell, y(r) + 0.5);
        }
        for (int c = 0; c <= maze.cols(); c++) {
            g.strokeLine(x(c) + 0.5, offsetY, x(c) + 0.5, offsetY + maze.rows() * cell);
        }
    }

    private boolean isDrunk(int r, int c, int mask) {
        if (env == null || mask == 0) {
            return false;
        }
        int idx = 0;
        for (Position p : maze.positionsOf(CellType.WATER)) {
            if (p.row() == r && p.col() == c) {
                return (mask & (1 << idx)) != 0;
            }
            idx++;
        }
        return false;
    }

    private void drawItems(GraphicsContext g, int mask) {
        double k = cell;
        for (int r = 0; r < maze.rows(); r++) {
            for (int c = 0; c < maze.cols(); c++) {
                double cx = cx(c);
                double cy = cy(r);
                switch (maze.get(r, c)) {
                    case CHEESE -> {
                        g.setFill(CHEESE);
                        g.fillPolygon(new double[]{cx - k * .38, cx + k * .38, cx + k * .38},
                                new double[]{cy + k * .3, cy + k * .3, cy - k * .3}, 3);
                        g.setFill(CHEESE_HOLE);
                        g.fillOval(cx + k * .1, cy + k * .05, k * .12, k * .12);
                        g.fillOval(cx + k * .22, cy - k * .12, k * .08, k * .08);
                    }
                    case WATER -> {
                        g.setFill(isDrunk(r, c, mask) ? WATER.deriveColor(0, 0.4, 1.2, 0.5) : WATER);
                        g.fillOval(cx - k * .22, cy - k * .14, k * .44, k * .44);
                        g.fillPolygon(new double[]{cx - k * .19, cx, cx + k * .19},
                                new double[]{cy, cy - k * .36, cy}, 3);
                    }
                    case TRAP -> {
                        g.setFill(TRAP);
                        g.fillPolygon(
                                new double[]{cx + k * .08, cx - k * .2, cx, cx - k * .08, cx + k * .2, cx},
                                new double[]{cy - k * .38, cy + k * .04, cy + k * .04, cy + k * .38, cy - k * .04, cy - k * .04},
                                6);
                    }
                    case START -> {
                        g.setStroke(MOUSE);
                        g.setLineWidth(Math.max(1, k * .06));
                        g.strokeOval(cx - k * .3, cy - k * .3, k * .6, k * .6);
                    }
                    default -> { }
                }
            }
        }
    }

    private Position mousePosition() {
        if (episode != null && env != null) {
            return env.positionOf(episode.states()[shownSteps]);
        }
        return maze.start();
    }

    private void drawMouse(GraphicsContext g) {
        Position p = mousePosition();
        if (p == null) {
            return;
        }
        double k = cell;
        double cx = cx(p.col());
        double cy = cy(p.row());
        g.setFill(MOUSE);
        g.fillOval(cx - k * .3, cy - k * .24, k * .6, k * .6);
        g.fillOval(cx - k * .38, cy - k * .4, k * .3, k * .3);
        g.fillOval(cx + k * .08, cy - k * .4, k * .3, k * .3);
        g.setFill(MOUSE_EAR);
        g.fillOval(cx - k * .32, cy - k * .34, k * .18, k * .18);
        g.fillOval(cx + k * .14, cy - k * .34, k * .18, k * .18);
        g.setFill(Color.web("#2C2C2A"));
        g.fillOval(cx - k * .05, cy + k * .2, k * .1, k * .1);
    }

    private void drawPath(GraphicsContext g) {
        if (episode == null || env == null || shownSteps < 1) {
            return;
        }
        g.setStroke(bestPathStyle ? BEST_PATH : PATH);
        g.setLineWidth(Math.max(2, cell * (bestPathStyle ? .22 : .18)));
        g.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        g.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        g.beginPath();
        int[] states = episode.states();
        for (int i = 0; i <= shownSteps; i++) {
            Position p = env.positionOf(states[i]);
            if (i == 0) {
                g.moveTo(cx(p.col()), cy(p.row()));
            } else {
                g.lineTo(cx(p.col()), cy(p.row()));
            }
        }
        g.stroke();
    }

    /** Номер шага, на котором мышь впервые оказалась в клетке. */
    private void drawStepNumbers(GraphicsContext g) {
        if (episode == null || env == null || cell < 14) {
            return;
        }
        boolean[][] labeled = new boolean[maze.rows()][maze.cols()];
        g.setFont(Font.font("System", FontWeight.BOLD, Math.max(9, cell * .32)));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        int[] states = episode.states();
        for (int i = 1; i <= shownSteps; i++) {
            Position p = env.positionOf(states[i]);
            if (labeled[p.row()][p.col()] || maze.get(p) != CellType.EMPTY) {
                continue;
            }
            labeled[p.row()][p.col()] = true;
            g.setFill(Color.web("#E1F5EE"));
            g.fillOval(cx(p.col()) - cell * .32, cy(p.row()) - cell * .32, cell * .64, cell * .64);
            g.setFill(Color.web("#085041"));
            g.fillText(String.valueOf(i), cx(p.col()), cy(p.row()));
        }
    }

    /** Стрелка лучшего действия по Q-таблице в каждой клетке, которую мышь изучила. */
    private void drawPolicy(GraphicsContext g, int mask) {
        if (agent == null || env == null || cell < 10) {
            return;
        }
        g.setFont(Font.font("System", FontWeight.NORMAL, cell * .55));
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.setFill(Color.web("#444441", 0.75));
        for (int r = 0; r < maze.rows(); r++) {
            for (int c = 0; c < maze.cols(); c++) {
                CellType t = maze.get(r, c);
                if (t != CellType.EMPTY && t != CellType.START) {
                    continue;
                }
                int state = env.encode(r, c, mask);
                if (!agent.isVisited(state)) {
                    continue;
                }
                Action best = Action.UP;
                for (Action a : Action.ALL) {
                    if (agent.getQ(state, a) > agent.getQ(state, best)) {
                        best = a;
                    }
                }
                g.fillText(best.arrow(), cx(c), cy(r));
            }
        }
    }
}
