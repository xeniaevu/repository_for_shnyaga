package mousemaze;

import javafx.application.Application;
import mousemaze.ui.MainApp;

/**
 * Точка входа для запуска из IDE «зелёной стрелкой».
 * Класс не наследует Application, поэтому JavaFX можно подключить
 * просто как библиотеку из Maven, без настройки module-path.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
