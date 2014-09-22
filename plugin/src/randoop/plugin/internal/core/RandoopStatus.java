package randoop.plugin.internal.core;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import randoop.plugin.RandoopPlugin;

/**
 * Status class used to create new <code>IStatus</code> objects for both error
 * handling (as in <code>CoreExceptions</code>) and UI messages for method such
 * as those in <code>IOption</code>.
 * <p>
 * (design pattern taken from com.mountainminds.eclemma.core.EclEmmaStatus)
 * 
 * @see org.eclipse.core.runtime.CoreException
 */
public class RandoopStatus {

  public final int fCode;

  public final int fSeverity;

  public final String fMessage;

  /**
   * Creates a new <code>RandoopStatus</code> that may be used to construct
   * <code>IStatus</code> objects for error handling. The message may use
   * <code>MessageFormat</code> argument {0} for displaying the code, and {1}
   * for an additional parameter that may be useful for debugging. Messages must
   * be human-readable and give a good description of the nature of the problem.
   * Ideally, possible steps to resolve the problem should also be given.
   * 
   * @param code
   * @param severity
   * @param message
   */
  private RandoopStatus(int code, int severity, String message) {
    fCode = code;
    fSeverity = severity;
    fMessage = message;
  }

  /**
   * An OK status with an empty message. This should only be used for UI methods
   * that return an <code>IStatus</code>.
   */
  public static final IStatus OK_STATUS = createUIStatus(IStatus.OK, ""); //$NON-NLS-1$

  public IStatus getStatus() {
    String m = MessageFormat.format(fMessage, new Integer(fCode));
    return new Status(fSeverity, RandoopPlugin.getPluginId(), fCode, m, null);
  }

  public IStatus getStatus(Throwable t) {
    String m = MessageFormat.format(fMessage, new Integer(fCode));
    return new Status(fSeverity, RandoopPlugin.getPluginId(), fCode, m, t);
  }

  public IStatus getStatus(Object param, Throwable t) {
    String m = NLS.bind(fMessage, new Integer(fCode), param);
    return new Status(fSeverity, RandoopPlugin.getPluginId(), fCode, m, t);
  }

  public static final RandoopStatus NO_LOCAL_RANDOOPJAR_ERROR =
    new RandoopStatus(1001, IStatus.ERROR,
        "Error while retreiving Randoop archive (code {0}).");

  public static final RandoopStatus COMM_NO_FREE_PORT =
    new RandoopStatus(5000, IStatus.ERROR,
        "Randoop plug-in could not find a free communication port. Check your firewall settings (code {0}).");
  
  public static final RandoopStatus COMM_TERMINATED_SESSION =
    new RandoopStatus(5001, IStatus.ERROR,
        "Randoop closed communications on port {1} unexpectadely. Was the process killed? (code {0}).");
  
  public static final RandoopStatus COMM_MESSAGE_CLASS_NOT_FOUND =
    new RandoopStatus(5002, IStatus.ERROR,
        "Randoop plug-in received a message from Randoop with an Object whose class is not in the plug-ins classpath (code {0}).");
  
  public static final RandoopStatus GENERATED_FILE_NOT_IN_PROJECT =
    new RandoopStatus(10000, IStatus.ERROR,
        "The JUnit test file {1} is not in the project for which it was generated (code {0}).");
  
  public static final RandoopStatus JAVA_MODEL_EXCEPTION =
    new RandoopStatus(10001, IStatus.ERROR,
        "A Java model object either does not exist or an error occured while accessing its corresponding resource. Try refreshing the project (code {0})");
  
  public static final RandoopStatus PART_INIT_EXCEPTION =
    new RandoopStatus(10001, IStatus.ERROR,
    "The view part could not be created or initialized. Try again or open the view by hand (code {0})");
  
  public static final RandoopStatus RESOURCE_REFRESH_EXCEPTION =
    new RandoopStatus(10002, IStatus.ERROR,
        "An error occured while trying to refresh a resource. Try running the operation again (code {0})");

  public static final RandoopStatus NO_JAVA_PROJECT =
    new RandoopStatus(20003, IStatus.ERROR,
        "Java project ''{1}'' was not found or could not be created (code {0})");

  /**
   * ID of a status to be used internally in Randoop for validation of UI
   * features only
   */
  public static int RANDOOP_VALIDATION_METHOD = 110;

  /**
   * ID of status to be used for exceptions thrown by
   * <code>RandoopArgumentCollector</code> and statuses returned by its
   * validation methods.
   */
  public static int RANDOOP_LAUNCH_CONFIGURATION = 111;

  /**
   * A new IStatus with the specified message and severity. This method should
   * only be used for by methods needing to provide details of failures (e.g.,
   * validation methods for the UI). It should <i>never</i> be used for logging
   * or for throwing an <code>CoreException</code>.
   * 
   * @param message
   *          message to be used for the returned <code>IStatus</code>
   * @return
   */
  public static IStatus createUIStatus(int severity, String message) {
    return new Status(severity, RandoopPlugin.getPluginId(),
        RANDOOP_VALIDATION_METHOD, message, null);
  }

  
  public static IStatus createLaunchConfigurationStatus(int severity, String message, Throwable exception) {
    return new Status(severity, RandoopPlugin.getPluginId(), RANDOOP_LAUNCH_CONFIGURATION,
        message, exception);
  }
  
}
