public class DiscountCostStrategy implements CostStrategy {
    private double discountRate;

    public DiscountCostStrategy(double discountRate) {
        this.discountRate = discountRate;
    }

    @Override
    public double calculateCost(double trafficMb, Tariff tariff) {
        double cost = tariff.calculateCost(trafficMb);
        return cost * (1 - discountRate);
    }
}