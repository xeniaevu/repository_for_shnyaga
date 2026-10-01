package lifesim.view;

import lifesim.model.SimulationConfig;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.ToIntFunction;

/** Окно параметров модели. */
public class SettingsDialog extends JDialog {
    private final Form form;
    private boolean accepted;

    public SettingsDialog(JFrame owner, SimulationConfig current) {
        super(owner, "Параметры модели", true);
        form = new Form(current);
        form.onCancel = this::dispose;
        form.onApply = () -> {
            accepted = true;
            dispose();
        };
        setContentPane(form);
        getRootPane().setDefaultButton(form.applyButton);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        if (Theme.MAC) getRootPane().putClientProperty("apple.awt.windowAppearance", "NSAppearanceNameDarkAqua");
        setResizable(false);
        pack();
        setLocationRelativeTo(owner);
    }

    /** Показать окно; возвращает новые параметры или null, если нажата «Отмена». */
    public SimulationConfig showDialog() {
        setVisible(true);
        return accepted ? form.config : null;
    }

    // =====================================================================

    /** Содержимое окна: четыре группы параметров и кнопки. */
    static final class Form extends JPanel {

        /** Связь «поле ввода ↔ параметр конфигурации». */
        private static final class IntParam {
            final String label;
            final NumberField field;
            final ToIntFunction<SimulationConfig> getter;
            final IntConsumer setter;

            IntParam(String label, NumberField field, ToIntFunction<SimulationConfig> getter, IntConsumer setter) {
                this.label = label;
                this.field = field;
                this.getter = getter;
                this.setter = setter;
            }
        }

        final SimulationConfig config;
        final FlatButton applyButton = new FlatButton("Применить и перезапустить", null, true);
        Runnable onApply = () -> { };
        Runnable onCancel = () -> { };

        private final List<IntParam> params = new ArrayList<>();
        private final JLabel errorLabel = new JLabel(" ");
        private JPanel currentGroup;
        private GridBagConstraints gc;

        Form(SimulationConfig current) {
            super(new BorderLayout());
            this.config = current.copy();
            setBackground(Theme.WINDOW);
            SimulationConfig c = config;

            JPanel groups = new JPanel(new GridLayout(2, 2, 40, 26));
            groups.setOpaque(false);
            groups.setBorder(BorderFactory.createEmptyBorder(22, 24, 18, 24));

            groups.add(group("Поле", null));
            add("Ширина M", SimulationConfig::getWidth, c::setWidth, 10, 300);
            add("Высота N", SimulationConfig::getHeight, c::setHeight, 10, 200);
            add("Растений в начале", SimulationConfig::getInitialPlants, c::setInitialPlants, 0, 50000);
            add("Травоядных в начале", SimulationConfig::getInitialHerbivores, c::setInitialHerbivores, 0, 50000);
            add("Хищников в начале", SimulationConfig::getInitialPredators, c::setInitialPredators, 0, 50000);

            groups.add(group("Растения", Theme.Species.PLANT.icon(10)));
            add("Начальная энергия", SimulationConfig::getPlantInitialEnergy, c::setPlantInitialEnergy, 1, 1000);
            add("Прирост за шаг (+n)", SimulationConfig::getPlantGrowth, c::setPlantGrowth, 0, 100);
            add("Порог размножения P", SimulationConfig::getPlantReproductionThreshold, c::setPlantReproductionThreshold, 1, 1000);
            add("Энергия потомка", SimulationConfig::getPlantChildEnergy, c::setPlantChildEnergy, 1, 1000);

            groups.add(group("Травоядные", Theme.Species.HERBIVORE.icon(10)));
            add("Начальная энергия", SimulationConfig::getHerbivoreInitialEnergy, c::setHerbivoreInitialEnergy, 1, 1000);
            add("Расход за шаг (F)", SimulationConfig::getHerbivoreMetabolism, c::setHerbivoreMetabolism, 0, 100);
            add("Энергия от растения (E1)", SimulationConfig::getHerbivoreFoodGain, c::setHerbivoreFoodGain, 0, 1000);
            add("Порог размножения H", SimulationConfig::getHerbivoreReproductionThreshold, c::setHerbivoreReproductionThreshold, 1, 5000);
            add("Энергия потомка", SimulationConfig::getHerbivoreChildEnergy, c::setHerbivoreChildEnergy, 1, 5000);

            groups.add(group("Хищники", Theme.Species.PREDATOR.icon(10)));
            add("Начальная энергия", SimulationConfig::getPredatorInitialEnergy, c::setPredatorInitialEnergy, 1, 1000);
            add("Расход за шаг (F2)", SimulationConfig::getPredatorMetabolism, c::setPredatorMetabolism, 0, 100);
            add("Энергия от травоядного (E2)", SimulationConfig::getPredatorMeatGain, c::setPredatorMeatGain, 0, 1000);
            add("Порог размножения X", SimulationConfig::getPredatorReproductionThreshold, c::setPredatorReproductionThreshold, 1, 5000);
            add("Энергия потомка", SimulationConfig::getPredatorChildEnergy, c::setPredatorChildEnergy, 1, 5000);

            fill(config);
            add(groups, BorderLayout.CENTER);
            add(buildFooter(), BorderLayout.SOUTH);
        }

