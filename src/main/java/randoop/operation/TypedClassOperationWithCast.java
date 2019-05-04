package randoop.operation;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * Represents a method with a return type that is a type variable that must be instantiated, and for
 * which execution performs a cast to the instantiating type to emulate handling of casts that are
 * not done in reflection.
 */
public class TypedClassOperationWithCast extends TypedClassOperation {
  TypedClassOperationWithCast(
      CallableOperation op,
      ClassOrInterfaceType declaringType,
      TypeTuple inputTypes,
      Type outputType) {
    super(op, declaringType, inputTypes, outputType);
  }

  @Override
  public TypedClassOperationWithCast substitute(Substitution substitution) {
    if (substitution.isEmpty()) {
      return this;
    }
    ClassOrInterfaceType declaringType = getDeclaringType().substitute(substitution);
    TypeTuple inputTypes = this.getInputTypes().substitute(substitution);
    Type outputType = this.getOutputType().substitute(substitution);
    return new TypedClassOperationWithCast(
        this.getOperation(), declaringType, inputTypes, outputType);
  }

  @Override
  public TypedClassOperationWithCast applyCaptureConversion() {
    return new TypedClassOperationWithCast(
        this.getOperation(),
        this.getDeclaringType(),
        this.getInputTypes().applyCaptureConversion(),
        this.getOutputType());
  }

  /**
   * {@inheritDoc}
   *
   * <p>Performs cast to output type of this operation to ensure that any {@code ClassCastException}
   * that would be thrown in JVM execution is also thrown.
   */
  @Override
  public ExecutionOutcome execute(Object[] input) {
    ExecutionOutcome outcome = super.execute(input);
    if (outcome instanceof NormalExecution) {
      NormalExecution execution = (NormalExecution) outcome;
      Object result = null;
      try {
        result = getOutputType().getRuntimeClass().cast(execution.getRuntimeValue());
      } catch (ClassCastException e) {
        return new ExceptionalExecution(e, 0);
      }
      if (result != null) {
        return new NormalExecution(result, execution.getExecutionTime());
      }
    }
    return outcome;
  }
}
