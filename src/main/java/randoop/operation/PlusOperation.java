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
 *    + : [t, t] -&gt; t
 * </pre>
 *
 * where both operands are of type <i>t</i> (one of: byte, short, char, int, long, float, or
 * double). Two operands can have different types. The output type <i>t</i> is determined by the
 * Java language rules for numeric promotion.
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
    return 20250416;
  }

  @Override
  public ExecutionOutcome execute(Object[] input) {
    if (input.length != 2) {
      throw new IllegalArgumentException("Plus operation requires two arguments");
    }
    Object a = input[0], b = input[1];
    if (!((a instanceof Number || a instanceof Character)
        && (b instanceof Number || b instanceof Character))) {
      throw new IllegalArgumentException("Arguments must be numbers or characters");
    }

    long startTimeMillis = System.currentTimeMillis();
    Object result;

    // Same-type narrow cases
    if (a instanceof Byte && b instanceof Byte) {
      result = (byte) (((Byte) a) + ((Byte) b));
    } else if (a instanceof Short && b instanceof Short) {
      result = (short) (((Short) a) + ((Short) b));
    } else if (a instanceof Character && b instanceof Character) {
      result = (char) (((Character) a) + ((Character) b));

      // Normal Java promotion
    } else if (a instanceof Double || b instanceof Double) {
      result = toDouble(a) + toDouble(b);
    } else if (a instanceof Float || b instanceof Float) {
      result = toFloat(a) + toFloat(b);
    } else if (a instanceof Long || b instanceof Long) {
      result = toLong(a) + toLong(b);
    } else {
      // everything else (including mixed byte/short/char or int combos)
      result = toInt(a) + toInt(b);
    }

    long executionTimeMillis = System.currentTimeMillis() - startTimeMillis;
    return new NormalExecution(result, executionTimeMillis);
  }

  // Helpers for mixed-type arithmetic
  private int toInt(Object x) {
    return (x instanceof Character) ? ((Character) x) : ((Number) x).intValue();
  }

  private long toLong(Object x) {
    return (x instanceof Character) ? ((Character) x) : ((Number) x).longValue();
  }

  private float toFloat(Object x) {
    return (x instanceof Character) ? ((Character) x) : ((Number) x).floatValue();
  }

  private double toDouble(Object x) {
    return (x instanceof Character) ? ((Character) x) : ((Number) x).doubleValue();
  }

  @Override
  public void appendCode(
      Type declaringType,
      TypeTuple inputTypes,
      Type outputType,
      List<Variable> inputVars,
      StringBuilder b) {

    String out = outputType.getFqName();
    boolean isNarrow = out.equals("byte") || out.equals("short") || out.equals("char");

    // Cast to narrow type
    if (isNarrow) {
      b.append("(").append(out).append(")(");
    }
    b.append(getArgumentString(inputVars.get(0)));
    b.append(" + ");
    b.append(getArgumentString(inputVars.get(1)));
    if (isNarrow) {
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
