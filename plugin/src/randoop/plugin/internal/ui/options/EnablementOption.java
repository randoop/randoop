package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;

import randoop.plugin.internal.core.StatusFactory;

public abstract class EnablementOption extends Option {
  IEnableableOption fEnabledOption;
  Button fEnablement;
  
  public EnablementOption(IEnableableOption enabledOption, Button enablement) {
    fEnablement = enablement;
    fEnabledOption = enabledOption;
    
    Assert.isTrue(SWT.CHECK == (fEnablement.getStyle() & SWT.CHECK));
    
    fEnablement.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        fEnabledOption.setEnabled(isEnabled());
      }
    });
  }
  
  protected boolean isEnabled() {
    Assert.isNotNull(fEnablement);
    return fEnablement.getSelection();
  }

  @Override
  public IStatus canSave() {
    if(fEnablement == null || fEnabledOption == null) {
      return StatusFactory.createErrorStatus(EnablementOption.class.getName()
          + " incorrectly initialized"); //$NON-NLS-1$
    }
    
    if (isEnabled()) {
      return fEnabledOption.canSave();
    } else {
      return StatusFactory.createOkStatus();
    }
  }

  @Override
  public IStatus isValid(ILaunchConfiguration config) {
    if(fEnablement == null || fEnabledOption == null) {
      return StatusFactory.createErrorStatus(EnablementOption.class.getName()
          + " incorrectly initialized"); //$NON-NLS-1$
    }
    
    if (isEnabled()) {
      return fEnabledOption.isValid(config);
    } else {
      return StatusFactory.createOkStatus();
    }
  }

  @Override
  public void initializeFrom(ILaunchConfiguration config) {
    if (fEnablement != null && fEnabledOption != null)
      fEnabledOption.initializeFrom(config);    
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fEnablement != null && fEnabledOption != null)
      fEnabledOption.performApply(config); 
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    fEnabledOption.setDefaults(config); 
    setDefaultEnablement(config);
  }
  
  protected abstract void setDefaultEnablement(ILaunchConfigurationWorkingCopy config);
}
