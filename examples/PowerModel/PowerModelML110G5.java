package PowerModel;

import org.cloudbus.cloudsim.power.models.PowerModelSpecPower;

public class PowerModelML110G5 extends PowerModelSpecPower {
    private final double[] power = new double[]{93.7D, 97.0D, 101.0D, 105.0D, 110.0D, 116.0D, 121.0D, 125.0D, 129.0D, 133.0D, 135.0D};

    public String getName() {
        return "G5";
    }

    public PowerModelML110G5() {
    }

    protected double getPowerData(int index) {
        return this.power[index];
    }
}
