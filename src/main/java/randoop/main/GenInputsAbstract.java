package randoop.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

import plume.EntryReader;
import plume.Option;
import plume.OptionGroup;
import plume.Options;
import plume.Unpublicized;

import randoop.util.Randomness;
import randoop.util.Util;

/**
 * Container for Randoop options.
 *
 */
public abstract class GenInputsAbstract extends CommandHandler {

  public GenInputsAbstract(
      String command,
      String pitch,
      String commandGrammar,
      String where,
      String summary,
      List<String> notes,
      String input,
      String output,
      String example,
      Options options) {
    super(command, pitch, commandGrammar, where, summary, notes, input, output, example, options);
  }

  /**
   * The fully-qualified name of a class to test. This class is tested in
   * addition to any specified using <code>--classlist</code>, and must be
   * accessible from the package of the tests (set with
   * <code>--junit-package-name</code>).
   */
  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Code under test")
  @Option("The fully-qualified name of a class under test")
  public static List<String> testclass = new ArrayList<>();

  /**
   * File that lists classes to test.
   *
   * In the file, each class under test is specified by its fully-qualified name
   * on a separate line. See an <a href=
   * "https://randoop.github.io/randoop/manual/class_list_example.txt">
   * example</a>. These classes are tested in addition to any specified using
   * <code>--testclass</code>. All classes must be accessible from the package of
   * the tests (set with <code>--junit-package-name</code>).
   */
  @Option("File that lists classes under test")
  public static File classlist = null;

  // A relative URL like <a href="#specifying-methods"> works when this
  // Javadoc is pasted into the manual, but not in Javadoc proper.
  /**
   * File that lists methods to test.
   *
   * In the file, each each method under test is specified on a separate line.
   * The list of methods given by this argument augment any methods derived via
   * the <code>--testclass</code> or <code>--classlist</code> option.
   *
   * <p>
   * A constructor line begins with <code>"cons :"</code> followed by the
   * classname, the string <code>&lt;init&gt;</code> and the constructor's
   * parameter types, enclosed in parentheses. Methods are specified in a
   * similar way. For example:
   * </p>
   *
   * <pre class="code">
   * cons : Type0.&lt;init&gt;(Type1, Type2, ..., TypeN)
   * method : Type0.method_name(Type1, Type2, ..., TypeN)
   * </pre>
   *
   * <p>
   * Each <code>Type<i>i</i></code> must be fully-qualified (include package
   * names).
   * </p>
   *
   * <p>
   * See an <a href=
   * "https://randoop.github.io/randoop/manual/method_list_example.txt">
   * example</a>.
   * </p>
   */
  @Option("File that lists methods under test")
  public static File methodlist = null;

  /**
   * File containing side-effect-free observer methods. These are used to create
   * regression assertions, and to prune tests.
   */
  @Option("File containing observer functions")
  // This file is used to populate RegressionCaptureVisitor.observer_map
  public static File observers = null;

  /**
   * Randoop will not attempt to directly call methods whose
   * {@link java.lang.reflect.Method#toString()} matches the regular expression
   * given. This does not prevent indirect calls to such methods from other,
   * allowed methods.
   *
   * <p>
   * Randoop only calls methods that are specified by one of the
   * <code>--testclass</code>, <code>-classlist</code>, or <code>--methodlist</code>
   * command-line options; the purpose of <code>--omitmethods</code> is to override
   * one of those other command-line options.
   * </p>
   */
  @Option("Do not call methods that match regular expression <string>")
  public static Pattern omitmethods = null;

  /**
   * File that contains fully-qualified field names to be excluded
   * from test generation. Otherwise, Randoop includes all public fields of a
   * visible class.
   */
  @Option("File containing field names to omit from generated tests")
  public static File omit_field_list = null;

  /**
   * Restrict tests to only include public members of classes. Ordinarily, the
   * setting of <code>--junit-package-name</code> and package accessibility is used
   * to determine which members will be used in tests. Using this option
   * restricts the tests to only use public members even if the class is a
   * member of the same package as the generated tests.
   */
  @Option("Only use public members in tests")
  public static boolean only_test_public_members = false;

  @Option("Ignore class names specified by user that cannot be found")
  public static boolean silently_ignore_bad_class_names = false;

