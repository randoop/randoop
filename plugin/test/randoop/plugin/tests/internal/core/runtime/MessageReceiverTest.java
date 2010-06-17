package randoop.plugin.tests.internal.core.runtime;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.runtime.IMessageListener;
import randoop.plugin.internal.core.runtime.MessageReceiver;
import randoop.plugin.internal.ui.launchConfigurations.RandoopArgumentCollector;
import randoop.plugin.internal.ui.launchConfigurations.RandoopLaunchDelegate;
import randoop.plugin.internal.ui.launchConfigurations.StatementsTab;
import randoop.plugin.tests.ui.launchConfigurations.ProjectCreator;
import randoop.runtime.Message;

public class MessageReceiverTest extends TestCase {
  IJavaProject fJavaProject;
  
  private static IPath getFullPath(IPath localPath) {
    URL url = FileLocator.find(RandoopPlugin.getDefault().getBundle(),
        localPath, null);
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

  public void testStartRandoop() throws CoreException, IOException {
    ILaunchConfigurationType javaType = DebugPlugin.getDefault()
        .getLaunchManager().getLaunchConfigurationType(
            IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
    ILaunchConfigurationWorkingCopy config = javaType.newInstance(null,
        "Test Config"); //$NON-NLS-1$
    
    IProject project = fJavaProject.getProject();
    IFolder folder = project.getFolder(ProjectCreator.testFolderName);
    IPackageFragmentRoot testFolder = fJavaProject.getPackageFragmentRoot(folder);

    List<String> availableTypes = new ArrayList<String>();
    for (IPackageFragmentRoot pfRoot : fJavaProject.getPackageFragmentRoots()) {
      for (IJavaElement element : pfRoot.getChildren()) {
        if (element instanceof ICompilationUnit) {
          IType[] types = ((ICompilationUnit) element).getAllTypes();
          for (IType type : types) {
            availableTypes.add(type.getHandleIdentifier());
          }
        }
      }
    }
    
    RandoopArgumentCollector.setAllJavaTypes(config, availableTypes);
    RandoopArgumentCollector.setTimeLimit(config, "10"); //$NON-NLS-1$
    RandoopArgumentCollector.setOutputDirectoryHandlerId(config, testFolder.getHandleIdentifier()); //$NON-NLS-1$
    RandoopArgumentCollector.setJUnitPackageName(config, "demo.pathplanning.allTests"); //$NON-NLS-1$
    RandoopArgumentCollector.setJUnitPackageName(config, "Test"); //$NON-NLS-1$
    
    new Thread(new MessageReceiver(new IMessageListener() {
      @Override
      public void handleMessage(Message m) {
        System.out.println(m);
      }
    })).start();

    DebugUITools.launch(config, ILaunchManager.RUN_MODE);
    System.out.println("Done"); //$NON-NLS-1$
  }
}
