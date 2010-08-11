package randoop.plugin.model.resultstree;

import java.io.File;

/**
 * Represents a single generated unit test.
  */
public class UnitTest {
  
  private static final IRandoopTreeElement[] EMPTY_ARRAY = new IRandoopTreeElement[0];
  
  public final String testCode;

  public final File junitFile;

  public UnitTest(String testCode, File junitFile) {
    this.testCode = testCode;
    this.junitFile = junitFile;
  }
}
