package lifesim.view;

import javax.swing.ButtonModel;
import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Кнопка, которая рисуется полностью вручную, поэтому одинаково выглядит
 * на macOS и Windows (системный вид macOS не даёт менять цвет кнопок).
 */
final class FlatButton extends JButton {

    /** Маленький значок слева от текста, рисуется в квадрате size×size. */
    interface Glyph {
        void paint(Graphics2D g2, float x, float y, float size);
    }

    private static final int HEIGHT = 30;
    private static final int PAD_X = 14;
    private static final int GLYPH = 12;
    private static final int GLYPH_GAP = 7;

    private final boolean primary;
    private Glyph glyph;

    FlatButton(String text, Glyph glyph, boolean primary) {
        super(text);
        this.glyph = glyph;
        this.primary = primary;
        setFont(primary ? Theme.medium(13) : Theme.regular(13));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
    }

    void setGlyph(Glyph glyph) {
        if (this.glyph == glyph) return;
        this.glyph = glyph;
        revalidate();
        repaint();
    }

    /** Ширина по самому длинному из вариантов текста, чтобы кнопка не «прыгала». */
    void reserveWidthFor(String... texts) {
        int w = 0;
        for (String t : texts) w = Math.max(w, contentWidth(t));
        Dimension d = new Dimension(w + 2 * PAD_X, HEIGHT);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
    }

    private int contentWidth(String text) {
        FontMetrics fm = getFontMetrics(getFont());
        return fm.stringWidth(text) + (glyph != null ? GLYPH + GLYPH_GAP : 0);
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) return super.getPreferredSize();
        return new Dimension(contentWidth(getText()) + 2 * PAD_X, HEIGHT);
    }

    @Override
    public Dimension getMaximumSize() { return getPreferredSize(); }

    @Override
    public Dimension getMinimumSize() { return getPreferredSize(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.antialias(g2);
        ButtonModel m = getModel();
        boolean enabled = isEnabled();
        int w = getWidth();
        int h = getHeight();

        Color bg;
        Color fg;
        if (primary) {
            bg = !enabled ? Theme.CONTROL : m.isPressed() ? Theme.ACCENT_PRESSED
                    : m.isRollover() ? Theme.ACCENT_HOVER : Theme.ACCENT;
            fg = enabled ? Theme.ACCENT_TEXT : Theme.TEXT_DISABLED;
        } else {
            bg = !enabled ? Theme.CONTROL : m.isPressed() ? Theme.CONTROL_PRESSED
                    : m.isRollover() ? Theme.CONTROL_HOVER : Theme.CONTROL;
            fg = enabled ? Theme.TEXT : Theme.TEXT_DISABLED;
        }

        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, 12, 12);
        g2.setColor(bg);
        g2.fill(shape);
        if (!primary) {
            g2.setColor(Theme.CONTROL_BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(shape);
        }
        if (isFocusOwner()) {
            g2.setColor(Theme.alpha(Theme.ACCENT, 200));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Float(1f, 1f, w - 2f, h - 2f, 11, 11));
        }

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        String text = getText();
        int content = contentWidth(text);
        float x = (w - content) / 2f;
        float cy = h / 2f;
        g2.setColor(fg);
        if (glyph != null) {
            glyph.paint(g2, x, cy - GLYPH / 2f, GLYPH);
            x += GLYPH + GLYPH_GAP;
        }
        g2.setColor(fg);
        g2.drawString(text, x, cy + (fm.getAscent() - fm.getDescent()) / 2f);
        g2.dispose();
    }

    // ---------- значки ----------

    static final Glyph PLAY = (g2, x, y, s) -> {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(x + s * 0.12f, y);
        p.lineTo(x + s, y + s / 2f);
        p.lineTo(x + s * 0.12f, y + s);
        p.closePath();
        g2.fill(p);
    };

    static final Glyph PAUSE = (g2, x, y, s) -> {
        float bar = s * 0.3f;
        g2.fill(new RoundRectangle2D.Float(x + s * 0.08f, y, bar, s, 2, 2));
        g2.fill(new RoundRectangle2D.Float(x + s * 0.92f - bar, y, bar, s, 2, 2));
    };

    static final Glyph STEP = (g2, x, y, s) -> {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(x, y + s * 0.05f);
        p.lineTo(x + s * 0.7f, y + s / 2f);
        p.lineTo(x, y + s * 0.95f);
        p.closePath();
        g2.fill(p);
        g2.fill(new Rectangle2D.Float(x + s * 0.76f, y + s * 0.05f, s * 0.2f, s * 0.9f));
    };

    static final Glyph RESET = (g2, x, y, s) -> {
        g2.setStroke(new BasicStroke(s * 0.15f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float pad = s * 0.1f;
        g2.draw(new Arc2D.Float(x + pad, y + pad, s - 2 * pad, s - 2 * pad, 100, 280, Arc2D.OPEN));
        // стрелка на конце дуги
        float ax = x + s / 2f - s * 0.02f;
        float ay = y + pad;
        Path2D.Float head = new Path2D.Float();
        head.moveTo(ax - s * 0.02f, ay - s * 0.24f);
        head.lineTo(ax + s * 0.3f, ay);
        head.lineTo(ax - s * 0.02f, ay + s * 0.24f);
        head.closePath();
        g2.fill(head);
    };

    static final Glyph SETTINGS = (g2, x, y, s) -> {
        g2.setStroke(new BasicStroke(s * 0.12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float[] rows = {0.2f, 0.5f, 0.8f};
        float[] knobs = {0.68f, 0.3f, 0.58f};
        for (int i = 0; i < 3; i++) {
            float ly = y + s * rows[i];
            g2.draw(new Line2D.Float(x, ly, x + s, ly));
            float kx = x + s * knobs[i];
            g2.fill(new RoundRectangle2D.Float(kx - s * 0.13f, ly - s * 0.17f, s * 0.26f, s * 0.34f, 3, 3));
        }
    };
}
