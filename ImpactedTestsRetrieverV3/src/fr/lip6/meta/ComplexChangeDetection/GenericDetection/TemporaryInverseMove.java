package fr.lip6.meta.ComplexChangeDetection.GenericDetection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.InverseMoveProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;

public class TemporaryInverseMove extends TemporaryComplexChange{

	public ArrayList<DeleteProperty> deletes = null;
	public ArrayList<AddProperty> adds = null;
	public String name = "";
	//public ArrayList<ComplexChange> complexEvolutionTrace = null;
	
	public TemporaryInverseMove(){
		//this.moveElements = new ArrayList<AtomicChange>();
		this.adds = new ArrayList<AddProperty>();
		this.deletes = new ArrayList<DeleteProperty>();
		
	}
	
	@Override
	public ArrayList<ComplexChange> validate(ArrayList<AtomicChange> trace,
			InitialState initState) {
		// TODO Auto-generated method stub
		ArrayList<ComplexChange> results = new  ArrayList<ComplexChange>();
		for(DeleteProperty delete : this.deletes){
			for(AddProperty add : this.adds){
				if((this.checkReferenceInInitialState(initState, add.getClassName(), delete.getClassName())
						|| this.checkReferenceInTrace(trace, add.getClassName(), delete.getClassName()))
						//&& this.doesReferenceExist(name, delete.getClassName(), add.getClassName(), trace, initState)
						){
					
					InverseMoveProperty move = new InverseMoveProperty(
							delete.getName(),
							delete.getClassName(),
							add.getClassName()	);
					//saving the position of when the move statrted and ended in the trace, in first and last variables
					move.setDeletePropertyPosition(trace.indexOf(delete));
					move.setAddPropertyPosition(trace.indexOf(add));
					move.setAddProperty(add);
					move.setDeleteProperty(delete);
					
					results.add(move);
				}
			}
		}
		return results;
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
		System.out.println(" name = "+this.name );
		if(element instanceof AddProperty) this.adds.add((AddProperty) element);
		else if(element instanceof DeleteProperty) this.deletes.add((DeleteProperty) element);
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
