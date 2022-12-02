package fr.lip6.meta.ComplexChangeDetection.GenericDetection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.SetProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.ComplexChangeDetection.Utils.References;
import fr.lip6.meta.ComplexChangeDetection.Utils.TemporaryMemory;

public class TemporaryExtractV0 extends TemporaryComplexChange{

	public ArrayList<AddClass> extractClass = null;
	public ArrayList<DeleteProperty> deletes = null;
	public ArrayList<AddProperty> adds = null;
	public String name = "";
	public ArrayList<ComplexChange> complexEvolutionTrace = null;
	
	public TemporaryExtractV0(){
		//this.moveElements = new ArrayList<AtomicChange>();
		this.adds = new ArrayList<AddProperty>();
		this.deletes = new ArrayList<DeleteProperty>();
		this.extractClass = new ArrayList<AddClass>();
		
	}
	
	@Override
	public ArrayList<ComplexChange> validate(ArrayList<AtomicChange> trace,
			InitialState initState) {
		// TODO Auto-generated method stub
		ArrayList<ComplexChange> results = new  ArrayList<ComplexChange>();
		this.complexEvolutionTrace = new  ArrayList<ComplexChange>();
		for(DeleteProperty delete : this.deletes){
			for(AddProperty add : this.adds){
				if(delete.getName().equals(add.getName()) 
						&& (this.checkReferenceInInitialState(initState, delete.getClassName(), add.getClassName())
						|| this.checkReferenceInTrace(trace, delete.getClassName(), add.getClassName()))
						//&& this.doesReferenceExist(name, delete.getClassName(), add.getClassName(), trace, initState)
						){
					
					MoveProperty move = new MoveProperty(
							delete.getName(),
							delete.getClassName(),
							add.getClassName()	);
					//saving the position of when the move statrted and ended in the trace, in first and last variables
					move.setDeletePropertyPosition(trace.indexOf(delete));
					move.setAddPropertyPosition(trace.indexOf(add));
					complexEvolutionTrace.add(move);
					//System.out.println("	move name "+move.getName());
				}
			}
		}

		
		/**/ArrayList<TemporaryMemory> ptm= this.reorderMoves(complexEvolutionTrace);
		for(AtomicChange atomicChange : trace){
			if(atomicChange instanceof AddClass) this.extractClass.add((AddClass) atomicChange); 
		}
		for(TemporaryMemory tempMem : ptm){
			
			//for(AtomicChange atomicChange : trace.subList(0, tempMem.getFirstMoveAddPosition())){//we look for the add class before the first add property to it, and the added class is not in the initState of the MM
			for(AddClass atomicChange : this.extractClass){
				System.out.println("	** inside "+atomicChange.getName());
				if(atomicChange instanceof AddClass && atomicChange.getName().equals(tempMem.getName().split("_")[1]) && initState.getOneClass(atomicChange.getName()) == null){
					//instanciate extract class, and in the preComplexEvolutionTrace remove the moves that are part of it
					//System.out.println("	** inside "+atomicChange.getName());
					ArrayList<String> propertiesNames = new ArrayList<String>();
					//	System.out.println("	extract 2");
					ArrayList<ComplexChange> moves = new ArrayList<ComplexChange>();
					for(ComplexChange move : tempMem.getMoveOccurences()){
						propertiesNames.add(move.getName());
						moves.add(move);
						complexEvolutionTrace.remove(move);//here we remove the moves that are part of this extract class, to remain coherent
					}
					//System.out.println("	extract 3");
					ExtractClass extractClass = new ExtractClass("n/a", tempMem.getName().split("_")[0], tempMem.getName().split("_")[1], propertiesNames, moves);
					results.add(extractClass);
				}
			}
		}
		
		return results;
	}

