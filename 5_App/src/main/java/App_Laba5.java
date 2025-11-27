import javax.swing.*;
import java.awt.*;

public class App_Laba5 {
    private static Provider provider;

    public static void main(String[] args) {
        // Современная палитра
        Color bgMain = new Color(32, 43, 61);
        Color panelBg = new Color(56, 68, 94);
        Color buttonBg = new Color(230, 236, 241);
        Color buttonText = new Color(32, 43, 61);
        Color headerText = new Color(255, 255, 255);

        JFrame frame = new JFrame("Internet Provider System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 540);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(bgMain);

        // --- Provider управляет через БД ---
        provider = new Provider();

        // Шапка
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(bgMain);
        JLabel header = new JLabel("Internet Provider System");
        header.setForeground(headerText);
        header.setFont(new Font("Arial", Font.BOLD, 28));
        headerPanel.add(header);

        // --- Панель ТАРИФОВ ---
        JPanel tariffPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tariffPanel.setBackground(panelBg);
        JButton tariffsBtn = new JButton("Тарифы");
        JButton editTariffBtn = new JButton("Редактировать тариф");
        JButton deleteTariffBtn = new JButton("Удалить тариф");
        JButton sortTariffsBtn = new JButton("Сортировать тарифы по цене");
        addStyled(tariffPanel, buttonBg, buttonText, tariffsBtn, editTariffBtn, deleteTariffBtn, sortTariffsBtn);

        // --- Панель клиентов ---
        JPanel clientPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        clientPanel.setBackground(panelBg);
        JButton clientsBtn = new JButton("Клиенты");
        JButton showClientsBtn = new JButton("Показать всех клиентов");
        JButton editClientBtn = new JButton("Редактировать клиента");
        JButton deleteClientBtn = new JButton("Удалить клиента");
        JButton sortClientsByNameBtn = new JButton("Сортировать клиентов по имени");
        JButton sortClientsByCostBtn = new JButton("Сортировать клиентов по оплате");
        addStyled(clientPanel, buttonBg, buttonText, clientsBtn, showClientsBtn, editClientBtn, deleteClientBtn, sortClientsByNameBtn, sortClientsByCostBtn);

        // --- Панель отчётов ---
        JPanel reportsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportsPanel.setBackground(panelBg);
        JButton reportsBtn = new JButton("Отчеты");
        JButton saveBtn = new JButton("Сохранить данные");
        JButton rollbackBtn = new JButton("Откатить до последнего коммита");
        addStyled(reportsPanel, buttonBg, buttonText, reportsBtn, saveBtn, rollbackBtn);

        // --- Верх ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(bgMain);
        topPanel.add(tariffPanel);
        topPanel.add(clientPanel);
        topPanel.add(reportsPanel);

        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(topPanel, BorderLayout.CENTER);

        // --------- ОБРАБОТЧИКИ -------------
        tariffsBtn.addActionListener(e -> {
            TariffDialog dialog = new TariffDialog(frame, "Добавить тариф", new Color(230, 230, 250));
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                try {
                    String type = dialog.getSelectedTariffType().name();
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
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
                }
            }
        });

        editTariffBtn.addActionListener(e -> {
            try {
                java.util.List<Tariff> tariffs = provider.getTariffs();
                if (tariffs.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Сначала добавьте тарифы.");
                    return;
                }
                Tariff selectedTariff = (Tariff) JOptionPane.showInputDialog(frame,
                        "Выберите тариф для редактирования:", "Редактировать тариф",
                        JOptionPane.PLAIN_MESSAGE, null, tariffs.toArray(), null);
                if (selectedTariff != null) {
                    TariffDialog dialogEdit = new TariffDialog(frame, "Редактировать тариф", new Color(255, 255, 200), selectedTariff);
                    dialogEdit.setVisible(true);
                    if (dialogEdit.isConfirmed()) {
                        String type = dialogEdit.getSelectedTariffType().name();
                        double priceMonth = Double.parseDouble(dialogEdit.getPriceMonth());
                        double pricePerMb = Double.parseDouble(dialogEdit.getPricePerMb());
                        if (priceMonth < 0 || pricePerMb < 0 || priceMonth > 1_000_000 || pricePerMb > 1000)
                            throw new NumberFormatException();
                        Tariff t = new Tariff(selectedTariff.getId(), type, priceMonth, pricePerMb);
                        provider.editTariff(t);
                        JOptionPane.showMessageDialog(frame, "Тариф обновлён.");
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
            }
        });

        deleteTariffBtn.addActionListener(e -> {
            try {
                java.util.List<Tariff> tariffs = provider.getTariffs();
                if (tariffs.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Нет тарифов для удаления.");
                    return;
                }
                Tariff selectedTariff = (Tariff) JOptionPane.showInputDialog(frame,
                        "Выберите тариф для удаления:", "Удалить тариф",
                        JOptionPane.PLAIN_MESSAGE, null, tariffs.toArray(), null);
                if (selectedTariff != null) {
                    provider.deleteTariff(selectedTariff.getId());
                    JOptionPane.showMessageDialog(frame, "Тариф удалён.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
            }
        });

        sortTariffsBtn.addActionListener(e -> {
            try {
                java.util.List<Tariff> tariffs = provider.getTariffsSortedByPrice();
                JOptionPane.showMessageDialog(frame, "Тарифы отсортированы:\n" + tariffs);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
            }
        });

        clientsBtn.addActionListener(e -> {
            Object[] options = {"Зарегистрировать пользователя", "Ввод трафика"};
            int res = JOptionPane.showOptionDialog(frame, "Выберите действие:", "Клиенты",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (res == 0) {
                try {
                    java.util.List<Tariff> tariffs = provider.getTariffs();
                    if (tariffs.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Сначала добавьте тарифы.");
                        return;
                    }
                    JPanel panel = new JPanel(new GridLayout(4, 2));
                    JTextField nameField = new JTextField();
                    JComboBox<Tariff> tariffComboBox = new JComboBox<>();
                    for (Tariff t : tariffs) tariffComboBox.addItem(t);
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
                            String costStrategy = strategy.equals("Клиент со скидкой 20%") ? "DISCOUNT_20" : "NORMAL";
                            Client newClient = new Client(name, selectedTariff, 0, costStrategy);
                            provider.addClient(newClient);
                            JOptionPane.showMessageDialog(frame, "Пользователь зарегистрирован.");
                        } else JOptionPane.showMessageDialog(frame, "Введите все данные.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
                }
            } else if (res == 1) {
                try {
                    java.util.List<Client> clients = provider.getClients();
                    if (clients.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "Сначала зарегистрируйте пользователей.");
                        return;
                    }
                    JPanel panel = new JPanel(new GridLayout(3, 2));
                    JComboBox<Client> userComboBox = new JComboBox<>();
                    for (Client u : clients) userComboBox.addItem(u);
                    JTextField trafficField = new JTextField();
                    panel.add(new JLabel("Выберите пользователя:"));
                    panel.add(userComboBox);
                    panel.add(new JLabel("Введите потребленный трафик (Мбайт):"));
                    panel.add(trafficField);
                    int result = JOptionPane.showConfirmDialog(frame, panel, "Ввод трафика сверх заслуженного (с доплатой)",
                            JOptionPane.OK_CANCEL_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                        Client selectedClient = (Client) userComboBox.getSelectedItem();
                        double traffic = Double.parseDouble(trafficField.getText());
                        if (traffic < 0) throw new NumberFormatException();
                        if (selectedClient != null) {
                            selectedClient.setTrafficMb(selectedClient.getTrafficMb() + traffic);
                            provider.editClient(selectedClient);
                            JOptionPane.showMessageDialog(frame, "Трафик добавлен.");
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Введите корректное положительное число.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
                }
            }
        });

        showClientsBtn.addActionListener(e -> {
            try {
                java.util.List<Client> clients = provider.getClients();
                if (clients.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Нет клиентов для отображения.");
                    return;
                }
                StringBuilder clientsText = new StringBuilder();
                for (Client c : clients) {
                    clientsText.append(c.toString()).append("\n");
                }
                JTextArea area = new JTextArea(clientsText.toString());
                area.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(area);
                scrollPane.setPreferredSize(new Dimension(600, 200));
                JOptionPane.showMessageDialog(frame, scrollPane, "Список клиентов", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
            }
        });

        editClientBtn.addActionListener(e -> {
            try {
                java.util.List<Client> clients = provider.getClients();
                java.util.List<Tariff> tariffs = provider.getTariffs();
                if (clients.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Нет клиентов для редактирования.");
                    return;
                }
                Client selectedClient = (Client) JOptionPane.showInputDialog(frame,
                        "Выберите клиента для редактирования:", "Редактировать клиента",
                        JOptionPane.PLAIN_MESSAGE, null, clients.toArray(), null);
                if (selectedClient != null) {
                    JPanel panel = new JPanel(new GridLayout(4, 2));
                    JTextField nameField = new JTextField(selectedClient.getName());
                    JComboBox<Tariff> tariffComboBox = new JComboBox<>();
                    for (Tariff t : tariffs) tariffComboBox.addItem(t);
                    tariffComboBox.setSelectedItem(selectedClient.getTariff());
                    JComboBox<String> strategyCombo = new JComboBox<>(new String[]{"Обычный клиент", "Клиент со скидкой 20%"});
                    strategyCombo.setSelectedItem(selectedClient.getCostStrategy().equals("DISCOUNT_20") ? "Клиент со скидкой 20%" : "Обычный клиент");

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
                            selectedClient.setCostStrategy(strategy.equals("Клиент со скидкой 20%") ? "DISCOUNT_20" : "NORMAL");
                            provider.editClient(selectedClient);
                            JOptionPane.showMessageDialog(frame, "Данные клиента обновлены.");
                        } else {
                            JOptionPane.showMessageDialog(frame, "Введите все данные.");
                        }
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
            }
        });

        deleteClientBtn.addActionListener(e -> {
            try {
                java.util.List<Client> clients = provider.getClients();
                if (clients.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Нет клиентов для удаления.");
                    return;
                }
                Client selectedClient = (Client) JOptionPane.showInputDialog(frame,
                        "Выберите клиента для удаления:", "Удалить клиента",
                        JOptionPane.PLAIN_MESSAGE, null, clients.toArray(), null);
                if (selectedClient != null) {
                    provider.deleteClient(selectedClient.getId());
                    JOptionPane.showMessageDialog(frame, "Клиент удалён.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
            }
        });

        sortClientsByNameBtn.addActionListener(e -> {
            try {
                java.util.List<Client> clients = provider.getClientsSortedByName();
                JOptionPane.showMessageDialog(frame, "Клиенты отсортированы по имени:\n" + clients);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
            }
        });

        sortClientsByCostBtn.addActionListener(e -> {
            try {
                java.util.List<Client> clients = provider.getClientsSortedByCost();
                JOptionPane.showMessageDialog(frame, "Клиенты отсортированы по оплате:\n" + clients);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
            }
        });

        reportsBtn.addActionListener(e -> {
            Object[] options = {"Подсчитать общую стоимость", "Найти клиента с максимальной оплатой"};
            int res = JOptionPane.showOptionDialog(frame, "Выберите действие:", "Отчеты",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            try {
                if (res == 0) {
                    double totalCost = provider.getTotalCost();
                    JOptionPane.showMessageDialog(frame,
                            String.format("Общая стоимость всех пользователей: %.2f", totalCost));
                } else if (res == 1) {
                    Client top = provider.findTopPayer();
                    if (top != null) {
                        JOptionPane.showMessageDialog(frame,
                                "Клиент с максимальной оплатой:\n" + top.getName() +
                                        "\nСтоимость: " + String.format("%.2f", top.getCost()));
                    } else {
                        JOptionPane.showMessageDialog(frame, "Нет зарегистрированных пользователей.");
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Ошибка: " + ex.getMessage());
            }
        });

        // --- Эти кнопки реализуйте, если нужна физическая сериализация, на БД их можно убрать ---
        saveBtn.setVisible(false);     // отключено для БД
        rollbackBtn.setVisible(false);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void addStyled(JPanel panel, Color bg, Color fg, JButton... buttons) {
        for (JButton b : buttons) {
            b.setBackground(bg);
            b.setForeground(fg);
            b.setFont(new Font("Arial", Font.BOLD, 15));
            b.setFocusPainted(false);
            b.setPreferredSize(new Dimension(500, 36));
            panel.add(b);
        }
    }
}
