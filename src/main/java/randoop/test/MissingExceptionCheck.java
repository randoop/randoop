package randoop.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import plume.UtilMDE;
import randoop.Globals;
import randoop.condition.ThrowsClause;

/**
 * Represents the fact that a statement should throw an exception, but did not. It is used in an
 * error-revealing test to indicate that normal execution of the statement violates the stated
 * throws-condition of the method/constructor.
 */
public class MissingExceptionCheck implements Check {

  private final List<List<ThrowsClause>> expected;

  /** The index of the statement where the exception should be thrown */
  private final int index;

  /**
   * Creates a {@link MissingExceptionCheck} object for the expected exception type at the given
   * statement.
   *
   * @param expected the expected exceptions
   * @param index the statement index
   */
  MissingExceptionCheck(List<List<ThrowsClause>> expected, int index) {
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
    StringJoiner result = new StringJoiner(Globals.lineSep);
    result.add("MissingExceptionCheck at index " + index);
    for (List<ThrowsClause> set : expected) {
      result.add(set.toString());
    }
    result.add(""); // get final newline
    return result.toString();
  }

  @Override
  public String toCodeStringPreStatement() {
    StringBuilder msg = new StringBuilder(String.format("// this statement should throw one of%n"));
    for (List<ThrowsClause> exceptionSet : expected) {
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
    for (List<ThrowsClause> set : expected) {
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
}
