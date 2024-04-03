package randoop.generation.ConstantMining;

import java.util.HashMap;
import java.util.Map;
import randoop.sequence.Sequence;
import randoop.util.Log;
import randoop.util.Randomness;
import randoop.util.SimpleList;

/**
 * This is the helper class that calculates the weight of the sequence based on the TFIDF and select
 * a sequence from candidates based on the weight. When the literal level is ClassOrInterfaceType or
 * Package, TfIdfSelector is created and store the constant information inside its corresponding
 * Class or Package, and when the literal level is ALL, TfIdfSelector stores all constants'
 * information instead and only one global TfIdfSelector is created. By information, it means
 * sequence frequency and number of occurrence. TfIdfSelector is only used when constant mining is
 * enabled.
 */
public class TfIdfSelector {

  /** Map from a sequence to its corresponding weight based on TF-IDF */
  Map<Sequence, Double> constantWeight = new HashMap<>();

  private static final boolean DEBUG_Constant_Mining = true;

  /**
   * Initialize the TfIdfSelector with the frequency of the sequence, the number of classes that
   * contain the sequence, and the total number of classes in the current scope.
   *
   * @param frequency The frequency of the sequence
   * @param classesWithConstant The number of classes in the current scope that contain the sequence
   * @param classCount The total number of classes in the current scope
   */
  public TfIdfSelector(
      Map<Sequence, Integer> frequency,
      Map<Sequence, Integer> classesWithConstant,
      int classCount) {
    if (DEBUG_Constant_Mining) {
      Log.logPrintf(
          "Initializing TFIDF Selector: %n"
              + "Sequence frequency: "
              + frequency
              + "\n"
              + "Sequence occurrence: "
              + classesWithConstant
              + "\n"
              + "Class count: "
              + classCount
              + "\n");
    }
    // TODO: Test when it is empty
    if (frequency.isEmpty()) {
      Log.logPrintf("TFIDF Selector: Sequence frequency is empty");
      return;
    }

    for (Sequence sequence : frequency.keySet()) {
      int freq = frequency.get(sequence);
      int classesWithConstants;
      if (classesWithConstant != null) {
        // Literal level is either PACKAGE or ALL
        classesWithConstants = classesWithConstant.get(sequence);
      } else {
        // Literal level is CLASS
        // Set to 1 to avoid log(1) = 0
        classesWithConstants = 1;
      }
      // TF-IDF formula: tf(t, D) * log((|D| + 1) / (|D| + 1 - |d \in D : t \in d|))
      // tf(t, D): frequency of constant t in a set of classes D, where the scope corresponds to the
      // scope that user passes in
      // |D|: total number of classes corresponding to the scope that user passes in
      // |d \in D : t \in d|: number of classes in the current scope that contain constant t
      double tfidf =
          (double) freq
              * Math.log(
                  ((double) classCount + 1)
                      / (((double) classCount + 1) - (double) classesWithConstants));
      constantWeight.put(sequence, tfidf);
      if (DEBUG_Constant_Mining) {
        Log.logPrintf(
            "Sequence: "
                + sequence
                + "\n"
                + "Frequency: "
                + frequency
                + "\n"
                + "Occurrence: "
                + classesWithConstants
                + "\n"
                + "TfIdf: "
                + tfidf
                + "\n");
      }
    }
    if (DEBUG_Constant_Mining) {
      Log.logPrintf("TfIdf map: " + constantWeight + "%n");
    }
  }

  /**
   * Select a sequence from candidates based on the weight of the sequence calculated by TFIDF.
   *
   * @param candidates The candidate sequences
   * @return The selected sequence
   */
  public Sequence selectSequence(SimpleList<Sequence> candidates) {
    if (constantWeight.isEmpty()) {
      if (DEBUG_Constant_Mining) {
        Log.logPrintf("TFIDF Selector: TfIdf map is empty");
      }
      return null;
    }
    if (candidates == null || candidates.isEmpty()) {
      Log.logPrintf("TFIDF Selector: Candidates is null or empty");
      return null;
    }
    if (DEBUG_Constant_Mining) {
      Log.logPrintf(
          "Constant Mining success: Candidates: "
              + candidates
              + "\n"
              + "tfidf map: "
              + constantWeight
              + "\n");
    }
    Sequence selectedSequence = Randomness.randomMemberWeighted(candidates, constantWeight);
    Log.logPrintf("Selected sequence: " + selectedSequence + "\n");
    return selectedSequence;
  }
}
