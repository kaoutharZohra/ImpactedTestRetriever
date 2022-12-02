package fr.lip6.meta.ComplexChangeDetection.Heuristics;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.InverseMoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PushProperty;
import fr.lip6.meta.ComplexChangeDetection.Overlap.DetectOverlap;

public class SolvingRate {

	private ArrayList<ComplexChange> overlaping = null;
	
	public SolvingRate() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public SolvingRate(ArrayList<ComplexChange> overlaping) {
		super();
		this.overlaping = overlaping;
	}
	
	public ArrayList<ComplexChange> getOverlaping() {
		return overlaping;
	}

	public void setOverlaping(ArrayList<ComplexChange> overlaping) {
		this.overlaping = overlaping;
	}
	
	public void calculatePriorities(){
		
		ArrayList<AtomicChange> atomicChanges = new ArrayList<AtomicChange>();
		//here we add the atomic changes of the complex changes
		for(ComplexChange c : this.overlaping){
			atomicChanges.addAll(c.getAtomicChanges());
		}
				
		
		for(int i = 0; i < this.overlaping.size(); i++){
			
			System.out.println("iteration n° = "+i);
			
			ArrayList<ComplexChange> tempOverlaping = new ArrayList<ComplexChange> (this.overlaping.subList(i+1, this.overlaping.size())); 
			if(i != 0){
				tempOverlaping.addAll(this.overlaping.subList(0, i));
			}
			
			//now we re-compute the set of overlapping cc when removing one cc, and we calculate the size, i.e. N° of overlapping cc, whether it decreased or not. 
			DetectOverlap dOverlap = new DetectOverlap(atomicChanges, tempOverlaping); 
			ArrayList< ArrayList<ComplexChange> > sets = dOverlap.costructOverlapSets();	
			
			for(ArrayList<ComplexChange> lc : sets){
				
				if(lc.size() > 1){
					double  priority = ((double)1) - ((double)lc.size() / (double)this.overlaping.size());
					//((double)(a.size() -1))/((double)(this.getMax(a) - this.getMin(a)));
					this.overlaping.get(i).setPriority(priority);
				} else {
					this.overlaping.get(i).setPriority(((double)1));
				}
			}
			
		}
		
	}
	
}
