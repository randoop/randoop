package randoop.runtime;

import java.nio.file.Path;

/** A message indicating that Randoop created a specific JUnit file containing generated tests. */
public class CreatedJUnitFile implements IMessage {

  private static final long serialVersionUID = 3786576811718698647L;

  /** The file that was created. */
  @SuppressWarnings("serial") // TODO: use a serializable type.
  private final Path file;

  private final boolean isDriver;

  public CreatedJUnitFile(Path f, boolean isDriver) {
    if (f == null) {
      throw new IllegalArgumentException("f is null");
    }
    this.file = f;

    this.isDriver = isDriver;
  }

  public Path getFile() {
    return file;
  }

  public boolean isDriver() {
    return isDriver;
  }
}
