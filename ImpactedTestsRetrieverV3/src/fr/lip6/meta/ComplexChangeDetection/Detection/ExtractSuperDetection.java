package fr.lip6.meta.ComplexChangeDetection.Detection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractSuperClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.ComplexChangeDetection.Utils.TemporaryMemory;

public class ExtractSuperDetection {

	public InitialState initState = null;
	
	public ExtractSuperDetection(InitialState initState) {
		super();
		this.initState = initState;
	}
	/* 
	 * Our choice of implementation for Extract Super Class :
	 * Here we detect the extract super class from multiple pull, the first pull after addClass (superClass), will be considered as extract super class, and the pulls that share the same sub classes, the others are just pulls
	 * Note that the pulls that are part of the extract super class are removed form the set of pulls
	 * */
	public ArrayList<ComplexChange> detectExtractSuperClass(ArrayList<ComplexChange> preComplexEvolutionTrace, ArrayList<AtomicChange> atomicEvolutionTrace){
		ArrayList<ComplexChange> complexEvolutionTrace = new  ArrayList<ComplexChange>();
		ArrayList<ComplexChange> involvedElements = new  ArrayList<ComplexChange>();
		ArrayList<TemporaryMemory> ptm= this.reorderPulls(preComplexEvolutionTrace);//new ArrayList<TemporaryMemory>();
		
		for(TemporaryMemory tempMem : ptm){
			//tempMem.displayPropertiesTemporaryMemory();

			for(AtomicChange atomicChange : atomicEvolutionTrace.subList(0, tempMem.getFirstPullAddPosition())){//we look for the add class before the first add property to it, and the added class is not in the initState of the MM
				
				if(atomicChange instanceof AddClass && atomicChange.getName().equals(tempMem.getName()) && this.initState.getOneClass(atomicChange.getName()) == null){
					//instanciate extract super class, and in the preComplexEvolutionTrace remove the pulles that are part of it
					
					for(ComplexChange pull : tempMem.getPullOccurences()){
						
						if(!involvedElements.contains(pull) && (tempMem.getPullOccurences().indexOf(pull) <= tempMem.getPullOccurences().size() - 1)){//if the element has not been treated before
							involvedElements.add(pull);
							
							ArrayList<String> propertiesNames = new ArrayList<String>();
							ArrayList<ComplexChange> pulles = new ArrayList<ComplexChange>();
							ArrayList<String> subClassesNames = new ArrayList<String>();
							
							for(ComplexChange subPull : tempMem.getPullOccurences().subList(
									tempMem.getPullOccurences().indexOf(pull), tempMem.getPullOccurences().size())){
								if(!involvedElements.contains(subPull) && this.sameSubClasses((PullProperty)pull, (PullProperty)subPull)){//if not the same pull and has the same subclasses
									propertiesNames.add(subPull.getName());
									pulles.add(subPull);
									preComplexEvolutionTrace.remove(subPull);//here we remove the pulles that are part of this extract super class, to remain coherent
									involvedElements.add(subPull);
								}
							}
							
							propertiesNames.add(pull.getName());
							subClassesNames = ((PullProperty) pull).getSubClassesNames();
							pulles.add(pull);
							preComplexEvolutionTrace.remove(pull);//here we remove the pulles that are part of this extract super class, to remain coherent
						
							ExtractSuperClass extractSuperClass = new ExtractSuperClass("n/a", tempMem.getName(), subClassesNames, propertiesNames, pulles);
							//ExtractClass extractClass = new ExtractClass("n/a", tempMem.getName().split("_")[0], tempMem.getName().split("_")[1], propertiesNames, pulles);
							complexEvolutionTrace.add(extractSuperClass);
						}
						/*if(((PullProperty) pull).getAddPropertyPosition() == tempMem.getFirstPullAddPosition()){//the first pull in the atomic sequence and its borthers (pulls on th esame sub classes)  will be part of the extract super class
						}
						//old version where we detect the first pull and its brothers as extract superclass and the the others remain pulls
						if(atomicChange instanceof AddClass && atomicChange.getName().equals(tempMem.getName()) && this.initState.getOneClass(atomicChange.getName()) == null){
						//instanciate extract super class, and in the preComplexEvolutionTrace remove the pulles that are part of it
						ArrayList<String> propertiesNames = new ArrayList<String>();
						ArrayList<ComplexChange> pulles = new ArrayList<ComplexChange>();
						ArrayList<String> subClassesNames = new ArrayList<String>();
						for(ComplexChange pull : tempMem.getPullOccurences()){
							if(((PullProperty) pull).getAddPropertyPosition() == tempMem.getFirstPullAddPosition()){//the first pull in the atomic sequence and its borthers (pulls on th esame sub classes)  will be part of the extract super class
								for(ComplexChange subPull : tempMem.getPullOccurences()){
									if(!pull.equals(subPull) && this.sameSubClasses((PullProperty)pull, (PullProperty)subPull)){
										propertiesNames.add(subPull.getName());
										pulles.add(subPull);
										preComplexEvolutionTrace.remove(subPull);//here we remove the pulles that are part of this extract super class, to remain coherent
									}
								}
								propertiesNames.add(pull.getName());
								subClassesNames = ((PullProperty) pull).getSubClassesNames();
								pulles.add(pull);
								preComplexEvolutionTrace.remove(pull);//here we remove the pulles that are part of this extract super class, to remain coherent
							}
							
						}
						ExtractSuperClass extractSuperClass = new ExtractSuperClass("n/a", tempMem.getName(), subClassesNames, propertiesNames, pulles);
						//ExtractClass extractClass = new ExtractClass("n/a", tempMem.getName().split("_")[0], tempMem.getName().split("_")[1], propertiesNames, pulles);
						complexEvolutionTrace.add(extractSuperClass);
					}
						*/
					}
					
				}
			}
		}
		/**/for(ComplexChange leftPulles : preComplexEvolutionTrace){//put both moves and extract classes in the same array list of complex changes
			complexEvolutionTrace.add(leftPulles);
		}
		
		return complexEvolutionTrace;
	}
	
