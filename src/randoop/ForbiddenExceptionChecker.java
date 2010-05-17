package randoop;

import java.io.ObjectStreamException;
import java.io.Serializable;

import randoop.util.Reflection;

/**
 * A checker that checks that a forbidden exception was not thrown.
 */
public class ForbiddenExceptionChecker implements Check, Serializable {

  private static final long serialVersionUID = -5315922475732874865L;

  private final Class<? extends Throwable> exceptionClass;

  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (o == this)
      return true;
    if (!(o instanceof ForbiddenExceptionChecker)) {
      return false;
    }
    ForbiddenExceptionChecker other = (ForbiddenExceptionChecker) o;
    return this.exceptionClass.equals(other.exceptionClass);
  }

  @Override
  public int hashCode() {
    int h = 7;
    h = h * 31 + exceptionClass.hashCode();
    return h;
  }

  public ForbiddenExceptionChecker(Throwable exception) {
    if (exception == null)
      throw new IllegalArgumentException("exception cannot be null.");
    this.exceptionClass = exception.getClass();
  }
  
  public ForbiddenExceptionChecker (Class<? extends Throwable> exception_class) {
    this.exceptionClass = exception_class;
  }

  private Object writeReplace() throws ObjectStreamException {
    // System.out.printf ("writeReplace %s in StatementThrowsException%n", this);
    return new SerializableForbiddenExceptionChecker(exceptionClass);
  }

  public String toString() {
    return "// throws " + get_value();
  }

  /** The 'value' of this exception is always NPE **/
  public String get_value() {
    if (exceptionClass.equals(NullPointerException.class)) {
      return "NPE"; // backwards-compatibility with Jeff's code.
    } else {
      return exceptionClass.getName();
    }
  }

  /**
   * The "try" half of the try-catch wrapper.
   */
  public String toCodeStringPreStatement() {
    StringBuilder b = new StringBuilder();
    String className = Reflection.getCompilableName(exceptionClass);
    b.append("// Checks that no " + className + " is thrown." + Globals.lineSep);
    b.append("try {" + Globals.lineSep + "  ");
    return b.toString();
  }

  /**
   * The "catch" half of the try-catch wrapper.
   */
  public String toCodeStringPostStatement() {
    StringBuilder b = new StringBuilder();
    String className = Reflection.getCompilableName(exceptionClass);
    b.append("} catch (" + className + " e) {" + Globals.lineSep);
    b.append("  fail(\"Statement throw " + className + ".\");" + Globals.lineSep);
    b.append("}" + Globals.lineSep);
    return b.toString();
  }
}
