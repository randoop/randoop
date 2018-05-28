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
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import randoop.main.GenInputsAbstract;
import randoop.types.ClassOrInterfaceType;

/**
 * Tracks the branch coverage of each method under test. Specifically, for each method under test,
 * this class records the total number of branches and the number of branches that have not been
 * covered in generated tests. This class periodically updates branch coverage information for each
 * method from Jacoco's data structures.
 */
public class CoverageTracker {
  /** In-memory store of the coverage information for all classes under test. */
  private final ExecutionDataStore executionData = new ExecutionDataStore();

  /**
   * Map from method name to uncovered branch ratio. In cases where a method's total branches is
   * zero, the missed ratio is NaN, and this map uses zero instead.
   */
  private final Map<String, Double> branchCoverageMap = new HashMap<>();

  /** Names of all the classes under test */
  public final Set<String> classesUnderTest = new HashSet<>();

  /**
   * Initialize the coverage tracker.
   *
   * @param classInterfaceTypes names of all the classes under test
   */
  public CoverageTracker(Set<ClassOrInterfaceType> classInterfaceTypes) {
    for (ClassOrInterfaceType classOrInterfaceType : classInterfaceTypes) {
      classesUnderTest.add(classOrInterfaceType.getRuntimeClass().getName());
    }
  }

  /**
   * Retrieve execution data from the Jacoco Java agent and merge the coverage information into
   * {@code executionData}.
   */
  private void collectCoverageInformation() {
    try {
      // Retrieve the execution data from the Jacoco Java agent.
      final InputStream execDataStream =
          new ByteArrayInputStream(RT.getAgent().getExecutionData(false));
      final ExecutionDataReader reader = new ExecutionDataReader(execDataStream);

      // The reader requires a session info visitor, however we don't need any information from it.
      reader.setSessionInfoVisitor(
          new ISessionInfoVisitor() {
            @Override
            public void visitSessionInfo(final SessionInfo info) {}
          });
      reader.setExecutionDataVisitor(
          new IExecutionDataVisitor() {
            @Override
            public void visitClassExecution(final ExecutionData data) {
              // Add the execution data for each class into the execution data store.
              executionData.put(data);
            }
          });
      reader.read();
      execDataStream.close();
    } catch (IOException e) {
      System.err.println("Error in Coverage Tracker in collecting coverage information.");
      e.printStackTrace();
      System.exit(1);
    }
  }

  /**
   * Updates branch coverage information for all methods under test. At this point, Jacoco has
   * already generated coverage data while Randoop has been constructing and executing its test
   * sequences. Coverage data is now collected and the {@code branchCoverageMap} is updated to
   * contain the updated coverage information of each method branch.
   */
  public void updateBranchCoverageMap() {
    // Collect coverage information. This updates the executionData object and gives us updated
    // coverage information for all of the classes under test.
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
        String ifMethodName = cc.getName() + "." + cm.getName();
        // Jacoco uses class names in internal form.
        // Randoop uses fully-qualified class names, with only periods as delimiters.
        String fqMethodName = ifMethodName.replaceAll("/", ".").replaceAll("\\$", ".");

        if (GenInputsAbstract.bloodhound_logging) {
          System.out.println(fqMethodName + " - " + cm.getBranchCounter().getMissedRatio());
        }

        // In cases where a method's total branches is zero, the missed ratio is NaN,
        // but use zero as the uncovRatio instead.
        double uncovRatio = cm.getBranchCounter().getMissedRatio();
        uncovRatio = Double.isNaN(uncovRatio) ? 0 : uncovRatio;
        branchCoverageMap.put(fqMethodName, uncovRatio);
      }
    }

    if (GenInputsAbstract.bloodhound_logging) {
      System.out.println("---------------------------");
    }
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
   * Returns the uncovered branch ratio associated with the input method.
   *
   * @param methodName name of the method to examine
   * @return uncovered branch ratio associated with the method
   */
  public Double getBranchCoverageForMethod(String methodName) {
    return this.branchCoverageMap.get(methodName);
  }
}
