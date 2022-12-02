package fr.lip6.meta.ComplexChangeDetection.Detection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.ComplexChangeDetection.Utils.TemporaryMemory;

public class ExtractDetection {

	public InitialState initState = null;
	
	public ExtractDetection(InitialState initState) {
		super();
		this.initState = initState;
	}

	/*
	 * Our choice of implementation for Extract  Class :
	 * Here we detect the extract class from mutlitple moves, all the moves that share the source and target classes are put in the same extract class
	 * Note that the moves that are part of the extract class are removed form the set of moves
	 * */
	public ArrayList<ComplexChange> detectExtractClass(ArrayList<ComplexChange> preComplexEvolutionTrace, ArrayList<AtomicChange> atomicEvolutionTrace){
		ArrayList<ComplexChange> complexEvolutionTrace = new  ArrayList<ComplexChange>();
		ArrayList<TemporaryMemory> ptm= this.reorderMoves(preComplexEvolutionTrace);//new ArrayList<TemporaryMemory>();
		
		for(TemporaryMemory tempMem : ptm){
			//tempMem.displayPropertiesTemporaryMemory();
			//System.out.println("	extract first move " + tempMem.getFirstMoveAddPosition());
			//System.out.println("	extract name in tmp " + tempMem.getName().split("_")[1]);
			//System.out.println("	extract name in initstate = "+this.initState.getOneClass(tempMem.getName().split("_")[1]));
			for(AtomicChange atomicChange : atomicEvolutionTrace.subList(0, tempMem.getFirstMoveAddPosition())){//we look for the add class before the first add property to it, and the added class is not in the initState of the MM
				//System.out.println("	extract 1");
				if(atomicChange instanceof AddClass && atomicChange.getName().equals(tempMem.getName().split("_")[1]) && this.initState.getOneClass(atomicChange.getName()) == null){
					//instanciate extract class, and in the preComplexEvolutionTrace remove the moves that are part of it
					ArrayList<String> propertiesNames = new ArrayList<String>();
					//	System.out.println("	extract 2");
					ArrayList<ComplexChange> moves = new ArrayList<ComplexChange>();
					for(ComplexChange move : tempMem.getMoveOccurences()){
						propertiesNames.add(move.getName());
						moves.add(move);
						preComplexEvolutionTrace.remove(move);//here we remove the moves that are part of this extract class, to remain coherent
					}
					//System.out.println("	extract 3");
					ExtractClass extractClass = new ExtractClass("n/a", tempMem.getName().split("_")[0], tempMem.getName().split("_")[1], propertiesNames, moves);
					complexEvolutionTrace.add(extractClass);
				}
			}
		}
		/*for(ComplexChange leftMoves : preComplexEvolutionTrace){//put both moves and extract classes in the same array list of complex changes
			complexEvolutionTrace.add(leftMoves);
		}*/
		
		return complexEvolutionTrace;
	}

