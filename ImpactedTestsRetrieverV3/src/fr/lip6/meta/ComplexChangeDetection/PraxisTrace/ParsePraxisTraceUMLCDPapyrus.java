package fr.lip6.meta.ComplexChangeDetection.PraxisTrace;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Map.Entry;

public class ParsePraxisTraceUMLCDPapyrus extends ParsePraxisTrace{
			

	/*private File fileTrace = null;
	private ArrayList<String> parsedTrace = null;
	private ArrayList<String> praxisTrace = null;
	
	public ArrayList<String> getParsedTrace() {
		return parsedTrace;
	}

	public void setParsedTrace(ArrayList<String> parsedTrace) {
		this.parsedTrace = parsedTrace;
	}

	public ArrayList<String> getPraxisTrace() {
		return praxisTrace;
	}

	public void setPraxisTrace(ArrayList<String> praxisTrace) {
		this.praxisTrace = praxisTrace;
	}
	
	public File getFileTrace() {
		return fileTrace;
	}

	public void setFileTrace(File fileTrace) {
		this.fileTrace = fileTrace;
	}*/

	public ParsePraxisTraceUMLCDPapyrus(File fileTrace) {
		super(fileTrace);
		//this.fileTrace = fileTrace;
		//this.parsedTrace = new ArrayList<String>();
	}
	
	public ParsePraxisTraceUMLCDPapyrus(ArrayList<String> praxisTrace) {
		super(praxisTrace);
		//this.praxisTrace = praxisTrace;
		//this.parsedTrace = new ArrayList<String>();
	}
	
