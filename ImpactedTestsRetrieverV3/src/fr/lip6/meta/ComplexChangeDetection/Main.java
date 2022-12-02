package fr.lip6.meta.ComplexChangeDetection;

import java.io.File;
import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractSuperClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.FlattenHierarchy;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.InlineClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.InverseMoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PushProperty;
import fr.lip6.meta.ComplexChangeDetection.Detection.DetectComplexChange;
import fr.lip6.meta.ComplexChangeDetection.EvolutionTrace.EvolutionTrace;
import fr.lip6.meta.ComplexChangeDetection.EvolutionTrace.ParseEvolutionTrace;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionComplexMove;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionExtract;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionExtractSuper;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionExtractV0;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionFlattenHierarchy;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionHiddenMoveV1;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionInline;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionInverseMove;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionMove;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionPull;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionPush;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.GenericDefinition;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.GenericDetection;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.TemporaryComplexMove;
import fr.lip6.meta.ComplexChangeDetection.PraxisTrace.ParsePraxisTrace;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/*ParsePraxisTrace parsePraxisTrace = new ParsePraxisTrace(new File("EvolutionTrace/praxisTrace_remove.txt"));
		parsePraxisTrace.parseTrace();
		
		EvolutionTrace evolutionTrace = new EvolutionTrace();
		
		ArrayList trace = new ArrayList();
		*/
		InitialState initState = new InitialState("Model/test.ecore");
		initState.initialize(true);//ecore
		//initState.dislpayClasses();
		
		File fileTrace = new File("EvolutionTrace/trace1.txt"); 
		ParseEvolutionTrace parse = new ParseEvolutionTrace(fileTrace);
		parse.parseTrace();
		parse.getEvolutionTrace().displayAtomicEvolutionTrace();
		parse.getEvolutionTrace().displayComplexEvolutionTrace();
		DetectComplexChange detection = new DetectComplexChange(initState);
		ArrayList<ComplexChange> detectedComplexChanges = new ArrayList<ComplexChange>();
		
		ArrayList<GenericDefinition> genericDef = new ArrayList<GenericDefinition>();
		genericDef.clear();
		long s = System.currentTimeMillis();
		long sm = Runtime.getRuntime().freeMemory();
		//genericDef.add(new DefinitionMove());
		genericDef.add(new DefinitionComplexMove());
		genericDef.add(new DefinitionPull());
		genericDef.add(new DefinitionPush());
		//genericDef.add(new DefinitionExtractV0());
		genericDef.add(new DefinitionExtract());
		genericDef.add(new DefinitionExtractSuper());
		genericDef.add(new DefinitionFlattenHierarchy());
		genericDef.add(new DefinitionInverseMove());
		genericDef.add(new DefinitionInline());/**/
		genericDef.add(new DefinitionHiddenMoveV1());
		//add inverse move (or use move in the oposite way) , and detect inline class
		
		GenericDetection generic = new GenericDetection(genericDef);
		generic.detect(parse.getEvolutionTrace().getAtomicEvolutionTrace());
		detectedComplexChanges = generic.validateDetectedCC(parse.getEvolutionTrace().getAtomicEvolutionTrace(), initState, new ArrayList<ComplexChange>());
		generic.detectMoreCC(detectedComplexChanges);
		detectedComplexChanges = generic.validateDetectedCC(parse.getEvolutionTrace().getAtomicEvolutionTrace(), initState, detectedComplexChanges);
		
		//detectedComplexChanges = detection.callMoveDetection(parse.getEvolutionTrace().getAtomicEvolutionTrace());//detectMoveProperty(parse.getEvolutionTrace().getAtomicEvolutionTrace());
		
		//detectedComplexChanges = detection.callPullDetection(parse.getEvolutionTrace().getAtomicEvolutionTrace());
		
		//detectedComplexChanges = detection.callPushDetection(parse.getEvolutionTrace().getAtomicEvolutionTrace());
		
		//ArrayList<ComplexChange> abstractDetectedComplexChanges = ...
		
		//detectedComplexChanges = detection.callExtractDetection(detectedComplexChanges, parse.getEvolutionTrace().getAtomicEvolutionTrace());
		
		//detectedComplexChanges = detection.callExtractSuperDetection(detectedComplexChanges, parse.getEvolutionTrace().getAtomicEvolutionTrace());
		
		//detectedComplexChanges = detection.callFlattenHierarchyDetection(detectedComplexChanges, parse.getEvolutionTrace().getAtomicEvolutionTrace());
		
		//detectedComplexChanges = detection.callInlineDetection(parse.getEvolutionTrace().getAtomicEvolutionTrace());//detectedComplexChanges,
		
		long e = System.currentTimeMillis();
		long em = Runtime.getRuntime().freeMemory();
		System.out.println("time = "+(e-s));
		System.out.println("memory = "+(sm-em)/1000000d);
		
		System.out.println("size of detected complex changes = "+detectedComplexChanges.size());
		parse.getEvolutionTrace().displayComplexEvolutionTrace();
		
		for(ComplexChange complexchange : detectedComplexChanges){
			if(complexchange instanceof MoveProperty){
				System.out.println("delete pos = "+((MoveProperty) complexchange).getDeletePropertyPosition());
				System.out.println("add pos = "+((MoveProperty) complexchange).getAddPropertyPosition());
				System.out.println(""+((MoveProperty) complexchange).getName());
				System.out.println(""+((MoveProperty) complexchange).getSourceClassName());
				System.out.println(""+((MoveProperty) complexchange).getTargetClassName());
				System.out.println(""+((MoveProperty) complexchange).getKind());
			} else if(complexchange instanceof PullProperty){
				System.out.println(""+((PullProperty) complexchange).getName());
				System.out.println(""+((PullProperty) complexchange).getSuperClassName());
				System.out.println(""+((PullProperty) complexchange).getSubClassesNames().toString());
				System.out.println(""+((PullProperty) complexchange).getKind());
			} else if(complexchange instanceof PushProperty){
				System.out.println(""+((PushProperty) complexchange).getName());
				System.out.println(""+((PushProperty) complexchange).getSuperClassName());
				System.out.println(""+((PushProperty) complexchange).getSubClassesNames().toString());
				System.out.println(""+((PushProperty) complexchange).getKind());
			} else if(complexchange instanceof ExtractClass){
				System.out.println(""+((ExtractClass) complexchange).getName() );
				System.out.println(""+((ExtractClass) complexchange).getSourceClassName() );
				System.out.println(""+((ExtractClass) complexchange).getTargetClassName() );
				System.out.println(""+((ExtractClass) complexchange).getPropertiesNames().toString() );
				System.out.println(""+((ExtractClass) complexchange).getMoves());
				System.out.println(""+((ExtractClass) complexchange).getKind() );
			} else if(complexchange instanceof ExtractSuperClass){
				System.out.println(""+((ExtractSuperClass) complexchange).getName());
				System.out.println(""+((ExtractSuperClass) complexchange).getSuperClassName());		
				System.out.println(""+((ExtractSuperClass) complexchange).getSubClassesNames().toString());
				System.out.println(""+((ExtractSuperClass) complexchange).getPropertiesNames().toString());
				System.out.println(""+((ExtractSuperClass) complexchange).getPulles());
				System.out.println(""+((ExtractSuperClass) complexchange).getKind());
				//System.out.println(""+((ExtractSuperClass) complexchange));
			} else if(complexchange instanceof FlattenHierarchy){
				System.out.println(""+((FlattenHierarchy) complexchange).getName());
				System.out.println(""+((FlattenHierarchy) complexchange).getSuperClassName());		
				System.out.println(""+((FlattenHierarchy) complexchange).getSubClassesNames().toString());
				System.out.println(""+((FlattenHierarchy) complexchange).getPropertiesNames().toString());
				System.out.println(""+((FlattenHierarchy) complexchange).getPushes());
				System.out.println(""+((FlattenHierarchy) complexchange).getKind());
			} else if(complexchange instanceof InlineClass){
				System.out.println(""+((InlineClass) complexchange).getName() );
				System.out.println(""+((InlineClass) complexchange).getSourceClassName() );
				System.out.println(""+((InlineClass) complexchange).getTargetClassName() );
				System.out.println(""+((InlineClass) complexchange).getPropertiesNames().toString() );
				System.out.println(""+((InlineClass) complexchange).getInverseMoves());
				System.out.println(""+((InlineClass) complexchange).getKind() );
			} else if(complexchange instanceof InverseMoveProperty){
				System.out.println("delete pos = "+((InverseMoveProperty) complexchange).getDeletePropertyPosition());
				System.out.println("add pos = "+((InverseMoveProperty) complexchange).getAddPropertyPosition());
				System.out.println(""+((InverseMoveProperty) complexchange).getName());
				System.out.println(""+((InverseMoveProperty) complexchange).getSourceClassName());
				System.out.println(""+((InverseMoveProperty) complexchange).getTargetClassName());
				System.out.println(""+((InverseMoveProperty) complexchange).getKind());
			}
			
		}
		
		/*
		System.out.println("size of detected abstract complex changes = "+abstractDetectedComplexChanges.size());
		for(ComplexChange complexchange : abstractDetectedComplexChanges){
			if(complexchange instanceof ExtractClass){
				System.out.println(""+((ExtractClass) complexchange).getName() );
				System.out.println(""+((ExtractClass) complexchange).getSourceClassName() );
				System.out.println(""+((ExtractClass) complexchange).getTargetClassName() );
				System.out.println(""+((ExtractClass) complexchange).getPropertiesNames().toString() );
				System.out.println(""+((ExtractClass) complexchange).getKind() );
			}
		}*/
	
		
	}

}
