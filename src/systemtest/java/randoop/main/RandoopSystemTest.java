package randoop.main;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.report.JavaNames;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import plume.UtilMDE;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * A JUnit test class that runs the Randoop system tests.
 * These are tests that were run from within the original Makefile using shell
 * commands.
 * The test methods in this class assume that the current working directory
 * has subdirectories <tt>resources/systemTest</tt> where resources files are
 * located (standard Gradle organization), and <tt>working-directories/</tt>
 * where working files can be written. The Gradle file sets the working directory
 * for the <tt>systemTest</tt> source set to which this class belongs.
 * <p>
 * Each of the test methods
 * <ul>
 *  <li> creates it's own subdirectory,
 *  <li> runs Randoop and saves generated tests to the subdirectory, and
 *  <li> compiles the generated test files.
 * </ul>
 * Most of the methods then run the tests and check that the expected number of
 * failed tests matches the number of error-revealing tests, or that the number
 * of passed tests matches the number of regression tests.
 * <p>
 * The Makefile also checked diffs of generated tests for some of the tests.
 * These methods do not do this check.
 */
public class RandoopSystemTest {

  private static SystemTestEnvironment systemTestEnvironment;

  /**
   * Sets up the environment for test execution.
   */
  @BeforeClass
  public static void setupClass() {
    String classpath = System.getProperty("java.class.path");
    /* the current working directory for this test class */
    Path buildDir = Paths.get("").toAbsolutePath().normalize();
    systemTestEnvironment = SystemTestEnvironment.createSystemTestEnvironment(classpath, buildDir);
  }

  /**
   * Enumerated type to quantify expected test generation:
   * <ul>
   *   <li>{@code SOME} - at least one test is generated,</li>
   *   <li>{@code NONE} - no tests are generated, or</li>
   *   <li>{@code DONT_CARE} - the number of tests does not need to be checked.</li>
   * </ul>
   */
  private enum ExpectedTests {
    SOME,
    NONE,
    DONT_CARE
  }

  /* --------------------------------------- test methods --------------------------------------- */

