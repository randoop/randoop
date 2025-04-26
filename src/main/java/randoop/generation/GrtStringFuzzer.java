package randoop.generation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import randoop.main.RandoopBug;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.types.NonParameterizedType;
import randoop.types.Type;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;

/**
 * Fuzzer that mutates {@link String} values by <em>INSERT</em>, <em>REMOVE</em>, <em>REPLACE</em>,
 * or <em>SUBSTRING</em> operations on a {@link StringBuilder}.
 *
 * <p>INSERT: Insert a random character at a random index. REMOVE: Remove a character at a random
 * index. REPLACE: Replace a character at a random index with a random character. SUBSTRING: Extract
 * a substring from a random start index to a random end index.
 */
@SuppressWarnings("NotJavadoc")
public final class GrtStringFuzzer extends GrtBaseFuzzer {

  /* --------------------------- Singleton --------------------------- */
  private static final GrtStringFuzzer INSTANCE = new GrtStringFuzzer();

  public static GrtStringFuzzer getInstance() {
    return INSTANCE;
  }

  private GrtStringFuzzer() {
    /* no-op */
  }

  /* ------------------------------ Constants ------------------------------ */
  /** StringBuilder(String) constructor operation cached for reuse. */
  private static final TypedClassOperation SB_CTOR_OP;

  static {
    try {
      Constructor<StringBuilder> ctor = StringBuilder.class.getConstructor(String.class);
      SB_CTOR_OP = TypedOperation.forConstructor(ctor);
    } catch (NoSuchMethodException e) {
      throw new AssertionError("StringBuilder constructor missing", e);
    }
  }

  /** The starting ASCII value for printable characters. */
  private static final int PRINTABLE_ASCII_START = 32;

  /** Number of printable ASCII characters (codes 32-126 inclusive). */
  private static final int PRINTABLE_ASCII_SPAN = 95;

  /* ------------------------------- API ----------------------------------- */
  @Override
  public boolean canFuzz(Type type) {
    return type.getRuntimeClass() == String.class;
  }

  @Override
  public Sequence fuzz(Sequence sequence) {
    try {
      String original = getStringValue(sequence);
      StringFuzzingOperation strFuzzingOp = StringFuzzingOperation.random();

      // Don't fuzz empty strings with REMOVE, REPLACE, or SUBSTRING if the original is empty
      if (original.isEmpty() && strFuzzingOp != StringFuzzingOperation.INSERT) {
        return sequence;
      }

      List<Statement> stmts = new ArrayList<>();
      // Build StringBuilder(sb) from original string literal
      Sequence strLitSeq = Sequence.createSequenceForPrimitive(original);
      Sequence sbSeq =
          Sequence.createSequence(
              SB_CTOR_OP, Collections.singletonList(strLitSeq), Collections.singletonList(0));
      stmts.addAll(sbSeq.statements.toJDKList());

      // Extra inputs for the chosen fuzz operation (indices / chars)
      Sequence opInputs = strFuzzingOp.inputs(original.length());
      stmts.addAll(opInputs.statements.toJDKList());

      // Method calls that perform the fuzzing operation
      stmts.addAll(convertExecutables(strFuzzingOp.methods()));

      return Sequence.concatenate(sequence, new Sequence(new SimpleArrayList<>(stmts)));

    } catch (Exception e) {
      throw new RandoopBug("String fuzzing failed: " + e.getMessage(), e);
    }
  }

  /* ------------------------ Helper & utility methods --------------------- */
  /**
   * Retrieve last runtime value (assumed String).
   *
   * @param seq the sequence to retrieve the last value from
   * @return the last value of the sequence
   */
  private static String getStringValue(Sequence seq) {
    Object v = seq.getStatement(seq.size() - 1).getValue();
    if (!(v instanceof String)) {
      throw new IllegalArgumentException("Last statement did not produce a String");
    }
    return (String) v;
  }

  /**
   * Convert reflection objects to Randoop {@link Statement}s.
   *
   * @param execs the list of {@link Executable} objects to convert
   * @return the list of {@link Statement}s
   */
  private static List<Statement> convertExecutables(List<Executable> execs) {
    List<Statement> out = new ArrayList<>();
    for (Executable ex : execs) {
      TypedClassOperation op;
      if (ex instanceof Method) {
        op = TypedOperation.forMethod((Method) ex);
      } else {
        op = TypedOperation.forConstructor((Constructor<?>) ex);
      }
      int nInputs = collectInputTypes(ex, new NonParameterizedType(ex.getDeclaringClass())).size();
      out.add(new Statement(op, getRelativeNegativeIndices(nInputs)));
    }
    return out;
  }

