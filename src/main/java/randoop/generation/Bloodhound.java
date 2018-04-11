package randoop.generation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import randoop.operation.TypedOperation;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;

/**
 * Implements the Bloodhound component, as described by the Guided Random Testing (GRT) paper.
 * Bloodhound computes a weight for each method under test by taking a weighted combination of the
 * uncovered branch ratio and the ratio between the number of times the method has been selected and
 * the maximum number of times any method under test has been selected.
 *
 * <p>However, some hyper-parameters and edge cases were left unspecified in the GRT paper. We have
 * chosen our own values for the following unspecified hyper-parameters:
 *
 * <ul>
 *   <li>alpha - parameter to balance branch coverage and number of invocations when computing
 *       weight.
 *   <li>p - parameter for decreasing weights of methods between updates to coverage information.
 *   <li>Interval for recomputing branch coverage information.
 *   <li>Default weight for cases where a method has zero branches or computed weight is zero.
 * </ul>
 */
public class Bloodhound implements TypedOperationSelector {

  /**
   * Map of methods under test to their weights. These weights are dynamic and depend on branch
   * coverage.
   */
  private final Map<TypedOperation, Double> methodWeights = new HashMap<>();

  /**
   * Map of methods under test to the number of times they have been recently selected by the {@link
   * ForwardGenerator} to construct a new sequence. This map is cleared every time branch coverage
   * is recomputed.
   */
  private final Map<TypedOperation, Integer> currMethodSelectionCounts = new HashMap<>();

  /**
   * Map of methods under test to the total number of times they have ever been selected by the
   * {@link ForwardGenerator} to extend an existing sequence to construct a new sequence. This
   * definition is the same as that of {@code currMethodSelectionCounts} except that we do not clear
   * this map every time we recompute branch coverage. Thus, the integer value for a given method is
   * non-decreasing during a run of Randoop. The GRT paper does not state its definition of the
   * "number of invocations" of a method under test.
   */
  private final Map<TypedOperation, Integer> methodSelectionTotals = new HashMap<>();

  /** List of operations, identical to ForwardGenerator's operation list. */
  private final SimpleArrayList<TypedOperation> operationSimpleList;

  /**
   * Hyper-parameter for balancing branch coverage and number of times a method was chosen. The name
   * alpha is from the GRT paper.
   */
  private final double alpha = 0.7;

  /**
   * Hyper-parameter for decreasing weights of methods between updates to coverage information. The
   * name p is from the GRT paper.
   */
  private final double p = 0.5;

  /** How often to recompute branch coverage, in steps. */
  private final int branchCoverageInterval = 100;

  /** Maximum number of times any method under test has been selected. */
  private int maxSelectionsOfAnyMethod = 0;

  /**
   * The number of times {@code step()} has been invoked in {@link ForwardGenerator} to construct a
   * new sequence.
   */
  private int stepNum = 0;

  /**
   * Construct a new instance of Bloodhound, which will choose from among the given operations.
   *
   * @param operations list of operations to copy
   */
  public Bloodhound(List<TypedOperation> operations) {
    this.operationSimpleList = new SimpleArrayList<>(operations);

    // Initialize the weight of every method to be uniform, random probability.
    for (TypedOperation operation : this.operationSimpleList) {
      methodWeights.put(operation, 1.0 / this.operationSimpleList.size());
    }
  }

  /**
   * The branch coverage information for all methods under test is re-summarized at every {@code
   * branchCoverageInterval}'th call of this method. Weights for all methods under test are
   * recomputed when branch information is re-summarized.
   */
  private void updateBranchCoverageMaybe() {
    stepNum += 1;
    if (stepNum % branchCoverageInterval == 0) {
      currMethodSelectionCounts.clear();
      CoverageTracker.instance.summarizeCoverageInformation();
      updateWeightsForAllOperations();
    }
  }

  /** Computes and updates weights in our method weights map for all methods under test. */
  private void updateWeightsForAllOperations() {
    for (TypedOperation operation : operationSimpleList) {
      updateWeightForOperation(operation);
    }
  }

