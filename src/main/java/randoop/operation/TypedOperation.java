package randoop.operation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import randoop.ExecutionOutcome;
import randoop.condition.ExecutableSpecification;
import randoop.condition.ExpectedOutcomeTable;
import randoop.field.AccessibleField;
import randoop.reflection.ReflectionPredicate;
import randoop.sequence.Variable;
import randoop.types.ArrayType;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GenericClassType;
import randoop.types.InstantiatedType;
import randoop.types.JavaTypes;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.types.TypeVariable;

/**
 * Type decorator of {@link Operation} objects. An operation has zero or more input types, and one
 * output type that may be {@code void}.
 *
 * @see randoop.operation.TypedClassOperation
 * @see randoop.operation.TypedTermOperation
 */
public abstract class TypedOperation implements Operation, Comparable<TypedOperation> {

  /** The operation to be decorated. */
  private final CallableOperation operation;

  /**
   * The type tuple of input types. For a non-static method call or an instance field access, the
   * first input type is always that of the receiver, that is, the declaring class of the method or
   * the field. Refer to {@link Operation}.
   */
  protected final TypeTuple inputTypes;

  /** The output type. */
  private final Type outputType;

  /** The specification for this operation. */
  private ExecutableSpecification execSpec;

  /**
   * Create typed operation for the given {@link Operation}.
   *
   * @param operation the operation to wrap
   * @param inputTypes the input types
   * @param outputType the output types
   */
  TypedOperation(CallableOperation operation, TypeTuple inputTypes, Type outputType) {
    this.operation = operation;
    this.inputTypes = inputTypes;
    this.outputType = outputType;
    this.execSpec = null;
  }