        private JPanel group(String title, Icon icon) {
            currentGroup = new JPanel(new GridBagLayout());
            currentGroup.setOpaque(false);
            gc = new GridBagConstraints();
            gc.gridy = 0;
            gc.gridx = 0;
            gc.gridwidth = 2;
            gc.anchor = GridBagConstraints.WEST;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.insets = new Insets(0, 0, 8, 0);

            JLabel label = new JLabel(title, icon, SwingConstants.LEFT);
            label.setIconTextGap(8);
            label.setFont(Theme.medium(13));
            label.setForeground(Theme.TEXT);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.HAIRLINE),
                    BorderFactory.createEmptyBorder(0, 0, 7, 0)));
            currentGroup.add(label, gc);
            gc.gridy++;
            gc.gridwidth = 1;
            gc.insets = new Insets(3, 0, 3, 0);

            JPanel wrapper = new JPanel(new BorderLayout());   // прижать группу к верху ячейки
            wrapper.setOpaque(false);
            wrapper.add(currentGroup, BorderLayout.NORTH);
            return wrapper;
        }

        private void add(String label, ToIntFunction<SimulationConfig> getter, IntConsumer setter, int min, int max) {
            JLabel l = new JLabel(label);
            l.setFont(Theme.regular(13));
            l.setForeground(Theme.TEXT_MUTED);
            gc.gridx = 0;
            gc.weightx = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.insets = new Insets(3, 0, 3, 16);
            currentGroup.add(l, gc);
            gc.insets = new Insets(3, 0, 3, 0);

            NumberField field = new NumberField(min, max);
            gc.gridx = 1;
            gc.weightx = 0;
            gc.fill = GridBagConstraints.NONE;
            gc.anchor = GridBagConstraints.EAST;
            currentGroup.add(field, gc);
            gc.anchor = GridBagConstraints.WEST;
            gc.gridy++;
            params.add(new IntParam(label, field, getter, setter));
        }

        private void fill(SimulationConfig source) {
            for (IntParam p : params) p.field.setValue(p.getter.applyAsInt(source));
            showError(null, null);
        }

        private JComponent buildFooter() {
            FlatButton defaults = new FlatButton("По умолчанию", null, false);
            FlatButton cancel = new FlatButton("Отмена", null, false);
            defaults.setToolTipText("Вернуть подобранные значения в поля");
            defaults.addActionListener(e -> fill(new SimulationConfig()));
            cancel.addActionListener(e -> onCancel.run());
            applyButton.addActionListener(e -> apply());

            errorLabel.setFont(Theme.regular(12));
            errorLabel.setForeground(Theme.DANGER);

            JPanel footer = new JPanel();
            footer.setBackground(Theme.WINDOW);
            footer.setLayout(new BoxLayout(footer, BoxLayout.X_AXIS));
            footer.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.HAIRLINE),
                    BorderFactory.createEmptyBorder(14, 24, 16, 24)));
            footer.add(defaults);
            footer.add(Box.createHorizontalStrut(16));
            footer.add(errorLabel);
            footer.add(Box.createHorizontalStrut(12));
            footer.add(Box.createHorizontalGlue());
            footer.add(cancel);
            footer.add(Box.createHorizontalStrut(8));
            footer.add(applyButton);
            for (Component c : footer.getComponents()) ((JComponent) c).setAlignmentY(Component.CENTER_ALIGNMENT);
            return footer;
        }

        private void apply() {
            for (IntParam p : params) {
                Integer v = p.field.getValidValue();
                if (v == null) {
                    showError(p, "Введите число от " + p.field.min + " до " + p.field.max);
                    p.field.requestFocusInWindow();
                    return;
                }
            }
            for (IntParam p : params) p.setter.accept(p.field.getValidValue());
            onApply.run();
        }

        private void showError(IntParam bad, String text) {
            for (IntParam p : params) p.field.setError(p == bad);
            errorLabel.setText(text == null ? " " : text);
        }
    }

    // =====================================================================

    /** Поле для целого числа: только цифры, ↑/↓ меняют значение на 1. */
    static final class NumberField extends JTextField {
        final int min;
        final int max;
        private boolean error;

        NumberField(int min, int max) {
            super(6);
            this.min = min;
            this.max = max;
            setFont(Theme.regular(13));
            setForeground(Theme.TEXT);
            setBackground(new Color(0x2A2722));
            setCaretColor(Theme.TEXT);
            setSelectionColor(Theme.alpha(Theme.ACCENT, 110));
            setSelectedTextColor(Theme.TEXT);
            setHorizontalAlignment(RIGHT);
            setOpaque(false);
            setBorder(new FieldBorder());
            Dimension d = new Dimension(84, 28);
            setPreferredSize(d);
            setMinimumSize(d);

            ((AbstractDocument) getDocument()).setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String s, AttributeSet a)
                        throws BadLocationException {
                    super.insertString(fb, offset, digits(s), a);
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String s, AttributeSet a)
                        throws BadLocationException {
                    super.replace(fb, offset, length, digits(s), a);
                }
            });
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { selectAll(); repaint(); }
                @Override public void focusLost(FocusEvent e) { repaint(); }
            });
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    int delta = e.getKeyCode() == KeyEvent.VK_UP ? 1 : e.getKeyCode() == KeyEvent.VK_DOWN ? -1 : 0;
                    if (delta == 0) return;
                    Integer v = getValidValue();
                    setValue(Math.max(min, Math.min(max, (v == null ? min : v) + delta)));
                    e.consume();
                }
            });
        }

        private static String digits(String s) {
            return s == null ? "" : s.replaceAll("[^0-9]", "");
        }

        void setValue(int v) { setText(String.valueOf(v)); }

        /** Значение, если оно число в допустимых пределах, иначе null. */
        Integer getValidValue() {
            String t = getText().trim();
            if (t.isEmpty() || t.length() > 9) return null;
            int v = Integer.parseInt(t);
            return v < min || v > max ? null : v;
        }

        void setError(boolean error) {
            this.error = error;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.antialias(g2);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 10, 10));
            g2.dispose();
            super.paintComponent(g);
        }

        /** Скруглённая рамка: зелёная при фокусе, рыжая при ошибке. */
        private final class FieldBorder extends AbstractBorder {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.antialias(g2);
                boolean focus = c.isFocusOwner();
                g2.setColor(error ? Theme.DANGER : focus ? Theme.ACCENT : Theme.CONTROL_BORDER);
                g2.setStroke(new BasicStroke(error || focus ? 1.6f : 1f));
                g2.draw(new RoundRectangle2D.Float(x + 0.75f, y + 0.75f, w - 1.5f, h - 1.5f, 10, 10));
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) { return new Insets(4, 10, 4, 10); }

            @Override
            public Insets getBorderInsets(Component c, Insets insets) {
                insets.set(4, 10, 4, 10);
                return insets;
            }
        }
    }
}
