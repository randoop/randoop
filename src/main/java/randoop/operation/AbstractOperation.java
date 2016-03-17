package randoop.operation;

import randoop.reflection.ReflectionPredicate;

/**
 * AbstractOperation is an abstract implementation of the Operation interface to
 * provide default implementations of Operation predicates that are false except
 * for a few kinds of operations.
 *
 */
public abstract class AbstractOperation implements Operation {

  @Override
  public boolean isStatic() {
    return false;
  }

  @Override
  public boolean isMessage() {
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
   * Compares two {@link Operation} objects. Uses order on result of
   * {@link Operation#toString()}.
   *
   * @param op
   *          the {@link Operation} to compare with this operation
   * @return value &lt; 0 if this parseable string is less than for op, 0 if the
   *         strings are equal, and &gt; 0 if string for this object greater
   *         than for op.
   */
  @Override
  public int compareTo(Operation op) {
    return (this.toString()).compareTo(op.toString());
  }

  /**
   * Checks whether reflective object contained in an {@link Operation}
   * satisfies the predicate. Since there is no reflective object in an
   * {@code AbstractOperation}, returns false.
   *
   * @param predicate
   *          {@link ReflectionPredicate} against which object to be checked.
   * @return false as there is no object to check.
   */
  @Override
  public boolean satisfies(ReflectionPredicate predicate) {
    return false;
  }
}
