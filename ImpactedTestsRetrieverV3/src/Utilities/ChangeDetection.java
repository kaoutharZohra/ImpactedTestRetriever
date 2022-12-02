package Utilities;

import java.util.ArrayList;

import fr.lip6.meta.ComplexChangeDetection.Change;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddSuperType;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.ChangeLowerBound;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.ChangeUpperBound;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteSuperType;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.RenameClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.RenameProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.SetProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractSuperClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PushProperty;

public class ChangeDetection {
	public static ArrayList<Change> initializeChangements()
	{
		ArrayList<Change> changes = new ArrayList<Change>();
		
		RenameProperty rp1 = new RenameProperty("discoveryError", "discoveryErrors", "Discovery");		
		RenameProperty rp2 = new RenameProperty("dicoveryDate", "discoveryDate", "Discovery");
		RenameProperty rp3 = new RenameProperty("totalExecutionTimeInSeconds", "discoveryTimeInSeconds", "Discovery");
		RenameProperty rp4 = new RenameProperty("averageSaveTimeInSeconds", "saveTimeAverageInSeconds", "AveragedProjectDiscovery");
		RenameProperty rp5 = new RenameProperty("averageExecutionTimeInSeconds", "discoveryTimeAverageInSeconds", "AveragedProjectDiscovery");
		
		changes.add(rp1);
		changes.add(rp2);
		changes.add(rp3);
		changes.add(rp4);
		changes.add(rp5);
		
		ArrayList<ComplexChange> moves = new ArrayList<ComplexChange>();
		
		MoveProperty mp1 = new MoveProperty("discoveryDate", "Discovery", "DiscoveryIteration");//after its rename
		MoveProperty mp2 = new MoveProperty("maxUsedMemoryInBytes", "Discovery", "DiscoveryIteration");
		MoveProperty mp3 = new MoveProperty("saveTimeInSeconds", "Discovery", "DiscoveryIteration");
		MoveProperty mp4 = new MoveProperty("discoveryErrors", "Discovery", "DiscoveryIteration");//after its rename
		                                  
		MoveProperty mp5 = new MoveProperty("discoveryTimeInSeconds", "Discovery", "DiscoveryIteration");//after its rename
		moves.add(mp1);
		moves.add(mp2);
		moves.add(mp3);
		moves.add(mp4);
		moves.add(mp5);
		
		ExtractClass ec = new ExtractClass("EC", "Discovery", "DiscoveryIteration", null, moves);
		
		changes.add(ec);
		
		MoveProperty mp6 = new MoveProperty("saveTimeStandardDeviation", "AveragedProjectDiscovery", "ProjectDiscovery");
		MoveProperty mp7 = new MoveProperty("executionTimeStandardDeviation", "AveragedProjectDiscovery", "ProjectDiscovery");
		MoveProperty mp8 = new MoveProperty("saveTimeAverageInSeconds", "AveragedProjectDiscovery", "ProjectDiscovery");//after its rename
		MoveProperty mp9 = new MoveProperty("discoveryTimeAverageInSeconds", "AveragedProjectDiscovery", "ProjectDiscovery");//after its rename
		
		changes.add(mp6);
		changes.add(mp7);
		changes.add(mp8);
		changes.add(mp9);
		
		ArrayList<String> subclasses = new ArrayList<String>();
		subclasses.add("ProjectDiscovery");
		
		PullProperty pp1 = new PullProperty("saveTimeStandardDeviation", "Discovery", subclasses);
		PullProperty pp2 = new PullProperty("executionTimeStandardDeviation", "Discovery", subclasses);
		PullProperty pp3 = new PullProperty("saveTimeAverageInSeconds", "Discovery", subclasses);
		PullProperty pp4 = new PullProperty("discoveryTimeAverageInSeconds", "Discovery", subclasses);
		
		changes.add(pp1);
		changes.add(pp2);
		changes.add(pp3);
		changes.add(pp4);
		
		ArrayList<ComplexChange> pulls = new ArrayList<ComplexChange>();
		
		ArrayList<String> subclasses1 = new ArrayList<String>();
		subclasses1.add("Porject");
		PullProperty pp5 = new PullProperty("name", "Resource", subclasses1);
		PullProperty pp6 = new PullProperty("totalSizeInBytes", "Resource", subclasses1);
		pulls.add(pp5);
		pulls.add(pp6);
		
		ExtractSuperClass esc = new ExtractSuperClass("", "Resource", subclasses1, null, pulls);
		changes.add(esc);
		
		ArrayList<String> subclasses2 = new ArrayList<String>();
		subclasses2.add("MultiProjectBenchmark");
		subclasses2.add("ProjectDiscovery");
		
		ArrayList<String> subclasses3 = new ArrayList<String>();
		subclasses3.add("MultiDiscoveryBenchmark");
		subclasses3.add("AveragedMultiDiscoveryBenchmark");
		subclasses3.add("DiscoveredProject");
		
		PullProperty pp7 = new PullProperty("projects", "Benchmark", subclasses2);
		PullProperty pp8 = new PullProperty("discoveries", "Benchmark", subclasses3);
		changes.add(pp7);
		changes.add(pp8);
		
		DeleteProperty dp1 = new DeleteProperty("occurrences", "AveragedProjectDiscovery"); 
		DeleteProperty dp2 = new DeleteProperty("metaModelVariant", "Discovery");
		DeleteProperty dp3 = new DeleteProperty("algorithmVariant", "Discovery");
		
		changes.add(dp1);
		changes.add(dp2);
		changes.add(dp3);
		
		DeleteClass dc1 = new DeleteClass("AveragedProjectDiscovery");
		DeleteClass dc2 = new DeleteClass("AveragedMultiDiscoveryBenchmark");
		DeleteClass dc3 = new DeleteClass("DiscoveredProject");
		DeleteClass dc4 = new DeleteClass("ProjectDiscovery");
		DeleteClass dc5 = new DeleteClass("MultiDiscoveryBenchmark");
		DeleteClass dc6 = new DeleteClass("MultiProjectBenchmark");
		
		changes.add(dc1);
		changes.add(dc2);
		changes.add(dc3);
		changes.add(dc4);
		changes.add(dc5);
		changes.add(dc6);
		
		DeleteSuperType dst1 = new DeleteSuperType("MultiProjectBenchmark", "Benchmark");
		DeleteSuperType dst2 = new DeleteSuperType("MultiDiscoveryBenchmark", "Benchmark");
		DeleteSuperType dst3 = new DeleteSuperType("ProjectDiscovery", "Discovery");
		DeleteSuperType dst4 = new DeleteSuperType("DiscoveredProject", "Project");
		DeleteSuperType dst5 = new DeleteSuperType("AveragedMultiDiscoveryBenchmark", "Benchmark");
		
		changes.add(dst1);
		changes.add(dst2);
		changes.add(dst3);
		changes.add(dst4);
		changes.add(dst5);
		
		
		AddClass ac1 = new AddClass("DiscoveryIteration");
		AddClass ac2 = new AddClass("Resource");
		AddClass ac3 = new AddClass("EndEvent");
		AddClass ac4 = new AddClass("BeginEvent");
		AddClass ac5 = new AddClass("EventType");
		AddClass ac6 = new AddClass("Event");
		AddClass ac7 = new AddClass("MemoryMeasurement");
		
		changes.add(ac1);
		changes.add(ac2);
		changes.add(ac3);
		changes.add(ac4);
		changes.add(ac5);
		changes.add(ac6);
		changes.add(ac7);
		
		AddSuperType ast1 = new AddSuperType("BeginEvent", "Event");
		AddSuperType ast2 = new AddSuperType("MemoryMeasurement", "Event");
		AddSuperType ast3 = new AddSuperType("EndEvent", "Event");
		AddSuperType ast4 = new AddSuperType("Project", "Resource");
		
		changes.add(ast1);
		changes.add(ast2);
		changes.add(ast3);
		changes.add(ast4);
		
		AddProperty ap1 = new AddProperty("memoryUsed", "MemoryMeasurement");
		AddProperty ap2 = new AddProperty("name", "EventType");
		AddProperty ap3 = new AddProperty("eventType", "Event");
		AddProperty ap4 = new AddProperty("time", "Event");
		AddProperty ap5 = new AddProperty("beginning", "EndEvent");
		AddProperty ap6 = new AddProperty("copyOfDiscovererDescription", "Discovery");
		AddProperty ap7 = new AddProperty("discovererLaunchConfiguration", "Discovery");
		AddProperty ap8 = new AddProperty("iterations", "Discovery");
		AddProperty ap9 = new AddProperty("project", "Discovery");
		AddProperty ap10 = new AddProperty("memoryMeasurements", "DiscoveryIteration");
		AddProperty ap11 = new AddProperty("events", "DiscoveryIteration");
		
		changes.add(ap1);
		changes.add(ap2);
		changes.add(ap3);
		changes.add(ap4);
		changes.add(ap5);
		changes.add(ap6);
		changes.add(ap7);
		changes.add(ap8);
		changes.add(ap9);
		changes.add(ap10);
		changes.add(ap11);
		
		return changes;
	}

}
