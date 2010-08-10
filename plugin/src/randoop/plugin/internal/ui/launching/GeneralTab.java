package randoop.plugin.internal.ui.launching;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.WorkbenchJob;

import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.ui.options.IOption;
import randoop.plugin.internal.ui.options.JUnitTestClassNameOption;
import randoop.plugin.internal.ui.options.ProjectOption;
import randoop.plugin.internal.ui.options.ClassSelectorOption;

public class GeneralTab extends OptionLaunchConfigurationTab {
  private ProjectOption fProjectOption;
  private JUnitTestClassNameOption fJUnitTestClassNameOption;
  private ClassSelectorOption fTestInputSelectorOption;
  
  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    ProjectOption.writeDefaults(config);
    JUnitTestClassNameOption.writeDefaults(config);
    ClassSelectorOption.writeDefaults(config);
  }

  @Override
  public void createControl(Composite parent) {
    Composite tabcomp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_HORIZONTAL);
    setControl(tabcomp);

    // Project group:
    Composite comp = SWTFactory.createComposite(tabcomp, 3, 1, GridData.FILL_HORIZONTAL);

    SWTFactory.createLabel(comp, "Pro&ject:", 1);

    Text projectText = new Text(comp, SWT.SINGLE | SWT.BORDER);
    projectText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Button projectBrowseButton = SWTFactory.createPushButton(comp, "Browse...", null);
    projectBrowseButton.setText("&Browse...");

    SWTFactory.createLabel(comp, "&Output Folder:", 1);
    Text outputSourceFolderText = SWTFactory.createSingleText(comp, 1);

    Button sourceFolderBrowseButton = SWTFactory.createPushButton(comp, "&Search...", null);

    fProjectOption = new ProjectOption(getShell(), projectText, projectBrowseButton,
        outputSourceFolderText, sourceFolderBrowseButton);

    // Class name option:
    comp = SWTFactory.createComposite(tabcomp, 3, 1, GridData.FILL_HORIZONTAL);

    SWTFactory.createLabel(comp, "JUnit Class Na&me:", 1);
    Text fullyQualifiedTestName = SWTFactory.createSingleText(comp, 2);

    fJUnitTestClassNameOption = new JUnitTestClassNameOption(fullyQualifiedTestName);

    // Test inputs option:
    fTestInputSelectorOption = new ClassSelectorOption(tabcomp, getLaunchConfigurationDialog(), getBasicSelectionListener());
    fProjectOption.addChangeListener(fTestInputSelectorOption);

    addOption(fProjectOption);
    addOption(fJUnitTestClassNameOption);
    addOption(fTestInputSelectorOption);
    
    projectText.addModifyListener(getBasicModifyListener());
    outputSourceFolderText.addModifyListener(getBasicModifyListener());
    projectBrowseButton.addSelectionListener(getBasicSelectionListener());
    sourceFolderBrowseButton.addSelectionListener(getBasicSelectionListener());

    fullyQualifiedTestName.addModifyListener(getBasicModifyListener());
  }

  @Override
  public String getName() {
    return "&General";
  }
  
  @Override
  public Image getImage() {
    return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
  }

