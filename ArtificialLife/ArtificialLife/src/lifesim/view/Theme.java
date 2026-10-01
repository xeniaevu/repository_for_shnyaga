package lifesim.view;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Оформление оконной версии: цвета, шрифты, значки видов.
 * Палитра «вид на экосистему сверху»: почва, мох, светлая шерсть травоядных, рыжий хищник.
 */
final class Theme {

    // ---------- цвета интерфейса ----------
    static final Color WINDOW = new Color(0x1F1D19);
    static final Color HAIRLINE = new Color(0x34302A);
    static final Color CONTROL = new Color(0x302D27);
    static final Color CONTROL_HOVER = new Color(0x3A362F);
    static final Color CONTROL_PRESSED = new Color(0x272520);
    static final Color CONTROL_BORDER = new Color(0x423D35);
    static final Color TEXT = new Color(0xECE5D5);
    static final Color TEXT_MUTED = new Color(0x9A917E);
    static final Color TEXT_DISABLED = new Color(0x625C50);

    // ---------- цвета поля ----------
    static final Color SOIL = new Color(0x2E2921);
    static final Color SOIL_LINE = new Color(0x26221C);
    static final Color FIELD_BORDER = new Color(0x3D382F);

    // ---------- цвета видов ----------
    static final Color PLANT = new Color(0x9BCB50);
    static final Color PLANT_YOUNG = new Color(0x4C6630);
    static final Color HERBIVORE = new Color(0xF0E2BF);
    static final Color PREDATOR = new Color(0xE2622F);

    /** Основная кнопка («Старт») — цвет растущей жизни. */
    static final Color ACCENT = PLANT;
    static final Color ACCENT_HOVER = new Color(0xAAD660);
    static final Color ACCENT_PRESSED = new Color(0x86B540);
    static final Color ACCENT_TEXT = new Color(0x1C260F);
    static final Color DANGER = PREDATOR;

    static final boolean MAC = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac");

    // ---------- шрифты ----------
    private static final String FAMILY = pickFamily(
            "SF Pro Text", "SF Pro", "Helvetica Neue", "Segoe UI", "Inter", "Noto Sans", "DejaVu Sans");
    private static final Font REGULAR = new Font(FAMILY, Font.PLAIN, 13);
    private static final Font MEDIUM = pickMedium();

    private Theme() { }

    static Font regular(float size) { return REGULAR.deriveFont(size); }
    static Font medium(float size) { return MEDIUM.deriveFont(size); }

    private static String pickFamily(String... candidates) {
        Set<String> installed = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String name : candidates) {
            if (installed.contains(name)) return name;
        }
        return Font.SANS_SERIF;
    }

    /** Полужирное начертание: «Medium»/«Semibold», если есть, иначе обычный Bold. */
    private static Font pickMedium() {
        for (String suffix : new String[]{" Medium", " Semibold"}) {
            Font f = new Font(FAMILY + suffix, Font.PLAIN, 13);
            if (!Font.DIALOG.equals(f.getFamily())) return f;
        }
        return new Font(FAMILY, Font.BOLD, 13);
    }

    /** Сочетание клавиш для подсказок: ⌘ на macOS, Ctrl на остальных системах. */
    static String shortcut(String key) {
        return MAC && REGULAR.canDisplay('\u2318') ? "\u2318" + key : "Ctrl+" + key;
    }

    static Color mix(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    static Color alpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    static void antialias(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    /**
     * Базовый Look&Feel — кроссплатформенный (Metal), поверх которого всё рисуется вручную.
     * Системный Aqua на macOS игнорирует цвета кнопок и полей, поэтому здесь он не используется.
     */
    static void installLookAndFeel() {
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception ignored) {
            // останется стандартный вид — всё равно будет работать
        }
        UIManager.put("Panel.background", new ColorUIResource(WINDOW));
        UIManager.put("Label.foreground", new ColorUIResource(TEXT));
        UIManager.put("Label.font", new FontUIResource(regular(13)));
        UIManager.put("ToolTip.background", new ColorUIResource(new Color(0x3A362F)));
        UIManager.put("ToolTip.foreground", new ColorUIResource(TEXT));
        UIManager.put("ToolTip.font", new FontUIResource(regular(12)));
        UIManager.put("ToolTip.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CONTROL_BORDER), BorderFactory.createEmptyBorder(3, 7, 3, 7)));
    }

    // ---------- виды агентов: цвет и форма ----------

    /** Вид агента в легенде: у каждого своя форма, чтобы различать не только по цвету. */
    enum Species {
        PLANT("Растения", Theme.PLANT),
        HERBIVORE("Травоядные", Theme.HERBIVORE),
        PREDATOR("Хищники", Theme.PREDATOR);

        final String title;
        final Color color;

        Species(String title, Color color) {
            this.title = title;
            this.color = color;
        }

        /** Нарисовать форму вида в квадрате size×size с левым верхним углом (x, y). */
        void paint(Graphics2D g2, float x, float y, float size) {
            g2.setColor(color);
            switch (this) {
                case PLANT:
                    float arc = size * 0.42f;
                    g2.fill(new RoundRectangle2D.Float(x, y, size, size, arc, arc));
                    break;
                case HERBIVORE:
                    g2.fill(new Ellipse2D.Float(x, y, size, size));
                    break;
                default:
                    g2.fill(diamond(x + size / 2f, y + size / 2f, size / 2f));
            }
        }

        Icon icon(int size) {
            return new Icon() {
                public int getIconWidth() { return size; }
                public int getIconHeight() { return size; }
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    antialias(g2);
                    Species.this.paint(g2, x, y, size);
                    g2.dispose();
                }
            };
        }
    }

    static Path2D.Float diamond(float cx, float cy, float r) {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(cx, cy - r);
        p.lineTo(cx + r, cy);
        p.lineTo(cx, cy + r);
        p.lineTo(cx - r, cy);
        p.closePath();
        return p;
    }

    /** Значок приложения (Dock на macOS, панель задач на Windows): кусочек поля 4×4. */
    static BufferedImage appIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        antialias(g2);
        float m = size * 0.1f;
        float s = size - 2 * m;
        g2.setColor(SOIL);
        g2.fill(new RoundRectangle2D.Float(m, m, s, s, s * 0.45f, s * 0.45f));
        g2.setColor(FIELD_BORDER);
        g2.setStroke(new BasicStroke(size / 128f));
        g2.draw(new RoundRectangle2D.Float(m, m, s, s, s * 0.45f, s * 0.45f));

        // 0 — пусто, 1..3 — растения разной энергии, 4 — травоядное, 5 — хищник
        int[][] map = {
                {3, 2, 0, 0},
                {2, 4, 0, 5},
                {1, 3, 0, 0},
                {0, 2, 3, 1},
        };
        float pad = s * 0.14f;
        float cell = (s - 2 * pad) / 4f;
        float gap = cell * 0.12f;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                int v = map[r][c];
                if (v == 0) continue;
                float x = m + pad + c * cell + gap / 2;
                float y = m + pad + r * cell + gap / 2;
                float k = cell - gap;
                if (v <= 3) {
                    g2.setColor(mix(PLANT_YOUNG, PLANT, v / 3.0));
                    g2.fill(new RoundRectangle2D.Float(x, y, k, k, k * 0.42f, k * 0.42f));
                } else if (v == 4) {
                    Species.HERBIVORE.paint(g2, x + k * 0.1f, y + k * 0.1f, k * 0.8f);
                } else {
                    Species.PREDATOR.paint(g2, x, y, k);
                }
            }
        }
        g2.dispose();
        return img;
    }
}
