package randoop.generation.constanttfidf;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
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
   * @param constantStats map from sequence to its usage statistics (in the represented scope)
   * @param numClasses the total number of classes (in the represented scope)
   */
  @SuppressWarnings("keyfor:enhancedfor")
  public TfIdfSelector(
      Map<Sequence, ConstantStatistics.ConstantStats> constantStats, int numClasses) {
    if (DEBUG) {
      Log.logPrintln("Initializing TF-IDF Selector.  Arguments to constructor are:");
      Log.logPrintln("  constant stats: " + constantStats);
      Log.logPrintln("  number of classes: " + numClasses);
    }
    if (constantStats.isEmpty()) {
      Log.logPrintf("TF-IDF Selector: constantStats is empty");
      this.constantWeight = Collections.emptyMap();
      return;
    }

    this.constantWeight = new LinkedHashMap<>();
    for (Map.Entry<Sequence, ConstantStatistics.ConstantStats> entry : constantStats.entrySet()) {
      Sequence sequence = entry.getKey();
      ConstantStatistics.ConstantStats stats = entry.getValue();
      int numUses = stats.getNumUses();
      int numClassesWithConstant = stats.getNumClassesWith();

      // TF-IDF formula: tf(t, D) * log((|D| + 1) / (|D| + 1 - |d \in D : t \in d|))
      // D: a set of classes, which is the represented scope
      // tf(t, D): numUses of constant t in D
      // |D|: number of classes in D
      // |d \in D : t \in d|: number of classes in the current scope that contain constant t.
      double tfidf =
          (double) numUses
              * Math.log((numClasses + 1.0) / ((numClasses + 1.0) - numClassesWithConstant));
      constantWeight.put(sequence, tfidf);
      if (DEBUG) {
        Log.logPrintln("Sequence: " + sequence);
        Log.logPrintln("  NumUses: " + numUses);
        Log.logPrintln("  numClassesWithConstant: " + numClassesWithConstant);
        Log.logPrintln("  TfIdf: " + tfidf);
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
      Log.logPrintln("Candidates: " + candidates);
      Log.logPrintln("tfidf map: " + constantWeight);
    }
    Sequence selectedSequence = Randomness.randomMemberWeighted(candidates, constantWeight);
    if (DEBUG) {
      Log.logPrintf("Selected sequence: " + selectedSequence + "\n");
    }
    return selectedSequence;
  }
}
