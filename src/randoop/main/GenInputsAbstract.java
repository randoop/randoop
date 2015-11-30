package randoop.main;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import randoop.util.Randomness;
import randoop.util.Util;

import plume.Option;
import plume.OptionGroup;
import plume.Options;
import plume.Unpublicized;

/**
 * Container for Randoop options.
 *
 */
public abstract class GenInputsAbstract extends CommandHandler {

  public GenInputsAbstract(String command, String pitch,
      String commandGrammar, String where, String summary, List<String> notes, String input,
      String output, String example, Options options) {
    super(command, pitch, commandGrammar, where, summary, notes, input, output,
        example, options);
  }


  /** Each element is the fully-qualified name of a class under test. */
  ///////////////////////////////////////////////////////////////////
  @OptionGroup ("Code under test")
  @Option("The fully-qualified name of a class under test")
  public static List<String> testclass = new ArrayList<String>();

  /**
   * The name of a file that lists classes under test.
   * 
   * In the file, each class under test is specified by its
   * fully-qualified name on a separate line.
   */
  @Option("The name of a file that lists classes under test")
  public static String classlist = null;

  // A relative URL like <a href="#specifying-methods"> works when this
  // Javadoc is pasted into the manual, but not in Javadoc proper.
  /**
   * The name of a file that lists methods under test.
   * 
   * In the file, each each method under test is specified on a separate
   * line. The list of methods given by this argument augment
   * any methods derived via the <tt>--testclass</tt> or
   * <tt>--classlist</tt> option.
   * 
   * Also see the manual section on <a href="https://rawgit.com/randoop/randoop/master/doc/index.html#specifying-methods">specifying methods 
   * and constructors that may appear in a test</a>.
   */
  @Option("The name of a file that lists methods under test")
  public static String methodlist = null;

  /**
   * Randoop will not attempt to directly call methods whose {@link
   * java.lang.reflect.Method#toString()} matches the regular expression
   * given.  This does not prevent indirect calls to such methods from
   * other, allowed methods.
   * <p>
   * 
   * Randoop only calls methods
   * that are specified by one of the <tt>--testclass</tt>,
   * <tt>-classlist</tt>, or <tt>--methodlist</tt> command-line options;
   * the purpose of <tt>--omitmethods</tt> is to override one of those other
   * command-line options.
   */
  @Option("Do not call methods that match regular expression <string>")
  public static Pattern omitmethods = null;
  
  /**
   * omit_field_list is the name of a file that contains fully-qualified
   * field names to be excluded from test generation. Otherwise, Randoop
   * includes all public fields of a visible class.
   */
  @Option("The name of a file containing field names to omit from generated tests")
  public static String omit_field_list = null;
  
  /**
   * If the command line argument public_only is true, only public
   * classes/methods are considered visible.  If public_only is false
   * then any class/method that is not private is considered visible.
   * 
   * <p>
   * FIXME: This option outputs tests that do not compile. Until a fix
   *        is done (which probably involves reflective invocation),
   *        keep the option @Unpublicized. The option should
   *        probably be an enum with PUBLIC, PACKAGE, PUBLIC elements.
   */
  @Unpublicized  
  @Option("Specify whether to use only public members in tests")
  public static boolean public_only = true;

  @Option("Specifies initialization routine (class.method)")
  public static String init_routine = null;
  
  @Option("Ignore class names specified by user that cannot be found")
  public static boolean silently_ignore_bad_class_names = false;
  
  /**
   * The possible values of the literals_level command-line argument.
   * @see #literals_level
   */
  public static enum ClassLiteralsMode {
    /** do not use literals specified in a literals file */
    NONE,
      /** a literal for a given class is used as input only to methods of that class */
      CLASS,
      /** a literal is used as input to methods of any classes in the same package */
      PACKAGE,
      /** each literal is used as input to any method under test */
      ALL;
  }
  
