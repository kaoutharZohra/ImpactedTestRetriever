package fr.lip6.meta.ComplexChangeDetection.GenericDetection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractSuperClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.ComplexChangeDetection.Utils.TemporaryMemory;

public class TemporaryExtractSuper extends TemporaryComplexChange{
	
	
	public ArrayList<PullProperty> pulls = null;
	public String name = "";
	
	public TemporaryExtractSuper(){
		this.pulls = new ArrayList<PullProperty>();
	}
	
	@Override
	public ArrayList<ComplexChange> validate(ArrayList<AtomicChange> trace,
			InitialState initState) {
		// TODO Auto-generated method stub
		ArrayList<ComplexChange> involvedElements = new  ArrayList<ComplexChange>();
		ArrayList<ComplexChange> results = new  ArrayList<ComplexChange>();
		//ArrayList<TemporaryMemory> ptm= this.reorderPulls(this.pulls);
		
		//for(TemporaryMemory tempMem : ptm){
			//tempMem.displayPropertiesTemporaryMemory();

			//for(AtomicChange atomicChange : atomicEvolutionTrace.subList(0, tempMem.getFirstPullAddPosition())){//we look for the add class before the first add property to it, and the added class is not in the initState of the MM
			if(!this.existClassInInitialState(initState, this.name)//tempMem.getName()
					&& this.existAddClassInTrace(trace, this.name)){
				//if(atomicChange instanceof AddClass && atomicChange.getName().equals(tempMem.getName()) && this.initState.getOneClass(atomicChange.getName()) == null){
					//instanciate extract super class, and in the preComplexEvolutionTrace remove the pulles that are part of it
				
				ArrayList<PullProperty> clonedPulls = (ArrayList<PullProperty>) this.pulls.clone();
					for(PullProperty pull : clonedPulls){//tempMem.getPullOccurences()){
						
						if(!involvedElements.contains(pull) && (clonedPulls.indexOf(pull) <= clonedPulls.size() - 1)
								//&& (tempMem.getPullOccurences().indexOf(pull) <= tempMem.getPullOccurences().size() - 1)
								){//if the element has not been treated before
							involvedElements.add(pull);
							
							ArrayList<String> propertiesNames = new ArrayList<String>();
							ArrayList<ComplexChange> involvedPulles = new ArrayList<ComplexChange>();
							ArrayList<String> subClassesNames = new ArrayList<String>();
							
							for(PullProperty subPull : clonedPulls.subList(clonedPulls.indexOf(pull), clonedPulls.size())
								//tempMem.getPullOccurences().subList(tempMem.getPullOccurences().indexOf(pull), tempMem.getPullOccurences().size())
									){
								if(!involvedElements.contains(subPull) && this.sameSubClasses((PullProperty)pull, (PullProperty)subPull)){//if not the same pull and has the same subclasses
									propertiesNames.add(subPull.getName());
									involvedPulles.add(subPull);
									//decoment below if you want h1 to be applied automatically
									//this.pulls.remove(subPull);//here we remove the pulles that are part of this extract super class, to remain coherent
									involvedElements.add(subPull);
								}
							}
							
							propertiesNames.add(pull.getName());
							subClassesNames = ((PullProperty) pull).getSubClassesNames();
							involvedPulles.add(pull);
							//decoment below if you want h1 to be applied automatically
							//this.pulls.remove(pull);//here we remove the pulles that are part of this extract super class, to remain coherent
						
							ExtractSuperClass extractSuperClass = new ExtractSuperClass("n/a", this.name, 
									//tempMem.getName(), 
									subClassesNames, propertiesNames, involvedPulles);
							extractSuperClass.setAddClass(this.getAddClassInTrace(trace, this.name));
							//ExtractClass extractClass = new ExtractClass("n/a", tempMem.getName().split("_")[0], tempMem.getName().split("_")[1], propertiesNames, pulles);
							results.add(extractSuperClass);
						}
						
					}
					
				}
		//}
		/**/for(PullProperty p : this.pulls){
			results.add(p);
		}
		return results;
	}

	@Override
	public boolean isPossibleToAdd(AtomicChange element) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void add(AtomicChange element) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isPossibleToAdd(ComplexChange element) {
		// TODO Auto-generated method stub
		if(this.pulls.size() == 0) return true;
		else if(element instanceof PullProperty 
				&& this.name.equals(((PullProperty) element).getSuperClassName())) return true;
		else return false;
	}

	@Override
	public void add(ComplexChange element) {
		// TODO Auto-generated method stub
		this.name = ((PullProperty) element).getSuperClassName();
		this.pulls.add((PullProperty) element);
	}

	public AddClass getAddClassInTrace(ArrayList<AtomicChange> trace, String element){
		
		for(AtomicChange ac : trace){
			
			if(ac instanceof AddClass && ac.getName().equals(element)){ 
				return ((AddClass)ac);
			}
		}
		return null;
	}

	/*private ArrayList<TemporaryMemory> reorderPulls(ArrayList<PullProperty> preComplexEvolutionTrace) {//to put the pulls between the same source and target together so they can becomre an extract class
		
		ArrayList<TemporaryMemory> ptm= new ArrayList<TemporaryMemory>();
		
		for(PullProperty complexChange : preComplexEvolutionTrace){
			if((preComplexEvolutionTrace.indexOf(complexChange) <= preComplexEvolutionTrace.size() - 1) &&
					!this.doesTemporaryMemoryInstanceExist(complexChange.getSuperClassName(), ptm)	){//if the superClass exists in ptm, it means that we already treated it before
				
				TemporaryMemory temp = new TemporaryMemory(complexChange.getSuperClassName());
				
				for(PullProperty subComplexChange : preComplexEvolutionTrace.subList(preComplexEvolutionTrace.indexOf(complexChange) + 1, preComplexEvolutionTrace.size())){// + 1 is to not take the pull twice
					if(complexChange.getSuperClassName().equals(subComplexChange.getSuperClassName())	){
						
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
	}*/
	
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
}
