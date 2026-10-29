package wumpus.gui;

import wumpus.Game;
import wumpus.agent.Agent;
import wumpus.world.World;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Random;

/** Главное окно: настройки сверху, поле в центре, состояние и журнал рассуждений справа. */
public class WumpusFrame extends JFrame {

    private final JSpinner widthSpin = new JSpinner(new SpinnerNumberModel(4, 2, 15, 1));
    private final JSpinner heightSpin = new JSpinner(new SpinnerNumberModel(4, 2, 15, 1));
    private final JSpinner pitSpin = new JSpinner(new SpinnerNumberModel(0.20, 0.0, 0.6, 0.05));
    private final JSpinner riskSpin = new JSpinner(new SpinnerNumberModel(0.30, 0.0, 1.0, 0.05));
    private final JTextField seedField = new JTextField(7);

    private final BoardPanel board = new BoardPanel();
    private final JTextArea logArea = new JTextArea();

    private final JLabel statusLabel = new JLabel();
    private final JLabel scoreLabel = new JLabel();
    private final JLabel actionsLabel = new JLabel();
    private final JLabel positionLabel = new JLabel();
    private final JLabel arrowLabel = new JLabel();
    private final JLabel goldLabel = new JLabel();
    private final JLabel perceptLabel = new JLabel();
    private final JLabel goalLabel = new JLabel();
    private final JLabel worldLabel = new JLabel();

    private final JButton stepButton = new JButton("Шаг");
    private final JButton autoButton = new JButton("Авто ▶");
    private final JButton finishButton = new JButton("До конца");
    private final JSlider speedSlider = new JSlider(30, 1500, 450);
    private final JCheckBox truthBox = new JCheckBox("Показать настоящую карту");
    private final JCheckBox probBox = new JCheckBox("Показать вероятности", true);

    private final Timer timer;
    private Game game;

    public WumpusFrame() {
        super("Мир вампуса — логический агент");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        timer = new Timer(speedSlider.getValue(), e -> {
            if (game == null || game.isOver()) {
                stopAuto();
                return;
            }
            game.step();
            refresh();
        });

        setLayout(new BorderLayout());
        add(buildSettings(), BorderLayout.NORTH);
        add(board, BorderLayout.CENTER);
        add(buildSidePanel(), BorderLayout.EAST);

        setupKeys();
        newRandomGame();

        setSize(1240, 800);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
    }

    // ===================== Построение интерфейса =====================

