package randoop.main;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import plume.Options;
import plume.Options.ArgException;
import plume.SimpleLog;
import randoop.generation.ComponentManager;
import randoop.DummyVisitor;
import randoop.ExecutionVisitor;
import randoop.JunitFileWriter;
import randoop.MultiVisitor;
import randoop.generation.RandoopListenerManager;
import randoop.generation.SeedSequences;
import randoop.contract.ObjectContract;
import randoop.instrument.ExercisedClassVisitor;
import randoop.operation.ConcreteOperation;
import randoop.operation.Operation;
import randoop.operation.OperationParseException;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationModel;
import randoop.reflection.PackageVisibilityPredicate;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.VisibilityPredicate;
import randoop.generation.AbstractGenerator;
import randoop.sequence.ExecutableSequence;
import randoop.generation.ForwardGenerator;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceExceptionError;
import randoop.test.ContractCheckingVisitor;
import randoop.test.ErrorTestPredicate;
import randoop.test.ExcludeTestPredicate;
import randoop.test.ExpectedExceptionCheckGen;
import randoop.test.ExtendGenerator;
import randoop.test.IncludeIfCoversPredicate;
import randoop.test.IncludeTestPredicate;
import randoop.test.RegressionCaptureVisitor;
import randoop.test.RegressionTestPredicate;
import randoop.test.TestCheckGenerator;
import randoop.test.ValidityCheckingVisitor;
import randoop.test.predicate.AlwaysFalseExceptionPredicate;
import randoop.test.predicate.ExceptionBehaviorPredicate;
import randoop.test.predicate.ExceptionPredicate;
import randoop.types.ConcreteType;
import randoop.util.CollectionsExt;
import randoop.util.Log;
import randoop.util.MultiMap;
import randoop.util.Randomness;
import randoop.util.ReflectionExecutor;
import randoop.util.predicate.AlwaysFalse;
import randoop.util.predicate.Predicate;

public class GenTests extends GenInputsAbstract {

  private static final String command = "gentests";

  private static final String pitch = "Generates unit tests for a set of classes.";

  private static final String commandGrammar = "gentests OPTIONS";

  private static final String where =
      "At least one class is specified via `--testclass' or `--classlist'.";

  private static final String summary =
      "Attempts to generate JUnit tests that "
          + "capture the behavior of the classes under test and/or find contract violations. "
          + "Randoop generates tests using feedback-directed random test generation. ";

  private static final String input =
      "One or more names of classes to test. A class to test can be specified "
          + "via the `--testclass=<CLASSNAME>' or `--classlist=<FILENAME>' options.";

  private static final String output =
      "A JUnit test suite (as one or more Java source files). The "
          + "tests in the suite will pass when executed using the classes under test.";

  private static final String example =
      "java randoop.main.Main gentests --testclass=java.util.Collections "
          + " --testclass=java.util.TreeSet";

  private static final List<String> notes;

  static {
    notes = new ArrayList<>();
    notes.add(
        "Randoop executes the code under test, with no mechanisms to protect your system from harm resulting from arbitrary code execution. If random execution of your code could have undesirable effects (e.g. deletion of files, opening network connections, etc.) make sure you execute Randoop in a sandbox machine.");
    notes.add(
        "Randoop will only use methods from the classes that you specify for testing. If Randoop is not generating tests for a particular method, make sure that you are including classes for the types that the method requires. Otherwise, Randoop may fail to generate tests due to missing input parameters.");
    notes.add(
        "Randoop is designed to be deterministic when the code under test is itself deterministic. This means that two runs of Randoop will generate the same tests. To get variation across runs, use the --randomseed option.");
  }

  public static SimpleLog progress = new SimpleLog(true);

  private static Options options =
      new Options(
          GenTests.class,
          GenInputsAbstract.class,
          ReflectionExecutor.class,
          ForwardGenerator.class,
          AbstractGenerator.class);

