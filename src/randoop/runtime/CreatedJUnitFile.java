package randoop.runtime;

import java.io.File;

/**
 * A message indicating that Randoop created a specific JUnit file containing
 * generated tests.
 */
public class CreatedJUnitFile implements IMessage {

  private static final long serialVersionUID = 3786576811718698647L;

  private final File file;
  
  private final boolean isDriver;

  public CreatedJUnitFile(File f, boolean isDriver) {
    if (f == null) {
      throw new IllegalArgumentException("f is null");
    }
    this.file = f;
    
    this.isDriver = isDriver;
  }
  
  public File getFile() {
    return file;
  }
  
  public boolean isDriver() {
    return isDriver;
  }
}