//  /**
//   * The control for this page, or <code>null</code>
//   */
//  private Control fControl;
//
//  /**
//   * The launch configuration dialog this tab is contained in.
//   */
//  private ILaunchConfigurationDialog fLaunchConfigurationDialog;
//
//  /**
//   * Current error message, or <code>null</code>
//   */
//  private String fErrorMessage;
//
//  /**
//   * Current message, or <code>null</code>
//   */
//  private String fMessage;
//
//  /**
//   * Whether this tab needs to apply changes. This attribute is initialized to
//   * <code>true</code> to be backwards compatible. If clients want to take
//   * advantage of such a feature, they should set the flag to false, and check
//   * it before applying changes to the launch configuration working copy.
//   * 
//   * @since 2.1
//   */
//  private boolean fDirty = true;
//
//  /**
//   * Returns the dialog this tab is contained in, or <code>null</code> if not
//   * yet set.
//   * 
//   * @return launch configuration dialog, or <code>null</code>
//   */
//  protected ILaunchConfigurationDialog getLaunchConfigurationDialog() {
//    return fLaunchConfigurationDialog;
//  }
//
//  /**
//   * Updates the buttons and message in this page's launch configuration dialog.
//   */
//  protected void updateLaunchConfigurationDialog() {
////    if (getLaunchConfigurationDialog() != null) {
////      // order is important here due to the call to
////      // refresh the tab viewer in updateButtons()
////      // which ensures that the messages are up to date
////      getLaunchConfigurationDialog().updateButtons();
////      getLaunchConfigurationDialog().updateMessage();
////    }
//  }
//
//  /**
//   * @see ILaunchConfigurationTab#getControl()
//   */
//  @Override
//  public Control getControl() {
//    return fControl;
//  }
//
//  /**
//   * Sets the control to be displayed in this tab.
//   * 
//   * @param control
//   *          the control for this tab
//   */
//  protected void setControl(Control control) {
//    fControl = control;
//  }
//
//  /**
//   * @see ILaunchConfigurationTab#getErrorMessage()
//   */
//  @Override
//  public String getErrorMessage() {
//    return fErrorMessage;
//  }
//
//  /**
//   * @see ILaunchConfigurationTab#getMessage()
//   */
//  @Override
//  public String getMessage() {
//    return fMessage;
//  }
//
//  /**
//   * By default, do nothing.
//   * 
//   * @see ILaunchConfigurationTab#launched(ILaunch)
//   * @deprecated
//   */
//  @Override
//  public void launched(ILaunch launch) {
//  }
//
//  /**
//   * @see ILaunchConfigurationTab#setLaunchConfigurationDialog(ILaunchConfigurationDialog)
//   */
//  @Override
//  public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
//    fLaunchConfigurationDialog = dialog;
//  }
//
//  /**
//   * Sets this page's error message, possibly <code>null</code>.
//   * 
//   * @param errorMessage
//   *          the error message or <code>null</code>
//   */
//  protected void setErrorMessage(String errorMessage) {
//    fErrorMessage = errorMessage;
//  }
//
//  /**
//   * Sets this page's message, possibly <code>null</code>.
//   * 
//   * @param message
//   *          the message or <code>null</code>
//   */
//  protected void setMessage(String message) {
//    fMessage = message;
//  }
//
//  /**
//   * By default, do nothing.
//   * 
//   * @see ILaunchConfigurationTab#dispose()
//   */
//  @Override
//  public void dispose() {
//  }
//
//  /**
//   * Returns the shell this tab is contained in, or <code>null</code>.
//   * 
//   * @return the shell this tab is contained in, or <code>null</code>
//   */
//  protected Shell getShell() {
//    Control control = getControl();
//    if (control != null) {
//      return control.getShell();
//    }
//    return null;
//  }
//
//  /**
//   * @see ILaunchConfigurationTab#canSave()
//   */
//  public boolean canSave() {
//    return true;
//  }
//
//  /**
//   * @see ILaunchConfigurationTab#isValid(ILaunchConfiguration)
//   */
//  public boolean isValid(ILaunchConfiguration launchConfig) {
//    return true;
//  }
//
//  /**
//   * This method was added to the <code>ILaunchConfigurationTab</code> interface
//   * in the 3.0 release to allow tabs to distinguish between a tab being
//   * activated and a tab group be initialized for the first time, from a
//   * selected launch configuration. To maintain backwards compatible behavior,
//   * the default implementation provided, calls this tab's
//   * <code>initializeFrom</code> method. Tabs should override this method as
//   * required.
//   * <p>
//   * The launch tab framework was originally designed to take care of inter tab
//   * communication by applying attributes from the active tab to the launch
//   * configuration being edited, when a tab is exited, and by initializing a tab
//   * when activated. The addition of the methods <code>activated</code> and
//   * <code>deactivated</code> allow tabs to determine the appropriate course of
//   * action.
//   * </p>
//   * 
//   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
//   * @since 3.0
//   */
//  @Override
//  public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
////    initializeFrom(workingCopy);
//  }
//
//  /**
//   * This method was added to the <code>ILaunchConfigurationTab</code> interface
//   * in the 3.0 release to allow tabs to distinguish between a tab being
//   * deactivated and saving its attributes to a launch configuration. To
//   * maintain backwards compatible behavior, the default implementation
//   * provided, calls this tab's <code>performApply</code> method. Tabs should
//   * override this method as required.
//   * <p>
//   * The launch tab framework was originally designed to take care of inter tab
//   * communication by applying attributes from the active tab to the launch
//   * configuration being edited, when a tab is exited, and by initializing a tab
//   * when activated. The addition of the methods <code>activated</code> and
//   * <code>deactivated</code> allow tabs to determine the appropriate course of
//   * action.
//   * </p>
//   * 
//   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
//   * @since 3.0
//   */
//  @Override
//  public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
////    performApply(workingCopy);
//  }
//
//  @Override
//  public void initializeFrom(ILaunchConfiguration configuration) {
//    System.out.println("initing");
//  }
//
//  @Override
//  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
//    System.out.println("apply"); //$NON-NLS-1$
//  }

}
