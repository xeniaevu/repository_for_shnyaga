import javax.swing.*;
import java.awt.*;

public class App_Laba4 {
    private static Provider provider = new Provider();

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        JFrame frame = new JFrame("Internet Provider System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.getContentPane().setBackground(new Color(110, 70, 131));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.BLACK);

        JButton tariffsBtn = new JButton("Тарифы");
        JButton editTariffBtn = new JButton("Редактировать тариф");
        JButton clientsBtn = new JButton("Клиенты");
        JButton reportsBtn = new JButton("Отчеты");

        Color buttonBackground = Color.WHITE;
        Color buttonForeground = new Color(110, 70, 131);

        tariffsBtn.setBackground(buttonBackground);
        editTariffBtn.setBackground(buttonBackground);
        clientsBtn.setBackground(buttonBackground);
        reportsBtn.setBackground(buttonBackground);

        tariffsBtn.setForeground(buttonForeground);
        editTariffBtn.setForeground(buttonForeground);
        clientsBtn.setForeground(buttonForeground);
        reportsBtn.setForeground(buttonForeground);

        topPanel.add(tariffsBtn);
        topPanel.add(editTariffBtn);
        topPanel.add(clientsBtn);
        topPanel.add(reportsBtn);

        frame.add(topPanel, BorderLayout.NORTH);

        tariffsBtn.addActionListener(e -> {
            TariffDialog dialog = new TariffDialog(frame, "Добавить тариф", new Color(147, 130, 155));
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                try {
                    TariffType type = dialog.getSelectedTariffType();
                    double priceMonth = Double.parseDouble(dialog.getPriceMonth());
                    double pricePerMb = Double.parseDouble(dialog.getPricePerMb());

                    if (priceMonth < 0 || pricePerMb < 0 || priceMonth > 1_000_000 || pricePerMb > 1000)
                        throw new NumberFormatException();

                    Tariff t = new Tariff(type, priceMonth, pricePerMb);
                    provider.addTariff(t);
                    JOptionPane.showMessageDialog(frame, "Тариф добавлен.");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,
                            "Введите корректные положительные числа :(( (стоимость тарифа не больше 1_000_000 и стоимость за Мб не больше 1000)");
                }
            }
        });

        editTariffBtn.addActionListener(e -> {
            if (provider.getTariffs().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Сначала добавьте тарифы.");
                return;
            }

            Tariff selectedTariff = (Tariff) JOptionPane.showInputDialog(frame,
                    "Выберите тариф для редактирования:",
                    "Редактировать тариф",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    provider.getTariffs().toArray(),
                    null);

            if (selectedTariff != null) {
                TariffDialog dialogEdit = new TariffDialog(frame, "Редактировать тариф", new Color(255, 255, 200), selectedTariff);
                dialogEdit.setVisible(true);

                if (dialogEdit.isConfirmed()) {
                    try {
                        TariffType type = dialogEdit.getSelectedTariffType();
                        double priceMonth = Double.parseDouble(dialogEdit.getPriceMonth());
                        double pricePerMb = Double.parseDouble(dialogEdit.getPricePerMb());

                        if (priceMonth < 0 || pricePerMb < 0 || priceMonth > 1_000_000 || pricePerMb > 1000)
                            throw new NumberFormatException();

                        selectedTariff.setType(type);
                        selectedTariff.setPriceMonth(priceMonth);
                        selectedTariff.setPricePerMb(pricePerMb);

                        JOptionPane.showMessageDialog(frame, "Тариф обновлён.");
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame,
                                "Введите корректные положительные числа :(( (стоимость тарифа не больше 1_000_000 и стоимость за Мб не больше 1000)");
                    }
                }
            }
        });

        clientsBtn.addActionListener(e -> {
            Object[] options = {"Зарегистрировать пользователя", "Ввод трафика"};
            int res = JOptionPane.showOptionDialog(
                    frame,
                    "Выберите действие:",
                    "Клиенты",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]
            );
            if (res == 0) {
                if (provider.getTariffs().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Сначала добавьте тарифы.");
                    return;
                }

                JPanel panel = new JPanel(new GridLayout(4, 2));
                JTextField nameField = new JTextField();
                JComboBox<Tariff> tariffComboBox = new JComboBox<>();
                for (Tariff t : provider.getTariffs()) {
                    tariffComboBox.addItem(t);
                }
                JComboBox<String> strategyCombo = new JComboBox<>(new String[]{"Обычный клиент", "Клиент со скидкой 20%"});

                panel.add(new JLabel("Имя пользователя:"));
                panel.add(nameField);
                panel.add(new JLabel("Выберите тариф:"));
                panel.add(tariffComboBox);
                panel.add(new JLabel("Тип клиента:"));
                panel.add(strategyCombo);

                int result = JOptionPane.showConfirmDialog(frame, panel, "Регистрация пользователя",
                        JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    String name = nameField.getText().trim();
                    Tariff selectedTariff = (Tariff) tariffComboBox.getSelectedItem();
                    String strategy = (String) strategyCombo.getSelectedItem();

                    if (!name.isEmpty() && selectedTariff != null && strategy != null) {
                        CostStrategy costStrategy;
                        if (strategy.equals("Клиент со скидкой 20%")) {
                            costStrategy = new DiscountCostStrategy(0.2);
                        } else {
                            costStrategy = new NormalCostStrategy();
                        }
                        Client newClient = new Client(name, selectedTariff, costStrategy);
                        provider.addClient(newClient);
                        JOptionPane.showMessageDialog(frame, "Пользователь зарегистрирован.");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Введите все данные.");
                    }
                }
            } else if (res == 1) {
                if (provider.getClients().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Сначала зарегистрируйте пользователей.");
                    return;
                }

                JPanel panel = new JPanel(new GridLayout(3, 2));
                JComboBox<Client> userComboBox = new JComboBox<>();
                for (Client u : provider.getClients()) {
                    userComboBox.addItem(u);
                }
                JTextField trafficField = new JTextField();

                panel.add(new JLabel("Выберите пользователя:"));
                panel.add(userComboBox);
                panel.add(new JLabel("Введите потребленный трафик (Мбайт):"));
                panel.add(trafficField);

                int result = JOptionPane.showConfirmDialog(frame, panel, "Ввод трафика сверх заслуженного (с доплатой)",
                        JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    try {
                        Client selectedClient = (Client) userComboBox.getSelectedItem();
                        double traffic = Double.parseDouble(trafficField.getText());
                        if (traffic < 0) throw new NumberFormatException();
                        if (selectedClient != null) {
                            selectedClient.addTraffic(traffic);
                            JOptionPane.showMessageDialog(frame, "Трафик добавлен.");
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Введите корректное положительное число.");
                    }
                }
            }
        });

        reportsBtn.addActionListener(e -> {
            Object[] options = {"Подсчитать общую стоимость", "Найти клиента с максимальной оплатой"};
            int res = JOptionPane.showOptionDialog(
                    frame,
                    "Выберите действие:",
                    "Отчеты",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]
            );
            if (res == 0) {
                if (provider.getClients().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Нет зарегистрированных пользователей.");
                    return;
                }
                double totalCost = 0;
                for (Client u : provider.getClients()) {
                    totalCost += u.getCost();
                }
                JOptionPane.showMessageDialog(frame,
                        String.format("Общая стоимость всех пользователей: %.2f", totalCost));
            } else if (res == 1) {
                if (provider.getClients().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Нет зарегистрированных пользователей.");
                    return;
                }
                Client top = provider.findTopPayer();
                JOptionPane.showMessageDialog(frame,
                        "Клиент с максимальной оплатой:\n" + top.getName()
                                + "\nСтоимость: " + String.format("%.2f", top.getCost()));
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
