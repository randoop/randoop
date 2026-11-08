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

  /**
   * Creates a message indicating that a test revealed an error.
   *
   * @param testCode the code of the test that revealed the error
   * @param description a description of the error
   * @param failingClassNames the classes whose tests failed
   * @param junitFile the file for the JUnit test
   */
  public ErrorRevealed(
      String testCode, String description, List<String> failingClassNames, Path junitFile) {
    this.testCode = testCode;
    this.description = description;
    this.failingClassNames = Collections.unmodifiableList(new ArrayList<>(failingClassNames));
    this.junitFile = junitFile;
  }

  /** ID for serializing this class. */
  private static final long serialVersionUID = -9131735651851725022L;

  @Override
  public String toString() {
    return description;
  }

  /**
   * Returns the classes whose tests failed.
   *
   * @return the classes whose tests failed, as an unmodifiable list
   */
  public List<String> getFailingClassNames() {
    return failingClassNames;
  }
}
