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
import randoop.plugin.internal.ui.RandoopMessages;
import randoop.plugin.internal.ui.options.IOption;
import randoop.plugin.internal.ui.options.OptionFactory;
import randoop.plugin.internal.ui.options.JUnitTestClassNameOption;
import randoop.plugin.internal.ui.options.ProjectOption;

public class ParametersPage extends OptionWizardPage {
  private final IJavaProject fProject;
  
  private IOption fOutputFolderOption;
  private IOption fClassName;
  
  private IOption fRandomSeed;
  private IOption fMaxTestSize;
  private IOption fUseThreads;
  private IOption fUseNull;

  private IOption fInputLimit;
  private IOption fTimeLimit;
  
  private IOption fTestKinds;
  private IOption fMaxTestsWritten;
  private IOption fMaxTestsPerFile;

  protected ParametersPage(String pageName, IJavaProject project, ILaunchConfigurationWorkingCopy config) {
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
    
    SWTFactory.createLabel(comp, RandoopMessages.RandoopOption_junit_output_dir, 1);
    Text outputSourceFolderText = SWTFactory.createSingleText(comp, 1);
    Button sourceFolderBrowseButton = SWTFactory.createPushButton(comp, "&Browse...", //$NON-NLS-1$
        null);

    fOutputFolderOption = new ProjectOption(getShell(), fProject,
        outputSourceFolderText, sourceFolderBrowseButton);
    
    SWTFactory.createLabel(comp, RandoopMessages.RandoopOption_junit_package_name, 1);
    Text packageNameText = SWTFactory.createSingleText(comp, 2);
    
    SWTFactory.createLabel(comp, RandoopMessages.RandoopOption_junit_classname, 1);
    Text classNameText = SWTFactory.createSingleText(comp, 2);
    
    fClassName = new JUnitTestClassNameOption(packageNameText, classNameText);
    
    addOption(fOutputFolderOption);
    addOption(fClassName);
    
    outputSourceFolderText.addModifyListener(getBasicModifyListener());
    packageNameText.addModifyListener(getBasicModifyListener());
    classNameText.addModifyListener(getBasicModifyListener());
    
  }
  
  private void createGeneralComposite(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 2, 1, GridData.FILL_HORIZONTAL);

    GridLayout ld = (GridLayout) comp.getLayout();
    ld.marginWidth = 1;
    ld.marginHeight = 1;

    Label randomSeedLabel = SWTFactory.createLabel(comp, RandoopMessages.RandoopOption_randomseed, 1);
    Text randomSeedText = SWTFactory.createSingleText(comp, 1);
    randomSeedLabel.setToolTipText(RandoopMessages.RandoopOption_randomseed_tooltip);
    randomSeedText.setToolTipText(RandoopMessages.RandoopOption_randomseed_tooltip);
    fRandomSeed = OptionFactory.createRandomSeedOption(randomSeedText);

    Label maxTestSizeLabel = SWTFactory.createLabel(comp, RandoopMessages.RandoopOption_maxsize, 1);
    Text maxTestSizeText = SWTFactory.createSingleText(comp, 1);
    maxTestSizeLabel.setToolTipText(RandoopMessages.RandoopOption_maxsize_tooltip);
    maxTestSizeText.setToolTipText(RandoopMessages.RandoopOption_maxsize_tooltip);
    fMaxTestSize = OptionFactory.createMaximumTestSizeOption(maxTestSizeText);

    Button threadTimeoutButton = SWTFactory.createCheckButton(comp, RandoopMessages.RandoopOption_usethreads, null, true, 1);
    Text threadTimeoutText = SWTFactory.createSingleText(comp, 1);
    threadTimeoutButton.setToolTipText(RandoopMessages.RandoopOption_usethreads_tooltip);
    threadTimeoutText.setToolTipText(RandoopMessages.RandoopOption_timeout_tooltip);
    threadTimeoutText.setEnabled(threadTimeoutButton.getSelection());
    IOption threadTimeout = OptionFactory.createThreadTimeoutOption(threadTimeoutText);
    fUseThreads = OptionFactory.createUseThreads(threadTimeout, threadTimeoutButton);
    
    Button nullRatioButton = SWTFactory.createCheckButton(comp, RandoopMessages.RandoopOption_forbid_null, null, false, 1);
    Text nullRatioText = SWTFactory.createSingleText(comp, 1);
    nullRatioButton.setToolTipText(RandoopMessages.RandoopOption_forbid_null_tooltip);
    nullRatioButton.setSelection(false);
    nullRatioText.setToolTipText(RandoopMessages.RandoopOption_null_ratio_tooltip);
    nullRatioText.setEnabled(nullRatioButton.getSelection());
    IOption nullRatio = OptionFactory.createNullRatioOption(nullRatioText);
    fUseNull = OptionFactory.createUseNull(nullRatio, nullRatioButton);
    
