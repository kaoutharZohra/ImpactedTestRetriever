package fr.lip6.meta.ComplexChangeDetection.GenericDetection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddSuperType;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.Classes;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.ComplexChangeDetection.Utils.TemporaryMemory;

public class TemporaryPull extends TemporaryComplexChange{

	public ArrayList<DeleteProperty> deletes = null;
	public ArrayList<AddProperty> adds = null;
	public String name = "";
	public ArrayList<ComplexChange> complexEvolutionTrace = null;
	
	public TemporaryPull(){
		//this.moveElements = new ArrayList<AtomicChange>();
		this.adds = new ArrayList<AddProperty>();
		this.deletes = new ArrayList<DeleteProperty>();
		
	}
	
	
	@Override
	public ArrayList<ComplexChange> validate(ArrayList<AtomicChange> trace,
			InitialState initState) {
		// TODO Auto-generated method stub
		this.complexEvolutionTrace = new ArrayList<ComplexChange>();
		ArrayList<TemporaryMemory> ptm= new ArrayList<TemporaryMemory>();
		for(AddProperty add : this.adds){
			TemporaryMemory temp = new TemporaryMemory(add.getName());
			for(DeleteProperty delete : this.deletes){
				//if(!temp.isPropertyInvolved(delete)){
					temp.getInvolvedAtomicElement().add(delete);
				//}
			}
			temp.getInvolvedAtomicElement().add(add);
			ptm.add(temp);
		}
		for(TemporaryMemory p : ptm){
			p.displayPropertiesTemporaryMemory();
			ArrayList<AtomicChange> deleteOccurences = p.getDeleteOccurences();
			ArrayList<AtomicChange> addOccurences = p.getAddOccurences();
			//just here below we prepare the deletes properties
			ArrayList<DeleteProperty> deletes = new ArrayList<DeleteProperty>();
			
			if(addOccurences.size() == 1 && deleteOccurences.size() > 0){//we might fullfil the pull definition
				AddProperty added = (AddProperty)addOccurences.get(0);
				ArrayList<String> subClasses = new ArrayList<String>();//subclasses of the pull property
				
				for(AtomicChange deleted : deleteOccurences){
					if((this.checkInheritanceInInitialState(initState, ((DeleteProperty) deleted ).getClassName(), added.getClassName())
							|| this.checkInheritanceInTrace(trace, ((DeleteProperty) deleted ).getClassName(), added.getClassName()))
							//&& this.doesInheritanceExist(((DeleteProperty) deleted ).getClassName(), added.getClassName(), trace, initState)
							){
						subClasses.add(((DeleteProperty) deleted ).getClassName());
						deletes.add((DeleteProperty) deleted);
					}
				}
				
				if(subClasses.size() > 0){
					PullProperty pullProperty = new PullProperty(added.getName(), added.getClassName(), subClasses);
					pullProperty.setAddPropertyPosition(trace.indexOf(added));
					pullProperty.setAddProperty(added);
					pullProperty.setDeleteProperties(deletes);
					
					complexEvolutionTrace.add(pullProperty);
					System.out.println("found");
				}
			}
		}
		return complexEvolutionTrace;
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
	public void add(AtomicChange element) {
		// TODO Auto-generated method stub
		this.name = element.getName();
		//System.out.println(" name = "+this.name );
		if(element instanceof AddProperty) this.adds.add((AddProperty) element);
		else if(element instanceof DeleteProperty) this.deletes.add((DeleteProperty) element);
	}
	
	public boolean doesInheritanceExist(String subclass, String superclass, ArrayList<AtomicChange> atomicEvolutionTrace, InitialState initState){
		//sourceClass = subclass , targetClass = superclass
		return this.doesInheritanceExistInitailState(subclass, superclass, initState) || this.doesInheritanceExistTrace(subclass, superclass, atomicEvolutionTrace);
	}

	private boolean doesInheritanceExistInitailState(String subclass, String superclass, InitialState initState) {
		
		//I should add the constraints that there is no delete super type in the trace, otherwise it could detect a pull even after delete supertype.
		
		if(initState.getOneClass(subclass) != null){
			for(Classes inheritance : initState.getOneClass(subclass).getInheritances()){
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
