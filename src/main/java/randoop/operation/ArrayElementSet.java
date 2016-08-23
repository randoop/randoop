package randoop.operation;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.List;

import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * Created by bjkeller on 8/19/16.
 */
class ArrayElementSet extends CallableOperation {

  private final int ARRAY = 0;
  private final int INDEX = 1;
  private final int VALUE = 2;

  private final Type elementType;

  ArrayElementSet(Type elementType) {
    this.elementType = elementType;
  }

  @Override
  public ExecutionOutcome execute(Object[] input, PrintStream out) {
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

    b.append(inputVars.get(ARRAY).getName()).append("[");
    String index = inputVars.get(INDEX).getName();
    Statement statementCreatingIndex = inputVars.get(INDEX).getDeclaringStatement();
    String shortIndex = statementCreatingIndex.getShortForm();
    if (shortIndex != null) {
      index = shortIndex;
    }
    b.append(index).append("]").append(" = ");
    String value = inputVars.get(VALUE).getName();
    Statement statementCreatingVar = inputVars.get(VALUE).getDeclaringStatement();
    String shortValue = statementCreatingVar.getShortForm();
    if (shortValue != null) {
      value = shortValue;
    }
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
