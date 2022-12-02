package fr.lip6.meta.tracerecorder.views;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.presentation.EcoreEditor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
//import org.eclipse.emf.ecoretools.diagram.part.EcoreDiagramEditor;
//import org.eclipse.sirius.diagram.ui.tools.internal.editor.DDiagramEditorImpl;
import org.eclipse.sirius.diagram.ui.tools.api.editor.DDiagramEditor;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.papyrus.editor.PapyrusMultiDiagramEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.uml.editor.presentation.UMLEditor;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;

import fr.lip6.meta.ComplexChangeDetection.Change;
import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.*;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractSuperClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.FlattenHierarchy;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.InlineClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.InverseMoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PushProperty;
import fr.lip6.meta.ComplexChangeDetection.Detection.DetectComplexChange;
import fr.lip6.meta.ComplexChangeDetection.EvolutionTrace.FinalEvolutionTrace;
import fr.lip6.meta.ComplexChangeDetection.EvolutionTrace.ParseEvolutionTrace;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionComplexMove;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionExtract;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionExtractSuper;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionFlattenHierarchy;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionHiddenMoveV1;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionInline;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionInverseMove;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionPull;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.DefinitionPush;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.GenericDefinition;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.GenericDetection;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.Undo.DefinitionUndoAddProperty;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.Undo.DefinitionUndoDeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.GenericDetection.Undo.DefinitionUndoPull;
import fr.lip6.meta.ComplexChangeDetection.Heuristics.Distance;
import fr.lip6.meta.ComplexChangeDetection.Overlap.DetectOverlap;
import fr.lip6.meta.ComplexChangeDetection.PraxisTrace.ParsePraxisTrace;
import fr.lip6.meta.ComplexChangeDetection.PraxisTrace.ParsePraxisTraceEcore;
import fr.lip6.meta.ComplexChangeDetection.PraxisTrace.ParsePraxisTraceUMLCDPapyrus;
import fr.lip6.meta.ComplexChangeDetection.UndoChanges.UndoAddProperty;
import fr.lip6.meta.ComplexChangeDetection.UndoChanges.UndoDeleteProperty;
import fr.lip6.meta.ComplexChangeDetection.UndoChanges.UndoPull;
import fr.lip6.meta.ComplexChangeDetection.Utils.InitialState;
import fr.lip6.meta.traceimport.ExtensionTraceImport;
import fr.lip6.meta.traceimport.RawTraceImport;
import fr.lip6.meta.tracerecorder.listner.ActionListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Combo;



public class PraxisTraceRecorder extends ViewPart {

	public static final String ID = "tracerecorder.views.PraxisTraceRecorder"; //$NON-NLS-1$
	//PraxisTraceRecorder view = null;
	private Label lblPraxisTrace;
	private Label lblFiltrerdTrace;
	private List ListPraxisTrace;
	private List listFiltredTrace;
	private Label praxisSize;
	private Label filtredSize;
	private Tree tree;
	private TreeItem item1;
	private TreeItem item2;
	private TreeItem item3;
	private TreeItem item4;
	private TreeItem item5;
	private TreeItem item6;
	private TreeItem item7;
	private TreeItem item8;
	private TreeItem item9;
	private Button btnStartRecording;
	private int intialTraceSize = 0;
	private boolean first = true;
	private ScrolledComposite scrolledComposite_2;
	private ActionListener actionLinstner = null;
	private ArrayList<String> praxisTrace = null;
	private ArrayList<String> parsedTrace = null;
	private Label lblDetectedComplexChanges;
	private Label lblInitialPraxisTrace;
	private Button btnReset;
	private Button btnTestcheking;
	
