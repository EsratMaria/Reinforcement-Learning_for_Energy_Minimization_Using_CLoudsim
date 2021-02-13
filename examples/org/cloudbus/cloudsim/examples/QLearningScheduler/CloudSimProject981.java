/*
 * Title: CloudSim Project for Cloud Computing Course 98-1
 * Author: Soroush Hashemi Far
 */

package org.cloudbus.cloudsim.examples.QLearningScheduler;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.network.datacenter.AggregateSwitch;
import org.cloudbus.cloudsim.network.datacenter.EdgeSwitch;
import org.cloudbus.cloudsim.network.datacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.datacenter.NetworkHost;
import org.cloudbus.cloudsim.power.*;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.util.WorkloadFileReader;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.*;

public class CloudSimProject981 {

	private static PowerDatacenter datacenter0, datacenter1;
	private static DatacenterBroker broker;
	private static List<Vm> vmlist;

	private static int mips = 1000; // Host MIPS
	private static int ram = 4096; // Host Memory (MB)
	private static long storage = 10000000; // Host Storage
	private static int bw = 1000000; // Host BW
	private static int maxpower = 117; // Host Max Power
	private static int staticPowerPercentage = 50; // Host Static Power Percentage

	private static String arch = "x86"; // Datacenter Architecture
	private static String os = "Linux"; // Datacenter OS Types
	private static String vmm = "Xen"; // Datacenter VMM Types
	private static double time_zone = 10.0; // Datacenter Machines Timezone
	private static double cost = 3.0; // The cost of using processing in this resource (in Datacenter)
	private static double costPerMem = 0.05; // The cost of using memory in this resource (in Datacenter)
	private static double costPerStorage = 0.1; // The cost of using storage in this resource (in Datacenter)
	private static double costPerBw = 0.1; // The cost of using bw in this resource (in Datacenter)

	private static int VMmips = 1000; // VM Mips Needed
	private static long VMsize = 10000; // VM Image Size (MB)
	private static int VMram = 512; // VM Memory Needed (MB)
	private static long VMbw = 1000; // VM BW needed
	private static int VMpesNumber = 1; // VM Number of CPUs
	private static String VMvmm = "Xen"; // VM VMM name

	private static int numberOfVMs = 6;
	private static int numberOfPEs = 5;
	private static int numberOfHosts = 2;

	private static int pauseTime = -1;

