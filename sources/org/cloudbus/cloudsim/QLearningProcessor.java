/**
 * This is the implementation class for a Machine Learning based
 * (Q- Learning) approach for Scheduling/placing
 * cloudlets.
 * Trying to incorporate ML with cloudsim version 3.0.0
 * :)
 */
package org.cloudbus.cloudsim;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import com.sun.org.apache.regexp.internal.recompile;

import sun.security.krb5.internal.APOptions;

import java.util.*;
/**
 * @author Esrat Maria
 *
 */
public class QLearningProcessor {
	
	private int Epoch;
	private double gamma;
	private double epsilon;
	private double finalEpsilon;
	private double epsilonRate;
	private Random random;
	private List<Double> simulationRam;
	private List<Double> simulationCPU;
	
	private List<Vm> vmList;
	
	private HashMap<List<Integer>, double[]> Q;
	
	public QLearningProcessor() {
		// TODO Auto-generated constructor stub
	}
	
	public QLearningProcessor(int seed, double gamma, double epsilon,
			double epsilonRate, double finalEpsilon, int Epoch,
			List<Vm> vmList) {
		setRandom(seed);
		setGamma(gamma);
		setEpsilon(epsilon);
		setEpsilonRate(epsilonRate);
		setFinalEpsilon(finalEpsilon);
		setEpoch(Epoch);
		setVmList(vmList);
		
		setQ(new HashMap<>());
		setSimulationCPU(new ArrayList<>());
		setSimulationRam(new ArrayList<>());
		
	}
	
	/*
	 * training system with Q- learning algorithm :)
	 * 
	 * will it work? 
	 * 
	 */
	public void train(List<Vm> vmList) {
		double delta = (getEpsilon() - getFinalEpsilon()) / (getEpoch() * getEpsilonRate());
		for(int epoch = 0; epoch < getEpoch(); epoch++) {
			setSimulationRam();
			setSimulationCPU();
			List <Integer> S = computeS(getVmList().get(0));
			for(int i = 0; i < getVmList().size(); i++) {
				Vm vm = getVmList().get(i);
				
				int action;
				double[] q = new double[vmList.size()];
				if(getQ().containsKey(S))
					q = getQ().get(S);
				
				if(getRandom().nextDouble() < getEpsilon()) {
					action = getRandom().nextInt(vmList.size()); // choosing random actions
				}
				else {
					action = getMaxIdx(q);
				}
				
				double reward = computeReward(action, S); // reward -1 means the VM has already been served
				updateSimulationCPU(action, vm); // has been served -> the index value is zero (0)
				updateSimulationRam(action, vm);
				
				List <Integer> _S;
				double[] _q = new double[vmList.size()];
				if(i < getVmList().size()-1) {
					_S = computeS(getVmList().get(i+1));
					if(getQ().containsKey(_S))
						_q = getQ().get(_S);
					S = _S;
				}
				computeQ(q, action, reward, _q);
				putQ(S, q);
			}
			if(getEpsilon() > getFinalEpsilon()) {
				setEpsilon(getEpsilon() - delta);
			}
			for (List<Integer> s : getQ().keySet()) {
	            double[] value = getQ().get(s);
				System.out.println(Arrays.toString(value));
	        }
			Log.printLine("----------------------");
			Log.printLine(getQ().keySet());
			Log.printLine("----------------------");
		}
	}
	
	public double computeReward(int action, List <Integer> S) {
		if(S.get(action) == 0) {
			return -1;
		}
		else {
			return 1 / S.get(action);
		}
	}
	
	public int getMaxIdx(double[] arr) {
		if(arr == null || arr.length == 0)
			return -1;
		int idx = 0;
		for(int i = 0; i < arr.length-1; i++) {
			if(arr[idx] > arr[i + 1]) {
				idx = i;
			}
		}
		return idx + 1;
	}
	
	public List<Integer> computeS(Vm vm){
		ArrayList<Integer> S = new ArrayList<>();
		for(int i = 0; i < getVmList().size(); i++)
			S.add(computeSValue(i, vm));
		return S;
	}
	
	
    public int computeSValue(int i, Vm vm){
        double tmp = Math.max(Math.floor(getSimulationCPU().get(i)/vm.getCurrentRequestedTotalMips()),
                Math.floor(getSimulationRam().get(i)/ vm.getRam()));
        return (int)(tmp);
    }
	public void putQ(List<Integer> S, double[] q) {
		this.Q.put(S, q); // S -> key in the HasMap and q -> value in the HasMap
	}
	public void computeQ(double[] q, int action, double reward, double[] _q) {
		q[action] = (1 - getGamma()) * q[action] + getGamma() * (reward + Arrays.stream(_q).max().getAsDouble());
	}

	/**
	 * Getters and Setters
	 */
	
	public List<Double> getSimulationCPU(){
		return this.simulationCPU;
	}
	public void setSimulationCPU() {
		getSimulationCPU().clear();
		for(Vm vm : getVmList()) {
			this.simulationCPU.add(vm.getMips());
		}
	}
	public void setSimulationCPU(List<Double> simulationCPU) {
		this.simulationCPU = simulationCPU;
	}
	public void updateSimulationCPU(int i, Vm vm) {
		this.simulationCPU.set(i, this.simulationCPU.get(i) - vm.getCurrentRequestedTotalMips());
	}
	public List<Double> getSimulationRam(){
		return this.simulationRam;
	}
	public void setSimulationRam() {
		getSimulationRam().clear();
		for(Vm vm : getVmList()) {
			this.simulationRam.add((double)(vm.getRam() - vm.getCurrentAllocatedRam()));
		}
	}
	public void setSimulationRam(List<Double> simulationRam) {
		this.simulationRam = simulationRam;
	}
	public void updateSimulationRam(int i, Vm vm) {
		this.simulationRam.set(i, this.simulationRam.get(i) - vm.getRam());
	}
	public List<Vm> getVmList(){
		return this.vmList;
	}
	public void setVmList(List<Vm> vmList) {
		this.vmList = vmList;
	}
	
	/**
	 * 
	 * |
	 * v
	 * 
	 */
	
	public double getEpoch() {
		return Epoch;
	}
	public void setEpoch(int Epoch) {
		this.Epoch = Epoch;
	}
	public double getGamma() {
		return this.gamma;
	}
	public void setGamma(double gamma) {
		this.gamma = gamma;
	}
	public double getEpsilon() {
		return epsilon;
	}
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}
    public double getFinalEpsilon() {
        return finalEpsilon;
    }

    public void setFinalEpsilon(double finalEpsilon) {
        this.finalEpsilon = finalEpsilon;
    }

    public double getEpsilonRate() {
        return epsilonRate;
    }
    public void setEpsilonRate(double epsilonRate) {
        this.epsilonRate = epsilonRate;
    }
    public Random getRandom() {
    	return this.random;
    }
    public void setRandom(int seed) {
    	this.random = new Random(seed);
    }
    public void setRandom(Random random) {
    	this.random = random;
    }
    
    public HashMap<List<Integer>, double[]> getQ(){
    	return this.Q;
    }
    public void setQ(HashMap<List<Integer>, double[]> q) {
    	this.Q = q;
    }

}
