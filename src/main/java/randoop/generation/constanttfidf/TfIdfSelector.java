package randoop.generation.constanttfidf;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.SIList;
import randoop.main.RandoopBug;
import randoop.sequence.Sequence;
import randoop.util.Log;
import randoop.util.Randomness;

/**
 * This class selects a sequence based on TF-IDF. TfIdfSelector is only used when {@code
 * --constant-tfidf} is enabled.
 *
 * <p>There is one TfIdfSelector per scope. See {@link ScopeToTfIdfSelector}.
 */
public class TfIdfSelector {

  /** If true, output debugging information. */
  private static final boolean DEBUG = false;

  /** Map from a sequence to its TF-IDF weight. Once computed, it is never updated. */
  private final Map<Sequence, Double> constantWeight;

  /**
   * Create a TfIdfSelector.
   *
   * <p>The two maps that are passed in have the same keyset.
   *
   * @param numUses map from sequence to its number of uses (in the represented scope)
   * @param classesWithConstant map from sequence to the number of classes (in the represented
   *     scope) that contain the sequence;
   * @param classCount the total number of classes (in the represented scope)
   */
  @SuppressWarnings("keyfor:enhancedfor")
  public TfIdfSelector(
      Map<@KeyFor("#2") Sequence, Integer> numUses,
      Map<@KeyFor("#1") Sequence, Integer> classesWithConstant,
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
      constantWeight = Collections.emptyMap();
      return;
    }

    if (!numUses.keySet().equals(classesWithConstant.keySet())) {
      throw new RandoopBug(
          "Non-matching number of keys (constants): " + numUses + " " + classesWithConstant);
    }

    Map<Sequence, Double> constantWeightTmp = new LinkedHashMap<>();
    for (@KeyFor({"classesWithConstant", "numUses"}) Sequence sequence : numUses.keySet()) {
      int freq = numUses.get(sequence);
      int numClassesWithConstant = classesWithConstant.get(sequence);

      // TF-IDF formula: tf(t, D) * log((|D| + 1) / (|D| + 1 - |d \in D : t \in d|))
      // D: a set of classes, which is the represented scope
      // tf(t, D): numUses of constant t in D
      // |D|: number of classes in D
      // |d \in D : t \in d|: number of classes in the current scope that contain constant t.
      double tfidf =
          (double) freq
              * Math.log((classCount + 1.0) / ((classCount + 1.0) - numClassesWithConstant));
      constantWeightTmp.put(sequence, tfidf);
      if (DEBUG) {
        Log.logPrintf(
            "Sequence: "
                + sequence
                + "%n"
                + "NumUses (frequency): "
                + freq
                + "%n"
                + "numClassesWithConstant: "
                + numClassesWithConstant
                + "%n"
                + "TfIdf: "
                + tfidf
                + "%n");
      }
    }
    this.constantWeight = constantWeightTmp;
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
  public @Nullable Sequence selectSequence(SIList<Sequence> candidates) {
    if (constantWeight.isEmpty()) {
      if (DEBUG) {
        Log.logPrintf("TfIdfSelector.java: constantWeight map is empty");
      }
      return null;
    }
    if (candidates == null) {
      throw new RandoopBug("TF-IDF Selector: Candidates is null");
    }
    if (candidates.isEmpty()) {
      Log.logPrintf("TfIdfSelector.java: candidates is empty");
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
    if (DEBUG) {
      Log.logPrintf("Selected sequence: " + selectedSequence + "\n");
    }
    return selectedSequence;
  }
}
