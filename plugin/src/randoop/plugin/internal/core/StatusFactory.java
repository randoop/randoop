package randoop.plugin.internal.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import randoop.plugin.RandoopActivator;
import randoop.plugin.launching.IRandoopLaunchConfigConstants;

public class StatusFactory {
  public static IStatus createOkStatus() {
    return createOkStatus(IRandoopLaunchConfigConstants.EMPTY_STRING);
  }
  
  public static IStatus createOkStatus(String message) {
    return new Status(IStatus.OK, RandoopActivator.getPluginId(), message);
  }
  
  public static IStatus createErrorStatus() {
    return createErrorStatus(IRandoopLaunchConfigConstants.EMPTY_STRING);
  }
  
  public static IStatus createErrorStatus(String message) {
    return new Status(IStatus.ERROR, RandoopActivator.getPluginId(), message);
  }
}