  /*
   * WRITING TEST METHODS:
   *
   * Methods with the Test annotation will be run normally as JUnit tests.
   * Each method should consist of one system test, and is responsible for setting up the
   * directories for the test, setting the options for Randoop, running Randoop, compiling the
   * generated tests, and then doing whatever checks are required for the test.  The steps each
   * test should follow are:
   *
   * 1. Set up the test environment.
   *
   *    Each test method should create the working environment for running the test with a call like
   *
   *      TestEnvironment testEnvironment = systemTestEnvironment.createTestEnvironment(testName);
   *
   *    where testName is the name of your test (be sure that it doesn't conflict with the name
   *    of any test already in this class).
   *    The variable systemTestEnvironment refers to the global environment for a run of the
   *    system tests, and contains information about the classpath, and directories needed while
   *    running all of the system tests.
   *
   * 2. Set the options for Randoop.
   *
   *    The method that executes Randoop takes the command-line arguments as a RandoopOptions object,
   *    which can be constructed by the line
   *      RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
   *    using the TestEnvironment built in the first step.
   *    This class has methods to explicitly set the test package name and base names:
   *      options.setPackageName("foo.bar");
   *      options.setRegressionBasename("TestClass");
   *      options.setErrorBasename("ErrorTestClass");
   *    And, the input classes should be specified by using the methods
   *      options.addTestClass(testClassName);
   *      options.addClassList(classListFilename);
   *    that correspond to the Randoop arguments.
   *    Other options can be set using the methods
   *      options.setFlag(flagName);
   *      options.setOption(optionName, optionValue);
   *    This object will also set options for output directories and logging, so only options
   *    affecting generation are needed.
   *
   *
   *  3. Run Randoop and compile generated tests.
   *
   *     This is where things can vary somewhat depending on the condition of the test.
   *
   *     In the majority of cases, we want to check that Randoop generates an expected number of
   *     regression and/or error-revealing tests; that the generated tests compile; and that when run,
   *     regression tests succeed, error tests fail, and that the methods of the classes-under-test
   *     are covered by all tests.  In this case, the test method will make a call like
   *
   *       generateAndTestWithCoverage(
   *         testEnvironment,
   *         options,
   *         expectedRegressionTests,
   *         expectedErrorTests);
   *
   *     where testEnvironment, packageName, regressionBasename, errorBasename, and options are all
   *     defined in steps 1 and 2.
   *     The expected-tests parameters are values of the ExpectedTests enumerated type.
   *     Use the value SOME if there must be at least one test, NONE if there should be no tests,
   *     and DONT_CARE if, well, it doesn't matter how many tests there are.
   *     The generateAndTestWithCoverage() method handles the standard test behavior, checking the standard
   *     assumptions about regression and error tests (given the quantifiers), and dumping output
   *     when the results don't meet expectations.
   *
   *     By default, coverage is checked against all methods returned by Class.getDeclaredMethods()
   *     for an input class. Some tests need to specifically exclude methods that Randoop should not
   *     generate.  These can be indicated by creating a Set<String> with these method names, and giving
   *     them as the last argument to the alternate version of generateAndTestWithCoverage(). When excluded
   *     methods are given, these methods may not be covered, and any method not excluded is
   *     expected to be covered.
   *
   *     As a stop-gap, the method
   *       generateAndTest(
   *         testEnvironment,
   *         options,
   *         expectedRegressionTests,
   *         expectedErrorTests);
   *     is used for tests where the coverage is non-deterministic. This is not meant to be a
   *     permanent solution, and new tests should not be written this way.
   *
   *     There are cases where the test may not follow this standard pattern. In that case, the
   *     test should minimally make a call like
   *       RandoopRunStatus randoopRunDesc =
   *           RandoopRunStatus.generateAndCompile(
   *              testEnvironment,
   *              options);
   *     where the arguments are defined in steps 1 and 2.
   *     This call will run Randoop to generate tests and then compile them.  All tests should
   *     minimally confirm that generated tests compile before testing anything else.
   *     Information about the Randoop run is included in the return value, including the output
   *     from the execution.
   */

