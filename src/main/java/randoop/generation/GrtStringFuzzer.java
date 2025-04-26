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

  /** Number of printable ASCII characters (codes 32–126 inclusive). */
  private static final int PRINTABLE_ASCII_SPAN = 95;

  /* ------------------------------- API ----------------------------------- */
  @Override
  public boolean canFuzz(Type type) {
    return type.getRuntimeClass() == String.class;
  }

  @Override
  public Sequence fuzz(Sequence sequence) {
    Class<?> cls = sequence.getLastVariable().getType().getRuntimeClass();
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
   * @params seq the sequence to extract the value from
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

  /** Random string operations adapted from the original code. */
  private enum StringFuzzingOperation {
    INSERT("insert", int.class, char.class),
    REMOVE("deleteCharAt", int.class),
    REPLACE("replace", int.class, int.class, String.class),
    SUBSTRING("substring", int.class, int.class);

    private final String name; // method name on StringBuilder
    private final List<Class<?>> params; // parameter types

    StringFuzzingOperation(String n, Class<?>... p) {
      this.name = n;
      this.params = Arrays.asList(p);
    }

    static StringFuzzingOperation random() {
      StringFuzzingOperation[] v = values();
      return v[Randomness.nextRandomInt(v.length)];
    }

    Sequence inputs(int len) throws Exception {
      List<Statement> stmts = new ArrayList<>();
      for (Class<?> p : params) {
        Object lit;
        if (p == int.class) {
          lit = Randomness.nextRandomInt(Math.max(len, 1));
        } else if (p == char.class) {
          lit = (char) (Randomness.nextRandomInt(PRINTABLE_ASCII_SPAN) + PRINTABLE_ASCII_START);
        } else if (p == String.class) {
          lit =
              String.valueOf(
                  (char) (Randomness.nextRandomInt(PRINTABLE_ASCII_SPAN) + PRINTABLE_ASCII_START));
        } else {
          throw new IllegalStateException(p.getName());
        }
        stmts.add(Sequence.createSequenceForPrimitive(lit).getStatement(0));
      }
      return new Sequence(new SimpleArrayList<>(stmts));
    }

    List<Executable> methods() throws NoSuchMethodException {
      Method m = StringBuilder.class.getMethod(name, params.toArray(new Class<?>[0]));
      Method toString = StringBuilder.class.getMethod("toString");
      return Arrays.asList(m, toString);
    }
  }
}
