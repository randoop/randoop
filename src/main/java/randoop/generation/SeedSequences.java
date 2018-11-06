package randoop.generation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import randoop.sequence.Sequence;
import randoop.types.JavaTypes;
import randoop.types.Type;

/**
 * Provides functionality for creating a set of sequences that create a set of primitive values.
 * Used by sequence generators.
 */
public final class SeedSequences {
  private SeedSequences() {
    throw new IllegalStateException("no instance");
  }

  /** The initial pool of primitive values. */
  private static final List<Object> primitiveSeeds =
      Arrays.<Object>asList(
          (byte) -1,
          (byte) 0,
          (byte) 1,
          (byte) 10,
          (byte) 100,
          (short) -1,
          (short) 0,
          (short) 1,
          (short) 10,
          (short) 100,
          -1,
          0,
          1,
          10,
          100,
          -1L,
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
   * A set of sequences that create primitive values, e.g. int i = 0; or String s = "hi";
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
        seedSequences.add(Sequence.zero(JavaTypes.STRING_TYPE));
      } else {
        seedSequences.add(Sequence.createSequenceForPrimitive(seed));
      }
    }
    return seedSequences;
  }

  /**
   * Returns the set of seed values with the given raw type.
   *
   * @param type the type
   * @return the set of seed values with the given raw type
   */
  static Set<Object> getSeeds(Type type) {
    Set<Object> result = new LinkedHashSet<>();
    for (Object seed : primitiveSeeds) {
      if (type.isAssignableFromTypeOf(seed)) {
        result.add(seed);
      }
    }
    return result;
  }
}
