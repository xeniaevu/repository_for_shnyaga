import java.io.Serializable;

public class Client implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private double trafficMb;
    private Tariff tariff;
    private CostStrategy costStrategy;

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

    public double getTrafficMb() {
        return trafficMb;
    }

    public Tariff getTariff() {
        return tariff;
    }

    public CostStrategy getCostStrategy() {
        return costStrategy;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTariff(Tariff tariff) {
        this.tariff = tariff;
    }

    public void setCostStrategy(CostStrategy costStrategy) {
        this.costStrategy = costStrategy;
    }

    @Override
    public String toString() {
        return name + " (Трафик: " + trafficMb + " Мбайт, Стоимость: " + getCost() + ")";
    }
}
