package randoop.generation.constanttfidf;

import java.util.HashMap;
import java.util.Map;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;
import randoop.util.Log;
import randoop.util.list.SimpleList;

/**
 * A map from a scope to a TfIdfSelector. A scope is a type, package, or {@link
 * ConstantMiningStatistics#ALL_SCOPE}. There is only one global ConstantMiningSelector, but its
 * type argument depends on {@link GenInputsAbstract#literals_level}.
 */
public class ConstantMiningSelector {

  /** If true, output debugging information. */
  private static final boolean DEBUG = false;

  /** Map from a scope (e.g., a Package or ClassOrInterfaceType) to its TfIdfSelector. */
  private Map<Object, TfIdfSelector> tfIdfSelectors;

  /** Creates a new, empty ConstantMiningSelector. */
  public ConstantMiningSelector() {
    tfIdfSelectors = new HashMap<>();
  }

  /**
   * Select a sequence from {@code candidates} based on the weight of the sequence calculated by
   * TF-IDF associated with the given Package or ClassOrInterfaceType.
   *
   * @param candidates the candidate sequences
   * @param scope a type, a package, or the ALL_SCOPE (for "all")
   * @param frequency the frequency information of the sequences associated with the given literal
   *     level
   * @param classesWithConstant the occurrence information of the sequence associated with the given
   *     literal level
   * @param classCount the number of classes in the given literal level
   * @return the selected sequence or null if either the input candidate sequences or the frequency
   *     information is empty
   */
  public Sequence selectSequence(
      SimpleList<Sequence> candidates,
      Object scope,
      Map<Sequence, Integer> frequency,
      Map<Sequence, Integer> classesWithConstant,
      Integer classCount) {

    if (candidates.isEmpty() || frequency.isEmpty()) {
      return null;
    }

    if (DEBUG) {
      System.out.printf("Selecting sequence: %s%ntfidf map: %s%n", candidates, tfIdfSelectors);
      Log.logPrintln("scope: " + scope);
    }

    TfIdfSelector tfIdfSelector =
        tfIdfSelectors.computeIfAbsent(
            scope, __ -> new TfIdfSelector(frequency, classesWithConstant, classCount));
    return tfIdfSelector.selectSequence(candidates);
  }
}
