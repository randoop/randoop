package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Combo;

import randoop.plugin.internal.core.StatusFactory;

public abstract class ComboOption extends Option {
  protected Combo fCombo;
  
  public ComboOption(Combo combo) {
    fCombo = combo;
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
  
  protected abstract IStatus validate(String text);

  public void initializeFrom(ILaunchConfiguration config) {
    if (fCombo != null)
      fCombo.setText(getValue(config));
  }

  public void performApply(ILaunchConfigurationWorkingCopy config) {
    if (fCombo != null)
      setValue(config, getValue());
  }
  
  protected abstract String getValue();
  
  protected abstract String getValue(ILaunchConfiguration config);
  
  protected abstract void setValue(ILaunchConfigurationWorkingCopy config, String value);
}
