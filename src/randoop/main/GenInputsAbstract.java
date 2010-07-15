package randoop.main;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import plume.Option;
import plume.Options;
import plume.Unpublicized;
import randoop.util.Randomness;
import randoop.util.Reflection;
import randoop.util.Util;

public abstract class GenInputsAbstract extends CommandHandler {

  public final static String all = "all";
  public final static String fail = "fail";
  public final static String pass = "pass";

  @Option("Specify the fully-qualified name of a class under test.")
  public static List<String> testclass = new ArrayList<String>();

  @Option("Specify the name of a file that contains a list of classes under test. Each"
      + "class is specified by its fully qualified name on a separate line.")
      public static String classlist = null;

  @Option("Specify the name of a file that contains a list of methods under test. Each "
      + "method is specified on a separate line.")
      public static String methodlist = null;
  
  @Option("Tells Randoop what kinds of tests to output. Use \"" + fail + "\" to output only test that fail, \"" + pass + "\" to output only tests that pass (regressions), and \"" + all + "\" to output both kinds.")
  public static String output_tests = "all";

  @Option("Used to determine when to stop test generation. Generation stops when " +
      "either the time limit (--timelimit=int) OR the input limit (--inputlimit=int) is reached. " +
      "Note that the number of tests output may be smaller than then number of inputs " +
  "created, because redundant and illegal inputs may be discarded.")
  public static int inputlimit = 100000000;

  @Option ("Maximum number of tests to ouput.  Allows a more exact number than inputlimit")
  public static int outputlimit = 100000000;

  @Option("Used to determine when to stop test generation. Generation stops when " +
  "either the time limit (--timelimit=int) OR the input limit (--inputlimit=int) is reached.")
  public static int timelimit = 100;

  @Option("Maximum number of tests to write to each JUnit file.")
  public static int testsperfile = 500;

  @Option("Name of the JUnit file containing Randoop-generated tests.")
  public static String junit_classname = "RandoopTest";

  @Option("Name of the package that the generated JUnit files should have.")
  public static String junit_package_name = "";

  @Option("Name of the directory to which JUnit files should be written.")
  public static String junit_output_dir = null;

  @Option("The random seed to use in the generation process")
  public static int randomseed = (int) Randomness.SEED;

  @Option("Do not generate tests with more than <int> statements")
  public static int maxsize = 100;

  @Option("Forbid Randoop to use null as input to methods. IMPORTANT: even if "
      + "this option is set to true, null is only used if there is no non-null values "
      + "available.")
  public static boolean forbid_null = true;

  @Option("Use null with the given frequency. [TODO explain]")
  public static Double null_ratio = null;
  
  @Option("Causes Randoop to relay information about the program's execution over "
      + "a connection to the specified port on the local machine. Information is "
  		+ "sent using a serialized randoop.runtime.Message object. Printing is also "
      + "suppressed.")
  public static int comm_port = -1;
  
  @Unpublicized @Option("Use long format for outputting JUnit tests. The long format" +
  "emits exactly one line per statement, including primitive declarations, and" +
  "uses boxed primitives. This option is used in the branch-directed generation project.")
  public static boolean long_format = false; 

  @Unpublicized
  @Option("Has to do with experiments...")
  public static boolean size_equalizer = false;
  
  @Unpublicized
  @Option("Write experiment results file.")
  public static FileWriter expfile = null;

  @Unpublicized
  @Option("Works only with naive offline. ")
  public static Integer filter_short_dep = null;

  @Option("specifies initialization routine (class.method)")
  public static String init_routine = null;

  @Unpublicized
  @Option("specifies regex of classes that must be in any regression tests")
  public static Pattern test_classes = null;

  @Unpublicized
  @Option("File containing observer functions")
  public static File observers = null;

  @Unpublicized  
  @Option("Use only public classes/methods")
  public static boolean public_only = true;

  @Unpublicized  
  @Option("Install the given runtime visitor.")
  public static List<String> visitor = new ArrayList<String>();

  @Unpublicized  
  @Option("Capture all output to stdout and stderr")
  public static boolean capture_output = false;