  /**
   * Collect input {@link Type}s (including receiver for non-static methods).
   *
   * @param ex the {@link Executable} to collect input types from
   * @param declaringType the {@link NonParameterizedType} of the declaring class
   * @return the list of input {@link Type}s
   */
  private static List<Type> collectInputTypes(Executable ex, NonParameterizedType declaringType) {
    List<Type> list = new ArrayList<>();
    if (!Modifier.isStatic(ex.getModifiers()) && ex instanceof Method) {
      list.add(declaringType);
    }
    for (Class<?> p : ex.getParameterTypes()) {
      list.add(Type.forClass(p));
    }
    return list;
  }

  /* ------------------------- String fuzz enum ------------------------ */

  private enum StringFuzzingOperation {
    /** Insert a random character at a random index in the string. */
    INSERT("insert", int.class, char.class),

    /** Remove a character at a random index in the string. */
    REMOVE("deleteCharAt", int.class),

    /** Replace a random substring with a random character. */
    REPLACE("replace", int.class, int.class, String.class),

    /** Create a substring with random start and end indices of the string. */
    SUBSTRING("substring", int.class, int.class);

    /** The list of methods to be invoked on a StringBuilder object. */
    private final List<Executable> transformMethods;

    /** The list of all StringFuzzingOperation values. */
    private static final StringFuzzingOperation[] VALUES = values();

    /**
     * Constructor for StringFuzzingOperation.
     *
     * @param methodName the name of the method to be invoked on StringBuilder
     * @param paramTypes the parameter types of the method
     */
    StringFuzzingOperation(String methodName, Class<?>... paramTypes) {
      try {
        Method m = StringBuilder.class.getMethod(methodName, paramTypes);
        if (methodName.equals("substring")) {
          // only substring()
          this.transformMethods = Arrays.asList(m);
        } else {
          // method + toString()
          Method toStringM = StringBuilder.class.getMethod("toString");
          this.transformMethods = Arrays.asList(m, toStringM);
        }
      } catch (NoSuchMethodException e) {
        throw new AssertionError("StringBuilder method missing: " + e);
      }
    }

    /**
     * Randomly select one of the StringFuzzingOperation values.
     *
     * @return a random StringFuzzingOperation
     */
    static StringFuzzingOperation random() {
      return VALUES[Randomness.nextRandomInt(VALUES.length)];
    }

    /** Generate the argument sequence for this fuzz operation on a string of given length. */
    Sequence inputs(int length) {
      if (this == INSERT) {
        int idx = Randomness.nextRandomInt(length + 1);
        char c = (char) (Randomness.nextRandomInt(PRINTABLE_ASCII_SPAN) + PRINTABLE_ASCII_START);
        return Sequence.concatenate(
                Sequence.createSequenceForPrimitive(idx), Sequence.createSequenceForPrimitive(c));
      } else if (this == REMOVE) {
        return Sequence.createSequenceForPrimitive(Randomness.nextRandomInt(length));
      } else if (this == REPLACE) {
        int i1 = Randomness.nextRandomInt(length);
        int i2 = Randomness.nextRandomInt(length);
        int start = Math.min(i1, i2);
        int end = Math.max(i1, i2);
        String r =
                String.valueOf(
                        (char) (Randomness.nextRandomInt(PRINTABLE_ASCII_SPAN) + PRINTABLE_ASCII_START));
        return Sequence.concatenate(
                Sequence.createSequenceForPrimitive(start),
                Sequence.createSequenceForPrimitive(end),
                Sequence.createSequenceForPrimitive(r));
      } else { // SUBSTRING
        int i1 = Randomness.nextRandomInt(length);
        int i2 = Randomness.nextRandomInt(length);
        int start = Math.min(i1, i2);
        int end = Math.max(i1, i2);
        return Sequence.concatenate(
                Sequence.createSequenceForPrimitive(start), Sequence.createSequenceForPrimitive(end));
      }
    }

    /** Return the list of StringBuilder methods to invoke for this operation. */
    List<Executable> methods() {
      return transformMethods;
    }
  }
}
