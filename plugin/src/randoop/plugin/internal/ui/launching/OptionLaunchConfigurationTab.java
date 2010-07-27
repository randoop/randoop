package randoop.plugin.internal.ui.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import randoop.plugin.internal.IConstants;
import randoop.plugin.internal.ui.options.IOption;

public abstract class OptionLaunchConfigurationTab extends AbstractLaunchConfigurationTab {

  private List<IOption> fOptions;
  
  private ModifyListener fBasicModifyListener = new ModifyListener() {
    
    @Override
    public void modifyText(ModifyEvent e) {
      setErrorMessage(null);
      updateLaunchConfigurationDialog();
    }
  };
  
  private SelectionListener fBasicSelectionListener = new SelectionListener() {

    @Override
    public void widgetSelected(SelectionEvent e) {
      setErrorMessage(null);
      updateLaunchConfigurationDialog();
    }
    
    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
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
  protected void addOption(IOption option) {
    fOptions.add(option);
  }

  @Override
  public boolean canSave() {
    setErrorMessage(null);

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
            isMessageSet = true;
          }
        }
      }
    }
    
    return true;
  }

  @Override
  public boolean isValid(ILaunchConfiguration config) {
    setErrorMessage(null);

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
    if (msg != null && !msg.equals(IConstants.EMPTY_STRING)) {
      setMessage(message);
      return true;
    }
    
    return false;
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    for (IOption option : fOptions) {
      option.performApply(config);
    }
  }

  @Override
  public void initializeFrom(ILaunchConfiguration config) {
    for (IOption option : fOptions) {
      option.initializeFrom(config);
    }
  }

  @Override
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
  
  protected ModifyListener getBasicModifyListener() {
    return fBasicModifyListener;
  }

  protected SelectionListener getBasicSelectionListener() {
    return fBasicSelectionListener;
  }

}