  @Unpublicized  
  @Option("Remove tests that are subsumed in other tests")
  public static boolean remove_subsequences = true;

  @Unpublicized  
  @Option("Run each test twice and compare the checks")
  public static boolean compare_checks = false;

  @Unpublicized  
  @Option("Create clean checks for a serialized sequence")
  public static File clean_checks = null;

  @Unpublicized  
  @Option("Print any checks that are different in the clean run")
  public static boolean print_diff_obs = false;

  @Unpublicized  
  @Option("Specify agent command for recursive JVM calls")
  public static String agent = null;

  @Unpublicized  
  @Option("specify the memory size (in megabytes) for recursive JVM calls")
  public static int mem_megabytes = 1000;

  // We do this rather than using java -D so that we can easily pass these
  // to other JVMs
  @Unpublicized  
  @Option("-D Specify system properties to be set (similar to java -Dx=y)")
  public static List<String> system_props = new ArrayList<String>();

  @Unpublicized
  @Option("Output sequences that do not complete execution.")
  public static boolean output_nonexec = false;

  @Unpublicized
  @Option("Output coverage plot (percent cov. vs. secs.) to the given file.")
  public static String output_coverage_plot = null;

  @Unpublicized
  @Option("Use object cache.")
  public static boolean use_object_cache = false;

  @Unpublicized
  @Option("Aliasing factor.")
  public static Double alias_ratio = null;

  @Unpublicized
  @Option("Call checkRep methods when executing (for Randoop development).")
  public static boolean check_reps = false;

  @Unpublicized
  @Option("Use component-based generation.")
  public static boolean component_based = true;

  @Unpublicized
  @Option("Output witness sequences for coverage branches.")
  public static boolean output_cov_witnesses = false;

  @Unpublicized
  @Option("Check default set of object contracts, e.g. equals(Object) is reflexive, equals(null) returns false, etc.")
  public static boolean check_object_contracts = true;

  @Unpublicized
  @Option("Whenever an object is called for, use an integer.")
  public static boolean always_use_ints_as_objects = false;

  @Unpublicized
  @Option("Create helper sequences.")
  public static boolean helpers = false;

  @Unpublicized
  @Option("Name of a file containing a serialized list of sequences.")
  public static List<String> componentfile_ser = new ArrayList<String>();

  @Unpublicized
  @Option("Name of a file containing a textual list of sequences.")
  public static List<String> componentfile_txt = new ArrayList<String>();

  @Unpublicized
  @Option("Print to the given file source files annotated with coverage information.")
  public static String covreport = null;

  @Unpublicized
  @Option("Output components (serialized, GZIPPED) to the given file. Suggestion: use a .gz suffix in file name.")
  public static String output_components = null;

  @Unpublicized
  @Option("Output tests (sequences plus checkers) in serialized form to the given file. Suggestion: use a .gz suffix in file name.")
  public static String output_tests_serialized = null;

  @Unpublicized
  @Option("Output covered branches to the given text file.")
  public static String output_branches = null;

  @Unpublicized
  @Option("Output branch->witness-sequences map.")
  public static String output_covmap = null;

  @Unpublicized
  @Option("Output a SequenceGenerationStats object to the given file.")
  public static String output_stats = null;

  @Unpublicized
  @Option("The name of a file containing the list of coverage-instrumented classes.")
  public static String coverage_instrumented_classes = null;
 
  @Unpublicized
  @Option("Display progress every <int> seconds.")
  public static int progressinterval = 1;

  @Unpublicized
  @Option("Do not display progress.")
  public static boolean noprogressdisplay = false;

  @Unpublicized
  @Option("Minimize testclasses cases.")
  public static boolean minimize = true;

  @Unpublicized
  @Option("Create a file containing experiment results.")
  public static String experiment = null;

  @Unpublicized
  @Option("Do not do online redundancy checks.")
  public static boolean noredundancychecks = false;

  @Unpublicized
  @Option("Create sequences but never execute them.")
  public static boolean dontexecute = false;

  @Unpublicized
  @Option("Clear the component set when it reaches <int> inputs.")
  public static int clear = Integer.MAX_VALUE;

