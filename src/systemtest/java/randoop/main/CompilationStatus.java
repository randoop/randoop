package randoop.main;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/** Captures the compilation status for a set of test files. */
class CompilationStatus {

  /** The flag to indicate whether the compilation succeeded. */
  final Boolean succeeded;

  /** The list of compiler diagnostics. */
  private final List<Diagnostic<? extends JavaFileObject>> diagnostics;

  /**
   * Creates a {@link CompilationStatus} object with the success and diagnostic output.
   *
   * @param succeeded the success flag
   * @param diagnostics the diagnostic output of the compiler
   */
  private CompilationStatus(
      Boolean succeeded, List<Diagnostic<? extends JavaFileObject>> diagnostics) {
    this.succeeded = succeeded;
    this.diagnostics = diagnostics;
  }

  /**
   * Compile the test files, writing the class files to the destination directory, and capturing the
   * status as a {@link CompilationStatus} object.
   *
   * @param testSourceFiles the Java source for the tests
   * @param classpath the classpath for compiling
   * @param destinationDir the path to the desination directory
   * @return true if compile succeeded, false otherwise
   */
  static CompilationStatus compileTests(
      List<File> testSourceFiles, String classpath, String destinationDir) {
    final Locale locale = null; // use default locale
    final Charset charset = null; // use default charset
    final Writer writer = null; // use System.err for output
    final List<String> annotatedClasses = null; // no classes

    List<String> options = new ArrayList<>();
    options.add("-classpath");
    options.add(classpath);
    options.add("-d");
    options.add(destinationDir);

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    Boolean succeeded = false;
    try (StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(diagnostics, locale, charset)) {
      Iterable<? extends JavaFileObject> filesToCompile =
          fileManager.getJavaFileObjectsFromFiles(testSourceFiles);
      succeeded =
          compiler
              .getTask(writer, fileManager, diagnostics, options, annotatedClasses, filesToCompile)
              .call();
    } catch (IOException e) {
      fail("I/O Error while compiling generated tests: " + e);
    }
    return new CompilationStatus(succeeded, diagnostics.getDiagnostics());
  }

  /**
   * Dumps compiler error diagnostics to the given {@code PrintStream}.
   *
   * @param err the {@code PrintStream}
   */
  void printDiagnostics(PrintStream err) {
    for (Diagnostic<? extends JavaFileObject> diag : diagnostics) {
      if (diag != null) {
        if (diag.getSource() != null) {
          String sourceName = diag.getSource().toUri().toString();
          if (diag.getLineNumber() >= 0) {
            err.printf(
                "Error on line %d, col %d of %s%n%s%n",
                diag.getLineNumber(), diag.getColumnNumber(), sourceName, diag.getMessage(null));
          } else {
            err.printf("%s%n", diag.getMessage(null));
          }
        } else {
          err.printf("%s%n", diag.getMessage(null));
        }
      }
    }
  }
}
