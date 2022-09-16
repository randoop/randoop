package randoop.runtime;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A message indicating that an error was revealed. */
public class ErrorRevealed implements IMessage {

  /** The Java code. */
  public final String testCode;
  /** A description of the error. */
  public final String description;

  /** The file for the JUnit test. */
  @SuppressWarnings("serial") // TODO: use a serializable type.
  public final Path junitFile;

  /** The classes whose tests failed. An unmodifiable collection. */
  @SuppressWarnings("serial") // TODO: use a serializable type.
  public final List<String> failingClassNames;

  public ErrorRevealed(
      String testCode, String description, List<String> failingClassNames, Path junitFile) {
    this.testCode = testCode;
    this.description = description;
    this.failingClassNames = Collections.unmodifiableList(new ArrayList<>(failingClassNames));
    this.junitFile = junitFile;
  }

  private static final long serialVersionUID = -9131735651851725022L;

  @Override
  public String toString() {
    return description;
  }

  public List<String> getFailingClassNames() {
    return failingClassNames;
  }
}
