package org.cloudbus.cloudsim.examples.NormalWithoutQ;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.*;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.util.WorkloadFileReader;
import org.cloudbus.cloudsim.examples.NormalWithoutQ.Constants;
import org.cloudbus.cloudsim.examples.NormalWithoutQ.Qhelper;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.*;

public class QLearningSchedulerTest {

	/**
	 * The cloudlet list.
	 */
	private static List<Cloudlet> cloudletList;

	/**
	 * The vmlist.
	 */
	public static List<Vm> vmList;

	private static PowerDatacenter datacenter0, datacenter1;
	private static List<PowerHost> hostList;
	private static PowerDatacenterBroker broker;
	public static int brokerId;



	public void execute() throws Exception {
		PrintStream fileOut = new PrintStream("C:\\Users\\Esrat Maria\\Desktop\\output.txt");
		Log.printLine(
				"Starting Energy Efficient Scheduling Algorithm in Federated Edge Cloud Using Reinforcement Learning (Q)");
		try {
			CloudSim.init(1, Calendar.getInstance(), false);

			datacenter0 = Qhelper.createDatacenter("DataCenter_0");
			datacenter1 = Qhelper.createDatacenter("DataCenter_1");

			broker = Qhelper.createBroker();
			int brokerId = broker.getId();

			cloudletList = Qhelper.createCloudletListPlanetLab(brokerId, Constants.inputFolder);
			vmList = Qhelper.createVmList(brokerId, cloudletList.size());

			broker.submitVmList(vmList);
			broker.submitCloudletList(cloudletList);

			CloudSim.terminateSimulation(Constants.SIMULATION_LIMIT);

			double lastClock = CloudSim.startSimulation();

			List<Cloudlet> newList = broker.getCloudletReceivedList();
			// printCloudletList(newList);
			CloudSim.stopSimulation();
			Log.printLine();
			Qhelper.printCloudletList(newList);
			Log.printLine();
			System.setOut(fileOut);
			System.out.println();
			System.out.println("Received cloudlets: " + newList.size());
			System.out.println(String.format("Energy consumption: %.2f kWh",
					(datacenter0.getPower() + datacenter1.getPower()) / (3600 * 1000)));
			System.out.println("Datacenter 0 energy consumption without kwh conversion: " + datacenter0.getPower());
			System.out.println("Datacenter 1 energy consumption without kwh conversion: " + datacenter1.getPower());
			System.out.println("Number of VM migrations: DC0 = " + datacenter0.getMigrationCount() + ", DC1 = "
					+ datacenter1.getMigrationCount());
			System.out.println();
			
			Log.printLine(vmList.size());
			Qhelper.printResults(datacenter0, vmList, Constants.SIMULATION_LIMIT,
					"Energy Efficient Scheduling Algorithm in Federated Edge Cloud without Using Reinforcement Learning", false,
					"");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}





}
