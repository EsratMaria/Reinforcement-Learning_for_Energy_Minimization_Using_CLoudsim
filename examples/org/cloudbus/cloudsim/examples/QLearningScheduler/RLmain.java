package org.cloudbus.cloudsim.examples.QLearningScheduler;

import java.util.List;

import org.cloudbus.cloudsim.Log;

public class RLmain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			QLearningSchedulerTest qTest = new QLearningSchedulerTest();
			qTest.execute();
//			List<Double> learningPowerList = learningScheduleTest.execute();
//            Log.printLine(learningPowerList);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
