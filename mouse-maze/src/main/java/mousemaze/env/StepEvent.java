package mousemaze.env;

/** Что произошло с мышью на шаге — для статистики и журнала. */
public enum StepEvent {
    MOVE,
    WALL,
    WATER,
    WATER_ALREADY_DRUNK,
    TRAP,
    CHEESE
}
