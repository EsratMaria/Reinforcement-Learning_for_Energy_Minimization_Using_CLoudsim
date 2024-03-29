package org.cloudbus.cloudsim.examples.QLearningScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicy;

public class QPowerVmAllocationPolicy extends PowerVMAllocation_ClusterComputing {

	public QPowerVmAllocationPolicy(List<? extends Host> hostList, PowerVmSelectionPolicy vmSelectionPolicy) {
		super(hostList, vmSelectionPolicy);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cloudbus.cloudsim.VmAllocationPolicy#optimizeAllocation(java.util.List)
	 */
//	@Override
//	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
//		List<Map<String, Object>> migrationMap = new LinkedList<Map<String, Object>>();
//		Map<String, Object> migrate = new HashMap<String, Object>();
//		
//
//		// Find VMs to migrate
//		Vm vm = null;
//		for (PowerHostUtilizationHistory host : this.<PowerHostUtilizationHistory>getHostList()) {
//			if (host.getUtilizationOfCpu() * 100 > 85) {
//				vm = host.getVmList().get(0);
//
//			}
//		}
//
//		// Find target hosts
//		Host allocatedHost = null;
//		for (PowerHostUtilizationHistory host1 : this.<PowerHostUtilizationHistory>getHostList()) {
//			if (host1.getUtilizationOfCpu() * 100 < 35) {
//				allocatedHost = host1;
//				//sum += 1;
//			}
//		}
//		
//		if (vm != null && allocatedHost != null) {
//			migrate.put("vm", vm);
//			migrate.put("host", allocatedHost);
//			migrationMap.add(migrate);
//		}
//		System.out.println(migrationMap.size());
//
//		return migrationMap;
//	}

}
