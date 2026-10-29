package wumpus.agent;

import wumpus.world.Direction;
import wumpus.world.Grid;
import wumpus.world.Percept;
import wumpus.world.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * База знаний агента и логический вывод.
 *
 * <p>В базе хранятся только факты, которые агент сам почувствовал, плюс правила игры:
 * <ul>
 *   <li>ветерок в клетке ⇔ хотя бы в одной соседней клетке яма;</li>
 *   <li>запах в клетке ⇔ вампус в этой или соседней клетке;</li>
 *   <li>вампус ровно один, в [1,1] нет ни ям, ни вампуса;</li>
 *   <li>каждая клетка (кроме [1,1]) — яма с известной вероятностью p.</li>
 * </ul>
 *
 * <p><b>Вывод — проверка по моделям (раздел 7.3 книги).</b> Перебираются расстановки ям
 * и положения вампуса, совместимые с базой знаний. Утверждение «в клетке нет ямы» следует из KB,
 * если оно истинно во всех таких моделях. Если же оно истинно лишь в части моделей, то доля этих
 * моделей (с учётом априорной вероятности p) — это вероятность, которую агент использует для риска.
 *
 * <p>Чтобы перебор не рос как 2^(число клеток), перебираются только клетки «границы» —
 * неизвестные клетки рядом с посещёнными. Остальные клетки на наблюдения не влияют.
 * Граница дополнительно делится на независимые компоненты. Ямы и вампус независимы
 * (наблюдения про ветерок касаются только ям, про запах — только вампуса), поэтому
 * их выводы считаются отдельно.
 */
public class KnowledgeBase {

    /** Компоненты до такого размера перебираются точно (2^18 ≈ 262 тысячи моделей). */
    public static final int EXACT_LIMIT = 18;
    private static final int GIBBS_BURN_IN = 300;
    private static final int GIBBS_SAMPLES = 4000;

    private final Grid grid;
    private final double pitPrior;

    // Наблюдения
    private final boolean[][] visited;
    private final boolean[][] breeze;
    private final boolean[][] stench;
    private final boolean[][] glitter;
    private final boolean[][] visitedWhileWumpusAlive;
    private final boolean[][] clearedByArrow;
    private List<Position> screamLine = null; // если вампус убит: он был на одной из этих клеток
    private boolean wumpusAlive = true;

    // Результаты вывода
    private double[][] pitProb;
    private double[][] wumpusProb;
    private int wumpusCandidateCount;
    private boolean approximate;
    private int version = 0;

    public KnowledgeBase(Grid grid, double pitPrior) {
        this.grid = grid;
        this.pitPrior = pitPrior;
        int w = grid.width + 1, h = grid.height + 1;
        visited = new boolean[w][h];
        breeze = new boolean[w][h];
        stench = new boolean[w][h];
        glitter = new boolean[w][h];
        visitedWhileWumpusAlive = new boolean[w][h];
        clearedByArrow = new boolean[w][h];
        infer();
    }

    // ===================== TELL: добавление фактов =====================

    /** Агент находится в клетке p и получил восприятие percept. */
    public void tell(Position p, Percept percept) {
        int x = p.x(), y = p.y();
        visited[x][y] = true;
        breeze[x][y] = percept.breeze();
        stench[x][y] = percept.stench();
        glitter[x][y] = percept.glitter();
        if (wumpusAlive) visitedWhileWumpusAlive[x][y] = true;
        infer();
        version++;
    }

    /** Агент выстрелил из клетки from в направлении dir и услышал (или не услышал) крик. */
    public void tellShot(Position from, Direction dir, boolean scream) {
        List<Position> line = arrowLine(from, dir);
        if (scream) {
            wumpusAlive = false;
            screamLine = line;
        } else {
            for (Position c : line) clearedByArrow[c.x()][c.y()] = true;
        }
        infer();
        version++;
    }

    public void tellGoldTaken(Position p) {
        glitter[p.x()][p.y()] = false;
        version++;
    }

    /** Клетки, через которые пролетит стрела. */
    public List<Position> arrowLine(Position from, Direction dir) {
        List<Position> line = new ArrayList<>();
        for (Position c = from.step(dir); grid.inside(c); c = c.step(dir)) line.add(c);
        return line;
    }

