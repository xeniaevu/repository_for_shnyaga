public class NormalCostStrategy implements CostStrategy {
    @Override
    public double calculateCost(double mb, Tariff tariff) {
        return tariff.calculateCost(mb);
    }
}
