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

  public TextOption() {
  }
  
  public TextOption(Text text) {
    fText = text;
    fText.addModifyListener(new ModifyListener() {
      
      public void modifyText(ModifyEvent e) {
        notifyListeners(new OptionChangeEvent(getAttribute(), fText.getText()));
      }
    });
  }
  
  public IStatus canSave() {
    if (fText != null && !fText.isDisposed()) {
      String text = fText.getText();
      if (!text.isEmpty()) {
        return validate(text);
      }
    }
    
    return RandoopStatus.OK_STATUS;
  }
  
  public IStatus isValid(ILaunchConfiguration config) {
    return validate(getValue(config));
  }

  protected abstract IStatus validate(String text);

  @Override
  public void initializeWithoutListenersFrom(ILaunchConfiguration config) {
    if (fText != null && !fText.isDisposed()) {
      fText.setText(getValue(config));
    }
  }

  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fText != null && !fText.isDisposed()) {
      config.setAttribute(getAttribute(), fText.getText());
    }
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
    if (fText != null && !fText.isDisposed()) {
      fText.setText(getDefaultValue());
    }
  }
  
  protected abstract String getAttribute();
  
  protected abstract String getDefaultValue();
  
}
