package randoop.generation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import randoop.main.GenInputsAbstract;
import randoop.operation.CallableOperation;
import randoop.operation.EnumConstant;
import randoop.operation.FieldGet;
import randoop.operation.FieldSet;
import randoop.operation.MethodCall;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;

/**
 * Implements the Bloodhound component, as described by the paper "GRT: Program-Analysis-Guided
 * Random Testing" by Ma et. al (appears in ASE 2015):
 * https://people.kth.se/~artho/papers/lei-ase2015.pdf .
 *
 * <p>Bloodhound computes a weight for each method under test by taking a weighted combination of
 * the uncovered branch ratio and the ratio between the number of times the method has been
 * successfully invoked (to be the last statement of a new regression test) and the maximum number
 * of times any method under test has been successfully invoked. A method is "successfully invoked"
 * when a method under test is used to create a new sequence and the sequence is kept as a
 * regression test.
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

  /** Coverage tracker used to get branch coverage information of methods under test. */
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
   * Parameter for balancing branch coverage and number of times a method was chosen. The name alpha
   * is from the GRT paper.
   */
  private static final double alpha = 0.9;

  /**
   * Parameter for decreasing weights of methods between updates to coverage information. The name p
   * is from the GRT paper.
   */
  private static final double p = 0.99;

  /**
   * Time interval, in milliseconds, at which to recompute weights. The name t is from the GRT
   * paper.
   */
  private static final long t = 50000;

  /** Time, in milliseconds, when branch coverage was last updated. */
  private long lastUpdateTime = 0;

  /**
   * Branch coverage is recomputed after this many successful invocations (= this many new tests
   * were generated).
   */
  private static final int branchCoverageInterval = 100;

  /** The total number of successful invocations of all the methods under test. */
  private int totalSuccessfulInvocations = 0;

  /**
   * Maximum number of times any method under test has been successfully invoked. This value is
   * initialized to 1 because it is used as the denominator of a division in computing a method's
   * weight. The name is from the GRT paper, which calls this quantity "maxSucc(M)".
   */
  private int maxSuccM = 1;

  /**
   * The total weight of all the methods that are under test. This is used by {@link Randomness} to
   * randomly select an element from a list of weighted elements.
   */
  private double totalWeightOfMethodsUnderTest = 0;

  /**
   * Initialize Bloodhound.
   *
   * @param operations list of operations under test
   */
  public Bloodhound(List<TypedOperation> operations) {
    this.operationSimpleList = new SimpleArrayList<>(operations);
    this.coverageTracker = new CoverageTracker(GenInputsAbstract.getClassnamesFromArgs());

    // Compute an initial weight for all methods under test. We also initialize the uncovered ratio
    // value of all methods under test by updating branch coverage information. The weights for all
    // methods may not be uniform in cases where we have methods with "zero" branches and methods
    // with non-"zero" branches. This initialization depends on lastUpdateTime being initialized to zero.
    updateBranchCoverageMaybe();
  }

  /**
   * Selects a method under test for the {@link ForwardGenerator} to use to construct a new
   * sequence. A method under test is randomly selected with a weight probability.
   *
   * <p>Branch coverage information, which is used to compute weights for methods under test, is
   * updated at every {@code branchCoverageInterval}'th call of this method.
   *
   * @return the chosen {@code TypedOperation} for the new sequence
   */
  @Override
  public TypedOperation selectOperation() {
    // Periodically collect branch coverage and recompute weights for all methods under test.
    updateBranchCoverageMaybe();

    // Make a random, weighted choice for the next method.
    TypedOperation selectedOperation =
        Randomness.randomMemberWeighted(
            operationSimpleList, methodWeights, totalWeightOfMethodsUnderTest);

    // Update the selected method's selection count and recompute its weight.
    incrementInMap(methodSelectionCounts, selectedOperation);
    updateWeight(selectedOperation);

    return selectedOperation;
  }

  /**
   * At every {@code branchCoverageInterval}'th call of this method, the branch coverage information
   * for all methods under test is updated and weights for all methods under test are recomputed.
   */
  private void updateBranchCoverageMaybe() {
    boolean shouldUpdateBranchCoverage;

    if (GenInputsAbstract.bloodhound_update_mode
        == GenInputsAbstract.BloodhoundCoverageUpdateMode.TIME) {
      // Use a time based approach for determining whether or not we want to update branch coverage.
      long currentTime = System.currentTimeMillis();
      shouldUpdateBranchCoverage = currentTime - lastUpdateTime >= t;

      // Update the last update time if we decide that it's time to update branch coverage information.
      if (shouldUpdateBranchCoverage) {
        lastUpdateTime = currentTime;
      }
    } else {
      // Use a selection based approach for determining whether or not we want to update branch coverage.
      shouldUpdateBranchCoverage = totalSuccessfulInvocations % branchCoverageInterval == 0;
    }

    if (shouldUpdateBranchCoverage) {
      if (GenInputsAbstract.bloodhound_logging) {
        System.out.println("Updating branch coverage information.");
      }

      methodSelectionCounts.clear();
      coverageTracker.updateBranchCoverageMap();
      updateWeightsForAllOperations();
      logMethodWeights();
    }
  }

  /** For debugging, print all method weights to standard output. */
  private void logMethodWeights() {
    if (GenInputsAbstract.bloodhound_logging) {
      System.out.println("Method name: method weight");
      for (TypedOperation typedOperation : methodWeights.keySet()) {
        System.out.println(typedOperation.getName() + ": " + methodWeights.get(typedOperation));
      }
      System.out.println("--------------------------");
    }
  }

  /**
   * Computes and updates weights in {@code methodWeights} map for all methods under test.
   * Recomputes the {@code totalWeightOfMethodsUnderTest} to aovid problems with roundoff error.
   */
  private void updateWeightsForAllOperations() {
    double totalWeight = 0;
    for (TypedOperation operation : operationSimpleList) {
      totalWeight += updateWeight(operation);
    }
    totalWeightOfMethodsUnderTest = totalWeight;
  }

  /**
   * Recompute weight for a method under test. A method under test is assigned a weight based on a
   * weighted combination of the number of branches uncovered and the ratio between the number of
   * times this method has been recently selected and the maximum number of times any method under
   * test has been successfully invoked. The weighting scheme is based on Bloodhound in the Guided
   * Random Testing (GRT) paper.
   *
   * @param operation method to compute weight for
   * @return the updated weight for the given operation
   */
  private double updateWeight(TypedOperation operation) {
    // Remove type arguments, because Jacoco does not include type arguments when naming a method.
    String methodName = operation.getName().replaceAll("<.*>\\.", ".");

    // Corresponds to uncovRatio(m) in the GRT paper.
    Double uncovRatio = coverageTracker.getBranchCoverageForMethod(methodName);

    if (uncovRatio == null) {
      // Default to zero for methods with no coverage information.
      // This is the case for the following methods under test:
      // - Object.<init> and Object.getClass which Randoop always includes as methods under test.
      // - Classes that are from the JDK (eg, java.lang) or from external jars.
      // - Getters and setters operations for public member variables that Randoop synthesizes.
      // - Abstract method declarations and interface methods.
      // - Enum constants.
      String operationName = operation.getName();
      CallableOperation callableOperation = operation.getOperation();

      boolean isAbstractMethod = false;
      boolean isGetterMethod = callableOperation instanceof FieldGet;
      boolean isSetterMethod = callableOperation instanceof FieldSet;
      boolean isEnumConstant = callableOperation instanceof EnumConstant;

      if (callableOperation instanceof MethodCall) {
        Method method = ((MethodCall) callableOperation).getMethod();
        isAbstractMethod = Modifier.isAbstract(method.getModifiers());
      }

      assert isAbstractMethod
          || isGetterMethod
          || isSetterMethod
          || isEnumConstant
          || operationName.startsWith("java.")
          || operationName.startsWith("javax.")
          || operationName.equals("java.lang.Object.<init>")
          || operationName.equals("java.lang.Object.getClass");
      uncovRatio = 0.0;
    }

    // The number of successful invocations of this method. Corresponds to "succ(m)" in the GRT
    // paper.
    Integer succM = methodInvocationCounts.get(operation);
    if (succM == null) {
      succM = 0;
    }

    // Corresponds to w(m, 0) in the GRT paper.
    double wm0 = alpha * uncovRatio + (1.0 - alpha) * (1.0 - (succM.doubleValue() / maxSuccM));

    // Corresponds to w(m, k) in the GRT paper.
    double wmk;
    // In the GRT paper, "k" is the number of times this method was selected since the last update
    // of branch coverage. It is reset to zero every time branch coverage is recomputed.
    Integer k = methodSelectionCounts.get(operation);
    if (k == null) {
      wmk = wm0;
    } else {
      // Corresponds to the case where k >= 1 in the GRT paper.
      double val1 = (-3.0 / Math.log(1.0 - p)) * (Math.pow(p, k) / k);
      double val2 = 1.0 / Math.log(operationSimpleList.size() + 3.0);
      wmk = Math.max(val1, val2) * wm0;
    }

    // Retrieve the weight from the methodWeights map if it exists. Otherwise, default to zero.
    Double existingWeight = methodWeights.get(operation);
    if (existingWeight == null) {
      existingWeight = 0.0;
    }

    methodWeights.put(operation, wmk);

    // Update the contribution of this method to the total weight of all methods under test.
    totalWeightOfMethodsUnderTest -= existingWeight;
    totalWeightOfMethodsUnderTest += wmk;

    return wmk;
  }

  /**
   * Increments the count of the number of times a method under test was successfully invoked.
   *
   * @param operation the method under test that was successfully invoked
   */
  public void incrementSuccessfulInvocationCount(TypedOperation operation) {
    totalSuccessfulInvocations += 1;
    int numSuccessfulInvocations = incrementInMap(methodInvocationCounts, operation);
    maxSuccM = Math.max(maxSuccM, numSuccessfulInvocations);
  }

  /**
   * Increment the number of successful invocations of the last method in the newly created sequence
   * that was classified as a regression test
   *
   * @param sequence newly created sequence that was classified as a regression test
   */
  @Override
  public void newRegressionTestHook(Sequence sequence) {
    incrementSuccessfulInvocationCount(sequence.getOperation());
  }

  /**
   * Increment value mapped to from key in map. Set the value to 1 if not currently mapped.
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
