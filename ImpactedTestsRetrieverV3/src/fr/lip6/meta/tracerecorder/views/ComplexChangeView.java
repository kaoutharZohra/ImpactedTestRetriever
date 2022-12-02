package fr.lip6.meta.tracerecorder.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ComplexChangeView extends ViewPart {

	public static final String ID = "fr.lip6.meta.tracerecorder.views.ComplexChangeView"; //$NON-NLS-1$

	public ComplexChangeView() {
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		
		Button btnGetOtherView = new Button(container, SWT.NONE);
		btnGetOtherView.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IViewPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					    .findView("tracerecorder.views.PraxisTraceRecorder");
					if (part instanceof PraxisTraceRecorder) {
						System.out.println("\n-----------found----------------\n");
						PraxisTraceRecorder view = (PraxisTraceRecorder) part;
					    // now access whatever internals you can get to
						List i = view.getListPraxisTrace();
						System.out.println("\n----------- n° item = "+i.getItemCount()+"----------------\n");
					} else {
						System.out.println("\n-----------Not found----------------\n");
					}
			}
		});
		btnGetOtherView.setBounds(10, 73, 149, 30);
		btnGetOtherView.setText("get other view");

		createActions();
		initializeToolBar();
		initializeMenu();
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
