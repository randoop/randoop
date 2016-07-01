package randoop.operation;

import java.io.PrintStream;
import java.util.List;

import randoop.ExecutionOutcome;
import randoop.reflection.ReflectionPredicate;
import randoop.sequence.Variable;
import randoop.types.GeneralType;
import randoop.types.TypeTuple;

/**
 * CallableOperation is an abstract implementation of the Operation interface to
 * provide default implementations of Operation predicates that are false except
 * for a few kinds of operations.
 *
 */
public abstract class CallableOperation implements Operation {

  @Override
  public boolean isStatic() {
    return false;
  }

  @Override
  public boolean isMessage() {
    return false;
  }

  @Override
  public boolean isMethodCall() {
    return false;
  }

  @Override
  public boolean isConstructorCall() {
    return false;
  }

  @Override
  public boolean isNonreceivingValue() {
    return false;
  }

  @Override
  public Object getValue() {
    throw new IllegalArgumentException("No value for this kind of operation.");
  }

  /**
   * Compares this object to an {@link Operation} object.
   * Uses order on result of {@link #toString()}.
   *
   * @param op
   *          the {@link Operation} to compare with this operation
   * @return value &lt; 0 if this parsable string is less than for op, 0 if the
   *         strings are equal, and &gt; 0 if string for this object greater
   *         than for op
   */
  @Override
  public int compareTo(Operation op) {
    return (this.toString()).compareTo(op.toString());
  }

  /**
   * Checks whether reflective object contained in an {@link Operation}
   * satisfies the predicate. Since there is no reflective object in an
   * {@code CallableOperation}, returns false.
   *
   * @param predicate
   *          {@link ReflectionPredicate} against which object to be checked.
   * @return false as there is no object to check
   */
  @Override
  public boolean satisfies(ReflectionPredicate predicate) {
    return false;
  }

  /**
   * Performs this operation using the array of input values. Returns
   * the results of execution as an ResultOrException object and can
   * output results to specified PrintStream.
   * @param input array containing appropriate inputs to operation
   * @param out stream to output results of execution;
   *            can be null if you don't want to print
   * @return results of executing this statement
   */
  public abstract ExecutionOutcome execute(Object[] input, PrintStream out);

  /**
   * Produces a Java source code representation of this statement and appends it
   * to the given StringBuilder.
   *
   * @param declaringType  the declaring type for this operation
   * @param inputTypes  the input types for this operation
   * @param outputType  the output type for this operation
   * @param inputVars  the list of variables that are inputs to operation
   * @param b  the {@link StringBuilder} to which code is added
   */
  public abstract void appendCode(
      GeneralType declaringType,
      TypeTuple inputTypes,
      GeneralType outputType,
      List<Variable> inputVars,
      StringBuilder b);

  /**
   * Returns a string representation of this Operation, which can be read by
   * static parse method for class.
   * For a class C implementing the Operation interface, this method should
   * return a String s such that parsing the string
   * returns an object equivalent to this object, i.e. C.parse(this.s).equals(this).
   *
   * @param declaringType  the declaring type for this operation
   * @param inputTypes  the input types for this operation
   * @param outputType  the output type for this operation
   * @return a string representation of this operation
   */
  public abstract String toParsableString(
      GeneralType declaringType, TypeTuple inputTypes, GeneralType outputType);
}
