public class Client {

    private String name;
    private double trafficMb;
    private Tariff tariff;


    public Client(String name, Tariff tariff) {
        this.name = name;
        this.tariff = tariff;
        this.trafficMb = 0;
    }

    public void addTraffic(double mb) {
        this.trafficMb += mb;
    }


    public double getCost() {
        return tariff.calculateCost(trafficMb);
    }


    public String getName() { return name; }

    public Tariff getTariff() { return tariff; }

    public double getTrafficMb() { return trafficMb; }

    @Override
    public String toString() {
        return name + " (Трафик: " + trafficMb + " Мбайт, Стоимость: " + getCost() + ")";
    }
}

