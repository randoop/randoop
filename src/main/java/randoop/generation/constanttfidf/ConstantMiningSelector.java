package randoop.generation.constanttfidf;

import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;
import randoop.util.Log;
import randoop.util.SimpleList;

/**
 * A map from a type or package to a TfIdfSelector for it.
 *
 * <p>ConstantMiningSelector is only used when constant mining is enabled and the literal level is
 * either PACKAGE or CLASS.
 *
 * <p>There is only one global ConstantMiningSelector, but its type argument depends on {@link
 * GenInputsAbstract#literals_level}.
 *
 * @param <T> the literal level, either Package or ClassOrInterfaceType
 */
public class ConstantMiningSelector<T> {

  /** If true, output debugging information. */
  private static final boolean DEBUG = false;

  /** Map from a scope (e.g., a Package or ClassOrInterfaceType) to its TfIdfSelector. */
  private Map<T, TfIdfSelector> tfIdfSelectors;

  /** Creates a new ConstantMiningSelector with an empty tfIdfSelectors. */
  public ConstantMiningSelector() {
    tfIdfSelectors = new HashMap<>();
  }

  /**
   * Select a sequence from {@code candidates} based on the weight of the sequence calculated by
   * TF-IDF associated with the given Package or ClassOrInterfaceType.
   *
   * @param candidates the candidate sequences
   * @param scope the literal level
   * @param frequency the frequency information of the sequences associated with the given literal
   *     level
   * @param classesWithConstant the occurrence information of the sequence associated with the given
   *     literal level
   * @param classCount the number of classes in the given literal level
   * @return the selected sequence, or null if either the input candidate sequences or the frequency
   *     information is empty
   */
  public @Nullable Sequence selectSequence(
      SimpleList<Sequence> candidates,
      T scope,
      Map<Sequence, Integer> frequency,
      Map<Sequence, Integer> classesWithConstant,
      Integer classCount) {

    if (candidates.isEmpty() || frequency.isEmpty()) {
      return null;
    }

    if (DEBUG) {
      System.out.printf("Selecting sequence: %s%ntfidf map: %s%n", candidates, tfIdfSelectors);

      if (GenInputsAbstract.literals_level == GenInputsAbstract.ClassLiteralsMode.CLASS) {
        Log.logPrintf("type: " + (ClassOrInterfaceType) scope);
      } else if (GenInputsAbstract.literals_level == GenInputsAbstract.ClassLiteralsMode.PACKAGE) {
        Log.logPrintf("type: " + (Package) scope);
      }
    }

    TfIdfSelector tfIdfSelector =
        tfIdfSelectors.computeIfAbsent(
            scope, __ -> new TfIdfSelector(frequency, classesWithConstant, classCount));
    return tfIdfSelector.selectSequence(candidates);
  }
}
