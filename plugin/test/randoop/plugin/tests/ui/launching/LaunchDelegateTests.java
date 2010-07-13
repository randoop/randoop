package randoop.plugin.tests.ui.launching;

import java.util.HashSet;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;

import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.ui.launching.GeneralTab;
import randoop.plugin.internal.ui.launching.ParametersTab;

@SuppressWarnings("nls")
public class LaunchDelegateTests extends TestCase {
  private IJavaProject fJavaProject;
  
  @Override
  protected void setUp() throws Exception {
    fJavaProject = ProjectCreator.createStandardDemoProject();
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
    
    new GeneralTab().setDefaults(workingCopy);
    new ParametersTab().setDefaults(workingCopy);
    
    // delegate will throw exception if test fails
    ILaunch launch = workingCopy.launch(ILaunchManager.RUN_MODE, null);
    manager.removeLaunch(launch);
  }
}
