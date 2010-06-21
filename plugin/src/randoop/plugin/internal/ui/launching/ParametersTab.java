package randoop.plugin.internal.ui.launching;

import java.text.DecimalFormat;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.jdt.internal.debug.ui.SWTFactory;
import org.eclipse.jdt.ui.ISharedImages;

import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class ParametersTab extends AbstractLaunchConfigurationTab {
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
  
  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent) {
    Composite comp = SWTFactory.createComposite(parent, 1, 1,
        GridData.FILL_HORIZONTAL);
    setControl(comp);

    createGeneralGroup(comp);
    createGenerationLimitGroup(comp);

    Button bRestoreDefaults = new Button(comp, 0);
    bRestoreDefaults.setText("Restore Defaults");
    bRestoreDefaults.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
    bRestoreDefaults.addSelectionListener(new SelectionAdapter() {
      @Override
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

    SWTFactory.createLabel(comp, IConstants.EMPTY_STRING, 1); // spacer
    lConvertedTimeLimit = SWTFactory.createLabel(comp, IConstants.EMPTY_STRING, 1);
    lConvertedTimeLimit.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false,
        false));
    setConvertedTime();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
   */
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

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
   */
  @Override
  public boolean isValid(ILaunchConfiguration config) {
    String randomSeed = RandoopArgumentCollector.getRandomSeed(config);
    String maxTestSize = RandoopArgumentCollector.getMaxTestSize(config);
    boolean useThreads = RandoopArgumentCollector.getUseThreads(config);
    String threadTimeout = RandoopArgumentCollector.getThreadTimeout(config);
    boolean useNull = RandoopArgumentCollector.getUseNull(config);
    String nullRatio = RandoopArgumentCollector.getNullRatio(config);
    String junitTestInputs = RandoopArgumentCollector.getJUnitTestInputs(config);
    String timeLimit = RandoopArgumentCollector.getTimeLimit(config);

    IStatus status = validate(randomSeed, maxTestSize, useThreads,
        threadTimeout, useNull, nullRatio, junitTestInputs, timeLimit);
    if (status.getSeverity() == IStatus.ERROR) {
      setErrorMessage(status.getMessage());
      return false;
    } else {
      setMessage(status.getMessage());
      return true;
    }
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  @Override
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fRandomSeed != null)
      RandoopArgumentCollector.setRandomSeed(config, fRandomSeed.getText());
    if (fMaxTestSize != null)
      RandoopArgumentCollector.setMaxTestSize(config, fMaxTestSize.getText());
    if (fUseThreads != null)
      RandoopArgumentCollector.setUseThreads(config, fUseThreads.getSelection());
    if (fThreadTimeout != null)
      RandoopArgumentCollector.setThreadTimeout(config,  fThreadTimeout.getText());
    if (fUseNull != null)
      RandoopArgumentCollector.setUseNull(config, fUseNull.getSelection());
    if (fNullRatio != null)
      RandoopArgumentCollector.setNullRatio(config, fNullRatio.getText());
    if (fJUnitTestInputs != null)
      RandoopArgumentCollector.setJUnitTestInputs(config, fJUnitTestInputs.getText());
    if (fTimeLimit != null)
      RandoopArgumentCollector.setTimeLimit(config, fTimeLimit.getText());
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
   */
  @Override
  public void initializeFrom(ILaunchConfiguration config) {
    if (fRandomSeed != null)
      fRandomSeed.setText(RandoopArgumentCollector.getRandomSeed(config));
    if (fMaxTestSize != null)
      fMaxTestSize.setText(RandoopArgumentCollector.getMaxTestSize(config));
    if (fUseThreads != null)
      fUseThreads.setSelection(RandoopArgumentCollector.getUseThreads(config));
    if (fThreadTimeout != null)
      fThreadTimeout.setText(RandoopArgumentCollector.getThreadTimeout(config));
    if (fUseNull != null)
      fUseNull.setSelection(RandoopArgumentCollector.getUseNull(config));
    if (fNullRatio != null)
      fNullRatio.setText(RandoopArgumentCollector.getNullRatio(config));
    if (fJUnitTestInputs != null)
      fJUnitTestInputs.setText(RandoopArgumentCollector.getJUnitTestInputs(config));
    if (fTimeLimit != null) {
      fTimeLimit.setText(RandoopArgumentCollector.getTimeLimit(config));
    }
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    RandoopArgumentCollector.restoreRandomSeed(config);
    RandoopArgumentCollector.restoreMaxTestSize(config);
    RandoopArgumentCollector.restoreUseThreads(config);
    RandoopArgumentCollector.restoreThreadTimeout(config);
    RandoopArgumentCollector.restoreUseNull(config);
    RandoopArgumentCollector.restoreNullRatio(config);
    RandoopArgumentCollector.restoreJUnitTestInputs(config);
    RandoopArgumentCollector.restoreTimeLimit(config);
  }

  /**
   * Returns an OK <code>IStatus</code> if the specified arguments could be
   * passed to Randoop without raising any error. If the arguments are not
   * valid, an ERROR status is returned with a message indicating what is wrong.
   * 
   * @param randomSeed
   * @param maxTestSize
   * @param useThreads
   * @param threadTimeout
   * @param useNull
   * @param nullRatio
   * @param junitTestInputs
   * @param timeLimit
   * @return
   */
  protected IStatus validate(String randomSeed, String maxTestSize,
      boolean useThreads, String threadTimeout, boolean useNull,
      String nullRatio, String junitTestInputs, String timeLimit) {
    try {
      Integer.parseInt(randomSeed);
    } catch (NumberFormatException nfe) {
      return StatusFactory.createErrorStatus("Random Seed is not a valid integer");
    }
    
    IStatus status = RandoopLaunchConfigurationUtil.validatePositiveInt(maxTestSize,
        "Maximum Test Size is not a positive integer",
        "Maximum Test Size is not a valid integer");
    if (status.getSeverity() == IStatus.ERROR) {
      return status;
    }
    if (useThreads) {
      status = RandoopLaunchConfigurationUtil.validatePositiveInt(threadTimeout,
          "Thread Timeout is not a positive integer",
          "Thread Timeout is not a valid integer");
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

    status = RandoopLaunchConfigurationUtil.validatePositiveInt(junitTestInputs,
        "JUnit Test Inputs is not a positive integer",
        "JUnit Test Inputs is not a valid integer");
    if (status.getSeverity() == IStatus.ERROR) {
      return status;
    }
    
    status = RandoopLaunchConfigurationUtil.validatePositiveInt(timeLimit,
        "Time Limit is not a positive integer",
        "Time Limit is not a valid integer");
    if (status.getSeverity() == IStatus.ERROR) {
      return status;
    }

    return StatusFactory.createOkStatus();
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
        lConvertedTimeLimit.setText(IConstants.EMPTY_STRING);
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
      lConvertedTimeLimit.setText(IConstants.EMPTY_STRING);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  @Override
  public String getName() {
    return "Parameters";
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
   */
  @Override
  public String getId() {
    return "randoop.plugin.ui.launching.parametersTab"; //$NON-NLS-1$
  }
  
}
