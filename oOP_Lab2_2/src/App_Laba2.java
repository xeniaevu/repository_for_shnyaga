import javax.swing.*;
import java.awt.*;



public class App_Laba2 {
    private static Provider provider = new Provider();

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        //настройка основного окошка
        JFrame frame = new JFrame("Internet Provider System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.getContentPane().setBackground(new Color(255, 192, 203)); // розови

        //создаем главное меню
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(0, 0, 0));

        //меню Тарифы
        JMenu tariffsMenu = new JMenu("Тарифы");
        tariffsMenu.setForeground(new Color(255, 192, 203));
        JMenuItem addTariffItem = new JMenuItem("Ввести тариф");
        addTariffItem.setBackground(new Color(0, 0, 0));
        addTariffItem.setForeground(new Color(255, 192, 203));

        tariffsMenu.add(addTariffItem);

        //меню Клиенты
        JMenu clientsMenu = new JMenu("Клиенты");
        clientsMenu.setForeground(new Color(255, 192, 203));
        JMenuItem registerUserItem = new JMenuItem("Зарегистрировать пользователя");
        registerUserItem.setBackground(new Color(0, 0, 0));
        registerUserItem.setForeground(new Color(255, 192, 203));
        JMenuItem inputTrafficItem = new JMenuItem("Ввод трафика сверх заслуженного (с доплатой за превышение тарифа)");
        inputTrafficItem.setBackground(new Color(0, 0, 0));
        inputTrafficItem.setForeground(new Color(255, 192, 203));

        clientsMenu.add(registerUserItem);
        clientsMenu.add(inputTrafficItem);

        //меню Отчеты
        JMenu reportsMenu = new JMenu("Отчеты");
        reportsMenu.setForeground(new Color(255, 192, 203));
        JMenuItem calculateCostItem = new JMenuItem("Подсчитать общую стоимость");
        calculateCostItem.setBackground(new Color(0, 0, 0));
        calculateCostItem.setForeground(new Color(255, 192, 203));
        JMenuItem findTopPayerItem = new JMenuItem("Найти клиента с максимальной оплатой");
        findTopPayerItem.setBackground(new Color(0, 0, 0));
        findTopPayerItem.setForeground(new Color(255, 192, 203));

        reportsMenu.add(calculateCostItem);
        reportsMenu.add(findTopPayerItem);

        menuBar.add(tariffsMenu);
        menuBar.add(clientsMenu);
        menuBar.add(reportsMenu);

        frame.setJMenuBar(menuBar);




        //обработчики событий, что происходит при клике
        addTariffItem.addActionListener(e -> {
            JPanel panel = new JPanel(new GridLayout(4, 2));
            JComboBox<TariffType> tariffTypeCombo = new JComboBox<>(TariffType.values());
            JTextField priceMounthField = new JTextField();
            JTextField pricePerMbField = new JTextField();

            panel.add(new JLabel("Выберите тип тарифа:"));
            panel.add(tariffTypeCombo);
            panel.add(new JLabel("Абонентская плата:"));
            panel.add(priceMounthField);
            panel.add(new JLabel("Цена за 1 Мбайт:"));
            panel.add(pricePerMbField);

            int result = JOptionPane.showConfirmDialog(frame, panel, "Ввод тарифа",
                    JOptionPane.OK_CANCEL_OPTION);//окей или не окей

            if (result == JOptionPane.OK_OPTION) {
                try {
                    TariffType type = (TariffType) tariffTypeCombo.getSelectedItem();
                    double priceMounth = Double.parseDouble(priceMounthField.getText());
                    double pricePerMb = Double.parseDouble(pricePerMbField.getText());
                    if (priceMounth < 0 || pricePerMb < 0 || priceMounth > 1_000_000 || pricePerMb > 1000) throw new NumberFormatException();
                    Tariff t = new Tariff(type, priceMounth, pricePerMb);
                    provider.addTariff(t);
                    JOptionPane.showMessageDialog(frame, "Тариф добавлен.");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Введите корректные положительные числа :(( (стоимость тарифа не боьше 1_000_000 и стоимость за Мб не больше 1000)");
                }
            }
        });

        registerUserItem.addActionListener(e -> {
            if (provider.getTariffs().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Сначала добавьте тарифы.");
                return;
            }

            JPanel panel = new JPanel(new GridLayout(3, 2));
            JTextField nameField = new JTextField();
            JComboBox<Tariff> tariffComboBox = new JComboBox<>();
            for (Tariff t : provider.getTariffs()) {
                tariffComboBox.addItem(t);
            }

            panel.add(new JLabel("Имя пользователя:"));
            panel.add(nameField);
            panel.add(new JLabel("Выберите тариф:"));
            panel.add(tariffComboBox);

            int result = JOptionPane.showConfirmDialog(frame, panel, "Регистрация пользователя",
                    JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                Tariff selectedTariff = (Tariff) tariffComboBox.getSelectedItem();

                if (!name.isEmpty() && selectedTariff != null) {
                    Client newUser = new Client(name, selectedTariff);
                    provider.addClient(newUser);
                    JOptionPane.showMessageDialog(frame, "Пользователь зарегистрирован.");
                } else {
                    JOptionPane.showMessageDialog(frame, "Введите все данные.");
                }
            }
        });

        inputTrafficItem.addActionListener(e -> {
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

            int result = JOptionPane.showConfirmDialog(frame, panel, "Ввод трафика сверх заслуженного (с доплатой за превышение тарифа)",
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
        });

        calculateCostItem.addActionListener(e -> {
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
        });

        findTopPayerItem.addActionListener(e -> {
            if (provider.getClients().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Нет зарегистрированных пользователей.");
                return;
            }
            Client top = provider.findTopPayer();
            JOptionPane.showMessageDialog(frame,
                    "Клиент с максимальной оплатой:\n" + top.getName()
                            + "\nСтоимость: " + String.format("%.2f", top.getCost()));
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
