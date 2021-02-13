/**
 * A simple example to calculate the total host utilization
 * improved utilization of host while migration occurs
 * 
 * energy calculation
 * DC/Host
 */
package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.PowerTestVmAllocationPolicy;
import org.cloudbus.cloudsim.PowerTestVmSchedulerTimeShared;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * @author Esrat Maria
 *
 */
public class HostUtilizationExample {
	private static PowerDatacenter datacenter0, datacenter1;
	private static DatacenterBroker broker;
	private static List<Vm> vmlist;

	/**
	 * generating host parameters.
	 * 
	 **/

	private static int mips = 1000; // Host MIPS
	private static int ram = 4096; // Host Memory (MB)
	private static long storage = 10000000; // Host Storage
	private static int bw = 1000000; // Host BW
	private static int maxpower = 117; // Host Max Power
	private static int staticPowerPercentage = 50; // Host Static Power Percentage

	/**
	 * generating datacenter parameters.
	 * 
	 **/

	private static String arch = "x86"; // Datacenter Architecture
	private static String os = "Linux"; // Datacenter OS Types
	private static String vmm = "Xen"; // Datacenter VMM Types
	private static double time_zone = 10.0; // Datacenter Machines Timezone
	private static double cost = 3.0; // The cost of using processing in this resource (in Datacenter)
	private static double costPerMem = 0.05; // The cost of using memory in this resource (in Datacenter)
	private static double costPerStorage = 0.1; // The cost of using storage in this resource (in Datacenter)
	private static double costPerBw = 0.1; // The cost of using bw in this resource (in Datacenter)

	/**
	 * generating virtual machine parameters.
	 * 
	 **/

	private static int VMmips = 1000; // VM Mips Needed
	private static long VMsize = 10000; // VM Image Size (MB)
	private static int VMram = 512; // VM Memory Needed (MB)
	private static long VMbw = 1000; // VM BW needed
	private static int VMpesNumber = 1; // VM Number of CPUs
	private static String VMvmm = "Xen"; // VM VMM name

	private static int numberOfVMs = 15;
	private static int numberOfPEs = 5;
	private static int numberOfHosts = 4;

	public static void main(String[] args) {
		Log.printLine("Starting Host Utilization Example ...");

		try {
			CloudSim.init(1, Calendar.getInstance(), false);

			// creating datacenters
			datacenter0 = createDatacenter("DataCenter_0");
			datacenter1 = createDatacenter("DataCenter_1");

			// initializing the broker
			broker = createBroker("MainBroker");
			int brokerId = broker.getId();

			// initializing the vms and cloudlets
			vmlist = createVM(brokerId, numberOfVMs);
			List<Cloudlet> cloudletList = createCloudlet(brokerId, 100);

			// submitting the vms and cloudlets to the broker
			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);

			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			List<Cloudlet> newList = broker.getCloudletReceivedList();
			Log.printLine("Received " + newList.size() + " cloudlets");

			printCloudletList(newList);

			Log.printLine("The example has finished successfully !");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}

	}

	/**
	 * Create multiple power hosts, each with multiple CPUs
	 */
	public static List<PowerHost> createHostList() {
		List<Pe> peList1 = new ArrayList<>();

		// Generate cpu cores
		for (int i = 0; i < numberOfPEs; i++)
			peList1.add(new Pe(i, new PeProvisionerSimple(mips)));

		// Generate hosts and assign cpu cores to them
		List<PowerHost> hostList = new ArrayList<>();
		for (int i = 0; i < numberOfHosts; i++)
			hostList.add(new PowerHostUtilizationHistory(i, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw),
					storage, peList1, new PowerTestVmSchedulerTimeShared(peList1),
					new PowerModelLinear(maxpower, staticPowerPercentage)));

		return hostList;
	}

	/**
	 * Creating power datacenter
	 * 
	 * @param name Name of the datacenter
	 */
	private static PowerDatacenter createDatacenter(String name) {
		List<PowerHost> hostList = createHostList();
		LinkedList<Storage> storageList = new LinkedList<>();

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
				cost, costPerMem, costPerStorage, costPerBw);

		PowerDatacenter datacenter = null;
		try {
			datacenter = new PowerDatacenter(name, characteristics, new PowerTestVmAllocationPolicy(hostList),
					storageList, 15); // 15 --> is the cloud scheduling interval
			datacenter.setDisableMigrations(false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	/**
	 * Creating power broker
	 * 
	 * @param name Name of the broker
	 */
	private static DatacenterBroker createBroker(String name) {
		DatacenterBroker broker;

		try {
			broker = new DatacenterBroker(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return broker;
	}

	/**
	 * Create multiple VMs
	 * 
	 * @param brokerId Id of broker
	 * @param vms      number of vms to create
	 */
	private static List<Vm> createVM(int brokerId, int vms) {
		LinkedList<Vm> list = new LinkedList<>();

		for (int i = 0; i < vms; i++) {
			list.add(new Vm(i, brokerId, VMmips, VMpesNumber, VMram, VMbw, VMsize, VMvmm,
					new CloudletSchedulerSpaceShared()));
		}

		return list;
	}

	/**
	 * Creating cloudlets
	 * 
	 * @param brokerId Id of broker
	 * @param given    number of cloudlets
	 */

	private static List<Cloudlet> createCloudlet(int userId, int cloudlets) {
		// Creates a container to store Cloudlets
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		// cloudlet parameters
		long length = 1000;
		long fileSize = 300;
		long outputSize = 300;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		Cloudlet[] cloudlet = new Cloudlet[cloudlets];

		for (int i = 0; i < cloudlets; i++) {
			cloudlet[i] = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel,
					utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}

		return list;
	}

	/**
	 * Prints the Cloudlet objects
	 * 
	 * @param list list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + indent
				+ "Time" + indent + "Start Time" + indent + "Finish Time");

		double execTime = 0;
		int numOfSuccessCloudlets = 0;
		DecimalFormat dft = new DecimalFormat("###.##");
		for (Cloudlet value : list) {
			Log.print(indent + value.getCloudletId() + indent + indent);

			if (value.getStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + value.getResourceId() + indent + indent + indent + value.getVmId()
						+ indent + indent + indent + dft.format(value.getActualCPUTime()) + indent + indent
						+ dft.format(value.getExecStartTime()) + indent + indent + indent
						+ dft.format(value.getFinishTime()));

				execTime += value.getFinishTime() - value.getExecStartTime();
				numOfSuccessCloudlets += 1;
			}
		}

		Log.printLine();
		Log.printLine(String.format("Total Energy Consumption: %.2f kWh",
				(datacenter0.getPower() + datacenter1.getPower()) / (3600 * 1000)));
		Log.printLine("Number of VM migrations: DataCenter 0 = " + datacenter0.getMigrationCount() + ", DataCenter 1 = "
				+ datacenter1.getMigrationCount());
		Log.printLine("Average response time: " + execTime * 1. / numOfSuccessCloudlets);
		Log.printLine();
	}

}
