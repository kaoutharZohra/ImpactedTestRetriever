package fr.lip6.meta.ComplexChangeDetection.UndoChanges;

import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PushProperty;

public class UndoPull extends UndoChange{

	public PullProperty pull = null;
	public PushProperty push = null;
	
	
	public UndoPull(String name, PullProperty pull, PushProperty push) {
		super(name);
		this.pull = pull;
		this.push = push;
	}


	public PullProperty getPull() {
		return pull;
	}


	public void setPull(PullProperty pull) {
		this.pull = pull;
	}


	public PushProperty getPush() {
		return push;
	}


	public void setPush(PushProperty push) {
		this.push = push;
	}
	
	
	
}
