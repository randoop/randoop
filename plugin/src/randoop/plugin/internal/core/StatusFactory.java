package randoop.plugin.internal.core;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import randoop.plugin.RandoopPlugin;

/**
 * Factory class to create <code>IStatus</code> objects.
 * 
 * @author Peter Kalauskas
 */
public class StatusFactory {
  
  public final int fCode;

  public final int fSeverity;

  public final String fMessage;

  private StatusFactory(int code, int severity, String message) {
    fCode = code;
    fSeverity = severity;
    fMessage = message;
    
  }
  
  /**
   * An OK status with an empty message.
   */
  public static final IStatus OK_STATUS =  createOkStatus(""); //$NON-NLS-1$

  /**
   * An ERROR status with an empty message.
   */
  public static final IStatus ERROR_STATUS = createErrorStatus(""); //$NON-NLS-1$
  
  public IStatus getStatus() {
    String m = MessageFormat.format(fMessage, new Integer(fCode));
    return new Status(fSeverity, RandoopPlugin.getPluginId(), fCode, m, null);
  }

  public IStatus getStatus(Throwable t) {
    String m = MessageFormat.format(fMessage, new Integer(fCode));
    return new Status(fSeverity, RandoopPlugin.getPluginId(), fCode, m, t);
  }
  
  public static final StatusFactory NO_LOCAL_RANDOOPJAR_ERROR = new StatusFactory(
      1001, IStatus.ERROR, "Error while retreiving Randoop archive (code {0}).");
  
  public static final StatusFactory NO_LOCAL_PLUMEJAR_ERROR = new StatusFactory(
      1002, IStatus.ERROR, "Error while retreiving Plume archive (code {0}).");

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
