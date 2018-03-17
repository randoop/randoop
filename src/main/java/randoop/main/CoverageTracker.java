package randoop.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
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

  private final IRuntime runtime = new LoggerRuntime();
  private final MemoryClassLoader memoryClassLoader = new MemoryClassLoader();

  private final ExecutionDataStore executionData = new ExecutionDataStore();
  private final SessionInfoStore sessionInfos = new SessionInfoStore();

  private final Instrumenter instrumenter;
  private final RuntimeData data;

  /** Map from method name to coverage details. */
  private final Map<String, CoverageDetails> coverageDetailsMap = new HashMap<>();

  /**
   * Map from fully-qualified class name to instrumented version, for all classes that are
   * instrumented through the {@code CoverageTracker} class.
   */
  private final Map<String, Class<?>> instrumentedClasses = new HashMap<>();

  /**
   * Coverage details related to a single method under test. Tracks total number of branches and
   * number of uncovered branches.
   */
  public static class CoverageDetails {
    /** Total number of branches. */
    public int numBranches;
    /** Number of uncovered branches. */
    public int uncoveredBranches;
  }

  private CoverageTracker() {
    instrumenter = new Instrumenter(runtime);

    data = new RuntimeData();
    try {
      runtime.startup(data);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Instruments and loads into memory, each of the given classes.
   *
   * @param classNames fully-qualified names of classes
   */
  public void instrumentAndLoad(Set<String> classNames) {
    if (classNames == null) {
      return;
    }

    // Instrument and load into memory, each class that is under test.
    for (String className : classNames) {
      final byte[] instrumented;

      final String resource = '/' + className.replace('.', '/') + ".class";
      InputStream original = getClass().getResourceAsStream(resource);

      if (original == null) {
        System.err.println("No resource with name: " + resource + " found!");
        continue;
      }

      try {
        // Instrument the class to prepare for coverage collection later.
        instrumented = instrumenter.instrument(original, className);
        original.close();
      } catch (IOException e) {
        e.printStackTrace();
        continue;
      }

      memoryClassLoader.addDefinition(className, instrumented);
    }
  }

  /**
   * Returns the instrumented version of the class.
   *
   * @param className name of the class
   * @return {@Code Class} object that has been instrumented for coverage data collection. Returns
   *     null if class with target name cannot be found.
   */
  public Class<?> getInstrumentedClass(String className) {
    if (className == null || className.isEmpty()) {
      return null;
    }

    // Check our cache first, to see if this class has already been loaded.
    Class<?> instrumentedClass = instrumentedClasses.get(className);
    if (instrumentedClass != null) {
      return instrumentedClass;
    }

    try {
      instrumentedClass = memoryClassLoader.loadClass(className);
      instrumentedClasses.put(className, instrumentedClass);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return instrumentedClass;
  }

  /** Collect coverage information for all methods under test. Updates the coverageDetailsMap. */
  public void collect() {
    // Collect coverage information.
    data.collect(executionData, sessionInfos, false);

    CoverageBuilder coverageBuilder = new CoverageBuilder();
    Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

    // Analyze the coverage of each of the tracked classes.
    for (String className : instrumentedClasses.keySet()) {
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
        //        System.out.println(methodName);
        //        System.out.println("Total branches: " + cm.getBranchCounter().getTotalCount());
        //        System.out.println("Missed count: " + cm.getBranchCounter().getMissedCount());

        CoverageDetails methodDetails = coverageDetailsMap.get(methodName);
        if (methodDetails == null) {
          methodDetails = new CoverageDetails();
        }
        methodDetails.numBranches = cm.getBranchCounter().getTotalCount();
        methodDetails.uncoveredBranches = cm.getBranchCounter().getMissedCount();

        coverageDetailsMap.put(methodName, methodDetails);
      }
    }
    //    System.out.println("---------------------------");
  }

  /** Clean up the coverage tracker instance. */
  public void finish() {
    runtime.shutdown();
  }

  /**
   * Returns the coverage details associated with the input method.
   *
   * @param methodName name of the method to examine.
   * @return coverage details associated with the method
   */
  public CoverageDetails getDetailsForMethod(String methodName) {
    return this.coverageDetailsMap.get(methodName);
  }
}
