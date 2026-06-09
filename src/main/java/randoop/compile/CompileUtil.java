package randoop.compile;

import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/** Utilities for compiler classes. */
final class CompileUtil {

  /** Do not instantiate. */
  private CompileUtil() {
    throw new Error("Do not instantiate");
  }

  /**
   * Converts the path string to a URI for use by the file manager of the compiler.
   *
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

  /**
   * Prints the compiler diagnostics to the writer.
   *
   * @param out the print writer
   * @param diagnostics the compiler diagnostics
   */
  @SuppressWarnings("nullness:argument") // needed in CF 3.49.4 and earlier
  public static void printDiagnostics(
      PrintWriter out, List<Diagnostic<? extends JavaFileObject>> diagnostics) {
    for (Diagnostic<? extends JavaFileObject> diag : diagnostics) {
      if (diag != null) {
        JavaFileObject source = diag.getSource();
        if (source != null) {
          String sourceName = source.toUri().toString();
          long lineNumber = diag.getLineNumber();
          if (lineNumber >= 0) {
            out.printf(
                "Error on line %d, col %d of %s%n%s%n",
                lineNumber, diag.getColumnNumber(), sourceName, diag.getMessage(null));
          } else {
            out.printf("%s%n", diag.getMessage(null));
          }
        } else {
          out.printf("%s%n", diag.getMessage(null));
        }
      }
    }
  }
}