  /**
   * How to use literal values (see --literals-file).
   * @see ClassLiteralsMode
   */
  @Option("How to use literal values (see --literals-file): ALL, PACKAGE, CLASS, or NONE")
  public static ClassLiteralsMode literals_level = ClassLiteralsMode.CLASS;
  
  /**
   * A file containing literal values to be used as inputs to methods under test.
   * 
   * Literals in these files are used in addition to all other constants in the pool.
   * For the format of this file, see documentation in class {@link randoop.LiteralFileReader}.
   * The special value "CLASSES" (with no quotes) means to read literals from all classes under test.
   */
  @Option("A file containing literal values to be used as inputs to methods under test")
  public static List<String> literals_file = new ArrayList<String>(); 


  /** The random seed to use in the generation process */
  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Controlling randomness")
  @Option("The random seed to use in the generation process")
  public static int randomseed = (int) Randomness.SEED;

  
  /**
   * Maximum number of seconds to spend generating tests.
   * 
   * Used to determine when to stop test generation. Generation stops when
   * either the time limit (--timelimit=int) OR the input limit
   * (--inputlimit=int) is reached.
   *
   * The default value is appropriate for generating tests for a single
   * class in the context of a larger program, but is too small to be effectiev
   * for generating tests for an entire program.
   *
   * Note that if you use this option, Randoop is nondeterministic: it
   * may generate different test suites on different runs.
   */
  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Limiting test generation")
  @Option("Maximum number of seconds to spend generating tests")
  public static int timelimit = 100;

  /**
   * Determines the maximum number of tests to output.
   * 
   * This command-line option is generally more appropriate than
   * --inputlimit, which controls how many test candidates Randoop
   * generates internally.
   */
  @Option ("Maximum number of tests to ouput; contrast to --inputlimit")
  public static int outputlimit = 100000000;

  /**
   * Maximum number of test candidates generated.
   * 
   * Used to determine when to stop test generation. Generation stops when
   * either the time limit (--timelimit=int) OR the input limit
   * (--inputlimit=int) is reached.  The number of tests output
   * may be smaller than then number of test candidates generated,
   * because redundant and illegal tests may be discarded.
   * 
   * The --outputlimit command-line option is usually more appropriate than
   * --inputlimit.
   */
  @Option("Maximum number of tests generated")
  public static int inputlimit = 100000000;

  /** Do not generate tests with more than this many statements */
  @Option("Do not generate tests with more than <int> statements")
  public static int maxsize = 100;
  
  /**
   * Never use null as input to methods or constructors.
   * 
   * This option causes Randoop to abandon the method call rather than providing
   * null as an input, when no non-null value of the appropriate type is available.
   * 
   * To ask Randoop to calls methods with null with greater frequency,
   * see option --null-ratio.
   */
  @Option("Never use null as input to methods or constructors")
  public static boolean forbid_null = true;


  // Implementation note: when checking whether a String S exceeds the given
  // maxlength, we test if StringEscapeUtils.escapeJava(S), because this is
  // the length of the string that will atually be printed out as code.
  /**
   * Maximum length of strings in generated tests.  Strings longer than
   * 65KB (or about 10,000 characters) may be rejected by the Java
   * compiler, according to the Java Virtual Machine specification.
   */
  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Varying the nature of generated tests")
  @Option("Maximum length of Strings in generated tests")
  public static int string_maxlen = 10000;
  
  /**
   * Use null with the given frequency.
   * 
   * If a null ratio is given, it should be between 0 and 1. A ratio of X means that
   * null will be used instead of a non-null value as a parameter to method calls,
   * with X frequency (1 means always use null, 0 means never use null). For example,
   * a ratio of 0.5 directs Randoop to use null inputs 50 percent of the time.
   * 
   * Randoop never uses null for receiver values.
   * 
   */
  @Option("Use null as an input with the given frequency")
  public static double null_ratio = 0;
  
