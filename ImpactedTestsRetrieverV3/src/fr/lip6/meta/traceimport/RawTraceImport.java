package fr.lip6.meta.traceimport;

import java.io.FileNotFoundException;
import java.util.Scanner;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddSuperType;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteSuperType;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.RenameClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.RenameProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.SetProperty;

public class RawTraceImport {

	int id = 0;
	
	/*
	 * here we parse the metamodel changes 
	 * */
	public AtomicChange parseRawChangeTrace(String line){
		
		//String[] change = line.split(", ");
				
		//test for each change its type and create the correspong instance
		if (line != null && line != "") {
			//AtomicChange atomicChange = null;
			String[] names = {"","","",""};
			if (this.getKind(line).equals("AddClass")) {
				AddClass atomicChange = new AddClass(this.getSingleName(line));
				//atomicChange.setName(this.getSingleName(line));
				atomicChange.setId(String.valueOf(id));
				return atomicChange;
				
			} else if (this.getKind(line).equals("AddSuperType")) {
				names = this.getDoubleName(line);
				AddSuperType atomicChange = new AddSuperType(names[0], names[1]);
				//atomicChange.setName(names[0]);
				//atomicChange.setNameTarget(names[1]);
				atomicChange.setId(String.valueOf(id));
				return atomicChange;
				
			} else if (this.getKind(line).equals("AddProperty")) {
				names = this.getDoubleName(line);
				AddProperty atomicChange = new AddProperty(names[0], names[1]);
				//atomicChange.setName(names[0]);
				//atomicChange.setClassName(names[1]);
				atomicChange.setId(String.valueOf(id));
				return atomicChange;
				
			} else if (this.getKind(line).equals("SetProperty")) {
				names = this.getDoubleName(line);
				SetProperty atomicChange = new SetProperty(names[0], names[1], this.isBasicType(names[1]));
				//atomicChange.setName(names[0]);
				//atomicChange.setType(names[1]);
				//atomicChange.setBasic(this.isBasic(names[1]));
				atomicChange.setId(String.valueOf(id));
				return atomicChange;
				
			} else if (this.getKind(line).equals("DeleteClass")) {
				DeleteClass atomicChange = new DeleteClass(this.getSingleName(line));
				//atomicChange.setName(this.getSingleName(line));
				atomicChange.setId(String.valueOf(id));
				return atomicChange;
				
			} else if (this.getKind(line).equals("DeleteProperty")) {
				names = this.getDoubleName(line);
				DeleteProperty atomicChange = new DeleteProperty(names[0], names[1]);
				//atomicChange.setName(names[0]);
				//atomicChange.setClassName(names[1]);
				atomicChange.setId(String.valueOf(id));
				return atomicChange;
				
			} else if (this.getKind(line).equals("DeleteSuperType")) {
				names = this.getDoubleName(line);
				DeleteSuperType atomicChange = new DeleteSuperType(names[0], names[1]);
				//atomicChange.setName(names[0]);
				//atomicChange.setNameTarget(names[1]);
				atomicChange.setId(String.valueOf(id));
				return atomicChange;
				
			} else if (this.getKind(line).equals("RenameClass")) {
				names = this.getDoubleName(line);
				RenameClass atomicChange = new RenameClass(names[0], names[1], names[3]);
				atomicChange.setId(String.valueOf(id));
				return atomicChange;
				
			} else if (this.getKind(line).equals("RenameProperty")) {
				names = this.getDoubleName(line);
				System.out.println("zehahahha 1 "+names[0]+" -_- "+ names[1]+" -_- "+ names[3]);
				RenameProperty atomicChange = new RenameProperty(names[0], names[1], names[3]);
				atomicChange.setId(String.valueOf(id));
				//this.evolutionTrace.getAtomicEvolutionTrace().add(atomicChange);
				return atomicChange;
				
			} else if (this.getKind(line).equals("RenameParameter")) {
				
				
			} else if (this.getKind(line).equals("ChangeLowerBound")) {
				
				
			} else if (this.getKind(line).equals("ChangeUpperBound")) {
				
				
			} else if (this.getKind(line).equals("AddParameter")) {
				
				
			} else if (this.getKind(line).equals("DeleteParameter")) {
				
				
			}
			
			// this.trace.getTrace().add();
		
			id++;
		}
	
		return null;
	}
	
	private String getKind(String change) {//add try catch ArrayIndexOutOfBoundsException: 1
		return (change.split("\\(")[0]);
	}

	private String getSingleName(String change) {
		return (change.split("\\(")[1].split("\\)")[0]).split(", ")[0];
	}
	
	private String[] getDoubleName(String change) {
		String[] names = {"","","","","",""};
		names = (change.split("\\(")[1].split("\\)")[0]).split(", ");
		return names;
	}
	
	private boolean isBasicType(String change) {
		
		return change.equalsIgnoreCase("int") || change.equalsIgnoreCase("boolean") 
				|| change.equalsIgnoreCase("integer") || change.equalsIgnoreCase("double") 
				|| change.equalsIgnoreCase("String") || change.equalsIgnoreCase("real") 
				|| change.equalsIgnoreCase("float") ||
				change.equalsIgnoreCase("ETreeIterator") ||
				change.equalsIgnoreCase("EString") ||
				change.equalsIgnoreCase("EShortObject") ||
				change.equalsIgnoreCase("EShort") ||
				change.equalsIgnoreCase("EMap") ||
				change.equalsIgnoreCase("ELongObject") ||
				change.equalsIgnoreCase("EIntegerObject") ||
				change.equalsIgnoreCase("EInt") ||
				change.equalsIgnoreCase("EFloatObject") ||
				change.equalsIgnoreCase("EFloat") ||
				change.equalsIgnoreCase("EFeatureMapEntry") ||
				change.equalsIgnoreCase("EFeatureMap") ||
				change.equalsIgnoreCase("EEnumerator") ||
				change.equalsIgnoreCase("EEList") ||
				change.equalsIgnoreCase("EDoubleObject") ||
				change.equalsIgnoreCase("EDouble") ||
				change.equalsIgnoreCase("EDate") ||
				change.equalsIgnoreCase("ECharacterObject") ||
				change.equalsIgnoreCase("EChar") ||
				change.equalsIgnoreCase("EByteObject") ||
				change.equalsIgnoreCase("EByteArray") ||
				change.equalsIgnoreCase("EByte") ||
				change.equalsIgnoreCase("EBooleanObject") ||
				change.equalsIgnoreCase("EBoolean") ||
				change.equalsIgnoreCase("EBigInteger") ||
				change.equalsIgnoreCase("EBigDecimal");
	}
}
