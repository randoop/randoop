package randoop.operation;

import randoop.types.GeneralType;
import randoop.types.GeneralTypeTuple;
import randoop.types.GenericType;
import randoop.types.GenericTypeTuple;
import randoop.types.Substitution;

/**
 * {@code GenericOperation} is an abstract implementation of {@link Operation} that
 * represents an operation whose definition includes a type variable.
 * <p>
 * An operation is represented as a {@code GenericOperation} if
 * <ul>
 *     <li>it is defined in a {@link GenericType} in which case it may have concrete or generic type, or</li>
 *     <li>it is a generic method or constructor of a concrete type.</li>
 * </ul>
 * This variability means that at least one of the types associated with a {@code GenericOperation}
 * must be generic, but the others may be concrete.
 * So, all types are represented by {@code GeneralType} objects.
 * <p>
 * Note that the <i>declaring</i> and <i>output</i> types of a creation operation are both defined
 * as the type created.
 */
public abstract class GenericOperation extends AbstractOperation {

  /** The type that declares this operation. */
  private GeneralType declaringType;

  /** The input types for the operation. */
  private GeneralTypeTuple inputTypes;

  /** The output type for the operation. */
  private GeneralType outputType;

  /**
   * Create a generic operation with the given declaring type, input types and output type.
   *
   * @param declaringType  the type in which the operation is declared
   * @param inputTypes  the input types for the operation
   * @param outputType  the output type for the operation
   */
  public GenericOperation(
      GeneralType declaringType, GenericTypeTuple inputTypes, GeneralType outputType) {
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
  public GeneralTypeTuple getInputTypes() {
    return inputTypes;
  }

  /**
   * Returns the type returned by the operation.
   *
   * @return the type returned by this operation
   */
  public GeneralType getOutputType() {
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
