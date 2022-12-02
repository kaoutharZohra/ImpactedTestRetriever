package fr.lip6.meta.ComplexChangeDetection.Detection;

import java.util.ArrayList;
import java.util.List;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddSuperType;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.SetProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.Classes;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.ComplexChangeDetection.Utils.TemporaryMemory;
import fr.lip6.meta.ComplexChangeDetection.Utils.References;

public class PullDetection {

	public InitialState initState = null;
	
	public PullDetection(InitialState initState) {
		super();
		this.initState = initState;
	}
	
	public ArrayList<ComplexChange> detectPullProperty(ArrayList<AtomicChange> atomicEvolutionTrace){
		ArrayList<ComplexChange> complexEvolutionTrace = new  ArrayList<ComplexChange>();
		ArrayList<TemporaryMemory> ptm= new ArrayList<TemporaryMemory>();
		
		for(AtomicChange element : atomicEvolutionTrace){
			if(element instanceof DeleteProperty){
				
			} else if(element instanceof AddProperty){
				if(atomicEvolutionTrace.indexOf(element) <= atomicEvolutionTrace.size() - 1){
					TemporaryMemory temp = new TemporaryMemory(element.getName());
					//check before the element if there is deletes of it, and add them to the PropertiesTemporaryMemory
					for(AtomicChange subElement : atomicEvolutionTrace.subList(0, atomicEvolutionTrace.indexOf(element))){
						if(subElement instanceof DeleteProperty && element.getName().equals(subElement.getName()) && !temp.isPropertyInvolved(subElement)){
							temp.getInvolvedAtomicElement().add(subElement);
							//System.out.println("1");
						}
						
					}
					//check after the element if there is deletes of it, and add them to the PropertiesTemporaryMemory
					for(AtomicChange subElement : atomicEvolutionTrace.subList(atomicEvolutionTrace.indexOf(element), atomicEvolutionTrace.size())){
						if(subElement instanceof DeleteProperty && element.getName().equals(subElement.getName()) && !temp.isPropertyInvolved(subElement)){
							temp.getInvolvedAtomicElement().add(subElement);
							//System.out.println("2");
						}
						
					}
					temp.getInvolvedAtomicElement().add(element);
					ptm.add(temp);
				}
				
			}
		}
		for(TemporaryMemory p : ptm){
			p.displayPropertiesTemporaryMemory();
			ArrayList<AtomicChange> deleteOccurences = p.getDeleteOccurences();
			ArrayList<AtomicChange> addOccurences = p.getAddOccurences();
			if(addOccurences.size() == 1 && deleteOccurences.size() > 0){//we might fullfil the pull definition
				AddProperty added = (AddProperty)addOccurences.get(0);
				ArrayList<String> subClasses = new ArrayList<String>();//subclasses of the pull property
				
				for(AtomicChange deleted : deleteOccurences){
					if(this.doesInheritanceExist(((DeleteProperty) deleted ).getClassName(), added.getClassName(), atomicEvolutionTrace)){
						subClasses.add(((DeleteProperty) deleted ).getClassName());
					}
				}
				
				if(subClasses.size() > 0){
					PullProperty pullProperty = new PullProperty(added.getName(), added.getClassName(), subClasses);
					pullProperty.setAddPropertyPosition(atomicEvolutionTrace.indexOf(added));
					
					complexEvolutionTrace.add(pullProperty);
					System.out.println("found");
				}
			}
		}
		
		
		return complexEvolutionTrace;
	}
	
	public boolean doesInheritanceExist(String subclass, String superclass, ArrayList<AtomicChange> atomicEvolutionTrace){
		//sourceClass = subclass , targetClass = superclass
		return this.doesInheritanceExistInitailState(subclass, superclass) || this.doesInheritanceExistTrace(subclass, superclass, atomicEvolutionTrace);
	}

	private boolean doesInheritanceExistInitailState(String subclass, String superclass) {
		
		//I should add the constraints that there is no delete super type in the trace, otherwise it could detect a pull even after delete supertype.
		
		if(this.initState.getOneClass(subclass) != null){
			for(Classes inheritance : this.initState.getOneClass(subclass).getInheritances()){
				if(inheritance.getName().equals(superclass)){
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean doesInheritanceExistTrace(String subclass, String superclass, ArrayList<AtomicChange> atomicEvolutionTrace) {
		
		for(AtomicChange c : atomicEvolutionTrace){
			if(c instanceof AddSuperType){
				if(((AddSuperType) c).getName().equals(subclass) && ((AddSuperType) c).getNameTarget().equals(superclass)){
					return true;
				}
				//check if there is not missing cases, ex: there is a deleteSuperType after AddSuperType between the same classes...
			}
		}
		return false;
	}
}
