package randoop.compile;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import randoop.sequence.Sequence;

/**
 * Created by bjkeller on 3/2/17.
 */
public class SequenceCompilerException extends Throwable {

  private static final long serialVersionUID = -1901576275093767250L;

  public SequenceCompilerException(
      String s, Sequence sequence, DiagnosticCollector<JavaFileObject> diagnostics) {}
}
