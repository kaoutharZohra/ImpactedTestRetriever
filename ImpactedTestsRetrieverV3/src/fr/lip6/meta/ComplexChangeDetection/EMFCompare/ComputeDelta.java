package fr.lip6.meta.ComplexChangeDetection.EMFCompare;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.DifferenceKind;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.internal.spec.AttributeChangeSpec;
import org.eclipse.emf.compare.internal.spec.ReferenceChangeSpec;
import org.eclipse.emf.compare.match.DefaultComparisonFactory;
import org.eclipse.emf.compare.match.DefaultEqualityHelperFactory;
import org.eclipse.emf.compare.match.DefaultMatchEngine;
import org.eclipse.emf.compare.match.IComparisonFactory;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.eobject.IEObjectMatcher;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryImpl;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryRegistryImpl;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.EAnnotationImpl;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EDataTypeImpl;
import org.eclipse.emf.ecore.impl.EOperationImpl;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.impl.EReferenceImpl;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

import fr.lip6.meta.ComplexChangeDetection.Change;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddSuperType;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.ChangeLowerBound;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.ChangeUpperBound;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteSuperType;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.RenameClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.RenamePackage;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.RenameProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.SetProperty;
import fr.lip6.meta.ComplexChangeDetection.Utils.Classes;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.ComplexChangeDetection.Utils.Property;
import fr.lip6.meta.ComplexChangeDetection.Utils.References;

public class ComputeDelta {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//"Model/OCLstdlibCSV6.0.0.ecore", "Model/OCLstdlibCSV6.18.0.ecore");
		//"Model/PivotV6.0.1.ecore", "Model/PivotV6.7.0.ecore"); //"Model/PivotV6.18.0.ecore"
		//String [] tab = {"Model/PivotV6.1.0.ecore","Model/PivotV6.7.0.ecore"};
		
		//"Model/PivotV6.1.0.ecore", "Model/PivotV6.7.0.ecore");
		//"Model/PivotV6.1.0.ecore", "Model/PivotV6.9.0.ecore");
		String [] tab = {"Model/test2.ecore", "Model/test3.ecore"};
		//"C:/Users/dkhellad/Documents/eclipses/eclipse-epsilon-1.4-win32-x86_64/workspace2/MCCoEvolutator/models/ocl/3.2.2/Pivot.ecore", "C:/Users/dkhellad/Documents/eclipses/eclipse-epsilon-1.4-win32-x86_64/workspace2/MCCoEvolutator/models/ocl/3.4.4/Pivot.ecore");
		
		String source = tab[0];
		String target = tab[1];
		
		InitialState initState = new InitialState(source); //initialize the source metamodel that we use for precise cc detection
		initState.initialize(true);//ecore
		
		InitialState initStateTarget = new InitialState(target); //initialize the target metamodel that we use for precise cc detection
		initStateTarget.initialize(true);//ecore
		