  /**
   * Try to reuse values from a sequence with the given frequency.
   * 
   * If an alias ratio is given, it should be between 0 and 1.
   * 
   * A ratio of 0 results in tests where each value created within a test input is typically used at most once
   * as an argument in a method call. A ratio of 1 tries to maximize the number of times
   * values are used as inputs to parameters within a test. 
   */
  @Option("Reuse values with the given frequency")
  public static double alias_ratio = 0;

  /**
   * Favor shorter sequences when assembling new sequences out of old ones.
   *
   * Randoop generate new tests by combining old previously-generated tests.
   * If this option is given, tests with fewer calls are given greater weight during
   * its random selection. This has the overall effect of producing smaller JUnit tests.
   */
  @Option("Favor shorter tests during generation")
  public static boolean small_tests = false;

  /**
   * Clear the component set each time it contains the given number of inputs.
   *
   * <p>
   * Randoop stores previously-generated tests in a "component" set, and uses them to
   * generate new tests. Setting this variable to a small number can sometimes result
   * in a greater variety of tests generated during a single run.
   * </p>
   */
  @Option("Clear the component set when it gets this big")
  public static int clear = 100000000;

  
  /**
   * Use the methods specified in the given file to create regression assertions.
   */
  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Creating test oracles")
  @Option("File containing observer functions")
  // This file is used to populate RegressionCaptureVisitor.observer_map
  public static File observers = null;
  
  /**
   * Use Randoop's default set of object contracts as assertions.
   * If disabled, these assertions are not created.
   * 
   * <p>
   * The default set of contracts includes:
   *   equals(Object) is reflexive,
   *   equals(Object) is symmetric,
   *   equals(Object) and hashCode() are consistent,
   *   x.equals(null) returns false,
   *   any nullary method annotated with {@code @CheckRep} returns true.
   * </p>
   */
  @Option("Use Randoop's object contracts as assertions")
  public static boolean check_object_contracts = true;

  /**
   * Capture the current behavior as assertions.
   * This makes Randoop's tests act as regression tests that ensure that the
   * code continues to behave as it did when the tests were generated.
   */
  @Option("Use current behavior as assertions")
  public static boolean check_regression_behavior = true;


  
  ///////////////////////////////////////////////////////////////////
  @OptionGroup ("Outputting the JUnit tests")
  // TODO make an enum. (But presently Options package requires upper-case
  // strings for enums, which will break Make targets, plugin, etc.)
  /** For details, see the Javadoc documentation for {@link DefaultTestFilter}. */
  @Option("What kinds of tests to output: pass, fail, or all")
  public static String output_tests = "all";
  
  public final static String ALL = "all";
  public final static String FAIL = "fail";
  public final static String PASS = "pass";

  @Option("Simplify (shorten) failed tests while preserving failure behavior")
  public static boolean simplify_failed_tests = false;

  /** Maximum number of tests to write to each JUnit file */
  @Option("Maximum number of tests to write to each JUnit file")
  public static int testsperfile = 500;

  /** Base name (no ".java" suffix) of the JUnit file containing Randoop-generated tests */
  @Option("Base name of the JUnit file(s) containing tests")
  public static String junit_classname = "RandoopTest";

  /** Name of the package for the generated JUnit files */
  @Option("Name of the package for the generated JUnit files")
  public static String junit_package_name = "";

  /** Name of the directory to which JUnit files should be written */
  @Option("Name of the directory to which JUnit files should be written")
  public static String junit_output_dir = null;
  
  @Option("Run Randoop but do not create JUnit tests")
  public static boolean dont_output_tests = false;
  
  /**
   * Output sequences even if they do not complete execution.
   * 
   *  Randoop's default behavior is to output only tests consisting of
   *  method call sequences that execute every statement, rather than throwing
   *  an exception or failing a contract check before the last statement.
   */
  @Option("Output sequences even if they do not complete execution")
  public static boolean output_nonexec = false;
  
  @Option("specifies regex of classes that must be in any regression tests")
  public static Pattern test_classes = null;
  
