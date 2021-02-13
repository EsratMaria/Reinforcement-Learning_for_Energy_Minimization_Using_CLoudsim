/**
 * 
 */
package org.cloudbus.cloudsim.examples.QLearningScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Esrat Maria
 *
 */
public class QLearningAgent {

	private double gamma;
	private double alpha;
	private double epsilon;
	public static Map<String, Map<Integer, Double>> QList = new HashMap<String, Map<Integer, Double>>();

	public QLearningAgent(double gamma, double alpha, double epsilon) {
		this.gamma = gamma;
		this.alpha = alpha;
		this.epsilon = epsilon;
	}

	public void initRowOfQList(String state_idx) {
		QList.put(state_idx, new HashMap<Integer, Double>());
		for (int i = 0; i < Constants.NUMBER_OF_HOSTS; i++) {
			QList.get(state_idx).put(i, 0.0);
		}
	}

	public int randomInt(int min, int max) {
		if (min == max) {
			return min;
		}
		Random random = new Random();
		return random.nextInt(max) % (max - min + 1) + min;
	}

	public String createLastState_idx(String lastcpulist) {
		return lastcpulist;
	}

	public String createState_idx(String cpulist) {
		return cpulist;
	}

	public int createAction(String cpulist) {
		int current_action;
		int x = randomInt(0, 100);
		String state_idx = createState_idx(cpulist);
		if (!QList.containsKey(state_idx)) {
			initRowOfQList(state_idx);
		}

		if (((double) x / 100) < (1 - epsilon)) {
			int umax = 0;
			double tmp = -1000000000000000.0;
			for (int i = 0; i < Constants.NUMBER_OF_HOSTS; i++) {
				if (tmp < QList.get(state_idx).get(i)) {
					tmp = QList.get(state_idx).get(i);
					umax = i;
				}
			}
			if (tmp == -1000000000000000.0) {
				System.out.println("exploitation is not proceeding as expected!");
				System.exit(0);
			}
			current_action = umax;
		} else {
			current_action = randomInt(0, Constants.NUMBER_OF_HOSTS - 1);
		}
		return current_action;
	}

	public void updateQList(int action_idx, double reward, String lastcpulist, String cpulist) {
		if (reward == 0.0) {
			reward = 1;
		}
		double finalreward = 1.0 / reward;
		String state_idx = createLastState_idx(lastcpulist);
		String next_state_idx = createState_idx(cpulist);

		if (!QList.containsKey(next_state_idx)) {
			initRowOfQList(next_state_idx);
		}
		double QMaxNextState = -1.0;
		for (int i = 0; i < Constants.NUMBER_OF_HOSTS; i++) {
			if (QMaxNextState < QList.get(next_state_idx).get(i)) {
				QMaxNextState = QList.get(next_state_idx).get(i);
			}
		}
		double QValue = QList.get(state_idx).get(action_idx)
				+ alpha * (finalreward + gamma * QMaxNextState - QList.get(state_idx).get(action_idx));
		QList.get(state_idx).put(action_idx, QValue);
	}

}
