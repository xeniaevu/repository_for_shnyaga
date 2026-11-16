package antcolony.ui;

import antcolony.algorithm.AntColony;
import antcolony.algorithm.Parameters;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

/** Панель управления: кнопки, параметры алгоритма, скорость и статистика. */
public class ControlPanel extends JPanel {

    private final SimulationView view;
    private final Parameters params;

    private final JButton startButton = new JButton("Старт");
    private final JSpinner nodeSpinner = new JSpinner(new SpinnerNumberModel(10, 3, 40, 1));
    private final JComboBox<String> layoutBox =
            new JComboBox<String>(new String[]{"Случайно", "По окружности"});
    private final JSpinner antSpinner;
    private final JSlider speedSlider = new JSlider(1, 100, 25);
    private final JCheckBox fastBox = new JCheckBox("Без анимации (быстрый счёт)");

    private final JLabel iterationLabel = new JLabel();
    private final JLabel bestLabel = new JLabel();
    private final JLabel exactLabel = new JLabel();
    private final JLabel iterationBestLabel = new JLabel();
    private final JLabel averageLabel = new JLabel();
    private final JLabel convergedLabel = new JLabel();
    private final JLabel stateLabel = new JLabel();

    public ControlPanel(SimulationView simulationView, Parameters parameters) {
        this.view = simulationView;
        this.params = parameters;
        this.antSpinner = new JSpinner(new SpinnerNumberModel(params.antCount, 1, 200, 1));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        setPreferredSize(new Dimension(310, 700));

        add(title("Управление"));
        JPanel buttons = new JPanel(new GridLayout(2, 2, 6, 6));
        JButton stepButton = new JButton("Один цикл");
        JButton resetButton = new JButton("Сброс феромона");
        JButton newGraphButton = new JButton("Новый граф");
        buttons.add(startButton);
        buttons.add(stepButton);
        buttons.add(resetButton);
        buttons.add(newGraphButton);
        add(row(buttons));

        add(title("Граф"));
        add(field("Количество узлов (городов)", nodeSpinner));
        add(field("Расположение узлов", layoutBox));

        add(title("Параметры алгоритма"));
        add(field("Количество муравьёв", antSpinner));
        final JSpinner alpha = new JSpinner(new SpinnerNumberModel(params.alpha, 0.0, 10.0, 0.1));
        final JSpinner beta = new JSpinner(new SpinnerNumberModel(params.beta, 0.0, 10.0, 0.1));
        final JSpinner rho = new JSpinner(new SpinnerNumberModel(params.rho, 0.01, 0.99, 0.05));
        final JSpinner q = new JSpinner(new SpinnerNumberModel(params.q, 1.0, 10000.0, 10.0));
        add(field("α — значимость феромона", alpha));
        add(field("β — значимость расстояния", beta));
        add(field("ρ — испарение феромона", rho));
        add(field("Q — запас феромона муравья", q));
        final JCheckBox eliteBox = new JCheckBox("Элитные муравьи", params.elitist);
        eliteBox.setToolTipText("Лучший найденный маршрут дополнительно подкрепляется феромоном");
        add(row(eliteBox));

        add(title("Отображение"));
        add(field("Скорость", speedSlider));
        add(row(fastBox));
        final JCheckBox valuesBox = new JCheckBox("Показывать значения феромона");
        final JCheckBox bestBox = new JCheckBox("Показывать лучший маршрут", true);
        add(row(valuesBox));
        add(row(bestBox));

        add(title("Статистика"));
        JPanel stats = new JPanel(new GridLayout(0, 1, 0, 3));
        stats.add(iterationLabel);
        stats.add(bestLabel);
        stats.add(exactLabel);
        stats.add(iterationBestLabel);
        stats.add(averageLabel);
        stats.add(convergedLabel);
        stats.add(stateLabel);
        stateLabel.setFont(stateLabel.getFont().deriveFont(Font.BOLD));
        add(row(stats));

        add(Box.createVerticalGlue());
        JLabel hint = new JLabel("<html><body style='width:215px'>Толщина оранжевой линии — количество "
                + "феромона на пути. Зелёный пунктир — лучший маршрут, найденный муравьями. "
                + "Узлы можно перетаскивать мышью.</body></html>");
        hint.setForeground(new Color(110, 110, 110));
        add(row(hint));

        // --- обработчики ---
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                view.toggleRunning();
            }
        });
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                view.runSingleIteration();
            }
        });
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                view.resetColony();
            }
        });
        newGraphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                view.createNewGraph();
            }
        });
        nodeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // по умолчанию муравьёв столько же, сколько узлов
                antSpinner.setValue(nodeSpinner.getValue());
                view.createNewGraph();
            }
        });
        layoutBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                view.createNewGraph();
            }
        });
        antSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = ((Number) antSpinner.getValue()).intValue();
                if (value != params.antCount) {
                    params.antCount = value;
                    view.resetColony();
                }
            }
        });
        ChangeListener paramListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                params.alpha = ((Number) alpha.getValue()).doubleValue();
                params.beta = ((Number) beta.getValue()).doubleValue();
                params.rho = ((Number) rho.getValue()).doubleValue();
                params.q = ((Number) q.getValue()).doubleValue();
            }
        };
        alpha.addChangeListener(paramListener);
        beta.addChangeListener(paramListener);
        rho.addChangeListener(paramListener);
        q.addChangeListener(paramListener);
        eliteBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                params.elitist = eliteBox.isSelected();
            }
        });
        valuesBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                view.getGraphPanel().setShowPheromoneValues(valuesBox.isSelected());
            }
        });
        bestBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                view.getGraphPanel().setShowBestTour(bestBox.isSelected());
            }
        });
    }

    public int getNodeCount() {
        return ((Number) nodeSpinner.getValue()).intValue();
    }

    public boolean isCircleLayout() {
        return layoutBox.getSelectedIndex() == 1;
    }

    /** Положение ползунка скорости от 0 до 1. */
    public double getSpeed() {
        return speedSlider.getValue() / 100.0;
    }

    public boolean isFastMode() {
        return fastBox.isSelected();
    }

    public void setRunning(boolean running) {
        startButton.setText(running ? "Пауза" : "Старт");
    }

    /** Обновляет блок статистики. */
    public void updateStats(AntColony colony, double exactLength) {
        int ants = colony.getAnts().size();
        iterationLabel.setText("Цикл: " + colony.getIteration());
        boolean hasBest = colony.getBestTour() != null;
        bestLabel.setText("Лучший маршрут: " + (hasBest ? format(colony.getBestLength()) : "—"));
        if (Double.isNaN(exactLength)) {
            exactLabel.setText("Точный оптимум: не считается (узлов > 15)");
        } else {
            exactLabel.setText("Точный оптимум: " + format(exactLength));
        }
        iterationBestLabel.setText("Лучший в цикле: " + format(colony.getIterationBestLength()));
        averageLabel.setText("Средний в цикле: " + format(colony.getIterationAverageLength()));
        convergedLabel.setText("Муравьёв на лучшем маршруте: "
                + (hasBest ? colony.getAntsOnBestTour() : 0) + " из " + ants);

        String state;
        Color color;
        if (colony.getIteration() == 0) {
            state = "Феромон распределён равномерно";
            color = new Color(90, 90, 90);
        } else if (hasBest && colony.getAntsOnBestTour() == ants) {
            boolean optimal = !Double.isNaN(exactLength)
                    && colony.getBestLength() <= exactLength * (1 + 1e-9);
            state = optimal ? "Все муравьи на кратчайшем маршруте" : "Все муравьи на одном маршруте";
            color = new Color(25, 140, 75);
        } else if (hasBest && colony.getAntsOnBestTour() * 2 >= ants) {
            state = "Колония сходится к маршруту";
            color = new Color(200, 110, 20);
        } else {
            state = "Поиск: муравьи исследуют граф";
            color = new Color(40, 100, 200);
        }
        stateLabel.setText(state);
        stateLabel.setForeground(color);
    }

    private static String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "—";
        }
        return String.format(Locale.US, "%.1f", value);
    }

    private static JComponent title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13.5f));
        label.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        return row(label);
    }

    private static JComponent field(String text, JComponent editor) {
        JPanel panel = new JPanel(new java.awt.BorderLayout(8, 0));
        panel.add(new JLabel(text), java.awt.BorderLayout.CENTER);
        editor.setPreferredSize(new Dimension(editor instanceof JSlider ? 170 : editor instanceof JComboBox ? 135 : 86,
                editor.getPreferredSize().height));
        panel.add(editor, java.awt.BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        return row(panel);
    }

    private static JComponent row(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
        return component;
    }
}
