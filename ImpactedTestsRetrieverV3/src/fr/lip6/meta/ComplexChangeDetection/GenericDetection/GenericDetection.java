package fr.lip6.meta.ComplexChangeDetection.GenericDetection;

import java.util.ArrayList;
import java.util.Hashtable;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;

public class GenericDetection {

	public ArrayList<GenericDefinition> genericDefinitions = null;
	public ArrayList<TemporaryComplexChange> temporaryComplexChange = null;
	public Hashtable<String, Boolean> tracking = null; 
	public ArrayList<ComplexChange> firstPhase = null;
	public ArrayList<ComplexChange> finalPhase = null;
	
	public GenericDetection(ArrayList<GenericDefinition> genericDefinitions){
		this.genericDefinitions = genericDefinitions;
		this.temporaryComplexChange = new ArrayList<TemporaryComplexChange>();
		this.tracking = new Hashtable<String, Boolean>();
	}
	
	public void detect(ArrayList<AtomicChange> trace){
		
		for(AtomicChange element : trace){
			
			this.tracking.clear();
			System.out.println("size of tmpCC = "+this.temporaryComplexChange.size());
			for(TemporaryComplexChange tempCC : this.temporaryComplexChange){
				System.out.println("	first loop");
				if(tempCC.isPossibleToAdd(element)){
					tempCC.add(element);
					//System.out.println(tempCC.getClass().getSimpleName().split("y")[1]);
					//here we extract the XXX from "TemporaryXXX"
					this.tracking.put(tempCC.getClass().getSimpleName().split("ry")[1]+element.getName(), true);
				} else {
					//System.out.println(tempCC.getClass().getSimpleName().split("y")[1]);
					//this.tracking.put(tempCC.getClass().getSimpleName().split("y")[1]+element.getName(), false);
					//System.out.println(this.tracking.get("Move").equals(false));
				}
			}
			
			for(GenericDefinition def : this.genericDefinitions){
				
				//&& (this.tracking.size() == 0 || ((this.tracking.get("Move") != null && this.tracking.get("Move").equals(false))))
				System.out.println("1"+(this.tracking.size() == 0));
				System.out.println("2"+(this.tracking.get("Move") != null && this.tracking.get("Move").equals(true)));
				System.out.println("3"+(this.tracking.size() == 0 || ((this.tracking.get("Move") != null && this.tracking.get("Move").equals(true)))));
				System.out.println("4"+def.doesContain(element));
				System.out.println("5"+element.getClass());
				System.out.println("6"+def.getClass());
				 /*previous cond in the following if => it was wrong! because :
				 * 	//ex: if we have pull (a) and pull (b), then in the previous loop we meet add/delete proporty a,
					//we will indeed add it but after that we test if we can add it to pull (a) ,
					//and since we cannot, there is a false that is put to the hashmap this.tracking, 
					//Thus, we will create another pull (b), so here test if what we are going to create is not already created!!
				 * && (this.tracking.size() == 0 
							|| ((this.tracking.get(def.getClass().getSimpleName().split("on")[1]+element.getName()) != null 
								&& this.tracking.get(def.getClass().getSimpleName().split("on")[1]+element.getName()).equals(false)))	
								)
				 * */
				System.out.println("	second loop");
				if(def.doesContain(element) 
						&& (this.tracking.size() == 0 
								//here we extract the XXX from "DefinitionXXX"
							|| (!this.tracking.containsKey(def.getClass().getSimpleName().split("on")[1]+element.getName()))
								//&& this.tracking.get(def.getClass().getSimpleName().split("on")[1]+element.getName()).equals(false)))	
								)
						){
					
					//TemporaryMove tm = new TemporaryMove();
					TemporaryComplexChange tm = def.create();
					tm.add(element);
					this.temporaryComplexChange.add(tm);
					//&& not found in hash map
					//create temp def
					System.out.println("zehahah");
				} /*else if(def.doesContains(element) 
						&& (this.tracking.size() == 0 
						|| ((this.tracking.get("ComplexMove") != null && this.tracking.get("ComplexMove").equals(false)))	)
					){
					TemporaryComplexMove tm = new TemporaryComplexMove();
					tm.add(element);
					this.temporaryComplexChange.add(tm);
					//&& not found in hash map
					//create temp def
					System.out.println("zehahah2");
				}*/
			}
		}
		System.out.println("tmp cc = "+this.temporaryComplexChange.size());
	}
	
	public ArrayList<ComplexChange> validateDetectedCC(ArrayList<AtomicChange> trace, InitialState initState, ArrayList<ComplexChange> detectedComplexChanges){
		ArrayList<ComplexChange> cc = new ArrayList<ComplexChange>();
		for(TemporaryComplexChange tempCC : this.temporaryComplexChange){
			ArrayList<ComplexChange> tmp = new ArrayList<ComplexChange>();
			tmp = tempCC.validate(trace, initState);
			System.out.println("	size "+tmp.size());
			for(ComplexChange c : tmp){
				cc.add(c);
			}
		}
		if(this.firstPhase != null){
			for(ComplexChange detected : this.firstPhase){
				cc.add(detected);
			}
		}
		System.out.println("	size "+cc.size());
		//here to keep the previous CC when detecting for the second time
		/*for(ComplexChange detected : detectedComplexChanges){
			cc.add(detected);
		}*/
		
		//we put the final complex changes to the attribut finalPhase
		this.finalPhase = cc;
		return cc;
	}
	
	public void detectMoreCC(ArrayList<ComplexChange> trace){
		
		this.temporaryComplexChange.clear();
		ArrayList<ComplexChange> clonedTrace = (ArrayList<ComplexChange>) trace.clone();
		for(ComplexChange element : clonedTrace){
			
			this.tracking.clear();
			System.out.println("size of tmpCC = "+this.temporaryComplexChange.size());
			for(TemporaryComplexChange tempCC : this.temporaryComplexChange){
				System.out.println("	first loop");
				if(tempCC.isPossibleToAdd(element)){
					tempCC.add(element);
					trace.remove(element);
					//System.out.println(tempCC.getClass().getSimpleName().split("y")[1]);
					this.tracking.put(tempCC.getClass().getSimpleName().split("Temporary")[1]+element.getName(), true);
				}
			}
			
			for(GenericDefinition def : this.genericDefinitions){
				
				
				System.out.println("	second loop");
				if(def.doesContain(element) 
						&& (this.tracking.size() == 0 
							|| (!this.tracking.containsKey(def.getClass().getSimpleName().split("Definition")[1]+element.getName()))
								//&& this.tracking.get(def.getClass().getSimpleName().split("on")[1]+element.getName()).equals(false)))	
								)
						){
					
					//TemporaryMove tm = new TemporaryMove();
					TemporaryComplexChange tm = def.create();
					tm.add(element);
					trace.remove(element);
					this.temporaryComplexChange.add(tm);
					//&& not found in hash map
					//create temp def
					System.out.println("More zehahah");
				} 
			}
		}
		this.firstPhase = trace;
		System.out.println("tmp cc = "+this.temporaryComplexChange.size());
	}
}
