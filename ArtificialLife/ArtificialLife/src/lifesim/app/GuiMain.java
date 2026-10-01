package lifesim.app;

import lifesim.controller.SimulationController;
import lifesim.model.SimulationConfig;
import lifesim.view.MainFrame;

import javax.swing.SwingUtilities;
import java.util.Locale;

/** Точка входа окончательной (оконной) версии. */
public class GuiMain {
    public static void main(String[] args) {
        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac")) {
            // Должно быть задано до первого обращения к AWT/Swing.
            System.setProperty("apple.awt.application.name", "Искусственная жизнь");
            System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua");
        }
        SwingUtilities.invokeLater(() -> {
            MainFrame.installLookAndFeel();
            SimulationController controller = new SimulationController(new SimulationConfig());
            new MainFrame(controller).setVisible(true);
        });
    }
}
