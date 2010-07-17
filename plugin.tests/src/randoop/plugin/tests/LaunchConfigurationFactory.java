package randoop.plugin.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;

import randoop.plugin.internal.core.launching.IRandoopLaunchConfigurationConstants;
import randoop.plugin.internal.core.launching.RandoopArgumentCollector;

public class LaunchConfigurationFactory {
  
  public static ILaunchConfigurationWorkingCopy createNewAllTypeConfig(IJavaProject javaProject, int timelimit) throws CoreException {
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfigurationType randoopType = launchManager.getLaunchConfigurationType(IRandoopLaunchConfigurationConstants.ID_RANDOOP_TEST_GENERATION);

    final ILaunchConfigurationWorkingCopy config = randoopType.newInstance(null, "All Type Config");

    IProject project = javaProject.getProject();
    IFolder folder = project.getFolder("test");
    IPackageFragmentRoot testFolder = javaProject.getPackageFragmentRoot(folder);

    // Search for all java elements in the project and add them to the
    // configuration
    List<String> availableTypes = new ArrayList<String>();
    for (IPackageFragmentRoot pfRoot : javaProject.getPackageFragmentRoots()) {
      if (pfRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
        for (IJavaElement element : pfRoot.getChildren()) {
          if (element instanceof IPackageFragment) {
            IPackageFragment packageFragment = (IPackageFragment) element;
            for (IJavaElement compElement : packageFragment.getChildren()) {
              if (compElement instanceof ICompilationUnit) {
                IType[] types = ((ICompilationUnit) compElement).getAllTypes();
                for (IType type : types) {
                  availableTypes.add(type.getHandleIdentifier());
                }
              }
            }
          }
        }
      }
    }
    
    RandoopArgumentCollector.setSelectedTypes(config, availableTypes);
    RandoopArgumentCollector.setTimeLimit(config, "" + timelimit);
    RandoopArgumentCollector.setOutputDirectoryName(config, testFolder.getElementName());
    RandoopArgumentCollector.setJUnitPackageName(config, "demo.pathplanning.allTests");
    RandoopArgumentCollector.setJUnitClassName(config, "AllTypeTest");
    
    return config;
  }
  
}
