package randoop.plugin.internal.ui.launching;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;

import randoop.plugin.internal.ui.options.IOption;

public abstract class OptionLaunchConfigurationTab extends AbstractLaunchConfigurationTab {

  private Set<IOption> fProjectOption;
  
  public OptionLaunchConfigurationTab() {
    fProjectOption = new HashSet<IOption>();
  }

  /**
   * Adds the specified option to this tab if it is not already present.
   * 
   * @param option
   *          option to be added to this tab
   * @return <code>true</code> if this tab did not already contain the specified
   *         option
   */
  protected boolean addOption(IOption option) {
    return fProjectOption.add(option);
  }

  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
   */
  @Override
  public boolean canSave() {
    setErrorMessage(null);

    boolean isMessageSet = false;
    
    for (IOption option : fProjectOption) {
      IStatus status = option.canSave();

      String message = status.getMessage();
      if (!status.isOK()) {
        setErrorMessage(message);
        return false;
      } else {
        if (message != null && !message.isEmpty()) {
          if (!isMessageSet) {
            isMessageSet = setReadableMessage(message);
            isMessageSet = true;
          }
        }
      }
    }
    
    return true;
  }

  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
   */
  @Override
  public boolean isValid(ILaunchConfiguration config) {
    setErrorMessage(null);

    boolean isMessageSet = false;
    
    for (IOption option : fProjectOption) {
      IStatus status = option.isValid(config);

      String message = status.getMessage();
      if (!status.isOK()) {
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
    if (msg != null && !msg.equals("")) {
      setMessage(message);
      return true;
    }
    
    return false;
  }

  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
   */
  @Override
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    for (IOption option : fProjectOption) {
      option.performApply(config);
    }
  }

  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
   */
  @Override
  public void initializeFrom(ILaunchConfiguration config) {
    for (IOption option : fProjectOption) {
      option.initializeFrom(config);
    }
  }

  /*
   * Implements a method in ILaunchConfigurationTab
   * 
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
   */
  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    for (IOption option : fProjectOption) {
      option.setDefaults(config);
    }
  }

}
