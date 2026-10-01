package lifesim.model;

import java.util.List;

public class Predator extends Animal {

    public Predator(int energy) {
        super(energy);
    }

    @Override
    protected void act(Simulation sim) {
        Herbivore prey = findAdjacent(sim, Herbivore.class);
        if (prey != null) {
            eat(sim, prey, sim.getConfig().getPredatorMeatGain());
            return;
        }

        List<Herbivore> visible = filter(look(sim), Herbivore.class);
        Herbivore target = nearest(visible, sim.getRandom());
        if (target != null && stepTowards(sim, target.getPosition())) {
            return;
        }
        wander(sim);
    }


    @Override
    protected int getEnergyChangePerStep(SimulationConfig config) {
        return -config.getPredatorMetabolism();
    }

    @Override
    protected int getReproductionThreshold(SimulationConfig config) {
        return config.getPredatorReproductionThreshold();
    }

    @Override
    protected int getChildEnergy(SimulationConfig config) {
        return config.getPredatorChildEnergy();
    }

    @Override
    protected Agent createChild(int energy) {
        return new Predator(energy);
    }

    @Override
    public char getSymbol() { return 'X'; }

    @Override
    public String getSpeciesName() { return "Хищник"; }
}
