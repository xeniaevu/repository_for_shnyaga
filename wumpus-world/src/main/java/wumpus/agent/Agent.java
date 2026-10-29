package wumpus.agent;

import wumpus.world.Action;
import wumpus.world.Direction;
import wumpus.world.Grid;
import wumpus.world.Percept;
import wumpus.world.Position;
import wumpus.world.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Агент, основанный на знаниях. Видит только свои восприятия, всё остальное выводит
 * через {@link KnowledgeBase}. Порядок решений на каждом шаге:
 * <ol>
 *   <li>блеск — хватаем золото; с золотом — идём в [1,1] и выбираемся;</li>
 *   <li>есть достижимая безопасная (OK) непосещённая клетка — идём в ближайшую;</li>
 *   <li>иначе рассматриваем граничные клетки: для каждой считаем риск гибели.
 *       Если там может быть вампус и стрела окупается — сначала стреляем
 *       (после выстрела вампуса в этой клетке точно нет: либо он убит, либо стрела
 *       прошла мимо и, значит, его там не было);</li>
 *   <li>если наименьший риск не больше допустимого — рискуем, иначе идём домой без золота.</li>
 * </ol>
 */
public class Agent {

    private final Grid grid;
    private final KnowledgeBase kb;
    private final Consumer<String> log;
    private double maxRisk;

    private Position pos = Grid.START;
    private Direction dir = Direction.EAST;
    private boolean hasArrow = true;
    private boolean hasGold = false;
    private boolean glitterHere = false;

    private final Deque<Action> plan = new ArrayDeque<>();
    private int planVersion = -1;
    private String goal = "—";

    public Agent(Grid grid, double pitPrior, double maxRisk, Consumer<String> log) {
        this.grid = grid;
        this.kb = new KnowledgeBase(grid, pitPrior);
        this.maxRisk = maxRisk;
        this.log = log != null ? log : s -> { };
    }

    /** Первое восприятие в стартовой клетке. */
    public void start(Percept first) {
        say("Старт в [1,1], смотрю на восток. Восприятие " + first);
        learn(first);
    }

    /** Следующее действие. */
    public Action nextAction() {
        if (planVersion != kb.version()) plan.clear(); // узнали что-то новое — пересматриваем план
        if (plan.isEmpty()) {
            makePlan();
            planVersion = kb.version();
        }
        return plan.poll();
    }

    /** Результат действия: агент обновляет своё положение и базу знаний. */
    public void afterAction(Action action, Percept p) {
        switch (action) {
            case FORWARD -> {
                if (p.bump()) {
                    say("Удар о стену (Bump).");
                } else {
                    pos = pos.step(dir);
                    if (kb.isVisited(pos)) glitterHere = p.glitter(); // здесь уже были — нового не узнаем
                    else learn(p);
                }
            }
            case TURN_LEFT -> dir = dir.left();
            case TURN_RIGHT -> dir = dir.right();
            case GRAB -> {
                if (glitterHere) {
                    hasGold = true;
                    glitterHere = false;
                    kb.tellGoldTaken(pos);
                    say("Золото у меня!");
                }
            }
            case SHOOT -> {
                if (hasArrow) {
                    hasArrow = false;
                    String[][] before = marks();
                    kb.tellShot(pos, dir, p.scream());
                    if (p.scream()) say("Слышу крик (Scream) — вампус убит!");
                    else say("Крика нет — промах. Значит, на линии выстрела вампуса нет.");
                    reportNewFacts(before);
                }
            }
            case CLIMB -> { }
        }
        if (plan.isEmpty() || planVersion != kb.version()) goal = "—";
    }

    // ===================== Восприятие и выводы =====================

