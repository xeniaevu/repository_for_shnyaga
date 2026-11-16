package antcolony.ui;

import antcolony.algorithm.AntColony;
import antcolony.algorithm.ExactSolver;
import antcolony.algorithm.Parameters;
import antcolony.model.Graph;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

/**
 * Главная панель программы. Связывает модель (колонию) с визуализацией
 * и управляет ходом моделирования по таймеру.
 */
public class SimulationView extends JPanel {

    /** Период таймера анимации, мс. */
    private static final int TICK_MS = 16;

    private final Parameters params = new Parameters();
    private final Random random = new Random();
    private final GraphPanel graphPanel = new GraphPanel();
    private final ChartPanel chartPanel = new ChartPanel();
    private final ControlPanel controlPanel;
    private final Timer timer;

    private AntColony colony;
    private double exactLength = Double.NaN;
    private boolean running;
    /** Доля текущего шага, уже показанная в анимации (0..1). */
    private double progress = 1;
    private double fastBudget;

    public SimulationView() {
        super(new BorderLayout());
        controlPanel = new ControlPanel(this, params);

        JPanel center = new JPanel(new BorderLayout());
        center.add(graphPanel, BorderLayout.CENTER);
        center.add(chartPanel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        graphPanel.setGraphChangeListener(new Runnable() {
            @Override
            public void run() {
                // среда изменилась: пересчитываем точный оптимум, муравьи адаптируются сами
                recalculateExactLength();
                refresh();
            }
        });
        timer = new Timer(TICK_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tick();
            }
        });
        createNewGraph();
    }

    public GraphPanel getGraphPanel() {
        return graphPanel;
    }

    public AntColony getColony() {
        return colony;
    }

    /** Создаёт новый граф и новую колонию. */
    public void createNewGraph() {
        int nodeCount = controlPanel.getNodeCount();
        Graph graph = controlPanel.isCircleLayout()
                ? Graph.createCircle(nodeCount)
                : Graph.createRandom(nodeCount, random);
        colony = new AntColony(graph, params, random);
        graphPanel.setColony(colony);
        chartPanel.setColony(colony);
        progress = 1;
        recalculateExactLength();
        refresh();
    }

    /** Сбрасывает феромон и статистику, граф остаётся прежним. */
    public void resetColony() {
        colony.reset();
        progress = 1;
        refresh();
    }

    public void toggleRunning() {
        running = !running;
        if (running) {
            timer.start();
        } else {
            timer.stop();
        }
        controlPanel.setRunning(running);
    }

    /** Кнопка «Один цикл»: довыполнить текущий цикл без анимации. */
    public void runSingleIteration() {
        colony.runIteration();
        progress = 1;
        refresh();
    }

    /** Один такт таймера. */
    private void tick() {
        double speed = controlPanel.getSpeed();
        if (controlPanel.isFastMode()) {
            // без анимации: несколько циклов в секунду, муравьи стоят в узлах
            fastBudget += 0.03 + speed * speed * 2;
            while (fastBudget >= 1) {
                fastBudget -= 1;
                colony.runIteration();
            }
            progress = 1;
        } else {
            progress += 0.004 + speed * speed * 1.5;
            while (progress >= 1) {
                progress -= 1;
                colony.advance();
            }
        }
        refresh();
    }

    private void recalculateExactLength() {
        exactLength = ExactSolver.shortestTourLength(colony.getGraph());
        chartPanel.setExactLength(exactLength);
    }

    private void refresh() {
        graphPanel.setProgress(progress);
        controlPanel.updateStats(colony, exactLength);
        graphPanel.repaint();
        chartPanel.repaint();
    }
}
