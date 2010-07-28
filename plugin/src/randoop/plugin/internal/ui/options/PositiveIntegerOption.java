package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.core.StatusFactory;

public abstract class PositiveIntegerOption extends IntegerOption {
  private String fNonPosErrorMsg;
  
  public PositiveIntegerOption(Text text) {
    super(text);
  }
  
  protected void setNonPositiveIntErrorMessage(String nonPositiveIntErrorMsg) {
    fNonPosErrorMsg = nonPositiveIntErrorMsg;
  }
  
  @Override
  protected IStatus validate(String text) {
    try {
      if (Integer.parseInt(text) < 1) {
        return StatusFactory.createErrorStatus(fNonPosErrorMsg);
      }
      
      return StatusFactory.OK_STATUS;
    } catch (NumberFormatException nfe) {
      return StatusFactory.createErrorStatus(fInvalidErrorMsg);
    }
  }
}
