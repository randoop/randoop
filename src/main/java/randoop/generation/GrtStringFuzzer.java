package randoop.generation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
  /** Singleton instance. */
  private static final GrtStringFuzzer INSTANCE = new GrtStringFuzzer();

  /**
   * Obtain the singleton instance of {@link GrtStringFuzzer}.
   *
   * @return the singleton instance
   */
  public static GrtStringFuzzer getInstance() {
    return INSTANCE;
  }

  /** Private constructor to enforce singleton. */
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
  /**
   * An enum representing the fuzzing operations for Strings. Each set of fuzzing operations has a
   * corresponding method to be invoked on a StringBuilder object. Each run of GRT Fuzzing will
   * randomly select one of these set of operations to perform on the input String.
   *
   * <p>Assumptions:
   *
   * <ul>
   *   <li>Each {@code getStringBuilderTransform()} list contains methods to be invoked sequentially
   *       on a StringBuilder object.
   *   <li>Among these methods, only one method accepts arguments, which are exactly the arguments
   *       generated by {@code getInputs()}.
   *   <li>The remaining methods (if any) in the {@code getStringBuilderTransform()} list take no
   *       arguments (other than the receiver, which is the StringBuilder instance).
   * </ul>
   */
  private enum StringFuzzingOperation {
    /** The operation to insert a character at a random index. */
    INSERT {
      @Override
      Sequence inputs(int length) {
        int idx = Randomness.nextRandomInt(length + 1);
        char c = (char) (Randomness.nextRandomInt(PRINTABLE_ASCII_SPAN) + PRINTABLE_ASCII_START);
        return Sequence.concatenate(
            Sequence.createSequenceForPrimitive(idx), Sequence.createSequenceForPrimitive(c));
      }
    },
    /** The operation to remove a character at a random index. */
    REMOVE {
      @Override
      Sequence inputs(int length) {
        return Sequence.createSequenceForPrimitive(Randomness.nextRandomInt(length));
      }
    },
    /** The operation to replace a character at a random index with a random character. */
    REPLACE {
      @Override
      Sequence inputs(int length) {
        int i1 = Randomness.nextRandomInt(length);
        int i2 = Randomness.nextRandomInt(length);
        int start = Math.min(i1, i2), end = Math.max(i1, i2);
        String r =
            String.valueOf(
                (char) (Randomness.nextRandomInt(PRINTABLE_ASCII_SPAN) + PRINTABLE_ASCII_START));
        return Sequence.concatenate(
            Sequence.createSequenceForPrimitive(start),
            Sequence.createSequenceForPrimitive(end),
            Sequence.createSequenceForPrimitive(r));
      }
    },
    /** The operation to extract a substring from a random start index to a random end index. */
    SUBSTRING {
      @Override
      Sequence inputs(int length) {
        int i1 = Randomness.nextRandomInt(length);
        int i2 = Randomness.nextRandomInt(length);
        int start = Math.min(i1, i2), end = Math.max(i1, i2);
        return Sequence.concatenate(
            Sequence.createSequenceForPrimitive(start), Sequence.createSequenceForPrimitive(end));
      }
    };

    /** The set of all StringFuzzingOperation values. */
    private static final StringFuzzingOperation[] VALUES = values();

    /**
     * Return a random StringFuzzingOperation.
     *
     * @return a random StringFuzzingOperation
     */
    static StringFuzzingOperation random() {
      return VALUES[Randomness.nextRandomInt(VALUES.length)];
    }

    /** The set of all StringBuilder methods for this operation. */
    private static final Map<StringFuzzingOperation, List<Executable>> METHOD_CACHE;

    static {
      EnumMap<StringFuzzingOperation, List<Executable>> m =
          new EnumMap<>(StringFuzzingOperation.class);
      m.put(INSERT, init("insert", int.class, char.class));
      m.put(REMOVE, init("deleteCharAt", int.class));
      m.put(REPLACE, init("replace", int.class, int.class, String.class));
      m.put(SUBSTRING, init("substring", int.class, int.class));
      METHOD_CACHE = Collections.unmodifiableMap(m);
    }

    /** Return the list of methods for this operation. */
    List<Executable> methods() {
      return METHOD_CACHE.get(this);
    }

    /**
     * Return the inputs for this operation. The inputs are generated randomly and depend on the
     * length of the input string.
     *
     * @param length the length of the input string
     * @return a sequence of inputs for this operation
     */
    abstract Sequence inputs(int length);

    /**
     * Initialize the list of methods for a given operation.
     *
     * @param name the name of the method
     * @param params the parameter types of the method
     * @return a list of {@link Executable} objects representing the method and its toString method
     */
    private static List<Executable> init(String name, Class<?>... params) {
      try {
        Method m = StringBuilder.class.getMethod(name, params);
        if (m.getReturnType() == StringBuilder.class) {
          Method toStr = StringBuilder.class.getMethod("toString");
          return Collections.unmodifiableList(Arrays.asList(m, toStr));
        } else {
          return Collections.singletonList(m);
        }
      } catch (NoSuchMethodException e) {
        throw new AssertionError("Missing StringBuilder method: " + name, e);
      }
    }
  }
}
