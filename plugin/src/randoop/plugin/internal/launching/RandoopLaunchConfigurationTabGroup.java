package randoop.plugin.internal.launching;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;

import randoop.plugin.internal.ui.launchConfigurations.OutputTab;
import randoop.plugin.internal.ui.launchConfigurations.ParametersTab;
import randoop.plugin.internal.ui.launchConfigurations.StatementsTab;

public class RandoopLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

  /*
   * (non-Javadoc)
   * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(org.eclipse.debug.ui.ILaunchConfigurationDialog, java.lang.String)
   */
  @Override
    public void createTabs(ILaunchConfigurationDialog arg0, String arg1) {
    ILaunchConfigurationTab[] tabs= new ILaunchConfigurationTab[] {
        new StatementsTab(),
        new OutputTab(),
        new ParametersTab(),
        new JavaClasspathTab(),
        new JavaJRETab(),
        new EnvironmentTab(),
        new CommonTab()
    };
    setTabs(tabs);
  }
}
