package randoop.generation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jacoco.agent.rt.RT;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.*;

/**
 * Tracks the branch coverage of each method under test. Specifically, for each method under test,
 * this class records the total number of branches and the number of branches that have not been
 * covered in generated tests. This class periodically updates branch coverage information for each
 * method from Jacoco's data structures.
 *
 */
public class CoverageTracker {
  /**
   * In memory store of the coverage information for all classes.
   */
  private final ExecutionDataStore executionData = new ExecutionDataStore();

  /** Map from method name to branch coverage. */
  private final Map<String, BranchCoverage> branchCoverageMap = new HashMap<>();

  /** Names of all the classes under test */
  private Set<String> classesUnderTest;

  /**
   * Branch coverage for a single method under test. Records "uncovRatio" which is the ratio of
   * uncovered branches to total branches. In cases where total branches is zero, the uncovered
   * ratio will be zero. A method with only straight-line code that will always be executed entirely
   * as a sequence, with no possibility of an exception or early return, will have zero "total
   * branches".
   */
  public static class BranchCoverage {
    public double uncovRatio;
  }

  /**
   * Initialize the coverage tracker and also create a copy of the set of names of classes that are
   * under test.
   *
   * @param classesUnderTest names of all the classes under test
   */
  public CoverageTracker(Set<String> classesUnderTest) {
    this.classesUnderTest = new HashSet<>(classesUnderTest);
  }

  /**
   * Retrieve execution data from the Jacoco Java agent and merge the coverage information
   * into executionData.
   */
  private void collectCoverageInformation() {
    try {
      InputStream execDataStream = new ByteArrayInputStream(RT.getAgent().getExecutionData(false));
      final ExecutionDataReader reader = new ExecutionDataReader(execDataStream);
      reader.setExecutionDataVisitor(new IExecutionDataVisitor() {
        @Override
        public void visitClassExecution(final ExecutionData data) {
          executionData.put(data);
        }
      });
      reader.read();
      execDataStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Updates branch coverage information for all methods under test. At this point, Jacoco has
   * already generated coverage data while Randoop has been constructing and executing its test
   * sequences. Coverage data is now collected and the {@code branchCoverageMap} is updated to
   * contain the updated coverage information of each method branch.
   */
  public void updateBranchCoverageMap() {
    // Collect coverage information.
    collectCoverageInformation();

    CoverageBuilder coverageBuilder = new CoverageBuilder();
    Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

    // For each class that is under test, summarize the branch coverage information
    // produced by Jacoco and update the coverageBuilder to store this information.
    for (String className : classesUnderTest) {
      String resource = getResourceFromClassName(className);
      InputStream original = getClass().getResourceAsStream(resource);
      try {
        analyzer.analyzeClass(original, className);
        original.close();
      } catch (IOException e) {
        throw new Error(e);
      }
    }

    // For each method under test, copy its branch coverage information from the coverageBuilder to
    // branchCoverageMap.
    for (final IClassCoverage cc : coverageBuilder.getClasses()) {
      for (final IMethodCoverage cm : cc.getMethods()) {
        String methodName = cc.getName() + "." + cm.getName();
        // Randoop defines method names with only periods delimiters.
        // Thus, for the method names produced by Jacoco we replace
        // forward slashes that are used to delimit packages
        // and $ that are used to delimit nested classes.
        methodName = methodName.replaceAll("/", ".");
        methodName = methodName.replaceAll("\\$", ".");

         System.out.println(methodName + " - " + cm.getBranchCounter().getMissedRatio());

        BranchCoverage methodCoverage = branchCoverageMap.get(methodName);
        if (methodCoverage == null) {
          methodCoverage = new BranchCoverage();
          branchCoverageMap.put(methodName, methodCoverage);
        }

        // In cases where a method's total branches is zero, the missed ratio is NaN, and
        // the resulting uncovRatio is set to zero.
        double uncovRatio = cm.getBranchCounter().getMissedRatio();
        methodCoverage.uncovRatio = Double.isNaN(uncovRatio) ? 0 : uncovRatio;
      }
    }
     System.out.println("--------------------------- ");
  }

  /**
   * Construct the absolute resource name of a class given a class name.
   *
   * @param className name of class
   * @return absolute resource name of the class
   */
  private String getResourceFromClassName(String className) {
    return '/' + className.replace('.', '/') + ".class";
  }

  /**
   * Returns the branch coverage associated with the input method.
   *
   * @param methodName name of the method to examine
   * @return branch coverage associated with the method
   */
  public BranchCoverage getBranchCoverageForMethod(String methodName) {
    return this.branchCoverageMap.get(methodName);
  }
}