  /**
   * Whether to use JUnit's standard reflective mechanisms for invoking
   * tests.  JUnit's reflective invocations can interfere with code
   * instrumentation, such as by the DynComp tool.  If that is a problem,
   * then set this to false and Randoop will output tests that use direct
   * method calls instead of reflection.  The tests will execute methods and
   * assertions, but won't be JUnit suites.
   */
  @Option("If true, use JUnit's reflective invocation; if false, use direct method calls")
  public static boolean junit_reflection_allowed = true;


  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Runtime environment")
  // We do this rather than using java -D so that we can easily pass these
  // to other JVMs
  @Option("-D Specify system properties to be set (similar to java -Dx=y)")
  public static List<String> system_props = new ArrayList<String>();
  
  /**
   * Specify an extra command for recursive JVM calls that Randoop spawns.
   * The argument to the --agent option is the entire extra JVM command.  A
   * typical invocation of Randoop might be:
   * <pre>java -javaagent:<em>jarpath</em>=<em>args</em> randoop.main.Main gentests --agent="-javaagent:<em>jarpath</em>=<em>args</em>"</pre>
   */
  @Option("Specify an extra command for recursive JVM calls")
  public static String agent = null;

  @Option("specify the memory size (in megabytes) for recursive JVM calls")
  public static int mem_megabytes = 1000;

  @Option("Capture all output to stdout and stderr")
  public static boolean capture_output = false;



  ///////////////////////////////////////////////////////////////////
  // I don't see how to create the serialized files, only write to them.
  // Maybe the writing code has bit-rotted?
  @OptionGroup("Serialized input/output of generated tests")  
  @Option("Read serialized test inputs from the given file")
  public static List<String> componentfile_ser = new ArrayList<String>();

  @Option("Read serialized test inputs from the given file (text-based)")
  public static List<String> componentfile_txt = new ArrayList<String>();

  /**
   * Output components (serialized, GZIPPED) to the given file.
   * 
   * Suggestion: use a .gz suffix in file name.
   */
  @Option("Output components (serialized, GZIPPED) to the given file.")
  public static String output_components = null;

  /**
   * Output tests (sequences plus checkers) in serialized form to the given file.
   * 
   * Suggestion: use a .gz suffix in file name.
   */
  @Option("Output tests (sequences plus checkers) in serialized form to the given file.")
  public static String output_tests_serialized = null;


  /**
   * Randoop uses the specified port for output, in serialized form (used by Eclipse plugin).
   * 
   * If this value is not -1, Randoop relays information about the
   * program's execution over a connection to the specified port on the
   * local machine. Information is sent using a serialized
   * randoop.runtime.Message object. Printing is also suppressed.
   */
  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Notifications")
  @Option("Uses the specified port for notifications (used by Eclipse plugin).")
  public static int comm_port = -1;
  
  @Option("Do not display progress update message to console")
  public static boolean noprogressdisplay = false;

  @Option("Display progress message every <int> milliseconds")
  public static long progressinterval = 5000;


  /**
   * Install the given runtime visitor. See class randoop.ExecutionVisitor.
   */
  ///////////////////////////////////////////////////////////////////
  @OptionGroup(value="Advanced extension points")
  @Option("Install the given runtime visitor")
  public static List<String> visitor = new ArrayList<String>();

  
  ///////////////////////////////////////////////////////////////////
  @OptionGroup(value="Logging and troubleshooting Randoop")
  @Option("Perform expensive internal checks (for Randoop debugging)")
  public static boolean debug_checks = false;

  /**
   * Name of a file to which to log lots of information.
   * If not specified, no logging is done.
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


   
  /**
   * Whether to use the long format for outputting JUnit tests.
   * The long format emits exactly one line per statement, including
   * primitive declarations, and uses boxed primitives. This option is used
   * in the branch-directed generation project.
   */
  ///////////////////////////////////////////////////////////////////
  // These options are only used for the branch-directed generation
  // research project.
  @OptionGroup(value="Branch-directed generation", unpublicized=true)
  @Unpublicized
  /**
   * In long format, primitive values are stored in variables and the
   * variables are used, as in "int x = 3 ; foo(x)".  In short format,
   * primitive values are directly added to methods, as in "foo(3)".
   */
  @Option("Use long format for outputting JUnit tests.")
  public static boolean long_format = false;

