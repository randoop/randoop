package randoop.compile;

import java.io.File;
import java.util.List;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

/** Exception for compilation of java class given as a list of {@code File} objects */
public class FileCompilerException extends Throwable {
  private static final long serialVersionUID = 8362158619216912395L;

  /** The list of source files for the compilation */
  private final List<File> sourceFiles;

  /** The compiler diagnostics */
  private final DiagnosticCollector<JavaFileObject> diagnostics;

  /**
   * Creates a {@link FileCompilerException} with a message, list of source classes and compiler
   * diagnostics.
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

  public List<File> getSourceFiles() {
    return sourceFiles;
  }

  public DiagnosticCollector<JavaFileObject> getDiagnostics() {
    return diagnostics;
  }
}
