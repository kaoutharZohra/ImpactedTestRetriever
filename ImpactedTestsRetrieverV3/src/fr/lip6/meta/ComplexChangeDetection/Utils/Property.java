package fr.lip6.meta.ComplexChangeDetection.Utils;

public class Property implements MetamodelElement{

	private String name = "";
	private String type = "";
	
	public Property() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Property(String name, String type) {
		super();
		this.name = name;
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String afficher(){
		return " prop: "+this.name +" "+ this.type+". ";
	}
	
	
}