package randoop.plugin.internal.ui.launching;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.jdt.internal.debug.ui.launcher.DebugTypeSelectionDialog;

import randoop.plugin.internal.ui.options.IOption;
import randoop.plugin.internal.ui.options.JUnitTestClassNameOption;
import randoop.plugin.internal.ui.options.ProjectOption;
import randoop.plugin.internal.ui.options.ClassSelectorOption;

public class GeneralTab extends OptionTab {
  private IOption fProjectOption;
  private IOption fJUnitTestClassNameOption;
  private ClassSelectorOption fTestInputSelectorOption;

  
  private ModifyListener fBasicModifyListener = new RandoopTabListener();
  private SelectionListener fBasicSelectionListener = new RandoopTabListener();
  
  private class RandoopTabListener extends SelectionAdapter implements
      ModifyListener {
    @Override
    public void widgetSelected(SelectionEvent e) {
      setErrorMessage(null);
      updateLaunchConfigurationDialog();
    }

    @Override
    public void modifyText(ModifyEvent e) {
      setErrorMessage(null);
      updateLaunchConfigurationDialog();
    }
  }

  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl(Composite parent) {
    Composite tabcomp = SWTFactory.createComposite(parent, 1, 1,
        GridData.FILL_HORIZONTAL);
    setControl(tabcomp);

    // Project group:
    Composite comp = SWTFactory.createComposite(tabcomp, 3, 1,
        GridData.FILL_HORIZONTAL);

    SWTFactory.createLabel(comp, "Project:", 1);

    Text projectText = new Text(comp, SWT.SINGLE | SWT.BORDER);
    projectText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Button projectBrowseButton = SWTFactory.createPushButton(comp, "Browse...", null);
    projectBrowseButton.setText("&Browse...");

    SWTFactory.createLabel(comp, "Output Folder:", 1);
    Text outputSourceFolderText = SWTFactory.createSingleText(comp, 1);

    Button sourceFolderBrowseButton = SWTFactory.createPushButton(comp, "Search...",
        null);

    fProjectOption = new ProjectOption(getShell(), projectText,
        projectBrowseButton, outputSourceFolderText,
        sourceFolderBrowseButton);
    Assert.isTrue(addOption(fProjectOption));
    
    projectBrowseButton.addSelectionListener(fBasicSelectionListener);
    sourceFolderBrowseButton.addSelectionListener(fBasicSelectionListener);
    
    // Class name option:
    comp = SWTFactory.createComposite(tabcomp, 3, 1,
        GridData.FILL_HORIZONTAL);
    
    SWTFactory.createLabel(comp, "JUnit Class Name:", 1);
     Text fullyQualifiedTestName = SWTFactory.createSingleText(comp, 2);
    
    fJUnitTestClassNameOption = new JUnitTestClassNameOption(
        fullyQualifiedTestName);
    Assert.isTrue(addOption(fJUnitTestClassNameOption));

    fullyQualifiedTestName.addModifyListener(fBasicModifyListener);
    
    // Test inputs option:
    fTestInputSelectorOption = new ClassSelectorOption(tabcomp,
        getLaunchConfigurationDialog(), fBasicSelectionListener);
    fProjectOption.addChangeListener(fTestInputSelectorOption);

    Assert.isTrue(addOption(fTestInputSelectorOption));
  }

  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  @Override
  public String getName() {
    return "General";
  }
  
  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
   */
  @Override
  public String getId() {
    return "randoop.plugin.ui.launching.generalrTab"; //$NON-NLS-1$
  }
  
}
