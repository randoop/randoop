package randoop.operation;

import randoop.reflection.ReflectionPredicate;

/**
 * Superclass for type decorator of {@link Operation} objects.
 * Serves as facade to forward {@link Operation} method calls.
 */
public abstract class TypedOperation<T extends Operation> implements Operation {

  /** The operation to be decorated */
  private T operation;

  /**
   * Create typed operation for the given {@link Operation}.
   *
   * @param operation  the operation to wrap
   */
  public TypedOperation(T operation) {
    this.operation = operation;
  }

  /**
   * Get the enclosed operation in this typed operation.
   *
   * @return the enclosed operation
   */
  public T getOperation() { return operation; }

  /**
   * Indicate whether this operation is generic.
   *
   * @return true if the operation is generic, false if not
   */
  public abstract boolean isGeneric();

  @Override
  public boolean isStatic() {
    return operation.isStatic();
  }

  @Override
  public boolean isMessage() {
    return operation.isMessage();
  }

  @Override
  public boolean isMethodCall() {
    return operation.isMethodCall();
  }

  @Override
  public boolean isConstructorCall() {
    return operation.isConstructorCall();
  }

  @Override
  public boolean isNonreceivingValue() {
    return operation.isNonreceivingValue();
  }

  @Override
  public Object getValue() {
    return operation.getValue();
  }

  @Override
  public boolean satisfies(ReflectionPredicate reflectionPredicate) {
    return operation.satisfies(reflectionPredicate);
  }

  @Override
  public int compareTo(Operation o) {
    return operation.compareTo(o);
  }

}
