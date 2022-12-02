package fr.lip6.meta.ComplexChangeDetection.GenericDetection.Undo;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.GenericDefinition;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.TemporaryComplexChange;

public class DefinitionUndoAddProperty implements GenericDefinition{

	public ArrayList<AtomicChange> genericDefinitions = null;

	public DefinitionUndoAddProperty(){
		this.genericDefinitions = new ArrayList<AtomicChange>();
		//add its composition/contents
		this.genericDefinitions.add(new AddProperty());
		this.genericDefinitions.add(new DeleteProperty());
		
	}
	
	@Override
	public boolean doesContain(AtomicChange element) {
		// TODO Auto-generated method stub
		for(AtomicChange a : this.genericDefinitions){
			//System.out.println("does contain = " + a.getClass()+" -_- "+  element.getClass());
			if(a.getClass().equals(element.getClass())){ 
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean doesContain(ComplexChange element) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TemporaryComplexChange create() {
		// TODO Auto-generated method stub
		return new TemporaryUndoAddProperty();
	}
	
}
