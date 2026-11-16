package antcolony.ui;

import javax.swing.JFrame;

/** Главное окно программы. */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("Муравьиный алгоритм — задача коммивояжёра");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(new SimulationView());
        setSize(1280, 820);
        setMinimumSize(new java.awt.Dimension(980, 720));
        setLocationRelativeTo(null);
    }
}
