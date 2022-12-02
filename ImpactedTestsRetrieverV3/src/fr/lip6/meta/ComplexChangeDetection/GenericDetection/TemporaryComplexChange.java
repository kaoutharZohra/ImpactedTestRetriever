package fr.lip6.meta.ComplexChangeDetection.GenericDetection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddSuperType;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.SetProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.Utils.Classes;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.ComplexChangeDetection.Utils.References;

public abstract class TemporaryComplexChange {

	public abstract ArrayList<ComplexChange> validate(ArrayList<AtomicChange> trace, InitialState initState);
	//public boolean doesContains(AtomicChange element){ return false; }	
	public abstract boolean isPossibleToAdd(AtomicChange element);
	public abstract void add(AtomicChange element);
	public abstract boolean isPossibleToAdd(ComplexChange element);
	public abstract void add(ComplexChange element);
	
	
	public boolean checkReferenceInInitialState(InitialState initState, String sourceClass, String targetClass){
				
		if(initState.getOneClass(sourceClass) != null){
			for(References reference : initState.getOneClass(sourceClass).getReferences()){
				if(reference.getTargetClass().getName().equals(targetClass)){
					return true;
				}
			}
		}
		return false;
	}
	
	public ArrayList<String> getReferenceInInitialState(InitialState initState, String sourceClass, String targetClass){
		
		ArrayList<String> referencesNames = new ArrayList<String>();
		
		if(initState.getOneClass(sourceClass) != null){
			for(References reference : initState.getOneClass(sourceClass).getReferences()){
				if(reference.getTargetClass().getName().equals(targetClass)){
					//return true;
					referencesNames.add(reference.getName());
					//return reference.getName();
				}
			}
		}
		//return false;
		return referencesNames;
	}
	
	public boolean checkReferenceInTrace(ArrayList<AtomicChange> trace, String sourceClass, String targetClass){
		
		for(AtomicChange c : trace){// I will check if there is an added ref between source and target, ie. if there is an addProperty in source and then its type is set to target with setProperty
			if(c instanceof AddProperty && ((AddProperty) c).getClassName().equals(sourceClass)){
				for(AtomicChange subC : trace.subList(trace.indexOf(c), trace.size())){
					if(subC instanceof SetProperty){
						if(subC.getName().equals(c.getName()) && ((SetProperty) subC).getType().equals(targetClass)){
							return true;
						}
			
						//check if there is not missing cases, ex: there is a setProperty without another setProperty of the same property to another type...
					}
				}
			}
			// and you can check whether an existing property in sourceClass now have the type targetClass (with a setProportyType(_, targetClass); 
		}
		return false;
	}
	
	public ArrayList<String> getReferenceInTrace(ArrayList<AtomicChange> trace, String sourceClass, String targetClass){
		
		ArrayList<String> referencesNames = new ArrayList<String>();
		
		for(AtomicChange c : trace){// I will check if there is an added ref between source and target, ie. if there is an addProperty in source and then its type is set to target with setProperty
			if(c instanceof AddProperty && ((AddProperty) c).getClassName().equals(sourceClass)){
				for(AtomicChange subC : trace.subList(trace.indexOf(c), trace.size())){
					if(subC instanceof SetProperty){
						if(subC.getName().equals(c.getName()) && ((SetProperty) subC).getType().equals(targetClass)){
							referencesNames.add(subC.getName());
							//return subC.getName();
						}
			
						//check if there is not missing cases, ex: there is a setProperty without another setProperty of the same property to another type...
					}
				}
			}
			// and you can check whether an existing property in sourceClass now have the type targetClass (with a setProportyType(_, targetClass); 
		}
		return referencesNames;
	}
	
	public boolean checkInheritanceInInitialState(InitialState initState, String subclass, String superclass){
		
		if(initState.getOneClass(subclass) != null){
			for(Classes inheritance : initState.getOneClass(subclass).getInheritances()){
				if(inheritance.getName().equals(superclass)){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean checkInheritanceInTrace(ArrayList<AtomicChange> trace, String subclass, String superclass){
		//System.out.println("\n\nZEhhahahah\n\n");
		for(AtomicChange c : trace){
			if(c instanceof AddSuperType){
				//System.out.println("\n\n"+((AddSuperType) c).getName()+" "+subclass+" -_- "+((AddSuperType) c).getNameTarget()+" "+ superclass+"\n\n");
				if(((AddSuperType) c).getName().equals(subclass) && ((AddSuperType) c).getNameTarget().equals(superclass)){
					//System.out.println("\n\n found true \n\n");
					return true;
				}
				//check if there is not missing cases, ex: there is a deleteSuperType after AddSuperType between the same classes...
			}
		}
		return false;
	}
	//String sourceClass, String targetClass, ArrayList<AtomicChange> atomicEvolutionTrace, InitialState initState
	
	/**
	 * checks whether the class element is present in the Initial State
	 * */
	public boolean existClassInInitialState(InitialState initState, String element){
		
		return (initState.getOneClass(element) != null);
	}
	
	/**
	 * checks whether the class element is added in the Trace
	 * */
	public boolean existAddClassInTrace(ArrayList<AtomicChange> trace, String element){
		
		for(AtomicChange ac : trace){
			
			if(ac instanceof AddClass && ac.getName().equals(element)){ 
				System.out.println("current = "+ac.getName());
				System.out.println("element = "+ac.getName());
				return true;
			}
		}
		return false;
	}
	
	/**
	 * checks whether the class element is deleted in the Trace
	 * */
	public boolean existDeleteClassInTrace(ArrayList<AtomicChange> trace, String element){
		
		for(AtomicChange ac : trace){
			if(ac instanceof DeleteClass && ac.getName().equals(element)) return true;
		}
		return false;
	}
}