  public GenTests() {
    super(command, pitch, commandGrammar, where, summary, notes, input, output, example, options);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean handle(String[] args) throws RandoopTextuiException {

    try {
      String[] nonargs = options.parse(args);
      if (nonargs.length > 0)
        throw new ArgException("Unrecognized arguments: " + Arrays.toString(nonargs));
    } catch (ArgException ae) {
      usage("while parsing command-line arguments: %s", ae.getMessage());
    }

    checkOptionsValid();

    Randomness.reset(randomseed);

    java.security.Policy policy = java.security.Policy.getPolicy();

    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.printf("policy = %s%n", policy);
    }

    // If some properties were specified, set them
    for (String prop : GenInputsAbstract.system_props) {
      String[] pa = prop.split("=", 2);
      if (pa.length != 2) usage("invalid property definition: %s%n", prop);
      System.setProperty(pa[0], pa[1]);
    }

    // If an initializer method was specified, execute it
    executeInitializationRoutine(1);

    // Check that there are classes to test
    if (classlist == null && methodlist == null && testclass.size() == 0) {
      System.out.println("You must specify some classes or methods to test.");
      System.out.println("Use the --classlist, --testclass, or --methodlist options.");
      System.exit(1);
    }

    /*
     * Setup model of classes under test
     */

    // get names of classes under test
    Set<String> classnames = GenInputsAbstract.getClassnamesFromArgs();

    // get names of classes that must be covered by output tests
    Set<String> coveredClassnames =
        GenInputsAbstract.getStringSetFromFile(
            include_if_class_exercised, "Unable to read coverage class names");

    // get names of fields to be omitted
    Set<String> omitFields =
        GenInputsAbstract.getStringSetFromFile(omit_field_list, "Error reading field file");

    VisibilityPredicate visibility;
    Package junitPackage = Package.getPackage(GenInputsAbstract.junit_package_name);
    if (junitPackage == null || GenInputsAbstract.only_test_public_members) {
      visibility = new PublicVisibilityPredicate();
    } else {
      visibility = new PackageVisibilityPredicate(junitPackage);
    }

    ReflectionPredicate reflectionPredicate =
        new DefaultReflectionPredicate(omitmethods, omitFields, visibility);

    ClassNameErrorHandler classNameErrorHandler = new ThrowClassNameError();
    if (silently_ignore_bad_class_names) {
      classNameErrorHandler = new WarnOnBadClassName();
    }

    Set<String> methodSignatures =
            GenInputsAbstract.getStringSetFromFile(methodlist, "Error while reading method list file");

    OperationModel operationModel = null;
    try {
      operationModel=
          OperationModel.createModel(
              visibility,
              reflectionPredicate,
              classnames,
              coveredClassnames,
              methodSignatures,
              classNameErrorHandler,
              GenInputsAbstract.literals_file);
    } catch (OperationParseException e) {
      System.out.printf("Error: parse exception thrown %s%n", e);
      System.exit(1);
    }
    assert operationModel != null;

    if (operationModel.hasClasses()) {
      System.out.println("No classes to test");
      System.exit(1);
    }

    List<ConcreteOperation> model = operationModel.getConcreteOperations();

    if (model.size() == 0) {
      Log.out.println("There are no methods to test. Exiting.");
      System.exit(1);
    }
    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.println("PUBLIC MEMBERS=" + model.size());
    }

    /*
     * Initialize components:
     * - Add default seeds for primitive types
     * - Add any values for TestValue annotated static fields in operationModel
     */
    Set<Sequence> components = new LinkedHashSet<>();
    components.addAll(SeedSequences.defaultSeeds());
    components.addAll(operationModel.getAnnotatedTestValues());

    ComponentManager componentMgr = new ComponentManager(components);
    operationModel.addClassLiterals(
        componentMgr, GenInputsAbstract.literals_file, GenInputsAbstract.literals_level);

    RandoopListenerManager listenerMgr = new RandoopListenerManager();

    Set<String> observerSignatures = GenInputsAbstract.getStringSetFromFile(GenInputsAbstract.observers,"Unable to read observer file", "//.*", null);

    MultiMap<ConcreteType,ConcreteOperation> observerMap = null;
    try {
      observerMap = operationModel.getObservers(observerSignatures);
    } catch (OperationParseException e) {
      System.out.printf("Error: parse exception thrown while reading observers: %s%n", e);
      System.exit(1);
    }
    assert observerMap != null;
    Set<ConcreteOperation> observers = new LinkedHashSet<>();
    for (ConcreteType keyType : observerMap.keySet()) {
      observers.addAll(observerMap.getValues(keyType));
    }

    /*
     * Create the generator for this session.
     */
    AbstractGenerator explorer;
    explorer =
        new ForwardGenerator(
            model, observers, timelimit * 1000, inputlimit, outputlimit, componentMgr, listenerMgr);

    /*
     * setup for check generation
     */
    Set<ObjectContract> contracts = operationModel.getContracts();


    Set<ConcreteOperation> excludeAsObservers = new LinkedHashSet<>();
    // TODO add Object.toString() and Object.hashCode() to exclude set
    TestCheckGenerator testGen = createTestCheckGenerator(visibility, contracts, observerMap, excludeAsObservers);

    explorer.addTestCheckGenerator(testGen);

