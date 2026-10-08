package mousemaze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import mousemaze.env.Environment;
import mousemaze.env.RewardConfig;
import mousemaze.env.StepEvent;
import mousemaze.env.StepResult;
import mousemaze.generator.MazeFileIO;
import mousemaze.model.Action;
import mousemaze.model.Position;

class EnvironmentTest {

    private final Environment env = new Environment(MazeFileIO.parse(List.of(
            "######",
            "#SWT.#",
            "####C#",
            "######")).copy(), RewardConfig.defaults());

    @Test
    void wallKeepsMouseInPlace() {
        env.reset();
        StepResult r = env.step(Action.UP);
        assertEquals(StepEvent.WALL, r.event());
        assertEquals(-5, r.reward());
        assertEquals(new Position(1, 1), env.position());
    }

    @Test
    void waterIsDrunkOncePerEpisode() {
        env.reset();
        assertEquals(10, env.step(Action.RIGHT).reward());
        int stateWithWater = env.currentState();
        env.step(Action.LEFT);
        StepResult again = env.step(Action.RIGHT);
        assertEquals(StepEvent.WATER_ALREADY_DRUNK, again.event());
        assertEquals(-1, again.reward());
        assertEquals(stateWithWater, again.nextState());

        env.reset(); // новый эпизод — вода снова на месте
        assertEquals(StepEvent.WATER, env.step(Action.RIGHT).event());
    }

    @Test
    void sameCellDiffersByDrunkMask() {
        env.reset();
        int before = env.encode(1, 1, 0);
        env.step(Action.RIGHT);
        env.step(Action.LEFT);
        assertEquals(new Position(1, 1), env.position());
        assertTrue(env.currentState() != before, "состояние должно помнить выпитую воду");
    }

    @Test
    void trapAndCheese() {
        env.reset();
        env.step(Action.RIGHT);
        StepResult trap = env.step(Action.RIGHT);
        assertEquals(-30, trap.reward());
        assertFalse(trap.done());
        assertEquals(-1, env.step(Action.RIGHT).reward());
        StepResult cheese = env.step(Action.DOWN);
        assertEquals(StepEvent.CHEESE, cheese.event());
        assertEquals(100, cheese.reward());
        assertTrue(cheese.done());
    }
}
