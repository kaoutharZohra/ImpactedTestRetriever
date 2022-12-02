package fr.lip6.meta.ComplexChangeDetection.GenericDetection.Undo;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PushProperty;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.TemporaryComplexChange;
import fr.lip6.meta.ComplexChangeDetection.UndoChanges.UndoPull;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;

public class TemporaryUndoPull extends TemporaryComplexChange{

	public PullProperty pull = null;
	public PushProperty push = null;
	public String name = "";
	public ArrayList<ComplexChange> complexEvolutionTrace = null;
	
	public TemporaryUndoPull(){
		
	}
	
	@Override
	public ArrayList<ComplexChange> validate(ArrayList<AtomicChange> trace,
			InitialState initState) {
		// TODO Auto-generated method stub
		this.complexEvolutionTrace = new  ArrayList<ComplexChange>();
		
		if(this.pull != null && this.push != null &&
				this.pull.getSuperClassName().equals(this.push.getSuperClassName())){
			boolean sameSubClasses = true;
			
			for(String sub : this.pull.getSubClassesNames()){
				if(!this.push.getSubClassesNames().contains(sub)) sameSubClasses = false;
			}
			
			for(String sub : this.push.getSubClassesNames()){
				if(!this.pull.getSubClassesNames().contains(sub)) sameSubClasses = false;
			}
			
			if(sameSubClasses){
				UndoPull iPull = new UndoPull(this.name, this.pull, this.push);
				complexEvolutionTrace.add(iPull);
			}
			
		}
		return complexEvolutionTrace;
	}

	@Override
	public boolean isPossibleToAdd(AtomicChange element) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void add(AtomicChange element) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isPossibleToAdd(ComplexChange element) {
		// TODO Auto-generated method stub
		if(this.pull == null && this.push == null) return true;
		else if(element instanceof PullProperty && this.pull == null && element.getName().equals(this.name)) return true;
		else if(element instanceof PushProperty && this.push == null && element.getName().equals(this.name)) return true;
		return false;
	}

	@Override
	public void add(ComplexChange element) {
		// TODO Auto-generated method stub
		this.name = element.getName();
		if(element instanceof PullProperty && this.pull == null) this.pull = (PullProperty) element;
		else if(element instanceof PushProperty && this.push == null) this.push = (PushProperty) element;
		
	}

}