    addOption(fRandomSeed);
    addOption(fMaxTestSize);
    addOption(fUseThreads);
    addOption(fUseNull);
    
    randomSeedText.addModifyListener(getBasicModifyListener());
    maxTestSizeText.addModifyListener(getBasicModifyListener());
    threadTimeoutButton.addSelectionListener(getBasicSelectionListener());
    threadTimeoutText.addModifyListener(getBasicModifyListener());
    nullRatioButton.addSelectionListener(getBasicSelectionListener());
    nullRatioText.addModifyListener(getBasicModifyListener());
  }

  private void createGenerationLimitComposite(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 2, 1, GridData.FILL_HORIZONTAL);

    GridLayout ld = (GridLayout) comp.getLayout();
    ld.marginWidth = 1;
    ld.marginHeight = 1;

    Label inputLimitLabel = SWTFactory.createLabel(comp, RandoopMessages.RandoopOption_inputlimit,
        1);
    Text inputLimitText = SWTFactory.createSingleText(comp, 1);
    inputLimitLabel.setToolTipText(RandoopMessages.RandoopOption_inputlimit_tooltip);
    inputLimitText.setToolTipText(RandoopMessages.RandoopOption_inputlimit_tooltip);
    fInputLimit = OptionFactory.createInputsLimitOption(inputLimitText);

    Label timeLimitLabel = SWTFactory.createLabel(comp, RandoopMessages.RandoopOption_timelimit, 1);
    Text timeLimitText = SWTFactory.createSingleText(comp, 1);
    timeLimitLabel.setToolTipText(RandoopMessages.RandoopOption_timelimit_tooltip);
    timeLimitText.setToolTipText(RandoopMessages.RandoopOption_timelimit_tooltip);

    // Create a spacer
    SWTFactory.createLabel(comp, "", 1); //$NON-NLS-1$
    Label convertedTimeLimit = SWTFactory.createLabel(comp, "", 1); //$NON-NLS-1$
    convertedTimeLimit.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));

    fTimeLimit = OptionFactory.createTimeLimitOption(timeLimitText, convertedTimeLimit);

    addOption(fInputLimit);
    addOption(fTimeLimit);

    inputLimitText.addModifyListener(getBasicModifyListener());
    timeLimitText.addModifyListener(getBasicModifyListener());
  }

  private void createOutputRestrictionsComposite(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 3, 1, GridData.FILL_HORIZONTAL);

    Label testKindsLabel = SWTFactory.createLabel(comp, RandoopMessages.RandoopOption_output_tests, 1);
    Combo testKindsCombo = SWTFactory.createCombo(comp, SWT.READ_ONLY, 2,
        TestKinds.getTranslatableNames());
    testKindsLabel.setToolTipText(RandoopMessages.RandoopOption_output_tests_tooltip);
    testKindsCombo.setToolTipText(RandoopMessages.RandoopOption_output_tests_tooltip);
    fTestKinds = OptionFactory.createTestKindsOption(testKindsCombo);

    Label maxTestsWrittenLabel = SWTFactory.createLabel(comp,
        RandoopMessages.RandoopOption_outputlimit, 1);
    Text maxTestsWrittenText = SWTFactory.createSingleText(comp, 2);
    maxTestsWrittenLabel.setToolTipText(RandoopMessages.RandoopOption_outputlimit_tooltip);
    maxTestsWrittenText.setToolTipText(RandoopMessages.RandoopOption_outputlimit_tooltip);
    fMaxTestsWritten = OptionFactory.createMaximumTestsWrittenOption(maxTestsWrittenText);

    Label maxTestsPerFileLabel = SWTFactory.createLabel(comp,
        RandoopMessages.RandoopOption_testsperfile, 1);
    Text maxTestsPerFileText = SWTFactory.createSingleText(comp, 2);
    maxTestsPerFileLabel.setToolTipText(RandoopMessages.RandoopOption_testsperfile_tooltip);
    maxTestsPerFileText.setToolTipText(RandoopMessages.RandoopOption_testsperfile_tooltip);
    fMaxTestsPerFile = OptionFactory.createMaximumTestsPerFileOption(maxTestsPerFileText);

    addOption(fTestKinds);
    addOption(fMaxTestsWritten);
    addOption(fMaxTestsPerFile);

    testKindsCombo.addModifyListener(getBasicModifyListener());
    maxTestsWrittenText.addModifyListener(getBasicModifyListener());
    maxTestsPerFileText.addModifyListener(getBasicModifyListener());
  }

  @Override
  public String getName() {
    return ""; //$NON-NLS-1$
  }

  @Override
  public void performHelp() {
  }

}