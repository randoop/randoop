package randoop;

import randoop.sequence.Execution;

/**
 * Checks that an exception is not thrown by a given statement in a sequence. 
 * This is meant for an error-revealing check where, in fact, an exception
 * was observed at the statement, and this check is meant to represent that
 * the throws is actually an expected failure.
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
    if (o == null) return false;
    if (o == this) return true;
    if (!(o instanceof NoExceptionCheck)) return false;
    NoExceptionCheck other = (NoExceptionCheck)o;
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
   * Returns the empty string: there is no code associated
   * with this check (if an exception occurs, it will
   * be reported by JUnit). 
   */
  @Override
  public String toCodeStringPostStatement() {
    return "";
  }

  @Override
  public String toCodeStringPreStatement() {
    return "// contract failure: this statement threw " + exceptionName + Globals.lineSep;
  }

  @Override
  public boolean evaluate(Execution execution) {
    ExecutionOutcome outcomeAtIdx = execution.get(statementIdx);
    if (outcomeAtIdx instanceof NotExecuted) {
      throw new IllegalArgumentException("Statement not executed");
    }
    return outcomeAtIdx instanceof NormalExecution;
  }
}
