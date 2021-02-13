/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.examples.QLearningScheduler;
import java.util.ArrayList;
import java.util.List;
import org.cloudbus.cloudsim.util.MathUtil;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicy;

/**
 * The Minimum Migration Time (MMT) VM selection policy.
 * 
 * If you are using any algorithms, policies or workload included in the power
 * package, please cite the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic
 * Algorithms and Adaptive Heuristics for Energy and Performance Efficient
 * Dynamic Consolidation of Virtual Machines in Cloud Data Centers", Concurrency
 * and Computation: Practice and Experience (CCPE), Volume 24, Issue 13, Pages:
 * 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public class PowerVmSelectionPolicyMinimumMigrationTime_ClusterComputing extends PowerVmSelectionPolicy {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cloudbus.cloudsim.experiments.power.PowerVmSelectionPolicy#
	 * getVmsToMigrate(org.cloudbus .cloudsim.power.PowerHost)
	 */
	@Override
	public Vm getVmToMigrate(PowerHost host) {
		List<PowerVm> migratableVms = getMigratableVms(host);
		if (migratableVms.isEmpty()) {
			return null;
		}
		Vm vmToMigrate = null;

		int factor = 0;
		int iter = 9;

		double minMetric = Double.MAX_VALUE;
//		System.out.println("migratableVms : "+migratableVms.size());
		for (Vm vm : migratableVms) {
			if (vm.isInMigration()) {
				continue;
			}

			double metric_avg = 0;
			List<Double> metric = new ArrayList<Double>();
			if (CloudSim.clock() >= 3000) {

				for (int j = 0; j < iter; j++) {
					metric.add((double) (vm.getTotalUtilizationOfCpuMips(CloudSim.clock() - 300 * j) / vm.getMips())
							* 100);
				}

				metric_avg = MathUtil.mean(metric);
			}

			double metric1 = vm.getRam();
			if (metric1 < minMetric) { // minumum ram size
				minMetric = metric1;
				if (metric_avg < 80) {
					vmToMigrate = vm;
					//factor = 1;
				}
			}
		}

		return vmToMigrate;

	}

}
