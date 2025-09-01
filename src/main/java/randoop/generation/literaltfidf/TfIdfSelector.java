package randoop.generation.literaltfidf;

import java.util.LinkedHashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.SIList;
import randoop.sequence.Sequence;
import randoop.util.Log;
import randoop.util.Randomness;

/**
 * This class selects a sequence based on TF-IDF. TfIdfSelector is only used when {@code
 * --literal-tfidf} is enabled.
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
  private final Map<Sequence, Double> literalWeight;

  /**
   * Create a TfIdfSelector.
   *
   * @param literalStats map from sequence to its usage statistics (in the represented scope)
   */
  public TfIdfSelector(LiteralStatistics literalStats) {
    Map<Sequence, LiteralStatistics.LiteralUses> seqToUses = literalStats.getLiteralUses();
    int numClasses = literalStats.getNumClasses();

    if (DEBUG) {
      Log.logPrintln("Initializing TF-IDF Selector (" + numClasses + " classes) from:");
      Log.logPrintln("  literal stats: " + seqToUses);
    }

    this.literalWeight = new LinkedHashMap<>();
    for (Map.Entry<Sequence, LiteralStatistics.LiteralUses> entry : seqToUses.entrySet()) {
      Sequence sequence = entry.getKey();
      LiteralStatistics.LiteralUses litUses = entry.getValue();
      int numUses = litUses.getNumUses();
      int numClassesWithLiteral = litUses.getNumClassesWith();

      // TF-IDF formula: tf(t, D) * log((|D| + 1) / (|D| + 1 - |d \in D : t \in d|))
      // D: a set of classes, which is the represented scope
      // tf(t, D): numUses of literal t in D
      // |D|: number of classes in D
      // |d \in D : t \in d|: number of classes in the current scope that contain literal t.
      double tfidf =
          (double) numUses
              * Math.log((numClasses + 1.0) / ((numClasses + 1.0) - numClassesWithLiteral));
      literalWeight.put(sequence, tfidf);
      if (DEBUG) {
        Log.logPrintln("Sequence: " + sequence);
        Log.logPrintln("  NumUses: " + numUses);
        Log.logPrintln("  numClassesWithLiteral: " + numClassesWithLiteral);
        Log.logPrintln("  TfIdf: " + tfidf);
      }
    }
    if (DEBUG) {
      Log.logPrintln("TfIdf map: " + literalWeight);
    }
  }

  /**
   * Select a sequence from {@code candidates} based on TF-IDF.
   *
   * @param candidates the candidate sequences which includes literal sequences from
   *     {ComponentManager.getLiteralSequences()}
   * @return the selected sequence, or null if there are no sequences in this
   */
  public @Nullable Sequence selectSequence(SIList<Sequence> candidates) {
    // Empty when no literals in scope.
    if (literalWeight.isEmpty()) {
      if (DEBUG) {
        Log.logPrintf("TfIdfSelector.java: literalWeight map is empty");
      }
      return null;
    }
    if (DEBUG) {
      Log.logPrintln("Candidates: " + candidates);
      Log.logPrintln("tfidf map: " + literalWeight);
    }
    Sequence selectedSequence = Randomness.randomMemberWeighted(candidates, literalWeight);
    if (DEBUG) {
      Log.logPrintln("Selected sequence: " + selectedSequence);
    }
    return selectedSequence;
  }
}