	private boolean sameSubClasses(PullProperty pull, PullProperty subPull) {
		//first, test if all elements of pull are in subPull
		for(String pullElement : pull.getSubClassesNames()){
			if(!subPull.getSubClassesNames().contains(pullElement)){
				return false;
			}
		}
		//second, test if all elements of subPull are in pull		
		for(String pullSubElement : subPull.getSubClassesNames()){
			if(!pull.getSubClassesNames().contains(pullSubElement)){
				return false;
			}
		}
		
		return true;//the following does not work all time, especially if it is not ordered = ((PullProperty) pull).getSubClassesNames().equals(((PullProperty) subPull).getSubClassesNames());
	}

	private ArrayList<TemporaryMemory> reorderPulls(ArrayList<ComplexChange> preComplexEvolutionTrace) {//to put the pulls between the same source and target together so they can becomre an extract class
		
		ArrayList<TemporaryMemory> ptm= new ArrayList<TemporaryMemory>();
		
		/**/for(ComplexChange complexChange : preComplexEvolutionTrace){
			if(complexChange instanceof PullProperty && (preComplexEvolutionTrace.indexOf(complexChange) <= preComplexEvolutionTrace.size() - 1) &&
					!this.doesTemporaryMemoryInstanceExist(((PullProperty) complexChange).getSuperClassName(), ptm)	){//if the superClass exists in ptm, it means that we already treated it before
				
				TemporaryMemory temp = new TemporaryMemory(((PullProperty) complexChange).getSuperClassName());
				
				for(ComplexChange subComplexChange : preComplexEvolutionTrace.subList(preComplexEvolutionTrace.indexOf(complexChange) + 1, preComplexEvolutionTrace.size())){// + 1 is to not take the pull twice
					if(subComplexChange instanceof PullProperty && 
							((PullProperty) complexChange).getSuperClassName().equals(((PullProperty) subComplexChange).getSuperClassName())	){
						
						temp.getInvolvedComplexElement().add(subComplexChange);
					}
				}
				temp.getInvolvedComplexElement().add(complexChange);
				
				ptm.add(temp);
			}
		}
		//System.out.println("zehaahha"+ptm);
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
