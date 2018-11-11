package randoop.contract;

// NOTE: This is a publicized user extension point. If you add any
// methods, document them well and update the Randoop manual.

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.NotExecuted;
import randoop.main.ExceptionBehaviorClassifier;
import randoop.main.GenInputsAbstract.BehaviorType;
import randoop.main.RandoopBug;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Variable;
import randoop.test.Check;
import randoop.test.InvalidExceptionCheck;
import randoop.test.ObjectCheck;
import randoop.types.TypeTuple;
import randoop.util.Log;
import randoop.util.TimeoutExceededException;

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
   * Evaluates the contract on the given values. Returns {@code false} if the contract was violated.
   * Returns {@code true} if the contract was satisfied or was not applicable.
   *
   * <p>When calling this method during execution of a test, Randoop guarantees that {@code objects}
   * does not contain any {@code null} objects, and that {@code objects.length == getArity()}.
   *
   * @param objects the actual parameters to this contract
   * @return false if the contract is violated, true otherwise
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
   * @return a {@link ObjectCheck} if the contract fails, an {@link InvalidExceptionCheck} if the
   *     contract throws an exception indicating that the sequence is invalid, null otherwise
   */
  public final Check checkContract(ExecutableSequence eseq, Object[] values) {

    ExecutionOutcome outcome = ObjectContractUtils.execute(this, values);

    if (Log.isLoggingOn()) {
      // Commented out because it makes the logs too big.  Uncomment when debugging this code.
      // Log.logPrintf("Executed contract %s%n", this.getClass());
      // Log.logPrintf("  values (length %d) =%n", values.length);
      // for (Object value : values) {
      //   Log.logPrintf(
      //       "  %s @%s%n", toStringHandleExceptions(value), System.identityHashCode(value));
      // }
      // Log.logPrintf("  Contract outcome = %s%n", outcome);
    }

    if (outcome instanceof NormalExecution) {
      boolean result = ((Boolean) ((NormalExecution) outcome).getRuntimeValue()).booleanValue();
      if (result) {
        return null;
      } else {
        return failedContract(eseq, values);
      }
    } else if (outcome instanceof ExceptionalExecution) {
      Throwable e = ((ExceptionalExecution) outcome).getException();
      Log.logPrintf(
          "checkContract(): Contract %s threw exception of class %s with message %s%n",
          this, e.getClass(), e.getMessage());
      if (e instanceof RandoopBug) {
        throw (RandoopBug) e;
      }
      if (e instanceof TimeoutExceededException) {
        // The index and name won't get used, but set them anyway.
        return new InvalidExceptionCheck(e, eseq.size() - 1, e.getClass().getName());
      }

      BehaviorType eseqBehavior = ExceptionBehaviorClassifier.classify(e, eseq);
      Log.logPrintf("  ExceptionBehaviorClassifier.classify(e, eseq) => %s%n", eseqBehavior);

      if (eseqBehavior == BehaviorType.EXPECTED) {
        eseqBehavior = BehaviorType.INVALID;
      }

      switch (eseqBehavior) {
        case ERROR:
          return failedContract(eseq, values);
        case EXPECTED:
          // ***** I'm not really sure what this should return. *****

          // The goal is to make the expected behavior of the contract check be a thrown exception.
          // (That's somewhat weird behavior!  Do I want to even support it?)
          // In general a contract should not throw an exception, but the contract might call a
          // method that throws an exception; for example, the method might throw
          // NullPointerException or ConcurrentModificationException.

          // This is wrong:  it attaches an expected exeption to the method call that
          // created the object, not to the contract check that comes afterward.
          // return new ExpectedExceptionCheck(
          //     e, eseq.size(), ExpectedExceptionCheckGen.getCatchClassName(e.getClass()));

          // Possible solutions:
          //  * Create a new type of ObjectCheck with an expected exception.
          //  * Don't support the weird use case, and treat this like INVALID instead.
          //    For now, do this.

          throw new Error("unreachable (for now)");

        case INVALID:
          // The index and name won't get used, but set them anyway.
          return new InvalidExceptionCheck(e, eseq.size() - 1, e.getClass().getName());
        default:
          throw new Error("unreachable");
      }

    } else {
      assert outcome instanceof NotExecuted;
      throw new RandoopBug("Contract " + this + " failed to execute during evaluation");
    }
  }

  /**
   * Return an ObjectCheck indicating that a contract failed.
   *
   * @param eseq the sequence for which a contract failed
   * @param values the input values
   * @return an ObjectCheck indicating that a contract failed
   */
  ObjectCheck failedContract(ExecutableSequence eseq, Object[] values) {
    Variable[] varArray = new Variable[values.length];
    for (int i = 0; i < varArray.length; i++) {
      varArray[i] = eseq.getVariable(values[i]);
      // Note: the following alternative to the above line slightly improves coverage
      // varArray[i] = Randomness.randomMember(eseq.getVariables(values[i]));

      //   Log.logPrintf(
      //       "values[%d] = %s @%s%n",
      //       i, toStringHandleExceptions(values[i]), System.identityHashCode(values[i]));
      //   Log.logPrintf("  candidate variables = %s%n", variables);
      //   Log.logPrintf(
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
