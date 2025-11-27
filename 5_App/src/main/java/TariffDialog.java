import javax.swing.*;
import java.awt.*;

public class TariffDialog extends JDialog {
    private JComboBox<TariffType> tariffTypeCombo;
    private JTextField priceMonthField;
    private JTextField pricePerMbField;
    private boolean confirmed = false;

    // Для добавления тарифа
    public TariffDialog(Frame owner, String title, Color backgroundColor) {
        super(owner, title, true);
        initComponents(backgroundColor, null);
        setSize(400, 200);
        setLocationRelativeTo(owner);
    }

    // Для редактирования тарифа
    public TariffDialog(Frame owner, String title, Color backgroundColor, Tariff tariffToEdit) {
        super(owner, title, true);
        initComponents(backgroundColor, tariffToEdit);
        setSize(400, 200);
        setLocationRelativeTo(owner);
    }

    private void initComponents(Color backgroundColor, Tariff tariff) {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.setBackground(backgroundColor);

        tariffTypeCombo = new JComboBox<>(TariffType.values());
        priceMonthField = new JTextField();
        pricePerMbField = new JTextField();

        if (tariff != null) {
            tariffTypeCombo.setSelectedItem(TariffType.valueOf(tariff.getType()));
            priceMonthField.setText(Double.toString(tariff.getPriceMonth()));
            pricePerMbField.setText(Double.toString(tariff.getPricePerMb()));
        }

        panel.add(new JLabel("Выберите тип тарифа:"));
        panel.add(tariffTypeCombo);
        panel.add(new JLabel("Абонентская плата:"));
        panel.add(priceMonthField);
        panel.add(new JLabel("Цена за 1 Мбайт:"));
        panel.add(pricePerMbField);

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Отмена");

        okButton.addActionListener(e -> {
            confirmed = true;
            setVisible(false);
        });
        cancelButton.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public TariffType getSelectedTariffType() {
        return (TariffType) tariffTypeCombo.getSelectedItem();
    }

    public String getPriceMonth() {
        return priceMonthField.getText();
    }

    public String getPricePerMb() {
        return pricePerMbField.getText();
    }
}
