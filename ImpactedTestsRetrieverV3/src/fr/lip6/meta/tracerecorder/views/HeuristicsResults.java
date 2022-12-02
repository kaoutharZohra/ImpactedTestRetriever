package fr.lip6.meta.tracerecorder.views;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import fr.lip6.meta.ComplexChangeDetection.AtomicChanges.AtomicChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ComplexChange;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.ExtractSuperClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.FlattenHierarchy;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.InlineClass;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.InverseMoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.MoveProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PullProperty;
import fr.lip6.meta.ComplexChangeDetection.ComplexChanges.PushProperty;
import fr.lip6.meta.ComplexChangeDetection.Heuristics.Containment;
import fr.lip6.meta.ComplexChangeDetection.Heuristics.Distance;
import fr.lip6.meta.ComplexChangeDetection.Heuristics.SolvingRate;
import fr.lip6.meta.ComplexChangeDetection.Overlap.DetectOverlap;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.wb.swt.ResourceManager;

public class HeuristicsResults extends Dialog {

	protected Object result;
	protected Shell shlUserSupportTo;
	private ArrayList<TreeItem> overlappingSetsItems = null;
	private Tree tree;
	//private ArrayList< ArrayList<ComplexChange> > sets = null;
	private ArrayList<ComplexChange> complexChanges = null;
	private ArrayList<AtomicChange> atomicChanges = null;
	private Combo combo;
	private ScrolledComposite scrolledComposite;
	private TreeColumn column1;
	private TreeColumn column2;
	private Button button;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public HeuristicsResults(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
	}
	
	/*public HeuristicsResults(Shell parent, int style, ArrayList< ArrayList<ComplexChange> > sets) {
		super(parent, style);
		setText("SWT Dialog");
		this.sets = sets;
	}*/
	
