package randoop.operation;

import randoop.types.GeneralType;
import randoop.types.GenericType;
import randoop.types.GenericTypeTuple;
import randoop.types.Substitution;

/**
 * {@code GenericOperation} is an abstract implementation of {@link Operation} that
 * represents an operation whose definition includes a type variable.
 */
public abstract class GenericOperation extends AbstractOperation {

  private GeneralType declaringType;
  private GenericTypeTuple inputTypes;
  private GenericType outputType;

  public GenericOperation(GeneralType declaringType, GenericTypeTuple inputTypes, GenericType outputType) {
    this.declaringType = declaringType;
    this.inputTypes = inputTypes;
    this.outputType = outputType;
  }

  /**
   * Returns the tuple of input types for this operation.
   * If a method call or field access, the first input corresponds to the
   * receiver, which must be an object of the declaring class.
   *
   * @return tuple of generic input types for this operation
   */
  public GenericTypeTuple getInputTypes() {
    return inputTypes;
  }

  /**
   * Returns the type returned by the operation.
   *
   * @return the type returned by this operation
   */
  public GenericType getOutputType() {
    return outputType;
  }

  /**
   * Returns the class in which the operation is defined, or, if the operation
   * represents a value, the type of the value.
   *
   * @return class to which the operation belongs.
   */
  public GeneralType getDeclaringType() {
    return declaringType;
  }

  /**
   * Creates a {@link ConcreteOperation} from this generic operation by
   * using the given {@link Substitution} on type variables.
   *
   * @param substitution  the type substitution
   * @return the concrete operation with type variables replaced by substitution
   */
  public abstract ConcreteOperation instantiate(Substitution substitution);

}
