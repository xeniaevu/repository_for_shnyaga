public class Tariff {
    private TariffType type;
    private double priceMounth;
    private double pricePerMb;

    public Tariff(TariffType type, double priceMounth, double pricePerMb) {
        this.type = type;
        this.priceMounth = priceMounth;
        this.pricePerMb = pricePerMb;
    }

    public double calculateCost(double mbUsed) {
        return priceMounth + mbUsed * pricePerMb;
    }

    public TariffType getType() {
        return type;
    }

    public double getpriceMounth() {
        return priceMounth;
    }

    public double getPricePerMb() {
        return pricePerMb;
    }

    @Override
    public String toString() {
        return type + " (Абонплата: " + priceMounth + ", Цена/Мбайт: " + pricePerMb + ")";
    }
}
