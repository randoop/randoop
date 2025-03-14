package flaky;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

/**
 * Input class for attempting to create a flaky test due to methods using a file.
 * One method creates the file and the others read from it.
 */
public class FlakyFileDependency {

  private final String name;

  public FlakyFileDependency(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof FlakyFileDependency)) {
      return false;
    }
    FlakyFileDependency fileDependency = (FlakyFileDependency)object;
    return this.name.equals(fileDependency.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name);
  }

  @Override
  public String toString() {
    return "@FlakyDependency[" + name + "]";
  }

  public boolean createFile() {
    File file = new File(name);
    try (FileWriter out = new FileWriter(file)) {
      out.write("this is junk for a test");
      out.flush();
    } catch (IOException e) {
      return false;
    }
    file.deleteOnExit();
    return true;
  }

  public boolean renameFile() {
    File file = new File(name);
    File anotherFile = new File("another-" + name);
    return file.renameTo(anotherFile);
  }

  public boolean exists() {
    return (new File(name).exists());
  }

}
