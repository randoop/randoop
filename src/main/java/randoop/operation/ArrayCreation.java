package randoop.operation;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Objects;

import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.types.ArrayType;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * {@code ArrayCreation} is a {@link Operation} representing the construction of a one-dimensional
 * array of a given type.
 * The operation takes a length argument and creates an array of that size.
 */
public class ArrayCreation extends CallableOperation {

  /** The element type for the created array */
  private final Type elementType;

  /**
   * Creates an object representing the construction of an array of the given type.
   *
   * @param arrayType  the type of the created array
   */
  ArrayCreation(ArrayType arrayType) {
    this.elementType = arrayType.getElementType();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ArrayCreation)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    ArrayCreation arrayCreation = (ArrayCreation) obj;
    return this.elementType.equals(arrayCreation.elementType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(elementType);
  }

  @Override
  public String toString() {
    return elementType.getName() + "[]";
  }

  @Override
  public ExecutionOutcome execute(Object[] input, PrintStream out) {
    assert input.length == 1 : "requires array length as input";
    int length = Integer.parseInt(input[0].toString());
    long startTime = System.currentTimeMillis();
    Object theArray = Array.newInstance(this.elementType.getRuntimeClass(), length);
    long totalTime = System.currentTimeMillis() - startTime;
    return new NormalExecution(theArray, totalTime);
  }

  @Override
  public void appendCode(
      Type declaringType,
      TypeTuple inputTypes,
      Type outputType,
      List<Variable> inputVars,
      StringBuilder b) {
    b.append("new").append(" ").append(this.elementType.getName()).append("[ ");
    String param = inputVars.get(0).getName();
    Statement statementCreatingVar = inputVars.get(0).getDeclaringStatement();
    if (statementCreatingVar.isPrimitiveInitialization()
        && !statementCreatingVar.isNullInitialization()) {
      String shortForm = statementCreatingVar.getShortForm();
      if (shortForm != null) {
        param = shortForm;
      }
    }
    b.append(param).append(" ]");
  }

  @Override
  public String toParsableString(Type declaringType, TypeTuple inputTypes, Type outputType) {
    return elementType.getName() + "[ " + inputTypes.get(0) + " ]";
  }

  @Override
  public String getName() {
    return this.toString();
  }
}