  /**
   * Classes, one of which every test must use. Randoop will only output tests
   * that have at least one use of a member of a class whose name matches the
   * regular expression.
   */
  @Option("Classes, one of which every test must use")
  public static Pattern include_if_classname_appears = null;

  /**
   * File containing fully qualified names of classes that the tests must exercise.
   * This option only works if Randoop is run using
   * <code>-javaagent:exercised_agent.jar</code>
   * to instrument the classes. A test is output only if it exercises at least one
   * of the class names in the file. A test exercises a class if it executes any
   * constructor or method of the class, directly or indirectly (the constructor
   * or method might not appear in the source code of the test).
   * Included classes may be abstract.
   */
  @Option("File containing class names that tests must exercise")
  public static File include_if_class_exercised = null;

  /**
   * Whether to output error-revealing tests.
   *
   * <p>
   * Will completely disable output when used with
   * <code>--no-regression-tests</code>. Be aware that restricting output can
   * result in long runs if the default values of <code>--inputlimit</code> and
   * <code>--timelimit</code> are used.
   * </p>
   */
  @OptionGroup("Test classification")
  @Option("Whether to output error-revealing tests")
  public static boolean no_error_revealing_tests = false;

  /**
   * Whether to output regression tests.
   *
   * <p>
   * Will completely disable output when used with
   * <code>--no-error-revealing-tests</code>. Be aware that restricting output
   * can result in long runs if the default values of <code>--inputlimit</code>
   * and <code>--timelimit</code> are used.
   * </p>
   */
  @Option("Whether to output regression tests")
  public static boolean no_regression_tests = false;

  /**
   * Whether to include assertions in regression tests. If false, then the
   * regression tests contain no assertions (except that if the test throws an
   * exception, it should continue to throw an exception of the same type).
   * Tests without assertions can be used to exercise the code, but they do not
   * enforce any particular behavior, such as values returned.
   */
  @Option("Whether to include assertions in regression tests")
  public static boolean no_regression_assertions = false;

  /**
   * The possible values for exception behavior types. The order INVALID, ERROR,
   * EXPECTED should be maintained.
   */
  public enum BehaviorType {
    /** Occurrence of exception reveals an error */
    ERROR,
    /** Occurrence of exception is expected behavior */
    EXPECTED,
    /** Occurrence of exception indicates an invalid test */
    INVALID
  }

  /**
   * If a test throws a checked exception, should it be included in the
   * error-revealing test suite (value: ERROR), regression test suite (value:
   * EXPECTED), or should it be discarded (value: INVALID)?
   */
  @Option("Whether checked exception is an ERROR, EXPECTED or INVALID")
  public static BehaviorType checked_exception = BehaviorType.EXPECTED;

  /**
   * If a test throws an unchecked exception, should the test be included in the
   * error-revealing test suite (value: ERROR), regression test suite (value:
   * EXPECTED), or should it be discarded (value: INVALID)?
   *
   * <p>
   * The arguments <code>--npe-on-null-input</code>,
   * <code>--npe-on-non-null-input</code>, and <code>--oom-exception</code> handle
   * special cases of unchecked exceptions.
   * </p>
   */
  @Option("Whether unchecked exception is an ERROR, EXPECTED or INVALID")
  public static BehaviorType unchecked_exception = BehaviorType.EXPECTED;

  /**
   * If a test where a <code>null</code> value is given as an input throws a
   * <code>NullPointerException</code>, should the test be be included in the
   * error-revealing test suite (value: ERROR), regression test suite (value:
   * EXPECTED), or should it be discarded (value: INVALID)?
   */
  @Option("Whether NullPointerException on null inputs is an ERROR, EXPECTED or INVALID")
  public static BehaviorType npe_on_null_input = BehaviorType.EXPECTED;

  /**
   * If a test where no <code>null</code> values are given as an input throws a
   * <code>NullPointerExceptoin</code>, should the test be included in the
   * error-revealing test suite (value: ERROR), regression test suite (value:
   * EXPECTED), or should it be discarded (value: INVALID)?
   */
  @Option("Whether NullPointerException on non-null inputs is an ERROR, EXPECTED or INVALID")
  public static BehaviorType npe_on_non_null_input = BehaviorType.ERROR;

  /**
   * If a test throws an <code>OutOfMemoryError</code> exception, should it be
   * included in the error-revealing test suite (value: ERROR), regression test
   * suite (value: EXPECTED), or should it be discarded (value: INVALID)?
   */
  @Option("Whether OutOfMemoryException is an ERROR, EXPECTED or INVALID")
  public static BehaviorType oom_exception = BehaviorType.INVALID;

