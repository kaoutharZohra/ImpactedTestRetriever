package fr.lip6.meta.ComplexChangeDetection.Overlap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;

public class DetectOverlap {

	private ArrayList<AtomicChange> atomicChanges = null;
	private ArrayList<ComplexChange> complexChanges = null;
	
	public DetectOverlap() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public DetectOverlap(ArrayList<AtomicChange> atomicChanges, ArrayList<ComplexChange> complexChanges) {
		super();
		// TODO Auto-generated constructor stub
		this.atomicChanges = atomicChanges;
		this.complexChanges = complexChanges;
	}

	public HashMap<AtomicChange, ArrayList<ComplexChange>> constructOverlapMap(){
		//here first we get the atomic changes that are present in the complex changes
		ArrayList<AtomicChange> selectedAtomicChange = new ArrayList<AtomicChange>();
		
		for(ComplexChange c : complexChanges){
			for(AtomicChange a : c.getAtomicChanges()){
				selectedAtomicChange.add(a);
			}
		}
		
		//here we create a hash map to stock for each atomic change the complex changes that use it
		HashMap<AtomicChange, ArrayList<ComplexChange>> hashmap = new HashMap<AtomicChange, ArrayList<ComplexChange>>();
		for(AtomicChange a : selectedAtomicChange){
			hashmap.put(a, new ArrayList<ComplexChange>());
			for(ComplexChange c : complexChanges){
				if(c.doesContainAtomicChange(a)){
					System.out.println("found one overlap"+c);
					hashmap.get(a).add(c);
				}
			}
		}
		//here just print the content of the map
		for(AtomicChange a : hashmap.keySet()){
			System.out.println("hashmap = "+a+", \n		"+hashmap.get(a).toString());
			
		}
		
		return hashmap;
	}
	
	public ArrayList< ArrayList<ComplexChange> > costructOverlapSets(){// here we construct the sets of overlapping CC
		
		HashMap<AtomicChange, ArrayList<ComplexChange>> hashmap = this.constructOverlapMap();
		ArrayList< ArrayList<ComplexChange> > sets = new ArrayList<ArrayList<ComplexChange>>();
		
		int i = 0;
		for(AtomicChange a : hashmap.keySet()){
			
			if(sets.size() == 0){//here first case, we put all complex changes of the first atomic change in the first set, obviously
				ArrayList<ComplexChange> newSet = new ArrayList<ComplexChange>();
				
				for(ComplexChange c : hashmap.get(a)){
					newSet.add(c);
				}
				
				sets.add(newSet);
				
			} else {
				
				boolean overallFound = false;
				for(ArrayList<ComplexChange> s: sets){
					//now for each of the existing sets, we see if one of the complex changes of the current atomic change belongs to the set, 
					//then add the rest if not already added.
					boolean found = false;
					/*for(ComplexChange c : hashmap.get(a)){
						//newSet.add(c);
						if(s.contains(c)){//now add the rest of CC is not already in
							found = true;
						}
					}*/
					Iterator<ComplexChange> it = hashmap.get(a).iterator();
					while(it.hasNext() && !found){
						if(s.contains(it.next())){//now add the rest of CC is not already in
							found = true;
							overallFound = true;
						}
					}
					
					if(found){
						for(ComplexChange c : hashmap.get(a)){
							if(!s.contains(c)) s.add(c);
						}
						
					}
				}	
				
				if(!overallFound){
					ArrayList<ComplexChange> newSet = new ArrayList<ComplexChange>();
					
					for(ComplexChange c : hashmap.get(a)){
						newSet.add(c);
					}
					
					sets.add(newSet);
				}

			}
			for(ArrayList<ComplexChange> s: sets){
				System.out.println("sets "+i+" iteration = "+s.toString());
			}
			i++;
			
		}
		
		
				
				
		
		//print the sets
		System.out.println("\n Found Overlapping set : Before\n");
		for(ArrayList<ComplexChange> s : sets){
			System.out.println("overlapping CC = "+s.toString());
		}
		
		//sets.get(1).add(sets.get(0).get(1));
		//we check if we can merge sets ;)
		ArrayList< ArrayList<ComplexChange> > finalSets = this.finalizeSets(sets);
		
		//print the sets
		System.out.println("Found Overlapping set : After\n");
		for(ArrayList<ComplexChange> s : finalSets){
			System.out.println("overlapping CC = "+s.toString());
		}
		
		ArrayList<ArrayList<ComplexChange>> identical = new ArrayList<ArrayList<ComplexChange>>();
		for(ArrayList<ComplexChange> s : finalSets){
			for(ArrayList<ComplexChange> sub : finalSets.subList(finalSets.indexOf(s)+1, finalSets.size())){
				//System.out.println(s.equals(sub) +"\n s = " + s +"\n sub = "+ sub);
				//here we check if two sets are equal, s and sub, if so one will be removed afetrwards
				boolean found1 = true;
				for(ComplexChange c : s){
					if(!sub.contains(c)) found1 = false;
				}
				boolean found2 = true;
				for(ComplexChange c : sub){
					if(!s.contains(c)) found2 = false;
				}
				
				if(found1 && found2){
					identical.add(sub);
					//identical.add(s); // se remove only one,i.e. sub	
				}
				
			}
		}
		
		for(ArrayList<ComplexChange> s : identical){
			finalSets.remove(s);
		}
		
		return finalSets;
		
	}
	
