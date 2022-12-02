package fr.lip6.meta.ComplexChangeDetection.Detection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.SetProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.InlineClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.ComplexChangeDetection.Utils.References;
import fr.lip6.meta.ComplexChangeDetection.Utils.TemporaryMemory;

public class InlineDetection {

public InitialState initState = null;
	
	public InlineDetection(InitialState initState) {
		super();
		this.initState = initState;
	}
	
	public ArrayList<ComplexChange> detectInlineClass(ArrayList<AtomicChange> atomicEvolutionTrace){//ArrayList<ComplexChange> preComplexEvolutionTrace,
		ArrayList<ComplexChange> complexEvolutionTrace = new  ArrayList<ComplexChange>();
		ArrayList<ComplexChange> inversedMoves = new  ArrayList<ComplexChange>();
		ArrayList<AtomicChange> involvedElement = new  ArrayList<AtomicChange>();
		

		for(AtomicChange element : atomicEvolutionTrace){
			if(!involvedElement.contains(element) && element instanceof AddProperty){
				involvedElement.add(element);
				//we will look up in the rest of the trace if there is a Delete of the same property
				boolean found = false; //to match the add only with one delete
				if(atomicEvolutionTrace.indexOf(element) <= atomicEvolutionTrace.size() - 1){//test if we are not in the last element
					for(AtomicChange subElement : atomicEvolutionTrace.subList(atomicEvolutionTrace.indexOf(element), atomicEvolutionTrace.size())){
						if(!involvedElement.contains(subElement) && subElement instanceof DeleteProperty && !found){
							if(element.getName().equals(subElement.getName()) && this.doesInverseReferenceExist(element.getName(), ((DeleteProperty) subElement).getClassName(), ((AddProperty)element).getClassName(), atomicEvolutionTrace)){ //chech whether there is a ref in the class of the deleteProperty going to the class of the addProperty
								MoveProperty move = new MoveProperty(
										((AddProperty)element).getName(),
										((AddProperty)element).getClassName(),
										((DeleteProperty)subElement).getClassName()	);
								//saving the position of when the move statrted and ended in the trace, in first and last variables
								move.setDeletePropertyPosition(atomicEvolutionTrace.indexOf(subElement));
								move.setAddPropertyPosition(atomicEvolutionTrace.indexOf(element));
								
								inversedMoves.add(move);
								//complexEvolutionTrace.add(move);
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
							if(element.getName().equals(subElement.getName()) && this.doesInverseReferenceExist(element.getName(), ((DeleteProperty) element).getClassName(), ((AddProperty)subElement).getClassName(), atomicEvolutionTrace)){ //chech whether there is a ref in the class of the deleteProperty going to the class of the addProperty
								MoveProperty move = new MoveProperty(
										((DeleteProperty)element).getName(),
										((AddProperty)subElement).getClassName(),
										((DeleteProperty)element).getClassName()	);
								//saving the position of when the move statrted and ended in the trace, in first and last variables
								move.setDeletePropertyPosition(atomicEvolutionTrace.indexOf(element));
								move.setAddPropertyPosition(atomicEvolutionTrace.indexOf(subElement));
								
								inversedMoves.add(move);
								//complexEvolutionTrace.add(move);
								System.out.println("found2");
								
								involvedElement.add(subElement);
								//found = true;// to look for one move only that contains the add and delete, and not other moves that share the delete
								
							}
						}
					}
				}
			}
		}
		//up to now, we detected possible inline properties, now we will group with the same source and target class + and check if there a delete inlined class and ref to it.
		ArrayList<TemporaryMemory> ptm= this.reorderInversedMoves(inversedMoves);
		System.out.println("size = " + ptm.size());
		for(TemporaryMemory tempMem : ptm){
			tempMem.displayPropertiesTemporaryMemory();
			
			for(AtomicChange atomicChange : atomicEvolutionTrace.subList(tempMem.getLastMoveDeletePosition(), atomicEvolutionTrace.size())){//we look for the delete class after the last delete property to it, and the deleted class is in the initState of the MM
				if(atomicChange instanceof DeleteClass && atomicChange.getName().equals(tempMem.getName().split("_")[0]) && this.initState.getOneClass(atomicChange.getName()) != null){// to the cond in the MM, we can add that before the last inverse move there is an add class of the inlined one [in atomicEvolutionTrace.subList(0, tempMem.getLastMoveDeletePosition())]
					//instantiate inline class, //and in the preComplexEvolutionTrace remove the inverse moves that are part of it
					ArrayList<String> propertiesNames = new ArrayList<String>();
					ArrayList<ComplexChange> inverseMoves = new ArrayList<ComplexChange>();
					for(ComplexChange inverseMove : tempMem.getMoveOccurences()){
						propertiesNames.add(inverseMove.getName());
						inverseMoves.add(inverseMove);
						//preComplexEvolutionTrace.remove(inverseMove);//here we remove the moves that are part of this extract class, to remain coherent
					}
					InlineClass inlineClass = new InlineClass("n/a", tempMem.getName().split("_")[0], tempMem.getName().split("_")[1], propertiesNames, inverseMoves);
					complexEvolutionTrace.add(inlineClass);
				}
			}
			//end
			
		}
		/*for(ComplexChange otherMoves : preComplexEvolutionTrace){//put both moves and extract classes in the same array list of complex changes
			complexEvolutionTrace.add(otherMoves);
		}*/
		
		return complexEvolutionTrace;
	}
	
	private ArrayList<TemporaryMemory> reorderInversedMoves(ArrayList<ComplexChange> inversedMoves) {//to put the inversedMoves between the same source and target together so they can becomre an inline class
		
		ArrayList<TemporaryMemory> ptm= new ArrayList<TemporaryMemory>();
		
		for(ComplexChange complexChange : inversedMoves){
			if(complexChange instanceof MoveProperty && (inversedMoves.indexOf(complexChange) <= inversedMoves.size() - 1) &&
					!this.doesTemporaryMemoryInstanceExist(((MoveProperty) complexChange).getTargetClassName()+"_"+((MoveProperty) complexChange).getSourceClassName(), ptm)	){//if the composed name exist in ptm, it means that we already treated it before
				
				TemporaryMemory temp = new TemporaryMemory(((MoveProperty) complexChange).getTargetClassName()+"_"+((MoveProperty) complexChange).getSourceClassName());
				
				for(ComplexChange subComplexChange : inversedMoves.subList(inversedMoves.indexOf(complexChange) + 1, inversedMoves.size())){// + 1 is to not take the move twice
					if(subComplexChange instanceof MoveProperty && 
							((MoveProperty) complexChange).getSourceClassName().equals(((MoveProperty) subComplexChange).getSourceClassName()) &&
							((MoveProperty) complexChange).getTargetClassName().equals(((MoveProperty) subComplexChange).getTargetClassName())	){
						
						temp.getInvolvedComplexElement().add(subComplexChange);
					}
				}
				temp.getInvolvedComplexElement().add(complexChange);
				
				ptm.add(temp);
			}
		}
		
		return ptm;
	}
	
	private boolean doesTemporaryMemoryInstanceExist(String name, ArrayList<TemporaryMemory> ptm){
		
		for(TemporaryMemory tempMem : ptm){
			if(tempMem.getName().equals(name)){
				return true;
			}
		}
		return false;
	}
	
	public boolean doesInverseReferenceExist(String property, String sourceClass, String targetClass, ArrayList<AtomicChange> atomicEvolutionTrace){
		
		//here we test, if there is a ref in the target class and source class, first in the metamodel and then in the trace
		//missing cases, where in the trace we add a ref and then we deleted it , or it exists in the metamodel but deleted in the trace => to be added/checked later!!
		return this.doesInverseReferenceExistInitailState(sourceClass, targetClass) || this.doesInverseReferenceExistTrace(property, sourceClass, targetClass, atomicEvolutionTrace);
	}
	
	public boolean doesInverseReferenceExistInitailState(String sourceClass, String targetClass){
		
		if(this.initState.getOneClass(targetClass) != null){
			for(References reference : this.initState.getOneClass(targetClass).getReferences()){
				if(reference.getTargetClass().getName().equals(sourceClass)){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean doesInverseReferenceExistTrace(String property, String sourceClass, String targetClass, ArrayList<AtomicChange> atomicEvolutionTrace){
		
		for(AtomicChange c : atomicEvolutionTrace){// I will check if there is an added ref between source and target, ie. if there is an addProperty in source and then its type is set to target with setProperty
			if(c instanceof AddProperty && ((AddProperty) c).getClassName().equals(targetClass)){
				for(AtomicChange subC : atomicEvolutionTrace.subList(atomicEvolutionTrace.indexOf(c), atomicEvolutionTrace.size())){
					if(subC instanceof SetProperty){
						if(subC.getName().equals(c.getName()) && ((SetProperty) subC).getType().equals(sourceClass)){
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
