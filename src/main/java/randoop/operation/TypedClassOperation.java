package randoop.operation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import plume.UtilMDE;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.types.TypeVariable;

/** Represents a type decoration for an operation that has a declaring class. */
public class TypedClassOperation extends TypedOperation {
  /** The declaring type for this operation */
  private final ClassOrInterfaceType declaringType;

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
    super(operation, inputTypes, outputType);
    this.declaringType = declaringType;
  }

  @Override
  public boolean equals(Object obj) {
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
   * Compares this operation to another {@link TypedOperation}. Ensures that any {@link
   * TypedTermOperation} objects precedes a {@link TypedClassOperation}. Otherwise, orders {@link
   * TypedClassOperation} objects by first comparing the declaring types, and then comparing by
   * {@link TypedOperation#compareTo(TypedOperation)}.
   *
   * @param op the {@link TypedOperation} to compare with this operation
   * @return value &lt; 0 if this operation precedes {@code op}, 0 if the operations are identical,
   *     and &gt; 0 if this operation succeeds op
   */
  @Override
  public int compareTo(TypedOperation op) {
    int result = 0;
    if (op instanceof TypedTermOperation) {
      return 1;
    }
    TypedClassOperation other = (TypedClassOperation) op;
    result = declaringType.compareTo(other.declaringType);
    if (result != 0) {
      return result;
    }
    return super.compareTo(other);
  }

  /**
   * Returns the class in which the operation is defined, or, if the operation represents a value,
   * the type of the value.
   *
   * @return class to which the operation belongs
   */
  public ClassOrInterfaceType getDeclaringType() {
    return declaringType;
  }

  /**
   * Creates a {@link TypedOperation} from this operation by using the given {@link Substitution} on
   * type variables.
   *
   * @param substitution the type substitution
   * @return the concrete operation with type variables replaced by substitution
   */
  public TypedClassOperation apply(Substitution<ReferenceType> substitution) {
    if (substitution.isEmpty()) {
      return this;
    }
    ClassOrInterfaceType declaringType = this.declaringType.apply(substitution);
    TypeTuple inputTypes = this.getInputTypes().apply(substitution);
    Type outputType = this.getOutputType().apply(substitution);
    return new TypedClassOperation(this.getOperation(), declaringType, inputTypes, outputType);
  }

  @Override
  public TypedClassOperation applyCaptureConversion() {
    TypeTuple inputTypes = this.getInputTypes().applyCaptureConversion();
    Type outputType = this.getOutputType();
    return new TypedClassOperation(this.getOperation(), declaringType, inputTypes, outputType);
  }

  /**
   * Produces a Java source code representation of this statement and append it to the given
   * StringBuilder.
   *
   * @param inputVars the list of variables that are inputs to operation
   * @param b the {@link StringBuilder} to which code is added
   */
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
    assert inputVars.size() == this.getInputTypes().size()
        : "number of inputs doesn't match on operation appendCode";
    this.getOperation().appendCode(declaringType, getInputTypes(), getOutputType(), inputVars, b);
  }

  /**
   * Returns a string representation of this Operation, which can be read by static parse method for
   * class. For a class C implementing the Operation interface, this method should return a String s
   * such that parsing the string returns an object equivalent to this object, i.e.
   * C.parse(this.s).equals(this).
   *
   * @return string descriptor of {@link Operation} object
   */
  @Override
  public String toParsableString() {
    return this.getOperation().toParsableString(declaringType, getInputTypes(), getOutputType());
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    if (this.isGeneric()) {
      b.append("<");
      b.append(UtilMDE.join(this.getTypeParameters(), ","));
      b.append(">").append(" ");
    }
    return b.toString() + super.toString();
  }

  @Override
  public String getName() {
    return declaringType + "." + super.getName();
  }

  @Override
  public boolean hasWildcardTypes() {
    return getInputTypes().hasWildcard()
        || (getOutputType().isParameterized()
            && ((ClassOrInterfaceType) getOutputType()).hasWildcard());
  }

  @Override
  public List<TypeVariable> getTypeParameters() {
    Set<TypeVariable> paramSet = new LinkedHashSet<>();
    paramSet.addAll(getInputTypes().getTypeParameters());
    if (getOutputType().isReferenceType()) {
      paramSet.addAll(((ReferenceType) getOutputType()).getTypeParameters());
    }
    return new ArrayList<>(paramSet);
  }
}
