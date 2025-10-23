public interface CostStrategy {
    double calculateCost(double trafficMb, Tariff tariff);
}
