package randoop.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.io.Writer;
import java.lang.InterruptedException;
import java.lang.ProcessBuilder;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import plume.TimeLimitProcess;

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

  private static final String SOURCE_DIR_NAME = "java";
  private static final String CLASS_DIR_NAME = "class";

  /** the classpath for this test class */
  private static String classpath = null;

  /** the root for the system test working directories */
  private static Path workingDirsRoot = null;

  /**
   * Sets up the paths for test execution.
   */
  @BeforeClass
  public static void setupClass() {
    classpath = System.getProperty("java.class.path");
    /* the current working directory for this test class */
    Path currentWorkingDir = Paths.get("").toAbsolutePath().normalize();
    workingDirsRoot = currentWorkingDir.resolve("working-directories");
  }

  /**
   * Test formerly known as randoop1
   * This test previously did a diff on TestClass0.java with goal file.
   */
  @Test
  public void runCollectionsTest() {

    Path workingPath = createTestDirectory(workingDirsRoot, "collections-test");
    String packageName = "foo.bar";
    String regressionBasename = "TestClass";
    String errorBasename = "";

    List<String> options =
        getStandardOptions(workingPath, packageName, regressionBasename, errorBasename);
    options.add("--no-error-revealing-tests");
    options.add("--inputlimit=500");
    options.add("--testclass=java2.util2.TreeSet");
    options.add("--testclass=java2.util2.Collections");
    options.add("--npe-on-null-input=EXPECTED");
    options.add("--debug_checks");
    options.add("--observers=resources/systemTest/randoop1_observers.txt");
    options.add("--omit-field-list=resources/systemTest/testclassomitfields.txt");

    long timeout = 60000L;
    RandoopRunDescription randoopRunDesc =
        generateAndCompile(
            classpath,
            workingPath,
            packageName,
            regressionBasename,
            errorBasename,
            options,
            timeout);

    assertThat("...has regression tests", randoopRunDesc.regressionTestCount, is(greaterThan(0)));
    TestRunDescription testRunDesc =
        runTests(classpath, workingPath, packageName, regressionBasename);
    assertThat(
        "all regression tests should pass",
        testRunDesc.testsSucceed,
        is(equalTo(randoopRunDesc.regressionTestCount)));

    assertThat("...has no error tests", randoopRunDesc.errorTestCount, is(equalTo(0)));
  }

  /**
   * Test formerly known as randoop2
   * Previously did a diff on generated test.
   */
  @Test
  public void runNaiveCollectionsTest() {

    Path workingPath = createTestDirectory(workingDirsRoot, "naive-collections-test");
    String packageName = "foo.bar";
    String regressionBasename = "NaiveRegression";
    String errorBasename = "NaiveError";

    List<String> options =
        getStandardOptions(workingPath, packageName, regressionBasename, errorBasename);
    options.add("--inputlimit=100");
    options.add("--testclass=java2.util2.TreeSet");
    options.add("--testclass=java2.util2.ArrayList");
    options.add("--testclass=java2.util2.LinkedList");
    options.add("--testclass=java2.util2.Collections");
    options.add("--omit-field-list=resources/systemTest/naiveomitfields.txt");

    RandoopRunDescription randoopRunDesc =
        generateAndCompile(
            classpath, workingPath, packageName, regressionBasename, errorBasename, options);

    assertThat("...has regression tests", randoopRunDesc.regressionTestCount, is(greaterThan(0)));
    TestRunDescription testRunDesc =
        runTests(classpath, workingPath, packageName, regressionBasename);
    assertThat(
        "all regression tests should pass",
        testRunDesc.testsSucceed,
        is(equalTo(randoopRunDesc.regressionTestCount)));

    if (randoopRunDesc.errorTestCount > 0) {
      TestRunDescription errorTestRunDesc = runTests(classpath, workingPath, packageName, errorBasename);
      assertThat("all regression tests should fail", errorTestRunDesc.testsFail, is(equalTo(randoopRunDesc.errorTestCount)));
    }
  }

  /**
   * Test formerly known as randoop3
   * Previously this test did nothing beyond generating the tests.
   */
  @Test
  public void runJDKTest() {

    Path workingPath = createTestDirectory(workingDirsRoot, "jdk-test");
    String packageName = "jdktests";
    String regressionBasename = "JDK_Tests_regression";
    String errorBasename = "JDK_Tests_error";

    List<String> options =
        getStandardOptions(workingPath, packageName, regressionBasename, errorBasename);
    options.add("--inputlimit=1000");
    options.add("--null-ratio=0.3");
    options.add("--alias-ratio=0.3");
    options.add("--small-tests");
    options.add("--clear=100");
    options.add("--classlist=resources/systemTest/jdk_classlist.txt");

    RandoopRunDescription randoopRunDesc =
        generateAndCompile(
            classpath, workingPath, packageName, regressionBasename, errorBasename, options);

    assertThat("...has regression tests", randoopRunDesc.regressionTestCount, is(greaterThan(0)));
    TestRunDescription testRunDesc =
        runTests(classpath, workingPath, packageName, regressionBasename);
    assertThat(
        "all regression tests should pass",
        testRunDesc.testsSucceed,
        is(equalTo(randoopRunDesc.regressionTestCount)));

    // this is flaky - sometimes there are error tests, and sometimes not
    if (randoopRunDesc.errorTestCount > 0) {
      TestRunDescription errorTestRunDesc = runTests(classpath, workingPath, packageName, errorBasename);
      assertThat("all error tests should fail", errorTestRunDesc.testsFail, is(equalTo(randoopRunDesc.errorTestCount)));
    }
    // assertThat("...has no error tests", randoopRunDesc.errorTestCount, is(equalTo(0)));
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

    Path workingPath = createTestDirectory(workingDirsRoot, "contracts-test"); // temp directory
    String packageName = "";
    String regressionBasename = "";
    String errorBasename = "BuggyTest";

    List<String> options =
        getStandardOptions(workingPath, packageName, regressionBasename, errorBasename);
    options.add("--no-regression-tests");
    options.add("--inputlimit=1000");
    options.add("--classlist=resources/systemTest/buggyclasses.txt");

    long timeout = 120000L;

    RandoopRunDescription randoopRunDesc =
        generateAndCompile(
            classpath,
            workingPath,
            packageName,
            regressionBasename,
            errorBasename,
            options,
            timeout);

    assertThat(
        "Contracts test should have no regression tests",
        randoopRunDesc.regressionTestCount,
        is(equalTo(0)));

    assertTrue("...should have error tests", randoopRunDesc.errorTestCount > 0);
    TestRunDescription testRunDesc = runTests(classpath, workingPath, packageName, errorBasename);
    assertThat(
        "...all error tests should fail",
        testRunDesc.testsFail,
        is(equalTo(randoopRunDesc.errorTestCount)));
  }

  /**
   * Test formerly known as randoop-checkrep
   */
  @Test
  public void runCheckRepTest() {

    Path workingPath = createTestDirectory(workingDirsRoot, "checkrep-test"); // temp directory
    String packageName = "";
    String regressionBasename = "";
    String errorBasename = "CheckRepTest";

    List<String> options =
        getStandardOptions(workingPath, packageName, regressionBasename, errorBasename);
    options.add("--no-regression-tests");
    options.add("--timelimit=2");
    options.add("--testclass=examples.CheckRep1");
    options.add("--testclass=examples.CheckRep2");

    RandoopRunDescription randoopRunDesc =
        generateAndCompile(
            classpath, workingPath, packageName, regressionBasename, errorBasename, options);

    assertThat(
        "Contracts test should have no regression tests",
        randoopRunDesc.regressionTestCount,
        is(equalTo(0)));

    assertThat("...should have 2 error tests", randoopRunDesc.errorTestCount, is(equalTo(2)));
    TestRunDescription testRunDesc = runTests(classpath, workingPath, packageName, errorBasename);
    assertThat(
        "...all error tests should fail",
        testRunDesc.testsFail,
        is(equalTo(randoopRunDesc.errorTestCount)));
  }

  /**
   * Test formerly known as randoop-literals
   * Previously did a diff on generated test file and goal.
   */
  @Test
  public void runLiteralsTest() {

    Path workingPath = createTestDirectory(workingDirsRoot, "literals-test"); // temp directory
    String packageName = "";
    String regressionBasename = "LiteralsReg";
    String errorBasename = "LiteralsErr";

    List<String> options =
        getStandardOptions(workingPath, packageName, regressionBasename, errorBasename);
    options.add("--inputlimit=1000");
    options.add("--testclass=randoop.literals.A");
    options.add("--testclass=randoop.literals.A2");
    options.add("--testclass=randoop.literals.B");
    options.add("--literals-level=CLASS");
    options.add("--literals-file=resources/systemTest/literalsfile.txt");

    RandoopRunDescription randoopRunDesc =
        generateAndCompile(
            classpath, workingPath, packageName, regressionBasename, errorBasename, options);

    assertThat("...has regression tests", randoopRunDesc.regressionTestCount, is(greaterThan(0)));
    TestRunDescription testRunDesc =
        runTests(classpath, workingPath, packageName, regressionBasename);
    assertThat(
        "all regression tests should pass",
        testRunDesc.testsSucceed,
        is(equalTo(randoopRunDesc.regressionTestCount)));

    assertThat("...has no error tests", randoopRunDesc.errorTestCount, is(equalTo(0)));
  }

  /**
   * Test formerly known as randoop-long-string
   * Previously performed a diff on generated test and goal file.
   */
  @Test
  public void runLongStringTest() {
    Path workingPath = createTestDirectory(workingDirsRoot, "longstring-test"); // temp directory
    String packageName = "";
    String regressionBasename = "LongString";
    String errorBasename = "";

    List<String> options =
        getStandardOptions(workingPath, packageName, regressionBasename, errorBasename);
    options.add("--timelimit=1");
    options.add("--testclass=randoop.test.LongString");

    RandoopRunDescription randoopRunDesc =
        generateAndCompile(
            classpath, workingPath, packageName, regressionBasename, errorBasename, options);

    assertThat("...has regression tests", randoopRunDesc.regressionTestCount, is(greaterThan(0)));
    TestRunDescription testRunDesc =
        runTests(classpath, workingPath, packageName, regressionBasename);
    assertThat(
        "all regression tests should pass",
        testRunDesc.testsSucceed,
        is(equalTo(randoopRunDesc.regressionTestCount)));

    assertThat("...has no error tests", randoopRunDesc.errorTestCount, is(equalTo(0)));
  }

  /**
   * Test formerly known as randoop-visibility
   */
  @Test
  public void runVisibilityTest() {
    Path workingPath = createTestDirectory(workingDirsRoot, "visibility-test"); // temp directory
    String packageName = "";
    String regressionBasename = "VisibilityTest";
    String errorBasename = "";

    List<String> options =
        getStandardOptions(workingPath, packageName, regressionBasename, errorBasename);
    options.add("--timelimit=2");
    options.add("--testclass=examples.Visibility");

    RandoopRunDescription randoopRunDesc =
        generateAndCompile(
            classpath, workingPath, packageName, regressionBasename, errorBasename, options);

    assertThat("...has regression tests", randoopRunDesc.regressionTestCount, is(greaterThan(0)));
    TestRunDescription testRunDesc =
        runTests(classpath, workingPath, packageName, regressionBasename);
    assertThat(
        "all regression tests should pass",
        testRunDesc.testsSucceed,
        is(equalTo(randoopRunDesc.regressionTestCount)));

    assertThat("...has no error tests", randoopRunDesc.errorTestCount, is(equalTo(0)));
  }

  /**
   * Test formerly known as randoop-no-output.
   * Runs with <tt>--noprogressdisplay</tt> and so should have no output.
   */
  @Test
  public void runNoOutputTest() {
    Path workingPath = createTestDirectory(workingDirsRoot, "no-output-test"); // temp directory
    String packageName = "";
    String regressionBasename = "NoOutputTest";
    String errorBasename = "";

    List<String> options =
        getStandardOptions(workingPath, packageName, regressionBasename, errorBasename);
    options.add("--timelimit=1");
    options.add("--testclass=java.util.LinkedList");
    options.add("--noprogressdisplay");

    RandoopRunDescription randoopRunDesc =
        generateAndCompile(
            classpath, workingPath, packageName, regressionBasename, errorBasename, options);

    assertThat(
        "There should be no output",
        randoopRunDesc.processStatus.outputLines.size(),
        is(equalTo(0)));
  }

  @Test
  public void runInnerClassTest() {
    Path workingPath = createTestDirectory(workingDirsRoot, "inner-class-test");
    String packageName = "";
    String regressionBasename = "InnerClass";
    String errorBasename = "InnerClass";

    List<String> options =
            getStandardOptions(workingPath, packageName, regressionBasename, errorBasename);
    options.add("--testclass=randoop.test.ClassWithInnerClass");
    options.add("--testclass=randoop.test.ClassWithInnerClass$A");
    options.add("--timelimit=2");
    options.add("--outputlimit=2");
//    options.add("--junit-reflection-allowed=false");
    options.add("--silently-ignore-bad-class-names");
    options.add("--unchecked-exception=ERROR");
    options.add("--no-regression-tests");
    options.add("--npe-on-null-input=ERROR");
    options.add("--npe-on-non-null-input=ERROR");

    RandoopRunDescription randoopRunDescription = generateAndCompile(classpath, workingPath, packageName, regressionBasename, errorBasename, options);
    assertThat("...should not have regression tests", randoopRunDescription.regressionTestCount, is(equalTo(0)));
    assertThat("...should have error tests", randoopRunDescription.errorTestCount, is(greaterThan(0)));
    TestRunDescription errorTestRunDesc = runTests(classpath, workingPath, packageName, errorBasename);
    if (errorTestRunDesc.testsFail != randoopRunDescription.errorTestCount) {
      for (String line : errorTestRunDesc.processStatus.outputLines) {
        System.err.println(line);
      }
      fail("all error tests should fail");
    }
  }

  @Test
  public void runParameterizedTypeTest() {
    Path workingPath = createTestDirectory(workingDirsRoot, "parameterized-type");
    String packageName = "";
    String regressionBasename = "ParamTypeReg";
    String errorBasename = "ParamTypeErr";

    List<String> options =
            getStandardOptions(workingPath, packageName, regressionBasename, errorBasename);
    options.add("--testclass=muse.SortContainer");
    options.add("--outputlimit=100");
    options.add("--timelimit=300");
    options.add("--forbid-null");
    options.add("--null-ratio=0");

    RandoopRunDescription randoopRunDescription = generateAndCompile(classpath, workingPath, packageName, regressionBasename, errorBasename, options);
    assertThat("...should have regression tests", randoopRunDescription.regressionTestCount, is(greaterThan(0)));
    TestRunDescription regressionTestRunDesc = runTests(classpath, workingPath, packageName, regressionBasename);
    if (regressionTestRunDesc.testsSucceed != randoopRunDescription.regressionTestCount) {
      for (String line : regressionTestRunDesc.processStatus.outputLines) {
        System.err.println(line);
      }
      fail("all regression tests should pass");
    }
    assertThat("...should not have error tests", randoopRunDescription.errorTestCount, is(equalTo(0)));

    for (String line : randoopRunDescription.processStatus.outputLines) {
      System.out.println(line);
    }
  }

  /**
   * simply runs Randoop on a class in the default package to ensure nothing breaks.
   */
  @Test
  public void runDefaultPackageTest() {
    Path workingPath = createTestDirectory(workingDirsRoot, "default-package");
    String packageName = "";
    String regressionBasename = "DefaultPackageReg";
    String errorBasename = "DefaultPackageErr";

    List<String> options =
      getStandardOptions(workingPath, packageName, regressionBasename, errorBasename);
      options.add("--testclass=ClassInDefaultPackage");
      options.add("--outputlimit=2");
      options.add("--timelimit=3");

      RandoopRunDescription randoopRunDescription = generateAndCompile(classpath, workingPath, packageName, regressionBasename, errorBasename, options);
      assertThat("...should have regression tests", randoopRunDescription.regressionTestCount, is(greaterThan(0)));
      TestRunDescription regressionTestRunDesc = runTests(classpath, workingPath, packageName, regressionBasename);
      if (regressionTestRunDesc.testsSucceed != randoopRunDescription.regressionTestCount) {
        for (String line : regressionTestRunDesc.processStatus.outputLines) {
          System.err.println(line);
        }
        fail("all regression tests should pass");
      }
      assertThat("...should not have error tests", randoopRunDescription.errorTestCount, is(equalTo(0)));
  }

  /********************************** utility methods ***************************/

  /**
   * Creates a working directory for a test using the given directory name.
   * Contains subdirectories:
   * <ul>
   *   <li> src - Java source of Randoop generated tests
   *   <li> classes - binaries of Randoop generated tests
   * </ul>
   * Will fail calling test if an {@code IOException} is thrown
   *
   * @param currentWorkingDir  the parent directory for created directory
   * @param dirname  the name of the directory to create
   * @return the path to the created directory
   */
  private Path createTestDirectory(Path currentWorkingDir, String dirname) {
    Path testDir = null;
    try {
      testDir = createSubDirectory(currentWorkingDir, dirname);
      createSubDirectory(testDir, SOURCE_DIR_NAME);
      createSubDirectory(testDir, CLASS_DIR_NAME);
    } catch (IOException e) {
      fail("failed to create working directory for test: " + e);
    }
    return testDir;
  }

  /**
   * Creates a directory in the given parent directory with the subdirectory name.
   *
   * @param parentDir  the parent directory
   * @param subdirName  the subdirectory name
   * @return the path of the created subdirectory
   */
  private Path createSubDirectory(Path parentDir, String subdirName) throws IOException {
    Path subDir = parentDir.resolve(subdirName);
    if (!Files.exists(subDir)) {
      Files.createDirectory(subDir);
    }
    return subDir;
  }

  /**
   * Creates a list of the basic Randoop options for generating system tests
   * including package and base names, and the working directory.
   * Assumes a name is non-null, and that an empty string indicates that the
   * option is not being used.
   * Requires that at least one of the regression or error basenames be non-empty.
   *
   * @param workingPath  the working directory for the test
   * @param packageName  the packageName for generated tests, empty if none
   * @param regressionBasename  the regression test basename, empty if none
   * @param errorBasename  the error test basename, empty if none
   *
   * @return the Randoop options constructed from the parameters
   */
  private List<String> getStandardOptions(
          Path workingPath, String packageName, String regressionBasename, String errorBasename) {
    assert (errorBasename.length() > 0 || regressionBasename.length() > 0)
        : "either error or regression basenames must be nonempty";
    List<String> options = new ArrayList<>();
    if (regressionBasename.length() > 0) {
      options.add("--regression-test-basename=" + regressionBasename);
    }
    if (errorBasename.length() > 0) {
      options.add("--error-test-basename=" + errorBasename);
    }
    if (packageName.length() > 0) {
      options.add("--junit-package-name=" + packageName);
    }
    Path srcDir = workingPath.resolve(SOURCE_DIR_NAME);
    options.add("--junit-output-dir=" + srcDir);
    options.add("--log=" + workingPath + "/randoop-log.txt");

    return options;
  }

  private class RandoopRunDescription {
    final ProcessStatus processStatus;
    final int operatorCount;
    final int regressionTestCount;
    final int errorTestCount;

    RandoopRunDescription(
            ProcessStatus processStatus,
            int operatorCount,
            int regressionTestCount,
            int errorTestCount) {
      this.processStatus = processStatus;
      this.operatorCount = operatorCount;
      this.regressionTestCount = regressionTestCount;
      this.errorTestCount = errorTestCount;
    }
  }

  private RandoopRunDescription getRandoopRunDescription(ProcessStatus ps) {
    int operatorCount = 0;
    int regressionTestCount = 0;
    int errorTestCount = 0;

    for (String line : ps.outputLines) {
      if (line.contains("PUBLIC MEMBERS=") || line.contains("test count:")) {
        int count = Integer.valueOf(line.replaceFirst("\\D*(\\d*).*", "$1"));
        if (line.contains("PUBLIC MEMBERS=")) {
          operatorCount = count;
        } else if (line.contains("Regression")) {
          regressionTestCount = count;
        } else if (line.contains("Error")) {
          errorTestCount = count;
        }
      }
    }

    return new RandoopRunDescription(ps, operatorCount, regressionTestCount, errorTestCount);
  }

  private RandoopRunDescription generateAndCompile(
          String classpath,
          Path workingPath,
          String packageName,
          String regressionBasename,
          String errorBasename,
          List<String> randoopOptions) {
    long defaultTimeout = 15000L;
    return generateAndCompile(
        classpath,
        workingPath,
        packageName,
        regressionBasename,
        errorBasename,
        randoopOptions,
        defaultTimeout);
  }

  private RandoopRunDescription generateAndCompile(
          String classpath,
          Path workingPath,
          String packageName,
          String regressionBasename,
          String errorBasename,
          List<String> randoopOptions,
          long timeout) {

    ProcessStatus randoopExitStatus = runRandoop(classpath, randoopOptions, timeout);

    // runCommand should take care of this, but let's just be sure
    if (randoopExitStatus.exitStatus != 0) {
      for (String line : randoopExitStatus.outputLines) {
        System.err.println(line);
      }
      fail("Randoop exited badly, exit value = " + randoopExitStatus.exitStatus);
    }

    // determine whether files are really there and have the right names
    Path srcDir = workingPath.resolve(SOURCE_DIR_NAME);
    List<File> testClassSourceFiles = new ArrayList<>();
    Path sourcePath = srcDir.resolve(packageName.replace('.', '/'));
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourcePath, "*.java")) {
      for (Path entry : stream) {
        String filename = entry.getFileName().toString();
        assertThat(
            "Test class filename should start with basename",
            filename,
            is(anyOf(startsWith(regressionBasename), startsWith(errorBasename))));
        testClassSourceFiles.add(entry.toFile());
      }
    } catch (IOException e) {
      fail("Exception reading working directory " + e);
    }

    // definitely cannot do anything useful if no generated test files
    // but not sure that this is the right way to deal with it
    // what if test is meant not to generate anything ?
    if (testClassSourceFiles.size() == 0) {
      for (String line : randoopExitStatus.outputLines) {
        System.err.println(line);
      }
      fail("No test class source files found");
    }

    Path classDir = workingPath.resolve(CLASS_DIR_NAME);
    CompileStatus compileStatus = compileTests(testClassSourceFiles, classDir.toString());
    if (! compileStatus.succeeded) {
      for (Diagnostic<? extends JavaFileObject> diag : compileStatus.diagnostics) {
        String sourceName = diag.getSource().toUri().toString();
        System.err.printf("Error on %d of %s%n%s%n", diag.getLineNumber(), sourceName, diag.getMessage(null));
      }
      fail("Compilation failed");
    }

    // collect class files for generated tests
    List<File> testClassClassFiles = new ArrayList<>();
    Path testFilePath = classDir.resolve(packageName.replace('.', '/'));
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(testFilePath, "*.class")) {
      for (Path entry : stream) {
        String filename = entry.getFileName().toString();
        assertThat(
            "Test class filename should start with basename",
            filename,
            is(anyOf(startsWith(regressionBasename), startsWith(errorBasename))));
        testClassClassFiles.add(entry.toFile());
      }
    } catch (IOException e) {
      fail("Exception reading working directory " + e);
    }
    assertThat(
        "Number of compiled tests equals source tests",
        testClassClassFiles.size(),
        is(equalTo(testClassSourceFiles.size())));

    return getRandoopRunDescription(randoopExitStatus);
  }

  private class TestRunDescription {
    final ProcessStatus processStatus;
    final int testsRun;
    final int testsFail;
    final int testsSucceed;

    TestRunDescription(
            ProcessStatus processStatus, int testsRun, int testsFail, int testsSucceed) {
      this.processStatus = processStatus;
      this.testsRun = testsRun;
      this.testsFail = testsFail;
      this.testsSucceed = testsSucceed;
    }
  }

  private TestRunDescription getTestRunDescription(ProcessStatus ps) {
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

    return new TestRunDescription(ps, testsRun, testsFail, testsSucceed);
  }

  private TestRunDescription runTests(
          String classpath, Path workingPath, String packageName, String basename) {
    long defaultTimeout = 10000L;
    return runTests(classpath, workingPath, packageName, basename, defaultTimeout);
  }

  private TestRunDescription runTests(
          String classpath, Path workingPath, String packageName, String basename, long timeout) {
    Path classDir = workingPath.resolve(CLASS_DIR_NAME);
    String testClasspath = classpath + ":" + classDir.toString();

    String jUnitTestSuiteName = "";
    if (!packageName.isEmpty()) {
      jUnitTestSuiteName = packageName + ".";
    }
    jUnitTestSuiteName += basename;

    ProcessStatus testRunStatus = runGeneratedTests(testClasspath, jUnitTestSuiteName, timeout);

    return getTestRunDescription(testRunStatus);
  }

  /**
   * Runs randoop using the given options.
   * Note: the timeout is for the command process and can be different than
   * the timeout given in the options.
   *
   * @param classpath  the classpath for running Randoop
   * @param options  the Randoop options
   * @param timeout  the timeout (in milliseconds) for running Randoop
   */
  //classpath should be process classpath + inputtests
  private ProcessStatus runRandoop(String classpath, List<String> options, long timeout) {
    List<String> command = new ArrayList<>();
    command.add("java");
    command.add("-ea");
    command.add("-classpath");
    command.add(classpath);
    command.add("randoop.main.Main");
    command.add("gentests");
    command.addAll(options);
    return runCommand(command, timeout);
  }

  /**
   * Class to hold the return status from running a command assuming that it
   * is run in a process where stderr and stdout are linked.
   * Includes the exit status, and the list of output lines.
   */
  private class ProcessStatus {
    final List<String> command;
    final int exitStatus;
    final List<String> outputLines;

    ProcessStatus(List<String> command, int exitStatus, List<String> outputLines) {
      this.command = command;
      this.exitStatus = exitStatus;
      this.outputLines = outputLines;
    }
  }

  /**
   * Runs the given command in a new process using the given timeout.
   *
   * @param command  the command to be run in the process
   * @param timeout  the timeout (in milliseconds) for the command
   * @return the exit status and combined standard stream output
   */
  private ProcessStatus runCommand(List<String> command, long timeout) {

    ProcessBuilder randoopBuilder = new ProcessBuilder(command);
    randoopBuilder.redirectErrorStream(true);

    TimeLimitProcess p = null;

    try {
      p = new TimeLimitProcess(randoopBuilder.start(), timeout, true);
    } catch (IOException e) {
      fail("Exception starting process: " + e);
    }

    int exitValue = -1;
    try {
      exitValue = p.waitFor();
    } catch (InterruptedException e) {
      // TODO (maybe) print stream (stderr is linked with stdout)
      fail("Exception running process: " + e);
    }

    List<String> outputLines = new ArrayList<>();
    try (BufferedReader rdr = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
      String line = rdr.readLine();
      while (line != null) {
        outputLines.add(line);
        line = rdr.readLine();
      }
    } catch (IOException e) {
      fail("Exception getting output " + e);
    }

    if (p.timed_out()) {
      for (String line : outputLines) {
        System.out.println(line);
      }
      assert !p.timed_out() : "Process timed out after " + p.timeout_msecs() + " msecs";
    }
    return new ProcessStatus(command, exitValue, outputLines);
  }

  private class CompileStatus {

    private final Boolean succeeded;
    private final List<Diagnostic<? extends JavaFileObject>> diagnostics;

    public CompileStatus(Boolean succeeded, List<Diagnostic<? extends JavaFileObject>> diagnostics) {
      this.succeeded = succeeded;
      this.diagnostics = diagnostics;
    }
  }
  /**
   * Compile the test files, writing the class files to the desination directory.
   *
   * @param testSourceFiles  the Java source for the tests
   * @param destinationDir  the path to the desination directory
   * @return true if compile succeeded, false otherwise
   */
  private CompileStatus compileTests(List<File> testSourceFiles, String destinationDir) {
    Locale locale = null; // use default locale
    Charset charset = null; // use default charset
    Writer writer = null; // use System.err for output
    List<String> annotatedClasses = null; // no classes

    List<String> options = new ArrayList<>();
    options.add("-d");
    options.add(destinationDir);

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    Boolean succeeded = false;
    try (StandardJavaFileManager fileManager =
            compiler.getStandardFileManager(diagnostics, locale, charset)) {
      Iterable<? extends JavaFileObject> filesToCompile =
          fileManager.getJavaFileObjectsFromFiles(testSourceFiles);
      succeeded =
          compiler
              .getTask(writer, fileManager, diagnostics, options, annotatedClasses, filesToCompile)
              .call();
    } catch (IOException e) {
      fail("I/O Error while compiling generated tests: " + e);
    }
    return new CompileStatus(succeeded, diagnostics.getDiagnostics());
  }

  /**
   * Runs the given JUnit suite in a separate process.
   *
   * @param classpath  the classpath for the tests
   * @param junitTestName  the name of the test suite
   * @param timeout  the timeout (milliseconds) for running the test
   * @return the capture of exit status and standard stream output for test run
   */
  private ProcessStatus runGeneratedTests(String classpath, String junitTestName, long timeout) {
    List<String> command = new ArrayList<>();
    command.add("java");
    command.add("-ea");
    command.add("-classpath");
    command.add(classpath);
    command.add("org.junit.runner.JUnitCore");
    command.add(junitTestName);
    return runCommand(command, timeout);
  }
}
