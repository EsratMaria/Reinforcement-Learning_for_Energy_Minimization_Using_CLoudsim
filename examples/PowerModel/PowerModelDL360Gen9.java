package PowerModel;

import org.cloudbus.cloudsim.power.models.PowerModelSpecPower;

public class PowerModelDL360Gen9 extends PowerModelSpecPower {
    private final double[] power = new double[]{45.0D, 83.7D, 101.0D, 118.0D, 133.0D, 145.0D, 162.0D, 188.0D, 218.0D, 248.0D, 276.0D};
    public String getName(){
        return "G9";
    }
    public PowerModelDL360Gen9() {
    }

    protected double getPowerData(int index) {
        return this.power[index];
    }
}