  @Unpublicized
  @Option("Do not do online illegal.")
  public static boolean offline = false;

  @Unpublicized
  @Option("Do not exercise methods that match regular expresssion <string>")
  public static Pattern omitmethods = null;

  @Unpublicized
  @Option("Generate inputs but do not check any contracts")
  public static boolean dont_check_contracts = false;

  @Unpublicized
  @Option("TODO document.")
  public static boolean weighted_inputs = false;

  @Unpublicized
  @Option("TODO document.")
  public static boolean no_args_statement_heuristic = true;

  @Unpublicized
  @Option("Use heuristic that may randomly repeat a method call several times.")
  public static boolean repeat_heuristic = false;

  @Unpublicized
  @Option("Run Randoop but do not create JUnit tests (used in research experiments).")
  public static boolean dont_output_tests = false;

  @Unpublicized
  @Option("Silently ignore any class names specified by the user that cannot be found by Randoop at runtime.")
  public static boolean silently_ignore_bad_class_names = false;
  
  @Option("Maximum length of strings allowed in assertions. "
      + "Note that String constants of length greater than 65KB "
      + "may be rejected, according to the Java Virtual Machine specification.")
  public static int assertion_string_maxlen = 10000;

  public static int string_maxlen = 10000;

  public static enum ClassLiteralsMode {
    NONE, CLASS, PACKAGE, ALL;
  }
  
  @Option("Specify how to use literal values given in a literals file (see --literals-file). " +
  "Set --literals-level=CLASS if you wish literals for a given class to be used as inputs to methods of only that class. " +
  "Set --literals-level=PACKAGE if you wish literals for a given class to be used as inputs to methods of any classes in the same package. " +
  "Set --literals-level=ALL if you wish literals for a given class to be used as inputs to any method under test." +
  "Set --literals-level=NONE if you wish not to use any literals specified in a literals file."
  )
  public static ClassLiteralsMode literals_level = ClassLiteralsMode.NONE;
  
  @Option("Specifies a file containing literal values to be used as inputs to methods under test. " +
      "These literals are used in addition to all other constants in the pool. " +
      "May be specified multiple times. " +
      "For the format of this file, see documentation in class randoop.LiteralFileReader. " +
      "The special value \"CLASSES\" (with no quotes) means to read literals from all classes under test.")
  public static List<String> literals_file = new ArrayList<String>(); 

  public GenInputsAbstract(String command, String pitch,
      String commandGrammar, String where, String summary, List<String> notes, String input,
      String output, String example, Options options) {
    super(command, pitch, commandGrammar, where, summary, notes, input, output,
        example, options);

    if (observers != null) {
      throw new RuntimeException("observers option not implemented.");
    }
    
    // Check consistency of arguments.

    if (!( output_tests.equals(all)
        || output_tests.equals(pass)
        || output_tests.equals(fail))) {

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
    
    if (use_object_cache) {
      if (!component_based) {
        throw new RuntimeException("Options use-object-cache requires option component-based.");
      }
    }
    if (alias_ratio != null) {
      if (!component_based) {
        throw new RuntimeException("Option alias-ratio requires option component-based.");
      }
      if (!forbid_null) {
        throw new RuntimeException("TODO alias-ratio doesn't play well with null factor.");
      }
      if (alias_ratio < 0 && alias_ratio > 1) {
        throw new RuntimeException("Alias ratio must be between 0 and 1.");
      }
    }

    if (null_ratio != null) {
      if (!component_based) {
        throw new RuntimeException("Option null-ratio requires option component-based.");
      }
      if (null_ratio < 0 && null_ratio > 1) {
        throw new RuntimeException("Null ratio must be between 0 and 1.");
      }
    }

    if (output_cov_witnesses) {
      if (!component_based) {
        throw new RuntimeException("Options output-cov-witnesses requires option component-based.");
      }
    }

    if (maxsize <= 0) {
      throw new RuntimeException("Maximum sequence size must be greater than zero but was " + maxsize);
    }

    if (!component_based) {
      if (output_components != null) {
        throw new RuntimeException("TODO output message");
      }
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