    private void learn(Percept p) {
        String[][] before = marks();
        kb.tell(pos, p);
        glitterHere = p.glitter();

        if (p.glitter()) say("Блеск (Glitter) — здесь золото!");
        if (p.breeze()) {
            List<Position> cand = grid.neighbors(pos).stream()
                    .filter(c -> !kb.isVisited(c) && kb.pitProbability(c) > 0).toList();
            List<Position> knownBefore = cand.stream().filter(c -> "P!".equals(before[c.x()][c.y()])).toList();
            boolean sureNow = cand.stream().anyMatch(c -> kb.pitProbability(c) == 1.0);
            if (!knownBefore.isEmpty()) say("Ветерок — от уже известной ямы в " + list(knownBefore));
            else if (!sureNow && cand.size() > 1) say("Ветерок → яма хотя бы в одной из клеток " + list(cand));
        }
        if (p.stench() && kb.isWumpusAlive()) {
            List<Position> cand = grid.cells().stream()
                    .filter(c -> kb.wumpusProbability(c) > 0).toList();
            if (cand.size() > 1) say("Запах → вампус в одной из клеток " + list(cand));
        }
        if (!p.breeze() && !p.stench() && !p.glitter()) say("Ничего не чувствую — соседние клетки без ям и вампуса.");
        reportNewFacts(before);
    }

    /** Отметка клетки для сравнения «до/после»: V, OK, P!, W! или ?. */
    private String mark(Position c) {
        if (kb.isVisited(c)) return "V";
        if (kb.pitProbability(c) == 1.0) return "P!";
        if (kb.isWumpusAlive() && kb.wumpusProbability(c) == 1.0) return "W!";
        if (kb.isSafe(c)) return "OK";
        return "?";
    }

    private String[][] marks() {
        String[][] m = new String[grid.width + 1][grid.height + 1];
        for (Position c : grid.cells()) m[c.x()][c.y()] = mark(c);
        return m;
    }

    private void reportNewFacts(String[][] before) {
        List<Position> ok = new ArrayList<>(), pits = new ArrayList<>(), wumpus = new ArrayList<>();
        for (Position c : grid.cells()) {
            String now = mark(c);
            if (now.equals(before[c.x()][c.y()])) continue;
            switch (now) {
                case "OK" -> ok.add(c);
                case "P!" -> pits.add(c);
                case "W!" -> wumpus.add(c);
                default -> { }
            }
        }
        if (!ok.isEmpty()) say("Вывод: безопасно (OK) " + list(ok));
        if (!pits.isEmpty()) say("Вывод: точно яма (P!) в " + list(pits));
        if (!wumpus.isEmpty()) say("Вывод: точно вампус (W!) в " + list(wumpus));
    }

    // ===================== Планирование =====================

    /** Вариант хода в граничную (неизвестную) клетку. */
    private record Option(Position target, Position from, Direction dirTo, int cost,
                         double pitP, double wumpusP, double danger, boolean shootFirst, double risk) { }

