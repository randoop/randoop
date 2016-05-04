package randoop.operation;

import java.util.List;

import randoop.sequence.Variable;
import randoop.types.GeneralType;
import randoop.types.ReferenceType;
import randoop.types.Substitution;
import randoop.types.TypeTuple;

/**
 *
 */
class TypedTermOperation extends TypedOperation {

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
  public String toParsableString() {
    return this.getOperation().toParsableString(null, getInputTypes(), getOutputType());
  }
}
