package randoop.plugin.launching;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

import randoop.plugin.internal.core.StatusFactory;

public class RandoopLaunchConfigUtil {
  public static IStatus validatePositiveInt(String n, String name) {
    try {
      if (Integer.parseInt(n) < 1) {
        return StatusFactory.createErrorStatus(name + " is not a positive integer");
      }
      return Status.OK_STATUS;
    } catch (NumberFormatException nfe) {
      return StatusFactory.createErrorStatus(name + " is not a valid integer");
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
}