    // ===================== ASK: логический вывод =====================

    private void infer() {
        inferWumpus();
        inferPits();
    }

    /**
     * Вампус ровно один, поэтому модель для него — это просто его клетка.
     * Перебираем все клетки и оставляем те, что не противоречат ни одному наблюдению.
     * Априорно положение равновероятно, значит и апостериорно оно равновероятно среди оставшихся.
     */
    private void inferWumpus() {
        List<Position> candidates = new ArrayList<>();
        for (Position c : grid.cells()) {
            if (c.equals(Grid.START)) continue;
            if (visitedWhileWumpusAlive[c.x()][c.y()]) continue;  // агент был тут и выжил
            if (clearedByArrow[c.x()][c.y()]) continue;           // стрела пролетела мимо
            if (screamLine != null && !screamLine.contains(c)) continue;
            if (consistentWithStench(c)) candidates.add(c);
        }
        wumpusProb = new double[grid.width + 1][grid.height + 1];
        wumpusCandidateCount = candidates.size();
        for (Position c : candidates) wumpusProb[c.x()][c.y()] = 1.0 / candidates.size();
    }

    /** Проверка модели «вампус в клетке c» по всем посещённым клеткам. */
    private boolean consistentWithStench(Position c) {
        for (Position v : grid.cells()) {
            if (!visited[v.x()][v.y()]) continue;
            boolean shouldSmell = c.equals(v) || Grid.adjacent(c, v);
            if (shouldSmell != stench[v.x()][v.y()]) return false;
        }
        return true;
    }

    private void inferPits() {
        int w = grid.width + 1, h = grid.height + 1;
        pitProb = new double[w][h];
        approximate = false;

        // 1. Соседи клеток без ветерка точно без ям
        boolean[][] noPit = new boolean[w][h];
        for (Position v : grid.cells()) {
            if (!visited[v.x()][v.y()]) continue;
            noPit[v.x()][v.y()] = true;
            if (!breeze[v.x()][v.y()]) {
                for (Position n : grid.neighbors(v)) noPit[n.x()][n.y()] = true;
            }
        }
        noPit[1][1] = true;

        // 2. Ветерок в v ⇒ дизъюнкция «яма в одной из оставшихся неизвестных соседних клеток»
        List<Position> vars = new ArrayList<>();
        Map<Position, Integer> index = new HashMap<>();
        List<int[]> clauses = new ArrayList<>();
        for (Position v : grid.cells()) {
            if (!visited[v.x()][v.y()] || !breeze[v.x()][v.y()]) continue;
            List<Integer> clause = new ArrayList<>();
            for (Position n : grid.neighbors(v)) {
                if (noPit[n.x()][n.y()]) continue;
                Integer i = index.get(n);
                if (i == null) {
                    i = vars.size();
                    vars.add(n);
                    index.put(n, i);
                }
                clause.add(i);
            }
            if (!clause.isEmpty()) clauses.add(clause.stream().mapToInt(Integer::intValue).toArray());
        }

        // 3. Делим границу на независимые компоненты (система непересекающихся множеств)
        int[] parent = new int[vars.size()];
        for (int i = 0; i < parent.length; i++) parent[i] = i;
        for (int[] clause : clauses) {
            for (int k = 1; k < clause.length; k++) union(parent, clause[0], clause[k]);
        }
        Map<Integer, List<Integer>> compVars = new HashMap<>();
        Map<Integer, List<int[]>> compClauses = new HashMap<>();
        for (int i = 0; i < vars.size(); i++) compVars.computeIfAbsent(find(parent, i), k -> new ArrayList<>()).add(i);
        for (int[] clause : clauses) compClauses.computeIfAbsent(find(parent, clause[0]), k -> new ArrayList<>()).add(clause);

        double[] varProb = new double[vars.size()];
        for (Map.Entry<Integer, List<Integer>> e : compVars.entrySet()) {
            List<Integer> globalIds = e.getValue();
            Map<Integer, Integer> local = new HashMap<>();
            for (int i = 0; i < globalIds.size(); i++) local.put(globalIds.get(i), i);
            List<int[]> localClauses = new ArrayList<>();
            for (int[] clause : compClauses.getOrDefault(e.getKey(), List.of())) {
                int[] lc = new int[clause.length];
                for (int k = 0; k < clause.length; k++) lc[k] = local.get(clause[k]);
                localClauses.add(lc);
            }
            double[] probs = globalIds.size() <= EXACT_LIMIT
                    ? solveExact(globalIds.size(), localClauses)
                    : solveSampling(globalIds.size(), localClauses);
            for (int i = 0; i < globalIds.size(); i++) varProb[globalIds.get(i)] = probs[i];
        }

        // 4. Итог по всем клеткам
        for (Position c : grid.cells()) {
            int x = c.x(), y = c.y();
            if (noPit[x][y]) pitProb[x][y] = 0.0;
            else if (index.containsKey(c)) pitProb[x][y] = varProb[index.get(c)];
            else pitProb[x][y] = pitPrior; // клетка далеко — о ней ничего не известно
        }
    }

