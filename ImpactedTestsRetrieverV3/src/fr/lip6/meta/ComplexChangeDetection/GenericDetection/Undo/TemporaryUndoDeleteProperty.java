package fr.lip6.meta.ComplexChangeDetection.GenericDetection.Undo;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.TemporaryComplexChange;
import fr.lip6.meta.ComplexChangeDetection.UndoChanges.UndoDeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;

public class TemporaryUndoDeleteProperty extends TemporaryComplexChange{

	public DeleteProperty delete = null;
	public AddProperty add = null;
	public String name = "";
	public ArrayList<ComplexChange> complexEvolutionTrace = null;
	
	public TemporaryUndoDeleteProperty(){
		
	}
	
	@Override
	public ArrayList<ComplexChange> validate(ArrayList<AtomicChange> trace,
			InitialState initState) {
		// TODO Auto-generated method stub
		this.complexEvolutionTrace = new  ArrayList<ComplexChange>();
		if(this.add != null && this.delete != null &&
				this.add.getClassName().equals(this.delete.getClassName())){
			UndoDeleteProperty iDelete = new UndoDeleteProperty(this.name, this.add, this.delete);
			complexEvolutionTrace.add(iDelete);
		}
		return complexEvolutionTrace;
	}

	@Override
	public boolean isPossibleToAdd(AtomicChange element) {
		// TODO Auto-generated method stub
		if(this.delete == null && this.add == null) return true;
		else if(element instanceof AddProperty && this.add == null && element.getName().equals(this.name)
				&& ((AddProperty)element).getClassName().equals(this.delete.getClassName())) return true;
		else if(element instanceof DeleteProperty && this.delete == null  && element.getName().equals(this.name)
				&& ((DeleteProperty)element).getClassName().equals(this.add.getClassName())) return true;
		
		return false;
	}

	@Override
	public void add(AtomicChange element) {
		// TODO Auto-generated method stub
		this.name = element.getName();
		if(element instanceof AddProperty && this.add == null) this.add = (AddProperty) element;
		else if(element instanceof DeleteProperty && this.delete == null) this.delete = (DeleteProperty) element;
	
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
