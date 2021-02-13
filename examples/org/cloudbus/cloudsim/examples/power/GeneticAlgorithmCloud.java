/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples.power;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;


import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * An example showing how to create scalable simulations.
 */
public class GeneticAlgorithmCloud {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;
	private static List<Cloudlet> finalcloudletList;

	/** The vmlist. */
	private static List<Vm> finalvmlist;

	private static List<Vm> createVM(int userId, int vms) {

		// Creates a container to store VMs. This list is passed to the broker
		// later
		LinkedList<Vm> list = new LinkedList<Vm>();

		// VM Parameters
		long size = 10000; // image size (MB)
		int ram = 512; // vm memory (MB)
		int mips = 500;
		long bw = 10;
		int pesNumber = 4;// number of cpus
		String vmm = "Xen"; // VMM name
		Random rOb = new Random();
		// create VMs
		Vm[] vm = new Vm[vms];
		for (int i = 0; i < vms; i++) {
			vm[i] = new Vm(i, userId, mips + rOb.nextInt(500), pesNumber, ram,
					bw , size, vmm,
					new CloudletSchedulerSpaceShared());
			// for creating a VM with a space shared scheduling policy for
			// cloudlets:
			// vm[i] = Vm(i, userId, mips, pesNumber, ram, bw, size, priority,
			// vmm, new CloudletSchedulerSpaceShared());

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
		Random randomObj = new Random();

		for (int i = 0; i < cloudlets; i++) {
			int x = (int) (Math.random() * ((2000 - 1) + 1)) + 1;
			cloudlet[i] = new Cloudlet(i, (length ), pesNumber, fileSize,
					outputSize, utilizationModel, utilizationModel,
					utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}

		return list;
	}

	// //////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
     * @param args
	 */
	public static void main(String[] args) {
		Log.printLine("Starting GeneticAlgorithmCloudSimulation...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1; // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			@SuppressWarnings("unused")
			Datacenter datacenter0 = createDatacenter("Datacenter_0");
			@SuppressWarnings("unused")
			Datacenter datacenter1 = createDatacenter("Datacenter_1");

			// Third step: Create Broker
			DatacenterBroker1 broker = createBroker();
			int brokerId = broker.getId();

			// Fourth step: Create VMs and Cloudlets and send them to broker
			vmlist = createVM(brokerId, 25); // creating 20 vms
			cloudletList = createCloudlet(brokerId, 50); // creating 20 cloudlets
			
			List<Cloudlet> sortedList = new ArrayList<Cloudlet>();
			for(Cloudlet cloudlet:cloudletList){
				sortedList.add(cloudlet);
			}
			int numCloudlets=sortedList.size();
			for(int i=0;i<numCloudlets;i++){
				Cloudlet tmp=sortedList.get(i);
				int idx=i;
				for(int j=i+1;j<numCloudlets;j++)
				{
					if(sortedList.get(j).getCloudletLength()<tmp.getCloudletLength())
					{
						idx=j;
						tmp=sortedList.get(j);
					}
				}
				Cloudlet tmp2 = sortedList.get(i);
				sortedList.set(i, tmp);
				sortedList.set(idx,tmp2);
			}
			
			ArrayList<Vm> sortedListVm = new ArrayList<Vm>();
		ArrayList<Vm> toBeUsedVm = new ArrayList<Vm>();
		ArrayList<Vm> leftOutVm = new ArrayList<Vm>();
		for(Vm vm:vmlist){
			sortedListVm.add(vm);
		}
		int numVms=sortedListVm.size();
		
		for(int i=0;i<numVms;i++){
			Vm tmp=sortedListVm.get(i);
			int idx=i;
			if(i<numCloudlets)
				toBeUsedVm.add(tmp);
			else
				leftOutVm.add(tmp);
			for(int j=i+1;j<numVms;j++)
			{
				if(sortedListVm.get(j).getMips()>tmp.getMips())
				{
					idx=j;
					tmp=sortedListVm.get(j);
				}
			}
			Vm tmp2 = sortedListVm.get(i);
			sortedListVm.set(i, tmp);
			sortedListVm.set(idx,tmp2);
		}
		ArrayList<Chromosomes> initialPopulation = new ArrayList<Chromosomes>();
		for(int j=0;j<numCloudlets;j++)
		{
			ArrayList<Gene> firstChromosome = new ArrayList<Gene>();
			
			for(int i=0;i<numCloudlets;i++)
			{
				int k=(i+j)%numVms;
				k=(k+numCloudlets)%numCloudlets;
				Gene geneObj = new Gene(sortedList.get(i),sortedListVm.get(k));
				firstChromosome.add(geneObj);
			}
			Chromosomes chromosome = new Chromosomes(firstChromosome);
			initialPopulation.add(chromosome);
		}
		
		int populationSize=initialPopulation.size();
		Random random = new Random();
		for(int itr=0;itr<20;itr++)
		{
			int index1,index2;
			index1=random.nextInt(populationSize) % populationSize;
			index2=random.nextInt(populationSize) % populationSize;
			ArrayList<Gene> l1= new ArrayList<Gene>();
			l1=initialPopulation.get(index1).getGeneList();
			Chromosomes chromosome1 = new Chromosomes(l1);
			ArrayList<Gene> l2= new ArrayList<Gene>();
			l2=initialPopulation.get(index2).getGeneList();
			Chromosomes chromosome2 = new Chromosomes(l2);
			double rangeMin = 0.0f;
		    double rangeMax = 1.0f;
		    Random r = new Random();
		    double crossProb = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
			if(crossProb<0.5)
			{
				int i,j;
				i=random.nextInt(numCloudlets) % numCloudlets;
				j=random.nextInt(numCloudlets) % numCloudlets;
				Vm vm1 = l1.get(i).getVmFromGene();
				Vm vm2 = l2.get(j).getVmFromGene();
				chromosome1.updateGene(i, vm2);
				chromosome2.updateGene(j, vm1);
				initialPopulation.set(index1, chromosome1);
				initialPopulation.set(index2, chromosome2);
			}
			double mutProb = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
			if(mutProb<0.5)
			{
				int i;
				i=random.nextInt(populationSize) % populationSize;
				ArrayList<Gene> l= new ArrayList<Gene>();
				l=initialPopulation.get(i).getGeneList();
				Chromosomes mutchromosome = new Chromosomes(l);
				int j;
				j=random.nextInt(numCloudlets) % numCloudlets;
				Vm vm1 = sortedListVm.get(0);
				mutchromosome.updateGene(j,vm1);
			}
		}
		int fittestIndex=0;
		double time=1000000;
		
		for(int i=0;i<populationSize;i++)
		{
			ArrayList<Gene> l= new ArrayList<Gene>();
			l=initialPopulation.get(i).getGeneList();
			double sum=0;
			for(int j=0;j<numCloudlets;j++)
			{
				Gene g = l.get(j);
				Cloudlet c = g.getCloudletFromGene();
				Vm v = g.getVmFromGene();
				double temp = c.getCloudletLength()/v.getMips();
				sum+=temp;
			}
			if(sum<time)
			{
				time=sum;
				fittestIndex=i;
			}
		}
		
		ArrayList<Gene> result = new ArrayList<Gene>();
		result = initialPopulation.get(fittestIndex).getGeneList();
		
			List<Cloudlet> finalcloudletList = new ArrayList<Cloudlet>();
			List<Vm> finalvmlist = new ArrayList<Vm>();
			
			
			
			
			for(int i=0;i<result.size();i++)
			{
				finalcloudletList.add(result.get(i).getCloudletFromGene());
				finalvmlist.add(result.get(i).getVmFromGene());
				Vm vm=result.get(i).getVmFromGene();
				Log.printLine("############### VM FROM GENE  "+vm.getId());
			}
			
			broker.submitVmList(finalvmlist);
			broker.submitCloudletList(finalcloudletList);
			

			// Fifth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

			printCloudletList(newList);

			Log.printLine("CloudSimExample6 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}

	private static Datacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store one or more
		// Machines
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores. Therefore,
		// should
		// create a list to store these PEs before creating
		// a Machine.
		List<Pe> peList1 = new ArrayList<Pe>();

		int mips = 1000;

		// 3. Create PEs and add these into the list.
		// for a quad-core machine, a list of 4 PEs is required:
		peList1.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store
																// Pe id and
																// MIPS Rating
		peList1.add(new Pe(1, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(2, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(3, new PeProvisionerSimple(mips)));

		// Another list, for a dual-core machine
		List<Pe> peList2 = new ArrayList<Pe>();

		peList2.add(new Pe(0, new PeProvisionerSimple(mips)));
		peList2.add(new Pe(1, new PeProvisionerSimple(mips)));

		// 4. Create Hosts with its id and list of PEs and add them to the list
		// of machines
		int hostId = 0;
		int ram = 204800; // host memory (MB)
		long storage = 10000000; // host storage
		int bw = 100000;

		hostList.add(new Host(hostId, new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw), storage, peList1,
				new VmSchedulerTimeShared(peList1))); // This is our first
														// machine

		hostId++;

		hostList.add(new Host(hostId, new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw), storage, peList2,
				new VmSchedulerTimeShared(peList2))); // Second machine

		// To create a host with a space-shared allocation policy for PEs to
		// VMs:
		// hostList.add(
		// new Host(
		// hostId,
		// new CpuProvisionerSimple(peList1),
		// new RamProvisionerSimple(ram),
		// new BwProvisionerSimple(bw),
		// storage,
		// new VmSchedulerSpaceShared(peList1)
		// )
		// );

		// To create a host with a oportunistic space-shared allocation policy
		// for PEs to VMs:
		// hostList.add(
		// new Host(
		// hostId,
		// new CpuProvisionerSimple(peList1),
		// new RamProvisionerSimple(ram),
		// new BwProvisionerSimple(bw),
		// storage,
		// new VmSchedulerOportunisticSpaceShared(peList1)
		// )
		// );

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
		double costPerStorage = 0.1; // the cost of using storage in this
										// resource
		double costPerBw = 0.1; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are
																		// not
																		// adding
																		// SAN
																		// devices
																		// by
																		// now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics,
					new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	private static DatacenterBroker1 createBroker() {

		DatacenterBroker1 broker = null;
		try {
			broker = new DatacenterBroker1("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * 
	 * @param list
	 *            list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + indent
				+ "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}

	}
}
