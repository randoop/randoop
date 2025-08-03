package randoop.generation;

import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.sequence.Sequence;
import randoop.sequence.VarAndSeq;
import randoop.sequence.Variable;
import randoop.types.PrimitiveTypes;
import randoop.types.Type;
import randoop.util.Randomness;

/**
 * Fuzzer for primitive numeric values and {@code char}. Samples a Gaussian delta <em>g ~
 * N(0,sigma^2)</em> (sigma configurable via {@link GenInputsAbstract#grt_fuzzing_stddev}), and adds
 * it to the original.
 */
public final class GrtNumericFuzzer extends GrtFuzzer {

  /* --------------------------- Singleton --------------------------- */

  /** Singleton instance. */
  private static final GrtNumericFuzzer INSTANCE = new GrtNumericFuzzer();

  /**
   * Returns the singleton instance of {@link GrtNumericFuzzer}.
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
  public VarAndSeq fuzz(Sequence sequence, Variable variable) {
    if (sequence.size() == 0) {
      throw new IllegalArgumentException("Cannot fuzz an empty Sequence");
    }

    Type inputType = variable.getType();
    Object lastValue = sequence.getStatement(variable.index).getValue();
    Object fuzzValue;
    if (lastValue instanceof Number) {
      fuzzValue = sampleMutatedValue(inputType, (Number) lastValue);
    } else if (lastValue instanceof Character) {
      fuzzValue = sampleMutatedValue(inputType, (int) ((Character) lastValue));
    } else {
      throw new RandoopBug("Unexpected type " + lastValue.getClass());
    }
    Sequence fuzzedSeq =
        Sequence.concatenate(sequence, Sequence.createSequenceForPrimitive(fuzzValue));
    return new VarAndSeq(variable, fuzzedSeq);
  }

  /**
   * Sample <em>g ~ N(0,sigma^2)</em>, add to orig.doubleValue(), and cast to the right type. Does
   * not return Number because the result may be a Character.
   *
   * @param type the type of the value to fuzz
   * @param orig the original value
   * @return the fuzzed value
   */
  private static Object sampleMutatedValue(Type type, Number orig) {
    Class<?> cls = type.getRuntimeClass();
    double fuzzed =
        orig.doubleValue() + Randomness.nextRandomGaussian(0, GenInputsAbstract.grt_fuzzing_stddev);
    if (cls == byte.class || cls == Byte.class) return (byte) Math.round(fuzzed);
    if (cls == short.class || cls == Short.class) return (short) Math.round(fuzzed);
    if (cls == char.class || cls == Character.class) return (char) Math.round(fuzzed);
    if (cls == int.class || cls == Integer.class) return (int) Math.round(fuzzed);
    if (cls == long.class || cls == Long.class) return Math.round(fuzzed);
    if (cls == float.class || cls == Float.class) return (float) fuzzed;
    if (cls == double.class || cls == Double.class) return fuzzed;
    throw new RandoopBug("Unexpected numeric type " + type);
  }
}