		ComputeDelta cDelta = new ComputeDelta();
		Comparison comparison = cDelta.compare(source, target);
				
		
		ArrayList<AtomicChange> changes = cDelta.importeEMFCompareChanges(comparison, initState, initStateTarget);
		System.out.println("transfered diff delta size: " + changes.size());
	}

	
	
	public void load(String absolutePath, ResourceSet resourceSet) {
		  URI uri = URI.createFileURI(absolutePath);

		  resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().
		  	put("ecore", new EcoreResourceFactoryImpl());//new XMIResourceFactoryImpl()

		  // Resource will be loaded within the resource set
		  resourceSet.getResource(uri, true);
		}

	public Comparison compare(String pathOriginalVersion, String pathEvolvedVersion) {
		
		// Load the two input models
		ResourceSet resourceSet1 = new ResourceSetImpl();
		ResourceSet resourceSet2 = new ResourceSetImpl();
		String ecore1 = pathEvolvedVersion;
		String ecore2 = pathOriginalVersion;
		this.load(ecore1, resourceSet1);
		this.load(ecore2, resourceSet2);

		// Configure EMF Compare
		IEObjectMatcher matcher = DefaultMatchEngine.createDefaultEObjectMatcher(UseIdentifiers.NEVER);
		IComparisonFactory comparisonFactory = new DefaultComparisonFactory(new DefaultEqualityHelperFactory());
		IMatchEngine.Factory matchEngineFactory = new MatchEngineFactoryImpl(matcher, comparisonFactory);
	        matchEngineFactory.setRanking(20);
	        IMatchEngine.Factory.Registry matchEngineRegistry = new MatchEngineFactoryRegistryImpl();
	        matchEngineRegistry.add(matchEngineFactory);
		EMFCompare comparator = EMFCompare.builder().setMatchEngineFactoryRegistry(matchEngineRegistry).build();

		// Compare the two models
		IComparisonScope scope = new DefaultComparisonScope(resourceSet1, resourceSet2, null);//EMFCompare.createDefaultScope(resourceSet1, resourceSet2);
		return comparator.compare(scope);
	}
	
	public ArrayList<AtomicChange> importeEMFCompareChanges(Comparison comparison, InitialState initState, InitialState initStateTarget) {//the states are so far needed for the MOVE change
		
		System.out.println("diff delta size: " + comparison.getDifferences().size());
		int i = 1;
		int id = 0;
		ArrayList<AtomicChange> changes = new ArrayList<AtomicChange>();
		
		for(Diff diff : comparison.getDifferences()){
//			System.out.println('\n');
//			System.out.println(i +") diff " + diff);
//			System.out.println("equiv " + diff.getEquivalence());
//			System.out.println("match " + diff.getMatch());
//			System.out.println("	match " + diff.getMatch().getComparison());
//			System.out.println("	match " + diff.getMatch().getDifferences());
//			System.out.println("	match " + diff.getMatch().getSubmatches());
			i++;
			id++;
			
			
			if(diff.getKind().equals(DifferenceKind.ADD)){
//				System.out.println(i +") diff " + diff);
//				System.out.println(i +") class " + diff.getClass());
//				System.out.println("match " + diff.getMatch());
//				System.out.println("	add left " + diff.getMatch().getLeft());
//				System.out.println("	add right " + diff.getMatch().getRight());
				
				if(diff instanceof ReferenceChangeSpec){
					ReferenceChangeSpec ref = (ReferenceChangeSpec) diff;
					String name = "";
					int cas = 0;
					
					if(ref.getValue() instanceof EReferenceImpl){
						name = ((EReferenceImpl)ref.getValue()).getName();
						cas = 1;
						
					} else if(ref.getValue() instanceof EAttributeImpl){
						name = ((EAttributeImpl)ref.getValue()).getName();
						cas = 1;
						
					} else if(ref.getValue() instanceof EClassImpl){
						name = ((EClassImpl)ref.getValue()).getName();
						cas = 2;
						
					} else if(ref.getValue() instanceof EClassImpl){
						name = ((EClassImpl)ref.getValue()).getName();
						cas = 3;
						
					} else if(ref.getValue() instanceof EOperationImpl){
						name = ((EOperationImpl)ref.getValue()).getName();
						cas = 1;
					}
					
					if(cas == 1){
						String className = ((EClassImpl) ref.getMatch().getLeft()).getName();
						AddProperty atomicChange = new AddProperty(name, className);
						atomicChange.setId(String.valueOf(id));
						changes.add(atomicChange);
						System.out.println("Added property " + atomicChange.getName() + " in " +atomicChange.getClassName());
					
					} else if (cas == 2){
						if(ref.getReference().getName().equals("eClassifiers")){//here we treate the add class
							AddClass atomicChange = new AddClass(name);
							atomicChange.setId(String.valueOf(id));
							changes.add(atomicChange);
							System.out.println("Added class " + atomicChange.getName());
							
						} else if(ref.getReference().getName().equals("eSuperTypes")){//here we treate the add super class
							String sourceName = "";
							
							if(ref.getMatch().getLeft() != null) sourceName = ((EClassImpl) ref.getMatch().getLeft()).getName(); //this if is not necessayr as we can always use safely the left as we can never add super to a deleted class
							else if(ref.getMatch().getRight() != null) sourceName = ((EClassImpl) ref.getMatch().getRight()).getName();
							
							AddSuperType atomicChange = new AddSuperType(sourceName, name);
							atomicChange.setId(String.valueOf(id));
							changes.add(atomicChange);
							System.out.println("Added super type in " + atomicChange.getName() + " to " +atomicChange.getNameTarget());
						}
					}
					
					//String className = ((EClassImpl) ref.getMatch().getRight()).getName();
					
					//AddProperty atomicChange = new AddProperty(propName, className);
					//atomicChange.setName(names[0]);
					//atomicChange.setClassName(names[1]);
					//atomicChange.setId(String.valueOf(id));
					//changes.add(atomicChange);
					//System.out.println("Added " + atomicChange.getName() + " in " +atomicChange.getClassName());
					
				} 
				
			} else if(diff.getKind().equals(DifferenceKind.DELETE)){
//				System.out.println(i +") diff " + diff);
//				System.out.println(i +") class " + diff.getClass());
//				System.out.println("match " + diff.getMatch());
//				System.out.println("	delete left " + diff.getMatch().getLeft());
//				System.out.println("	delete right " + diff.getMatch().getRight());
				
				if(diff instanceof ReferenceChangeSpec){
					ReferenceChangeSpec ref = (ReferenceChangeSpec) diff;
					
					String name = "";
					int cas = 0;
					if(ref.getMatch().getRight() instanceof EAnnotationImpl){
						cas = -1; //do not treat so far
						//System.out.println("	Djamel delete right " + ((EAnnotationImpl)ref.getMatch().getRight()).getEModelElement()); //EContainingClass());
						
					} else if(ref.getValue() instanceof EReferenceImpl){
						name = ((EReferenceImpl)ref.getValue()).getName();
						cas = 1;
						
					} else if(ref.getValue() instanceof EAttributeImpl){
						name = ((EAttributeImpl)ref.getValue()).getName();
						cas = 1;
						
					} else if(ref.getValue() instanceof EClassImpl){
						name = ((EClassImpl)ref.getValue()).getName();
						cas = 2;
						
					}  else if(ref.getValue() instanceof EOperationImpl){
						name = ((EOperationImpl)ref.getValue()).getName();
						cas = 1;
					}
					
					if(cas==1){
						
							String className = ((EClassImpl) ref.getMatch().getRight()).getName();
							
							DeleteProperty atomicChange = new DeleteProperty(name, className);
							atomicChange.setId(String.valueOf(id));
							changes.add(atomicChange);
							System.out.println("Deleted property " + atomicChange.getName() + " in " +atomicChange.getClassName());
							
					} else if(cas==2){

						if(ref.getReference().getName().equals("eClassifiers")){//here we treate the delete class
							//String packageName = ((EClassImpl) ref.getMatch().getRight()).getName();
							DeleteClass atomicChange = new DeleteClass(name);
							atomicChange.setId(String.valueOf(id));
							changes.add(atomicChange);
							System.out.println("Deleted class " + atomicChange.getName());
							
						} else if(ref.getReference().getName().equals("eSuperTypes")){//here we treate the delete super class
							String sourceName = "";
							
							if(ref.getMatch().getLeft() != null) sourceName = ((EClassImpl) ref.getMatch().getLeft()).getName(); //this if treats different cases of deleting super in existign calss or deleted class 
							else if(ref.getMatch().getRight() != null) sourceName = ((EClassImpl) ref.getMatch().getRight()).getName();
							
							DeleteSuperType atomicChange = new DeleteSuperType(sourceName, name);
							atomicChange.setId(String.valueOf(id));
							changes.add(atomicChange);
							System.out.println("Deteletd super type in " + atomicChange.getName() + " to " +atomicChange.getNameTarget());
							
						}
							
					}
					
					
				}
				
			} else if(diff.getKind().equals(DifferenceKind.CHANGE)){
				
				//System.out.println(i +") diff change => " + diff);

				//System.out.println("	change left " + diff.getMatch().getLeft());
				//System.out.println("	change right " + diff.getMatch().getRight());
				
				if(diff instanceof AttributeChangeSpec){
					AttributeChangeSpec aChange = (AttributeChangeSpec) diff;
					
					
					//System.out.println("	change attribute " + aChange.get);
					
					String oldName = "";
					String newName = "";
					String containerName = "";
					int cas = 0;
					if(diff.getMatch().getLeft() instanceof EClassImpl
							& aChange.getAttribute().getName().equals("name")){
						
						oldName = ((EClassImpl) diff.getMatch().getRight()).getName();
						newName = ((EClassImpl) diff.getMatch().getLeft()).getName();
						containerName = ((EClassImpl) diff.getMatch().getRight()).getEPackage().getName();
						
						cas = 1;
						
						
						
					} else if(diff.getMatch().getLeft() instanceof EAttributeImpl
							& aChange.getAttribute().getName().equals("name")){
						
						oldName = ((EAttributeImpl) diff.getMatch().getRight()).getName();
						newName = ((EAttributeImpl) diff.getMatch().getLeft()).getName();
						containerName = ((EAttributeImpl) diff.getMatch().getRight()).getEContainingClass().getName();
						
						cas = 2;
						
					} else if(diff.getMatch().getLeft() instanceof EPackageImpl 
							& aChange.getAttribute().getName().equals("name")){
						
						oldName = ((EPackageImpl) diff.getMatch().getRight()).getName();
						newName = ((EPackageImpl) diff.getMatch().getLeft()).getName();
						
						cas = 3;

					} else if(diff.getMatch().getLeft() instanceof EReferenceImpl
							& aChange.getAttribute().getName().equals("name")){//ref.getValue()
						oldName = ((EReferenceImpl) diff.getMatch().getRight()).getName();
						newName = ((EReferenceImpl) diff.getMatch().getLeft()).getName();
						containerName = ((EReferenceImpl) diff.getMatch().getRight()).getEContainingClass().getName();
						
						cas = 2;
						
					} else if(diff.getMatch().getLeft() instanceof EOperationImpl
							& aChange.getAttribute().getName().equals("name")){//ref.getValue()
						oldName = ((EOperationImpl) diff.getMatch().getRight()).getName();
						newName = ((EOperationImpl) diff.getMatch().getLeft()).getName();
						containerName = ((EOperationImpl) diff.getMatch().getRight()).getEContainingClass().getName();
						
						cas = 2;
						 
					} else if(diff.getMatch().getLeft() instanceof EReferenceImpl
							& aChange.getAttribute().getName().equals("lowerBound")){
						//treat case here directly
						
						int oldLower = ((EReferenceImpl) diff.getMatch().getRight()).getLowerBound();
						int newLower = ((EReferenceImpl) diff.getMatch().getLeft()).getLowerBound();
						oldName = ((EReferenceImpl) diff.getMatch().getRight()).getName();
						containerName = ((EReferenceImpl) diff.getMatch().getRight()).getEContainingClass().getName();
						
						ChangeLowerBound atomicChange = new ChangeLowerBound(oldName, containerName, oldLower+"", newLower+"");
						
						atomicChange.setId(String.valueOf(id));
						changes.add(atomicChange);
						System.out.println("Change Lower Bound property " + atomicChange.getName() + " in " + atomicChange.getClassName()+ " from " +atomicChange.getOldValue() +" to " + atomicChange.getNewValue());
						
						
					} else if(diff.getMatch().getLeft() instanceof EReferenceImpl
							& aChange.getAttribute().getName().equals("upperBound")){
						//treat case here directly
						
						int oldUpper = ((EReferenceImpl) diff.getMatch().getRight()).getUpperBound();
						int newUpper = ((EReferenceImpl) diff.getMatch().getLeft()).getUpperBound();
						
						oldName = ((EReferenceImpl) diff.getMatch().getRight()).getName();
						containerName = ((EReferenceImpl) diff.getMatch().getRight()).getEContainingClass().getName();
						
						ChangeUpperBound atomicChange = new ChangeUpperBound(oldName, containerName, oldUpper+"", newUpper+"");
						
						atomicChange.setId(String.valueOf(id));
						changes.add(atomicChange);
						System.out.println("Change Upper Bound property " + atomicChange.getName() + " in " + atomicChange.getClassName()+ " from " +atomicChange.getOldValue() +" to " + atomicChange.getNewValue());
						
					} 
					
					
					
					//if(!oldName.equals(newName)){
						if(cas == 1){
							RenameClass atomicChange = new RenameClass(oldName, newName, containerName);
							atomicChange.setId(String.valueOf(id));
							changes.add(atomicChange);
							System.out.println("Rename class " + atomicChange.getName() + " to " + atomicChange.getNewname()+ " in " +atomicChange.getPackageName());
							
						} else if(cas == 2){
							RenameProperty atomicChange = new RenameProperty(oldName, newName, containerName);
							atomicChange.setId(String.valueOf(id));
							changes.add(atomicChange);
							System.out.println("Rename property " + atomicChange.getName() + " to " + atomicChange.getNewname()+ " in " +atomicChange.getClassName());
							
						} else if(cas == 3){
							RenamePackage atomicChange = new RenamePackage(oldName, newName);
							atomicChange.setId(String.valueOf(id));
							changes.add(atomicChange);
							System.out.println("Rename package " + atomicChange.getName() + " to " + atomicChange.getNewname());
							
						}
					//}
					
				} else if(diff instanceof ReferenceChangeSpec){ // could add the condition "&  diff.getMatch().getLeft() != null" to treat onl the additions and changes, but since in deletions the lest if null, it will not match any type below
					ReferenceChangeSpec ref = (ReferenceChangeSpec) diff;
					
					String name = "";
					String type = "";
					String isbasic = "";
					String container = "";
					
					if(diff.getMatch().getLeft() instanceof EAttributeImpl){
						name = ((EAttributeImpl) diff.getMatch().getLeft()).getName();
						if(diff.getMatch().getLeft() != null) container = ((EAttributeImpl) diff.getMatch().getLeft()).getEContainingClass().getName(); //here to make sure that we take the name from the original MM, otherwise it means it is an added prop
						else container = ((EAttributeImpl) diff.getMatch().getRight()).getEContainingClass().getName();
								//((EAttributeImpl) comparison.getMatch(ref.getValue()).getRight()).getEContainingClass().getName();
						 
					} else if(diff.getMatch().getLeft() instanceof EReferenceImpl){
						name = ((EReferenceImpl) diff.getMatch().getLeft()).getName();
						if(diff.getMatch().getLeft() != null) container = ((EReferenceImpl) diff.getMatch().getLeft()).getEContainingClass().getName();
						else container = ((EReferenceImpl) diff.getMatch().getRight()).getEContainingClass().getName();
								//((EReferenceImpl) comparison.getMatch(ref.getValue()).getRight()).getEContainingClass().getName();
						
					} else if(diff.getMatch().getLeft() instanceof EOperationImpl){
						name = ((EOperationImpl) diff.getMatch().getLeft()).getName();
						if(diff.getMatch().getLeft() != null) container = ((EOperationImpl) diff.getMatch().getLeft()).getEContainingClass().getName();
						else container = ((EOperationImpl) diff.getMatch().getRight()).getEContainingClass().getName();
								//((EOperationImpl) comparison.getMatch(ref.getValue()).getRight()).getEContainingClass().getName();
					}
					
					if(ref.getValue() instanceof EDataTypeImpl){
						/*System.out.println(((EDataTypeImpl) ref.getValue()).getInstanceClassName());
						System.out.println(((EDataTypeImpl) ref.getValue()).getInstanceClassNameGen());
						System.out.println(((EDataTypeImpl) ref.getValue()).getInstanceTypeName());
						System.out.println(((EDataTypeImpl) ref.getValue()).getName());
						System.out.println(((EDataTypeImpl) ref.getValue()).getNameGen());
						System.out.println(((EDataTypeImpl) ref.getValue()).get);*/
						type =  ((EDataTypeImpl) ref.getValue()).getName();
						
					} else if(ref.getValue() instanceof EClassImpl){
						type = ((EClassImpl)ref.getValue()).getName();
					}
					
					if(name != ""){
						SetProperty atomicChange = new SetProperty(name, type, this.isBasicType(type));
						atomicChange.setContainer(container);
						atomicChange.setId(String.valueOf(id));
						changes.add(atomicChange);
						System.out.println("Set type of property " + atomicChange.getName() + " to " + atomicChange.getType() + " in class " + atomicChange.getContainer());
					}
					
				}
				
			} else if(diff.getKind().equals(DifferenceKind.MOVE)){
				
//				System.out.println('\n');
//				System.out.println(i +") diff " + diff);
//				System.out.println("equiv " + diff.getEquivalence());
//				System.out.println("match " + diff.getMatch());
//				System.out.println("	match " + diff.getMatch().getComparison());
//				System.out.println("	match " + diff.getMatch().getDifferences());
//				System.out.println("	match " + diff.getMatch().getSubmatches());
//				
//				
				String name = "";
				String source = "";
				String target = "";
				if(diff instanceof ReferenceChangeSpec){
					ReferenceChangeSpec ref = (ReferenceChangeSpec) diff;
					
					if(ref.getValue() instanceof EAttributeImpl){
						name = ((EAttributeImpl) ref.getValue()).getName();
						source = ((EAttributeImpl) comparison.getMatch(ref.getValue()).getRight()).getEContainingClass().getName();
						target = ((EAttributeImpl) comparison.getMatch(ref.getValue()).getLeft()).getEContainingClass().getName();
						 
					} else if(ref.getValue() instanceof EReferenceImpl){
						name = ((EReferenceImpl) ref.getValue()).getName();
						source = ((EReferenceImpl) comparison.getMatch(ref.getValue()).getRight()).getEContainingClass().getName();
						target = ((EReferenceImpl) comparison.getMatch(ref.getValue()).getLeft()).getEContainingClass().getName();
						
						
						
						
					} else if(ref.getValue() instanceof EOperationImpl){
						name = ((EOperationImpl) ref.getValue()).getName();
						source = ((EOperationImpl) comparison.getMatch(ref.getValue()).getRight()).getEContainingClass().getName();
						target = ((EOperationImpl) comparison.getMatch(ref.getValue()).getLeft()).getEContainingClass().getName();
					}
					
					//System.out.println("	match source " +  source);
					//System.out.println("	match target " +  target);
					
					
					if(name != "" & source != "" & target != ""){
						boolean checkSource = this.isStillInSource(name, source, target, initState, initStateTarget);
						boolean checkTarget = this.isStillInTarget(name, source, target, initState, initStateTarget);
						boolean checkInheritance = this.isTargetInheritingSource(name, source, target, initState, initStateTarget);
						
					//	System.out.println(checkSource + " " + checkTarget + " " + checkInheritance);
						
						//TOOD bug here the push ref is not well detected
						
						if(!checkSource & !checkTarget){
							
							DeleteProperty atomicChange0 = new DeleteProperty(name, source);
							atomicChange0.setId(String.valueOf(id));
							changes.add(atomicChange0);
							System.out.println("Deleted property " + atomicChange0.getName() + " in " +atomicChange0.getClassName());
							
							AddProperty atomicChange1 = new AddProperty(name, target);
							atomicChange1.setId(String.valueOf(id));
							changes.add(atomicChange1);
							System.out.println("Added property " + atomicChange1.getName() + " in " +atomicChange1.getClassName());
							
						} else if(!checkSource & checkTarget & checkInheritance){
								
								DeleteProperty atomicChange0 = new DeleteProperty(name, source);
								atomicChange0.setId(String.valueOf(id));
								changes.add(atomicChange0);
								System.out.println("Deleted property " + atomicChange0.getName() + " in " +atomicChange0.getClassName());
								
								AddProperty atomicChange1 = new AddProperty(name, target);
								atomicChange1.setId(String.valueOf(id));
								changes.add(atomicChange1);
								System.out.println("Added property " + atomicChange1.getName() + " in " +atomicChange1.getClassName());
								
						} else if(checkSource & !checkTarget){
							//here we add in target
							AddProperty atomicChange1 = new AddProperty(name, target);
							atomicChange1.setId(String.valueOf(id));
							changes.add(atomicChange1);
							System.out.println("Added property " + atomicChange1.getName() + " in " +atomicChange1.getClassName());
							
						} else if(!checkSource & checkTarget){
							//here we delete in source
							DeleteProperty atomicChange0 = new DeleteProperty(name, source);
							atomicChange0.setId(String.valueOf(id));
							changes.add(atomicChange0);
							System.out.println("Deleted property " + atomicChange0.getName() + " in " +atomicChange0.getClassName());
							
						} else if(checkSource & checkTarget){
							//nothing to do
						} 
					}
					
				}
				
				
			}
			
			
		}
	return changes;
	
	}
	
	private boolean isStillInSource(String name, String source, String target, InitialState initState, InitialState initStateTarget){
				
		//now for source
		Classes sourceClassV1 = initState.getOneClass(source);
		Classes sourceClassV2 = initStateTarget.getOneClass(source);
		
		if(sourceClassV1 == null || sourceClassV2 == null ||
				sourceClassV1.getProperties() == null || sourceClassV2.getProperties() == null) return false;
		for(Property prop1: sourceClassV1.getProperties()){
			
			for(Property prop2: sourceClassV2.getProperties()){
				if(prop1.getName().equals(prop2.getName()) 
						& prop1.getType().equals(prop2.getType())) return true;
			}
		}
		
		References ref1 = sourceClassV1.getOneReference(sourceClassV1.getReferences(), name);
		References ref2 = sourceClassV2.getOneReference(sourceClassV2.getReferences(), name);
		
		if(ref1 == null || ref2 == null) return false;
		
		if(ref1.getName().equals(ref2.getName())
				& ref1.getTargetClass().getName().equals(ref2.getTargetClass().getName())){
			return true;
		}
		
		return false;
	}
	
	private boolean isStillInTarget(String name, String source, String target, InitialState initState, InitialState initStateTarget){
		
		//now for target
		Classes targetClassV1 = initState.getOneClass(target);
		Classes targetClassV2 = initStateTarget.getOneClass(target);
		
		if(targetClassV1 == null || targetClassV2 == null ||
				targetClassV1.getProperties() == null || targetClassV2.getProperties() == null) return false;
		for(Property prop1: targetClassV1.getProperties()){
			
			for(Property prop2: targetClassV2.getProperties()){
				if(prop1.getName().equals(prop2.getName()) 
						& prop1.getType().equals(prop2.getType())) return true;
				
			}
		}
			
		References ref3 = targetClassV1.getOneReference(targetClassV1.getReferences(), name);
		References ref4 = targetClassV2.getOneReference(targetClassV2.getReferences(), name);
		
		if(ref3 == null || ref4 == null) return false;
		
		if(ref3.getName().equals(ref4.getName())
				& ref3.getTargetClass().getName().equals(ref4.getTargetClass().getName())){
			return true;
		}
		
		return false;
	}
	
	private boolean isTargetInheritingSource(String name, String source, String target, InitialState initState, InitialState initStateTarget){
		
		//here we test in V2 is the target inherits the source, for the case of push property
		//Classes targetClassV1 = initState.getOneClass(target);
		Classes targetClassV2 = initStateTarget.getOneClass(target);

		//Classes sourceClassV1 = initState.getOneClass(source);
		Classes sourceClassV2 = initStateTarget.getOneClass(source);
		
		if(targetClassV2.getInheritances().contains(sourceClassV2)) return true;
			
		//for(Classes ihc : targetClassV1.getInheritances()){	}
				
		return false;
	}
	
	private boolean isBasicType(String change) {
		
		if(change == null) return false; 
		else return change.equalsIgnoreCase("int") || change.equalsIgnoreCase("boolean") 
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