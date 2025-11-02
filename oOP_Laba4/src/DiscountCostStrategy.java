public class DiscountCostStrategy implements CostStrategy {
    private static final long serialVersionUID = 1L;

    private double discount;

    public DiscountCostStrategy(double discount) {
        this.discount = discount;
    }

    @Override
    public double calculateCost(double mb, Tariff tariff) {
        return (1 - discount) * tariff.calculateCost(mb);
    }
}
