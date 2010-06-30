package randoop.plugin.internal.ui.views;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.part.ViewPart;

import randoop.ErrorRevealed;
import randoop.plugin.RandoopPlugin;

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

  ILaunch launch;

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
    ImageDescriptor desc = RandoopPlugin.getImageDescriptor("icons/arrow_redo.png");
    relaunchAction.setImageDescriptor(desc);
  }

  private void createToolBar() {
    IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
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
}