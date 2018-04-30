package randoop.runtime;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ErrorRevealed implements IMessage {

  public final String testCode;
  public final String description;
  public final Path junitFile;

  // Unmodifiable collection.
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