    private void makePlan() {
        if (glitterHere && !hasGold) {
            decide("Хватаю золото (Grab).", "взять золото");
            plan.add(Action.GRAB);
            return;
        }
        if (hasGold) {
            goHome("Золото у меня — возвращаюсь в [1,1].", "домой с золотом");
            return;
        }

        PathFinder pf = new PathFinder(grid, pos, dir, kb::isSafe);

        // 1. Ближайшая безопасная непосещённая клетка
        Position bestSafe = null;
        int bestCost = PathFinder.UNREACHABLE;
        for (Position c : grid.cells()) {
            if (kb.isVisited(c) || !kb.isSafe(c)) continue;
            int cost = pf.cost(c);
            if (cost < bestCost) {
                bestCost = cost;
                bestSafe = c;
            }
        }
        if (bestSafe != null) {
            decide("Иду в безопасную клетку " + bestSafe + ".", "исследовать " + bestSafe);
            plan.addAll(pf.actionsTo(bestSafe, null));
            return;
        }

        // 2. Безопасных ходов нет — оцениваем риск граничных клеток
        boolean canShoot = hasArrow && kb.isWumpusAlive();
        Option best = null;
        for (Position c : grid.cells()) {
            if (kb.isVisited(c)) continue;
            for (Position s : grid.neighbors(c)) {
                if (!kb.isSafe(s)) continue;
                Direction d = Direction.between(s, c);
                int cost = pf.cost(s, d);
                if (cost == PathFinder.UNREACHABLE) continue;
                double pp = kb.pitProbability(c);
                double wp = kb.isWumpusAlive() ? kb.wumpusProbability(c) : 0.0;
                // Стрела окупается, если ожидаемый спасённый штраф больше её цены
                boolean shoot = canShoot && wp * World.DEATH_PENALTY > World.ARROW_COST;
                // После выстрела вампуса в клетке c точно нет: либо убит, либо его там не было
                double risk = shoot ? pp : kb.danger(c);
                if (risk >= 1.0) continue; // верная смерть
                Option o = new Option(c, s, d, cost + 1, pp, wp, kb.danger(c), shoot, risk);
                if (best == null || o.risk < best.risk - 1e-12
                        || (Math.abs(o.risk - best.risk) <= 1e-12 && o.cost < best.cost)) best = o;
            }
        }

        if (best == null) {
            goHome("Достижимых неисследованных клеток не осталось, золота не нашёл.", "домой без золота");
            return;
        }
        if (best.risk > maxRisk) {
            goHome(String.format(Locale.ROOT,
                    "Безопасных ходов нет. Меньше всего риск в %s: %s — больше допустимого %s. Ухожу без золота.",
                    best.target, pct(best.risk), pct(maxRisk)), "домой без золота");
            return;
        }

        List<Action> path = pf.actionsTo(best.from, best.dirTo);
        if (best.shootFirst) {
            double kill = 0;
            for (Position c : kb.arrowLine(best.from, best.dirTo)) kill += kb.wumpusProbability(c);
            decide(String.format(Locale.ROOT,
                    "Безопасных ходов нет. В %s может быть вампус (%s). Стреляю из %s на %s, шанс попасть %s.",
                    best.target, pct(best.wumpusP), best.from, best.dirTo.nameRu, pct(kill)),
                    "выстрел на " + best.dirTo.nameRu);
            plan.addAll(path);
            plan.add(Action.SHOOT);
        } else {
            decide(String.format(Locale.ROOT,
                    "Безопасных ходов нет. Рискую: иду в %s, вероятность гибели %s (яма %s, вампус %s).",
                    best.target, pct(best.danger), pct(best.pitP), pct(best.wumpusP)),
                    "рискованный ход в " + best.target);
            plan.addAll(path);
            plan.add(Action.FORWARD);
        }
    }

    private void goHome(String reason, String goalText) {
        if (pos.equals(Grid.START)) {
            decide(reason + " Выбираюсь из пещеры (Climb).", goalText);
            plan.add(Action.CLIMB);
            return;
        }
        List<Action> path = new PathFinder(grid, pos, dir, kb::isSafe).actionsTo(Grid.START, null);
        decide(reason, goalText);
        if (path != null) plan.addAll(path);
        plan.add(Action.CLIMB);
    }

    // ===================== Вспомогательное =====================

    private void decide(String text, String goalText) {
        goal = goalText;
        log.accept("→ " + text);
    }

    private void say(String text) {
        log.accept("   " + text);
    }

    private static String list(List<Position> cells) {
        if (cells.size() > 8) {
            return cells.subList(0, 8).stream().map(Position::toString).collect(Collectors.joining(", "))
                    + " и ещё " + (cells.size() - 8);
        }
        return cells.stream().map(Position::toString).collect(Collectors.joining(", "));
    }

    private static String pct(double v) {
        return Math.round(v * 100) + "%";
    }

    public KnowledgeBase knowledge() { return kb; }
    public Position position() { return pos; }
    public Direction direction() { return dir; }
    public boolean hasGold() { return hasGold; }
    public boolean hasArrow() { return hasArrow; }
    public String goal() { return goal; }
    public double maxRisk() { return maxRisk; }
    public void setMaxRisk(double maxRisk) { this.maxRisk = maxRisk; }
}
