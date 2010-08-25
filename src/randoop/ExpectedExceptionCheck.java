package randoop;

import java.io.ObjectStreamException;

import randoop.util.Reflection;

/**
 * A checker that checks for an expected exception from a method call.
 */
public class ExpectedExceptionCheck implements Check {

  private static final long serialVersionUID = -1172907532417774517L;

  private final Class<? extends Throwable> exceptionClass;
  
  // Indicates which statement results in the given exception. 
  private final int statementIdx;
  
  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    if (o == this) return true;
    if (!(o instanceof ExpectedExceptionCheck)) {
      return false;
    }
    ExpectedExceptionCheck other = (ExpectedExceptionCheck)o;
    return this.exceptionClass.equals(other.exceptionClass) && statementIdx == other.statementIdx;
  }
  
  @Override
  public int hashCode() {
    int h = 7;
    h = h * 31 + exceptionClass.hashCode();
    h = h * 31 + new Integer(statementIdx).hashCode();
    return h;
  }

  public ExpectedExceptionCheck(Throwable exception, int statementIdx) {
    if (exception == null)
      throw new IllegalArgumentException("exception cannot be null.");
    this.exceptionClass = exception.getClass();
    this.statementIdx = statementIdx;
  }

  public ExpectedExceptionCheck (Class<? extends Throwable> exception_class, int statementIdx) {
    this.exceptionClass = exception_class;
    this.statementIdx = statementIdx;
  }

  private Object writeReplace() throws ObjectStreamException {
    // System.out.printf ("writeReplace %s in StatementThrowsException%n", this);
    return new SerializableExpectedExceptionChecker(exceptionClass, statementIdx);
  }

  public String toString() {
    return "// throws exception of type " + exceptionClass.getName() + Globals.lineSep;
  }

  @Override
  public String get_id() {
    return "Throws exception @" + statementIdx;
  }

  /** Returns the class of the exception thrown**/
  public String get_value() {
    return exceptionClass.getName();
  }

  /** return the offset of this statement in the sequence **/
  public int get_stmt_no() {
    return statementIdx;
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

  @Override
  public boolean evaluate(Execution execution) {
    ExecutionOutcome outcomeAtIdx = execution.get(statementIdx);
    if (outcomeAtIdx instanceof NotExecuted) {
      throw new IllegalArgumentException("Statement not executed");
    }
    if (!(outcomeAtIdx instanceof ExceptionalExecution)) {
      return false;
    }
    ExceptionalExecution e = (ExceptionalExecution)outcomeAtIdx;
    return Reflection.canBeUsedAs(e.getException().getClass(), exceptionClass);
  }

}
