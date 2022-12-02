package fr.lip6.meta.ComplexChangeDetection.Utils;

import java.util.ArrayList;

public class Classes implements MetamodelElement{

	public String name = "";
	public ArrayList<References> references = null;
	public ArrayList<Classes> inheritances = null;
	public ArrayList<Property> properties = null;
	public ArrayList<Classes> enteringReferences = null;
	public Classes() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Classes(String name) {
		super();
		this.name = name;
		this.references = new ArrayList<References>();
		this.inheritances = new ArrayList<Classes>();
		this.properties = new ArrayList<Property>();
		this.enteringReferences = new ArrayList<Classes>();
	}

	public Classes(String name, ArrayList<References> references,
			ArrayList<Classes> inheritances, ArrayList<Property> properties, ArrayList<Classes> enteringReferences) {
		super();
		this.name = name;
		this.references = references;
		this.inheritances = inheritances;
		this.properties = properties;
		this.enteringReferences = enteringReferences;
	}

	public ArrayList<Property> getProperties() {
		return properties;
	}

	public void setProperties(ArrayList<Property> properties) {
		this.properties = properties;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<References> getReferences() {
		return references;
	}

	public void setReferences(ArrayList<References> references) {
		this.references = references;
	}

	public ArrayList<Classes> getEnteringReferences() {
		return enteringReferences;
	}

	public void setEnteringReferences(ArrayList<Classes> enteringReferences) {
		this.enteringReferences = enteringReferences;
	}	
	
	public ArrayList<Classes> getInheritances() {
		return inheritances;
	}

	public void setInheritances(ArrayList<Classes> inheritances) {
		this.inheritances = inheritances;
	}	
	
	public References getOneReference(ArrayList<References> foundReferences, String name){
		
		for(References c : foundReferences){
			if(c.getName().equals(name)){
				return c;
			}
		}
		return null;
	}
	
	public boolean doesReferenceExist(ArrayList<References> foundReferences, String name){
		
		for(References c : foundReferences){
			if(c.getName().equals(name)){
				return true;
			}
		}
		return false;
	}
	
	public String afficher(){
		String s=" class: ";
		s+=this.name+". ";
		/*for(References ref : this.references){
			s+=ref.afficher();
		}
		
		for(Property prop : this.properties){
			s+=prop.afficher();
		}*/
		
		return s;
	}
}
