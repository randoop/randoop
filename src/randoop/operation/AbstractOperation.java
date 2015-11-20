package randoop.operation;

import randoop.reflection.ReflectionPredicate;

/**
 * AbstractOperation is an abstract implementation of the Operation interface
 * to provide default implementations of Operation predicates that are false
 * except for a few kinds of operations.
 * 
 * @author bjkeller
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
   * compareTo(Operation) compares two {@link Operation} objects.
   * Uses canonical string representation of operations to compare.
   */
  @Override
  public int compareTo(Operation op) {
    return (this.toParseableString()).compareTo(op.toParseableString());
  }

  /**
   * satisfies checks to see if reflective object contained in this {@link Operation}
   * satisfies the predicate. Since there is no reflective object, this returns false.
   * @param predicate {@link ReflectionPredicate} against which object to be checked.
   * @return false as there is no object to check.
   */
  @Override
  public boolean satisfies(ReflectionPredicate predicate) {
    return false;
  }
}
