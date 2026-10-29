package wumpus.gui;

import wumpus.Game;
import wumpus.agent.KnowledgeBase;
import wumpus.world.Direction;
import wumpus.world.Grid;
import wumpus.world.Position;
import wumpus.world.World;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Рисует пещеру. Фон клетки показывает знания агента:
 * светлая — посещена, зелёная — безопасна (OK), красноватая — чем краснее, тем опаснее.
 * Буквы как в книге: B — ветерок, S — запах, G — блеск, OK / P! / W! — выводы агента.
 */
public class BoardPanel extends JPanel {

    private static final Color BG = new Color(0x1c1d21);
    private static final Color UNKNOWN = new Color(0x31333a);
    private static final Color VISITED = new Color(0x5a5e68);
    private static final Color SAFE = new Color(0x2e5040);
    private static final Color DANGER = new Color(0x9a2f2f);
    private static final Color GRID_LINE = new Color(0x141518);
    private static final Color TEXT_DIM = new Color(0xa6abb5);
    private static final Color TEXT = new Color(0xe9ebef);
    private static final Color OK_GREEN = new Color(0x7fdc8f);
    private static final Color SURE_RED = new Color(0xff6f6f);
    private static final Color MAYBE = new Color(0xffb36b);
    private static final Color BREEZE = new Color(0x86ccff);
    private static final Color STENCH = new Color(0xd9a35a);
    private static final Color GOLD = new Color(0xf3c24f);
    private static final Color GOLD_DARK = new Color(0xa57a14);
    private static final Color AGENT = new Color(0x5fb0ff);
    private static final Color WUMPUS = new Color(0x6f9a3e);

    private Game game;
    private boolean showTruth = false;
    private boolean showProbabilities = true;

    public BoardPanel() {
        setPreferredSize(new Dimension(640, 640));
        setBackground(BG);
    }

    public void setGame(Game game) {
        this.game = game;
        repaint();
    }

    public void setShowTruth(boolean v) {
        showTruth = v;
        repaint();
    }

    public void setShowProbabilities(boolean v) {
        showProbabilities = v;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(BG);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (game == null) {
            g.dispose();
            return;
        }

        World world = game.world();
        Grid grid = world.grid();
        int ml = 34, mr = 14, mt = 14, mb = 30;
        int cell = Math.min((getWidth() - ml - mr) / grid.width, (getHeight() - mt - mb) / grid.height);
        cell = Math.max(cell, 24);
        int gw = cell * grid.width, gh = cell * grid.height;
        int ox = ml + Math.max(0, (getWidth() - ml - mr - gw) / 2);
        int oy = mt + Math.max(0, (getHeight() - mt - mb - gh) / 2);
        boolean reveal = showTruth || world.isOver();

        for (Position p : grid.cells()) {
            int rx = ox + (p.x() - 1) * cell;
            int ry = oy + (grid.height - p.y()) * cell;
            drawCell(g, world, p, rx, ry, cell, reveal);
        }

        // Номера столбцов и строк, как на рисунках в книге
        g.setColor(TEXT_DIM);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        FontMetrics fm = g.getFontMetrics();
        for (int x = 1; x <= grid.width; x++) {
            String s = String.valueOf(x);
            g.drawString(s, ox + (x - 1) * cell + (cell - fm.stringWidth(s)) / 2, oy + gh + 20);
        }
        for (int y = 1; y <= grid.height; y++) {
            String s = String.valueOf(y);
            g.drawString(s, ox - 12 - fm.stringWidth(s), oy + (grid.height - y) * cell + cell / 2 + 5);
        }
        g.dispose();
    }

