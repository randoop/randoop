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
 * information, ConstantMiningSelector selects a sequence from candidates based on its weight
 * calculated by TFIDF. ConstantMiningSelector is only used when constant mining is enabled and the
 * literal level is either PACKAGE or CLASS, and there is only one global ConstantMiningSelector.
 *
 * @param <T> The literal level that user pass in, either Package or ClassOrInterfaceType
 */
public class ConstantMiningSelector<T> {
  /** A map from each specific Package or ClassOrInterfaceType to its TfIdfSelector. */
  private Map<T, TfIdfSelector> constantMap;

  private static final boolean DEBUG_Constant_Mining = false;

  public ConstantMiningSelector() {
    constantMap = new HashMap<>();
  }

  /**
   * Given a desired Package or ClassOrInterfaceType, select a sequence from {@code candidates}
   * based on the weight of the sequence calculated by TFIDF.
   *
   * @param candidates The candidate sequences
   * @param classOrPackage The specific ClassOrInterfaceType or Package that the caller wants to
   *     select a sequence
   * @param sequenceFrequency The frequency information of the sequences associated with the type
   * @param sequenceOccurrence The occurrence information of the sequence associated with the type
   * @param classCount The number of classes in the project
   * @return The selected sequence
   */
  public Sequence selectSequence(
      SimpleList<Sequence> candidates,
      // TODO: This is badly named. It refers to the specific Class or Package, not the literal
      // level
      T classOrPackage,
      Map<Sequence, Integer> sequenceFrequency,
      Map<Sequence, Integer> sequenceOccurrence,
      int classCount) {
    // TODO: This can be also implemented by validation in ForwardGenerator before calling this
    //  method
    if (candidates == null || sequenceFrequency == null) {
      return null;
    }

    if (DEBUG_Constant_Mining) {
      System.out.println(
          "Selecting sequence: " + candidates + "%n" + "tfidf map: " + constantMap + "%n" + "%n");
      if (GenInputsAbstract.literals_level == GenInputsAbstract.ClassLiteralsMode.CLASS) {
        Log.logPrintf("type: " + (ClassOrInterfaceType) classOrPackage);
      } else if (GenInputsAbstract.literals_level == GenInputsAbstract.ClassLiteralsMode.PACKAGE) {
        Log.logPrintf("type: " + (Package) classOrPackage);
      }
    }

    TfIdfSelector weightSelector =
        constantMap.computeIfAbsent(
            classOrPackage,
            __ -> new TfIdfSelector(sequenceFrequency, sequenceOccurrence, classCount));
    return weightSelector.selectSequence(candidates);
  }
}
