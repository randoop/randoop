package randoop.generation.constanttfidf;

import java.util.LinkedHashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.SIList;
import randoop.sequence.Sequence;
import randoop.util.Log;
import randoop.util.Randomness;

/**
 * This class selects a sequence based on TF-IDF. TfIdfSelector is only used when {@code
 * --constant-tfidf} is enabled.
 *
 * <p>There is one TfIdfSelector per scope.
 */
public class TfIdfSelector {

  /** If true, output debugging information. */
  private static final boolean DEBUG = false;

  /**
   * Map from a sequence to its TF-IDF weight. Once computed during construction, the map is never
   * modified.
   */
  private final Map<Sequence, Double> constantWeight;

  /**
   * Create a TfIdfSelector.
   *
   * @param constantStats map from sequence to its usage statistics (in the represented scope)
   */
  public TfIdfSelector(ConstantStatistics constantStats) {
    Map<Sequence, ConstantStatistics.ConstantUses> seqToUses = constantStats.getConstantUses();
    int numClasses = constantStats.getNumClasses();

    if (DEBUG) {
      Log.logPrintln("Initializing TF-IDF Selector.  Arguments to constructor are:");
      Log.logPrintln("  constant stats: " + seqToUses);
      Log.logPrintln("  number of classes: " + numClasses);
    }

    this.constantWeight = new LinkedHashMap<>();
    for (Map.Entry<Sequence, ConstantStatistics.ConstantUses> entry : seqToUses.entrySet()) {
      Sequence sequence = entry.getKey();
      ConstantStatistics.ConstantUses stats = entry.getValue();
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
   * @param candidates the candidate sequences which includes constant sequences from
   *     {ComponentManager.getConstantSequences()}
   * @return the selected sequence, or null if constantWeight map is empty or candidates is empty
   */
  public @Nullable Sequence selectSequence(SIList<Sequence> candidates) {
    // Empty when no constants in scope. Defaults to regular selection.
    if (constantWeight.isEmpty()) {
      if (DEBUG) {
        Log.logPrintf("TfIdfSelector.java: constantWeight map is empty");
      }
      return null;
    }
    if (DEBUG) {
      Log.logPrintln("Candidates: " + candidates);
      Log.logPrintln("tfidf map: " + constantWeight);
    }
    Sequence selectedSequence = Randomness.randomMemberWeighted(candidates, constantWeight);
    if (DEBUG) {
      Log.logPrintln("Selected sequence: " + selectedSequence);
    }
    return selectedSequence;
  }
}
