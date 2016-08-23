package randoop.operation;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.List;

import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.Variable;
import randoop.types.ArrayType;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * Represents an array creation using reflection, and corresponds to the code
 * <code>
 *   (List&lt;String&gt;[])(Array.newInstance(componentType, length));
 * </code>
 */
public class ReflectionArrayCreation extends CallableOperation {
  private final Type elementType;
  private final int length;

  public ReflectionArrayCreation(ArrayType arrayType, int length) {
    this.elementType = arrayType.getElementType();
    this.length = length;
  }

  @Override
  public ExecutionOutcome execute(Object[] input, PrintStream out) {
    long startTime = System.currentTimeMillis();
    assert input.length == 0;
    Object arrayObject = Array.newInstance(this.elementType.getRuntimeClass(), this.length);
    long totalTime = System.currentTimeMillis() - startTime;
    return new NormalExecution(arrayObject, totalTime);
  }

  @Override
  public String toString() {
    return "Array.newInstance(" + elementType.getName() + ", " + length + ")";
  }

  @Override
  public void appendCode(
      Type declaringType,
      TypeTuple inputTypes,
      Type outputType,
      List<Variable> inputVars,
      StringBuilder b) {
    b.append("Array.newInstance(")
        .append(elementType.getSimpleName())
        .append(", ")
        .append(length)
        .append(")");
  }

  @Override
  public String toParsableString(Type declaringType, TypeTuple inputTypes, Type outputType) {
    return null;
  }

  @Override
  public String getName() {
    return this.toString();
  }
}
