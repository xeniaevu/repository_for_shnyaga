package wumpus.world;

/** Исполнительные механизмы агента (раздел PEAS). */
public enum Action {
    FORWARD("Forward"),
    TURN_LEFT("TurnLeft"),
    TURN_RIGHT("TurnRight"),
    GRAB("Grab"),
    SHOOT("Shoot"),
    CLIMB("Climb");

    public final String title;

    Action(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
