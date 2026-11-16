package antcolony.ui;

import antcolony.algorithm.AntColony;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.Locale;

/** График сходимости: длина лучшего и среднего маршрута в каждом цикле. */
public class ChartPanel extends JPanel {

    private static final Color BEST = new Color(40, 100, 200);
    private static final Color AVERAGE = new Color(160, 160, 160);
    private static final Color EXACT = new Color(140, 70, 200);

    private AntColony colony;
    private double exactLength = Double.NaN;

    public ChartPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 170));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 210, 210)));
    }

    public void setColony(AntColony colony) {
        this.colony = colony;
        repaint();
    }

    /** Длина точного кратчайшего маршрута (NaN — неизвестна). */
    public void setExactLength(double exactLength) {
        this.exactLength = exactLength;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));

        int left = 64;
        int right = getWidth() - 16;
        int top = 30;
        int bottom = getHeight() - 24;

        g2.setColor(new Color(60, 60, 60));
        g2.drawString("Длина маршрута по циклам", left, 18);
        drawLegend(g2, right);

        g2.setColor(new Color(225, 225, 225));
        g2.drawLine(left, bottom, right, bottom);
        g2.drawLine(left, top, left, bottom);

        if (colony == null || colony.getBestHistory().isEmpty()) {
            g2.dispose();
            return;
        }
        List<Double> best = colony.getBestHistory();
        List<Double> average = colony.getAverageHistory();

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < best.size(); i++) {
            min = Math.min(min, best.get(i));
            max = Math.max(max, average.get(i));
        }
        if (!Double.isNaN(exactLength)) {
            min = Math.min(min, exactLength);
        }
        double range = Math.max(max - min, 1e-9);
        min -= range * 0.08;
        max += range * 0.08;

        g2.setColor(new Color(110, 110, 110));
        g2.drawString(String.format(Locale.US, "%.0f", max), 8, top + 5);
        g2.drawString(String.format(Locale.US, "%.0f", min), 8, bottom);
        String count = String.valueOf(best.size());
        g2.drawString(count, right - g2.getFontMetrics().stringWidth(count), bottom + 16);
        g2.drawString("1", left, bottom + 16);

        g2.setStroke(new BasicStroke(1.4f));
        g2.setColor(AVERAGE);
        g2.draw(curve(average, min, max, left, right, top, bottom));
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(BEST);
        g2.draw(curve(best, min, max, left, right, top, bottom));
        // рисуем последним, поверх остальных линий: когда муравьи нашли оптимум,
        // синяя линия совпадает с этой, и пунктир должен остаться виден
        if (!Double.isNaN(exactLength)) {
            int y = toY(exactLength, min, max, top, bottom);
            g2.setColor(EXACT);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{7f, 7f}, 0f));
            g2.drawLine(left, y, right, y);
        }
        g2.dispose();
    }

    private void drawLegend(Graphics2D g2, int right) {
        String[] labels = {"лучший в цикле", "средний в цикле", "точный оптимум"};
        Color[] colors = {BEST, AVERAGE, EXACT};
        int x = right;
        // «точный оптимум» показываем в легенде, только когда он посчитан
        int last = Double.isNaN(exactLength) ? labels.length - 2 : labels.length - 1;
        for (int i = last; i >= 0; i--) {
            x -= g2.getFontMetrics().stringWidth(labels[i]);
            g2.setColor(new Color(60, 60, 60));
            g2.drawString(labels[i], x, 18);
            x -= 22;
            g2.setColor(colors[i]);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawLine(x, 14, x + 16, 14);
            x -= 16;
        }
    }

    private static int toY(double value, double min, double max, int top, int bottom) {
        return (int) Math.round(bottom - (value - min) / (max - min) * (bottom - top));
    }

    private static Path2D curve(List<Double> values, double min, double max,
                                int left, int right, int top, int bottom) {
        Path2D path = new Path2D.Double();
        int n = values.size();
        for (int i = 0; i < n; i++) {
            double x = n == 1 ? left : left + (double) i / (n - 1) * (right - left);
            double y = bottom - (values.get(i) - min) / (max - min) * (bottom - top);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        return path;
    }
}
