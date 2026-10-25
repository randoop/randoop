package randoop.operation;

import java.lang.reflect.Array;
import java.util.List;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.Variable;
import randoop.types.Type;
import randoop.types.TypeTuple;

/** Represents assigning an array element. */
class ArrayElementSet extends CallableOperation {

  /** The index, within the arguments, of the array being assigned into. */
  private static final int ARRAY = 0;

  /** The index, within the arguments, of the array index being assigned to. */
  private static final int INDEX = 1;

  /** The index, within the arguments, of the value being assigned. */
  private static final int VALUE = 2;

  /** The type of the array's elements. */
  private final Type elementType;

  /**
   * Creates an operation that assigns an element of an array with the given element type.
   *
   * @param elementType the type of the array's elements
   */
  ArrayElementSet(Type elementType) {
    this.elementType = elementType;
  }

  @Override
  public ExecutionOutcome execute(Object[] input) {
    assert input.length == 3
        : "array element assignment must have array, index and value as arguments";
    Object array = input[ARRAY];
    int index = (int) input[INDEX];
    Object value = input[VALUE];

    try {
      Array.set(array, index, value);
    } catch (Throwable thrown) {
      return new ExceptionalExecution(thrown, 0);
    }
    return new NormalExecution(null, 0);
  }

  @Override
  public void appendCode(
      Type declaringType,
      TypeTuple inputTypes,
      Type outputType,
      List<Variable> inputVars,
      StringBuilder b) {

    b.append(inputVars.get(ARRAY).getName()).append('[');
    Variable indexVariable = inputVars.get(INDEX);
    String index = getArgumentString(indexVariable);
    b.append(index).append(']').append(" = ");
    String value = getArgumentString(inputVars.get(VALUE));
    b.append(value);
  }

  @Override
  public String toParsableString(Type declaringType, TypeTuple inputTypes, Type outputType) {
    return getName();
  }

  @Override
  public String getName() {
    return "<set>" + elementType + "[]";
  }
}
