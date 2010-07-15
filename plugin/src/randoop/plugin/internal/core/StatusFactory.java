package randoop.plugin.internal.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import randoop.plugin.RandoopPlugin;
import randoop.plugin.internal.IConstants;

/**
 * Factory class to create <code>IStatus</code> objects.
 * 
 * @author Peter Kalauskas
 */
public class StatusFactory {

  /**
   * Returns an OK status with an empty message.
   */
  public static IStatus createOkStatus() {
    return createOkStatus(IConstants.EMPTY_STRING);
  }

  /**
   * An OK status with the specified message.
   * 
   * @param message
   *          message to be used for the returned <code>IStatus</code>
   * @return
   */
  public static IStatus createOkStatus(String message) {
    return new Status(IStatus.OK, RandoopPlugin.getPluginId(), message);
  }

  /**
   * Returns an ERROR status with an empty message.
   */
  public static IStatus createErrorStatus() {
    return createErrorStatus(IConstants.EMPTY_STRING);
  }

  /**
   * 
   * @param message
   *          message to be used for the returned <code>IStatus</code>
   * @return
   */
  public static IStatus createErrorStatus(String message) {
    return new Status(IStatus.ERROR, RandoopPlugin.getPluginId(), message);
  }
}
