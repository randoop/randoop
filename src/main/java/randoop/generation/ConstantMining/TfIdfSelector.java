package randoop.generation.ConstantMining;

import java.util.HashMap;
import java.util.Map;
import randoop.sequence.Sequence;
import randoop.util.Log;
import randoop.util.Randomness;
import randoop.util.SimpleList;

/**
 * This class selects a sequence based on TF-IDF. TfIdfSelector is only used when constant mining is
 * enabled.
 *
 * <p>When the literal level is ClassOrInterfaceType or Package, TfIdfSelector is created and store
 * the constant information inside its corresponding Class or Package, and when the literal level is
 * ALL, TfIdfSelector stores all constants' information instead and only one global TfIdfSelector is
 * created. By information, it means sequence frequency and number of occurrence.
 */
public class TfIdfSelector {

  /** Map from a sequence to its TF-IDF weight. */
  Map<Sequence, Double> constantWeight = new HashMap<>();

  /** If true, output debugging information. */
  private static final boolean DEBUG = false;

  /**
   * Initialize the TfIdfSelector with the frequency of the sequence, the number of classes that
   * contain the sequence, and the total number of classes in the current scope.
   *
   * @param frequency map from sequence to its frequency
   * @param classesWithConstant map from sequence to the number of classes in the current scope that
   *     contain the sequence
   * @param classCount the total number of classes in the current scope
   */
  public TfIdfSelector(
      Map<Sequence, Integer> frequency,
      Map<Sequence, Integer> classesWithConstant,
      int classCount) {
    if (DEBUG) {
      Log.logPrintf(
          "Initializing TF-IDF Selector: %n"
              + "Sequence frequency: "
              + frequency
              + "%n"
              + "Sequence occurrence: "
              + classesWithConstant
              + "%n"
              + "Class count: "
              + classCount
              + "%n");
    }
    // TODO: Test when it is empty
    if (frequency.isEmpty()) {
      Log.logPrintf("TF-IDF Selector: Sequence frequency is empty");
      return;
    }

    for (Sequence sequence : frequency.keySet()) {
      int freq = frequency.get(sequence);
      int numClassesWithConstant;
      if (classesWithConstant != null) {
        // Literal level is either PACKAGE or ALL
        numClassesWithConstant = classesWithConstant.get(sequence);
      } else {
        // Literal level is CLASS
        // Set to 1 to avoid log(1) = 0
        numClassesWithConstant = 1;
      }
      // TF-IDF formula: tf(t, D) * log((|D| + 1) / (|D| + 1 - |d \in D : t \in d|))
      // tf(t, D): frequency of constant t in a set of classes D, where the scope corresponds to the
      // scope that user passes in.
      // |D|: total number of classes corresponding to the scope that user passes in.
      // |d \in D : t \in d|: number of classes in the current scope that contain constant t.
      double tfidf =
          (double) freq
              * Math.log((classCount + 1.0) / ((classCount + 1.0) - numClassesWithConstant));
      constantWeight.put(sequence, tfidf);
      if (DEBUG) {
        Log.logPrintf(
            "Sequence: "
                + sequence
                + "%n"
                + "Frequency: "
                + frequency
                + "%n"
                + "numClassesWithConstant: "
                + numClassesWithConstant
                + "%n"
                + "TfIdf: "
                + tfidf
                + "%n");
      }
    }
    if (DEBUG) {
      Log.logPrintf("TfIdf map: " + constantWeight + "%n");
    }
  }

  /**
   * Select a sequence from {@code candidates} based on TF-IDF.
   *
   * @param candidates the candidate sequences
   * @return the selected sequence
   */
  public Sequence selectSequence(SimpleList<Sequence> candidates) {
    if (constantWeight.isEmpty()) {
      if (DEBUG) {
        Log.logPrintf("TF-IDF Selector: TfIdf map is empty");
      }
      return null;
    }
    if (candidates == null || candidates.isEmpty()) {
      Log.logPrintf("TF-IDF Selector: Candidates is null or empty");
      return null;
    }
    if (DEBUG) {
      Log.logPrintf(
          "Constant Mining success: Candidates: "
              + candidates
              + "%n"
              + "tfidf map: "
              + constantWeight
              + "%n");
    }
    Sequence selectedSequence = Randomness.randomMemberWeighted(candidates, constantWeight);
    Log.logPrintf("Selected sequence: " + selectedSequence + "\n");
    return selectedSequence;
  }
}
