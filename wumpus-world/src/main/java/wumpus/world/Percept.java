package wumpus.world;

/** Восприятие — список из пяти символов [Stench, Breeze, Glitter, Bump, Scream]. */
public record Percept(boolean stench, boolean breeze, boolean glitter, boolean bump, boolean scream) {

    @Override
    public String toString() {
        return "[" + (stench ? "Stench" : "None") + ", "
                + (breeze ? "Breeze" : "None") + ", "
                + (glitter ? "Glitter" : "None") + ", "
                + (bump ? "Bump" : "None") + ", "
                + (scream ? "Scream" : "None") + "]";
    }
}
