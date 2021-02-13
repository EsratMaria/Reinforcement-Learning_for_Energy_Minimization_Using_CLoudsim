package PowerModel;

import org.cloudbus.cloudsim.power.models.PowerModelSpecPower;

public class PowerModelDL360G7 extends PowerModelSpecPower {
    private final double[] power = new double[]{54.6D, 87.8D, 98.5D, 107.0D, 115.0D, 123.0D, 131.0D, 140.0D, 153.0D, 165.0D, 178.0D};

    public String getName(){
        return "G7";
    }

    public PowerModelDL360G7() {
    }

    protected double getPowerData(int index) {
        return this.power[index];
    }
}
