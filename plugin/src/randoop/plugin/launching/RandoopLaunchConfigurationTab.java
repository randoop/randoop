package randoop.plugin.launching;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.widgets.Composite;

public class RandoopLaunchConfigurationTab extends AbstractLaunchConfigurationTab {

  @Override
    public void createControl(Composite arg0) {
    // TODO Auto-generated method stub
                
  }

  @Override
    public String getName() {
    return "Randoop options";
  }

  @Override
    public void initializeFrom(ILaunchConfiguration arg0) {
    // TODO Auto-generated method stub
                
  }

  @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    configuration.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "test");
  }

  @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy arg0) {
    // TODO Auto-generated method stub
                
  }

  @Override
    public boolean isValid(ILaunchConfiguration config) {
    return true;
  }

}
