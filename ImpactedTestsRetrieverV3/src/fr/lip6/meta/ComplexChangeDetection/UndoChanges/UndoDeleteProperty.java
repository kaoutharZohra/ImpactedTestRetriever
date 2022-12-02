package fr.lip6.meta.ComplexChangeDetection.UndoChanges;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;

public class UndoDeleteProperty extends UndoChange{

	public AddProperty addProperty = null;
	public DeleteProperty deleteProperty = null;
	
	public UndoDeleteProperty(String name, AddProperty addProperty, DeleteProperty deleteProperty) {
		super(name);
		this.addProperty = addProperty;
		this.deleteProperty = deleteProperty;
	}
	
	public AddProperty getAddProperty() {
		return addProperty;
	}


	public void setAddProperty(AddProperty addProperty) {
		this.addProperty = addProperty;
	}


	public DeleteProperty getDeleteProperty() {
		return deleteProperty;
	}


	public void setDeleteProperty(DeleteProperty deleteProperty) {
		this.deleteProperty = deleteProperty;
	}
}
