package randoop.plugin.internal.ui.launching;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

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

  public static IJavaProject getProject(String handlerId) {
    IJavaElement element = JavaCore.create(handlerId);

    if (element == null || !(element instanceof IJavaProject)) {
      return null;
    } else {
      return (IJavaProject) element;
    }
  }
}
