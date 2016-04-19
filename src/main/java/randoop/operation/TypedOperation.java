package randoop.operation;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import randoop.ExecutionOutcome;
import randoop.reflection.ReflectionPredicate;
import randoop.sequence.Variable;
import randoop.types.ArrayType;
import randoop.types.GeneralType;
import randoop.types.PrimitiveTypes;
import randoop.types.RandoopTypeException;
import randoop.types.Substitution;
import randoop.types.TypeTuple;

/**
 * Superclass for type decorator of {@link Operation} objects.
 * Serves as facade to forward {@link Operation} method calls.
 */
public class TypedOperation implements Operation {

  /** The operation to be decorated */
  private CallableOperation operation;

  /**
   * The declaring type for this operation
   */
  private final GeneralType declaringType;

  /**
   * The type tuple of concrete input types.
   */
  private final TypeTuple inputTypes;

  /**
   * The concrete output type.
   */
  private final GeneralType outputType;

  /**
   * Create typed operation for the given {@link Operation}.
   *
   * @param operation  the operation to wrap
   */
  public TypedOperation(CallableOperation operation, GeneralType declaringType, TypeTuple inputTypes, GeneralType outputType) {
    this.operation = operation;
    this.declaringType = declaringType;
    this.inputTypes = inputTypes;
    this.outputType = outputType;
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof TypedOperation)) {
      return false;
    }
    TypedOperation op = (TypedOperation)obj;
    return getOperation().equals(op.getOperation())
            && declaringType.equals(op.declaringType)
            && inputTypes.equals(op.inputTypes)
            && outputType.equals(op.outputType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getOperation(), declaringType, inputTypes, outputType);
  }

  @Override
  public String toString() {
    return declaringType + "." + super.toString() + " : " + inputTypes + " -> " + outputType;
  }

  @Override
  public String getName() {
    return operation.getName();
  }

  /**
   * Returns the tuple of input types for this operation. If a method call or field access, the
   * first input corresponds to the receiver, which must be an object of the declaring class.
   *
   * @return tuple of concrete input types
   */
  public TypeTuple getInputTypes() {
    return inputTypes;
  }

  /**
   * Returns the type returned by the operation.
   *
   * @return {@link GeneralType} type returned by this operation
   */
  public GeneralType getOutputType() {
    return outputType;
  }

  /**
   * Returns the class in which the operation is defined, or, if the operation represents a value,
   * the type of the value.
   *
   * @return class to which the operation belongs.
   */
  public GeneralType getDeclaringType() {
    return declaringType;
  }
  
  /**
   * Get the enclosed operation in this typed operation.
   *
   * @return the enclosed operation
   */
  public CallableOperation getOperation() { return operation; }

  /**
   * Indicate whether this operation is generic.
   * An operation is generic if any of its input and output types are generic.
   *
   * @return true if the operation is generic, false if not
   */
  public boolean isGeneric() {
    return inputTypes.isGeneric() || outputType.isGeneric();
  }

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

  /**
   * Performs this operation using the array of input values. Returns the results of execution as an
   * ResultOrException object and can output results to specified PrintStream.
   *
   * @param input array containing appropriate inputs to operation
   * @param out   stream to output results of execution; can be null if you don't want to print.
   * @return results of executing this statement
   */
  public ExecutionOutcome execute(Object[] input, PrintStream out) {
    assert input.length == inputTypes.size()
            : "operation execute expected " + inputTypes.size() + ", but got " + input.length;

    return this.getOperation().execute(input, out);
  }

  /**
   * Produces a Java source code representation of this statement and append it to the given
   * StringBuilder.
   *
   * @param inputVars the list of variables that are inputs to operation.
   * @param b         the {@link StringBuilder} to which code is added.
   */
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
    assert inputVars.size() == this.inputTypes.size(): "number of inputs doesn't match on operation appendCode";
    this.getOperation().appendCode(declaringType, inputTypes, outputType, inputVars, b);
  }

  /**
   * Returns a string representation of this Operation, which can be read by static parse method for
   * class. For a class C implementing the Operation interface, this method should return a String s
   * such that parsing the string returns an object equivalent to this object, i.e.
   * C.parse(this.s).equals(this).
   *
   * @return string descriptor of {@link Operation} object.
   */
  public String toParseableString() {
    return this.getOperation().toParseableString(declaringType, inputTypes, outputType);
  }

  /**
   * Creates a {@link TypedOperation} from this operation by
   * using the given {@link Substitution} on type variables.
   *
   * @param substitution  the type substitution
   * @return the concrete operation with type variables replaced by substitution
   */
  public GenericOperation apply(Substitution substitution) throws RandoopTypeException {
    GeneralType declaringType = this.declaringType.apply(substitution);
    TypeTuple inputTypes = this.inputTypes.apply(substitution);
    GeneralType outputType = this.outputType.apply(substitution);
    return new GenericOperation(this.getOperation(), declaringType, inputTypes, outputType);
  }

  public static TypedOperation createNullInitializationWithType(GeneralType type) {
    assert ! type.isPrimitive() : "cannot initialize primitive to null: " + type;
    return TypedOperation.createNonreceiverInitialization(new NonreceiverTerm(type, null));
  }

  public static TypedOperation createNullOrZeroInitializationForType(GeneralType type) {
    return TypedOperation.createNonreceiverInitialization(NonreceiverTerm.createNullOrZeroTerm(type));
  }

  public static TypedOperation createPrimitiveInitialization(GeneralType type, Object value) {
    assert PrimitiveTypes.isBoxedOrPrimitiveOrStringType(type.getRuntimeClass()) : "must be nonreceiver type, got " + type.getName();
    return TypedOperation.createNonreceiverInitialization(new NonreceiverTerm(type,value));
  }

  public static TypedOperation createNonreceiverInitialization(NonreceiverTerm term) {
    return new TypedOperation(term, term.getType(), new TypeTuple(), term.getType());
  }

  public static TypedOperation createArrayCreation(ArrayType arrayType, int size) {
    List<GeneralType> typeList = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      typeList.add(arrayType.getElementType());
    }
    TypeTuple inputTypes = new TypeTuple(typeList);
    return new TypedOperation(new ArrayCreation(arrayType, size), arrayType, inputTypes, arrayType);
  }
}
