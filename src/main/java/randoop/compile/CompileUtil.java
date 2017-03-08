package randoop.compile;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utilities for compiler classes.
 */
class CompileUtil {
  static final String JAVA_EXTENSION = ".java";

  static URI toURI(String name) {
    try {
      return new URI(name);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
