package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * An observation recording the exception that a particular
 * statement threw during execution.
 */
public class StatementThrowsException implements Observation, Serializable {

  private static final long serialVersionUID = 1L;

  private final Class<? extends Throwable> exceptionClass;

  public StatementThrowsException(Throwable exception) {
    if (exception == null)
      throw new IllegalArgumentException("exception cannot be null.");
    this.exceptionClass = exception.getClass();
  }

  public StatementThrowsException (Class<? extends Throwable> exception_class) {
    this.exceptionClass = exception_class;
  }

  private Object writeReplace() throws ObjectStreamException {
    // System.out.printf ("writeReplace %s in StatementThrowsException%n", this);
    return new SerializableExceptionObservation(exceptionClass);
  }

  public String toString() {
    return "// throws exception of type " + exceptionClass.getName() + Globals.lineSep;
  }

  /** Returns the class of the exception thrown**/
  public String get_value() {
    return exceptionClass.getName();
  }

  /**
   * The "try" half of the try-catch wrapper.
   */
  public String toCodeStringPreStatement() {
    StringBuilder b = new StringBuilder();
    b.append("// The following exception was thrown during execution." + Globals.lineSep);
    b.append("// This behavior will recorded for regression testing." + Globals.lineSep);
    b.append("try {" + Globals.lineSep + "  ");
    return b.toString();
  }

  /**
   * The "catch" half of the try-catch wrapper.
   */
  public String toCodeStringPostStatement() {
    StringBuilder b = new StringBuilder();
    String exceptionClassName = exceptionClass.getCanonicalName();
    b.append("  fail(\"Expected exception of type " + exceptionClassName + "\");" + Globals.lineSep);
    b.append("} catch (");
    b.append(exceptionClassName);
    b.append(" e) {" + Globals.lineSep);
    b.append("  // Expected exception." + Globals.lineSep);
    b.append("}" + Globals.lineSep);
    return b.toString();
  }


}
