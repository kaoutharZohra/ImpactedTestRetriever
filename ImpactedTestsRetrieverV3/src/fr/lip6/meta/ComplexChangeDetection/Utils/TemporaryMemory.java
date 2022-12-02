package fr.lip6.meta.ComplexChangeDetection.Utils;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PushProperty;

public class TemporaryMemory {

	public String name = "";
	public ArrayList<AtomicChange> involvedAtomicElement = null;
	public ArrayList<ComplexChange> involvedComplexElement = null;
	
	public TemporaryMemory(String name) {
		super();
		this.name = name;
		this.involvedAtomicElement =  new  ArrayList<AtomicChange>();
		this.involvedComplexElement = new ArrayList<ComplexChange>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<AtomicChange> getInvolvedAtomicElement() {
		return involvedAtomicElement;
	}

	public void setInvolvedAtomicElement(ArrayList<AtomicChange> involvedElement) {
		this.involvedAtomicElement = involvedElement;
	}
	
	public ArrayList<ComplexChange> getInvolvedComplexElement() {
		return involvedComplexElement;
	}

	public void setInvolvedComplexElement(
			ArrayList<ComplexChange> involvedComplexElement) {
		this.involvedComplexElement = involvedComplexElement;
	}

	public boolean isPropertyInvolved(AtomicChange atomicChange){
		
		if(atomicChange instanceof DeleteProperty){
			for(AtomicChange t : this.involvedAtomicElement){
				if(t instanceof DeleteProperty){
					if(atomicChange.getName().equals(t.getName()) && ((DeleteProperty) atomicChange).getClassName().equals(((DeleteProperty) t).getClassName())){
						return true;
					}
				}
			} 
		} else if (atomicChange instanceof AddProperty){
			for(AtomicChange t : this.involvedAtomicElement){
				if(t instanceof AddProperty){
					if(atomicChange.getName().equals(t.getName()) && ((AddProperty) atomicChange).getClassName().equals(((AddProperty) t).getClassName())){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void displayPropertiesTemporaryMemory(){
		
		System.out.println("	*** TemporaryMemory name = "+this.name);
		for(AtomicChange t : this.involvedAtomicElement){
			if(t instanceof DeleteProperty){
				System.out.println("		*** Involved delete property name = "+((DeleteProperty) t).getName());
				System.out.println("		*** Involved delete property class name = "+((DeleteProperty) t).getClassName());
			} else if(t instanceof AddProperty){
				System.out.println("		*** Involved add property name = "+((AddProperty) t).getName());
				System.out.println("		*** Involved add property class name = "+((AddProperty) t).getClassName());
			}
		}
		
		for(ComplexChange t : this.involvedComplexElement){
			if(t instanceof MoveProperty){
				System.out.println("		*** Involved Move property name = "+t.getName());
				System.out.println("		*** Involved Move property source name = "+((MoveProperty) t).getSourceClassName());
				System.out.println("		*** Involved Move property target name = "+((MoveProperty) t).getTargetClassName());
			}
		}
	}
	
	public ArrayList<AtomicChange>  getAddOccurences(){
		
		ArrayList<AtomicChange> addOccurences = new ArrayList<AtomicChange>();
		for(AtomicChange t : this.involvedAtomicElement){
			if(t instanceof AddProperty){
				addOccurences.add(t);
			}
		}
		return addOccurences;
	}
	
	public ArrayList<AtomicChange>  getDeleteOccurences(){

		ArrayList<AtomicChange> deleteOccurences = new ArrayList<AtomicChange>();
		for(AtomicChange t : this.involvedAtomicElement){
			if(t instanceof DeleteProperty){
				deleteOccurences.add(t);
			}
		}
		return deleteOccurences;
	}
	
	public ArrayList<ComplexChange>  getMoveOccurences(){

		ArrayList<ComplexChange> moveOccurences = new ArrayList<ComplexChange>();
		for(ComplexChange t : this.involvedComplexElement){
			if(t instanceof MoveProperty){
				moveOccurences.add(t);
			}
		}
		return moveOccurences;
	}
	
	public int  getFirstMoveAddPosition(){
		
		int pos = this.getLastMoveAddPosition();
		for(ComplexChange t : this.involvedComplexElement){
			if(t instanceof MoveProperty && ((MoveProperty) t).getAddPropertyPosition() < pos){
				pos = ((MoveProperty) t).getAddPropertyPosition();
			}
		}
		return pos;
	}
	
	public int  getLastMoveAddPosition(){
		
		int pos = 0;
		for(ComplexChange t : this.involvedComplexElement){
			if(t instanceof MoveProperty && pos < ((MoveProperty) t).getAddPropertyPosition()){
				pos = ((MoveProperty) t).getAddPropertyPosition();
			}
		}
		return pos;
	}
	
	public int  getFirstMoveDeletePosition(){
		
		int pos = this.getLastMoveDeletePosition();
		for(ComplexChange t : this.involvedComplexElement){
			if(t instanceof MoveProperty && ((MoveProperty) t).getDeletePropertyPosition() < pos){
				pos = ((MoveProperty) t).getDeletePropertyPosition();
			}
		}
		return pos;
	}
	
	public int  getLastMoveDeletePosition(){
		
		int pos = 0;
		for(ComplexChange t : this.involvedComplexElement){
			if(t instanceof MoveProperty && pos < ((MoveProperty) t).getDeletePropertyPosition()){
				pos = ((MoveProperty) t).getDeletePropertyPosition();
			}
		}
		return pos;
	}
	
	public ArrayList<ComplexChange>  getPullOccurences(){

		ArrayList<ComplexChange> pullOccurences = new ArrayList<ComplexChange>();
		for(ComplexChange t : this.involvedComplexElement){
			if(t instanceof PullProperty){
				pullOccurences.add(t);
			}
		}
		return pullOccurences;
	}
	
	public int  getFirstPullAddPosition(){
		
		int pos = this.getLastPullAddPosition();
		for(ComplexChange t : this.involvedComplexElement){
			if(t instanceof PullProperty && ((PullProperty) t).getAddPropertyPosition() < pos){
				pos = ((PullProperty) t).getAddPropertyPosition();
			}
		}
		return pos;
	}
	
	public int  getLastPullAddPosition(){
		
		int pos = 0;
		for(ComplexChange t : this.involvedComplexElement){
			if(t instanceof PullProperty && pos < ((PullProperty) t).getAddPropertyPosition()){
				pos = ((PullProperty) t).getAddPropertyPosition();
			}
		}
		return pos;
	}
	
	public ArrayList<ComplexChange>  getPushOccurences(){

		ArrayList<ComplexChange> pushOccurences = new ArrayList<ComplexChange>();
		for(ComplexChange t : this.involvedComplexElement){
			if(t instanceof PushProperty){
				pushOccurences.add(t);
			}
		}
		return pushOccurences;
	}

	public int  getFirstPushDeletePosition(){
		
		int pos = this.getLastPushDeletePosition();
		for(ComplexChange t : this.involvedComplexElement){
			if(t instanceof PushProperty && ((PushProperty) t).getDeletePropertyPosition() < pos){
				pos = ((PushProperty) t).getDeletePropertyPosition();
			}
		}
		return pos;
	}
	
	public int  getLastPushDeletePosition(){
		
		int pos = 0;
		for(ComplexChange t : this.involvedComplexElement){
			if(t instanceof PushProperty && pos < ((PushProperty) t).getDeletePropertyPosition()){
				pos = ((PushProperty) t).getDeletePropertyPosition();
			}
		}
		return pos;
	}
	
}