    private void drawCell(Graphics2D g, World world, Position p, int rx, int ry, int s, boolean reveal) {
        KnowledgeBase kb = game.agent().knowledge();
        boolean visited = kb.isVisited(p);
        double danger = kb.danger(p);
        // Граница — неизвестные клетки рядом с посещёнными. Про дальние клетки агент знает
        // только априорные вероятности, поэтому их не подсвечиваем и не подписываем.
        boolean frontier = !visited && world.grid().neighbors(p).stream().anyMatch(kb::isVisited);
        boolean certain = kb.pitProbability(p) == 1.0 || (kb.isWumpusAlive() && kb.wumpusProbability(p) == 1.0);

        Color bg = visited ? VISITED
                : danger == 0.0 ? SAFE
                : (frontier || certain) ? blend(UNKNOWN, DANGER, Math.min(1.0, danger) * 0.85)
                : UNKNOWN;
        g.setColor(bg);
        g.fillRect(rx, ry, s, s);
        g.setColor(GRID_LINE);
        g.setStroke(new BasicStroke(2f));
        g.drawRect(rx, ry, s, s);

        int fs = Math.max(9, s / 9);
        Font small = new Font(Font.SANS_SERIF, Font.PLAIN, fs);
        Font bold = new Font(Font.SANS_SERIF, Font.BOLD, Math.max(10, (int) (fs * 1.25)));

        // Координаты
        g.setFont(small);
        g.setColor(TEXT_DIM);
        g.drawString(p.x() + "," + p.y(), rx + 4, ry + fs + 2);

        // Восприятия, полученные в этой клетке
        if (visited) {
            g.setFont(bold);
            FontMetrics fm = g.getFontMetrics();
            int tx = rx + s - 5;
            String[] letters = {"G", "S", "B"};
            boolean[] on = {kb.hadGlitter(p), kb.hadStench(p), kb.hadBreeze(p)};
            Color[] colors = {GOLD, STENCH, BREEZE};
            for (int i = 0; i < 3; i++) {
                if (!on[i]) continue;
                tx -= fm.stringWidth(letters[i]);
                g.setColor(colors[i]);
                g.drawString(letters[i], tx, ry + fm.getAscent() + 1);
                tx -= 4;
            }
        }

        // Настоящее содержимое пещеры
        int cy = ry + s / 2 + s / 30;
        double r = s * 0.2;
        boolean agentHere = world.agentPosition().equals(p);
        boolean pit = reveal && world.hasPit(p);
        boolean wumpus = reveal && world.wumpus().equals(p);
        boolean gold = reveal && p.equals(world.gold());
        boolean anyTruth = pit || wumpus || gold;
        int itemsX = rx + (agentHere && anyTruth ? (int) (s * 0.66) : s / 2);
        int agentX = rx + (agentHere && anyTruth ? (int) (s * 0.32) : s / 2);

        if (pit) drawPit(g, itemsX, cy, r);
        if (wumpus) drawWumpus(g, itemsX, cy, r * 0.9, world.isWumpusAlive());
        if (gold) drawGold(g, itemsX, cy + (wumpus || pit ? r * 0.55 : 0), r * (wumpus || pit ? 0.6 : 0.85));
        if (agentHere) drawAgent(g, agentX, cy, r * 0.8, world.agentDirection(), world.isAgentDead(), world.agentHasGold());

        // Выводы агента
        int by = ry + s - 6;
        g.setFont(bold);
        if (visited) {
            drawCentered(g, "V  OK", rx, s, by, TEXT);
        } else if (kb.pitProbability(p) == 1.0) {
            drawCentered(g, "P!", rx, s, by, SURE_RED);
        } else if (kb.isWumpusAlive() && kb.wumpusProbability(p) == 1.0) {
            drawCentered(g, "W!", rx, s, by, SURE_RED);
        } else if (danger == 0.0) {
            drawCentered(g, "OK", rx, s, by, OK_GREEN);
        } else if (showProbabilities && frontier && s >= 56) {
            g.setFont(small);
            double pp = kb.pitProbability(p);
            double wp = kb.isWumpusAlive() ? kb.wumpusProbability(p) : 0;
            String line1 = pp > 0 ? "P? " + pct(pp) : null;
            String line2 = wp > 0 ? "W? " + pct(wp) : null;
            if (line1 != null && line2 != null) {
                drawCentered(g, line1, rx, s, by - fs - 1, MAYBE);
                drawCentered(g, line2, rx, s, by, MAYBE);
            } else if (line1 != null) {
                drawCentered(g, line1, rx, s, by, MAYBE);
            } else if (line2 != null) {
                drawCentered(g, line2, rx, s, by, MAYBE);
            }
        }
    }

    private static void drawCentered(Graphics2D g, String text, int rx, int s, int baseline, Color c) {
        FontMetrics fm = g.getFontMetrics();
        g.setColor(c);
        g.drawString(text, rx + (s - fm.stringWidth(text)) / 2, baseline);
    }

    private static void drawPit(Graphics2D g, double cx, double cy, double r) {
        g.setColor(new Color(0x050506));
        g.fill(new Ellipse2D.Double(cx - r * 1.25, cy - r * 0.75, r * 2.5, r * 1.5));
        g.setColor(new Color(0x6b6f78));
        g.setStroke(new BasicStroke((float) Math.max(1.5, r / 8)));
        g.draw(new Ellipse2D.Double(cx - r * 1.25, cy - r * 0.75, r * 2.5, r * 1.5));
    }

