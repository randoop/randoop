package randoop.generation.constanttfidf;

import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import randoop.main.RandoopBug;
import randoop.sequence.Sequence;
import randoop.util.Log;
import randoop.util.Randomness;
import randoop.util.SimpleList;

/**
 * This class selects a sequence based on TF-IDF. TfIdfSelector is only used when {@code
 * --constant-tfidf} is enabled.
 *
 * <p>There is one TfIdfSelector per scope. When the literal level is ClassOrInterfaceType or
 * Package, a scope is a class or a package, respectively. When the literal level is ALL, there is
 * one global TfIdfSelector.
 *
 * <p>By information, it means sequence numUses and number of occurrence.
 */
public class TfIdfSelector {

  /** If true, output debugging information. */
  private static final boolean DEBUG = false;

  /** Map from a sequence to its TF-IDF weight. */
  Map<Sequence, Double> constantWeight = new HashMap<>();

  /**
   * Create a TfIdfSelector.
   *
   * <p>The two maps that are passed in have the same keyset.
   *
   * @param numUses map from sequence to its number of uses (in the represented scope)
   * @param classesWithConstant map from sequence to the number of classes (in the represented
   *     scope) that contain the sequence; null if the literal level is CLASS
   * @param classCount the total number of classes (in the represented scope)
   */
  public TfIdfSelector(
      Map<Sequence, Integer> numUses,
      @Nullable Map<Sequence, Integer> classesWithConstant,
      int classCount) {
    if (DEBUG) {
      Log.logPrintf(
          "Initializing TF-IDF Selector: %n"
              + "Sequence numUses: "
              + numUses
              + "%n"
              + "Sequence occurrence: "
              + classesWithConstant
              + "%n"
              + "Class count: "
              + classCount
              + "%n");
    }
    if (numUses.isEmpty()) {
      Log.logPrintf("TF-IDF Selector: Sequence numUses is empty");
      return;
    }

    if (!numUses.keySet().equals(classesWithConstant.keySet())) {
      throw new RandoopBug(
          "Non-matching number of keys (constants): " + numUses + " " + classesWithConstant);
    }

    for (Sequence sequence : numUses.keySet()) {
      int freq = numUses.get(sequence);
      int numClassesWithConstant;
      if (classesWithConstant != null) {
        // Literal level is either PACKAGE or ALL
        numClassesWithConstant = classesWithConstant.get(sequence);
      } else {
        // Literal level is CLASS
        numClassesWithConstant = 1;
      }
      // TF-IDF formula: tf(t, D) * log((|D| + 1) / (|D| + 1 - |d \in D : t \in d|))
      // D: a set of classes, which is the represented scope
      // tf(t, D): numUses of constant t in D
      // |D|: number of classes in D
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
                + "NumUses: "
                + numUses
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
    if (candidates == null) {
      throw new RandoopBug("TF-IDF Selector: Candidates is null");
    }
    if (candidates.isEmpty()) {
      Log.logPrintf("TF-IDF Selector: Candidates is empty");
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
