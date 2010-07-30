package randoop.plugin.internal.ui.wizards;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.core.TestKinds;
import randoop.plugin.internal.ui.options.IOption;
import randoop.plugin.internal.ui.options.IOptionFactory;
import randoop.plugin.internal.ui.options.JUnitTestClassNameOption;
import randoop.plugin.internal.ui.options.ProjectOption;

public class MainPage extends OptionWizardPage {
  private final IJavaProject fProject;
  
  private IOption fOutputFolderOption;
  private IOption fClassName;
  
  private IOption fRandomSeed;
  private IOption fMaxTestSize;
  private IOption fUseThreads;
  private IOption fThreadTimeout;
  private IOption fUseNull;
  private IOption fNullRatio;

  private IOption fJUnitTestInputs;
  private IOption fTimeLimit;
  
  private IOption fTestKinds;
  private IOption fMaxTestsWritten;
  private IOption fMaxTestsPerFile;

  protected MainPage(String pageName, IJavaProject project, ILaunchConfigurationWorkingCopy config) {
    super(pageName, config);
    
    fProject = project;
    setTitle("Randoop Launch Configuration");
  }
  
  @Override
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_HORIZONTAL);
    setControl(comp);    
    
    createResourcesComposite(comp);
    createSeperator(comp);
    createGeneralComposite(comp);
    createSeperator(comp);
    createGenerationLimitComposite(comp);
    createSeperator(comp);
    createOutputRestrictionsComposite(comp);

    restoreDefualts();
    
    super.createControl(parent);
  }

  private void createSeperator(Composite comp) {
    new Separator(SWT.SEPARATOR | SWT.HORIZONTAL | SWT.BORDER).doFillIntoGrid(
        comp, 1, convertHeightInCharsToPixels(1));
  }
  
  private void createResourcesComposite(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 3, 1, GridData.FILL_HORIZONTAL);
    
    SWTFactory.createLabel(comp, "Output Folder:", 1);
    Text outputSourceFolderText = SWTFactory.createSingleText(comp, 1);
    Button sourceFolderBrowseButton = SWTFactory.createPushButton(comp, "&Browse...",
        null);

    fOutputFolderOption = new ProjectOption(getShell(), fProject,
        outputSourceFolderText, sourceFolderBrowseButton);
    
    SWTFactory.createLabel(comp, "Package:", 1);
    Text packageNameText = SWTFactory.createSingleText(comp, 2);
    
    SWTFactory.createLabel(comp, "Name:", 1);
    Text classNameText = SWTFactory.createSingleText(comp, 2);
    
    fClassName = new JUnitTestClassNameOption(packageNameText, classNameText);
    
    addOption(fOutputFolderOption);
    addOption(fClassName);
    
    outputSourceFolderText.addModifyListener(getModifyListener());
    packageNameText.addModifyListener(getModifyListener());
    classNameText.addModifyListener(getModifyListener());
    
  }
  
  private void createGeneralComposite(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 2, 1,
        GridData.FILL_HORIZONTAL);

    GridLayout ld = (GridLayout) comp.getLayout();
    ld.marginWidth = 1;
    ld.marginHeight = 1;

    SWTFactory.createLabel(comp, "Random &Seed:", 1);
    Text randomSeed = SWTFactory.createSingleText(comp, 1);
    fRandomSeed = IOptionFactory.createRandomSeedOption(randomSeed);

    SWTFactory.createLabel(comp, "Maximum Test Si&ze:", 1);
    Text maxTestSize = SWTFactory.createSingleText(comp, 1);
    fMaxTestSize = IOptionFactory.createMaximumTestSizeOption(maxTestSize);

    Button useThreads = SWTFactory.createCheckButton(comp, "Thread Time&out:", null, false, 1);
    useThreads.setSelection(true);
    
    Text threadTimeout = SWTFactory.createSingleText(comp, 1);
    fThreadTimeout = IOptionFactory.createThreadTimeoutOption(threadTimeout);
    fUseThreads = IOptionFactory.createUseThreads(fThreadTimeout, useThreads);
    
    Button useNull = SWTFactory.createCheckButton(comp, "Null R&atio:", null, false, 1);
    useNull.setSelection(false);
    
    Text nullRatio = SWTFactory.createSingleText(comp, 1);
    nullRatio.setEnabled(useNull.getSelection());
    fNullRatio = IOptionFactory.createNullRatioOption(nullRatio);
    fUseNull = IOptionFactory.createUseNull(fNullRatio, useNull);
    
    addOption(fRandomSeed);
    addOption(fMaxTestSize);
    addOption(fUseThreads);
    addOption(fUseNull);
    
    randomSeed.addModifyListener(getModifyListener());
    maxTestSize.addModifyListener(getModifyListener());
    useThreads.addSelectionListener(getSelectionListener());
    threadTimeout.addModifyListener(getModifyListener());
    useNull.addSelectionListener(getSelectionListener());
    nullRatio.addModifyListener(getModifyListener());
  }

  private void createGenerationLimitComposite(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 2, 1,
        GridData.FILL_HORIZONTAL);

    GridLayout ld = (GridLayout) comp.getLayout();
    ld.marginWidth = 1;
    ld.marginHeight = 1;

    SWTFactory.createLabel(comp, "JUnit Test &Inputs:", 1);
    Text junitTestInputs = SWTFactory.createSingleText(comp, 1);
    fJUnitTestInputs = IOptionFactory.createJUnitTestInputsOption(junitTestInputs);

    SWTFactory.createLabel(comp, "&Time Limit:", 1);
    Text timeLimit = SWTFactory.createSingleText(comp, 1);

    // Create a spacer
    SWTFactory.createLabel(comp, "", 1); //$NON-NLS-1$
    Label convertedTimeLimit = SWTFactory.createLabel(comp, "", 1); //$NON-NLS-1$
    convertedTimeLimit.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true,
       false));
    
    fTimeLimit = IOptionFactory.createTimeLimitOption(timeLimit, convertedTimeLimit);
    
    addOption(fJUnitTestInputs);
    addOption(fTimeLimit);
    
    junitTestInputs.addModifyListener(getModifyListener());
    timeLimit.addModifyListener(getModifyListener());
  }
  
  private void createOutputRestrictionsComposite(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 3, 1,
        GridData.FILL_HORIZONTAL);

    SWTFactory.createLabel(comp, "Test &Kinds:", 1);
    Combo testKinds = SWTFactory.createCombo(comp, SWT.READ_ONLY, 2, TestKinds
        .getTranslatableNames());
    fTestKinds = IOptionFactory.createTestKindsOption(testKinds);

    SWTFactory.createLabel(comp, "Maximum Tests &Written:", 1);
    Text maxTestsWritten = SWTFactory.createSingleText(comp, 2);
    fMaxTestsWritten = IOptionFactory.createMaximumTestsWrittenOption(maxTestsWritten);

    SWTFactory.createLabel(comp, "Maximum Tests Per &File:", 1);
    Text maxTestsPerFile = SWTFactory.createSingleText(comp, 2);
    fMaxTestsPerFile = IOptionFactory.createMaximumTestsPerFileOption(maxTestsPerFile);
    
    addOption(fTestKinds);
    addOption(fMaxTestsWritten);
    addOption(fMaxTestsPerFile);
    
    testKinds.addModifyListener(getModifyListener());
    maxTestsWritten.addModifyListener(getModifyListener());
    maxTestsPerFile.addModifyListener(getModifyListener());
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public void performHelp() {
  }

}
