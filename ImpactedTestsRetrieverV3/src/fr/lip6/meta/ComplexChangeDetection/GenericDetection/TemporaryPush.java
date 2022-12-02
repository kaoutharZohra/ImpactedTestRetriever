package fr.lip6.meta.ComplexChangeDetection.GenericDetection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddSuperType;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PushProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.Classes;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.ComplexChangeDetection.Utils.TemporaryMemory;

public class TemporaryPush extends TemporaryComplexChange{

	public ArrayList<DeleteProperty> deletes = null;
	public ArrayList<AddProperty> adds = null;
	public String name = "";
	public ArrayList<ComplexChange> complexEvolutionTrace = null;
	
	public TemporaryPush(){
		//this.moveElements = new ArrayList<AtomicChange>();
		this.adds = new ArrayList<AddProperty>();
		this.deletes = new ArrayList<DeleteProperty>();
		
	}
	
	@Override
	public ArrayList<ComplexChange> validate(ArrayList<AtomicChange> trace,
			InitialState initState) {
		// TODO Auto-generated method stub
		this.complexEvolutionTrace = new  ArrayList<ComplexChange>();
		ArrayList<TemporaryMemory> ptm= new ArrayList<TemporaryMemory>();
		for(DeleteProperty delete : this.deletes){
			TemporaryMemory temp = new TemporaryMemory(delete.getName());
			for(AddProperty add : this.adds){
				//if(!temp.isPropertyInvolved(delete)){
					temp.getInvolvedAtomicElement().add(add);
				//}
			}
			temp.getInvolvedAtomicElement().add(delete);
			ptm.add(temp);
		}
		for(TemporaryMemory p : ptm){
			p.displayPropertiesTemporaryMemory();
			ArrayList<AtomicChange> deleteOccurences = p.getDeleteOccurences();
			ArrayList<AtomicChange> addOccurences = p.getAddOccurences();
			//just here below we prepare the add properties
			ArrayList<AddProperty> adds = new ArrayList<AddProperty>();
			
			if(addOccurences.size() > 0 && deleteOccurences.size() == 1){//we might fullfil the pull definition
				DeleteProperty deleted = (DeleteProperty) deleteOccurences.get(0);
				ArrayList<String> subClasses = new ArrayList<String>();
				for(AtomicChange added : addOccurences){
					if((this.checkInheritanceInInitialState(initState, ((AddProperty) added ).getClassName(), deleted.getClassName())
							|| this.checkInheritanceInTrace(trace, ((AddProperty) added ).getClassName(), deleted.getClassName()))
							//&& this.doesInheritanceExist(((AddProperty) added ).getClassName(), deleted.getClassName(), trace, initState)
							){
						subClasses.add(((AddProperty) added ).getClassName());
						adds.add((AddProperty) added);
					}
				}
				
				if(subClasses.size() > 0){
					PushProperty pushProperty = new PushProperty(deleted.getName(), deleted.getClassName(), subClasses);
					pushProperty.setDeletePropertyPosition(trace.indexOf(deleted));
					pushProperty.setDeleteProperty(deleted);
					pushProperty.setAddProperties(adds);
					
					complexEvolutionTrace.add(pushProperty);
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
