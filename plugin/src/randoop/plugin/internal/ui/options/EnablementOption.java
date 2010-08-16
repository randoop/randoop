package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;

import randoop.plugin.internal.core.RandoopStatus;

public abstract class EnablementOption extends Option {
  IEnableableOption fEnabledOption;
  Button fEnablementButton;
  
  public EnablementOption(IEnableableOption enabledOption) {
    fEnabledOption = enabledOption;
  }
  
  public EnablementOption(IEnableableOption enabledOption, Button enablement) {
    fEnablementButton = enablement;
    enablement.addSelectionListener(new SelectionListener() {
      
      public void widgetSelected(SelectionEvent e) {
        notifyListeners(new OptionChangeEvent(getAttribute(), fEnablementButton.getSelection()));
      }
      
      public void widgetDefaultSelected(SelectionEvent e) {
      }
    });
    fEnabledOption = enabledOption;
    
    Assert.isTrue(SWT.CHECK == (fEnablementButton.getStyle() & SWT.CHECK), "EnablementOption can only use check buttons");
    
    fEnablementButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fEnabledOption.setEnabled(isEnabled());
      }
    });
  }

  protected boolean isEnabled() {
    if (fEnablementButton != null) {
      return fEnablementButton.getSelection();
    }
    
    return true;
  }

  public IStatus canSave() {
    if (fEnablementButton != null) {
      if (isEnabled()) {
        return fEnabledOption.canSave();
      }
    }
    return RandoopStatus.OK_STATUS;
  }

  public IStatus isValid(ILaunchConfiguration config) {
    if (fEnablementButton != null) {
      if (isEnabled()) {
        return fEnabledOption.isValid(config);
      }
    }
    return RandoopStatus.OK_STATUS;
  }

  @Override
  public void initializeWithoutListenersFrom(ILaunchConfiguration config) {
    if (fEnablementButton != null && fEnabledOption != null) {
      boolean enabled;
      try {
        enabled = config.getAttribute(getAttribute(), getDefaultValue());
      } catch (CoreException e) {
        enabled = getDefaultValue();
      }
      
      fEnablementButton.setSelection(enabled);
      fEnabledOption.initializeFrom(config);
      fEnabledOption.setEnabled(enabled);
    }
  }

  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fEnablementButton != null) {
      boolean enabled = fEnablementButton.getSelection();
      config.setAttribute(getAttribute(), enabled);
    }

    fEnabledOption.performApply(config);
  }

  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    config.setAttribute(getAttribute(), getDefaultValue());
    fEnabledOption.setDefaults(config);
  }
  
  public void restoreDefaults() {
    boolean enabled = getDefaultValue();
    fEnablementButton.setSelection(enabled);
    fEnabledOption.setEnabled(enabled);
  }
  
  protected abstract String getAttribute();

  protected abstract boolean getDefaultValue();
  
}
