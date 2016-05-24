package randoop.operation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import randoop.sequence.Variable;
import randoop.types.AbstractTypeVariable;
import randoop.types.ClassOrInterfaceType;
import randoop.types.GeneralType;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.TypeTuple;
import randoop.types.TypeVariable;

/**
 * Represents a type decoration for an operation that has a declaring class.
 */
public class TypedClassOperation extends TypedOperation {
  /**
   * The declaring type for this operation
   */
  private final ClassOrInterfaceType declaringType;

  public TypedClassOperation(CallableOperation operation, ClassOrInterfaceType declaringType, TypeTuple inputTypes, GeneralType outputType) {
    super(operation, inputTypes, outputType);
    this.declaringType = declaringType;
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof TypedClassOperation)) {
      return false;
    }
    TypedClassOperation op = (TypedClassOperation)obj;
    return declaringType.equals(op.declaringType)
            && super.equals(obj);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), declaringType);
  }

  /**
   * Returns the class in which the operation is defined, or, if the operation represents a value,
   * the type of the value.
   *
   * @return class to which the operation belongs.
   */
  public ClassOrInterfaceType getDeclaringType() {
    return declaringType;
  }

  /**
   * Creates a {@link TypedOperation} from this operation by
   * using the given {@link Substitution} on type variables.
   *
   * @param substitution  the type substitution
   * @return the concrete operation with type variables replaced by substitution
   */
  public TypedClassOperation apply(Substitution<ReferenceType> substitution) {
    ClassOrInterfaceType declaringType = this.declaringType.apply(substitution);
    TypeTuple inputTypes = this.getInputTypes().apply(substitution);
    GeneralType outputType = this.getOutputType().apply(substitution);
    return new TypedClassOperation(this.getOperation(), declaringType, inputTypes, outputType);
  }

  @Override
  public TypedClassOperation applyCaptureConversion() {
    TypeTuple inputTypes = this.getInputTypes().applyCaptureConversion();
    GeneralType outputType = this.getOutputType().applyCaptureConversion();
    return new TypedClassOperation(this.getOperation(), declaringType, inputTypes, outputType);
  }

  /**
   * Produces a Java source code representation of this statement and append it to the given
   * StringBuilder.
   *
   * @param inputVars the list of variables that are inputs to operation.
   * @param b         the {@link StringBuilder} to which code is added.
   */
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
    assert inputVars.size() == this.getInputTypes().size(): "number of inputs doesn't match on operation appendCode";
    this.getOperation().appendCode(declaringType, getInputTypes(), getOutputType(), inputVars, b);
  }

  /**
   * Returns a string representation of this Operation, which can be read by static parse method for
   * class. For a class C implementing the Operation interface, this method should return a String s
   * such that parsing the string returns an object equivalent to this object, i.e.
   * C.parse(this.s).equals(this).
   *
   * @return string descriptor of {@link Operation} object.
   */
  @Override
  public String toParsableString() {
    return this.getOperation().toParsableString(declaringType, getInputTypes(), getOutputType());
  }

  @Override
  public String toString() {
    return declaringType + "." + super.toString();
  }

  @Override
  public List<AbstractTypeVariable> getTypeParameters() {
    Set<AbstractTypeVariable> paramSet = new LinkedHashSet<>();
    paramSet.addAll(getInputTypes().getTypeParameters());
    paramSet.addAll(getOutputType().getTypeParameters());
    return new ArrayList<>(paramSet);
  }
}
