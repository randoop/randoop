package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

public interface IOption {

  IStatus canSave();

  IStatus isValid(ILaunchConfiguration config);

  void initializeFrom(ILaunchConfiguration config);

  void performApply(ILaunchConfigurationWorkingCopy config);

  void setDefaults(ILaunchConfigurationWorkingCopy config);

  void addChangeListener(IOptionChangeListener listener);
}
