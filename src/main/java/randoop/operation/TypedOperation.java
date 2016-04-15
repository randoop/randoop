package randoop.operation;

import java.util.Objects;

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

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof TypedOperation)) {
      return false;
    }
    TypedOperation<?> op = (TypedOperation<?>)obj;
    return operation.equals(op.operation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(operation);
  }

  @Override
  public String toString() {
    return this.getName();
  }

  @Override
  public String getName() {
    return operation.getName();
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
