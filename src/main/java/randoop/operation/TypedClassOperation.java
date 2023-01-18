package randoop.operation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.plumelib.util.CollectionsPlume;
import org.plumelib.util.StringsPlume;
import randoop.condition.ExecutableSpecification;
import randoop.reflection.RawSignature;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.types.TypeVariable;

/**
 * Represents a TypedOperation and its declaring class. Examples of TypedOperations that have a
 * declaring class are a method call or field access.
 */
public class TypedClassOperation extends TypedOperation {
  /** The declaring type for this operation. */
  private final ClassOrInterfaceType declaringType;

  /** The cached value of {@link #getRawSignature()}. */
  private RawSignature rawSignature = null;

  /**
   * Creates a {@link TypedClassOperation} for a given {@link CallableOperation} indicating the
   * signature of the operation.
   *
   * @param operation the {@link CallableOperation}
   * @param declaringType the declaring class type for this operation
   * @param inputTypes the input types for the operation
   * @param outputType the output types for the operation
   */
  public TypedClassOperation(
      CallableOperation operation,
      ClassOrInterfaceType declaringType,
      TypeTuple inputTypes,
      Type outputType) {
    this(operation, declaringType, inputTypes, outputType, null);
  }

  /**
   * Creates a {@link TypedClassOperation} for a given {@link CallableOperation} indicating the
   * signature of the operation.
   *
   * @param operation the {@link CallableOperation}
   * @param declaringType the declaring class type for this operation
   * @param inputTypes the input types for the operation
   * @param outputType the output types for the operation
   * @param execSpec the specification for the operation
   */
  public TypedClassOperation(
      CallableOperation operation,
      ClassOrInterfaceType declaringType,
      TypeTuple inputTypes,
      Type outputType,
      ExecutableSpecification execSpec) {
    super(operation, inputTypes, outputType, execSpec);
    this.declaringType = declaringType;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TypedClassOperation)) {
      return false;
    }
    TypedClassOperation op = (TypedClassOperation) obj;
    return declaringType.equals(op.declaringType) && super.equals(obj);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), declaringType);
  }

  /**
   * Returns the class in which the operation is defined.
   *
   * @return class to which the operation belongs
   */
  public ClassOrInterfaceType getDeclaringType() {
    return declaringType;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Applies the substitution to the declaring type, all input types, and the output type.
   */
  @Override
  public TypedClassOperation substitute(Substitution substitution) {
    if (substitution.isEmpty()) {
      return this;
    }
    ClassOrInterfaceType declaringType = this.declaringType.substitute(substitution);
    TypeTuple inputTypes = this.getInputTypes().substitute(substitution);
    Type outputType = this.getOutputType().substitute(substitution);
    return new TypedClassOperation(
        this.getOperation(),
        declaringType,
        inputTypes,
        outputType,
        this.getExecutableSpecification());
  }

  @Override
  public TypedClassOperation applyCaptureConversion() {
    TypeTuple inputTypes = this.getInputTypes().applyCaptureConversion();
    Type outputType = this.getOutputType();
    return new TypedClassOperation(
        this.getOperation(),
        declaringType,
        inputTypes,
        outputType,
        this.getExecutableSpecification());
  }

  /**
   * Produces a Java source code representation of this operation and appends it to the given
   * StringBuilder.
   *
   * @param inputVars the list of variables that are inputs to operation
   * @param b the {@link StringBuilder} to which code is added
   */
  @Override
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
    assert inputVars.size() == this.getInputTypes().size()
        : "number of inputs doesn't match on operation appendCode";
    this.getOperation().appendCode(declaringType, getInputTypes(), getOutputType(), inputVars, b);
  }

  /**
   * Returns a string representation of this Operation, which can be read by the static {@code
   * parse} method for an Operation class. For a class C implementing the Operation interface, this
   * method should return a String s such that parsing the string returns an object equivalent to
   * this object, i.e., C.parse(this.s).equals(this).
   *
   * @return string descriptor of {@link Operation} object
   */
  @Override
  public String toParsableString() {
    return this.getOperation().toParsableString(declaringType, getInputTypes(), getOutputType());
  }

  @Override
  public String toString() {
    if (this.isGeneric()) {
      String b = "<" + StringsPlume.join(",", this.getTypeParameters()) + ">" + " ";
      return b + super.toString();
    } else {
      return super.toString();
    }
  }

  @Override
  public String getName() {
    return declaringType + "." + super.getName();
  }

  /**
   * Returns the simple name of this operation, not qualified by the declaring class.
   *
   * @return the unqualified name of this operation
   */
  public String getUnqualifiedBinaryName() {
    return super.getName();
  }

  @Override
  public boolean hasWildcardTypes() {
    return getInputTypes().hasWildcard()
        || (getOutputType().isParameterized()
            && ((ClassOrInterfaceType) getOutputType()).hasWildcard());
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    List<TypeVariable> inputTypeParams = getInputTypes().getTypeParameters();
    // This set, and the returned list, is likely to be very small.
    Set<TypeVariable> paramSet =
        new LinkedHashSet<>(CollectionsPlume.mapCapacity(inputTypeParams.size()));
    paramSet.addAll(inputTypeParams);
    if (getOutputType().isReferenceType()) {
      paramSet.addAll(((ReferenceType) getOutputType()).getTypeParameters());
    }
    return new ArrayList<>(paramSet);
  }

  /**
   * Returns the fully-qualified signature for this operation if it is a method or constructor call.
   *
   * @return this operation's fully qualified signature if it is a method or constructor call, null
   *     otherwise
   */
  public String getFullyQualifiedSignature() {
    if (!this.isConstructorCall() && !this.isMethodCall()) {
      return null;
    }

    Package classPackage = this.declaringType.getPackage();
    String packageName = RawSignature.getPackageName(classPackage);
    String classname = this.getDeclaringType().getRawtype().getUnqualifiedBinaryName();
    String name =
        this.getUnqualifiedBinaryName().equals("<init>")
            ? classname
            : this.getUnqualifiedBinaryName();

    Iterator<Type> inputTypeIterator = inputTypes.iterator();
    List<String> typeNames = new ArrayList<>(inputTypes.size());

    for (int i = 0; inputTypeIterator.hasNext(); i++) {
      String typeName = inputTypeIterator.next().getFqName();
      if (i == 0 && !isStatic()) {
        continue;
      }
      typeNames.add(typeName);
    }

    return ((packageName == null) ? "" : packageName + ".")
        + (classname.equals(name) ? name : classname + "." + name)
        + "("
        + StringsPlume.join(",", typeNames)
        + ")";
  }

  /**
   * Returns the {@link RawSignature} for this operation if it is a method or constructor call.
   *
   * @return the {@link RawSignature} of this method or constructor operation, null if this is
   *     another kind of operation
   */
  public RawSignature getRawSignature() {
    // XXX Awkward: either refactor operations, or allow RawSignature to represent fields, probably
    // both.
    if (!this.isConstructorCall() && !this.isMethodCall()) {
      return null;
    }
    if (rawSignature == null) {
      Package classPackage = this.declaringType.getPackage();
      String packageName = RawSignature.getPackageName(classPackage);
      // There should be a way to do this without calling getUnqualifiedBinaryName.
      String classname =
          RawSignature.classNameToIdentifier(
              this.getDeclaringType().getRawtype().getUnqualifiedBinaryName());
      String name =
          this.getUnqualifiedBinaryName().equals("<init>")
              ? classname
              : this.getUnqualifiedBinaryName();

      Class<?>[] parameterTypes =
          this.isMethodCall()
              ? ((MethodCall) getOperation()).getMethod().getParameterTypes()
              : ((ConstructorCall) getOperation()).getConstructor().getParameterTypes();
      rawSignature = new RawSignature(packageName, classname, name, parameterTypes);
    }
    return rawSignature;
  }

  /**
   * Creates an operation with the same name, input types, and output type as this operation, but
   * having the given type as the owning class.
   *
   * <p>Note: this is only a valid object if {@code type} has the method. This is definitely the
   * case if {@code type} is a subtype of the declaring type of the operation, but this method does
   * not force that check because we sometimes want to create the operation for superclasses.
   *
   * @param type a type to substitute into the operation
   * @return an operation with {@code type} substituted for the declaring type of this operation.
   *     The returned object will be invalid if {@code type} does not have the method. The returned
   *     object may be {@code this}, if the argument is already {@code this}'s declaring type.
   */
  @SuppressWarnings("ReferenceEquality")
  public TypedClassOperation getOperationForType(ClassOrInterfaceType type) {
    if (type == this.getDeclaringType()) {
      return this;
    } else {
      return new TypedClassOperation(
          this.getOperation(),
          type,
          this.getInputTypes(),
          this.getOutputType(),
          // TODO: Is this the right ExecutableSpecification?
          this.getExecutableSpecification());
    }
  }
}
