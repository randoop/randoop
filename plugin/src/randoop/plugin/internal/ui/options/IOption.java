package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

public interface IOption {

  public IStatus canSave();

  public IStatus isValid(ILaunchConfiguration config);

  public void initializeFrom(ILaunchConfiguration config);

  public void performApply(ILaunchConfigurationWorkingCopy config);

  public void setDefaults(ILaunchConfigurationWorkingCopy config);

  public void addChangeListener(IOptionChangeListener listener);
  
  public void removeChangeListener(IOptionChangeListener listener);

  public void restoreDefaults();
  
}
