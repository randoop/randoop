package randoop.main;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * A JUnit test class that runs the Randoop system tests. These are tests that were run from within
 * the original Makefile using shell commands. The test methods in this class assume that the
 * current working directory has subdirectories <tt>resources/systemTest</tt> where resources files
 * are located (standard Gradle organization), and <tt>working-directories/</tt> where working files
 * can be written. The Gradle file sets the working directory for the <tt>systemTest</tt> source set
 * to which this class belongs.
 *
 * <p>Each of the test methods
 *
 * <ul>
 *   <li>creates its own subdirectory,
 *   <li>runs Randoop and saves generated tests to the subdirectory, and
 *   <li>compiles the generated test files.
 * </ul>
 *
 * Most of the methods then run the tests and check that the expected number of failed tests matches
 * the number of error-revealing tests, or that the number of passed tests matches the number of
 * regression tests.
 *
 * <p>The Makefile also checked diffs of generated tests for some of the tests. These methods do not
 * do this check.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RandoopSystemTest {

  // Keep in synch with GenTests.NO_CLASSES_TO_TEST.
  // TODO: how to use the GenTests version?
  public static final String NO_CLASSES_TO_TEST = "There are no classes to test. Exiting.";

  private static SystemTestEnvironment systemTestEnvironment;

  /** Sets up the environment for test execution. */
  @BeforeClass
  public static void setupClass() {
    String classpath = System.getProperty("java.class.path");
    /* the current working directory for this test class */
    Path buildDir = Paths.get("").toAbsolutePath().normalize();
    systemTestEnvironment = SystemTestEnvironment.createSystemTestEnvironment(classpath, buildDir);
  }

  /**
   * Enumerated type to quantify expected test generation:
   *
   * <ul>
   *   <li>{@code SOME} - at least one test is generated,
   *   <li>{@code NONE} - no tests are generated, or
   *   <li>{@code DONT_CARE} - the number of tests does not need to be checked.
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
   *    Input files should be placed in src/systemtest/resources (or src/inputtest/resources if they
   *    relate to classes in the inputTest source set), and can be used in option using the path
   *    prefix resources/systemTest/.
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
   *     The generateAndTestWithCoverage() method handles the standard test behavior, checking the
   *     standard assumptions about regression and error tests (given the quantifiers), and dumping
   *     output when the results don't meet expectations.
   *
   *     By default, coverage is checked against all methods returned by Class.getDeclaredMethods()
   *     for an input class. Some tests need to specifically exclude methods that Randoop should not
   *     generate, or need to ignore methods.  These can be indicated by creating a
   *     CoverageChecker object and adding these method names using either the exclude() or ignore()
   *     methods, and then giving the CoverageChecker as the last argument to the alternate version
   *     of generateAndTestWithCoverage(). When excluded methods are given, these methods may not be
   *     covered, and, unless ignored, any method not excluded is expected to be covered.
   */

  /**
   * Test formerly known as randoop1. This test previously did a diff on TestClass0.java with goal
   * file.
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
    options.setOption("outputLimit", "200");
    options.setOption("npe-on-null-input", "EXPECTED");
    options.setFlag("debug_checks");
    options.setOption("observers", "resources/systemTest/randoop1_observers.txt");
    options.setOption("omit-field-list", "resources/systemTest/testclassomitfields.txt");

    CoverageChecker coverageChecker = new CoverageChecker(options);
    coverageChecker.exclude("java2.util2.Collections.get(java2.util2.ListIterator, int)");
    coverageChecker.exclude(
        "java2.util2.Collections.iteratorBinarySearch(java2.util2.List, java.lang.Object)");
    coverageChecker.exclude(
        "java2.util2.Collections.iteratorBinarySearch(java2.util2.List, java.lang.Object, java2.util2.Comparator)");
    coverageChecker.exclude("java2.util2.Collections.rotate2(java2.util2.List, int)");
    coverageChecker.exclude("java2.util2.Collections.swap(java.lang.Object[], int, int)");
    coverageChecker.exclude("java2.util2.Collections.swap(java2.util2.List, int, int)");
    coverageChecker.exclude(
        "java2.util2.Collections.synchronizedCollection(java2.util2.Collection, java.lang.Object)");
    coverageChecker.exclude(
        "java2.util2.Collections.synchronizedList(java2.util2.List, java.lang.Object)");
    coverageChecker.exclude(
        "java2.util2.Collections.synchronizedSet(java2.util2.Set, java.lang.Object)");
    coverageChecker.exclude("java2.util2.Collections.synchronizedSortedMap(java2.util2.SortedMap)");
    coverageChecker.exclude("java2.util2.Collections.unmodifiableSortedMap(java2.util2.SortedMap)");
    coverageChecker.exclude("java2.util2.TreeSet.readObject(java.io.ObjectInputStream)");
    coverageChecker.exclude("java2.util2.TreeSet.subSet(java.lang.Object, java.lang.Object)");
    coverageChecker.exclude("java2.util2.TreeSet.writeObject(java.io.ObjectOutputStream)");

    //TODO after changed types to ordered set in OperationModel, failing on Travis, but not locally
    coverageChecker.ignore("java2.util2.Collections.synchronizedSet(java2.util2.Set)");
    coverageChecker.ignore("java2.util2.Collections.synchronizedSortedSet(java2.util2.SortedSet)");
    coverageChecker.ignore("java2.util2.TreeSet.first()");
    coverageChecker.ignore("java2.util2.TreeSet.last()");
    coverageChecker.ignore("java2.util2.TreeSet.tailSet(java.lang.Object)");
    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;

    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests, coverageChecker);
  }

  /** Test formerly known as randoop2. Previously did a diff on generated test. */
  @Test
  public void runNaiveCollectionsTest() {
    String directoryName = "naive-collections-test";
    TestEnvironment testEnvironment = systemTestEnvironment.createTestEnvironment(directoryName);
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("foo.bar");
    options.setRegressionBasename("NaiveRegression");
    options.setErrorBasename("NaiveError");
    options.setOption("outputLimit", "200");
    options.addTestClass("java2.util2.TreeSet");
    options.addTestClass("java2.util2.ArrayList");
    options.addTestClass("java2.util2.LinkedList");
    options.addTestClass("java2.util2.Collections");
    options.setOption("omit-field-list", "resources/systemTest/naiveomitfields.txt");
    options.setOption("operation-history-log", "-"); //log to stdout

    CoverageChecker coverageChecker = new CoverageChecker(options);
    coverageChecker.exclude("java2.util2.ArrayList.add(int, java.lang.Object)");
    coverageChecker.exclude("java2.util2.ArrayList.get(int)");
    coverageChecker.exclude("java2.util2.ArrayList.lastIndexOf(java.lang.Object)");
    coverageChecker.exclude("java2.util2.ArrayList.readObject(java.io.ObjectInputStream)");
    coverageChecker.exclude("java2.util2.ArrayList.remove(int)");
    coverageChecker.exclude("java2.util2.ArrayList.removeRange(int, int)");
    coverageChecker.exclude("java2.util2.ArrayList.set(int, java.lang.Object)");
    coverageChecker.exclude("java2.util2.ArrayList.writeObject(java.io.ObjectOutputStream)");
    coverageChecker.exclude("java2.util2.Collections.eq(java.lang.Object, java.lang.Object)");
    coverageChecker.exclude("java2.util2.Collections.get(java2.util2.ListIterator, int)");
    coverageChecker.exclude(
        "java2.util2.Collections.indexOfSubList(java2.util2.List, java2.util2.List)");
    coverageChecker.exclude(
        "java2.util2.Collections.iteratorBinarySearch(java2.util2.List, java.lang.Object)");
    coverageChecker.exclude(
        "java2.util2.Collections.iteratorBinarySearch(java2.util2.List, java.lang.Object, java2.util2.Comparator)");
    coverageChecker.exclude("java2.util2.Collections.rotate2(java2.util2.List, int)");
    coverageChecker.exclude("java2.util2.Collections.shuffle(java2.util2.List)");
    coverageChecker.exclude(
        "java2.util2.Collections.shuffle(java2.util2.List, java2.util2.Random)");
    coverageChecker.exclude("java2.util2.Collections.swap(java.lang.Object[], int, int)");
    coverageChecker.exclude("java2.util2.Collections.swap(java2.util2.List, int, int)");
    coverageChecker.exclude(
        "java2.util2.Collections.synchronizedCollection(java2.util2.Collection, java.lang.Object)");
    coverageChecker.exclude(
        "java2.util2.Collections.synchronizedList(java2.util2.List, java.lang.Object)");
    coverageChecker.exclude(
        "java2.util2.Collections.synchronizedSet(java2.util2.Set, java.lang.Object)");
    coverageChecker.exclude("java2.util2.Collections.synchronizedSortedMap(java2.util2.SortedMap)");
    coverageChecker.exclude("java2.util2.Collections.unmodifiableList(java2.util2.List)");
    coverageChecker.exclude("java2.util2.Collections.unmodifiableSortedMap(java2.util2.SortedMap)");
    coverageChecker.exclude("java2.util2.Collections.unmodifiableSortedSet(java2.util2.SortedSet)");
    coverageChecker.exclude("java2.util2.LinkedList.add(int, java.lang.Object)");
    coverageChecker.exclude("java2.util2.LinkedList.addFirst(java.lang.Object)");
    coverageChecker.exclude("java2.util2.LinkedList.addLast(java.lang.Object)");
    coverageChecker.exclude("java2.util2.LinkedList.get(int)");
    coverageChecker.exclude("java2.util2.LinkedList.readObject(java.io.ObjectInputStream)");
    coverageChecker.exclude("java2.util2.LinkedList.remove(int)");
    coverageChecker.exclude("java2.util2.LinkedList.writeObject(java.io.ObjectOutputStream)");
    coverageChecker.exclude("java2.util2.TreeSet.first()");
    coverageChecker.exclude("java2.util2.TreeSet.headSet(java.lang.Object)");
    coverageChecker.exclude("java2.util2.TreeSet.last()");
    coverageChecker.exclude("java2.util2.TreeSet.readObject(java.io.ObjectInputStream)");
    coverageChecker.exclude("java2.util2.TreeSet.subSet(java.lang.Object, java.lang.Object)");
    coverageChecker.exclude("java2.util2.TreeSet.tailSet(java.lang.Object)");
    coverageChecker.exclude("java2.util2.TreeSet.writeObject(java.io.ObjectOutputStream)");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.DONT_CARE;

    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests, coverageChecker);
  }

  /**
   * Test formerly known as randoop3. Previously this test did nothing beyond generating the tests.
   */
  @Test
  public void runJDKTest() {

    TestEnvironment testEnvironment = systemTestEnvironment.createTestEnvironment("jdk-test");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("jdktests");
    options.setRegressionBasename("JDK_Tests_regression");
    options.setErrorBasename("JDK_Tests_error");

    options.setOption("generatedLimit", "1000");
    options.setOption("null-ratio", "0.3");
    options.setOption("alias-ratio", "0.3");
    options.setFlag("small-tests");
    options.setFlag("clear=100");
    options.addClassList("resources/systemTest/jdk_classlist.txt");

    // omit methods that use Random
    options.setOption(
        "omitmethods", "java2\\.util2\\.Collections\\.shuffle\\(java2\\.util2\\.List\\)");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.DONT_CARE;

    CoverageChecker coverageChecker = new CoverageChecker(options);
    coverageChecker.exclude("java2.util2.ArrayList.addAll(int, java2.util2.Collection)");
    coverageChecker.exclude("java2.util2.ArrayList.clear()");
    coverageChecker.exclude("java2.util2.ArrayList.clone()");
    coverageChecker.exclude("java2.util2.ArrayList.get(int)");
    coverageChecker.exclude("java2.util2.ArrayList.readObject(java.io.ObjectInputStream)");
    coverageChecker.exclude("java2.util2.ArrayList.remove(int)");
    coverageChecker.exclude("java2.util2.ArrayList.removeRange(int, int)");
    coverageChecker.exclude("java2.util2.ArrayList.set(int, java.lang.Object)");
    coverageChecker.exclude("java2.util2.ArrayList.trimToSize()");
    coverageChecker.exclude("java2.util2.ArrayList.writeObject(java.io.ObjectOutputStream)");
    coverageChecker.exclude("java2.util2.Arrays.med3(byte[], int, int, int)");
    coverageChecker.exclude("java2.util2.Arrays.med3(char[], int, int, int)");
    coverageChecker.exclude("java2.util2.Arrays.med3(double[], int, int, int)");
    coverageChecker.exclude("java2.util2.Arrays.med3(float[], int, int, int)");
    coverageChecker.exclude("java2.util2.Arrays.med3(int[], int, int, int)");
    coverageChecker.exclude("java2.util2.Arrays.med3(long[], int, int, int)");
    coverageChecker.exclude("java2.util2.Arrays.med3(short[], int, int, int)");
    coverageChecker.exclude(
        "java2.util2.Arrays.mergeSort(java.lang.Object[], java.lang.Object[], int, int, int, java2.util2.Comparator)");
    coverageChecker.exclude("java2.util2.Arrays.sort(byte[], int, int)");
    coverageChecker.exclude("java2.util2.Arrays.sort(double[], int, int)");
    coverageChecker.exclude("java2.util2.Arrays.sort(java.lang.Object[], int, int)");
    coverageChecker.exclude("java2.util2.Arrays.sort(long[], int, int)");
    coverageChecker.exclude("java2.util2.Arrays.swap(java.lang.Object[], int, int)");
    coverageChecker.exclude("java2.util2.Arrays.vecswap(byte[], int, int, int)");
    coverageChecker.exclude("java2.util2.Arrays.vecswap(char[], int, int, int)");
    coverageChecker.exclude("java2.util2.Arrays.vecswap(double[], int, int, int)");
    coverageChecker.exclude("java2.util2.Arrays.vecswap(float[], int, int, int)");
    coverageChecker.exclude("java2.util2.Arrays.vecswap(int[], int, int, int)");
    coverageChecker.exclude("java2.util2.Arrays.vecswap(long[], int, int, int)");
    coverageChecker.exclude("java2.util2.Arrays.vecswap(short[], int, int, int)");
    coverageChecker.exclude("java2.util2.BitSet.and(java2.util2.BitSet)");
    coverageChecker.exclude("java2.util2.BitSet.andNot(java2.util2.BitSet)");
    coverageChecker.exclude("java2.util2.BitSet.bit(int)");
    coverageChecker.exclude("java2.util2.BitSet.bitCount(long)");
    coverageChecker.exclude("java2.util2.BitSet.bitLen(int)");
    coverageChecker.exclude("java2.util2.BitSet.bitsLeftOf(int)");
    coverageChecker.exclude("java2.util2.BitSet.bitsRightOf(int)");
    coverageChecker.exclude("java2.util2.BitSet.cardinality()");
    coverageChecker.exclude("java2.util2.BitSet.clear()");
    coverageChecker.exclude("java2.util2.BitSet.clear(int)");
    coverageChecker.exclude("java2.util2.BitSet.equals(java.lang.Object)");
    coverageChecker.exclude("java2.util2.BitSet.flip(int)");
    coverageChecker.exclude("java2.util2.BitSet.get(int)");
    coverageChecker.exclude("java2.util2.BitSet.getBits(int)");
    coverageChecker.exclude("java2.util2.BitSet.hashCode()");
    coverageChecker.exclude("java2.util2.BitSet.intersects(java2.util2.BitSet)");
    coverageChecker.exclude("java2.util2.BitSet.nextSetBit(int)");
    coverageChecker.exclude("java2.util2.BitSet.or(java2.util2.BitSet)");
    coverageChecker.exclude("java2.util2.BitSet.readObject(java.io.ObjectInputStream)");
    coverageChecker.exclude("java2.util2.BitSet.set(int)");
    coverageChecker.exclude("java2.util2.BitSet.set(int, boolean)");
    coverageChecker.exclude("java2.util2.BitSet.size()");
    coverageChecker.exclude("java2.util2.BitSet.toString()");
    coverageChecker.exclude("java2.util2.BitSet.trailingZeroCnt(long)");
    coverageChecker.exclude("java2.util2.Collections.get(java2.util2.ListIterator, int)");
    coverageChecker.exclude(
        "java2.util2.Collections.indexedBinarySearch(java2.util2.List, java.lang.Object, java2.util2.Comparator)");
    coverageChecker.exclude(
        "java2.util2.Collections.iteratorBinarySearch(java2.util2.List, java.lang.Object)");
    coverageChecker.exclude(
        "java2.util2.Collections.iteratorBinarySearch(java2.util2.List, java.lang.Object, java2.util2.Comparator)");
    coverageChecker.exclude("java2.util2.Collections.nCopies(int, java.lang.Object)");
    coverageChecker.exclude("java2.util2.Collections.reverse(java2.util2.List)");
    coverageChecker.exclude("java2.util2.Collections.rotate2(java2.util2.List, int)");
    coverageChecker.exclude("java2.util2.Collections.shuffle(java2.util2.List)");
    coverageChecker.exclude("java2.util2.Collections.swap(java.lang.Object[], int, int)");
    coverageChecker.exclude("java2.util2.Collections.swap(java2.util2.List, int, int)");
    coverageChecker.exclude("java2.util2.Collections.synchronizedSortedMap(java2.util2.SortedMap)");
    coverageChecker.exclude("java2.util2.Collections.unmodifiableSortedMap(java2.util2.SortedMap)");
    coverageChecker.exclude("java2.util2.Collections.unmodifiableSortedSet(java2.util2.SortedSet)");
    coverageChecker.exclude("java2.util2.Hashtable.containsValue(java.lang.Object)");
    coverageChecker.exclude("java2.util2.Hashtable.get(java.lang.Object)");
    coverageChecker.exclude("java2.util2.Hashtable.keySet()");
    coverageChecker.exclude("java2.util2.Hashtable.keys()");
    coverageChecker.exclude("java2.util2.Hashtable.readObject(java.io.ObjectInputStream)");
    coverageChecker.exclude("java2.util2.Hashtable.rehash()");
    coverageChecker.exclude("java2.util2.Hashtable.writeObject(java.io.ObjectOutputStream)");
    coverageChecker.exclude("java2.util2.LinkedHashMap.get(java.lang.Object)");
    coverageChecker.exclude("java2.util2.LinkedHashMap.newValueIterator()");
    coverageChecker.exclude("java2.util2.LinkedList.add(int, java.lang.Object)");
    coverageChecker.exclude("java2.util2.LinkedList.addLast(java.lang.Object)");
    coverageChecker.exclude("java2.util2.LinkedList.get(int)");
    coverageChecker.exclude("java2.util2.LinkedList.getFirst()");
    coverageChecker.exclude("java2.util2.LinkedList.lastIndexOf(java.lang.Object)");
    coverageChecker.exclude("java2.util2.LinkedList.readObject(java.io.ObjectInputStream)");
    coverageChecker.exclude("java2.util2.LinkedList.remove(int)");
    coverageChecker.exclude("java2.util2.LinkedList.toArray()");
    coverageChecker.exclude("java2.util2.LinkedList.writeObject(java.io.ObjectOutputStream)");
    coverageChecker.exclude("java2.util2.Observable.addObserver(java2.util2.Observer)");
    coverageChecker.exclude("java2.util2.Observable.clearChanged()");
    coverageChecker.exclude("java2.util2.Observable.countObservers()");
    coverageChecker.exclude("java2.util2.Observable.deleteObserver(java2.util2.Observer)");
    coverageChecker.exclude("java2.util2.Observable.deleteObservers()");
    coverageChecker.exclude("java2.util2.Observable.notifyObservers()");
    coverageChecker.exclude("java2.util2.Observable.notifyObservers(java.lang.Object)");
    coverageChecker.exclude("java2.util2.Observable.setChanged()");
    coverageChecker.exclude("java2.util2.Stack.empty()");
    coverageChecker.exclude("java2.util2.Stack.pop()");
    coverageChecker.exclude("java2.util2.Stack.push(java.lang.Object)");
    coverageChecker.exclude("java2.util2.Stack.search(java.lang.Object)");
    coverageChecker.exclude("java2.util2.StringTokenizer.nextElement()");
    coverageChecker.exclude(
        "java2.util2.TreeMap.addAllForTreeSet(java2.util2.SortedSet, java.lang.Object)");
    coverageChecker.exclude("java2.util2.TreeMap.colorOf(java2.util2.TreeMap.Entry)");
    coverageChecker.exclude("java2.util2.TreeMap.containsKey(java.lang.Object)");
    coverageChecker.exclude("java2.util2.TreeMap.firstKey()");
    coverageChecker.exclude("java2.util2.TreeMap.fixAfterDeletion(java2.util2.TreeMap.Entry)");
    coverageChecker.exclude("java2.util2.TreeMap.fixAfterInsertion(java2.util2.TreeMap.Entry)");
    coverageChecker.exclude("java2.util2.TreeMap.getCeilEntry(java.lang.Object)");
    coverageChecker.exclude("java2.util2.TreeMap.getPrecedingEntry(java.lang.Object)");
    coverageChecker.exclude("java2.util2.TreeMap.leftOf(java2.util2.TreeMap.Entry)");
    coverageChecker.exclude("java2.util2.TreeMap.parentOf(java2.util2.TreeMap.Entry)");
    coverageChecker.exclude("java2.util2.TreeMap.readObject(java.io.ObjectInputStream)");
    coverageChecker.exclude(
        "java2.util2.TreeMap.readTreeSet(int, java.io.ObjectInputStream, java.lang.Object)");
    coverageChecker.exclude("java2.util2.TreeMap.rightOf(java2.util2.TreeMap.Entry)");
    coverageChecker.exclude("java2.util2.TreeMap.rotateLeft(java2.util2.TreeMap.Entry)");
    coverageChecker.exclude("java2.util2.TreeMap.rotateRight(java2.util2.TreeMap.Entry)");
    coverageChecker.exclude("java2.util2.TreeMap.setColor(java2.util2.TreeMap.Entry, boolean)");
    coverageChecker.exclude("java2.util2.TreeMap.subMap(java.lang.Object, java.lang.Object)");
    coverageChecker.exclude("java2.util2.TreeMap.valEquals(java.lang.Object, java.lang.Object)");
    coverageChecker.exclude(
        "java2.util2.TreeMap.valueSearchNonNull(java2.util2.TreeMap.Entry, java.lang.Object)");
    coverageChecker.exclude("java2.util2.TreeMap.valueSearchNull(java2.util2.TreeMap.Entry)");
    coverageChecker.exclude("java2.util2.TreeMap.writeObject(java.io.ObjectOutputStream)");
    coverageChecker.exclude("java2.util2.TreeSet.clear()");
    coverageChecker.exclude("java2.util2.TreeSet.comparator()");
    coverageChecker.exclude("java2.util2.TreeSet.contains(java.lang.Object)");
    coverageChecker.exclude("java2.util2.TreeSet.first()");
    coverageChecker.exclude("java2.util2.TreeSet.headSet(java.lang.Object)");
    coverageChecker.exclude("java2.util2.TreeSet.readObject(java.io.ObjectInputStream)");
    coverageChecker.exclude("java2.util2.TreeSet.remove(java.lang.Object)");
    coverageChecker.exclude("java2.util2.TreeSet.subSet(java.lang.Object, java.lang.Object)");
    coverageChecker.exclude("java2.util2.TreeSet.writeObject(java.io.ObjectOutputStream)");
    coverageChecker.exclude("java2.util2.Vector.add(int, java.lang.Object)");
    coverageChecker.exclude("java2.util2.Vector.hashCode()");
    coverageChecker.exclude("java2.util2.Vector.lastIndexOf(java.lang.Object)");
    coverageChecker.exclude("java2.util2.Vector.removeRange(int, int)");
    coverageChecker.exclude("java2.util2.Vector.setSize(int)");
    coverageChecker.exclude("java2.util2.Vector.writeObject(java.io.ObjectOutputStream)");
    coverageChecker.exclude("java2.util2.WeakHashMap.containsNullValue()");
    coverageChecker.exclude("java2.util2.WeakHashMap.containsValue(java.lang.Object)");
    coverageChecker.exclude("java2.util2.WeakHashMap.remove(java.lang.Object)");
    coverageChecker.exclude("java2.util2.WeakHashMap.removeMapping(java.lang.Object)");

    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests, coverageChecker);
  }

  /**
   * Test formerly known as randoop-contracts. Takes a long time. Evidence from running <tt>time
   * make randoop-contracts</tt> with previous Makefile. Reports: <tt>
   *
   * <pre>
   *  real	0m15.976s
   *  user	0m17.902s
   *  sys	0m9.814s
   * </pre>
   *
   * </tt>
   */
  @Test
  public void runContractsTest() {

    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("contracts-test"); // temp directory
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setErrorBasename("BuggyTest");

    options.setFlag("no-regression-tests");
    options.setOption("generatedLimit", "1000");
    // Don't minimize the tests because it would take too long to finish.
    options.setOption("minimize_error_test", "false");
    options.addClassList("resources/systemTest/buggyclasses.txt");

    ExpectedTests expectedRegressionTests = ExpectedTests.NONE;
    ExpectedTests expectedErrorTests = ExpectedTests.SOME;

    CoverageChecker coverageChecker = new CoverageChecker(options);
    coverageChecker.ignore("examples.Buggy.hashCode()");
    coverageChecker.ignore("examples.Buggy.toString()");
    /* don't care about hashCode for compareTo input classes */
    coverageChecker.ignore("examples.Buggy.BuggyCompareToAntiSymmetric.hashCode()");
    coverageChecker.ignore("examples.Buggy.BuggyCompareToEquals.hashCode()");
    coverageChecker.ignore("examples.Buggy.BuggyCompareToTransitive.hashCode()");
    coverageChecker.ignore("examples.Buggy.BuggyCompareToReflexive.hashCode()");
    coverageChecker.ignore("examples.Buggy.BuggyCompareToSubs.hashCode()");
    coverageChecker.ignore("examples.Buggy.BuggyEqualsTransitive.hashCode()");

    coverageChecker.ignore("examples.Buggy.StackOverflowError()");

    /* these should be covered, but are in failing assertions and wont show up in JaCoCo results */
    coverageChecker.exclude(
        "examples.Buggy.BuggyCompareToAntiSymmetric.compareTo(java.lang.Object)");
    coverageChecker.exclude("examples.Buggy.BuggyCompareToEquals.compareTo(java.lang.Object)");
    coverageChecker.exclude("examples.Buggy.BuggyCompareToEquals.equals(java.lang.Object)");
    coverageChecker.exclude("examples.Buggy.BuggyCompareToReflexive.compareTo(java.lang.Object)");
    coverageChecker.exclude("examples.Buggy.BuggyCompareToReflexive.equals(java.lang.Object)");
    coverageChecker.exclude("examples.Buggy.BuggyCompareToSubs.compareTo(java.lang.Object)");
    coverageChecker.exclude("examples.Buggy.BuggyCompareToTransitive.compareTo(java.lang.Object)");

    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests, coverageChecker);
  }

  /** Test formerly known as randoop-checkrep. */
  @Test
  public void runCheckRepTest() {

    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("checkrep-test"); // temp directory
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setErrorBasename("CheckRepTest");

    options.setFlag("no-regression-tests");
    options.setOption("attemptedLimit", "1000");
    options.setOption("generatedLimit", "200");
    options.addTestClass("examples.CheckRep1");
    options.addTestClass("examples.CheckRep2");

    ExpectedTests expectedRegressionTests = ExpectedTests.NONE;
    ExpectedTests expectedErrorTests = ExpectedTests.SOME;
    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  /**
   * Test formerly known as randoop-literals. Previously did a diff on generated test file and goal.
   */
  @Test
  public void runLiteralsTest() {

    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("literals-test"); // temp directory
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("");
    options.setRegressionBasename("LiteralsReg");
    options.setErrorBasename("LiteralsErr");

    options.setOption("generatedLimit", "1000");
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
   * Test formerly known as randoop-long-string. Previously performed a diff on generated test and
   * goal file.
   */
  @Test
  public void runLongStringTest() {
    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("longstring-test"); // temp directory
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("");
    options.setRegressionBasename("LongString");
    options.setErrorBasename("");

    options.setOption("attemptedLimit", "1000");
    options.setOption("generatedLimit", "100");
    options.addTestClass("randoop.test.LongString");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;

    CoverageChecker coverageChecker = new CoverageChecker(options);
    //XXX after adding compile check this method did not appear in JDK7 runs
    coverageChecker.ignore("randoop.test.LongString.tooLongString()");
    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests, coverageChecker);
  }

  /** Test formerly known as randoop-visibility. */
  @Test
  public void runVisibilityTest() {
    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("visibility-test"); // temp directory
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("");
    options.setRegressionBasename("VisibilityTest");
    options.setErrorBasename("");

    options.setOption("attemptedLimit", "1000");
    options.setOption("generatedLimit", "200");
    options.addTestClass("examples.Visibility");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;

    CoverageChecker coverageChecker = new CoverageChecker(options);
    coverageChecker.exclude("examples.Visibility.getNonVisible()");
    coverageChecker.exclude("examples.Visibility.takesNonVisible(examples.NonVisible)");

    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests, coverageChecker);
  }

  /**
   * Test formerly known as randoop-no-output. Runs with <tt>--progressdisplay=false</tt> and so
   * should have no output.
   */
  @Test
  public void runNoOutputTest() {
    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("no-output-test"); // temp directory
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("");
    options.setRegressionBasename("NoOutputTest");
    options.setErrorBasename("");

    options.setOption("generatedLimit", "100");
    options.addTestClass("java.util.LinkedList");
    options.setOption("progressdisplay", "false");

    RandoopRunStatus randoopRunDesc =
        RandoopRunStatus.generateAndCompile(testEnvironment, options, false);

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
    options.setRegressionBasename("InnerClassRegression");
    options.setErrorBasename("InnerClassError");
    options.addTestClass("randoop.test.ClassWithInnerClass");
    options.addTestClass("randoop.test.ClassWithInnerClass$A");
    options.setOption("generatedLimit", "20");
    options.setFlag("silently-ignore-bad-class-names");
    options.setOption("unchecked-exception", "ERROR");
    options.setOption("npe-on-null-input", "ERROR");
    options.setOption("npe-on-non-null-input", "ERROR");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
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
    options.setOption("generatedLimit", "30000");
    options.setOption("outputLimit", "100");
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
    options.setOption("generatedLimit", "30000");
    options.setOption("outputLimit", "100");
    options.setFlag("forbid-null");
    options.setOption("null-ratio", "0");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;
    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  /** Simply runs Randoop on a class in the default package to ensure nothing breaks. */
  @Test
  public void runDefaultPackageTest() {
    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("default-package");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("");
    options.setRegressionBasename("DefaultPackageReg");
    options.setErrorBasename("DefaultPackageErr");
    options.addTestClass("ClassInDefaultPackage");
    options.setOption("generatedLimit", "20");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;
    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  /** Tests that Randoop deals properly with exceptions */
  @Test
  public void runExceptionTest() {
    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("exception-tests");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("misc");
    options.setRegressionBasename("RegressionTest");
    options.setErrorBasename("ErrorTest");
    options.addTestClass("misc.ThrowsAnonymousException");
    options.setOption("outputLimit", "2");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;
    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  /**
   * Test collection generation.
   *
   * <p>Uses collectiongen package in testInput. Expect that generated test will cover all methods
   * of collectiongen.InputClass as long as method input type is a test class. This will include the
   * enum Day and the class AnInputClass, but exclude the enum Season and the class ANonInputClass.
   *
   * <p>Note: if this test is failing coverage for a generic method (the message says a parameter is
   * Object), make sure that there are no overloads of the generic method with more specific
   * arguments in InputClass. If there are, method resolution rules may lead to a call that Randoop
   * thinks is to the generic method turning into a call to a more specific method, leading to
   * coverage issues.
   */
  @Test
  public void runCollectionGenerationTest() {
    TestEnvironment testEnvironment = systemTestEnvironment.createTestEnvironment("coll-gen-tests");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("gen");
    options.setRegressionBasename("GenRegressionTest");
    options.setErrorBasename("GenErrorTest");
    options.addTestClass("collectiongen.InputClass");
    options.addTestClass("collectiongen.Day");
    options.addTestClass("collectiongen.AnInputClass");
    options.setFlag("small-tests");
    options.setOption("generatedLimit", "500");
    options.setOption("omitmethods", "hashCode\\(\\)");

    CoverageChecker coverageChecker = new CoverageChecker(options);
    coverageChecker.exclude("collectiongen.Day.valueOf(java.lang.String)");
    coverageChecker.ignore("collectiongen.AnInputClass.hashCode()");
    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;
    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests, coverageChecker);
  }

  /**
   * Test for Enum value assertion generation.
   *
   * <p>Uses examples.Option class in testInput. Only actually tests whether methods are called.
   *
   * <p>Need to scrape generated source file for Enum constant values.
   */
  @Test
  public void runEnumAssertionTest() {
    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("enum-assertions");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.setPackageName("check");
    options.setRegressionBasename("EnumCheckRegression");
    options.setErrorBasename("EnumCheckError");
    options.addTestClass("examples.Option");
    options.setFlag("small-tests");
    options.setOption("generatedLimit", "20");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;
    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  /** Test what happens when have empty input class names. */
  @Test
  public void runEmptyInputNamesTest() {
    TestEnvironment testEnvironment = systemTestEnvironment.createTestEnvironment("empty-names");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.addClassList("resources/systemTest/emptyclasslist.txt");
    options.setOption("attemptedLimit", "20");

    RandoopRunStatus result = generateAndCompile(testEnvironment, options, true);

    Iterator<String> it = result.processStatus.outputLines.iterator();
    String line = "";
    while (!line.contains(NO_CLASSES_TO_TEST) && it.hasNext()) {
      line = it.next();
    }
    assertTrue("should fail to find class names in file", line.contains(NO_CLASSES_TO_TEST));
  }

  /**
   * Test for flaky NaN: the value Double.NaN and the computed NaN value are distinct. This means
   * that the same computation over each can have different outcomes, but both are printed as
   * Double.NaN so when run may have a different result from test during generation.
   */
  @Test
  public void runFlakyNaNTest() {
    TestEnvironment testEnvironment = systemTestEnvironment.createTestEnvironment("flaky-nan");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.addTestClass("examples.NaNBadness");
    options.setRegressionBasename("NaNRegression");
    options.setErrorBasename("NaNError");
    options.setOption("generatedLimit", "200");

    ExpectedTests expectedRegressionTests = ExpectedTests.SOME;
    ExpectedTests expectedErrorTests = ExpectedTests.NONE;
    generateAndTestWithCoverage(
        testEnvironment, options, expectedRegressionTests, expectedErrorTests);
  }

  /** Test for inserted test fixtures. */
  @Test
  public void runFixtureTest() {
    TestEnvironment testEnvironment = systemTestEnvironment.createTestEnvironment("fixtures");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.addTestClass("examples.Dummy");
    options.setRegressionBasename("FixtureRegression");
    options.setOption("junit-before-all", "resources/systemTest/beforeallcode.txt");
    options.setOption("junit-after-all", "resources/systemTest/afterallcode.txt");
    options.setOption("junit-before-each", "resources/systemTest/beforeeachcode.txt");
    options.setOption("junit-after-each", "resources/systemTest/aftereachcode.txt");
    options.setOption("generatedLimit", "200");
    options.setFlag("no-error-revealing-tests");

    RandoopRunStatus runStatus = generateAndCompile(testEnvironment, options, false);
    String packageName = options.getPackageName();
    TestRunStatus regressionRunDesc =
        runRegressionTests(testEnvironment, options, ExpectedTests.SOME, runStatus, packageName);

    int beforeAllCount = 0;
    int beforeEachCount = 0;
    int afterAllCount = 0;
    int afterEachCount = 0;
    for (String line : regressionRunDesc.processStatus.outputLines) {
      if (line.contains("Before All")) {
        beforeAllCount++;
      }
      if (line.contains("Before Each")) {
        beforeEachCount++;
      }
      if (line.contains("After All")) {
        afterAllCount++;
      }
      if (line.contains("After Each")) {
        afterEachCount++;
      }
    }

    assertThat("should only have one BeforeAll", beforeAllCount, is(equalTo(1)));
    assertThat("should have one AfterAll", afterAllCount, is(equalTo(1)));
    assertThat(
        "should have one BeforeEach for each test",
        beforeEachCount,
        is(equalTo(regressionRunDesc.testsRun)));
    assertThat(
        "should have one AfterEach for each test",
        afterEachCount,
        is(equalTo(regressionRunDesc.testsRun)));
  }

  /** Runs the FixtureTest except with a driver instead of a JUnit test suite. */
  @Test
  public void runFixtureDriverTest() {
    TestEnvironment testEnvironment = systemTestEnvironment.createTestEnvironment("fixture-driver");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.addTestClass("examples.Dummy");
    options.setRegressionBasename("FixtureRegression");
    options.setOption("junit-before-all", "resources/systemTest/beforeallcode.txt");
    options.setOption("junit-after-all", "resources/systemTest/afterallcode.txt");
    options.setOption("junit-before-each", "resources/systemTest/beforeeachcode.txt");
    options.setOption("junit-after-each", "resources/systemTest/aftereachcode.txt");
    options.setOption("generatedLimit", "200");
    options.setFlag("no-error-revealing-tests");
    options.unsetFlag("junit-reflection-allowed");

    RandoopRunStatus runStatus = generateAndCompile(testEnvironment, options, false);
    String driverName = options.getRegressionBasename() + "Driver";
    List<String> command = new ArrayList<>();
    command.add("java");
    command.add("-ea");
    command.add("-classpath");
    command.add(testEnvironment.testClassPath);
    command.add(driverName);
    ProcessStatus status = ProcessStatus.runCommand(command);

    int beforeAllCount = 0;
    int beforeEachCount = 0;
    int afterAllCount = 0;
    int afterEachCount = 0;
    for (String line : status.outputLines) {
      if (line.contains("Before All")) {
        beforeAllCount++;
      }
      if (line.contains("Before Each")) {
        beforeEachCount++;
      }
      if (line.contains("After All")) {
        afterAllCount++;
      }
      if (line.contains("After Each")) {
        afterEachCount++;
      }
    }

    assertThat("should only have one BeforeAll", beforeAllCount, is(equalTo(1)));
    assertThat("should have one AfterAll", afterAllCount, is(equalTo(1)));
    assertThat(
        "should have one BeforeEach for each test",
        beforeEachCount,
        is(equalTo(runStatus.regressionTestCount)));
    assertThat(
        "should have one AfterEach for each test",
        afterEachCount,
        is(equalTo(runStatus.regressionTestCount)));
  }

  /**
   * recreate problem with tests over Google Guava where value from private enum returned by public
   * method and value used in {@code randoop.test.ObjectCheck} surfaces in test code, creating
   * uncompilable code.
   */
  @Test
  public void runPrivateEnumTest() {
    TestEnvironment testEnvironment = systemTestEnvironment.createTestEnvironment("private-enum");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.addTestClass("generror.Ints");
    options.setErrorBasename("LexError");
    options.setRegressionBasename("LexRegression");
    options.setOption("attemptedLimit", "10000");
    options.setOption("generatedLimit", "3000");

    generateAndTestWithCoverage(
        testEnvironment, options, ExpectedTests.SOME, ExpectedTests.DONT_CARE);
  }

  /** This test uses input classes that result in uncompilable tests. */
  @Test
  public void runInstantiationErrorTest() {
    TestEnvironment testEnvironment = systemTestEnvironment.createTestEnvironment("compile-error");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.addTestClass("compileerr.WildcardCollection");
    options.setErrorBasename("CompError");
    options.setRegressionBasename("CompRegression");
    options.setOption("attemptedLimit", "3000");

    CoverageChecker coverageChecker = new CoverageChecker(options);
    coverageChecker.ignore("compileerr.WildcardCollection.getAStringList()");
    coverageChecker.ignore("compileerr.WildcardCollection.getAnIntegerList()");
    coverageChecker.ignore("compileerr.WildcardCollection.munge(java.util.List, java.util.List)");
    generateAndTestWithCoverage(
        testEnvironment, options, ExpectedTests.SOME, ExpectedTests.NONE, coverageChecker);
  }

  @Test
  public void runCoveredClassFilterTest() {
    TestEnvironment testEnvironment = systemTestEnvironment.createTestEnvironment("covered-class");
    testEnvironment.addJavaAgent(systemTestEnvironment.coveredClassAgentPath);
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.addClassList("resources/systemTest/instrument/testcase/allclasses.txt");
    options.setOption(
        "require-covered-classes", "resources/systemTest/instrument/testcase/coveredclasses.txt");
    options.setOption("generatedLimit", "500");
    options.setOption("outputLimit", "250");
    options.setErrorBasename("ExError");
    options.setRegressionBasename("ExRegression");

    CoverageChecker coverageChecker = new CoverageChecker(options);
    //TODO figure out why this method not covered
    coverageChecker.ignore("instrument.testcase.A.toString()");
    coverageChecker.exclude("instrument.testcase.C.getValue()");
    coverageChecker.exclude("instrument.testcase.C.isZero()");
    coverageChecker.exclude("instrument.testcase.C.jumpValue()");
    generateAndTestWithCoverage(
        testEnvironment, options, ExpectedTests.SOME, ExpectedTests.NONE, coverageChecker);
  }

  /**
   * Expecting something like
   *
   * <pre>
   * generation.Dim6Matrix dim6Matrix = new generation.Dim6Matrix();
   * generation.Dim5Matrix copy = dim6Matrix.copy();
   * double d = copy.a1;
   * </pre>
   *
   * which fails at the second line in the JVM because of a bad cast that is not caught using
   * reflection.
   */
  @Test
  public void runBadCopyCastTest() {
    TestEnvironment testEnvironment = systemTestEnvironment.createTestEnvironment("bad-copy-cast");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.addTestClass("generation.Dim5Matrix");
    options.addTestClass("generation.Dim6Matrix");
    options.setOption("generatedLimit", "2000");
    options.setOption("outputLimit", "200");

    generateAndTestWithCoverage(testEnvironment, options, ExpectedTests.SOME, ExpectedTests.NONE);
  }

  /* Test based on classes from the olajgo library. Has an instantiation error for
      <N> randoop.types.CompoundFunction<N>.<init> : () -> randoop.types.CompoundFunction<N>
      and generates no sequences
  @Test
  public void runAbstractWithRecursiveBoundTest() {
    TestEnvironment testEnvironment =
        systemTestEnvironment.createTestEnvironment("abstract-recursive-bound");
    RandoopOptions options = RandoopOptions.createOptions(testEnvironment);
    options.addTestClass("randoop.types.AbstractMultiary"); // abstract shouldn't load
    options.addTestClass("randoop.types.CompoundFunction"); //uses AbstractMultiary
    options.setOption("generatedLimit", "1");
    generateAndTestWithCoverage(testEnvironment, options, ExpectedTests.SOME, ExpectedTests.SOME);
  }
  */

  /* ------------------------------ utility methods ---------------------------------- */

  /**
   * Runs a standard system test:
   *
   * <ol>
   *   <li>runs Randoop and compiles the generated tests,
   *   <li>checks that the number of generated tests meets the expectation (none or some),
   *   <li>runs any generated tests,
   *   <li>checks that types of tests run as expected.
   * </ol>
   *
   * @param environment the working environment
   * @param options the Randoop command-line arguments
   * @param expectedRegression the minimum expected number of regression tests
   * @param expectedError the minimum expected number of error tests
   */
  private void generateAndTestWithCoverage(
      TestEnvironment environment,
      RandoopOptions options,
      ExpectedTests expectedRegression,
      ExpectedTests expectedError,
      CoverageChecker coverageChecker) {

    RandoopRunStatus runStatus = generateAndCompile(environment, options, false);

    String packageName = options.getPackageName();

    TestRunStatus regressionRunDesc =
        runRegressionTests(environment, options, expectedRegression, runStatus, packageName);

    TestRunStatus errorRunDesc =
        runErrorTests(environment, options, expectedError, runStatus, packageName);

    coverageChecker.checkCoverage(regressionRunDesc, errorRunDesc);
  }

  /**
   * Performs a standard test of Randoop including a check of coverage that assumes all declared
   * methods of the classes under test should be covered.
   *
   * @param environment the working environment of the test
   * @param options the Randoop options
   * @param expectedRegression the minimum expected number of regression tests
   * @param expectedError the minimum expected error tests
   */
  private void generateAndTestWithCoverage(
      TestEnvironment environment,
      RandoopOptions options,
      ExpectedTests expectedRegression,
      ExpectedTests expectedError) {
    generateAndTestWithCoverage(
        environment, options, expectedRegression, expectedError, new CoverageChecker(options));
  }

  /**
   * Checks that the expected number of error-revealing tests have been generated, and if any are
   * expected runs them, captures and returns the result.
   *
   * @param environment the working environment for the test
   * @param options the Randoop options
   * @param expectedError the quantifier for the expected number of error tests
   * @param runStatus the status of the Randoop run
   * @param packageName the package name for generated tests
   * @return the {@link TestRunStatus} for running the error tests, may be null
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
   * @param environment the working environment of the test
   * @param options the Randoop options
   * @param expectedRegression the quantifier for expected regression tests
   * @param runStatus the Randoop run status
   * @param packageName the package name for generated tests
   * @return the {@link TestRunStatus} for the execution of the regression tests, null if there are
   *     none
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
   * output. Failure of Randoop may be allowed by passing true for {@code allowRandoopFailure},
   * otherwise, the test will fail.
   *
   * @param environment the working environment for the test
   * @param options the Randoop options
   * @param allowRandoopFailure flag whether to allow Randoop failure
   * @return the captured {@link RandoopRunStatus} from running Randoop
   */
  private RandoopRunStatus generateAndCompile(
      TestEnvironment environment, RandoopOptions options, boolean allowRandoopFailure) {
    RandoopRunStatus runStatus =
        RandoopRunStatus.generateAndCompile(environment, options, allowRandoopFailure);

    System.out.println("Randoop:");
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
}
