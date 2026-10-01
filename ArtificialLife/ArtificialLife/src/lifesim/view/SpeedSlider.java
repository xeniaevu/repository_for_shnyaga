package lifesim.view;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.IntConsumer;

/** Ползунок скорости, нарисованный вручную (одинаково на macOS и Windows). */
final class SpeedSlider extends JComponent {
    private static final int THUMB = 14;

    private final int min;
    private final int max;
    private int value;
    private boolean active;
    private IntConsumer listener = v -> { };

    SpeedSlider(int min, int max, int value) {
        this.min = min;
        this.max = max;
        this.value = value;
        Dimension d = new Dimension(150, 24);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { active = true; setFromX(e.getX()); }
            @Override public void mouseDragged(MouseEvent e) { setFromX(e.getX()); }
            @Override public void mouseReleased(MouseEvent e) { active = false; repaint(); }
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                setValue(SpeedSlider.this.value - e.getWheelRotation());
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseWheelListener(mouse);
    }

    void onChange(IntConsumer listener) { this.listener = listener; }

    int getValue() { return value; }

    void setValue(int v) {
        v = Math.max(min, Math.min(max, v));
        if (v == value) return;
        value = v;
        listener.accept(v);
        repaint();
    }

    private void setFromX(int x) {
        float left = THUMB / 2f;
        float width = getWidth() - THUMB;
        double t = (x - left) / width;
        setValue((int) Math.round(min + t * (max - min)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.antialias(g2);
        float left = THUMB / 2f;
        float width = getWidth() - THUMB;
        float cy = getHeight() / 2f;
        float t = (float) (value - min) / (max - min);
        float tx = left + t * width;

        g2.setColor(Theme.CONTROL_HOVER);
        g2.fill(new RoundRectangle2D.Float(left, cy - 2, width, 4, 4, 4));
        g2.setColor(Theme.ACCENT);
        g2.fill(new RoundRectangle2D.Float(left, cy - 2, tx - left, 4, 4, 4));

        float r = active ? THUMB / 2f + 1 : THUMB / 2f;
        Ellipse2D.Float thumb = new Ellipse2D.Float(tx - r, cy - r, 2 * r, 2 * r);
        g2.setColor(Theme.TEXT);
        g2.fill(thumb);
        g2.setColor(Theme.WINDOW);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(thumb);
        g2.dispose();
    }
}
