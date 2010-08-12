package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.core.StatusFactory;

public abstract class TextOption extends Option {
  protected Text fText;
  
  public TextOption(Text text) {
    fText = text;
  }
  
  public IStatus canSave() {
    if (fText == null) {
      return StatusFactory.createErrorStatus(PositiveIntegerOption.class.getName()
          + " incorrectly initialized"); //$NON-NLS-1$
    }
    
    String text = fText.getText();
    if (text.isEmpty()) {
      return StatusFactory.OK_STATUS;
    } else {
      return validate(text);
    }
  }

  public IStatus isValid(ILaunchConfiguration config) {
    return validate(getValue(config));
  }
  
  protected abstract IStatus validate(String text);

  public void initializeFrom(ILaunchConfiguration config) {
    if (fText != null)
      fText.setText(getValue(config));
  }

  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fText != null)
      setValue(config, fText.getText());
  }
  
  protected abstract String getValue(ILaunchConfiguration config);
  
  protected abstract void setValue(ILaunchConfigurationWorkingCopy config, String value);
  
}