  /**
   * Ignore the situation where a code sequence that previously executed
   * normally throws an exception when executed as part of a longer test
   * sequence. If true, the sequence will be classified as invalid. If false,
   * Randoop will halt with information about the sequence to aid in identifying
   * the issue.
   *
   * <p>
   * Use of this option is a last resort. Flaky tests are usually due to calling
   * Randoop on side-effecting or nondeterministic methods, and a better
   * solution is not to call Randoop on such methods.
   * </p>
   */
  @Option("Whether to ignore non-determinism in test execution")
  public static boolean ignore_flaky_tests = false;

  /**
   * Maximum number of seconds to spend generating tests.
   *
   * Test generation stops when either the time limit (--timelimit) is reached,
   * OR the number of generated sequences reaches the input limit
   * (--inputlimit), OR the number of error-revealing and regression tests
   * reaches the output limit (--outputlimit).
   *
   * The default value is appropriate for generating tests for a single class in
   * the context of a larger program, but is too small to be effective for
   * generating tests for an entire program.
   *
   * Note that if you use this option, Randoop is nondeterministic: it may
   * generate different test suites on different runs.
   */
  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Limiting test generation")
  @Option("Maximum number of seconds to spend generating tests")
  public static int timelimit = 100;

  /**
   * The maximum number of regression and error-revealing tests to output. Test
   * generation stops when either the time limit (--timelimit) is reached, OR
   * the number of generated sequences reaches the input limit (--inputlimit),
   * OR the number of error-revealing and regression tests reaches the output
   * limit (--outputlimit).
   *
   * <p>
   * This option affects how many tests will occur in the output, as opposed to
   * --inputlimit, which affects the number of test method candidates that are
   * generated internally. This option is a better choice for controlling the
   * tests you get, because the number of candidates generated will always be
   * larger than the number output since redundant and invalid tests are
   * filtered. However, the current implementation means that the actual number
   * of tests in the output can still be substantially smaller than this limit.
   * </p>
   *
   * <p>
   * This limit will have no effect if there is no output, which occurs when
   * using either <code>--dont-output-tests</code> or
   * <code>--no-error-revealing-tests</code> together with
   * <code>--no-regression-tests</code>. In this case, the option
   * <code>--inputlimit</code> should be used.
   * </p>
   */
  @Option("Maximum number of tests to ouput; contrast to --inputlimit")
  public static int outputlimit = 100000000;

  /**
   * Maximum number of test method candidates generated. Test generation stops
   * when either the time limit (--timelimit) is reached, OR the number of
   * generated sequences reaches the input limit (--inputlimit), OR the number
   * of error-revealing and regression tests reaches the output limit
   * (--outputlimit). The number of tests output will be smaller than then
   * number of test candidates generated, because redundant and illegal tests
   * may be discarded.
   *
   * <p>
   * This limit should be used when no tests are output, which occurs when using
   * either <code>--dont-output-tests</code> or
   * <code>--no-error-revealing-tests</code> together with
   * <code>--no-regression-tests</code>. Otherwise the
   * <code>--outputlimit</code> command-line option is usually more appropriate.
   * </p>
   */
  @Option("Maximum number of tests generated")
  public static int inputlimit = 100000000;

  /** Do not generate tests with more than this many statements */
  @Option("Do not generate tests with more than <int> statements")
  public static int maxsize = 100;

  /**
   * Use null with the given frequency as an argument to method calls.
   *
   * For example, a null ratio of 0.05 directs Randoop to use <code>null</code>
   * as an input 5 percent of the time when a non-<code>null</code> value of the
   * appropriate type is available.
   *
   * Unless --forbid_null is true, a <code>null</code> value will still be used
   * if no other value can be passed as an argument even if --null-ratio=0.
   *
   * Randoop never uses <code>null</code> for receiver values.
   */
  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Values used in tests")
  @Option("Use null as an input with the given frequency")
  public static double null_ratio = 0.05;

