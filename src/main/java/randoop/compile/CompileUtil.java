package randoop.compile;

import java.net.URI;
import java.net.URISyntaxException;
import javax.tools.JavaFileManager;

/** Utilities for compiler classes. */
class CompileUtil {

  /** The file extension for a Java source file */
  static final String JAVA_EXTENSION = ".java";

  /**
   * Converts the path string to a URI for use by the file manager of the compiler.
   *
   * @see SequenceJavaFileManager#uri(JavaFileManager.Location, String, String)
   * @param pathString the path to a file as a string
   * @return the {@code URI} for the path
   */
  static URI toURI(String pathString) {
    try {
      return new URI(pathString);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
