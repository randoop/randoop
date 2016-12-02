package randoop.operation;

import java.util.List;

import randoop.sequence.Variable;
import randoop.types.Type;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.TypeTuple;

/**
 * Represents operations that have no declaring class.
 */
class TypedTermOperation extends TypedOperation {

  /**
   * Creates a {@link TypedOperation} for a given operation and input and output types.
   *
   * @param operation the operation
   * @param inputTypes  the input types
   * @param outputType  the output type
   */
  TypedTermOperation(CallableOperation operation, TypeTuple inputTypes, Type outputType) {
    super(operation, inputTypes, outputType);
  }

  @Override
  public boolean hasWildcardTypes() {
    return false;
  }

  @Override
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
    this.getOperation().appendCode(null, getInputTypes(), getOutputType(), inputVars, b);
  }

  @Override
  public TypedTermOperation apply(Substitution<ReferenceType> substitution) {
    TypeTuple inputTypes = this.getInputTypes().apply(substitution);
    Type outputType = this.getOutputType().apply(substitution);
    return new TypedTermOperation(this.getOperation(), inputTypes, outputType);
  }

  @Override
  public TypedOperation applyCaptureConversion() {
    TypeTuple inputTypes = this.getInputTypes().applyCaptureConversion();
    Type outputType = this.getOutputType().applyCaptureConversion();
    return new TypedTermOperation(this.getOperation(), inputTypes, outputType);
  }

  @Override
  public String toParsableString() {
    return this.getOperation().toParsableString(null, getInputTypes(), getOutputType());
  }

  /**
   * Compares this operation to another {@link TypedOperation}.
   * Ensures that any {@link TypedTermOperation} objects precedes a {@link TypedClassOperation}.
   * Otherwise, orders {@link TypedTermOperation} objects with
   * {@link TypedOperation#compareTo(TypedOperation)}.
   *
   * @param op  the {@link TypedOperation} to compare with this operation
   * @return value &lt; 0 if this operation precedes {@code op}, 0 if the
   *         operations are identical, and &gt; 0 if this operation succeeds op
   */
  @Override
  public int compareTo(TypedOperation op) {
    if (op instanceof TypedClassOperation) {
      return -1;
    }
    return super.compareTo(op);
  }
}
