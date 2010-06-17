package randoop.plugin.tests.ui.launchConfigurations;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.ui.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.ui.launchConfigurations.OutputTab;
import randoop.plugin.internal.ui.launchConfigurations.ParametersTab;
import randoop.plugin.internal.ui.launchConfigurations.StatementsTab;

public class LaunchDelegateTests extends TestCase {
  private static IPath getFullPath(IPath localPath) {
    URL url = FileLocator.find(RandoopPlugin.getDefault().getBundle(), localPath, null);
    try {
      url = FileLocator.toFileURL(url);
    } catch (IOException e) {
      return null;
    }
    return new Path(url.getPath());
  }
  
  /**
   * Returns the launch manager
   * 
   * @return launch manager
   */
  private ILaunchManager getLaunchManager() {
    return DebugPlugin.getDefault().getLaunchManager();
  }
  
  /**
   * Tests if the java launch delegate was found as one of the delegates for
   * debug mode.
   * 
   * @throws CoreException
   */
  public void testNoDebugModeDelegate() throws CoreException {
    ILaunchManager manager = getLaunchManager();
    ILaunchConfigurationType type = manager.getLaunchConfigurationType(IRandoopLaunchConfigurationConstants.ID_RANDOOP_TEST_GENERATION);
    assertNotNull("Missing java application launch config type", type); //$NON-NLS-1$

    assertFalse("Should not support mode (debug)", type.supportsMode(ILaunchManager.DEBUG_MODE)); //$NON-NLS-1$
  }

  /**
   * Tests if the java launch delegate was found as one of the delegates for
   * debug mode.
   * 
   * @throws CoreException
   */
  public void testSingleRunModeDelegate() throws CoreException {
    ILaunchManager manager = getLaunchManager();
    ILaunchConfigurationType type = manager.getLaunchConfigurationType(IRandoopLaunchConfigurationConstants.ID_RANDOOP_TEST_GENERATION);
    assertNotNull("Missing java application launch config type", type); //$NON-NLS-1$

    HashSet modes = new HashSet();
    modes.add(ILaunchManager.RUN_MODE);
    assertTrue("Should support mode (run)", type.supportsMode(ILaunchManager.RUN_MODE)); //$NON-NLS-1$
    ILaunchDelegate[] delegates = type.getDelegates(modes);
    assertTrue("missing delegate", delegates.length > 0); //$NON-NLS-1$
    boolean found = false;
    for (int i = 0; i < delegates.length; i++) {
      if (delegates[i].getDelegate().getClass().getName().endsWith(
          "RandoopLaunchDelegate")) { //$NON-NLS-1$
        found = true;
        break;
      }
    }
    assertTrue("The randoop launch delegate was not one of the returned delegates", found); //$NON-NLS-1$
  }
  
  /**
   * Ensures a launch delegate can provide a launch object for
   * a launch.
   * @throws CoreException
   */
  public void testProvideLaunch() throws CoreException {
    ILaunchManager manager = getLaunchManager();
    ILaunchConfigurationType configurationType = manager.getLaunchConfigurationType(IRandoopLaunchConfigurationConstants.ID_RANDOOP_TEST_GENERATION); //$NON-NLS-1$
    assertNotNull("Missing test launch config type", configurationType); //$NON-NLS-1$
    ILaunchConfigurationWorkingCopy workingCopy = configurationType.newInstance(null, "provide-launch-object"); //$NON-NLS-1$
    
    new OutputTab().setDefaults(workingCopy);
    new ParametersTab().setDefaults(workingCopy);
    new StatementsTab().setDefaults(workingCopy);
    
    // delegate will throw exception if test fails
    ILaunch launch = workingCopy.launch(ILaunchManager.RUN_MODE, null);
    manager.removeLaunch(launch);
  }
}
