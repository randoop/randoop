package randoop.operation;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import randoop.ExecutionOutcome;
import randoop.reflection.ReflectionPredicate;
import randoop.sequence.Variable;
import randoop.types.ArrayType;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GeneralType;
import randoop.types.PrimitiveTypes;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.TypeTuple;

/**
 * Type decorator of {@link Operation} objects.
 */
public abstract class TypedOperation implements Operation {

  /** The operation to be decorated */
  private final CallableOperation operation;

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
  public TypedOperation(CallableOperation operation, TypeTuple inputTypes, GeneralType outputType) {
    this.operation = operation;
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
            && inputTypes.equals(op.inputTypes)
            && outputType.equals(op.outputType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getOperation(), inputTypes, outputType);
  }

  @Override
  public String toString() {
    return super.toString() + " : " + inputTypes + " -> " + outputType;
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

  public abstract void appendCode(List<Variable> inputVars, StringBuilder b);

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

  public abstract TypedOperation apply(Substitution<ReferenceType> substitution);

  public abstract String toParsableString();

  public static TypedClassOperation forConstructor(Constructor<?> constructor) {
    ConstructorCall op = new ConstructorCall(constructor);
    ClassOrInterfaceType declaringType = ClassOrInterfaceType.forClass(constructor.getDeclaringClass());
    List<GeneralType> paramTypes = new ArrayList<>();
    for (Type t : constructor.getGenericParameterTypes()) {
      paramTypes.add(GeneralType.forType(t));
    }
    TypeTuple inputTypes = new TypeTuple(paramTypes);
    return new TypedClassOperation(op, declaringType, inputTypes, declaringType);
  }

  public static TypedClassOperation forMethod(Method method) {
    MethodCall op = new MethodCall(method);
    ClassOrInterfaceType declaringType = ClassOrInterfaceType.forClass(method.getDeclaringClass());
    List<GeneralType> paramTypes = new ArrayList<>();
    if (op.isStatic()) {
      paramTypes.add(declaringType);
    }
    for (Type t : method.getGenericParameterTypes()) {
      paramTypes.add(GeneralType.forType(t));
    }
    TypeTuple inputTypes = new TypeTuple(paramTypes);
    GeneralType outputType = GeneralType.forType(method.getGenericReturnType());
    return new TypedClassOperation(op, declaringType, inputTypes, outputType);
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
    return new TypedTermOperation(term, new TypeTuple(), term.getType());
  }

  public static TypedOperation createArrayCreation(ArrayType arrayType, int size) {
    List<GeneralType> typeList = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      typeList.add(arrayType.getElementType());
    }
    TypeTuple inputTypes = new TypeTuple(typeList);
    return new TypedTermOperation(new ArrayCreation(arrayType, size), inputTypes, arrayType);
  }


}
