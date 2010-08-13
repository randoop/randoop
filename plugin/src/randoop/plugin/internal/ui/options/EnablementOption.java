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
  Button fEnablement;
  
  public EnablementOption(IEnableableOption enabledOption, Button enablement) {
    fEnablement = enablement;
    enablement.addSelectionListener(new SelectionListener() {
      
      public void widgetSelected(SelectionEvent e) {
        notifyListeners(new OptionChangeEvent(getAttribute(), fEnablement.getSelection()));
      }
      
      public void widgetDefaultSelected(SelectionEvent e) {
      }
    });
    fEnabledOption = enabledOption;
    
    Assert.isTrue(SWT.CHECK == (fEnablement.getStyle() & SWT.CHECK), "EnablementOption can only use check buttons");
    
    fEnablement.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fEnabledOption.setEnabled(isEnabled());
      }
    });
  }
  
  protected boolean isEnabled() {
    return fEnablement.getSelection();
  }

  public IStatus canSave() {
    if(fEnablement == null || fEnabledOption == null) {
      return RandoopStatus.createErrorStatus(EnablementOption.class.getName()
          + " incorrectly initialized"); //$NON-NLS-1$
    }
    
    if (isEnabled()) {
      return fEnabledOption.canSave();
    } else {
      return RandoopStatus.OK_STATUS;
    }
  }

  public IStatus isValid(ILaunchConfiguration config) {
    if(fEnablement == null || fEnabledOption == null) {
      return RandoopStatus.createErrorStatus(EnablementOption.class.getName()
          + " incorrectly initialized"); //$NON-NLS-1$
    }
    
    if (isEnabled()) {
      return fEnabledOption.isValid(config);
    } else {
      return RandoopStatus.OK_STATUS;
    }
  }

  public void initializeFrom(ILaunchConfiguration config) {
    setDisableListeners(true);
    
    if (fEnablement != null && fEnabledOption != null) {
      boolean enabled;
      try {
        enabled = config.getAttribute(getAttribute(), getDefaultValue());
      } catch (CoreException e) {
        enabled = getDefaultValue();
      }
      
      fEnablement.setSelection(enabled);
      fEnabledOption.initializeFrom(config);
      fEnabledOption.setEnabled(enabled);
    }
    
    setDisableListeners(false);
  }

  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fEnablement != null && fEnabledOption != null) {
      boolean enabled = fEnablement.getSelection();

      config.setAttribute(getAttribute(), enabled);
      fEnabledOption.performApply(config);
    }
  }

  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    config.setAttribute(getAttribute(), getDefaultValue());
    fEnabledOption.setDefaults(config);
  }
  
  public void restoreDefaults() {
    boolean enabled = getDefaultValue();
    fEnablement.setSelection(enabled);
    fEnabledOption.setEnabled(enabled);
  }
  
  protected abstract String getAttribute();

  protected abstract boolean getDefaultValue();
  
}
