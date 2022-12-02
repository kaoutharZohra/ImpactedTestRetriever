package fr.lip6.meta.traceimport;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/*import monoge.dsl.ExtensionStandaloneSetup;
import monoge.dsl.extension.Create;
import monoge.dsl.extension.Extension;
import monoge.dsl.extension.FilterClass;
import monoge.dsl.extension.FilterProperty;
import monoge.dsl.extension.Generalize;
import monoge.dsl.extension.Model;
import monoge.dsl.extension.ModifyClass;
import monoge.dsl.extension.ModifyOperator;
import monoge.dsl.extension.ModifyProperty;
import monoge.dsl.extension.Refine;
import monoge.dsl.extension.ValueAssignment;*/

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddSuperType;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.ChangeLowerBound;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.ChangeUpperBound;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.RenameProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.SetProperty;

public class ExtensionTraceImport {

	String file = null;
	String path = null;
	ArrayList<AtomicChange> atomicTrace = null;
	ArrayList<String> atomicStringTrace = null;
	
	public ExtensionTraceImport() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ExtensionTraceImport(String file, String path) {
		super();
		this.file = file;
		this.path = path;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public ArrayList<AtomicChange> getAtomicTrace() {
		return atomicTrace;
	}

	public void setAtomicTrace(ArrayList<AtomicChange> atomicTrace) {
		this.atomicTrace = atomicTrace;
	}

	public ArrayList<String> getAtomicStringTrace() {
		return atomicStringTrace;
	}

	public void setAtomicStringTrace(ArrayList<String> atomicStringTrace) {
		this.atomicStringTrace = atomicStringTrace;
	}
/*
	public void loadExtensionTrace(){
		new ExtensionStandaloneSetup().createInjectorAndDoEMFRegistration();
		ResourceSet rs = new ResourceSetImpl();
		
		String fullPath = "file:"+File.separator+File.separator+File.separator+this.path+File.separator+this.file;
		System.out.println("\n	file full path "+fullPath.replace('\\', '/')+"\n");
		
		Resource resource = rs.getResource(URI.createURI(fullPath.replace('\\', '/')), true);//"model/dj.djamel"
		
		Model model = (Model) resource.getContents().get(0);
		
		System.out.println("\n	extensions = "+model.getExtensions()+"\n");
		
		//translate the extension operators to atomic changes
		this.extensionOperators2AtomicChanges(model.getExtensions());
		
		System.out.println("\n	END  \n");
		
	}
	
	private void extensionOperators2AtomicChanges(EList<Extension> operators){
		//here construct a mapping between Extension Operators and Atomic Changes, operator "create class" = > atomic change "add class"
		ArrayList<AtomicChange> translated = new ArrayList<AtomicChange>();
		ArrayList<String> translatedString = new ArrayList<String>();
		
		Iterator<Extension> it = operators.iterator();
		
		while(it.hasNext()){
			Extension ext = it.next();
			//Create | Refine | Generalize | ModifyClass | FilterClass 
			if(ext instanceof Create){//addClass
				
				translated.add(new AddClass(((Create) ext).getClass_()));
				translatedString.add("AddClass("+((Create) ext).getClass_()+", classID, packageID);");
				
			} else if(ext instanceof Refine){//addClass + addGeneralize
				
				translated.add(new AddClass(((Refine) ext).getClassNew()));
				translatedString.add("AddClass("+((Refine) ext).getClassNew()+", classID, packageID);");
				
				translated.add( new AddSuperType(((Refine) ext).getClassNew(), ((Refine) ext).getClassOriginal()));
				translatedString.add("AddSuperType("+((Refine) ext).getClassNew()+", "+((Refine) ext).getClassOriginal()+", classID1, classID2);");
				
			} else if(ext instanceof Generalize){//addClass + [addGeneralize]*
				
				translated.add(new AddClass(((Generalize) ext).getClassNew()));
				translatedString.add("AddClass("+((Generalize) ext).getClassNew()+", classID, packageID);");
				
				for(String subclass : ((Generalize) ext).getClass_()){
					translated.add( new AddSuperType(subclass, ((Generalize) ext).getClassNew()));
					translatedString.add("AddSuperType("+subclass+", "+((Generalize) ext).getClassNew()+", classID1, classID2);");
				}
					
			} else if(ext instanceof ModifyClass){//here call the sub function "translateModifyClassOperators"
				
				this.translateModifyClassOperators(translated, translatedString, ((ModifyClass) ext));
				
			} else if(ext instanceof FilterClass){//deleteClass
				
				translated.add(new DeleteClass(((FilterClass) ext).getClass_()));
				translatedString.add("DeleteClass("+((FilterClass) ext).getClass_()+", classID, packageID);");//maybe add package name??
			}
		}
		
		atomicTrace = translated;
		atomicStringTrace = translatedString;
		//return translated;
	}
	
	private void translateModifyClassOperators(ArrayList<AtomicChange> translated, ArrayList<String> translatedString, ModifyClass modify){
		//here traslate the operators of ModifyClass to atomic changes
		
		//AddProperty | ModifyProperty | FilterProperty | AddConstraint | FilterConstraint
		Iterator<ModifyOperator> it = modify.getModifyOperators().iterator();
		
		while(it.hasNext()){
			ModifyOperator op = it.next();
			
			if(op instanceof monoge.dsl.extension.AddProperty){//addPreoperty + setType
				
				translated.add(new AddProperty(((monoge.dsl.extension.AddProperty) op).getProperty(), modify.getClass_()));
				translatedString.add("AddProperty("+((monoge.dsl.extension.AddProperty) op).getProperty()+", " + modify.getClass_()+", propertyID, classID);");
				
				System.out.println("\nZehahahah \n"+ ((monoge.dsl.extension.AddProperty) op).getType());
				//System.out.println("\nZehahahah \n"+ ((monoge.dsl.extension.AddProperty) op).getType().get(0).getType());
				//System.out.println("\nZehahahah \n"+ ((monoge.dsl.extension.AddProperty) op).getType().get(0).getClass_());
				//System.out.println("\nZehahahah \n"+ ((monoge.dsl.extension.AddProperty) op).getType().get(0).getPrefix());
				
				if(((monoge.dsl.extension.AddProperty) op).getType().size() != 0){
					if(((monoge.dsl.extension.AddProperty) op).getType().get(0).getType().size() != 0){
						translated.add(new SetProperty(((monoge.dsl.extension.AddProperty) op).getProperty(), ((monoge.dsl.extension.AddProperty) op).getType().get(0).getType().get(0), true));
						translatedString.add("SetProperty("+((monoge.dsl.extension.AddProperty) op).getProperty()+", "+((monoge.dsl.extension.AddProperty) op).getType().get(0).getType().get(0)+", propertyID, typeID);");
					} else { 
						translated.add(new SetProperty(((monoge.dsl.extension.AddProperty) op).getProperty(), ((monoge.dsl.extension.AddProperty) op).getType().get(0).getClass_().get(0), true));
						translatedString.add("SetProperty("+((monoge.dsl.extension.AddProperty) op).getProperty()+", "+((monoge.dsl.extension.AddProperty) op).getType().get(0).getClass_().get(0)+", propertyID, typeID);");
					}
				}
				
				for(String car : ((monoge.dsl.extension.AddProperty) op).getCardinality()){
					
					
					System.out.println("\n "+car+" \n");
					String[] s = car.split("\\.\\.");//here it does not work, !!!
					System.out.println("\n "+s+" \n");
					System.out.println("\n "+s.length+" \n");
					if(s.length == 1){
						translated.add(new ChangeUpperBound(((monoge.dsl.extension.AddProperty) op).getProperty(), modify.getClass_(), "1", "*")); //here I put "1", since in the trace I do not know what is the old value, however this could be retrieved from the model
						translatedString.add("ChangeUpperBound("+((monoge.dsl.extension.AddProperty) op).getProperty()+", 1, *, propertyID"+", "+modify.getClass_()+", classID);");
					} else if(s.length == 2){
						translated.add(new ChangeLowerBound(((monoge.dsl.extension.AddProperty) op).getProperty(), modify.getClass_(), "0", s[0])); //here I put "0", since in the trace I do not know what is the old value, however this could be retrieved from the model
						translatedString.add("ChangeLowerBound("+((monoge.dsl.extension.AddProperty) op).getProperty()+", 0"+", "+s[0]+", propertyID"+", "+modify.getClass_()+", classID);");
						
						translated.add(new ChangeUpperBound(((monoge.dsl.extension.AddProperty) op).getProperty(), modify.getClass_(), "1", s[1])); //here I put "1", since in the trace I do not know what is the old value, however this could be retrieved from the model
						translatedString.add("ChangeUpperBound("+((monoge.dsl.extension.AddProperty) op).getProperty()+", 1"+", "+s[1]+", propertyID"+", "+modify.getClass_()+", classID);");
					}
				}
				
			} else if(op instanceof ModifyProperty){//here modifu the properties (ex: name type bound...) of a property
				
				Iterator<ValueAssignment> it2 = ((ModifyProperty) op).getValue().iterator();
				
				while(it2.hasNext()){
					ValueAssignment va = it2.next();
					System.out.println("\nhere ValueAssignment\n");
					if(va.getAttribute().equals("name")){//rename
						System.out.println("\nhere rename\n");
						
						translated.add(new RenameProperty(((ModifyProperty) op).getProperty(), this.getTheValue(va.getValue()), modify.getClass_()));
						translatedString.add("RenameProperty("+((ModifyProperty) op).getProperty()+", "+this.getTheValue(va.getValue())+", propertyID"+", "+modify.getClass_()+", classID);");
						
					} else if(va.getAttribute().contains("type")){//change type
						System.out.println("\nhere type\n");
						translated.add(new SetProperty(((ModifyProperty) op).getProperty(), this.getTheValue(va.getValue()), true));
						translatedString.add("SetProperty("+((ModifyProperty) op).getProperty()+", "+this.getTheValue(va.getValue())+", propertyID, typeID);");
						
					} else if(va.getAttribute().contains("lower") && va.getAttribute().contains("bound")){//lower bound
						System.out.println("\nhere louwer\n");
						translated.add(new ChangeLowerBound(((ModifyProperty) op).getProperty(), modify.getClass_(), "0", this.getTheValue(va.getValue()))); //here I put "0", since in the trace I do not know what is the old value, however this could be retrieved from the model
						translatedString.add("ChangeLowerBound("+((ModifyProperty) op).getProperty()+", 0"+", "+this.getTheValue(va.getValue())+", propertyID"+", "+modify.getClass_()+", classID);");
						
					} else if(va.getAttribute().contains("upper") && va.getAttribute().contains("bound")){//upper bound
						
						translated.add(new ChangeUpperBound(((ModifyProperty) op).getProperty(), modify.getClass_(), "1", this.getTheValue(va.getValue()))); //here I put "1", since in the trace I do not know what is the old value, however this could be retrieved from the model
						translatedString.add("ChangeUpperBound("+((ModifyProperty) op).getProperty()+", 1"+", "+this.getTheValue(va.getValue())+", propertyID"+", "+modify.getClass_()+", classID);");
					}
					
				}
				
			} else if(op instanceof FilterProperty){//deleteClass
				
				translated.add(new DeleteProperty(((FilterProperty) op).getProperty(), modify.getClass_()));
				translatedString.add("DeleteProperty("+((FilterProperty) op).getProperty()+", "+modify.getClass_()+", propertyID, classID);");	
				
			} else if(op instanceof AddConstraint){
				
			} else if(op instanceof FilterConstraint){
				
			}
		}
		
		//return translated;
	}*/
	
	
	private String getTheValue(String val){
		if(val != null && val.contains("\"")) return val.split("\"")[1];
		return val;
	}
}
