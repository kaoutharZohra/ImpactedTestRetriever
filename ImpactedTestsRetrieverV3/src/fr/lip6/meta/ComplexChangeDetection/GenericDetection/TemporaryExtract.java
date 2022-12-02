package fr.lip6.meta.ComplexChangeDetection.GenericDetection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.ComplexChangeDetection.Utils.TemporaryMemory;

public class TemporaryExtract extends TemporaryComplexChange{

	public ArrayList<MoveProperty> moves = null;
	public String name = "";
	
	public TemporaryExtract(){
		this.moves = new ArrayList<MoveProperty>();
	}
	
	@Override
	public ArrayList<ComplexChange> validate(ArrayList<AtomicChange> trace,
			InitialState initState) {
		// TODO Auto-generated method stub
		ArrayList<ComplexChange> results = new  ArrayList<ComplexChange>();
		ArrayList<TemporaryMemory> ptm= this.reorderMoves(this.moves);
		/*for(AtomicChange atomicChange : trace){
			if(atomicChange instanceof AddClass) this.extractClass.add((AddClass) atomicChange); 
		}*/
		for(TemporaryMemory tempMem : ptm){
			System.out.println("	** "+tempMem.getName().split("_")[1]+" first = "+this.existClassInInitialState(initState, tempMem.getName().split("_")[1]));
			System.out.println("	** "+tempMem.getName().split("_")[1]+" && second = "+this.existAddClassInTrace(trace, tempMem.getName().split("_")[1]));
			
			if(!this.existClassInInitialState(initState, tempMem.getName().split("_")[1])
					&& this.existAddClassInTrace(trace, tempMem.getName().split("_")[1])){
				//TODO here may be check whetehr the ref to it is single or not, or check it at the end may be??
			//for(AtomicChange atomicChange : trace.subList(0, tempMem.getFirstMoveAddPosition())){//we look for the add class before the first add property to it, and the added class is not in the initState of the MM
			//for(AddClass atomicChange : this.extractClass){
				//System.out.println("	** inside "+atomicChange.getName());
				//if(atomicChange instanceof AddClass && atomicChange.getName().equals(tempMem.getName().split("_")[1]) && initState.getOneClass(atomicChange.getName()) == null){
					//instanciate extract class, and in the preComplexEvolutionTrace remove the moves that are part of it
					//System.out.println("	** inside "+atomicChange.getName());
					ArrayList<String> propertiesNames = new ArrayList<String>();
					//	System.out.println("	extract 2");
					ArrayList<ComplexChange> involvedMoves = new ArrayList<ComplexChange>();
					
					for(ComplexChange move : tempMem.getMoveOccurences()){
						propertiesNames.add(move.getName());
						involvedMoves.add(move);
						//decoment below if you want h1 to be applied automatically
						//this.moves.remove(move);//here we remove the moves that are part of this extract class, to remain coherent
					}
					//System.out.println("	extract 3");
					ExtractClass extractClass = new ExtractClass("n/a", tempMem.getName().split("_")[0], tempMem.getName().split("_")[1], propertiesNames, involvedMoves);
					extractClass.setAddClass(this.getAddClassInTrace(trace, tempMem.getName().split("_")[1]));
					results.add(extractClass);
				//}
			}
		}
		/**/for(MoveProperty m : this.moves){
			results.add(m);
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
		if(this.moves.size() == 0) return true;
		else if(element instanceof MoveProperty 
				&& (this.name.equals(((MoveProperty) element).getSourceClassName()+"_"+((MoveProperty) element).getTargetClassName()))) return true;
		else return false;
	}

	@Override
	public void add(ComplexChange element) {
		// TODO Auto-generated method stub	
		this.name = ((MoveProperty) element).getSourceClassName()+"_"+((MoveProperty) element).getTargetClassName();
		this.moves.add((MoveProperty) element);
	}
	
	public AddClass getAddClassInTrace(ArrayList<AtomicChange> trace, String element){
		
		for(AtomicChange ac : trace){
			
			if(ac instanceof AddClass && ac.getName().equals(element)){ 
				return ((AddClass)ac);
			}
		}
		return null;
	}

	private ArrayList<TemporaryMemory> reorderMoves(ArrayList<MoveProperty> preComplexEvolutionTrace) {//to put the moves between the same source and target together so they can becomre an extract class
		
		ArrayList<TemporaryMemory> ptm= new ArrayList<TemporaryMemory>();
		
		for(MoveProperty complexChange : preComplexEvolutionTrace){
			if((preComplexEvolutionTrace.indexOf(complexChange) <= preComplexEvolutionTrace.size() - 1) &&
					!this.doesTemporaryMemoryInstanceExist(complexChange.getSourceClassName()+"_"+complexChange.getTargetClassName(), ptm)	){//if the composed name exist in ptm, it means that we already treated it before
				
				TemporaryMemory temp = new TemporaryMemory(complexChange.getSourceClassName()+"_"+complexChange.getTargetClassName());
				
				for(MoveProperty subComplexChange : preComplexEvolutionTrace.subList(preComplexEvolutionTrace.indexOf(complexChange) + 1, preComplexEvolutionTrace.size())){// + 1 is to not take the move twice
					if(complexChange.getSourceClassName().equals(subComplexChange.getSourceClassName()) &&
							complexChange.getTargetClassName().equals(subComplexChange.getTargetClassName())	){
						
						temp.getInvolvedComplexElement().add(subComplexChange);
					}
				}
				temp.getInvolvedComplexElement().add(complexChange);
				//temp.displayPropertiesTemporaryMemory();
				ptm.add(temp);
			}
		}
		
		return ptm;
	}

	private boolean doesTemporaryMemoryInstanceExist(String name, ArrayList<TemporaryMemory> ptm){
		
		for(TemporaryMemory tempMem : ptm){
			if(tempMem.getName().equals(name)){
				return true;
			}
		}
		return false;
	}

}
