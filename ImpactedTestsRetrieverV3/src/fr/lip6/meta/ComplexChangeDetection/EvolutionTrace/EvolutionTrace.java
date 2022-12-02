package fr.lip6.meta.ComplexChangeDetection.EvolutionTrace;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;

public class EvolutionTrace {

	private ArrayList<AtomicChange> atomicEvolutionTrace = null;
	private ArrayList<ComplexChange> complexEvolutionTrace = null;
	
	 public EvolutionTrace() {
		// TODO Auto-generated constructor stub
		this.atomicEvolutionTrace = new ArrayList<AtomicChange>();
		this.complexEvolutionTrace = new  ArrayList<ComplexChange>();
	}

	public ArrayList<AtomicChange> getAtomicEvolutionTrace() {
		return atomicEvolutionTrace;
	}

	public void setAtomicEvolutionTrace(ArrayList<AtomicChange> trace) {
		this.atomicEvolutionTrace = trace;
	}
	 
	public ArrayList<ComplexChange> getComplexEvolutionTrace() {
		return complexEvolutionTrace;
	}

	public void setComplexEvolutionTrace(
			ArrayList<ComplexChange> complexEvolutionTrace) {
		this.complexEvolutionTrace = complexEvolutionTrace;
	}

	public void displayAtomicEvolutionTrace(){
		System.out.println("size of atomic changes = "+this.atomicEvolutionTrace.size());
		int at = 0;
		for(AtomicChange e : this.atomicEvolutionTrace){
			System.out.println("e at "+at+" = "+e);
			at++;
		}
	}
	
	public void displayComplexEvolutionTrace(){
		System.out.println("size of complex changes = "+this.complexEvolutionTrace.size());
		int at = 0;
		for(ComplexChange e : this.complexEvolutionTrace){
			System.out.println("e at "+at+" = "+e);
			at++;
		}
	}
	
}
