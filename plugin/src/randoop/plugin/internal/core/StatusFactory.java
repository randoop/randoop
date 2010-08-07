package randoop.plugin.internal.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import randoop.plugin.RandoopPlugin;

/**
 * Factory class to create <code>IStatus</code> objects.
 * 
 * @author Peter Kalauskas
 */
public class StatusFactory {

  /**
   * An OK status with an empty message.
   */
  public static final IStatus OK_STATUS =  createOkStatus(""); //$NON-NLS-1$

  /**
   * An ERROR status with an empty message.
   */
  public static final IStatus ERROR_STATUS = createErrorStatus(""); //$NON-NLS-1$

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
   * An ERROR status with the specified message.
   * 
   * @param message
   *          message to be used for the returned <code>IStatus</code>
   * @return
   */
  public static IStatus createErrorStatus(String message) {
    return new Status(IStatus.ERROR, RandoopPlugin.getPluginId(), message);
  }
  
  /**
   * A WARNING status with the specified message.
   * 
   * @param message
   *          message to be used for the returned <code>IStatus</code>
   * @return
   */
  public static IStatus createWarningStatus(String message) {
    return new Status(IStatus.WARNING, RandoopPlugin.getPluginId(), message);
  }
  
}
