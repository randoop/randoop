package randoop.plugin.internal.ui.views;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import randoop.ErrorRevealed;
import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class TestGeneratorViewPart extends ViewPart {
  /**
   * The ID of the view as specified by the extension.
   */
  public static final String ID = "randoop.plugin.ui.views.TestGeneratorViewPart"; //$NON-NLS-1$

  private TreeViewer viewer;
  private Composite fParent;
  private CounterPanel fCounterPanel;
  private RandoopProgressBar fProgressBar;

  Map<String, Set<ErrorRevealed>> errors;

  RandoopErrors randoopErrors;

  ICompilationUnit junitDriver;
  
  ILaunch launch;
  
  Action debugWithJUnitAction;
  
  Action runWithJUnitAction;
  
  Action relaunchAction;

  /**
   * The constructor.
   */
  public TestGeneratorViewPart() {
  }

  /**
   * This is a callback that will allow us to create the viewer and initialize
   * it.
   */
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

    errors = new LinkedHashMap<String, Set<ErrorRevealed>>();

    Label errTitle = new Label(fParent, SWT.NONE);
    errTitle.setText("Errors:");
    
    viewer = new TreeViewer(parent);
    randoopErrors = new RandoopErrors(errors);
    randoopErrors.viewPart = this;
    viewer.setContentProvider(randoopErrors);
    viewer.setLabelProvider(new RandoopLabelProvider());
    viewer.setInput(errors);
    GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    gd.horizontalAlignment = SWT.FILL;
    gd.verticalAlignment = SWT.FILL;
    viewer.getControl().setLayoutData(gd);
    viewer.addDoubleClickListener(randoopErrors);

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
    
    runWithJUnitAction = new Action("Run tests with JUnit") {
      @Override
      public void run() {
        System.out.println("Running " + junitDriver);
        if (junitDriver != null) {
          List<IJavaElement> list = new ArrayList<IJavaElement>();
          list.add(junitDriver);
          IStructuredSelection selection = new StructuredSelection(list);
          
          // TODO: Is there a shared instance of JUnitLaunchShortcut?
          new JUnitLaunchShortcut().launch(selection, "run");
        }
      }
    };
    desc = RandoopPlugin.getImageDescriptor("icons/run_junit.png");
    runWithJUnitAction.setImageDescriptor(desc);
    
    relaunchAction = new Action("Rerun last launch") {
      public void run() {
        ILaunchConfiguration config = launch.getLaunchConfiguration();
        assert config != null; // TODO right?
        String mode = launch.getLaunchMode();
        assert mode != null; // TODO right?
        DebugUITools.launch(config, mode);
      }
    };

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