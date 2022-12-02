package fr.lip6.meta.ComplexChangeDetection.GenericDetection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.InlineClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.InverseMoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;

public class TemporaryInline extends TemporaryComplexChange{

	public ArrayList<InverseMoveProperty> inverseMoves = null;
	public String name = "";
	
	public TemporaryInline(){
		this.inverseMoves = new ArrayList<InverseMoveProperty>();
	}
	
	@Override
	public ArrayList<ComplexChange> validate(ArrayList<AtomicChange> trace,
			InitialState initState) {
		// TODO Auto-generated method stub
		ArrayList<ComplexChange> results = new  ArrayList<ComplexChange>();
		
		if((this.existClassInInitialState(initState, this.name.split("_")[0])
				|| this.existAddClassInTrace(trace, this.name.split("_")[0]))
				&& this.existDeleteClassInTrace(trace, this.name.split("_")[0])){
			System.out.println("probabely inline class");
			ArrayList<InverseMoveProperty> clonedInverseMoves = (ArrayList<InverseMoveProperty>) this.inverseMoves.clone();
			
			ArrayList<String> propertiesNames = new ArrayList<String>();
			ArrayList<ComplexChange> inverseMoves = new ArrayList<ComplexChange>();
			for(InverseMoveProperty inverseMove : clonedInverseMoves){
				propertiesNames.add(inverseMove.getName());
				inverseMoves.add(inverseMove);
				//decoment below if you want h1 to be applied automatically
				//this.inverseMoves.remove(inverseMove);
				//preComplexEvolutionTrace.remove(inverseMove);//here we remove the moves that are part of this extract class, to remain coherent
			}
			InlineClass inlineClass = new InlineClass("n/a", this.name.split("_")[0], this.name.split("_")[1], propertiesNames, inverseMoves);
			inlineClass.setDeleteClass(this.getDeleteClassInTrace(trace, this.name.split("_")[0]));
			results.add(inlineClass);
			
		}
		/**/for(InverseMoveProperty im : this.inverseMoves){
			results.add(im);
		}
		return results;
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
		if(this.inverseMoves.size() == 0) return true;
		else if(element instanceof InverseMoveProperty 
				&& (this.name.equals(((InverseMoveProperty) element).getSourceClassName()+"_"+((InverseMoveProperty) element).getTargetClassName()))) return true;
		else return false;
	}

	@Override
	public void add(ComplexChange element) {
		// TODO Auto-generated method stub
		this.name = ((InverseMoveProperty) element).getSourceClassName()+"_"+((InverseMoveProperty) element).getTargetClassName();
		this.inverseMoves.add((InverseMoveProperty) element);
	}
	
	public DeleteClass getDeleteClassInTrace(ArrayList<AtomicChange> trace, String element){
		
		for(AtomicChange ac : trace){
			if(ac instanceof DeleteClass && ac.getName().equals(element)) return (DeleteClass)ac;
		}
		return null;
	}

}
