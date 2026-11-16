package antcolony.ui;

import antcolony.algorithm.Ant;
import antcolony.algorithm.AntColony;
import antcolony.model.Graph;
import antcolony.model.Node;
import antcolony.model.Path;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.List;
import java.util.Locale;

/**
 * Визуализация: граф, феромон на путях (толщина и яркость линии),
 * лучший маршрут и движущиеся муравьи. Узлы можно перетаскивать мышью.
 */
public class GraphPanel extends JPanel {

    private static final Color BACKGROUND = new Color(250, 249, 245);
    private static final Color PATH_BASE = new Color(205, 205, 200);
    private static final Color PHEROMONE = new Color(232, 120, 30);
    private static final Color BEST = new Color(25, 140, 75);
    private static final Color NODE_FILL = Color.WHITE;
    private static final Color NODE_BORDER = new Color(55, 60, 70);
    private static final Color ANT = new Color(45, 30, 25);
    private static final double NODE_RADIUS = 17;

    private AntColony colony;
    private double progress;
    private boolean showAnts = true;
    private boolean showBestTour = true;
    private boolean showPheromoneValues;
    private Runnable graphChangeListener;

    private double scale = 1;
    private double offsetX;
    private double offsetY;
    private Node draggedNode;

    public GraphPanel() {
        setBackground(BACKGROUND);
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                draggedNode = findNode(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedNode == null) {
                    return;
                }
                double x = (e.getX() - offsetX) / scale;
                double y = (e.getY() - offsetY) / scale;
                draggedNode.moveTo(
                        Math.max(25, Math.min(Graph.WIDTH - 25, x)),
                        Math.max(25, Math.min(Graph.HEIGHT - 25, y)));
                colony.forgetBestTour();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (draggedNode != null && graphChangeListener != null) {
                    graphChangeListener.run();
                }
                draggedNode = null;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                setCursor(findNode(e.getX(), e.getY()) != null
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setColony(AntColony colony) {
        this.colony = colony;
        repaint();
    }

    /** Доля пройденного муравьями текущего ребра: 0 — в начале, 1 — в конце. */
    public void setProgress(double progress) {
        this.progress = Math.max(0, Math.min(1, progress));
    }

    public void setShowAnts(boolean value) {
        showAnts = value;
        repaint();
    }

    public void setShowBestTour(boolean value) {
        showBestTour = value;
        repaint();
    }

    public void setShowPheromoneValues(boolean value) {
        showPheromoneValues = value;
        repaint();
    }

    /** Вызывается, когда пользователь закончил перетаскивать узел. */
    public void setGraphChangeListener(Runnable listener) {
        graphChangeListener = listener;
    }

    private Node findNode(int screenX, int screenY) {
        if (colony == null) {
            return null;
        }
        double x = (screenX - offsetX) / scale;
        double y = (screenY - offsetY) / scale;
        for (Node node : colony.getGraph().getNodes()) {
            if (Math.hypot(node.getX() - x, node.getY() - y) <= NODE_RADIUS + 4) {
                return node;
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (colony == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // масштабируем условное поле под размер панели
        scale = Math.min(getWidth() / Graph.WIDTH, getHeight() / Graph.HEIGHT);
        offsetX = (getWidth() - Graph.WIDTH * scale) / 2;
        offsetY = (getHeight() - Graph.HEIGHT * scale) / 2;
        g2.translate(offsetX, offsetY);
        g2.scale(scale, scale);

        Graph graph = colony.getGraph();
        drawPaths(g2, graph);
        if (showBestTour) {
            drawBestTour(g2);
        }
        if (showPheromoneValues) {
            drawPheromoneValues(g2, graph);
        }
        drawNodes(g2, graph);
        if (showAnts) {
            for (Ant ant : colony.getAnts()) {
                drawAnt(g2, ant);
            }
        }
        g2.dispose();
    }

    /**
     * Относительная «сила» пути от 0 до 1. Равна 1, когда весь феромон графа
     * сосредоточен на рёбрах одного маршрута (алгоритм полностью сошёлся).
     */
    private double strength(Path path, Graph graph, double totalPheromone) {
        if (totalPheromone <= 0) {
            return 0;
        }
        return Math.min(1.0, path.getPheromone() * graph.size() / totalPheromone);
    }

    private void drawPaths(Graphics2D g2, Graph graph) {
        double total = graph.getTotalPheromone();
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(PATH_BASE);
        for (Path path : graph.getPaths()) {
            g2.draw(line(path));
        }
        for (Path path : graph.getPaths()) {
            double s = strength(path, graph, total);
            if (s < 0.01) {
                continue;
            }
            g2.setStroke(new BasicStroke((float) (1 + 10 * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(PHEROMONE.getRed(), PHEROMONE.getGreen(), PHEROMONE.getBlue(),
                    (int) (50 + 190 * s)));
            g2.draw(line(path));
        }
    }

    private void drawBestTour(Graphics2D g2) {
        List<Path> best = colony.getBestTourPaths();
        if (best == null) {
            return;
        }
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                10f, new float[]{10f, 8f}, 0f));
        g2.setColor(BEST);
        for (Path path : best) {
            g2.draw(line(path));
        }
    }

    private void drawPheromoneValues(Graphics2D g2, Graph graph) {
        double total = graph.getTotalPheromone();
        g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        FontMetrics fm = g2.getFontMetrics();
        boolean all = graph.size() <= 7;
        for (Path path : graph.getPaths()) {
            if (!all && strength(path, graph, total) < 0.15) {
                continue;
            }
            String text = String.format(Locale.US, "%.3f", path.getPheromone());
            // подпись на трети ребра, чтобы подписи не слипались в центре графа
            double x = path.getFirst().getX() * 0.62 + path.getSecond().getX() * 0.38;
            double y = path.getFirst().getY() * 0.62 + path.getSecond().getY() * 0.38;
            int w = fm.stringWidth(text);
            g2.setColor(new Color(255, 255, 255, 215));
            g2.fillRoundRect((int) (x - w / 2.0 - 3), (int) (y - 9), w + 6, 17, 6, 6);
            g2.setColor(new Color(120, 60, 10));
            g2.drawString(text, (float) (x - w / 2.0), (float) (y + 4));
        }
    }

    private void drawNodes(Graphics2D g2, Graph graph) {
        g2.setFont(getFont().deriveFont(Font.BOLD, 15f));
        FontMetrics fm = g2.getFontMetrics();
        for (Node node : graph.getNodes()) {
            Ellipse2D circle = new Ellipse2D.Double(node.getX() - NODE_RADIUS, node.getY() - NODE_RADIUS,
                    2 * NODE_RADIUS, 2 * NODE_RADIUS);
            g2.setColor(NODE_FILL);
            g2.fill(circle);
            g2.setStroke(new BasicStroke(2.2f));
            g2.setColor(NODE_BORDER);
            g2.draw(circle);
            String label = node.toString();
            g2.drawString(label,
                    (float) (node.getX() - fm.stringWidth(label) / 2.0),
                    (float) (node.getY() + fm.getAscent() / 2.0 - 2));
        }
    }

    private void drawAnt(Graphics2D g2, Ant ant) {
        Node from = ant.getPreviousNode();
        Node to = ant.getCurrentNode();
        double x;
        double y;
        double angle;
        if (from == to) {
            // муравей ждёт в узле: расставляем муравьёв вокруг узла
            angle = ant.getId() * 2.4;
            x = to.getX() + (NODE_RADIUS + 13) * Math.cos(angle);
            y = to.getY() + (NODE_RADIUS + 13) * Math.sin(angle);
        } else {
            double dx = to.getX() - from.getX();
            double dy = to.getY() - from.getY();
            double length = Math.max(Math.hypot(dx, dy), 1e-6);
            angle = Math.atan2(dy, dx);
            // небольшое смещение вбок, чтобы муравьи на одном ребре не сливались
            double side = ((ant.getId() % 5) - 2) * 4.0;
            x = from.getX() + dx * progress - dy / length * side;
            y = from.getY() + dy * progress + dx / length * side;
        }

        AffineTransform saved = g2.getTransform();
        g2.translate(x, y);
        g2.rotate(angle);
        g2.scale(1.25, 1.25);
        g2.setColor(ANT);
        g2.setStroke(new BasicStroke(1f));
        // ножки и усики
        g2.draw(new Line2D.Double(-1, 0, -5, -7));
        g2.draw(new Line2D.Double(-1, 0, -5, 7));
        g2.draw(new Line2D.Double(0, 0, 0, -7));
        g2.draw(new Line2D.Double(0, 0, 0, 7));
        g2.draw(new Line2D.Double(1, 0, 5, -7));
        g2.draw(new Line2D.Double(1, 0, 5, 7));
        g2.draw(new Line2D.Double(7, -1, 11, -4));
        g2.draw(new Line2D.Double(7, 1, 11, 4));
        // брюшко, грудь, голова
        g2.fill(new Ellipse2D.Double(-11, -3.5, 9, 7));
        g2.fill(new Ellipse2D.Double(-3, -2.2, 6, 4.4));
        g2.fill(new Ellipse2D.Double(2.5, -2.8, 5.5, 5.6));
        g2.setTransform(saved);
    }

    private static Line2D line(Path path) {
        return new Line2D.Double(path.getFirst().getX(), path.getFirst().getY(),
                path.getSecond().getX(), path.getSecond().getY());
    }
}
