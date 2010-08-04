package randoop.plugin.internal.ui.launching;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.internal.debug.ui.JavaDebugImages;

import randoop.plugin.internal.core.TestKinds;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.ui.RandoopMessages;
import randoop.plugin.internal.ui.options.ClassSelectorOption;
import randoop.plugin.internal.ui.options.IOption;
import randoop.plugin.internal.ui.options.JUnitTestClassNameOption;
import randoop.plugin.internal.ui.options.OptionFactory;
import randoop.plugin.internal.ui.options.ProjectOption;

public class ParametersTab extends OptionLaunchConfigurationTab {
  private IOption fRandomSeed;
  private IOption fMaxTestSize;
  private IOption fUseThreads;
  private IOption fUseNull;

  private IOption fInputLimit;
  private IOption fTimeLimit;
  
  private IOption fTestKinds;
  private IOption fMaxTestsWritten;
  private IOption fMaxTestsPerFile;

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    RandoopArgumentCollector.restoreRandomSeed(config);
    RandoopArgumentCollector.restoreMaxTestSize(config);
    RandoopArgumentCollector.restoreUseThreads(config);
    RandoopArgumentCollector.restoreUseNull(config);
    
    RandoopArgumentCollector.restoreInputLimit(config);
    RandoopArgumentCollector.restoreTimeLimit(config);
    
