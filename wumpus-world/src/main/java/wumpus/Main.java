package wumpus;

import wumpus.gui.WumpusFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Точка входа: графический интерфейс. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
                // останется стандартный внешний вид
            }
            new WumpusFrame().setVisible(true);
        });
    }
}
