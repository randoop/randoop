package randoop.main;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import randoop.SequenceCollection;
import randoop.util.Randomness;
import randoop.util.Reflection;
import randoop.util.Util;
import utilpag.Invisible;
import utilpag.Option;
import utilpag.Options;

public abstract class GenInputsAbstract extends CommandHandler {

  public final static String all = "all";
  public final static String fail = "fail";
  public final static String pass = "pass";

  @Invisible
  @Option("Has to do with experiments...")
  public static boolean size_equalizer = false;


  @Invisible
  @Option("Write experiment results file.")
  public static FileWriter expfile = null;

  @Invisible
  @Option("Works only with naive offline. ")
  public static Integer filter_short_dep = null;

  @Option("specifies initialization routine (class.method)")
  public static String init_routine = null;

  @Option("specifies regex of classes that must be in any regression tests")
  public static Pattern test_classes = null;

  @Option("File containing observer functions")
  public static File observers = null;

  @Option("Install the given runtime visitor.")
  public static List<String> visitor = new ArrayList<String>();

  @Option("Capture all output to stdout and stderr")
  public static boolean capture_output = false;

  @Option("Run each test twice and compare the observations")
  public static boolean compare_observations = false;

  @Option("Create clean observations for a serialized sequence")
  public static File clean_observations = null;

  @Option("Specify agent command for recursive JVM calls")
  public static String agent = null;

  // We do this rather than using java -D so that we can easily pass these
  // to other JVMs
  @Option("-D Specify system properties to be set (similar to java -Dx=y)")
  public static List<String> system_props = new ArrayList<String>();

  @Invisible
  @Option("Output sequences that do not complete execution.")
  public static boolean output_nonexec = false;

  @Invisible
  @Option("Output only contract-violating sequences.")
  public static String output_tests = "all";

  @Invisible
  @Option("Don't generate anything, just count the state space.")
  public static Integer calc_sequence_space = null;

  @Invisible
  @Option("Don't generate anything, just count the state space.")
  public static String output_sequence_space = null;

  @Invisible
  @Option("Output coverage plot (percent cov. vs. secs.) to the given file.")
  public static String output_coverage_plot = null;

  @Invisible
  @Option("Use object cache.")
  public static boolean use_object_cache = false;

  @Invisible
  @Option("Aliasing factor.")
  public static Double alias_ratio = null;

  @Invisible
  @Option("Call checkRep methods when executing (for Randoop development).")
  public static boolean check_reps = false;

  @Invisible
  @Option("Use component-based generation.")
  public static boolean component_based = true;

  @Invisible
  @Option("Output witness sequences for coverage branches.")
  public static boolean output_cov_witnesses = false;

  @Invisible
  @Option("Check java.lang.Object contracts, e.g. equals(Object) is reflexive, hashCode() throws no exceptions, etc.")
  public static boolean check_object_contracts = true;

  @Invisible
  @Option("Whenever an object is called for, use an integer.")
  public static boolean always_use_ints_as_objects = false;


  @Invisible
  @Option("Create helper sequences.")
  public static boolean helpers = false;

  @Invisible
  @Option("Name of a file containing a serialized list of sequences.")
  public static List<String> componentfile_ser = new ArrayList<String>();

  @Invisible
  @Option("Name of a file containing a textual list of sequences.")
  public static List<String> componentfile_txt = new ArrayList<String>();

  // Set in main method. Component sequences to help bdgen.
  public static SequenceCollection components;

  @Invisible
  @Option("Print to the given file source files annotated with coverage information.")
  public static String covreport = null;

  @Invisible
  @Option("Output components (serialized, GZIPPED) to the given file. Suggestion: use a .gz suffix in file name.")
  public static String output_components = null;

  @Invisible
  @Option("Output covered branches to the given text file.")
  public static String output_branches = null;

  @Invisible
  @Option("Output branch->witness-sequences map.")
  public static String output_covmap = null;

  @Invisible
  @Option("Output a SequenceGenerationStats object to the given file.")
  public static String output_stats = null;

  @Invisible
  @Option("The name of a file containing the list of coverage-instrumented classes.")
  public static String coverage_instrumented_classes = null;

  @Option("Specify the fully-qualified name of a class under test.")
  public static List<String> testclass = new ArrayList<String>();

  @Option("Specify the name of a file that contains a list of classes under test. Each"
      + "class is specified by its fully qualified name on a separate line.")
      public static String classlist = null;

  @Option("Specify the name of a file that contains a list of methods under test. Each"
      + "method is specified on a separate line.")
      public static String methodlist = null;

  @Option("Used to determine when to stop test generation. Generation stops when " +
      "either the time limit (--timelimit=int) OR the input limit (--inputlimit=int) is reached. " +
      "Note that the number of tests output may be smaller than then number of inputs " +
  "created, because redundant and illegal inputs may be discarded.")
  public static int inputlimit = 100000000;

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

  @Invisible
  @Option("Display progress every <int> seconds.")
  public static int progressinterval = 1;

  @Invisible
  @Option("Do not display progress.")
  public static boolean noprogressdisplay = false;

  @Invisible
  @Option("Minimize testclasses cases.")
  public static boolean minimize = true;

  @Invisible
  @Option("Create a file containing experiment results.")
  public static String experiment = null;

  @Invisible
  @Option("Do not do online redundancy checks.")
  public static boolean noredundancychecks = false;

  @Invisible
  @Option("Create sequences but never execute them.")
  public static boolean dontexecute = false;

  @Invisible
  @Option("Clear the component set when it reaches <int> inputs.")
  public static int clear = Integer.MAX_VALUE;

  @Invisible
  @Option("Do not do online illegal.")
  public static boolean offline = false;

  @Invisible
  @Option("Do not exercise methods that match regular expresssion <string>")
  public static Pattern omitmethods = null;

  @Invisible
  @Option("Generate inputs but do not check any contracts")
  public static boolean dont_check_contracts = false;

  @Invisible
  @Option("TODO document.")
  public static boolean weighted_inputs = false;

  @Invisible
  @Option("TODO document.")
  public static boolean no_args_statement_heuristic = true;

  @Invisible
  @Option("Only generate inputs, do not test for errors.")
  public static boolean dontcheckcontracts;

  @Invisible
  @Option("Use heuristic that may randomly repeat a method call several times.")
  public static boolean repeat_heuristic = false;

  @Option("Run Randoop but do not create JUnit tests (used in research experiments).")
  public static boolean dont_output_tests = false;


  public GenInputsAbstract(String command, String pitch,
      String commandGrammar, String where, String summary, List<String> notes, String input,
      String output, String example, Options options) {
    super(command, pitch, commandGrammar, where, summary, notes, input, output,
        example, options);

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

    if (calc_sequence_space != null) {

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
        classes.addAll(Reflection.loadClassesFromFile(classListingFile));
      }
      classes.addAll(Reflection.loadClassesFromList(testclass));
    } catch (Exception e) {
      String msg = Util.toNColsStr("ERROR while reading list of classes to test: " + e.getMessage(), 70);
      System.out.println(msg);
      System.exit(1);
    }
    return classes;
  }
}
