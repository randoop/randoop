package randoop;

import java.io.Serializable;

/**
 * A correctness check performed on some aspect of a unit test's execution.
 * Examples include assertions, calls to invariant or "checkRep" methods,
 * or try-catch clauses that check for expected exceptions.
 * 
 * <p>
 * 
 * <code>Check</code> objects are inserted as a decorations on specific
 * indices of an {@link ExecutableSequence}. Thus, a check is always associated
 * with a specific statement in a sequence.
 * 
 * <p>
 * 
 * A check may require some code to be emitted before and/or after the
 * statement is printed. For example, a checker that inserts the assertion "
 * <code>x != null</code>" for a statement "<code>Foo var0 = m()</code>" need
 * only emit the assertion code after the statement. A checker that surrounds
 * the statement with a try-catch clause to test for an expected exception needs
 * to emit the <i>try</i> part of the clause before the statement, and the
 * <i>catch</i> part after the statement (see {@link ExpectedExceptionCheck},
 * for example).
 *
 * <p>
 * 
 * A check must implement two methods that specify the code to be emitted
 * before and/or after a statement in a <code>Sequence</code> is executed.
 * 
 * <p>
 * 
 * Checks should be Serializable so that an ExecutableSequence can be
 * serialized along with its associated checks.
 * 
 */
public interface Check extends Serializable {

  /**
   * Returns a string of Java source code to be emitted before a
   * statement containing this check.
   */
  String toCodeStringPreStatement();

  /**
   * Returns a string of Java source code to be emitted after a
   * statement containing this check.
   */
  String toCodeStringPostStatement();

  /**
   * Returns a short string that can be used to uniquely identify
   * this check.
   */
  String get_value();
  
  /**
   * Evaluates this check on the given unfolding execution of a sequence,
   * returning <code>true</code> if the check succeeded, and <code>false</code>
   * otherwise.
   */
  boolean evaluate(Execution execution);
  
}
