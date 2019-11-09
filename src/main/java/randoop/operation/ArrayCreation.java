package randoop.operation;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Objects;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.Variable;
import randoop.types.ArrayType;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * {@code ArrayCreation} is a {@link Operation} representing the construction of a one-dimensional
 * array of a given type. The operation takes a length argument and creates an array of that size.
 */
public class ArrayCreation extends CallableOperation {

  /** The element type for the created array. */
  private final Type elementType;

  /** The component type for the created array. */
  private final Type componentType;

  /** The dimensions of the created array. */
  private int dimensions;

  /**
   * Creates an object representing the construction of an array of the given type.
   *
   * @param arrayType the type of the created array
   */
  ArrayCreation(ArrayType arrayType) {
    this.elementType = arrayType.getElementType();
    this.componentType = arrayType.getComponentType();
    this.dimensions = arrayType.getDimensions();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ArrayCreation)) {
      return false;
    }
    ArrayCreation arrayCreation = (ArrayCreation) obj;
    return this.elementType.equals(arrayCreation.elementType)
        && this.dimensions == arrayCreation.dimensions;
  }

  @Override
  public int hashCode() {
    return Objects.hash(elementType, dimensions);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder(elementType.getName());
    for (int i = 0; i < dimensions; i++) {
      result.append("[]");
    }
    return result.toString();
  }

  @Override
  // The argument array contains a single Integer.
  public ExecutionOutcome execute(Object[] input) {
    assert input.length == 1 : "requires array dimension as input";
    int length = ((Integer) input[0]).intValue();
    long startTime = System.currentTimeMillis();
    Object theArray = Array.newInstance(this.componentType.getRuntimeClass(), length);
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
    Variable inputVar = inputVars.get(0);
    b.append("new").append(" ").append(this.elementType.getName());
    b.append("[ ");
    String param = getArgumentString(inputVar);
    b.append(param).append(" ]");
    for (int i = 1; i < dimensions; i++) {
      b.append("[]");
    }
  }

  @Override
  public String toParsableString(Type declaringType, TypeTuple inputTypes, Type outputType) {
    StringBuilder result =
        new StringBuilder(elementType.getName() + "[ " + inputTypes.get(0) + " ]");
    for (int i = 1; i < dimensions; i++) {
      result.append("[]");
    }
    return result.toString();
  }

  @Override
  public String getName() {
    return this.toString();
  }
}
