package randoop.plugin.internal.ui.options;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.core.StatusFactory;

public abstract class BoundedDoubleOption extends TextOption {
  private double fLowerBound;
  private double fUpperBound;
  private String fOutOfBoundsMsg;
  private String fInvalidErrorMsg;

  public BoundedDoubleOption(Text text, double lowerBound, double upperBound) {
    super(text);
    fLowerBound = lowerBound;
    fUpperBound = upperBound;
    
    fOutOfBoundsMsg = MessageFormat.format("{0} must be between {1} and {2}", getName(), fLowerBound, fUpperBound);
    fInvalidErrorMsg = MessageFormat.format("{0} is not a valid number", getName());
  }

  @Override
  protected String getValue(ILaunchConfiguration config) {
    String value = super.getValue(config);
    try {
      return new Double(value).toString();
    } catch (NumberFormatException e) {
      return value;
    }
  }
  
  @Override
  protected IStatus validate(String text) {
    try {
      double d = Double.parseDouble(text);

      if (d < fLowerBound || d > fUpperBound) {
        return StatusFactory.createErrorStatus(fOutOfBoundsMsg);
      }

      return StatusFactory.OK_STATUS;
    } catch (NumberFormatException nfe) {
      return StatusFactory.createErrorStatus(fInvalidErrorMsg);
    }
  }
  
  @Override
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    String value = fText.getText();
    try {
      value = new Double(value).toString();
    } catch (NumberFormatException e) {
    }

    config.setAttribute(getAttribute(), value);
  }
  
  protected abstract String getName();
  
}
