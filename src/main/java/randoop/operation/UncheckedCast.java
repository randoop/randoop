package randoop.operation;

import java.util.List;
import org.checkerframework.checker.signedness.qual.Signed;
import randoop.ExceptionalExecution;
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
   * <p>The cast can legitimately fail at run time: this operation is used (in particular, by
   * "GRT Elephant-Brain" run-time-type casting, see {@link
   * randoop.sequence.ExecutableSequence#castToRunTimeType}) to cast a value to a concrete type
   * that was observed on a previous execution. If the value comes from a call whose result depends
   * on mutable state (for example a getter for a mutable static field), a later execution of the
   * same generated code may produce a value of a different, incompatible runtime type. Rather than
   * letting {@link ClassCastException} propagate (which would abort test generation entirely),
   * report it the same way other operations report failures: as an {@link ExceptionalExecution}.
   *
   * @param input array containing appropriate inputs to operation
   * @return the value cast to the type of this cast, or an {@link ExceptionalExecution} if the cast
   *     fails
   */
  @Override
  public ExecutionOutcome execute(Object[] input) {
    assert input.length == 1 : "cast only takes one input";
    try {
      @Signed Object result = type.getRuntimeClass().cast(input[0]);
      return new NormalExecution(result, 0);
    } catch (ClassCastException e) {
      return new ExceptionalExecution(e, 0);
    }
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
    b.append('(').append(type.getFqName()).append(')');
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
    return "(" + type.getBinaryName() + ")" + inputTypes.get(0);
  }

  @Override
  public String getName() {
    return "(" + type.getBinaryName() + ")";
  }
}
