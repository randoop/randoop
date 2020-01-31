package randoop.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.checkerframework.checker.signature.qual.InternalForm;
import org.plumelib.options.Option;
import org.plumelib.options.OptionGroup;
import org.plumelib.options.Options;
import org.plumelib.options.Unpublicized;
import org.plumelib.reflection.Signatures;
import org.plumelib.util.EntryReader;
import org.plumelib.util.FileWriterWithName;
import randoop.Globals;
import randoop.reflection.OperationModel;
import randoop.reflection.VisibilityPredicate;
import randoop.util.Randomness;
import randoop.util.ReflectionExecutor;

/** Container for Randoop options. They are stored as static variables, not instance variables. */
@SuppressWarnings("WeakerAccess")
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
   * Treat every class in the given jar file as a class to test. The jarfile must be on the
   * classpath.
   *
   * <p>See the notes about <a
   * href="https://randoop.github.io/randoop/manual/#specifying-methods">specifying methods that may
   * appear in a test</a>.
   */
  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Code under test:  which classes and members may be used by a test")
  @Option("A jarfile, all of whose classes should be tested")
  public static List<Path> testjar = new ArrayList<>();

  /**
   * File that lists classes to test. All of their methods are methods under test.
   *
   * <p>In the file, each class under test is specified by its fully-qualified name on a separate
   * line. See an <a href= "https://randoop.github.io/randoop/manual/class_list_example.txt">
   * example</a>. These classes are tested in addition to any specified using {@code --testjar} and
   * {@code --testclass}.
   *
   * <p>Using {@code --classlist} is less common than {@code --testjar}. See the notes about <a
   * href="https://randoop.github.io/randoop/manual/#specifying-methods">specifying methods that may
   * appear in a test</a>.
   */
  @Option("File that lists classes under test")
  public static Path classlist = null;

  // TODO: implement this feature
  // /**
  //  * A regex that indicates classes that should not be used in tests, even if included by some
  //  * other command-line option. The regex is matched against fully-qualified class names. If the
  //  * regular expression contains anchors "{@code ^}" or "{@code $}", they refer to the beginning
  //  * and the end of the class name.
  //  */
  // @Option("Do not test classes that match regular expression <string>")
  // public static List<Pattern> omit_classes = new ArrayList<>();

  // TODO: implement this feature
  // /**
  //  * A file containing a list of regular expressions that indicate classes not to call in a test.
  //  * These patterns are used along with those provided with {@code --omit-classes}.
  //  */
  // @Option("File containing regular expressions for methods to omit")
  // public static Path omit_classes_file = null;

  /**
   * The fully-qualified raw name of a class to test; for example, {@code
   * --testclass=java.util.TreeSet}. All of its methods are methods under test. This class is tested
   * in addition to any specified using {@code --testjar} or {@code --classlist}.
   *
   * <p>See the notes about <a
   * href="https://randoop.github.io/randoop/manual/#specifying-methods">specifying methods that may
   * appear in a test</a>.
   */
  @Option("The fully-qualified name of a class under test")
  public static List<String> testclass = new ArrayList<>();

  // A relative URL like <a href="#specifying-methods"> works when this
  // Javadoc is pasted into the manual, but not in Javadoc proper.
  /**
   * A file containing a list of methods and constructors to call in tests, each given as a <a
   * href="https://randoop.github.io/randoop/manual/#fully-qualified-signature">fully-qualified
   * signature</a> on a separate line.
   *
   * <p>These methods augment any methods from classes given by the {@code --testjar}, {@code
   * --classlist}, and {@code --testclass} options.
   *
   * <p>See an <a href= "https://randoop.github.io/randoop/manual/method_list_example.txt">example
   * file</a>.
   *
   * <p>Using {@code --methodlist} is less common, and more error-prone, than {@code --testjar},
   * {@code --classlist}, or {@code --testclass}. See the notes about <a
   * href="https://randoop.github.io/randoop/manual/#specifying-methods">specifying methods that may
   * appear in a test</a>.
   */
  @Option("File that lists methods under test")
  public static Path methodlist = null;

  /**
   * A regex that indicates methods that should not be called directly in generated tests. This does
   * not prevent indirect calls to such methods from other, allowed methods.
   *
   * <p>Randoop will not directly call a method whose <a
   * href="https://randoop.github.io/randoop/manual/#fully-qualified-signature">fully-qualified
   * signature</a> matches the regular expression, or a method inherited from a superclass or
   * interface whose signature matches the regular expression.
   *
   * <p>If the regular expression contains anchors "{@code ^}" or "{@code $}", they refer to the
   * beginning and the end of the signature string.
   */
  @Option("Do not call methods that match regular expression <string>")
  public static List<Pattern> omitmethods = null;

  /**
   * A file containing a list of regular expressions that indicate methods that should not be
   * included in generated tests. These patterns are used along with those provided with {@code
   * --omitmethods}, and the default omissions.
   */
  @Option("File containing regular expressions for methods to omit")
  public static List<Path> omitmethods_file = null;

  /**
   * Include methods that are otherwise omitted by default. Unless you set this to true, every
   * method replaced by the {@code replacecall} agent is treated as if it had been supplied as an
   * argument to {@code --omitmethods}.
   */
  @Unpublicized
  @Option("Don't use the default omitmethods value")
  public static boolean omitmethods_no_defaults = false;

  /**
   * Include methods that are otherwise omitted by default. Unless you set this to true, every
   * method replaced by the {@code replacecall} agent is treated as if it had been supplied as an
   * argument to {@code --omitmethods}.
   */
  @Unpublicized
  @Option("Don't omit methods that are replaced by the replacecall agent")
  public static boolean dont_omit_replaced_methods = false;

  /**
   * A fully-qualified field name of a field to be excluded from test generation. An accessible
   * field is used unless it is omitted by this or the {@code --omit-field-list} option.
   */
  @Option("Omit field from generated tests")
  public static List<String> omit_field = null;

  /**
   * File that contains fully-qualified field names to be excluded from test generation. An
   * accessible field is used unless it is omitted by this or the {@code --omit-field} option.
   */
  @Option("File containing field names to omit from generated tests")
  public static Path omit_field_list = null;

  /**
   * Restrict tests to only include public members of classes. Ordinarily, the setting of {@code
   * --junit-package-name} and package accessibility is used to determine which members will be used
   * in tests. Using this option restricts the tests to only use public members even if the class is
   * a member of the same package as the generated tests.
   */
  @Option("Only use public members in tests")
  public static boolean only_test_public_members = false;

  @Option("Ignore class names specified by user that cannot be found")
  public static boolean silently_ignore_bad_class_names = false;

  /**
   * (For debugging.) If an error or exception is thrown during type instantiation or input
   * selection, this option allows the error to be passed through to {@link
   * GenTests#handle(String[])} where a comprehensive error message is printed.
   */
  @Unpublicized
  @Option("Allow Randoop to fail on any error during test generation")
  public static boolean fail_on_generation_error = false;

  /**
   * Possible behaviors if Randoop generates a flaky test.
   *
   * @see #flaky_test_behavior
   */
  public enum FlakyTestAction {
    /**
     * Randoop halts with a diagnostic message. You can determine the responsible methods, fix or
     * exclude them, and re-run Randoop.
     */
    HALT,
    /**
     * Discard the flaky test. This option should be a last resort. It is inefficient and
     * unproductive for Randoop to produce and discard a lot of flaky tests.
     */
    DISCARD,
    /**
     * Output the flaky test, but with flaky assertions commented out. When the value is {@code
     * OUTPUT}, Randoop also suggests methods under test that might have caused the flakiness. You
     * should <a
     * href="https://randoop.github.io/randoop/manual/index.html#nondeterminism">investigate</a>
     * them, fix or exclude them, then re-run Randoop.
     */
    OUTPUT
  }

  /**
   * What to do if Randoop generates a flaky test. A flaky test is one that behaves differently on
   * different executions.
   *
   * <p>Flaky tests are usually due to calling Randoop on side-effecting or nondeterministic
   * methods, and ultimately, the solution is not to call Randoop on such methods; see section <a
   * href="https://randoop.github.io/randoop/manual/index.html#nondeterminism">Nondeterminism</a> in
   * the Randoop manual.
   */
  @Option("What to do if a flaky test is generated")
  public static FlakyTestAction flaky_test_behavior = FlakyTestAction.OUTPUT;

  /**
   * How many suspected side-effecting or nondeterministic methods (from the program under test) to
   * print.
   */
  @Option("Number of suspected nondeterministic methods to print")
  public static int nondeterministic_methods_to_output = 10;

  /**
   * Whether to output error-revealing tests. Disables all output when used with {@code
   * --no-regression-tests}. Restricting output can result in long runs if the default values of
   * {@code --generated-limit} and {@code --time-limit} are used.
   */
  ///////////////////////////////////////////////////////////////////////////
  @OptionGroup("Which tests to output")
  @Option("Whether to output error-revealing tests")
  public static boolean no_error_revealing_tests = false;

  /**
   * Whether to output regression tests. Disables all output when used with {@code
   * --no-error-revealing-tests}. Restricting output can result in long runs if the default values
   * of {@code --generated-limit} and {@code --time-limit} are used.
   */
  @Option("Whether to output regression tests")
  public static boolean no_regression_tests = false;

  /**
   * Whether to include assertions in regression tests. If false, then the regression tests contain
   * no assertions (except that if the test throws an exception, it should continue to throw an
   * exception of the same type). Tests without assertions can be used to exercise the code, but
   * they do not enforce any particular behavior, such as values returned.
   */
  @Option("Whether to include assertions in regression tests")
  public static boolean no_regression_assertions = false;

  /**
   * Whether to check that generated sequences can be compiled. If true, the code for each generated
   * sequence is compiled, and the sequence is only kept if the compilation succeeds without error.
   * This check is useful because the assumptions in Randoop generation heuristics are sometimes
   * violated by input methods, and, as a result, a generated test may not compile. This check does
   * increases the runtime by approximately 50%.
   */
  @Option("Whether to check if test sequences are compilable")
  public static boolean check_compilable = true;

  /**
   * Classes that must occur in a test. Randoop will only output tests whose source code has at
   * least one use of a member of a class whose name matches the regular expression.
   */
  @Option("Classes that must occur in a test")
  public static Pattern require_classname_in_test = null;

  /**
   * File containing fully-qualified names of classes that the tests must use, directly or
   * indirectly. This option only works if Randoop is run using the <a
   * href="https://randoop.github.io/randoop/manual/index.html#covered-filter">covered-class
   * javaagent</a> to instrument the classes. A test is output only if it uses at least one of the
   * class names in the file. A test uses a class if it invokes any constructor or method of the
   * class, directly or indirectly (the constructor or method might not appear in the source code of
   * the test). Included classes may be abstract.
   */
  @Option("File containing class names that tests must cover")
  public static Path require_covered_classes = null;

  /**
   * If true, Randoop outputs both original error-revealing tests and a minimized version. Setting
   * this option may cause long Randoop run times if Randoop outputs and minimizes more than about
   * 100 error-revealing tests; consider using <a
   * href="https://randoop.github.io/randoop/manual/index.html#option:stop-on-error-test"><code>
   * --stop-on-error-test=true</code></a>. Also see the <a
   * href="https://randoop.github.io/randoop/manual/index.html#optiongroup:Test-case-minimization">test
   * case minimization options</a>.
   */
  // Omit this to keep the documentation short:
  // Regardless of this option's setting, minimization is enabled when
  // {@code --stop-on-error-test} is set.
  @Option("<boolean> to indicate automatic minimization of error-revealing tests")
  // Defaulting to true sometimes causes unacceptable slowdowns.
  public static boolean minimize_error_test = false;

  /** The possible values for exception behavior types. */
  public enum BehaviorType {
    /** Occurrence of exception reveals an error. */
    ERROR,
    /** Occurrence of exception is expected behavior. */
    EXPECTED,
    /** Occurrence of exception indicates an invalid test. */
    INVALID
  }

  /**
   * If a test throws a checked exception, should it be included in the error-revealing test suite
   * (value: ERROR), regression test suite (value: EXPECTED), or should it be discarded (value:
   * INVALID)?
   */
  ///////////////////////////////////////////////////////////////////////////
  @OptionGroup("Test classification")
  @Option("Whether checked exception is an ERROR, EXPECTED or INVALID")
  public static BehaviorType checked_exception = BehaviorType.EXPECTED;

  /**
   * If a test throws an unchecked exception other than {@code ConcurrentModificationException},
   * {@code NoClassDefFoundError}, {@code NullPointerException}, {@code OutOfMemoryError}, and
   * {@code StackOverflowError}, should the test be included in the error-revealing test suite
   * (value: ERROR), regression test suite (value: EXPECTED), or should it be discarded (value:
   * INVALID)?
   *
   * <p>The arguments {@code --cm-exception}, {@code --ncdf-exception}, {@code --npe-on-null-input},
   * {@code --npe-on-non-null-input}, {@code --oom-exception}, and {@code --sof-exception} handle
   * special cases of unchecked exceptions.
   */
  @Option("Whether unchecked exception is an ERROR, EXPECTED or INVALID")
  public static BehaviorType unchecked_exception = BehaviorType.EXPECTED;

  /**
   * If a test throws a {@code ConcurrentModificationException} exception, should it be included in
   * the error-revealing test suite (value: ERROR), regression test suite (value: EXPECTED), or
   * should it be discarded (value: INVALID)?
   */
  @Option("Whether ConcurrentModificationException is an ERROR, EXPECTED or INVALID")
  public static BehaviorType cm_exception = BehaviorType.INVALID;

  /**
   * If a test throws a {@code NoClassDefFoundError} exception, should it be included in the
   * error-revealing test suite (value: ERROR), regression test suite (value: EXPECTED), or should
   * it be discarded (value: INVALID)?
   */
  @Option("Whether NoClassDefFoundError is an ERROR, EXPECTED or INVALID")
  public static BehaviorType ncdf_exception = BehaviorType.INVALID;

  /**
   * If a test that passes {@code null} as an argument throws a {@code NullPointerException}, should
   * the test be be included in the error-revealing test suite (value: ERROR), regression test suite
   * (value: EXPECTED), or should it be discarded (value: INVALID)?
   */
  @Option("Whether NullPointerException on null inputs is an ERROR, EXPECTED or INVALID")
  public static BehaviorType npe_on_null_input = BehaviorType.EXPECTED;

  /**
   * If a test that never passes {@code null} as an argument throws a {@code NullPointerException},
   * should the test be included in the error-revealing test suite (value: ERROR), regression test
   * suite (value: EXPECTED), or should it be discarded (value: INVALID)?
   */
  @Option("Whether NullPointerException on non-null inputs is an ERROR, EXPECTED or INVALID")
  public static BehaviorType npe_on_non_null_input = BehaviorType.ERROR;

  /**
   * If a test throws an {@code OutOfMemoryError} exception, should it be included in the
   * error-revealing test suite (value: ERROR), regression test suite (value: EXPECTED), or should
   * it be discarded (value: INVALID)?
   */
  @Option("Whether OutOfMemoryError is an ERROR, EXPECTED or INVALID")
  public static BehaviorType oom_exception = BehaviorType.INVALID;

  /**
   * If a test throws a {@code StackOverflowError} exception, should it be included in the
   * error-revealing test suite (value: ERROR), regression test suite (value: EXPECTED), or should
   * it be discarded (value: INVALID)?
   */
  @Option("Whether StackOverflowError is an ERROR, EXPECTED or INVALID")
  public static BehaviorType sof_exception = BehaviorType.INVALID;

  ///////////////////////////////////////////////////////////////////
  /** Read file of specifications; see manual section "Specifying expected code behavior". */
  @Option("JSON specifications for methods/constructors")
  public static List<Path> specifications = null;

  /**
   * Use built-in specifications for JDK classes and for classes that inherit from them, as if they
   * had been supplied using the {@code --specifications} command-line argument.
   */
  @Option("Use specifications for JDK classes to classify behaviors for methods/constructors")
  public static boolean use_jdk_specifications = true;

  /**
   * Make Randoop proceed, instead of failing, if the Java condition text of a specification cannot
   * be compiled.
   */
  @Option("Terminate Randoop if specification condition is uncompilable")
  public static boolean ignore_condition_compilation_error = false;

  /**
   * Make Randoop treat a specification whose execution throws an exception as returning {@code
   * false}. If true, Randoop treats {@code x.f == 22} equivalently to the wordier {@code x != null
   * && x.f == 22}. If false, Randoop halts when a specification throws an exception.
   */
  @Option("Terminate Randoop if specification condition throws an exception")
  public static boolean ignore_condition_exception = false;

  ///////////////////////////////////////////////////////////////////
  /**
   * File containing side-effect-free methods, each given as a <a
   * href="https://randoop.github.io/randoop/manual/#fully-qualified-signature">fully-qualified
   * signature</a> on a separate line. Specifying side-effect-free methods has two benefits: it
   * makes regression tests stronger, and it helps Randoop create smaller tests.
   */
  @OptionGroup("Side-effect-free methods")
  @Option("File containing side-effect-free methods")
  public static Path side_effect_free_methods = null;

  /**
   * Maximum number of seconds to spend generating tests. Zero means no limit. If nonzero, Randoop
   * is nondeterministic: it may generate different test suites on different runs.
   *
   * <p>The default value is appropriate for generating tests for a single class in the context of a
   * larger program, but is too small to be effective for generating tests for an entire program.
   *
   * <p>Randoop may run for longer than this because of long-running tests. The elapsed time is
   * checked after each test, not during its execution.
   */
  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Limiting test generation")
  @Option("Maximum number of seconds to spend generating tests")
  public static int time_limit = 100;

  private static int LIMIT_DEFAULT = 100000000;

  /** Maximum number of attempts to generate a test method candidate. */
  @Option("Maximum number of attempts to generate a candidate test")
  public static int attempted_limit = LIMIT_DEFAULT;

  /** Maximum number of test method candidates generated internally. */
  @Option("Maximum number of candidate tests generated")
  public static int generated_limit = LIMIT_DEFAULT;

  /**
   * The maximum number of regression and error-revealing tests to output. If there is no output,
   * this limit has no effect. There is no output when using either {@code --dont-output-tests} or
   * {@code --no-error-revealing-tests} together with {@code --no-regression-tests}.
   *
   * <p>In the current implementation, the number of tests in the output can be substantially
   * smaller than this limit. One reason is that Randoop does not output subsumed tests, which
   * appear as a subsequence of some longer test.
   */
  @Option("Maximum number of tests to ouput")
  public static int output_limit = LIMIT_DEFAULT;

  /**
   * Wraps the three ways of limiting Randoop test generation.
   *
   * <p>The purpose is to shorten parameter lists and make them easier to read.
   */
  public static class Limits {
    /**
     * Maximum time in milliseconds to spend in generation. Must be non-negative. Zero means no
     * limit.
     */
    public int time_limit_millis;
    /** Maximum number of attempts to generate a sequence. Must be non-negative. */
    public int attempted_limit;
    /** Maximum number of sequences to generate. Must be non-negative. */
    public int generated_limit;
    /** Maximum number of sequences to output. Must be non-negative. */
    public int output_limit;

    public Limits() {
      this(
          GenInputsAbstract.time_limit,
          GenInputsAbstract.attempted_limit,
          GenInputsAbstract.generated_limit,
          GenInputsAbstract.output_limit);
    }

    /**
     * @param time_limit maximum time in seconds to spend in generation. Must be non-negative. Zero
     *     means no limit.
     * @param attempted_limit the maximum number of attempts to create a sequence. Must be
     *     non-negative.
     * @param generated_limit the maximum number of sequences to output. Must be non-negative.
     * @param output_limit the maximum number of sequences to generate. Must be non-negative.
     */
    public Limits(int time_limit, int attempted_limit, int generated_limit, int output_limit) {
      this.time_limit_millis = time_limit * 1000;
      this.attempted_limit = attempted_limit;
      this.generated_limit = generated_limit;
      this.output_limit = output_limit;
    }
  }

  /** Do not generate tests with more than this many statements. */
  @Option("Do not generate tests with more than this many statements")
  public static int maxsize = 100;

  /** Stop generation as soon as one error-revealing test has been generated. */
  @Option("Stop after generating any error-revealing test")
  public static boolean stop_on_error_test = false;

  /**
   * Use null with the given frequency as an argument to method calls.
   *
   * <p>For example, a null ratio of 0.05 directs Randoop to use {@code null} as an input 5 percent
   * of the time when a non-{@code null} value of the appropriate type is available.
   *
   * <p>Unless --forbid_null is true, a {@code null} value will still be used if no other value can
   * be passed as an argument even if --null-ratio=0.
   *
   * <p>Randoop never uses {@code null} for receiver values.
   */
  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Values used in tests")
  @Option("Use null as an input with the given frequency")
  public static double null_ratio = 0.05;

  /**
   * Do not use {@code null} as input to methods or constructors, even when no other argument value
   * can be generated.
   *
   * <p>If true, Randoop will not generate a test when unable to find a non-null value of
   * appropriate type as an input. This could result in certain class members being untested.
   */
  @Option("Never use null as input to methods or constructors")
  public static boolean forbid_null = false;

  /**
   * A file containing literal values to be used as inputs to methods under test, or "CLASSES".
   *
   * <p>Literals in these files are used in addition to all other constants in the pool. For the
   * format of this file, see documentation in class {@link randoop.reflection.LiteralFileReader}.
   * The special value "CLASSES" (with no quotes) means to read literals from all classes under
   * test.
   */
  @Option("A file containing literal values to be used as inputs to methods under test")
  public static List<String> literals_file = new ArrayList<>();

  /**
   * How to use literal values that are specified via the {@code --literals-file} command-line
   * option.
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
    /** Do not use literals specified in a literals file. */
    NONE,
    /** A literal for a given class is used as input only to methods of that class. */
    CLASS,
    /** A literal is used as input to methods of any classes in the same package. */
    PACKAGE,
    /** Each literal is used as input to any method under test. */
    ALL
  }

  /**
   * Randoop generates new tests by choosing from a set of methods under test. This controls how the
   * next method is chosen, from among all methods under test.
   */
  @Option("How to choose the next method to test")
  public static MethodSelectionMode method_selection = MethodSelectionMode.UNIFORM;

  /** The possible values of the method_selection command-line argument. */
  public enum MethodSelectionMode {
    /** Select methods randomly with uniform probability. */
    UNIFORM,
    /**
     * The "Bloodhound" technique from the GRT paper prioritizes methods with lower branch coverage.
     */
    BLOODHOUND
  }

  /** Print to standard out, method weights and method uncovered ratios. */
  @Unpublicized
  @Option("Output Bloodhound-related information such as method weights and coverage ratios")
  public static boolean bloodhound_logging = false;

  /**
   * Bloodhound can update coverage information at a regular interval that is either based on time
   * or on the number of successful invocations.
   */
  @Unpublicized
  @Option("Specify how Bloodhound decides when to update coverage information")
  public static BloodhoundCoverageUpdateMode bloodhound_update_mode =
      BloodhoundCoverageUpdateMode.TIME;

  /** The possible modes for updating the coverage information that is used by Bloodhound. */
  public enum BloodhoundCoverageUpdateMode {
    /** Update coverage information at some regular interval of time. */
    TIME,
    /** Update coverage information after some number of successful invocations. */
    INVOCATIONS
  }

  // Implementation note: when checking whether a String S exceeds the given
  // maxlength, we test if StringEscapeUtils.escapeJava(S), because this is
  // the length of the string that will actually be printed out as code.
  /**
   * Maximum length of strings in generated tests, including in assertions. Strings longer than 65KB
   * (or about 10,000 characters) may be rejected by the Java compiler, according to the Java
   * Virtual Machine specification.
   */
  @Option("Maximum length of Strings in generated tests")
  public static int string_maxlen = 10000;

  ///////////////////////////////////////////////////////////////////
  /**
   * Try to reuse values from a sequence with the given frequency. If an alias ratio is given, it
   * should be between 0 and 1.
   *
   * <p>A ratio of 0 results in tests where each value created within a test input is typically used
   * at most once as an argument in a method call. A ratio of 1 tries to maximize the number of
   * times values are used as inputs to parameters within a test.
   */
  @OptionGroup("Varying the nature of generated tests")
  @Option("Reuse values with the given frequency")
  public static double alias_ratio = 0;

  public enum InputSelectionMode {
    /** Favor shorter sequences. This makes Randoop produce smaller JUnit tests. */
    SMALL_TESTS,
    /** Select sequences uniformly at random. */
    UNIFORM
  }

  /**
   * Randoop generates new tests by combining old previously-generated tests. This controls how the
   * old tests are chosen, from among all existing tests.
   */
  @Option("How to choose tests for Randoop to extend")
  public static InputSelectionMode input_selection = InputSelectionMode.UNIFORM;

  /**
   * Clear the component set each time it contains the given number of inputs.
   *
   * <p>Randoop stores previously-generated tests in a "component" set, and uses them to generate
   * new tests. Setting this variable to a small number can sometimes result in a greater variety of
   * tests generated during a single run.
   */
  @Option("Clear the component set when it gets this big")
  public static int clear = 100000000;

  ///////////////////////////////////////////////////////////////////
  /** Maximum number of tests to write to each JUnit file. */
  @OptionGroup("Outputting the JUnit tests")
  @Option("Maximum number of tests to write to each JUnit file")
  public static int testsperfile = 500;

  /** Base name (no ".java" suffix) of the JUnit file containing error-revealing tests */
  @Option("Base name of the JUnit file(s) containing error-revealing tests")
  public static String error_test_basename = "ErrorTest";

  /** Base name (no ".java" suffix) of the JUnit file containing regression tests */
  @Option("Base name of the JUnit file(s) containing regression tests")
  public static String regression_test_basename = "RegressionTest";

  /**
   * Name of the package for the generated JUnit files. When the package is the same as the package
   * of a class under test, then package visibility rules are used to determine whether to include
   * the class or class members in a test. Tests can be restricted to public members only by using
   * the option {@code --only-test-public-members}.
   */
  @Option("Name of the package for the generated JUnit files (optional)")
  public static String junit_package_name;

  /**
   * Name of file containing code text to be added to the <a
   * href="http://junit.sourceforge.net/javadoc/org/junit/Before.html">{@code @Before}</a>-annotated
   * method of each generated test class. Code is uninterpreted, and, so, is not run during
   * generation. Intended for use when run-time behavior of classes under test requires setup
   * behavior that is not needed for execution by reflection. (The annotation {@code @Before} is
   * JUnit 4, and {@code @BeforeEach} is JUnit 5.)
   */
  @Option("Filename for code to include in Before-annotated method of test classes")
  public static String junit_before_each = null;

  /**
   * Name of file containing code text to be added to the <a
   * href="http://junit.sourceforge.net/javadoc/org/junit/After.html">{@code @After} </a>-annotated
   * method of each generated test class. Intended for use when run-time behavior of classes under
   * test requires tear-down behavior that is not needed for execution by reflection. Code is
   * uninterpreted, and, so, is not run during generation. (The annotation {@code @After} is JUnit
   * 4, and {@code @AfterEach} is JUnit 5.)
   */
  @Option("Filename for code to include in After-annotated method of test classes")
  public static String junit_after_each = null;

  /**
   * Name of file containing code text to be added to the <a
   * href="http://junit.sourceforge.net/javadoc/org/junit/BeforeClass.html">{@code @BeforeClass}</a>-annotated
   * method of each generated test class. Intended for use when run-time behavior of classes under
   * test requires setup behavior that is not needed for execution by reflection. Code is
   * uninterpreted, and, so, is not run during generation. (The annotation {@code @BeforeClass} is
   * JUnit 4, and {@code @BeforeAll} is JUnit 5.)
   */
  @Option("Filename for code to include in BeforeClass-annotated method of test classes")
  public static String junit_before_all = null;

  /**
   * Name of file containing code text to be added to the <a
   * href="http://junit.sourceforge.net/javadoc/org/junit/AfterClass.html">{@code @AfterClass}</a>-annotated
   * method of each generated test class. Intended for use when run-time behavior of classes under
   * test requires tear-down behavior that is not needed for execution by reflection. Code is
   * uninterpreted, and, so, is not run during generation. (The annotation {@code @AfterClass} is
   * JUnit 4, and {@code @AfterAll} is JUnit 5.)
   */
  @Option("Filename for code to include in AfterClass-annotated method of test classes")
  public static String junit_after_all = null;

  /** Name of the directory in which JUnit files should be written. */
  @Option("Name of the directory to which JUnit files should be written")
  public static String junit_output_dir = null;

  /**
   * Run test generation without output. May be desirable when running with a visitor.
   *
   * <p>NOTE: Because there is no output, the value of {@code --output-limit} will never be met, so
   * be sure to set {@code --generated-limit} or {@code --time-limit} to a reasonable value when
   * using this option.
   */
  @Option("Run Randoop but do not output JUnit tests")
  public static boolean dont_output_tests = false;

  /**
   * Whether to use JUnit's standard reflective mechanisms for invoking tests. JUnit's reflective
   * invocations can interfere with code instrumentation, such as by the DynComp tool. If that is a
   * problem, then set this to false and Randoop will output tests that use direct method calls
   * instead of reflection. The tests will include a {@code main} method and will execute methods
   * and assertions, but won't be JUnit suites.
   */
  @Option("If true, use JUnit's reflective invocation; if false, use direct method calls")
  public static boolean junit_reflection_allowed = true;

  ///////////////////////////////////////////////////////////////////
  /** System properties that Randoop will set similarly to {@code java -D}, of the form "x=y". */
  @OptionGroup("Runtime environment")
  // This list enables Randoop to pass these properties to other JVMs, which woud not be easy if the
  // user ran Randoop using `java -D`.  (But, Randoop does not seem to do so!  It was removed.)
  @Option("-D Specify system properties to be set; similar to <code>java -Dx=y</code>.")
  public static List<String> system_props = new ArrayList<>();

  /**
   * How much memory Randoop should use when starting new JVMs. This only affects new JVMs; you
   * still need to supply {@code -Xmx...} when starting Randoop itself.
   */
  @Option("Maximum memory for JVM; will be passed with <code>-Xmx</code>.")
  // CircleCI runs out of memory during test generation if 2500m.
  public static String jvm_max_memory = "3000m";

  @Unpublicized
  @Option("Store all output to stdout and stderr in the ExecutionOutcome.")
  public static boolean capture_output = false;

  /**
   * The random seed to use in the generation process. If you want to produce multiple different
   * test suites, run Randoop multiple times with a different random seed. By default, Randoop is
   * deterministic: you do not need to provide this option to make Randoop deterministic.
   */
  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Controlling randomness")
  @Option("The random seed to use in the generation process")
  public static int randomseed = (int) Randomness.DEFAULT_SEED;

  // Currently, Randoop is deterministic, and there isn't a way to make Randoop not pay the costs of
  // (for example) LinkedHashMaps instead of HashMaps.  The only effect of this command-line
  // argument is to forbid certain other command-line arguments that would themselves introduce
  // nondeterminism.
  /**
   * If true, Randoop is deterministic: running Randoop twice with the same arguments (including
   * {@code --randomseed}) will produce the same test suite, so long as the program under test is
   * deterministic. If false, Randoop may or may not produce the same test suite. To produce
   * multiple different test suites, use the {@code --randomseed} command-line option.
   */
  @Option("If true, Randoop is deterministic")
  public static boolean deterministic = false;

  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Logging, notifications, and troubleshooting Randoop")
  @Option("Run noisily: display information such as progress updates.")
  public static boolean progressdisplay = true;

  // Default value for progressintervalmillis; helps to see if user has set it.
  public static long PROGRESSINTERVALMILLIS_DEFAULT = 60000;

  @Option("Display progress message every <int> milliseconds. -1 means no display.")
  public static long progressintervalmillis = PROGRESSINTERVALMILLIS_DEFAULT;

  @Option("Display progress message every <int> attempts to create a test; -1 means none")
  public static long progressintervalsteps = 1000;

  @Option("Perform expensive internal checks (for Randoop debugging)")
  public static boolean debug_checks = false;

  /**
   * A file to which to log lots of information. If not specified, no logging is done. Enabling the
   * logs slows down Randoop.
   */
  @Option("<filename> Log lots of information to this file")
  public static FileWriterWithName log = null;

  /**
   * A file to which to log selections; helps find sources of non-determinism. If not specified, no
   * logging is done.
   */
  @Option("<filename> Log each random selection to this file")
  public static FileWriterWithName selection_log = null;

  /** A file to which to log the operation usage history. */
  @Option("<filename> Log operation usage counts to this file")
  public static FileWriterWithName operation_history_log = null;

  /**
   * True if Randoop should print generated tests that do not compile, which indicate Randoop bugs.
   */
  @Option("Display source if a generated test contains a compilation error.")
  public static boolean print_non_compiling_file = false;

  /**
   * Create sequences but never execute them. Used to test performance of Randoop's sequence
   * generation code.
   */
  @Unpublicized
  @Option("Create sequences but never execute them")
  public static boolean dontexecute = false;

  ///////////////////////////////////////////////////////////////////
  /** Install the given runtime visitor. See class randoop.ExecutionVisitor. */
  @OptionGroup(value = "Advanced extension points")
  @Option("Install the given runtime visitor")
  public static List<@ClassGetName String> visitor = new ArrayList<>();

  ///////////////////////////////////////////////////////////////////
  // This is only here to keep the ICSE07ContainersTest working
  // TODO Need to decide to keep the heuristic that uses this in
  /////////////////////////////////////////////////////////////////// ForwardGenerator
  @OptionGroup(value = "Pacheco thesis", unpublicized = true)
  @Unpublicized
  @Option("Use heuristic that may randomly repeat a method call several times")
  public static boolean repeat_heuristic = false;

  /** Check that the options given satisfy any specified constraints, and fail if they do not. */
  public void checkOptionsValid() {

    if (alias_ratio < 0 || alias_ratio > 1) {
      throw new RandoopUsageError("--alias-ratio must be between 0 and 1, inclusive.");
    }

    if (null_ratio < 0 || null_ratio > 1) {
      throw new RandoopUsageError("--null-ratio must be between 0 and 1, inclusive.");
    }

    if (maxsize <= 0) {
      throw new RandoopUsageError(
          "Maximum sequence size --maxsize must be greater than zero but was " + maxsize);
    }

    if (!literals_file.isEmpty() && literals_level == ClassLiteralsMode.NONE) {
      throw new RandoopUsageError(
          "Invalid parameter combination: specified a class literal file and --use-class-literals=NONE");
    }

    if (deterministic && ReflectionExecutor.usethreads) {
      throw new RandoopUsageError(
          "Invalid parameter combination: --deterministic with --usethreads");
    }

    if (deterministic && time_limit != 0) {
      throw new RandoopUsageError(
          "Invalid parameter combination: --deterministic without --time-limit=0");
    }

    if (deterministic && progressintervalmillis != -1) {
      if (progressintervalmillis == PROGRESSINTERVALMILLIS_DEFAULT) {
        // User didn't supply --progressintervalmillis_default; set it to -1 to suppress output
        progressintervalmillis = -1;
      } else {
        throw new RandoopUsageError(
            "Invalid parameter combination: --deterministic with --progressintervalmillis");
      }
    }

    if (deterministic
        && method_selection == MethodSelectionMode.BLOODHOUND
        && bloodhound_update_mode == BloodhoundCoverageUpdateMode.TIME) {
      throw new RandoopUsageError(
          "Invalid parameter combination: --deterministic with --bloodhound-update-mode=time");
    }

    if (ReflectionExecutor.call_timeout != ReflectionExecutor.CALL_TIMEOUT_DEFAULT
        && !ReflectionExecutor.usethreads) {
      throw new RandoopUsageError(
          "Invalid parameter combination: --call-timeout without --usethreads");
    }

    if (time_limit == 0
        && attempted_limit >= LIMIT_DEFAULT
        && generated_limit >= LIMIT_DEFAULT
        && output_limit >= LIMIT_DEFAULT) {
      throw new RandoopUsageError(
          String.format(
              "Unlikely parameter combination: --time-limit=0 and high other limits:%n"
                  + " --attempted-limit=%s --generated-limit=%s --output-limit=%s",
              attempted_limit, generated_limit, output_limit));
    }

    if (testclass.isEmpty() && testjar.isEmpty() && classlist == null && methodlist == null) {
      throw new RandoopUsageError(
          "You must specify some classes or methods to test."
              + Globals.lineSep
              + "Use the --testclass, --testjar, --classlist, or --methodlist options.");
    }
  }

  /**
   * Read names of classes under test, as provided with the --classlist command-line argument.
   *
   * @param visibility the visibility predicate
   * @return the classes provided via the --classlist command-line argument
   */
  public static Set<@ClassGetName String> getClassnamesFromArgs(VisibilityPredicate visibility) {
    Set<@ClassGetName String> classnames = getClassNamesFromFile(classlist);
    for (Path jarFile : testjar) {
      classnames.addAll(getClassnamesFromJarFile(jarFile, visibility));
    }
    for (String classname : testclass) {
      if (!Signatures.isClassGetName(classname)) {
        throw new RandoopUsageError(
            "Illegal argument --testclass=" + classname + ", should be a class name");
      }
      classnames.add(classname);
    }
    return classnames;
  }

  /**
   * Read names of classes from a jar file. Ignores interfaces, abstract classes, and non-visible
   * classes.
   *
   * @param jarFile the jar file from which to read classes
   * @param visibility the visibility predicate
   * @return the names of classes in the jar file
   */
  public static Set<@ClassGetName String> getClassnamesFromJarFile(
      Path jarFile, VisibilityPredicate visibility) {
    try {
      Set<@ClassGetName String> classNames = new TreeSet<>();
      ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFile.toString()));
      for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
        if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
          // This ZipEntry represents a class. Now, what class does it represent?
          String classFileName = entry.getName();
          @SuppressWarnings("signature") // string manipulation: convert filename to class name
          @InternalForm String ifClassName =
              classFileName.substring(0, classFileName.length() - ".class".length());
          @ClassGetName String className = Signatures.internalFormToClassGetName(ifClassName);
          Class<?> c;
          try {
            c = Class.forName(className);
          } catch (ClassNotFoundException e) {
            throw new RandoopUsageError(
                className
                    + " not found on classpath.  Ensure that "
                    + jarFile
                    + " is on the classpath.");
          }
          if (OperationModel.nonInstantiable(c, visibility) == null) {
            classNames.add(className);
          }
        }
      }
      return classNames;
    } catch (IOException e) {
      String message =
          String.format("Error while reading jar file %s: %s%n", jarFile, e.getMessage());
      throw new RandoopUsageError(message, e);
    }
  }

  /**
   * Returns the class names listed in the file.
   *
   * @param file the file containing the strings
   * @return the lines in the file, or null if listFile is null
   */
  public static Set<@ClassGetName String> getClassNamesFromFile(Path file) {
    Set<@ClassGetName String> result = new LinkedHashSet<>();
    for (String line : getStringSetFromFile(file, "class names")) {
      if (!Signatures.isClassGetName(line)) {
        throw new RandoopUsageError(
            "Illegal value \"" + line + "\" in " + file + ", should be a class name");
      }

      result.add(line);
    }
    return result;
  }

  /**
   * Returns a set consisting of the lines of the file, except those starting with "#". Returns
   * empty set if listFile is null.
   *
   * @param listFile the file containing the strings
   * @param fileDescription string used in error messages
   * @return the lines in the file, or null if listFile is null
   */
  public static Set<String> getStringSetFromFile(Path listFile, String fileDescription) {
    return getStringSetFromFile(listFile, fileDescription, "^#.*", null);
  }

  /**
   * Returns a set consisting of the lines of the file. Returns empty set if listFile is null.
   *
   * @param listFile the file containing the strings
   * @param fileDescription string used in error messages
   * @param commentRegex indicates which lines are comments that should be ignored
   * @param includeRegex if this string appears in the file, then another file is recursively read
   * @return the strings in the file, or null if listFile is null
   */
  @SuppressWarnings("SameParameterValue")
  public static Set<String> getStringSetFromFile(
      @Nullable Path listFile, String fileDescription, String commentRegex, String includeRegex) {
    Set<String> elementSet = new LinkedHashSet<>();
    if (listFile != null) {
      try (EntryReader er = new EntryReader(listFile.toFile(), commentRegex, includeRegex)) {
        for (String line : er) {
          String trimmed = line.trim();
          if (!trimmed.isEmpty()) {
            elementSet.add(trimmed);
          }
        }
      } catch (IOException e) {
        String message =
            String.format(
                "Error while reading %s file %s: %s%n", fileDescription, listFile, e.getMessage());
        throw new RandoopUsageError(message, e);
      }
    }
    return elementSet;
  }
}
