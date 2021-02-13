package org.cloudbus.cloudsim.examples.QLearningScheduler;

import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;

import PowerModel.PowerModelDL360G7;
import PowerModel.PowerModelDL360Gen9;
import PowerModel.PowerModelML110G5;

public class Constants {

    public static final double SCHEDULING_INTERVAL = 300.0D;
    public static final double SIMULATION_LIMIT = 1000.0D;
    public static final int CLOUDLET_LENGTH = 216000000;
    public static final int CLOUDLET_PES = 1;
    public static final int VM_TYPES = 4;

    public static final int VM_BW = 100000;
    public static final int VM_SIZE = 2500;
    public static final int[] VM_MIPS = new int[]{350, 300, 250, 200, 150};
    public static final int[] VM_PES = new int[]{1, 1, 1, 1, 1};
    public static final int[] VM_RAM = new int[]{2048, 2048, 1024, 1024, 512};
    
    public final static int HOST_TYPES	 = 2;
    public static final int HOST_BW = 1000000;
    public static final int HOST_STORAGE = 1000000;
    public static final int[] HOST_MIPS = new int[]{300, 1800, 5400};
    public static final int[] HOST_PES = new int[]{1, 1, 1};
    public static final int[] HOST_RAM = new int[]{65536, 65536, 65536};
    public final static PowerModel HOST_POWER = new PowerModelSpecPowerHpProLiantMl110G4Xeon3040();
    //    public static final PowerModel[] HOST_POWER = new PowerModel[]{new PowerModelML110G5(), new PowerModelDL360G7(), new PowerModelDL360Gen9()};

    public static final int CREATE_VM = 99;
    public static final int CREATE_VM_ACK = 100;
    public static final int SUBMIT_CLOUDLET = 101;
    public static final int CLOUDSIM_RESTART = 102;
    
    public static final int Iteration = 100;

	public static final int NUMBER_OF_HOSTS = /*300*/800;

    public static final int terminateTime = 9300;
    
    public static String inputFolder = "C:\\Users\\Esrat Maria\\Desktop\\cloudsim-3.0.3\\examples\\workload\\planetlab\\20110420";
    //"C:\\Users\\Esrat Maria\\Desktop\\CloudPowerDeployment-master\\src\\main\\resources\\datas\\200";
    //"C:\\Users\\Esrat Maria\\Desktop\\cloudsim-3.0.3\\examples\\workload\\planetlab\\20110303";
    //"C:\Users\Esrat Maria\Desktop\cloudsim-3.0.3\examples\workload\planetlab\\20110403"
	
	public Constants() {
		
	}
}
