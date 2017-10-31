package randoop.test;

import randoop.Globals;

/**
 * This check represents the fact that a statement should not throw any exception.
 *
 * <p>It is used in an error-revealing test to indicate that an exception that is considered to be
 * an error (e.g., not "expected" or "invalid") was thrown by the statement during test generation.
 * Only a comment is included when the test is output noting the occurrence of the exception during
 * test generation, though the statement is expected to throw the method when the error-revealing
 * test is run.
 */
public class NoExceptionCheck implements Check {

  /** Indicates which statement is expected to return normally. */
  private final int statementIdx;
  /**
   * The exception that the statement threw during generation. Used only in a comment in the
   * generated code -- the contract of NoExceptionCheck is that no exception should be thrown,
   * whether or not it's the same as what was observed during generation.
   */
  private String exceptionName;

  NoExceptionCheck(int statementIdx, String exceptionName) {
    this.statementIdx = statementIdx;
    this.exceptionName = exceptionName;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof NoExceptionCheck)) {
      return false;
    }
    NoExceptionCheck other = (NoExceptionCheck) o;
    return statementIdx == other.statementIdx;
  }

  @Override
  public int hashCode() {
    return Integer.valueOf(statementIdx).hashCode();
  }

  /**
   * Returns the empty string: there is no code associated with this check (if an exception occurs,
   * it will be reported by JUnit).
   */
  @Override
  public String toCodeStringPostStatement() {
    return "";
  }

  @Override
  public String toCodeStringPreStatement() {
    return "// during test generation this statement threw an exception of type "
        + exceptionName
        + " in error"
        + Globals.lineSep;
  }
}
