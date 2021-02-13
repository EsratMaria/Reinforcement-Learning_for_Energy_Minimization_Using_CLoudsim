/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cloudbus.cloudsim.examples.power;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.SimEntity;
/**
 *
 * @author hp
 */
public class Gene {
    private Cloudlet task;
	private Vm vm;
	public Gene(Cloudlet cl, Vm v)
	{
		this.task=cl;
		this.vm=v;
	}
	public Cloudlet getCloudletFromGene()
	{
		return this.task;
	}
	public Vm getVmFromGene()
	{
		return this.vm;
	}
	public void setCloudletForGene(Cloudlet cl)
	{
		this.task=cl;
	}
	public void setVmForGene(Vm vm)
	{
		this.vm=vm;
	}
}
