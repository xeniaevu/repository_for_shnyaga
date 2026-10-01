package lifesim.view;

import lifesim.model.Agent;
import lifesim.model.Environment;
import lifesim.model.Simulation;

/** Консольное представление: сетка из символов P (растение), T (травоядное), X (хищник). */
public class ConsoleView {
    private static final char EMPTY = '.';

    public void render(Simulation sim) {
        Environment env = sim.getEnvironment();
        StringBuilder sb = new StringBuilder();
        sb.append('+').append("-".repeat(env.getWidth())).append("+\n");
        for (int y = 0; y < env.getHeight(); y++) {
            sb.append('|');
            for (int x = 0; x < env.getWidth(); x++) {
                Agent a = env.getAgent(x, y);
                sb.append(a == null ? EMPTY : a.getSymbol());
            }
            sb.append("|\n");
        }
        sb.append('+').append("-".repeat(env.getWidth())).append("+\n");
        sb.append(sim.getStats());
        System.out.println(sb);
    }
}
