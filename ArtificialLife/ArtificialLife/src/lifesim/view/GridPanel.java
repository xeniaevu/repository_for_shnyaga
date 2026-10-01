package lifesim.view;

import lifesim.model.Agent;
import lifesim.model.Animal;
import lifesim.model.Environment;
import lifesim.model.Herbivore;
import lifesim.model.Plant;
import lifesim.model.Position;
import lifesim.model.Predator;
import lifesim.model.Simulation;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Отрисовка поля (метод paintComponent).
 * Растение — квадратик (чем ближе к порогу размножения, тем ярче), травоядное — светлый круг,
 * хищник — рыжий ромб. При наведении мыши на животное подсвечивается его область видимости 5×5.
 */
public class GridPanel extends JPanel {
    private static final int PADDING = 16;
    private static final int PREFERRED_CELL = 10;
    private static final int PLANT_SHADES = 12;

    private final Color[] plantShades = new Color[PLANT_SHADES];

    /** Готовые картинки агентов под текущий размер клетки и масштаб экрана (Retina = 2). */
    private final BufferedImage[] plantSprites = new BufferedImage[PLANT_SHADES];
    private BufferedImage herbivoreSprite;
    private BufferedImage predatorSprite;
    private int spriteSize = -1;
    private double spriteScale = -1;

    private Simulation simulation;
    private int hoverX = -1;
    private int hoverY = -1;
    private Runnable hoverListener = () -> { };

