package randoop.compile;

import java.io.File;
import java.util.List;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

/**
 * Exception for errors during compilation using {@link FileCompiler}, which compiles classes from
 * Java source files.
 */
public class FileCompilerException extends Throwable {

  private static final long serialVersionUID = 8362158619216912395L;

  /** The list of source files for the compilation */
  private final List<File> sourceFiles;

  /** The compiler diagnostics */
  private final DiagnosticCollector<JavaFileObject> diagnostics;

  /**
   * Creates a {@link FileCompilerException} with the error message, the list of source classes, and
   * compiler diagnostics from the compilation.
   *
   * @param message the exception message
   * @param sourceFiles the list of source files
   * @param diagnostics the compiler diagnostics
   */
  FileCompilerException(
      String message, List<File> sourceFiles, DiagnosticCollector<JavaFileObject> diagnostics) {
    super(message);
    this.sourceFiles = sourceFiles;
    this.diagnostics = diagnostics;
  }

  /**
   * Returns the list of source files used in the compilation that generated this error.
   *
   * @return the list of source files for which compilation generated this exception
   */
  public List<File> getSourceFiles() {
    return sourceFiles;
  }

  /**
   * Returns the compiler diagnostics for the compilation that generated this exception.
   *
   * @return the compiler diagnostics for the compilation that generated this exception
   */
  public DiagnosticCollector<JavaFileObject> getDiagnostics() {
    return diagnostics;
  }
}
