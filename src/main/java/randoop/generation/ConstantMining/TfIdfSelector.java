package randoop.generation.ConstantMining;

import java.util.HashMap;
import java.util.Map;
import randoop.sequence.Sequence;
import randoop.util.Log;
import randoop.util.Randomness;
import randoop.util.SimpleList;

public class TfIdfSelector {

  /** Map from sequence to TFIDF weight. */
  Map<Sequence, Double> tfidfMap;

  private static final boolean DEBUG = false;

  // Optimization: Better to also include the type it is associated with

  public TfIdfSelector(
      Map<Sequence, Integer> sequenceFrequency,
      Map<Sequence, Integer> sequenceOccurrence,
      int classCount) {
    if (DEBUG) {
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
    tfidfMap = new HashMap<>();
    // TODO: Test when it is empty
    if (sequenceFrequency.isEmpty()) {
      Log.logPrintf("TFIDF Selector: Sequence frequency is empty");
      return;
    }

    for (Sequence sequence : sequenceFrequency.keySet()) {
      int frequency = sequenceFrequency.get(sequence);
      int occurrence = 1;
      if (sequenceOccurrence != null) { // Which means the literal level is not CLASS
        occurrence = sequenceOccurrence.get(sequence);
      }
      // TODO: add comment for the formula and the paper
      double tfidf = frequency * (classCount + 1.0) / ((classCount + 1.0) - occurrence);
      tfidfMap.put(sequence, tfidf);
      if (DEBUG) {
        Log.logPrintf(
            "Sequence: "
                + sequence
                + "%n"
                + "Frequency: "
                + frequency
                + "%n"
                + "Occurrence: "
                + occurrence
                + "%n"
                + "TfIdf: "
                + tfidf
                + "%n");
      }
    }
    if (DEBUG) {
      Log.logPrintf("TfIdf map: " + tfidfMap + "%n");
    }
  }

  /**
   * Select a sequence from {@code candidates} based on the weight of the sequence calculated by
   * TFIDF.
   *
   * @param candidates the candidate sequences
   * @return the selected sequence
   */
  public Sequence selectSequence(SimpleList<Sequence> candidates) {
    Log.logPrintf("Selecting sequence: " + candidates + "%n" + "tfidf map: " + tfidfMap + "%n");
    // TODO: POTENTIAL BUG: candidates have sequence that is not in tfidfMap. Check if it is
    //  possible
    if (tfidfMap.isEmpty()) {
      if (DEBUG) {
        Log.logPrintf("TfIdfSelector.selectSequence(): tfidfMap is empty");
      }
      return null;
    }
    if (candidates == null || candidates.isEmpty()) {
      Log.logPrintf("TFIDF Selector: Candidates is null or empty");
      return null;
    }
    if (DEBUG) {
      Log.logPrintf(
          "Constant Mining success: Candidates: "
              + candidates
              + "%n"
              + "tfidf map: "
              + tfidfMap
              + "%n");
    }
    return Randomness.randomMemberWeighted(candidates, tfidfMap);
  }
}
