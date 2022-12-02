package fr.lip6.meta.ComplexChangeDetection.GenericDetection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;

public class DefinitionExtractSuper implements GenericDefinition{

	public ArrayList<ComplexChange> genericDefinitions = null;
	
	public DefinitionExtractSuper(){
		this.genericDefinitions = new ArrayList<ComplexChange>();
		//add its composition/contents
		//this.genericDefinitions.add(new AddClass());
		this.genericDefinitions.add(new PullProperty("", "", new ArrayList<String>()));
		//this.genericDefinitions.add(new AddProperty());
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
		return new TemporaryExtractSuper();
	}

}