    public GridPanel(Simulation simulation) {
        this.simulation = simulation;
        setOpaque(true);
        setBackground(Theme.WINDOW);
        setFocusable(true);
        for (int i = 0; i < PLANT_SHADES; i++) {
            plantShades[i] = Theme.mix(Theme.PLANT_YOUNG, Theme.PLANT, (double) i / (PLANT_SHADES - 1));
        }

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) { hoverAt(e.getX(), e.getY()); }
            @Override public void mouseDragged(MouseEvent e) { hoverAt(e.getX(), e.getY()); }
            @Override public void mouseExited(MouseEvent e) { setHover(-1, -1); }
            @Override public void mousePressed(MouseEvent e) { requestFocusInWindow(); }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
        repaint();
    }

    /** Вызывается, когда курсор переходит на другую клетку. */
    public void setHoverListener(Runnable listener) { this.hoverListener = listener; }

    /** Клетка под курсором или null. */
    public Position getHoveredCell() {
        return simulation.getEnvironment().isInside(hoverX, hoverY) ? new Position(hoverX, hoverY) : null;
    }

    @Override
    public Dimension getPreferredSize() {
        Environment env = simulation.getEnvironment();
        return new Dimension(env.getWidth() * PREFERRED_CELL + 2 * PADDING,
                env.getHeight() * PREFERRED_CELL + 2 * PADDING);
    }

    // ---------- геометрия ----------

    private int cellSize() {
        Environment env = simulation.getEnvironment();
        int byWidth = (getWidth() - 2 * PADDING) / env.getWidth();
        int byHeight = (getHeight() - 2 * PADDING) / env.getHeight();
        return Math.max(2, Math.min(byWidth, byHeight));
    }

    private int offsetX(int cell) { return (getWidth() - cell * simulation.getEnvironment().getWidth()) / 2; }
    private int offsetY(int cell) { return (getHeight() - cell * simulation.getEnvironment().getHeight()) / 2; }

    private void hoverAt(int mx, int my) {
        int cell = cellSize();
        int dx = mx - offsetX(cell);
        int dy = my - offsetY(cell);
        if (dx < 0 || dy < 0) {
            setHover(-1, -1);
            return;
        }
        int x = dx / cell;
        int y = dy / cell;
        if (simulation.getEnvironment().isInside(x, y)) setHover(x, y);
        else setHover(-1, -1);
    }

    private void setHover(int x, int y) {
        if (x == hoverX && y == hoverY) return;
        hoverX = x;
        hoverY = y;
        repaint();
        hoverListener.run();
    }

    // ---------- отрисовка ----------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        Environment env = simulation.getEnvironment();
        int cols = env.getWidth();
        int rows = env.getHeight();
        int cell = cellSize();
        int ox = offsetX(cell);
        int oy = offsetY(cell);
        int w = cell * cols;
        int h = cell * rows;
        boolean lines = cell >= 6;

        // почва и сетка (без сглаживания — линии остаются чёткими)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setColor(Theme.FIELD_BORDER);
        g2.fillRect(ox - 1, oy - 1, w + 2, h + 2);
        g2.setColor(Theme.SOIL);
        g2.fillRect(ox, oy, w, h);
        if (lines) {
            g2.setColor(Theme.SOIL_LINE);
            for (int x = 1; x < cols; x++) g2.fillRect(ox + x * cell, oy, 1, h);
            for (int y = 1; y < rows; y++) g2.fillRect(ox, oy + y * cell, w, 1);
        }

        // агенты
        int inner = lines ? cell - 1 : cell;      // часть клетки без линии сетки
        int shift = lines ? 1 : 0;
        prepareSprites(g2, inner);
        int threshold = Math.max(1, simulation.getConfig().getPlantReproductionThreshold());

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                Agent a = env.getAgent(x, y);
                if (a == null) continue;
                BufferedImage sprite;
                if (a instanceof Plant) {
                    int shade = (int) Math.round((double) a.getEnergy() / threshold * (PLANT_SHADES - 1));
                    sprite = plantSprites[Math.max(0, Math.min(PLANT_SHADES - 1, shade))];
                } else if (a instanceof Herbivore) {
                    sprite = herbivoreSprite;
                } else if (a instanceof Predator) {
                    sprite = predatorSprite;
                } else {
                    continue;
                }
                g2.drawImage(sprite, ox + x * cell + shift, oy + y * cell + shift, inner, inner, null);
            }
        }

        Theme.antialias(g2);
        paintHover(g2, env, cell, ox, oy);
        g2.dispose();
    }

    /** Нарисовать фигуры агентов один раз; дальше они только копируются (быстро даже на 60 шагах/с). */
    private void prepareSprites(Graphics2D g2, int inner) {
        double scale = Math.max(1, g2.getTransform().getScaleX());
        if (inner == spriteSize && scale == spriteScale) return;
        spriteSize = inner;
        spriteScale = scale;
        int px = Math.max(1, (int) Math.ceil(inner * scale));
        GraphicsConfiguration gc = g2.getDeviceConfiguration();

        float arc = inner * 0.42f;
        for (int i = 0; i < PLANT_SHADES; i++) {
            Color c = plantShades[i];
            plantSprites[i] = sprite(gc, px, inner, s -> {
                s.setColor(c);
                s.fill(new RoundRectangle2D.Float(0, 0, inner, inner, arc, arc));
            });
        }
        float inset = Math.max(0.5f, inner * 0.1f);
        herbivoreSprite = sprite(gc, px, inner, s -> {
            s.setColor(Theme.HERBIVORE);
            s.fill(new Ellipse2D.Float(inset, inset, inner - 2 * inset, inner - 2 * inset));
        });
        predatorSprite = sprite(gc, px, inner, s -> {
            s.setColor(Theme.PREDATOR);
            s.fill(Theme.diamond(inner / 2f, inner / 2f, inner / 2f));
        });
    }

    private static BufferedImage sprite(GraphicsConfiguration gc, int px, int inner, Consumer<Graphics2D> painter) {
        BufferedImage img = gc != null
                ? gc.createCompatibleImage(px, px, Transparency.TRANSLUCENT)
                : new BufferedImage(px, px, BufferedImage.TYPE_INT_ARGB);
        Graphics2D s = img.createGraphics();
        Theme.antialias(s);
        s.scale((double) px / inner, (double) px / inner);
        painter.accept(s);
        s.dispose();
        return img;
    }

    /** Подсветка клетки под курсором и области видимости животного (5×5). */
    private void paintHover(Graphics2D g2, Environment env, int cell, int ox, int oy) {
        if (!env.isInside(hoverX, hoverY)) return;
        Agent a = env.getAgent(hoverX, hoverY);
        if (a instanceof Animal) {
            int r = Animal.VISION_RADIUS;
            int x0 = Math.max(0, hoverX - r);
            int y0 = Math.max(0, hoverY - r);
            int x1 = Math.min(env.getWidth() - 1, hoverX + r);
            int y1 = Math.min(env.getHeight() - 1, hoverY + r);
            Color c = a instanceof Predator ? Theme.PREDATOR : Theme.HERBIVORE;
            float vx = ox + x0 * cell;
            float vy = oy + y0 * cell;
            float vw = (x1 - x0 + 1) * cell + 1;
            float vh = (y1 - y0 + 1) * cell + 1;
            g2.setColor(Theme.alpha(c, 34));
            g2.fill(new RoundRectangle2D.Float(vx, vy, vw, vh, 6, 6));
            g2.setColor(Theme.alpha(c, 170));
            g2.setStroke(new BasicStroke(1.25f));
            g2.draw(new RoundRectangle2D.Float(vx + 0.5f, vy + 0.5f, vw - 1, vh - 1, 6, 6));
        }
        g2.setColor(Theme.TEXT);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Float(ox + hoverX * cell - 0.25f, oy + hoverY * cell - 0.25f,
                cell + 1.5f, cell + 1.5f, 4, 4));
    }
}
