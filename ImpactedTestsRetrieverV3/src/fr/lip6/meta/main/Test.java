package fr.lip6.meta.main;

import java.util.ArrayList;

import org.eclipse.emf.compare.Comparison;

import fr.lip6.meta.ComplexChangeDetection.Change;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.ChangeUpperBound;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.RenameClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.RenameProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.InverseMoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PushProperty;
import fr.lip6.meta.ComplexChangeDetection.EMFCompare.ComputeDelta;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import testppup3.controlers.ImpactAnalyzer;

public class Test {

	public static void main(String[] args) {
		

		
		
		  String [] tab =
		  {"/home/zkebaili/eclipse-workspace/ImpactedTestsRetrieverV3/Model/PivotV6.7.0.ecore",
		  "/home/zkebaili/eclipse-workspace/ImpactedTestsRetrieverV3/Model/PivotV6.18.0.ecore"
		  }; // //
		//  "C:/Users/dkhellad/Documents/eclipses/eclipse-epsilon-1.4-win32-x86_64/workspace2/MCCoEvolutator/models/ocl/3.2.2/Pivot.ecore",
		  //
		//  "C:/Users/dkhellad/Documents/eclipses/eclipse-epsilon-1.4-win32-x86_64/workspace2/MCCoEvolutator/models/ocl/3.4.4/Pivot.ecore"
		  // );
		  
		  String source = tab[0]; String target = tab[1];
		  
		  InitialState initState = new InitialState(source); //initialize the source //
		//  metamodel that we use for precise cc detection
		  initState.initialize(true);//ecore
		  
		  InitialState initStateTarget = new InitialState(target); //initialize the //
		 // target metamodel that we use for precise cc detection
		  initStateTarget.initialize(true);//ecore
		  
		  ComputeDelta cDelta = new ComputeDelta(); Comparison comparison =
		  cDelta.compare(source, target);
		  
		  
		  ArrayList<AtomicChange> achanges =
		  cDelta.importeEMFCompareChanges(comparison, initState, initStateTarget);
		  System.out.println("transfered diff delta size: " + achanges.size());
		  ArrayList<Change> changes= new ArrayList<Change>(); changes.addAll(achanges);
		  System.out.println(" The changes size is " + changes.size());
		  
		 
	
		//System.out.println("example to literal  "+	ImpactAnalyzer.makeLiteral("example"));
		//System.out.println("ClassRename to literal  "+	ImpactAnalyzer.makeLiteral("ClassRename"));
		//System.out.println("Class_Rename to literal  "+	ImpactAnalyzer.makeLiteral("Class_Rename"));
	
//		System.out.println("CLASS  "+ ImpactAnalyzer.containOnlyCapitals("NAME"));
//		System.out.println("Classename  "+ ImpactAnalyzer.containOnlyCapitals("Classename"));
//		
//		System.out.println("CLASS_NAME  "+ ImpactAnalyzer.containOnlyCapitals("CLASS_NAME"));
	
		//System.out.println(" The binding info are "+ ImpactAnalyzer.formatBinding(null)
//	String key= "Lorg/eclipse/ocl/pivot/IteratorExp;.validateSafeIteratorIsRequired(Lorg/eclipse/emf/common/util/DiagnosticChain;Ljava/util/Map<Ljava/lang/Object;Ljava/lang/Object;>;)Z";
	//	String key= "Lorg/eclipse/ocl/pivot/PivotPackage$Literals;.ANNOTATION)Lorg/eclipse/emf/ecore/EClass;";
//	String key="Lorg/eclipse/ocl/pivot/PivotPackage$Literals;.ANNOTATION__OWNED_CONTENTS)Lorg/eclipse/emf/ecore/EReference;";
//		String classname = "";
//		String methodname="";
//		String[] methodnameinit =key.split("\\.");
//
//		if(key.contains("<"))
//		{
//			classname = ((key.split("/"))[key.split("/").length-1]).split("<")[0];
//
//		}
//		else {
//			//classname = ((key.split("/"))[key.split("/").length-1]).split(";")[0];
//			String parts[] = (key.split(";")[0]).split("/");
//			classname=parts[parts.length-1];
//		}
//		if(methodnameinit.length>1) {
//
//			methodname=methodnameinit[1].split("\\(")[0];
//			//System.out.println(" in method name "+ methodname);
//		}
//		//else  System.out.println("Format Binding exception");
//		String variablename="";
//		if(key.contains("#")){
//			variablename=key.split("#")[1];
//		}
//		else {
//
//			variablename= key.split("\\.")[1].split("\\)")[0];
//		}
//
//
//		//String parts[] = (key.split(";")[0]).split("/");
		  
		 String key="Lorg/eclipse/modisco/infra/discovery/benchmark/impl/DiscoveryIterationImpl;.getDiscoveryErrors()Ljava/lang/String;";
		 String classname = "";
			String methodname="";
			String[] methodnameinit =key.split("\\.");
	
	if(key.contains("<"))
		{
			classname = ((key.split("/"))[key.split("/").length-1]).split("<")[0];
	
		}	
	else {
			classname = ((key.split("/"))[key.split("/").length-1]).split(";")[0];			String parts[] = (key.split(";")[0]).split("/");
				classname=parts[parts.length-1];
			}
			if(methodnameinit.length>1) {
	
				methodname=methodnameinit[1].split("\\(")[0];
				//System.out.println(" in method name "+ methodname);
		}
			//else  System.out.println("Format Binding exception");
			String variablename="";
		if(key.contains("#")){
			variablename=key.split("#")[1];
		}
		else {
	
				variablename= key.split("\\.")[1].split("\\)")[0];
			}
	
	
		//String parts[] = (key.split(";")[0]).split("/");
			  
	System.out.println("classe name is " +classname+ "  methodname "+methodname+ "   variablename "+variablename);
	}

}
