package randoop.generation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.plumelib.util.CollectionsPlume;
import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;
import randoop.operation.CallableOperation;
import randoop.operation.EnumConstant;
import randoop.operation.FieldGet;
import randoop.operation.FieldSet;
import randoop.operation.MethodCall;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.types.ClassOrInterfaceType;
import randoop.util.Randomness;
import randoop.util.SimpleArrayList;

/**
 * Implements the Bloodhound component, as described by the paper "GRT: Program-Analysis-Guided
 * Random Testing" by Ma et. al (appears in ASE 2015):
 * https://people.kth.se/~artho/papers/lei-ase2015.pdf .
 *
 * <p>Bloodhound computes a weight for each method under test by taking a weighted combination of
 *
 * <ul>
 *   <li>the uncovered branch ratio and
 *   <li>the ratio between the number of times the method has been successfully invoked (to be the
 *       last statement of a new regression test) and the maximum number of times any method under
 *       test has been successfully invoked.
 * </ul>
 *
 * A method is "successfully invoked" when a method under test is used to create a new sequence and
 * the sequence is kept as a regression test. An alternative definition of "successful invocations"
 * is the total number of times the method appears in any regression test. Both definitions are
 * consistent with the description in the GRT paper. We believe our implementation, which uses the
 * first definition, is likely what was intended by the authors of the GRT paper.
 */
public class Bloodhound implements TypedOperationSelector {

  /** Coverage tracker used to get branch coverage information of methods under test. */
  private final CoverageTracker coverageTracker;

  /**
   * Map from methods under test to their weights. These weights are dynamic and depend on branch
   * coverage.
   */
  private final Map<TypedOperation, Double> methodWeights = new HashMap<>();

  /**
   * Map from methods under test to the number of times they have been recently selected by the
   * {@link ForwardGenerator} to construct a new sequence. This map is cleared every time branch
   * coverage is recomputed.
   */
  private final Map<TypedOperation, Integer> methodSelectionCounts = new HashMap<>();

  /**
   * Map from methods under test to the total number of times they have ever been successfully
   * invoked by the {@link AbstractGenerator}. The integer value for a given method is
   * non-decreasing during a run of Randoop.
   */
  private final Map<TypedOperation, Integer> methodInvocationCounts = new HashMap<>();

  /**
   * List of operations, identical to {@link ForwardGenerator}'s operation list. Used for making
   * random, weighted selections for a method under test.
   */
  private final SimpleArrayList<TypedOperation> operationSimpleList;

  /**
   * Parameter for balancing branch coverage and number of times a method was chosen. The name
   * "alpha" and the specified value are both from the GRT paper.
   */
  private static final double alpha = 0.9;

  /**
   * Parameter for decreasing weights of methods between updates to coverage information. The name
   * "p" and the specified value are both from the GRT paper.
   */
  private static final double p = 0.99;

  /**
   * Time interval, in milliseconds, at which to recompute weights. The name "t" and the specified
   * value are both from the GRT paper.
   */
  private static final long t = 50000;

  /** {@code System.currentTimeMillis()} when branch coverage was last updated. */
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
   * Initialize Bloodhound. Branch coverage information is initialized and all methods under test
   * are assigned a weight based on the weighting scheme defined by GRT's description of Bloodhound.
   *
   * @param operations list of operations under test
   * @param classesUnderTest set of classes under test
   */
  public Bloodhound(List<TypedOperation> operations, Set<ClassOrInterfaceType> classesUnderTest) {
    this.operationSimpleList = new SimpleArrayList<>(operations);
    this.coverageTracker = new CoverageTracker(classesUnderTest);

    // Compute an initial weight for all methods under test. We also initialize the uncovered ratio
    // value of all methods under test by updating branch coverage information. The weights for all
    // methods may not be uniform in cases where we have methods with "zero" branches and methods
    // with non-"zero" branches. This initialization depends on lastUpdateTime being initialized to
    // zero.
    updateBranchCoverageMaybe();
  }

