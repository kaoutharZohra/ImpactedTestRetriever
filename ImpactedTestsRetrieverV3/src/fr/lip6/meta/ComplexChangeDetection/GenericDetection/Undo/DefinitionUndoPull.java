package fr.lip6.meta.ComplexChangeDetection.GenericDetection.Undo;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PushProperty;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.GenericDefinition;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.TemporaryComplexChange;

public class DefinitionUndoPull implements GenericDefinition{

	public ArrayList<ComplexChange> genericDefinitions = null;
	
	public DefinitionUndoPull(){
		this.genericDefinitions = new ArrayList<ComplexChange>();
		//add its composition/contents
		this.genericDefinitions.add(new PullProperty("","", new ArrayList<String>()));
		this.genericDefinitions.add(new PushProperty("","", new ArrayList<String>()));
	}
	
	@Override
	public boolean doesContain(AtomicChange element) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doesContain(ComplexChange element) {
		// TODO Auto-generated method stub
		for(ComplexChange a : this.genericDefinitions){
			//System.out.println("does contain = " + a.getClass()+" -_- "+  element.getClass());
			if(a.getClass().equals(element.getClass())){ 
				return true;
			}
		}
		return false;
	}

	@Override
	public TemporaryComplexChange create() {
		// TODO Auto-generated method stub
		return new TemporaryUndoPull();
	}

}
