package randoop.compile;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

/** Exception for compilation of Java classes given as {@code String}. */
public class SequenceCompilerException extends Throwable {

  private static final long serialVersionUID = -1901576275093767250L;

  /** the source text for the class */
  private final String sourceText;

  /** the compiler diagnostics */
  private final DiagnosticCollector<JavaFileObject> diagnostics;

  /**
   * Creates a {@link SequenceCompilerException} with a message, source text and compiler
   * diagnostics.
   *
   * @param message the exception message
   * @param sourceText the source text for the compiled class
   * @param diagnostics the compiler diagnostics
   */
  SequenceCompilerException(
      String message, String sourceText, DiagnosticCollector<JavaFileObject> diagnostics) {
    super(message);
    this.sourceText = sourceText;
    this.diagnostics = diagnostics;
  }

  /**
   * Creates a {@link SequenceCompilerException} with a message, a compiler thrown exception, the
   * source text and compiler diagnostics.
   *
   * @param message the exception message
   * @param cause the compiler exception
   * @param sourceText the source text for the compiled class
   * @param diagnostics the compiler diagnostics
   */
  SequenceCompilerException(
      String message,
      Throwable cause,
      String sourceText,
      DiagnosticCollector<JavaFileObject> diagnostics) {
    super(message, cause);
    this.sourceText = sourceText;
    this.diagnostics = diagnostics;
  }

  /**
   * Get the source text for the class being compiled when this exception was thrown.
   *
   * @return the source text for the class that was being compiled
   */
  public String getSourceText() {
    return sourceText;
  }

  /**
   * Return the compiler diagnostics.
   *
   * @return the compiler diagnostics
   */
  public DiagnosticCollector<JavaFileObject> getDiagnostics() {
    return diagnostics;
  }
}
