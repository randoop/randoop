package randoop.execution;

import java.io.File;
import java.util.List;

/**
 * Exception representing an error that occurs while running a process with {@link
 * RunEnvironment#run(List, File, long)}.
 */
public class ProcessException extends Throwable {
  private static final long serialVersionUID = 736230736083495268L;

  public ProcessException(String s, Throwable e) {
    super(s, e);
  }
}
