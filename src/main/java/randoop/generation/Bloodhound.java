package randoop.generation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import randoop.operation.TypedOperation;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;

/**
 * Implements the Bloodhound component, as described by the paper "GRT:
 * Program-Analysis-Guided Random Testing" (Ma et. al, ASE 2015).
 *
 * <p>Bloodhound computes a weight for each method under test by taking a weighted combination of the
 * uncovered branch ratio and the ratio between the number of times the method has been invoked and
 * the maximum number of times any method under test has been invoked.
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
  private final Map<TypedOperation, Integer> methodSelections = new HashMap<>();

  /**
   * Map of methods under test to the number of times they have ever been selected by the {@link
   * ForwardGenerator} to extend an existing sequence to construct a new sequence. This definition
   * is the same as that of {@code methodSelections} except that we do not clear this map every time
   * we recompute branch coverage. Thus, the integer value for a given method is non-decreasing
   * during a run of Randoop. The GRT paper does not state its definition of the "number of
   * invocations" of a method under test.
   */
  private final Map<TypedOperation, Integer> methodSuccessfulCalls = new HashMap<>();

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

  /** Maximum number of successful calls so far to any method under test. */
  private int maxSuccessfulCalls = 0;

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
  }

  /**
   * The branch coverage information for all methods under test is updated at every {@code
   * branchCoverageInterval}'th call of this method.
   */
  private void updateBranchCoverageMaybe() {
    stepNum += 1;
    if (stepNum % branchCoverageInterval == 0) {
      methodSelections.clear();
      CoverageTracker.instance.collect();
    }
  }

  /**
   * Recompute weights for all methods under test. Each method under test is assigned a weight based
   * on a weighted combination of the number of branches uncovered and the ratio between the number
   * of times this method has been invoked and the maximum number of times any method under test has
   * been invoked. The weighting scheme is based on Bloodhound in the Guided Random Testing (GRT)
   * paper.
   */
  private void updateWeightsForOperations() {
    // The number of methods under test, corresponds to |M| in the GRT paper.
    int numOperations = this.operationSimpleList.size();

    // Default weight is uniform probability.  If is used if no coverage details are available.
    // This is the case for classes like java.lang.Object (not explicitly under test).
    double weight = 1.0 / numOperations;

    // Recompute weights for all operations.
    for (TypedOperation operation : operationSimpleList) {
      CoverageTracker.BranchCoverage covDet =
          CoverageTracker.instance.getDetailsForMethod(operation.getName());

      if (covDet != null) {
        // Coverage details are available.

        // The number of successful invocations of this method. Corresponds to succ(m).
        Integer numSuccessfulInvocations = methodSuccessfulCalls.get(operation);
        if (numSuccessfulInvocations == null) {
          numSuccessfulInvocations = 0;
        }

        // If this method has no uncovered branches, or it is the most-invoked method,
        // use the default weight and skip this step.
        if (covDet.uncoveredBranches != 0 && numSuccessfulInvocations != maxSuccessfulCalls) {
          // Uncovered branch ratio of this method. The name uncovRatio(m) is from the GRT paper.
          double uncovRatio;
          if (covDet.totalBranches == 0) {
            uncovRatio = 0.5;
          } else {
            uncovRatio = (double) covDet.uncoveredBranches / covDet.totalBranches;
          }

          // Call ratio of this method. Corresponds to succ(m) / maxSucc(M) in the GRT paper.
          double callRatio;

          if (maxSuccessfulCalls == 0) {
            callRatio = 0.5;
          } else {
            callRatio = numSuccessfulInvocations.doubleValue() / maxSuccessfulCalls;
          }

          // Corresponds to w(m, 0) in the GRT paper.
          weight = alpha * uncovRatio + (1 - alpha) * (1 - callRatio);

          // Corresponds to the k variable in the GRT paper.
          Integer numSelectionsOfMethod = methodSelections.get(operation);
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
    updateWeightsForOperations();

    TypedOperation operation = Randomness.randomMemberWeighted(operationSimpleList, methodWeights);

    // Update the number of times this method was invoked.
    incrementInMap(methodSelections, operation);
    int numSuccessfulInvocations = incrementInMap(methodSuccessfulCalls, operation);

    maxSuccessfulCalls = Math.max(maxSuccessfulCalls, numSuccessfulInvocations);

    return operation;
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