  /**
   * Test formerly known as randoop1
   * This test previously did a diff on TestClass0.java with goal file.
   */
  @Test
  public void runCollectionsTest() {

    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("collections-test");

    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("foo.bar");
    options.setRegressionBasename("TestClass");
    options.addTestClass("java2.util2.TreeSet");
    options.addTestClass("java2.util2.Collections");
    options.setFlag("no-error-revealing-tests");
    options.setOption("inputlimit", "600");
    options.setOption("npe-on-null-input", "EXPECTED");
    options.setFlag("debug_checks");
    options.setOption("observers", "resources/systemTest/randoop1_observers.txt");
    options.setOption("omit-field-list", "resources/systemTest/testclassomitfields.txt");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;

    generateAndTest(testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  /**
   * Test formerly known as randoop2
   * Previously did a diff on generated test.
   */
  @Test
  public void runNaiveCollectionsTest() {

    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("naive-collections-test");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("foo.bar");
    options.setRegressionBasename("NaiveRegression");
    options.setErrorBasename("NaiveError");
    options.setOption("inputlimit", "100");
    options.addTestClass("java2.util2.TreeSet");
    options.addTestClass("java2.util2.ArrayList");
    options.addTestClass("java2.util2.LinkedList");
    options.addTestClass("java2.util2.Collections");
    options.setOption("omit-field-list", "resources/systemTest/naiveomitfields.txt");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.DONT_CARE;

    generateAndTest(testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  /**
   * Test formerly known as randoop3
   * Previously this test did nothing beyond generating the tests.
   */
  @Test
  public void runJDKTest() {

    TestEnvironment testEnvironment = systemTestEnvironment.createTestEnvironment("jdk-test");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("jdktests");
    options.setRegressionBasename("JDK_Tests_regression");
    options.setErrorBasename("JDK_Tests_error");

    options.setOption("inputlimit", "1000");
    options.setOption("null-ratio", "0.3");
    options.setOption("alias-ratio", "0.3");
    options.setFlag("small-tests");
    options.setFlag("clear=100");
    options.addClassList("resources/systemTest/jdk_classlist.txt");
    options.setOption(
        "omitmethods", "java2\\.util2\\.Collections\\.shuffle\\(java2\\.util2\\.List\\)");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.DONT_CARE;

    generateAndTest(testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  /**
   * Test formerly known as randoop-contracts
   * Takes a long time. Evidence from running
   * <tt>time make randoop-contracts</tt>
   * with previous Makefile. Reports:
   * <tt><pre>
   *  real	0m15.976s
   *  user	0m17.902s
   *  sys	0m9.814s
   * </pre></tt>
   */
  @Test
  public void runContractsTest() {

    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("contracts-test"); // temp directory
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setErrorBasename("BuggyTest");

    options.setFlag("no-regression-tests");
    options.setOption("inputlimit", "1000");
    options.addClassList("resources/systemTest/buggyclasses.txt");

    ExpectedTests expectedRegressionTests = ExpectedTests.NONE;
    ExpectedTests expectedErrorTests = ExpectedTests.SOME;

    Set<String> excludedMethods = new HashSet<>();

    // TODO check which of these should actually not be expected
    excludedMethods.add("examples.Buggy.BuggyCompareToSubs.compareTo(java.lang.Object)");
    excludedMethods.add("examples.Buggy.BuggyCompareToSubs.hashCode()");
    excludedMethods.add("examples.Buggy.BuggyEqualsTransitive.hashCode()");
    excludedMethods.add("examples.Buggy.BuggyCompareToReflexive.compareTo(java.lang.Object)");
    excludedMethods.add("examples.Buggy.BuggyCompareToReflexive.hashCode()");
    excludedMethods.add("examples.Buggy.BuggyCompareToAntiSymmetric.compareTo(java.lang.Object)");
    excludedMethods.add("examples.Buggy.BuggyCompareToAntiSymmetric.hashCode()");
    excludedMethods.add("examples.Buggy.BuggyCompareToEquals.compareTo(java.lang.Object)");
    excludedMethods.add("examples.Buggy.BuggyCompareToEquals.hashCode()");
    excludedMethods.add("examples.Buggy.BuggyCompareToTransitive.compareTo(java.lang.Object)");
    excludedMethods.add("examples.Buggy.BuggyCompareToTransitive.hashCode()");
    excludedMethods.add("examples.Buggy.hashCode()");
    excludedMethods.add("examples.Buggy.toString()");

    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests, excludedMethods);
  }

  /**
   * Test formerly known as randoop-checkrep
   */
  @Test
  public void runCheckRepTest() {

    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("checkrep-test"); // temp directory
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setErrorBasename("CheckRepTest");

    options.setFlag("no-regression-tests");
    options.setOption("timelimit", "2");
    options.addTestClass("examples.CheckRep1");
    options.addTestClass("examples.CheckRep2");

    ExpectedTests expectedRegressionTests = ExpectedTests.NONE;
    ExpectedTests expectedErrorTests = ExpectedTests.SOME;
    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  /**
   * Test formerly known as randoop-literals
   * Previously did a diff on generated test file and goal.
   */
  @Test
  public void runLiteralsTest() {

    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("literals-test"); // temp directory
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("");
    options.setRegressionBasename("LiteralsReg");
    options.setErrorBasename("LiteralsErr");

    options.setOption("inputlimit", "1000");
    options.addTestClass("randoop.literals.A");
    options.addTestClass("randoop.literals.A2");
    options.addTestClass("randoop.literals.B");
    options.setOption("literals-level", "CLASS");
    options.setOption("literals-file", "resources/systemTest/literalsfile.txt");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;
    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  /**
   * Test formerly known as randoop-long-string
   * Previously performed a diff on generated test and goal file.
   */
  @Test
  public void runLongStringTest() {
    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("longstring-test"); // temp directory
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("");
    options.setRegressionBasename("LongString");
    options.setErrorBasename("");

    options.setOption("timelimit", "1");
    options.addTestClass("randoop.test.LongString");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;
    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  /**
   * Test formerly known as randoop-visibility
   */
  @Test
  public void runVisibilityTest() {
    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("visibility-test"); // temp directory
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("");
    options.setRegressionBasename("VisibilityTest");
    options.setErrorBasename("");

    options.setOption("timelimit", "2");
    options.addTestClass("examples.Visibility");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;

    Set<String> excludedMethods = new HashSet<>();
    excludedMethods.add("examples.Visibility.getNonVisible()");
    excludedMethods.add("examples.Visibility.takesNonVisible(examples.NonVisible)");

    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests, excludedMethods);
  }

  /**
   * Test formerly known as randoop-no-output.
   * Runs with <tt>--noprogressdisplay</tt> and so should have no output.
   */
  @Test
  public void runNoOutputTest() {
    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("no-output-test"); // temp directory
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("");
    options.setRegressionBasename("NoOutputTest");
    options.setErrorBasename("");

    options.setOption("timelimit", "1");
    options.addTestClass("java.util.LinkedList");
    options.setFlag("noprogressdisplay");

    RandoopRunStatus randoopRunDesc = RandoopRunStatus.generateAndCompile(testEnvironment, options);

    assertThat(
        "There should be no output",
        randoopRunDesc.processStatus.outputLines.size(),
        is(equalTo(0)));
  }

  @Test
  public void runInnerClassTest() {
    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("inner-class-test");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("");
    options.setRegressionBasename("InnerClass");
    options.setErrorBasename("InnerClass");
    options.addTestClass("randoop.test.ClassWithInnerClass");
    options.addTestClass("randoop.test.ClassWithInnerClass$A");
    options.setOption("timelimit", "2");
    options.setOption("outputlimit", "2");
    //    options.setFlag("junit-reflection-allowed","false");
    options.setFlag("silently-ignore-bad-class-names");
    options.setOption("unchecked-exception", "ERROR");
    options.setFlag("no-regression-tests");
    options.setOption("npe-on-null-input", "ERROR");
    options.setOption("npe-on-non-null-input", "ERROR");

    ExpectedTests expectedRegressionTests = ExpectedTests.NONE;
    ExpectedTests expectedErrorTests = ExpectedTests.SOME;
    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  @Test
  public void runParameterizedTypeTest() {
    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("parameterized-type");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("");
    options.setRegressionBasename("ParamTypeReg");
    options.setErrorBasename("ParamTypeErr");
    options.addTestClass("muse.SortContainer");
    options.setOption("outputlimit", "100");
    options.setOption("timelimit", "300");
    options.setFlag("forbid-null");
    options.setOption("null-ratio", "0");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;
    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  @Test
  public void runRecursiveBoundTest() {
    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("recursive-bound");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("muse");
    options.setRegressionBasename("BoundsReg");
    options.setErrorBasename("BoundsErr");
    options.addTestClass("muse.RecursiveBound");
    options.setOption("outputlimit", "100");
    options.setOption("timelimit", "300");
    options.setFlag("forbid-null");
    options.setOption("null-ratio", "0");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;
    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  /**
   * simply runs Randoop on a class in the default package to ensure nothing breaks.
   */
  @Test
  public void runDefaultPackageTest() {
    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("default-package");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("");
    options.setRegressionBasename("DefaultPackageReg");
    options.setErrorBasename("DefaultPackageErr");
    options.addTestClass("ClassInDefaultPackage");
    options.setOption("outputlimit", "2");
    options.setOption("timelimit", "3");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;
    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  /**
   * Tests that Randoop deals properly with exceptions
   */
  @Test
  public void runExceptionTest() {
    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("exception-tests");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("misc");
    options.setRegressionBasename("RegressionTest");
    options.setErrorBasename("ErrorTest");
    options.addTestClass("misc.ThrowsAnonymousException");
    options.setOption("outputlimit", "2");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;
    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  /* ------------------------------ utility methods ---------------------------------- */

  /**
   * Runs a standard system test:
   * <ol>
   *   <li>runs Randoop and compiles the generated tests,</li>
   *   <li>checks that the number of generated tests meets the expectation (none or some),</li>
   *   <li>runs any generated tests,</li>
   *   <li>checks that types of tests run as expected.</li>
   * </ol>
   *
   * @param environment  the working environment
   * @param options  the Randoop command-line arguments
   * @param expectedRegression  the minimum expected number of regression tests
   * @param expectedError  the minimum expected number of error tests
   */
  private void generateAndTestWithCoverage(
      TestEnvironment environment,
      RandoopOptions options,
      ExpectedTests expectedRegression,
      ExpectedTests expectedError,
      Set<String> excludedMethods) {

    RandoopRunStatus runStatus = generateAndCompile(environment, options);

    String packageName = options.getPackageName();

    TestRunStatus regressionRunDesc =
        runRegressionTests(environment, options, expectedRegression, runStatus, packageName);

    TestRunStatus errorRunDesc =
        runErrorTests(environment, options, expectedError, runStatus, packageName);

    checkCoverage(options.getClassnames(), excludedMethods, regressionRunDesc, errorRunDesc);
  }

  /**
   * Performs a standard test of Randoop including a check of coverage that assumes all declared
   * methods of the classes under test should be covered.
   *
   * @param environment  the working environment of the test
   * @param options  the Randoop options
   * @param expectedRegression  the minimum expected number of regression tests
   * @param expectedError  the minimum expected error tests
   */
  private void generateAndTestWithCoverage(
      TestEnvironment environment,
      RandoopOptions options,
      ExpectedTests expectedRegression,
      ExpectedTests expectedError) {
    generateAndTestWithCoverage(
        environment, options, expectedRegression, expectedError, new HashSet<String>());
  }

  /**
   * Performs the standard test except does not check coverage.
   * This method is used (presumably) temporarily by some tests where the coverage is
   * non-deterministic, and should eventually not be needed.
   *
   * @see #runJDKTest()
   * @see #runCollectionsTest()
   * @see #runNaiveCollectionsTest()
   *
   * @param environment  the working environment for the test
   * @param options  the Randoop options for the test
   * @param expectedRegression  the quantifier for generated regression tests
   * @param expectedError  the quantifier for generated error tests
   */
  private void generateAndTest(
      TestEnvironment environment,
      RandoopOptions options,
      ExpectedTests expectedRegression,
      ExpectedTests expectedError) {
    RandoopRunStatus runStatus = generateAndCompile(environment, options);

    String packageName = options.getPackageName();

    // the result of running the tests is not used
    runRegressionTests(environment, options, expectedRegression, runStatus, packageName);

    // the result of running the tests is not used
    runErrorTests(environment, options, expectedError, runStatus, packageName);
  }

  /**
   * Checks that the expected number of error-revealing tests have been generated, and if any are
   * expected runs them, captures and returns the result.
   *
   * @param environment  the working environment for the test
   * @param options  the Randoop options
   * @param expectedError  the quantifier for the expected number of error tests
   * @param runStatus  the status of the Randoop run
   * @param packageName  the package name for generated tests
   * @return  the {@link TestRunStatus} for running the error tests, may be null
   */
  private TestRunStatus runErrorTests(
      TestEnvironment environment,
      RandoopOptions options,
      ExpectedTests expectedError,
      RandoopRunStatus runStatus,
      String packageName) {
    TestRunStatus errorRunDesc = null;
    switch (expectedError) {
      case SOME:
        assertThat("...has error tests", runStatus.errorTestCount, is(greaterThan(0)));
        String errorBasename = options.getErrorBasename();
        try {
          errorRunDesc = TestRunStatus.runTests(environment, packageName, errorBasename);
        } catch (IOException e) {
          fail("Exception collecting coverage from error tests: " + e.getMessage());
        }
        assert errorRunDesc.processStatus.exitStatus != 0 : "JUnit should exit with error";
        if (errorRunDesc.testsFail != errorRunDesc.testsRun) {
          for (String line : errorRunDesc.processStatus.outputLines) {
            System.err.println(line);
          }
          fail("all error tests should fail, but " + errorRunDesc.testsSucceed + " passed");
        }
        break;
      case NONE:
        assertThat("...has no error tests", runStatus.errorTestCount, is(equalTo(0)));
        break;
      case DONT_CARE:
        break;
    }
    return errorRunDesc;
  }

  /**
   * Checks that the expected number of regression tests have been generated, and if so runs them,
   * captures and returns the results.
   *
   * @param environment  the working environment of the test
   * @param options  the Randoop options
   * @param expectedRegression  the quantifier for expected regression tests
   * @param runStatus  the Randoop run status
   * @param packageName  the package name for generated tests
   * @return the {@link TestRunStatus} for the execution of the regression tests, null if there are none
   */
  private TestRunStatus runRegressionTests(
      TestEnvironment environment,
      RandoopOptions options,
      ExpectedTests expectedRegression,
      RandoopRunStatus runStatus,
      String packageName) {
    TestRunStatus regressionRunDesc = null;
    switch (expectedRegression) {
      case SOME:
        assertThat("...has regression tests", runStatus.regressionTestCount, is(greaterThan(0)));
        String regressionBasename = options.getRegressionBasename();
        try {
          regressionRunDesc = TestRunStatus.runTests(environment, packageName, regressionBasename);
        } catch (IOException e) {
          fail("Exception collecting coverage from regression tests: " + e.getMessage());
        }
        if (regressionRunDesc.processStatus.exitStatus != 0) {
          for (String line : regressionRunDesc.processStatus.outputLines) {
            System.err.println(line);
          }
          fail("JUnit should exit properly");
        }
        if (regressionRunDesc.testsSucceed != regressionRunDesc.testsRun) {
          for (String line : regressionRunDesc.processStatus.outputLines) {
            System.err.println(line);
          }
          fail("all regression tests should pass, but " + regressionRunDesc.testsFail + " failed");
        }
        break;
      case NONE:
        assertThat("...has no regression tests", runStatus.regressionTestCount, is(equalTo(0)));
        break;
      case DONT_CARE:
        break;
    }
    return regressionRunDesc;
  }

  /**
   * Runs Randoop using the given test environment and options, printing captured output to standard
   * output.
   *
   * @param environment  the working environment for the test
   * @param options  the Randoop options
   * @return the captured {@link RandoopRunStatus} from running Randoop
   */
  private RandoopRunStatus generateAndCompile(TestEnvironment environment, RandoopOptions options) {
    RandoopRunStatus runStatus = RandoopRunStatus.generateAndCompile(environment, options);

    boolean prevLineIsBlank = false;
    for (String line : runStatus.processStatus.outputLines) {
      if ((line.isEmpty() && !prevLineIsBlank)
          || (!line.isEmpty() && !line.startsWith("Progress update:"))) {
        System.out.println(line);
      }
      prevLineIsBlank = line.isEmpty();
    }
    return runStatus;
  }

  /**
   * Performs a coverage check for the given set of classes relative to the full set of tests.
   * Each declared method of a class that does not satisfy {@link #isIgnoredMethod(String)} is
   * checked for coverage.
   * If the method occurs in the excluded methods, then it must not be covered by any test.
   * Otherwise, the method must be covered by some test.
   *
   * @param classnames  the set of class names to check
   * @param excludedMethods  the methods that should not be covered
   * @param regressionStatus  the {@link TestRunStatus} from the regression tests
   * @param errorStatus  the {@link TestRunStatus} from the error tests
   */
  private void checkCoverage(
      Set<String> classnames,
      Set<String> excludedMethods,
      TestRunStatus regressionStatus,
      TestRunStatus errorStatus) {

    Set<String> missingMethods = new TreeSet<>();
    Set<String> shouldBeMissingMethods = new TreeSet<>();

    for (String classname : classnames) {
      Set<String> methods = new HashSet<>();

      String canonicalClassname = classname.replace('$', '.');
      getCoveredMethodsForClass(regressionStatus, canonicalClassname, methods);
      getCoveredMethodsForClass(errorStatus, canonicalClassname, methods);

      Class<?> c;
      try {
        c = Class.forName(classname);

        for (Method m : c.getDeclaredMethods()) {
          String methodname = methodName(m);
          if (!isIgnoredMethod(methodname)) {
            if (excludedMethods.contains(methodname)) {
              if (methods.contains(methodname)) {
                shouldBeMissingMethods.add(methodname);
              }
            } else {
              if (!methods.contains(methodname)) {
                missingMethods.add(methodname);
              }
            }
          } else {
            System.out.println("Ignoring " + methodname + " in coverage checks");
          }
        }
      } catch (ClassNotFoundException e) {
        fail("Could not load input class" + classname + ": " + e.getMessage());
      }
    }

    if (!missingMethods.isEmpty()) {
      String msg = String.format("Expected methods not covered:%n");
      for (String name : missingMethods) {
        msg += String.format("  %s%n", name);
      }
      fail(msg);
    }
    if (!shouldBeMissingMethods.isEmpty()) {
      String msg = String.format("Excluded methods that are covered:%n");
      for (String name : shouldBeMissingMethods) {
        msg += String.format("  %s%n", name);
      }
      fail(msg);
    }
  }

  /**
   * Adds methods from the given class to the set if they are covered in the {@link MethodCoverageMap}
   * of the given {@link TestRunStatus}.
   *
   * @param testRunStatus  the {@link TestRunStatus}
   * @param classname the name of the class
   * @param methods  the set to which method names are added
   */
  private void getCoveredMethodsForClass(
      TestRunStatus testRunStatus, String classname, Set<String> methods) {
    if (testRunStatus != null) {
      Set<String> regressionMethods = testRunStatus.coverageMap.getMethods(classname);
      if (regressionMethods != null) {
        methods.addAll(regressionMethods);
      }
    }
  }

  /**
   * Constructs a method signature for a {@code java.lang.reflect.Method} object in a format that
   * matches the name construction in
   * {@link MethodCoverageMap#getMethodName(JavaNames, IClassCoverage, String, IMethodCoverage)}.
   *
   * @param m  the {@code java.lang.reflect.Method} object
   * @return the method signature for the method object
   */
  private String methodName(Method m) {
    List<String> params = new ArrayList<>();
    for (Class<?> paramType : m.getParameterTypes()) {
      params.add(paramType.getCanonicalName());
    }
    return m.getDeclaringClass().getCanonicalName()
        + "."
        + m.getName()
        + "("
        + UtilMDE.join(params, ", ")
        + ")";
  }

  /**
   * Pattern for excluding method names from coverage checks.
   * Excludes JaCoCo, and Java private access inner class methods.
   */
  private static final Pattern IGNORE_PATTERN = Pattern.compile("\\$jacocoInit|access\\$\\d{3}+");

  /**
   * Indicates whether the given method name should be ignored during the coverage check.
   *
   * @param methodname  the method name
   * @return true if the method should be ignored, false otherwise
   */
  private boolean isIgnoredMethod(String methodname) {
    Matcher matcher = IGNORE_PATTERN.matcher(methodname);
    return matcher.find();
  }
}
