package fr.lip6.meta.ComplexChangeDetection.GenericDetection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.SetProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.ComplexChangeDetection.Utils.References;

public class TemporaryMove extends TemporaryComplexChange{

	public DeleteProperty delete = null;
	public ArrayList<AddProperty> adds = null;
	public String name = "";
	public ArrayList<ComplexChange> complexEvolutionTrace = null;
	
	public TemporaryMove(){
		//this.moveElements = new ArrayList<AtomicChange>();
		this.adds = new ArrayList<AddProperty>();
		
	}
	
	@Override
	public ArrayList<ComplexChange> validate(ArrayList<AtomicChange> trace, InitialState initState) {
		// TODO Auto-generated method stub
		this.complexEvolutionTrace = new  ArrayList<ComplexChange>();
		for(AddProperty add : this.adds){
			if(this.delete != null && (this.checkReferenceInInitialState(initState, delete.getClassName(), add.getClassName())
					|| this.checkReferenceInTrace(trace, delete.getClassName(), add.getClassName())) 
					//&& this.doesReferenceExist(name, this.delete.getClassName(), add.getClassName(), trace, initState)
					){
				
				MoveProperty move = new MoveProperty(
						this.delete.getName(),
						this.delete.getClassName(),
						add.getClassName()	);
				//saving the position of when the move statrted and ended in the trace, in first and last variables
				move.setDeletePropertyPosition(trace.indexOf(this.delete));
				move.setAddPropertyPosition(trace.indexOf(add));
				complexEvolutionTrace.add(move);
			}
		}
		return complexEvolutionTrace;
	}

	@Override
	public boolean isPossibleToAdd(AtomicChange element) {
		// TODO Auto-generated method stub
		
		if(this.delete == null && this.adds.size() == 0) return true;
		else if(element instanceof AddProperty && element.getName().equals(this.name)) return true;
		else if(element instanceof DeleteProperty && this.delete == null  && element.getName().equals(this.name)) return true;
		else if(element instanceof DeleteProperty && this.delete != null) return false;
		return false;
	}

	@Override
	public void add(AtomicChange element){
		// TODO Auto-generated method stub
		this.name = element.getName();
		System.out.println(" name = "+this.name );
		if(element instanceof AddProperty) this.adds.add((AddProperty) element);
		else if(element instanceof DeleteProperty && this.delete == null) this.delete = (DeleteProperty) element;
	}
	
	public boolean doesReferenceExist(String property, String sourceClass, String targetClass, ArrayList<AtomicChange> atomicEvolutionTrace, InitialState initState){
		
		//here we test, if there is a ref in the source class going to the target class, first in the metamodel and then in the trace
		//missing cases, where in the trace we add a ref and then we deleted it , or it exists in the metamodel but deleted in the trace => to be added/checked later!!
		return this.doesReferenceExistInitailState(sourceClass, targetClass, initState) || this.doesReferenceExistTrace(property, sourceClass, targetClass, atomicEvolutionTrace);
	}
	
	public boolean doesReferenceExistInitailState(String sourceClass, String targetClass, InitialState initState){
		
		if(initState.getOneClass(sourceClass) != null){
			for(References reference : initState.getOneClass(sourceClass).getReferences()){
				if(reference.getTargetClass().getName().equals(targetClass)){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean doesReferenceExistTrace(String property, String sourceClass, String targetClass, ArrayList<AtomicChange> atomicEvolutionTrace){
		
		for(AtomicChange c : atomicEvolutionTrace){// I will check if there is an added ref between source and target, ie. if there is an addProperty in source and then its type is set to target with setProperty
			if(c instanceof AddProperty && ((AddProperty) c).getClassName().equals(sourceClass)){
				for(AtomicChange subC : atomicEvolutionTrace.subList(atomicEvolutionTrace.indexOf(c), atomicEvolutionTrace.size())){
					if(subC instanceof SetProperty){
						if(subC.getName().equals(c.getName()) && ((SetProperty) subC).getType().equals(targetClass)){
							return true;
						}
			
						//check if there is not missing cases, ex: there is a setProperty without another setProperty of the same property to another type...
					}
				}
			}
			// and you can check whether an existing property in sourceClass now have the type targetClass (with a setProportyType(_, targetClass); 
		}
		return false;
	}
	
	@Override
	public boolean isPossibleToAdd(ComplexChange element) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void add(ComplexChange element) {
		// TODO Auto-generated method stub	
	}

}
