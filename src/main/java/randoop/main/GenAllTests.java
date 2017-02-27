package randoop.main;

import plume.EntryReader;
import plume.Options;
import plume.Options.ArgException;
import plume.SimpleLog;
import randoop.*;
import randoop.generation.*;
import randoop.generation.exhaustive.SequenceGenerator;
import randoop.instrument.ExercisedClassVisitor;
import randoop.operation.Operation;
import randoop.operation.OperationParseException;
import randoop.operation.TypedOperation;
import randoop.reflection.*;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceExceptionError;
import randoop.test.*;
import randoop.test.predicate.AlwaysFalseExceptionPredicate;
import randoop.test.predicate.ExceptionBehaviorPredicate;
import randoop.test.predicate.ExceptionPredicate;
import randoop.types.Type;
import randoop.util.*;
import randoop.util.predicate.AlwaysFalse;
import randoop.util.predicate.Predicate;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

public class GenAllTests extends GenInputsAbstract {

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
          GenAllTests.class,
          GenInputsAbstract.class,
          ReflectionExecutor.class,
          ForwardExhaustiveGenerator.class,
          AbstractGenerator.class);

  private static JunitFileWriter jfw;
  private static SequencesFileWriter sfw;
  private AbstractGenerator explorer;
  private int errorSubsequenceStartIndex = 0;
  private int errorSubsequenceEndIndex = testsperfile;
  private int regressionSubsequenceStartIndex = 0;
  private int regressionSubsequenceEndIndex = testsperfile;

  private AbstractGenerator getExplorer() {
    return explorer;
  }

  public GenAllTests() {
    super(command, pitch, commandGrammar, where, summary, notes, input, output, example, options);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean handle(String[] args) throws RandoopTextuiException {

    try {
      String[] nonargs = options.parse(args);
      if (nonargs.length > 0) {
        throw new ArgException("Unrecognized arguments: " + Arrays.toString(nonargs));
      }
    } catch (ArgException ae) {
      usage("while parsing command-line arguments: %s", ae.getMessage());
    }

    checkOptionsValid();

    errorSubsequenceEndIndex = regressionSubsequenceEndIndex = testsperfile;
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

    // Check that there are classes to test
    if (classlist == null && methodlist == null && testclass.isEmpty()) {
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
        new DefaultReflectionPredicate(omitmethods, omitFields);

    ClassNameErrorHandler classNameErrorHandler = new ThrowClassNameError();
    if (silently_ignore_bad_class_names) {
      classNameErrorHandler = new WarnOnBadClassName();
    }

    Set<String> methodSignatures =
        GenInputsAbstract.getStringSetFromFile(methodlist, "Error while reading method list file");

    OperationModel operationModel = null;
    try {
      operationModel =
          OperationModel.createModel(
              visibility,
              reflectionPredicate,
              classnames,
              coveredClassnames,
              methodSignatures,
              classNameErrorHandler,
              GenInputsAbstract.literals_file);
    } catch (OperationParseException e) {
      System.out.printf("%nError: parse exception thrown %s%n", e);
      System.out.println("Exiting Randoop.");
      System.exit(1);
    } catch (NoSuchMethodException e) {
      System.out.printf("%nError building operation model: %s%n", e);
      System.out.println("Exiting Randoop.");
      System.exit(1);
    } catch (RandoopClassNameError e) {
      System.out.printf("Error: %s%n", e.getMessage());
      System.out.println(
          "       This is most likely a problem with the classpath. It may be wrong, or");
      System.out.println(
          "       it is formatted incorrectly on the command line. The other possibility");
      System.out.println("       is that the wrong class name is given.");
      System.out.println("Exiting Randoop.");
      System.exit(1);
    }
    assert operationModel != null;

    if (!operationModel.hasClasses()) {
      System.out.println("No classes to test");
      System.exit(1);
    }

    List<TypedOperation> model = operationModel.getOperations();

    if (model.isEmpty()) {
      Log.out.println("There are no methods to test. Exiting.");
      System.exit(1);
    }

    methods_count = model.stream().filter(to -> to.getOperation().isMethodCall()).count();
    execution_start = new Date();
    num_sequences_to_be_examined =
        SequenceGenerator.getExpectedNumberOfSequences(methods_count, maxsize);

    if (!GenInputsAbstract.noprogressdisplay) {

      System.out.println("PUBLIC MEMBERS=" + model.size());
      System.out.println("\tCONSTRUCTORS=" + (model.size() - methods_count));
      System.out.println("\tMETHODS=" + methods_count);
      System.out.println("Number of sequences to be examined: " + num_sequences_to_be_examined);
    }

    /*
     * Initialize components:
     * <ul>
     *   <li>Add default seeds for primitive types
     *   <li>Add any values for TestValue annotated static fields in operationModel
     * </ul>
     */
    Set<Sequence> components = new LinkedHashSet<>();
    components.addAll(SeedSequences.defaultSeeds());
    components.addAll(operationModel.getAnnotatedTestValues());

    ComponentManager componentMgr = new ComponentManager(components);
    operationModel.addClassLiterals(
        componentMgr, GenInputsAbstract.literals_file, GenInputsAbstract.literals_level);

    RandoopListenerManager listenerMgr = new RandoopListenerManager();

    listenerMgr.addListener(
        new IEventListener() {
          @Override
          public void explorationStart() {}

          @Override
          public void explorationEnd() {}

          @Override
          public void generationStepPre() {
            // Vê se está na hora de imprimir os testes!
            if (getExplorer() != null) {
              int currentSize = getExplorer().getRegressionSequences().size();
              if (currentSize > 0 && currentSize % testsperfile == 0) {
                outputResult(getExplorer());
              }
            }
          }

          @Override
          public void generationStepPost(ExecutableSequence s) {}

          @Override
          public void progressThreadUpdate() {}

          @Override
          public boolean stopGeneration() {
            return false;
          }
        });

    Set<String> observerSignatures =
        GenInputsAbstract.getStringSetFromFile(
            GenInputsAbstract.observers, "Unable to read observer file", "//.*", null);

    MultiMap<Type, TypedOperation> observerMap = null;
    try {
      observerMap = operationModel.getObservers(observerSignatures);
    } catch (OperationParseException e) {
      System.out.printf("Error reading observers: %s%n", e);
      System.exit(1);
    }
    assert observerMap != null;
    Set<TypedOperation> observers = new LinkedHashSet<>();
    for (Type keyType : observerMap.keySet()) {
      observers.addAll(observerMap.getValues(keyType));
    }

    /*
     * Create the generator for this session.
     */
    explorer =
        new ForwardExhaustiveGenerator(
            model, observers, timelimit * 1000, inputlimit, outputlimit, componentMgr, listenerMgr);

    /*
     * setup for check generation
     */
    ContractSet contracts = operationModel.getContracts();

    Set<TypedOperation> excludeAsObservers = new LinkedHashSet<>();
    // Add Object.toString() and Object.hashCode() to exclude set
    Method toStringMethod = null;
    Method hashCodeMethod = null;
    try {
      toStringMethod = Object.class.getMethod("toString", Object.class);
      hashCodeMethod = Object.class.getMethod("hashCode", Object.class);
    } catch (NoSuchMethodException e) {
      // Ignore
    }

    if (toStringMethod != null) {
      excludeAsObservers.add(TypedOperation.forMethod(toStringMethod));
    }

    if (hashCodeMethod != null) {
      excludeAsObservers.add(TypedOperation.forMethod(hashCodeMethod));
    }

    TestCheckGenerator testGen =
        createTestCheckGenerator(visibility, contracts, observerMap, excludeAsObservers);

    explorer.addTestCheckGenerator(testGen);

    /*
     * Setup for test predicate
     */
    // Always exclude a singleton sequence with just new Object()
    TypedOperation objectConstructor = null;
    try {
      objectConstructor = TypedOperation.forConstructor(Object.class.getConstructor());
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
      System.out.print(e);
      System.exit(1);
    }

    /* post generation */
    if (GenInputsAbstract.dont_output_tests) {
      return true;
    }

    outputResult(explorer, false);

    return true;
  }

  private void outputResult(AbstractGenerator explorer) {
    outputResult(explorer, true);
  }

  private void outputResult(AbstractGenerator explorer, boolean isOutputDuringExecution) {

    System.out.println(
        " steps = "
            + explorer.num_steps
            + " sequences to examine = "
            + getNumSequencesToBeExamined()
            + " started at "
            + "("
            + GenInputsAbstract.getExecutionStart()
            + ")");

    if (!GenInputsAbstract.no_error_revealing_tests) {
      List<ExecutableSequence> errorSequences = explorer.getErrorTestSequences();
      if (!errorSequences.isEmpty()) {
        if (isOutputDuringExecution) {
          if (errorSequences.size() >= errorSubsequenceEndIndex) {
            List<ExecutableSequence> toPrint =
                errorSequences.subList(errorSubsequenceStartIndex, errorSubsequenceEndIndex);
            error_seqs_count = error_seqs_count.add(BigInteger.valueOf(toPrint.size()));

            if (!GenInputsAbstract.noprogressdisplay) {
              System.out.printf("%nError-revealing test output:%n");
              System.out.printf("Error-revealing test count: %d%n", error_seqs_count);
            }
            outputTests(toPrint, GenInputsAbstract.error_test_basename);

            explorer.removeErrorSequences(toPrint);
          }
        } else {
          List<ExecutableSequence> toPrint =
              errorSequences.subList(errorSubsequenceStartIndex, errorSequences.size());
          error_seqs_count = error_seqs_count.add(BigInteger.valueOf(toPrint.size()));
          outputTests(toPrint, GenInputsAbstract.error_test_basename);
          explorer.removeErrorSequences(toPrint);
        }
      } else {
        if (!GenInputsAbstract.noprogressdisplay) {
          System.out.printf("%nNo error-revealing tests to output%n");
        }
      }
    }

    if (!GenInputsAbstract.no_regression_tests) {
      List<ExecutableSequence> regressionSequences = explorer.getRegressionSequences();
      if (!regressionSequences.isEmpty()) {
        if (isOutputDuringExecution) {
          if (regressionSequences.size() >= regressionSubsequenceEndIndex) {
            List<ExecutableSequence> dumpedSequences =
                regressionSequences.subList(
                    regressionSubsequenceStartIndex, regressionSubsequenceEndIndex);
            regression_seqs_count =
                regression_seqs_count.add(BigInteger.valueOf(dumpedSequences.size()));
            outputTests(dumpedSequences, GenInputsAbstract.regression_test_basename);
            explorer.removeRegressionSequences(dumpedSequences);
            if (!GenInputsAbstract.noprogressdisplay) {
              System.out.printf("%nRegression test output:%n");
              System.out.printf("Regression test count: %d%n", regression_seqs_count);
            }
          }
        } else {
          if (regressionSequences.size() >= regressionSubsequenceStartIndex) {
            List<ExecutableSequence> dumpedSequences =
                regressionSequences.subList(
                    regressionSubsequenceStartIndex, regressionSequences.size());
            regression_seqs_count =
                regression_seqs_count.add(BigInteger.valueOf(dumpedSequences.size()));
            outputTests(dumpedSequences, GenInputsAbstract.regression_test_basename);
            explorer.removeRegressionSequences(dumpedSequences);
            if (!GenInputsAbstract.noprogressdisplay) {
              System.out.printf("%nRegression test output:%n");
              System.out.printf("Regression test count: %d%n", regression_seqs_count);
            }
          }
        }
      } else {
        if (!GenInputsAbstract.noprogressdisplay) {
          System.out.printf("No regression tests to output%n");
        }
      }
    }

    File testsFolder = new File(junit_output_dir);
    File file = new File(testsFolder, "current_seq_index.tar.gz");
    explorer.saveCurrentGenerationStep(file);
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
    writeSequencesToFile(junit_output_dir, sequences);

    if (!output_only_sequences) {
      writeJUnitTests(junit_output_dir, sequences, junitPrefix);
    }
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
      VisibilityPredicate visibility,
      ContractSet contracts,
      MultiMap<Type, TypedOperation> observerMap,
      Set<TypedOperation> excludeAsObservers) {

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
      regressionVisitor =
          new RegressionCaptureVisitor(
              expectation, observerMap, excludeAsObservers, includeAssertions);

      testGen = new ExtendGenerator(testGen, regressionVisitor);
    }
    return testGen;
  }

  private static void writeSequencesToFile(String output_dir, List<ExecutableSequence> seqList) {
    if (seqList != null && !seqList.isEmpty()) {
      if (sfw == null) {
        sfw = new SequencesFileWriter(output_dir);
      }
      sfw.writeSequences(seqList);
    } else {
      System.out.println("No sequences to be written.");
    }
  }
  /**
   * Writes the sequences as JUnit files to the specified directory.
   *
   * @param output_dir
   *          string name of output directory
   * @param seqList
   *          a list of sequences to write
   * @param junitClassname
   *          the base name for the class
   * @return list of files written
   **/
  private static List<File> writeJUnitTests(
      String output_dir, List<ExecutableSequence> seqList, String junitClassname) {

    List<File> files = new ArrayList<>();

    if (!seqList.isEmpty()) {
      List<List<ExecutableSequence>> seqPartition =
          CollectionsExt.formSublists(new ArrayList<>(seqList), testsperfile);

      if (jfw == null) {
        jfw = new JunitFileWriter(output_dir, junit_package_name, junitClassname);
      }

      List<String> beforeAllText = getFileText(GenInputsAbstract.junit_before_all);
      if (beforeAllText != null) {
        jfw.addBeforeAll(beforeAllText);
      }

      List<String> afterAllText = getFileText(GenInputsAbstract.junit_after_all);
      if (afterAllText != null) {
        jfw.addAfterAll(afterAllText);
      }

      List<String> beforeEachText = getFileText(GenInputsAbstract.junit_before_each);
      if (beforeEachText != null) {
        jfw.addBeforeEach(beforeEachText);
      }

      List<String> afterEachText = getFileText(GenInputsAbstract.junit_after_each);
      if (afterEachText != null) {
        jfw.addAfterEach(afterEachText);
      }

      files.addAll(jfw.writeJUnitTestFiles(seqPartition));

      if (GenInputsAbstract.junit_reflection_allowed) {
        files.add(jfw.writeSuiteFile());
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
   * Print out usage error and stack trace and then exit
   *
   * @param format  the string format
   * @param args  the arguments
   */
  private static void usage(String format, Object... args) {
    System.out.print("ERROR: ");
    System.out.printf(format, args);
    System.out.println();
    System.out.println(options.usage());
    System.exit(-1);
  }

  private static List<String> getFileText(String filename) {
    if (filename != null) {
      List<String> textList = new ArrayList<>();
      textList.add("// code from file " + filename);
      try (EntryReader er = new EntryReader(filename)) {
        for (String line : er) {
          textList.add(line.trim());
        }
      } catch (IOException e) {
        System.err.println("Unable to read " + filename);
        //TODO this should really throw an exception
        return null;
      }
      return textList;
    }
    return null;
  }
}
