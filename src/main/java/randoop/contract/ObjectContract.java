package randoop.contract;

// NOTE: This is a publicized user extension point. If you add any
// methods, document them well and update the Randoop manual.

import randoop.types.TypeTuple;

/**
 * An object contract represents a property that must hold of any object of a given class. It is
 * used as part of the oracle (assertion) for a unit test: the oracle expects that every object
 * contract holds. Any sequence of method calls that leads to a failing contract is outputted by
 * Randoop as an error-revealing test case.
 *
 * <p>Implementing classes provide two key pieces functionality:
 *
 * <ul>
 *   <li>A method <code>{@link #evaluate}(Object... objects)</code> that determines if the given
 *       object(s) satisfy the property.
 *   <li>A method <code>{@link #toCodeString}()</code> that emits Java code that can be inserted
 *       into a unit test to check for the given property.
 * </ul>
 *
 * <p>See the various implementing classes for examples (for an example, see {@link
 * EqualsReflexive}).
 */
public interface ObjectContract {

  /**
   * The number of values that this contract is over.
   *
   * @return the number of arguments to the contract
   */
  int getArity();

  /**
   * Returns the input types for this contract.
   *
   * @return the input types for this contract
   */
  TypeTuple getInputTypes();

  /**
   * Evaluates the contract on the given values.
   *
   * <p>When calling this method during execution of a test, Randoop guarantees that <code>objects
   * </code> does not contain any <code>null</code> objects, and that <code>
   * objects.length == getArity()</code>.
   *
   * <p>This method should return <code>true</code> if the contract was satisfied and <code>false
   * </code> if it was violated.
   *
   * @param objects the actual parameters to this contract
   * @return true if this contract evaluates to true for the given values, and false otherwise
   * @throws Throwable if an exception is thrown in evaluation
   */
  boolean evaluate(Object... objects) throws Throwable;

  /**
   * Communicates to Randoop how to interpret exceptional behavior from the <code>evaluate</code>
   * method.
   *
   * <p>If this method returns <code>true</code>, Randoop will interpret an exception that escapes
   * during evaluation as a failure of the contract.
   *
   * <p>If the method returns <code>false</code>, Randoop will interpret an exception as passing
   * behavior.
   *
   * @return true if an exception in evaluating this contract should be interpreted as a failure,
   *     false otherwise
   */
  boolean evalExceptionMeansFailure();

  /**
   * A string that will be inserted as a comment in the test before the code corresponding to this
   * contract. Occurrences of variables x0, x1, x2, etc. in the string will be replaced by actual
   * values.
   *
   * @return the comment string representation of this contract
   */
  String toCommentString();

  /**
   * A string that can be used as Java source code and will result in the expression being
   * evaluated.
   *
   * <p>The string should be formatted as follows: the N-th object that participates in the contract
   * check should be referred to as "xN" (for N one of 0, ... , 9). For example, if the expression
   * of arity 2 represents a call of the equals method between two objects, the comment should be
   * something like "x0.equals(x1)".
   *
   * @return the code string representation of this contract; must be non-null
   */
  String toCodeString();

  /**
   * Returns a string describing the observer.
   *
   * @return a string description of the contract
   */
  String get_observer_str();
}
