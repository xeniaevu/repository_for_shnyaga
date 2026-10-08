package mousemaze.env;

/**
 * Ответ среды на действие мыши (обратная связь = подкрепление).
 *
 * @param nextState номер нового состояния s'
 * @param reward    награда r за этот шаг
 * @param done      эпизод закончен (мышь нашла сыр)
 * @param event     что произошло
 */
public record StepResult(int nextState, double reward, boolean done, StepEvent event) {
}
