package org.cloudbus.cloudsim.examples.power;

import java.util.ArrayList;
import org.cloudbus.cloudsim.Vm;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hp
 */
public class Chromosomes {
    protected ArrayList<Gene> geneList;
	
	public Chromosomes(ArrayList<Gene> geneList){
		this.geneList=geneList;		
	}
	
	public ArrayList<Gene> getGeneList(){
		return this.geneList;
	}
	
	public void updateGene(int index,Vm vm){
		Gene gene=this.geneList.get(index);
		gene.setVmForGene(vm);
		this.geneList.set(index, gene);
	}
}
