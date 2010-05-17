package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * A checker that checks for an expected exception from a method call.
 */
public class ExpectedExceptionChecker implements Check, Serializable {

  private static final long serialVersionUID = 1L;

  private final Class<? extends Throwable> exceptionClass;
  
  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    if (o == this) return true;
    if (!(o instanceof ExpectedExceptionChecker)) {
      return false;
    }
    ExpectedExceptionChecker other = (ExpectedExceptionChecker)o;
    return this.exceptionClass.equals(other.exceptionClass);
  }
  
  @Override
  public int hashCode() {
    int h = 7;
    h = h * 31 + exceptionClass.hashCode();
    return h;
  }

  public ExpectedExceptionChecker(Throwable exception) {
    if (exception == null)
      throw new IllegalArgumentException("exception cannot be null.");
    this.exceptionClass = exception.getClass();
  }

  public ExpectedExceptionChecker (Class<? extends Throwable> exception_class) {
    this.exceptionClass = exception_class;
  }

  private Object writeReplace() throws ObjectStreamException {
    // System.out.printf ("writeReplace %s in StatementThrowsException%n", this);
    return new SerializableExpectedExceptionChecker(exceptionClass);
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
