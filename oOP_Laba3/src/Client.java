public class Client {
    private String name;
    private double trafficMb;
    private Tariff tariff;
    private CostStrategy costStrategy; // стратегия подсчёта стоимости

    public Client(String name, Tariff tariff, CostStrategy costStrategy) {
        this.name = name;
        this.tariff = tariff;
        this.costStrategy = costStrategy;
        this.trafficMb = 0;
    }

    public void addTraffic(double mb) {
        this.trafficMb += mb;
    }

    public double getCost() {
        return costStrategy.calculateCost(trafficMb, tariff);
    }

    public String getName() {
        return name;
    }

    public Tariff getTariff() {
        return tariff;
    }

    public double getTrafficMb() {
        return trafficMb;
    }

    @Override
    public String toString() {
        return name + " (Трафик: " + trafficMb + " Мбайт, Стоимость: " + getCost() + ")";
    }
}