	public static void main(String[] args) {
		Log.printLine("Starting CloudSim Project (Cloud Computing 98-1)...");

		try {
			CloudSim.init(1, Calendar.getInstance(), false);

			datacenter0 = createDatacenter("DataCenter_0");
			datacenter1 = createDatacenter("DataCenter_1");

			broker = createBroker("MainBroker");
			int brokerId = broker.getId();

			vmlist = createVM(brokerId, numberOfVMs);
			List<Cloudlet> cloudletList = createCloudlet(brokerId, 10);
			broker.submitVmList(vmlist);
			broker.submitCloudletList(cloudletList);

//			// Part 2
//
//			// Determine cloudlets deployed on each VM
//			for (Vm vm : vmlist) {
//				List<Integer> list = new LinkedList<>();
//				for (Cloudlet cloudlet : cloudletList) {
//					if (cloudlet.getVmId() == vm.getId())
//						list.add(cloudlet.getCloudletId());
//				}
//
//				Log.printLine("Cloudlets exist in VM #" + vm.getId() + ": " + list);
//			}
//
//			Scanner scanner = new Scanner(System.in);
//			while (true) {
//				Log.printLine();
//
//				// Determine available amount of processing capacity in each host for DC0 & DC1
//				List<Map<String, Double>> availMipsMap;
//				for (PowerDatacenter datacenter : new PowerDatacenter[] { datacenter0, datacenter1 }) {
//					availMipsMap = new LinkedList<>();
//					for (Host host : datacenter.getHostList()) {
//						Map<String, Double> hostAvailMipsMap = new HashMap<>();
//						hostAvailMipsMap.put("host #" + host.getId(), host.getAvailableMips());
//						availMipsMap.add(hostAvailMipsMap);
//					}
//
//					Log.printLine("Hosts available processing (Mips) in " + datacenter.getName() + ": " + availMipsMap);
//				}
//
//				// Determine utilization of each host in DC0 & DC1
//				List<Map<String, Double>> utilMap;
//				for (PowerDatacenter datacenter : new PowerDatacenter[] { datacenter0, datacenter1 }) {
//					utilMap = new LinkedList<>();
//					for (PowerHost host : datacenter.getVmAllocationPolicy()
//							.<PowerHostUtilizationHistory>getHostList()) {
//						Map<String, Double> hostUtilizationMap = new HashMap<>();
//						hostUtilizationMap.put("host #" + host.getId(), host.getUtilizationOfCpu() * 100);
//						utilMap.add(hostUtilizationMap);
//					}
//
//					Log.printLine("Utilization of hosts in " + datacenter.getName() + ": " + utilMap);
//				}
//
//				Log.printLine();
//
//				// Part 3
//				Log.printLine("Enter twice to continue. Or enter datacenter number for migration");
//				String input = scanner.nextLine();
//				if (input.equals("")) {
//					CloudSim.resumeSimulation();
//					break;
//				} else {
//					Log.printLine("Enter vm number to migrate in this datacenter");
//					String vmNum = scanner.nextLine();
//					Log.printLine("Enter host number to migrate on, in this datacenter");
//					String hostNum = scanner.nextLine();
//					try {
//						boolean result = manuallyMigrateVM(Integer.valueOf(input), Integer.valueOf(vmNum),
//								Integer.valueOf(hostNum));
//						if (result)
//							Log.printLine("Migration of VM #" + vmNum + " successfully done");
//						else
//							Log.printLine("Migration of VM #" + vmNum + " has not successfully done");
//					} catch (Exception e) {
//						e.printStackTrace();
//						Log.printLine("The migration has been cancelled due to an unexpected error");
//					}
//				}
//			}
			NetworkTopology.addLink(datacenter0.getHostList().get(0).getId(),datacenter0.Switchlist.get(3).getId(),10.0,10);
			NetworkTopology.addLink(datacenter0.Switchlist.get(3).getId(),datacenter0.getHostList().get(1).getId(),10.0,10);
			NetworkTopology.addLink(datacenter1.getHostList().get(0).getId(),datacenter1.Switchlist.get(6).getId(),10.0,10);
			NetworkTopology.addLink(datacenter1.Switchlist.get(6).getId(),datacenter1.getHostList().get(1).getId(),10.0,10);
			
			NetworkTopology.addLink(datacenter0.getId(), datacenter0.Switchlist.get(4).getId(), datacenter1.Switchlist.get(7).getId(), datacenter1.getId());
			
			//NetworkTopology.addLink(datacenter0.getId(), datacenter0.Switchlist.get(4).getId(), 10.0, 10);
			//NetworkTopology.addLink(datacenter1.Switchlist.get(7).getId(), datacenter1.getId(), 10.0, 10);
			
			//NetworkTopology.mapNode(datacenter0.getId(), datacenter0.Switchlist.get(4).getId());
			//NetworkTopology.mapNode(datacenter0.Switchlist.get(4).getId(), datacenter1.getId());
			
			//Log.printLine(datacenter0.Switchlist);
			//System.exit(1);


			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			List<Cloudlet> newCloudletsList = broker.getCloudletReceivedList();
			Log.printLine("Received " + newCloudletsList.size() + " cloudlets");
			printCloudletList(newCloudletsList);
			Log.printLine("Simulation has finished!");
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
	 * Create power datacenter with a given name
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
					storageList, 15);// 15 --> scheduling interval
			datacenter.setDisableMigrations(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		CreateNetwork(2, datacenter);
		return datacenter;
	}
	
	

	static void CreateNetwork(int numhost, PowerDatacenter datacenter) {
		// TODO Auto-generated method stub
		// Edge Switch
		EdgeSwitch edgeswitch[] = new EdgeSwitch[1];
		AggregateSwitch aggSwitch[] = new AggregateSwitch[1];

		for (int i = 0; i < 1; i++) {
			edgeswitch[i] = new EdgeSwitch("Edge" + i, NetworkConstants.EDGE_LEVEL, datacenter);
			aggSwitch[i] = new AggregateSwitch("Aggregate" + i, NetworkConstants.Agg_LEVEL, datacenter);
			// edgeswitch[i].uplinkswitches.add(null);
			aggSwitch[i].uplinkbandwidth = 500;
            edgeswitch[i].uplinkbandwidth = 100;
            
            aggSwitch[i].downlinkbandwidth = 500;
            edgeswitch[i].downlinkbandwidth = 100;
            
            aggSwitch[i].latency = 5;
            edgeswitch[i].latency = 10;
            
            aggSwitch[i].switching_delay = 5;
            edgeswitch[i].switching_delay = 10;
            
            edgeswitch[i].uplinkswitches.add(aggSwitch[i]);
            aggSwitch[i].downlinkswitches.add(edgeswitch[i]);
            
			datacenter.Switchlist.put(edgeswitch[i].getId(), edgeswitch[i]); // Edge switch connected with DC in the path
			datacenter.Switchlist.put(aggSwitch[i].getId(), aggSwitch[i]);
			// aggswitch[(int)
			// (i/Constants.AggSwitchPort)].downlinkswitches.add(edgeswitch[i]);
		}
		for (Host hs : datacenter.getHostList()) {
			PowerHost hs1 = (PowerHost) hs;
			hs1.bandwidth = NetworkConstants.BandWidthEdgeHost;
			int switchnum = (int) (hs.getId() / NetworkConstants.EdgeSwitchPort);
			edgeswitch[switchnum].powerHostList.put(hs.getId(), hs1);
			datacenter.HostToSwitchid.put(hs.getId(), edgeswitch[switchnum].getId());
			hs1.sw = edgeswitch[switchnum];
			List<PowerHost> hslist = hs1.sw.powerFinTimeListHost.get(0D);
			if (hslist == null) {
				hslist = new ArrayList<PowerHost>();
				hs1.sw.powerFinTimeListHost.put(0D, hslist);
			}
			hslist.add(hs1);

		}
	}

	/**
	 * Create power broker with a given name
	 * 
	 * @param name Name of the broker
	 */
	private static DatacenterBroker createBroker(String name) {
		DatacenterBroker broker;

		try {
			// Assign cloudlets to datacenters sequentially
            broker = new DatacenterBroker(name);

			// Assign 3 cloudlets to each datacenter
			//broker = new PowerDatacenterBroker3VMs(name);

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
			//Random r = new Random();
			cloudlet[i] = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel,
					utilizationModel, utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}

		return list;
	}

	/**
	 * Load workload data set from swf formatted file
	 * 
	 * @param brokerId    Id of broker
	 * @param filedir     workload data set file path
	 * @param startIndex  Start index to load
	 * @param finishIndex Last index to load
	 */
	private static List<Cloudlet> loadCloudlet(int brokerId, String filedir, int startIndex, int finishIndex)
			throws FileNotFoundException {
		List<Cloudlet> cloudletList;

		WorkloadFileReader workloadFileReader = new WorkloadFileReader(filedir, 1);
		cloudletList = workloadFileReader.generateWorkload().subList(startIndex, finishIndex);

		for (Cloudlet cloudlet : cloudletList)
			cloudlet.setUserId(brokerId);

		return cloudletList;
	}

	/**
	 * Execute VM migration manually Based on classes PowerTestVmAllocationPolicy &
	 * PowerDatacenter
	 * 
	 * @param targetDatacenterNum Destination datacenter id (0, 1, ...)
	 * @param VMnum               VM id to migrate (0, 1, ...)
	 * @param targetHostNum       Destination host id (0, 1, ...)
	 */
	private static boolean manuallyMigrateVM(int targetDatacenterNum, int VMnum, int targetHostNum) {
		Vm vm = vmlist.get(VMnum);
		PowerHost oldHost = (PowerHost) vm.getHost();

		PowerDatacenter datacenter;
		if (targetDatacenterNum == 0)
			datacenter = datacenter0;
		else if (targetDatacenterNum == 1)
			datacenter = datacenter1;
		else
			return false;

		if (targetHostNum < 0 || targetHostNum > numberOfHosts - 1)
			return false;

		if (VMnum < 0 || VMnum > numberOfVMs - 1)
			return false;

		// Check target host to pass the utilization condition
		PowerHost targetHost = datacenter.getVmAllocationPolicy().<PowerHostUtilizationHistory>getHostList()
				.get(targetHostNum);
		if (targetHost.getUtilizationOfCpu() * 100 >= 90) {
			Log.printLine("Host #" + targetHost.getId() + " utilization is >90. Searching for new target host...");
			targetHost = null;

			double sumMips = 0;
			for (int i = 0; i < vm.getNumberOfPes(); i++)
				sumMips += vm.getCurrentRequestedMips().get(i);

			for (PowerHostUtilizationHistory host1 : datacenter.getVmAllocationPolicy()
					.<PowerHostUtilizationHistory>getHostList()) {
				if ((1 - host1.getUtilizationOfCpu()) * host1.getTotalMips() <= sumMips) {
					targetHost = host1;
					break;
				}
			}
		}

		// Pack vm and target host to run migration
		Map<String, Object> migrate = new HashMap<>();
		if (targetHost != null) {
			migrate.put("vm", vm);
			migrate.put("host", targetHost);
		} else {
			return false;
		}

		if (oldHost == null) {
			Log.formatLine("Migration of VM #%d to Host #%d is started", vm.getId(), targetHost.getId());
		} else {
			Log.formatLine("Migration of VM #%d from Host #%d to Host #%d is started", vm.getId(), oldHost.getId(),
					targetHost.getId());
		}

		targetHost.addMigratingInVm(vm);
		datacenter.incrementMigrationCount();

		// Datacenter sends an event to itself with migration tag to run migration and
		// delay the simulation time
		datacenter.send(datacenter.getId(), vm.getRam() / ((double) targetHost.getBw() / (2 * 8000)),
				CloudSimTags.VM_MIGRATE, migrate);

		CloudSim.cancelAll(datacenter.getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
		datacenter.send(datacenter.getId(), datacenter.getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);

		return true;
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
		Log.printLine(String.format("Energy consumption: %.2f kWh",
				(datacenter0.getPower() + datacenter1.getPower()) / (3600 * 1000)));
		Log.printLine("Number of VM migrations: DC0 = " + datacenter0.getMigrationCount() + ", DC1 = "
				+ datacenter1.getMigrationCount());
		Log.printLine("Average response time: " + execTime * 1. / numOfSuccessCloudlets);
		Log.printLine();
	}
}
