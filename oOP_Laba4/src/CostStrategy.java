import java.io.Serializable;

public interface CostStrategy extends Serializable {
    double calculateCost(double mb, Tariff tariff);
}
