package randoop.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Captures the status of running a suite of JUnit tests.
 */
class TestRunStatus {
  final ProcessStatus processStatus;
  final int testsRun;
  final int testsFail;
  final int testsSucceed;
  final MethodCoverageMap coverageMap;

  private TestRunStatus(
      ProcessStatus processStatus,
      MethodCoverageMap coverageMap,
      int testsRun,
      int testsFail,
      int testsSucceed) {
    this.processStatus = processStatus;
    this.coverageMap = coverageMap;
    this.testsRun = testsRun;
    this.testsFail = testsFail;
    this.testsSucceed = testsSucceed;
  }

  /**
   * Runs the tests with the given basename, and captures and returns a description of the results.
   *
   * @param testEnvironment  the environment for this test run
   * @param packageName  the package name of the JUnit tests
   * @param basename  the base name of the JUnit files
   * @return the {@link TestRunStatus} for the execution of the JUnit tests
   */
  static TestRunStatus runTests(
      TestEnvironment testEnvironment, String packageName, String basename) throws IOException {
    String testClasspath = testEnvironment.testClassPath;
    Path jacocoDir = testEnvironment.jacocoDir;
    String execFile = jacocoDir.resolve(basename + "jacoco.exec").toString();
    String jUnitTestSuiteName = "";
    if (!packageName.isEmpty()) {
      jUnitTestSuiteName = packageName + ".";
    }
    jUnitTestSuiteName += basename;

    List<String> command = new ArrayList<>();
    command.add("java");
    command.add(
        "-javaagent:"
            + testEnvironment.getJacocoAgentPath().toString()
            + "="
            + "destfile="
            + execFile
            + ",excludes=org.junit.*");
    command.add("-ea");
    command.add("-classpath");
    command.add(testClasspath);
    command.add("org.junit.runner.JUnitCore");
    command.add(jUnitTestSuiteName);

    ProcessStatus status = ProcessStatus.runCommand(command);

    File classesDirectory = testEnvironment.getTestInputClassDir().toFile();
    MethodCoverageMap coverageMap = MethodCoverageMap.collectCoverage(execFile, classesDirectory);

    return getTestRunStatus(status, coverageMap);
  }

  /**
   * Translates the output of a run of a JUnit test suite to a {@link TestRunStatus},
   * extracting and adding information about the number of passing and failing tests.
   *
   * @param ps  the {@link ProcessStatus} of the run of the JUnit test suite
   * @return the run description for the given process results
   */
  private static TestRunStatus getTestRunStatus(ProcessStatus ps, MethodCoverageMap coverageMap) {
    int testsRun = 0;
    int testsSucceed = 0;
    int testsFail = 0;

    for (String line : ps.outputLines) {
      if (line.contains("OK (")) {
        testsSucceed = Integer.valueOf(line.replaceFirst("\\D*(\\d*).*", "$1"));
        testsRun = testsSucceed;
      } else if (line.contains("Failures:")) {
        String[] toks = line.split(",");
        assert toks.length == 2;
        testsRun = Integer.valueOf(toks[0].replaceFirst("\\D*(\\d*).*", "$1"));
        testsFail = Integer.valueOf(toks[1].replaceFirst("\\D*(\\d*).*", "$1"));
        testsSucceed = testsRun - testsFail;
      }
    }

    return new TestRunStatus(ps, coverageMap, testsRun, testsFail, testsSucceed);
  }
}
