package randoop.plugin.internal.ui.launching;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.core.StatusFactory;

public class RandoopLaunchConfigurationUtil {
  public static IStatus validatePositiveInt(String n, String nonposErrorMsg,
      String invalidErrorMsg) {
    try {
      if (Integer.parseInt(n) < 1) {
        return StatusFactory.createErrorStatus(nonposErrorMsg);
      }
      return StatusFactory.createOkStatus();
    } catch (NumberFormatException nfe) {
      return StatusFactory.createErrorStatus(invalidErrorMsg);
    }
  }
  
  public static IPackageFragmentRoot getPackageFragmentRoot(String handlerId) {
    IJavaElement element = JavaCore.create(handlerId);

    if (element == null || !(element instanceof IPackageFragmentRoot)) {
      return null;
    } else {
      return (IPackageFragmentRoot) element;
    }
  }
  
  public static IPackageFragmentRoot getPackageFragmentRoot(IJavaProject javaProject, String folder) {
    if (javaProject != null && javaProject.exists() && javaProject.isOpen()) {
      try {
        IPath path = javaProject.getPath().append(folder).makeAbsolute();
        IPackageFragmentRoot pfr = javaProject.findPackageFragmentRoot(path);
        if (pfr != null && pfr.exists()) {
          return pfr;
        }
      } catch (JavaModelException e) {
        RandoopPlugin.log(e);
      }
    }
    return null;
  }

  public static IJavaProject getProjectFromName(String projectName) {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IStatus status = workspace.validateName(projectName, IResource.PROJECT);

    if (status.isOK()) {
      IProject project = workspace.getRoot().getProject(projectName);
      try {
        return (IJavaProject) project.getNature(JavaCore.NATURE_ID);
      } catch (CoreException e) {
        RandoopPlugin.log(e);
      }
    }
    return null;
  }
  
}
