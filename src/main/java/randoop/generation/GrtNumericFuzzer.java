package randoop.generation;

import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;
import randoop.types.PrimitiveTypes;
import randoop.types.Type;
import randoop.util.Randomness;

/**
 * Fuzzer for primitive numeric values and {@code char}. Samples a Gaussian delta <em>g ~
 * N(0,sigma^2)</em> (sigma configurable via {@link GenInputsAbstract#grt_fuzzing_stddev}), and adds
 * it to the original
 */
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
    if (sequence.size() == 0) {
      return sequence; // nothing to fuzz
    }
    Type inputType = sequence.getLastVariable().getType();

    // 1) Grab the last runtime value:
    Object lastValue = sequence.getStatement(sequence.size() - 1).getValue();

    // 2) If it's not a Number, just skip fuzzing:
    if (!(lastValue instanceof Number)) {
      return sequence;
    }

    // 3) Compute mutated value in one shot
    Object fuzzValue = sampleMutatedValue(inputType, (Number) lastValue);

    // 4) Concatenate
    return Sequence.concatenate(sequence, Sequence.createSequenceForPrimitive(fuzzValue));
  }

  /**
   * Sample g ~ N(0,σ²), add to orig.doubleValue(), and cast back to the right wrapper. Returns a
   * Byte, Short, Integer, Long, Float, or Double as needed.
   */
  private static Object sampleMutatedValue(Type type, Number orig) {
    Class<?> cls = type.getRuntimeClass();
    double raw = orig.doubleValue() + Randomness.nextRandomGaussian(0, GAUSSIAN_STD);
    if (cls == byte.class || cls == Byte.class) return (byte) Math.round(raw);
    if (cls == short.class || cls == Short.class) return (short) Math.round(raw);
    if (cls == char.class || cls == Character.class) return (char) Math.round(raw);
    if (cls == int.class || cls == Integer.class) return (int) Math.round(raw);
    if (cls == long.class || cls == Long.class) return Math.round(raw);
    if (cls == float.class || cls == Float.class) return (float) raw;
    // else double/Double:
    return raw;
  }
}