	public HeuristicsResults(Shell parent, int style, ArrayList<AtomicChange> atomicChanges, ArrayList<ComplexChange> complexChanges) {
		super(parent, style);
		setText("SWT Dialog");
		this.complexChanges = complexChanges;
		this.atomicChanges = atomicChanges;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlUserSupportTo.open();
		shlUserSupportTo.layout();
		Display display = getParent().getDisplay();
		while (!shlUserSupportTo.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlUserSupportTo = new Shell(getParent(), SWT.SHELL_TRIM | SWT.BORDER);
		shlUserSupportTo.setImage(ResourceManager.getPluginImage("TraceRecorder", "icon/1411164364_eclipse.png"));
		shlUserSupportTo.setSize(897, 240);
		shlUserSupportTo.setText("User support to select correct changes");
		
		scrolledComposite = new ScrolledComposite(shlUserSupportTo, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setBounds(0, 51, 879, 142);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		tree = new Tree(scrolledComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setHeaderVisible(true);
		
		column1 = new TreeColumn(tree, SWT.LEFT);
		column1.setText("Overlapping Complex Changes");
		column1.setWidth(750);
		column2 = new TreeColumn(tree, SWT.CENTER);
		column2.setText("Priority");
		column2.setWidth(100);
		scrolledComposite.setContent(tree);
		scrolledComposite.setMinSize(tree.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	    
		
		//here we compute the overlapping changes
		DetectOverlap dOverlap = new DetectOverlap(atomicChanges, complexChanges); 
		ArrayList< ArrayList<ComplexChange> > sets = dOverlap.costructOverlapSets();
		
		int i = 1;
		for(ArrayList<ComplexChange> lc : sets){
			if(lc.size()>1){
				TreeItem overlappingset = new TreeItem(tree, SWT.NULL);
				overlappingset.setText("Set n° "+i+" of overlapping changes.");
				
				for(ComplexChange c : lc){
					TreeItem change = new TreeItem(overlappingset, SWT.NULL);
					change.setText(new String[] {printWithoutPriorities(c), "n/a"});
					
					change.setForeground(1, tree.getDisplay().getSystemColor(SWT.COLOR_BLUE));
					Font boldFont = new Font( tree.getDisplay(), new FontData( "Arial", 12, SWT.BOLD ) );
					change.setFont(1, boldFont);

				}
				
				i++;
			}
		}
		
		Button btnClose = new Button(shlUserSupportTo, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlUserSupportTo.dispose();
				//shell.close();
			}
		});
		btnClose.setBounds(332, 10, 90, 30);
		btnClose.setText("Close");
		btnClose.setVisible(false);
		
		Label lblRankingHeuristics = new Label(shlUserSupportTo, SWT.NONE);
		lblRankingHeuristics.setBounds(10, 15, 134, 20);
		lblRankingHeuristics.setText("Ranking heuristics :");
		
		combo = new Combo(shlUserSupportTo, SWT.NONE);
		combo.setToolTipText("");
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//System.out.print("widgetSelected = "+combo.getItem(combo.getSelectionIndex()));
				
				tree = new Tree(scrolledComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);//SWT.CHECK | 
				tree.setLayoutData(new GridData(GridData.FILL_BOTH));
				scrolledComposite.setContent(tree);
				scrolledComposite.setMinSize(tree.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				tree.setHeaderVisible(true);
				
				column1 = new TreeColumn(tree, SWT.LEFT);
			    column1.setText("Overlapping Complex Changes");
			    column1.setWidth(750);
			    column2 = new TreeColumn(tree, SWT.CENTER);
			    column2.setText("Priority");
			    column2.setWidth(100);
				
				if(combo.getItem(combo.getSelectionIndex()).equals("None")){
					//here we compute the overlapping changes
					DetectOverlap dOverlap = new DetectOverlap(atomicChanges, complexChanges); 
					ArrayList< ArrayList<ComplexChange> > sets = dOverlap.costructOverlapSets();
					
					int i = 1;
					for(ArrayList<ComplexChange> lc : sets){
						if(lc.size()>1){
							TreeItem overlappingset = new TreeItem(tree, SWT.NULL);
							overlappingset.setText("Set n° "+i+" of overlapping changes.");
							
							for(ComplexChange c : lc){
								TreeItem change = new TreeItem(overlappingset, SWT.NULL);
								change.setText(new String[] {printWithoutPriorities(c), "n/a"});
								
								//Change color and font 
								change.setForeground(1, tree.getDisplay().getSystemColor(SWT.COLOR_BLUE));
								Font boldFont = new Font( tree.getDisplay(), new FontData( "Arial", 12, SWT.BOLD ) );
								change.setFont(1, boldFont);
							}
							
							i++;
						}
					}
					
				} else if(combo.getItem(combo.getSelectionIndex()).contains("H1")){
					//here we compute the overlapping changes
					DetectOverlap dOverlap = new DetectOverlap(atomicChanges, preProcess4H1(complexChanges)); 
					ArrayList< ArrayList<ComplexChange> > sets = dOverlap.costructOverlapSets();
					
					int i = 1;
					for(ArrayList<ComplexChange> lc : sets){
						if(lc.size()>1){
							TreeItem overlappingset = new TreeItem(tree, SWT.NULL);
							overlappingset.setText("Set n° "+i+" of overlapping changes.");
							
							//here we call H1 to compute priorities on lc
							Containment containment = new Containment(lc);
							containment.calculatePriorities();
							
							for(ComplexChange c : lc){
								TreeItem change = new TreeItem(overlappingset, SWT.NULL);
								change.setText(new String[] {printWithoutPriorities(c), c.getPriority()+""});
								
								change.setForeground(1, tree.getDisplay().getSystemColor(SWT.COLOR_BLUE));
								Font boldFont = new Font( tree.getDisplay(), new FontData( "Arial", 12, SWT.BOLD ) );
								change.setFont(1, boldFont);
							}
							
							i++;
						}
					}
					
				} else if(combo.getItem(combo.getSelectionIndex()).contains("H2")){
					//here we compute the overlapping changes
					DetectOverlap dOverlap = new DetectOverlap(atomicChanges, complexChanges); 
					ArrayList< ArrayList<ComplexChange> > sets = dOverlap.costructOverlapSets();
					
					int i = 1;
					for(ArrayList<ComplexChange> lc : sets){
						if(lc.size()>1 && isValuableSet(lc)){//here we ignore the sets that have only full overlap 
							TreeItem overlappingset = new TreeItem(tree, SWT.NULL);
							overlappingset.setText("Set n° "+i+" of overlapping changes.");
							
							//here we call H2 to compute priorities on lc
							Distance distance = new Distance(lc);
							distance.calculatePriorities();
							
							for(ComplexChange c : lc){
								TreeItem change = new TreeItem(overlappingset, SWT.NULL);
								//change.setText(printWithPriorities(c));
								change.setText(new String[] {printWithoutPriorities(c), c.getPriority()+""});
								
								change.setForeground(1, tree.getDisplay().getSystemColor(SWT.COLOR_BLUE));
								Font boldFont = new Font( tree.getDisplay(), new FontData( "Arial", 12, SWT.BOLD ) );
								change.setFont(1, boldFont);
							}
							
							i++;
						}
					}
					
				} else if(combo.getItem(combo.getSelectionIndex()).contains("H3")){
					//here we compute the overlapping changes
					DetectOverlap dOverlap = new DetectOverlap(atomicChanges, complexChanges); 
					ArrayList< ArrayList<ComplexChange> > sets = dOverlap.costructOverlapSets();
					
					int i = 1;
					for(ArrayList<ComplexChange> lc : sets){
						if(lc.size()>1 && isValuableSet(lc)){//here we ignore the sets that have only full overlap 
							TreeItem overlappingset = new TreeItem(tree, SWT.NULL);
							overlappingset.setText("Set n° "+i+" of overlapping changes.");
							
							//here we call H3 to compute priorities on lc
							SolvingRate solvingRate = new SolvingRate(lc);
							solvingRate.calculatePriorities();
							
							for(ComplexChange c : lc){
								TreeItem change = new TreeItem(overlappingset, SWT.NULL);
								//change.setText(printWithoutPriorities(c));
								change.setText(new String[] {printWithoutPriorities(c), c.getPriority()+""});
								
								change.setForeground(1, tree.getDisplay().getSystemColor(SWT.COLOR_BLUE));
								Font boldFont = new Font( tree.getDisplay(), new FontData( "Arial", 12, SWT.BOLD ) );
								change.setFont(1, boldFont);
							}
							
							i++;
						}
					}
					
				}
			}
			/*@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				System.out.print("widgetDefaultSelected = "+combo.getItem(combo.getSelectionIndex()));
			}*/
		});
		combo.setItems(new String[] {"None", "H1 Containement (full overlap)", "H2 Distance (partial overlap)", "H3 Solving rate (partial overlap)"});
		combo.setBounds(171, 12, 251, 28);
		
		button = new Button(shlUserSupportTo, SWT.NONE);
		button.setImage(ResourceManager.getPluginImage("TraceRecorder", "icon/1441750269_Help_Circle_Blue.png"));
		button.setBounds(441, 5, 39, 40);

	}
	
