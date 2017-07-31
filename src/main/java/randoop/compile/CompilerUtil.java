package randoop.compile;

import java.io.PrintStream;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/** Utilities related to using the compiler */
public class CompilerUtil {
  public static void printDiagnostics(
      PrintStream out, List<Diagnostic<? extends JavaFileObject>> diagnostics) {
    for (Diagnostic<? extends JavaFileObject> diag : diagnostics) {
      if (diag != null) {
        if (diag.getSource() != null) {
          String sourceName = diag.getSource().toUri().toString();
          if (diag.getLineNumber() >= 0) {
            out.printf(
                "Error on line %d, col %d of %s%n%s%n",
                diag.getLineNumber(), diag.getColumnNumber(), sourceName, diag.getMessage(null));
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
