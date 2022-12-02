package fr.lip6.meta.ComplexChangeDetection.Detection;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.SetProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.ComplexChangeDetection.Utils.References;

public class DetectComplexChange {

	public InitialState initState = null;
	
	public DetectComplexChange(InitialState initState) {
		super();
		this.initState = initState;
	}
	
	public ArrayList<ComplexChange> callMoveDetection(ArrayList<AtomicChange> atomicEvolutionTrace){
		MoveDetection moveDetection = new MoveDetection(this.initState);
		return moveDetection.detectMoveProperty(atomicEvolutionTrace);
	}

	public ArrayList<ComplexChange> callPullDetection(ArrayList<AtomicChange> atomicEvolutionTrace){
		PullDetection pullDetection = new PullDetection(this.initState);
		return pullDetection.detectPullProperty(atomicEvolutionTrace);
	}
	
	public ArrayList<ComplexChange> callPushDetection(ArrayList<AtomicChange> atomicEvolutionTrace){
		PushDetection pushDetection = new PushDetection(this.initState);
		return pushDetection.detectPushProperty(atomicEvolutionTrace);
	}
	
	public ArrayList<ComplexChange> callExtractDetection(ArrayList<ComplexChange> preComplexEvolutionTrace, ArrayList<AtomicChange> atomicEvolutionTrace){
		ExtractDetection extractDetection = new ExtractDetection(this.initState);
		return extractDetection.detectExtractClass(preComplexEvolutionTrace, atomicEvolutionTrace);
	}
	
	public ArrayList<ComplexChange> callExtractSuperDetection(ArrayList<ComplexChange> preComplexEvolutionTrace, ArrayList<AtomicChange> atomicEvolutionTrace){
		ExtractSuperDetection extractSuperDetection = new ExtractSuperDetection(this.initState);
		return extractSuperDetection.detectExtractSuperClass(preComplexEvolutionTrace, atomicEvolutionTrace);
	}
	
	public ArrayList<ComplexChange> callFlattenHierarchyDetection(ArrayList<ComplexChange> preComplexEvolutionTrace, ArrayList<AtomicChange> atomicEvolutionTrace){
		FlattenHierarchyDetection flattenHierarchyDetection = new FlattenHierarchyDetection(this.initState);
		return flattenHierarchyDetection.detectFlattenHierarchy(preComplexEvolutionTrace, atomicEvolutionTrace);
	}
	
	public ArrayList<ComplexChange> callInlineDetection(ArrayList<AtomicChange> atomicEvolutionTrace){//ArrayList<ComplexChange> preComplexEvolutionTrace,
		InlineDetection inlineDetection = new InlineDetection(this.initState);
		return inlineDetection.detectInlineClass(atomicEvolutionTrace);//
	}
}
