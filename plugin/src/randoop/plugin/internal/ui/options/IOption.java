package randoop.plugin.internal.ui.options;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public interface IOption {

  IStatus canSave();

  IStatus isValid(ILaunchConfiguration config);

  void initializeFrom(ILaunchConfiguration config);

  void performApply(ILaunchConfigurationWorkingCopy config);

  void setDefaults(ILaunchConfigurationWorkingCopy config);

}