  /**
   * Recompute weights for a method under test. A method under test is assigned a weight based on a
   * weighted combination of the number of branches uncovered and the ratio between the number of
   * times this method has been selected and the maximum number of times any method under test has
   * been selected. The weighting scheme is based on Bloodhound in the Guided Random Testing (GRT)
   * paper.
   *
   * @param operation method to compute weight for
   */
  private void updateWeightForOperation(TypedOperation operation) {
    // The number of methods under test, corresponds to |M| in the GRT paper.
    int numOperations = this.operationSimpleList.size();

    // Default weight is uniform probability.  It is used if no coverage details are available.
    // This is the case for classes like java.lang.Object which is not explicitly under test.
    double weight = 1.0 / numOperations;

    CoverageTracker.BranchCoverage covDet =
        CoverageTracker.instance.getDetailsForMethod(operation.getName());

    // Check that branch coverage details are available for this method.
    if (covDet != null) {
      // The number of successful invocations of this method. Corresponds to succ(m).
      Integer numSuccessfulInvocations = methodSelectionTotals.get(operation);
      if (numSuccessfulInvocations == null) {
        numSuccessfulInvocations = 0;
      }

      // If this method has no uncovered branches, or it is the most-selected method,
      // use the default weight and skip this step.
      if (covDet.uncoveredBranches != 0 && numSuccessfulInvocations != maxSelectionsOfAnyMethod) {
        // Uncovered branch ratio of this method. The name uncovRatio(m) is from the GRT paper.
        double uncovRatio;
        if (covDet.totalBranches == 0) {
          uncovRatio = 0.5;
        } else {
          uncovRatio = (double) covDet.uncoveredBranches / covDet.totalBranches;
        }

        // Call ratio of this method. Corresponds to succ(m) / maxSucc(M) in the GRT paper.
        double callRatio;

        if (maxSelectionsOfAnyMethod == 0) {
          callRatio = 0.5;
        } else {
          callRatio = numSuccessfulInvocations.doubleValue() / maxSelectionsOfAnyMethod;
        }

        // Corresponds to w(m, 0) in the GRT paper.
        weight = alpha * uncovRatio + (1.0 - alpha) * (1.0 - callRatio);

        // Corresponds to the k variable in the GRT paper.
        Integer numSelectionsOfMethod = currMethodSelectionCounts.get(operation);
        if (numSelectionsOfMethod != null) {
          // Corresponds to the case where k >= 1 in the GRT paper.
          double val1 =
              (-3.0 / Math.log(1 - p))
                  * (Math.pow(p, numSelectionsOfMethod) / numSelectionsOfMethod);
          double val2 = 1.0 / Math.log(numOperations + 3);
          weight *= Math.max(val1, val2);
        }
      }
    }

    methodWeights.put(operation, weight);
  }

  /**
   * First, update the weights of all methods under test. Retrieve the next method for constructing
   * a new sequence while also considering each method's weights. Update the number of times the
   * method has been selected.
   *
   * @return the chosen {@code TypedOperation} for the new sequence
   */
  @Override
  public TypedOperation selectOperation() {
    // Collect branch coverage and recompute weights for methods under test.
    updateBranchCoverageMaybe();

    TypedOperation selectedOperation =
        Randomness.randomMemberWeighted(operationSimpleList, methodWeights);
    // Update the number of times this method was selected.
    incrementInMap(currMethodSelectionCounts, selectedOperation);
    int numSuccessfulInvocations = incrementInMap(methodSelectionTotals, selectedOperation);

    maxSelectionsOfAnyMethod = Math.max(maxSelectionsOfAnyMethod, numSuccessfulInvocations);

    updateWeightForOperation(selectedOperation);

    return selectedOperation;
  }

  /**
   * Increment value mapped to from key in map.
   *
   * @param map input map
   * @param key key to use
   * @return resulting value
   */
  private static int incrementInMap(Map<TypedOperation, Integer> map, TypedOperation key) {
    Integer value = map.get(key);
    if (value == null) {
      value = 0;
    }
    value += 1;

    map.put(key, value);
    return value;
  }
}
