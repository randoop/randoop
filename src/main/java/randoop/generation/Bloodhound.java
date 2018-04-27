package randoop.generation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import randoop.operation.TypedOperation;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;

/**
 * Implements the Bloodhound component, as described by the paper "GRT: Program-Analysis-Guided
 * Random Testing" by Ma et. al (appears in ASE 2015):
 * https://people.kth.se/~artho/papers/lei-ase2015.pdf .
 *
 * <p>Bloodhound computes a weight for each method under test by taking a weighted combination of
 * the uncovered branch ratio and the ratio between the number of times the method has been
 * successfully invoked and the maximum number of times any method under test has been successfully
 * invoked. A method is "successfully invoked" when a method under test is used to create a new and
 * unique sequence and the sequence is kept as a regression test.
 *
 * <p>However, some hyper-parameters and edge cases were left unspecified in the GRT paper. We have
 * chosen our own values for the following unspecified hyper-parameters:
 *
 * <ul>
 *   <li>{@code alpha} - parameter to balance branch coverage and number of invocations when
 *       computing weight.
 *   <li>{@code p} - parameter for decreasing weights of methods between updates to coverage
 *       information.
 *   <li>{@code branchCoverageInterval} - interval for recomputing branch coverage information.
 *   <li>Default weight for cases where a method has zero branches or computed weight is zero.
 * </ul>
 */
public class Bloodhound implements TypedOperationSelector {

  /** Coverage tracker used to get branch coverage information. */
  private final CoverageTracker coverageTracker;

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
  private final Map<TypedOperation, Integer> methodSelectionCounts = new HashMap<>();

  /**
   * Map of methods under test to the total number of times they have ever been successfully invoked
   * by the {@link AbstractGenerator}. The integer value for a given method is non-decreasing during
   * a run of Randoop.
   */
  private final Map<TypedOperation, Integer> methodInvocationCounts = new HashMap<>();

  /*
   * List of operations, identical to {@link ForwardGenerator}'s operation list. Used for making
   * random, weighted selections for a method under test.
   */
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

  /**
   * How often to recompute branch coverage, in the number of successful invocations of all the
   * methods under test.
   */
  private final int branchCoverageInterval = 100;

  /** The total number of successful invocations of all the methods under test. */
  private int numSuccessfulInvocationsOfAMethodUnderTest = 0;

  /**
   * Maximum number of times any method under test has been successfully invoked. This value is
   * initialized to 1 because it is used as the denominator of a division in computing a method's
   * weight.
   */
  private int maxSuccM = 1;

  /**
   * Initialize Bloodhound by making a copy of the list of methods under test and assigning each
   * method to have the same weight.
   *
   * @param operations list of operations to copy
   */
  public Bloodhound(List<TypedOperation> operations, CoverageTracker coverageTracker) {
    this.operationSimpleList = new SimpleArrayList<>(operations);
    this.coverageTracker = coverageTracker;
    // Compute an initial weight for all methods under test. We also initialize the uncovered ratio
    // value of all methods under test by updating branch coverage information. This is to satisfy
    // the assertion in updateWeightForOperation which expects only two methods belonging to the
    // class Object to not have coverage details. The weights for all methods may not be uniform
    // in cases where we have methods with "zero" branches and methods with non-"zero" branches.
    updateBranchCoverageMaybe();
    updateWeightsForAllOperations();
  }

  /**
   * Selects a method under test for the {@link ForwardGenerator} to use to construct a new and
   * unique sequence. Branch coverage information, which is used to compute weights for methods
   * under test, is updated at every {@code branchCoverageInterval}'th call of this method. A method
   * under test is randomly selected with a weight probability. The selection count for the selected
   * method is incremented in the {@code methodSelectionCounts} map. Finally, the weight of the
   * selected method is recomputed.
   *
   * @return the chosen {@code TypedOperation} for the new and unique sequence
   */
  @Override
  public TypedOperation selectOperation() {
    // Collect branch coverage and recompute weights for methods under test.
    updateBranchCoverageMaybe();

    // Make a random, weighted choice for the next method.
    TypedOperation selectedOperation =
        Randomness.randomMemberWeighted(operationSimpleList, methodWeights);

    // Update the selected method's selection count and recompute its weight.
    incrementInMap(methodSelectionCounts, selectedOperation);
    updateWeightForOperation(selectedOperation);

    return selectedOperation;
  }

  /**
   * The branch coverage information for all methods under test is re-summarized at every {@code
   * branchCoverageInterval}'th call of this method. Weights for all methods under test are
   * recomputed when branch information is re-summarized.
   */
  private void updateBranchCoverageMaybe() {
    if (numSuccessfulInvocationsOfAMethodUnderTest % branchCoverageInterval == 0) {
      methodSelectionCounts.clear();
      coverageTracker.summarizeCoverageInformation();
      updateWeightsForAllOperations();
    }
  }

  /** Computes and updates weights in {@code methodWeights} map for all methods under test. */
  private void updateWeightsForAllOperations() {
    for (TypedOperation operation : operationSimpleList) {
      updateWeightForOperation(operation);
    }
  }

  /**
   * Recompute weights for a method under test. A method under test is assigned a weight based on a
   * weighted combination of the number of branches uncovered and the ratio between the number of
   * times this method has been recently selected and the maximum number of times any method under
   * test has been successfully invoked. The weighting scheme is based on Bloodhound in the Guided
   * Random Testing (GRT) paper.
   *
   * @param operation method to compute weight for
   */
  private void updateWeightForOperation(TypedOperation operation) {
    CoverageTracker.BranchCoverage covDet =
        coverageTracker.getDetailsForMethod(operation.getName());

    // Corresponds to uncovRatio(m) in the GRT paper.
    double uncovRatio;
    if (covDet != null) {
      uncovRatio = covDet.uncovRatio;
    } else {
      // Default to zero for methods with no coverage information.
      // This is the case for Object.<init> and Object.getClass which are included
      // by Randoop by default.
      assert operation.getName().equals("java.lang.Object.<init>")
          || operation.getName().equals("java.lang.Object.getClass");
      uncovRatio = 0;
    }

    // The number of successful invocations of this method. Corresponds to succ(m).
    Integer succM = methodInvocationCounts.get(operation);
    if (succM == null) {
      succM = 0;
    }

    // Corresponds to w(m, 0) in the GRT paper.
    double weight = alpha * uncovRatio + (1.0 - alpha) * (1.0 - (succM.doubleValue() / maxSuccM));

    // k, in the GRT paper, is defined as the number of times this method was selected since
    // the last update of branch coverage. It is reset to zero every time branch coverage is recomputed.
    Integer k = methodSelectionCounts.get(operation);
    if (k != null) {
      // Corresponds to the case where k >= 1 in the GRT paper.
      double val1 = (-3.0 / Math.log(1.0 - p)) * (Math.pow(p, k) / k);
      double val2 = 1.0 / Math.log(operationSimpleList.size() + 3.0);
      weight *= Math.max(val1, val2);
    }

    methodWeights.put(operation, weight);
  }

  /**
   * Increments the count of the number of times a method under test was successfully invoked.
   *
   * @param operation the method under test that was successfully invoked
   */
  public void incrementSuccessfulInvocationCountForOperation(TypedOperation operation) {
    numSuccessfulInvocationsOfAMethodUnderTest += 1;
    int numSuccessfulInvocations = incrementInMap(methodInvocationCounts, operation);
    maxSuccM = Math.max(maxSuccM, numSuccessfulInvocations);
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