	/*
	public void displayOverlappingSets(ArrayList< ArrayList<ComplexChange> > sets){
		
		int i = 1;
		for(ArrayList<ComplexChange> lc : sets){
			TreeItem overlappingset = new TreeItem(tree, SWT.NULL);
			overlappingset.setText("Set n° "+i+" of overlapping changes.");
			
			for(ComplexChange c : lc){
				TreeItem change = new TreeItem(overlappingset, SWT.NULL);
				change.setText(this.print(c));
			}
			
			i++;
		}
		
	}
	*/
	
	private boolean isValuableSet(ArrayList<ComplexChange> lc){
		ArrayList<ComplexChange> temp = this.preProcess4H1(lc);
		
		for(ComplexChange c : temp){
			
			if(c instanceof ExtractClass && ((ExtractClass) c).getMoves().size() + 1 == lc.size()){//also check if all the moves are in the lc 
				return false;
			} else if(c instanceof ExtractSuperClass && ((ExtractSuperClass) c).getPulles().size() + 1 == lc.size()){//also check if all the pulls are in the lc
				return false;
			} else if(c instanceof FlattenHierarchy && ((FlattenHierarchy) c).getPushes().size() + 1 == lc.size()){//also check if all the pushes are in the lc
				return false;
			}
		}
		return true;
	}

