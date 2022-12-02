package fr.lip6.meta.tracerecorder.listner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EDataTypeImpl;
import org.eclipse.emf.ecore.impl.EEnumImpl;
import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl;
import org.eclipse.emf.ecore.impl.EcoreFactoryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.internal.impl.PrimitiveTypeImpl;

import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.tracerecorder.utils.EMFUtils;
import fr.lip6.meta.tracerecorder.views.PraxisTraceRecorder;


public class ActionListener implements Adapter {

	private Set<EObject> knownObjects = new HashSet<EObject>();

	// remembers the object that is being moved
	public Object objectBeingMoved = null;

	private Set<Resource> resources;
	private Resource resource;
	private SequenceListener listener;

	private Notifier target;
	
	public boolean isEcore = false;
	
	private Hashtable<EObject,String> ids = new Hashtable<EObject,String>();//the delete case is not treated, whether to remove the object of not, but it should be null, since the object does not exist anymore//
	
	private PraxisTraceRecorder praxisTraceRecorder = null;
	
	private ArrayList<String> praxisTrace = null;
	
	private InitialState initState = null;
	
	private String pathFile = "";
	
	private boolean initialization = false;
	
	private File fileTrace = null;
	
	public ArrayList<String> getPraxisTrace(){
		return this.praxisTrace;
	}
	
	public InitialState getInitState(){
		return this.initState;
	}
	
	public ActionListener(Set<Resource> resourceSet, SequenceListener listener, boolean isEcore, PraxisTraceRecorder praxisTraceRecorder, File fileTrace) {
		this.listener = listener;
		this.resources = resourceSet;
		this.isEcore = isEcore;
		this.praxisTraceRecorder = praxisTraceRecorder;
		this.praxisTrace = new ArrayList<String>(); 
		this.fileTrace = fileTrace;
		
		for (Resource resource : resourceSet) {
			this.resource = resource;
			//if(isEcore){}//we do not test here if it is Ecore ou UML CD, we leave it to the initial state
				initState = new InitialState(this.resource);
				initState.initialize(isEcore);
				
			System.out.println("	path = "+this.resource.getURI().path());
			System.out.println("	toString = "+this.resource.getURI().toString());
			System.out.println("	toFileString = "+this.resource.getURI().toFileString());
			System.out.println("	toPlatformString false = "+this.resource.getURI().toPlatformString(false));
			System.out.println("	toPlatformString true = "+this.resource.getURI().toPlatformString(true));
			System.out.println("	authority = "+this.resource.getURI().authority());
			System.out.println("	device = "+this.resource.getURI().device());
			System.out.println("	devicePath = "+this.resource.getURI().devicePath());
			this.pathFile = this.resource.getURI().toPlatformString(true);
			File fTemp = new File(this.pathFile+".txt");
			
			for (Iterator<EObject> iterator = resource.getAllContents(); iterator
					.hasNext();) {
				EObject obj = iterator.next();
				//System.out.println("obj initState = "+obj);
				if(obj.toString().split(" ")[0].split(".impl.").length < 1){
					System.out.println("	obj initState [0] = "+obj.toString().split(" ")[0].split(".impl.")[0]);
					System.out.println("	obj initState [1] = "+obj.toString().split(" ")[0].split(".impl.")[1]);
				} else System.out.println("	else obj initState = "+obj);
				
				if(obj.toString().split(" ")[0].split(".impl.").length>1) // because ennum elements are just represented with their names
					this.ids.put(obj, obj.toString().split(" ")[0].split(".impl.")[1]);
			}
		}
		
		for (Resource resource : resourceSet) {			
			for (Iterator<EObject> iterator = resource.getAllContents(); iterator
					.hasNext();) {
				EObject obj = iterator.next();
				//if(obj.toString().split(" ")[0].split(".impl.").length>1 || obj.toString().contains("impl"))  // because ennum elements are just represented with their names
					generateCreationActions(obj);
			}
		}
		this.flushAction("@Start_Evolution_Trace_Praxis.");
		this.initialization = true; 
		//listener.initializationFinished();
	}
	

	private boolean isInObservedResources(EObject obj) {
		return resources.contains(obj.eResource());
	}
	
	@Override
	public Notifier getTarget() {
		return this.target;
	}

	@Override
	public void setTarget(Notifier newTarget) {
		this.target = newTarget;
	}

