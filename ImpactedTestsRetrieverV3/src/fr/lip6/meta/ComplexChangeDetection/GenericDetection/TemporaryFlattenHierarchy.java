package fr.lip6.meta.ComplexChangeDetection.GenericDetection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.FlattenHierarchy;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PushProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;

public class TemporaryFlattenHierarchy extends TemporaryComplexChange{
	
	public ArrayList<PushProperty> pushes = null;
	public String name = "";
	
	public TemporaryFlattenHierarchy(){
		this.pushes = new ArrayList<PushProperty>();
	}
	@Override
	public ArrayList<ComplexChange> validate(ArrayList<AtomicChange> trace,
			InitialState initState) {
		// TODO Auto-generated method stub
		ArrayList<ComplexChange> involvedElements = new  ArrayList<ComplexChange>();
		ArrayList<ComplexChange> results = new  ArrayList<ComplexChange>();
		
		
		if((this.existClassInInitialState(initState, this.name)//tempMem.getName()
				|| this.existAddClassInTrace(trace, this.name.split("_")[0]))
				&& this.existDeleteClassInTrace(trace, this.name)){
			ArrayList<PushProperty> clonedPushes = (ArrayList<PushProperty>) this.pushes.clone();

			for(PushProperty push : clonedPushes){
				//if(((PushProperty) push).getDeletePropertyPosition() == tempMem.getFirstPushDeletePosition()){//the first push in the atomic sequence and its borthers (pulls on th esame sub classes)  will be part of the flatten hierarchy
				if(!involvedElements.contains(push) && (clonedPushes.indexOf(push) <= clonedPushes.size() - 1)){//if the element has not been treated before
					involvedElements.add(push);
				
					ArrayList<String> propertiesNames = new ArrayList<String>();
					ArrayList<ComplexChange> involvedPushes = new ArrayList<ComplexChange>();
					ArrayList<String> subClassesNames = new ArrayList<String>();
					
					for(PushProperty subPush : clonedPushes.subList(clonedPushes.indexOf(push), clonedPushes.size())){
						if(!involvedElements.contains(subPush) && this.sameSubClasses((PushProperty)push, (PushProperty)subPush)){//if not the same push and has the same subclasses
							propertiesNames.add(subPush.getName());
							involvedPushes.add(subPush);
							//decoment below if you want h1 to be applied automatically
							//this.pushes.remove(subPush);//here we remove the pushes that are part of this flatten hierarchy, to remain coherent
							involvedElements.add(subPush);
						}
					}
					
					propertiesNames.add(push.getName());
					subClassesNames = ((PushProperty) push).getSubClassesNames();
					involvedPushes.add(push);
					//decoment below if you want h1 to be applied automatically
					//this.pushes.remove(push);//here we remove the pushes that are part of this flatten hierarchy, to remain coherent
				
					FlattenHierarchy flattenHierarchy = new FlattenHierarchy("n/a", this.name,
							//tempMem.getName(), 
							subClassesNames, propertiesNames, involvedPushes);
					flattenHierarchy.setDeleteClass(this.getDeleteClassInTrace(trace, this.name));
					//ExtractClass extractClass = new ExtractClass("n/a", tempMem.getName().split("_")[0], tempMem.getName().split("_")[1], propertiesNames, pulles);
					results.add(flattenHierarchy);
				}
				
			}
		}
		/**/for(PushProperty p : this.pushes){
			results.add(p);
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
		if(this.pushes.size() == 0) return true;
		else if(element instanceof PushProperty 
				&& this.name.equals(((PushProperty) element).getSuperClassName())) return true;
		else return false;
	}

	@Override
	public void add(ComplexChange element) {
		// TODO Auto-generated method stub
		this.name = ((PushProperty) element).getSuperClassName();
		this.pushes.add((PushProperty) element);
	}
	
	public DeleteClass getDeleteClassInTrace(ArrayList<AtomicChange> trace, String element){
		
		for(AtomicChange ac : trace){
			if(ac instanceof DeleteClass && ac.getName().equals(element)) return (DeleteClass)ac;
		}
		return null;
	}
	
	private boolean sameSubClasses(PushProperty push, PushProperty subPush) {
		//first, test if all elements of push are in subPush
		for(String pushElement : push.getSubClassesNames()){
			if(!subPush.getSubClassesNames().contains(pushElement)){
				return false;
			}
		}
		//second, test if all elements of subPush are in push		
		for(String pushSubElement : subPush.getSubClassesNames()){
			if(!push.getSubClassesNames().contains(pushSubElement)){
				return false;
			}
		}
		
		return true;//the following does not work all time, especially if it is not ordered = ((PullProperty) pull).getSubClassesNames().equals(((PullProperty) subPull).getSubClassesNames());
	}

}
