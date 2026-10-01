package lifesim.view;

import lifesim.controller.SimulationController;
import lifesim.model.Agent;
import lifesim.model.PopulationStats;
import lifesim.model.Position;
import lifesim.model.Simulation;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Содержимое главного окна: панель управления сверху, поле по центру,
 * строка состояния снизу (легенда с численностью видов и номер шага).
 */
public class MainPanel extends JPanel {
    private final SimulationController controller;
    private final Runnable openSettings;

    private final GridPanel gridPanel;
    private final JPanel toolbar = new JPanel();
    private final FlatButton runButton = new FlatButton("Старт", FlatButton.PLAY, true);
    private final JLabel speedValue = new JLabel();
    private final LegendItem plants = new LegendItem(Theme.Species.PLANT);
    private final LegendItem herbivores = new LegendItem(Theme.Species.HERBIVORE);
    private final LegendItem predators = new LegendItem(Theme.Species.PREDATOR);
    private final JLabel messageLabel = new JLabel();
    private final JLabel stepLabel = new JLabel();

    public MainPanel(SimulationController controller, Runnable openSettings) {
        super(new BorderLayout());
        this.controller = controller;
        this.openSettings = openSettings;
        this.gridPanel = new GridPanel(controller.getSimulation());
        setBackground(Theme.WINDOW);

        add(buildToolbar(), BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
        installShortcuts();

        gridPanel.setHoverListener(this::updateMessage);
        controller.addUpdateListener(this::refresh);
        refresh();
    }

    /** Дополнительный отступ сверху под «светофор» окна macOS (когда поле заходит под заголовок). */
    public void setTitleBarInset(int inset) {
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.HAIRLINE),
                BorderFactory.createEmptyBorder(10 + inset, 16, 10, 16)));
    }

    public GridPanel getGridPanel() { return gridPanel; }

    // ---------- панель управления ----------

    private JComponent buildToolbar() {
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setBackground(Theme.WINDOW);
        setTitleBarInset(0);

        FlatButton stepButton = new FlatButton("Шаг", FlatButton.STEP, false);
        FlatButton resetButton = new FlatButton("Сброс", FlatButton.RESET, false);
        FlatButton settingsButton = new FlatButton("Параметры", FlatButton.SETTINGS, false);

        runButton.reserveWidthFor("Старт", "Пауза");
        runButton.setToolTipText("Запустить или приостановить (пробел)");
        stepButton.setToolTipText("Сделать один шаг (\u2192)");
        resetButton.setToolTipText("Начать заново с теми же параметрами (" + Theme.shortcut("R") + ")");
        settingsButton.setToolTipText("Параметры модели (" + Theme.shortcut(",") + ")");

        runButton.addActionListener(e -> toggleRun());
        stepButton.addActionListener(e -> controller.step());
        resetButton.addActionListener(e -> controller.reset());
        settingsButton.addActionListener(e -> openSettings.run());

        JLabel speedTitle = new JLabel("Скорость");
        speedTitle.setForeground(Theme.TEXT_MUTED);
        speedTitle.setFont(Theme.regular(13));
        SpeedSlider speed = new SpeedSlider(1, 60, 10);
        speed.onChange(v -> {
            controller.setStepsPerSecond(v);
            speedValue.setText(speedText(v));
        });
        controller.setStepsPerSecond(speed.getValue());
        speedValue.setText(speedText(speed.getValue()));
        speedValue.setFont(Theme.regular(13));
        speedValue.setForeground(Theme.TEXT);
        fixWidth(speedValue, speedText(60) + "  ");

        for (JComponent c : new JComponent[]{runButton, stepButton, resetButton, settingsButton, speed}) {
            c.setFocusable(false);   // пробел и стрелки управляют симуляцией, а не кнопкой в фокусе
        }

        toolbar.add(runButton);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(stepButton);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(resetButton);
        toolbar.add(Box.createHorizontalStrut(32));
        toolbar.add(speedTitle);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(speed);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(speedValue);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(settingsButton);
        for (Component c : toolbar.getComponents()) {
            ((JComponent) c).setAlignmentY(Component.CENTER_ALIGNMENT);
        }
        return toolbar;
    }

    private void toggleRun() {
        if (controller.isRunning()) controller.pause();
        else controller.start();
    }

    private static String speedText(int v) {
        int m10 = v % 10;
        int m100 = v % 100;
        String word;
        if (m10 == 1 && m100 != 11) word = "шаг";
        else if (m10 >= 2 && m10 <= 4 && (m100 < 12 || m100 > 14)) word = "шага";
        else word = "шагов";
        return v + " " + word + "/с";
    }

    // ---------- строка состояния ----------

    private JComponent buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout(24, 0));
        bar.setBackground(Theme.WINDOW);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.HAIRLINE),
                BorderFactory.createEmptyBorder(9, 16, 10, 16)));

        JPanel legend = new JPanel();
        legend.setOpaque(false);
        legend.setLayout(new BoxLayout(legend, BoxLayout.X_AXIS));
        legend.add(plants);
        legend.add(Box.createHorizontalStrut(22));
        legend.add(herbivores);
        legend.add(Box.createHorizontalStrut(22));
        legend.add(predators);

        messageLabel.setFont(Theme.regular(12));
        messageLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        stepLabel.setFont(Theme.regular(12));
        stepLabel.setForeground(Theme.TEXT_MUTED);
        stepLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        fixWidth(stepLabel, "Шаг 0000000");

        bar.add(legend, BorderLayout.WEST);
        bar.add(messageLabel, BorderLayout.CENTER);
        bar.add(stepLabel, BorderLayout.EAST);
        return bar;
    }

    /** Фиксированная ширина, чтобы подписи не дёргались при смене чисел. */
    private static void fixWidth(JLabel label, String sample) {
        int w = label.getFontMetrics(label.getFont()).stringWidth(sample);
        Dimension d = new Dimension(w, label.getPreferredSize().height);
        label.setPreferredSize(d);
        label.setMinimumSize(d);
        label.setMaximumSize(d);
    }

    // ---------- клавиши ----------

    private void installShortcuts() {
        int menu = Theme.MAC ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;   // ⌘ или Ctrl
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "run", this::toggleRun);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "step", controller::step);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_R, menu), "reset", controller::reset);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, menu), "settings", openSettings);
    }

    private void bind(KeyStroke key, String name, Runnable action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(key, name);
        getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    // ---------- обновление ----------

    private void refresh() {
        Simulation sim = controller.getSimulation();
        gridPanel.setSimulation(sim);
        PopulationStats s = sim.getStats();
        plants.setCount(s.getPlants());
        herbivores.setCount(s.getHerbivores());
        predators.setCount(s.getPredators());
        stepLabel.setText("Шаг " + s.getStep());

        boolean running = controller.isRunning();
        runButton.setText(running ? "Пауза" : "Старт");
        runButton.setGlyph(running ? FlatButton.PAUSE : FlatButton.PLAY);
        updateMessage();
    }

    /** Справа в строке состояния: сведения о клетке под курсором или сообщение о вымирании. */
    private void updateMessage() {
        Simulation sim = controller.getSimulation();
        Position cell = gridPanel.getHoveredCell();
        if (cell != null) {
            Agent a = sim.getEnvironment().getAgent(cell.getX(), cell.getY());
            messageLabel.setForeground(Theme.TEXT_MUTED);
            messageLabel.setText(a == null
                    ? "Клетка (" + cell.getX() + ", " + cell.getY() + ") пуста"
                    : a.getSpeciesName() + " (" + cell.getX() + ", " + cell.getY() + "): энергия "
                      + a.getEnergy() + ", возраст " + a.getAge());
        } else if (sim.isAnySpeciesExtinct()) {
            messageLabel.setForeground(Theme.DANGER);
            messageLabel.setText(extinctText(sim.getStats()) + ". Нажмите «Сброс», чтобы начать заново");
        } else {
            messageLabel.setText(" ");
        }
    }

    private static String extinctText(PopulationStats s) {
        List<String> gone = new ArrayList<>();
        if (s.getPlants() == 0) gone.add("растения");
        if (s.getHerbivores() == 0) gone.add("травоядные");
        if (s.getPredators() == 0) gone.add("хищники");
        String list = String.join(" и ", gone);
        return Character.toUpperCase(list.charAt(0)) + list.substring(1) + " вымерли";
    }

    // ---------- легенда ----------

    /** Значок вида, название и численность. Рисуется целиком, чтобы число не «прыгало». */
    private static final class LegendItem extends JComponent {
        private static final int ICON = 10;
        private final Theme.Species species;
        private final Font nameFont = Theme.regular(12);
        private final Font countFont = Theme.medium(12);
        private String count = "0";

        LegendItem(Theme.Species species) {
            this.species = species;
            FontMetrics name = getFontMetrics(nameFont);
            FontMetrics num = getFontMetrics(countFont);
            Dimension d = new Dimension(ICON + 7 + name.stringWidth(species.title) + 7 + num.stringWidth("00000"),
                    Math.max(ICON, name.getHeight()));
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        void setCount(int value) {
            String text = String.valueOf(value);
            if (text.equals(count)) return;
            count = text;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.antialias(g2);
            float cy = getHeight() / 2f;
            species.paint(g2, 0, cy - ICON / 2f, ICON);

            g2.setFont(nameFont);
            FontMetrics fm = g2.getFontMetrics();
            float baseline = cy + (fm.getAscent() - fm.getDescent()) / 2f;
            float x = ICON + 7;
            g2.setColor(Theme.TEXT_MUTED);
            g2.drawString(species.title, x, baseline);
            x += (float) fm.getStringBounds(species.title, g2).getWidth() + 7;

            g2.setFont(countFont);
            g2.setColor(Theme.TEXT);
            g2.drawString(count, x, baseline);
            g2.dispose();
        }
    }
}
