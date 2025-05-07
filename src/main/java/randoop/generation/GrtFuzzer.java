package randoop.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import randoop.main.RandoopBug;
import randoop.sequence.Sequence;
import randoop.sequence.Sequence.RelativeNegativeIndex;
import randoop.sequence.Statement;
import randoop.types.Type;

/**
 * Abstract strategy for "impurity fuzzing" as described in <a
 * href="https://people.kth.se/~artho/papers/lei-ase2015.pdf">GRT: Program-Analysis-Guided Random
 * Testing (ASE 2015)</a> (Ma&nbsp;et&nbsp;al., ASE 2015):
 *
 * <p>Implementations receive a {@link Sequence} whose <em>last</em> variable's value they wish to
 * fuzz in order to explore additional program states and improve branch coverage. A concrete fuzzer
 * may append extra {@link Statement}s, return an unchanged sequence (if the type is unsupported or
 * uninteresting), or throw a {@link RandoopBug} on error.
 */
public abstract class GrtFuzzer {
  /**
   * Cache mapping a size {@code n} to the unmodifiable list {@code [-n,...,-1]} of {@link
   * RelativeNegativeIndex}.
   */
  protected static final Map<Integer, List<RelativeNegativeIndex>> NEGATIVE_INDEX_CACHE =
      new HashMap<>();

  /**
   * Obtain the list {@code [-size,...,-1]}. This creates the argument list, for a operation that
   * consumes the previous {@code size} values.
   *
   * @param size the size of the list to obtain
   * @return the list {@code [-size,...,-1]}
   */
  protected static List<RelativeNegativeIndex> getRelativeNegativeIndices(int size) {
    return NEGATIVE_INDEX_CACHE.computeIfAbsent(
        size,
        s -> {
          List<RelativeNegativeIndex> list = new ArrayList<>(s);
          for (int i = -s; i < 0; i++) {
            list.add(new RelativeNegativeIndex(i));
          }
          return Collections.unmodifiableList(list);
        });
  }

  /** Returns {@code true} if this fuzzer can handle the {@code type}. */
  public abstract boolean canFuzz(Type type);

  /**
   * Appends fuzzing statements to {@code sequence}. If the type is unsupported by this fuzzer, the
   * implementation returns the original sequence.
   *
   * @param sequence the sequence whose <em>last</em> value will be fuzzed
   * @return a new sequence with additional fuzzing statements, or the original sequence if no
   *     fuzzing was performed
   */
  public abstract Sequence fuzz(Sequence sequence);
}
