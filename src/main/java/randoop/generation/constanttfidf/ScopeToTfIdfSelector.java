package randoop.generation.constanttfidf;

import java.util.HashMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.SIList;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;
import randoop.util.Log;

/**
 * A map from a scope to a {@link TfIdfSelector}. A scope is a type, package, or {@link
 * ScopeToConstantStatistics#ALL_SCOPE}. There is only one global {@link ScopeToTfIdfSelector}, but
 * the type of its scopes depends on {@link GenInputsAbstract#literals_level}.
 */
public class ScopeToTfIdfSelector {

  /** If true, output debugging information. */
  private static final boolean DEBUG = false;

  /**
   * Map from a scope to its TfIdfSelector. All the scopes (keys) have the same type, which is
   * ClassOrInterfaceType or Package or {@link ScopeToConstantStatistics#ALL_SCOPE}.
   */
  private HashMap<@Nullable Object, TfIdfSelector> tfIdfSelectors = new HashMap<>();

  /** Creates a new, empty ScopeToTfIdfSelector. */
  public ScopeToTfIdfSelector() {}

  /**
   * Select a sequence from {@code candidates} based on the weight of the sequence. The weight is
   * calculated by the TF-IDF associated with the given scope.
   *
   * @param candidates the candidate sequences, all of which have the same return type
   * @param scope a type, a package, or {@link ScopeToConstantStatistics#ALL_SCOPE}
   * @param constantStats the constant statistics for the given scope
   * @return the selected sequence, or null if either {@code candidates} or {@code constantStats} is
   *     empty
   */
  public @Nullable Sequence selectSequence(
      SIList<Sequence> candidates, @Nullable Object scope, ConstantStatistics constantStats) {

    if (candidates.isEmpty() || constantStats.getConstantUses().isEmpty()) {
      return null;
    }

    if (DEBUG) {
      System.out.println("Selecting sequence: " + candidates);
      System.out.println("tfidf map: " + tfIdfSelectors);
      Log.logPrintln("scope: " + scope);
    }

    TfIdfSelector tfIdfSelector =
        tfIdfSelectors.computeIfAbsent(scope, __ -> new TfIdfSelector(constantStats));
    return tfIdfSelector.selectSequence(candidates);
  }
}
