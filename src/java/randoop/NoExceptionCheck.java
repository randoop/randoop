package randoop;

import randoop.sequence.Execution;

/**
 * This check represents the fact that a statement should not throw an
 * exception. It is used in an error-revealing test to indicate that an
 * exception that is considered to be an error (e.g., not "expected" or
 * "invalid") was thrown by the statement during test generation. Only a comment
 * is included when the test is output noting the occurrence of the exception
 * during test generation, though the statement is expected to throw the method
 * when the error-revealing test is run.
 */
public class NoExceptionCheck implements Check {

  private static final long serialVersionUID = 6915136819752903798L;

  // Indicates which statement is expected to return normally.
  private final int statementIdx;
  private String exceptionName;

  public NoExceptionCheck(int statementIdx, String exceptionName) {
    this.statementIdx = statementIdx;
    this.exceptionName = exceptionName;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (o == this)
      return true;
    if (!(o instanceof NoExceptionCheck))
      return false;
    NoExceptionCheck other = (NoExceptionCheck) o;
    return statementIdx == other.statementIdx;
  }

  @Override
  public int hashCode() {
    return new Integer(statementIdx).hashCode();
  }

  @Override
  public String getValue() {
    return "no_exception";
  }

  @Override
  public int getStatementIndex() {
    return statementIdx;
  }

  @Override
  public String getID() {
    return "NoExceptionCheck @" + statementIdx;
  }

  /**
   * Returns the empty string: there is no code associated with this check (if
   * an exception occurs, it will be reported by JUnit).
   */
  @Override
  public String toCodeStringPostStatement() {
    return "";
  }

  @Override
  public String toCodeStringPreStatement() {
    return "// during test generation this statement threw an exception of type " + exceptionName + " in error" + Globals.lineSep;
  }

  /**
   * {@inheritDoc}
   * 
   * @return true when no exception is observed, false when one is
   */
  @Override
  public boolean evaluate(Execution execution) {
    ExecutionOutcome outcomeAtIdx = execution.get(statementIdx);
    if (outcomeAtIdx instanceof NotExecuted) {
      throw new IllegalArgumentException("Statement not executed");
    }
    return outcomeAtIdx instanceof NormalExecution;
  }
}
