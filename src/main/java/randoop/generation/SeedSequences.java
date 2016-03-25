package randoop.generation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.sequence.Sequence;
import randoop.types.ConcreteType;
import randoop.types.PrimitiveTypes;
import randoop.util.Reflection;

/**
 * Provides functionality for creating a set of sequences that create a set of
 * primitive values. Used by sequence generators.
 */
public final class SeedSequences {
  private SeedSequences() {
    throw new IllegalStateException("no instance");
  }

  public static final List<Object> primitiveSeeds =
      Arrays.<Object>asList(
          (byte) (-1),
          (byte) 0,
          (byte) 1,
          (byte) 10,
          (byte) 100,
          (short) (-1),
          (short) 0,
          (short) 1,
          (short) 10,
          (short) 100,
          (-1),
          0,
          1,
          10,
          100,
          (-1L),
          0L,
          1L,
          10L,
          100L,
          (float) -1.0,
          (float) 0.0,
          (float) 1.0,
          (float) 10.0,
          (float) 100.0,
          -1.0,
          0.0,
          1.0,
          10.0,
          100.0,
          '#',
          ' ',
          '4',
          'a',
          true,
          false,
          "",
          "hi!");

  /**
   * A set of sequences that create primitive values, e.g. int i = 0; or String
   * s = "hi";
   *
   * @return the default set of seed sequences
   */
  public static Set<Sequence> defaultSeeds() {
    List<Object> seeds = new ArrayList<>(primitiveSeeds);
    return SeedSequences.objectsToSeeds(seeds);
  }

  public static Set<Sequence> objectsToSeeds(List<Object> seeds) {
    Set<Sequence> seedSequences = new LinkedHashSet<>();
    for (Object seed : seeds) {
      if (seed == null) {
        seedSequences.add(Sequence.zero(ConcreteType.STRING_TYPE));
      } else {
        seedSequences.add(Sequence.createSequenceForPrimitive(seed));
      }
    }
    return seedSequences;
  }

  /**
   * Returns the set of seed values with the given raw type.
   *
   * @param type  the type
   * @return the set of seed values with the given raw type
   */
  public static Set<Object> getSeeds(ConcreteType type) {
    Set<Object> result = new LinkedHashSet<>();
    for (Object seed : primitiveSeeds) {
      boolean seedOk = isTypeForValue(type.getRuntimeClass(), seed);
      if (seedOk) result.add(seed);
    }
    return result;
  }

  /**
   * Indicates whether the seed value has the given raw type.
   *
   * @param type  the type
   * @param seedValue  the value
   * @return true if {@code type} is the type of the value, false otherwise
   */
  private static boolean isTypeForValue(Class<?> type, Object seedValue) {
    if (PrimitiveTypes.isBoxedPrimitiveTypeOrString(type)) {
      type = PrimitiveTypes.toUnboxedType(type);
    }
    return Reflection.canBePassedAsArgument(seedValue, type);
  }
}