	private ArrayList<TemporaryMemory> reorderMoves(ArrayList<ComplexChange> preComplexEvolutionTrace) {//to put the moves between the same source and target together so they can becomre an extract class
		
		ArrayList<TemporaryMemory> ptm= new ArrayList<TemporaryMemory>();
		
		for(ComplexChange complexChange : preComplexEvolutionTrace){
			if(complexChange instanceof MoveProperty && (preComplexEvolutionTrace.indexOf(complexChange) <= preComplexEvolutionTrace.size() - 1) &&
					!this.doesTemporaryMemoryInstanceExist(((MoveProperty) complexChange).getSourceClassName()+"_"+((MoveProperty) complexChange).getTargetClassName(), ptm)	){//if the composed name exist in ptm, it means that we already treated it before
				
				TemporaryMemory temp = new TemporaryMemory(((MoveProperty) complexChange).getSourceClassName()+"_"+((MoveProperty) complexChange).getTargetClassName());
				
				for(ComplexChange subComplexChange : preComplexEvolutionTrace.subList(preComplexEvolutionTrace.indexOf(complexChange) + 1, preComplexEvolutionTrace.size())){// + 1 is to not take the move twice
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
	
	/*private boolean doesClassReferenceExistTrace(String property, String sourceClass, 
			String targetClass, ArrayList<AtomicChange> atomicEvolutionTrace){
		
		return false;
	}
	
	
	private ArrayList<ComplexChange> old_detectExtractClass(ArrayList<AtomicChange> atomicEvolutionTrace){
		ArrayList<ComplexChange> complexEvolutionTrace = new  ArrayList<ComplexChange>();
		ArrayList<AtomicChange> involvedElement = new  ArrayList<AtomicChange>();
		
		for(AtomicChange element : atomicEvolutionTrace){
			if(!involvedElement.contains(element) && element instanceof AddProperty){
				//we will look up in the rest of the trace if there is a Delete of the same property
				boolean found = false; //to match the add only with one delete
				if(atomicEvolutionTrace.indexOf(element) < atomicEvolutionTrace.size() - 1){//test if we are not in the last element
					for(AtomicChange subElement : atomicEvolutionTrace.subList(atomicEvolutionTrace.indexOf(element), atomicEvolutionTrace.size())){//int i = atomicEvolutionTrace.indexOf(element) + 1; i< atomicEvolutionTrace.size(); i++){
						if(!involvedElement.contains(subElement) && subElement instanceof DeleteProperty && !found){
							if(element.getName().equals(subElement.getName()) && this.doesClassReferenceExistTrace(element.getName(), ((DeleteProperty) subElement).getClassName(), ((AddProperty)element).getClassName(), atomicEvolutionTrace)){ //chech whether there is a ref in the class of the deleteProperty going to the class of the addProperty
								MoveProperty move = new MoveProperty(
										((AddProperty)element).getName(),
										((DeleteProperty)subElement).getClassName(),
										((AddProperty)element).getClassName()	);
								//saving the position of when the move statrted and ended in the trace, in first and last variables
								move.deletePropertyPosition = atomicEvolutionTrace.indexOf(subElement);
								move.addPropertyPosition = atomicEvolutionTrace.indexOf(element);
								
								complexEvolutionTrace.add(move);
								System.out.println("found1");
								involvedElement.add(element);
								involvedElement.add(subElement);
								//found = true;// to look for one move only that contains the add and delete, and not other moves that share the add 
								
							}
						}
					}
				}
			} else if(!involvedElement.contains(element) && element instanceof DeleteProperty){
				//we will look up in the rest of the trace if there is an Add of the same property
				boolean found = false; //to match the delete only with one add
				if(atomicEvolutionTrace.indexOf(element) < atomicEvolutionTrace.size() - 1){//test if we are not in the last element
					for(AtomicChange subElement : atomicEvolutionTrace.subList(atomicEvolutionTrace.indexOf(element), atomicEvolutionTrace.size())){//int i = atomicEvolutionTrace.indexOf(element) + 1; i< atomicEvolutionTrace.size(); i++){
						if(!involvedElement.contains(subElement) && subElement instanceof AddProperty && !found){
							if(element.getName().equals(subElement.getName()) && this.doesClassReferenceExistTrace(element.getName(), ((DeleteProperty) element).getClassName(), ((AddProperty)subElement).getClassName(), atomicEvolutionTrace)){ //chech whether there is a ref in the class of the deleteProperty going to the class of the addProperty
								MoveProperty move = new MoveProperty(
										((DeleteProperty)element).getName(),
										((DeleteProperty)element).getClassName(),
										((AddProperty)subElement).getClassName()	);
								//saving the position of when the move statrted and ended in the trace, in first and last variables
								move.deletePropertyPosition = atomicEvolutionTrace.indexOf(element);
								move.addPropertyPosition = atomicEvolutionTrace.indexOf(subElement);
								
								complexEvolutionTrace.add(move);
								System.out.println("found2");
								involvedElement.add(element);
								involvedElement.add(subElement);
								//found = true;// to look for one move only that contains the add and delete, and not other moves that share the delete
								
							}
						}
					}
				}
			}
		}
		return complexEvolutionTrace;
	}*/

}
