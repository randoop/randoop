package randoop.runtime;

import java.nio.file.Path;

/** A message indicating that Randoop created a specific JUnit file containing generated tests. */
public class CreatedJUnitFile implements IMessage {

  /** ID for serializing this class. */
  private static final long serialVersionUID = 3786576811718698647L;

  /** The file that was created. */
  @SuppressWarnings("serial") // TODO: use a serializable type.
  private final Path file;

  /** True if the file is a test driver (a main class that runs the test classes). */
  private final boolean isDriver;

  /**
   * Creates a message indicating that the given JUnit file was created.
   *
   * @param f the file that was created
   * @param isDriver true if the file is a test driver (a main class that runs the test classes)
   */
  public CreatedJUnitFile(Path f, boolean isDriver) {
    if (f == null) {
      throw new IllegalArgumentException("f is null");
    }
    this.file = f;

    this.isDriver = isDriver;
  }

  /**
   * Returns the file that was created.
   *
   * @return the file that was created
   */
  public Path getFile() {
    return file;
  }

  /**
   * Returns true if the file is a test driver (a main class that runs the test classes).
   *
   * @return true if the file is a test driver
   */
  public boolean isDriver() {
    return isDriver;
  }
}
