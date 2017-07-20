package randoop.execution;

import java.io.File;
import java.util.List;

/**
 * Exception representing an error that occured while running a process with {@link
 * RunEnvironment#run(List, File, long)}.
 */
public class ProcessException extends Throwable {

  private static final long serialVersionUID = 736230736083495268L;

  /**
   * Creates a {@link ProcessException} with a message and causing exception.
   *
   * @param message the exception message
   * @param e the causing exception
   */
  public ProcessException(String message, Throwable e) {
    super(message, e);
  }
}
