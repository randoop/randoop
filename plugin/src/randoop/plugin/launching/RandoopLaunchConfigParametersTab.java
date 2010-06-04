package randoop.plugin.launching;

import java.text.DecimalFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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

import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.ui.SWTFactory;

public class RandoopLaunchConfigParametersTab extends AbstractLaunchConfigurationTab {
  private Text fRandomSeed;
  private Text fMaxTestSize;
  private Button fUseThreads;
  private Text fThreadTimeout;
  private Button fUseNull;
  private Text fNullRatio;

  private Text fJUnitTestInputs;
  private Text fTimeLimit;
  private Label lConvertedTimeLimit;

  private ModifyListener fBasicModifyListener = new GeneratorTabListener();

  private class GeneratorTabListener extends SelectionAdapter implements
      ModifyListener {
    public void modifyText(ModifyEvent e) {
      setErrorMessage(null);
      updateLaunchConfigurationDialog();
    }
  }
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
    fRandomSeed = SWTFactory.createSingleText(comp, 1);
    fRandomSeed.addModifyListener(fBasicModifyListener);

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
    fTimeLimit.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setConvertedTime();
      }
    });
    fTimeLimit.addModifyListener(fBasicModifyListener);

    SWTFactory.createLabel(comp, IRandoopLaunchConfigConstants.EMPTY_STRING, 1); // spacer
    lConvertedTimeLimit = SWTFactory.createLabel(comp, IRandoopLaunchConfigConstants.EMPTY_STRING, 1);
    lConvertedTimeLimit.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false,
        false));
    setConvertedTime();
  }

  @Override
  public boolean canSave() {
    setErrorMessage(null);

    if (fRandomSeed == null || fMaxTestSize == null || fUseThreads == null
        || fThreadTimeout == null || fUseNull == null || fNullRatio == null
        || fJUnitTestInputs == null || fTimeLimit == null
        || lConvertedTimeLimit == null) {
      return false;
    }
    
    String randomSeed = fRandomSeed.getText();
    String maxTestSize = fMaxTestSize.getText();
    boolean useThreads = fUseThreads.getSelection();
    String threadTimeout = fThreadTimeout.getText();
    boolean useNull = fUseNull.getSelection();
    String nullRatio = fNullRatio.getText();
    String junitTestInputs = fJUnitTestInputs.getText();
    String timeLimit = fTimeLimit.getText();

    IStatus status = validate(randomSeed, maxTestSize, useThreads, threadTimeout, useNull,
        nullRatio, junitTestInputs, timeLimit);
    if(status.isOK()) {
      return true;
    } else {
      setErrorMessage(status.getMessage());
      return false;
    }
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
   */
  @Override
  public boolean isValid(ILaunchConfiguration config) {
    String randomSeed = getRandomSeed(config);
    String maxTestSize = getMaxTestSize(config);
    boolean useThreads = getUseThreads(config);
    String threadTimeout = getThreadTimeout(config);
    boolean useNull = getUseNull(config);
    String nullRatio = getNullRatio(config);
    String junitTestInputs = getJUnitTestInputs(config);
    String timeLimit = getTimeLimit(config);

    return validate(randomSeed, maxTestSize, useThreads, threadTimeout, useNull,
        nullRatio, junitTestInputs, timeLimit).isOK();
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fRandomSeed != null)
      config.setAttribute(IRandoopLaunchConfigConstants.ATTR_RANDOM_SEED, fRandomSeed
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
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
   */
  public void initializeFrom(ILaunchConfiguration config) {
    if (fRandomSeed != null)
      fRandomSeed.setText(getRandomSeed(config));
    if (fMaxTestSize != null)
      fMaxTestSize.setText(getMaxTestSize(config));
    if (fUseThreads != null)
      fUseThreads.setSelection(getUseThreads(config));
    if (fThreadTimeout != null)
      fThreadTimeout.setText(getThreadTimeout(config));
    if (fUseNull != null)
      fUseNull.setSelection(getUseNull(config));
    if (fNullRatio != null)
      fNullRatio.setText(getNullRatio(config));
    if (fJUnitTestInputs != null)
      fJUnitTestInputs.setText(getJUnitTestInputs(config));
    if (fTimeLimit != null) {
      fTimeLimit.setText(getTimeLimit(config));
    }
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

  protected IStatus validate(String randomSeed, String maxTestSize,
      boolean useThreads, String threadTimeout, boolean useNull,
      String nullRatio, String junitTestInputs, String timeLimit) {
    
    try {
      Integer.parseInt(randomSeed);
    } catch (NumberFormatException nfe) {
      return StatusFactory.createErrorStatus("Random Seed is not a valid integer");
    }
    
    IStatus status = RandoopLaunchingUtil.validatePositiveInt(maxTestSize, "Maximum Test Size");
    if (status.getSeverity() == IStatus.ERROR) {
      return status;
    }
    if (useThreads) {
      status = RandoopLaunchingUtil.validatePositiveInt(threadTimeout, "Thread Timeout");
      if (status.getSeverity() == IStatus.ERROR) {
        return status;
      }
    }
    
    try {
      if (useNull) {
        Double.parseDouble(nullRatio);
      }
    } catch (NumberFormatException nfe) {
      return StatusFactory.createErrorStatus("Null Ratio is not a valid number");
    }

    status = RandoopLaunchingUtil.validatePositiveInt(junitTestInputs, "JUnit Test Inputs");
    if (status.getSeverity() == IStatus.ERROR) {
      return status;
    }
    
    status = RandoopLaunchingUtil.validatePositiveInt(timeLimit, "Time Limit");
    if (status.getSeverity() == IStatus.ERROR) {
      return status;
    }

    return Status.OK_STATUS;
  }

  public String getRandomSeed(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_RANDOM_SEED,
          IRandoopLaunchConfigConstants.DEFAULT_RANDOM_SEED);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_RANDOM_SEED;
    }
  }

  public String getMaxTestSize(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_MAXIMUM_TEST_SIZE,
          IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TEST_SIZE);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_MAXIMUM_TEST_SIZE;
    }
  }

  public boolean getUseThreads(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_USE_THREADS, Boolean
              .parseBoolean(IRandoopLaunchConfigConstants.ATTR_USE_THREADS));
    } catch (CoreException ce) {
      return Boolean
          .parseBoolean(IRandoopLaunchConfigConstants.DEFAULT_USE_THREADS);
    }
  }

  public String getThreadTimeout(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_THREAD_TIMEOUT,
          IRandoopLaunchConfigConstants.DEFAULT_THREAD_TIMEOUT);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_THREAD_TIMEOUT;
    }
  }

  public boolean getUseNull(ILaunchConfiguration config) {
    try {
      return config.getAttribute(IRandoopLaunchConfigConstants.ATTR_USE_NULL,
          Boolean.parseBoolean(IRandoopLaunchConfigConstants.ATTR_USE_NULL));
    } catch (CoreException ce) {
      return Boolean
          .parseBoolean(IRandoopLaunchConfigConstants.DEFAULT_USE_NULL);
    }
  }

  public String getNullRatio(ILaunchConfiguration config) {
    try {
      return config.getAttribute(IRandoopLaunchConfigConstants.ATTR_NULL_RATIO,
          IRandoopLaunchConfigConstants.DEFAULT_NULL_RATIO);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_NULL_RATIO;
    }
  }

  public String getJUnitTestInputs(ILaunchConfiguration config) {
    try {
      return config.getAttribute(
          IRandoopLaunchConfigConstants.ATTR_JUNIT_TEST_INPUTS,
          IRandoopLaunchConfigConstants.DEFAULT_JUNIT_TEST_INPUTS);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_JUNIT_TEST_INPUTS;
    }
  }

  public String getTimeLimit(ILaunchConfiguration config) {
    try {
      return config.getAttribute(IRandoopLaunchConfigConstants.ATTR_TIME_LIMIT,
          IRandoopLaunchConfigConstants.DEFAULT_TIME_LIMIT);
    } catch (CoreException ce) {
      return IRandoopLaunchConfigConstants.DEFAULT_TIME_LIMIT;
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
        lConvertedTimeLimit.setText(IRandoopLaunchConfigConstants.EMPTY_STRING);
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
      lConvertedTimeLimit.setText(IRandoopLaunchConfigConstants.EMPTY_STRING);
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
