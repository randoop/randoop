package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.core.StatusFactory;

public abstract class BoundedDoubleOption extends TextOption {
  private double fLowerBound;
  private double fUpperBound;
  private String fInvalidErrorMsg;
  private String fOutOfBoundsMsg;

  public BoundedDoubleOption(Text text, double lowerBound, double upperBound) {
    super(text);
    fLowerBound = lowerBound;
    fUpperBound = upperBound;
  }

  protected void setInvalidDoubleErrorMsg(String invalidDoubleErrorMsg) {
    fInvalidErrorMsg = invalidDoubleErrorMsg;
  }

  protected void setOutOfBoundsMsg(String outOfBoundsMsg) {
    fOutOfBoundsMsg = outOfBoundsMsg;
  }

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
}
