import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class App_Laba4 {
    private static Provider provider;

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        JFrame frame = new JFrame("Internet Provider System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(850, 500);
        frame.getContentPane().setBackground(new Color(110, 70, 131));

        // Загрузка данных
        try {
            provider = Provider.loadFromFile("data.ser");
        } catch (Exception e) {
            provider = new Provider();
        }

        // Первая панель — кнопки по тарифам
        JPanel topPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel1.setBackground(Color.BLACK);
        JButton tariffsBtn = new JButton("Тарифы");
        JButton editTariffBtn = new JButton("Редактировать тариф");
        JButton deleteTariffBtn = new JButton("Удалить тариф");
        JButton sortTariffsBtn = new JButton("Сортировать тарифы по цене");
        JButton[] tariffButtons = {tariffsBtn, editTariffBtn, deleteTariffBtn, sortTariffsBtn};
        for (JButton b : tariffButtons) {
            b.setBackground(Color.WHITE);
            b.setForeground(new Color(110, 70, 131));
            topPanel1.add(b);
        }

        // Вторая панель — кнопки по клиентам
        JPanel topPanelClients = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanelClients.setBackground(Color.BLACK);
        JButton clientsBtn = new JButton("Клиенты");
        JButton showClientsBtn = new JButton("Показать всех клиентов");
        JButton editClientBtn = new JButton("Редактировать клиента");
        JButton deleteClientBtn = new JButton("Удалить клиента");
        JButton sortClientsByNameBtn = new JButton("Сортировать клиентов по имени");
        JButton sortClientsByCostBtn = new JButton("Сортировать клиентов по оплате");
        JButton[] clientButtons = {clientsBtn, showClientsBtn, editClientBtn, deleteClientBtn, sortClientsByNameBtn, sortClientsByCostBtn};
        for (JButton b : clientButtons) {
            b.setBackground(Color.WHITE);
            b.setForeground(new Color(110, 70, 131));
            topPanelClients.add(b);
        }

        // Третья панель — отчёты, сохранение, откат
        JPanel topPanel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel3.setBackground(Color.BLACK);
        JButton reportsBtn = new JButton("Отчеты");
        JButton saveBtn = new JButton("Сохранить данные");
        JButton rollbackBtn = new JButton("Откатить до последнего коммита");
        JButton[] thirdRowButtons = {reportsBtn, saveBtn, rollbackBtn};
        for (JButton b : thirdRowButtons) {
            b.setBackground(Color.WHITE);
            b.setForeground(new Color(110, 70, 131));
            topPanel3.add(b);
        }

        // Общая панель с вертикальной компоновкой
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(topPanel1);
        topPanel.add(topPanelClients);
        topPanel.add(topPanel3);

        frame.add(topPanel, BorderLayout.NORTH);

        // Логика кнопок

        saveBtn.addActionListener(e -> {
            try {
                provider.saveToFile("data.ser");
                JOptionPane.showMessageDialog(frame, "Данные успешно сохранены");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка сохранения данных: " + ex.getMessage());
            }
        });

        rollbackBtn.addActionListener(e -> {
            try {
                provider = Provider.loadFromFile("data.ser");
                JOptionPane.showMessageDialog(frame, "Данные восстановлены до последнего коммита.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка восстановления: " + ex.getMessage());
            }
        });

        tariffsBtn.addActionListener(e -> {
            TariffDialog dialog = new TariffDialog(frame, "Добавить тариф", new Color(230, 230, 250));
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
                    "Выберите тариф для редактирования:", "Редактировать тариф",
                    JOptionPane.PLAIN_MESSAGE, null, provider.getTariffs().toArray(), null);
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

        deleteTariffBtn.addActionListener(e -> {
            if (provider.getTariffs().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Нет тарифов для удаления.");
                return;
            }
            Tariff selectedTariff = (Tariff) JOptionPane.showInputDialog(frame,
                    "Выберите тариф для удаления:", "Удалить тариф",
                    JOptionPane.PLAIN_MESSAGE, null, provider.getTariffs().toArray(), null);
            if (selectedTariff != null) {
                provider.getTariffs().remove(selectedTariff);
                JOptionPane.showMessageDialog(frame, "Тариф удалён.");
            }
        });

        sortTariffsBtn.addActionListener(e -> {
            provider.sortTariffsByPrice();
            JOptionPane.showMessageDialog(frame, "Тарифы отсортированы по абонентской плате.");
        });

        clientsBtn.addActionListener(e -> {
            Object[] options = {"Зарегистрировать пользователя", "Ввод трафика"};
            int res = JOptionPane.showOptionDialog(frame, "Выберите действие:", "Клиенты",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (res == 0) {
                if (provider.getTariffs().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Сначала добавьте тарифы.");
                    return;
                }
                JPanel panel = new JPanel(new GridLayout(4, 2));
                JTextField nameField = new JTextField();
                JComboBox<Tariff> tariffComboBox = new JComboBox<>();
                for (Tariff t : provider.getTariffs()) tariffComboBox.addItem(t);
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
                        if (strategy.equals("Клиент со скидкой 20%")) costStrategy = new DiscountCostStrategy(0.2);
                        else costStrategy = new NormalCostStrategy();
                        Client newClient = new Client(name, selectedTariff, costStrategy);
                        provider.addClient(newClient);
                        JOptionPane.showMessageDialog(frame, "Пользователь зарегистрирован.");
                    } else JOptionPane.showMessageDialog(frame, "Введите все данные.");
                }
            } else if (res == 1) {
                if (provider.getClients().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Сначала зарегистрируйте пользователей.");
                    return;
                }
                JPanel panel = new JPanel(new GridLayout(3, 2));
                JComboBox<Client> userComboBox = new JComboBox<>();
                for (Client u : provider.getClients()) userComboBox.addItem(u);
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

        showClientsBtn.addActionListener(e -> {
            if (provider.getClients().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Нет клиентов для отображения.");
                return;
            }
            StringBuilder clientsText = new StringBuilder();
            for (Client c : provider.getClients()) {
                clientsText.append(c.toString()).append("\n");
            }
            JTextArea area = new JTextArea(clientsText.toString());
            area.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(area);
            scrollPane.setPreferredSize(new Dimension(400, 200));
            JOptionPane.showMessageDialog(frame, scrollPane, "Список клиентов", JOptionPane.INFORMATION_MESSAGE);
        });

        editClientBtn.addActionListener(e -> {
            if (provider.getClients().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Нет клиентов для редактирования.");
                return;
            }
            Client selectedClient = (Client) JOptionPane.showInputDialog(frame,
                    "Выберите клиента для редактирования:", "Редактировать клиента",
                    JOptionPane.PLAIN_MESSAGE, null, provider.getClients().toArray(), null);
            if (selectedClient != null) {
                JPanel panel = new JPanel(new GridLayout(4, 2));
                JTextField nameField = new JTextField(selectedClient.getName());
                JComboBox<Tariff> tariffComboBox = new JComboBox<>();
                for (Tariff t : provider.getTariffs()) tariffComboBox.addItem(t);
                tariffComboBox.setSelectedItem(selectedClient.getTariff());
                JComboBox<String> strategyCombo = new JComboBox<>(new String[]{"Обычный клиент", "Клиент со скидкой 20%"});

                boolean hasDiscount = selectedClient.getCost() < selectedClient.getTariff().calculateCost(selectedClient.getTrafficMb());
                strategyCombo.setSelectedIndex(hasDiscount ? 1 : 0);

                panel.add(new JLabel("Имя пользователя:"));
                panel.add(nameField);
                panel.add(new JLabel("Выберите тариф:"));
                panel.add(tariffComboBox);
                panel.add(new JLabel("Тип клиента:"));
                panel.add(strategyCombo);
                int result = JOptionPane.showConfirmDialog(frame, panel, "Редактировать клиента", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    String name = nameField.getText().trim();
                    Tariff newTariff = (Tariff) tariffComboBox.getSelectedItem();
                    String strategy = (String) strategyCombo.getSelectedItem();
                    if (!name.isEmpty() && newTariff != null && strategy != null) {
                        selectedClient.setName(name);
                        selectedClient.setTariff(newTariff);
                        if (strategy.equals("Клиент со скидкой 20%"))
                            selectedClient.setCostStrategy(new DiscountCostStrategy(0.2));
                        else
                            selectedClient.setCostStrategy(new NormalCostStrategy());
                        JOptionPane.showMessageDialog(frame, "Данные клиента обновлены.");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Введите все данные.");
                    }
                }
            }
        });

        deleteClientBtn.addActionListener(e -> {
            if (provider.getClients().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Нет клиентов для удаления.");
                return;
            }
            Client selectedClient = (Client) JOptionPane.showInputDialog(frame,
                    "Выберите клиента для удаления:", "Удалить клиента",
                    JOptionPane.PLAIN_MESSAGE, null, provider.getClients().toArray(), null);
            if (selectedClient != null) {
                provider.getClients().remove(selectedClient);
                JOptionPane.showMessageDialog(frame, "Клиент удалён.");
            }
        });

        sortClientsByNameBtn.addActionListener(e -> {
            provider.sortClientsByName();
            JOptionPane.showMessageDialog(frame, "Клиенты отсортированы по имени.");
        });

        sortClientsByCostBtn.addActionListener(e -> {
            provider.sortClientsByCost();
            JOptionPane.showMessageDialog(frame, "Клиенты отсортированы по размеру оплаты.");
        });

        reportsBtn.addActionListener(e -> {
            Object[] options = {"Подсчитать общую стоимость", "Найти клиента с максимальной оплатой"};
            int res = JOptionPane.showOptionDialog(frame, "Выберите действие:", "Отчеты",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (res == 0) {
                if (provider.getClients().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Нет зарегистрированных пользователей.");
                    return;
                }
                double totalCost = 0;
                for (Client u : provider.getClients())
                    totalCost += u.getCost();
                JOptionPane.showMessageDialog(frame,
                        String.format("Общая стоимость всех пользователей: %.2f", totalCost));
            } else if (res == 1) {
                if (provider.getClients().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Нет зарегистрированных пользователей.");
                    return;
                }
                Client top = provider.findTopPayer();
                JOptionPane.showMessageDialog(frame,
                        "Клиент с максимальной оплатой:\n" + top.getName() +
                                "\nСтоимость: " + String.format("%.2f", top.getCost()));
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
