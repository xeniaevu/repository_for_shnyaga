public class DiscountCostStrategy implements CostStrategy {
    private double discount; // например, 0.2 для 20% скидки

    public DiscountCostStrategy(double discount) {
        this.discount = discount;
    }

    @Override
    public double calculateCost(double mb, Tariff tariff) {
        return (1 - discount) * tariff.calculateCost(mb);
    }
}
