package fr.lip6.meta.ComplexChangeDetection.Heuristics;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.*;

public class Containment {

	private ArrayList<ComplexChange> overlaping = null;
	
	public Containment() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Containment(ArrayList<ComplexChange> overlaping) {
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
			
			if(c instanceof MoveProperty){
				c.setPriority(1);
			} else if(c instanceof PushProperty){
				c.setPriority(1);
			} else if(c instanceof PullProperty){
				c.setPriority(1);
			} else if(c instanceof InverseMoveProperty){
				c.setPriority(1);
			} else {
				c.setPriority(2);
			}
		}
	}
}
