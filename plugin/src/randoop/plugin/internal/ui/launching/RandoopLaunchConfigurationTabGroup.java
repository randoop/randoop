package randoop.plugin.internal.ui.launching;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class RandoopLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

  @Override
  public void createTabs(ILaunchConfigurationDialog dialog, String mod) {
    // The following tabs may need to be used in future revisions:
    // JavaClasspathTab, JavaJRETab, EnvironmentTab

    ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
        new GeneralTab(),
        new ParametersTab(),
        new CommonTab()
    };
    
    setTabs(tabs);
  }
}
