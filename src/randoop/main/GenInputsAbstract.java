package randoop.main;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import plume.Option;
import plume.OptionGroup;
import plume.Options;
import plume.Unpublicized;
import randoop.util.Randomness;
import randoop.util.Reflection;
import randoop.util.Util;

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

  ///////////////////////////////////////////////////////////////////
  @OptionGroup ("Code under test")

  /** Each element is the fully-qualified name of a class under test. */
  @Option("The fully-qualified name of a class under test")
  public static List<String> testclass = new ArrayList<String>();

  /**
   * The name of a file that lists classes under test.
   * 
   * In the file, each class under test is specified by its fully
   * qualified name on a separate line.
   */
  @Option("The name of a file that lists classes under test")
  public static String classlist = null;

  // A relative URL like <a href="#specifying-methods"> works when this
  // Javadoc is pasted into the manual, but not in Javadoc proper.
  /**
   * The name of a file that lists methods under test.
   * 
   * In the file, each each method under test is specified on a separate
   * line. The list of methods given by this argument will augment
   * any methods derived via the --classlist option.
   * 
   * Also see the manual section on <a href="http://randoop.googlecode.com/hg/doc/index.html#specifying-methods">specifying methods 
   * and constructors under test</a>.
   */
  @Option("The name of a file that lists methods under test")
  public static String methodlist = null;

  /**
   * Randoop will not attempt to directly call methods whose {@link
   * java.lang.reflect.Method#toString()} matches the regular expression
   * given.  This does not prevent indirect calls to such methods from
   * other, allowed methods.
   */
  @Option("Do not call methods that match regular expression <string>")
  public static Pattern omitmethods = null;
  
  /**
   * If the command line argument public_only is true, only public
   * classes/methods are considered visible.  If public_only is false
   * then any class/method that is not private is considered visible.
   * 
   * FIXME: This option outputs tests that do not compile. Until a fix
   *        is done, keep the option @Unpublicized. The option should
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
  public static ClassLiteralsMode literals_level = ClassLiteralsMode.NONE;
  
  /**
   * A file containing literal values to be used as inputs to methods under test.
   * 
   * Literals in these files are used in addition to all other constants in the pool.
   * For the format of this file, see documentation in class {@link randoop.LiteralFileReader}.
   * The special value "CLASSES" (with no quotes) means to read literals from all classes under test.
   */
  @Option("A file containing literal values to be used as inputs to methods under test")
  public static List<String> literals_file = new ArrayList<String>(); 

  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Controlling randomness")
  
  /** The random seed to use in the generation process */
  @Option("The random seed to use in the generation process")
  public static int randomseed = (int) Randomness.SEED;

  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Limiting test generation")
  
  /**
   * Maximum number of seconds to spend generating tests.
   * 
   * Used to determine when to stop test generation. Generation stops when
   * either the time limit (--timelimit=int) OR the input limit (--inputlimit=int) is reached.
   */
  @Option("Maximum number of seconds to spend generating tests")
  public static int timelimit = 100;

  /**
   * Maximum number of tests generated.
   * 
   * Used to determine when to stop test generation. Generation stops when
   * either the time limit (--timelimit=int) OR the input limit
   * (--inputlimit=int) is reached.  The number of tests output
   * may be smaller than then number of inputs created, because redundant
   * and illegal inputs may be discarded.  Also see --outputlimit.
   */
  @Option("Maximum number of tests generated")
  public static int inputlimit = 100000000;

  /**
   * Determines the maximum number of tests to output, no matter how many
   * are generated.  Contrast to --inputlimit.
   */
  @Option ("Maximum number of tests to ouput; contrast to --inputlimit")
  public static int outputlimit = 100000000;

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

  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Varying the nature of generated tests")

  // Implementation note: when checking whether a String S exceeds the given
  // maxlength, we test if StringEscapeUtils.escapeJava(S), because this is
  // the length of the string that will atually be printed out as code.
  /**
   * Maximum length of strings in generated tests.  Strings longer than
   * 65KB (or about 10,000 characters) may be rejected by the Java
   * compiler, according to the Java Virtual Machine specification.
   */
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
   * <br />
   * Randoop stores previously-generated tests in a "component" set, and uses them to
   * generate new tests. Setting this variable to a small number can sometimes result
   * in a greater variety of tests generated during a single run.
   */
  @Option("Clear the component set when it gets this big")
  public static int clear = 100000000;

  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Creating test oracles")
  
  /**
   * Use the methods specified in the given file to create regression assertions.
   * 
   * TODO: this is a useful feature but has no tests to ensure it works.
   * Write tests and then remove @Unpublicized annotation. 
   */
  @Unpublicized
  @Option("File containing observer functions")
  public static File observers = null;
  
  /**
   * Use Randoop's default set of object contracts.
   *  
   * By default, Randoop checks a set of contracts, e.g.
   * equals(Object) is reflexive, equals(null) returns false, no NullPointerExceptions, no AssertionErrors, etc.
   */
  @Option("Use Randoop's default set of object contracts")
  public static boolean check_object_contracts = true;

  ///////////////////////////////////////////////////////////////////
  @OptionGroup ("Outputting the JUnit tests")
  
  // TODO make an enum. (But presently Options package requires upper-case
  // strings for enums, which will break Make targets, plugin, etc.)
  @Option("What kinds of tests to output: pass, fail, or all")
  public static String output_tests = "all";
  
  public final static String all = "all";
  public final static String fail = "fail";
  public final static String pass = "pass";

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
   *  method call sequences that execute to the end, rather than throwing
   *  an exception or failing a contract check in the middle of execution.
   */
  @Option("Output sequences even if they do not complete execution")
  public static boolean output_nonexec = false;

  @Option("Remove full package + class name declarations, and change the variables name (e.g., change ArrayList var0 to ArrayList arrayList0) in an output sequence. This option will not change the sequence behavior.")
  public static boolean pretty_print = false;
  
  @Option("specifies regex of classes that must be in any regression tests")
  public static Pattern test_classes = null;
  
  ///////////////////////////////////////////////////////////////////
  // We do this rather than using java -D so that we can easily pass these
  // to other JVMs
  @OptionGroup("Runtime environment")
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
  @OptionGroup("Serialized input/output of generated tests")  

  @Option("Serialize test inputs to the given file")
  public static List<String> componentfile_ser = new ArrayList<String>();

  @Option("Serialize test inputs to the given file (text-based)")
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

  ///////////////////////////////////////////////////////////////////
  @OptionGroup("Notifications")

  /**
   * Randoop uses the specified port for output, in serialized form (used by Eclipse plugin).
   * 
   * If this value is not -1, Randoop relays information about the
   * program's execution over a connection to the specified port on the
   * local machine. Information is sent using a serialized
   * randoop.runtime.Message object. Printing is also suppressed.
   */
  @Option("Uses the specified port for notifications (used by Eclipse plugin).")
  public static int comm_port = -1;
  
  @Option("Do not display progress update message to console")
  public static boolean noprogressdisplay = false;

  @Option("Display progress message every <int> milliseconds")
  public static long progressinterval = 5000;

  ///////////////////////////////////////////////////////////////////
  @OptionGroup(value="Advanced extension points")

  /**
   * Install the given runtime visitor. See class randoop.ExecutionVisitor.
   */
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


  ///////////////////////////////////////////////////////////////////
  // These options are only used for the branch-directed generation
  // research project.
  @OptionGroup(value="Branch-directed generation", unpublicized=true)
    
  /**
   * Whether to use the long format for outputting JUnit tests.
   * The long format emits exactly one line per statement, including
   * primitive declarations, and uses boxed primitives. This option is used
   * in the branch-directed generation project.
   */
  @Unpublicized
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
    
    if (!(output_tests.equals(all) || output_tests.equals(pass) || output_tests.equals(fail))) {
      StringBuilder b = new StringBuilder();
      b.append("Option output-tests must be one of ");
      b.append(all);
      b.append(", ");
      b.append(pass);
      b.append(", or ");
      b.append(fail);
      b.append(".");
      throw new RuntimeException(b.toString());
    }
    
    if (alias_ratio < 0 && alias_ratio > 1) {
      throw new RuntimeException("Alias ratio must be between 0 and 1.");
    }

    if (null_ratio < 0 && null_ratio > 1) {
      throw new RuntimeException("Null ratio must be between 0 and 1.");
    }

    if (maxsize <= 0) {
      throw new RuntimeException("Maximum sequence size must be greater than zero but was " + maxsize);
    }    
  }

  List<Class<?>> findClassesFromArgs(Options printUsageTo) {
    List<Class<?>> classes = new ArrayList<Class<?>>();
    try {
      if (classlist != null) {
        File classListingFile = new File(classlist);
        classes.addAll(Reflection.loadClassesFromFile(classListingFile, true));
      }
      classes.addAll(Reflection.loadClassesFromList(testclass, silently_ignore_bad_class_names));
    } catch (Exception e) {
      String msg = Util.toNColsStr("ERROR while reading list of classes to test: " + e.getMessage(), 70);
      System.out.println(msg);
      System.exit(1);
    }
    return classes;
  }
}
