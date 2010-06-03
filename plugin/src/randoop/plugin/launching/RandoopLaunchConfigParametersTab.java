package randoop.plugin.launching;

import java.text.DecimalFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.ui.SWTFactory;

public class RandoopLaunchConfigParametersTab extends AbstractLaunchConfigurationTab {
  private Text fRandomSeedText;
  private Text fMaxTestSize;
  private Button fUseThreads;
  private Text fThreadTimeout;
  private Button fUseNull;
  private Text fNullRatio;

  private Text fJUnitTestInputs;
  private Text fTimeLimit;
  private Label lConvertedTimeLimit;

  private class GeneratorTabListener extends SelectionAdapter implements
      ModifyListener {
    public void modifyText(ModifyEvent e) {
      setErrorMessage(null);
      updateLaunchConfigurationDialog();
    }
  }

  private ModifyListener fBasicModifyListener = new GeneratorTabListener();

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
   */
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 1, 1,
        GridData.FILL_HORIZONTAL);
    setControl(comp);

    createGeneralGroup(comp);
    createGenerationLimitGroup(comp);

    Button bRestoreDefaults = new Button(comp, 0);
    bRestoreDefaults.setText("Restore Defaults");
    bRestoreDefaults.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false,
        false));
    bRestoreDefaults.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {

      }
    });
    bRestoreDefaults.setEnabled(true);
  }

  private void createGeneralGroup(Composite parent) {
    Group group = SWTFactory.createGroup(parent, "General", 1, 1,
        GridData.FILL_HORIZONTAL);

    Composite comp = SWTFactory
        .createComposite(group, 2, 1, GridData.FILL_BOTH);
    GridLayout ld = (GridLayout) comp.getLayout();
    ld.marginWidth = 1;
    ld.marginHeight = 1;

    SWTFactory.createLabel(comp, "Random &Seed:", 1);
    fRandomSeedText = SWTFactory.createSingleText(comp, 1);
    fRandomSeedText.addModifyListener(fBasicModifyListener);

    SWTFactory.createLabel(comp, "Maximum Test Si&ze:", 1);
    fMaxTestSize = SWTFactory.createSingleText(comp, 1);
    fMaxTestSize.addModifyListener(fBasicModifyListener);

    fUseThreads = createCheckButton(comp, "Thread Time&out:");
    fUseThreads.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fThreadTimeout.setEnabled(fUseThreads.getSelection());
        updateLaunchConfigurationDialog();
      }
    });

    fThreadTimeout = SWTFactory.createSingleText(comp, 1);
    fThreadTimeout.addModifyListener(fBasicModifyListener);
    fThreadTimeout.setEnabled(Boolean
        .parseBoolean(IRandoopLaunchConfigConstants.DEFAULT_USE_THREADS));

    fUseNull = createCheckButton(comp, "Null R&atio:");
    fUseNull.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fNullRatio.setEnabled(fUseNull.getSelection());
        updateLaunchConfigurationDialog();
      }
    });
    fNullRatio = SWTFactory.createSingleText(comp, 1);
    fNullRatio.addModifyListener(fBasicModifyListener);
    fNullRatio.setEnabled(fUseNull.getSelection());
  }

  private void createGenerationLimitGroup(Composite parent) {
    Group group = SWTFactory.createGroup(parent, "Generation Limit", 1, 1,
        GridData.FILL_HORIZONTAL);

    Composite comp = SWTFactory
        .createComposite(group, 2, 2, GridData.FILL_BOTH);
    GridLayout ld = (GridLayout) comp.getLayout();
    ld.marginWidth = 1;
    ld.marginHeight = 1;

    SWTFactory.createLabel(comp, "JUnit Test &Inputs:", 1);
    fJUnitTestInputs = SWTFactory.createSingleText(comp, 1);
    fJUnitTestInputs.addModifyListener(fBasicModifyListener);

    SWTFactory.createLabel(comp, "&Time Limit:", 1);
    fTimeLimit = SWTFactory.createSingleText(comp, 1);
    fTimeLimit.addModifyListener(fBasicModifyListener);

    SWTFactory.createLabel(comp, "", 1); // spacer
    lConvertedTimeLimit = SWTFactory.createLabel(comp, "", 1);
    lConvertedTimeLimit.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false,
        false));
    setConvertedTime();
  }

  @Override
  public boolean canSave() {
    setErrorMessage(null);

    if (fRandomSeedText == null || fMaxTestSize == null || fUseThreads == null
        || fThreadTimeout == null || fUseNull == null || fNullRatio == null
        || fJUnitTestInputs == null || fTimeLimit == null
        || lConvertedTimeLimit == null) {
      return false;
    }

    try {
      Integer.parseInt(fRandomSeedText.getText());
    } catch (NumberFormatException nfe) {
      setErrorMessage("Random Seed is not a valid integer");
      return false;
    }
    if (!assertPositiveInt("Maximum Test Size", fMaxTestSize.getText())) {
      return false;
    }
    if (fUseThreads.getSelection()) {
      if (!assertPositiveInt("Thread Timeout", fThreadTimeout.getText())) {
        return false;
      }
    }
    try {
      if (fUseNull.getSelection()) {
        Double.parseDouble(fNullRatio.getText());
      }
    } catch (NumberFormatException nfe) {
      setErrorMessage("Null Ratio is not a valid number");
      return false;
    }

    if (!assertPositiveInt("JUnit Test Inputs", fJUnitTestInputs.getText())) {
      return false;
    }
    if (assertPositiveInt("Time Limit", fTimeLimit.getText())) {
      setConvertedTime();
    } else {
      lConvertedTimeLimit.setText("");
      return false;
    }

    return true;
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
   */
  @Override
  public boolean isValid(ILaunchConfiguration launchConfig) {
    RandoopLaunchConfigParametersTab tab = new RandoopLaunchConfigParametersTab();
    tab.initializeFrom(launchConfig);
    return tab.canSave();
  };

  private boolean assertPositiveInt(String name, String n) {
    try {
      if (Integer.parseInt(n) < 1) {
        setErrorMessage(name + " is not a positive integer");
        return false;
      }
      return true;
    } catch (NumberFormatException nfe) {
      setErrorMessage(name + " is not a valid integer");
      return false;
    }
  }

  private void setConvertedTime() {
    final String MINUTES = "minutes";
    final String HOURS = "hours";
    final String DAYS = "days";
    final String YEARS = "years";

    try {
      int seconds = Integer.parseInt(fTimeLimit.getText());

      DecimalFormat time = new DecimalFormat("#0.0"); //$NON-NLS-1$
      if (seconds < 60) {
        lConvertedTimeLimit.setText(""); //$NON-NLS-1$
      } else if (seconds < 3600) {
        lConvertedTimeLimit
            .setText("(" + time.format(seconds / 60.0) + " " + MINUTES + ")"); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
      } else if (seconds < 86400) {
        lConvertedTimeLimit
            .setText("(" + time.format(seconds / 3600.0) + " " + HOURS + ")"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
      } else if (seconds < 31556926) {
        lConvertedTimeLimit
            .setText("(" + time.format(seconds / 86400.0) + " " + DAYS + ")"); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
      } else {
        lConvertedTimeLimit
            .setText("(" + time.format(seconds / 31556926.0) + " " + YEARS + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    } catch (NumberFormatException e) {
    }

  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fRandomSeedText != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_RANDOM_SEED, fRandomSeedText
          .getText());
    if (fMaxTestSize != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_MAXIMUM_TEST_SIZE,
          fMaxTestSize.getText());
    if (fUseThreads != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_USE_THREADS, Boolean
          .toString(fUseThreads.getSelection()));
    if (fThreadTimeout != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_THREAD_TIMEOUT,
          fThreadTimeout.getText());
    if (fUseNull != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_USE_NULL, Boolean
          .toString(fUseNull.getSelection()));
    if (fNullRatio != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_NULL_RATIO, fNullRatio
          .getText());
    if (fJUnitTestInputs != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_JUNIT_TEST_INPUTS,
          fJUnitTestInputs.getText());
    if (fTimeLimit != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_TIME_LIMIT, fTimeLimit
          .getText());
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
   */
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    config.setAttribute(IRandoopLaunchConfigConstants.ATTR_RANDOM_SEED,
        IRandoopLaunchConfigConstants.DEFAULT_RANDOM_SEED);
    config.setAttribute(IRandoopLaunchConfigConstants.ATTR_MAXIMUM_TEST_SIZE,
        IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TEST_SIZE);
    config.setAttribute(IRandoopLaunchConfigConstants.ATTR_USE_THREADS,
        IRandoopLaunchConfigConstants.DEFAULT_USE_THREADS);
    config.setAttribute(IRandoopLaunchConfigConstants.ATTR_THREAD_TIMEOUT,
        IRandoopLaunchConfigConstants.DEFAULT_THREAD_TIMEOUT);
    config.setAttribute(IRandoopLaunchConfigConstants.ATTR_USE_NULL,
        IRandoopLaunchConfigConstants.DEFAULT_USE_NULL);
    config.setAttribute(IRandoopLaunchConfigConstants.ATTR_NULL_RATIO,
        IRandoopLaunchConfigConstants.DEFAULT_NULL_RATIO);
    config.setAttribute(IRandoopLaunchConfigConstants.ATTR_JUNIT_TEST_INPUTS,
        IRandoopLaunchConfigConstants.DEFAULT_JUNIT_TEST_INPUTS);
    config.setAttribute(IRandoopLaunchConfigConstants.ATTR_TIME_LIMIT,
        IRandoopLaunchConfigConstants.DEFAULT_TIME_LIMIT);
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
   */
  public void initializeFrom(ILaunchConfiguration config) {
    if (fRandomSeedText != null)
      try {
        fRandomSeedText.setText(config.getAttribute(
            IRandoopLaunchConfigConstants.ATTR_RANDOM_SEED,
            IRandoopLaunchConfigConstants.DEFAULT_RANDOM_SEED));
      } catch (CoreException ce) {
        fRandomSeedText.setText(IRandoopLaunchConfigConstants.DEFAULT_RANDOM_SEED);
      }
    if (fMaxTestSize != null)
      try {
        fMaxTestSize.setText(config.getAttribute(
            IRandoopLaunchConfigConstants.ATTR_MAXIMUM_TEST_SIZE,
            IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TEST_SIZE));
      } catch (CoreException ce) {
        fMaxTestSize.setText(IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TEST_SIZE);
      }
    if (fUseThreads != null)
      try {
        fUseThreads.setSelection(config.getAttribute(
            IRandoopLaunchConfigConstants.ATTR_USE_THREADS, Boolean
                .parseBoolean(IRandoopLaunchConfigConstants.ATTR_USE_THREADS)));
      } catch (CoreException ce) {
        fUseThreads.setSelection(Boolean
            .parseBoolean(IRandoopLaunchConfigConstants.DEFAULT_USE_THREADS));
      }
    if (fThreadTimeout != null)
      try {
        fThreadTimeout.setText(config.getAttribute(
            IRandoopLaunchConfigConstants.ATTR_THREAD_TIMEOUT,
            IRandoopLaunchConfigConstants.DEFAULT_THREAD_TIMEOUT));
      } catch (CoreException ce) {
        fThreadTimeout.setText(IRandoopLaunchConfigConstants.DEFAULT_THREAD_TIMEOUT);
      }
    if (fUseNull != null)
      try {
        fUseNull.setSelection(config.getAttribute(
            IRandoopLaunchConfigConstants.ATTR_USE_NULL, Boolean
                .parseBoolean(IRandoopLaunchConfigConstants.ATTR_USE_NULL)));
      } catch (CoreException ce) {
        fUseNull.setSelection(Boolean
            .parseBoolean(IRandoopLaunchConfigConstants.DEFAULT_USE_NULL));
      }
    if (fNullRatio != null)
      try {
        fNullRatio.setText(config.getAttribute(
            IRandoopLaunchConfigConstants.ATTR_NULL_RATIO,
            IRandoopLaunchConfigConstants.DEFAULT_NULL_RATIO));
      } catch (CoreException ce) {
        fNullRatio.setText(IRandoopLaunchConfigConstants.DEFAULT_NULL_RATIO);
      }
    if (fJUnitTestInputs != null)
      try {
        fJUnitTestInputs.setText(config.getAttribute(
            IRandoopLaunchConfigConstants.ATTR_JUNIT_TEST_INPUTS,
            IRandoopLaunchConfigConstants.DEFAULT_JUNIT_TEST_INPUTS));
      } catch (CoreException ce) {
        fJUnitTestInputs.setText(IRandoopLaunchConfigConstants.DEFAULT_JUNIT_TEST_INPUTS);
      }
    if (fTimeLimit != null) {
      try {
        fTimeLimit.setText(config.getAttribute(
            IRandoopLaunchConfigConstants.ATTR_TIME_LIMIT,
            IRandoopLaunchConfigConstants.DEFAULT_TIME_LIMIT));
      } catch (CoreException ce) {
        fTimeLimit.setText(IRandoopLaunchConfigConstants.DEFAULT_TIME_LIMIT);
      }
      if (lConvertedTimeLimit != null)
        setConvertedTime();
    }
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  public String getName() {
    return "Parameters";
  }

  /**
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
   * 
   * @since 3.3
   */
  @Override
  public String getId() {
    return "randoop.plugin.launching.testInputConfig.parameters"; //$NON-NLS-1$
  }
}
