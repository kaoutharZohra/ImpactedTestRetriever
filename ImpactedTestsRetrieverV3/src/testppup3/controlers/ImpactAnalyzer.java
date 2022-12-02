package testppup3.controlers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.correction.JavaCorrectionProcessor;
import org.eclipse.jdt.internal.ui.text.correction.ProblemLocation;

import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.correction.*;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.SubActionBars;

import Utilities.ASTManager;
import Utilities.ChangeDetection;
import Utilities.JavaVisitor;
import Utilities.UtilProjectParser;
import fr.lip6.meta.ComplexChangeDetection.Change;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AddProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.ChangeLowerBound;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.ChangeUpperBound;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.DeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.RenameClass;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.RenameProperty;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.SetProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PushProperty;
import fr.lip6.meta.ComplexChangeDetection.EMFCompare.ComputeDelta;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;

public class ImpactAnalyzer {
	private static ICompilationUnit iCompilUnittest = null;

	/* Checks if a string is empty ("") or null. */
	public static boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	}



	/* Counts how many times the substring appears in the larger string. */
	public static int countMatches(String text, String str)
	{
		if (isEmpty(text) || isEmpty(str)) {
			return 0;
		}

		return text.split(str, -1).length - 1;
	}


	public static int testCounter(CompilationUnit compilUnit)
	{
		int count=0;

		count =countMatches( ASTManager.findCompilationUnit(compilUnit).toString()," public void test");

		return count;
	}

	public static String formatChange(Change change)
	{
		String changeMessage = "";
		if(change instanceof RenameClass)
		{
			RenameClass rc = (RenameClass)change;
			changeMessage= " Rename class "+rc.getName() +  "  to "+rc.getNewname();

		}
		else if(change instanceof DeleteClass)
		{
			changeMessage= "Deleted class " + ((DeleteClass)change).getName();
		}
		else if (change instanceof RenameProperty)
		{
			RenameProperty rp = (RenameProperty)change;
			changeMessage="Rename property " + rp.getName() + " to " + rp.getNewname()+ " in " +rp.getClassName();

		}
		else if( change instanceof DeleteProperty)
		{
			DeleteProperty dp = (DeleteProperty)change;
			changeMessage="Deleted property " + dp.getName() + " in " +dp.getClassName();
		}
		else if(change instanceof MoveProperty)
		{
			MoveProperty mp =(MoveProperty)change;
			changeMessage= "Move property  "+mp.getName() + " From  "+mp.getSourceClassName()+ "  to "+ mp.getTargetClassName();


		}
		else if (change instanceof PushProperty)
		{
			PushProperty pshp  =(PushProperty)change;
			String classes="";
			ArrayList<String> subclasses = pshp.getSubClassesNames();
			int pos =0;
			for( String s : subclasses)
			{
				pos++;
				classes= classes+ s ;
				if (pos< subclasses.size()-1)
				{
					classes=classes + "and";
				}


			}
			changeMessage= "Push property "+ pshp.getName() + " From "+ pshp.getSuperClassName() +" To "+ classes; 
		}
		else if (change instanceof PullProperty)
		{
			PullProperty pllp= (PullProperty)change;
			String classes="";
			ArrayList<String> subclasses = pllp.getSubClassesNames();
			int pos =0;
			for( String s : subclasses)
			{
				pos++;
				classes= classes+ s ;
				if (pos< subclasses.size()-1)
				{
					classes=classes + "and";
				}


			}
			changeMessage= "Pull property "+pllp.getName()+ " from "+ classes + " to "+pllp.getSuperClassName();

		}
		else if(change instanceof SetProperty)
		{
			SetProperty sp=(SetProperty)change;
			changeMessage="Set type of property " + sp.getName() + " to " + sp.getType() + " in class " + sp.getContainer();

		}
		else if(change instanceof MoveProperty)
		{
			MoveProperty sp=(MoveProperty)change;
			changeMessage="Move  property " +  sp.getName()+ "  From  "+ sp.getSourceClassName()+ " to " + sp.getTargetClassName();

		}
		else if(change instanceof AddClass)
		{
			AddClass sp=(AddClass)change;
			changeMessage=" Add the class " + sp.getName() ;

		}
		else if(change instanceof AddProperty)
		{
			AddProperty sp=(AddProperty)change;
			changeMessage="Add the property " + sp.getName() ;

		}
		else if(change instanceof ChangeUpperBound)
		{
			ChangeUpperBound sp=(ChangeUpperBound)change;
			changeMessage="ChangeUpperBound of property " + sp.getName() ;

		}
		else if(change instanceof ChangeLowerBound)
		{
			ChangeLowerBound sp=(ChangeLowerBound)change;
			changeMessage="ChangeLowerBound of property " + sp.getName() ;

		}
		else changeMessage = "Not any type";
		return changeMessage;
	}
	public static void run() {
		/************************** Preparing data *******************************/


		IProject project = UtilProjectParser.getSelectedProject();
		ArrayList<ICompilationUnit> ListICompilUnit = UtilProjectParser.getCompilationUnits(project);

		CompilationUnit compilUnit = null;
		JavaVisitor jVisitor = new JavaVisitor();


		/**                changes with trace coder **/



//		String [] tab = {"/home/zkebaili/eclipse-workspace/ImpactedTestsRetrieverV3/Model/PivotV6.7.0.ecore", "/home/zkebaili/eclipse-workspace/ImpactedTestsRetrieverV3/Model/PivotV6.18.0.ecore"};
//		//"C:/Users/dkhellad/Documents/eclipses/eclipse-epsilon-1.4-win32-x86_64/workspace2/MCCoEvolutator/models/ocl/3.2.2/Pivot.ecore", "C:/Users/dkhellad/Documents/eclipses/eclipse-epsilon-1.4-win32-x86_64/workspace2/MCCoEvolutator/models/ocl/3.4.4/Pivot.ecore");
//
//		String source = tab[0];
//		String target = tab[1];
//
//		InitialState initState = new InitialState(source); //initialize the source metamodel that we use for precise cc detection
//		initState.initialize(true);//ecore
//
//		InitialState initStateTarget = new InitialState(target); //initialize the target metamodel that we use for precise cc detection
//		initStateTarget.initialize(true);//ecore
//
//		ComputeDelta cDelta = new ComputeDelta();
//		Comparison comparison = cDelta.compare(source, target);
//
//
//		ArrayList<AtomicChange> achanges = cDelta.importeEMFCompareChanges(comparison, initState, initStateTarget);
//		System.out.println("transfered diff delta size: " + achanges.size());


		/**            end    changes with trace coder **/

		//ComputeDelta cDelta = new ComputeDelta();
		//Comparison comparison =
		//	cDelta.compare("/home/zkebaili/runtime-EclipseApplication/TraceRecorder/Model/PivotV6.0.1.ecore",
		//	"/home/zkebaili/runtime-EclipseApplication/TraceRecorder/Model/PivotV6.18.0.ecore");
		//MainEMFCompare mainEMFCompare = new MainEMFCompare();
		//mainEMFCompare.detectComplexChangeFromEMFCompareDiff("/home/zkebaili/runtime-EclipseApplication/TraceRecorder/Model/PivotV6.3.0.ecore",
		//	"/home/zkebaili/runtime-EclipseApplication/TraceRecorder/Model/PivotV6.5.0.ecore");
		//ArrayList<ComplexChange> complexChanges=mainEMFCompare.detectComplexChangeFromEMFCompareDiff(
		//	"/home/zkebaili/runtime-EclipseApplication/TraceRecorder/Model/PivotV6.3.0.ecore",
		//	"/home/zkebaili/runtime-EclipseApplication/TraceRecorder/Model/PivotV6.5.0.ecore");

		//ArrayList<AtomicChange> changes0 = cDelta.importeEMFCompareChanges(comparison);
		//	ArrayList<Change>  changes0 =ChangeDetection.initializeChangements();


		//	System.out.println(" BEGIN DIFF ****************************************");
		//	ArrayList<Change> changess= new ArrayList<Change>();
		//	changess.addAll(changes0);
		//	changess.addAll(complexChanges);
		/*for (int i = 0; i <changess.size(); i++) {
			System.out.println("change n° " + i + " is : " + (AtomicChange)changess.get(i));

		}*/
		//	System.out.println(" END DIFF ****************************************");

		//	ArrayList<AtomicChange> changes = new ArrayList<AtomicChange>();
		/** BEGIN Fake changes **/
		//	ArrayList<Change>  changes = ChangeDetection.initializeChangements();

		//RenameProperty rp1 = new RenameProperty("x", "a", "MyPoint");
		//DeleteClass dc1 = new DeleteClass("ITranslation");
		//RenameClass rc1 = new RenameClass("ArrayIterable", "Point", "point");
		//changes.add(rp1);
		//changes.add(dc1);
		//changes.add(rc1);
		/** END Fake changes **/
		//	System.out.println("transfered diff delta size: " + changes.size());

		//myChanges.addAll(changes);

		// myChanges.addAll(complexChanges);
		int nbTests=0;
		/************************** Retrieving process *******************************/
		for (ICompilationUnit iCompilUnit : ListICompilUnit) {
			compilUnit = ASTManager.getCompilationUnit(iCompilUnit); // Refresh the compilation unit
			jVisitor.process(compilUnit);
			if (iCompilUnit.getElementName().contains("_ESTest")) // process only code and ignore tests
			{
				nbTests=nbTests+testCounter(compilUnit);
				iCompilUnittest = iCompilUnit;


			}

			//System.out.println("ICompilation unit root : " + ASTManager.findCompilationUnit(compilUnit).toString());

			//System.out.println("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
			//System.out.println("Compilation unit : " + compilUnit.toString());

		}
		System.out.println(" Total number of tests is "+ nbTests);
		System.out.println("BEGIN ///////////////////////////////////////////////////////////////");
		JavaParser.printManageBindings();
		System.out.println(" END ///////////////////////////////////////////////////////////////");

		// JavaParser.printManageBindings();
		ArrayList<Change>  achanges = ChangeDetection.initializeChangements();
		ArrayList<Change>  changes= new ArrayList<Change>();
		// RenameClass renameClass = new RenameClass("MyPoint","A","mainTest"); 
		//changes.add(renameClass);

		changes.addAll(achanges);
		System.out.println(" The changes size is " + changes.size());
		for (int i = 0; i < changes.size(); i++) {

			System.out.println(" Change number  " +i+ "   the change is " + formatChange(changes.get(i))+ " / "+changes.size());
			matchBindingChange(changes.get(i));

		}
		System.out.println(" END OF EXECUTION of IA");
	}
	public static boolean isTestMethod(MethodDeclaration md) {
		if(md !=null)
		{
			if(md.resolveBinding().getAnnotations()!=null) 
			{
				if(md.resolveBinding().getAnnotations().length!=0) 
				{
					if( md.resolveBinding().getAnnotations()[0].getName().equals("Test"))
					{
						return true;
					}
				}
			}
		}
		return false;

	}

	public static void addPath(String s) throws Exception {
		File f = new File(s);
		URL u = f.toURL();
		URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class urlClass = URLClassLoader.class;
		Method method = urlClass.getDeclaredMethod("addURL", new Class[] { URL.class });
		method.setAccessible(true);
		method.invoke(urlClassLoader, new Object[] { u });
	}

	public static void testLoad() {
		File f = new File("/home/zkebaili/runtime-New_configuration(6)/BasicProject2/bin");
		URI u = f.toURI();

		URLClassLoader child;
		try {
			child = new URLClassLoader(new URL[] { u.toURL() },
					/* this.getClass().getClassLoader() */ClassLoader.getSystemClassLoader());
			Class classToLoad = Class.forName("myPackage.MyPoint", true, ClassLoader.getSystemClassLoader());
			Method method = classToLoad.getDeclaredMethod("testMethod");
			Object instance = classToLoad.newInstance();
			Object result = method.invoke(instance);
		} catch (MalformedURLException | ClassNotFoundException | NoSuchMethodException | SecurityException
				| InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public static boolean matchingKeys(String key1, String key2)
	{
		if(key1.equals(key2)) return true;
		else
		{
			if(key1.contains("<") ) // due generic types
			{

				String partOfKey1 = key1.split("<")[0];
				String partOfKey2 = key2.split("<")[0];


				if(partOfKey1.equals(partOfKey2))
					return true;
				else return false;
			}
			else return false; // not treated cases
		}

	}
	public static ArrayList<ASTNode> mapKeyNodes(String keykey) {
		ArrayList<ASTNode> nodes = new ArrayList<ASTNode>();
		for (IBinding key : JavaVisitor.getManageBindings().getBindingsNodes().keySet()) {

			if (key != null) {

				if (matchingKeys(key.getKey(), keykey)) {

					nodes.addAll(JavaVisitor.getManageBindings().getBindingsNodes().get(key));
				}
			}
		}
		return nodes;

	}

	public static ArrayList<ASTNode> mapBindingNodes(IBinding b) {

		return JavaVisitor.getManageBindings().getBindingsNodes().get(b);

	}

	public static void printNodesByBinding(ArrayList<ASTNode> nodes) {
		int i = 0;
		for (ASTNode node : nodes) {

			System.out.println(" Node : " + i + " is :  " + ((SimpleName) node).getIdentifier() +"  Its real binding is :  "+  ((SimpleName)node).resolveBinding().getKey()+ "  it starts at "+ node.getStartPosition());
			i++;
		}
	}
	public static String typeBinding(IBinding binding)
	{

		String[] result=((binding.getClass()).toString()).split("\\.");
		return result[5];

	}

	public static ArrayList<String> formatBinding(IBinding binding) {
		String key = binding.getKey();
		ArrayList<String> bindingDetails = new ArrayList<String>();
		if (typeBinding(binding).equals("TypeBinding")) {
			String classname ="";
			if(key.charAt(0)=='L') {

				key=key.substring(1);

			}
			if(key.contains("<"))
			{
				classname = ((key.split("/"))[key.split("/").length-1]).split("<")[0];

			}
			else {

				//classname = ((key.split("/"))[key.split("/").length-1]).split(";")[0];
				String parts[] = (key.split(";")[0]).split("/");
				classname=parts[parts.length-1];
			}

			bindingDetails.add(classname);
		} else if (typeBinding(binding).equals("MethodBinding")) {
			String classname = "";
			String methodname="";
			String[] methodnameinit =key.split("\\.");

			if(key.contains("<") ) System.out.println(" Method binding with generic type not treated");

			//	if(key.contains("<"))
			//	{
			//		classname = ((key.split("/"))[key.split("/").length-1]).split("<")[0];

			//	}
			//	else {
			//classname = ((key.split("/"))[key.split("/").length-1]).split(";")[0];
			String parts[] = (key.split(";")[0]).split("/");
			classname=parts[parts.length-1];
			//	}
			if(methodnameinit.length>1) {

				methodname=methodnameinit[1].split("\\(")[0];
				//				System.out.println(" in method name 0"+ methodname);
				//				if(methodname.equals("validateSafeIteratorIsRequired"))
				//					System.out.println(" the class name from binding is "+classname);

			}
			else  System.out.println("Format Binding exception");
			bindingDetails.add(classname);
			bindingDetails.add(methodname);
			//System.out.println("The result of format :"+ bindingDetails.get(1));
		} else if (typeBinding(binding).equals("VariableBinding")) {

			String classname = "";
			String methodname="";
			String[] methodnameinit =key.split("\\.");

			if(key.contains("<"))
			{
				classname = ((key.split("/"))[key.split("/").length-1]).split("<")[0];

			}
			else {
				//classname = ((key.split("/"))[key.split("/").length-1]).split(";")[0];
				String parts[] = (key.split(";")[0]).split("/");
				classname=parts[parts.length-1];
			}
			if(methodnameinit.length>1) {

				methodname=methodnameinit[1].split("\\(")[0];
				//System.out.println(" in method name "+ methodname);
			}
			//else  System.out.println("Format Binding exception");
			String variablename="";
			if(key.contains("#")){
				variablename=key.split("#")[1];
			}
			else {

				variablename= key.split("\\.")[1].split("\\)")[0];
			}
			bindingDetails.add(classname);
			bindingDetails.add(methodname);
			bindingDetails.add(variablename);
			if(key.equals("org/eclipse/modisco/infra/discovery/benchmark/impl/DiscoveryIterationImpl;.getDiscoveryErrors()Ljava/lang/String;"))
			{
				System.out.print(" you formate the wanted");
			}
		}
		else 
		{
			//System.out.println(" Not any type ");
		}

		return bindingDetails;
	}
	public static String getCUName(ICompilationUnit icu, IProject project)
	{
		icu= UtilProjectParser.getCompilationUnit(project, "testi.java");
		return icu.getElementName();

	}
	public static boolean isLiteral(ASTNode node)
	{
		ASTNode parent =node.getParent();

		List<ASTNode> children ;
		ASTNode child;
		if(parent instanceof QualifiedName)
		{


			children=ASTManager.getChildren(parent) ; 

			child=children.get(0);
			if (child instanceof QualifiedName)
			{
				children=ASTManager.getChildren(child);
				child=children.get(1);
				if(child instanceof SimpleName) {
					if(((SimpleName)child).getIdentifier().equals("Literals"))
					{

						return true;
					}
				}

			}



		}
		return false;
	}

	public static Boolean containOnlyCapitals(String literal)
	{

		char currentCharacter ;
		for (int i = 0; i < literal.length(); i++) {
			currentCharacter = literal.charAt(i);

			if (!Character.isUpperCase(currentCharacter) && currentCharacter !='_'  ) {

				return false;
			}


		}


		return true;
	}
	public static String makeLiteral(String literal)
	{
		//System.out.println(" your literal before is "+literal);
		if(! ASTManager.containOnlyCapitals(literal))
		{
			String newLiteral="";
			char currentCharacter;
			char precedantCharacter = '\0';
			for (int i = 0; i < literal.length(); i++) {
				if( i>0)
				{
					precedantCharacter	=literal.charAt(i-1);
				}
				currentCharacter = literal.charAt(i);

				if (Character.isUpperCase(currentCharacter) && i!=0  && !Character.isUpperCase(precedantCharacter) ) {

					newLiteral=newLiteral+"_";
				}

				newLiteral=newLiteral+ Character.toUpperCase(currentCharacter);

			}
			//.out.println(" NEW LITERAL "+newLiteral);
			return newLiteral;
		}
		else 
			return literal;


	}


	public static void matchBindingChange(Change change) // for each change, we browse the list of bindings and check for tests, save tests at the end 
	{


		IProject project=null;
		IPath path =null;
		//System.out.println(" The change in is "+ change.toString());
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
		{
			IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof IAdaptable)
			{
				project = (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);
				path = project.getLocation();

			}
		}

		ArrayList<IBinding> next_round=new ArrayList<IBinding>(); // to confirm the object type of next_round

		int k =0;
		int pos =0;
		int posi=0;
		Map<String, ArrayList<MethodDeclaration>> impactedTests = new HashMap<String, ArrayList<MethodDeclaration>>();

		File file =new File("/home/zkebaili/eclipse-workspace/ImpactedTestsRetrieverV3/report_impacted_tests.txt");
		FileWriter myWriter=null;
		try {
			myWriter = new FileWriter(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}





		for (IBinding key : JavaVisitor.getManageBindings().getBindingsNodes().keySet()) {
			if (key != null) {
				if(key.getKey().equals("Lorg/eclipse/modisco/infra/discovery/benchmark/impl/DiscoveryIterationImpl;.getDiscoveryErrors()Ljava/lang/String;"))
				{
					System.out.println(" you parse the wanted binding here");
				}

				//System.out.println(" Trying to link : "+ key.getKey());

				//	if(change instanceof RenameProperty && formatBinding(key).size()>2)
				//if( key.getKey().equals("Lorg/eclipse/ocl/pivot/IteratorExp;.validateSafeIteratorIsRequired(Lorg/eclipse/emf/common/util/DiagnosticChain;Ljava/util/Map<Ljava/lang/Object;Ljava/lang/Object;>;)Z"))

				//System.out.println(" The current binding  "+key.getKey()+ " the change is "+ formatChange(change));
				//if(linkBindingChange(formatBinding(key), change))
				if(linkBindingInverseChange(formatBinding(key), change))		
				{
					
					//System.out.println(" BEGIN USAGES **********************************");
					//	printNodesByBinding(mapKeyNodes(key.getKey()));
					//System.out.println(" END USAGES **********************************");

					for( ASTNode sn: mapKeyNodes(key.getKey()))
					{
						//System.out.println(" begin for binding ");
						MethodDeclaration md = (MethodDeclaration) ASTManager.findMethodDeclaration(sn);
						//CompilationUnit cu= (CompilationUnit)ASTManager.findCompilationUnit(md);
						//ICompilationUnit icu2 = (ICompilationUnit)(cu.getJavaElement());

						//	System.out.println(" current usage " +((SimpleName)sn).getIdentifier()+ "Its real binding is :  "+  ((SimpleName)sn).resolveBinding().getKey()+"  starts at  "+sn.getStartPosition());
						if(isTestMethod(md))
						{
							System.out.println("  find a test ");
							if(!isAdded(impactedTests, md))
							{
								CompilationUnit compilationu= (CompilationUnit) ASTManager.findCompilationUnit(md);

								ICompilationUnit icu = 	 (ICompilationUnit) compilationu.getJavaElement();
								/*
								 * try { ICompilationUnit copy= (ICompilationUnit) icu.getWorkingCopy(); }
								 * catch (JavaModelException e) { // TODO Auto-generated catch block
								 * e.printStackTrace(); }
								 */
								try {
									System.out.println("Impacting change (directly): "+ formatChange(change)+" Tests class:  "+compilationu.getJavaElement().getElementName()+ "  Impacted test method :"+md.getName());
									myWriter.write("Impacting change (directly): "+ formatChange(change)+" Tests class:  "+compilationu.getJavaElement().getElementName()+ "  Impacted test method :"+md.getName()+"\n");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								if(impactedTests.containsKey(compilationu.getJavaElement().getElementName()))
								{

									impactedTests.get(compilationu.getJavaElement().getElementName()).add(md);
								}
								else
								{
									ArrayList<MethodDeclaration> mdlist = new ArrayList<MethodDeclaration>();
									mdlist.add(md);
									impactedTests.put(compilationu.getJavaElement().getElementName(), mdlist);

								}

							}
							//	System.out.println(" === It is a test === "+md.getName() + "in:  "+ ASTManager.findCompilationUnit(md)   );

						}
						else if(md !=null)
						{
							//System.out.println(" not a test md  !");

							IBinding newBinding=   md.resolveBinding();// problem here

							boolean added_elem=false;
							//	System.out.println(" The key of binding is "+newBinding.getKey());
							if(mapKeyNodes(newBinding.getKey()).size()>1)
							{
								//printNodesByBinding(mapKeyNodes(newBinding.getKey()));
								//System.out.println(" there are usages -- we add this binding for next round "+newBinding  + "  for the method  "+md.getName());

								next_round.add(newBinding);

								added_elem=true;

								int round=0;
								while( added_elem )
								{
									//	System.out.println(" begin while  ");
									round++;
									//System.out.println(" in another round " +round);

									for( ASTNode snij: mapKeyNodes(next_round.get(pos).getKey()))
									{
										//System.out.println(" begin for next round");
										//System.out.println(" in for usages ( " + ((SimpleName)snij).getIdentifier()+" )" + "of key  "+next_round.get(pos).getKey());
										MethodDeclaration newmd = (MethodDeclaration) ASTManager.findMethodDeclaration(snij);

										if(isTestMethod(newmd) ){
											if(!isAdded(impactedTests, newmd))
											{
												CompilationUnit compilationu= (CompilationUnit) ASTManager.findCompilationUnit(newmd);
												//compilationu.imp
												try {
													System.out.println("Impacting change (indirectly): "+ formatChange(change)+" Tests class:  "+compilationu.getJavaElement().getElementName()+ "  Impacted test method :"+md.getName());

													myWriter.write("Impacting change (indirectly): "+formatChange(change)+" Tests class:  "+compilationu.getJavaElement().getElementName()+ "  Impacted test method :"+md.getName()+ "\n");
												} catch (IOException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
												if(impactedTests.containsKey(compilationu.getJavaElement().getElementName()))
												{
													impactedTests.get(compilationu.getJavaElement().getElementName()).add(newmd);
												}
												else
												{
													ArrayList<MethodDeclaration> mdlist = new ArrayList<MethodDeclaration>();
													mdlist.add(newmd);
													impactedTests.put(compilationu.getJavaElement().getElementName(), mdlist);

												}

											}

											//System.out.println(" Indirect Test Method detected here!  "+newmd.getName()  );//+usages.get(k).getStartPosition ());
											added_elem=false;
										}
										else if(newmd !=null)
										{
											IBinding anotherbinding= newmd.resolveBinding();
											//printNodesByBinding(mapBindingNodes(anotherbinding));
											if(mapKeyNodes(anotherbinding.getKey()).size()>1 && !next_round.contains(anotherbinding))
											{
												//	printNodesByBinding(mapKeyNodes(anotherbinding.getKey()));
												next_round.add(anotherbinding);
												pos++;
												//	System.out.println("  Another binding added :  "+anotherbinding);

											}
											else {
												added_elem=false;
												/*	if(mapKeyNodes(anotherbinding.getKey()).size()==1)
													System.out.println(" No usages !" + newmd.getName());
												if ( next_round.contains(anotherbinding))
													System.out.println(" Binding already treated !" + anotherbinding.getKey());
												 */
											}

										}

										//	System.out.println(" end for next round");
									}
									//System.out.println("end while ");
								}
							}
							else 
							{
								//System.out.println(" not a test md and without usages "+ md.getName() + " its key is "+md.resolveBinding().getKey());

							}

						}

						//System.out.println(" end for binding");
					}
				}
				else System.out.println("  not matched");
				//}
				//}

				/*	else if (change instanceof RenameProperty) {
						String changeproperty=((RenameProperty)change).getName();
						String  changeclass= ((RenameProperty)change).getClassName();

					} 
					else if (change instanceof DeleteClass) {

					}*/

			}
		}
		try {
			myWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(" +BEGIN++++++++++++++++++++++++++++++IMPACTED TESTS ++++++++++++++++++++++++++++++++++++++++++");
		//printImpactedTests(impactedTests);
		saveImpactedTests(impactedTests);
		System.out.println(" +END++++++++++++++++++++++++++++++IMPACTED TESTS ++++++++++++++++++++++++++++++++++++++++++");
	}

	public static void printImpactedTests(Map<String, ArrayList<MethodDeclaration>> impactedTests)
	{
		for(String cuname:impactedTests.keySet() )
		{
			System.out.println("Impacted Compilation unit  "+ cuname);
			for(MethodDeclaration md : impactedTests.get(cuname))
			{
				System.out.println(" Impacted test : "+md.toString());

			}
		}
	}
	public static void saveImpactedTests(Map<String, ArrayList<MethodDeclaration>> impactedTests)
	{
		boolean first =true;
		int pos=0;
		String code ="";


		try {


			String newPath="";
			for(String cuname:impactedTests.keySet() )// case no impacted tests
			{

				//	System.out.println("Impacted Compilation unit  "+ (cuname.split("\\.java"))[0] );
				first=true;
				for(MethodDeclaration md : impactedTests.get(cuname))
				{
					CompilationUnit cu = (CompilationUnit) ASTManager.findCompilationUnit(md);
					//(TypeDeclation) cu.types().add(0)
					TypeDeclaration td= (TypeDeclaration) ASTManager.findTypeDeclaration(md);
					td.resolveBinding().getAnnotations();
					//.out.println(" Project path is "+path);
					//	System.out.println(" CU  path is "+cu.getJavaElement().getPath());
					//	System.out.println(" The result of spliting "+((cu.getJavaElement().getPath().toString()).split("\\.java"))[0]);
						//newPath= "/home/zkebaili/runtime-EclipseApplication"+((cu.getJavaElement().getPath().toString()).split("\\.java"))[0]+"_impacted.java"; // to check
					newPath= "/home/zkebaili/runtime-New_configuration(6)"+((cu.getJavaElement().getPath().toString()).split("\\.java"))[0]+"_impacted.java"; // to check


					//System.out.println(" its path is  "+ newPath);




					if (first)
					{


						//myWriter1.write(cu.getPackage().toString());
						//	myWriter1.write(cu.imports().toString()); 
						//	myWriter1.close();
						if(cu.getPackage() !=null) {
							code = code+cu.getPackage().toString();
						}

						for (Object o :cu.imports() )
						{

							code = code+"\n"+((ImportDeclaration)o).toString();
						}

						/*	code =code+"\n"+ "@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true, useJEE = true) \r\n"
								+ "public class "+cuname+ "_impacted "+" extends"+ "c";
						 */

						for(IAnnotationBinding nb:td.resolveBinding().getAnnotations())
						{
							code =code+"\n"+nb.toString();	
						}
						code = code +"\n public class "+(cuname.split("\\.java"))[0]+ "_impacted "+" extends "+ (cuname.split("\\.java"))[0]+"_scaffolding {";
						first=false;
					}

					//	FileWriter	myWriter2 = new FileWriter(myObj);

					//myWriter2.write(md.toString());
					code=code+"\n"+md.toString();
					//myWriter2.close();
					//System.out.println(" Impacted test : "+md.toString());

				}
				if(!code.equals("")) {
					File file =new File(newPath);
					FileWriter myWriter0= new FileWriter(file);
					code = code +"\n"+"}";
					myWriter0.write(code);
					code="";
					myWriter0.close();
				}
				else  System.out.println(" nothing to write !");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static boolean isAdded( Map<String, ArrayList<MethodDeclaration>> impactedTests , MethodDeclaration md)
	{
		boolean exist=false;
		CompilationUnit cu= (CompilationUnit) ASTManager.findCompilationUnit((ASTNode)md);
		if(cu !=null)
		{
			String cuname=cu.getJavaElement().getElementName();
			//System.out.println(" The tested cu : "+cuname+"  the size of the tests  "+impactedTests.size());

			if(!impactedTests.containsKey(cuname))
			{
				return false;
			}
			else {
				for(MethodDeclaration method: impactedTests.get(cuname))
				{
					if(method.getName().getIdentifier().equals(md.getName().getIdentifier()))
					{
						//System.out.println(" Test already added !");
						return true;
					}
				}
			}
		}
		return exist;
	}
	public static boolean linkBindingChange( ArrayList<String> bindingInfo, Change change)
	{
		boolean match=false;
		if(!bindingInfo.isEmpty()) // if the binding is well treated 
		{
			if (change instanceof DeleteClass)
			{
				DeleteClass dc = (DeleteClass)change;
				String classname = dc.getName();
				if(bindingInfo.get(0)!=null)
				{
					if(bindingInfo.get(0).equals(classname) || bindingInfo.get(0).equals(classname+"Impl")  )
					{
						match=true;
					}
				}else if(bindingInfo.get(1)!=null)
				{
					if(bindingInfo.get(1).equals("get"+classname)  || bindingInfo.get(1).equals("set"+classname) || bindingInfo.get(1).equals("create"+classname)  )
					{
						match=true;
					}

				}
				else  if(bindingInfo.get(2)!=null){
					//System.out.println(" in delete class literal");
					if(containOnlyCapitals(bindingInfo.get(2)) )// Check if Literal
					{
						if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).contains(makeLiteral(classname)))
								//	||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
								)
						{
							match=true;
						}
					}

				}

			}
			else  if (change instanceof RenameClass)
			{
				RenameClass rc =(RenameClass)change;
				String classname = rc.getName();
				if(bindingInfo.get(0)!=null){
					if(bindingInfo.get(0).equals(classname) || bindingInfo.get(0).equals(classname+"Impl") )
					{
						match=true;
					}
				}else if(bindingInfo.get(1)!=null){
					if(bindingInfo.get(1).equals("get"+classname)  || bindingInfo.get(1).equals("set"+classname) || bindingInfo.get(1).equals("create"+classname)  )
					{
						match=true;
					}

				}
				else  if(bindingInfo.get(2)!=null){
					System.out.println(" in rename class literal");
					if(containOnlyCapitals(bindingInfo.get(2)) )// Check if Literal
					{
						if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).contains(makeLiteral(classname)))
								//	||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
								)
						{
							match=true;
						}
					}
				}
			}
			else
				if(bindingInfo.size()>1) {
					if (change instanceof DeleteProperty)
					{
						DeleteProperty dp = (DeleteProperty)change; //attribute or method
						String propertyname= dp.getName();
						String classname=dp.getClassName();
						//	System.out.println(" in Delete property lining "+ propertyname+ " the binding info 1 is" +bindingInfo.get(1));
						if(bindingInfo.size()>1) {

							if(bindingInfo.get(0) !=null & bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals(propertyname) )
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals(propertyname)) )
								{
									match=true;
								}

							}
						}
						if(bindingInfo.size()>2) {

							if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
							{

								if((bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals(propertyname) )||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals(propertyname))
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("get"+capitalizeFirstLetter( propertyname)))
										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
										)

								{
									match=true;
								}


								if(containOnlyCapitals(bindingInfo.get(2))) {
									System.out.println(" in delete property literal case ");
									String literal =makeLiteral(classname)+"__"+makeLiteral(propertyname);
									if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
											//	||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
											)
									{
										match=true;
									}
								}
							}
						}


					} 
					else  if (change instanceof RenameProperty)
					{
						RenameProperty rp= (RenameProperty)change;

						String propertyname= rp.getName();
						System.out.println(" Trying to link the property : " + propertyname);
						String classname=rp.getClassName();
						if(bindingInfo.size()>1)
						{
							if(bindingInfo.get(0) !=null & bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals(propertyname))
										||( bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals(propertyname))

										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										)

								{
									match=true;
								}
							}
						}
						if(bindingInfo.size()>2) {
							if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
							{
								if((bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals(propertyname))
										||( bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals(propertyname))

										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
										)

								{
									match=true;
								}

								//System.out.println(" in rename property literal case ");
								if(containOnlyCapitals(bindingInfo.get(2))) {
									String literal =makeLiteral(classname)+"__"+makeLiteral(propertyname);
									if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
											//	||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
											)
									{
										match=true;
									}
								}
							}
						}

					} 
					else if( change instanceof MoveProperty)
					{
						MoveProperty mp =(MoveProperty)change;
						String propertyname= mp.getName();
						String classname=mp.getSourceClassName();
						if(bindingInfo.size()>1)
						{
							if(bindingInfo.get(0) !=null && bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals(propertyname)) 
										|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname))) 
										||( bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname))) 
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")&& bindingInfo.get(1).equals(propertyname)) 
										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname))) 
										||( bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname))) 
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										
										)
								{
									match=true;
								}
							}
						}
						if(bindingInfo.size()>2) {
							if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
							{
								System.out.println(" in move property literal case ");
								if(containOnlyCapitals(bindingInfo.get(2))) {
									String literal =makeLiteral(classname)+"__"+makeLiteral(propertyname);
									if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
											//	||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
											)
									{
										match=true;
									}
								}
							}
						}
					}
					else if( change instanceof PullProperty)
					{
						PullProperty pllp= (PullProperty)change;
						String propertyname= pllp.getName();
						String classname=pllp.getSuperClassName();
						ArrayList<String > classes = pllp.getSubClassesNames();

						if(bindingInfo.size()>2)
						{



							for(String s : classes)
							{
								if(bindingInfo.get(0) !=null & bindingInfo.get(1) !=null )
								{
									if((bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals(propertyname))||( bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname) )) || (bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname))) ||(bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname))))
									{
										match=true;
									}
								}
								if(containOnlyCapitals(bindingInfo.get(2))) {
									if(bindingInfo.size()>2) {
										if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
										{
											System.out.println(" in pull property literal case ");
											if(containOnlyCapitals(bindingInfo.get(2))) {
												String literal =makeLiteral(s)+"__"+makeLiteral(propertyname);
												if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
														//	||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
														)
												{
													match=true;
												}
											}
										}
									}
								}
							}
						}


					}
					else if( change instanceof PushProperty)
					{
						PushProperty pshp= (PushProperty)change;
						String classname=pshp.getSuperClassName();
						String propertyname= pshp.getName();

						ArrayList<String > classes = pshp.getSubClassesNames();
						if(bindingInfo.size()>2)
						{

							if(bindingInfo.get(0) !=null && bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals(propertyname))||( bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)  )) || (bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname))) ||(bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname))))
								{
									match=true;
								}
							}

							//							for(String s : classes)
							//							{
							//								if(bindingInfo.get(0) !=null & bindingInfo.get(1) !=null )
							//								{
							//									if((bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals(propertyname))||( bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname) )) || (bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname))) ||(bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname))))
							//									{
							//										match=true;
							//									}
							//								}
							//							}
						}
						if(bindingInfo.size()>2) {
							if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
							{
								System.out.println(" in push property literal case ");
								if(containOnlyCapitals(bindingInfo.get(2))) {
									if(bindingInfo.size()>2) {
										if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
										{
											System.out.println(" in pull property literal case ");
											if(containOnlyCapitals(bindingInfo.get(2))) {
												String literal =makeLiteral(classname)+"__"+makeLiteral(propertyname);
												if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
														//	||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
														)
												{
													match=true;
												}
											}
										}
									}
								}
							}
						}
					}



					else if( change instanceof SetProperty)
					{ 
						System.out.println(" Trying to link setPoperty");

						SetProperty sp= (SetProperty)change;
						String classname=sp.getContainer(); // case : FD
						String propertyname= sp.getName();
						if(bindingInfo.size()>1)
						{
							if(bindingInfo.get(0) !=null & bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals(propertyname))
										||( bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals(propertyname))

										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										)

								{
									match=true;
								}
							}
						}
						if(bindingInfo.size()>2) {
							if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
							{
								String literal= makeLiteral(classname)+"__"+makeLiteral(propertyname);
								System.out.println(" in set property literal case ");
								if(containOnlyCapitals(bindingInfo.get(2))) {
									if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(makeLiteral(propertyname)))
											||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
											)
									{
										match=true;
									}
								}
								if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
								{
									if((bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals(propertyname))
											||( bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
											|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals(propertyname))

											|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
											|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
											)

									{
										match=true;
									}
								}
							}
						}


					}
					else if( change instanceof ChangeUpperBound)
					{ 
						System.out.println(" Trying to link setPoperty");

						ChangeUpperBound sp= (ChangeUpperBound)change;
						String classname=sp.getClassName(); // case : FD
						String propertyname= sp.getName();
						if(bindingInfo.size()>1)
						{
							if(bindingInfo.get(0) !=null & bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals(propertyname))
										||( bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals(propertyname))

										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										)

								{
									match=true;
								}
							}
						}
						if(bindingInfo.size()>2) {
							if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
							{
								String literal= makeLiteral(classname)+"__"+makeLiteral(propertyname);
								System.out.println(" in set property literal case ");
								if(containOnlyCapitals(bindingInfo.get(2))) {
									if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(makeLiteral(propertyname)))
											||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
											)
									{
										match=true;
									}
								}
								if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
								{
									if((bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals(propertyname))
											||( bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
											|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals(propertyname))

											|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
											|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
											)

									{
										match=true;
									}
								}
							}
						}


					}
					else if( change instanceof ChangeLowerBound)
					{ 
						System.out.println(" Trying to link setPoperty");

						ChangeLowerBound sp= (ChangeLowerBound)change;
						String classname=sp.getClassName(); // case : FD
						String propertyname= sp.getName();
						if(bindingInfo.size()>1)
						{
							if(bindingInfo.get(0) !=null & bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals(propertyname))
										||( bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals(propertyname))

										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										)

								{
									match=true;
								}
							}
						}
						if(bindingInfo.size()>2) {
							if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
							{
								String literal= makeLiteral(classname)+"__"+makeLiteral(propertyname);
								System.out.println(" in set property literal case ");
								if(containOnlyCapitals(bindingInfo.get(2))) {
									if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(makeLiteral(propertyname)))
											||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
											)
									{
										match=true;
									}
								}
								if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
								{
									if((bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals(propertyname))
											||( bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
											|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals(propertyname))

											|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
											|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
											)

									{
										match=true;
									}
								}
							}
						}


					}


				}

		}
		else System.out.println(" Binding info is empty!");

		return match;
	}


	public static boolean linkBindingInverseChange( ArrayList<String> bindingInfo, Change inversechange)
	{
		boolean match=false;
		if(!bindingInfo.isEmpty()) // if the binding is well treated 
		{
			//if (inversechange instanceof DeleteClass)
			//{
			if (inversechange instanceof AddClass)
			{
				AddClass dc = (AddClass)inversechange;

				String classname = dc.getName();
				if(bindingInfo.get(0)!=null)
				{
					if(bindingInfo.get(0).equals(classname) || bindingInfo.get(0).equals(classname+"Impl")  )
					{
						match=true;
					}
				}else if(bindingInfo.get(1)!=null)
				{
					if(bindingInfo.get(1).equals("get"+classname)  || bindingInfo.get(1).equals("set"+classname) || bindingInfo.get(1).equals("create"+classname)  )
					{
						match=true;
					}

				}
				else  if(bindingInfo.get(2)!=null){
					//System.out.println(" in delete class literal");
					if(containOnlyCapitals(bindingInfo.get(2)) )// Check if Literal
					{
						if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).contains(makeLiteral(classname)))
								//	||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
								)
						{
							match=true;
						}
					}

				}

			}
			else  if (inversechange instanceof RenameClass)
			{
				RenameClass rc =(RenameClass)inversechange;
				String classname = rc.getNewname()
						;
				if(bindingInfo.get(0)!=null){
					if(bindingInfo.get(0).equals(classname) || bindingInfo.get(0).equals(classname+"Impl") )
					{
						match=true;
					}
				}else if(bindingInfo.get(1)!=null){
					if(bindingInfo.get(1).equals("get"+classname)  
							|| bindingInfo.get(1).equals("set"+classname) ||
							bindingInfo.get(1).equals("create"+classname)  )
					{
						match=true;
					}

				}
				else  if(bindingInfo.get(2)!=null){
					System.out.println(" in rename class literal");
					if(containOnlyCapitals(bindingInfo.get(2)) )// Check if Literal
					{
						if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).contains(makeLiteral(classname)))
								//	||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
								)
						{
							match=true;
						}
					}
				}
			}
			else
				if(bindingInfo.size()>1) {
					//if (inversechange instanceof DeleteProperty)
					if (inversechange instanceof AddProperty)
					{
						AddProperty dp = (AddProperty)inversechange; //attribute or method
						String propertyname= dp.getName();
						String classname=dp.getClassName();
						//	System.out.println(" in Delete property lining "+ propertyname+ " the binding info 1 is" +bindingInfo.get(1));
						if(bindingInfo.size()>1) {

							if(bindingInfo.get(0) !=null & bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals(propertyname) )
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals(propertyname)) )
								{
									match=true;
								}

							}
						}
						if(bindingInfo.size()>2) {

							if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
							{

								if((bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals(propertyname) )||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals(propertyname))
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("get"+capitalizeFirstLetter( propertyname)))
										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
										)

								{
									match=true;
								}


								if(containOnlyCapitals(bindingInfo.get(2))) {
									System.out.println(" in add property literal case ");
									String literal =makeLiteral(classname)+"__"+makeLiteral(propertyname);
									if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).contains(literal))
											//	||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
											)
									{
										match=true;
									}
								}
							}
						}


					} 
					else  if (inversechange instanceof RenameProperty)
					{
						RenameProperty rp= (RenameProperty)inversechange;

						String propertyname= rp.getNewname();
						//System.out.println(" Trying to link inverse the property : " + propertyname);
						String classname=rp.getClassName();
						if(bindingInfo.size()>1)
						{
							if(bindingInfo.get(0) !=null & bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals(propertyname))
										||( bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals(propertyname))

										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										)

								{
									match=true;
								}
							}
						}
						if(bindingInfo.size()>2) {
							if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
							{
								if((bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals(propertyname))
										||( bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals(propertyname))

										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
										)

								{
									match=true;
								}

								//System.out.println(" in rename property literal case ");
								if(containOnlyCapitals(bindingInfo.get(2))) {
									String literal =makeLiteral(classname)+"__"+makeLiteral(propertyname);
									if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).contains(literal))
											//	||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
											)
									{
										match=true;
									}
								}
							}
						}

					} 
					else if( inversechange instanceof MoveProperty)
					{
						MoveProperty mp =(MoveProperty)inversechange;
						String propertyname= mp.getName();
						String classname=mp.getTargetClassName();
						
						if(bindingInfo.size()>1)
						{
							if(bindingInfo.get(0) !=null && bindingInfo.get(1) !=null )
							{
								System.out.println(" in move property  "+ bindingInfo.get(0)+ "  And "+bindingInfo.get(1));
								System.out.println(" You compare  : "+"set"+capitalizeFirstLetter(propertyname));
								if((bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals(propertyname)) 
										|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										||( bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname))) 
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")&& bindingInfo.get(1).equals(propertyname))
										||( bindingInfo.get(0).equals(classname+"Impl")&& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname) )) 
										|| (bindingInfo.get(0).equals(classname+"Impl")&& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname))) 
										||(bindingInfo.get(0).equals(classname+"Impl")&& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))

										)
								{
									match=true;
								}
							}
						}
						if(bindingInfo.size()>2) {
							if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
							{
								System.out.println(" in move property  2 "+ bindingInfo.get(0)+ "  And "+bindingInfo.get(2));
								
								System.out.println(" in move property literal case ");
								if(containOnlyCapitals(bindingInfo.get(2))) {
									String literal =makeLiteral(classname)+"__"+makeLiteral(propertyname);
									if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).contains(literal))
											//	||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
											)
									{
										match=true;
									}
								}
							}
							if(bindingInfo.get(0) !=null && bindingInfo.get(1) !=null )
							{
								System.out.println(" in move property  "+ bindingInfo.get(0)+ "  And "+bindingInfo.get(2));
								if((bindingInfo.get(0).equals(classname)&& bindingInfo.get(2).equals(propertyname)) 
										|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
										||( bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname))) 
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")&& bindingInfo.get(2).equals(propertyname))
										||( bindingInfo.get(0).equals(classname+"Impl")&& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname) )) 
										|| (bindingInfo.get(0).equals(classname+"Impl")&& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname))) 
										||(bindingInfo.get(0).equals(classname+"Impl")&& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))

										)
								{
									match=true;
								}
							}
						}
					}
					else if( inversechange instanceof PushProperty)
					{
						PushProperty pllp= (PushProperty)inversechange;
						String propertyname= pllp.getName();
						String classname=pllp.getSuperClassName();
						ArrayList<String > classes = pllp.getSubClassesNames();

						if(bindingInfo.size()>1)
						{



							for(String s : classes)
							{
								if(bindingInfo.get(0) !=null & bindingInfo.get(1) !=null )
								{
									if((bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals(propertyname))
											||( bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname) )) 
											|| (bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname))) 
											||(bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(s+"Impl")&& bindingInfo.get(1).equals(propertyname))
											||( bindingInfo.get(0).equals(s+"Impl")&& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname) )) 
											|| (bindingInfo.get(0).equals(s+"Impl")&& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname))) 
											||(bindingInfo.get(0).equals(s+"Impl")&& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))


											)
									{
										match=true;
									}
								}
								if(bindingInfo.size()>2)
								{

								if(containOnlyCapitals(bindingInfo.get(2))) {
									if(bindingInfo.size()>2) {
										if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
										{
											System.out.println(" in pull property literal case ");
											if(containOnlyCapitals(bindingInfo.get(2))) {
												String literal =makeLiteral(s)+"__"+makeLiteral(propertyname);
												if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).contains(literal))
														//	||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
														)
												{
													match=true;
												}
											}
										}
									}
								}
								}
							}
						}


					}
					else if( inversechange instanceof PullProperty)
					{
						PullProperty pshp= (PullProperty)inversechange;
						String classname=pshp.getSuperClassName();
						String propertyname= pshp.getName();

						ArrayList<String > classes = pshp.getSubClassesNames();
						if(bindingInfo.size()>1)
						{

							if(bindingInfo.get(0) !=null && bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals(propertyname))
										||( bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)  ))
										|| (bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")&& bindingInfo.get(1).equals(propertyname))
										||( bindingInfo.get(0).equals(classname+"Impl")&& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)  ))
										|| (bindingInfo.get(0).equals(classname+"Impl")&& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")&& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))

										)

								{
									match=true;
								}
							}

							//							for(String s : classes)
							//							{
							//								if(bindingInfo.get(0) !=null & bindingInfo.get(1) !=null )
							//								{
							//									if((bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals(propertyname))||( bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname) )) || (bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname))) ||(bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname))))
							//									{
							//										match=true;
							//									}
							//								}
							//							}
						}
						if(bindingInfo.size()>2) {
							if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
							{
								System.out.println(" in push property literal case ");
								if(containOnlyCapitals(bindingInfo.get(2))) {
									if(bindingInfo.size()>2) {
										if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
										{
											System.out.println(" in pull property literal case ");
											if(containOnlyCapitals(bindingInfo.get(2))) {
												String literal =makeLiteral(classname)+"__"+makeLiteral(propertyname);
												if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).contains(literal))
														//	||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(literal))
														)
												{
													match=true;
												}
											}
										}
									}
								}
							}
						}
					}



					else if( inversechange instanceof SetProperty)
					{ 
						//	System.out.println(" Trying to link setPoperty");

						SetProperty sp= (SetProperty)inversechange;
						String classname=sp.getContainer(); // case : FD
						String propertyname= sp.getName();
						if(bindingInfo.size()>1)
						{
							if(bindingInfo.get(0) !=null & bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals(propertyname))
										||( bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals(propertyname))

										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										)

								{
									match=true;
								}
							}
						}
						if(bindingInfo.size()>2) {
							if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
							{
								String literal= makeLiteral(classname)+"__"+makeLiteral(propertyname);
								System.out.println(" in set property literal case ");
								if(containOnlyCapitals(bindingInfo.get(2))) {
									if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(makeLiteral(propertyname)))
											||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).contains(literal))
											)
									{
										match=true;
									}
								}
								if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
								{
									if((bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals(propertyname))
											||( bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
											|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals(propertyname))

											|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
											|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
											)

									{
										match=true;
									}
								}
							}
						}


					}
					else if( inversechange instanceof ChangeUpperBound)
					{ 
						//	System.out.println(" Trying to link setPoperty");

						ChangeUpperBound sp= (ChangeUpperBound)inversechange;
						String classname=sp.getClassName(); // case : FD
						String propertyname= sp.getName();
						if(bindingInfo.size()>1)
						{
							if(bindingInfo.get(0) !=null & bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals(propertyname))
										||( bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals(propertyname))

										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										)

								{
									match=true;
								}
							}
						}
						if(bindingInfo.size()>2) {
							if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
							{
								String literal= makeLiteral(classname)+"__"+makeLiteral(propertyname);
								System.out.println(" in set property literal case ");
								if(containOnlyCapitals(bindingInfo.get(2))) {
									if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(makeLiteral(propertyname)))
											||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).contains(literal))
											)
									{
										match=true;
									}
								}
								if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
								{
									if((bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals(propertyname))
											||( bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
											|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals(propertyname))

											|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
											|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
											)

									{
										match=true;
									}
								}
							}
						}


					}
					else if( inversechange instanceof ChangeLowerBound)
					{ 
						//	System.out.println(" Trying to link setPoperty");

						ChangeLowerBound sp= (ChangeLowerBound)inversechange;
						String classname=sp.getClassName(); // case : FD
						String propertyname= sp.getName();
						if(bindingInfo.size()>1)
						{
							if(bindingInfo.get(0) !=null & bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals(propertyname))
										||( bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals(propertyname))

										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
										|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)))
										||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname)))
										)

								{
									match=true;
								}
							}
						}
						if(bindingInfo.size()>2) {
							if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
							{
								String literal= makeLiteral(classname)+"__"+makeLiteral(propertyname);
								System.out.println(" in set property literal case ");
								if(containOnlyCapitals(bindingInfo.get(2))) {
									if((bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).equals(makeLiteral(propertyname)))
											||(bindingInfo.get(0).contains("$Literals")& bindingInfo.get(2).contains(literal))
											)
									{
										match=true;
									}
								}
								if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
								{
									if((bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals(propertyname))
											||( bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
											|| (bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals(propertyname))

											|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname)))
											|| (bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(classname+"Impl")& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname)))
											)

									{
										match=true;
									}
								}
							}
						}


					}


				}

		}
		else System.out.println(" Binding info is empty!");

		return match;
	}



	public static boolean linkBindingInverseChangeV0( ArrayList<String> bindingInfo, Change inverseChange)
	{
		boolean match=false;
		if(!bindingInfo.isEmpty()) // if the binding is well treated 
		{
			if (inverseChange instanceof AddClass)
			{
				AddClass dc = (AddClass)inverseChange;
				String classname = dc.getName();
				if(bindingInfo.get(0)!=null)
				{
					if(bindingInfo.get(0).equals(classname) || bindingInfo.get(0).equals(classname+"Impl")  )
					{
						match=true;
					}
				}else if(bindingInfo.get(1)!=null)
				{
					if(bindingInfo.get(1).equals("get"+classname)  || bindingInfo.get(1).equals("set"+classname) || bindingInfo.get(1).equals("create"+classname)  )
					{
						match=true;
					}

				}
				else  if(bindingInfo.get(2)!=null){
					System.out.println(" in delete class literal");
					if(containOnlyCapitals(bindingInfo.get(2)) )// Check if Literal
					{
						if(bindingInfo.get(2).equals(makeLiteral(classname)))
						{
							match=true;
						}
					}

				}

			}

			else if (inverseChange instanceof RenameClass)
			{
				RenameClass rc =(RenameClass)inverseChange;
				String newclassname = rc.getNewname();
				if(bindingInfo.get(0)!=null){
					if(bindingInfo.get(0).equals(newclassname)  )
					{
						match=true;
					}
				}else if(bindingInfo.get(1)!=null){
					if(bindingInfo.get(1).equals("get"+newclassname)  || bindingInfo.get(1).equals("set"+newclassname) || bindingInfo.get(1).equals("create"+newclassname))
					{
						match=true;
					}

				}
				else  if(bindingInfo.get(2)!=null){
					/*	if( )// Check if Literal
						{
							match=true;
						}
					 */
				}
			}
			else
				if(bindingInfo.size()>1) {
					if (inverseChange instanceof RenameProperty)
					{
						RenameProperty rp= (RenameProperty)inverseChange;

						String newpropertyname= rp.getNewname();
						String classname=rp.getClassName();
						if(bindingInfo.size()>2)
						{
							if(bindingInfo.get(0) !=null & bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals(newpropertyname) )||( bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(newpropertyname)  )) || (bindingInfo.get(0).equals(classname)& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(newpropertyname))))
								{
									match=true;
								}
							}
						}
					} 
					else if( inverseChange instanceof MoveProperty)
					{
						MoveProperty mp =(MoveProperty)inverseChange;
						String propertyname= mp.getName();
						String classname=mp.getTargetClassName();
						if(bindingInfo.size()>2)
						{
							if(bindingInfo.get(0) !=null && bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals(propertyname))||( bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)  )) || (bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname))) ||(bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname))))
								{
									match=true;
								}
							}
						}
					}
					else if( inverseChange instanceof PullProperty)
					{
						PullProperty pllp= (PullProperty)inverseChange;
						String propertyname= pllp.getName();
						String classname=pllp.getSuperClassName();
						if(bindingInfo.size()>2)
						{
							if(bindingInfo.get(0) !=null && bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals(propertyname))||( bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)  )) || (bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname))) ||(bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname))))
								{
									match=true;
								}
							}
						}


					}
					else if( inverseChange instanceof PushProperty)
					{
						PushProperty pshp= (PushProperty)inverseChange;
						String classname=pshp.getSuperClassName();
						String propertyname= pshp.getName();

						ArrayList<String > classes = pshp.getSubClassesNames();
						if(bindingInfo.size()>2)
						{

							for(String s : classes)
							{
								if(bindingInfo.get(0) !=null & bindingInfo.get(1) !=null )
								{
									if((bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals(propertyname))
											||( bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname) )) 
											|| (bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname)))
											||(bindingInfo.get(0).equals(s)&& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname))))
									{
										match=true;
									}
								}
							}
						}
					}



					else if( inverseChange instanceof SetProperty)
					{ 
						System.out.println(" Trying to link setPoperty");

						SetProperty sp= (SetProperty)inverseChange;
						String classname=sp.getContainer(); // case : FD
						String propertyname= sp.getName();
						if(bindingInfo.size()>2)
						{
							if(bindingInfo.get(0) !=null && bindingInfo.get(1) !=null )
							{
								if((bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals(propertyname))||( bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals("get"+capitalizeFirstLetter(propertyname)  )) || (bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals("set"+capitalizeFirstLetter(propertyname))) ||(bindingInfo.get(0).equals(classname)&& bindingInfo.get(1).equals("is"+capitalizeFirstLetter(propertyname))))
								{
									match=true;
								}
							}
						}
						if(bindingInfo.size()>2) {
							if(bindingInfo.get(0) !=null && bindingInfo.get(2) !=null )
							{
								System.out.println(" in set property literal case ");
								if(containOnlyCapitals(bindingInfo.get(2))) {
									if(bindingInfo.get(0).equals(classname)& bindingInfo.get(2).equals(makeLiteral(propertyname)))
									{
										match=true;
									}
								}
								if((bindingInfo.get(0).equals(classname)&& bindingInfo.get(2).equals(propertyname))||( bindingInfo.get(0).equals(classname)&& bindingInfo.get(2).equals("get"+capitalizeFirstLetter(propertyname)  )) || (bindingInfo.get(0).equals(classname)&& bindingInfo.get(2).equals("set"+capitalizeFirstLetter(propertyname))) ||(bindingInfo.get(0).equals(classname)&& bindingInfo.get(2).equals("is"+capitalizeFirstLetter(propertyname))))
								{
									match=true;
								}
							}
						}


					}
				}

		}
		else System.out.println(" Binding info is empty!");

		return match;
	}


	public static void matchBindingChange0(Change change)

	{
		int i = 0;
		RenameProperty rp = new RenameProperty();
		if (change instanceof RenameProperty) {
			rp = (RenameProperty) change;
		}

		for (IBinding key : JavaVisitor.getManageBindings().getBindingsNodes().keySet()) {
			System.out.println("key_" + i + " >> " + key);
			if (key != null) {
				// System.out.println(" key key>> "+key.getKey());
				System.out.println("    key class >> " + key.getClass());
				i++;

				int pos = 0;
				for (ASTNode node : JavaVisitor.getManageBindings().getBindingsNodes().get(key)) {
					System.out.println(" key key>> " + key.getKey() + "			node_" + pos + " >> " + node
							+ " >> parent >> " + node.getParent());

					if (change instanceof RenameClass) {
						if (((SimpleName) node).getIdentifier().equals(((RenameClass) change).getName())) {
							System.out.println(" Impacted node detected !");
							TypeDeclaration td = (TypeDeclaration) ASTManager.findTypeDeclaration(node);
							MethodDeclaration md = (MethodDeclaration) ASTManager.findMethodDeclaration(node);
							FieldDeclaration fd = (FieldDeclaration) ASTManager.findFieldDeclaration(node);

							if (md != null) {
								if (md.resolveBinding().getAnnotations() != null) {
									if (md.resolveBinding().getAnnotations().length != 0) {

										if (md.resolveBinding().getAnnotations()[0].getName().equals("Test")) {
											System.out.println(" Test Method detected !  " + node.getStartPosition()
											+ " in Class "
											+ md.resolveBinding().getDeclaringClass().getQualifiedName());

											// try {
											// // System.out.println("adding " + iCompilUnittest.getAbsolutePath() + "
											// to classpath");
											// File jarFile = new
											// File("/home/zkebaili/runtime-New_configuration(6)/BasicProject2/bin/MyPoint.class");
											// URL u = jarFile.toURI().toURL();
											// URLClassLoader sysloader = (URLClassLoader)
											// ClassLoader.getSystemClassLoader();
											//
											// /* Here is a trick to call URLClassLoader.addURL whereas this method is
											// 'protected' */
											// Class sysclass = URLClassLoader.class;
											// Method method = sysclass.getDeclaredMethod("addURL", new
											// Class[]{URL.class});
											// method.setAccessible(true);
											// method.invoke(sysloader, new Object[] { u });
											// } catch (Exception e) {
											// System.out.println("Error while updating classpath: "+ e.toString());
											// }
											// testLoad();
											// URL classUrl;
											// classUrl = new
											// URL("/home/zkebaili/runtime-New_configuration(6)/BasicProject2/bin/myPackage/MyPoint");
											// //This is location of .class file
											// URL[] classUrls = {classUrl};
											// URLClassLoader ucl = new URLClassLoader(classUrls);
											// Class cls = ucl.loadClass("myPackage.MyPoint"); // Current .class files
											// name
											// URLClassLoader classLoader = new URLClassLoader (yourJar.toURL(),
											// this.getClass().getClassLoader());
											// Class c = Class.forName ("myPackage.MyPoint", true, classLoader);
											// Class<?>c = Class.forName("myPackage.MyPoint");
											// File f = new
											// File("/home/zkebaili/runtime-New_configuration(6)/BasicProject2/bin/myPackage/MyPoint");
											// URI u = f.toURI();
											// URLClassLoader urlClassLoader = (URLClassLoader)
											// ClassLoader.getSystemClassLoader();
											// Class<URLClassLoader> urlClass = URLClassLoader.class;
											// Method method = urlClass.getDeclaredMethod("addURL", new
											// Class[]{URL.class});
											// method.setAccessible(true);
											// method.invoke(urlClassLoader, new Object[]{u.toURL()});

											// Request request = Request.method(c, "testSetGetX");
											// new JUnitCore().run(request);

											// Class<?>c = Class.forName("myPackage.MyPoint");
											// TestsRunner.runTests(cls);
											/*** fetch usages ***/
											ArrayList<ASTNode> nodes = mapKeyNodes(key.getKey());
											for (ASTNode usagenode : nodes) {
												System.out.println(" The size of usages is  " + nodes.size());

												if (ASTManager.findTypeDeclaration(usagenode) == null)
													System.out.println(" td null : usage in package"
															+ usagenode.getStartPosition());
												else if (ASTManager.findTypeDeclaration(usagenode) != null) {
													System.out
													.println(
															"usages : "
																	+ ((SimpleName) usagenode).getIdentifier()
																	+ " the source location  :  "
																	+ ((TypeDeclaration) ASTManager
																			.findTypeDeclaration(usagenode))
																	.getName());
													if (ASTManager.findMethodDeclaration(usagenode) != null) {
														System.out.println(
																"usage in MD :" + ((MethodDeclaration) ASTManager
																		.findMethodDeclaration(usagenode)).getName());
														ArrayList<ASTNode> method_usages = JavaVisitor
																.getManageBindings().getBindingsNodes()
																.get(((MethodDeclaration) ASTManager
																		.findMethodDeclaration(usagenode))
																		.resolveBinding());
														for (ASTNode musage : method_usages) {
															System.out.println(" The size of musages is  "
																	+ method_usages.size() + " the musage class is "
																	+ musage.getClass());
															/*
															 * MethodInvocation mi = (MethodInvocation)
															 * ASTManager.findMethodInvocation(musage); if(mi !=null) {
															 * 
															 * }
															 */
														}
													}

												}

											}

										}
									}
								}

							} else if (fd != null)// useless case?
							{
								if (td != null) {
									if (td.getName().getIdentifier().contains(((RenameClass) change).getName())) {
										System.out.println(" Test Class detected !  ");

									}
								}
							}

						}

					} else if (change instanceof RenameProperty) {

					} else if (change instanceof DeleteClass) {

					}

					pos++;
				}
			}

		}
	}
	public static String capitalizeFirstLetter(String s)
	{

		String firstLetStr = s.substring(0, 1);
		// Get remaining letter using substring
		String remLetStr = s.substring(1);

		// convert the first letter of String to uppercase
		firstLetStr = firstLetStr.toUpperCase();

		// concantenate the first letter and remaining string
		return firstLetStr + remLetStr;
	}

}
