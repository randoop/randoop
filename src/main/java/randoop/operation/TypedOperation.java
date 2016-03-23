package randoop.operation;

import randoop.reflection.ReflectionPredicate;

/**
 * Superclass for type decorator for {@link Operation} objects.
 */
public abstract class TypedOperation<T extends Operation> implements Operation {

  private T operation;

  public TypedOperation(T operation) {
    this.operation = operation;
  }

  protected T getOperation() { return operation; }

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
