package fr.lip6.meta.tracerecorder.utils;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;

public class EMFUtils {
	private static final String UNKNOWN_ID = "unknown";

	/**
	 * @param obj the Eobject from which you want to exctract the XMI ID
	 * @return The ID as a String
	 * @throws NullPointerException if the Eobject has no resource (it is deleted)
	 */
	public static String extractXMIID(EObject obj) throws NullPointerException{
		XMLResource res =  ((XMLResource) obj.eResource());
		String id =null;

		if (res == null) {
			// if the object could not be found try to find it among the deleted ones
			id = extractXMIIDDeletedObject(obj);
		}else{
			id = res.getID(obj);
		}

		if(id == null){
			return UNKNOWN_ID;
		}
		return id;
	}

	public static String extractPrologID(EObject obj) {
		return printID(extractXMIID(obj))+"_"+obj.getClass().getSimpleName();
	}
	
	/**
	 * HACK: the XMI ids of deleted objects are cached into its previous XMLResource in a static protected attribute,
	 * this hack allows us to access this attribute and get the id of the object
	 */
	private static class MyXMLResource extends XMLResourceImpl {
		public static String getXMIIDDeletedObject(EObject obj) {
			return MyXMLResource.DETACHED_EOBJECT_TO_ID_MAP.get(obj);
		}
	}

	private static String extractXMIIDDeletedObject(EObject obj){
		String stid = MyXMLResource.getXMIIDDeletedObject(obj);	
		return stid;
	}

	public static boolean isInterrestingObject(Object o) {
		return isInterresting(o, new String[] { "org.eclipse.uml2.uml.internal." });
	}

	public static boolean isInterresting(Object o, String[] interrestingArray) {
		if (o == null)
			return false;
		String name = o.getClass().getCanonicalName();
		return isInterresting(name, interrestingArray);
	}

	private static boolean isInterresting(String name,
			String[] interrestingArray) {
		for (String p : interrestingArray) {
			if (name.contains(p)) {
				return true;
			}
		}
		return false;
	}

	public static String printID(String xmiID){
		return "xmiid"+xmiID.toString().replace("-", "MxM").replace(".", "_pt_")+"xmiid";
	}

	public static String translatePrologIDToXMIID(String id){
		String trunk = id.substring(5, id.length() -5); //first and final xmiid
		String uid = trunk.replace("MxM", "-").replace("_pt_", ".");
		return uid;
	}
	
	public static boolean isPrologID(String id){
		return id.startsWith("xmiid")&& id.endsWith("xmiid");
	}

}