  /**
   * Do not use <code>null</code> as input to methods or constructors when no
   * other argument value can be generated.
   *
   * <p>
   * If true, Randoop will not generate a test when unable to find a non-null
   * value of appropriate type as an input. This could result in certain class
   * members being untested.
   * </p>
   *
   * <p>
   * Does not affect the behavior based on --null_ratio, which independently
   * determines the frequency that <code>null</code> is used as an input.
   * </p>
   */
  @Option("Never use null as input to methods or constructors")
  public static boolean forbid_null = false;

  /**
   * A file containing literal values to be used as inputs to methods under
   * test, or "CLASSES".
   *
   * <p>
   * Literals in these files are used in addition to all other constants in the
   * pool. For the format of this file, see documentation in class
   * {@link randoop.reflection.LiteralFileReader}. The special value "CLASSES" (with no
   * quotes) means to read literals from all classes under test.
   * </p>
   */
  @Option("A file containing literal values to be used as inputs to methods under test")
  public static List<String> literals_file = new ArrayList<>();

  /**
   * How to use literal values that are specified via the
   * <code>--literals-file</code> command-line option.
   *
   * @see ClassLiteralsMode
   */
  @Option("How to use literal values specified via --literals-file: ALL, PACKAGE, CLASS, or NONE")
  public static ClassLiteralsMode literals_level = ClassLiteralsMode.CLASS;

  /**
   * The possible values of the literals_level command-line argument.
   *
   * @see #literals_level
   */
  public enum ClassLiteralsMode {
    /** do not use literals specified in a literals file */
    NONE,
    /**
     * a literal for a given class is used as input only to methods of that
     * class
     */
    CLASS,
    /**
     * a literal is used as input to methods of any classes in the same package
     */
    PACKAGE,
    /** each literal is used as input to any method under test */
    ALL
  }

  // Implementation note: when checking whether a String S exceeds the given
  // maxlength, we test if StringEscapeUtils.escapeJava(S), because this is
  // the length of the string that will atually be printed out as code.
  /**
   * Maximum length of strings in generated tests, including in assertions.
   * Strings longer than 65KB (or about 10,000 characters) may be rejected by
   * the Java compiler, according to the Java Virtual Machine specification.
   */
  @Option("Maximum length of Strings in generated tests")
  public static int string_maxlen = 10000;

  ///////////////////////////////////////////////////////////////////
  /**
   * Try to reuse values from a sequence with the given frequency. If an alias
   * ratio is given, it should be between 0 and 1.
   *
   * <p>
   * A ratio of 0 results in tests where each value created within a test input
   * is typically used at most once as an argument in a method call. A ratio of
   * 1 tries to maximize the number of times values are used as inputs to
   * parameters within a test.
   * </p>
   */
  @OptionGroup("Varying the nature of generated tests")
  @Option("Reuse values with the given frequency")
  public static double alias_ratio = 0;

  /**
   * Favor shorter sequences when assembling new sequences out of old ones.
   *
   * <p>
   * Randoop generates new tests by combining old previously-generated tests. If
   * this option is given, tests with fewer calls are given greater weight
   * during its random selection. This has the overall effect of producing
   * smaller JUnit tests.
   * </p>
   */
  @Option("Favor shorter tests during generation")
  public static boolean small_tests = false;

  /**
   * Clear the component set each time it contains the given number of inputs.
   *
   * <p>
   * Randoop stores previously-generated tests in a "component" set, and uses
   * them to generate new tests. Setting this variable to a small number can
   * sometimes result in a greater variety of tests generated during a single
   * run.
   * </p>
   */
  @Option("Clear the component set when it gets this big")
  public static int clear = 100000000;

  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Outputting the JUnit tests")

  /** Maximum number of tests to write to each JUnit file */
  @Option("Maximum number of tests to write to each JUnit file")
  public static int testsperfile = 500;

  /**
   * Base name (no ".java" suffix) of the JUnit file containing error-revealing
   * tests
   */
  @Option("Base name of the JUnit file(s) containing error-revealing tests")
  public static String error_test_basename = "ErrorTest";

  /**
   * Base name (no ".java" suffix) of the JUnit file containing regression tests
   */
  @Option("Base name of the JUnit file(s) containing regression tests")
  public static String regression_test_basename = "RegressionTest";

  /**
   * Name of the package for the generated JUnit files. When the package is the
   * same as the package of a class under test, then package visibility rules
   * are used to determine whether to include the class or class members in a
   * test. Tests can be restricted to public members only by using the option
   * <code>--only-test-public-members</code>.
   */
  @Option("Name of the package for the generated JUnit files")
  public static String junit_package_name = "";

