package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class QLearningScheduling {
	private static List<Cloudlet> cloudletlist;
	private static List<Vm> vmList;
	private static List<Host> hostList;

	private static List<Vm> createVM(int userId, int vms) {

		// Creates a container to store VMs. This list is passed to the broker later
		LinkedList<Vm> list = new LinkedList<Vm>();

		// VM Parameters
		long size = 10000; // image size (MB)
		int ram = 512; // vm memory (MB)
		int mips = 1000;
		long bw = 1000;
		int pesNumber = 1; // number of cpus
		String vmm = "Xen"; // VMM name

		// create VMs
		Vm[] vm = new Vm[vms];

		for (int i = 0; i < vms; i++) {
			vm[i] = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
			// for creating a VM with a space shared scheduling policy for cloudlets:
			// vm[i] = Vm(i, userId, mips, pesNumber, ram, bw, size, vmm, new
			// CloudletSchedulerSpaceShared());

			list.add(vm[i]);
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
			Random r = new Random();
			cloudlet[i] = new Cloudlet(i, length + r.nextInt(2000), pesNumber, fileSize, outputSize, utilizationModel,
					utilizationModel, utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}

		return list;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Log.printLine("Starting Q Learning Scheduliung Test ...");
		try {
			int num_user = 1;
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;
			CloudSim.init(num_user, calendar, trace_flag);

			Datacenter datacenter0 = createDatacenter("Datacenter_0");
			Datacenter datacenter1 = createDatacenter("Datacenter_1");

			DatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			// Fourth step: Create one virtual machine
			vmList = new ArrayList<Vm>();

			// VM description
			int vmid = 0;
			int mips = 250;
			long size = 10000; // image size (MB)
			int ram = 512; // vm memory (MB)
			long bw = 1000;
			int pesNumber = 1; // number of cpus
			String vmm = "Xen"; // VMM name

			// create VMs
			for (int i = 0; i < 5; i++) {
				Vm vms = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
				// addling vms to the list
				vmList.add(vms);
				vmid++;
			}
			/*

			vmid = 20;
			mips = 190;
			size = 10000;
			ram = 1024;
			bw = 1000;
			pesNumber = 1;
			vmm = "Xen";

			for (int i = 0; i < 30; i++) {
				Vm vms = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
				vmList.add(vms);

				vmid++;
			}
			*
			*/

			// submit vm list to the broker
			broker.submitVmList(vmList);

			// Fifth step: Create two Cloudlets
			cloudletlist = new ArrayList<Cloudlet>();

			// Cloudlet properties
			int id = 0;
			long length = 40000;
			long fileSize = 300;
			long outputSize = 300;
			UtilizationModel utilizationModel = new UtilizationModelFull();

			for (int i = 0; i < 5; i++) {
				Vm vms = vmList.get(i);
				Cloudlet cloudlet1 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel,
						utilizationModel, utilizationModel);
				cloudlet1.setUserId(brokerId);

				id++;

				// add the cloudlets to the list
				cloudletlist.add(cloudlet1);
			}

			// submit cloudlet list to the broker
			broker.submitCloudletList(cloudletlist);

			// bind the cloudlets to the vms. This way, the broker
			// will submit the bound cloudlets only to the specific VM
			for (int i = 0; i < 5; i++) {
				Vm vms = vmList.get(i);
				Cloudlet cloudlet1 = cloudletlist.get(i);
				broker.bindCloudletToVm(cloudlet1.getCloudletId(), vms.getId());
			}

			CloudSim.terminateSimulation(86400.00);

			/*
			 * Implementing the machine learning algorithm Q learning as a scheduler for the
			 * cloudlets
			 */

			QLearningProcessor qScheduler = new QLearningProcessor(500, 0.9, 1, 0.5, 0.1, 3, vmList);
			qScheduler.train(vmList);
			System.exit(1); 
			
			for (List<Integer> s : qScheduler.getQ().keySet()) {
	            double[] value = qScheduler.getQ().get(s);
				System.out.println(Arrays.toString(value));
	        }
			Log.print(qScheduler.getQ().size());
	        /*
			Object[] keys = qScheduler.getQ().keySet().toArray();
			double[] value = qScheduler.getQ().get(keys[1]);
			Log.printLine(Arrays.toString(value));
			Log.print(qScheduler.getQ().size());
			*/
			System.exit(1); 

		    CloudSim.startSimulation(); 
		    CloudSim.stopSimulation(); 
		    
		    List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);
			
			 Log.printLine("Q Learning Scheduliung Test Finished.");

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Log.printLine("Unwanted errors happened.");
		}

	}

	private static Datacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 2800;
		int hostId = 0;
		peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
		for (int i = 0; i < 5; i++) {
			// 3. Create PEs and add these into a list.
			// peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id
			// and MIPS Rating

			// 4. Create Hosts with its id and list of PEs and add them to the list of
			// machines

			int ram = 36000; // host memory (MB)
			long storage = 1000000; // host storage
			int bw = 40000;

			hostList.add(new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList,
					new VmSchedulerTimeShared(peList))); // This is our first machine
			hostId++;
		}

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
				cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to submit
	// vms and cloudlets according
	// to the specific rules of the simulated scenario
	private static DatacenterBroker createBroker() {

		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * 
	 * @param list list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time"
				+ indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId()
						+ indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent
						+ dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}

	}

}
