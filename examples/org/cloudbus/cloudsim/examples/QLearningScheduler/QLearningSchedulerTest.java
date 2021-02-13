package org.cloudbus.cloudsim.examples.QLearningScheduler;

import static org.cloudbus.cloudsim.examples.QLearningScheduler.QLearningAgent.QList;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicy;

public class QLearningSchedulerTest {

	/**
	 * The cloudlet list.
	 */
	private static List<Cloudlet> cloudletList;

	/**
	 * The vmlist.
	 */
	public static List<Vm> vmList;

	private static QlearningPowerDataCenter datacenter0, datacenter1;
	private static List<PowerHost> hostList;
	private static QLearningAgent qAgent;
	private static PowerDatacenterBroker broker;
	public static int brokerId;

	private double LEARNING_GAMMA = 0.2;
	private double LEARNING_ALPHA = 0.2;
	private double LEARNING_EPSILON = 0.05;

	public double getLEARNING_GAMMA() {
		return LEARNING_GAMMA;
	}

	public void setLEARNING_GAMMA(double LEARNING_GAMMA) {
		this.LEARNING_GAMMA = LEARNING_GAMMA;
	}

	public double getLEARNING_ALPHA() {
		return LEARNING_ALPHA;
	}

	public void setLEARNING_ALPHA(double LEARNING_ALPHA) {
		this.LEARNING_ALPHA = LEARNING_ALPHA;
	}

	public double getLEARNING_EPSILON() {
		return LEARNING_EPSILON;
	}

	public void setLEARNING_EPSILON(double LEARNING_EPSILON) {
		this.LEARNING_EPSILON = LEARNING_EPSILON;
	}

	public void execute() throws Exception {
		PrintStream fileOut = new PrintStream("C:\\Users\\Esrat Maria\\Desktop\\output.txt");
		qAgent = new QLearningAgent(LEARNING_GAMMA, LEARNING_ALPHA, LEARNING_EPSILON);
		Log.printLine(
				"Starting Energy Efficient Scheduling Algorithm in Federated Edge Cloud Using Reinforcement Learning (Q)");
		for (int i = 0; i < 100; i++) {
			LEARNING_EPSILON = 1 / (i + 1);
			CloudSim.init(1, Calendar.getInstance(), false);

			datacenter0 = createDatacenter("DataCenter_0");
			//datacenter1 = Qhelper.createDatacenter("DataCenter_1");

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
			//Qhelper.printCloudletList(newList);
			System.setOut(fileOut);
			// System.out.println();
			// System.out.println("Received cloudlets: " + newList.size());
//			System.out.println(String.format("%.2f",
//					(datacenter0.getPtants.SIMULATION_LIMIT,
//					"Energy Efficient Scheduling Algorithm in Federated Edge Cloud Using Reinforceower() + datacenter1.getPower()) / (3600 * 1000)));
//			Qhelper.printResults(datacenter0, vmList, Consment Learning", false,
//					"");
			 System.out.println(String.format("%.2f kWh",
			 (datacenter0.getPowerModified()) / (3600 * 1000)));
			 System.out.println(datacenter0.getMigrationCount());
			// System.out.println("Datacenter 0 energy consumption without kwh conversion: "
			// + datacenter0.getPower());
			// System.out.println("Datacenter 1 energy consumption without kwh conversion: "
			// + datacenter1.getPower());
			// System.out.println("Number of VM migrations: DC0 = " +
			// datacenter0.getMigrationCount() + ", DC1 = "
			// + datacenter1.getMigrationCount());
			// System.out.println("\n" + i + "----------------------------------");
			// System.out.println();
			
			int totalTotalRequested = 0;
		    int totalTotalAllocated = 0;
		    ArrayList<Double> sla = new ArrayList<Double>();
		    int numberOfAllocations = 0;
		    
			for (Entry<String, List<List<Double>>> entry : datacenter0.getUnderAllocatedMips().entrySet()) {
			    List<List<Double>> underAllocatedMips = entry.getValue();
			    double totalRequested = 0;
			    double totalAllocated = 0;
			    for (List<Double> mips : underAllocatedMips) {
			    	if (mips.get(0) != 0) {
			    		numberOfAllocations++;
			    		totalRequested += mips.get(0);
			    		totalAllocated += mips.get(1);
			    		double _sla = (mips.get(0) - mips.get(1)) / mips.get(0) * 100;
			    		if (_sla > 0) {
			    			sla.add(_sla);
			    		}
			    	}
				}
			    totalTotalRequested += totalRequested;
			    totalTotalAllocated += totalAllocated;
			}
			double averageSla = 0;
			if (sla.size() > 0) {
			    double totalSla = 0;
			    for (Double _sla : sla) {
			    	totalSla += _sla;
				}
			    averageSla = totalSla / sla.size()*0.6;
			}
//			PrintStream fileOut1 = new PrintStream("C:\\Users\\Esrat Maria\\Desktop\\sla.txt");
//			System.setOut(fileOut1);
			//System.out.println(datacenter0.getMigrationCount());
			//System.out.println(String.format("%.2f%%", (double) sla.size() * 100 * 0.4 / numberOfAllocations));
			//System.out.println(String.format("Average SLA violation: %.2f%%", averageSla));

		}
		Log.printLine(QList.size());
		Log.printLine(vmList.size());
//		Qhelper.printResults(datacenter0, vmList, Constants.SIMULATION_LIMIT,
//				"Energy Efficient Scheduling Algorithm in Federated Edge Cloud Using Reinforcement Learning", false,
//				"");
		// System.out.println(QlearningPowerDataCenter.allpower);
	}

	public static QlearningPowerDataCenter createDatacenter(String name) {
		List<PowerHost> hostList = Qhelper.createHostList(Constants.NUMBER_OF_HOSTS);
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this resource
		double costPerBw = 0.001; // the cost of using bw in this resource

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
				cost, costPerMem, costPerStorage, costPerBw);
		QlearningPowerDataCenter datacenter = null;
		PowerVmSelectionPolicy vmSelectionPolicy = null;
		try {
			datacenter = new QlearningPowerDataCenter(name, characteristics, new QPowerVmAllocationPolicy(hostList, vmSelectionPolicy),
					new LinkedList<Storage>(), 300, qAgent);
			datacenter.setDisableMigrations(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datacenter;
	}

}
