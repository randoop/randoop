package randoop.generation.ConstantMining;

import java.util.HashMap;
import java.util.Map;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;
import randoop.util.Log;
import randoop.util.SimpleList;

/**
 * Given a scope (ClassOrInterfaceType or Package) and its statistics (frequency and occurrence
 * information), ConstantMiningSelector passes information to the helper class TfIdfSelector to
 * select a sequence from candidates based on its weight. ConstantMiningSelector is only used when
 * constant mining is enabled and the literal level is either PACKAGE or CLASS.
 *
 * <p>There is only one global ConstantMiningSelector, but its type argument depends on {@link
 * GenInputsAbstract#literals_level}.
 *
 * @param <T> the literal level, either Package or ClassOrInterfaceType
 */
public class ConstantMiningSelector<T> {
  /** Map from a specific Package or ClassOrInterfaceType to its TfIdfSelector. */
  private Map<T, TfIdfSelector> tfIdfSelectors;

  /** If true, output debugging information. */
  private static final boolean DEBUG = false;

  /** Creates a new ConstantMiningSelector with an empty tfIdfSelectors. */
  public ConstantMiningSelector() {
    tfIdfSelectors = new HashMap<>();
  }

  /**
   * Select a sequence from {@code candidates} based on the weight of the sequence calculated by
   * TF-IDF associated with the given Package or ClassOrInterfaceType.
   *
   * @param candidates the candidate sequences
   * @param scope the type of the sequence
   * @param frequency the frequency information of the sequences associated with the type
   * @param classesWithConstant, the occurrence information of the sequence associated with the type
   * @param classCount the number of classes in the project
   * @return the selected sequence
   */
  public Sequence selectSequence(
      SimpleList<Sequence> candidates,
      T scope,
      Map<Sequence, Integer> frequency,
      Map<Sequence, Integer> classesWithConstant,
      Integer classCount) {

    if (candidates == null || frequency == null) {
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

    TfIdfSelector weightSelector =
        tfIdfSelectors.computeIfAbsent(
            scope, __ -> new TfIdfSelector(frequency, classesWithConstant, classCount));
    return weightSelector.selectSequence(candidates);
  }
}
