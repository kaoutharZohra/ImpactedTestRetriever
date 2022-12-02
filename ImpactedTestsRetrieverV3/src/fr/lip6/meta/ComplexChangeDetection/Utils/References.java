package fr.lip6.meta.ComplexChangeDetection.Utils;


public class References implements MetamodelElement{

	public String name = "";
	public Classes sourceClass = null;
	public Classes targetClass = null;
	
	public References() {
		super();
		// TODO Auto-generated constructor stub
	}

	public References(String name, Classes sourceClass, Classes targetClass) {
		super();
		this.name = name;
		this.sourceClass = sourceClass;
		this.targetClass = targetClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Classes getSourceClass() {
		return sourceClass;
	}

	public void setSourceClass(Classes sourceClass) {
		this.sourceClass = sourceClass;
	}

	public Classes getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(Classes targetClass) {
		this.targetClass = targetClass;
	}
	
	public String afficher(){
		return " ref: "+this.name +" "+ this.sourceClass.getName()+" "+ this.targetClass.getName()+". ";
	}
	
	
}
