package wumpus;

import wumpus.world.Grid;
import wumpus.world.Position;
import wumpus.world.World;

import java.util.ArrayDeque;
import java.util.Locale;
import java.util.Random;

/**
 * Прогон агента на множестве случайных пещер без интерфейса.
 * Запуск: java -cp wumpus.jar wumpus.Benchmark [игр] [ширина] [высота] [p_ямы] [допустимый_риск]
 */
public class Benchmark {

    public static void main(String[] args) {
        int games = args.length > 0 ? Integer.parseInt(args[0]) : 10000;
        int w = args.length > 1 ? Integer.parseInt(args[1]) : 4;
        int h = args.length > 2 ? Integer.parseInt(args[2]) : 4;
        double p = args.length > 3 ? Double.parseDouble(args[3]) : 0.2;
        double risk = args.length > 4 ? Double.parseDouble(args[4]) : 0.30;
        run(games, w, h, p, risk, 42);
    }

    public static void run(int games, int w, int h, double p, double risk, long seed) {
        Random rnd = new Random(seed);
        int won = 0, pit = 0, eaten = 0, empty = 0, stuck = 0, reachable = 0;
        long totalScore = 0;
        long start = System.nanoTime();
        for (int i = 0; i < games; i++) {
            World world = World.random(w, h, p, rnd);
            if (goldReachable(world)) reachable++;
            Game game = new Game(world, risk, null);
            game.runToEnd(20 * w * h * 4 + 100);
            totalScore += world.score();
            switch (world.status()) {
                case WON -> won++;
                case FELL_INTO_PIT -> pit++;
                case EATEN_BY_WUMPUS -> eaten++;
                case CLIMBED_WITHOUT_GOLD -> empty++;
                case PLAYING -> stuck++;
            }
        }
        double secs = (System.nanoTime() - start) / 1e9;
        System.out.printf(Locale.ROOT,
                "%dx%d, p=%.2f, риск<=%.2f, игр %d: победы %.1f%% (золото в принципе достижимо в %.1f%%), "
                        + "яма %.1f%%, вампус %.1f%%, ушёл без золота %.1f%%, зависло %d, средний счёт %.1f, %.1f с%n",
                w, h, p, risk, games, 100.0 * won / games, 100.0 * reachable / games,
                100.0 * pit / games, 100.0 * eaten / games, 100.0 * empty / games, stuck,
                (double) totalScore / games, secs);
    }

    /** Можно ли дойти до золота, обходя ямы (вампуса можно убить). */
    static boolean goldReachable(World world) {
        Position gold = world.gold();
        if (world.hasPit(gold)) return false;
        Grid g = world.grid();
        boolean[][] seen = new boolean[g.width + 1][g.height + 1];
        ArrayDeque<Position> q = new ArrayDeque<>();
        q.add(Grid.START);
        seen[1][1] = true;
        while (!q.isEmpty()) {
            Position c = q.poll();
            if (c.equals(gold)) return true;
            for (Position n : g.neighbors(c)) {
                if (!seen[n.x()][n.y()] && !world.hasPit(n)) {
                    seen[n.x()][n.y()] = true;
                    q.add(n);
                }
            }
        }
        return false;
    }
}
