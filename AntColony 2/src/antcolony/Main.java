package antcolony;

import antcolony.ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Точка входа: запуск программы с графической визуализацией. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                    // остаётся стандартное оформление
                }
                new MainFrame().setVisible(true);
            }
        });
    }
}
