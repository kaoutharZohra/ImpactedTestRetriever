package fr.lip6.meta.ComplexChangeDetection.Heuristics;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;

public class Distance {

	private ArrayList<ComplexChange> overlaping = null;
	
	public Distance() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Distance(ArrayList<ComplexChange> overlaping) {
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
		
		for(ComplexChange c : this.overlaping){
			ArrayList<AtomicChange> a = c.getAtomicChanges();
			
			double  priority = ((double)(a.size() -1))/((double)(this.getMax(a) - this.getMin(a)));
			
			c.setPriority(priority);
		}
	}
	
	public int getMin(ArrayList<AtomicChange> a){
		
		int min = 0;
		
		if(a.size() > 1) min = Integer.parseInt(a.get(0).getId());
		
		for(AtomicChange temp : a){
			if(Integer.parseInt(temp.getId()) < min) min = Integer.parseInt(temp.getId());
		}
		
		return min;
	}
	
	public int getMax(ArrayList<AtomicChange> a){
		
		int max = 0;
		
		for(AtomicChange temp : a){
			if(Integer.parseInt(temp.getId()) > max) max = Integer.parseInt(temp.getId());
		}
		
		return max;
	}
}