  /**
   * Sets the specification; any previous value is ignored.
   *
   * @param execSpec the specification to use for this object
   */
  public void setExecutableSpecification(ExecutableSpecification execSpec) {
    this.execSpec = execSpec;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TypedOperation)) {
      return false;
    }
    TypedOperation op = (TypedOperation) obj;
    return getOperation().equals(op.getOperation())
        && inputTypes.equals(op.inputTypes)
        && outputType.equals(op.outputType);
  }

  /**
   * Compares this {@link TypedOperation} to another. Orders operations by type (any {@link
   * TypedTermOperation} object precedes a {@link TypedClassOperation}) then lexicographically
   * (alphabetically comparing class names, then operation names, then input type names, and finally
   * output type names).
   *
   * @param other the {@link TypedOperation} to compare with this operation
   * @return value &lt; 0 if this operation precedes {@code op}, 0 if the operations are identical,
   *     and &gt; 0 if this operation succeeds op
   */
  @Override
  public final int compareTo(TypedOperation other) {
    // term operations precede any class operation
    if (this instanceof TypedTermOperation && other instanceof TypedClassOperation) {
      return -1;
    }
    if (this instanceof TypedClassOperation && other instanceof TypedTermOperation) {
      return 1;
    }

    int result;

    // do lexicographical comparison of name
    result = this.getName().compareTo(other.getName());
    if (result != 0) {
      return result;
    }
    // then input types
    result = this.inputTypes.compareTo(other.inputTypes);
    if (result != 0) {
      return result;
    }

    if (this instanceof TypedClassOperation) {
      // For class operations, compare declaring class last to reduce size of diffs
      // (though it makes the log harder for a person to read!).
      TypedClassOperation thisOp = (TypedClassOperation) this;
      TypedClassOperation otherOp = (TypedClassOperation) other;
      result = thisOp.getDeclaringType().compareTo(otherOp.getDeclaringType());
      if (result != 0) {
        return result;
      }
    }

    // the output type
    // (TODO: Why is this comparison necessary?  MethodComparator ignores the output type, and this
    // comparison makes this method comparator inconsistent with MethodComparator.)
    result = this.outputType.compareTo(other.outputType);
    if (result != 0) {
      return result;
    }

    assert result == 0;
    return result;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getOperation(), inputTypes, outputType);
  }

  @Override
  public String toString() {
    String specString = (execSpec == null) ? "" : (" [spec: " + execSpec.toString() + "]");
    return getName() + " : " + inputTypes + " -> " + outputType + specString;
  }

  @Override
  public String getName() {
    return operation.getName();
  }

  /**
   * Returns the signature string for this operation.
   *
   * @return a string with the fully-qualified operation name and input type-tuple
   */
  public String getSignatureString() {
    return getName() + inputTypes;
  }

  /**
   * Returns the tuple of input types for this operation. For a non-static method call or an
   * instance field access, the first input type is always the declaring class of the method or
   * field.
   *
   * @return tuple of concrete input types
   */
  public TypeTuple getInputTypes() {
    return inputTypes;
  }

  /**
   * Returns the output type returned by the operation.
   *
   * @return {@link Type} type returned by this operation
   */
  public Type getOutputType() {
    return outputType;
  }

  /**
   * Get the enclosed operation in this typed operation.
   *
   * @return the enclosed operation
   */
  public CallableOperation getOperation() {
    return operation;
  }

  /**
   * Indicates whether this operation has a type that is a wildcard type.
   *
   * @return true if at least one input or output type has a wildcard, false otherwise
   */
  public abstract boolean hasWildcardTypes();

  /**
   * Indicate whether this operation is generic. An operation is generic if any of its input and
   * output types are generic.
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
  public boolean isConstantField() {
    return operation.isConstantField();
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

  /**
   * Appends Java text for this operation to the given {@code StringBuilder}, and using the given
   * variables.
   *
   * @param inputVars the list of input variables for this operation
   * @param b the {@code StringBuilder}
   */
  public abstract void appendCode(List<Variable> inputVars, StringBuilder b);

  /**
   * Performs this operation using the array of input values. Returns the results of execution as an
   * ResultOrException object and can output results to specified PrintStream.
   *
   * @param input array containing appropriate inputs to operation
   * @return results of executing this statement
   */
  public ExecutionOutcome execute(Object[] input) {
    assert input.length == inputTypes.size()
        : "operation execute expected " + inputTypes.size() + ", but got " + input.length;

    return this.getOperation().execute(input);
  }

  /**
   * Applies the given substitution to the generic types in this operation, and returns a new
   * operation with the instantiated types.
   *
   * @param substitution the substitution
   * @return the operation resulting from applying the substitution to the types of this operation
   */
  public abstract TypedOperation substitute(Substitution substitution);

  /**
   * Applies a capture conversion to the wildcard types of this operation, and returns a new
   * operation with new type variables for the wildcard types.
   *
   * @return the operation result from applying a capture conversion to wildcard types of this
   *     operation
   */
  public abstract TypedOperation applyCaptureConversion();

  // Implementation note: clients mutate the list, so don't use Collections.emptyList.
  public List<TypeVariable> getTypeParameters() {
    return new ArrayList<>();
  }

  /**
   * Constructs a string representation of this operation that can be parsed by parse methods of the
   * implementing types.
   *
   * @return a string representation of this operation
   */
  public abstract String toParsableString();

  /**
   * Constructs a {@link TypedOperation} for a constructor object.
   *
   * @param constructor the reflective constructor object
   * @return the typed operation for the constructor
   */
  public static TypedClassOperation forConstructor(Constructor<?> constructor) {
    ConstructorCall op = new ConstructorCall(constructor);
    ClassOrInterfaceType declaringType =
        ClassOrInterfaceType.forClass(constructor.getDeclaringClass());
    List<Type> paramTypes = new ArrayList<>();
    for (java.lang.reflect.Type t : constructor.getGenericParameterTypes()) {
      paramTypes.add(Type.forType(t));
    }
    TypeTuple inputTypes = new TypeTuple(paramTypes);
    return new TypedClassOperation(op, declaringType, inputTypes, declaringType);
  }

  /**
   * Constructs a {@link TypedOperation} for a method object.
   *
   * @param method the reflective method object
   * @return the typed operation for the given method
   */
  public static TypedClassOperation forMethod(Method method) {

    List<Type> methodParamTypes = new ArrayList<>();
    for (java.lang.reflect.Type t : method.getGenericParameterTypes()) {
      methodParamTypes.add(Type.forType(t));
    }

    Class<?> declaringClass = method.getDeclaringClass();
    if (declaringClass.isAnonymousClass()
        && declaringClass.getEnclosingClass() != null
        && declaringClass.getEnclosingClass().isEnum()) {
      // is a method in anonymous class for enum constant
      return getAnonEnumOperation(method, methodParamTypes, declaringClass.getEnclosingClass());
    }

    List<Type> paramTypes = new ArrayList<>();
    MethodCall op = new MethodCall(method);
    ClassOrInterfaceType declaringType = ClassOrInterfaceType.forClass(method.getDeclaringClass());
    if (!op.isStatic()) {
      paramTypes.add(declaringType);
    }
    paramTypes.addAll(methodParamTypes);
    TypeTuple inputTypes = new TypeTuple(paramTypes);
    Type outputType = Type.forType(method.getGenericReturnType());
    if (outputType.isVariable()) {
      return new TypedClassOperationWithCast(op, declaringType, inputTypes, outputType);
    }
    return new TypedClassOperation(op, declaringType, inputTypes, outputType);
  }

  /**
   * Constructs a {@link TypedOperation} for an enum from a method object that is a member of an
   * anonymous class for an enum constant. Will return null if no matching method is found in the
   * enum.
   *
   * @param method the method of the anonymous class
   * @param methodParamTypes the parameter types of the method
   * @param enumClass the declaring class
   * @return the typed operation for the given method, null if no matching method is found in {@code
   *     enumClass}
   */
  private static TypedClassOperation getAnonEnumOperation(
      Method method, List<Type> methodParamTypes, Class<?> enumClass) {
    ClassOrInterfaceType enumType = ClassOrInterfaceType.forClass(enumClass);

    /*
     * Have to determine whether parameter types match.
     * If the method comes from a generic type, the parameters for the method will be instantiated
     * and it is necessary to build the instantiated parameter list.
     */
    // TODO verify that subsignature conditions on erasure met (JLS 8.4.2)
    for (Method m : enumClass.getMethods()) {
      if (m.getName().equals(method.getName())
          && m.getGenericParameterTypes().length == method.getGenericParameterTypes().length) {
        List<Type> paramTypes = new ArrayList<>();
        MethodCall op = new MethodCall(m);
        if (!op.isStatic()) {
          paramTypes.add(enumType);
        }
        for (java.lang.reflect.Type t : m.getGenericParameterTypes()) {
          paramTypes.add(Type.forType(t));
        }
        TypeTuple inputTypes = new TypeTuple(paramTypes);
        Type outputType = Type.forType(m.getGenericReturnType());

        ClassOrInterfaceType methodDeclaringType =
            ClassOrInterfaceType.forClass(m.getDeclaringClass());
        if (methodDeclaringType.isGeneric()) {
          GenericClassType genDeclaringType = (GenericClassType) methodDeclaringType;
          InstantiatedType superType = enumType.getMatchingSupertype(genDeclaringType);
          assert superType != null
              : "should exist a super type of enum instantiating " + genDeclaringType;
          Substitution substitution = superType.getTypeSubstitution();
          inputTypes = inputTypes.substitute(substitution);
          outputType = outputType.substitute(substitution);
        }

        // check if param types match
        int d = op.isStatic() ? 0 : 1;
        int i = 0;
        while (i < methodParamTypes.size()
            && methodParamTypes.get(i).equals(inputTypes.get(i + d))) {
          i++;
        }
        if (i == methodParamTypes.size()) {
          return new TypedClassOperation(op, enumType, inputTypes, outputType);
        }
      }
    }
    /*
     * When dredging methods from anonymous classes, end up with methods that have Object instead
     * of generic type parameter. These just cause pain when generating code, and this code
     * assumes that the current method is one of these if we cannot find a match.
     */
    System.out.println(
        method.getName()
            + " is bridge? "
            + method.isBridge()
            + " is synthetic? "
            + method.isSynthetic());
    return null;
  }

  /**
   * Creates a {@link TypedOperation} that represents a read access to a field.
   *
   * @param field the field
   * @param declaringType the declaring type for the field
   * @return an operation to access the given field of the declaring type
   */
  public static TypedClassOperation createGetterForField(
      Field field, ClassOrInterfaceType declaringType) {
    Type fieldType = Type.forType(field.getGenericType());
    AccessibleField accessibleField = new AccessibleField(field, declaringType);
    List<Type> inputTypes = new ArrayList<>();
    if (!accessibleField.isStatic()) {
      inputTypes.add(declaringType);
    }
    return new TypedClassOperation(
        new FieldGet(accessibleField), declaringType, new TypeTuple(inputTypes), fieldType);
  }

  /**
   * Creates a {@link TypedOperation} that represents a write access to a field.
   *
   * @param field the field
   * @param declaringType the declaring type of the field
   * @return an operation to set the value of the given field of the declaring type
   */
  public static TypedClassOperation createSetterForField(
      Field field, ClassOrInterfaceType declaringType) {
    Type fieldType = Type.forType(field.getGenericType());
    AccessibleField accessibleField = new AccessibleField(field, declaringType);
    List<Type> inputTypes = new ArrayList<>();
    if (!accessibleField.isStatic()) {
      inputTypes.add(declaringType);
    }
    inputTypes.add(fieldType);
    return new TypedClassOperation(
        new FieldSet(accessibleField),
        declaringType,
        new TypeTuple(inputTypes),
        JavaTypes.VOID_TYPE);
  }

  /**
   * Creates an operation that initializes a variable to the zero value for the given type.
   *
   * @param type the type of the initialization
   * @return the initialization operation
   */
  public static TypedOperation createNullOrZeroInitializationForType(Type type) {
    return TypedOperation.createNonreceiverInitialization(
        NonreceiverTerm.createNullOrZeroTerm(type));
  }

  /**
   * Creates an operation that initializes a variable to a given primitive value.
   *
   * @param type the primitive type
   * @param value the value for initialization
   * @return the initialization operation
   */
  public static TypedOperation createPrimitiveInitialization(Type type, Object value) {
    Type valueType = Type.forValue(value);
    assert valueType.isNonreceiverType() : "must be nonreceiver type, got " + type.getName();
    return TypedOperation.createNonreceiverInitialization(new NonreceiverTerm(type, value));
  }

  /**
   * Creates an operation that uses the given {@link NonreceiverTerm} for initializing a variable.
   *
   * @param term the {@link NonreceiverTerm}
   * @return the initialization operation
   */
  public static TypedOperation createNonreceiverInitialization(NonreceiverTerm term) {
    return new TypedTermOperation(term, new TypeTuple(), term.getType());
  }

  /**
   * Creates an operation that creates an array of the given type and size.
   *
   * @param arrayType the type of the array
   * @param size the size of the created array
   * @return the array creation operation
   */
  public static TypedOperation createInitializedArrayCreation(ArrayType arrayType, int size) {
    List<Type> typeList = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      typeList.add(arrayType.getComponentType());
    }
    TypeTuple inputTypes = new TypeTuple(typeList);
    return new TypedTermOperation(
        new InitializedArrayCreation(arrayType, size), inputTypes, arrayType);
  }

  /**
   * Creates a simple array creation operation for the given type.
   *
   * @param arrayType the desired array type
   * @return an operation to create an array of the given type
   */
  public static TypedOperation createArrayCreation(ArrayType arrayType) {
    List<Type> typeList = Collections.singletonList(JavaTypes.INT_TYPE);
    TypeTuple inputTypes = new TypeTuple(typeList);
    return new TypedTermOperation(new ArrayCreation(arrayType), inputTypes, arrayType);
  }

  /**
   * Creates an operation to cast from one type to another.
   *
   * @param fromType the input type
   * @param toType the resulting type
   * @return an operation that casts the input type to the result type
   */
  public static TypedOperation createCast(Type fromType, Type toType) {
    List<Type> typeList = Collections.singletonList(fromType);
    TypeTuple inputTypes = new TypeTuple(typeList);
    return new TypedTermOperation(new UncheckedCast(toType), inputTypes, toType);
  }

  /**
   * Creates an operation to assign a value to an array element.
   *
   * @param arrayType the type of the array
   * @return return an operation that
   */
  public static TypedOperation createArrayElementAssignment(ArrayType arrayType) {
    List<Type> typeList = new ArrayList<>();
    typeList.add(arrayType);
    typeList.add(JavaTypes.INT_TYPE);
    typeList.add(arrayType.getComponentType());
    TypeTuple inputTypes = new TypeTuple(typeList);
    return new TypedTermOperation(
        new ArrayElementSet(arrayType.getComponentType()), inputTypes, JavaTypes.VOID_TYPE);
  }

  @Override
  public boolean isUncheckedCast() {
    return operation.isUncheckedCast();
  }

  /**
   * Tests the specification for this operation against the argument values and returns the {@link
   * ExpectedOutcomeTable} indicating the results of checking the pre-conditions of the
   * specifications of the operation.
   *
   * @param values the argument values
   * @return the {@link ExpectedOutcomeTable} indicating the results of checking the pre-conditions
   *     of the specifications of the operation
   */
  public ExpectedOutcomeTable checkPrestate(Object[] values) {
    if (execSpec == null) {
      return new ExpectedOutcomeTable();
    }
    return execSpec.checkPrestate(addNullReceiverIfStatic(values));
  }

  /**
   * Inserts {@code null} as first argument when this operation is static.
   *
   * <p>This is necessary because the argument array for checking an {@link Operation} is always
   * assumed to have a "receiver" argument, which is null (and ignored) for a static method.
   *
   * @param values the argument array for this operation
   * @return the corresponding operation array for checking a {@link
   *     randoop.condition.ExecutableBooleanExpression}
   */
  private Object[] addNullReceiverIfStatic(Object[] values) {
    Object[] args = values;
    if (this.isStatic()) {
      args = new Object[values.length + 1];
      args[0] = null;
      System.arraycopy(values, 0, args, 1, values.length);
    }
    return args;
  }

  /**
   * RankedTypedOperation is a wrapper around a TypedOperation and a number. The number represents a
   * ranking or priority. The purpose of this class is to be put in a priority queue.
   */
  public static class RankedTypeOperation {
    /** Ranking value for the TypedOperation. */
    public final double ranking;

    /** The wrapped operation. */
    public final TypedClassOperation operation;

    /**
     * Constructor to populate ranking and operation.
     *
     * @param ranking value associated with the operation
     * @param operation wrapped operation
     */
    public RankedTypeOperation(double ranking, TypedClassOperation operation) {
      this.ranking = ranking;
      this.operation = operation;
    }
  }

  /** Comparator used for sorting by ranking. */
  public static final Comparator<RankedTypeOperation> compareRankedTypeOperation =
      (RankedTypeOperation t, RankedTypeOperation t1) ->
          Double.valueOf(t.ranking).compareTo(t1.ranking);
}
