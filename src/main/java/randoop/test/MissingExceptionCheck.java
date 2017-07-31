package randoop.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import plume.UtilMDE;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.Globals;
import randoop.NotExecuted;
import randoop.condition.ThrowsClause;
import randoop.sequence.Execution;
import randoop.types.ClassOrInterfaceType;

/**
 * Represents the fact that a statement should throw an exception, but did not. It is used in an
 * error-revealing test to indicate that normal execution of the statement violates the stated
 * throws-condition of the method/constructor.
 */
public class MissingExceptionCheck implements Check {

  /** The type of the expected exception */
  private final List<Set<ThrowsClause>> expected;

  /** The index of the statement where the exception should be thrown */
  private final int index;

  /**
   * Creates a {@link MissingExceptionCheck} object for the expected exception type at the given
   * statement.
   *
   * @param expected the expected exceptions
   * @param index the statement index
   */
  MissingExceptionCheck(List<Set<ThrowsClause>> expected, int index) {
    this.expected = expected;
    this.index = index;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MissingExceptionCheck)) {
      return false;
    }
    MissingExceptionCheck other = (MissingExceptionCheck) obj;
    return this.expected.equals(other.expected) && this.index == other.index;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.expected, this.index);
  }

  @Override
  public String toString() {
    StringBuilder result =
        new StringBuilder("MissingExceptionCheck at line " + index + Globals.lineSep);
    for (Set<ThrowsClause> set : expected) {
      result.append(set.toString()).append(Globals.lineSep);
    }
    return result.toString();
  }

  @Override
  public String toCodeStringPreStatement() {
    StringBuilder msg = new StringBuilder(String.format("// this statement should throw one of%n"));
    for (Set<ThrowsClause> exceptionSet : expected) {
      for (ThrowsClause exception : exceptionSet) {
        msg.append(
            String.format(
                "//   %s %s%n", exception.getExceptionType().getName(), exception.getComment()));
      }
    }
    return msg.toString();
  }

  @Override
  public String toCodeStringPostStatement() {
    List<String> exceptionNameList = new ArrayList<>();
    for (Set<ThrowsClause> set : expected) {
      List<String> expectedNames = new ArrayList<>();
      for (ThrowsClause exception : set) {
        expectedNames.add(exception.getExceptionType().getName());
      }
      exceptionNameList.add("\"[ " + UtilMDE.join(expectedNames, ", ") + " ]\"");
    }
    return "org.junit.Assert.fail(\"exception is expected: \" + "
        + UtilMDE.join(exceptionNameList, " + ")
        + ");";
  }

  @Override
  public String getValue() {
    return "missing_exception";
  }

  @Override
  public String getID() {
    return "MissingExceptionCheck @" + index;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Checks that an exception of the expected type is thrown by the statement in this object in
   * the given {@link Execution}.
   *
   * @return true if the statement throws the expected exception, false otherwise
   */
  @Override
  public boolean evaluate(Execution execution) {
    ExecutionOutcome outcomeAtIndex = execution.get(index);
    if (outcomeAtIndex instanceof NotExecuted) {
      throw new IllegalArgumentException("Statement not executed");
    }
    if (!(outcomeAtIndex instanceof ExceptionalExecution)) {
      return false;
    }
    ExceptionalExecution exec = (ExceptionalExecution) outcomeAtIndex;
    Throwable t = exec.getException();
    ClassOrInterfaceType thrownType = ClassOrInterfaceType.forClass(t.getClass());
    for (Set<ThrowsClause> exceptionSet : expected) {
      for (ThrowsClause exception : exceptionSet) {
        if (!thrownType.isSubtypeOf(exception.getExceptionType())) {
          return false;
        }
      }
    }
    return true;
  }
}
