package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;

import randoop.plugin.internal.core.StatusFactory;
import randoop.plugin.internal.core.TestKinds;

public abstract class ComboOption extends Option {
  protected Combo fCombo;
  
  public ComboOption(Combo combo) {
    fCombo = combo;
    
    fCombo.addSelectionListener(new SelectionListener() {
      
      public void widgetSelected(SelectionEvent e) {
        notifyListeners(new OptionChangeEvent(getAttributeName(), getValue()));
      }
      
      public void widgetDefaultSelected(SelectionEvent e) {
      }
    });
  }
  
  public IStatus canSave() {
    if (fCombo == null) {
      return StatusFactory.createErrorStatus(ComboOption.class.getName()
          + " incorrectly initialized"); //$NON-NLS-1$
    }
    
    String text = getValue();
    
    return validate(text);
  }
  
  public IStatus isValid(ILaunchConfiguration config) {
    return validate(getValue(config));
  }
  

  public void initializeFrom(ILaunchConfiguration config) {
    setDisableListeners(true);
    
    if (fCombo != null)
      fCombo.select(TestKinds.valueOf(getValue(config)).getCommandId());
    
    setDisableListeners(false);
  }

  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fCombo != null)
      config.setAttribute(getAttributeName(), getValue());
  }
  
  protected String getValue(ILaunchConfiguration config) {
    try {
      return config.getAttribute(getAttributeName(), getDefaltValue());
    } catch (CoreException ce) {
      return getDefaltValue();
    }
  }

  public void restoreDefaults() {
    fCombo.select(getDefaultIndex());
  }
  
  protected abstract IStatus validate(String text);
  
  protected abstract String getAttributeName();
  
  protected abstract String getValue();
  
  protected abstract String getDefaltValue();
  
  protected abstract int getDefaultIndex();
  
}
