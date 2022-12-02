package fr.lip6.meta.ComplexChangeDetection.Detection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddSuperType;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PushProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.Classes;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.ComplexChangeDetection.Utils.TemporaryMemory;

public class PushDetection {

	public InitialState initState = null;
	
	public PushDetection(InitialState initState) {
		super();
		this.initState = initState;
	}	
	
	public ArrayList<ComplexChange> detectPushProperty(ArrayList<AtomicChange> atomicEvolutionTrace){
		ArrayList<ComplexChange> complexEvolutionTrace = new  ArrayList<ComplexChange>();
		ArrayList<TemporaryMemory> ptm= new ArrayList<TemporaryMemory>();
		
		for(AtomicChange element : atomicEvolutionTrace){
			if(element instanceof DeleteProperty){
				if(atomicEvolutionTrace.indexOf(element) <= atomicEvolutionTrace.size() - 1){
					TemporaryMemory temp = new TemporaryMemory(element.getName());
					//check before the element if there is deletes of it, and add them to the PropertiesTemporaryMemory
					for(AtomicChange subElement : atomicEvolutionTrace.subList(0, atomicEvolutionTrace.indexOf(element))){
						if(subElement instanceof AddProperty && element.getName().equals(subElement.getName()) && !temp.isPropertyInvolved(subElement)){
							temp.getInvolvedAtomicElement().add(subElement);
							System.out.println("1");
						}
						
					}
					//check after the element if there is deletes of it, and add them to the PropertiesTemporaryMemory
					for(AtomicChange subElement : atomicEvolutionTrace.subList(atomicEvolutionTrace.indexOf(element), atomicEvolutionTrace.size())){
						if(subElement instanceof AddProperty && element.getName().equals(subElement.getName()) && !temp.isPropertyInvolved(subElement)){
							temp.getInvolvedAtomicElement().add(subElement);
							System.out.println("2");
						}
						
					}
					temp.getInvolvedAtomicElement().add(element);
					ptm.add(temp);
				}
			} else if(element instanceof AddProperty){
				
			}
		}
		for(TemporaryMemory p : ptm){
			p.displayPropertiesTemporaryMemory();
			ArrayList<AtomicChange> deleteOccurences = p.getDeleteOccurences();
			ArrayList<AtomicChange> addOccurences = p.getAddOccurences();
			if(addOccurences.size() > 0 && deleteOccurences.size() == 1){//we might fullfil the pull definition
				DeleteProperty deleted = (DeleteProperty) deleteOccurences.get(0);
				ArrayList<String> subClasses = new ArrayList<String>();
				for(AtomicChange added : addOccurences){
					if(this.doesInheritanceExist(((AddProperty) added ).getClassName(), deleted.getClassName(), atomicEvolutionTrace)){
						subClasses.add(((AddProperty) added ).getClassName());
					}
				}
				
				if(subClasses.size() > 0){
					PushProperty pushProperty = new PushProperty(deleted.getName(), deleted.getClassName(), subClasses);
					pushProperty.setDeletePropertyPosition(atomicEvolutionTrace.indexOf(deleted));
					
					complexEvolutionTrace.add(pushProperty);
					System.out.println("found");
				}
			}
			
		}
		/*for(PropertiesTemporaryMemory p : ptm){
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
					complexEvolutionTrace.add(pullProperty);
					System.out.println("found");
				}
			}
		}
		*/
		
		return complexEvolutionTrace;
	}
	
	public boolean doesInheritanceExist(String subclass, String superclass, ArrayList<AtomicChange> atomicEvolutionTrace){
		//sourceClass = subclass , targetClass = superclass
		return this.doesInheritanceExistInitailState(subclass, superclass) || this.doesInheritanceExistTrace(subclass, superclass, atomicEvolutionTrace);
	}
	
	private boolean doesInheritanceExistInitailState(String subclass, String superclass) {
		
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