	@Override
	public boolean isPossibleToAdd(AtomicChange element) {
		// TODO Auto-generated method stub
		if(this.extractClass.size() == 0 && this.deletes.size() == 0 && this.adds.size() == 0) return true;
		//else if(element instanceof AddClass) return true; 
		else if(element instanceof AddProperty ) return true;//&& (element.getName().equals(this.name) || this.name=="")
		else if(element instanceof DeleteProperty ) return true;//&& (element.getName().equals(this.name) || this.name=="")
		else return false;
		
	}

	@Override
	public void add(AtomicChange element) {
		// TODO Auto-generated method stub
		//if(!(element instanceof AddClass)) 
		this.name = element.getName();
		//System.out.println(" name = "+this.name );
		System.out.println(" name = "+element.getName());
		//if (element instanceof AddClass) this.extractClass.add((AddClass) element);
		//else 
		if(element instanceof AddProperty) this.adds.add((AddProperty) element);
		else if(element instanceof DeleteProperty) this.deletes.add((DeleteProperty) element);
	}
	
	private ArrayList<TemporaryMemory> reorderMoves(ArrayList<ComplexChange> preComplexEvolutionTrace) {//to put the moves between the same source and target together so they can becomre an extract class
		
		ArrayList<TemporaryMemory> ptm= new ArrayList<TemporaryMemory>();
		
		for(ComplexChange complexChange : preComplexEvolutionTrace){
			if(complexChange instanceof MoveProperty && (preComplexEvolutionTrace.indexOf(complexChange) <= preComplexEvolutionTrace.size() - 1) &&
					!this.doesTemporaryMemoryInstanceExist(((MoveProperty) complexChange).getSourceClassName()+"_"+((MoveProperty) complexChange).getTargetClassName(), ptm)	){//if the composed name exist in ptm, it means that we already treated it before
				
				TemporaryMemory temp = new TemporaryMemory(((MoveProperty) complexChange).getSourceClassName()+"_"+((MoveProperty) complexChange).getTargetClassName());
				
				for(ComplexChange subComplexChange : preComplexEvolutionTrace.subList(preComplexEvolutionTrace.indexOf(complexChange) + 1, preComplexEvolutionTrace.size())){// + 1 is to not take the move twice
					if(subComplexChange instanceof MoveProperty && 
							((MoveProperty) complexChange).getSourceClassName().equals(((MoveProperty) subComplexChange).getSourceClassName()) &&
							((MoveProperty) complexChange).getTargetClassName().equals(((MoveProperty) subComplexChange).getTargetClassName())	){
						
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
	
	public boolean doesReferenceExist(String property, String sourceClass, String targetClass, ArrayList<AtomicChange> atomicEvolutionTrace, InitialState initState){
		
		//here we test, if there is a ref in the source class going to the target class, first in the metamodel and then in the trace
		//missing cases, where in the trace we add a ref and then we deleted it , or it exists in the metamodel but deleted in the trace => to be added/checked later!!
		return this.doesReferenceExistInitailState(sourceClass, targetClass, initState) || this.doesReferenceExistTrace(property, sourceClass, targetClass, atomicEvolutionTrace);
	}
	
	public boolean doesReferenceExistInitailState(String sourceClass, String targetClass, InitialState initState){
		
		if(initState.getOneClass(sourceClass) != null){
			for(References reference : initState.getOneClass(sourceClass).getReferences()){
				if(reference.getTargetClass().getName().equals(targetClass)){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean doesReferenceExistTrace(String property, String sourceClass, String targetClass, ArrayList<AtomicChange> atomicEvolutionTrace){
		
		for(AtomicChange c : atomicEvolutionTrace){// I will check if there is an added ref between source and target, ie. if there is an addProperty in source and then its type is set to target with setProperty
			if(c instanceof AddProperty && ((AddProperty) c).getClassName().equals(sourceClass)){
				for(AtomicChange subC : atomicEvolutionTrace.subList(atomicEvolutionTrace.indexOf(c), atomicEvolutionTrace.size())){
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
