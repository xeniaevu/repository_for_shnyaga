package lifesim.model;

/**
 * Растение: не двигается, получает энергию от «солнца» (+n за ход, до максимума)
 * и размножается вегетативно в соседние свободные клетки.
 */
public class Plant extends Agent {

    public Plant(int energy) {
        super(energy);
    }

    @Override
    protected void act(Simulation sim) {
        // Растение ничего не делает активно — только растёт (см. metabolize).
    }



    @Override
    protected int getEnergyChangePerStep(SimulationConfig config) {
        return config.getPlantGrowth();
    }

    @Override
    protected int getReproductionThreshold(SimulationConfig config) {
        return config.getPlantReproductionThreshold();
    }

    @Override
    protected int getChildEnergy(SimulationConfig config) {
        return config.getPlantChildEnergy();
    }

    @Override
    protected Agent createChild(int energy) {
        return new Plant(energy);
    }

    @Override
    public char getSymbol() { return 'P'; }

    @Override
    public String getSpeciesName() { return "Растение"; }
}