	private ArrayList<AtomicChange> atomicChanges = null;
	private ArrayList<ComplexChange> complexChanges = null;
	private ArrayList<Change> finalChanges = null;
	private ArrayList<AtomicChange> importedAtomicChanges = null;
	private ArrayList<String> importedStringAtomicChanges = null;
	private boolean isImported = false;
	
	
	private String editor = "";
	private Shell shell = null;
	public PraxisTraceRecorder() {
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		
		/*Display.getCurrent().addFilter(SWT.KeyDown, new Listener()
		{
			  public void handleEvent(Event e)
			  {
				System.out.println("1111");
			    System.out.println("Filter-ctrl: " + SWT.CTRL);
			    System.out.println("Filter-mask: " + e.stateMask);
			    System.out.println("Filter-char: " + e.character);
			    System.out.println("Filter-char: " + e.keyCode);
			  }
			});
		Display.getDefault().addFilter(SWT.KeyDown, new Listener()
		{
			  public void handleEvent(Event e)
			  {
				System.out.println("2222");
			    System.out.println("Filter-ctrl: " + SWT.CTRL);
			    System.out.println("Filter-mask: " + e.stateMask);
			    System.out.println("Filter-char: " + e.character);
			    System.out.println("Filter-char: " + e.keyCode);
			  }
			});
		container.getDisplay().addFilter(SWT.KeyDown, new Listener()
		{
			  public void handleEvent(Event e)
			  {
				System.out.println("3333");
			    System.out.println("Filter-ctrl: " + SWT.CTRL);
			    System.out.println("Filter-mask: " + e.stateMask);
			    System.out.println("Filter-char: " + e.character);
			    System.out.println("Filter-char: " + e.keyCode);
			  }
			});*/
		
		lblPraxisTrace = new Label(container, SWT.NONE);
		lblPraxisTrace.setBounds(30, 36, 157, 20);
		lblPraxisTrace.setText("Evolution Trace Length :");
		
		lblFiltrerdTrace = new Label(container, SWT.NONE);
		lblFiltrerdTrace.setBounds(30, 302, 157, 20);
		lblFiltrerdTrace.setText("Evolution Trace Length :");
		lblFiltrerdTrace.setVisible(false);
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setBounds(20, 62, 491, 197);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		listFiltredTrace = new List(scrolledComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setContent(listFiltredTrace);
		scrolledComposite.setMinSize(listFiltredTrace.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		ScrolledComposite scrolledComposite_1 = new ScrolledComposite(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite_1.setBounds(20, 328, 455, 125);
		scrolledComposite_1.setExpandHorizontal(true);
		scrolledComposite_1.setExpandVertical(true);
		scrolledComposite_1.setVisible(false);
		
		ListPraxisTrace = new List(scrolledComposite_1, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		ListPraxisTrace.setItems(new String[] {});
		scrolledComposite_1.setContent(ListPraxisTrace);
		scrolledComposite_1.setMinSize(ListPraxisTrace.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		ListPraxisTrace.setVisible(false);
		
		Button btnFilter = new Button(container, SWT.NONE);
		btnFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				praxisTrace = actionLinstner.getPraxisTrace();
				
				for(String parsed : actionLinstner.getPraxisTrace()){
					System.out.println("		 parsed = "+parsed);
					
				}
				
				ParsePraxisTrace parsePraxisTrace = null;
				
				if(editor == "EcoreDiagramEditor"){
					
					parsePraxisTrace = new ParsePraxisTraceEcore(actionLinstner.getPraxisTrace());
					
				} else if(editor == "PapyrusMultiDiagramEditor"){
					
					parsePraxisTrace = new ParsePraxisTraceUMLCDPapyrus(actionLinstner.getPraxisTrace());
					
				} 
				
				//ParsePraxisTrace parsePraxisTrace = new ParsePraxisTrace(actionLinstner.getPraxisTrace());
				parsePraxisTrace.parseTrace();
				
				parsedTrace = parsePraxisTrace.getParsedTrace();
				listFiltredTrace.removeAll();
				for(String parsed : parsePraxisTrace.getParsedTrace()){
					listFiltredTrace.add(parsed);
					
				}
				filtredSize.setText(listFiltredTrace.getItemCount()+"");
				
			}
		});
		btnFilter.setAlignment(SWT.RIGHT);
		btnFilter.setImage(ResourceManager.getPluginImage("TraceRecorder", "icon/1409149019_filter_data.png"));
		btnFilter.setBounds(494, 411, 85, 42);
		btnFilter.setText("Filter");
		btnFilter.setVisible(false);

		Button btnDetectComplexChanges = new Button(container, SWT.NONE);
		btnDetectComplexChanges.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				ParseEvolutionTrace parse = new ParseEvolutionTrace(parsedTrace);
				parse.parseTrace();
				parse.getEvolutionTrace().displayAtomicEvolutionTrace();
				parse.getEvolutionTrace().displayComplexEvolutionTrace();
				
				DetectComplexChange detection = new DetectComplexChange(actionLinstner.getInitState());
				ArrayList<ComplexChange> detectedComplexChanges = new ArrayList<ComplexChange>();
				
				ArrayList<GenericDefinition> genericDef = new ArrayList<GenericDefinition>();
				genericDef.clear();
				long s = System.currentTimeMillis();
				long sm = Runtime.getRuntime().freeMemory();
				//genericDef.add(new DefinitionMove());
				genericDef.add(new DefinitionComplexMove());
				genericDef.add(new DefinitionPull());
				genericDef.add(new DefinitionPush());
				//genericDef.add(new DefinitionExtractV0());
				genericDef.add(new DefinitionExtract());
				genericDef.add(new DefinitionExtractSuper());
				genericDef.add(new DefinitionFlattenHierarchy());
				//genericDef.add(new DefinitionHiddenMoveV1());
				
				//add inverse move (or use move in the oposite way) , and detect inline class
				
				GenericDetection generic = new GenericDetection(genericDef);
				generic.detect(parse.getEvolutionTrace().getAtomicEvolutionTrace());

				//DetectComplexChange detection = new DetectComplexChange(initState);
				//ArrayList<ComplexChange> detectedComplexChanges = new ArrayList<ComplexChange>();
				
				detectedComplexChanges = generic.validateDetectedCC(parse.getEvolutionTrace().getAtomicEvolutionTrace(), actionLinstner.getInitState(), new ArrayList<ComplexChange>());
				generic.detectMoreCC(detectedComplexChanges);
				detectedComplexChanges = generic.validateDetectedCC(parse.getEvolutionTrace().getAtomicEvolutionTrace(), actionLinstner.getInitState(), detectedComplexChanges);
				/*GenericDetection generic = new GenericDetection(genericDef);
				generic.detect(parse.getEvolutionTrace().getAtomicEvolutionTrace());
				detectedComplexChanges = generic.validateDetectedCC(parse.getEvolutionTrace().getAtomicEvolutionTrace(), initState, new ArrayList<ComplexChange>());
				generic.detectMoreCC(detectedComplexChanges);
				detectedComplexChanges = generic.validateDetectedCC(parse.getEvolutionTrace().getAtomicEvolutionTrace(), initState, detectedComplexChanges);
				*/
				System.out.println("size of detected complex changes = "+detectedComplexChanges.size());
				parse.getEvolutionTrace().displayComplexEvolutionTrace();
				
				for(ComplexChange complexchange : detectedComplexChanges){
					if(complexchange instanceof MoveProperty){
						System.out.println("delete pos = "+((MoveProperty) complexchange).getDeletePropertyPosition());
						System.out.println("add pos = "+((MoveProperty) complexchange).getAddPropertyPosition());
						System.out.println(""+((MoveProperty) complexchange).getName());
						System.out.println(""+((MoveProperty) complexchange).getSourceClassName());
						System.out.println(""+((MoveProperty) complexchange).getTargetClassName());
						System.out.println(""+((MoveProperty) complexchange).getKind());
						
					} else if(complexchange instanceof PullProperty){
						System.out.println(""+((PullProperty) complexchange).getName());
						System.out.println(""+((PullProperty) complexchange).getSuperClassName());
						System.out.println(""+((PullProperty) complexchange).getSubClassesNames().toString());
						System.out.println(""+((PullProperty) complexchange).getKind());
						
					} else if(complexchange instanceof PushProperty){
						System.out.println(""+((PushProperty) complexchange).getName());
						System.out.println(""+((PushProperty) complexchange).getSuperClassName());
						System.out.println(""+((PushProperty) complexchange).getSubClassesNames().toString());
						System.out.println(""+((PushProperty) complexchange).getKind());
						
					} else if(complexchange instanceof ExtractClass){
						System.out.println(""+((ExtractClass) complexchange).getName() );
						System.out.println(""+((ExtractClass) complexchange).getSourceClassName() );
						System.out.println(""+((ExtractClass) complexchange).getTargetClassName() );
						System.out.println(""+((ExtractClass) complexchange).getPropertiesNames().toString() );
						System.out.println(""+((ExtractClass) complexchange).getMoves());
						System.out.println(""+((ExtractClass) complexchange).getKind() );
						
					} else if(complexchange instanceof ExtractSuperClass){
						System.out.println(""+((ExtractSuperClass) complexchange).getName());
						System.out.println(""+((ExtractSuperClass) complexchange).getSuperClassName());		
						System.out.println(""+((ExtractSuperClass) complexchange).getSubClassesNames().toString());
						System.out.println(""+((ExtractSuperClass) complexchange).getPropertiesNames().toString());
						System.out.println(""+((ExtractSuperClass) complexchange).getPulles());
						System.out.println(""+((ExtractSuperClass) complexchange).getKind());
						
						
					} else if(complexchange instanceof FlattenHierarchy){
						System.out.println(""+((FlattenHierarchy) complexchange).getName());
						System.out.println(""+((FlattenHierarchy) complexchange).getSuperClassName());		
						System.out.println(""+((FlattenHierarchy) complexchange).getSubClassesNames().toString());
						System.out.println(""+((FlattenHierarchy) complexchange).getPropertiesNames().toString());
						System.out.println(""+((FlattenHierarchy) complexchange).getPushes());
						System.out.println(""+((FlattenHierarchy) complexchange).getKind());
						
					} else if(complexchange instanceof InlineClass){
						System.out.println(""+((InlineClass) complexchange).getName() );
						System.out.println(""+((InlineClass) complexchange).getSourceClassName() );
						System.out.println(""+((InlineClass) complexchange).getTargetClassName() );
						System.out.println(""+((InlineClass) complexchange).getPropertiesNames().toString() );
						System.out.println(""+((InlineClass) complexchange).getInverseMoves());
						System.out.println(""+((InlineClass) complexchange).getKind() );
						
					}
					
				}
				long et = System.currentTimeMillis();
				long em = Runtime.getRuntime().freeMemory();
				System.out.println("time = "+(et-s));
				System.out.println("memory = "+(sm-em)/1000000d);
				
				ArrayList<ComplexChange> detectedMoves = new ArrayList<ComplexChange>();
				ArrayList<ComplexChange> detectedPulls = new ArrayList<ComplexChange>();
				ArrayList<ComplexChange> detectedPushes = new ArrayList<ComplexChange>();
				ArrayList<ComplexChange> detectedExtractClass = new ArrayList<ComplexChange>();
				ArrayList<ComplexChange> detectedExtractSuperClass = new ArrayList<ComplexChange>();
				ArrayList<ComplexChange> detectedFlattenHierarchy = new ArrayList<ComplexChange>();
				ArrayList<ComplexChange> detectedInlineClass = new ArrayList<ComplexChange>();
				
				
				/*detectedMoves = detection.callMoveDetection(parse.getEvolutionTrace().getAtomicEvolutionTrace());
				detectedPulls = detection.callPullDetection(parse.getEvolutionTrace().getAtomicEvolutionTrace());
				detectedPushes = detection.callPushDetection(parse.getEvolutionTrace().getAtomicEvolutionTrace());
				detectedExtractClass = detection.callExtractDetection(detectedMoves, parse.getEvolutionTrace().getAtomicEvolutionTrace());
				detectedExtractSuperClass = detection.callExtractSuperDetection(detectedPulls, parse.getEvolutionTrace().getAtomicEvolutionTrace());
				detectedFlattenHierarchy = detection.callFlattenHierarchyDetection(detectedPushes, parse.getEvolutionTrace().getAtomicEvolutionTrace());
				detectedInlineClass = detection.callInlineDetection(parse.getEvolutionTrace().getAtomicEvolutionTrace());
				
				for(ComplexChange c : detectedExtractClass){
					detectedComplexChanges.add(c);
				}
				
				for(ComplexChange c : detectedExtractClass){
					detectedComplexChanges.add(c);
				}
				
				for(ComplexChange c : detectedExtractClass){
					detectedComplexChanges.add(c);
				}
				
				tree = new Tree(scrolledComposite_2, SWT.BORDER);
				scrolledComposite_2.setContent(tree);
				scrolledComposite_2.setMinSize(tree.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				
				tree.setLayoutData(new GridData(GridData.FILL_BOTH));
				
				item1 = new TreeItem(tree, SWT.NULL);
			    item1.setText("Moves , nbr : "+detectedMoves.size());
			    
			    for(ComplexChange c : detectedMoves){
			    	if(c instanceof MoveProperty){
			   TraceRecorder 		TreeItem itemTemp = new TreeItem(item1, SWT.NULL);
			    		itemTemp.setText("Moved Property : "+((MoveProperty)c).getName()+
			    				", Form Class : "+((MoveProperty)c).getSourceClassName()+
			    				", To Class : "+((MoveProperty)c).getTargetClassName());
			    	}
			    }
			    
			    item2 = new TreeItem(tree, SWT.NULL);
			    item2.setText("Pulls , nbr : "+detectedPulls.size());

			    for(ComplexChange c : detectedPulls){
			    	if(c instanceof PullProperty){
			    		TreeItem itemTemp = new TreeItem(item2, SWT.NULL);
			    		itemTemp.setText("Pulled Property : "+((PullProperty)c).getName()+
			    				", Form Sub Classes : "+((PullProperty)c).getSubClassesNames().toString()+
			    				", To Super Class : "+((PullProperty)c).getSuperClassName());
			    	}
			    }
			    
			    item3 = new TreeItem(tree, SWT.NULL);
			    item3.setText("Pushes , nbr : "+detectedPushes.size());

			    for(ComplexChange c : detectedPushes){
			    	if(c instanceof PushProperty){
			    		TreeItem itemTemp = new TreeItem(item3, SWT.NULL);
			    		itemTemp.setText("Pushed Property : "+((PushProperty)c).getName()+
			    				", Form Super Class : "+((PushProperty)c).getSuperClassName()+
			    				", To Sub Classes : "+((PushProperty)c).getSubClassesNames().toString());
			    	}
			    }
			    
			    item4 = new TreeItem(tree, SWT.NULL);
			    item4.setText("Extract Classes , nbr : "+detectedExtractClass.size());

			    for(ComplexChange c : detectedExtractClass){
			    	if(c instanceof ExtractClass){
			    		TreeItem itemTemp = new TreeItem(item4, SWT.NULL);
			    		itemTemp.setText("Extract Class Properties : "+((ExtractClass)c).getPropertiesNames().toString()+
			    				", Form Class : "+((ExtractClass)c).getSourceClassName()+
			    				", To Class : "+((ExtractClass)c).getTargetClassName());
			    	}
			    }
			    
			    item5 = new TreeItem(tree, SWT.NULL);
			    item5.setText("Extract Super Classes , nbr : "+detectedExtractSuperClass.size());

			    for(ComplexChange c : detectedExtractSuperClass){
			    	if(c instanceof ExtractSuperClass){
			    		TreeItem itemTemp = new TreeItem(item5, SWT.NULL);
			    		itemTemp.setText("Extract Super Class Properties : "+((ExtractSuperClass)c).getPropertiesNames()+
			    				", Form Sub Classes : "+((ExtractSuperClass)c).getSubClassesNames().toString()+
			    				", To New Super Class : "+((ExtractSuperClass)c).getSuperClassName());
			    	}
			    }
			    
			    item6 = new TreeItem(tree, SWT.NULL);
			    item6.setText("Flatten Hierarchy , nbr : "+detectedFlattenHierarchy.size());

			    for(ComplexChange c : detectedFlattenHierarchy){
			    	if(c instanceof FlattenHierarchy){
			    		TreeItem itemTemp = new TreeItem(item6, SWT.NULL);
			    		itemTemp.setText("Flatten Hierarchy : "+((FlattenHierarchy)c).getPropertiesNames().toString()+
			    				", Form Super Class : "+((FlattenHierarchy)c).getSuperClassName()+
			    				", To Sub Classes : "+((FlattenHierarchy)c).getSubClassesNames().toString());
			    	}
			    }
			    
			    item7 = new TreeItem(tree, SWT.NULL);
			    item7.setText("Inline Classes , nbr : "+detectedInlineClass.size());
			    
			    for(ComplexChange c : detectedInlineClass){
			    	if(c instanceof InlineClass){
			    		TreeItem itemTemp = new TreeItem(item7, SWT.NULL);
			    		itemTemp.setText("Inline Class Properties : "+((InlineClass)c).getPropertiesNames().toString()+
			    				", Form Class : "+((InlineClass)c).getSourceClassName()+
			    				", To Class : "+((InlineClass)c).getTargetClassName());
			    		
			    	}
			    }
			    
				System.out.println("size move = "+detectedMoves.size());
				System.out.println("size pull = "+detectedPulls.size());
				System.out.println("size push = "+detectedPushes.size());
				System.out.println("size extract = "+detectedExtractClass.size());
				System.out.println("size extract super = "+detectedExtractSuperClass.size());
				System.out.println("size flatten hierarcy = "+detectedFlattenHierarchy.size());
				System.out.println("size inline = "+detectedInlineClass.size());
				*/
			}
		});
		btnDetectComplexChanges.setImage(ResourceManager.getPluginImage("TraceRecorder", "icon/1409165452_folder-search.png"));
		btnDetectComplexChanges.setBounds(593, 401, 216, 52);
		btnDetectComplexChanges.setText("Launch Detection Engine");
		btnDetectComplexChanges.setVisible(false);
		
