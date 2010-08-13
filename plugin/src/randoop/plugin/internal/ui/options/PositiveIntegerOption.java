package randoop.plugin.internal.ui.options;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.core.RandoopStatus;

public abstract class PositiveIntegerOption extends IntegerOption {
  
  private String fNonPosErrorMsg;
  
  public PositiveIntegerOption(Text text) {
    super(text);
    fNonPosErrorMsg = MessageFormat.format("{0} is not a positive integer", getName());
  }
  
  @Override
  protected IStatus validate(String text) {
    try {
      if (Integer.parseInt(text) < 1) {
        return RandoopStatus.createErrorStatus(getNonpositiveIntegerErrorMessage());
      }
      
      return RandoopStatus.OK_STATUS;
    } catch (NumberFormatException nfe) {
      return RandoopStatus.createErrorStatus(getInvalidIntegerErrorMessage());
    }
  }
  
  
  protected String getNonpositiveIntegerErrorMessage() {
    return fNonPosErrorMsg;
  }
  
}
