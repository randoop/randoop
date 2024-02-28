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

  private static final boolean DEBUG_Constant_Mining = false;

  // Optimization: Better to also include the type it is associated with

  public TfIdfSelector(
      Map<Sequence, Integer> sequenceFrequency,
      Map<Sequence, Integer> sequenceOccurrence,
      int classCount) {
    if (DEBUG_Constant_Mining) {
      Log.logPrintf(
          "Initializing TFIDF Selector: %n"
              + "Sequence frequency: "
              + sequenceFrequency
              + "%n"
              + "Sequence occurrence: "
              + sequenceOccurrence
              + "%n"
              + "Class count: "
              + classCount
              + "%n");
    }
    // TODO: Test when it is empty
    if (sequenceFrequency.isEmpty()) {
      Log.logPrintf("TFIDF Selector: Sequence frequency is empty");
      return;
    }

    for (Sequence sequence : sequenceFrequency.keySet()) {
      int frequency = sequenceFrequency.get(sequence);
      int classesWithConstants;
      if (sequenceOccurrence != null) {
        // Literal level is either PACKAGE or ALL
        classesWithConstants = sequenceOccurrence.get(sequence);
      } else {
        // Literal level is CLASS
        classesWithConstants = 0;
      }
      // TODO: add comment for the formula and the paper
      double tfidf =
          (double) frequency
              * Math.log(((double) classCount + 1)
              / (((double) classCount + 1) - (double) classesWithConstants));
      constantWeight.put(sequence, tfidf);
      if (DEBUG_Constant_Mining) {
        Log.logPrintf(
            "Sequence: "
                + sequence
                + "%n"
                + "Frequency: "
                + frequency
                + "%n"
                + "Occurrence: "
                + classesWithConstants
                + "%n"
                + "TfIdf: "
                + tfidf
                + "%n");
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
    Log.logPrintf(
        "Selecting sequence: " + candidates + "%n" + "tfidf map: " + constantWeight + "%n");
    // TODO: POTENTIAL BUG: candidates have sequence that is not in tfidfMap. Check if it is
    //  possible
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
              + "%n"
              + "tfidf map: "
              + constantWeight
              + "%n");
    }
    return Randomness.randomMemberWeighted(candidates, constantWeight);
  }
}
