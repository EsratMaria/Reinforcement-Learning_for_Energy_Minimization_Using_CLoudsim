package org.cloudbus.cloudsim.examples.power;
/*RR WORST
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.lang.Math;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;

/**
 * DatacentreBroker represents a broker acting on behalf of a user. It hides VM
 * management, as vm creation, submission of cloudlets to this VMs and
 * destruction of VMs.
 *
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class DatacenterBrokerGA extends SimEntity {

    /**
     * The vm list.
     */
    protected List<? extends Vm> vmList;

    /**
     * The vms created list.
     */
    protected List<? extends Vm> vmsCreatedList;

    /**
     * The cloudlet list.
     */
    protected List<? extends Cloudlet> cloudletList;

    /**
     * The cloudlet submitted list.
     */
    protected List<? extends Cloudlet> cloudletSubmittedList;

    /**
     * The cloudlet received list.
     */
    protected List<? extends Cloudlet> cloudletReceivedList;

    /**
     * The cloudlets submitted.
     */
    protected int cloudletsSubmitted;

    /**
     * The vms requested.
     */
    protected int vmsRequested;

    /**
     * The vms acks.
     */
    protected int vmsAcks;

    /**
     * The vms destroyed.
     */
    protected int vmsDestroyed;

    /**
     * The datacenter ids list.
     */
    protected List<Integer> datacenterIdsList;

    /**
     * The datacenter requested ids list.
     */
    protected List<Integer> datacenterRequestedIdsList;

    /**
     * The vms to datacenters map.
     */
    protected Map<Integer, Integer> vmsToDatacentersMap;

    /**
     * The datacenter characteristics list.
     */
    protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;

    /**
     * Created a new DatacenterBroker object.
     *
     * @param name name to be associated with this entity (as required by
     * Sim_entity class from simjava package)
     * @throws Exception the exception
     * @pre name != null
     * @post $none
     */
    public DatacenterBrokerGA(String name) throws Exception {
        super(name);

        setVmList(new ArrayList<Vm>());
        setVmsCreatedList(new ArrayList<Vm>());
        setCloudletList(new ArrayList<Cloudlet>());
        setCloudletSubmittedList(new ArrayList<Cloudlet>());
        setCloudletReceivedList(new ArrayList<Cloudlet>());

        cloudletsSubmitted = 0;
        setVmsRequested(0);
        setVmsAcks(0);
        setVmsDestroyed(0);

        setDatacenterIdsList(new LinkedList<Integer>());
        setDatacenterRequestedIdsList(new ArrayList<Integer>());
        setVmsToDatacentersMap(new HashMap<Integer, Integer>());
        setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
    }

    /**
     * This method is used to send to the broker the list with virtual machines
     * that must be created.
     *
     * @param list the list
     * @pre list !=null
     * @post $none
     */
    public void submitVmList(List<? extends Vm> list) {
        getVmList().addAll(list);
    }

    /**
     * This method is used to send to the broker the list of cloudlets.
     *
     * @param list the list
     * @pre list !=null
     * @post $none
     */
    public void submitCloudletList(List<? extends Cloudlet> list) {
        getCloudletList().addAll(list);
    }

    /**
     * Specifies that a given cloudlet must run in a specific virtual machine.
     *
     * @param cloudletId ID of the cloudlet being bount to a vm
     * @param vmId the vm id
     * @pre cloudletId > 0
     * @pre id > 0
     * @post $none
     */
    public void bindCloudletToVm(int cloudletId, int vmId) {
        CloudletList.getById(getCloudletList(), cloudletId).setVmId(vmId);
    }

    /**
     * Processes events available for this Broker.
     *
     * @param ev a SimEvent object
     * @pre ev != null
     * @post $none
     */
    @Override
    public void processEvent(SimEvent ev) {
        switch (ev.getTag()) {
            // Resource characteristics request
            case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
                processResourceCharacteristicsRequest(ev);
                break;
            // Resource characteristics answer
            case CloudSimTags.RESOURCE_CHARACTERISTICS:
                processResourceCharacteristics(ev);
                break;
            // VM Creation answer
            case CloudSimTags.VM_CREATE_ACK:
                processVmCreate(ev);
                break;
            // A finished cloudlet returned
            case CloudSimTags.CLOUDLET_RETURN:
                processCloudletReturn(ev);
                break;
            // if the simulation finishes
            case CloudSimTags.END_OF_SIMULATION:
                shutdownEntity();
                break;
            // other unknown tags are processed by this method
            default:
                processOtherEvent(ev);
                break;
        }
    }

    /**
     * Process the return of a request for the characteristics of a
     * PowerDatacenter.
     *
     * @param ev a SimEvent object
     * @pre ev != $null
     * @post $none
     */
    protected void processResourceCharacteristics(SimEvent ev) {
        DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
        getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

        if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
            setDatacenterRequestedIdsList(new ArrayList<Integer>());
            createVmsInDatacenter(getDatacenterIdsList().get(0));
        }
    }

    /**
     * Process a request for the characteristics of a PowerDatacenter.
     *
     * @param ev a SimEvent object
     * @pre ev != $null
     * @post $none
     */
    protected void processResourceCharacteristicsRequest(SimEvent ev) {
        setDatacenterIdsList(CloudSim.getCloudResourceList());
        setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

        //p//Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloud Resource List received with "
        //p//	+ getDatacenterIdsList().size() + " resource(s)");
        for (Integer datacenterId : getDatacenterIdsList()) {
            sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
        }
    }

    /**
     * Process the ack received due to a request for VM creation.
     *
     * @param ev a SimEvent object
     * @pre ev != null
     * @post $none
     */
    protected void processVmCreate(SimEvent ev) {
        int[] data = (int[]) ev.getData();
        int datacenterId = data[0];
        int vmId = data[1];
        int result = data[2];

        if (result == CloudSimTags.TRUE) {
            getVmsToDatacentersMap().put(vmId, datacenterId);
            getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
            //p//Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
            //p//	+ " has been created in Datacenter #" + datacenterId + ", Host #"
            //p//+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
        } else {
            //p//Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
            //p//	+ " failed in Datacenter #" + datacenterId);
        }

        incrementVmsAcks();

        // all the requested VMs have been created
        if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
            submitCloudlets();
        } else {
            // all the acks received, but some VMs were not created
            if (getVmsRequested() == getVmsAcks()) {
                // find id of the next datacenter that has not been tried
                for (int nextDatacenterId : getDatacenterIdsList()) {
                    if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
                        createVmsInDatacenter(nextDatacenterId);
                        return;
                    }
                }

                // all datacenters already queried
                if (getVmsCreatedList().size() > 0) { // if some vm were created
                    submitCloudlets();
                } else { // no vms created. abort
                    //p//Log.printLine(CloudSim.clock() + ": " + getName()
                    //p//	+ ": none of the required VMs could be created. Aborting");
                    finishExecution();
                }
            }
        }
    }

    /**
     * Process a cloudlet return event.
     *
     * @param ev a SimEvent object
     * @pre ev != $null
     * @post $none
     */
    protected void processCloudletReturn(SimEvent ev) {
        Cloudlet cloudlet = (Cloudlet) ev.getData();
        getCloudletReceivedList().add(cloudlet);
        //Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId()
        //		+ " received");
        cloudletsSubmitted--;
        if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
            //p//Log.printLine(CloudSim.clock() + ": " + getName() + ": All Cloudlets executed. Finishing...");
            clearDatacenters();
            finishExecution();
        } else { // some cloudlets haven't finished yet
            if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
                // all the cloudlets sent finished. It means that some bount
                // cloudlet is waiting its VM be created
                clearDatacenters();
                createVmsInDatacenter(0);
            }

        }
    }

    /**
     * Overrides this method when making a new and different type of Broker.
     * This method is called by {@link #body()} for incoming unknown tags.
     *
     * @param ev a SimEvent object
     * @pre ev != null
     * @post $none
     */
    protected void processOtherEvent(SimEvent ev) {
        if (ev == null) {
            //p//Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
            return;
        }

        //p//Log.printLine(getName() + ".processOtherEvent(): "
        //p//	+ "Error - event unknown by this DatacenterBroker.");
    }

    /**
     * Create the virtual machines in a datacenter.
     *
     * @param datacenterId Id of the chosen PowerDatacenter
     * @pre $none
     * @post $none
     */
    protected void createVmsInDatacenter(int datacenterId) {
        // send as much vms as possible for this datacenter before trying the next one
        int requestedVms = 0;
        String datacenterName = CloudSim.getEntityName(datacenterId);
        for (Vm vm : getVmList()) {
            if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
                //p//Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
                //p//		+ " in " + datacenterName);
                sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
                requestedVms++;
            }
        }

        getDatacenterRequestedIdsList().add(datacenterId);

        setVmsRequested(requestedVms);
        setVmsAcks(0);
    }

    double find_max(double[] x) {
        double max = x[0];
        for (int i = 1; i < x.length; i++) {
            if (x[i] > max) {
                max = x[i];
            }
        }
        return max;
    }

    /**
     * Submit cloudlets to the created VMs.
     *
     * @pre $none
     * @post $none
     */
    protected void submitCloudlets() {
        int vmIndex = 0;
        int[][] b = new int[100][100];
        int[][] final_matrix = new int[100][100];
        double final_makespans2[] = new double[50];
        int needed_index = 0;
        double current_min_makespan, min_one = 9999.99;
        double min_makespan = 9999.99;
        double makespans2[] = new double[50];

        //Genetic Algorithm
        //INITIAL POPULATION
        //first 18 chromosomes are made as random
        Random rand = new Random();
        int[][] a = new int[100][100];
        int cno = 20;
        for (int m = 0; m < (cno - 2); m++) {
            for (int n = 0; n < getCloudletList().size(); n++) {
                a[m][n] = rand.nextInt(getVmsCreatedList().size());
            }
        }

        //the 19th chromosome
        //Log.printLine("FCFS\ncloudlet id -  - vm id");
        for (Cloudlet cloudlet : getCloudletList()) {
            Vm vm;
            // if user didn't bind this cloudlet and it has not been executed yet
            if (cloudlet.getVmId() == -1) {
                vm = getVmsCreatedList().get(vmIndex);
            } else { // submit to the specific vm
                vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
                if (vm == null) { // vm was not created
                    //p//	Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
                    //p//		+ cloudlet.getCloudletId() + ": bount VM not available");
                    continue;
                }
            }
            //Log.printLine("     "+cloudlet.getCloudletId() +"	    - -  "+vm.getId());
            cloudlet.setVmId(vm.getId());
            //fcfs is added as the 21st chromosome
            a[cno - 2][cloudlet.getCloudletId()] = vm.getId();
            cloudletsSubmitted++;
            vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
            getCloudletSubmittedList().add(cloudlet);
        }

        //array = vmid and vmmips
        double vmmips[] = new double[50];
        int vmmipsc = 0;
        System.out.println("VMID  MIPS");
        for (Vm mipsvm : getVmsCreatedList()) {
            vmmips[vmmipsc] = mipsvm.getMips();
            System.out.println(vmmipsc + "    " + vmmips[vmmipsc]);
            vmmipsc++;

        }

        //array = cloudletid and cloudlength
        double cllen[] = new double[100];
        int cllenc = 0;
        System.out.println("cloudletID  length");
        for (Cloudlet lengthcl : getCloudletSubmittedList()) {
            cllen[cllenc] = lengthcl.getCloudletLength();
            System.out.println(" " + cllenc + "	    " + cllen[cllenc]);
            cllenc++;

        }

        //vms are sorted based on mips value
        //sort_list = list which is sorted based on mips
        //temp_list = vm list
        //longest task - fastest processor//LPFP
        List<Vm> sortList = new ArrayList<Vm>();
        ArrayList<Vm> tempList = new ArrayList<Vm>();
        for (Vm vm1 : getVmsCreatedList()) {
            //adding each element of vm list to temp list
            tempList.add(vm1);
        }

        int totalvms = tempList.size();
        for (int i = 0; i < totalvms; i++) {
            Vm fastestvm = tempList.get(0);
            //assuming that the first element in temp list is fastest one
            for (Vm checkvm : tempList) {	//linear search
                if (fastestvm.getMips() < checkvm.getMips()) //comparing each element with fastest element
                {
                    fastestvm = checkvm;
                }
            }
            sortList.add(fastestvm);//add only the fastest elt to sortlist
            tempList.remove(fastestvm);
        }

        int count1 = 1;
        int sortlistofvm[] = new int[50];
        System.out.println("\nvm list sorted according to mips value");
        System.out.println("vm Id    mips");
        for (Vm printvm : sortList) {
            Log.printLine("  " + printvm.getId() + "    " + printvm.getMips());
            sortlistofvm[count1] = printvm.getId();
            count1++;
        }

        //based on cloudlet length
        List<Cloudlet> sortList1 = new ArrayList<Cloudlet>(); //sort_list = list which is sorted based on time
        ArrayList<Cloudlet> tempList1 = new ArrayList<Cloudlet>(); //temp_list = cloud_let list
        for (Cloudlet cloudlet1 : getCloudletList()) {
            tempList1.add(cloudlet1);	//adding each element of cloud list to temp list
        }
        int totalCloudlets = tempList1.size();
        for (int i = 0; i < totalCloudlets; i++) {
            Cloudlet largestCloudlet = tempList1.get(0);
            //assuming that the first elt in temp list is smallest one
            for (Cloudlet checkCloudlet : tempList1) {	//linear search
                if (largestCloudlet.getCloudletLength() < checkCloudlet.getCloudletLength()) //comparing each elt with smallest elt
                {
                    largestCloudlet = checkCloudlet;
                }
            }

            sortList1.add(largestCloudlet);//add only the smallest elt to sortlist
            tempList1.remove(largestCloudlet);

        }

        int count3 = 1;
        int sortlistofcl[] = new int[100];
        int temp3[] = new int[100];
        System.out.println("\ncl based on length");
        System.out.println("cl Id    length");
        for (Cloudlet printCloudlet : sortList1) {
            Log.printLine(printCloudlet.getCloudletId() + " - " + printCloudlet.getCloudletLength());
            sortlistofcl[count3] = printCloudlet.getCloudletId();
            count3++;
        }

        //since the first element in sortlistof vm has 0
        //i am creating a new array temp where the first element is excluded
        for (int c = 1, c2 = 0; c < (getVmsCreatedList().size() + 1); c++) {
            temp3[c2] = sortlistofvm[c];
            c2++;
        }
        //assigning the temp list to array a
        //temp list will be last chromosome
        int n1 = 0;
        for (int n = 1; n < getCloudletList().size(); n++) {
            a[cno - 1][sortlistofcl[n]] = temp3[n1];
            n1++;
            n1 = n1 % getVmsCreatedList().size();
        }

        //the array of initial population is printed
        System.out.print("\nINITIAL POPULATION\n  ");
        System.out.print("  ");
        for (int n = 0; n < getCloudletList().size(); n++) {
            System.out.print("    " + n);
        }
        System.out.println("\n");
        for (int m = 0; m < cno; m++) {
            System.out.print(m + " - - ");
            for (int n = 0; n < getCloudletList().size(); n++) {
                System.out.print(a[m][n] + "   ");
            }
            System.out.println("\n");
        }
        //initial population is done and is in a[][]

        double etc[][] = new double[80][30];
        double throughput;

        for (int i = 0; i < getCloudletList().size(); i++) {
            for (int j = 0; j < getVmsCreatedList().size(); j++) {
                etc[i][j] = cllen[i] / vmmips[j];
            }
        }

        //FITNESS FUNCTION
        int k = -1, w1 = 0;
        double make[] = new double[getCloudletList().size()];
        double makespans[] = new double[cno];
        for (int i = 0; i < cno; i++) {
            makespans[i] = 0.0;
        }
        for (int i = 0; i < getVmsCreatedList().size(); i++) {
            make[i] = 0.0;
        }
        for (int m = 0; m < cno; m++) {
            k++;
            for (int q = 0; q < getVmsCreatedList().size(); q++) {
                for (int n = 0; n < getCloudletList().size(); n++) {
                    if (a[m][n] == q) {
                        make[w1] += etc[n][q];
                    }
                }
                w1++;

            }
            w1 = 0;
            makespans[k] = find_max(make);
        }

        System.out.println("MAKESPANS");
        for (int w = 0; w < cno; w++) {
            System.out.println(" " + w + "  " + makespans[w]);
        }

        for (int w = 0; w < cno; w++) {
            makespans2[w] = makespans[w];
        }

        //SELECTION
        //roulette wheel as per algo selects one out of the whole!
        /*double total_fitness = 0.0;
			for(int w=0;w<cno;w++)
				total_fitness+=makespans[w];
         */
        //System.out.println("the total fitness is "+total_fitness);
        current_min_makespan = makespans2[0];
        for (int w = 1; w < cno; w++) {
            if (current_min_makespan > makespans2[w]) {
                current_min_makespan = makespans2[w];
                //needed_index=w;
            }
        }
        throughput = getCloudletList().size() / current_min_makespan;
        current_min_makespan += 1 / throughput;
        int count = 0, count_same = 0, ind = 0, countm = 0;
        double[] index1 = new double[5];
        while (!(Math.abs(min_one) == Math.abs(current_min_makespan) && count_same == 3)) {
            for (int m = 0; m < cno; m++) {
                for (int n = 0; n < getCloudletList().size(); n++) {
                    final_matrix[m][n] = b[m][n];
                }
            }
            for (int q = 0; q < cno; q++) {
                final_makespans2[q] = makespans2[q];
            }
            //min_makespan = total_fitness;
            if (min_one > current_min_makespan) {
                min_one = current_min_makespan;
            } else if (Math.abs(min_one) == Math.abs(current_min_makespan)) {
                index1[ind++] = min_one;
                if (ind == 0) {
                    break;
                }
                if (Math.abs(index1[--ind]) == Math.abs(current_min_makespan)) {
                    count_same = 2;
                    if (ind == 0) {
                        break;
                    }
                    if (Math.abs(index1[--ind]) == Math.abs(current_min_makespan)) {
                        count_same = 3;
                    } else {
                        ind++;
                    }
                } else {
                    ind++;
                }
            } else {
            }

            int[] select = new int[100];
            int[] for_selection = new int[100];
            for (int i = 0; i < cno; i++) {
                for_selection[i] = i;
            }
            double[] sort_makespan = new double[100];
            int temps;
            double temps1;
            for (int i = 0; i < cno; i++) {
                for (int j = 0; j < cno - i; j++) {
                    if (makespans2[i] > makespans2[j]) {
                        temps1 = makespans2[i];
                        makespans2[i] = makespans2[j];
                        makespans2[j] = temps1;
                        temps = for_selection[i];
                        for_selection[i] = for_selection[j];
                        for_selection[j] = temps;

                    }
                }
            }
            /*for(int i1=0;i1<cno;i1++)
			{
				double random_number = Math.random() * (total_fitness);
				System.out.println("the random number is "+random_number);
				double sums = 0.0;

				int i;
				for(i=0;i<cno;i++)
				{
					if(sums<random_number)
					{
						sums+=makespans2[i];
					}
					else 
						break;
				}
				select[i1]=--i;
				
			//roulett ends here
			}*/
            Random random = new Random();
            for (int i1 = 0; i1 < cno; i1++) {
                int random_number = random.nextInt((cno / 2) - 1);
                select[i1] = for_selection[random_number];
            }
            //for the newer populations

            System.out.println("The selected chromosomes are");
            for (int i2 = 0; i2 < cno; i2++) {
                System.out.println("  " + select[i2]);
            }

            for (int i = 0; i < cno; i++) {
                for (int i3 = 0; i3 < getCloudletList().size(); i3++) {
                    b[i][i3] = a[select[i]][i3];
                }
            }
            System.out.print("\nNEXT POPULATION\n  ");
            System.out.print("  ");
            for (int n = 0; n < getCloudletList().size(); n++) {
                System.out.print("    " + n);
            }
            System.out.println("\n");
            for (int m = 0; m < cno; m++) {
                System.out.print(m + " - - ");
                for (int n = 0; n < getCloudletList().size(); n++) {
                    System.out.print(b[m][n] + "   ");
                }
                System.out.println("\n");
            }

            //CROSSOVER
            int temp1 = 0;
            for (int m = 0; m < cno; m = m + 2) {
                for (int n = 0, o = getVmsCreatedList().size(); n < getVmsCreatedList().size() && o < getCloudletList().size(); n++, o++) {
                    temp1 = b[m][n];
                    b[m][n] = b[m + 1][o];
                    b[m + 1][o] = temp1;
                }

            }
            //POPULATION AFTER CROSSOVER
            System.out.print("After crossover\n");
            for (int m = 0; m < cno; m++) {
                System.out.print(m + " - - ");
                for (int n = 0; n < getCloudletList().size(); n++) {
                    System.out.print(b[m][n] + "   ");
                }
                System.out.println("\n");
            }

            //To PRINT THE MAKESPAN OF POPULATION AFTER CROSSOVER
            double[] make2 = new double[getCloudletList().size()];
            for (int r = 0; r < cno; r++) {
                makespans2[r] = 0;
            }
            for (int r = 0; r < cno; r++) {
                make2[r] = 0;
            }
            //total_fitness=0;
            int k12 = -1, w2 = 0;

            for (int m = 0; m < cno; m++) {
                k12++;
                for (int q = 0; q < getVmsCreatedList().size(); q++) {
                    for (int n = 0; n < getCloudletList().size(); n++) {
                        if (b[m][n] == q) {
                            make2[w2] += etc[n][q];
                        }
                    }
                    w2++;

                }
                w2 = 0;
                makespans2[k12] = find_max(make2);
            }
            System.out.println("MAKESPANS");
            for (int w = 0; w < cno; w++) {
                System.out.println(" " + w + "  " + makespans2[w]);
            }

            for (int w = 0; w < cno; w++) //total_fitness+=makespans2[w];
            {
                current_min_makespan = makespans2[0];
            }
            for (int w = 1; w < cno; w++) {
                if (current_min_makespan > makespans2[w]) {
                    current_min_makespan = makespans2[w];
                    throughput = getCloudletList().size() / current_min_makespan;
                }
            }
            current_min_makespan += 1 / throughput;
            count++;
            countm++;

            if (countm > 5) {
                countm = 0;
                //MUTATION
                System.out.print("Mutation\n");
                for (int m = 0; m < cno; m++) {
                    b[m][rand.nextInt(getVmsCreatedList().size())] = rand.nextInt(getVmsCreatedList().size());

                }

                //POPULATION AFTER MUTATION
                System.out.print("After mutation\n");
                for (int m = 0; m < cno; m++) {
                    System.out.print(m + " - - ");
                    for (int n = 0; n < getCloudletList().size(); n++) {
                        System.out.print(b[m][n] + "   ");
                    }
                    System.out.println("\n");
                }
            }
            System.out.println("minimum one so far: " + min_one + " current minimum makespan : " + current_min_makespan);
        }

        System.out.println("Final MAKESPANS :");
        for (int w = 0; w < cno; w++) {
            System.out.println(final_makespans2[w]);
        }

        //finding minimum of all the chromosomes in this population
        double needed_one = final_makespans2[0];
        for (int w = 1; w < cno; w++) {
            if (needed_one > final_makespans2[w]) {
                needed_one = final_makespans2[w];
                needed_index = w;
            }
        }
        System.out.println("The Allocation is :");
        int[] last = new int[100];
        for (int w = 0; w < getCloudletList().size(); w++) {
            System.out.println(w + "---" + final_matrix[needed_index][w] + "  ");
            last[w] = final_matrix[needed_index][w];
        }
        System.out.print("\n");
        System.out.println("With makespan " + final_makespans2[needed_index]);
        System.out.println("count : " + count);
        System.out.println("THROUGHPUT : " + 40 / final_makespans2[needed_index]);

        //GA DONE!!! :D
        //IMPROVED ROUND ROBIN
        int cl[] = new int[100];
        int f[] = new int[100];
        int kl = 0;
        int i = 0, flag = 0, pp2 = 0;
        int s;
        int vis[] = new int[100];
        int finalarr2[] = new int[400];

        for (int k1 = 0; k1 < getCloudletList().size(); k1++) {
            flag = 0;
            s = 0;
            for (int h = 0; h < i; h++) {
                if (vis[h] == last[k1]) {
                    flag = 1;
                }
            }
            if (flag == 0) {
                vis[i++] = last[k1];
                for (int p = 0; p < getCloudletList().size(); p++) {
                    if (last[k1] == last[p]) {
                        cl[s] = p;
                        s++;
                    }
                }
                System.out.print("VM :" + last[k1] + " cloudlets ");
                for (int h1 = 0; h1 < s; h1++) {
                    System.out.print(cl[h1] + " ");
                }

                System.out.print("\n\n");
            }
            finalarr2 = setprior(cl, s, cllen);
            for (int h1 = 0; h1 < s; h1++) {
                f[kl++] = finalarr2[h1];
                //System.out.println("f  "+finalarr2[h1]);
            }

        }
        System.out.println("\nSCHEDULED CLOUDLETS BASED ON IMPROVED ROUND ROBIN\nCLOUDLETID\t\tVMID");
        List<Cloudlet> cloudList2 = getCloudletList();
        for (int h1 = 0; h1 < kl; h1++) {
            System.out.println("   " + f[h1] + "\t\t\t" + last[f[h1]]);
            List<Cloudlet> cloudlet = getCloudletList();
            cloudlet.get(f[h1]).setVmId(last[f[h1]]);
        }

        //submission
        List<Cloudlet> cloudList = getCloudletList();

        for (int i1 = 0; i1 < getCloudletList().size(); i1++) {
            Vm vm;

            System.out.println(cloudList.get(f[i1]).getCloudletId() + "  " + cloudList.get(f[i1]).getVmId());

            // if user didn't bind this cloudlet and it has not been executed yet
            if (cloudList.get(f[i1]).getVmId() == -1) {
                vm = getVmsCreatedList().get(vmIndex);
            } else { // submit to the specific vm
                vm = VmList.getById(getVmsCreatedList(), cloudList.get(f[i1]).getVmId());
                if (vm == null) { // vm was not created
                    Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
                            + cloudList.get(f[i1]).getCloudletId() + ": bount VM not available");
                    continue;
                }
            }

            Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
                    + cloudList.get(f[i1]).getCloudletId() + " to VM #" + vm.getId());
            cloudList.get(f[i1]).setVmId(vm.getId());
            sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudList.get(f[i1]));
            cloudletsSubmitted++;
            vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
            getCloudletSubmittedList().add(cloudList.get(f[i1]));
        }

        // remove submitted cloudlets from waiting list
        for (Cloudlet cloudlet : getCloudletSubmittedList()) {
            getCloudletList().remove(cloudlet);
        }
    }

    /**
     * Destroy the virtual machines running in datacenters.
     *
     * @pre $none
     * @post $none
     */
    protected void clearDatacenters() {
        for (Vm vm : getVmsCreatedList()) {
            //p//Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
            sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
        }

        getVmsCreatedList().clear();
    }

    // RR PRIORITY SETTING
    public static int[] setprior(int[] cl, int s, double[] cllen) {
        int pp1 = 0;
        int p[] = new int[100];
        int finalarr1[] = new int[400];
        for (int i = 0; i < s; i++) {
            //System.out.print(cl[i]);
            //System.out.println("   "+cllen[cl[i]]);
            if (cl[i] % 2 == 0) {
                p[i] = 2;
            } else {
                p[i] = 1;
            }
        }
        finalarr1 = rr1(cl, s, p, cllen);
        return finalarr1;
    }

    public static int[] rr1(int[] cl, int s, int[] p, double[] cllen) {
        int threshold = 1700, x = 0, y = 0, z = 0, sort, count = 0, pp = 0;
        float tq = 1900;
        double temp1;
        int jq[] = new int[400];
        int lowarr[] = new int[400];
        int finalarr[] = new int[400];
        double t[] = new double[400];

        for (int i = 0; i < s; i++) {
            System.out.println("--->  " + cl[i] + "    " + cllen[cl[i]] + "    " + p[i]);
        }

        for (int i = 0; i < s; i++) {
            jq[i] = 0;
        }
        for (int i = 0; i < s; i++) {

            if (p[i] == 2) {
                temp1 = 0.2 * tq;
                t[i] = tq - temp1;
            } else {
                temp1 = 0.2 * tq;
                t[i] = tq + temp1;
            }
        }
        //System.out.println("The scheduled order of cloudlets is:");
        for (z = 0; z < s; z++) {
            if (cllen[cl[z]] < threshold) {
                lowarr[z] = cl[z];
                //cl[z]=0;
                jq[z] = 1;
                System.out.println("low   " + lowarr[z]);
                count++;
                finalarr[pp++] = lowarr[z];
            }
        }
        /*for(z=0;z<s;z++)
		{
			System.out.println(lowarr[z]);
		}
		System.out.println("\n\n\n");*/

        for (x = 0; x < count; x++) {
            for (y = x + 1; y < count; y++) {
                if (cllen[lowarr[x]] > cllen[lowarr[y]]) {
                    sort = lowarr[x];
                    lowarr[x] = lowarr[y];
                    lowarr[y] = sort;
                }
            }
        }

        for (int i = 0; i < s; i++) {
            if ((jq[i] == 0) && (cl[i] != 0)) {
                if (cllen[cl[i]] <= tq) {
                    jq[i] = 1;
                    System.out.println("C2   " + cl[i]);
                    finalarr[pp++] = cl[i];
                } else {
                    if (p[i] == 1)//high priority
                    {
                        temp1 = 0.3 * tq;
                        if (cllen[cl[i]] <= (tq + temp1)) {
                            jq[i] = 1;
                            System.out.println("C3   " + cl[i]);
                            finalarr[pp++] = cl[i];
                        } else {
                            cllen[cl[i]] = cllen[cl[i]] - temp1;
                        }
                    } else ////low priority
                    {
                        temp1 = 0.2 * tq;
                        if (cllen[cl[i]] <= (tq + temp1)) {
                            jq[i] = 1;
                            System.out.println("C4   " + cl[i]);
                            finalarr[pp++] = cl[i];
                        } else {
                            cllen[cl[i]] = cllen[cl[i]] - temp1;
                        }
                    }
                }
            }
        }
        return finalarr;
    }

    /**
     * Send an internal event communicating the end of the simulation.
     *
     * @pre $none
     * @post $none
     */
    protected void finishExecution() {
        sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
    }

    /*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#shutdownEntity()
     */
    @Override
    public void shutdownEntity() {
        Log.printLine(getName() + " is shutting down...");
    }

    /*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#startEntity()
     */
    @Override
    public void startEntity() {
        Log.printLine(getName() + " is starting...");
        schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
    }

    /**
     * Gets the vm list.
     *
     * @param <T> the generic type
     * @return the vm list
     */
    @SuppressWarnings("unchecked")
    public <T extends Vm> List<T> getVmList() {
        return (List<T>) vmList;
    }

    /**
     * Sets the vm list.
     *
     * @param <T> the generic type
     * @param vmList the new vm list
     */
    protected <T extends Vm> void setVmList(List<T> vmList) {
        this.vmList = vmList;
    }

    /**
     * Gets the cloudlet list.
     *
     * @param <T> the generic type
     * @return the cloudlet list
     */
    @SuppressWarnings("unchecked")
    public <T extends Cloudlet> List<T> getCloudletList() {
        return (List<T>) cloudletList;
    }

    /**
     * Sets the cloudlet list.
     *
     * @param <T> the generic type
     * @param cloudletList the new cloudlet list
     */
    protected <T extends Cloudlet> void setCloudletList(List<T> cloudletList) {
        this.cloudletList = cloudletList;
    }

    /**
     * Gets the cloudlet submitted list.
     *
     * @param <T> the generic type
     * @return the cloudlet submitted list
     */
    @SuppressWarnings("unchecked")
    public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
        return (List<T>) cloudletSubmittedList;
    }

    /**
     * Sets the cloudlet submitted list.
     *
     * @param <T> the generic type
     * @param cloudletSubmittedList the new cloudlet submitted list
     */
    protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
        this.cloudletSubmittedList = cloudletSubmittedList;
    }

    /**
     * Gets the cloudlet received list.
     *
     * @param <T> the generic type
     * @return the cloudlet received list
     */
    @SuppressWarnings("unchecked")
    public <T extends Cloudlet> List<T> getCloudletReceivedList() {
        return (List<T>) cloudletReceivedList;
    }

    /**
     * Sets the cloudlet received list.
     *
     * @param <T> the generic type
     * @param cloudletReceivedList the new cloudlet received list
     */
    protected <T extends Cloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
        this.cloudletReceivedList = cloudletReceivedList;
    }

    /**
     * Gets the vm list.
     *
     * @param <T> the generic type
     * @return the vm list
     */
    @SuppressWarnings("unchecked")
    public <T extends Vm> List<T> getVmsCreatedList() {
        return (List<T>) vmsCreatedList;
    }

    /**
     * Sets the vm list.
     *
     * @param <T> the generic type
     * @param vmsCreatedList the vms created list
     */
    protected <T extends Vm> void setVmsCreatedList(List<T> vmsCreatedList) {
        this.vmsCreatedList = vmsCreatedList;
    }

    /**
     * Gets the vms requested.
     *
     * @return the vms requested
     */
    protected int getVmsRequested() {
        return vmsRequested;
    }

    /**
     * Sets the vms requested.
     *
     * @param vmsRequested the new vms requested
     */
    protected void setVmsRequested(int vmsRequested) {
        this.vmsRequested = vmsRequested;
    }

    /**
     * Gets the vms acks.
     *
     * @return the vms acks
     */
    protected int getVmsAcks() {
        return vmsAcks;
    }

    /**
     * Sets the vms acks.
     *
     * @param vmsAcks the new vms acks
     */
    protected void setVmsAcks(int vmsAcks) {
        this.vmsAcks = vmsAcks;
    }

    /**
     * Increment vms acks.
     */
    protected void incrementVmsAcks() {
        vmsAcks++;
    }

    /**
     * Gets the vms destroyed.
     *
     * @return the vms destroyed
     */
    protected int getVmsDestroyed() {
        return vmsDestroyed;
    }

    /**
     * Sets the vms destroyed.
     *
     * @param vmsDestroyed the new vms destroyed
     */
    protected void setVmsDestroyed(int vmsDestroyed) {
        this.vmsDestroyed = vmsDestroyed;
    }

    /**
     * Gets the datacenter ids list.
     *
     * @return the datacenter ids list
     */
    protected List<Integer> getDatacenterIdsList() {
        return datacenterIdsList;
    }

    /**
     * Sets the datacenter ids list.
     *
     * @param datacenterIdsList the new datacenter ids list
     */
    protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
        this.datacenterIdsList = datacenterIdsList;
    }

    /**
     * Gets the vms to datacenters map.
     *
     * @return the vms to datacenters map
     */
    protected Map<Integer, Integer> getVmsToDatacentersMap() {
        return vmsToDatacentersMap;
    }

    /**
     * Sets the vms to datacenters map.
     *
     * @param vmsToDatacentersMap the vms to datacenters map
     */
    protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
        this.vmsToDatacentersMap = vmsToDatacentersMap;
    }

    /**
     * Gets the datacenter characteristics list.
     *
     * @return the datacenter characteristics list
     */
    protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
        return datacenterCharacteristicsList;
    }

    /**
     * Sets the datacenter characteristics list.
     *
     * @param datacenterCharacteristicsList the datacenter characteristics list
     */
    protected void setDatacenterCharacteristicsList(
            Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
        this.datacenterCharacteristicsList = datacenterCharacteristicsList;
    }

    /**
     * Gets the datacenter requested ids list.
     *
     * @return the datacenter requested ids list
     */
    protected List<Integer> getDatacenterRequestedIdsList() {
        return datacenterRequestedIdsList;
    }

    /**
     * Sets the datacenter requested ids list.
     *
     * @param datacenterRequestedIdsList the new datacenter requested ids list
     */
    protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
        this.datacenterRequestedIdsList = datacenterRequestedIdsList;
    }

}
