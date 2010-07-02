package randoop.runtime;

import java.io.File;

/**
 * A message indicating that Randoop created a specific JUnit file containing
 * generated tests.
 */
public class CreatedJUnitFile implements IMessage {

  private static final long serialVersionUID = 3786576811718698647L;

  private final File file;

  public CreatedJUnitFile(File f) {
    if (f == null) {
      throw new IllegalArgumentException("f is null");
    }
    this.file = f;
  }
  
  public File getFile() {
    return file;
  }
}
