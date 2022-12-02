package fr.lip6.meta.ComplexChangeDetection.AtomicChanges;

public class RenamePackage extends AtomicChange{

	private String newname = "";

	public RenamePackage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RenamePackage(String oldname, String newname) {
		super(oldname);
		this.newname = newname;
	}

	public String getNewname() {
		return newname;
	}

	public void setNewname(String newname) {
		this.newname = newname;
	}
	
	
}