  @Unpublicized
  @Option("Output branch->witness-sequences map")
  public static String output_covmap = null;

  @Unpublicized
  @Option("Output witness sequences for coverage branches")
  public static boolean output_cov_witnesses = false;

  @Unpublicized
  @Option("Whenever an object is called for, use an integer")
  public static boolean always_use_ints_as_objects = false;
  
  @Unpublicized
  @Option("The name of a file containing the list of coverage-instrumented classes")
  public static String coverage_instrumented_classes = null;

  @Unpublicized
  @Option("Output covered branches to the given text file")
  public static String output_branches = null;
  

  ///////////////////////////////////////////////////////////////////
  @OptionGroup(value="Eliminating redundant tests")
  @Option("Remove tests that are subsumed in other tests")
  public static boolean remove_subsequences = true;

  /**
   * Run each test twice and compare the checks.  If the results differ,
   * then disable the test.
   */
  @Option("Run each test twice and compare the checks")
  public static boolean compare_checks = false;

  @Option("Create clean checks for a serialized sequence")
  public static File clean_checks = null;

  @Option("Print any checks that are different in the clean run")
  public static boolean print_diff_obs = false;

  
  ///////////////////////////////////////////////////////////////////
  // These options are useful in the context of Carlos's PhD thesis
  // experiments and shouldn't be needed by external users.
  @OptionGroup(value="Pacheco thesis", unpublicized=true)
  @Unpublicized
  @Option("Write experiment results file")
  public static FileWriter expfile = null;

  @Unpublicized
  @Option("Do not do online illegal")
  public static boolean offline = false;

  @Unpublicized
  @Option("Use heuristic that may randomly repeat a method call several times")
  public static boolean repeat_heuristic = false;
  
  @Unpublicized
  @Option("Use object cache")
  public static boolean use_object_cache = false;
  
  /**
   * Check that the options given satisfy any specified constraints, and fail if they do not.
   */
  public void checkOptionsValid() {
    
    if (!(output_tests.equals(ALL) || output_tests.equals(PASS) || output_tests.equals(FAIL))) {
      StringBuilder b = new StringBuilder();
      b.append("Option output-tests must be one of ");
      b.append(ALL);
      b.append(", ");
      b.append(PASS);
      b.append(", or ");
      b.append(FAIL);
      b.append(".");
      throw new RuntimeException(b.toString());
    }
    
    if (alias_ratio < 0 || alias_ratio > 1) {
      throw new RuntimeException("Alias ratio must be between 0 and 1, inclusive.");
    }

    if (null_ratio < 0 || null_ratio > 1) {
      throw new RuntimeException("Null ratio must be between 0 and 1, inclusive.");
    }

    if (maxsize <= 0) {
      throw new RuntimeException("Maximum sequence size must be greater than zero but was " + maxsize);
    }    
  }

  List<Class<?>> findClassesFromArgs(Options printUsageTo) {
    List<Class<?>> classes = new ArrayList<Class<?>>();

    if (classlist != null) {
      File classListingFile = new File(classlist);
      try {
        classes.addAll(ClassReader.getClassesForFile(classListingFile));
      } catch (Exception e) {
        String msg = Util.toNColsStr("ERROR while reading list of classes to test: " + e.getMessage(), 70);
        System.out.println(msg);
        System.exit(1);
      }
    }
    
    ClassNameErrorHandler errorHandler = new ThrowClassNameError();
    if (silently_ignore_bad_class_names) {
      errorHandler = new WarnOnBadClassName();
    }
    classes.addAll(ClassReader.getClassesForNames(testclass, errorHandler));

    return classes;
  }
}
