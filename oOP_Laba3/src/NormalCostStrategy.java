public class NormalCostStrategy implements CostStrategy {
    @Override
    public double calculateCost(double trafficMb, Tariff tariff) {
        return tariff.calculateCost(trafficMb);
    }
}