  /**
   * Name of file containing code text to be added to the Before-annotated method of
   * each generated test class.
   * Code is uninterpreted, and, so, is not run during generation.
   * Intended for use when run-time behavior of classes under test requires setup
   * behavior that is not needed for execution by reflection.
   * (The annotation Before is JUnit 4, and BeforeEach is JUnit 5.)
   */
  @Option("Filename for code to include in Before-annotated method of test classes")
  public static String junit_before_each = null;

  /**
   * Name of file containing code text to be added to the After-annotated method of
   * each generated test class.
   * Intended for use when run-time behavior of classes under test requires
   * tear-down behavior that is not needed for execution by reflection.
   * Code is uninterpreted, and, so, is not run during generation.
   * (The annotation After is JUnit 4, and AfterEach is JUnit 5.)
   */
  @Option("Filename for code to include in After-annotated method of test classes")
  public static String junit_after_each = null;

  /**
   * Name of file containing code text to be added to the BeforeClass-annotated method of
   * each generated test class.
   * Intended for use when run-time behavior of classes under test requires setup
   * behavior that is not needed for execution by reflection.
   * Code is uninterpreted, and, so, is not run during generation.
   * (The annotation BeforeClass is JUnit 4, and BeforeAll is JUnit 5.)
   */
  @Option("Filename for code to include in BeforeClass-annotated method of test classes")
  public static String junit_before_all = null;

  /**
   * Name of file containing code text to be added to the AfterClass-annotated method of
   * each generated test class.
   * Intended for use when run-time behavior of classes under test requires
   * tear-down behavior that is not needed for execution by reflection.
   * Code is uninterpreted, and, so, is not run during generation.
   * (The annotation After is JUnit 4, and AfterEach is JUnit 5.)
   */
  @Option("Filename for code to include in AfterClass-annotated method of test classes")
  public static String junit_after_all = null;

  /** Name of the directory to which JUnit files should be written */
  @Option("Name of the directory to which JUnit files should be written")
  public static String junit_output_dir = null;

  /** Name of the file containing the current index from which generation must proceed. */
  @Option("Name of the file containing the current index from which generation must proceed")
  public static File generation_index_file = null;

  /**
   * Run test generation without output. May be desirable when running with a
   * visitor.
   *
   * <p>
   * NOTE: Because there is no output, the value of <code>--outputlimit</code>
   * will never be met, so be sure to set <code>--inputlimit</code> or
   * <code>--timelimit</code> to a reasonable value when using this option.
   * </p>
   */
  @Option("Run Randoop but do not output JUnit tests")
  public static boolean dont_output_tests = false;

  /**
   * Whether to use JUnit's standard reflective mechanisms for invoking tests.
   * JUnit's reflective invocations can interfere with code instrumentation,
   * such as by the DynComp tool. If that is a problem, then set this to false
   * and Randoop will output tests that use direct method calls instead of
   * reflection. The tests will execute methods and assertions, but won't be
   * JUnit suites. The tests will include a <code>main</code> method.
   */
  @Option("If true, use JUnit's reflective invocation; if false, use direct method calls")
  public static boolean junit_reflection_allowed = true;

  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Runtime environment")
  // We do this rather than using java -D so that we can easily pass these
  // to other JVMs
  @Option("-D Specify system properties to be set (similar to java -Dx=y)")
  public static List<String> system_props = new ArrayList<>();

  /**
   * Specify an extra command for recursive JVM calls that Randoop spawns. The
   * argument to the --agent option is the entire extra JVM command. A typical
   * invocation of Randoop might be:
   *
   * <pre>
   * java -javaagent:<em>jarpath</em>=<em>args</em> randoop.main.Main gentests --agent="-javaagent:<em>jarpath</em>=<em>args</em>"
   * </pre>
   */
  @Option("Specify an extra command for recursive JVM calls")
  public static String agent = null;

  @Option("specify the memory size (in megabytes) for recursive JVM calls")
  public static int mem_megabytes = 1000;

  @Option("Capture all output to stdout and stderr")
  public static boolean capture_output = false;

