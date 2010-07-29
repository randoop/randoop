package randoop.plugin.internal.ui.options;

import java.text.DecimalFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.core.TestKinds;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class IOptionFactory {
  
  public static IOption createRandomSeedOption(Text text) {
    return new RandomSeedOption(text);
  }
  
  public static IOption createMaximumTestSizeOption(Text text) {
    return new MaximumTestSizeOption(text);
  }
  
  public static IOption createUseThreads(IOption threadTimeout, Button enablement) {
    if (!(threadTimeout instanceof ThreadTimeoutOption))
      return null;
    
    return new UseThreadsOption((ThreadTimeoutOption) threadTimeout, enablement);
  }
  
  public static IOption createThreadTimeoutOption(Text text) {
    return new ThreadTimeoutOption(text);
  }
  
  public static IOption createUseNull(IOption nullRatio, Button enablement) {
    if (!(nullRatio instanceof NullRatioOption))
      return null;
    
    return new UseNullOption((NullRatioOption) nullRatio, enablement);
  }
  
  public static IOption createNullRatioOption(Text text) {
    return new NullRatioOption(text);
  }
  
  public static IOption createJUnitTestInputsOption(Text text) {
    return new JUnitTestInputsOption(text);
  }
  
  public static IOption createTimeLimitOption(Text text, Label convertedTimeLabel) {
    return new TimeLimitOption(text, convertedTimeLabel);
  }
  
  public static IOption createTestKindsOption(Combo testKinds) {
    return new TestKindsOption(testKinds);
  }
  
  public static IOption createMaximumTestsWrittenOption(Text text) {
    return new MaximumTestsWrittenOption(text);
  }
  
  public static IOption createMaximumTestsPerFileOption(Text text) {
    return new MaximumTestsPerFileOption(text);
  }
  
  private static class RandomSeedOption extends IntegerOption {

    public RandomSeedOption(Text text) {
      super(text);
      setInvalidIntErrorMessage("Random Seed is not a valid integer");
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
      RandoopArgumentCollector.restoreRandomSeed(config);
    }

    @Override
    protected String getValue(ILaunchConfiguration config) {
      return RandoopArgumentCollector.getRandomSeed(config);
    }

    @Override
    protected void setValue(ILaunchConfigurationWorkingCopy config, String value) {
      RandoopArgumentCollector.setRandomSeed(config, value);
    }

    @Override
    public void restoreDefaults() {
      fText.setText(IRandoopLaunchConfigurationConstants.DEFAULT_RANDOM_SEED);
    }
    
  }
  
  private static class MaximumTestSizeOption extends PositiveIntegerOption {

    public MaximumTestSizeOption(Text text) {
      super(text);
      setNonPositiveIntErrorMessage("Maximum Test Size is not a positive integer");
      setInvalidIntErrorMessage("Maximum Test Size is not a valid integer");
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
      RandoopArgumentCollector.restoreMaxTestSize(config);
    }

    @Override
    protected String getValue(ILaunchConfiguration config) {
      return RandoopArgumentCollector.getMaxTestSize(config);
    }

    @Override
    protected void setValue(ILaunchConfigurationWorkingCopy config, String value) {
      RandoopArgumentCollector.setMaxTestSize(config, value);    
    }
    
    @Override
    public void restoreDefaults() {
      fText.setText(IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TEST_SIZE);
    }
    
  }
  
  private static class UseThreadsOption extends EnablementOption {
    
    public UseThreadsOption(ThreadTimeoutOption option, Button enablement) {
      super(option, enablement);
    }

    @Override
    protected void setDefaultEnablement(ILaunchConfigurationWorkingCopy config) {
      RandoopArgumentCollector.restoreUseThreads(config);
    }
    
    @Override
    public void restoreDefaults() {
      fEnablement.setSelection(Boolean.parseBoolean(IRandoopLaunchConfigurationConstants.DEFAULT_USE_THREADS));
    }

    @Override
    protected boolean isEnabled(ILaunchConfiguration config) {
      return RandoopArgumentCollector.getUseThreads(config);
    }

    @Override
    protected void setEnabled(ILaunchConfigurationWorkingCopy config, boolean enabled) {
      RandoopArgumentCollector.setUseThreads(config, enabled);
    }
    
  }
  
  private static class ThreadTimeoutOption extends PositiveIntegerOption implements IEnableableOption {
    
    public ThreadTimeoutOption(Text text) {
      super(text);
      setNonPositiveIntErrorMessage("Thread Timeout is not a positive integer");
      setInvalidIntErrorMessage("Thread Timeout is not a valid integer");
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
      RandoopArgumentCollector.restoreThreadTimeout(config);
    }
    
    @Override
    protected void setValue(ILaunchConfigurationWorkingCopy config, String value) {
      RandoopArgumentCollector.setThreadTimeout(config, value);
    }

    @Override
    protected String getValue(ILaunchConfiguration config) {
      return RandoopArgumentCollector.getThreadTimeout(config);
    }

    @Override
    public void setEnabled(boolean enabled) {
      fText.setEnabled(enabled);
    }
    
    @Override
    public void restoreDefaults() {
      fText.setText(IRandoopLaunchConfigurationConstants.DEFAULT_THREAD_TIMEOUT);
    }
    
  }
  
  private static class UseNullOption extends EnablementOption {
    
    public UseNullOption(NullRatioOption option, Button enablement) {
      super(option, enablement);
    }

    @Override
    protected void setDefaultEnablement(ILaunchConfigurationWorkingCopy config) {
      RandoopArgumentCollector.restoreUseNull(config);
    }
    
    @Override
    public void restoreDefaults() {
      fEnablement.setSelection(Boolean.parseBoolean(IRandoopLaunchConfigurationConstants.DEFAULT_USE_NULL));
    }
    
    @Override
    protected boolean isEnabled(ILaunchConfiguration config) {
      return RandoopArgumentCollector.getUseNull(config);
    }

    @Override
    protected void setEnabled(ILaunchConfigurationWorkingCopy config, boolean enabled) {
      RandoopArgumentCollector.setUseNull(config, enabled);
    }
    
  }
  
  private static class NullRatioOption extends BoundedDoubleOption implements IEnableableOption {
    public NullRatioOption(Text text) {
      super(text, 0.0, 1.0);
      setOutOfBoundsMsg("Null Ratio must be between 0 and 1");
      setInvalidDoubleErrorMsg("Null Ratio is not a valid number");
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
      RandoopArgumentCollector.restoreNullRatio(config);
    }

    @Override
    protected String getValue(ILaunchConfiguration config) {
      return RandoopArgumentCollector.getNullRatio(config);
    }

    @Override
    protected void setValue(ILaunchConfigurationWorkingCopy config, String value) {
      RandoopArgumentCollector.setNullRatio(config, value);
    }

    @Override
    public void setEnabled(boolean enabled) {
      fText.setEnabled(enabled);
    }
    
    @Override
    public void restoreDefaults() {
      fText.setText(IRandoopLaunchConfigurationConstants.DEFAULT_NULL_RATIO);
    }

  }
  
  private static class JUnitTestInputsOption extends PositiveIntegerOption {
    
    public JUnitTestInputsOption(Text text) {
      super(text);
      setNonPositiveIntErrorMessage("JUnit Test Inputs is not a positive integer");
      setInvalidIntErrorMessage("JUnit Test Inputs is not a valid integer");
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
      RandoopArgumentCollector.restoreJUnitTestInputs(config);
    }
    
    @Override
    protected void setValue(ILaunchConfigurationWorkingCopy config, String value) {
      RandoopArgumentCollector.setJUnitTestInputs(config, value);
    }

    @Override
    protected String getValue(ILaunchConfiguration config) {
      return RandoopArgumentCollector.getJUnitTestInputs(config);
    }
    
    @Override
    public void restoreDefaults() {
      fText.setText(IRandoopLaunchConfigurationConstants.DEFAULT_JUNIT_TEST_INPUTS);
    }
  };
  
  private static class TimeLimitOption extends PositiveIntegerOption {
    Label fConvertedTimeLimit;
    
    public TimeLimitOption(Text text, Label convertedTime) {
      super(text);
      fConvertedTimeLimit = convertedTime;
      
      fText.addModifyListener(new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent e) {
          setConvertedTime();
        }
      });
      
      setNonPositiveIntErrorMessage("Time Limit is not a positive integer");
      setInvalidIntErrorMessage("Time Limit is not a valid integer");
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
      RandoopArgumentCollector.restoreTimeLimit(config);
    }
    
    @Override
    protected void setValue(ILaunchConfigurationWorkingCopy config, String value) {
      RandoopArgumentCollector.setTimeLimit(config, value);
    }

    @Override
    protected String getValue(ILaunchConfiguration config) {
      return RandoopArgumentCollector.getTimeLimit(config);
    }
    
    @Override
    public void restoreDefaults() {
      fText.setText(IRandoopLaunchConfigurationConstants.DEFAULT_TIME_LIMIT);
    }

    private void setConvertedTime() {
      final String MINUTES = "minutes";
      final String HOURS = "hours";
      final String DAYS = "days";
      final String YEARS = "years";
    
      try {
        int seconds = Integer.parseInt(fText.getText());
    
        DecimalFormat time = new DecimalFormat("#0.0"); //$NON-NLS-1$
        StringBuilder timeStr = new StringBuilder();
        if (seconds < 60) {
          fConvertedTimeLimit.setText(IConstants.EMPTY_STRING);
        } else if (seconds < 3600) {
          timeStr.append('(');
          timeStr.append(time.format(seconds / 60.0));
          timeStr.append(' ');
          timeStr.append(MINUTES);
          timeStr.append(')');
          fConvertedTimeLimit.setText(timeStr.toString());
        } else if (seconds < 86400) {
          timeStr.append('(');
          timeStr.append(time.format(seconds / 3600.0));
          timeStr.append(' ');
          timeStr.append(HOURS);
          timeStr.append(')');
          fConvertedTimeLimit.setText(timeStr.toString());
        } else if (seconds < 31556926) {
          timeStr.append('(');
          timeStr.append(time.format(seconds / 86400.0));
          timeStr.append(' ');
          timeStr.append(DAYS);
          timeStr.append(')');
          fConvertedTimeLimit.setText(timeStr.toString());
        } else {
          timeStr.append('(');
          timeStr.append(time.format(seconds / 31556926.0));
          timeStr.append(' ');
          timeStr.append(YEARS);
          timeStr.append(')');
          fConvertedTimeLimit.setText(timeStr.toString());
        }
      } catch (NumberFormatException e) {
        fConvertedTimeLimit.setText(IConstants.EMPTY_STRING);
      }
    }
  };
  
  private static class TestKindsOption extends ComboOption {

    public TestKindsOption(Combo testKinds) {
      super(testKinds);
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
      RandoopArgumentCollector.restoreTestKinds(config);
    }

    @Override
    protected IStatus validate(String testKinds) {
      boolean validKind = false;
      for (TestKinds kindCandidate : TestKinds.values()) {
        validKind |= kindCandidate.getArgumentName().equals(testKinds);
      }
      if (validKind) {
        return StatusFactory.OK_STATUS;
      } else {
        return StatusFactory
            .createErrorStatus("Test Kinds must be of type All, Pass, or Fail.");
      }
    }

    @Override
    protected String getValue(ILaunchConfiguration config) {
      return RandoopArgumentCollector.getTestKinds(config);
    }

    @Override
    protected void setValue(ILaunchConfigurationWorkingCopy config, String value) {
      RandoopArgumentCollector.setTestKinds(config, value);
    }

    @Override
    protected String getValue() {
      return TestKinds.getTestKind(fCombo.getSelectionIndex()).getArgumentName();
    }

    @Override
    public void restoreDefaults() {
      int commandId = Integer.parseInt(IRandoopLaunchConfigurationConstants.DEFAULT_RANDOM_SEED);
      fCombo.select(commandId);
    }
    
  }
  
  private static class MaximumTestsWrittenOption extends PositiveIntegerOption {
    
    public MaximumTestsWrittenOption(Text text) {
      super(text);
      setNonPositiveIntErrorMessage("Maximum Tests Written is not a positive integer");
      setInvalidIntErrorMessage("Maximum Tests Written is not a valid integer");
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
      RandoopArgumentCollector.restoreMaxTestsWritten(config);
    }
    
    @Override
    protected void setValue(ILaunchConfigurationWorkingCopy config, String value) {
      RandoopArgumentCollector.setMaxTestsWritten(config, value);
    }

    @Override
    protected String getValue(ILaunchConfiguration config) {
      return RandoopArgumentCollector.getMaxTestsWritten(config);
    }
    
    @Override
    public void restoreDefaults() {
      fText.setText(IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TESTS_WRITTEN);
    }
  };
  
  private static class MaximumTestsPerFileOption extends PositiveIntegerOption {
    
    public MaximumTestsPerFileOption(Text text) {
      super(text);
      setNonPositiveIntErrorMessage("Maximum Tests Per File is not a positive integer");
      setInvalidIntErrorMessage("Maximum Tests Per File is not a valid integer");
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
      RandoopArgumentCollector.restoreMaxTestsPerFile(config);
    }
    
    @Override
    protected void setValue(ILaunchConfigurationWorkingCopy config, String value) {
      RandoopArgumentCollector.setMaxTestsPerFile(config, value);
    }

    @Override
    protected String getValue(ILaunchConfiguration config) {
      return RandoopArgumentCollector.getMaxTestsPerFile(config);
    }
    
    @Override
    public void restoreDefaults() {
      fText.setText(IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TESTS_PER_FILE);
    }
  }

}
