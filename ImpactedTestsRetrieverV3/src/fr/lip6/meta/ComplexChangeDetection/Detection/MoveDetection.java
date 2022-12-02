package fr.lip6.meta.ComplexChangeDetection.Detection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.SetProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.ComplexChangeDetection.Utils.References;

public class MoveDetection {
	
	public InitialState initState = null;
	
	public MoveDetection(InitialState initState) {
		super();
		this.initState = initState;
	}

	public ArrayList<ComplexChange> detectMoveProperty(ArrayList<AtomicChange> atomicEvolutionTrace){
		ArrayList<ComplexChange> complexEvolutionTrace = new  ArrayList<ComplexChange>();
		ArrayList<AtomicChange> involvedElement = new  ArrayList<AtomicChange>();
		
		for(AtomicChange element : atomicEvolutionTrace){
			if(!involvedElement.contains(element) && element instanceof AddProperty){
				involvedElement.add(element);
				//we will look up in the rest of the trace if there is a Delete of the same property
				boolean found = false; //to match the add only with one delete
				if(atomicEvolutionTrace.indexOf(element) <= atomicEvolutionTrace.size() - 1){//test if we are not in the last element
					for(AtomicChange subElement : atomicEvolutionTrace.subList(atomicEvolutionTrace.indexOf(element), atomicEvolutionTrace.size())){//int i = atomicEvolutionTrace.indexOf(element) + 1; i< atomicEvolutionTrace.size(); i++){
						if(!involvedElement.contains(subElement) && subElement instanceof DeleteProperty && !found){
							if(element.getName().equals(subElement.getName()) && this.doesReferenceExist(element.getName(), ((DeleteProperty) subElement).getClassName(), ((AddProperty)element).getClassName(), atomicEvolutionTrace)){ //chech whether there is a ref in the class of the deleteProperty going to the class of the addProperty
								MoveProperty move = new MoveProperty(
										((AddProperty)element).getName(),
										((DeleteProperty)subElement).getClassName(),
										((AddProperty)element).getClassName()	);
								//saving the position of when the move statrted and ended in the trace, in first and last variables
								move.setDeletePropertyPosition(atomicEvolutionTrace.indexOf(subElement));
								move.setAddPropertyPosition(atomicEvolutionTrace.indexOf(element));
								
								complexEvolutionTrace.add(move);
								System.out.println("found1");
								
								involvedElement.add(subElement);
								//found = true;// to look for one move only that contains the add and delete, and not other moves that share the add 
								
							}
						}
					}
				}
			} else if(!involvedElement.contains(element) && element instanceof DeleteProperty){
				involvedElement.add(element);
				//we will look up in the rest of the trace if there is an Add of the same property
				boolean found = false; //to match the delete only with one add
				if(atomicEvolutionTrace.indexOf(element) <= atomicEvolutionTrace.size() - 1){//test if we are not in the last element
					for(AtomicChange subElement : atomicEvolutionTrace.subList(atomicEvolutionTrace.indexOf(element), atomicEvolutionTrace.size())){//int i = atomicEvolutionTrace.indexOf(element) + 1; i< atomicEvolutionTrace.size(); i++){
						if(!involvedElement.contains(subElement) && subElement instanceof AddProperty && !found){
							if(element.getName().equals(subElement.getName()) && this.doesReferenceExist(element.getName(), ((DeleteProperty) element).getClassName(), ((AddProperty)subElement).getClassName(), atomicEvolutionTrace)){ //chech whether there is a ref in the class of the deleteProperty going to the class of the addProperty
								MoveProperty move = new MoveProperty(
										((DeleteProperty)element).getName(),
										((DeleteProperty)element).getClassName(),
										((AddProperty)subElement).getClassName()	);
								//saving the position of when the move statrted and ended in the trace, in first and last variables
								move.setDeletePropertyPosition(atomicEvolutionTrace.indexOf(element));
								move.setAddPropertyPosition(atomicEvolutionTrace.indexOf(subElement));
								
								complexEvolutionTrace.add(move);
								System.out.println("found2");
								
								involvedElement.add(subElement);
								//found = true;// to look for one move only that contains the add and delete, and not other moves that share the delete
								
							}
						}
					}
				}
			}
		}
		return complexEvolutionTrace;
	}
	
	public boolean doesReferenceExist(String property, String sourceClass, String targetClass, ArrayList<AtomicChange> atomicEvolutionTrace){
		
		//here we test, if there is a ref in the source class going to the target class, first in the metamodel and then in the trace
		//missing cases, where in the trace we add a ref and then we deleted it , or it exists in the metamodel but deleted in the trace => to be added/checked later!!
		return this.doesReferenceExistInitailState(sourceClass, targetClass) || this.doesReferenceExistTrace(property, sourceClass, targetClass, atomicEvolutionTrace);
	}
	
	public boolean doesReferenceExistInitailState(String sourceClass, String targetClass){
		
		if(this.initState.getOneClass(sourceClass) != null){
			for(References reference : this.initState.getOneClass(sourceClass).getReferences()){
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
}
