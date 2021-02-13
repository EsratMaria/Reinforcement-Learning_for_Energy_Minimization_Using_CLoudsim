package org.cloudbus.cloudsim.examples.power;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.BufferedWriter;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.QLearningScheduler.Constants;
import org.cloudbus.cloudsim.examples.power.planetlab.PlanetLabHelper;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.PowerTestVmAllocationPolicy;
import org.cloudbus.cloudsim.PowerTestVmSchedulerTimeShared;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.UtilizationModelNull;
import org.cloudbus.cloudsim.UtilizationModelPlanetLabInMemory;
import org.cloudbus.cloudsim.CloudletSchedulerDynamicWorkload;
import org.cloudbus.cloudsim.VmSchedulerTimeSharedOverSubscription;
import org.cloudbus.cloudsim.VmStateHistoryEntry;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.HostDynamicWorkload;
import org.cloudbus.cloudsim.HostStateHistoryEntry;

import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimple;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationMedianAbsoluteDeviation;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationStaticThreshold;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyMinimumUtilization;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicy;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyMaximumCorrelation;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyMinimumMigrationTime;
//import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyEco;
//import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyEco;

import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G5Xeon3075;
import org.cloudbus.cloudsim.util.MathUtil;

import org.cloudbus.cloudsim.NetworkTopology;

public class Project2 {

	protected static List<PowerHost> hostList;