	//TODO Think again on the finalizeSets and finalizeSetsV2, normally the first is almost correct, but you need to use a marking system to mark the sets that have been added before !!
	private ArrayList< ArrayList<ComplexChange> > finalizeSets(ArrayList< ArrayList<ComplexChange> > sets){
		
		//TODO do not forget to check if we can merge sets ;)
		ArrayList< ArrayList<ComplexChange> > finalSets = new ArrayList<ArrayList<ComplexChange>>();
		
		for(ArrayList<ComplexChange> s : sets){
			
			ArrayList<ComplexChange> tempSet = new ArrayList<ComplexChange>();
			tempSet = (ArrayList<ComplexChange>) s.clone();
			
			for(ArrayList<ComplexChange> subs : sets.subList(sets.indexOf(s)+1, sets.size())){
				
				boolean found = false;
				//we check if the two sets do have a CC in common
				Iterator<ComplexChange> it1 = s.iterator();
				//Iterator<ComplexChange> it2 = subs.iterator();
				while(it1.hasNext() && !found){
					ComplexChange c1 = it1.next();
					if(subs.contains(c1)){
						found = true;
					}
					
				}
				
				if(found){//here we merge the two sets that have CC in comon
					for(ComplexChange c : subs){
						if(!tempSet.contains(c)) tempSet.add(c);
					}
				}
			}
			finalSets.add(tempSet);
		}
		return finalSets;		
	}

	/*
	private ArrayList< ArrayList<ComplexChange> > finalizeSetsV2(ArrayList< ArrayList<ComplexChange> > sets){
		
		//TODO do not forget to check if we can merge sets ;)
		ArrayList< ArrayList<ComplexChange> > finalSets = new ArrayList<ArrayList<ComplexChange>>();
		
		int k = 0;//to save the index of the first set that does not 
		
		for(int i = 0; i < sets.size(); i++){
			
			ArrayList<ComplexChange> tempSet = new ArrayList<ComplexChange>();
			tempSet = (ArrayList<ComplexChange>) sets.get(i);//clone??
			
			for(int j = i+1; j < sets.size(); j++){//ArrayList<ComplexChange> subs : sets.subList(sets.indexOf(s)+1, sets.size())){
				
				boolean found = false;
				//we check if the two sets do have a CC in common
				Iterator<ComplexChange> it1 = tempSet.iterator();
				//Iterator<ComplexChange> it2 = subs.iterator();
				while(it1.hasNext() && !found){
					ComplexChange c1 = it1.next();
					if(sets.get(j).contains(c1)){
						found = true;
					}
					
				}
				
				if(found){//here we merge the two sets that have CC in comon
					for(ComplexChange c : sets.get(j)){
						if(!tempSet.contains(c)) tempSet.add(c);
					}
				}
			}
			finalSets.add(tempSet);
		}
		return finalSets;		
	}

	*/
}
