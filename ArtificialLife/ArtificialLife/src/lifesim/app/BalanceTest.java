package lifesim.app;

import lifesim.model.PopulationStats;
import lifesim.model.Simulation;
import lifesim.model.SimulationConfig;

/**
 * Эмпирическая проверка баланса экосистемы: несколько прогонов без графики
 * с разными seed. Выводит, дожили ли все виды до конца и минимумы популяций.
 * Запуск: java -cp out lifesim.app.BalanceTest [прогонов] [шагов]
 */
public class BalanceTest {
    public static void main(String[] args) {
        int runs = args.length > 0 ? Integer.parseInt(args[0]) : 20;
        int steps = args.length > 1 ? Integer.parseInt(args[1]) : 10000;
        SimulationConfig config = new SimulationConfig();
        int survived = 0;

        for (int r = 0; r < runs; r++) {
            Simulation sim = new Simulation(config, 1000L + r);
            int minP = Integer.MAX_VALUE, minH = Integer.MAX_VALUE, minX = Integer.MAX_VALUE;
            long sumH = 0, sumX = 0, sumP = 0;
            long extinctAt = -1;
            for (int s = 0; s < steps; s++) {
                sim.step();
                PopulationStats st = sim.getStats();
                minP = Math.min(minP, st.getPlants());
                minH = Math.min(minH, st.getHerbivores());
                minX = Math.min(minX, st.getPredators());
                sumP += st.getPlants(); sumH += st.getHerbivores(); sumX += st.getPredators();
                if (sim.isAnySpeciesExtinct()) {
                    extinctAt = sim.getStep();
                    break;
                }
            }
            long n = Math.max(1, sim.getStep());
            if (extinctAt < 0) survived++;
            System.out.printf("Прогон %2d: %s | min P=%d T=%d X=%d | среднее P=%d T=%d X=%d%n",
                    r + 1,
                    extinctAt < 0 ? "жизнь продолжается " : "вымирание на шаге " + extinctAt,
                    minP, minH, minX, sumP / n, sumH / n, sumX / n);
        }
        System.out.printf("Итого: жизнь сохранилась в %d из %d прогонов (%d шагов)%n", survived, runs, steps);
    }
}
