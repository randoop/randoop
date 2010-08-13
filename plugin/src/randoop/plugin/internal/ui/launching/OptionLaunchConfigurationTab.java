package randoop.plugin.internal.ui.launching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;

import randoop.plugin.internal.ui.options.IOption;
import randoop.plugin.internal.ui.options.IOptionChangeEvent;
import randoop.plugin.internal.ui.options.IOptionChangeListener;

public abstract class OptionLaunchConfigurationTab extends AbstractLaunchConfigurationTab {

  private List<IOption> fOptions;
  
  private IOptionChangeListener fBasicOptionChangeListener = new IOptionChangeListener() {

    public void attributeChanged(IOptionChangeEvent event) {
      setErrorMessage(null);
      updateLaunchConfigurationDialog();
    }
  };
  
  public OptionLaunchConfigurationTab() {
    fOptions = new ArrayList<IOption>();
  }

  /**
   * Adds the specified option to this tab if it is not already present.
   * 
   * @param option
   *          option to be added to this tab
   */
  protected boolean addOption(IOption option) {
    return fOptions.add(option);
  }
  
  protected void addOptions(Collection<IOption> options) {
    fOptions.addAll(options);
  }
  
  protected boolean removeOption(IOption option) {
    return fOptions.remove(option);
  }
  
  protected void removeAllOptions() {
    fOptions = new ArrayList<IOption>();
  }

  @Override
  public boolean canSave() {
    setErrorMessage(null);
    setMessage(null);
    
    boolean isMessageSet = false;
    
    for (IOption option : fOptions) {
      IStatus status = option.canSave();

      String message = status.getMessage();
      if (status.getSeverity() == IStatus.ERROR) {
        setErrorMessage(message);
        return false;
      } else {
        if (message != null && !message.isEmpty()) {
          if (!isMessageSet) {
            isMessageSet = setReadableMessage(message);
          }
        }
      }
    }
    
    return true;
  }

  @Override
  public boolean isValid(ILaunchConfiguration config) {
    setErrorMessage(null);
    setMessage(null);
    
    boolean isMessageSet = false;
    
    for (IOption option : fOptions) {
      IStatus status = option.isValid(config);

      String message = status.getMessage();
      if (status.getSeverity() == IStatus.ERROR) {
        setErrorMessage(message);
        return false;
      } else {
        if (!isMessageSet) {
          isMessageSet = setReadableMessage(message);
        }
      }
    }
    
    return true;
  }
  
  /**
   * Sets the message so long as it is non-<code>null</code> and non-empty.
   * 
   * @param message
   *          <code>String</code> to pass to <code>setMessage</code> if it is
   *          valid
   * @return <code>true</code> if the message is set
   */
  protected boolean setReadableMessage(String message) {
    String msg = message;
    if (msg != null && !msg.isEmpty()) {
      setMessage(message);
      return true;
    }
    
    return false;
  }

  public void performApply(ILaunchConfigurationWorkingCopy config) {
   for (IOption option : fOptions) {
      option.performApply(config);
    }
  }

  public void initializeFrom(ILaunchConfiguration config) {
    for (IOption option : fOptions) {
      option.initializeFrom(config);
    }
  }

  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    for (IOption option : fOptions) {
      option.setDefaults(config);
    }
  }
  
  protected void restoreDefaults() {
    for (IOption option : fOptions) {
      option.restoreDefaults();
    }
  }
  
  protected IOptionChangeListener getBasicOptionChangeListener() {
    return fBasicOptionChangeListener;
  }

}
