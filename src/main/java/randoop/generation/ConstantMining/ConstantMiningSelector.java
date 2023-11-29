package randoop.generation.ConstantMining;

import java.util.HashMap;
import java.util.Map;

import randoop.util.Log;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;
import randoop.util.SimpleList;

public class ConstantMiningSelector<T> {
  /** Map from type to TfIdfSelector */
  private Map<T, TfIdfSelector> constantMap;

  public ConstantMiningSelector() {
    constantMap = new HashMap<>();
  }

  /**
   * Select a sequence from candidates based on the weight of the sequence calculated by TFIDF
   * associated with the given type.
   *
   * @param candidates The candidate sequences
   * @param type The type of the sequence
   * @param sequenceFrequency The frequency information of the sequences associated with the type
   * @param sequenceOccurrence The occurrence information of the sequence associated with the type
   * @param classCount The number of classes in the project
   * @return The selected sequence
   */
  public Sequence selectSequence(
      SimpleList<Sequence> candidates,
      T type,
      Map<Sequence, Integer> sequenceFrequency,
      Map<Sequence, Integer> sequenceOccurrence,
      int classCount) {
    // TODO: This can be also implemented by validation in ForwardGenerator before calling this
    //  method
    if (candidates == null || sequenceFrequency == null) {
      return null;
    }
    System.out.println(
        "Selecting sequence: "
            + candidates
            + "%n"
            + "tfidf map: "
            + constantMap
            + "%n"
            + "%n");
    if (GenInputsAbstract.literals_level == GenInputsAbstract.ClassLiteralsMode.CLASS) {
      Log.logPrintf("type: " + (ClassOrInterfaceType) type);
    } else if (GenInputsAbstract.literals_level == GenInputsAbstract.ClassLiteralsMode.PACKAGE) {
      Log.logPrintf("type: " + (Package) type);
    }
    TfIdfSelector weightSelector =
        constantMap.computeIfAbsent(
            type, __ -> new TfIdfSelector(sequenceFrequency, sequenceOccurrence, classCount));
    return weightSelector.selectSequence(candidates);
  }
}
