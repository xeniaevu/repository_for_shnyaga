package lifesim.app;

import lifesim.model.PopulationStats;
import lifesim.model.Simulation;
import lifesim.model.SimulationConfig;
import lifesim.view.ConsoleView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Точка входа предварительной (консольной) версии.
 * Управление: Enter — один шаг, число N — N шагов, q — выход.
 * Цвета выключаются аргументом --no-color или переменной окружения NO_COLOR.
 */
public class ConsoleMain {

    public static void main(String[] args) throws IOException {
        boolean colors = System.getenv("NO_COLOR") == null && !Arrays.asList(args).contains("--no-color");

        // Те же параметры, что в оконной версии (поле 100×60): с ними жизнь не прекращается.
        // Предустановка SimulationConfig.consoleDefaults() (поле 80×40) оказалась неустойчивой:
        // на маленьком поле популяции малы, и какой-нибудь вид часто вымирает случайно.
        Simulation sim = new Simulation(new SimulationConfig());
        ConsoleView view = new ConsoleView(colors);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        printHelp(view);
        view.render(sim);

        while (true) {
            System.out.print("> ");
            String line = in.readLine();
            if (line == null) break;                 // ввод закончился
            line = line.trim();
            if (line.equalsIgnoreCase("q") || line.equalsIgnoreCase("й")) break;

            int steps = parseSteps(line);
            if (steps <= 0) {
                System.out.println("Не понял команду «" + line + "».");
                printHelp(view);
                continue;
            }

            for (int i = 0; i < steps && !sim.isAnySpeciesExtinct(); i++) {
                sim.step();
            }
            view.render(sim);

            if (sim.isAnySpeciesExtinct()) {
                System.out.println(view.warning("Жизнь прекратилась: " + extinctSpecies(sim.getStats()) + "."));
                break;
            }
        }
        System.out.println("Выход.");
    }

    /** Пустая строка — 1 шаг, число — столько шагов; иначе 0 (ошибка). */
    private static int parseSteps(String line) {
        if (line.isEmpty()) return 1;
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String extinctSpecies(PopulationStats s) {
        StringBuilder sb = new StringBuilder();
        if (s.getPlants() == 0) sb.append("растения");
        if (s.getHerbivores() == 0) sb.append(sb.length() > 0 ? ", " : "").append("травоядные");
        if (s.getPredators() == 0) sb.append(sb.length() > 0 ? ", " : "").append("хищники");
        return "вымерли " + sb;
    }

    private static void printHelp(ConsoleView view) {
        System.out.println(view.legend());
        System.out.println("Enter — один шаг, число N — N шагов, q — выход.");
    }
}