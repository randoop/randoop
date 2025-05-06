package randoop.generation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.operation.PlusOperation;
import randoop.operation.TypedTermOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.types.PrimitiveType;
import randoop.types.PrimitiveTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;

/**
 * Fuzzer for primitive numeric values and {@code char}. It duplicates the last value, samples a
 * Gaussian delta <em>g ~ N(0,sigma^2)</em> (sigma configurable via {@link
 * GenInputsAbstract#grt_fuzzing_stddev}), and appends a shared {@code +} operation to obtain <code>
 * value + g</code>.
 */
@SuppressWarnings("NotJavadoc")
public final class GrtNumericFuzzer extends GrtFuzzer {

  /* --------------------------- Singleton --------------------------- */
  /** Singleton instance. */
  private static final GrtNumericFuzzer INSTANCE = new GrtNumericFuzzer();

  /**
   * Obtain the singleton instance of {@link GrtNumericFuzzer}.
   *
   * @return the singleton instance
   */
  public static GrtNumericFuzzer getInstance() {
    return INSTANCE;
  }

  /** Private constructor to enforce singleton. */
  private GrtNumericFuzzer() {
    /* no-op */
  }

  /* ------------------------------- Constants ------------------------------ */
  /** Standard deviation sigma for Gaussian fuzzing of numeric types. */
  private static final double GAUSSIAN_STD = GenInputsAbstract.grt_fuzzing_stddev;

  /** Shared {@link PlusOperation} instance (stateless). */
  private static final PlusOperation PLUS_OP = new PlusOperation();

  /** Cache: for each primitive numeric type, a prepared <code>x+y</code> statement. */
  private static final Map<PrimitiveType, Statement> PLUS_STMTS;

  static {
    Map<PrimitiveType, Statement> m = new HashMap<>();
    for (Class<?> c :
        Arrays.asList(
            byte.class,
            short.class,
            char.class,
            int.class,
            long.class,
            float.class,
            double.class)) {
      PrimitiveType pt = PrimitiveType.forClass(c);
      m.put(pt, createPlusStatement(pt));
    }
    PLUS_STMTS = Collections.unmodifiableMap(m);
  }

  /* ------------------------------- API ------------------------------------ */
  @Override
  public boolean canFuzz(Type type) {
    Class<?> runtimeClass = type.getRuntimeClass();
    return runtimeClass != void.class
        && runtimeClass != boolean.class
        && runtimeClass != Boolean.class
        && (runtimeClass.isPrimitive() || PrimitiveTypes.isBoxedPrimitive(runtimeClass));
  }

  @Override
  public Sequence fuzz(Sequence sequence) {
    Type inputType = sequence.getLastVariable().getType();
    try {
      // 1. Re-emit last value as a literal to avoid repeating impure stmt
      Statement valueLit =
          Sequence.createSequenceForPrimitive(sequence.getStatement(sequence.size() - 1).getValue())
              .getStatement(0);

      // 2. Gaussian delta literal
      Statement deltaLit =
          Sequence.createSequenceForPrimitive(sampleGaussian(inputType)).getStatement(0);

      // 3. + operation
      Statement plus = PLUS_STMTS.get(PrimitiveType.forClass(inputType.getRuntimeClass()));

      // 4. Concatenate
      return Sequence.concatenate(
          sequence, new Sequence(new SimpleArrayList<>(Arrays.asList(valueLit, deltaLit, plus))));

    } catch (Exception e) {
      throw new RandoopBug("Numeric fuzzing failed: " + e.getMessage(), e);
    }
  }

  /* ------------------------- Helper methods ------------------------------ */
  /** Build a {@code +} statement whose two operands are the previous two values. */
  private static Statement createPlusStatement(PrimitiveType type) {
    TypedTermOperation op =
        new TypedTermOperation(PLUS_OP, new TypeTuple(Arrays.asList(type, type)), type);
    return new Statement(op, getRelativeNegativeIndices(2));
  }

  /** Sample from N(0,sigma^2) and cast to the boxed primitive matching {@code type}. */
  private static Object sampleGaussian(Type type) {
    Class<?> cls = type.getRuntimeClass();
    double g = Randomness.nextRandomGaussian(0, GAUSSIAN_STD);
    if (cls == byte.class || cls == Byte.class) return (byte) Math.round(g);
    if (cls == short.class || cls == Short.class) return (short) Math.round(g);
    if (cls == char.class || cls == Character.class) return (char) Math.round(g);
    if (cls == int.class || cls == Integer.class) return (int) Math.round(g);
    if (cls == long.class || cls == Long.class) return Math.round(g);
    if (cls == float.class || cls == Float.class) return (float) g;
    return g; // double / Double
  }
}