	//this function pre process the list of complex changes by keeping only the full overlap
	private ArrayList<ComplexChange> preProcess4H1(ArrayList<ComplexChange> cc){
		ArrayList<ComplexChange> temp = new ArrayList<ComplexChange>();
		
		for(ComplexChange c : cc){
			
			if(c instanceof ExtractClass){
				temp.add(c);
				for(ComplexChange m : ((ExtractClass) c).getMoves()){
					temp.add(m);
				}
			} else if(c instanceof ExtractSuperClass){
				temp.add(c);
				for(ComplexChange p : ((ExtractSuperClass) c).getPulles()){
					temp.add(p);
				}
			} else if(c instanceof FlattenHierarchy){
				temp.add(c);
				for(ComplexChange p : ((FlattenHierarchy) c).getPushes()){
					temp.add(p);
				}
			} else if(c instanceof InlineClass){
				temp.add(c);
			}
		}
		
		return temp;
	}
	
	//this function pre process the list of complex changes by removing the full overlap
	private ArrayList<ComplexChange> preProcess4H2H3(ArrayList<ComplexChange> cc){
		ArrayList<ComplexChange> temp1 = (ArrayList<ComplexChange>) cc.clone();
		
		ArrayList<ComplexChange> temp2 = this.preProcess4H1(temp1);
		
		for(ComplexChange c : temp2){
			
			if(temp1.contains(c)){
			
				if(c instanceof ExtractClass){
					
					for(ComplexChange m : ((ExtractClass) c).getMoves()){
						temp1.remove(m);
					}
				} else if(c instanceof ExtractSuperClass){
					
					for(ComplexChange p : ((ExtractSuperClass) c).getPulles()){
						temp1.remove(p);
					}
					 
				} else if(c instanceof FlattenHierarchy){
					
					for(ComplexChange p : ((FlattenHierarchy) c).getPushes()){
						temp1.remove(p);
					}
					 
				}
				
				temp1.remove(c);
			}
		}
		
		return temp1;
	}
	
	private String printWithoutPriorities(ComplexChange complexchange){
		
		if(complexchange instanceof MoveProperty){
			
			return ""+(complexchange.getTreePosition()+") Moved Property : "+((MoveProperty)complexchange).getName()+
    				", Form Class : "+((MoveProperty)complexchange).getSourceClassName()+
    				", To Class : "+((MoveProperty)complexchange).getTargetClassName()+
    				", throught ref : "+((MoveProperty)complexchange).getThroughReference());
    		
		    
		} else if(complexchange instanceof PullProperty){
			
			return ""+(complexchange.getTreePosition()+") Pulled Property : "+((PullProperty)complexchange).getName()+
    				", Form Sub Classes : "+((PullProperty)complexchange).getSubClassesNames().toString()+
    				", To Super Class : "+((PullProperty)complexchange).getSuperClassName());
		    
		    
		} else if(complexchange instanceof PushProperty){
			
			return ""+(complexchange.getTreePosition()+") Pushed Property : "+((PushProperty)complexchange).getName()+
    				", Form Super Class : "+((PushProperty)complexchange).getSuperClassName()+
    				", To Sub Classes : "+((PushProperty)complexchange).getSubClassesNames().toString());
    
		    
		} else if(complexchange instanceof ExtractClass){

			return ""+(complexchange.getTreePosition()+") Extract Class Properties : "+((ExtractClass)complexchange).getPropertiesNames().toString()+
    				", Form Class : "+((ExtractClass)complexchange).getSourceClassName()+
    				", To Class : "+((ExtractClass)complexchange).getTargetClassName());
    
			    
		} else if(complexchange instanceof ExtractSuperClass){

			return ""+(complexchange.getTreePosition()+") Extract Super Class Properties : "+((ExtractSuperClass)complexchange).getPropertiesNames()+
    				", Form Sub Classes : "+((ExtractSuperClass)complexchange).getSubClassesNames().toString()+
    				", To New Super Class : "+((ExtractSuperClass)complexchange).getSuperClassName());
   
			
		} else if(complexchange instanceof FlattenHierarchy){
			
			return ""+(complexchange.getTreePosition()+") Flatten Hierarchy : "+((FlattenHierarchy)complexchange).getPropertiesNames().toString()+
    				", Form Super Class : "+((FlattenHierarchy)complexchange).getSuperClassName()+
    				", To Sub Classes : "+((FlattenHierarchy)complexchange).getSubClassesNames().toString());
    
			    
		} else if(complexchange instanceof InlineClass){
			
			return ""+(complexchange.getTreePosition()+") Inline Class Properties : "+((InlineClass)complexchange).getPropertiesNames().toString()+
    				", Form Class : "+((InlineClass)complexchange).getSourceClassName()+
    				", To Class : "+((InlineClass)complexchange).getTargetClassName());
    		
		    
		    
		} else if(complexchange instanceof InverseMoveProperty){

			return ""+(complexchange.getTreePosition()+") Inverse Moved Property : "+((InverseMoveProperty)complexchange).getName()+
    				", Form Class : "+((InverseMoveProperty)complexchange).getSourceClassName()+
    				", To Class : "+((InverseMoveProperty)complexchange).getTargetClassName());
    		
		    
		}
		return "";
	}
	
