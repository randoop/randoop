package randoop.generation.ConstantMining;

import java.util.HashMap;
import java.util.Map;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;
import randoop.util.Log;
import randoop.util.SimpleList;

/**
 * Given the specific ClassOrInterfaceType or Package and their frequency and occurrence
 * information, ConstantMiningSelector passes information to the helper class TfIdfSelector to
 * select a sequence from candidates based on its weight. ConstantMiningSelector is only used when
 * constant mining is enabled and the literal level is either PACKAGE or CLASS, and there is only
 * one global ConstantMiningSelector.
 *
 * @param <T> The literal level that user pass in, either Package or ClassOrInterfaceType
 */
public class ConstantMiningSelector<T> {
  /** A map from a specific Package or ClassOrInterfaceType to its TfIdfSelector. */
  private Map<T, TfIdfSelector> tfIdfSelectors;

  private static final boolean DEBUG_Constant_Mining = false;

  public ConstantMiningSelector() {
    tfIdfSelectors = new HashMap<>();
  }

  /**
   * Given a desired Package or ClassOrInterfaceType, select a sequence from {@code candidates}
   * based on the weight of the sequence calculated by TFIDF.
   *
   * @param candidates The candidate sequences
   * @param curScope The specific ClassOrInterfaceType or Package that the caller wants to select a
   *     sequence
   * @param frequency The frequency information of the sequences associated with the type
   * @param classesWithConstant The occurrence information of the sequence associated with the type
   * @param classCount The number of classes in the project
   * @return The selected sequence
   */
  public Sequence selectSequence(
      SimpleList<Sequence> candidates,
      T curScope,
      Map<Sequence, Integer> frequency,
      Map<Sequence, Integer> classesWithConstant,
      Integer classCount) {

    if (candidates == null || frequency == null) {
      return null;
    }

    if (DEBUG_Constant_Mining) {
      System.out.println(
          "Selecting sequence: "
              + candidates
              + "\n"
              + "tfidf map: "
              + tfIdfSelectors.toString()
              + "\n");
      if (GenInputsAbstract.literals_level == GenInputsAbstract.ClassLiteralsMode.CLASS) {
        Log.logPrintf("type: " + (ClassOrInterfaceType) curScope);
      } else if (GenInputsAbstract.literals_level == GenInputsAbstract.ClassLiteralsMode.PACKAGE) {
        Log.logPrintf("type: " + (Package) curScope);
      }
    }

    TfIdfSelector weightSelector =
        tfIdfSelectors.computeIfAbsent(
            curScope, __ -> new TfIdfSelector(frequency, classesWithConstant, classCount));
    return weightSelector.selectSequence(candidates);
  }
}
