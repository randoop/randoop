package randoop.plugin.internal.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.model.resultstree.RunResultsTree;

public class TestGeneratorViewPart extends ViewPart {
  /**
   * The ID of the view as specified by the extension.
   */
  public static final String ID = "randoop.plugin.ui.views.TestGeneratorViewPart"; //$NON-NLS-1$

  private TreeViewer viewer;
  private Composite fParent;
  private CounterPanel fCounterPanel;
  private RandoopProgressBar fProgressBar;

  RunResultsTree randoopErrors;

  ICompilationUnit junitDriver;
  
  ILaunch launch;
  
  public Action debugWithJUnitAction;
  
  public Action runWithJUnitAction;
  
  public Action relaunchAction;

  /**
   * The constructor.
   */
  public TestGeneratorViewPart() {
  }

  @Override
  public void createPartControl(Composite parent) {
    fParent = parent;

    GridLayout layout = new GridLayout();
    layout.marginWidth = 3;
    layout.marginHeight = 3;
    layout.horizontalSpacing = 3;
    layout.numColumns = 1;
    parent.setLayout(layout);

    fCounterPanel = new CounterPanel(parent);
    fCounterPanel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    fProgressBar = new RandoopProgressBar(parent);
    fProgressBar.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

    Label errTitle = new Label(fParent, SWT.NONE);
    errTitle.setText("Failures:");
    
    viewer = new TreeViewer(parent);
    randoopErrors = new RunResultsTree();
    randoopErrors.viewer = viewer;
    RandoopContentProvider prov = new RandoopContentProvider(randoopErrors);
    viewer.setContentProvider(prov);
    prov.viewer = viewer;
    viewer.setLabelProvider(new RandoopLabelProvider());
    viewer.setInput(randoopErrors);
    GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.FILL;
    viewer.getControl().setLayoutData(gd);
    FailureItemDoubleClickListener doubleClickListener = new FailureItemDoubleClickListener();
    viewer.addDoubleClickListener(doubleClickListener);
    doubleClickListener.viewPart = this;

    createActions();
    createToolBar();
  }

  private void createActions() {
    debugWithJUnitAction = new Action("Debug tests with JUnit") {
      @Override
      public void run() {
        System.out.println("Running " + junitDriver);
        if (junitDriver != null) {
          List<IJavaElement> list = new ArrayList<IJavaElement>();
          list.add(junitDriver);
          IStructuredSelection selection = new StructuredSelection(list);
          
          // TODO: Is there a shared instance of JUnitLaunchShortcut?
          new JUnitLaunchShortcut().launch(selection, "debug");
        }
      }
    };
    ImageDescriptor desc = RandoopPlugin.getImageDescriptor("icons/bug.png");
    debugWithJUnitAction.setImageDescriptor(desc);
    
    debugWithJUnitAction.setEnabled(false);
    
    runWithJUnitAction = new Action("Run tests with JUnit") {
      @Override
      public void run() {
        System.out.println("Running " + junitDriver);
        if (junitDriver != null) {
          List<IJavaElement> list = new ArrayList<IJavaElement>();
          list.add(junitDriver);
          IStructuredSelection selection = new StructuredSelection(list);
          
          new JUnitLaunchShortcut().launch(selection, "run");
        }
      }
    };
    
    runWithJUnitAction.setEnabled(false);
    
    desc = RandoopPlugin.getImageDescriptor("icons/run_junit.png");
    runWithJUnitAction.setImageDescriptor(desc);
    
    relaunchAction = new Action("Regenerate tests") {
      @Override
      public void run() {
        ILaunchConfiguration config = launch.getLaunchConfiguration();
        assert config != null; // TODO right?
        String mode = launch.getLaunchMode();
        assert mode != null; // TODO right?
        DebugUITools.launch(config, mode);
      }
    };
    
    relaunchAction.setEnabled(false);

    // TODO dispose?
    desc = RandoopPlugin.getImageDescriptor("icons/arrow_redo.png");
    relaunchAction.setImageDescriptor(desc);
  }

  private void createToolBar() {
    IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
    mgr.add(debugWithJUnitAction);
    mgr.add(runWithJUnitAction);
    mgr.add(relaunchAction);
  }

  public CounterPanel getCounterPanel() {
    return fCounterPanel;
  }

  public RandoopProgressBar getProgressBar() {
    return fProgressBar;
  }

  // TODO what is the right thing to do here?
  @Override
  public void setFocus() {
  }

  public void setLaunch(ILaunch launch) {
    this.launch = launch;
  }

  public void setDriver(ICompilationUnit driver) {
    junitDriver = driver;
  }
}