	public static void main(String[] args) throws IOException {

		String experimentName = "project_2";
		String outputFolder = "output";
		String inputFolder = "C:\\Users\\Esrat Maria\\Desktop\\cloudsim-3.0.3\\examples\\workload\\planetlab\\20110303";
		Log.setDisabled(!true);
		Log.printLine("Starting " + experimentName);

		try {
			CloudSim.init(1, Calendar.getInstance(), false);

			PowerTestVmAllocationPolicy vmAllocationPolicy = null;
			// PowerVmSelectionPolicy vmSelectionPolicy = new
			// PowerVmSelectionPolicyMaximumCorrelation(new
			// PowerVmSelectionPolicyMinimumMigrationTime());

//			PowerVmSelectionPolicy vmSelectionPolicy = new PowerVmSelectionPolicyEco(
//					new PowerVmSelectionPolicyMinimumMigrationTime());
			List<PowerHost> hostList1 = createHostList(800);

			PowerDatacenter datacenter1 = (PowerDatacenter) createDatacenter("Datacenter1", hostList1);

			List<PowerHost> hostList2 = createHostList(500);

			PowerDatacenter datacenter2 = (PowerDatacenter) createDatacenter("Datacenter2", hostList2);

			List<PowerHost> hostList3 = createHostList(600);

			PowerDatacenter datacenter3 = (PowerDatacenter) createDatacenter("Datacenter3", hostList3);

			// datacenter.setPower(150.0);

			datacenter1.setDisableMigrations(false);
			datacenter2.setDisableMigrations(false);
			datacenter3.setDisableMigrations(false);

			PowerDatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			List<Cloudlet> cloudletList = PlanetLabHelper.createCloudletListPlanetLab(brokerId, inputFolder);
			List<Vm> vmList = createVmList(brokerId, cloudletList.size());

			NetworkTopology.buildNetworkTopology(
					"C:\\Users\\Esrat Maria\\Desktop\\cloudsim-3.0.3\\examples\\org\\cloudbus\\cloudsim\\examples\\network\\topology.brite");

			NetworkTopology.addLink(datacenter1.getId(), broker.getId(), 10.0, 10);
			NetworkTopology.addLink(datacenter2.getId(), broker.getId(), 10.0, 10);
			NetworkTopology.addLink(datacenter3.getId(), broker.getId(), 10.0, 10);

			NetworkTopology.addLink(datacenter1.getId(), datacenter2.getId(), 10.0, 10);
			NetworkTopology.addLink(datacenter2.getId(), datacenter3.getId(), 10.0, 10);
			NetworkTopology.addLink(datacenter3.getId(), datacenter1.getId(), 10.0, 10);

			broker.submitVmList(vmList);
			broker.submitCloudletList(cloudletList);
			CloudSim.terminateSimulation(86400);
			double lastClock = CloudSim.startSimulation();

			List<Cloudlet> newList = broker.getCloudletReceivedList();
			Log.printLine("Received " + newList.size() + " cloudlets");

			CloudSim.stopSimulation();

			printResults(datacenter1, vmList, lastClock, experimentName, false, outputFolder);

		}

		catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
			System.exit(0);
		}
	}

	private static PowerDatacenter createDatacenter(String name, List<PowerHost> hostList) {
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
		PowerDatacenter datacenter = null;
		try {
			datacenter = new PowerDatacenter(name, characteristics, new PowerTestVmAllocationPolicy(hostList),
					new LinkedList<Storage>(), 300);
			datacenter.setDisableMigrations(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datacenter;
	}

	public static List<Cloudlet> createCloudletListPlanetLab(int brokerId, String inputFolderName)
			throws FileNotFoundException {
		List<Cloudlet> list = new ArrayList<Cloudlet>();

		long fileSize = 300;
		long outputSize = 300;
		UtilizationModel utilizationModelNull = new UtilizationModelNull();

		File inputFolder = new File(inputFolderName);
		File[] files = inputFolder.listFiles();

		for (int i = 0; i < files.length; i++) {
			Cloudlet cloudlet = null;
			try {
				cloudlet = new Cloudlet(i, 2500 * (int) 24 * 60 * 60, 1, fileSize, outputSize,
						new UtilizationModelPlanetLabInMemory(files[i].getAbsolutePath(), 300.0), utilizationModelNull,
						utilizationModelNull);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
			cloudlet.setUserId(brokerId);
			cloudlet.setVmId(i);
			list.add(cloudlet);
		}

		return list;
	}

	public static List<Vm> createVmList(int brokerId, int vmsNumber) {
		List<Vm> vms = new ArrayList<Vm>();
		for (int i = 0; i < vmsNumber; i++) {
			int vmType = i / (int) Math.ceil((double) vmsNumber / VM_TYPES);
			vms.add(new PowerVm(
					i,
					brokerId,
					VM_MIPS[vmType],
					VM_PES[vmType],
					VM_RAM[vmType],
					VM_BW,
					VM_SIZE,
					1,
					"Xen",
					new CloudletSchedulerDynamicWorkload(VM_MIPS[vmType], VM_PES[vmType]),
					SCHEDULING_INTERVAL));
		}
		return vms;
	}

	/**
	 * Creates the host list.
	 * 
	 * @param hostsNumber the hosts number
	 * 
	 * @return the list< power host>
	 */
	public static List<PowerHost> createHostList(int hostsNumber) {
		List<PowerHost> hostList = new ArrayList<PowerHost>();
		for (int i = 0; i < hostsNumber; i++) {
			int hostType = i % HOST_TYPES;

			List<Pe> peList = new ArrayList<Pe>();
			for (int j = 0; j < HOST_PES[hostType]; j++) {
				peList.add(new Pe(j, new PeProvisionerSimple(HOST_MIPS[hostType])));
			}

			hostList.add(new PowerHostUtilizationHistory(
					i,
					new RamProvisionerSimple(HOST_RAM[hostType]),
					new BwProvisionerSimple(HOST_BW),
					HOST_STORAGE,
					peList,
					new PowerTestVmSchedulerTimeShared
					(peList),
					HOST_POWER[hostType]));
		}
		return hostList;
	}

	/**
	 * Creates the broker.
	 * 
	 * @return the datacenter broker
	 */
	public static PowerDatacenterBroker createBroker() {
		PowerDatacenterBroker broker = null;
		try {
			broker = new PowerDatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return broker;
	}


	public final static boolean ENABLE_OUTPUT = true;
	public final static boolean OUTPUT_CSV = false;

	public final static double SCHEDULING_INTERVAL = 300;
	public final static double SIMULATION_LIMIT = 24 * 60 * 60;

	public final static int CLOUDLET_LENGTH = 2500 * (int) SIMULATION_LIMIT;
	public final static int CLOUDLET_PES = 1;

	/*
	 * VM instance types: High-Memory Extra Large Instance: 3.25 EC2 Compute Units,
	 * 8.55 GB // too much MIPS High-CPU Medium Instance: 2.5 EC2 Compute Units,
	 * 0.85 GB Extra Large Instance: 2 EC2 Compute Units, 3.75 GB Small Instance: 1
	 * EC2 Compute Unit, 1.7 GB Micro Instance: 0.5 EC2 Compute Unit, 0.633 GB We
	 * decrease the memory size two times to enable oversubscription
	 *
	 */
	public final static int VM_TYPES = 4;
	public final static int[] VM_MIPS = { 2500, 2000, 1000, 500 };
	public final static int[] VM_PES = { 1, 1, 1, 1 };
	public final static int[] VM_RAM = { 870, 1740, 1740, 613 };
	public final static int VM_BW = 100000; // 100 Mbit/s
	public final static int VM_SIZE = 2500; // 2.5 GB

	/*
	 * Host types: HP ProLiant ML110 G4 (1 x [Xeon 3040 1860 MHz, 2 cores], 4GB) HP
	 * ProLiant ML110 G5 (1 x [Xeon 3075 2660 MHz, 2 cores], 4GB) We increase the
	 * memory size to enable over-subscription (x4)
	 */
	public final static int HOST_TYPES = 2;
	public final static int[] HOST_MIPS = { 1860, 2660 };
	public final static int[] HOST_PES = { 2, 2 };
	public final static int[] HOST_RAM = { 4096, 4096 };
	public final static int HOST_BW = 1000000; // 1 Gbit/s
	public final static int HOST_STORAGE = 1000000; // 1 GB

	public final static PowerModel[] HOST_POWER = { new PowerModelSpecPowerHpProLiantMl110G4Xeon3040(),
			new PowerModelSpecPowerHpProLiantMl110G5Xeon3075() };


	public static void printResults(PowerDatacenter datacenter, List<Vm> vms, double lastClock, String experimentName,
			boolean outputInCsv, String outputFolder) {
		Log.enable();
		List<Host> hosts = datacenter.getHostList();

		int numberOfHosts = hosts.size();
		int numberOfVms = vms.size();

		double totalSimulationTime = lastClock;
		double energy = datacenter.getPower() / (3600 * 1000);
		Log.printLine("check " + energy);
		int numberOfMigrations = datacenter.getMigrationCount();

		Map<String, Double> slaMetrics = getSlaMetrics(vms);

		double slaOverall = slaMetrics.get("overall");
		double slaAverage = slaMetrics.get("average");
		double slaDegradationDueToMigration = slaMetrics.get("underallocated_migration");
		// double slaTimePerVmWithMigration =
		// slaMetrics.get("sla_time_per_vm_with_migration");
		// double slaTimePerVmWithoutMigration =
		// slaMetrics.get("sla_time_per_vm_without_migration");
		// double slaTimePerHost = getSlaTimePerHost(hosts);
		double slaTimePerActiveHost = getSlaTimePerActiveHost(hosts);

		double sla = slaTimePerActiveHost * slaDegradationDueToMigration;

		List<Double> timeBeforeHostShutdown = getTimesBeforeHostShutdown(hosts);

		int numberOfHostShutdowns = timeBeforeHostShutdown.size();

		double meanTimeBeforeHostShutdown = Double.NaN;
		double stDevTimeBeforeHostShutdown = Double.NaN;
		if (!timeBeforeHostShutdown.isEmpty()) {
			meanTimeBeforeHostShutdown = MathUtil.mean(timeBeforeHostShutdown);
			stDevTimeBeforeHostShutdown = MathUtil.stDev(timeBeforeHostShutdown);
		}

		List<Double> timeBeforeVmMigration = getTimesBeforeVmMigration(vms);
		double meanTimeBeforeVmMigration = Double.NaN;
		double stDevTimeBeforeVmMigration = Double.NaN;
		if (!timeBeforeVmMigration.isEmpty()) {
			meanTimeBeforeVmMigration = MathUtil.mean(timeBeforeVmMigration);
			stDevTimeBeforeVmMigration = MathUtil.stDev(timeBeforeVmMigration);
		}

		if (outputInCsv) {
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}
			File folder1 = new File(outputFolder + "/stats");
			if (!folder1.exists()) {
				folder1.mkdir();
			}
			File folder2 = new File(outputFolder + "/time_before_host_shutdown");
			if (!folder2.exists()) {
				folder2.mkdir();
			}
			File folder3 = new File(outputFolder + "/time_before_vm_migration");
			if (!folder3.exists()) {
				folder3.mkdir();
			}
			File folder4 = new File(outputFolder + "/metrics");
			if (!folder4.exists()) {
				folder4.mkdir();
			}

			StringBuilder data = new StringBuilder();
			String delimeter = ",";

			data.append(experimentName + delimeter);
			data.append(parseExperimentName(experimentName));
			data.append(String.format("%d", numberOfHosts) + delimeter);
			data.append(String.format("%d", numberOfVms) + delimeter);
			data.append(String.format("%.2f", totalSimulationTime) + delimeter);
			data.append(String.format("%.5f", energy) + delimeter);
			data.append(String.format("%d", numberOfMigrations) + delimeter);
			data.append(String.format("%.10f", sla) + delimeter);
			data.append(String.format("%.10f", slaTimePerActiveHost) + delimeter);
			data.append(String.format("%.10f", slaDegradationDueToMigration) + delimeter);
			data.append(String.format("%.10f", slaOverall) + delimeter);
			data.append(String.format("%.10f", slaAverage) + delimeter);
			// data.append(String.format("%.5f", slaTimePerVmWithMigration) + delimeter);
			// data.append(String.format("%.5f", slaTimePerVmWithoutMigration) + delimeter);
			// data.append(String.format("%.5f", slaTimePerHost) + delimeter);
			data.append(String.format("%d", numberOfHostShutdowns) + delimeter);
			data.append(String.format("%.2f", meanTimeBeforeHostShutdown) + delimeter);
			data.append(String.format("%.2f", stDevTimeBeforeHostShutdown) + delimeter);
			data.append(String.format("%.2f", meanTimeBeforeVmMigration) + delimeter);
			data.append(String.format("%.2f", stDevTimeBeforeVmMigration) + delimeter);

			if (datacenter.getVmAllocationPolicy() instanceof PowerVmAllocationPolicyMigrationAbstract) {
				PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerVmAllocationPolicyMigrationAbstract) datacenter
						.getVmAllocationPolicy();

				double executionTimeVmSelectionMean = MathUtil
						.mean(vmAllocationPolicy.getExecutionTimeHistoryVmSelection());
				double executionTimeVmSelectionStDev = MathUtil
						.stDev(vmAllocationPolicy.getExecutionTimeHistoryVmSelection());
				double executionTimeHostSelectionMean = MathUtil
						.mean(vmAllocationPolicy.getExecutionTimeHistoryHostSelection());
				double executionTimeHostSelectionStDev = MathUtil
						.stDev(vmAllocationPolicy.getExecutionTimeHistoryHostSelection());
				double executionTimeVmReallocationMean = MathUtil
						.mean(vmAllocationPolicy.getExecutionTimeHistoryVmReallocation());
				double executionTimeVmReallocationStDev = MathUtil
						.stDev(vmAllocationPolicy.getExecutionTimeHistoryVmReallocation());
				double executionTimeTotalMean = MathUtil.mean(vmAllocationPolicy.getExecutionTimeHistoryTotal());
				double executionTimeTotalStDev = MathUtil.stDev(vmAllocationPolicy.getExecutionTimeHistoryTotal());

				data.append(String.format("%.5f", executionTimeVmSelectionMean) + delimeter);
				data.append(String.format("%.5f", executionTimeVmSelectionStDev) + delimeter);
				data.append(String.format("%.5f", executionTimeHostSelectionMean) + delimeter);
				data.append(String.format("%.5f", executionTimeHostSelectionStDev) + delimeter);
				data.append(String.format("%.5f", executionTimeVmReallocationMean) + delimeter);
				data.append(String.format("%.5f", executionTimeVmReallocationStDev) + delimeter);
				data.append(String.format("%.5f", executionTimeTotalMean) + delimeter);
				data.append(String.format("%.5f", executionTimeTotalStDev) + delimeter);

				writeMetricHistory(hosts, vmAllocationPolicy, outputFolder + "/metrics/" + experimentName + "_metric");
			}

			data.append("\n");

			writeDataRow(data.toString(), outputFolder + "/stats/" + experimentName + "_stats.csv");
			writeDataColumn(timeBeforeHostShutdown,
					outputFolder + "/time_before_host_shutdown/" + experimentName + "_time_before_host_shutdown.csv");
			writeDataColumn(timeBeforeVmMigration,
					outputFolder + "/time_before_vm_migration/" + experimentName + "_time_before_vm_migration.csv");

		} else {
			Log.setDisabled(false);
			Log.printLine();
			Log.printLine(String.format("Experiment name: " + experimentName));
			Log.printLine(String.format("Number of hosts: " + numberOfHosts));
			Log.printLine(String.format("Number of VMs: " + numberOfVms));
			Log.printLine(String.format("Total simulation time: %.2f sec", totalSimulationTime));
			Log.printLine(String.format("Energy consumption: %.2f kWh", energy));
			Log.printLine(String.format("Number of VM migrations: %d", numberOfMigrations));
			Log.printLine(String.format("SLA: %.5f%%", sla * 100));
			Log.printLine(
					String.format("SLA perf degradation due to migration: %.2f%%", slaDegradationDueToMigration * 100));
			Log.printLine(String.format("SLA time per active host: %.2f%%", slaTimePerActiveHost * 100));
			Log.printLine(String.format("Overall SLA violation: %.2f%%", slaOverall * 100));
			Log.printLine(String.format("Average SLA violation: %.2f%%", slaAverage * 100));
			// Log.printLine(String.format("SLA time per VM with migration: %.2f%%",
			// slaTimePerVmWithMigration * 100));
			// Log.printLine(String.format("SLA time per VM without migration: %.2f%%",
			// slaTimePerVmWithoutMigration * 100));
			// Log.printLine(String.format("SLA time per host: %.2f%%", slaTimePerHost *
			// 100));
			Log.printLine(String.format("Number of host shutdowns: %d", numberOfHostShutdowns));
			Log.printLine(String.format("Mean time before a host shutdown: %.2f sec", meanTimeBeforeHostShutdown));
			Log.printLine(String.format("StDev time before a host shutdown: %.2f sec", stDevTimeBeforeHostShutdown));
			Log.printLine(String.format("Mean time before a VM migration: %.2f sec", meanTimeBeforeVmMigration));
			Log.printLine(String.format("StDev time before a VM migration: %.2f sec", stDevTimeBeforeVmMigration));

			if (datacenter.getVmAllocationPolicy() instanceof PowerVmAllocationPolicyMigrationAbstract) {
				PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy = (PowerVmAllocationPolicyMigrationAbstract) datacenter
						.getVmAllocationPolicy();

				double executionTimeVmSelectionMean = MathUtil
						.mean(vmAllocationPolicy.getExecutionTimeHistoryVmSelection());
				double executionTimeVmSelectionStDev = MathUtil
						.stDev(vmAllocationPolicy.getExecutionTimeHistoryVmSelection());
				double executionTimeHostSelectionMean = MathUtil
						.mean(vmAllocationPolicy.getExecutionTimeHistoryHostSelection());
				double executionTimeHostSelectionStDev = MathUtil
						.stDev(vmAllocationPolicy.getExecutionTimeHistoryHostSelection());
				double executionTimeVmReallocationMean = MathUtil
						.mean(vmAllocationPolicy.getExecutionTimeHistoryVmReallocation());
				double executionTimeVmReallocationStDev = MathUtil
						.stDev(vmAllocationPolicy.getExecutionTimeHistoryVmReallocation());
				double executionTimeTotalMean = MathUtil.mean(vmAllocationPolicy.getExecutionTimeHistoryTotal());
				double executionTimeTotalStDev = MathUtil.stDev(vmAllocationPolicy.getExecutionTimeHistoryTotal());

				Log.printLine(
						String.format("Execution time - VM selection mean: %.5f sec", executionTimeVmSelectionMean));
				Log.printLine(
						String.format("Execution time - VM selection stDev: %.5f sec", executionTimeVmSelectionStDev));
				Log.printLine(String.format("Execution time - host selection mean: %.5f sec",
						executionTimeHostSelectionMean));
				Log.printLine(String.format("Execution time - host selection stDev: %.5f sec",
						executionTimeHostSelectionStDev));
				Log.printLine(String.format("Execution time - VM reallocation mean: %.5f sec",
						executionTimeVmReallocationMean));
				Log.printLine(String.format("Execution time - VM reallocation stDev: %.5f sec",
						executionTimeVmReallocationStDev));
				Log.printLine(String.format("Execution time - total mean: %.5f sec", executionTimeTotalMean));
				Log.printLine(String.format("Execution time - total stDev: %.5f sec", executionTimeTotalStDev));
			}
			Log.printLine();
		}

		Log.setDisabled(true);
	}

	protected static Map<String, Double> getSlaMetrics(List<Vm> vms) {
		Map<String, Double> metrics = new HashMap<String, Double>();
		List<Double> slaViolation = new LinkedList<Double>();
		double totalAllocated = 0;
		double totalRequested = 0;
		double totalUnderAllocatedDueToMigration = 0;

		for (Vm vm : vms) {
			double vmTotalAllocated = 0;
			double vmTotalRequested = 0;
			double vmUnderAllocatedDueToMigration = 0;
			double previousTime = -1;
			double previousAllocated = 0;
			double previousRequested = 0;
			boolean previousIsInMigration = false;

			for (VmStateHistoryEntry entry : vm.getStateHistory()) {
				if (previousTime != -1) {
					double timeDiff = entry.getTime() - previousTime;
					vmTotalAllocated += previousAllocated * timeDiff;
					vmTotalRequested += previousRequested * timeDiff;

					if (previousAllocated < previousRequested) {
						slaViolation.add((previousRequested - previousAllocated) / previousRequested);
						if (previousIsInMigration) {
							vmUnderAllocatedDueToMigration += (previousRequested - previousAllocated) * timeDiff;
						}
					}
				}

				previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime();
				previousIsInMigration = entry.isInMigration();
			}

			totalAllocated += vmTotalAllocated;
			totalRequested += vmTotalRequested;
			totalUnderAllocatedDueToMigration += vmUnderAllocatedDueToMigration;
		}

		metrics.put("overall", (totalRequested - totalAllocated) / totalRequested);
		if (slaViolation.isEmpty()) {
			metrics.put("average", 0.);
		} else {
			metrics.put("average", MathUtil.mean(slaViolation));
		}
		metrics.put("underallocated_migration", totalUnderAllocatedDueToMigration / totalRequested);
		// metrics.put("sla_time_per_vm_with_migration",
		// slaViolationTimePerVmWithMigration /
		// totalTime);
		// metrics.put("sla_time_per_vm_without_migration",
		// slaViolationTimePerVmWithoutMigration /
		// totalTime);

		return metrics;
	}

	protected static double getSlaTimePerActiveHost(List<Host> hosts) {
		double slaViolationTimePerHost = 0;
		double totalTime = 0;

		for (Host _host : hosts) {
			HostDynamicWorkload host = (HostDynamicWorkload) _host;
			double previousTime = -1;
			double previousAllocated = 0;
			double previousRequested = 0;
			boolean previousIsActive = true;

			for (HostStateHistoryEntry entry : host.getStateHistory()) {
				if (previousTime != -1 && previousIsActive) {
					double timeDiff = entry.getTime() - previousTime;
					totalTime += timeDiff;
					if (previousAllocated < previousRequested) {
						slaViolationTimePerHost += timeDiff;
					}
				}

				previousAllocated = entry.getAllocatedMips();
				previousRequested = entry.getRequestedMips();
				previousTime = entry.getTime();
				previousIsActive = entry.isActive();
			}
		}

		return slaViolationTimePerHost / totalTime;
	}

	public static List<Double> getTimesBeforeHostShutdown(List<Host> hosts) {
		List<Double> timeBeforeShutdown = new LinkedList<Double>();
		for (Host host : hosts) {
			boolean previousIsActive = true;
			double lastTimeSwitchedOn = 0;
			for (HostStateHistoryEntry entry : ((HostDynamicWorkload) host).getStateHistory()) {
				if (previousIsActive == true && entry.isActive() == false) {
					timeBeforeShutdown.add(entry.getTime() - lastTimeSwitchedOn);
				}
				if (previousIsActive == false && entry.isActive() == true) {
					lastTimeSwitchedOn = entry.getTime();
				}
				previousIsActive = entry.isActive();
			}
		}
		return timeBeforeShutdown;
	}

	public static List<Double> getTimesBeforeVmMigration(List<Vm> vms) {
		List<Double> timeBeforeVmMigration = new LinkedList<Double>();
		for (Vm vm : vms) {
			boolean previousIsInMigration = false;
			double lastTimeMigrationFinished = 0;
			for (VmStateHistoryEntry entry : vm.getStateHistory()) {
				if (previousIsInMigration == true && entry.isInMigration() == false) {
					timeBeforeVmMigration.add(entry.getTime() - lastTimeMigrationFinished);
				}
				if (previousIsInMigration == false && entry.isInMigration() == true) {
					lastTimeMigrationFinished = entry.getTime();
				}
				previousIsInMigration = entry.isInMigration();
			}
		}
		return timeBeforeVmMigration;
	}

	public static String parseExperimentName(String name) {
		Scanner scanner = new Scanner(name);
		StringBuilder csvName = new StringBuilder();
		scanner.useDelimiter("_");
		for (int i = 0; i < 4; i++) {
			if (scanner.hasNext()) {
				csvName.append(scanner.next() + ",");
			} else {
				csvName.append(",");
			}
		}
		scanner.close();
		return csvName.toString();
	}

	public static void writeMetricHistory(List<? extends Host> hosts,
			PowerVmAllocationPolicyMigrationAbstract vmAllocationPolicy, String outputPath) {
		// for (Host host : hosts) {
		for (int j = 0; j < 10; j++) {
			Host host = hosts.get(j);

			if (!vmAllocationPolicy.getTimeHistory().containsKey(host.getId())) {
				continue;
			}
			File file = new File(outputPath + "_" + host.getId() + ".csv");
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(0);
			}
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				List<Double> timeData = vmAllocationPolicy.getTimeHistory().get(host.getId());
				List<Double> utilizationData = vmAllocationPolicy.getUtilizationHistory().get(host.getId());
				List<Double> metricData = vmAllocationPolicy.getMetricHistory().get(host.getId());

				for (int i = 0; i < timeData.size(); i++) {
					writer.write(String.format("%.2f,%.2f,%.2f\n", timeData.get(i), utilizationData.get(i),
							metricData.get(i)));
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}

	public static void writeDataRow(String data, String outputPath) {
		File file = new File(outputPath);
		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(data);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void writeDataColumn(List<? extends Number> data, String outputPath) {
		File file = new File(outputPath);
		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (Number value : data) {
				writer.write(value.toString() + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

}