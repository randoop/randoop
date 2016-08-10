package randoop.main;

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
   *       generateAndTest(
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
   *     The generateAndTest() method handles the standard test behavior, checking the standard
   *     assumptions about regression and error tests (given the quantifiers), and dumping output
   *     when the results don't meet expectations.
   *
   *     By default, coverage is checked against all methods returned by Class.getDeclaredMethods()
   *     for an input class. Some tests need to specifically exclude methods that Randoop should not
   *     generate.  These can be indicated by creating a Set<String> with these method names, and giving
   *     them as the last argument to the alternate version of generateAndTest(). When excluded
   *     methods are given, these methods may not be covered, and any method not excluded is
   *     expected to be covered.
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

    Set<String> excludedMethods = new HashSet<>();
    // these are excluded b/c cannot generate input object
    excludedMethods.add("java2.util2.TreeSet.readObject(java.io.ObjectInputStream)");
    excludedMethods.add("java2.util2.TreeSet.writeObject(java.io.ObjectOutputStream)");

    // Randoop not generating these
    excludedMethods.add("java2.util2.TreeSet.subSet(java.lang.Object, java.lang.Object)");
    excludedMethods.add("java2.util2.Collections.get(java2.util2.ListIterator, int)");
    excludedMethods.add(
        "java2.util2.Collections.iteratorBinarySearch(java2.util2.List, java.lang.Object)");
    excludedMethods.add(
        "java2.util2.Collections.iteratorBinarySearch(java2.util2.List, java.lang.Object, java2.util2.Comparator)");
    excludedMethods.add("java2.util2.Collections.rotate2(java2.util2.List, int)");
    excludedMethods.add("java2.util2.Collections.swap(java.lang.Object[], int, int)");
    excludedMethods.add("java2.util2.Collections.swap(java2.util2.List, int, int)");
    excludedMethods.add(
        "java2.util2.Collections.synchronizedCollection(java2.util2.Collection, java.lang.Object)");
    excludedMethods.add(
        "java2.util2.Collections.synchronizedList(java2.util2.List, java.lang.Object)");
    excludedMethods.add(
        "java2.util2.Collections.synchronizedSet(java2.util2.Set, java.lang.Object)");
    excludedMethods.add("java2.util2.Collections.synchronizedSortedMap(java2.util2.SortedMap)");
    excludedMethods.add("java2.util2.Collections.unmodifiableSortedMap(java2.util2.SortedMap)");
    generateAndTest(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests, excludedMethods);
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

    Set<String> excludedMethods = new HashSet<>();
    // these are excluded b/c cannot generate input object
    excludedMethods.add("java2.util2.TreeSet.readObject(java.io.ObjectInputStream)");
    excludedMethods.add("java2.util2.TreeSet.writeObject(java.io.ObjectOutputStream)");
    excludedMethods.add("java2.util2.ArrayList.readObject(java.io.ObjectInputStream)");
    excludedMethods.add("java2.util2.ArrayList.writeObject(java.io.ObjectOutputStream)");
    excludedMethods.add("java2.util2.LinkedList.readObject(java.io.ObjectInputStream)");
    excludedMethods.add("java2.util2.LinkedList.writeObject(java.io.ObjectOutputStream)");

    // these are excluded b/c Randoop is not generating them for some reason
    excludedMethods.add("java2.util2.TreeSet.subSet(java.lang.Object, java.lang.Object)");
    excludedMethods.add("java2.util2.Collections.get(java2.util2.ListIterator, int)");
    excludedMethods.add(
        "java2.util2.Collections.iteratorBinarySearch(java2.util2.List, java.lang.Object)");
    excludedMethods.add(
        "java2.util2.Collections.iteratorBinarySearch(java2.util2.List, java.lang.Object, java2.util2.Comparator)");
    excludedMethods.add("java2.util2.Collections.rotate2(java2.util2.List, int)");
    excludedMethods.add("java2.util2.Collections.swap(java.lang.Object[], int, int)");
    excludedMethods.add(
        "java2.util2.Collections.synchronizedCollection(java2.util2.Collection, java.lang.Object)");
    excludedMethods.add(
        "java2.util2.Collections.synchronizedList(java2.util2.List, java.lang.Object)");
    excludedMethods.add(
        "java2.util2.Collections.synchronizedSet(java2.util2.Set, java.lang.Object)");
    excludedMethods.add("java2.util2.Collections.synchronizedSortedMap(java2.util2.SortedMap)");
    excludedMethods.add("java2.util2.Collections.unmodifiableSortedMap(java2.util2.SortedMap)");
    excludedMethods.add("java2.util2.ArrayList.removeRange(int, int)");
    excludedMethods.add("java2.util2.Collections.swap(java2.util2.List, int, int)");
    excludedMethods.add("java2.util2.LinkedList.remove(int)");
    excludedMethods.add("java2.util2.TreeSet.headSet(java.lang.Object)");
    excludedMethods.add("java2.util2.TreeSet.tailSet(java.lang.Object)");

    excludedMethods.add("java2.util2.ArrayList.addAll(int, java2.util2.Collection)");
    excludedMethods.add("java2.util2.ArrayList.clear()");
    excludedMethods.add("java2.util2.ArrayList.set(int, java.lang.Object)");
    excludedMethods.add("java2.util2.ArrayList.trimToSize()");
    excludedMethods.add("java2.util2.Collections.enumeration(java2.util2.Collection)");
    excludedMethods.add("java2.util2.Collections.eq(java.lang.Object, java.lang.Object)");
    excludedMethods.add("java2.util2.Collections.fill(java2.util2.List, java.lang.Object)");
    excludedMethods.add(
        "java2.util2.Collections.indexedBinarySearch(java2.util2.List, java.lang.Object, java2.util2.Comparator)");
    excludedMethods.add(
        "java2.util2.Collections.min(java2.util2.Collection, java2.util2.Comparator)");
    excludedMethods.add("java2.util2.Collections.nCopies(int, java.lang.Object)");
    excludedMethods.add(
        "java2.util2.Collections.replaceAll(java2.util2.List, java.lang.Object, java.lang.Object)");
    excludedMethods.add("java2.util2.Collections.reverseOrder()");
    excludedMethods.add("java2.util2.Collections.synchronizedCollection(java2.util2.Collection)");
    excludedMethods.add("java2.util2.Collections.synchronizedList(java2.util2.List)");
    excludedMethods.add("java2.util2.Collections.synchronizedMap(java2.util2.Map)");
    excludedMethods.add("java2.util2.Collections.synchronizedSet(java2.util2.Set)");
    excludedMethods.add("java2.util2.Collections.synchronizedSortedSet(java2.util2.SortedSet)");
    excludedMethods.add("java2.util2.Collections.unmodifiableCollection(java2.util2.Collection)");
    excludedMethods.add("java2.util2.Collections.unmodifiableMap(java2.util2.Map)");
    excludedMethods.add("java2.util2.Collections.unmodifiableSortedSet(java2.util2.SortedSet)");
    excludedMethods.add("java2.util2.LinkedList.add(int, java.lang.Object)");
    excludedMethods.add("java2.util2.LinkedList.add(java.lang.Object)");
    excludedMethods.add("java2.util2.LinkedList.addFirst(java.lang.Object)");
    excludedMethods.add("java2.util2.LinkedList.clone()");
    excludedMethods.add("java2.util2.LinkedList.contains(java.lang.Object)");
    excludedMethods.add("java2.util2.LinkedList.get(int)");
    excludedMethods.add("java2.util2.LinkedList.getLast()");
    excludedMethods.add("java2.util2.LinkedList.indexOf(java.lang.Object)");
    excludedMethods.add("java2.util2.LinkedList.lastIndexOf(java.lang.Object)");
    excludedMethods.add("java2.util2.LinkedList.remove(java.lang.Object)");
    excludedMethods.add("java2.util2.LinkedList.remove(java2.util2.LinkedList.Entry)");
    excludedMethods.add("java2.util2.LinkedList.removeFirst()");
    excludedMethods.add("java2.util2.LinkedList.removeLast()");
    excludedMethods.add("java2.util2.LinkedList.set(int, java.lang.Object)");
    excludedMethods.add("java2.util2.LinkedList.toArray(java.lang.Object[])");
    excludedMethods.add("java2.util2.TreeSet.add(java.lang.Object)");
    excludedMethods.add("java2.util2.TreeSet.clear()");
    excludedMethods.add("java2.util2.TreeSet.contains(java.lang.Object)");
    excludedMethods.add("java2.util2.TreeSet.first()");
    excludedMethods.add("java2.util2.TreeSet.isEmpty()");
    excludedMethods.add("java2.util2.TreeSet.last()");

    generateAndTest(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests, excludedMethods);
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

    Set<String> excludedMethods = new HashSet<>();
    //this is excluded method - see above
    excludedMethods.add("java2.util2.Collections.shuffle(java2.util2.List)");

    // these are excluded b/c cannot generate input
    excludedMethods.add("java2.util2.TreeSet.readObject(java.io.ObjectInputStream)");
    excludedMethods.add("java2.util2.TreeSet.writeObject(java.io.ObjectOutputStream)");
    excludedMethods.add("java2.util2.ArrayList.readObject(java.io.ObjectInputStream)");
    excludedMethods.add("java2.util2.ArrayList.writeObject(java.io.ObjectOutputStream)");
    excludedMethods.add("java2.util2.BitSet.readObject(java.io.ObjectInputStream)");
    excludedMethods.add("java2.util2.Hashtable.readObject(java.io.ObjectInputStream)");
    excludedMethods.add("java2.util2.Hashtable.writeObject(java.io.ObjectOutputStream)");
    excludedMethods.add("java2.util2.LinkedList.readObject(java.io.ObjectInputStream)");
    excludedMethods.add("java2.util2.LinkedList.writeObject(java.io.ObjectOutputStream)");
    excludedMethods.add("java2.util2.TreeMap.readObject(java.io.ObjectInputStream)");
    excludedMethods.add(
        "java2.util2.TreeMap.readTreeSet(int, java.io.ObjectInputStream, java.lang.Object)");
    excludedMethods.add("java2.util2.TreeMap.writeObject(java.io.ObjectOutputStream)");
    excludedMethods.add("java2.util2.Vector.writeObject(java.io.ObjectOutputStream)");

    // these are not being covered by Randoop
    excludedMethods.add("java2.util2.ArrayList.removeRange(int, int)");
    excludedMethods.add("java2.util2.Arrays.med3(byte[], int, int, int)");
    excludedMethods.add("java2.util2.Arrays.med3(char[], int, int, int)");
    excludedMethods.add("java2.util2.Arrays.med3(double[], int, int, int)");
    excludedMethods.add("java2.util2.Arrays.med3(float[], int, int, int)");
    excludedMethods.add("java2.util2.Arrays.med3(int[], int, int, int)");
    excludedMethods.add("java2.util2.Arrays.med3(long[], int, int, int)");
    excludedMethods.add("java2.util2.Arrays.med3(short[], int, int, int)");
    excludedMethods.add(
        "java2.util2.Arrays.mergeSort(java.lang.Object[], java.lang.Object[], int, int, int, java2.util2.Comparator)");
    excludedMethods.add("java2.util2.Arrays.swap(java.lang.Object[], int, int)");
    excludedMethods.add("java2.util2.Arrays.vecswap(byte[], int, int, int)");
    excludedMethods.add("java2.util2.Arrays.vecswap(char[], int, int, int)");
    excludedMethods.add("java2.util2.Arrays.vecswap(double[], int, int, int)");
    excludedMethods.add("java2.util2.Arrays.vecswap(float[], int, int, int)");
    excludedMethods.add("java2.util2.Arrays.vecswap(int[], int, int, int)");
    excludedMethods.add("java2.util2.Arrays.vecswap(long[], int, int, int)");
    excludedMethods.add("java2.util2.Arrays.vecswap(short[], int, int, int)");
    excludedMethods.add("java2.util2.BitSet.getBits(int)");
    excludedMethods.add("java2.util2.Collections.get(java2.util2.ListIterator, int)");
    excludedMethods.add(
        "java2.util2.Collections.iteratorBinarySearch(java2.util2.List, java.lang.Object)");
    excludedMethods.add(
        "java2.util2.Collections.iteratorBinarySearch(java2.util2.List, java.lang.Object, java2.util2.Comparator)");
    excludedMethods.add("java2.util2.Collections.rotate2(java2.util2.List, int)");
    excludedMethods.add("java2.util2.Collections.swap(java.lang.Object[], int, int)");
    excludedMethods.add("java2.util2.Observable.clearChanged()");
    excludedMethods.add("java2.util2.Observable.countObservers()");
    excludedMethods.add("java2.util2.Observable.hasChanged()");
    excludedMethods.add("java2.util2.Observable.setChanged()");
    excludedMethods.add("java2.util2.TreeMap.colorOf(java2.util2.TreeMap.Entry)");
    excludedMethods.add("java2.util2.TreeMap.decrementSize()");
    excludedMethods.add("java2.util2.TreeMap.deleteEntry(java2.util2.TreeMap.Entry)");
    excludedMethods.add("java2.util2.TreeMap.fixAfterDeletion(java2.util2.TreeMap.Entry)");
    excludedMethods.add("java2.util2.TreeMap.getPrecedingEntry(java.lang.Object)");
    excludedMethods.add("java2.util2.TreeMap.leftOf(java2.util2.TreeMap.Entry)");
    excludedMethods.add("java2.util2.TreeMap.parentOf(java2.util2.TreeMap.Entry)");
    excludedMethods.add("java2.util2.TreeMap.rightOf(java2.util2.TreeMap.Entry)");
    excludedMethods.add("java2.util2.TreeMap.rotateLeft(java2.util2.TreeMap.Entry)");
    excludedMethods.add("java2.util2.TreeMap.rotateRight(java2.util2.TreeMap.Entry)");
    excludedMethods.add("java2.util2.TreeMap.setColor(java2.util2.TreeMap.Entry, boolean)");
    excludedMethods.add("java2.util2.TreeMap.subMap(java.lang.Object, java.lang.Object)");
    excludedMethods.add("java2.util2.TreeMap.valEquals(java.lang.Object, java.lang.Object)");
    excludedMethods.add(
        "java2.util2.TreeMap.valueSearchNonNull(java2.util2.TreeMap.Entry, java.lang.Object)");
    excludedMethods.add("java2.util2.TreeSet.subSet(java.lang.Object, java.lang.Object)");
    excludedMethods.add("java2.util2.Vector.removeRange(int, int)");
    excludedMethods.add("java2.util2.WeakHashMap.removeMapping(java.lang.Object)");
    generateAndTest(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests, excludedMethods);
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

    generateAndTest(
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
    generateAndTest(testEnvironment, options, expectedRegressionTests, expectedErrorTests);
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
    generateAndTest(testEnvironment, options, expectedRegressionTests, expectedErrorTests);
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
    generateAndTest(testEnvironment, options, expectedRegressionTests, expectedErrorTests);
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

    generateAndTest(
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
    generateAndTest(testEnvironment, options, expectedRegressionTests, expectedErrorTests);
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
    generateAndTest(testEnvironment, options, expectedRegressionTests, expectedErrorTests);
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
    generateAndTest(testEnvironment, options, expectedRegressionTests, expectedErrorTests);
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
    generateAndTest(testEnvironment, options, expectedRegressionTests, expectedErrorTests);
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
    generateAndTest(testEnvironment, options, expectedRegressionTests, expectedErrorTests);
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
  private void generateAndTest(
      TestEnvironment environment,
      RandoopOptions options,
      ExpectedTests expectedRegression,
      ExpectedTests expectedError,
      Set<String> excludedMethods) {

    RandoopRunStatus runStatus = RandoopRunStatus.generateAndCompile(environment, options);

    boolean prevLineIsBlank = false;
    for (String line : runStatus.processStatus.outputLines) {
      if ((line.isEmpty() && !prevLineIsBlank)
          || (!line.isEmpty() && !line.startsWith("Progress update:"))) {
        System.out.println(line);
      }
      prevLineIsBlank = line.isEmpty();
    }

    String packageName = options.getPackageName();

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

    checkCoverage(options.getClassnames(), excludedMethods, regressionRunDesc, errorRunDesc);
  }

  private void generateAndTest(
      TestEnvironment environment,
      RandoopOptions options,
      ExpectedTests expectedRegression,
      ExpectedTests expectedError) {
    generateAndTest(environment, options, expectedRegression, expectedError, new HashSet<String>());
  }

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

  private void getCoveredMethodsForClass(
      TestRunStatus testRunStatus, String classname, Set<String> methods) {
    if (testRunStatus != null) {
      Set<String> regressionMethods = testRunStatus.coverageMap.getMethods(classname);
      if (regressionMethods != null) {
        methods.addAll(regressionMethods);
      }
    }
  }

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

  private boolean isIgnoredMethod(String methodname) {
    Matcher matcher = IGNORE_PATTERN.matcher(methodname);
    return matcher.find();
  }
}
