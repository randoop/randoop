package randoop.operation;

import java.util.List;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.Variable;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * An {@link Operation} to perform an explicit cast. NOTE: there is no actual checking of the types
 * being done. This operation is only used in contexts where the cast is known to be unchecked.
 */
class UncheckedCast extends CallableOperation {

  /** The result type of the cast. */
  private final Type type;

  /**
   * Creates an operation that performs a cast. Intended for use in generated sequences where an
   * unchecked cast is needed.
   *
   * @param type the cast type
   */
  UncheckedCast(Type type) {
    this.type = type;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Performs this cast on the first value of the input array.
   *
   * @param input array containing appropriate inputs to operation
   * @return the value cast to the type of this cast
   */
  @Override
  public ExecutionOutcome execute(Object[] input) {
    assert input.length == 1 : "cast only takes one input";
    return new NormalExecution(type.getRuntimeClass().cast(input[0]), 0);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Appends the code for this cast applied to the given input to the given {@code
   * StringBuilder}.
   *
   * @param declaringType the declaring type for this operation
   * @param inputTypes the input types for this operation
   * @param outputType the output type for this operation
   * @param inputVars the list of variables that are inputs to operation
   * @param b the {@link StringBuilder} to which code is added
   */
  @Override
  public void appendCode(
      Type declaringType,
      TypeTuple inputTypes,
      Type outputType,
      List<Variable> inputVars,
      StringBuilder b) {
    b.append("(").append(type.getName()).append(")");
    int i = 0;
    String param = getArgumentString(inputVars.get(i));
    b.append(param);
  }

  @Override
  public boolean isUncheckedCast() {
    return true;
  }

  @Override
  public String toParsableString(Type declaringType, TypeTuple inputTypes, Type outputType) {
    return "(" + type.getName() + ")" + inputTypes.get(0);
  }

  @Override
  public String getName() {
    return "(" + type.getName() + ")";
  }
}
