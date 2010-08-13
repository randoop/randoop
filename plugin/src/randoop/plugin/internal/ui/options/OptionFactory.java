package randoop.plugin.internal.ui.options;

import java.text.DecimalFormat;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.core.RandoopStatus;
import randoop.plugin.internal.core.TestKinds;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;

public class OptionFactory {
  
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

  public static IOption createInputsLimitOption(Text text) {
    return new InputLimitOption(text);
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
    }

    @Override
    protected String getName() {
      return "Random Seed";
    }

    @Override
    protected String getAttribute() {
      return IRandoopLaunchConfigurationConstants.ATTR_RANDOM_SEED;
    }

    @Override
    protected String getDefaultValue() {
      return IRandoopLaunchConfigurationConstants.DEFAULT_RANDOM_SEED;
    }
    
  }
  
  private static class MaximumTestSizeOption extends PositiveIntegerOption {

    public MaximumTestSizeOption(Text text) {
      super(text);
    }

    @Override
    protected String getName() {
      return "Maximum Test Size";
    }

    @Override
    protected String getAttribute() {
      return IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TEST_SIZE;
    }

    @Override
    protected String getDefaultValue() {
      return IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TEST_SIZE;
    }
    
  }
  
  private static class UseThreadsOption extends EnablementOption {
    
    public UseThreadsOption(ThreadTimeoutOption option, Button enablement) {
      super(option, enablement);
    }

    @Override
    protected String getAttribute() {
      return IRandoopLaunchConfigurationConstants.ATTR_USE_THREADS;
    }

    @Override
    protected boolean getDefaultValue() {
      return Boolean.parseBoolean(IRandoopLaunchConfigurationConstants.DEFAULT_USE_THREADS);
    }

  }

  private static class ThreadTimeoutOption extends PositiveIntegerOption implements IEnableableOption {
    
    public ThreadTimeoutOption(Text text) {
      super(text);
    }

    @Override
    protected String getName() {
      return "Thread Timeout";
    }

    @Override
    protected String getAttribute() {
      return IRandoopLaunchConfigurationConstants.ATTR_THREAD_TIMEOUT;
    }

    @Override
    protected String getDefaultValue() {
      return IRandoopLaunchConfigurationConstants.DEFAULT_THREAD_TIMEOUT;
    }

    public void setEnabled(boolean enabled) {
      fText.setEnabled(enabled);
    }
    
  }
  
  private static class UseNullOption extends EnablementOption {
    
    public UseNullOption(NullRatioOption option, Button enablement) {
      super(option, enablement);
    }

    @Override
    protected String getAttribute() {
      return IRandoopLaunchConfigurationConstants.ATTR_USE_NULL;
    }

    @Override
    protected boolean getDefaultValue() {
      return Boolean.parseBoolean(IRandoopLaunchConfigurationConstants.DEFAULT_USE_NULL);
    }
    
  }
  
  private static class NullRatioOption extends BoundedDoubleOption implements IEnableableOption {
    public NullRatioOption(Text text) {
      super(text, 0.0, 1.0);
    }

    @Override
    protected String getName() {
      return "Null Ratio";
    }

    @Override
    protected String getAttribute() {
      return IRandoopLaunchConfigurationConstants.ATTR_NULL_RATIO;
    }

    @Override
    protected String getDefaultValue() {
      return IRandoopLaunchConfigurationConstants.DEFAULT_NULL_RATIO;
    }

    public void setEnabled(boolean enabled) {
      fText.setEnabled(enabled);
    }
    
  }
  
  private static class InputLimitOption extends PositiveIntegerOption {
    
    public InputLimitOption(Text text) {
      super(text);
    }

    @Override
    protected String getName() {
      return "Input Limit";
    }
    
    @Override
    public String getAttribute() {
      return IRandoopLaunchConfigurationConstants.ATTR_INPUT_LIMIT;
    }

    @Override
    protected String getDefaultValue() {
      return IRandoopLaunchConfigurationConstants.DEFAULT_INPUT_LIMIT;
    }
    
  }
  
  private static class TimeLimitOption extends PositiveIntegerOption {
    Label fConvertedTimeLimit;
    
    public TimeLimitOption(Text text, Label convertedTime) {
      super(text);
      fConvertedTimeLimit = convertedTime;
      
      fText.addModifyListener(new ModifyListener() {
        
        public void modifyText(ModifyEvent e) {
          setConvertedTime();
        }
      });
      
    }
    
    @Override
    protected String getName() {
      return "Time Limit";
    }

    @Override
    protected String getAttribute() {
      return IRandoopLaunchConfigurationConstants.ATTR_TIME_LIMIT;
    }

    @Override
    protected String getDefaultValue() {
      return IRandoopLaunchConfigurationConstants.DEFAULT_TIME_LIMIT;
    }
    
    private void setConvertedTime() {
    
      try {
        int seconds = Integer.parseInt(fText.getText());

        DecimalFormat twoPlacesFormat = new DecimalFormat("#0.0"); //$NON-NLS-1$
        String timeStr;
        if (seconds < 60) {
          timeStr = ""; //$NON-NLS-1$
        } else if (seconds < 3600) {
          timeStr = MessageFormat.format("({0} minutes)",
              twoPlacesFormat.format(seconds / 60.0));
        } else if (seconds < 86400) {
          timeStr = MessageFormat.format("({0} hours)",
              twoPlacesFormat.format(seconds / 3600.0));
        } else if (seconds < 31556926) {
          timeStr = MessageFormat.format("({0} days)",
              twoPlacesFormat.format(seconds / 86400.0));
        } else {
          timeStr = MessageFormat.format("({0} years)",
              twoPlacesFormat.format(seconds / 31556926.0));
        }
        fConvertedTimeLimit.setText(timeStr);
      } catch (NumberFormatException e) {
        fConvertedTimeLimit.setText(""); //$NON-NLS-1$
      }
    }

  };
  
  private static class TestKindsOption extends ComboOption {

    public TestKindsOption(Combo testKinds) {
      super(testKinds);
    }

    @Override
    protected IStatus validate(String testKindArgument) {
      try {
        TestKinds.valueOf(testKindArgument);
        return RandoopStatus.OK_STATUS;
      } catch (IllegalArgumentException e) {
        return RandoopStatus
            .createErrorStatus("Test Kinds must be of type All, Pass, or Fail.");
      }
    }

    @Override
    protected String getValue() {
      return TestKinds.getTestKind(fCombo.getSelectionIndex()).getArgumentName();
    }

    @Override
    protected String getAttributeName() {
      return IRandoopLaunchConfigurationConstants.ATTR_TEST_KINDS;
    }
    
    @Override
    protected String getDefaltValue() {
      return IRandoopLaunchConfigurationConstants.DEFAULT_TEST_KINDS;
    }
    
    @Override
    protected int getDefaultIndex() {
      return TestKinds.valueOf(getDefaltValue()).ordinal();
    }

  }
  
  private static class MaximumTestsWrittenOption extends PositiveIntegerOption {

    public MaximumTestsWrittenOption(Text text) {
      super(text);
    }

    @Override
    protected String getName() {
      return "Maximum Tests Written";
    }

    @Override
    public String getAttribute() {
      return IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_WRITTEN;
    }

    @Override
    protected String getDefaultValue() {
      return IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TESTS_WRITTEN;
    }
    
  }
  
  private static class MaximumTestsPerFileOption extends PositiveIntegerOption {
    public MaximumTestsPerFileOption(Text text) {
      super(text);
    }

    @Override
    protected String getName() {
      return "Maximum Tests Per File";
    }

    @Override
    public String getAttribute() {
      return IRandoopLaunchConfigurationConstants.ATTR_MAXIMUM_TESTS_PER_FILE;
    }

    @Override
    protected String getDefaultValue() {
      return IRandoopLaunchConfigurationConstants.DEFAULT_MAXIMUM_TESTS_PER_FILE;
    }
    
  }

}