		btnStartRecording = new Button(container, SWT.NONE);
		btnStartRecording.setImage(ResourceManager.getPluginImage("TraceRecorder", "icon/1423503066_Play1Hot.png"));
		btnStartRecording.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				System.out.println("active editor = "+activeEditor);
				String workspace = ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toPortableString();
			    System.out.println(" workspace path = "+workspace);
			    
				if(activeEditor instanceof  UMLEditor){
					UMLEditor umlEditor = (UMLEditor) activeEditor;
					 //TraceActionListner trace = new TraceActionListner();
					 Set<Resource> resourcesSet = new HashSet<Resource>();
					for(Resource r : umlEditor.getEditingDomain().getResourceSet().getResources()){
						System.out.println(" UML 1 = "+r);
						System.out.println(" UML 2 [0] = "+r.getContents().get(0));
						//r.getContents().get(0).eAdapters().add(trace.getAdapter());
						resourcesSet.add(r);
						File fileTrace = new File(workspace+r.getURI().path().split("/resource")[1]+".trace.txt");
						
						try {
							fileTrace.createNewFile();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						actionLinstner = new ActionListener(resourcesSet, null, false, getView(), fileTrace);
					}
				} else if(activeEditor instanceof PapyrusMultiDiagramEditor){
					PapyrusMultiDiagramEditor papEditor = (PapyrusMultiDiagramEditor) activeEditor;
					//TraceActionListner trace = new TraceActionListner();
					 Set<Resource> resourcesSet = new HashSet<Resource>();
					//for(Resource r : papEditor.getEditingDomain().getResourceSet().getResources()){
						Resource r = papEditor.getEditingDomain().getResourceSet().getResources().get(2);
						for(Resource re : papEditor.getEditingDomain().getResourceSet().getResources()){
							System.out.println(" 	for UML 1 = "+re);
							System.out.println(" 	for UML 2 [0] = "+re.getContents().get(0));
						}
						System.out.println(" UML 1 = "+r);
						System.out.println(" UML 2 [0] = "+r.getContents().get(0));
						
						EContentAdapter adapter1 = new EContentAdapter() {
						      public void notifyChanged(Notification notification) {
						        super.notifyChanged(notification);
						        System.out
						            .println("Notfication received from the data model. Data model has changed!!!");
						        System.out
					          .println(notification.toString());
						       
						      }
						      
						    };
						    
						    File fileTrace = new File(workspace+r.getURI().path().split("/resource")[1]+".trace.txt");
							
							try {
								fileTrace.createNewFile();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						    
						r.getContents().get(0).eAdapters().add(adapter1);//trace.getAdapter());
						resourcesSet.add(r);
						actionLinstner = new ActionListener(resourcesSet, null, false, getView(), fileTrace);
						
						//here we set the editor, so that the trace pareser will be chosen accordingly
						editor = "PapyrusMultiDiagramEditor";
					//}
					
				} else if(activeEditor instanceof EcoreEditor){
					EcoreEditor ecoreEditor = (EcoreEditor) activeEditor;
					//ecoreEditor.getEditingDomain().getResourceSet().getResources();
					EContentAdapter adapter1 = new EContentAdapter() {
					      public void notifyChanged(Notification notification) {
					        super.notifyChanged(notification);
					        System.out
					            .println("Notfication received from the data model. Data model has changed!!!");
					        System.out
				          .println(notification.toString());
					      }
					    };
					    
					   // TraceActionListner trace = new TraceActionListner();
					    
					for(Resource r : ecoreEditor.getEditingDomain().getResourceSet().getResources()){
						System.out.println(" r1 = "+r);
						System.out.println(" r2 [0] = "+r.getContents().get(0));
						r.getContents().get(0).eAdapters().add(adapter1);
						
						Set<Resource> resourcesSet = new HashSet<Resource>();

						File fileTrace = new File(workspace+r.getURI().path().split("/resource")[1]+".trace.txt");
						
						try {
							fileTrace.createNewFile();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						resourcesSet.add(r);
						actionLinstner = new ActionListener(resourcesSet, null, true, getView(), fileTrace);
						
						//here we set the editor, so that the trace pareser will be chosen accordingly
						editor = "EcoreDiagramEditor";
					}
					
					
					
				} else if (activeEditor instanceof DDiagramEditor) {
					DDiagramEditor dDiagramEditor = (DDiagramEditor) activeEditor;
					dDiagramEditor.getEditingDomain().getResourceSet().getResources();
					
					System.out.println(dDiagramEditor.getEditorInput());
					System.out.println(dDiagramEditor.getEditorSite());
					System.out.println(dDiagramEditor.getPaletteManager());
					//System.out.println(dDiagramEditor.getPermissionAuthority());
					
					
					System.out.println("" + dDiagramEditor.getRepresentation());
					//System.out.println("/n" + dDiagramEditor.getRepresentation().getRepresentationElements());
					//System.out.println("/n" + dDiagramEditor.getRepresentation().getOwnedRepresentationElements());
					//System.out.println("/n" + dDiagramEditor.getRepresentation().eResource());
					System.out.println("" + dDiagramEditor.getRepresentation().eResource().getResourceSet());
					//System.out.println("/n" + dDiagramEditor.getRepresentation().eContents());
					
					//System.out.println(dDiagramEditor.getSaveables());
					//System.out.println(dDiagramEditor.getSession());
					//System.out.println(dDiagramEditor.getSite());
					//System.out.println(dDiagramEditor.getTabBarManager());
			
					EContentAdapter adapter1 = new EContentAdapter() {
					      public void notifyChanged(Notification notification) {
					        super.notifyChanged(notification);
					        System.out
					            .println("Notfication received from the data model. Data model has changed!!!");
					        System.out
				          .println(notification.toString());
					       
					      }
					      
					    };
					    
					    //  TraceActionListner trace = new TraceActionListner();
					    
					    int i = 0;
					    Set<Resource> resourcesSet = new HashSet<Resource>();

					    //dDiagramEditor.getEditingDomain().g
					for(Resource r : dDiagramEditor.getRepresentation().eResource().getResourceSet().getResources()){//dDiagramEditor.getEditingDomain().getResourceSet().getResources()){
						//if(r instanceof org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl){
						
						System.out.println(r.getURI().toString());
						if(i == 3){//r.getURI().toString().contains(".ecore") && i == 0) {//i != 0
							
							//XMIResourceImpl rr = (XMIResourceImpl) new XMIResourceFactoryImpl().createResource(r.getURI());
							EcoreResourceFactoryImpl ef = new EcoreResourceFactoryImpl();
							Resource ecore = ef.createResource(r.getURI());
							try {
								ecore.load(null);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								//e1.printStackTrace();
								System.out.println(" error while loding the ecore = ");
							}
							System.out.println(" ecore = "+ecore);
							System.out.println(" ecore size = "+ecore.getContents().size());
							System.out.println(" ecore = "+ecore.getContents());
							
							System.out.println(" rr = "+r.getURI());
							System.out.println(" r2 = "+r.getContents());
							System.out.println(" r2 = "+r);
							System.out.println(" r2 = "+r.getURI().path());
							//System.out.println(" r2 [0] = "+r.getContents().get(0));
							
							File fileTrace = new File(workspace+r.getURI().path().split("/resource")[1]+".trace.txt");
							
							
							try {
								fileTrace.createNewFile();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
							
							r.getContents().get(0).eAdapters().add(adapter1);
							resourcesSet.add(r);
							actionLinstner = new ActionListener(resourcesSet, null, true, getView(), fileTrace);
							
							//here we set the editor, so that the trace pareser will be chosen accordingly
							editor = "EcoreDiagramEditor";
						}
						//else 
						i ++;
						//}
					}
				}
				/*else if (activeEditor instanceof EcoreDiagramEditor){
					EcoreDiagramEditor ecoreDiagramEditor = (EcoreDiagramEditor) activeEditor;
					//ecoreEditor.getEditingDomain().getResourceSet().getResources();
					EContentAdapter adapter1 = new EContentAdapter() {
					      public void notifyChanged(Notification notification) {
					        super.notifyChanged(notification);
					        System.out
					            .println("Notfication received from the data model. Data model has changed!!!");
					        System.out
				          .println(notification.toString());
					       
					      }
					      
					    };
					    
					    //  TraceActionListner trace = new TraceActionListner();
					    
					    int i = 0;
					    Set<Resource> resourcesSet = new HashSet<Resource>();

					    
					for(Resource r : ecoreDiagramEditor.getEditingDomain().getResourceSet().getResources()){
						//if(r instanceof org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl){
						
						
						if(i != 0) {
							
							//XMIResourceImpl rr = (XMIResourceImpl) new XMIResourceFactoryImpl().createResource(r.getURI());
							EcoreResourceFactoryImpl ef = new EcoreResourceFactoryImpl();
							Resource ecore = ef.createResource(r.getURI());
							try {
								ecore.load(null);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								//e1.printStackTrace();
								System.out.println(" error while loding the ecore = ");
							}
							System.out.println(" ecore = "+ecore);
							System.out.println(" ecore size = "+ecore.getContents().size());
							System.out.println(" ecore = "+ecore.getContents());
							
							System.out.println(" rr = "+r.getURI());
							System.out.println(" r2 = "+r.getContents());
							System.out.println(" r2 = "+r);
							System.out.println(" r2 = "+r.getURI().path());
							//System.out.println(" r2 [0] = "+r.getContents().get(0));
							
							File fileTrace = new File(workspace+r.getURI().path().split("/resource")[1]+".trace.txt");
							
							try {
								fileTrace.createNewFile();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
							
							r.getContents().get(0).eAdapters().add(adapter1);
							resourcesSet.add(r);
							actionLinstner = new ActionListener(resourcesSet, null, true, getView(), fileTrace);
							
							//here we set the editor, so that the trace pareser will be chosen accordingly
							editor = "EcoreDiagramEditor";
						}
						else i ++;
						//}
					}
				} else {
					Set<Resource> resourcesSet = new HashSet<Resource>();
					
				}
*/
				if(first){
					intialTraceSize = ListPraxisTrace.getItemCount();
					first = false;
					lblInitialPraxisTrace.setText(lblInitialPraxisTrace.getText()+" "+intialTraceSize);
					praxisSize.setText(""+0);
					praxisSize.setVisible(true);
				}
				btnStartRecording.setEnabled(false);
				
				//btnStartRecording.setVisible(false);
				//btnReset.setVisible(false);
				lblInitialPraxisTrace.setVisible(false);
				//lblPraxisTrace.setVisible(false);
				//praxisSize.setVisible(false);/**/
				finish = true;
			}
		});
		btnStartRecording.setBounds(9, 0, 147, 30);
		btnStartRecording.setText("Start Recording");
		
		praxisSize = new Label(container, SWT.NONE);
		praxisSize.setBounds(208, 265, 52, 20);
		praxisSize.setText("0");
		praxisSize.setVisible(false);

		filtredSize = new Label(container, SWT.NONE);
		filtredSize.setBounds(208, 36, 70, 20);
		filtredSize.setText("0");
		
		scrolledComposite_2 = new ScrolledComposite(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite_2.setBounds(517, 36, 429, 223);
		scrolledComposite_2.setExpandHorizontal(true);
		scrolledComposite_2.setExpandVertical(true);
		
		tree = new Tree(scrolledComposite_2, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		scrolledComposite_2.setContent(tree);
		scrolledComposite_2.setMinSize(tree.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.print("\n => here click on the tree");
			}
			
		});
		
		lblDetectedComplexChanges = new Label(container, SWT.NONE);
		lblDetectedComplexChanges.setBounds(517, 5, 204, 20);
		lblDetectedComplexChanges.setText("Detected Complex Changes :");
		
		lblInitialPraxisTrace = new Label(container, SWT.NONE);
		lblInitialPraxisTrace.setBounds(509, 303, 168, 20);
		lblInitialPraxisTrace.setText("Initial Praxis Trace : ");
		lblInitialPraxisTrace.setVisible(false);
		
		Button btnTemp = new Button(container, SWT.NONE);
		btnTemp.setImage(ResourceManager.getPluginImage("TraceRecorder", "icon/1417729046_folder-search.png"));
		btnTemp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				//praxisTrace = actionLinstner.getPraxisTrace();
				
				//for(String parsed : actionLinstner.getPraxisTrace()){
				//	System.out.println("		 parsed = "+parsed);
				//}

				ParsePraxisTrace parsePraxisTrace = new ParsePraxisTrace();
				
				if(!isImported){
				
					if(editor == "EcoreDiagramEditor"){
						
						parsePraxisTrace = new ParsePraxisTraceEcore(actionLinstner.getPraxisTrace());
						
					} else if(editor == "PapyrusMultiDiagramEditor"){
						
						parsePraxisTrace = new ParsePraxisTraceUMLCDPapyrus(actionLinstner.getPraxisTrace());
						
					} 
					
					//ParsePraxisTrace parsePraxisTrace = new ParsePraxisTrace(actionLinstner.getPraxisTrace());
					parsePraxisTrace.parseTrace();
						
				
				}else { //if(isImported){
					//here in case of a trace is imported
					parsePraxisTrace.setParsedTrace(importedStringAtomicChanges);//here may be add it to the existing one, or do it before to parse teh praxis trace so that evolution trace can be added to the impoprted trace
				}
					
				//here we have parsed the pracis trace to an atomic change trace
				parsedTrace = parsePraxisTrace.getParsedTrace();
				listFiltredTrace.removeAll();
				for(String parsed : parsePraxisTrace.getParsedTrace()){
					listFiltredTrace.add(parsed);
					
				}
				filtredSize.setText(listFiltredTrace.getItemCount()+"");
				
				//here we instantiate atomic changes objects from the atomic change trace
				ParseEvolutionTrace parse = new ParseEvolutionTrace(parsedTrace);
				parse.parseTrace();
				//parse.getEvolutionTrace().displayAtomicEvolutionTrace();
				//parse.getEvolutionTrace().displayComplexEvolutionTrace();
				
				//here simulation of atomic changes
				/*ArrayList<AtomicChange> templist = parse.getEvolutionTrace().getAtomicEvolutionTrace();
				int ac = Integer.parseInt(nbrAC.getText());
				
				//1
				for(int i = 0; i < 1000; i++){
					templist.add(new AddClass("C"+i));
				}
				//2
				for(int i = 0; i < 1000; i++){
					templist.add(new DeleteClass("D"+i));
				}
				//3
				for(int i = 0; i < 1000; i++){
					templist.add(new RenameClass("AB"+i, "AC"+i, "P"));
				}
				//4
				for(int i = 0; i < 1000; i++){
					templist.add(new RenameProperty("pa"+i, "pa"+i, "C"));
				}
				//5
				int finalAC = 0;
				if((ac/10)>40) finalAC = 50;
				else finalAC = (ac/10);
				
				for(int i = 0; i < finalAC; i++){
					templist.add(new DeleteProperty("prop"+i, "A"+i));
				}
				//6
				for(int i = 0; i < finalAC; i++){
					templist.add(new AddProperty("zz"+i, "B"+i));
				}
				
				//7
				for(int i = 0; i < ac; i++){
					templist.add(new ChangeLowerBound("p"+i, "C"+i, "o"+i, "n"+i));
				}
				//8
				for(int i = 0; i < ac; i++){
					templist.add(new SetProperty("pa"+i, "E"+i, true));
				}
				int cc = Integer.parseInt(nbrCC.getText());
				//move property prop from A to B
				for(int i = 0; i < cc; i++){
					templist.add(new DeleteProperty("prop"+i, "X"+i));
					templist.add(new AddProperty("prop"+i, "Y"+i));
					templist.add(new AddProperty("xtoy"+i, "X"+i));
					templist.add(new SetProperty("xtoy"+i, "Y"+i, true));//same effect with false
				}
				
				parse.getEvolutionTrace().setAtomicEvolutionTrace(templist);
				*/
				
				
				//DetectComplexChange detection = new DetectComplexChange(actionLinstner.getInitState());
				ArrayList<ComplexChange> detectedComplexChanges = new ArrayList<ComplexChange>();
				
				ArrayList<GenericDefinition> genericDef = new ArrayList<GenericDefinition>();
				genericDef.clear();
				
				long s = System.currentTimeMillis();
				long sm = Runtime.getRuntime().freeMemory();
				long tm = Runtime.getRuntime().totalMemory();
				long mm = Runtime.getRuntime().maxMemory();
				//genericDef.add(new DefinitionMove());
				genericDef.add(new DefinitionComplexMove());
				genericDef.add(new DefinitionPull());
				genericDef.add(new DefinitionPush());
				//genericDef.add(new DefinitionExtractV0());
				genericDef.add(new DefinitionExtract());
				genericDef.add(new DefinitionExtractSuper());
				genericDef.add(new DefinitionFlattenHierarchy());
				genericDef.add(new DefinitionInverseMove());
				genericDef.add(new DefinitionInline());
					//genericDef.add(new DefinitionHiddenMoveV1());
					//genericDef.add(new DefinitionUndoPull());
					//genericDef.add(new DefinitionUndoAddProperty());
					//genericDef.add(new DefinitionUndoDeleteProperty());
				//add inverse move (or use move in the oposite way) , and detect inline class
				
				GenericDetection generic = new GenericDetection(genericDef);
				generic.detect(parse.getEvolutionTrace().getAtomicEvolutionTrace());
				
				//DetectComplexChange detection = new DetectComplexChange(initState);
				//ArrayList<ComplexChange> detectedComplexChanges = new ArrayList<ComplexChange>();
				
				detectedComplexChanges = generic.validateDetectedCC(parse.getEvolutionTrace().getAtomicEvolutionTrace(), actionLinstner.getInitState(), new ArrayList<ComplexChange>());
				generic.detectMoreCC(detectedComplexChanges);
				detectedComplexChanges = generic.validateDetectedCC(parse.getEvolutionTrace().getAtomicEvolutionTrace(), actionLinstner.getInitState(), detectedComplexChanges);
				
				//here we just save the atomic changes and complex changes in the two attributes of this class
				atomicChanges = parse.getEvolutionTrace().getAtomicEvolutionTrace();
				complexChanges = detectedComplexChanges;
				
				System.out.println("size of detected complex changes = "+detectedComplexChanges.size());
				parse.getEvolutionTrace().displayComplexEvolutionTrace();
				
				tree = new Tree(scrolledComposite_2, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
				scrolledComposite_2.setContent(tree);
				scrolledComposite_2.setMinSize(tree.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				tree.setLayoutData(new GridData(GridData.FILL_BOTH));
				
				int move = 0;
				int pull = 0;
				int push = 0;
				int extract = 0;
				int extractSuper = 0;
				int flatten = 0;
				int inline = 0;
				int inverseMove = 0;
				int undo = 0;
				int position = 0;
				
				for(ComplexChange complexchange : detectedComplexChanges){
					
					complexchange.setTreePosition(String.valueOf(position));
					position++;
					
					if(complexchange instanceof MoveProperty){
						System.out.println("delete pos = "+((MoveProperty) complexchange).getDeletePropertyPosition());
						System.out.println("add pos = "+((MoveProperty) complexchange).getAddPropertyPosition());
						System.out.println(""+((MoveProperty) complexchange).getName());
						System.out.println(""+((MoveProperty) complexchange).getSourceClassName());
						System.out.println(""+((MoveProperty) complexchange).getTargetClassName());
						System.out.println(""+((MoveProperty) complexchange).getKind());
						
						if(move == 0){
							item1 = new TreeItem(tree, SWT.NULL);
							//try to add a listner
							item1.addListener(SWT.Selection, new Listener() {
							      public void handleEvent(Event event) {
							    	  System.out.println("\n1\n");
							         if(item1.getChecked()){
							        	 System.out.println("\n2\n");
							        	 for(TreeItem i : item1.getItems()){
							        		 System.out.println("\n3\n");
							        		 i.setChecked(true);
							        	 }
							         }
							        }
							      });
							
						}
						move++;
						item1.setText("Moves , nbr : "+move);
			    		TreeItem itemTemp = new TreeItem(item1, SWT.NULL);
			    		itemTemp.setText(complexchange.getTreePosition()+") Moved Property : "+((MoveProperty)complexchange).getName()+
			    				", Form Class : "+((MoveProperty)complexchange).getSourceClassName()+
			    				", To Class : "+((MoveProperty)complexchange).getTargetClassName()+
			    				", throught ref : "+((MoveProperty)complexchange).getThroughReference());
			    		
					    
					} else if(complexchange instanceof PullProperty){
						System.out.println(""+((PullProperty) complexchange).getName());
						System.out.println(""+((PullProperty) complexchange).getSuperClassName());
						System.out.println(""+((PullProperty) complexchange).getSubClassesNames().toString());
						System.out.println(""+((PullProperty) complexchange).getKind());
						
						if(pull == 0){
							item2 = new TreeItem(tree, SWT.NULL);
							
						}
						pull++;
						item2.setText("Pulls , nbr : "+pull);
			    		TreeItem itemTemp = new TreeItem(item2, SWT.NULL);
			    		itemTemp.setText(complexchange.getTreePosition()+") Pulled Property : "+((PullProperty)complexchange).getName()+
			    				", Form Sub Classes : "+((PullProperty)complexchange).getSubClassesNames().toString()+
			    				", To Super Class : "+((PullProperty)complexchange).getSuperClassName());
					    
					    
					} else if(complexchange instanceof PushProperty){
						System.out.println(""+((PushProperty) complexchange).getName());
						System.out.println(""+((PushProperty) complexchange).getSuperClassName());
						System.out.println(""+((PushProperty) complexchange).getSubClassesNames().toString());
						System.out.println(""+((PushProperty) complexchange).getKind());
						if(push == 0){
							item3 = new TreeItem(tree, SWT.NULL);
							
						}
						push++;
					    item3.setText("Pushes , nbr : "+push);
			    		TreeItem itemTemp = new TreeItem(item3, SWT.NULL);
			    		itemTemp.setText(complexchange.getTreePosition()+") Pushed Property : "+((PushProperty)complexchange).getName()+
			    				", Form Super Class : "+((PushProperty)complexchange).getSuperClassName()+
			    				", To Sub Classes : "+((PushProperty)complexchange).getSubClassesNames().toString());
			    
					    
					} else if(complexchange instanceof ExtractClass){
						System.out.println(""+((ExtractClass) complexchange).getName() );
						System.out.println(""+((ExtractClass) complexchange).getSourceClassName() );
						System.out.println(""+((ExtractClass) complexchange).getTargetClassName() );
						System.out.println(""+((ExtractClass) complexchange).getPropertiesNames().toString() );
						System.out.println(""+((ExtractClass) complexchange).getMoves());
						System.out.println(""+((ExtractClass) complexchange).getKind() );
						
						if(extract == 0){
							item4 = new TreeItem(tree, SWT.NULL);
							
						}
						extract++;
						item4.setText("Extract Classes , nbr : "+extract);    
			    		TreeItem itemTemp = new TreeItem(item4, SWT.NULL);
			    		itemTemp.setText(complexchange.getTreePosition()+") Extract Class Properties : "+((ExtractClass)complexchange).getPropertiesNames().toString()+
			    				", Form Class : "+((ExtractClass)complexchange).getSourceClassName()+
			    				", To Class : "+((ExtractClass)complexchange).getTargetClassName());
			    
						    
					} else if(complexchange instanceof ExtractSuperClass){
						System.out.println(""+((ExtractSuperClass) complexchange).getName());
						System.out.println(""+((ExtractSuperClass) complexchange).getSuperClassName());		
						System.out.println(""+((ExtractSuperClass) complexchange).getSubClassesNames().toString());
						System.out.println(""+((ExtractSuperClass) complexchange).getPropertiesNames().toString());
						System.out.println(""+((ExtractSuperClass) complexchange).getPulles());
						System.out.println(""+((ExtractSuperClass) complexchange).getKind());
						
						if(extractSuper == 0){
							item5 = new TreeItem(tree, SWT.NULL);
							
						}
						extractSuper++;
					    item5.setText("Extract Super Classes , nbr : "+extractSuper);
			    		TreeItem itemTemp = new TreeItem(item5, SWT.NULL);
			    		itemTemp.setText(complexchange.getTreePosition()+") Extract Super Class Properties : "+((ExtractSuperClass)complexchange).getPropertiesNames()+
			    				", Form Sub Classes : "+((ExtractSuperClass)complexchange).getSubClassesNames().toString()+
			    				", To New Super Class : "+((ExtractSuperClass)complexchange).getSuperClassName());
			   
						
					} else if(complexchange instanceof FlattenHierarchy){
						System.out.println(""+((FlattenHierarchy) complexchange).getName());
						System.out.println(""+((FlattenHierarchy) complexchange).getSuperClassName());		
						System.out.println(""+((FlattenHierarchy) complexchange).getSubClassesNames().toString());
						System.out.println(""+((FlattenHierarchy) complexchange).getPropertiesNames().toString());
						System.out.println(""+((FlattenHierarchy) complexchange).getPushes());
						System.out.println(""+((FlattenHierarchy) complexchange).getKind());
						if(flatten == 0){
							item6 = new TreeItem(tree, SWT.NULL);
						    
						}
						flatten++;
						item6.setText("Flatten Hierarchy , nbr : "+flatten);
			    		TreeItem itemTemp = new TreeItem(item6, SWT.NULL);
			    		itemTemp.setText(complexchange.getTreePosition()+") Flatten Hierarchy : "+((FlattenHierarchy)complexchange).getPropertiesNames().toString()+
			    				", Form Super Class : "+((FlattenHierarchy)complexchange).getSuperClassName()+
			    				", To Sub Classes : "+((FlattenHierarchy)complexchange).getSubClassesNames().toString());
			    
						    
					} else if(complexchange instanceof InlineClass){
						System.out.println(""+((InlineClass) complexchange).getName() );
						System.out.println(""+((InlineClass) complexchange).getSourceClassName() );
						System.out.println(""+((InlineClass) complexchange).getTargetClassName() );
						System.out.println(""+((InlineClass) complexchange).getPropertiesNames().toString() );
						System.out.println(""+((InlineClass) complexchange).getInverseMoves());
						System.out.println(""+((InlineClass) complexchange).getKind() );
						if(inline == 0){
							item7 = new TreeItem(tree, SWT.NULL);
							
						}
						inline++;
						item7.setText("Inline Classes , nbr : "+inline);
			    		TreeItem itemTemp = new TreeItem(item7, SWT.NULL);
			    		itemTemp.setText(complexchange.getTreePosition()+") Inline Class Properties : "+((InlineClass)complexchange).getPropertiesNames().toString()+
			    				", Form Class : "+((InlineClass)complexchange).getSourceClassName()+
			    				", To Class : "+((InlineClass)complexchange).getTargetClassName());
			    		
					    
					    
					} else if(complexchange instanceof InverseMoveProperty){
						/*
						 **/ System.out.println("delete pos = "+((InverseMoveProperty) complexchange).getDeletePropertyPosition());
						System.out.println("add pos = "+((InverseMoveProperty) complexchange).getAddPropertyPosition());
						System.out.println(""+((InverseMoveProperty) complexchange).getName());
						System.out.println(""+((InverseMoveProperty) complexchange).getSourceClassName());
						System.out.println(""+((InverseMoveProperty) complexchange).getTargetClassName());
						System.out.println(""+((InverseMoveProperty) complexchange).getKind());
						
						if(inverseMove == 0){
							item8 = new TreeItem(tree, SWT.NULL);
							
						}
						inverseMove++;
						item8.setText("Inverse Moves , nbr : "+inverseMove);
			    		TreeItem itemTemp = new TreeItem(item8, SWT.NULL);
			    		itemTemp.setText(complexchange.getTreePosition()+") Inverse Moved Property : "+((InverseMoveProperty)complexchange).getName()+
			    				", Form Class : "+((InverseMoveProperty)complexchange).getSourceClassName()+
			    				", To Class : "+((InverseMoveProperty)complexchange).getTargetClassName());
			    		
					    
					} else if(complexchange instanceof UndoAddProperty){
						
						if(undo == 0){
							item9 = new TreeItem(tree, SWT.NULL);
							
						}
						undo++;
						item9.setText("Undo changes , nbr : "+undo);
			    		TreeItem itemTemp = new TreeItem(item9, SWT.NULL);
			    		itemTemp.setText(complexchange.getTreePosition()+") UndoAddProperty : "+ ((UndoAddProperty)complexchange).getName()+
			    				", added in "+((UndoAddProperty)complexchange).addProperty.getClassName() +" at "+((UndoAddProperty)complexchange).addProperty.getId()+
			    				", deleted in "+((UndoAddProperty)complexchange).deleteProperty.getClassName() +" at "+((UndoAddProperty)complexchange).deleteProperty.getId());
			    		
			    		System.out.println(complexchange.getTreePosition()+") UndoAddProperty : "+ ((UndoAddProperty)complexchange).getName()+
			    				", added in "+((UndoAddProperty)complexchange).addProperty.getClassName() +" at "+((UndoAddProperty)complexchange).addProperty.getId()+
			    				", deleted in "+((UndoAddProperty)complexchange).deleteProperty.getClassName() +" at "+((UndoAddProperty)complexchange).deleteProperty.getId());
			    		
					} else if(complexchange instanceof UndoDeleteProperty){
						
						if(undo == 0){
							item9 = new TreeItem(tree, SWT.NULL);
							
						}
						undo++;
						item9.setText("Undo changes , nbr : "+undo);
			    		TreeItem itemTemp = new TreeItem(item9, SWT.NULL);
			    		itemTemp.setText(complexchange.getTreePosition()+") UndoDeleteProperty : "+ ((UndoDeleteProperty)complexchange).getName()+
			    				", added in "+((UndoDeleteProperty)complexchange).addProperty.getClassName() +" at "+((UndoDeleteProperty)complexchange).addProperty.getId()+
			    				", deleted in "+((UndoDeleteProperty)complexchange).deleteProperty.getClassName() +" at "+((UndoDeleteProperty)complexchange).deleteProperty.getId());
			    		
			    		System.out.println(complexchange.getTreePosition()+") UndoDeleteProperty : "+ ((UndoDeleteProperty)complexchange).getName()+
			    				", added in "+((UndoDeleteProperty)complexchange).addProperty.getClassName() +" at "+((UndoDeleteProperty)complexchange).addProperty.getId()+
			    				", deleted in "+((UndoDeleteProperty)complexchange).deleteProperty.getClassName() +" at "+((UndoDeleteProperty)complexchange).deleteProperty.getId());
			    		
						
					} else if(complexchange instanceof UndoPull){
						
						if(undo == 0){
							item9 = new TreeItem(tree, SWT.NULL);
							
						}
						undo++;
						item9.setText("Undo changes , nbr : "+undo);
			    		TreeItem itemTemp = new TreeItem(item9, SWT.NULL);
			    		itemTemp.setText(complexchange.getTreePosition()+") UndoPull : "+ ((UndoPull)complexchange).getName()+
			    				", pulled from "+((UndoPull)complexchange).getPull().getSubClassesNames().toString()+
			    				" to superClass "+((UndoPull)complexchange).getPull().getSuperClassName()+
			    				", then pushed from "+((UndoPull)complexchange).getPush().getSuperClassName()+
			    				" to "+((UndoPull)complexchange).getPush().getSubClassesNames().toString());
			    		
			    		System.out.println(complexchange.getTreePosition()+") UndoPull : "+ ((UndoPull)complexchange).getName()+
			    				", pulled from "+((UndoPull)complexchange).getPull().getSubClassesNames().toString()+
			    				" to superClass "+((UndoPull)complexchange).getPull().getSuperClassName()+
			    				", then pushed from "+((UndoPull)complexchange).getPush().getSuperClassName()+
			    				" to "+((UndoPull)complexchange).getPush().getSubClassesNames().toString());
					}
					
				}
			    
				long ed = System.currentTimeMillis();
				long em = Runtime.getRuntime().freeMemory();
				long etm = Runtime.getRuntime().totalMemory(); 
				//System.out.println("max memory 1 = "+mm);
				System.out.println("total memory 1 = "+tm/(1024 * 1024));
				//System.out.println("max memory 2 = "+Runtime.getRuntime().maxMemory());
				System.out.println("total memory 2 = "+etm/(1024 * 1024));
				System.out.println("time = "+(ed-s));
				System.out.println("memory = "+(sm-em)/(1024 * 1024));//1000000d
				System.out.println("used start = "+(tm - sm)/(1024 * 1024)+" used end = "+ (etm - em)/(1024 * 1024));
				System.out.println("total used = "+((etm - em)-(tm - sm)));
				System.out.println("total used in MO = "+((etm - em)-(tm - sm))/(1024 * 1024));//1000000d
				
				//related to the simulation of random traces
				//System.out.println("\n cc = "+cc+", ac = "+((ac+finalAC)*2)+" + "+ cc*4
					//				+" = "+(((ac+finalAC)*2)+(cc*4))+"\n");
				
			}
		});
		btnTemp.setBounds(362, 13, 138, 36);
		btnTemp.setText("Run Detection");
		
