package randoop.generation;

import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.types.Type;

/** Registry of GRT fuzzers. See {@link GrtFuzzer} for details on how fuzzers are used. */
public final class GrtFuzzerRegistry {

  /** List of all fuzzers. */
  private static final List<GrtFuzzer> FUZZERS =
      Arrays.asList(GrtNumericFuzzer.getInstance(), GrtStringFuzzer.getInstance());

  /**
   * Pick the first fuzzer that can handle this type, or null if none.
   *
   * @param type the type to fuzz
   * @return the fuzzer that can handle the type, or null if none can
   */
  public static @Nullable GrtFuzzer pickFuzzer(Type type) {
    for (GrtFuzzer f : FUZZERS) {
      if (f.canFuzz(type)) {
        return f;
      }
    }
    return null;
  }
}
