package randoop.output;

import java.io.File;
import randoop.main.Minimize;

/** Created by bjkeller on 7/3/17. */
public class MinimizerWriter implements CodeWriter {
  private final JavaFileWriter javaFileWriter;

  public MinimizerWriter(JavaFileWriter javaFileWriter) {
    this.javaFileWriter = javaFileWriter;
  }

  @Override
  public File writeClass(String packageName, String classname, String classString) {

    File testFile = javaFileWriter.writeClass(packageName, classname, classString);

    // Minimize the error-revealing test that has been output.
    Minimize.mainMinimize(
        testFile, Minimize.suiteclasspath, Minimize.testsuitetimeout, Minimize.verboseminimizer);

    return testFile;
  }

  @Override
  public File writeUnmodifiedClass(String packageName, String classname, String classString) {
    return javaFileWriter.writeClass(packageName, classname, classString);
  }
}
