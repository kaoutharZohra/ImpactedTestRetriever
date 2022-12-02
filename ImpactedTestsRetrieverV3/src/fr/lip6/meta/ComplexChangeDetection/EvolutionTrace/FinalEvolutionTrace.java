package fr.lip6.meta.ComplexChangeDetection.EvolutionTrace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import fr.lip6.meta.ComplexChangeDetection.Change;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractSuperClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.FlattenHierarchy;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.InlineClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.InverseMoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PushProperty;

public class FinalEvolutionTrace {

	private ArrayList<AtomicChange> atomicChanges = null;
	private ArrayList<ComplexChange> complexChanges = null;
	private ArrayList<Change> finalChanges = new ArrayList<Change>();
	
	public FinalEvolutionTrace() {
		super();
		// TODO Auto-generated constructor stub
		this.atomicChanges = new ArrayList<AtomicChange>();
		this.complexChanges = new ArrayList<ComplexChange>();
	}
	
	public FinalEvolutionTrace(ArrayList<AtomicChange> atomicChanges,
			ArrayList<ComplexChange> complexChanges) {
		super();
		this.atomicChanges = atomicChanges;
		this.complexChanges = complexChanges;
	}
	
	public ArrayList<AtomicChange> getAtomicChanges() {
		return atomicChanges;
	}
	
	public void setAtomicChanges(ArrayList<AtomicChange> atomicChanges) {
		this.atomicChanges = atomicChanges;
	}
	
	public ArrayList<ComplexChange> getComplexChanges() {
		return complexChanges;
	}
	
	public void setComplexChanges(ArrayList<ComplexChange> complexChanges) {
		this.complexChanges = complexChanges;
	}
	
	public ArrayList<Change> getFinalChanges() {
		return finalChanges;
	}
	
	public void setFinalChanges(ArrayList<Change> finalChanges) {
		this.finalChanges = finalChanges;
	}
	
	public ArrayList<Change> computeFinalChanges(){
		
		for(ComplexChange c : this.complexChanges){
			
			if(c instanceof MoveProperty){//here we remove the add property and the delete property
				this.atomicChanges.remove(((MoveProperty) c).getDeleteProperty());
				this.atomicChanges.remove(((MoveProperty) c).getAddProperty());
				
			} else if(c instanceof PullProperty){//here we remove the add property and the delete properties
				this.atomicChanges.remove(((PullProperty) c).getAddProperty());
				for(DeleteProperty d : ((PullProperty) c).getDeleteProperties()){
					this.atomicChanges.remove(d);
				}
				
			} else if(c instanceof PushProperty){//here we remove the add properties and the delete property
				this.atomicChanges.remove(((PushProperty) c).getDeleteProperty());
				for(AddProperty a : ((PushProperty) c).getAddProperties()){
					this.atomicChanges.remove(a);
				}
				
			} else if(c instanceof ExtractClass){
				for(ComplexChange move : ((ExtractClass) c).getMoves()){
					if (move instanceof MoveProperty){
						this.atomicChanges.remove(((MoveProperty) move).getDeleteProperty());
						this.atomicChanges.remove(((MoveProperty) move).getAddProperty());
					}
				}
				this.atomicChanges.remove(((ExtractClass) c).getAddClass());
				
			} else if(c instanceof ExtractSuperClass){
				for(ComplexChange pull : ((ExtractSuperClass) c).getPulles()){
					if(pull instanceof PullProperty){
						this.atomicChanges.remove(((PullProperty) pull).getAddProperty());
						for(DeleteProperty d : ((PullProperty) pull).getDeleteProperties()){
							this.atomicChanges.remove(d);
						}
					}
				}
				
			} else if(c instanceof FlattenHierarchy){
				for(ComplexChange push : ((FlattenHierarchy) c).getPushes()){
					if(push instanceof PushProperty){
						this.atomicChanges.remove(((PushProperty) push).getDeleteProperty());
						for(AddProperty a : ((PushProperty) push).getAddProperties()){
							this.atomicChanges.remove(a);
						}
					}
				}
				
			} else if(c instanceof InlineClass){
				for(ComplexChange imove : ((InlineClass) c).getInverseMoves()){
					if(imove instanceof InverseMoveProperty){
						this.atomicChanges.remove(((InverseMoveProperty) imove).getDeleteProperty());
						this.atomicChanges.remove(((InverseMoveProperty) imove).getAddProperty());
					}
				}
				this.atomicChanges.remove(((InlineClass) c).getDeleteClass());
				
			} else if(c instanceof InverseMoveProperty){
				
			}
		}
		
		//this.finalChanges = this.orderFinalChanges();
		//return this.finalChanges;
		return this.orderFinalChanges();
	}
	