  /**
   * The random seed to use in the generation process. Note that Randoop is
   * deterministic: running it twice will produce the same test suite. If you
   * want to produced multiple different test suites, run Randoop multiple times
   * with a different random seed.
   */
  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Controlling randomness")
  @Option("The random seed to use in the generation process")
  public static int randomseed = (int) Randomness.SEED;

  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Notifications")
  @Option("Do not display progress update message to console")
  public static boolean noprogressdisplay = false;

  @Option("Display progress message every <int> milliseconds")
  public static long progressinterval = 5000;

  /**
   * Install the given runtime visitor. See class randoop.ExecutionVisitor.
   */
  ///////////////////////////////////////////////////////////////////
  @OptionGroup(value = "Advanced extension points")
  @Option("Install the given runtime visitor")
  public static List<String> visitor = new ArrayList<>();

  ///////////////////////////////////////////////////////////////////
  @OptionGroup(value = "Logging and troubleshooting Randoop")
  @Option("Perform expensive internal checks (for Randoop debugging)")
  public static boolean debug_checks = false;

  /**
   * Name of a file to which to log lots of information. If not specified, no
   * logging is done.
   */
  @Option("<filename> Name of a file to which to log lots of information")
  public static FileWriter log = null;

  ///////////////////////////////////////////////////////////////////
  // Options used when testing Randoop.

  /**
   * Create sequences but never execute them. Used to test performance of
   * Randoop's sequence generation code.
   */
  @Unpublicized
  @Option("Create sequences but never execute them")
  public static boolean dontexecute = false;

  ///////////////////////////////////////////////////////////////////
  // This is only here to keep the ICSE07ContainersTest working
  // TODO Need to decide to keep the heuristic that uses this in
  /////////////////////////////////////////////////////////////////// ForwardGenerator
  @OptionGroup(value = "Pacheco thesis", unpublicized = true)
  @Unpublicized
  @Option("Use heuristic that may randomly repeat a method call several times")
  public static boolean repeat_heuristic = false;

  @Option(
    value = "Do not write junit test classes, instead, only output sequences to sequences file."
  )
  public static boolean output_only_sequences;

  protected static long methods_count;

  protected static Date execution_start;

  protected static BigInteger num_sequences_to_be_examined;

  public static long getMethodsCount() {
    return methods_count;
  }

  public static Date getExecutionStart() {
    return execution_start;
  }

  public static BigInteger getNumSequencesToBeExamined() {
    return num_sequences_to_be_examined;
  }

  /**
   * Check that the options given satisfy any specified constraints, and fail if
   * they do not.
   */
  public void checkOptionsValid() {

    if (alias_ratio < 0 || alias_ratio > 1) {
      throw new RuntimeException("Alias ratio must be between 0 and 1, inclusive.");
    }

    if (null_ratio < 0 || null_ratio > 1) {
      throw new RuntimeException("Null ratio must be between 0 and 1, inclusive.");
    }

    if (maxsize <= 0) {
      throw new RuntimeException(
          "Maximum sequence size must be greater than zero but was " + maxsize);
    }

    if (!literals_file.isEmpty() && literals_level == ClassLiteralsMode.NONE) {
      throw new RuntimeException(
          "Invalid parameter combination: specified a class literal file but --use-class-literals=NONE");
    }

    if (generation_index_file != null && !generation_index_file.exists()) {
      throw new RuntimeException(
          "Invalid parameter generation-index-file, which must point to an existing file.");
    }
  }

  public static Set<String> getClassnamesFromArgs() {
    String errMessage = "ERROR while reading list of classes to test";
    Set<String> classnames = getStringSetFromFile(classlist, errMessage);
    classnames.addAll(testclass);
    return classnames;
  }

  public static Set<String> getStringSetFromFile(File listFile, String errMessage) {
    return getStringSetFromFile(listFile, errMessage, "^#.*", null);
  }

  public static Set<String> getStringSetFromFile(
      File listFile, String errMessage, String commentRegex, String includeRegex) {
    Set<String> elementSet = new LinkedHashSet<>();
    if (listFile != null) {
      try (EntryReader er = new EntryReader(listFile, commentRegex, includeRegex)) {
        for (String line : er) {
          String trimmed = line.trim();
          if (!trimmed.isEmpty()) {
            elementSet.add(trimmed);
          }
        }
      } catch (IOException e) {
        String msg = Util.toNColsStr(errMessage + ": " + e.getMessage(), 70);
        System.err.println(msg);
        System.exit(1);
      }
    }
    return elementSet;
  }
}
