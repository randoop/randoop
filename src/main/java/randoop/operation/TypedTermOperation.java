package randoop.operation;

import java.util.List;

import randoop.sequence.Variable;
import randoop.types.GeneralType;
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
  TypedTermOperation(CallableOperation operation, TypeTuple inputTypes, GeneralType outputType) {
    super(operation, inputTypes, outputType);
  }

  @Override
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
    this.getOperation().appendCode(null, getInputTypes(), getOutputType(), inputVars, b);
  }

  @Override
  public TypedTermOperation apply(Substitution<ReferenceType> substitution) {
    TypeTuple inputTypes = this.getInputTypes().apply(substitution);
    GeneralType outputType = this.getOutputType().apply(substitution);
    return new TypedTermOperation(this.getOperation(), inputTypes, outputType);
  }

  @Override
  public TypedOperation applyCaptureConversion() {
    TypeTuple inputTypes = this.getInputTypes().applyCaptureConversion();
    GeneralType outputType = this.getOutputType().applyCaptureConversion();
    return new TypedTermOperation(this.getOperation(), inputTypes, outputType);
  }

  @Override
  public String toParsableString() {
    return this.getOperation().toParsableString(null, getInputTypes(), getOutputType());
  }
}