  /**
   * Selects a method under test for the {@link ForwardGenerator} to use to construct a new
   * sequence. A method under test is randomly selected with a weighted probability.
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
    CollectionsPlume.incrementMap(methodSelectionCounts, selectedOperation);
    updateWeight(selectedOperation);

    return selectedOperation;
  }

  /**
   * When an interval is reached, the branch coverage information for all methods under test is
   * updated and the weight for every method under test is recomputed.
   *
   * <p>There are two choices for when to update branch coverage information:
   *
   * <ul>
   *   <li>Time: branch coverage is updated when more than {@code t} milliseconds have elapsed since
   *       branch coverage was last updated. This is GRT's approach and is the default. It makes
   *       Randoop non-deterministic.
   *   <li>Count of successful invocations: branch coverage is updated after every {@code
   *       branchCoverageInteral} successful invocations (of any method under test).
   * </ul>
   */
  private void updateBranchCoverageMaybe() {
    boolean shouldUpdateBranchCoverage;

    switch (GenInputsAbstract.bloodhound_update_mode) {
      case TIME:
        long currentTime = System.currentTimeMillis();
        shouldUpdateBranchCoverage = currentTime - lastUpdateTime >= t;

        // Update the last update time if we decide that it's time to update branch coverage
        // information.
        if (shouldUpdateBranchCoverage) {
          lastUpdateTime = currentTime;
        }
        break;
      case INVOCATIONS:
        shouldUpdateBranchCoverage = totalSuccessfulInvocations % branchCoverageInterval == 0;

        // If we decide that it's time to update the branch coverage information, we "reset" the
        // totalSuccessfulInvocations to 1 (or we could have incremented it by 1). This is to
        // prevent
        // ourselves from immediately re-updating branch coverage information should it be the case
        // that the next test sequence that is generated is not a regression test and thus
        // totalSuccessfulInvocations is not recomputed causing shouldUpdateBranchCoverage to be
        // true
        // again.
        if (shouldUpdateBranchCoverage) {
          totalSuccessfulInvocations = 1;
        }
        break;
      default:
        throw new RandoopBug(
            "Unhandled value for bloodhound_update_mode: "
                + GenInputsAbstract.bloodhound_update_mode);
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
      for (TypedOperation typedOperation : new TreeSet<>(methodWeights.keySet())) {
        System.out.println(typedOperation.getName() + ": " + methodWeights.get(typedOperation));
      }
      System.out.println("--------------------------");
    }
  }

  /**
   * Computes and updates weights in {@code methodWeights} map for all methods under test.
   * Recomputes the {@code totalWeightOfMethodsUnderTest} to avoid problems with round-off error.
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
   * weighted combination of
   *
   * <ul>
   *   <li>the number of branches uncovered and
   *   <li>the ratio between the number of times this method has been recently selected and the
   *       maximum number of times any method under test has been successfully invoked.
   * </ul>
   *
   * The weighting scheme is based on Bloodhound in the Guided Random Testing (GRT) paper.
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
      // Default to 0.5 for methods with no coverage information. The GRT paper does not mention
      // how methods with no coverage information are handled. This value was chosen based on
      // the reasoning that methods with no coverage information should still be given a reasonable
      // chance at being selected. A more optimal value could be determined empirically.
      // This is the case for the following methods under test:
      // - Object.<init> and Object.getClass, which Randoop always includes as methods under test.
      // - Getter and setter operations for public member variables, which Randoop synthesizes.
      // - Abstract methods and interface methods.
      // - Methods defined in abstract classes.
      // - Inherited methods, which aren't overridden in the calling class.
      // - Enum constants.
      String operationName = operation.getName();
      CallableOperation callableOperation = operation.getOperation();

      boolean isAbstractMethod = false;
      boolean isSyntheticMethod = false;
      boolean isFromAbstractClass = false;
      if (callableOperation instanceof MethodCall) {
        Method method = ((MethodCall) callableOperation).getMethod();
        isAbstractMethod = Modifier.isAbstract(method.getModifiers());
        isSyntheticMethod = method.isSynthetic();
        isFromAbstractClass = Modifier.isAbstract(method.getDeclaringClass().getModifiers());
      }

      boolean isGetterMethod = callableOperation instanceof FieldGet;
      boolean isSetterMethod = callableOperation instanceof FieldSet;
      boolean isEnumConstant = callableOperation instanceof EnumConstant;

      boolean isExpectedToHaveNoCoverage =
          isAbstractMethod
              || isGetterMethod
              || isSetterMethod
              || isEnumConstant
              || isSyntheticMethod
              || isFromAbstractClass
              || operationName.equals("java.lang.Object.<init>")
              || operationName.equals("java.lang.Object.getClass");
      if (!isExpectedToHaveNoCoverage) {
        System.err.println(
            "The method " + methodName + " is expected to have coverage info but has none.");
      }
      assert isExpectedToHaveNoCoverage;
      uncovRatio = 0.5;
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
   * Increments the number of times a method under test was successfully invoked.
   *
   * @param operation the method under test that was successfully invoked
   */
  public void incrementSuccessfulInvocationCount(TypedOperation operation) {
    totalSuccessfulInvocations += 1;
    CollectionsPlume.incrementMap(methodInvocationCounts, operation);
    int numSuccessfulInvocations = methodInvocationCounts.get(operation);
    maxSuccM = Math.max(maxSuccM, numSuccessfulInvocations);
  }

  /**
   * Increment the number of successful invocations of the last method in the newly-created sequence
   * that was classified as a regression test.
   *
   * @param sequence newly-created sequence that was classified as a regression test
   */
  @Override
  public void newRegressionTestHook(Sequence sequence) {
    incrementSuccessfulInvocationCount(sequence.getOperation());
  }
}
