package randoop.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jacoco.core.analysis.*;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;

/**
 * Tracks the coverage of each method under test. Largely based on
 * http://www.jacoco.org/jacoco/trunk/doc/examples/java/CoreTutorial.java
 */
public class CoverageTracker {
  public static final CoverageTracker instance = new CoverageTracker();

  private final IRuntime runtime;
  private final Instrumenter instrumenter;
  private final RuntimeData data;
  private final MemoryClassLoader memoryClassLoader;

  private final ExecutionDataStore executionData;
  private final SessionInfoStore sessionInfos;

  // Set of names of the classes whose coverage is being tracked and calculated.
  private final Set<String> trackedClasses;

  // Map from method name to coverage details.
  private final Map<String, CoverageDetails> coverageDetailsMap;

  /**
   * Coverage details related to a single method under test. Tracks total number of branches and
   * number of uncovered branches.
   */
  public static class CoverageDetails {
    private int numBranches;
    private int uncoveredBranches;

    /**
     * Update number of branches to numBranches.
     *
     * @param numBranches number of branches of this method.
     */
    private void setNumBranches(int numBranches) {
      this.numBranches = numBranches;
    }

    /**
     * Set the number of uncovered branches.
     *
     * @param uncoveredBranches number of uncovered branches of this method.
     */
    private void setUncoveredBranches(int uncoveredBranches) {
      this.uncoveredBranches = uncoveredBranches;
    }

    /**
     * Return total number of branches
     *
     * @return number of branches of this method.
     */
    public int getNumBranches() {
      return numBranches;
    }

    /**
     * Return the number of uncovered branches.
     *
     * @return number of uncovered branches.
     */
    public int getUncoveredBranches() {
      return uncoveredBranches;
    }
  }

  /** Initializes the coverage tracker. */
  private CoverageTracker() {
    coverageDetailsMap = new HashMap<>();

    runtime = new LoggerRuntime();
    instrumenter = new Instrumenter(runtime);
    memoryClassLoader = new MemoryClassLoader();

    data = new RuntimeData();
    try {
      runtime.startup(data);
    } catch (Exception e) {
      e.printStackTrace();
    }

    executionData = new ExecutionDataStore();
    sessionInfos = new SessionInfoStore();

    trackedClasses = new HashSet<>();
  }

  /**
   * Returns the instrumented version of the class.
   *
   * @param targetName name of the class
   * @return {@Code Class} object that has been instrumented for coverage data collection. null if
   *     class with target name cannot be found.
   */
  public Class<?> getInstrumentedClass(String targetName) {
    if (targetName == null || targetName.isEmpty()) {
      return null;
    }

    // Add the name of the class to the set of tracked classes.
    trackedClasses.add(targetName);

    final String resource = '/' + targetName.replace('.', '/') + ".class";
    InputStream original = getClass().getResourceAsStream(resource);
    final byte[] instrumented;

    if (original == null) {
      System.err.println("No resource with name: " + resource + " found!");
      return null;
    }

    try {
      // Instrument the class to prepare for coverage collection later.
      instrumented = instrumenter.instrument(original, targetName);
      original.close();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    memoryClassLoader.addDefinition(targetName, instrumented);
    try {
      return memoryClassLoader.loadClass(targetName);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  /** Collect coverage information for all methods under test. Updates the coverageDetailsMap. */
  public void collect() {
    // Collect coverage information.
    data.collect(executionData, sessionInfos, false);

    CoverageBuilder coverageBuilder = new CoverageBuilder();
    Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

    // Analyze the coverage of each of the tracked classes.
    for (String className : trackedClasses) {
      String resource = '/' + className.replace('.', '/') + ".class";
      InputStream original = getClass().getResourceAsStream(resource);
      try {
        analyzer.analyzeClass(original, className);
        original.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // Collect the branch coverage information.
    for (final IClassCoverage cc : coverageBuilder.getClasses()) {
      for (final IMethodCoverage cm : cc.getMethods()) {
        String methodName = cc.getName() + "." + cm.getName();
        System.out.println(methodName);
        System.out.println("Total branches: " + cm.getBranchCounter().getTotalCount());
        System.out.println("Missed count: " + cm.getBranchCounter().getMissedCount());

        CoverageDetails methodDetails = coverageDetailsMap.get(methodName);
        if (methodDetails == null) {
          methodDetails = new CoverageDetails();
        }
        methodDetails.setNumBranches(cm.getBranchCounter().getTotalCount());
        methodDetails.setUncoveredBranches(cm.getBranchCounter().getMissedCount());
      }
    }
    System.out.println("---------------------------");
  }

  /** Clean up the coverage tracker instance. */
  public void finish() {
    runtime.shutdown();
  }

  /**
   * Returns the coverage details associated with the input method.
   *
   * @param methodName name of the method to examine.
   * @return Coverage details associated with the method.
   */
  public CoverageDetails getDetailsForMethod(String methodName) {
    return this.coverageDetailsMap.get(methodName);
  }
}