    /*
     * Setup for test predicate
     */
    // Always exclude a singleton sequence with just new Object()
    ConcreteOperation objectConstructor = null;
    try {
      objectConstructor = operationModel.getOperation(Object.class.getConstructor());
    } catch (NoSuchMethodException e) {
      assert false : "failed to get Object constructor: " + e;
    }
    assert objectConstructor != null;

    Sequence newObj = new Sequence().extend(objectConstructor);
    Set<Sequence> excludeSet = new LinkedHashSet<>();
    excludeSet.add(newObj);

    // Define test predicate to decide which test sequences will be output
    Predicate<ExecutableSequence> isOutputTest =
        createTestOutputPredicate(
            excludeSet,
            operationModel.getExercisedClasses(),
            GenInputsAbstract.include_if_classname_appears);

    explorer.addTestPredicate(isOutputTest);

    /*
     * Setup visitors
     */
    // list of visitors for collecting information from test sequences
    List<ExecutionVisitor> visitors = new ArrayList<>();

    // instrumentation visitor
    if (GenInputsAbstract.include_if_class_exercised != null) {
      visitors.add(new ExercisedClassVisitor(operationModel.getExercisedClasses()));
    }

    // Install any user-specified visitors.
    if (!GenInputsAbstract.visitor.isEmpty()) {
      for (String visitorClsName : GenInputsAbstract.visitor) {
        try {
          Class<ExecutionVisitor> cls = (Class<ExecutionVisitor>) Class.forName(visitorClsName);
          ExecutionVisitor vis = cls.newInstance();
          visitors.add(vis);
        } catch (Exception e) {
          System.out.println("Error while loading visitor class " + visitorClsName);
          System.out.println("Exception message: " + e.getMessage());
          System.out.println("Stack trace:");
          e.printStackTrace(System.out);
          System.out.println("Randoop will exit with code 1.");
          System.exit(1);
        }
      }
    }

    ExecutionVisitor visitor;
    if (visitors.isEmpty()) {
      visitor = new DummyVisitor();
    } else {
      visitor = new MultiVisitor(visitors);
    }

