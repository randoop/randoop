package randoop.contract;

// NOTE: This is a publicized user extension point. If you add any
// methods, document them well and update the Randoop manual.

import java.util.List;
import randoop.BugInRandoopException;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.NotExecuted;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Variable;
import randoop.test.Check;
import randoop.test.ObjectCheck;
import randoop.types.TypeTuple;
import randoop.util.Log;
import randoop.util.Randomness;

/**
 * An object contract represents a property that must hold of any object of a given class. It is
 * used as part of the oracle (assertion) for a unit test: the oracle expects that every object
 * contract holds. Any sequence of method calls that leads to a failing contract is outputted by
 * Randoop as an error-revealing test case.
 *
 * <p>Implementing classes provide two key pieces functionality:
 *
 * <ul>
 *   <li>A method {@link #evaluate}{@code (Object... objects)} that determines if the given
 *       object(s) satisfy the property.
 *   <li>A method {@link #toCodeString}{@code ()} that emits Java code that can be inserted into a
 *       unit test to check for the given property.
 * </ul>
 *
 * <p>See the various implementing classes for examples (for an example, see {@link
 * EqualsReflexive}).
 */
public abstract class ObjectContract {

  /**
   * The number of values that this contract is over.
   *
   * @return the number of arguments to the contract
   */
  public abstract int getArity();

  /**
   * Returns the input types for this contract.
   *
   * @return the input types for this contract
   */
  public abstract TypeTuple getInputTypes();

  /**
   * Evaluates the contract on the given values.
   *
   * <p>When calling this method during execution of a test, Randoop guarantees that {@code objects}
   * does not contain any {@code null} objects, and that {@code objects.length == getArity()}.
   *
   * <p>This method should return {@code true} if the contract was satisfied and {@code false} if it
   * was violated.
   *
   * @param objects the actual parameters to this contract
   * @return true if this contract evaluates to true for the given values, and false otherwise
   * @throws Throwable if an exception is thrown in evaluation
   */
  public abstract boolean evaluate(Object... objects) throws Throwable;

  /**
   * A string that will be inserted as a comment in the test before the code corresponding to this
   * contract. Occurrences of variables x0, x1, x2, etc. in the string will be replaced by actual
   * values.
   *
   * @return the comment string representation of this contract
   */
  public abstract String toCommentString();

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
  public abstract String toCodeString();

  // TODO: how is this different than toString, in terms of contract and in
  // terms of intended use?
  /**
   * Returns a string describing the observer.
   *
   * @return a string description of the contract
   */
  public abstract String get_observer_str();

  /**
   * Checks a contract on a particular array of values.
   *
   * @param eseq the executable sequence that is the source of values for checking contracts
   * @param values the input values
   * @return a {@link ObjectCheck} if the contract fails, null otherwise
   */
  public final Check checkContract(ExecutableSequence eseq, Object[] values) {

    ExecutionOutcome outcome = ObjectContractUtils.execute(this, values);

    if (Log.isLoggingOn()) {
      Log.logLine("Executed contract " + this.getClass());
      //   Log.logLine("  values (length %d) =%n", values.length);
      //   for (Object value : values) {
      //     Log.logLine(
      //         "  %s @%s%n", toStringHandleExceptions(value), System.identityHashCode(value));
      Log.logLine(" Contract outcome " + outcome);
    }

    if (outcome instanceof NormalExecution) {
      if (((NormalExecution) outcome).getRuntimeValue().equals(true)) {
        return null;
      }
    } else if (outcome instanceof ExceptionalExecution) {
      Throwable e = ((ExceptionalExecution) outcome).getException();
      if (Log.isLoggingOn()) {
        Log.logLine(
            String.format(
                "checkContract(): Contract %s threw exception of class %s with message %s",
                this, e.getClass(), e.getMessage()));
      }
      if (e instanceof BugInRandoopException) {
        throw (BugInRandoopException) e;
      }
      // ***** TODO: determine what the exception is
    } else {
      assert outcome instanceof NotExecuted;
      throw new BugInRandoopException("Contract " + this + " failed to execute during evaluation");
    }

    // the contract failed
    Variable[] varArray = new Variable[values.length];
    for (int i = 0; i < varArray.length; i++) {
      List<Variable> variables = eseq.getVariables(values[i]);
      varArray[i] = Randomness.randomMember(variables);
      //   Log.logLine(
      //       "values[%d] = %s @%s%n",
      //       i, toStringHandleExceptions(values[i]), System.identityHashCode(values[i]));
      //   Log.logLine("  candidate variables = %s%n", variables);
      //   Log.logLine(
      //       "  varArray[%d] = %s @%s%n", i, varArray[i], System.identityHashCode(varArray[i]));
    }

    return new ObjectCheck(this, varArray);
  }

  // The toString() of class Buggy throws an exception.
  static String toStringHandleExceptions(Object o) {
    try {
      return o.toString();
    } catch (Throwable t) {
      return "of " + o.getClass() + " with identityHashCode=@" + System.identityHashCode(o);
    }
  }
}
