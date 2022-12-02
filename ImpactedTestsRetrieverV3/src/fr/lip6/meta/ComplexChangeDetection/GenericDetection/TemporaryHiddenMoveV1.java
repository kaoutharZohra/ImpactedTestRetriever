package fr.lip6.meta.ComplexChangeDetection.GenericDetection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.RenameProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;

public class TemporaryHiddenMoveV1 extends TemporaryComplexChange{

	//public ArrayList<DeleteProperty> deletes = null;
	//public ArrayList<AddProperty> adds = null;
	public ArrayList<RenameProperty> renames = null;
	public String name = "";
	
	public TemporaryHiddenMoveV1(){
		//this.moveElements = new ArrayList<AtomicChange>();
		//this.adds = new ArrayList<AddProperty>();
		//this.deletes = new ArrayList<DeleteProperty>();
		this.renames = new ArrayList<RenameProperty>();
	}
	
	@Override
	public ArrayList<ComplexChange> validate(ArrayList<AtomicChange> trace,
			InitialState initState) {
		// TODO Auto-generated method stub
		ArrayList<ComplexChange> results = new  ArrayList<ComplexChange>();
		for(RenameProperty rename : this.renames){
			for(RenameProperty subRename :this.renames.subList(this.renames.indexOf(rename), this.renames.size())){
				if(rename.getName().equals(subRename.getNewname()) 
						&& (this.checkReferenceInInitialState(initState, rename.getClassName(), subRename.getClassName())
								|| this.checkReferenceInTrace(trace, rename.getClassName(), subRename.getClassName()))){
					MoveProperty move = new MoveProperty(
							rename.getName(),
							rename.getClassName(),
							subRename.getClassName()	);
					//saving the position of when the move statrted and ended in the trace, in first and last variables
					move.setDeletePropertyPosition(trace.indexOf(rename));
					move.setAddPropertyPosition(trace.indexOf(subRename));
					results.add(move);
				}
			}
		}	
		return results;
	}

	@Override
	public boolean isPossibleToAdd(AtomicChange element) {
		// TODO Auto-generated method stub
		//if(this.renames.size() == 0 ) return true;//&& this.deletes.size() == 0 && this.adds.size() == 0
		//else if(element instanceof AddProperty && element.getName().equals(this.name)) return true;
		//else if(element instanceof DeleteProperty && element.getName().equals(this.name)) return true;
		//else 
			if(element instanceof RenameProperty ) return true; //&& element.getName().equals(this.name)
		//&& (element.getName().equals(this.name) || ((RenameProperty)element).getNewname().equals(this.name))
		else return false;
	}

	@Override
	public void add(AtomicChange element) {
		// TODO Auto-generated method stub
		//if(!(element instanceof RenameProperty)){this.name = element.getName();}
		System.out.println(" name = "+this.name );
		//if(element instanceof AddProperty) this.adds.add((AddProperty) element);
		//else if(element instanceof DeleteProperty) this.deletes.add((DeleteProperty) element);
		//else 
			if(element instanceof RenameProperty) this.renames.add((RenameProperty) element);
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