	private String printWithPriorities(ComplexChange complexchange){
		
		if(complexchange instanceof MoveProperty){
			
			return ""+(complexchange.getTreePosition()+") Priority = "+complexchange.getPriority()+" | Moved Property : "+((MoveProperty)complexchange).getName()+
    				", Form Class : "+((MoveProperty)complexchange).getSourceClassName()+
    				", To Class : "+((MoveProperty)complexchange).getTargetClassName()+
    				", throught ref : "+((MoveProperty)complexchange).getThroughReference());
    		
		    
		} else if(complexchange instanceof PullProperty){
			
			return ""+(complexchange.getTreePosition()+") Priority = "+complexchange.getPriority()+" | Pulled Property : "+((PullProperty)complexchange).getName()+
    				", Form Sub Classes : "+((PullProperty)complexchange).getSubClassesNames().toString()+
    				", To Super Class : "+((PullProperty)complexchange).getSuperClassName());
		    
		    
		} else if(complexchange instanceof PushProperty){
			
			return ""+(complexchange.getTreePosition()+") Priority = "+complexchange.getPriority()+" | Pushed Property : "+((PushProperty)complexchange).getName()+
    				", Form Super Class : "+((PushProperty)complexchange).getSuperClassName()+
    				", To Sub Classes : "+((PushProperty)complexchange).getSubClassesNames().toString());
    
		    
		} else if(complexchange instanceof ExtractClass){

			return ""+(complexchange.getTreePosition()+") Priority = "+complexchange.getPriority()+" | Extract Class Properties : "+((ExtractClass)complexchange).getPropertiesNames().toString()+
    				", Form Class : "+((ExtractClass)complexchange).getSourceClassName()+
    				", To Class : "+((ExtractClass)complexchange).getTargetClassName());
    
			    
		} else if(complexchange instanceof ExtractSuperClass){

			return ""+(complexchange.getTreePosition()+") Priority = "+complexchange.getPriority()+" | Extract Super Class Properties : "+((ExtractSuperClass)complexchange).getPropertiesNames()+
    				", Form Sub Classes : "+((ExtractSuperClass)complexchange).getSubClassesNames().toString()+
    				", To New Super Class : "+((ExtractSuperClass)complexchange).getSuperClassName());
   
			
		} else if(complexchange instanceof FlattenHierarchy){
			
			return ""+(complexchange.getTreePosition()+") Priority = "+complexchange.getPriority()+" | Flatten Hierarchy : "+((FlattenHierarchy)complexchange).getPropertiesNames().toString()+
    				", Form Super Class : "+((FlattenHierarchy)complexchange).getSuperClassName()+
    				", To Sub Classes : "+((FlattenHierarchy)complexchange).getSubClassesNames().toString());
    
			    
		} else if(complexchange instanceof InlineClass){
			
			return ""+(complexchange.getTreePosition()+") Priority = "+complexchange.getPriority()+" | Inline Class Properties : "+((InlineClass)complexchange).getPropertiesNames().toString()+
    				", Form Class : "+((InlineClass)complexchange).getSourceClassName()+
    				", To Class : "+((InlineClass)complexchange).getTargetClassName());
    		
		    
		    
		} else if(complexchange instanceof InverseMoveProperty){

			return ""+(complexchange.getTreePosition()+") Priority = "+complexchange.getPriority()+" | Inverse Moved Property : "+((InverseMoveProperty)complexchange).getName()+
    				", Form Class : "+((InverseMoveProperty)complexchange).getSourceClassName()+
    				", To Class : "+((InverseMoveProperty)complexchange).getTargetClassName());
    		
		    
		}
		return "";
	}
}
