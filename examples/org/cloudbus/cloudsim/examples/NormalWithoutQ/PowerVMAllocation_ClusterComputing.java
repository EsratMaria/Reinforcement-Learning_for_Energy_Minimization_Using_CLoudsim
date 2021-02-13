package org.cloudbus.cloudsim.examples.NormalWithoutQ;

import java.util.List;
import java.util.Set;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicy;

public class PowerVMAllocation_ClusterComputing extends PowerVmAllocationPolicyMigrationAbstract {

	/** The safety parameter. */
	private double safetyParameter = 0;

	/** The fallback vm allocation policy. */
	private PowerVmAllocationPolicyMigrationAbstract fallbackVmAllocationPolicy;

	public static double slaFailcount = 0;

	public PowerVMAllocation_ClusterComputing(List<? extends Host> hostList, PowerVmSelectionPolicy vmSelectionPolicy,
			double safetyParameter, PowerVmAllocationPolicyMigrationAbstract fallbackVmAllocationPolicy,
			double utilizationThreshold) {
		super(hostList, vmSelectionPolicy);
		setSafetyParameter(safetyParameter);
		setFallbackVmAllocationPolicy(fallbackVmAllocationPolicy);
	}

	public PowerVMAllocation_ClusterComputing(List<? extends Host> hostList, PowerVmSelectionPolicy vmSelectionPolicy,
			double safetyParameter, PowerVmAllocationPolicyMigrationAbstract fallbackVmAllocationPolicy) {
		super(hostList, vmSelectionPolicy);
		setSafetyParameter(safetyParameter);
		setFallbackVmAllocationPolicy(fallbackVmAllocationPolicy);
	}

	@Override
	protected boolean isHostOverUtilized(PowerHost host) {
		PowerHostUtilizationHistory _host = (PowerHostUtilizationHistory) host;

		double totalRequestedMips = 0;
		double totalPredictedMips = 0;

		double stddev = 0;

		for (Vm vm : host.getVmList()) {
			totalRequestedMips += vm.getCurrentRequestedTotalMips();
			PowerVm _vm = (PowerVm) vm;
			totalPredictedMips += _vm.getUtilizationMean() + Math.sqrt(_vm.getUtilizationVariance());

		}

		double utilization = totalRequestedMips / host.getTotalMips();
		double utilizationPrediction = totalPredictedMips / host.getTotalMips();
		double upperThreshold = utilizationPrediction;
		addHistoryEntry(host, upperThreshold);
		if (utilization >= 1)
			slaFailcount += 1;
		if (utilizationPrediction >= 1 || (utilization >= 1)) {

			return true;
		} else
			return false;
	}

	public PowerHost findHostForVm(Vm vm, Set<? extends Host> excludedHosts) {
		double minPower = Double.MAX_VALUE;
		PowerHost allocatedHost = null;

		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (excludedHosts.contains(host)) {
				continue;
			}
			if (host.isSuitableForVm(vm)) {
				if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
					continue;
				}

				try {
					double powerAfterAllocation = getPowerAfterAllocation(host, vm);
					
					if (powerAfterAllocation != -1) {
						double powerDiff = powerAfterAllocation - host.getPower();
						//System.out.println("powerAfterAllocation : "+powerAfterAllocation+" host.getPower() : "+host.getPower());
						if (powerDiff < minPower) {
							minPower = powerDiff;
							allocatedHost = host;
						}
					}
				} catch (Exception e) {
				}
			}
		}
		return allocatedHost;
	}
	

	protected void setSafetyParameter(double safetyParameter) {
		if (safetyParameter < 0) {
			Log.printLine("The safety parameter cannot be less than zero. The passed value is: " + safetyParameter);
			System.exit(0);
		}
		this.safetyParameter = safetyParameter;
	}

	protected double getSafetyParameter() {
		return safetyParameter;
	}

	public void setFallbackVmAllocationPolicy(PowerVmAllocationPolicyMigrationAbstract fallbackVmAllocationPolicy) {
		this.fallbackVmAllocationPolicy = fallbackVmAllocationPolicy;
	}

	public PowerVmAllocationPolicyMigrationAbstract getFallbackVmAllocationPolicy() {
		return fallbackVmAllocationPolicy;
	}

}
