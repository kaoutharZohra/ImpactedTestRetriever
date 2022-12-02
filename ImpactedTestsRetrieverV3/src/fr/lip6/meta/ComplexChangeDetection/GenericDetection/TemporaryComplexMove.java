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

public class TemporaryComplexMove extends TemporaryComplexChange{

	public ArrayList<DeleteProperty> deletes = null;
	public ArrayList<AddProperty> adds = null;
	public String name = "";
	//public ArrayList<ComplexChange> complexEvolutionTrace = null;
	
	public TemporaryComplexMove(){
		//this.moveElements = new ArrayList<AtomicChange>();
		this.adds = new ArrayList<AddProperty>();
		this.deletes = new ArrayList<DeleteProperty>();
		
	}
	
	@Override
	public boolean isPossibleToAdd(AtomicChange element) {
		// TODO Auto-generated method stub
		
		if(this.deletes.size() == 0 && this.adds.size() == 0) return true;
		else if(element instanceof AddProperty && element.getName().equals(this.name)) return true;
		else if(element instanceof DeleteProperty && element.getName().equals(this.name)) return true;
		else return false;
	}

	@Override
	public void add(AtomicChange element){
		// TODO Auto-generated method stub
		this.name = element.getName();
		System.out.println(" name = "+this.name );
		if(element instanceof AddProperty) this.adds.add((AddProperty) element);
		else if(element instanceof DeleteProperty) this.deletes.add((DeleteProperty) element);
	}
	
	@Override
	public ArrayList<ComplexChange> validate(ArrayList<AtomicChange> trace, InitialState initState) {
		// TODO Auto-generated method stub
		ArrayList<ComplexChange> results = new  ArrayList<ComplexChange>();
		for(DeleteProperty delete : this.deletes){
			for(AddProperty add : this.adds){
				if((this.checkReferenceInInitialState(initState, delete.getClassName(), add.getClassName())
						|| this.checkReferenceInTrace(trace, delete.getClassName(), add.getClassName()))
						//&& this.doesReferenceExist(name, delete.getClassName(), add.getClassName(), trace, initState)
						){
					//now for each of the ref in the init state between the two classes we create a move proeprty, since we cannot know in advance wich ref is intended to be used
					ArrayList<String> option1 = this.getReferenceInInitialState(initState, delete.getClassName(), add.getClassName());
					for(String o : option1){
						MoveProperty move = new MoveProperty(
								delete.getName(),
								delete.getClassName(),
								add.getClassName()	);
						//saving the position of when the move statrted and ended in the trace, in first and last variables
						move.setDeletePropertyPosition(trace.indexOf(delete));
						move.setAddPropertyPosition(trace.indexOf(add));
						move.setAddProperty(add);
						move.setDeleteProperty(delete);
						results.add(move);
						move.setThroughReference(o);
					}
					
					//now for each of the ref in the trace between the two classes we create a move proeprty, since we cannot know in advance wich ref is intended to be used
					ArrayList<String> option2 = this.getReferenceInTrace(trace, delete.getClassName(), add.getClassName());
					for(String o : option2){
						MoveProperty move = new MoveProperty(
								delete.getName(),
								delete.getClassName(),
								add.getClassName()	);
						//saving the position of when the move statrted and ended in the trace, in first and last variables
						move.setDeletePropertyPosition(trace.indexOf(delete));
						move.setAddPropertyPosition(trace.indexOf(add));
						move.setAddProperty(add);
						move.setDeleteProperty(delete);
						results.add(move);
						move.setThroughReference(o);
					}
					
					/*String option2 = 
					if(option1 != null)
						move.setThroughReference(option1);
					else if(option2 != null)
						move.setThroughReference(option2);
					*/
					
				}
			}
		}
		return results;
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
