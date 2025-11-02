public class NormalCostStrategy implements CostStrategy {
    private static final long serialVersionUID = 1L;

    @Override
    public double calculateCost(double mb, Tariff tariff) {
        return tariff.calculateCost(mb);
    }
}
