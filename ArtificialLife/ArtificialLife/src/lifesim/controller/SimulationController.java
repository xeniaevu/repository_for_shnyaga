package lifesim.controller;

import lifesim.model.Simulation;
import lifesim.model.SimulationConfig;

import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер: управляет игровым циклом (Game Loop) и связывает модель с представлением.
 * Цикл работает на javax.swing.Timer — отдельном таймерном потоке, который вызывает
 * шаг модели в потоке обработки событий Swing (поэтому отрисовка и модель не конфликтуют).
 */
public class SimulationController {
    private SimulationConfig config;
    private Simulation simulation;
    private final Timer timer;
    private final List<Runnable> listeners = new ArrayList<>();
    private boolean stopOnExtinction = true;

    public SimulationController(SimulationConfig config) {
        this.config = config.copy();
        this.simulation = new Simulation(this.config);
        this.timer = new Timer(100, e -> tick());
    }

    private void tick() {
        simulation.step();
        if (stopOnExtinction && simulation.isAnySpeciesExtinct()) {
            timer.stop();
        }
        fireUpdate();
    }

    public void start() {
        if (!timer.isRunning()) timer.start();
        fireUpdate();
    }

    public void pause() {
        timer.stop();
        fireUpdate();
    }

    /** Один шаг вручную (симуляция при этом ставится на паузу). */
    public void step() {
        timer.stop();
        tick();
    }

    /** Новая симуляция с текущими параметрами. */
    public void reset() {
        timer.stop();
        simulation = new Simulation(config);
        fireUpdate();
    }

    /** Применить новые параметры и перезапустить модель. */
    public void applyConfig(SimulationConfig newConfig) {
        this.config = newConfig.copy();
        reset();
    }

    /** Скорость в шагах в секунду. */
    public void setStepsPerSecond(int stepsPerSecond) {
        int delay = Math.max(1, 1000 / Math.max(1, stepsPerSecond));
        timer.setDelay(delay);
        timer.setInitialDelay(delay);
    }

    public void setStopOnExtinction(boolean value) { this.stopOnExtinction = value; }
    public boolean isRunning() { return timer.isRunning(); }
    public Simulation getSimulation() { return simulation; }
    public SimulationConfig getConfig() { return config.copy(); }

    public void addUpdateListener(Runnable listener) { listeners.add(listener); }

    private void fireUpdate() {
        for (Runnable l : listeners) l.run();
    }
}