    private JComponent buildSettings() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xc8ccd4)));
        pitSpin.setEditor(new JSpinner.NumberEditor(pitSpin, "0.00"));
        riskSpin.setEditor(new JSpinner.NumberEditor(riskSpin, "0.00"));
        seedField.setToolTipText("Пусто — случайная пещера. Число — одна и та же пещера каждый раз.");
        riskSpin.setToolTipText("Агент делает рискованный ход, только если вероятность гибели не больше этого значения");
        riskSpin.addChangeListener(e -> {
            if (game != null) game.agent().setMaxRisk(((Number) riskSpin.getValue()).doubleValue());
        });

        JButton newGame = new JButton("Новая игра");
        newGame.addActionListener(e -> newRandomGame());
        JButton restart = new JButton("Заново ту же");
        restart.setToolTipText("Та же пещера, агент начинает с нуля");
        restart.addActionListener(e -> restartSameWorld());
        JButton book = new JButton("Мир из книги (рис. 7.2)");
        book.addActionListener(e -> startGame(World.bookExample(), "мир с рис. 7.2"));

        p.add(new JLabel("Ширина:"));
        p.add(widthSpin);
        p.add(new JLabel("Высота:"));
        p.add(heightSpin);
        p.add(Box.createHorizontalStrut(6));
        p.add(new JLabel("Вероятность ямы:"));
        p.add(pitSpin);
        p.add(new JLabel("Допустимый риск:"));
        p.add(riskSpin);
        p.add(new JLabel("Seed:"));
        p.add(seedField);
        p.add(Box.createHorizontalStrut(6));
        p.add(newGame);
        p.add(restart);
        p.add(book);
        return p;
    }

    private JComponent buildSidePanel() {
        JPanel side = new JPanel(new BorderLayout(0, 8));
        side.setBorder(BorderFactory.createEmptyBorder(10, 6, 10, 10));
        side.setPreferredSize(new Dimension(460, 600));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 17f));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(statusLabel);
        top.add(Box.createVerticalStrut(6));

        JPanel info = new JPanel(new GridBagLayout());
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.setBorder(BorderFactory.createTitledBorder("Состояние"));
        addRow(info, 0, "Счёт:", scoreLabel);
        addRow(info, 1, "Действий:", actionsLabel);
        addRow(info, 2, "Агент:", positionLabel);
        addRow(info, 3, "Стрела:", arrowLabel);
        addRow(info, 4, "Золото:", goldLabel);
        addRow(info, 5, "Восприятие:", perceptLabel);
        addRow(info, 6, "Цель:", goalLabel);
        addRow(info, 7, "Пещера:", worldLabel);
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD));
        top.add(info);
        top.add(Box.createVerticalStrut(6));

        JPanel controls = new JPanel(new GridBagLayout());
        controls.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.setBorder(BorderFactory.createTitledBorder("Управление"));
        stepButton.setToolTipText("Один шаг агента (пробел)");
        stepButton.addActionListener(e -> doStep());
        autoButton.addActionListener(e -> toggleAuto());
        finishButton.setToolTipText("Мгновенно доиграть до конца");
        finishButton.addActionListener(e -> finishGame());
        speedSlider.setInverted(true);
        speedSlider.setToolTipText("Скорость автоигры");
        speedSlider.addChangeListener(e -> timer.setDelay(speedSlider.getValue()));
        truthBox.addActionListener(e -> board.setShowTruth(truthBox.isSelected()));
        probBox.addActionListener(e -> board.setShowProbabilities(probBox.isSelected()));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttons.add(stepButton);
        buttons.add(autoButton);
        buttons.add(finishButton);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        controls.add(buttons, c);
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 0;
        c.insets = new Insets(6, 4, 0, 4);
        controls.add(new JLabel("Скорость:"), c);
        c.gridx = 1;
        c.weightx = 1;
        controls.add(speedSlider, c);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        controls.add(truthBox, c);
        c.gridy = 3;
        controls.add(probBox, c);
        top.add(controls);
        top.add(Box.createVerticalStrut(6));

        JLabel legend = new JLabel("<html><div style='width:330px'>"
                + "<b>B</b> ветерок, <b>S</b> запах, <b>G</b> блеск, <b>V</b> посещена.<br>"
                + "<b>OK</b> безопасна, <b>P!</b> / <b>W!</b> — точно яма / вампус,<br>"
                + "<b>P?</b> / <b>W?</b> — вероятность по перебору моделей.<br>"
                + "Чем краснее клетка, тем выше риск погибнуть в ней.</div></html>");
        legend.setAlignmentX(Component.LEFT_ALIGNMENT);
        legend.setBorder(BorderFactory.createTitledBorder("Обозначения"));
        top.add(legend);

        side.add(top, BorderLayout.NORTH);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Рассуждения агента"));
        side.add(scroll, BorderLayout.CENTER);
        return side;
    }

    private static void addRow(JPanel panel, int row, String name, JLabel value) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(1, 4, 1, 8);
        panel.add(new JLabel(name), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(value, c);
    }

    private void setupKeys() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "step");
        getRootPane().getActionMap().put("step", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!(getFocusOwner() instanceof JTextField)) doStep();
            }
        });
    }

    // ===================== Игра =====================

    private long lastSeed;

    private void newRandomGame() {
        int w = (Integer) widthSpin.getValue();
        int h = (Integer) heightSpin.getValue();
        double p = ((Number) pitSpin.getValue()).doubleValue();
        String text = seedField.getText().trim();
        long seed;
        if (text.isEmpty()) {
            seed = new Random().nextInt(1_000_000);
        } else {
            try {
                seed = Long.parseLong(text);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Seed должен быть целым числом", "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        lastSeed = seed;
        startGame(World.random(w, h, p, new Random(seed)), w + "×" + h + ", p=" + p + ", seed " + seed);
    }

    private String lastDescription;
    private World lastTemplate;

    private void restartSameWorld() {
        if (lastTemplate == null) return;
        if (lastDescription != null && lastDescription.startsWith("мир с рис")) {
            startGame(World.bookExample(), lastDescription);
            return;
        }
        int w = lastTemplate.grid().width, h = lastTemplate.grid().height;
        startGame(World.random(w, h, lastTemplate.pitProbability(), new Random(lastSeed)), lastDescription);
    }

    private void startGame(World world, String description) {
        stopAuto();
        lastTemplate = world;
        lastDescription = description;
        logArea.setText("");
        appendLog("Новая игра: " + description);
        double risk = ((Number) riskSpin.getValue()).doubleValue();
        game = new Game(world, risk, this::appendLog);
        board.setGame(game);
        refresh();
    }

    private void doStep() {
        if (game == null || game.isOver()) return;
        game.step();
        refresh();
    }

    private void finishGame() {
        if (game == null) return;
        stopAuto();
        game.runToEnd(100_000);
        refresh();
    }

    private void toggleAuto() {
        if (timer.isRunning()) stopAuto();
        else if (game != null && !game.isOver()) {
            timer.start();
            autoButton.setText("Пауза ❚❚");
        }
    }

    private void stopAuto() {
        timer.stop();
        autoButton.setText("Авто ▶");
    }

    private void appendLog(String line) {
        logArea.append(line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void refresh() {
        World w = game.world();
        Agent a = game.agent();
        World.Status st = w.status();
        statusLabel.setText(st.text);
        statusLabel.setForeground(switch (st) {
            case PLAYING -> new Color(0x2b2f36);
            case WON -> new Color(0x1f8a3b);
            case CLIMBED_WITHOUT_GOLD -> new Color(0xb36b00);
            default -> new Color(0xc0392b);
        });
        scoreLabel.setText(String.valueOf(w.score()));
        actionsLabel.setText(String.valueOf(w.actionCount()));
        positionLabel.setText(w.agentPosition() + ", смотрит на " + w.agentDirection().nameRu);
        arrowLabel.setText(w.agentHasArrow() ? "есть" : "использована");
        goldLabel.setText(w.agentHasGold() ? "у агента" : "не найдено");
        perceptLabel.setText(w.lastPercept().toString());
        goalLabel.setText(w.isOver() ? "—" : a.goal());
        String wumpusInfo = w.isWumpusAlive() ? "" : ", вампус убит";
        worldLabel.setText(w.grid().width + "×" + w.grid().height + wumpusInfo
                + (a.knowledge().isApproximate() ? ", вероятности ≈" : ""));

        boolean playing = !w.isOver();
        stepButton.setEnabled(playing);
        autoButton.setEnabled(playing);
        finishButton.setEnabled(playing);
        if (!playing) stopAuto();
        board.repaint();
    }
}