		btnReset = new Button(container, SWT.NONE);
		btnReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ListPraxisTrace.removeAll();
				listFiltredTrace.removeAll();
				btnStartRecording.setEnabled(true);
				isImported = false;
			}
		});
		btnReset.setImage(ResourceManager.getPluginImage("TraceRecorder", "icon/1423503103_Stop1NormalRed.png"));
		btnReset.setBounds(162, 0, 85, 30);
		btnReset.setText("Reset");
		
		Button btnImportAnEt = new Button(container, SWT.NONE);
		btnImportAnEt.setImage(ResourceManager.getPluginImage("TraceRecorder", "icon/1452294031_stock_mail-import.png"));
		btnImportAnEt.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//select the extension trace or in the future the trace of atomic changes that was recorded
				
				FileDialog fileDialog = new FileDialog(shell, SWT.MULTI);

		        //fileDialog.setFilterPath(fileFilterPath);
		        
		        fileDialog.setFilterExtensions(new String[]{"*.extension;*.txt"});//, new String[]{"*.txt", "*.*"}
		        fileDialog.setFilterNames(new String[]{ "Rich Text Format", "HTML Document", "Any"});
		        
		        String firstFile = fileDialog.open();
		        
		        if(firstFile != null) {
		            String fileFilterPath = fileDialog.getFilterPath();
		            String[] selectedFiles = fileDialog.getFileNames();
		            StringBuffer sb = new StringBuffer("Selected files under dir " + fileDialog.getFilterPath() +  ": \n");
		            for(int i=0; i<selectedFiles.length; i++) {
		              sb.append(selectedFiles[i] + "\n");
		            }
		            System.out.println("\n	"+sb.toString()+"\n");
		            //label.setText(sb.toString());
		            
		            if(selectedFiles[0].contains(".extension")){//load the trace in the extension file
			            ExtensionTraceImport etImport = new ExtensionTraceImport(selectedFiles[0], fileDialog.getFilterPath());
			            //etImport.loadExtensionTrace();
			            
			            importedAtomicChanges = etImport.getAtomicTrace();
			            importedStringAtomicChanges = etImport.getAtomicStringTrace();
		            } else {//here read the trace from the file
		            	//importedStringAtomicChanges
		            	File file = new File (fileDialog.getFilterPath()+File.separator+selectedFiles[0]);
		            	
		            	try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		            	    String line;
		            	    importedAtomicChanges = new ArrayList<AtomicChange>();
		            	    importedStringAtomicChanges = new ArrayList<String>();
		            	    RawTraceImport rtimport = new RawTraceImport(); 
		            	    
		            	    while ((line = br.readLine()) != null) {
		            	    	importedStringAtomicChanges.add(line);
		            	    	importedAtomicChanges.add(rtimport.parseRawChangeTrace(line));
		            	    	System.out.println("\n	"+line+"\n");
		            	    }
		            		System.out.println("\n	atomic changes zehahahahahah "+importedAtomicChanges+"\n");
		            	} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
		            }
		            
		            //here we add and display the imported trace
					parsedTrace = importedStringAtomicChanges;
					listFiltredTrace.removeAll();
					for(String parsed : importedStringAtomicChanges){
						listFiltredTrace.add(parsed);
						
					}
					filtredSize.setText(listFiltredTrace.getItemCount()+"");
					
		            isImported = true;
		        }
			}
		});
		btnImportAnEt.setBounds(253, 0, 97, 30);
		btnImportAnEt.setText("Import");
		//btnImportAnEt.setVisible(false);
		
		Combo combo = new Combo(container, SWT.NONE);
		combo.setItems(new String[] {"H1 Containement (full overlap)", "H2 Distance (partial overlap)", "H3 Solving rate (partial overlap)"});
		combo.setBounds(843, 2, 103, 28);
		combo.setVisible(false);
		
		Label lblHeuristics = new Label(container, SWT.NONE);
		lblHeuristics.setBounds(767, 5, 70, 20);
		lblHeuristics.setText("Heuristics :");
		lblHeuristics.setVisible(false);
		
		btnTestcheking = new Button(container, SWT.NONE);
		btnTestcheking.setImage(ResourceManager.getPluginImage("org.eclipse.ant.ui", "/icons/full/elcl16/run_tool.gif"));
		btnTestcheking.setToolTipText("Compute Final Trace");
		btnTestcheking.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ArrayList<ComplexChange> selected = new ArrayList<ComplexChange>();
				System.out.println("atomic trace "+listFiltredTrace.getItemCount());
				System.out.println("atomic changes "+atomicChanges.size());
				System.out.println("complex changes "+complexChanges.size());
				System.out.println("selected complex changes "+selected.size());
				
				for(TreeItem item : tree.getItems()){
					for(TreeItem subItem : item.getItems()){
						System.out.println("\n\nchecked = "+subItem.getChecked() + ", text = "+ subItem.getText());
						System.out.println("id of complex = "+ subItem.getText().split("\\)")[0]);
						
						if(subItem.getChecked()) {
							//subItem.getText().split("\\)")[0];
							for(ComplexChange c : complexChanges){
								if(c.getTreePosition().equals(subItem.getText().split("\\)")[0])) {selected.add(c); break;}
							}
						}
					}
				}
				
				System.out.println("selected complex changes = "+selected.size());
				
				FinalEvolutionTrace changes = new FinalEvolutionTrace((ArrayList<AtomicChange>)atomicChanges.clone(), selected);
				
				finalChanges = changes.computeFinalChanges();
				
				System.out.println("all changes = "+finalChanges.size());
				
				changes.afficherFinalChanges();
				
			}
		});
		btnTestcheking.setBounds(527, 265, 103, 30);
		btnTestcheking.setText("Final Trace");
		
		shell = parent.getShell();
		
		Button btnOverlapDetection = new Button(container, SWT.NONE);
		btnOverlapDetection.setImage(ResourceManager.getPluginImage("org.eclipse.emf.compare.rcp.ui", "/icons/full/toolb16/group.gif"));
		btnOverlapDetection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("atomic changes "+atomicChanges.size());
				System.out.println("complex changes "+complexChanges.size());
				
				ArrayList<ComplexChange> filtredCC = new ArrayList<ComplexChange>();
				
				for(ComplexChange c : complexChanges){
					if(!(c instanceof InverseMoveProperty)) filtredCC.add(c);
				}
				
				DetectOverlap dOverlap = new DetectOverlap(atomicChanges, filtredCC); 
				
				ArrayList< ArrayList<ComplexChange> > sets = dOverlap.costructOverlapSets();
				for(ArrayList<ComplexChange> lc : sets){
					Distance distance = new Distance(lc);
					distance.calculatePriorities();
					for(ComplexChange c : lc){
						System.out.println("complex change c = "+c+",\n \t priority = "+ c.getPriority());
					}
				}
				
				HeuristicsResults dialog = new HeuristicsResults(shell, 0, atomicChanges, filtredCC);
				dialog.open();
				
				
				
			}
		});
		
		btnOverlapDetection.setBounds(650, 265, 157, 30);
		btnOverlapDetection.setText("Overlap Detection");
		
		nbrAC = new Text(container, SWT.BORDER);
		nbrAC.setToolTipText("nbr of AC");
		nbrAC.setBounds(519, 328, 78, 26);
		nbrAC.setVisible(false);
		
		nbrCC = new Text(container, SWT.BORDER);
		nbrCC.setToolTipText("nbr CC");
		nbrCC.setBounds(603, 328, 78, 26);
		nbrCC.setVisible(false);
		//btnTestOverlapDetection.setVisible(false);
		
		/*
	    TreeItem item = new TreeItem(tree, SWT.NULL);
	    item.setText("ITEM");
	    
	    TreeItem item2 = new TreeItem(item, SWT.NULL);
	    item2.setText("ITEM2");
	    
	    TreeItem item3 = new TreeItem(item2, SWT.NULL);
	    item3.setText("ITEM3");
		*/
		
		
		createActions();
		initializeToolBar();
		initializeMenu();
	}
	public PraxisTraceRecorder getView(){
		return this;
	}
	
	boolean finish = false;
	private Text nbrAC;
	private Text nbrCC;
	public void updatePraxisTraceList(String ac){
		
		if(!ac.equals("@Start_Evolution_Trace_Praxis.")
				&& !ac.equals("@Start_Notification.")
				&& !ac.equals("@End_Notification.")){
			
			this.ListPraxisTrace.add(ac);
			this.praxisSize.setText((ListPraxisTrace.getItemCount() - intialTraceSize)+"");
			
			//praxisTrace = actionLinstner.getPraxisTrace();
			
			//for(String parsed : actionLinstner.getPraxisTrace()){
				//System.out.println("		 parsed = "+parsed);
			//}
			/*//if i put this code belwo, there are some issues like when i add a property , in the trace i loose the container, i.e. its class.
			if(finish){
				ParsePraxisTrace parsePraxisTrace = new ParsePraxisTrace(actionLinstner.getPraxisTrace());
				parsePraxisTrace.parseTrace();
				
				parsedTrace = parsePraxisTrace.getParsedTrace();
				listFiltredTrace.removeAll();
				for(String parsed : parsePraxisTrace.getParsedTrace()){
					listFiltredTrace.add(parsed);
					
				}
				filtredSize.setText(listFiltredTrace.getItemCount()+"");
			} */
		} else if(ac.equals("@End_Notification.")){
			//praxisTrace = actionLinstner.getPraxisTrace();
			
			//for(String parsed : actionLinstner.getPraxisTrace()){
				//System.out.println("		 parsed = "+parsed);
			//}
			/*//if i put this code belwo, there are some issues like when i add a property , in the trace i loose the container, i.e. its class.
			//if(finish){}
				System.out.println("**********");
				System.out.println("	read trace");
				System.out.println("**********");
				ParsePraxisTrace parsePraxisTrace = new ParsePraxisTrace(actionLinstner.getPraxisTrace());
				parsePraxisTrace.parseTrace();
				
				parsedTrace = parsePraxisTrace.getParsedTrace();
				listFiltredTrace.removeAll();
				for(String parsed : parsePraxisTrace.getParsedTrace()){
					listFiltredTrace.add(parsed);
					
				}
				filtredSize.setText(listFiltredTrace.getItemCount()+"");
			 */
			praxisTrace = actionLinstner.getPraxisTrace();
			System.out.println("**********");
			System.out.println("	read trace");
			System.out.println("**********");
			for(String parsed : actionLinstner.getPraxisTrace()){
				System.out.println("		 parsed = "+parsed);
				
			}

			ParsePraxisTrace parsePraxisTrace = null;
			
			if(editor == "EcoreDiagramEditor"){
				
				parsePraxisTrace = new ParsePraxisTraceEcore(actionLinstner.getPraxisTrace());
				
			} else if(editor == "PapyrusMultiDiagramEditor"){
				
				parsePraxisTrace = new ParsePraxisTraceUMLCDPapyrus(actionLinstner.getPraxisTrace());
				
			} 
			
			//ParsePraxisTrace parsePraxisTrace = new ParsePraxisTrace(actionLinstner.getPraxisTrace());
			parsePraxisTrace.parseTrace();
			
			//here we have parsed the pracis trace to an atomic change trace
			parsedTrace = parsePraxisTrace.getParsedTrace();
			listFiltredTrace.removeAll();
			for(String parsed : parsePraxisTrace.getParsedTrace()){
				listFiltredTrace.add(parsed);
				
			}
			filtredSize.setText(listFiltredTrace.getItemCount()+"");
		}
		
	}
	
	public List getListPraxisTrace(){
		return this.ListPraxisTrace;
	}
	
	public ArrayList<Change> getFinalChanges(){
		return this.finalChanges;
	}
	
	public InitialState getInitState(){
		return this.actionLinstner.getInitState();
	}



	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars()
				.getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}
}