	public ArrayList<Change> orderFinalChanges(){
		
		//here we put as id the start position of the complex changes
		for(ComplexChange c : this.complexChanges){
			c.setId(String.valueOf(c.startPosition()));
		}
		//now we add the atomic and complex changes into one list
		for(AtomicChange a : this.atomicChanges){
			this.finalChanges.add(a);
		}
		
		for(ComplexChange c : this.complexChanges){
			this.finalChanges.add(c);
		}
		System.out.println("	before");
		for(Change c : this.finalChanges){
			System.out.println("id = "+c.getId() +" => "+ c);
		}
		
		//**********************Using java Collection sort******************************************
		
		//List<CustomObject> list = new ArrayList<CustomObject>();
		Comparator<Change> comparator = new Comparator<Change>() {
		    public int compare(Change c1, Change c2) {
		        /*if(Integer.parseInt(c1.getId()) < Integer.parseInt(c2.getId()))
		        	return -1;
		        else if(Integer.parseInt(c1.getId()) > Integer.parseInt(c2.getId()))
		        	return 1;
		        else return 0;*/
		    	return Integer.parseInt(c1.getId()) - Integer.parseInt(c2.getId());
		    }
		};

		Collections.sort(this.finalChanges, comparator); // use the comparator as much as u want
		System.out.println(this.finalChanges);
		
		//**********************************************************************************
		
		//ArrayList<Change> changes = this.mergeSortAlgorithm(finalChanges);
		
		//now we re orden the complex changes
		//ArrayList<ComplexChange> occ = new ArrayList<ComplexChange>();
		//occ.ad
		System.out.println("	after");
		for(Change c : this.finalChanges){
			System.out.println("id = "+c.getId() +" => "+ c);
		}
		
		return this.finalChanges;
	}
	/*
	public ArrayList<Change> mergeSortAlgorithm(ArrayList<Change> m){
	    // Base case. A list of zero or one elements is sorted, by definition.
	    if (m.size() <= 1)
	        return m;
	
	    // Recursive case. First, *divide* the list into equal-sized sublists.
	    ArrayList<Change> left = new ArrayList<Change>(); 
	    ArrayList<Change> right = new ArrayList<Change>();
	    
	    int middle = m.size() / 2;
	    
	    for(Change c : m.subList(0, middle)){ //each x in m before middle
	         //add x to left
	         left.add(c);
	    }
	    for(Change c : m.subList(middle+1, m.size())){// each x in m after or equal middle
	         //add x to right
	    	right.add(c);
	    }
	    // Recursively sort both sublists.
	    left = mergeSortAlgorithm(left);
	    right = mergeSortAlgorithm(right);
	    // *Conquer*: merge the now-sorted sublists.
	    return merge(left, right);
	}
	
	public ArrayList<Change> merge(ArrayList<Change> left, ArrayList<Change> right){
		ArrayList<Change> result = new ArrayList<Change>();
	    //left.
		while(!left.isEmpty() && !right.isEmpty()){
	        if (Integer.parseInt(left.get(0).getId()) <= Integer.parseInt(right.get(0).getId())){
	        	result.add(left.get(0)); // append first(left) to result
	            if(left.size()!=1)
	            	left = (ArrayList<Change>) left.subList(1, left.size());//left = rest(left)
	            else left.remove(0);
	        }
	        else {
	        	result.add(right.get(0)); //append first(right) to result
	        	if(right.size()!=1)
	        		right = (ArrayList<Change>) right.subList(1, right.size());//right = rest(right)
	        	else right.remove(0);
	        }
		}
		
	    // either left or right may have elements left
	    //TODO you can try with a for each, to gain performance !! 
		while (!left.isEmpty()){
			result.add(left.get(0)); // append first(left) to result
			if(left.size()!=1)
            	left = (ArrayList<Change>) left.subList(1, left.size());//left = rest(left)
            else left.remove(0);
		}
	    while (!right.isEmpty()){
	    	result.add(right.get(0)); //append first(right) to result
	    	if(right.size()!=1)
        		right = (ArrayList<Change>) right.subList(1, right.size());//right = rest(right)
        	else right.remove(0);
	    }
	    return result;
	}
	*/
	
	public void afficherFinalChanges(){//show:print final changes
		for(Change c : this.finalChanges){
			System.out.println(c);
		}
	}
}
