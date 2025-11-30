public class Client {
    private int id;                // id из базы
    private String name;           // имя клиента
    private Tariff tariff;         // объект тарифа
    private double trafficMb;      // трафик
    private String costStrategy;   // стратегия расчёта ("NORMAL", "DISCOUNT_20" и т.п.)

    // Конструктор для загрузки из БД
    public Client(int id, String name, Tariff tariff, double trafficMb, String costStrategy) {
        this.id = id;
        this.name = name;
        this.tariff = tariff;
        this.trafficMb = trafficMb;
        this.costStrategy = costStrategy;
    }

    // Конструктор для создания нового клиента (без id)
    public Client(String name, Tariff tariff, double trafficMb, String costStrategy) {
        this.name = name;
        this.tariff = tariff;
        this.trafficMb = trafficMb;
        this.costStrategy = costStrategy;
    }

    //геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Tariff getTariff() { return tariff; }
    public void setTariff(Tariff tariff) { this.tariff = tariff; }

    public double getTrafficMb() { return trafficMb; }
    public void setTrafficMb(double trafficMb) { this.trafficMb = trafficMb; }

    public String getCostStrategy() { return costStrategy; }
    public void setCostStrategy(String costStrategy) { this.costStrategy = costStrategy; }

    //метод для расчёта стоимост
    public double getCost() {
        if ("DISCOUNT_20".equals(costStrategy)) {
            return 0.8 * (tariff.getPriceMonth() + trafficMb * tariff.getPricePerMb());
        }
        return tariff.getPriceMonth() + trafficMb * tariff.getPricePerMb();
    }

    @Override
    public String toString() {
        return name + " (Трафик: " + trafficMb + " Мбайт, Стоимость: " + getCost() + ")";
    }
}
