package randoop.generation;

import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.sequence.VarAndSeq;
import randoop.sequence.Variable;
import randoop.types.Type;

/**
 * Abstract strategy for "impurity fuzzing" as described in <a
 * href="https://people.kth.se/~artho/papers/lei-ase2015.pdf">GRT: Program-Analysis-Guided Random
 * Testing (ASE 2015)</a> (Ma&nbsp;et&nbsp;al., ASE 2015):
 *
 * <p>{@link #fuzz} receives a {@link Sequence} and fuzzes its <em>last</em> variable's value in
 * order to explore additional program states and improve branch coverage. A concrete fuzzer may
 * append extra {@link Statement}s, return an unchanged sequence (if the type is unsupported or
 * uninteresting).
 */
public abstract class GrtFuzzer {

  /** Creates a GrtFuzzer. */
  public GrtFuzzer() {}

  /** List of all fuzzers. */
  @SuppressWarnings("ClassInitializationDeadlock")
  private static final List<GrtFuzzer> FUZZERS =
      Arrays.asList(GrtNumericFuzzer.getInstance(), GrtStringFuzzer.getInstance());

  /**
   * Returns a fuzzer that can handle the given type, or null if none.
   *
   * @param type the type to fuzz
   * @return a fuzzer that can handle the type, or null if none can
   */
  public static @Nullable GrtFuzzer getFuzzer(Type type) {
    for (GrtFuzzer f : FUZZERS) {
      if (f.canFuzz(type)) {
        return f;
      }
    }
    return null;
  }

  /* --------------------------- Instance methods --------------------------- */

  /**
   * Returns {@code true} if this fuzzer can handle the {@code type}.
   *
   * @param type the type to check
   * @return {@code true} if this fuzzer can handle the {@code type}
   */
  public abstract boolean canFuzz(Type type);

  /**
   * Fuzz a variable and append fuzzing statements to {@code sequence}. If the type is unsupported
   * by this fuzzer, the implementation throws an exception.
   *
   * @param sequence the sequence containing the variable to fuzz
   * @param variable the variable whose value will be fuzzed
   * @return a pair of fuzzed variable and sequence with additional fuzzing statements, or the
   *     original variable and sequence if no fuzzing was performed
   */
  public abstract VarAndSeq fuzz(Sequence sequence, Variable variable);
}
