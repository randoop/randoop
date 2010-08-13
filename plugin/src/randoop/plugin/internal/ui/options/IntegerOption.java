package randoop.plugin.internal.ui.options;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.core.StatusFactory;

public abstract class IntegerOption extends TextOption {
  
  private String fInvalidErrorMsg;
  
  public IntegerOption(Text text) {
    super(text);
    fInvalidErrorMsg = MessageFormat.format("{0} is not a valid integer", getName());
  }
  
  @Override
  protected IStatus validate(String text) {
    try {
      Integer.parseInt(text);
      
      return StatusFactory.OK_STATUS;
    } catch (NumberFormatException nfe) {
      return StatusFactory.createErrorStatus(getInvalidIntegerErrorMessage());
    }
  }
  
  @Override
  protected String getValue(ILaunchConfiguration config) {
    String value = super.getValue(config);
    try {
      return new Integer(value).toString();
    } catch (NumberFormatException e) {
      return value;
    }
  }
  
  @Override
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    String value = fText.getText();
    try {
      value = new Integer(value).toString();
    } catch (NumberFormatException e) {
    }

    config.setAttribute(getAttribute(), value);
  }
  
  protected abstract String getName();
  
  protected String getInvalidIntegerErrorMessage() {
    return fInvalidErrorMsg;
  }
  
}
