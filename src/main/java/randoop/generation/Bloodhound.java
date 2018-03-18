package randoop.generation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import randoop.main.CoverageTracker;
import randoop.operation.TypedOperation;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;

/**
 * Implements the Bloodhound component, largely as described by the authors of the Guided Random
 * Testing paper. Bloodhound computes a weight for each method under test by taking a weighted
 * combination of the uncovered branch ratio and the ratio between the number of times the method
 * has been invoked and the number of times any method under test has been invoked.
 */
public class Bloodhound {
  /**
   * Map of methods under test to their weights. These weights are dynamic and depend on branch
   * coverage.
   */
  private final Map<TypedOperation, Double> methodWeights = new HashMap<>();

  /**
   * Map of methods under test to the number of times they have been selected for a new sequence.
   * Cleared every time coverage is recomputed.
   */
  private final Map<TypedOperation, Integer> methodSelections = new HashMap<>();

  /** Map of methods under test to the number of times they have been successfully invoked. */
  private final Map<TypedOperation, Integer> methodSuccCalls = new HashMap<>();

  /**
   * List of operations, identical to ForwardGenerator's operation list. Needed for getting weighted
   * member.
   */
  private SimpleArrayList<TypedOperation> operationSimpleList = new SimpleArrayList<>();

  /** Hyper-parameter for balancing branch coverage and number of time a method was chosen. */
  private final double alpha = 0.7;

  /** Hyper-parameter for decreasing weights. */
  private final double p = 0.5;

  /** Maximum number of successful calls to any method under test. */
  private int maxSuccessfulCalls = 0;

  /** Step number used to determine when to recompute method weights. */
  private int stepNum = 0;

  /**
   * Make Bloodhound's internal list, {@code operationSimpleList}, be a copy of the given list.
   *
   * @param operations list of operations to copy.
   */
  public void setOperations(List<TypedOperation> operations) {
    operationSimpleList = new SimpleArrayList<TypedOperation>(operations);
  }

  /**
   * Recompute weights for all methods under test at regular intervals. Each method under test is
   * assigned a weight based on a weighted combination of the number of branches uncovered and the
   * ratio between the number of times this method has been invoked and the maximum number of times
   * any method under test has been invoked. The weighting scheme is based on the scheme described
   * by the authors of the Guided Random Testing (GRT) paper. We have chosen our own reasonable
   * values for all parameters that were left unspecified by the authors of GRT.
   *
   * @param interval interval at which to recompute weights
   */
  public void processWeightsForOperations(int interval) {
    stepNum += 1;

    // After the specified interval, recompute the current coverage information.
    if (stepNum % interval == 0) {
      methodSelections.clear();

      // Collect coverage information of all methods under test.
      CoverageTracker.instance.collect();

      //      for (TypedOperation to : methodWeights.keySet()) {
      //        System.out.println(to + " " + methodWeights.get(to));
      //      }
    }

    // The number of methods under test, corresponds to |M| in the GRT paper.
    int numOperations = this.operationSimpleList.size();

    // Default weight is uniform probability.
    double weight = 1.0 / numOperations;

    // Recompute weights for all operations.
    for (int i = 0; i < operationSimpleList.size(); i++) {
      TypedOperation operation = operationSimpleList.get(i);

      CoverageTracker.CoverageDetails covDet =
          CoverageTracker.instance.getDetailsForMethod(operation.getName());

      // Use default weight if no coverage details are available.
      // This is the case for classes like java.lang.Object (not explicitly under test).
      if (covDet != null) {

        // The number of successful invocations of this method. Corresponds to succ(m).
        Integer numSuccessfulInvocation = methodSuccCalls.get(operation);
        if (numSuccessfulInvocation == null) {
          numSuccessfulInvocation = 0;
        }

        // If this method has no uncovered branches, or it's been invoked the current maximum number
        // of times, use the default weight and skip this step.
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
            // Corresponds to the case where k >= 1.
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
   * Retrieve the next method for constructing a new sequence while also considering each method's
   * weights. Update the number of times the method has been selected.
   *
   * @return the chosen {@code TypedOperation} for the new sequence
   */
  public TypedOperation getNextOperation() {
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