    private static void drawWumpus(Graphics2D g, double cx, double cy, double r, boolean alive) {
        Color body = alive ? WUMPUS : new Color(0x7a7d84);
        // Тело с «мехом» по краю
        Path2D fur = new Path2D.Double();
        int spikes = 14;
        for (int i = 0; i <= spikes * 2; i++) {
            double a = Math.PI * i / spikes;
            double rr = (i % 2 == 0) ? r * 1.2 : r * 1.02;
            double x = cx + Math.cos(a) * rr;
            double y = cy + Math.sin(a) * rr * 0.95;
            if (i == 0) fur.moveTo(x, y);
            else fur.lineTo(x, y);
        }
        fur.closePath();
        g.setColor(body.darker());
        g.fill(fur);
        g.setColor(body);
        g.fill(new Ellipse2D.Double(cx - r, cy - r * 0.92, r * 2, r * 1.84));

        double ex = r * 0.38, ey = cy - r * 0.2, er = r * 0.26;
        if (alive) {
            g.setColor(Color.WHITE);
            g.fill(new Ellipse2D.Double(cx - ex - er, ey - er, er * 2, er * 2));
            g.fill(new Ellipse2D.Double(cx + ex - er, ey - er, er * 2, er * 2));
            g.setColor(Color.BLACK);
            double pr = er * 0.5;
            g.fill(new Ellipse2D.Double(cx - ex - pr, ey - pr + er * 0.2, pr * 2, pr * 2));
            g.fill(new Ellipse2D.Double(cx + ex - pr, ey - pr + er * 0.2, pr * 2, pr * 2));
            // Рот с клыками
            double my = cy + r * 0.38;
            g.setColor(new Color(0x2a1010));
            g.fill(new RoundRectangle2D.Double(cx - r * 0.5, my - r * 0.12, r, r * 0.3, r * 0.2, r * 0.2));
            g.setColor(Color.WHITE);
            Polygon t1 = new Polygon(new int[]{(int) (cx - r * 0.35), (int) (cx - r * 0.2), (int) (cx - r * 0.27)},
                    new int[]{(int) (my - r * 0.12), (int) (my - r * 0.12), (int) (my + r * 0.1)}, 3);
            Polygon t2 = new Polygon(new int[]{(int) (cx + r * 0.2), (int) (cx + r * 0.35), (int) (cx + r * 0.27)},
                    new int[]{(int) (my - r * 0.12), (int) (my - r * 0.12), (int) (my + r * 0.1)}, 3);
            g.fill(t1);
            g.fill(t2);
        } else {
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke((float) Math.max(1.5, r / 9)));
            for (double sx : new double[]{-ex, ex}) {
                g.draw(new Line2D.Double(cx + sx - er * 0.7, ey - er * 0.7, cx + sx + er * 0.7, ey + er * 0.7));
                g.draw(new Line2D.Double(cx + sx - er * 0.7, ey + er * 0.7, cx + sx + er * 0.7, ey - er * 0.7));
            }
            g.draw(new Line2D.Double(cx - r * 0.35, cy + r * 0.45, cx + r * 0.35, cy + r * 0.45));
        }
    }

    private static void drawGold(Graphics2D g, double cx, double cy, double r) {
        Path2D bar = new Path2D.Double();
        bar.moveTo(cx - r * 0.75, cy - r * 0.35);
        bar.lineTo(cx + r * 0.75, cy - r * 0.35);
        bar.lineTo(cx + r * 1.1, cy + r * 0.4);
        bar.lineTo(cx - r * 1.1, cy + r * 0.4);
        bar.closePath();
        g.setColor(GOLD);
        g.fill(bar);
        g.setColor(GOLD_DARK);
        g.setStroke(new BasicStroke((float) Math.max(1.2, r / 10)));
        g.draw(bar);
        g.setColor(new Color(255, 250, 220, 200));
        g.draw(new Line2D.Double(cx - r * 0.5, cy - r * 0.15, cx + r * 0.2, cy - r * 0.15));
    }

    private static void drawAgent(Graphics2D g, double cx, double cy, double r, Direction d,
                                  boolean dead, boolean hasGold) {
        g.setColor(dead ? new Color(0x8a8f99) : AGENT);
        g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        g.setColor(new Color(0x0d2a4a));
        g.setStroke(new BasicStroke((float) Math.max(1.5, r / 8)));
        g.draw(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));

        // Стрелка направления взгляда (на экране ось y направлена вниз)
        double dx = d.dx, dy = -d.dy;
        double tipX = cx + dx * r * 0.8, tipY = cy + dy * r * 0.8;
        double baseX = cx - dx * r * 0.25, baseY = cy - dy * r * 0.25;
        double px = -dy * r * 0.45, py = dx * r * 0.45;
        Path2D tri = new Path2D.Double();
        tri.moveTo(tipX, tipY);
        tri.lineTo(baseX + px, baseY + py);
        tri.lineTo(baseX - px, baseY - py);
        tri.closePath();
        g.setColor(Color.WHITE);
        g.fill(tri);

        if (hasGold) {
            double gr = r * 0.38;
            g.setColor(GOLD);
            g.fill(new Ellipse2D.Double(cx + r * 0.55, cy + r * 0.45, gr * 2, gr * 2));
            g.setColor(GOLD_DARK);
            g.draw(new Ellipse2D.Double(cx + r * 0.55, cy + r * 0.45, gr * 2, gr * 2));
        }
        if (dead) {
            g.setColor(SURE_RED);
            g.setStroke(new BasicStroke((float) Math.max(2.5, r / 4)));
            g.draw(new Line2D.Double(cx - r, cy - r, cx + r, cy + r));
            g.draw(new Line2D.Double(cx - r, cy + r, cx + r, cy - r));
        }
    }

    private static Color blend(Color a, Color b, double t) {
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    private static String pct(double v) {
        long p = Math.round(v * 100);
        if (p == 0 && v > 0) return "<1%";
        return p + "%";
    }
}
