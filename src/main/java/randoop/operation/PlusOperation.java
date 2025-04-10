package randoop.operation;

import java.util.List;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.Variable;
import randoop.types.Type;
import randoop.types.TypeTuple;

/**
 * PlusOperation is an {@link Operation} that represents the addition operator (+). It is used to
 * perform addition on two numeric values or characters.
 *
 * <p>As an {@link Operation}, a call to the plus operation with two arguments is formally
 * represented as:
 *
 * <pre>
 *    + : [t, t] → t
 * </pre>
 *
 * where both operands are of type <i>t</i> (one of: byte, short, char, int, long, float, or
 * double), and the output type <i>t</i> is determined by the type of the first operand.
 */
public class PlusOperation extends CallableOperation {

  /** Creates an plus operation that performs addition. */
  public PlusOperation() {}

  @Override
  public String toString() {
    return "+";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof PlusOperation;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public ExecutionOutcome execute(Object[] input) {
    if (input.length != 2) {
      throw new IllegalArgumentException("UnaryPlus operation requires two arguments");
    }
    Object arg1 = input[0];
    Object arg2 = input[1];

    if (!((arg1 instanceof Number || arg1 instanceof Character)
        && (arg2 instanceof Number || arg2 instanceof Character))) {
      throw new IllegalArgumentException("Arguments must be numbers");
    }

    long startTimeMillis = System.currentTimeMillis();

    Object result;
    if (arg1 instanceof Integer) {
      result = ((Integer) arg1) + ((Number) arg2).intValue();
    } else if (arg1 instanceof Long) {
      result = ((Long) arg1) + ((Number) arg2).longValue();
    } else if (arg1 instanceof Float) {
      result = ((Float) arg1) + ((Number) arg2).floatValue();
    } else if (arg1 instanceof Double) {
      result = ((Double) arg1) + ((Number) arg2).doubleValue();
    } else if (arg1 instanceof Short) {
      result = (short) (((Short) arg1).shortValue() + ((Number) arg2).shortValue());
    } else if (arg1 instanceof Byte) {
      result = (byte) (((Byte) arg1).byteValue() + ((Number) arg2).byteValue());
    } else if (arg1 instanceof Character) {
      result = (char) (((Character) arg1).charValue() + ((Number) arg2).intValue());
    } else {
      throw new IllegalArgumentException(
          "Unsupported argument types: " + arg1.getClass() + ", " + arg2.getClass());
    }

    long executionTime = System.currentTimeMillis() - startTimeMillis;
    return new NormalExecution(result, executionTime);
  }

  @Override
  public void appendCode(
      Type declaringType,
      TypeTuple inputTypes,
      Type outputType,
      List<Variable> inputVars,
      StringBuilder b) {
    // If plus operation is called on byte, short, or char, we need to cast the result to the
    // appropriate type.
    String outputTypeName = outputType.getFqName();
    boolean isNarrowIntegralType =
        outputTypeName.equals("byte")
            || outputTypeName.equals("short")
            || outputTypeName.equals("char");
    if (isNarrowIntegralType) {
      b.append("(" + outputType.getFqName() + ")(");
    }
    b.append(getArgumentString(inputVars.get(0)));
    b.append(" + ");
    b.append(getArgumentString(inputVars.get(1)));
    if (isNarrowIntegralType) {
      b.append(")");
    }
  }

  @Override
  public String toParsableString(Type declaringType, TypeTuple inputTypes, Type outputType) {
    return "+";
  }

  @Override
  public String getName() {
    return "+";
  }
}
