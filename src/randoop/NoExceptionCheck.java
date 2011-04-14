package randoop;

/**
 * Checks that no exception is thrown after a given statement
 * in a sequence.
 */
public class NoExceptionCheck implements Check {
  
  private static final long serialVersionUID = 6915136819752903798L;
  
  // Indicates which statement is expected to return normally. 
  private final int statementIdx;
  
  public NoExceptionCheck(int statementIdx) {
    this.statementIdx = statementIdx;
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
  public String get_value() {
    return "no_exception";
  }

  @Override
  public int get_stmt_no() {
    return statementIdx;
  }

  @Override
  public String get_id() {
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
    return "";
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
