package randoop.plugin.launching;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import randoop.plugin.internal.core.StatusFactory;

public class RandoopLaunchingUtil {
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
}