    RandoopArgumentCollector.restoreTestKinds(config);
    RandoopArgumentCollector.restoreMaxTestSize(config);
    RandoopArgumentCollector.restoreMaxTestsPerFile(config);
  }
  
  @Override
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_HORIZONTAL);
    setControl(comp);

    createGeneralGroup(comp);
    createGenerationLimitGroup(comp);
    createOutputRestrictionsGroup(comp);

    Button restoreDefaults = new Button(comp, 0);
    restoreDefaults.setText("Restore &Defaults");
    restoreDefaults.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
    restoreDefaults.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        restoreDefaults();
      }
    });
    restoreDefaults.setEnabled(true);
  }

  private void createGeneralGroup(Composite parent) {
    Group group = SWTFactory.createGroup(parent, "General", 2, 1,
        GridData.FILL_HORIZONTAL);

    GridLayout ld = (GridLayout) group.getLayout();
    ld.marginWidth = 1;
    ld.marginHeight = 1;

    Label randomSeedLabel = SWTFactory.createLabel(group, RandoopMessages.RandoopOption_randomseed, 1);
    Text randomSeedText = SWTFactory.createSingleText(group, 1);
    randomSeedLabel.setToolTipText(RandoopMessages.RandoopOption_randomseed_tooltip);
    randomSeedText.setToolTipText(RandoopMessages.RandoopOption_randomseed_tooltip);
    fRandomSeed = OptionFactory.createRandomSeedOption(randomSeedText);

    Label maxTestSizeLabel = SWTFactory.createLabel(group, RandoopMessages.RandoopOption_maxsize, 1);
    Text maxTestSizeText = SWTFactory.createSingleText(group, 1);
    maxTestSizeLabel.setToolTipText(RandoopMessages.RandoopOption_maxsize_tooltip);
    maxTestSizeText.setToolTipText(RandoopMessages.RandoopOption_maxsize_tooltip);
    fMaxTestSize = OptionFactory.createMaximumTestSizeOption(maxTestSizeText);

    Button threadTimeoutButton = createCheckButton(group, RandoopMessages.RandoopOption_usethreads);
    Text threadTimeoutText = SWTFactory.createSingleText(group, 1);
    threadTimeoutButton.setToolTipText(RandoopMessages.RandoopOption_usethreads_tooltip);
    threadTimeoutButton.setSelection(true);
    threadTimeoutText.setToolTipText(RandoopMessages.RandoopOption_timeout_tooltip);
    threadTimeoutText.setEnabled(threadTimeoutButton.getSelection());
    IOption threadTimeout = OptionFactory.createThreadTimeoutOption(threadTimeoutText);
    fUseThreads = OptionFactory.createUseThreads(threadTimeout, threadTimeoutButton);
    
    Button nullRatioButton = createCheckButton(group, RandoopMessages.RandoopOption_forbid_null);
    Text nullRatioText = SWTFactory.createSingleText(group, 1);
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

  private void createGenerationLimitGroup(Composite parent) {
    Group group = SWTFactory
        .createGroup(parent, "Generation Limit", 2, 1, GridData.FILL_HORIZONTAL);

    GridLayout ld = (GridLayout) group.getLayout();
    ld.marginWidth = 1;
    ld.marginHeight = 1;

    Label inputLimitLabel = SWTFactory.createLabel(group, RandoopMessages.RandoopOption_inputlimit, 1);
    Text inputLimitText = SWTFactory.createSingleText(group, 1);
    inputLimitLabel.setToolTipText(RandoopMessages.RandoopOption_inputlimit_tooltip);
    inputLimitText.setToolTipText(RandoopMessages.RandoopOption_inputlimit_tooltip);
    fInputLimit = OptionFactory.createInputsLimitOption(inputLimitText);

    Label timeLimitLabel = SWTFactory.createLabel(group, RandoopMessages.RandoopOption_timelimit, 1);
    Text timeLimitText = SWTFactory.createSingleText(group, 1);
    timeLimitLabel.setToolTipText(RandoopMessages.RandoopOption_timelimit_tooltip);
    timeLimitText.setToolTipText(RandoopMessages.RandoopOption_timelimit_tooltip);

    // Create a spacer 
    SWTFactory.createLabel(group, "", 1); //$NON-NLS-1$
    Label convertedTimeLimit = SWTFactory.createLabel(group, "", 1); //$NON-NLS-1$
    convertedTimeLimit.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true,
       false));
    
    fTimeLimit = OptionFactory.createTimeLimitOption(timeLimitText, convertedTimeLimit);

    addOption(fInputLimit);
    addOption(fTimeLimit);
    
    inputLimitText.addModifyListener(getBasicModifyListener());
    timeLimitText.addModifyListener(getBasicModifyListener());
  }
  
  private void createOutputRestrictionsGroup(Composite parent) {
    Group group = SWTFactory.createGroup(parent, "Output Restrictions", 3, 1,
        GridData.FILL_HORIZONTAL);

    Label testKindsLabel = SWTFactory.createLabel(group, RandoopMessages.RandoopOption_output_tests, 1);
    Combo testKindsCombo = SWTFactory.createCombo(group, SWT.READ_ONLY, 2, TestKinds
        .getTranslatableNames());
    testKindsLabel.setToolTipText(RandoopMessages.RandoopOption_output_tests_tooltip);
    testKindsCombo.setToolTipText(RandoopMessages.RandoopOption_output_tests_tooltip);
    fTestKinds = OptionFactory.createTestKindsOption(testKindsCombo);

    Label maxTestsWrittenLabel = SWTFactory.createLabel(group, RandoopMessages.RandoopOption_outputlimit, 1);
    Text maxTestsWrittenText = SWTFactory.createSingleText(group, 2);
    maxTestsWrittenLabel.setToolTipText(RandoopMessages.RandoopOption_outputlimit_tooltip);
    maxTestsWrittenText.setToolTipText(RandoopMessages.RandoopOption_outputlimit_tooltip);
    fMaxTestsWritten = OptionFactory.createMaximumTestsWrittenOption(maxTestsWrittenText);

    Label maxTestsPerFileLabel = SWTFactory.createLabel(group, RandoopMessages.RandoopOption_testsperfile, 1);
    Text maxTestsPerFileText = SWTFactory.createSingleText(group, 2);
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
    return "&Parameters";
  }

  @Override
  public String getId() {
    return "randoop.plugin.ui.launching.parametersTab"; //$NON-NLS-1$
  }
  
  @Override
  public Image getImage() {
    return JavaDebugImages.get(JavaDebugImages.IMG_VIEW_ARGUMENTS_TAB);
  }

}