	@Override
	public void parseTrace() {//here treat the UML CD trace
		
		String result = "";
		ArrayList<ArrayList<String> > ordered = new ArrayList<ArrayList<String> >();
		ArrayList<String> firstPass = new ArrayList<String>();
		ArrayList<String> initTrace = new ArrayList<String>();
		ArrayList<String> notRename = new ArrayList<String>();
		ArrayList<String> notAdd = new ArrayList<String>();
		ArrayList<String> notDelete = new ArrayList<String>();
		Hashtable<String, String> initTable = new Hashtable<String, String>();
		Hashtable<String, String> containTable = new Hashtable<String, String>();
		if(this.fileTrace != null){
			try {
				Scanner scanner = new Scanner(this.fileTrace);// new
																// File("C:/Users/khelladi/Documents/Previous Desktop/marcos/finalProtos/MagicDraw/OBEO/src/test.txt"));
				String line = "";
				// StringBuffer str = new StringBuffer();
				// result = scanner.nextLine();
				boolean find = false;
				while (scanner.hasNextLine()) {
					line = scanner.nextLine();
					
					if (line != null && line != "" && !find) {
						initTrace.add(line);
						if(line.equals("@Start_Evolution_Trace_Praxis.")){
							find = true;
						}
					}
					
					if (line != null && line != "" && find) {
						firstPass.add(line);//initTrace
					}
					
				}
				scanner.close();
	
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(this.praxisTrace != null){
			
			boolean find = false;
			for(String praxis : this.praxisTrace){
				if (praxis != null && praxis != "" && !find) {
					initTrace.add(praxis);
					if(praxis.equals("@Start_Evolution_Trace_Praxis.")){
						find = true;
					}
				}
				
				if (praxis != null && praxis != "" && find) {
					firstPass.add(praxis);//initTrace
				}
			}
			
		}
			//here retrieve first the tuple (id, name) and then the tuple (id, id-container)
			initTable = this.initializeTable(initTrace);
			//intialize another table containementTable while looking into addReference(...)
			containTable = this.initializeContainTable(initTrace);
			
			Iterator<Entry<String, String>> it = initTable.entrySet().iterator();
			while(it.hasNext()){
				Entry<String, String> o = it.next();
				System.out.println("key = "+o.getKey());
				System.out.println("value = "+o.getValue());
			}
			//here we retrieve the lists praxis traces for the atomic changes
			ordered = this.orderList(firstPass);
			System.out.println("		ordered  ");
			this.displayOrdered(ordered);
			
			int i = 0;
			int j = 0;
			while(i < ordered.size()){
				ArrayList<String> element = ordered.get(i);
				
				/**/
				//System.out.println("key = "+element.getKey());
				if(element.size() == 2 && this.getKind(element.get(0)).equals("remProperty")
						&& this.getKind(element.get(1)).equals("addProperty")
						&& this.getId(element.get(0)).equals(this.getId(element.get(1)))
						&& !notRename.contains(element.get(0))//here we check that it is really a rename and not the first given name when the element is created
						&& !notRename.contains(element.get(1))//same here
						){//it is then a rename, or a change, for now we treat lower/upper bound change
					
					this.treatChangeProp(element, initTable, containTable);
					
				} else if(element.size() > 0 && 
						(this.getKind(element.get(0)).equals("create")
								//|| this.getKind(element.get(0)).equals("addReference")//treated afterward
								)){//here treat add class, property (att, ref, op), superType and parameter
					
					this.treatAdd(element, ordered, i, initTable, notRename, containTable);
				
				} else if(element.size() == 1 && this.getKind(element.get(0)).equals("remReference")){// for set type to null
					
					// if there is only a remReference ; we may be delete the info in initTable also, which i do not do for know!
					this.treatDelete(element, ordered, i, initTable, notDelete);
					
				} else if(element.size() == 2 && 
						this.getKind(element.get(0)).equals("delete")
						&& !notDelete.contains(initTable.get(this.getId(element.get(0)))+//here we ensure to not put a delete element twice in the trace
								", "+initTable.get(getId(element.get(1)))+
								", "+ this.getId(element.get(0))+
								", "+ this.getId(element.get(1)))){

					this.treatDelete(element, ordered, i, initTable, notDelete);
					
				} else if(element.size() == 1 && this.getKind(element.get(0)).equals("addReference")
						&& !notAdd.contains(initTable.get(this.getSecondID(element.get(0)))+//here we ensure to not put an add element twice in the trace
								", "+initTable.get(getId(element.get(0)))+
								", "+ this.getSecondID(element.get(0))+
								", "+ this.getId(element.get(0)))){//here part of move proeprty etc
					
					// if there is only a remReference ; we may be delete the info in initTable also, which i do not do for know!
					this.treatChangeRef(element, ordered, i, initTable, notDelete, notAdd);
					
				} else if(element.size() == 2 
						&& this.getKind(element.get(0)).equals("remReference")
						&& this.getKind(element.get(1)).equals("addReference")
						&& this.getId(element.get(0)).equals(this.getId(element.get(1)))
						&& !notAdd.contains(initTable.get(this.getSecondID(element.get(0)))+//here we ensure to not put an add element twice in the trace
								", "+initTable.get(getId(element.get(0)))+
								", "+ this.getSecondID(element.get(0))+
								", "+ this.getId(element.get(0)))
						&& !notDelete.contains(initTable.get(this.getId(element.get(0)))+//here we ensure to not put a delete element twice in the trace
						", "+initTable.get(getId(element.get(1)))+
						", "+ this.getId(element.get(0))+
						", "+ this.getId(element.get(1)))){// here we move association property, change type
					
					this.treatChangeRef(element, ordered, i, initTable, notDelete, notAdd);
				}
				/*
				 * && !notAdd.contains(initTable.get(this.getId(element.get(0)))+//here 
								", "+initTable.get(getId(element.get(1)))+
								", "+ this.getId(element.get(0))+
								", "+ this.getId(element.get(1)))
				*/
				
				//add/delete super type, change multiplicity
				
			
			i++;
			}
			for(String uneTrace : this.parsedTrace){
				System.out.println("	************** One Trace = "+uneTrace);
			}
	
		
	}
	
	

	//this function retrieve for already created elements the tuple (id, name) for each element 
	private Hashtable<String, String> initializeTable(ArrayList<String> initTrace) {
		// TODO Auto-generated method stub
		Hashtable<String, String> result = new Hashtable<String, String>();
		int i = 0;
		while(i < initTrace.size()){
			//System.out.println("	zehah = "+trace);
			if(this.getKind(initTrace.get(i)).equals("create") && !this.getId(initTrace.get(i)).contains("LiteralIntegerImpl") && i + 1 < initTrace.size()){
				if(this.getId(initTrace.get(i)).contains("GeneralizationImpl")){
					result.put(this.getId(initTrace.get(i)), "");
				} else{
					result.put(this.getId(initTrace.get(i)), this.getNameInChange(initTrace.get(i + 1)));
				}
				
			}
			i++;
		}
		return result;
	}
	
	//this function retrieve for already created elements their "container" the tuple (id-element, id-container) for each element
	private Hashtable<String, String> initializeContainTable(ArrayList<String> initTrace) {
		// TODO Auto-generated method stub
		Hashtable<String, String> result = new Hashtable<String, String>();
		int i = 0;
		while(i < initTrace.size()){
			//System.out.println("	zehah = "+trace);
			if(this.getKind(initTrace.get(i)).equals("addReference") 
					&& !this.getId(initTrace.get(i)).contains("LiteralIntegerImpl")
					&& this.getSingleName(initTrace.get(i)).equals("owner")){//normally owner cover all cases
				result.put(this.getId(initTrace.get(i)), this.getSecondID(initTrace.get(i)));
			}
			i++;
		}
		return result;
	}

	private String getKind(String change) {
		return (change.split("\\(")[0]);
	}

	private String getId(String change) {
		return (change.split("\\(")[1].split(",")[0]);
	}

	private String getSingleName(String change) {
		return (change.split("\\(")[1].split(",")[1]);
				//(change.split("\\(")[1].split("\\)")[0]);
	}
	
	private String getNameInChange(String change) {
		String temp = change.split("'")[1];
		//temp.replaceAll("\\(", "");//here in case the name contains '( )'
		//temp.replaceAll("\\)", "");
		return (temp.replaceAll("\\(", "").replaceAll("\\)", ""));
	}
	private String getSecondID(String change) {
		String ID = "";
		ID = (change.split("\\(")[1].split("\\)")[0]).split(",")[2];
		return ID;
	}
	
	public void displayOrdered(ArrayList<ArrayList<String> > ordered){
		Iterator<ArrayList<String>> it = ordered.iterator();
		while(it.hasNext()){
			ArrayList<String> element = it.next();
			System.out.println("size = "+element.size());
			System.out.println("elements = "+element);
		}
	}
	
	//here we take the evolution praxis trace, and we construct a list of lists, where each element is a list of praxis trace that combine a single atomic change
	public ArrayList<ArrayList<String> > orderList(ArrayList<String> firstPass){
		int i = 0;
		int k = 0;
		ArrayList<ArrayList<String> > ordered = new ArrayList<ArrayList<String> >();
		while( i < firstPass.size()){
			if(firstPass.get(i).equals("@Start_Notification.")){
				int j = i + 1;
				ArrayList<String> temp = new ArrayList<String>();
				while(j < firstPass.size() && !firstPass.get(j).equals("@End_Notification.")){
					temp.add(firstPass.get(j));
					j++;
				}
				ordered.add(temp);
				i = j;// ou + 1;
			}
			i++;
		}
		return ordered;
	}
	
	

	private void treatChangeProp(ArrayList<String> element, Hashtable<String, String> initTable, Hashtable<String, String> containTable){
		
		if(this.getSingleName(element.get(0)).equals("name")
				&& this.getSingleName(element.get(1)).equals("name")){
			
			//TODO When an element is renamed, it must be renamed in the table of ids, otherwise it poses problemes
			
			if(this.getId(element.get(0)).contains("ClassImpl")){
				//here create new RenameClass(id, oldvalue, newValue);
				this.parsedTrace.add("RenameClass("+this.getNameInChange(element.get(0))+
						", "+this.getNameInChange(element.get(1))+
						", "+ this.getId(element.get(0))+
						", "+initTable.get(containTable.get(this.getId(element.get(0))))+
						", "+containTable.get(this.getId(element.get(0)))+");");
			} else if(this.getId(element.get(0)).contains("OperationImpl")
					|| this.getId(element.get(0)).contains("PropertyImpl")
					|| this.getId(element.get(0)).contains("AssociationImpl")){// in UML without "E" and add PropertyImpl, but WEIRD fact: you cannot rename a reference 
				//here create new RenameProperty(id, oldvalue, newValue);
				//EOperationImpl
				//EReferenceImpl
				//EAttributeImpl
				this.parsedTrace.add("RenameProperty("+this.getNameInChange(element.get(0))+
						", "+this.getNameInChange(element.get(1))+
						", "+ this.getId(element.get(0))+
						", "+initTable.get(containTable.get(this.getId(element.get(0))))+
						", "+containTable.get(this.getId(element.get(0)))+");");
				
			} else if(this.getId(element.get(0)).contains("ParameterImpl")){// 
				this.parsedTrace.add("RenameParameter("+this.getNameInChange(element.get(0))+
						", "+this.getNameInChange(element.get(1))+
						", "+ this.getId(element.get(0))+
						", "+initTable.get(containTable.get(this.getId(element.get(0))))+
						", "+containTable.get(this.getId(element.get(0)))+");");
				
			} else if(this.getId(element.get(0)).contains("EnumerationImpl")){//in UML EnumerationImpl EnumerationLiteralImpl
				//EDataTypeImpl
				//EEnumImpl, for ennum literal, there is not class for it it is a value.
			}
		} else if(this.getSingleName(element.get(0)).equals("LiteralIntegerImpl")
				&& this.getSingleName(element.get(1)).equals("LiteralIntegerImpl")){//here it is not enoough, we need to associate the literal with its property, otherwise it does not work
			
			this.parsedTrace.add("ChangeLowerBound("+initTable.get(this.getId(element.get(0)))+
					", "+this.getNameInChange(element.get(0))+
					", "+this.getNameInChange(element.get(1))+
					", "+ this.getId(element.get(0))+//");");
					", "+initTable.get(containTable.get(this.getId(element.get(0))))+
					", "+containTable.get(this.getId(element.get(0)))+");");
			
		} else if(this.getSingleName(element.get(0)).equals("LiteralUnlimitedNaturalImpl")
				&& this.getSingleName(element.get(1)).equals("LiteralUnlimitedNaturalImpl")){//here it is not enoough, we need to associate the literal with its property, otherwise it does not work
			
			this.parsedTrace.add("ChangeUpperBound("+initTable.get(this.getId(element.get(0)))+
					", "+this.getNameInChange(element.get(0))+
					", "+this.getNameInChange(element.get(1))+
					", "+ this.getId(element.get(0))+//");");
					", "+initTable.get(containTable.get(this.getId(element.get(0))))+//here we get the name of the container
					", "+containTable.get(this.getId(element.get(0)))+");");//here we get the id of the container
			
		} else if(this.getSingleName(element.get(0)).equals("econtainingclass")
				&& this.getSingleName(element.get(1)).equals("econtainingclass")){//the values are for ecore, 
			
			this.parsedTrace.add("NotTreated!!!("+initTable.get(this.getId(element.get(0)))+
					", "+this.getNameInChange(element.get(0))+
					", "+this.getNameInChange(element.get(1))+
					", "+ this.getId(element.get(0))+");");
		} 
		
	}
	
	private void treatAdd(ArrayList<String> element, ArrayList<ArrayList<String> > ordered, int i, Hashtable<String, String> initTable, ArrayList<String> notRename, Hashtable<String, String> containTable) {
		
		/*if(this.getKind(element.get(0)).equals("addReference")){
			
			
			if(this.getSingleName(element.get(0)).equals("esupertypes")){//TODO GeneralizationImpl -> treat it in the create okay!!
				this.parsedTrace.add("AddSuperType("+initTable.get(this.getId(element.get(0)))+
						", "+initTable.get(this.getSecondID(element.get(0)))+
						", "+ this.getId(element.get(0))+
						", "+ this.getSecondID(element.get(0))+");");
			}
			
			
			if(this.getSingleName(element.get(0)).equals("ownedattribute")){
				this.parsedTrace.add("AddProperty("+initTable.get(this.getId(element.get(0)))+
						", "+initTable.get(getSecondID(element.get(0)))+
						", "+ this.getId(element.get(0))+
						", "+ this.getSecondID(element.get(0))+");");
			}
			
			if(this.getSingleName(element.get(0)).equals("type")){
				this.parsedTrace.add("SetProperty("+initTable.get(this.getId(element.get(0)))+
						", "+initTable.get(getSecondID(element.get(0)))+//here deal with basic types: int double ect
						", "+ this.getId(element.get(0))+
						", "+ this.getSecondID(element.get(0))+");");
			}
			
			
		} else*/ 
		if(this.getKind(element.get(0)).equals("create")){
			
			if(!this.getId(element.get(0)).contains("ParameterImpl")
					&& !this.getId(element.get(0)).contains("AssociationImpl")
					&& !this.getId(element.get(0)).contains("GeneralizationImpl")){//we first deal with EClassImpl, EAttributeImpl, EOperationImpl
				
				if(this.getId(element.get(0)).contains("PropertyImpl") 
						&& this.getSingleName(element.get(1)).equals("name")){//here we deal with the case when an association is added that result in a particular add of a property before that contains the name in the element.get(1) 
					
					this.parsedTrace.add("AddProperty("+this.getNameInChange(element.get(1))+//here property name
							", " + initTable.get(this.getId(element.get(element.size() - 1)))+
							", "+ this.getId(element.get(0))+//here id of the property
							", "+ this.getId(element.get(element.size() - 1))+");");// here id of the class
					
					int j = 0;
					while(j < element.size()){//here we add the type of the reference, other wise Extract class would not be detected
						
						if(this.getSingleName(element.get(j)).equals("type")){
							
							this.parsedTrace.add("SetProperty("+this.getNameInChange(element.get(1))+
									", "+initTable.get(this.getSecondID(element.get(j)))+//here deal with basic types: int double ect
									", "+ this.getId(element.get(0))+
									", "+ this.getSecondID(element.get(j))+");");
						}
						j++;
					}

					initTable.put(this.getId(element.get(0)), this.getNameInChange(element.get(1)));//update the initTable with the new property
					containTable.put(this.getSecondID(element.get(element.size() -1)), this.getId(element.get(element.size() -1)));//update the containTable with the new property
					//notRename.add(ordered.get(i+2).get(0));
					//notRename.add(ordered.get(i+2).get(1));
				
				} else if(i + 2 < ordered.size()
					&& ordered.get(i+2).size() == 2 
					&& this.getKind(ordered.get(i+2).get(0)).equals("remProperty")
					&& this.getKind(ordered.get(i+2).get(1)).equals("addProperty")
					&& this.getId(element.get(0)).equals(this.getId(ordered.get(i+2).get(1)))
					&& this.getSingleName(ordered.get(i+2).get(1)).equals("name")){//it means that they are followed with a name set not a rename
					
					if(this.getId(element.get(0)).contains("ClassImpl")){
						this.parsedTrace.add("AddClass("+this.getNameInChange(ordered.get(i+2).get(1))+//here class name
								", "+ this.getId(element.get(0))+//here id of the class
								", "+ this.getId(element.get(element.size() - 1))+");");// here id of the pakcage
						
						initTable.put(this.getId(element.get(0)), this.getNameInChange(ordered.get(i+2).get(1)));//update the initTable with the new class
						containTable.put(this.getSecondID(element.get(element.size() -1)), this.getId(element.get(element.size() -1)));//update the containTable with the new class
						notRename.add(ordered.get(i+2).get(0));
						notRename.add(ordered.get(i+2).get(1));
						
					} else if((this.getId(element.get(0)).contains("PropertyImpl") 
							|| this.getId(element.get(0)).contains("OperationImpl"))
							&& (i + 2 < ordered.size())){
						
						this.parsedTrace.add("AddProperty("+this.getNameInChange(ordered.get(i+2).get(1))+//here property name
								", " + initTable.get(this.getId(element.get(element.size() - 1)))+
								", "+ this.getId(element.get(0))+//here id of the property
								", "+ this.getId(element.get(element.size() - 1))+");");// here id of the class
						
						initTable.put(this.getId(element.get(0)), this.getNameInChange(ordered.get(i+2).get(1)));//update the initTable with the new property
						containTable.put(this.getSecondID(element.get(element.size() -1)), this.getId(element.get(element.size() -1)));//update the containTable with the new property
						notRename.add(ordered.get(i+2).get(0));
						notRename.add(ordered.get(i+2).get(1));
					}
					
					
				} else {//meaning that we set a default name !
					if(i + 1 < ordered.size()){
						if(this.getId(element.get(0)).contains("ClassImpl")){
							this.parsedTrace.add("AddClass("+this.getNameInChange(ordered.get(i+1).get(0))+//here class name
									", "+ this.getId(element.get(0))+//here id of the class
									", "+ this.getId(element.get(element.size() - 1))+");");// here id of the pakcage
							
							initTable.put(this.getId(element.get(0)), this.getNameInChange(ordered.get(i+1).get(0)));//update the initTable with the new class
							containTable.put(this.getSecondID(element.get(element.size() -1)), this.getId(element.get(element.size() -1)));//update the containTable with the new class
						
						} else if(this.getId(element.get(0)).contains("PropertyImpl") 
								|| this.getId(element.get(0)).contains("OperationImpl")){
							this.parsedTrace.add("AddProperty("+this.getNameInChange(ordered.get(i+1).get(0))+//here property name
									", " + initTable.get(this.getId(element.get(element.size() - 1)))+//here class name
									", "+ this.getId(element.get(0))+//here id of the property
									", "+ this.getId(element.get(element.size() - 1))+");");// here id of the class
							
							initTable.put(this.getId(element.get(0)), this.getNameInChange(ordered.get(i+1).get(0)));//update the initTable with the new property
							containTable.put(this.getSecondID(element.get(element.size() -1)), this.getId(element.get(element.size() -1)));//update the containTable with the new property
						}
					}
				}
				
			} else if(this.getId(element.get(0)).contains("AssociationImpl")) {
				
				if((i + 2 < ordered.size())
						&& ordered.get(i+2).size() == 2 
						&& this.getKind(ordered.get(i+2).get(0)).equals("remProperty")
						&& this.getKind(ordered.get(i+2).get(1)).equals("addProperty")
						&& this.getId(element.get(0)).equals(this.getId(ordered.get(i+2).get(1)))
						&& this.getSingleName(ordered.get(i+2).get(1)).equals("name")){//it means that it is followed with a name set not a rename
						
							
						this.parsedTrace.add("AddProperty("+this.getNameInChange(ordered.get(i+2).get(1))+//here property name
								", " + initTable.get(this.getId(element.get(element.size() - 1)))+//here class name
								", "+ this.getId(element.get(0))+//here id of the property
								", "+ this.getId(element.get(element.size() - 1))+");");// here id of the class
						
						initTable.put(this.getId(element.get(0)), this.getNameInChange(ordered.get(i+2).get(1)));//update the initTable with the new property
						containTable.put(this.getSecondID(element.get(element.size() -1)), this.getId(element.get(element.size() -1)));//update the containTable with the new property
						notRename.add(ordered.get(i+2).get(0));
						notRename.add(ordered.get(i+2).get(1));
						
					} else {//meaning that we set a default name !
						if(i + 1 < ordered.size()){
							this.parsedTrace.add("AddProperty("+this.getNameInChange(ordered.get(i+1).get(0))+//here property name
									", " + initTable.get(this.getId(element.get(element.size() - 1)))+
									", "+ this.getId(element.get(0))+//here id of the property
									", "+ this.getId(element.get(element.size() - 1))+");");// here id of the class
							
							initTable.put(this.getId(element.get(0)), this.getNameInChange(ordered.get(i+1).get(0)));//update the initTable with the new property	
							containTable.put(this.getSecondID(element.get(element.size() -1)), this.getId(element.get(element.size() -1)));//update the containTable with the new property			
						}
					}
				
			} else if(this.getId(element.get(0)).contains("ParameterImpl")) {
				
							
				if((element.size() > 15) 
						&& this.getSingleName(element.get(1)).equals("name")
						&& this.getSingleName(element.get(14)).equals("ownedparameter")
						&& (i + 2 < ordered.size())){
					
					this.parsedTrace.add("AddParameter("+this.getNameInChange(element.get(1))+//here property name
							", " + initTable.get(this.getId(element.get(element.size() - 1)))+//here operation name
							", "+ this.getId(element.get(0))+//here id of the parameter
							", "+ this.getId(element.get(element.size() - 1))+");");// here id of the operation
					
					initTable.put(this.getId(element.get(0)), this.getNameInChange(ordered.get(i+2).get(0)));//update the initTable with the new parameter
					containTable.put(this.getSecondID(element.get(element.size() -1)), this.getId(element.get(element.size() -1)));//update the containTable with the new parameter							
				}
				
			} else if(this.getId(element.get(0)).contains("GeneralizationImpl")
					&& (i + 1 < ordered.size())
					&& element.size() > 6
					&& this.getKind(ordered.get(i+1).get(0)).equals("addReference")
					&& this.getId(element.get(0)).equals(this.getId(ordered.get(i+1).get(0)))){
			
					this.parsedTrace.add("AddSuperType("+initTable.get(this.getId(element.get(element.size() -1)))+
							", "+initTable.get(this.getSecondID(ordered.get(i+1).get(0)))+
							", "+ this.getId(element.get(element.size() -1))+
							", "+ this.getSecondID(ordered.get(i+1).get(0))+");");
				
			}
		}
	}
	
	private void treatDelete(ArrayList<String> element,	ArrayList<ArrayList<String>> ordered, int i, Hashtable<String, String> initTable, ArrayList<String> notDelete) {

		if(this.getKind(element.get(0)).equals("remReference")){
			/*
			if(this.getSingleName(element.get(0)).equals("econtainingclass")){
				
				this.parsedTrace.add("DeleteProperty("+initTable.get(this.getId(element.get(0)))+
						", "+initTable.get(getSecondID(element.get(0)))+
						", "+ this.getId(element.get(0))+
						", "+ this.getSecondID(element.get(0))+");");
				
			} else if(this.getSingleName(element.get(0)).equals("epackage")){
				
				this.parsedTrace.add("DeleteClass("+initTable.get(this.getId(element.get(0)))+
						", "+initTable.get(getSecondID(element.get(0)))+
						", "+ this.getId(element.get(0))+
						", "+ this.getSecondID(element.get(0))+");");
				
			} else if(this.getSingleName(element.get(0)).equals("eoperation")){
				
				this.parsedTrace.add("DeleteParameter("+initTable.get(this.getId(element.get(0)))+
						", "+initTable.get(getSecondID(element.get(0)))+
						", "+ this.getId(element.get(0))+
						", "+ this.getSecondID(element.get(0))+");");
				
			} else if(this.getSingleName(element.get(0)).equals("esupertypes")){
				this.parsedTrace.add("DeleteSuperType("+initTable.get(this.getId(element.get(0)))+
						", "+initTable.get(this.getSecondID(element.get(0)))+
						", "+ this.getId(element.get(0))+
						", "+ this.getSecondID(element.get(0))+");");
				
			}  else*/ if(this.getSingleName(element.get(0)).equals("type")){
				
				this.parsedTrace.add("SetProperty("+initTable.get(this.getId(element.get(0)))+
						", null"+//initTable.get(getSecondID(element.get(0)))+//here deal with basic types: int double ect
						", "+ this.getId(element.get(0))+
						", "+ this.getSecondID(element.get(0))+");");
			}

		} else if(this.getKind(element.get(0)).equals("delete")){

			if(this.getSingleName(element.get(0)).equals("property") 
					|| this.getSingleName(element.get(0)).equals("association")
					|| this.getSingleName(element.get(0)).equals("operation")){
				
				this.parsedTrace.add("DeleteProperty("+initTable.get(this.getId(element.get(0)))+
						", "+initTable.get(getId(element.get(1)))+
						", "+ this.getId(element.get(0))+
						", "+ this.getId(element.get(1))+");");
				
			} else if(this.getSingleName(element.get(0)).equals("class")){
				
				this.parsedTrace.add("DeleteClass("+initTable.get(this.getId(element.get(0)))+
						", "+initTable.get(getId(element.get(1)))+
						", "+ this.getId(element.get(0))+
						", "+ this.getId(element.get(1))+");");
				
			} else if(this.getSingleName(element.get(0)).equals("parameter")){
				
				this.parsedTrace.add("DeleteParameter("+initTable.get(this.getId(element.get(0)))+
						", "+initTable.get(getId(element.get(1)))+
						", "+ this.getId(element.get(0))+
						", "+ this.getId(element.get(1))+");");
				
			} else if(this.getSingleName(element.get(0)).equals("generalization")){
				this.parsedTrace.add("DeleteSuperType("+initTable.get(this.getId(element.get(0)))+
						", "+initTable.get(this.getId(element.get(1)))+
						", "+ this.getId(element.get(0))+
						", "+ this.getId(element.get(1))+");");
				
			}
			
			notDelete.add(initTable.get(this.getId(element.get(0)))+//here we say that this element is deleted to ensure to not put a delete element twice in the trace
					", "+initTable.get(getId(element.get(1)))+
					", "+ this.getId(element.get(0))+
					", "+ this.getId(element.get(1)));
		}
		
	}
	
	private void treatChangeRef(ArrayList<String> element,	ArrayList<ArrayList<String>> ordered, int i, Hashtable<String, String> initTable, ArrayList<String> notDelete,ArrayList<String> notAdd) {

		if(element.size() == 1 ){
		
			if(this.getSingleName(element.get(0)).equals("ownedattribute")
					|| this.getSingleName(element.get(0)).equals("ownedoperation")){
				
				this.parsedTrace.add("AddProperty("+initTable.get(this.getSecondID(element.get(0)))+
						", "+initTable.get(getId(element.get(0)))+
						", "+ this.getSecondID(element.get(0))+
						", "+ this.getId(element.get(0))+");");
				
				notAdd.add(initTable.get(this.getSecondID(element.get(0)))+//here we say that this element is added to ensure to not put an add element twice in the trace
						", "+initTable.get(getId(element.get(0)))+
						", "+ this.getSecondID(element.get(0))+
						", "+ this.getId(element.get(0)));
			}
			
			if(this.getSingleName(element.get(0)).equals("type")){
				this.parsedTrace.add("SetProperty("+initTable.get(this.getId(element.get(0)))+
						//", "+initTable.get(getSecondID(element.get(0)))+//here deal with basic types: int double ect
						", "+((initTable.get(this.getSecondID(element.get(0))) != null) ? initTable.get(this.getSecondID(element.get(0))) : this.getSecondID(element.get(0)))+
						", "+ this.getId(element.get(0))+
						", "+ this.getSecondID(element.get(0))+");");
			}
			
			
					 
		
		} else if(element.size() == 2 ){
			
			if(this.getSingleName(element.get(0)).equals("type")){//TODO Here is an issue: the changes "change type" and "move a proerpty" produce the same trace, how to distinguish them? 
				
				this.parsedTrace.add("DeleteProperty("+initTable.get(this.getId(element.get(0)))+
						", "+initTable.get(getSecondID(element.get(0)))+
						", "+ this.getId(element.get(0))+
						", "+ this.getSecondID(element.get(0))+");");
				
				this.parsedTrace.add("AddProperty("+initTable.get(this.getId(element.get(1)))+
						", "+initTable.get(getSecondID(element.get(1)))+
						", "+ this.getId(element.get(1))+
						", "+ this.getSecondID(element.get(1))+");");
			
				
				notDelete.add(initTable.get(this.getId(element.get(0)))+//here we say that this element is deleted to ensure to not put a delete element twice in the trace
						", "+initTable.get(getId(element.get(1)))+
						", "+ this.getId(element.get(0))+
						", "+ this.getId(element.get(1)));
				
				notAdd.add(initTable.get(this.getSecondID(element.get(0)))+//here we say that this element is added to ensure to not put an add element twice in the trace
						", "+initTable.get(getId(element.get(0)))+
						", "+ this.getSecondID(element.get(0))+
						", "+ this.getId(element.get(0)));
				
			} else if(this.getSingleName(element.get(0)).equals("general")){//TODO not treated perfectly, may be put change "SuperType"
				
				/*this.parsedTrace.add("DeleteSuperType("+initTable.get(this.getId(element.get(0)))+
						", "+initTable.get(this.getId(element.get(1)))+
						", "+ this.getId(element.get(0))+
						", "+ this.getId(element.get(1))+");");
				
				this.parsedTrace.add("AddSuperType("+initTable.get(this.getId(element.get(element.size() -1)))+
						", "+initTable.get(this.getSecondID(ordered.get(i+1).get(0)))+
						", "+ this.getId(element.get(element.size() -1))+
						", "+ this.getSecondID(ordered.get(i+1).get(0))+");");
			
				
				notDelete.add(initTable.get(this.getId(element.get(0)))+//here we say that this element is deleted to ensure to not put a delete element twice in the trace
						", "+initTable.get(getId(element.get(1)))+
						", "+ this.getId(element.get(0))+
						", "+ this.getId(element.get(1)));
				
				notAdd.add(initTable.get(this.getSecondID(element.get(0)))+//here we say that this element is added to ensure to not put an add element twice in the trace
						", "+initTable.get(getId(element.get(0)))+
						", "+ this.getSecondID(element.get(0))+
						", "+ this.getId(element.get(0)));
				*/
			} 
			
		}
	}



}
