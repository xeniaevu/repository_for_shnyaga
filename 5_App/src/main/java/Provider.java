import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class Provider {

    private static final String URL = "jdbc:postgresql://localhost:5432/isp_provider";
    private static final String USER = "ksenapavlucenko"; // поменяй при необходимости
    private static final String PASSWORD = ""; // если нужен

    //добавить тариф
    public void addTariff(Tariff tariff) throws SQLException {
        String sql = "INSERT INTO tariffs (type, price_month, price_per_mb) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tariff.getType());
            stmt.setDouble(2, tariff.getPriceMonth());
            stmt.setDouble(3, tariff.getPricePerMb());
            stmt.executeUpdate();
        }
    }

    //получить все тарифы
    public List<Tariff> getTariffs() throws SQLException {
        List<Tariff> tariffs = new ArrayList<>();
        String sql = "SELECT id, type, price_month, price_per_mb FROM tariffs";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tariffs.add(new Tariff(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getDouble("price_month"),
                        rs.getDouble("price_per_mb")
                ));
            }
        }
        return tariffs;
    }

    //поправить тариф
    public void editTariff(Tariff tariff) throws SQLException {
        String sql = "UPDATE tariffs SET type=?, price_month=?, price_per_mb=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tariff.getType());
            stmt.setDouble(2, tariff.getPriceMonth());
            stmt.setDouble(3, tariff.getPricePerMb());
            stmt.setInt(4, tariff.getId());
            stmt.executeUpdate();
        }
    }

    //удалить тариф
    public void deleteTariff(int id) throws SQLException {
        String sql = "DELETE FROM tariffs WHERE id=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    //сортировать тарифы по цене
    public List<Tariff> getTariffsSortedByPrice() throws SQLException {
        List<Tariff> tariffs = new ArrayList<>();
        String sql = "SELECT id, type, price_month, price_per_mb FROM tariffs ORDER BY price_month ASC";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tariffs.add(new Tariff(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getDouble("price_month"),
                        rs.getDouble("price_perMb")  // если у тебя колонка именно price_per_mb, поправь на неё
                ));
            }
        }
        return tariffs;
    }

    //добавить клиента
    public void addClient(Client client) throws SQLException {
        String sql = "INSERT INTO clients (name, tariff_id, traffic_mb, cost_strategy) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, client.getName());
            stmt.setInt(2, client.getTariff().getId());
            stmt.setDouble(3, client.getTrafficMb());
            stmt.setString(4, client.getCostStrategy());
            stmt.executeUpdate();

            // При желании можем считать сгенерированный id и записать в объект
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    client.setId(keys.getInt(1));
                }
            }
        }
    }

    //получить всех клиентов
    public List<Client> getClients() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c.tariff_id, c.traffic_mb, c.cost_strategy, " +
                "t.type, t.price_month, t.price_per_mb " +
                "FROM clients c JOIN tariffs t ON c.tariff_id = t.id";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Tariff tariff = new Tariff(
                        rs.getInt("tariff_id"),
                        rs.getString("type"),
                        rs.getDouble("price_month"),
                        rs.getDouble("price_per_mb")
                );
                clients.add(new Client(
                        rs.getInt("id"),
                        rs.getString("name"),
                        tariff,
                        rs.getDouble("traffic_mb"),
                        rs.getString("cost_strategy")
                ));
            }
        }
        return clients;
    }

    //редактировать клиента
    public void editClient(Client client) throws SQLException {
        String sql = "UPDATE clients SET name=?, tariff_id=?, traffic_mb=?, cost_strategy=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, client.getName());
            stmt.setInt(2, client.getTariff().getId());
            stmt.setDouble(3, client.getTrafficMb());
            stmt.setString(4, client.getCostStrategy());
            stmt.setInt(5, client.getId());
            stmt.executeUpdate();
        }
    }

    //удалить клиента
    public void deleteClient(int id) throws SQLException {
        String sql = "DELETE FROM clients WHERE id=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    //сортировать клиентов по имени
    public List<Client> getClientsSortedByName() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c.tariff_id, c.traffic_mb, c.cost_strategy, " +
                "t.type, t.price_month, t.price_per_mb " +
                "FROM clients c JOIN tariffs t ON c.tariff_id = t.id ORDER BY c.name";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Tariff tariff = new Tariff(
                        rs.getInt("tariff_id"),
                        rs.getString("type"),
                        rs.getDouble("price_month"),
                        rs.getDouble("price_per_mb")
                );
                clients.add(new Client(
                        rs.getInt("id"),
                        rs.getString("name"),
                        tariff,
                        rs.getDouble("traffic_mb"),
                        rs.getString("cost_strategy")
                ));
            }
        }
        return clients;
    }

    //сортировать клиентов по оплате (по базовой формуле, без учёта скидки)
    public List<Client> getClientsSortedByCost() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c.tariff_id, c.traffic_mb, c.cost_strategy, " +
                "t.type, t.price_month, t.price_per_mb, " +
                "(t.price_month + c.traffic_mb * t.price_per_mb) AS total_cost " +
                "FROM clients c JOIN tariffs t ON c.tariff_id = t.id ORDER BY total_cost DESC";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Tariff tariff = new Tariff(
                        rs.getInt("tariff_id"),
                        rs.getString("type"),
                        rs.getDouble("price_month"),
                        rs.getDouble("price_per_mb")
                );
                clients.add(new Client(
                        rs.getInt("id"),
                        rs.getString("name"),
                        tariff,
                        rs.getDouble("traffic_mb"),
                        rs.getString("cost_strategy")
                ));
            }
        }
        return clients;
    }

    //найти клиента с максимальной оплатой (без учёта скидки)
    public Client findTopPayer() throws SQLException {
        String sql = "SELECT c.id, c.name, c.tariff_id, c.traffic_mb, c.cost_strategy, " +
                "t.type, t.price_month, t.price_per_mb, " +
                "(t.price_month + c.traffic_mb * t.price_per_mb) AS total_cost " +
                "FROM clients c JOIN tariffs t ON c.tariff_id = t.id ORDER BY total_cost DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                Tariff tariff = new Tariff(
                        rs.getInt("tariff_id"),
                        rs.getString("type"),
                        rs.getDouble("price_month"),
                        rs.getDouble("price_per_mb")
                );
                return new Client(
                        rs.getInt("id"),
                        rs.getString("name"),
                        tariff,
                        rs.getDouble("traffic_mb"),
                        rs.getString("cost_strategy")
                );
            }
        }
        return null;
    }

    //общая стоимость всех клиентов (по базовой формуле)
    public double getTotalCost() throws SQLException {
        String sql = "SELECT SUM(t.price_month + c.traffic_mb * t.price_per_mb) " +
                "FROM clients c JOIN tariffs t ON c.tariff_id = t.id";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    // ЭКСПОРТ / ИМПОРТ КЛИЕНТОВ В из CSV

    // Формат: id;name;tariff_id;traffic_mb;cost_strategy
    public void exportClientsToFile(File file) throws Exception {
        List<Client> clients = getClients(); // берём актуальные данные из БД

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {

            // заголовок
            writer.write("id;name;tariff_id;traffic_mb;cost_strategy");
            writer.newLine();

            for (Client c : clients) {
                String line = c.getId() + ";" +
                        escape(c.getName()) + ";" +
                        c.getTariff().getId() + ";" +
                        c.getTrafficMb() + ";" +
                        c.getCostStrategy();
                writer.write(line);
                writer.newLine();
            }
        }
    }

    //импорт клиентов из CSV-файла
    //тарифы НЕ создаём, используем существующие по tariff_id
    public void importClientsFromFile(File file) throws Exception {
        // заранее поднимем все тарифы в память
        List<Tariff> tariffs = getTariffs();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {

            String line = reader.readLine(); // пропускаем заголовок
            if (line == null) {
                return;
            }

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(";", -1);
                if (parts.length < 5) continue;


                // String idStr = parts[0];
                String name = unescape(parts[1]);
                int tariffId = Integer.parseInt(parts[2]);
                double trafficMb = Double.parseDouble(parts[3]);
                String costStrategy = parts[4];

                Tariff tariff = findTariffById(tariffs, tariffId);
                if (tariff == null) {
                    // если тарифа с таким id нет, можно пропустить запись или бросить исключение
                    // здесь просто пропустим
                    continue;
                }

                Client client = new Client(name, tariff, trafficMb, costStrategy);
                addClient(client);
            }
        }
    }

    //поиск тарифа в списке по id
    private Tariff findTariffById(List<Tariff> tariffs, int id) {
        for (Tariff t : tariffs) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    //экранирование ; и перевода строки в имени
    private String escape(String value) {
        if (value == null) return "";
        // заменяем ; и перевод строки, чтобы не ломать CSV
        String v = value.replace(";", ",");
        v = v.replace("\r", " ").replace("\n", " ");
        return v;
    }

    //обратное преобразование (сейчас у нас только замена ; -> , поэтому можно вернуть как есть)
    private String unescape(String value) {
        if (value == null) return "";
        return value;
    }
}
