package lifesim.model;

/**
 * Параметры модели (подобраны эмпирически так, чтобы жизнь не прекращалась)
 */
public class SimulationConfig {
    private int width = 90;
    private int height = 60;
    private int initialPlants = 1527;
    private int initialHerbivores = 330;
    private int initialPredators = 65;
    private int plantInitialEnergy = 5;
    private int plantGrowth = 1;
    private int plantReproductionThreshold = 10;
    private int plantChildEnergy = 5;
    private int herbivoreInitialEnergy = 20;
    private int herbivoreMetabolism = 1;
    private int herbivoreFoodGain = 6;
    private int herbivoreReproductionThreshold = 50;
    private int herbivoreChildEnergy = 23;
    private int predatorInitialEnergy = 60;
    private int predatorMetabolism = 1;
    private int predatorMeatGain = 40;
    private int predatorReproductionThreshold = 200;
    private int predatorChildEnergy = 60;

    /** Предустановка для консольной версии: поле поменьше, чтобы помещалось в окне терминала. */
    public static SimulationConfig consoleDefaults() {
        SimulationConfig c = new SimulationConfig();
        c.width = 80;
        c.height = 40;
        c.initialPlants = 900;
        c.initialHerbivores = 190;
        c.initialPredators = 38;
        return c;
    }

    public SimulationConfig copy() {
        SimulationConfig c = new SimulationConfig();
        c.width = width;
        c.height = height;
        c.initialPlants = initialPlants;
        c.initialHerbivores = initialHerbivores;
        c.initialPredators = initialPredators;
        c.plantInitialEnergy = plantInitialEnergy;
        c.plantGrowth = plantGrowth;
        c.plantReproductionThreshold = plantReproductionThreshold;
        c.plantChildEnergy = plantChildEnergy;
        c.herbivoreInitialEnergy = herbivoreInitialEnergy;
        c.herbivoreMetabolism = herbivoreMetabolism;
        c.herbivoreFoodGain = herbivoreFoodGain;
        c.herbivoreReproductionThreshold = herbivoreReproductionThreshold;
        c.herbivoreChildEnergy = herbivoreChildEnergy;
        c.predatorInitialEnergy = predatorInitialEnergy;
        c.predatorMetabolism = predatorMetabolism;
        c.predatorMeatGain = predatorMeatGain;
        c.predatorReproductionThreshold = predatorReproductionThreshold;
        c.predatorChildEnergy = predatorChildEnergy;
        return c;
    }

    /** Скопировать все параметры из другой конфигурации. */
    public void copyFrom(SimulationConfig other) {
        this.width = other.width;
        this.height = other.height;
        this.initialPlants = other.initialPlants;
        this.initialHerbivores = other.initialHerbivores;
        this.initialPredators = other.initialPredators;
        this.plantInitialEnergy = other.plantInitialEnergy;
        this.plantGrowth = other.plantGrowth;
        this.plantReproductionThreshold = other.plantReproductionThreshold;
        this.plantChildEnergy = other.plantChildEnergy;
        this.herbivoreInitialEnergy = other.herbivoreInitialEnergy;
        this.herbivoreMetabolism = other.herbivoreMetabolism;
        this.herbivoreFoodGain = other.herbivoreFoodGain;
        this.herbivoreReproductionThreshold = other.herbivoreReproductionThreshold;
        this.herbivoreChildEnergy = other.herbivoreChildEnergy;
        this.predatorInitialEnergy = other.predatorInitialEnergy;
        this.predatorMetabolism = other.predatorMetabolism;
        this.predatorMeatGain = other.predatorMeatGain;
        this.predatorReproductionThreshold = other.predatorReproductionThreshold;
        this.predatorChildEnergy = other.predatorChildEnergy;
    }


    public int getWidth() { return width; }
    public void setWidth(int value) { this.width = value; }

    public int getHeight() { return height; }
    public void setHeight(int value) { this.height = value; }

    public int getInitialPlants() { return initialPlants; }
    public void setInitialPlants(int value) { this.initialPlants = value; }

    public int getInitialHerbivores() { return initialHerbivores; }
    public void setInitialHerbivores(int value) { this.initialHerbivores = value; }

    public int getInitialPredators() { return initialPredators; }
    public void setInitialPredators(int value) { this.initialPredators = value; }

    public int getPlantInitialEnergy() { return plantInitialEnergy; }
    public void setPlantInitialEnergy(int value) { this.plantInitialEnergy = value; }

    public int getPlantGrowth() { return plantGrowth; }
    public void setPlantGrowth(int value) { this.plantGrowth = value; }

    public int getPlantReproductionThreshold() { return plantReproductionThreshold; }
    public void setPlantReproductionThreshold(int value) { this.plantReproductionThreshold = value; }

    public int getPlantChildEnergy() { return plantChildEnergy; }
    public void setPlantChildEnergy(int value) { this.plantChildEnergy = value; }

    public int getHerbivoreInitialEnergy() { return herbivoreInitialEnergy; }
    public void setHerbivoreInitialEnergy(int value) { this.herbivoreInitialEnergy = value; }

    public int getHerbivoreMetabolism() { return herbivoreMetabolism; }
    public void setHerbivoreMetabolism(int value) { this.herbivoreMetabolism = value; }

    public int getHerbivoreFoodGain() { return herbivoreFoodGain; }
    public void setHerbivoreFoodGain(int value) { this.herbivoreFoodGain = value; }

    public int getHerbivoreReproductionThreshold() { return herbivoreReproductionThreshold; }
    public void setHerbivoreReproductionThreshold(int value) { this.herbivoreReproductionThreshold = value; }

    public int getHerbivoreChildEnergy() { return herbivoreChildEnergy; }
    public void setHerbivoreChildEnergy(int value) { this.herbivoreChildEnergy = value; }

    public int getPredatorInitialEnergy() { return predatorInitialEnergy; }
    public void setPredatorInitialEnergy(int value) { this.predatorInitialEnergy = value; }

    public int getPredatorMetabolism() { return predatorMetabolism; }
    public void setPredatorMetabolism(int value) { this.predatorMetabolism = value; }

    public int getPredatorMeatGain() { return predatorMeatGain; }
    public void setPredatorMeatGain(int value) { this.predatorMeatGain = value; }

    public int getPredatorReproductionThreshold() { return predatorReproductionThreshold; }
    public void setPredatorReproductionThreshold(int value) { this.predatorReproductionThreshold = value; }

    public int getPredatorChildEnergy() { return predatorChildEnergy; }
    public void setPredatorChildEnergy(int value) { this.predatorChildEnergy = value; }
}
