package randoop.generation.constanttfidf;

import java.util.HashMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.plumelib.util.SIList;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;
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
   * calculated by the TF-IDF associated with the given type's scope.
   *
   * @param candidates the candidate sequences, all of which have the same return type
   * @param type the type whose scope will be used for TF-IDF calculation
   * @param scopeToConstantStatistics the statistics object to get constant data and scope
   *     information
   * @return the selected sequence, or null if either {@code candidates} is empty or the type has no
   *     constants
   */
  public @Nullable Sequence selectSequence(
      SIList<Sequence> candidates,
      ClassOrInterfaceType type,
      ScopeToConstantStatistics scopeToConstantStatistics) {

    if (candidates.isEmpty()) {
      return null;
    }

    // Get the scope key and constant statistics for the given type
    @Nullable Object scope = scopeToConstantStatistics.getScope(type);

    // Candidates are filtered from constantStats based on the needed type (from
    // ComponentManager.getConstantSequences),
    // while constantStats contains all sequences from the scope regardless of type.
    ConstantStatistics constantStats = scopeToConstantStatistics.getConstantStatistics(type);

    if (constantStats.getConstantUses().isEmpty()) {
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
