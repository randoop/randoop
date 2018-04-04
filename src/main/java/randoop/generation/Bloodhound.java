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
 * uncovered branch ratio and the ratio between the number of times the method has been invoked and
 * the maximum number of times any method under test has been invoked.
 *
 * <p>However, some hyper-parameters and edge cases were left unspecified in the GRT paper. We have
 * chosen our own values for the following unspecified aspects: 1) alpha - parameter to balance
 * branch coverage and number of invocations when computing weight. 2) p - parameter for decreasing
 * weights of methods between updates to coverage information. 3) Interval for recomputing branch
 * coverage information. 4) Default weight for cases where a method has zero branches or computed
 * weight is zero.
 */
public class Bloodhound implements TypedOperationSelector {

  /**
   * Map of methods under test to their weights. These weights are dynamic and depend on branch
   * coverage.
   */
  private final Map<TypedOperation, Double> methodWeights = new HashMap<>();

  /**
   * Map of methods under test to the number of times they have been selected by the {@link
   * ForwardGenerator} to extend an existing sequence to construct a new and unique sequence. This
   * map is cleared every time branch coverage is recomputed.
   */
  private final Map<TypedOperation, Integer> methodSelections = new HashMap<>();

  /**
   * Map of methods under test to the number of times they have been successfully invoked. We
   * define, for a method under test, the number of times that is has been invoked as the number of
   * times it is chosen by the {@link ForwardGenerator} to extend an existing sequence to construct
   * a new and unique sequence. This definition is the same as that of {@code methodSelections}
   * except that we do not clear this map every time we recompute branch coverage. Thus, the value
   * that each method maps to will always be non-decreasing throughout the duration of one run of
   * Randoop. The GRT paper does not state its definition of the "number of invocations" of a method
   * under test.
   */
  private final Map<TypedOperation, Integer> methodSuccCalls = new HashMap<>();

  /**
   * List of operations, identical to ForwardGenerator's operation list. Needed for getting weighted
   * member when using {@link Randomness}.
   */
  private final SimpleArrayList<TypedOperation> operationSimpleList;

  /** Hyper-parameter for balancing branch coverage and number of times a method was chosen. */
  private final double alpha = 0.7;

  /** Hyper-parameter for decreasing weights of methods between updates to coverage information. */
  private final double p = 0.5;

  /** Hyper-parameter for determining when to recompute branch coverage. */
  private final int branchCoverageInterval = 100;

  /** Maximum number of successful calls so far to any method under test. */
  private int maxSuccessfulCalls = 0;

  /** Step number used to determine when to recompute method weights. */
  private int stepNum = 0;

  /**
   * Construct a new instance of Bloodhound, which will choose from among the given operations.
   *
   * @param operations list of operations to copy.
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
      CoverageTracker.CoverageDetails covDet =
          CoverageTracker.instance.getDetailsForMethod(operation.getName());

      if (covDet != null) {
        // Coverage details are available.

        // The number of successful invocations of this method. Corresponds to succ(m).
        Integer numSuccessfulInvocation = methodSuccCalls.get(operation);
        if (numSuccessfulInvocation == null) {
          numSuccessfulInvocation = 0;
        }

        // If this method has no uncovered branches, or it is the most-invoked method,
        // use the default weight and skip this step.
        if (covDet.uncoveredBranches != 0 && numSuccessfulInvocation != maxSuccessfulCalls) {
          // Uncovered branch ratio of this method. Corresponds to uncovRatio(m) in the GRT paper.
          double uncoveredRatio = 0.5;
          if (covDet.numBranches != 0) {
            uncoveredRatio = (double) covDet.uncoveredBranches / covDet.numBranches;
          }

          // Call ratio of this method. Corresponds to succ(m) / maxSucc(M) in the GRT paper.
          double callRatio = 0.5;
          if (maxSuccessfulCalls != 0) {
            callRatio = numSuccessfulInvocation.doubleValue() / maxSuccessfulCalls;
          }

          // Corresponds to w(m, 0) in the GRT paper.
          weight = alpha * uncoveredRatio + (1 - alpha) * (1 - callRatio);

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

      // Assign the weight to the operation in the methods' weight map.
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

    // Update the number of times this method was selected for a new sequence.
    incrementInMap(methodSelections, operation);

    // Update the number of times this method was successfully invoked.
    int numSuccessfulInvocations = incrementInMap(methodSuccCalls, operation);
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
