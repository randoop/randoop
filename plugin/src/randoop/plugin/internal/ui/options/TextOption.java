package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.core.RandoopStatus;

public abstract class TextOption extends Option {
  protected Text fText;
  
  public TextOption(Text text) {
    fText = text;
    fText.addModifyListener(new ModifyListener() {
      
      public void modifyText(ModifyEvent e) {
        notifyListeners(new OptionChangeEvent(getAttribute(), fText.getText()));
      }
    });
  }
  
  public IStatus canSave() {
    if (fText == null) {
      return RandoopStatus.createErrorStatus(PositiveIntegerOption.class.getName()
          + " incorrectly initialized"); //$NON-NLS-1$
    }
    
    String text = fText.getText();
    if (text.isEmpty()) {
      return RandoopStatus.OK_STATUS;
    } else {
      return validate(text);
    }
  }
  
  public IStatus isValid(ILaunchConfiguration config) {
    return validate(getValue(config));
  }

  protected abstract IStatus validate(String text);

  public void initializeFrom(ILaunchConfiguration config) {
    setDisableListeners(true);
    fText.setText(getValue(config));
    setDisableListeners(false);
  }

  public void performApply(ILaunchConfigurationWorkingCopy config) {
    config.setAttribute(getAttribute(), fText.getText());
  }

  protected String getValue(ILaunchConfiguration config) {
    try {
      return config.getAttribute(getAttribute(), getDefaultValue());
    } catch (CoreException e) {
      return getDefaultValue();
    }
  }
  
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    config.setAttribute(getAttribute(), getDefaultValue());
  }
  
  public void restoreDefaults() {
    fText.setText(getDefaultValue());
  }
  
  protected abstract String getAttribute();
  
  protected abstract String getDefaultValue();
  
}
