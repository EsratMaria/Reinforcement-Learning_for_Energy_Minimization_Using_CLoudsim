package org.cloudbus.cloudsim.examples.QLearningScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;
import java.util.Map.Entry;

public class QlearningPowerDataCenter extends PowerDatacenter {

	private String cpulist;
	private boolean result = false;
	private double totalpower = 0;
	QLearningAgent qAgent;
	private List<Vm> allocatedVmList = new ArrayList<>();
	List<String> cpuHistoryList = new ArrayList<>();
	private String lastcpulist;
	private int hostid = 0;
	private int vmID =0;
	private double lastprocesstime = 0;
	private QPowerVmAllocationPolicy vmAllocationPolicy;
	private Map<Integer, String[]> cpuhistory = new HashMap<>();
	public static List<Double> allpower = new ArrayList<>();

	public QlearningPowerDataCenter(String name, DatacenterCharacteristics characteristics,
			QPowerVmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval,
			QLearningAgent qAgent) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
		setPower(0.0);
		setDisableMigrations(false);
		setCloudletSubmitted(-1);
		setMigrationCount(0);
		this.vmAllocationPolicy = vmAllocationPolicy;
		this.qAgent = qAgent;
		resetEnvironment();
	}

	public void resetEnvironment() {
		cpulist = "";
		for (int i = 0; i < Constants.NUMBER_OF_HOSTS; i++) {
			cpulist += "0";
		}
		lastcpulist = cpulist;
		for (Host host : getHostList()) {
			host.vmDestroyAll();
		}

	}

	public void getReward() {
		if (result == true) {
			double currentTime = CloudSim.clock();
			double timeDiff = currentTime - lastprocesstime;
			lastprocesstime = currentTime;
			double timeFrameDatacenterEnergy = 0.0;
			if (timeDiff > 0) {
				for (PowerHost powerhost : this.<PowerHost>getHostList()) {
					double previousUtilizationOfCpu = powerhost.getPreviousUtilizationOfCpu();
					double utilizationOfCpu = powerhost.getUtilizationOfCpu();
					if (previousUtilizationOfCpu != 0) {
						double timeFrameHostEnergy = powerhost.getEnergyLinearInterpolation(previousUtilizationOfCpu,
								utilizationOfCpu, timeDiff);
						timeFrameDatacenterEnergy += timeFrameHostEnergy;
						double reward = timeFrameHostEnergy;
						String[] savecpu = cpuhistory.get(powerhost.getId());
						lastcpulist = savecpu[0];
						cpulist = savecpu[1];
						qAgent.createLastState_idx(lastcpulist);
						qAgent.createState_idx(cpulist);
						qAgent.updateQList(powerhost.getId(), reward, lastcpulist, cpulist);
					}
				}
			}
			totalpower = timeFrameDatacenterEnergy;

			cpuHistoryList.add(cpulist);
		}
	}

	public String convertCPUUtilization(List<Host> hosts) {
		String convertresult = "";
		String convertList = "";
		for (Host host : hosts) {
			double cpuutil = getUtilizationOfCpuMips(host) * 100;
			if (cpuutil >= 0 && cpuutil < 10) {
				convertresult = "0";
			} else if (cpuutil >= 10 && cpuutil < 20) {
				convertresult = "1";
			} else if (cpuutil >= 20 && cpuutil < 30) {
				convertresult = "2";
			} else if (cpuutil >= 30 && cpuutil < 40) {
				convertresult = "3";
			} else if (cpuutil >= 40 && cpuutil < 50) {
				convertresult = "4";
			} else if (cpuutil >= 50 && cpuutil < 60) {
				convertresult = "5";
			} else if (cpuutil >= 60 && cpuutil < 70) {
				convertresult = "6";
			} else if (cpuutil >= 70 && cpuutil < 80) {
				convertresult = "7";
			} else if (cpuutil >= 80 && cpuutil < 90) {
				convertresult = "8";
			} else if (cpuutil >= 90 && cpuutil <= 100) {
				convertresult = "9";
			} else {
				System.out.println("CPU conversion error! ");
				break;
			}
			convertList += convertresult;
		}
		return convertList;
	}

	public double getUtilizationOfCpuMips(Host host) {
		double hostUtilizationMips = 0;
		for (Vm vm2 : host.getVmList()) {
			// calculate additional potential CPU usage of a migrating in VM
			hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2);
		}
		double s = hostUtilizationMips;
		double ss = host.getTotalMips();
		return hostUtilizationMips / host.getTotalMips();
	}

	/*
	 * I am over-riding the migration in the datacenter to modify with Q learning
	 * 
	 */
	@Override
	public void processVmMigrate(SimEvent ev, boolean ack) {

		Object tmp = ev.getData();
		if (!(tmp instanceof Map<?, ?>)) {
			throw new ClassCastException("The data object must be Map<String, Object>");
		}
		// qAgent.createAction(cpulist);

		@SuppressWarnings("unchecked")
		Map<String, Object> migrate = (HashMap<String, Object>) tmp;

		Vm vm = (Vm) migrate.get("vm");
		hostid = qAgent.createAction(cpulist);
		Host targethost = getHostList().get(hostid);
//		Log.printLine(targethost.getDatacenter().getName());
//		System.exit(1);
		migrate.put("targethost", targethost);
		Host host = (Host) migrate.get("targethost");
		/*
		 * choosing
		 * migratable vm
		 * 
		 * */
//		Host host = (Host) migrate.get("host");
//		vmID = qAgent.createAction(cpulist);
//		Vm targetVm = getVmList().get(vmID);
//		
//		migrate.put("targetVm", targetVm);
//		Vm vm = (Vm) migrate.get("targetVm");

		// destroy VM in src host
		getVmAllocationPolicy().deallocateHostForVm(vm);
		host.removeMigratingInVm(vm);
		boolean result = getVmAllocationPolicy().allocateHostForVm(vm, host);
		lastcpulist = cpulist;
		cpulist = convertCPUUtilization(getHostList());
		allocatedVmList.add(vm);
		if (result != true) {
			double reward = 1000000000;
			qAgent.updateQList(hostid, reward, lastcpulist, cpulist);
			Log.printLine("[Datacenter.processVmMigrate] VM allocation to the destination host failed");
			System.exit(0);
		} else {
			String[] cpus = new String[2];
			cpus[0] = lastcpulist;
			cpus[1] = cpulist;
			cpuhistory.put(hostid, cpus);
		}

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();

			if (result) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(ev.getSource(), CloudSimTags.VM_CREATE_ACK, data);
		}

		Log.formatLine("%.2f: Migration of VM #%d to Host #%d is completed", CloudSim.clock(), vm.getId(),
				host.getId());
		vm.setInMigration(false);
	}

	/**
	 * Updates processing of each cloudlet running in this PowerDatacenter. It is
	 * necessary because Hosts and VirtualMachines are simple objects, not entities.
	 * So, they don't receive events and updating cloudlets inside them must be
	 * called from the outside.
	 * 
	 * @pre $none
	 * @post $none
	 */
	@Override
	protected void updateCloudletProcessing() {
		if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
			CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
			schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			return;
		}
		double currentTime = CloudSim.clock();
		;
		// if some time passed since last processing
		if (currentTime > getLastProcessTime()) {
			Log.printLine(currentTime + " ");

			double minTime = updateCloudetProcessingWithoutSchedulingFutureEventsForce();

			if (!isDisableMigrations()) {
				List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(getVmList());

				if (migrationMap != null) {
					for (Map<String, Object> migrate : migrationMap) {
						Vm vm = (Vm) migrate.get("vm");
						PowerHost targetHost = (PowerHost) migrate.get("host");
						PowerHost oldHost = (PowerHost) vm.getHost();

						if (oldHost == null) {
							Log.formatLine("%.2f: Migration of VM #%d to Host #%d is started", currentTime, vm.getId(),
									targetHost.getId());
						} else {
							Log.formatLine("%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
									currentTime, vm.getId(), oldHost.getId(), targetHost.getId());
						}

						targetHost.addMigratingInVm(vm);
						incrementMigrationCount();

						/** VM migration delay = RAM / bandwidth **/
						// we use BW / 2 to model BW available for migration purposes, the other
						// half of BW is for VM communication
						// around 16 seconds for 1024 MB using 1 Gbit/s network
						send(getId(), vm.getRam() / ((double) targetHost.getBw() / (2 * 8000)), CloudSimTags.VM_MIGRATE,
								migrate);
					}
				}
			}

			// schedules an event to the next time
			if (minTime != Double.MAX_VALUE) {
				CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
				send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			}
			getReward();
			setLastProcessTime(currentTime);
		}
	}

	@Override
	protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
		double currentTime = CloudSim.clock();
		double minTime = Double.MAX_VALUE;
		double timeDiff = currentTime - getLastProcessTime();
		double timeFrameDatacenterEnergy = 0.0;

		Log.printLine("\n\n--------------------------------------------------------------\n\n");
		Log.formatLine("New resource usage for the time frame starting at %.2f:", currentTime);

		for (PowerHost host : this.<PowerHost>getHostList()) {
			Log.printLine();

			double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
			if (time < minTime) {
				minTime = time;
			}
			Log.printLine();
			Log.printLine("=============================================");
			Log.formatLine("| %.2f: [Host #%d] utilization is %.2f%%    |", currentTime, host.getId(),
					host.getUtilizationOfCpu() * 100);
			Log.printLine("=============================================");
		}

		if (timeDiff > 0) {
			Log.formatLine("\nEnergy consumption for the last time frame from %.2f to %.2f:", getLastProcessTime(),
					currentTime);

			for (PowerHost host : this.<PowerHost>getHostList()) {
				double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
				double utilizationOfCpu = host.getUtilizationOfCpu();
				double timeFrameHostEnergy = host.getEnergyLinearInterpolation(previousUtilizationOfCpu,
						utilizationOfCpu, timeDiff);
				timeFrameDatacenterEnergy += timeFrameHostEnergy;

				Log.printLine();
				Log.formatLine("%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%", currentTime,
						host.getId(), getLastProcessTime(), previousUtilizationOfCpu * 100, utilizationOfCpu * 100);
				Log.formatLine("%.2f: [Host #%d] energy is %.2f W*sec", currentTime, host.getId(), timeFrameHostEnergy);
			}

			Log.formatLine("\n%.2f: Data center's energy is %.2f W*sec\n", currentTime, timeFrameDatacenterEnergy);
		}

		setPower(getPower() + timeFrameDatacenterEnergy);

		checkCloudletCompletion();

		/** Remove completed VMs **/
		for (PowerHost host : this.<PowerHost>getHostList()) {
			for (Vm vm : host.getCompletedVms()) {
				getVmAllocationPolicy().deallocateHostForVm(vm);
				getVmList().remove(vm);
				Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
			}
		}

		Log.printLine();

		if (currentTime > Constants.SIMULATION_LIMIT) {
			allpower.add(getPower());
		}

		setLastProcessTime(currentTime);
		return minTime;
	}

	@Override
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		super.processCloudletSubmit(ev, ack);
		setCloudletSubmitted(CloudSim.clock());
	}
	
	/**
	 * Gets the under allocated mips.
	 *
	 * @return the under allocated mips
	 */
	public Map<String, List<List<Double>>> getUnderAllocatedMips() {
		Map<String, List<List<Double>>> underAllocatedMips = new HashMap<String, List<List<Double>>>();
		for (PowerHost host : this.<PowerHost>getHostList()) {
			for (Entry<String, List<List<Double>>> entry : host.getUnderAllocatedMips().entrySet()) {
				if (!underAllocatedMips.containsKey(entry.getKey())) {
					underAllocatedMips.put(entry.getKey(), new ArrayList<List<Double>>());
				}
				underAllocatedMips.get(entry.getKey()).addAll(entry.getValue());

			}
		}
		return underAllocatedMips;
	}

}
