package mousemaze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import mousemaze.generator.MazeFileIO;
import mousemaze.generator.MazeGenerator;
import mousemaze.model.CellType;
import mousemaze.model.Maze;

class MazeTest {

    private static int passable(Maze m) {
        int n = 0;
        for (int r = 0; r < m.rows(); r++) {
            for (int c = 0; c < m.cols(); c++) {
                if (m.get(r, c).isPassable()) {
                    n++;
                }
            }
        }
        return n;
    }

    @Test
    void dfsBuildsPerfectMaze() {
        for (long seed = 1; seed <= 20; seed++) {
            int rows = 3 + (int) (seed % 7);
            int cols = 4 + (int) (seed % 5);
            Maze m = new MazeGenerator(new Random(seed)).generate(rows, cols, 0, 3, 2);
            // Остовное дерево: R*C комнат и R*C-1 пробитых стен между ними.
            assertEquals(2 * rows * cols - 1, passable(m), "seed " + seed);
            assertNull(m.validate());
            assertEquals(3, m.count(CellType.WATER));
            assertEquals(2, m.count(CellType.TRAP));
        }
    }

    @Test
    void loopsRemoveExtraWalls() {
        Maze perfect = new MazeGenerator(new Random(7)).generate(8, 8, 0, 0, 0);
        Maze braided = new MazeGenerator(new Random(7)).generate(8, 8, 0.3, 0, 0);
        assertTrue(passable(braided) > passable(perfect));
        assertTrue(braided.shortestPathLength() <= perfect.shortestPathLength());
    }

    @Test
    void fileFormatRoundTrip() {
        Maze m = MazeFileIO.parse(List.of(
                "; комментарий",
                "#####",
                "#S.W#",
                "#.#T#",
                "#..C#",
                "#####"));
        assertEquals(5, m.rows());
        assertEquals(5, m.cols());
        assertEquals(CellType.START, m.get(m.start()));
        assertEquals(4, m.shortestPathLength());
        Maze again = MazeFileIO.parse(List.of(m.toText().split("\n")));
        assertEquals(m.toText(), again.toText());
    }

    @Test
    void unreachableCheeseIsReported() {
        Maze m = MazeFileIO.parse(List.of("#####", "#S#C#", "#####"));
        assertEquals("Сыр недостижим из стартовой клетки.", m.validate());
    }
}