	@Override
	public boolean isAdapterForType(Object type) {
		return EMFUtils.isInterrestingObject(type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void notifyChanged(Notification notification) {
		if (notification.getEventType()==Notification.REMOVING_ADAPTER) return;
		
		this.flushAction("@Start_Notification.");
		
		try{
			switch (notification.getEventType()) {
			case Notification.ADD: {
				if(notification.getFeature() == null){
					break;
				}
				/*if(notification.getNewValue() instanceof EClassImpl){
					EClassImpl obj = (EClassImpl) notification.getNewValue();
					//EStructuralFeature f = new EStructuralFeature();
					for(EStructuralFeature i : obj.getEStructuralFeatures()){
						 System.out.println("** " + i);
					}System.out.println("** " + obj.getEStructuralFeatures().size());
					//obj.setClassifierID(EcoreUtil.generateUUID());
					//obj.eClass().ge
					EcoreFactoryImpl f = new EcoreFactoryImpl();
					//EStructuralFeatureImpl s = new EStructuralFeatureImpl();
					EcoreUtil.setID(obj, EcoreUtil.generateUUID());
				} else if(notification.getNewValue() instanceof EAttributeImpl){
					EAttributeImpl obj = (EAttributeImpl) notification.getNewValue();
					//obj.setClassifierID(EcoreUtil.generateUUID());
					EcoreUtil.setID(obj, EcoreUtil.generateUUID());
				}*/
				//EOperationImpl
				//EReferenceImpl
						
				EObject newEobj=(EObject)notification.getNewValue();

				if(!knownObjects.contains(newEobj)){
					generateCreationActions(newEobj);
				}

				//are you sure it's a REF ?
				if(notification.getFeature() instanceof EReference){
					EObject added = null;

					//if it was a list we need to find the difference
					if(notification.getOldValue() != null && notification.getNewValue() instanceof EList && notification.getOldValue() instanceof EList){
						EList<EObject> old = (EList<EObject>) notification.getOldValue();
						EList<EObject> now = (EList<EObject>) notification.getNewValue();
						for (EObject ob : now) {
							if(!old.contains(ob))
								added = ob;
						}
						//else it is trivial
					}else{
						added = (EObject) notification.getNewValue();
					}

					generateAddRef((EObject)notification.getNotifier(),(EReference) notification.getFeature(), added);
				}else{
					System.err.println("Add with no ref from EMF ????");
				}

				if (objectBeingMoved != null) {
					// a MOVE action is composed of a REMOVE and a ADD actions,
					// after the ADD action it is thus finished
					objectBeingMoved = null;
				}
				break;
			}

			case Notification.REMOVE: {
				//FIXME not working because we lose the XMI ID !

				//are you sure it's a REF ?
				if(notification.getFeature() instanceof EReference){
					
					generateDeleteActions((EObject)notification.getOldValue());
					
					EObject removed = null;

					//if it was a list we need to find the difference
					if(notification.getNewValue() != null && notification.getNewValue() instanceof EList && notification.getOldValue() instanceof EList){
						EList<EObject> old = (EList<EObject>) notification.getOldValue();
						EList<EObject> now = (EList<EObject>) notification.getNewValue();
						for (EObject ob : old) {
							if(!now.contains(ob))
								removed = ob;
						}
						//else it is trivial
					}else{
						removed = (EObject) notification.getOldValue();
					}

					generateRemRef((EObject)notification.getNotifier(),(EReference) notification.getFeature(),removed);
					
					if (objectBeingMoved == null) {
						// delete actions are added only if we ARE NOT in the middle of a move operation!																	
						//storage.actionsCur.addAll(ActionGeneration.delete(removed , storage,ownerUUID));					
					}
				}else{
					System.err.println("REM with no ref from EMF ????");
				}

				break; }

			case Notification.SET: {
				if(notification.getFeature() instanceof EAttribute){
					//it is a property change

					EAttribute at = (EAttribute) notification.getFeature();
					EObject victim = (EObject) notification.getNotifier();
					generateChangeProp(victim, at, notification.getOldValue(), notification.getNewValue());

				}else{
									
					//it's a reference
					EReference ref = (EReference) notification.getFeature();
					EObject victim = (EObject) notification.getNotifier();
					Object oldval = notification.getOldValue();
					Object target = notification.getNewValue();

					/*This is a messy hack, just because EMF seems to suck real bad.
						if(target == null && !storage.hmapHashcode2UUIDDistant.containsKey(oldval.hashCode())){
							// the new value is null and the old value we dont know, means it's an addRef
							target = oldval;
							oldval = null;
						}
					 */

					/*
					 * maurelio1234 - 15092010
					 * 
					 * This code has been simplified in order to avoid a bug found in the handling of old and new values
					 * The complexity is in the fact that a reference may either be a single object (possibly null for no value)
					 * or an  EList of objects (possibly null or empty for no value). 
					 * 
					 * The new method getEList(Object) encapsulates this complexity by constructing an EList that is equivalent
					 * to the original value. 
					 * 
					 */
					
					EList<EObject> old = getEList(oldval);
					EList<EObject> nnn = getEList(target);

					// to remove: every element in old that is not in new
					for (EObject obj : old) {
						if (!nnn.contains(obj)) {
							generateRemRef(victim, ref, obj);
						}
					}

					// to add: every element in new that was not in old
					for (EObject obj : nnn) {
						if (!old.contains(obj)) {
							generateAddRef(victim, ref, obj);
						}
					}
				}
				break; 
			}

			case Notification.MOVE: {
				objectBeingMoved = notification.getOldValue();				
				break;
			}
		} 

		} catch(Throwable t) {
			t.printStackTrace();
		}
		
		this.flushAction("@End_Notification.");
	}

	@SuppressWarnings("unchecked")
	private EList<EObject> getEList(Object val) {
		if (val instanceof EList<?>) {
			return (EList<EObject>) val;
		} else {
			if (val != null) {
				return new BasicEList<EObject>(Arrays.asList(new EObject[]{(EObject) val}));				
			} else {
				return new BasicEList<EObject>();				
			}
		}
	}

	private void generateChangeProp(EObject victim, EAttribute at,
			Object oldval, Object newval) {
		String id = "null";
		/*if(isEcore){
			id= this.ids.get(victim);
		} else {
			id = EMFUtils.extractPrologID(victim);
		}*/
		id= this.ids.get(victim);
		

		//Same (dumass)
		if (oldval!=null && newval!=null && oldval.toString().equals(newval.toString())) {
			return;
		}

		//old val rem
		if (oldval!=null && !oldval.toString().equals("")) {
			sendAction("remProperty("+id+"," + at.getName().toLowerCase() + ", '" + truncatePropString(oldval.toString()) + "'," + Clock.getNextTimestamp() +").");
		}

		// new val add
		if (newval!=null && !newval.toString().equals("")) {
			sendAction("addProperty("+id+"," + at.getName().toLowerCase() + ", '" + truncatePropString(newval.toString()) + "'," + Clock.getNextTimestamp() +").");
		}

		//derivation
		// THIS CODE WAS REMOVED FOR NOW!
	}

	// property values are trucated when they are too long
	// because they are usually uninteresting in prolog
	private String truncatePropString(String string) {
		return string.subSequence(0, Math.min(32, string.length())).toString();
	}

	private void generateRemRef(EObject victim, EReference at,
			EObject change) {

		String id = "null";//EMFUtils.extractPrologID(victim);
		String trg = "null";//EMFUtils.extractPrologID(change);
		/*if(isEcore){
			id= this.ids.get(victim);
			if(change instanceof EDataTypeImpl){
				trg = ((EDataTypeImpl) change).getInstanceClassName();
			} else{
				trg = this.ids.get(change);
			}
		} else {
			id = EMFUtils.extractPrologID(victim);
			trg = EMFUtils.extractPrologID(change);
		}*/
		
		id= this.ids.get(victim);
		if(change instanceof EDataTypeImpl){
			trg = ((EDataTypeImpl) change).getInstanceClassName();
		} else if(change instanceof PrimitiveTypeImpl){
			trg = ((PrimitiveTypeImpl) change).getName();
		} else{
			trg = this.ids.get(change);
		}
		// the addref
		sendAction("remReference("+id+"," + at.getName().toLowerCase() + "," + trg + "," + Clock.getNextTimestamp() +").");

		//derivation
		// THIS CODE WAS REMOVED FOR NOW!
		
	}

	private void generateAddRef(EObject victim, EReference at,
			EObject change) {

		String id = "null";//EMFUtils.extractPrologID(victim);
		String trg = "null";//EMFUtils.extractPrologID(change);
		/*if(isEcore){
			id= this.ids.get(victim);
			if(change instanceof EDataTypeImpl){
				trg = ((EDataTypeImpl) change).getInstanceClassName();
			} else{
				trg = this.ids.get(change);
			}
		} else {
			id = EMFUtils.extractPrologID(victim);
			trg = EMFUtils.extractPrologID(change);
		}*/
		
		id= this.ids.get(victim);
		if(change instanceof EDataTypeImpl){
			trg = ((EDataTypeImpl) change).getInstanceClassName();
		} else if(change instanceof PrimitiveTypeImpl){
			trg = ((PrimitiveTypeImpl) change).getName();
		} else{
			trg = this.ids.get(change);
		}
		// the addref
		sendAction("addReference("+id+"," + at.getName().toLowerCase() + "," + trg + "," + Clock.getNextTimestamp() +").");

		//derivation
		// THIS CODE WAS REMOVED FOR NOW!
	}
	
	private void generateDeleteActions(EObject oldEobj) {
		
		String uuid = "null";//EMFUtils.extractPrologID(newEobj);
		
		/*if(isEcore){
			uuid= this.ids.get(oldEobj);
		} else {
			uuid = EMFUtils.extractPrologID(oldEobj);
		}*/
		
		uuid= this.ids.get(oldEobj);
		
		EObject victim = oldEobj;
		sendAction("delete("+uuid+"," + victim.eClass().getName().toLowerCase() + "," + Clock.getNextTimestamp() +").");
		
	}

	@SuppressWarnings("unchecked")
	private void generateCreationActions(EObject newEobj) {
		if (!knownObjects.contains(newEobj) && isInObservedResources(newEobj)) {
			knownObjects.add(newEobj);
			//System.out.println("		**** newEobj "+newEobj);
			if(newEobj != null && newEobj.toString().split(" ")[0].split(".impl.").length>1){ // because ennum elements are just represented with their names
				this.ids.put(newEobj, newEobj.toString().split(" ")[0].split(".impl.")[1]);
				//Check add ennum literal, it gives null pointer exception
			}
			newEobj.eAdapters().add(this);
			String uuid = "null";//EMFUtils.extractPrologID(newEobj);
			
			/*if(isEcore){
				uuid= this.ids.get(newEobj);
			} else {
				uuid = EMFUtils.extractPrologID(newEobj);
			}*/
			
			uuid= this.ids.get(newEobj);
			
			EObject victim = newEobj;
			
			sendAction("create("+uuid+"," + victim.eClass().getName().toLowerCase() + "," + Clock.getNextTimestamp() +").");
			
			//generation of attributes is safe
			for (EAttribute f : victim.eClass().getEAllAttributes()) {

				if(victim.eGet(f) != null && victim.eGet(f).toString() != "" ){
					String newvalue = victim.eGet(f).toString();
					generateChangeProp(victim, f, null, newvalue);					
				}
			}

			//generation of all references is a bit tricky
			for(EReference r : victim.eClass().getEAllReferences()){
				// we dont want the EMF stuffs !
				//if(!r.getName().startsWith("e") && !r.getName().contains("EObject")){
					Object v = victim.eGet(r);
					if (v instanceof EObject && v != null) {
						EObject referenced = (EObject) v;						
						generateCreationActions(referenced);
						generateAddRef(victim, r, referenced);
					} else if (v instanceof EList<?>) {
						EList<EObject> referenceds = (EList<EObject>) v;
						if(referenceds != null && referenceds.size() !=0) {
							for (Iterator<EObject> it = referenceds.iterator() ; it.hasNext() ; ) {
								EObject referenced = it.next();
								generateCreationActions(referenced);
								generateAddRef(victim, r, referenced);								
							}							
						}
					}
				//}
			}
		}
	}

	private void sendAction(String action) {
		//here instead of calling a listner to first put te trace in a file than make other treatment such as prolog checking
		//just record the trace
		this.flushAction(action);
		//listener.sendAction(action);
	}
	
	
	private void flushAction(String ac) {
		
		this.praxisTrace.add(ac);
		if(this.initialization){//here because during the initialization "var = new ActionListner()" in the new we us var, which causes a null pointer 
			this.praxisTraceRecorder.updatePraxisTraceList(ac);
		}
		
		try {
			String trace ="C:/Users/khelladi/Documents/eclipse modeling juno/workspaceCompare/TraceRecorder/Evaluationtrace.txt";
			//String trace ="C:/Users/khelladi/Documents/eclipse modeling juno/workspaceCompare/TraceRecorder/trace.txt"; 
					//"C:/Users/khelladi/Documents/PraxisWorkspace/ObserveR/trace.txt";
			
			
			
			PrintStream ps = new PrintStream(new FileOutputStream(this.fileTrace, true));
			ps.println(ac);
			ps.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*try {
			String trace =this.pathFile+".txt"; 
					//"C:/Users/khelladi/Documents/PraxisWorkspace/ObserveR/trace.txt";
			PrintStream ps = new PrintStream(new FileOutputStream(trace, true));
			ps.println(ac);
			ps.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}
}