    /**
     * Честный перебор моделей с отсечением: каждая переменная — «есть ли яма»,
     * вес модели = p^(ям) · (1−p)^(пустых). Ветви, где какой-то ветерок остался
     * без ямы, отбрасываются сразу. Если все дизъюнкции уже выполнены, остальные
     * переменные свободны и суммируются аналитически.
     */
    private double[] solveExact(int n, List<int[]> clauses) {
        ExactCounter counter = new ExactCounter(n, clauses, pitPrior);
        counter.dfs(0, 1.0);
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            if (!counter.canBePit[i]) result[i] = 0.0;
            else if (!counter.canBeEmpty[i]) result[i] = 1.0;
            else result[i] = counter.pitWeight[i] / counter.total;
        }
        return result;
    }

    private static final class ExactCounter {
        final int n;
        final int[][] clauses;
        final List<List<Integer>> clausesOf = new ArrayList<>();
        final double p;
        final int[] assign;
        final int[] unassignedInClause;
        final int[] pitsInClause;
        int satisfied = 0;
        double total = 0;
        final double[] pitWeight;
        final boolean[] canBePit;
        final boolean[] canBeEmpty;

        ExactCounter(int n, List<int[]> clauseList, double p) {
            this.n = n;
            this.p = p;
            this.clauses = clauseList.toArray(new int[0][]);
            for (int i = 0; i < n; i++) clausesOf.add(new ArrayList<>());
            for (int c = 0; c < clauses.length; c++) for (int v : clauses[c]) clausesOf.get(v).add(c);
            assign = new int[n];
            unassignedInClause = new int[clauses.length];
            pitsInClause = new int[clauses.length];
            for (int c = 0; c < clauses.length; c++) unassignedInClause[c] = clauses[c].length;
            pitWeight = new double[n];
            canBePit = new boolean[n];
            canBeEmpty = new boolean[n];
        }

        void dfs(int i, double w) {
            if (satisfied == clauses.length) {
                // Все наблюдения объяснены: оставшиеся клетки свободны
                total += w;
                for (int j = 0; j < i; j++) {
                    if (assign[j] == 1) { pitWeight[j] += w; canBePit[j] = true; }
                    else canBeEmpty[j] = true;
                }
                for (int j = i; j < n; j++) {
                    pitWeight[j] += w * p;
                    if (p > 0) canBePit[j] = true;
                    if (p < 1) canBeEmpty[j] = true;
                }
                return;
            }
            if (i == n) return;

            // Вариант «в клетке i яма»
            if (p > 0) {
                assign[i] = 1;
                for (int c : clausesOf.get(i)) {
                    if (pitsInClause[c]++ == 0) satisfied++;
                    unassignedInClause[c]--;
                }
                dfs(i + 1, w * p);
                for (int c : clausesOf.get(i)) {
                    if (--pitsInClause[c] == 0) satisfied--;
                    unassignedInClause[c]++;
                }
            }
            // Вариант «в клетке i ямы нет»
            if (p < 1) {
                assign[i] = 0;
                boolean ok = true;
                for (int c : clausesOf.get(i)) {
                    unassignedInClause[c]--;
                    if (unassignedInClause[c] == 0 && pitsInClause[c] == 0) ok = false;
                }
                if (ok) dfs(i + 1, w * (1 - p));
                for (int c : clausesOf.get(i)) unassignedInClause[c]++;
            }
        }
    }

    /**
     * Для очень большой компоненты (бывает только на больших полях) точный перебор слишком долгий.
     * Тогда вероятности оцениваются выборкой Гиббса по тем же моделям. Достоверные выводы при этом
     * остаются точными: в дизъюнкциях только «есть яма», поэтому клетка точно яма лишь тогда,
     * когда она единственная в какой-то дизъюнкции, а «точно нет ямы» внутри компоненты не бывает.
     */
    private double[] solveSampling(int n, List<int[]> clauses) {
        approximate = true;
        Random rnd = new Random(12345L + n);
        List<List<Integer>> clausesOf = new ArrayList<>();
        for (int i = 0; i < n; i++) clausesOf.add(new ArrayList<>());
        for (int c = 0; c < clauses.size(); c++) for (int v : clauses.get(c)) clausesOf.get(v).add(c);
        boolean[] forced = new boolean[n];
        for (int[] cl : clauses) if (cl.length == 1) forced[cl[0]] = true;

        int[] assign = new int[n];
        int[] pits = new int[clauses.size()];
        for (int i = 0; i < n; i++) assign[i] = 1; // «ямы везде» — заведомо допустимая модель
        for (int c = 0; c < clauses.size(); c++) pits[c] = clauses.get(c).length;

        long[] count = new long[n];
        for (int sweep = 0; sweep < GIBBS_BURN_IN + GIBBS_SAMPLES; sweep++) {
            for (int i = 0; i < n; i++) {
                boolean canBeEmpty = true;
                for (int c : clausesOf.get(i)) if (pits[c] - assign[i] == 0) { canBeEmpty = false; break; }
                int value = (!canBeEmpty || rnd.nextDouble() < pitPrior) ? 1 : 0;
                if (value != assign[i]) {
                    for (int c : clausesOf.get(i)) pits[c] += value - assign[i];
                    assign[i] = value;
                }
            }
            if (sweep >= GIBBS_BURN_IN) for (int i = 0; i < n; i++) count[i] += assign[i];
        }
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = forced[i] ? 1.0
                    : Math.min(0.999, Math.max(0.001, count[i] / (double) GIBBS_SAMPLES));
        }
        return result;
    }

    private static int find(int[] parent, int i) {
        while (parent[i] != i) i = parent[i] = parent[parent[i]];
        return i;
    }

    private static void union(int[] parent, int a, int b) {
        parent[find(parent, a)] = find(parent, b);
    }

    // ===================== Запросы =====================

    public boolean isVisited(Position p) { return visited[p.x()][p.y()]; }
    public boolean hadBreeze(Position p) { return breeze[p.x()][p.y()]; }
    public boolean hadStench(Position p) { return stench[p.x()][p.y()]; }
    public boolean hadGlitter(Position p) { return glitter[p.x()][p.y()]; }
    public boolean isWumpusAlive() { return wumpusAlive; }
    public int wumpusCandidateCount() { return wumpusCandidateCount; }
    public boolean isApproximate() { return approximate; }
    public int version() { return version; }
    public double pitPrior() { return pitPrior; }

    /** Вероятность ямы в клетке (0 и 1 — достоверные выводы). */
    public double pitProbability(Position p) { return pitProb[p.x()][p.y()]; }

    /** Вероятность того, что вампус (живой или мёртвый) находится в клетке. */
    public double wumpusProbability(Position p) { return wumpusProb[p.x()][p.y()]; }

    /** Вероятность погибнуть, войдя в клетку. */
    public double danger(Position p) {
        if (isVisited(p)) return 0.0;
        double pp = pitProbability(p);
        double wp = wumpusAlive ? wumpusProbability(p) : 0.0;
        if (pp == 0.0 && wp == 0.0) return 0.0;
        if (pp == 1.0 || wp == 1.0) return 1.0;
        return 1.0 - (1.0 - pp) * (1.0 - wp);
    }

    /** Клетка с отметкой OK: из базы знаний следует, что там нет ни ямы, ни живого вампуса. */
    public boolean isSafe(Position p) {
        return danger(p) == 0.0;
    }

    /** Достоверно известная клетка вампуса, или null. */
    public Position knownWumpus() {
        if (wumpusCandidateCount != 1) return null;
        for (Position c : grid.cells()) if (wumpusProb[c.x()][c.y()] == 1.0) return c;
        return null;
    }
}
