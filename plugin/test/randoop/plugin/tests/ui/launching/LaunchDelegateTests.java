package randoop.plugin.tests.ui.launching;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.ui.PlatformUI;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;
import randoop.plugin.internal.core.runtime.IMessageListener;
import randoop.plugin.internal.core.runtime.MessageReceiver;
import randoop.plugin.internal.ui.launching.OutputTab;
import randoop.plugin.internal.ui.launching.ParametersTab;
import randoop.plugin.internal.ui.launching.StatementsTab;
import randoop.runtime.Message;

public class LaunchDelegateTests extends TestCase {
  private IJavaProject fJavaProject;
  
  private static IPath getFullPath(IPath localPath) {
    URL url = FileLocator.find(RandoopPlugin.getDefault().getBundle(), localPath, null);
    try {
      url = FileLocator.toFileURL(url);
    } catch (IOException e) {
      return null;
    }
    return new Path(url.getPath());
  }
  
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
    
    new OutputTab().setDefaults(workingCopy);
    new ParametersTab().setDefaults(workingCopy);
    new StatementsTab().setDefaults(workingCopy);
    
    // delegate will throw exception if test fails
    ILaunch launch = workingCopy.launch(ILaunchManager.RUN_MODE, null);
    manager.removeLaunch(launch);
  }
  
  public void testStartRandoop() throws CoreException, IOException {
    ILaunchConfigurationType javaType = DebugPlugin.getDefault()
        .getLaunchManager().getLaunchConfigurationType(
            IRandoopLaunchConfigurationConstants.ID_RANDOOP_TEST_GENERATION);
    
    final ILaunchConfigurationWorkingCopy config = javaType.newInstance(null,
        "Test Config"); //$NON-NLS-1$
    
    IProject project = fJavaProject.getProject();
    IFolder folder = project.getFolder(ProjectCreator.testFolderName);
    IPackageFragmentRoot testFolder = fJavaProject.getPackageFragmentRoot(folder);

    List<String> availableTypes = new ArrayList<String>();
    for (IPackageFragmentRoot pfRoot : fJavaProject.getPackageFragmentRoots()) {
      if (pfRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
        for (IJavaElement element : pfRoot.getChildren()) {
          if (element instanceof IPackageFragment) {
            IPackageFragment packageFragment = (IPackageFragment) element;
            for(IJavaElement compElement : packageFragment.getChildren()) {
            if (compElement instanceof ICompilationUnit) {
              IType[] types = ((ICompilationUnit) compElement).getAllTypes();
              for (IType type : types) {
                availableTypes.add(type.getHandleIdentifier());
              }
            }
          }}
        }
      }
    }
    
    RandoopArgumentCollector.setAllJavaTypes(config, availableTypes);
    RandoopArgumentCollector.setTimeLimit(config, "100"); //$NON-NLS-1$
    RandoopArgumentCollector.setOutputDirectoryHandlerId(config, testFolder.getHandleIdentifier());
    RandoopArgumentCollector.setJUnitPackageName(config, "demo.pathplanning.allTests"); //$NON-NLS-1$
    RandoopArgumentCollector.setJUnitPackageName(config, "Test"); //$NON-NLS-1$
    
    MessageReceiver mr = new MessageReceiver(new IMessageListener() {
      @Override
      public void handleMessage(Message m) {
        System.out.println(m);
      }
    });
    RandoopArgumentCollector.setPort(config, mr.getPort());
    new Thread(mr).start();

    // Launch the configuration from the UI thread
    PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        try {
          config.launch(ILaunchManager.RUN_MODE, null, true);
        } catch (CoreException e) {
          e.printStackTrace();
          fail();
        }
      }
    });
  }
}
