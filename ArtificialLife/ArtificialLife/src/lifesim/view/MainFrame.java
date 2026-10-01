package lifesim.view;

import lifesim.controller.SimulationController;
import lifesim.model.SimulationConfig;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Taskbar;
import java.awt.image.BufferedImage;

/**
 * Главное окно. Содержимое — {@link MainPanel}; здесь только настройка самого окна:
 * размер, значок, «прозрачный» заголовок на macOS и открытие окна параметров.
 */
public class MainFrame extends JFrame {
    /** Высота заголовка окна macOS, под которым проходит содержимое. */
    private static final int MAC_TITLE_BAR = 28;

    private final SimulationController controller;
    private final MainPanel mainPanel;
    private boolean settingsOpen;

    public MainFrame(SimulationController controller) {
        super("Искусственная жизнь");
        this.controller = controller;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        if (Theme.MAC) {
            // заголовок окна сливается с панелью управления (как в современных приложениях macOS)
            getRootPane().putClientProperty("apple.awt.fullWindowContent", Boolean.TRUE);
            getRootPane().putClientProperty("apple.awt.transparentTitleBar", Boolean.TRUE);
            getRootPane().putClientProperty("apple.awt.windowAppearance", "NSAppearanceNameDarkAqua");
        }
        getContentPane().setBackground(Theme.WINDOW);

        mainPanel = new MainPanel(controller, this::openSettings);
        setContentPane(mainPanel);
        installIcon();
        installMacSettingsMenu();

        pack();
        // Если содержимое действительно ушло под заголовок, отодвигаем панель от «светофора».
        if (Theme.MAC && getInsets().top == 0) {
            mainPanel.setTitleBarInset(MAC_TITLE_BAR);
            pack();
        }
        fitToScreen();
        setLocationRelativeTo(null);
    }

    /** Оформление Swing; вызывать до создания окон. */
    public static void installLookAndFeel() {
        Theme.installLookAndFeel();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) mainPanel.getGridPanel().requestFocusInWindow();
    }

    private void fitToScreen() {
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int w = Math.min(getWidth(), (int) (screen.width * 0.94));
        int h = Math.min(getHeight(), (int) (screen.height * 0.94));
        setSize(w, h);
        setMinimumSize(new Dimension(Math.min(w, 820), Math.min(h, 560)));
    }

    private void installIcon() {
        BufferedImage icon = Theme.appIcon(512);
        setIconImage(icon);
        try {
            if (Taskbar.isTaskbarSupported() && Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
                Taskbar.getTaskbar().setIconImage(icon);   // значок в Dock на macOS
            }
        } catch (RuntimeException ignored) {
            // значок не критичен
        }
    }

    /** На macOS пункт «Настройки…» (⌘,) в меню приложения открывает параметры модели. */
    private void installMacSettingsMenu() {
        if (!Theme.MAC || !Desktop.isDesktopSupported()) return;
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
            desktop.setPreferencesHandler(e -> SwingUtilities.invokeLater(this::openSettings));
        }
    }

    private void openSettings() {
        if (settingsOpen) return;
        settingsOpen = true;
        try {
            controller.pause();
            SimulationConfig updated = new SettingsDialog(this, controller.getConfig()).showDialog();
            if (updated != null) controller.applyConfig(updated);
        } finally {
            settingsOpen = false;
        }
    }
}
