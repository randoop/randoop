package randoop.test;

import randoop.sequence.ExecutableSequence;
import randoop.sequence.Execution;

// A better name for Check would be SequenceDecoration.

/**
 * A Check represents the expected runtime behavior of a Sequence, at a particular offset. When a
 * unit test is run as a regression test, it should have the same behavior as it did previously, and
 * the Check objects represent that previous, expected behavior. Some examples of Checks are:
 *
 * <ul>
 *   <li>{@link ExpectedExceptionCheck} -- an exception should be thrown
 *   <li>{@link NoExceptionCheck} -- no exception should be thrown
 *   <li>{@link ObjectCheck} -- a particular {@link randoop.contract.ObjectContract} should hold
 * </ul>
 *
 * The visitor classes decorate a sequence with <code>Check</code> objects. A <code>Check</code>
 * object is inserted as a decoration on a specific index of an {@link ExecutableSequence}. Thus, a
 * check is always associated with a specific index in a sequence. A check at index i means that the
 * check is to be performed after statement i finishes executing.
 *
 * <p>A check implements two methods that specify the code to be emitted before and/or after a
 * statement in a <code>Sequence</code> is executed.
 *
 * <p>A check may require some code to be emitted before and/or after the statement is printed. For
 * example, a check for checking that <code>x</code> is not null after the statement "<code>Foo x =
 * m()</code>" is executed might emit the assertion code "<code>assertNotNull(x);</code>", and would
 * do so after the statement is printed. As a second example, a check for checking that an expected
 * exception is thrown by a statement would need to emit something like "<code>try {</code>" before
 * the statement, and the catch clause after the statement.
 */
public interface Check {

  /**
   * Returns a string of Java source code to be emitted before a statement containing this check.
   *
   * @return the string to be included before the statement
   */
  String toCodeStringPreStatement();

  /**
   * Returns a string of Java source code to be emitted after a statement containing this check.
   *
   * @return the string to be included following the statement
   */
  String toCodeStringPostStatement();

  /**
   * Returns a short string that can be used to uniquely identify this check.
   *
   * @return a string "value" for this check
   */
  String getValue();

  /**
   * Returns a unique string identifier for the check. Two checks are the same if these identifiers
   * match. The value is NOT included. Used to match up checks between the same sequence run at
   * different times. Note that because of changes in the environment (e.g., static variables), two
   * executions of the same sequence may have different checks (because the existence of some checks
   * depends on the value of variables)
   *
   * @return a unique string identifier for this check
   */
  String getID();

  /**
   * Evaluates this check on the given execution of a sequence.
   *
   * @param execution the execution of sequence on which to test this check
   * @return true if check succeeded, and false otherwise
   */
  boolean evaluate(Execution execution);
}
