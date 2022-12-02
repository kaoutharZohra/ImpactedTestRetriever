package fr.lip6.meta.ComplexChangeDetection.Utils;

import java.util.ArrayList;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EGenericTypeImpl;
import org.eclipse.emf.ecore.impl.EOperationImpl;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.impl.EReferenceImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.internal.impl.ClassImpl;
import org.eclipse.uml2.uml.internal.impl.AssociationImpl;
import org.eclipse.uml2.uml.internal.impl.PropertyImpl;


public class InitialState {// initial state of the metamodel and its element
	
	public ArrayList<Classes> classes = null;
	public String path = "";
	public ResourceSet resourceSet = null;//new ResourceSetImpl();
	public Resource resource = null;
	public ArrayList<Classes> getClasses() {
		return classes;
	}

	public void setClasses(ArrayList<Classes> classes) {
		this.classes = classes;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public InitialState() {
		super();
		// TODO Auto-generated constructor stub
		this.classes = new ArrayList<Classes>();
		this.resourceSet = new ResourceSetImpl();
	}
	
	public InitialState(String path) {
		super();
		// TODO Auto-generated constructor stub
		this.classes = new ArrayList<Classes>();
		this.resourceSet = new ResourceSetImpl();
		this.path = path;
	}
	
	public InitialState(Resource resource) {
		super();
		// TODO Auto-generated constructor stub
		this.classes = new ArrayList<Classes>();
		this.resourceSet = new ResourceSetImpl();
		this.resource = resource;
	}
	
	public void initialize(boolean isEcore){
		if(isEcore){
			this.initializeEcore();
		} else {
			this.initializeUMLCDPapyrus();
		}
		this.dislpayClasses();
	}
	
	public void initializeEcore(){
		
		EPackageImpl packages = null;
		if(this.path != ""){
			this.loadEcore(this.path, this.resourceSet);
			//this.resourceSet.getResources().get(0);
			packages = (EPackageImpl) this.resourceSet.getResources().get(0).getContents().get(0);
		} else packages = (EPackageImpl) this.resource.getContents().get(0);
		//EPackageImpl
		
		for(EClassifier c : packages.getEClassifiers()){
			System.out.println(c.getName() + " -- "+ c);
			Classes temporaryClass = new Classes(c.getName());
			this.classes.add(temporaryClass);
			
		}
		
		for(EClassifier c : packages.getEClassifiers()){
			//System.out.println(c.getName() + " -- "+ c);
			Classes foundClass = this.getOneClass(c.getName());
			if(foundClass != null){
				for(EObject a :c.eContents()){
					System.out.println("	content = "+a);
					if(a instanceof EOperationImpl){
						Property temporarProperty = new Property(((EOperationImpl) a).getName(), (((EOperationImpl) a).getEType() != null) ? ((EOperationImpl) a).getEType().getName() : null);
						foundClass.getProperties().add(temporarProperty);
						
						//if you want to include the parameter elements and their types it is here!!
						//((EOperationImpl) a).getEParameters();
						//((EOperationImpl) a).getETypeParameters();
					} else if(a instanceof EAttributeImpl){
						Property temporarProperty = new Property(((EAttributeImpl) a).getName(), (((EAttributeImpl) a).getEType() != null) ? ((EAttributeImpl) a).getEType().getName() : null);
						foundClass.getProperties().add(temporarProperty);
					} 
					
				}
				for(EObject j : c.eCrossReferences()){//c.eContents() is inluded in c.eCrossReferences()
					System.out.println("	cross ref = "+j);
					if(j instanceof EClassImpl){
						//here there is the same information as in EGenericTypeImpl, but in case something is missing, it might come from here!
						
					} else if(j instanceof EReferenceImpl){//if you need information about opposite reference or value ect. it is here to find them!
						EReferenceImpl refernece = (EReferenceImpl) j;
						if(!foundClass.doesReferenceExist(foundClass.references, refernece.getName())){
							//System.out.println("	ref name = "+refernece.getName());
							//System.out.println("	ref type = "+refernece.getEType().getName());
							References temporaryReference = new References(refernece.getName(), foundClass, (refernece.getEType() != null) ? this.getOneClass(refernece.getEType().getName()) : null);
							if(refernece.getEType() != null){//here to have backward path that can be used to access this class (for coevolution)
								this.getOneClass(refernece.getEType().getName()).getEnteringReferences().add(foundClass);
								}
							foundClass.getReferences().add(temporaryReference);
						}
					} else if(j instanceof EGenericTypeImpl){
						//System.out.println("	EGenericTypeImpl classifer = "+((EGenericTypeImpl) j).getEClassifier().getName());
						//System.out.println("	EGenericTypeImpl raw = "+((EGenericTypeImpl) j).getERawType().getName());
						EGenericTypeImpl inherit = (EGenericTypeImpl) j;
						if(!this.doesClassExist(foundClass.getInheritances(), inherit.getEClassifier().getName()) && this.getOneClass(inherit.getEClassifier().getName()) != null){
							//si la class n'est pas deja dans la liste de inheritances et que il y a une class qui porte ce nom != null
							foundClass.getInheritances().add(this.getOneClass(inherit.getEClassifier().getName()));
						}
					} 
				}
			}
		}
	}
	
	public Classes getOneClass(String name){
		
		for(Classes c : this.classes){
			if(c.getName().equals(name)){
				return c;
			}
		}
		return null;
	}
	
	public boolean doesClassExist(ArrayList<Classes> foundClass, String name){
		
		for(Classes c : foundClass){
			if(c.getName().equals(name)){
				return true;
			}
		}
		return false;
	}
	
	public void loadEcore(String absolutePath, ResourceSet resourceSet) {
		  
		URI uri = URI.createFileURI(absolutePath);
		//resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
		// Resource will be loaded within the resource set
		resourceSet.getResource(uri, true);
	}
	
	public void dislpayClasses(){
		for(Classes c : this.classes){
			System.out.println("main class = "+c.getName() + " -- "+ c);
				for(References ref : c.getReferences()){
					System.out.println("	ref = "+ref.getName() + " -- "+ ref.getSourceClass().getName() + " to "+ ref.getTargetClass().name + " -- "+ref);
				}/**/
				for(Classes inh : c.getInheritances()){
					System.out.println("	inh = "+inh.getName() + " -- "+ inh);
				}
				for(Property prop : c.getProperties()){
					System.out.println("	prop = "+prop.getName() + " -- "+ prop);
				}
		}
	}
	
	public void initializeUMLCDPapyrus(){
		
		System.out.println("the resource = "+this.resource.getContents().get(0));
		System.out.println("the resource class = "+this.resource.getContents().get(0).getClass());
		//org.eclipse.uml2.uml.internal.impl.ModelImpl
		//org.eclipse.uml2.uml.internal.impl.PackageImpl
		
		EList<EObject> l = resource.getContents().get(0).eContents();
		
		for(int i=0; i< l.size();i++){
			
			if (l.get(i) instanceof ClassImpl) {
				System.out.println(l.get(i));
				ClassImpl c = (ClassImpl)l.get(i);
				Classes temporaryClass = new Classes(c.getName());
				this.classes.add(temporaryClass);
			}
		}
		
		for(int i=0; i< l.size();i++){
			
			EObject o = l.get(i);
			if (o instanceof ClassImpl) {
				ClassImpl c = (ClassImpl)o;
				Classes foundClass = this.getOneClass(c.getName());
				
				for(org.eclipse.uml2.uml.Property p : c.getAttributes()){//use getAllAttributes for the attributes of the superclass
					//System.out.println("	association of "+p+" = "+p.getAssociation());
					Property temporarProperty = new Property(p.getName(), (p.getType() != null) ? p.getType().getName() : null);
					foundClass.getProperties().add(temporarProperty);
				}
				
				for(Operation op : c.getOperations()){//use c.getAllOperations() for the operations of the superclass
					Property temporarProperty = new Property(op.getName(), (op.getType() != null) ? op.getType().getName() : null);
					foundClass.getProperties().add(temporarProperty);
					
					//if you want to include the parameter elements and their types it should be here!!
				}
				
				//now we retrieve the references of the class Part (1) and the superclass Part (2)
				//Part (1)
				for(Association asso : c.getAssociations()){
					if(asso.getAllAttributes().size()>1){
						//EReferenceImpl refernece = (EReferenceImpl) j;
						if(!foundClass.doesReferenceExist(foundClass.references, asso.getAllAttributes().get(1).getName())
								&& foundClass != this.getOneClass(asso.getAllAttributes().get(1).getType().getName())){
							//System.out.println("	ref name = "+refernece.getName());
							//System.out.println("	ref type = "+refernece.getEType().getName());
							References temporaryReference = new References(asso.getAllAttributes().get(1).getName(), foundClass, (asso.getAllAttributes().get(1).getType() != null) ? this.getOneClass(asso.getAllAttributes().get(1).getType().getName()) : null);
							foundClass.getReferences().add(temporaryReference);
						}
					}
				}
				
				//Part (2)
				this.allReferences(c, foundClass);
				
				
				//here we retrieve the inheritances of the class Part (1) and the superclass Part (2)
				//Part (1)
				for(Class sc : c.getSuperClasses()){
					//EGenericTypeImpl inherit = (EGenericTypeImpl) j;
					if(!this.doesClassExist(foundClass.getInheritances(), sc.getName()) && this.getOneClass(sc.getName()) != null){
						//si la class n'est pas deja dans la liste de inheritances et que il y a une class qui porte ce nom != null
						foundClass.getInheritances().add(this.getOneClass(sc.getName()));
					}
				}
				
				//Part (2)
				this.allInhiritnaces(c, foundClass);
				
				}
		
			//System.out.println("zehahahh");
		}
	}
	
	private void allReferences(Class c, Classes foundClass){
		
		for(Class sc : c.getSuperClasses()){//here you get the reference only for the first direct super class, if you want further super class, you must make a recursive function 
			for(Association asso : sc.getAssociations()){
				if(asso.getAllAttributes().size()>1){
					//EReferenceImpl refernece = (EReferenceImpl) j;
					if(!foundClass.doesReferenceExist(foundClass.references, asso.getAllAttributes().get(1).getName())
							&& foundClass != this.getOneClass(asso.getAllAttributes().get(1).getType().getName())){
						//System.out.println("	ref name = "+refernece.getName());
						//System.out.println("	ref type = "+refernece.getEType().getName());
						References temporaryReference = new References(asso.getAllAttributes().get(1).getName(), foundClass, (asso.getAllAttributes().get(1).getType() != null) ? this.getOneClass(asso.getAllAttributes().get(1).getType().getName()) : null);
						foundClass.getReferences().add(temporaryReference);
					}
				}
			}
			this.allReferences(sc, foundClass);
		}
		
		
	}
	
	private void allInhiritnaces(Class c, Classes foundClass){
		
		for(Class sc : c.getSuperClasses()){
			//EGenericTypeImpl inherit = (EGenericTypeImpl) j;
			if(!this.doesClassExist(foundClass.getInheritances(), sc.getName()) && this.getOneClass(sc.getName()) != null){
				//si la class n'est pas deja dans la liste de inheritances et que il y a une class qui porte ce nom != null
				foundClass.getInheritances().add(this.getOneClass(sc.getName()));
			}
			this.allInhiritnaces(sc, foundClass);
		}
	}

}
