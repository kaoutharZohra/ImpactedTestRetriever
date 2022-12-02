package fr.lip6.meta.ComplexChangeDetection.Detection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractSuperClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.FlattenHierarchy;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PushProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.ComplexChangeDetection.Utils.TemporaryMemory;

public class FlattenHierarchyDetection {

	public InitialState initState = null;
	
	public FlattenHierarchyDetection(InitialState initState) {
		super();
		this.initState = initState;
	}
	/* 
	 * Our choice of implementation for Flatten hierarchy :
	 * Here we detect the flatten hierarchy from multiple push, if there is a delete after a push, it is considered as a flatten hierarchy for its property, with its brothers, that share the same subclasses the other are just pushes
	 * Note that the pushes that are part of the flatten hierarchy are removed form the set of pushes
	 * */
	public ArrayList<ComplexChange> detectFlattenHierarchy(ArrayList<ComplexChange> preComplexEvolutionTrace, ArrayList<AtomicChange> atomicEvolutionTrace){
		ArrayList<ComplexChange> complexEvolutionTrace = new  ArrayList<ComplexChange>();
		ArrayList<ComplexChange> involvedElements = new  ArrayList<ComplexChange>();
		ArrayList<TemporaryMemory> ptm= this.reorderPushes(preComplexEvolutionTrace);//new ArrayList<TemporaryMemory>();
		
		for(TemporaryMemory tempMem : ptm){
			//tempMem.displayPropertiesTemporaryMemory();
			
			for(AtomicChange atomicChange : atomicEvolutionTrace.subList(tempMem.getLastPushDeletePosition(), atomicEvolutionTrace.size())){//we look for the delete class after the last delete property to it, and the deleted class is in the initState of the MM
				
				if(atomicChange instanceof DeleteClass && atomicChange.getName().equals(tempMem.getName()) && this.initState.getOneClass(atomicChange.getName()) != null){
					//instanciate flatten hierarchy, and in the preComplexEvolutionTrace remove the pushes that are part of it
					
					for(ComplexChange push : tempMem.getPushOccurences()){
						//if(((PushProperty) push).getDeletePropertyPosition() == tempMem.getFirstPushDeletePosition()){//the first push in the atomic sequence and its borthers (pulls on th esame sub classes)  will be part of the flatten hierarchy
						if(!involvedElements.contains(push) && (tempMem.getPushOccurences().indexOf(push) <= tempMem.getPushOccurences().size() - 1)){//if the element has not been treated before
							involvedElements.add(push);
						
							ArrayList<String> propertiesNames = new ArrayList<String>();
							ArrayList<ComplexChange> pushes = new ArrayList<ComplexChange>();
							ArrayList<String> subClassesNames = new ArrayList<String>();
							
							for(ComplexChange subPush : tempMem.getPushOccurences().subList(
											tempMem.getPushOccurences().indexOf(push), tempMem.getPushOccurences().size())){
								if(!involvedElements.contains(subPush) && this.sameSubClasses((PushProperty)push, (PushProperty)subPush)){//if not the same push and has the same subclasses
									propertiesNames.add(subPush.getName());
									pushes.add(subPush);
									preComplexEvolutionTrace.remove(subPush);//here we remove the pushes that are part of this flatten hierarchy, to remain coherent
									involvedElements.add(subPush);
								}
							}
							
							propertiesNames.add(push.getName());
							subClassesNames = ((PushProperty) push).getSubClassesNames();
							pushes.add(push);
							preComplexEvolutionTrace.remove(push);//here we remove the pushes that are part of this flatten hierarchy, to remain coherent
						
							FlattenHierarchy flattenHierarchy = new FlattenHierarchy("n/a", tempMem.getName(), subClassesNames, propertiesNames, pushes);
							//ExtractClass extractClass = new ExtractClass("n/a", tempMem.getName().split("_")[0], tempMem.getName().split("_")[1], propertiesNames, pulles);
							complexEvolutionTrace.add(flattenHierarchy);
						}
						
					}
					
				}
			}
		}
		/*for(ComplexChange leftPushes : preComplexEvolutionTrace){//put both moves and extract classes in the same array list of complex changes
			complexEvolutionTrace.add(leftPushes);
		}*/
		
		return complexEvolutionTrace;
	}
	
	private boolean sameSubClasses(PushProperty push, PushProperty subPush) {
		//first, test if all elements of push are in subPush
		for(String pushElement : push.getSubClassesNames()){
			if(!subPush.getSubClassesNames().contains(pushElement)){
				return false;
			}
		}
		//second, test if all elements of subPush are in push		
		for(String pushSubElement : subPush.getSubClassesNames()){
			if(!push.getSubClassesNames().contains(pushSubElement)){
				return false;
			}
		}
		
		return true;//the following does not work all time, especially if it is not ordered = ((PullProperty) pull).getSubClassesNames().equals(((PullProperty) subPull).getSubClassesNames());
	}
	
	private ArrayList<TemporaryMemory> reorderPushes(ArrayList<ComplexChange> preComplexEvolutionTrace) {//to put the pushes between the same source and target together so they can becomre a flatten hierarchy

		ArrayList<TemporaryMemory> ptm= new ArrayList<TemporaryMemory>();
		
		/**/for(ComplexChange complexChange : preComplexEvolutionTrace){
				if(complexChange instanceof PushProperty && (preComplexEvolutionTrace.indexOf(complexChange) <= preComplexEvolutionTrace.size() - 1) &&
						!this.doesTemporaryMemoryInstanceExist(((PushProperty) complexChange).getSuperClassName(), ptm)	){//if the superClass exists in ptm, it means that we already treated it before
					
					TemporaryMemory temp = new TemporaryMemory(((PushProperty) complexChange).getSuperClassName());
					
					for(ComplexChange subComplexChange : preComplexEvolutionTrace.subList(preComplexEvolutionTrace.indexOf(complexChange) + 1, preComplexEvolutionTrace.size())){// + 1 is to not take the push twice
						if(subComplexChange instanceof PushProperty && 
								((PushProperty) complexChange).getSuperClassName().equals(((PushProperty) subComplexChange).getSuperClassName())	){
							
							temp.getInvolvedComplexElement().add(subComplexChange);
						}
					}
					temp.getInvolvedComplexElement().add(complexChange);
					
					ptm.add(temp);
				}
		}

		//System.out.println("zehaahha"+ptm.get(0).getInvolvedComplexElement().size());
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
}
