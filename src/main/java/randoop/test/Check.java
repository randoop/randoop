package randoop.test;

import randoop.sequence.ExecutableSequence;

// A better name for Check would be SequenceDecoration.

/**
 * A Check represents the expected runtime behavior of a Sequence, at a particular offset. For
 * example, a client might add a {@code NotNull} check to the <em>i</em>th index of a sequence to
 * signify that the value returned by the statement at index <em>i</em> should not be null.
 *
 * <p>When a unit test is run as a regression test, it should have the same behavior as it did
 * previously, and the Check objects represent that previous, expected behavior. Some examples of
 * Checks are:
 *
 * <ul>
 *   <li>{@link ExpectedExceptionCheck} -- an exception should be thrown
 *   <li>{@link NoExceptionCheck} -- no exception should be thrown
 *   <li>{@link ObjectCheck} -- a particular {@link randoop.contract.ObjectContract} should hold
 * </ul>
 *
 * The visitor classes decorate a sequence with {@code Check} objects. A {@code Check} object is
 * inserted as a decoration on a specific index of an {@link ExecutableSequence}. Thus, a check is
 * always associated with a specific index in a sequence. A check at index i means that the check is
 * to be performed after statement i finishes executing.
 *
 * <p>A check implements two methods that specify the code to be emitted before and/or after a
 * statement in a {@code Sequence} is executed.
 *
 * <p>A check may require some code to be emitted before and/or after the statement is printed. For
 * example, a check for checking that {@code x} is not null after the statement "{@code Foo x =
 * m()}" is executed might emit the assertion code "{@code assertNotNull(x); }", and would do so
 * after the statement is printed. As a second example, a check for checking that an expected
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
}
