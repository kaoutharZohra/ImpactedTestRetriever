package fr.lip6.meta.ComplexChangeDetection.AtomicChanges;

public class SetProperty extends AtomicChange{

	private String type = "";
	private boolean basic = false;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isBasic() {
		return basic;
	}
	public void setBasic(boolean basic) {
		this.basic = basic;
	}
	public SetProperty() {
		super();
		// TODO Auto-generated constructor stub
	}
	public SetProperty(String name, String type, boolean basic) {
		super(name);
		// TODO Auto-generated constructor stub
		this.type = type;
		this.basic = basic;
	}
	// This is added lately, so update the other construction from live recording
		private String container = "";
		
		public String getContainer() {
			return container;
		}
		public void setContainer(String container) {
			this.container = container;
		}
		
}