    explorer.addExecutionVisitor(visitor);

    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.printf("Explorer = %s\n", explorer);
    }

    /* Generate tests */
    try {
      explorer.explore();
    } catch (SequenceExceptionError e) {

      handleFlakySequenceException(explorer, e);

      System.exit(1);
    }

    /* post generation */
    if (GenInputsAbstract.dont_output_tests) return true;

    if (!GenInputsAbstract.no_error_revealing_tests) {
      List<ExecutableSequence> errorSequences = explorer.getErrorTestSequences();
      if (errorSequences.size() > 0) {
        if (!GenInputsAbstract.noprogressdisplay) {
          System.out.printf("%nError-revealing test output:%n");
          System.out.printf("Error-revealing test count: %d%n", errorSequences.size());
        }
        outputTests(errorSequences, GenInputsAbstract.error_test_basename);
      } else {
        if (!GenInputsAbstract.noprogressdisplay) {
          System.out.printf("%nNo error-revealing tests to output%n");
        }
      }
    }

    if (!GenInputsAbstract.no_regression_tests) {
      List<ExecutableSequence> regressionSequences = explorer.getRegressionSequences();
      if (regressionSequences.size() > 0) {
        if (!GenInputsAbstract.noprogressdisplay) {
          System.out.printf("%nRegression test output:%n");
          System.out.printf("Regression test count: %d%n", regressionSequences.size());
        }
        outputTests(regressionSequences, GenInputsAbstract.regression_test_basename);
      } else {
        if (!GenInputsAbstract.noprogressdisplay) {
          System.out.printf("No regression tests to output%n");
        }
      }
    }

    return true;
  }

  /**
   * Handles the occurrence of a {@code SequenceExceptionError} that indicates a
   * flaky test has been found. Prints information to help user identify source
   * of flakiness, including exception, statement that threw the exception, the
   * full sequence where exception was thrown, and the input subsequence.
   *
   * @param explorer
   *          the test generator
   * @param e
   *          the sequence exception
   */
  private void handleFlakySequenceException(AbstractGenerator explorer, SequenceExceptionError e) {

    String msg =
        String.format(
            "%n%nERROR: Randoop stopped because of a flaky test.%n%n"
                + "This can happen when Randoop is run on methods that side-effect global "
                + "state.%n"
                + "See the \"Randoop stopped because of a flaky test\" "
                + "section of the user manual.%n"
                + "For more details, rerun with logging turned on with --log=FILENAME.%n");
    System.out.printf(msg);

    Sequence subsequence = e.getSubsequence();

    if (Log.isLoggingOn()) {
      Log.log(msg);
      Log.log(String.format("%nException:%n  %s%n", e.getError()));
      Log.log(String.format("Statement:%n  %s%n", e.getStatement()));
      Log.log(String.format("Full sequence:%n%s%n", e.getSequence()));
      Log.log(String.format("Input subsequence:%n%s%n", subsequence.toCodeString()));

      Set<String> callSet = new TreeSet<>();

      Iterator<Sequence> s_i = explorer.getAllSequences().iterator();
      if (s_i.hasNext()) {
        Sequence s = s_i.next();
        while (!subsequence.equals(s) && s_i.hasNext()) {
          s = s_i.next();
        }
        while (s_i.hasNext()) {
          s = s_i.next();
          for (int i = 0; i < s.statements.size(); i++) {
            Operation operation = s.statements.get(i).getOperation();
            if (!operation.isNonreceivingValue()) {
              callSet.add(operation.toString());
            }
          }
        }
      }

      if (!callSet.isEmpty()) {
        Log.logLine("Operations performed since subsequence first executed:");
        for (String opName : callSet) {
          Log.logLine(opName);
        }
      } else {
        System.out.printf(
            "Unable to find a previous occurrence of subsequence%n"
                + "%s%n"
                + "where exception was thrown%n"
                + "Please submit an issue%n",
            subsequence);
      }
    }
  }

  /**
   * Builds the test predicate that determines whether a particular sequence
   * will be included in the output based on command-line arguments.
   *
   * @param excludeSet
   *          the set of sequences to exclude
   * @param coveredClasses
   *          the list of classes to test for coverage
   * @param includePattern
   *          the pattern for method name inclusion
   * @return the predicate
   */
  public Predicate<ExecutableSequence> createTestOutputPredicate(
      Set<Sequence> excludeSet, Set<Class<?>> coveredClasses, Pattern includePattern) {
    Predicate<ExecutableSequence> isOutputTest;
    if (GenInputsAbstract.dont_output_tests) {
      isOutputTest = new AlwaysFalse<>();
    } else {
      Predicate<ExecutableSequence> baseTest;
      // base case: exclude sequences in excludeSet, keep everything else
      // to exclude something else, add sequence to excludeSet
      baseTest = new ExcludeTestPredicate(excludeSet);
      if (includePattern != null) {
        baseTest = baseTest.and(new IncludeTestPredicate(includePattern));
      }
      if (!coveredClasses.isEmpty()) {
        baseTest = baseTest.and(new IncludeIfCoversPredicate(coveredClasses));
      }

      // Use arguments to determine which kinds of tests to output
      // Default is neither (e.g., no tests output)
      Predicate<ExecutableSequence> checkTest = new AlwaysFalse<>();

      // But, generate error-revealing tests if user says so
      if (!GenInputsAbstract.no_error_revealing_tests) {
        checkTest = new ErrorTestPredicate();
      }

      // And, generate regression tests, unless user says not to
      if (!GenInputsAbstract.no_regression_tests) {
        checkTest = checkTest.or(new RegressionTestPredicate());
      }
      isOutputTest = baseTest.and(checkTest);
    }
    return isOutputTest;
  }

  /**
   * Outputs JUnit tests for the sequence list.
   *
   * @param sequences
   *          the sequences to output
   * @param junitPrefix
   *          the filename prefix for test output
   */
  private void outputTests(List<ExecutableSequence> sequences, String junitPrefix) {
    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.printf("Writing JUnit tests...%n");
    }
    writeJUnitTests(junit_output_dir, sequences, null, junitPrefix);
  }

  /**
   * Creates the test check generator for this run based on the command-line
   * arguments. The goal of the generator is to produce all appropriate checks
   * for each sequence it is applied to. Validity and contract checks are always
   * needed to determine which sequences have invalid or error behaviors, even
   * if only regression tests are desired. So, this generator will always be
   * built. If in addition regression tests are to be generated, then the
   * regression checks generator is added.
   *
   * @param visibility
   *          the visibility predicate
   * @param contracts
   *          the contract checks
   * @param observerMap
   *          the map from types to observer methods
   * @param excludeAsObservers
   *          methods to exclude when generating observer map
   * @return the {@code TestCheckGenerator} that reflects command line
   *         arguments.
   */
  public TestCheckGenerator createTestCheckGenerator(
          VisibilityPredicate visibility, Set<ObjectContract> contracts, MultiMap<ConcreteType, ConcreteOperation> observerMap, Set<ConcreteOperation> excludeAsObservers) {

    // start with checking for invalid exceptions
    ExceptionPredicate isInvalid = new ExceptionBehaviorPredicate(BehaviorType.INVALID);
    TestCheckGenerator testGen =
        new ValidityCheckingVisitor(isInvalid, !GenInputsAbstract.ignore_flaky_tests);

    // extend with contract checker
    ExceptionPredicate isError = new ExceptionBehaviorPredicate(BehaviorType.ERROR);
    ContractCheckingVisitor contractVisitor = new ContractCheckingVisitor(contracts, isError);
    testGen = new ExtendGenerator(testGen, contractVisitor);

    // and, generate regression tests, unless user says not to
    if (!GenInputsAbstract.no_regression_tests) {
      ExceptionPredicate isExpected = new AlwaysFalseExceptionPredicate();
      boolean includeAssertions = true;
      if (GenInputsAbstract.no_regression_assertions) {
        includeAssertions = false;
      } else {
        isExpected = new ExceptionBehaviorPredicate(BehaviorType.EXPECTED);
      }
      ExpectedExceptionCheckGen expectation;
      expectation = new ExpectedExceptionCheckGen(visibility, isExpected);

      RegressionCaptureVisitor regressionVisitor;
      regressionVisitor = new RegressionCaptureVisitor(expectation, observerMap, excludeAsObservers, includeAssertions);

      testGen = new ExtendGenerator(testGen, regressionVisitor);
    }
    return testGen;
  }

  /**
   * Writes the sequences as JUnit files to the specified directory.
   *
   * @param output_dir
   *          string name of output directory.
   * @param seqList
   *          a list of sequences to write.
   * @param additionalJUnitClasses
   *          other classes to write (may be null).
   * @param junitClassname
   *          the base name for the class
   * @return list of files written.
   **/
  private static List<File> writeJUnitTests(
          String output_dir,
          List<ExecutableSequence> seqList,
          List<String> additionalJUnitClasses,
          String junitClassname) {

    List<File> files = new ArrayList<>();

    if (seqList.size() > 0) {
      List<List<ExecutableSequence>> seqPartition =
          CollectionsExt.<ExecutableSequence>chunkUp(new ArrayList<>(seqList), testsperfile);

      JunitFileWriter jfw = new JunitFileWriter(output_dir, junit_package_name, junitClassname);

      files.addAll(jfw.writeJUnitTestFiles(seqPartition));

      if (GenInputsAbstract.junit_reflection_allowed) {
        files.add(jfw.writeSuiteFile(additionalJUnitClasses));
      } else {
        files.add(jfw.writeDriverFile());
      }
    } else { // preserves behavior from previous version
      System.out.println("No tests were created. No JUnit class created.");
    }

    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.println();
    }

    for (File f : files) {
      if (!GenInputsAbstract.noprogressdisplay) {
        System.out.println("Created file: " + f.getAbsolutePath());
      }
    }
    return files;
  }

  /**
   * Execute the initialization routine (if user specified one)
   *
   * @param phase
   *          the phase number passed to initialization routine
   */
  private static void executeInitializationRoutine(int phase) {

    if (GenInputsAbstract.init_routine == null) return;

    String full_name = GenInputsAbstract.init_routine;
    int lastdot = full_name.lastIndexOf(".");
    if (lastdot == -1) usage("invalid init routine: %s\n", full_name);
    String classname = full_name.substring(0, lastdot);
    String methodname = full_name.substring(lastdot + 1);
    methodname = methodname.replaceFirst("[()]*$", "");
    System.out.printf("%s - %s\n", classname, methodname);
    Class<?> iclass = null;
    try {
      iclass = Class.forName(classname);
    } catch (Exception e) {
      usage("Can't load init class %s: %s", classname, e.getMessage());
    }
    assert iclass != null;

    Method imethod = null;
    try {
      imethod = iclass.getDeclaredMethod(methodname, int.class);
    } catch (Exception e) {
      usage("Can't find init method %s: %s", methodname, e);
    }

    assert imethod != null;
    if (!Modifier.isStatic(imethod.getModifiers()))
      usage("init method %s.%s must be static", classname, methodname);
    try {
      imethod.invoke(null, phase);
    } catch (Exception e) {
      usage(e, "problem executing init method %s.%s: %s", classname, methodname, e);
    }
  }

  /** Print out usage error and stack trace and then exit **/
  private static void usage(Throwable t, String format, Object... args) {

    System.out.print("ERROR: ");
    System.out.printf(format, args);
    System.out.println();
    System.out.println(options.usage());
    if (t != null) t.printStackTrace();
    System.exit(-1);
  }

  private static void usage(String format, Object... args) {
    usage(null, format, args);
  }
}
