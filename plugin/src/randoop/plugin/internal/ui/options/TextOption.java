package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

import randoop.plugin.internal.core.RandoopStatus;

/**
 * Option for a single SWT Text widget.
 * 
 * @author Peter Kalauskas
 */
public abstract class TextOption extends Option implements IEnableableOption {

  protected Text fText;

  /**
   * Creates a placeholder instance of this text option that may be used to set
   * defaults. Other operations will have no effect on the object or launch
   * configuration it is passed.
   */
  public TextOption() {
  }
  
  public TextOption(Text text) {
    fText = text;
    fText.addModifyListener(new ModifyListener() {
      
      public void modifyText(ModifyEvent e) {
        notifyListeners(new OptionChangeEvent(getAttributeName(), fText.getText()));
      }
    });
  }
  
  public IStatus canSave() {
    if (fText != null && !fText.isDisposed()) {
      String text = fText.getText();
      if (text.length() != 0) {
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
      config.setAttribute(getAttributeName(), fText.getText());
    }
  }

  protected String getValue(ILaunchConfiguration config) {
    try {
      return config.getAttribute(getAttributeName(), getDefaultValue());
    } catch (CoreException e) {
      return getDefaultValue();
    }
  }

  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    config.setAttribute(getAttributeName(), getDefaultValue());
  }

  public void restoreDefaults() {
    if (fText != null && !fText.isDisposed()) {
      fText.setText(getDefaultValue());
    }
  }
  
  public void setEnabled(boolean enabled) {
    fText.setEnabled(enabled);
  }

  /**
   * Returns the attribute used to get and set this text option to a launch
   * configuration
   * 
   * @return
   */
  protected abstract String getAttributeName();

  /**
   * Returns the default value of this text. This is used set and restore
   * defaults.
   * 
   * @return the default value of this text option
   */
  protected abstract String getDefaultValue();
  
}
