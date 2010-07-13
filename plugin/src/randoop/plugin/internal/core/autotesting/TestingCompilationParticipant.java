package randoop.plugin.internal.core.autotesting;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

public class TestingCompilationParticipant extends CompilationParticipant {
  @Override
  public int aboutToBuild(IJavaProject project) {
//    System.out.println("About to build " + project.getElementName());
    return READY_FOR_BUILD;
  }

  @Override
  public void buildStarting(BuildContext[] files, boolean isBatch) {
//    System.out.println("Build starting");
  }

  /**
   * Runs the Randoop tests associated with the the <code>IJavaProject</code>
   * that finished building.
   * 
   * @see org.eclipse.jdt.core.compiler.CompilationParticipant#buildFinished(org.eclipse.jdt.core.IJavaProject)
   */
  @Override
  public void buildFinished(IJavaProject project) {
//    System.out.println("Build finished " + project.getElementName());
//
//    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
//    try {
//      ILaunchConfiguration[] launchConfigs = launchManager
//          .getLaunchConfigurations();
//      for (ILaunchConfiguration lc : launchConfigs) {
//        System.out.println(lc.getName());
//      }
//    } catch (CoreException e) {
//    }
  }

  /**
   * Returns true for all <code>IJavaProject</code>s.
   * 
   * @see org.eclipse.jdt.core.compiler.CompilationParticipant#isActive(org.eclipse.jdt.core.IJavaProject)
   */
  @Override
  public boolean isActive(IJavaProject project) {
    return true;
  }
}
