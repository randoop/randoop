package randoop;

import java.io.ObjectStreamException;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * An {@code ExceptionCheck} is used to indicate that an exception is expected
 * at a particular statement in a sequence. Depending on command line arguments
 * to Randoop, an instance may be either a {@link ExpectedExceptionCheck} or 
 * {@link EmptyExceptionCheck}. Both will catch the exception if it occurs, but
 * differ on whether the expectation of the exception is enforced.
 */
public abstract class ExceptionCheck implements Check {
  
 /**
   * 
   */
  private static final long serialVersionUID = 4806179088639914364L;

  protected final Throwable exception;
  
  // Indicates which statement results in the given exception. 
  protected final int statementIndex;

  public ExceptionCheck(Throwable exception, int statementIndex) {
    this.exception = exception;
    this.statementIndex = statementIndex;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    if (o == this) return true;
    if (this.getClass() != o.getClass()) { //match implementing class
      return false;
    }
    ExceptionCheck other = (ExceptionCheck)o;
    return this.exception.equals(other.exception) 
        && statementIndex == other.statementIndex;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(exception,statementIndex);
  }
  
  @Override
  public String toString() {
    return "// throws exception of type " + exception.getClass().getCanonicalName() + Globals.lineSep;
  }

  /** Returns the class of the exception thrown**/
  @Override
  public String get_value() {
    return exception.getClass().getName();
  }

  @Override
  public String get_id() {
    return "Throws exception @" + statementIndex;
  }

  /** return the offset of this statement in the sequence **/
  @Override
  public int get_stmt_no() {
    return statementIndex;
  }

  /**
   * The "try" half of the try-catch wrapper.
   */
  @Override
  public String toCodeStringPreStatement() {
    StringBuilder b = new StringBuilder();
    b.append("// The following exception was thrown during execution." + Globals.lineSep);
    b.append("// This behavior will recorded for regression testing." + Globals.lineSep);
    b.append("try {" + Globals.lineSep + "  ");
    return b.toString();
  }
  
  /** The nearest visible superclass -- usually the argument itself. */
  // TODO: handle general visibility based on Randoop command-line
  // arguments, rather than hard-coding isPublic test.
  public static Class<?> nearestVisibleSuperclass(Class<?> clazz) {
    while (! Modifier.isPublic(clazz.getModifiers())) {
      clazz = clazz.getSuperclass();
    }
    return clazz;
  }

  /**
   * Returns the "catch" half of the try-catch wrapper.
   * Catches the closest public superclass of the exception.
   * Calls {@link this#appendCatchBehavior(StringBuilder, String)} to determine
   * what code to include in the catch block.
   */
  @Override
  public String toCodeStringPostStatement() {
    StringBuilder b = new StringBuilder();
    String exceptionClassName = getExceptionName();
    if (exceptionClassName == null) {
      exceptionClassName = "Exception";
    }
    appendTryBehavior(b, exceptionClassName);
    if (Modifier.isPublic(exception.getClass().getModifiers())) {
      b.append("} catch (" + exceptionClassName + " e) {" + Globals.lineSep);
      b.append("  // Expected exception." + Globals.lineSep);
      b.append("}" + Globals.lineSep);
    } else {
      // The exception type is private.  Catch the nearest public supertype.
      Class<?> publicSuperClass = nearestVisibleSuperclass(exception.getClass());
      String publicSuperClassName = publicSuperClass.getCanonicalName();
      b.append("} catch (" + publicSuperClassName + " e) {" + Globals.lineSep);
      b.append("  // Expected exception." + Globals.lineSep);
      appendCatchBehavior(b, exceptionClassName);
      b.append("}" + Globals.lineSep);
    }      
    return b.toString();
  }
  
  /**
   * Appends code for catch block behavior corresponding to expected exception.
   * 
   * @param b  the string builder to which code text is to be added
   * @param exceptionClassName  the class name of the expected exception
   */
  protected abstract void appendCatchBehavior(StringBuilder b, String exceptionClassName);

  /**
   * Appends code to follow statement throwing expected exception in try block.
   *   
   * @param b  the string builder to which code text is added
   * @param exceptionClassName  the class name of the expected exception
   */
  protected abstract void appendTryBehavior(StringBuilder b, String exceptionClassName);
  
  public String getExceptionName() {
    return exception.getClass().getCanonicalName();
  }

  public Throwable getException() {
    return exception;
  }
}
