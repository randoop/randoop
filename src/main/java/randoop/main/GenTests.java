package randoop.main;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.BlockStmt;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import plume.EntryReader;
import plume.Options;
import plume.Options.ArgException;
import plume.SimpleLog;
import plume.UtilMDE;
import randoop.BugInRandoopException;
import randoop.DummyVisitor;
import randoop.ExecutionVisitor;
import randoop.Globals;
import randoop.MethodReplacements;
import randoop.MultiVisitor;
import randoop.execution.TestEnvironment;
import randoop.generation.AbstractGenerator;
import randoop.generation.ComponentManager;
import randoop.generation.ForwardGenerator;
import randoop.generation.RandoopGenerationError;
import randoop.generation.RandoopListenerManager;
import randoop.generation.SeedSequences;
import randoop.generation.TestUtils;
import randoop.instrument.CoveredClassVisitor;
import randoop.operation.Operation;
import randoop.operation.OperationParseException;
import randoop.operation.TypedOperation;
import randoop.output.CodeWriter;
import randoop.output.FailingTestFilter;
import randoop.output.JUnitCreator;
import randoop.output.JavaFileWriter;
import randoop.output.MinimizerWriter;
import randoop.output.RandoopOutputException;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationModel;
import randoop.reflection.PackageVisibilityPredicate;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.RandoopInstantiationError;
import randoop.reflection.RawSignature;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.SignatureParseException;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceExceptionError;
import randoop.sequence.SequenceExecutionException;
import randoop.test.CompilableTestPredicate;
import randoop.test.ContractCheckingVisitor;
import randoop.test.ContractSet;
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
import randoop.types.Type;
import randoop.util.CollectionsExt;
import randoop.util.Log;
import randoop.util.MultiMap;
import randoop.util.Randomness;
import randoop.util.RandoopLoggingError;
import randoop.util.ReflectionExecutor;
import randoop.util.predicate.AlwaysFalse;
import randoop.util.predicate.Predicate;

public class GenTests extends GenInputsAbstract {

  // If this is changed, also change RandoopSystemTest.NO_OPERATIONS_TO_TEST
  private static final String NO_OPERATIONS_TO_TEST = "There are no operations to test. Exiting.";

  private static final String command = "gentests";

  private static final String pitch = "Generates unit tests for a set of classes.";

  private static final String commandGrammar = "gentests OPTIONS";

  private static final String where =
      "At least one of `--testclass', `--classlist', or `--methodlist' is specified.";

  private static final String summary =
      "Uses feedback-directed random test generation to generate "
          + "error-revealing tests and regression tests. ";

  private static final String input =
      "One or more names of classes to test. A class to test can be specified "
          + "via the `--testclass=<CLASSNAME>' or `--classlist=<FILENAME>' options.";

  private static final String output =
      "Two JUnit test suites (each as one or more Java source files): "
          + "an error-revealing test suite and a regression test suite.";

  private static final String example =
      "java randoop.main.Main gentests --testclass=java.util.Collections "
          + "--testclass=java.util.TreeSet";

  private static final List<String> notes;
  public static final String TEST_METHOD_NAME_PREFIX = "test";

  private BlockStmt afterAllFixtureBody;
  private BlockStmt afterEachFixtureBody;
  private BlockStmt beforeAllFixtureBody;
  private BlockStmt beforeEachFixtureBody;

  static {
    notes = new ArrayList<>();
    notes.add("See the Randoop manual for guidance.  Here are a few important tips.");
    notes.add(
        "Randoop executes the code under test, with no mechanisms to protect your system from "
            + "harm resulting from arbitrary code execution. If random execution of your code "
            + "could have undesirable effects (e.g., deletion of files, opening network "
            + "connections, etc.) make sure you execute Randoop in a sandbox.");
    notes.add(
        "Randoop will only use methods from the classes that you specify for testing. "
            + "If Randoop is not generating tests for a particular method, make sure that you "
            + "include classes for the types that the method requires. ");
    notes.add(
        "Randoop may be deterministic when the code under test is itself deterministic. "
            + "This means that two runs of Randoop may generate the same tests. "
            + "To get variation across runs, use the --randomseed option.");
  }

  public static SimpleLog progress = new SimpleLog(true);

  private static Options options =
      new Options(
          GenTests.class,
          GenInputsAbstract.class,
          ReflectionExecutor.class,
          ForwardGenerator.class,
          AbstractGenerator.class);

  /** The count of sequences that failed to compile */
  private int sequenceCompileFailureCount = 0;

  public GenTests() {
    super(command, pitch, commandGrammar, where, summary, notes, input, output, example, options);
  }

  @Override
  public boolean handle(String[] args) throws RandoopTextuiException, RandoopInputException {

    try {
      String[] nonargs = options.parse(args);
      if (nonargs.length > 0) {
        throw new ArgException("Unrecognized arguments: " + Arrays.toString(nonargs));
      }
    } catch (ArgException ae) {
      usage("while parsing command-line arguments: %s", ae.getMessage());
    }

    checkOptionsValid();

    Randomness.setSeed(randomseed);
    if (GenInputsAbstract.selection_log != null) {
      Randomness.selectionLog = new SimpleLog(GenInputsAbstract.selection_log);
    }

    //java.security.Policy policy = java.security.Policy.getPolicy();

    // This is distracting to the user as the first thing shown, and is not very informative.
    // Reinstate it with a --verbose option.
    // if (GenInputsAbstract.progressdisplay) {
    //   System.out.printf("Using security policy %s%n", policy);
    // }

    // If some properties were specified, set them
    for (String prop : GenInputsAbstract.system_props) {
      String[] pa = prop.split("=", 2);
      if (pa.length != 2) usage("invalid property definition: %s%n", prop);
      System.setProperty(pa[0], pa[1]);
    }

    /*
     * If there is fixture code check that it can be parsed first
     */
    if (!getFixtureCode()) {
      System.exit(1);
    }

    /*
     * Setup model of classes under test
     */
    // Get names of classes under test
    Set<String> classnames = GenInputsAbstract.getClassnamesFromArgs();

    // Get names of classes that must be covered by output tests
    Set<String> coveredClassnames =
        GenInputsAbstract.getStringSetFromFile(require_covered_classes, "coverage class names");

    // Get names of fields to be omitted
    Set<String> omitFields = GenInputsAbstract.getStringSetFromFile(omit_field_list, "field list");
    omitFields.addAll(omit_field);

    VisibilityPredicate visibility;
    if (GenInputsAbstract.junit_package_name == null) {
      visibility = new PublicVisibilityPredicate();
    } else if (GenInputsAbstract.only_test_public_members) {
      visibility = new PublicVisibilityPredicate();
      if (GenInputsAbstract.junit_package_name != null) {
        System.out.println(
            "Not using package "
                + GenInputsAbstract.junit_package_name
                + " since --only-test-public-members set");
      }
    } else {
      visibility = new PackageVisibilityPredicate(GenInputsAbstract.junit_package_name);
    }

    omitmethods.addAll(readOmitMethods(omitmethods_file));
    if (!GenInputsAbstract.dont_omit_replaced_methods) {
      omitmethods.addAll(createPatternsFromSignatures(MethodReplacements.getSignatureList()));
    }

    ReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(omitFields);

    ClassNameErrorHandler classNameErrorHandler = new ThrowClassNameError();
    if (silently_ignore_bad_class_names) {
      classNameErrorHandler = new WarnOnBadClassName();
    }

    Set<String> methodSignatures =
        GenInputsAbstract.getStringSetFromFile(methodlist, "method list");

    String classpath = Globals.getClassPath();

    OperationModel operationModel = null;
    try {
      operationModel =
          OperationModel.createModel(
              visibility,
              reflectionPredicate,
              omitmethods,
              classnames,
              coveredClassnames,
              methodSignatures,
              classNameErrorHandler,
              GenInputsAbstract.literals_file);
    } catch (SignatureParseException e) {
      System.out.printf("%nError: parse exception thrown %s%n", e);
      System.out.println("Exiting Randoop.");
      System.exit(1);
    } catch (NoSuchMethodException e) {
      System.out.printf("%nError building operation model: %s%n", e);
      System.out.println("Exiting Randoop.");
      System.exit(1);
    } catch (RandoopClassNameError e) {
      System.out.printf("Error: %s%n", e.getMessage());
      if (e.getMessage().startsWith("No class with name \"")) {
        System.out.println("More specifically, none of the following files could be found:");
        StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
          String classPathElt = tokenizer.nextToken();
          if (classPathElt.endsWith(".jar")) {
            String classFileName = e.className.replace(".", "/") + ".class";
            System.out.println("  " + classFileName + " in " + classPathElt);
          } else {
            String classFileName = e.className.replace(".", File.separator) + ".class";
            if (!classPathElt.endsWith(File.separator)) {
              classPathElt += File.separator;
            }
            System.out.println("  " + classPathElt + classFileName);
          }
        }
        System.out.println("Correct your classpath or the class name and re-run Randoop.");
      }
      System.exit(1);
    }
    assert operationModel != null;

    List<TypedOperation> operations = operationModel.getOperations();

    /*
     * Stop if there is only 1 operation. This will be Object().
     */
    if (operations.size() <= 1) {
      System.out.println(NO_OPERATIONS_TO_TEST);
      operationModel.dumpModel(System.out);
      System.exit(1);
    }
    if (GenInputsAbstract.progressdisplay) {
      System.out.println("PUBLIC MEMBERS=" + operations.size());
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

    Set<String> observerSignatures =
        GenInputsAbstract.getStringSetFromFile(
            GenInputsAbstract.observers, "observer", "//.*", null);

    MultiMap<Type, TypedOperation> observerMap;
    try {
      observerMap = operationModel.getObservers(observerSignatures);
    } catch (OperationParseException e) {
      System.out.printf("Error parsing observers: %s%n", e);
      System.exit(1);
      throw new Error("dead code");
    }
    Set<TypedOperation> observers = new LinkedHashSet<>();
    for (Type keyType : observerMap.keySet()) {
      observers.addAll(observerMap.getValues(keyType));
    }

    /*
     * Create the generator for this session.
     */
    AbstractGenerator explorer;

    int num_classes = operationModel.getClassTypes().size();
    Map<Sequence, Integer> literalsTermFrequency = operationModel.getLiteralsTermFrequency();

    explorer =
        new ForwardGenerator(
            operations,
            observers,
            new GenInputsAbstract.Limits(),
            componentMgr,
            listenerMgr,
            num_classes,
            literalsTermFrequency);

    /*
     * Create the test check generator for the contracts and observers
     */
    ContractSet contracts = operationModel.getContracts();
    TestCheckGenerator testGen = createTestCheckGenerator(visibility, contracts, observerMap);

    explorer.addTestCheckGenerator(testGen);

    /*
     * Setup for test predicate
     */
    // Always exclude a singleton sequence with just new Object()
    TypedOperation objectConstructor;
    try {
      objectConstructor = TypedOperation.forConstructor(Object.class.getConstructor());
    } catch (NoSuchMethodException e) {
      throw new BugInRandoopException("failed to get Object constructor", e);
    }

    Sequence newObj = new Sequence().extend(objectConstructor);
    Set<Sequence> excludeSet = new LinkedHashSet<>();
    excludeSet.add(newObj);

    // Define test predicate to decide which test sequences will be output
    Predicate<ExecutableSequence> isOutputTest =
        createTestOutputPredicate(
            excludeSet,
            operationModel.getCoveredClassesGoal(),
            GenInputsAbstract.require_classname_in_test);

    explorer.addTestPredicate(isOutputTest);

    /*
     * Setup visitors
     */
    // list of visitors for collecting information from test sequences
    List<ExecutionVisitor> visitors = new ArrayList<>();

    // instrumentation visitor
    if (GenInputsAbstract.require_covered_classes != null) {
      visitors.add(new CoveredClassVisitor(operationModel.getCoveredClassesGoal()));
    }

    // Install any user-specified visitors.
    if (!GenInputsAbstract.visitor.isEmpty()) {
      for (String visitorClsName : GenInputsAbstract.visitor) {
        try {
          @SuppressWarnings("unchecked")
          Class<ExecutionVisitor> cls = (Class<ExecutionVisitor>) Class.forName(visitorClsName);
          ExecutionVisitor vis = cls.getDeclaredConstructor().newInstance();
          visitors.add(vis);
        } catch (Exception e) {
          throw new BugInRandoopException("Error while loading visitor class " + visitorClsName, e);
        }
      }
    }

    ExecutionVisitor visitor;
    switch (visitors.size()) {
      case 0:
        visitor = new DummyVisitor();
        break;
      case 1:
        visitor = visitors.get(0);
        break;
      default:
        visitor = new MultiVisitor(visitors);
        break;
    }

    explorer.addExecutionVisitor(visitor);

    if (GenInputsAbstract.progressdisplay) {
      System.out.printf("Explorer = %s\n", explorer);
    }

    /* log setup */
    operationModel.log();
    // These two debugging lines make runNoOutputTest() fail:
    // operationModel.dumpModel(System.out);
    // System.out.println("isLoggingOn = " + Log.isLoggingOn());
    if (Log.isLoggingOn()) {
      Log.logLine("Initial sequences (seeds):");
      componentMgr.log();
    }
    TestUtils.setOperationLog(GenInputsAbstract.operation_history_log, explorer);

    /* Generate tests */
    try {
      explorer.explore();
    } catch (SequenceExceptionError e) {

      handleFlakySequenceException(explorer, e);

      System.exit(1);
    } catch (RandoopInstantiationError e) {
      throw new BugInRandoopException("Error instantiating operation " + e.getOpName(), e);
    } catch (RandoopGenerationError e) {
      throw new BugInRandoopException(
          "Error in generation with operation " + e.getInstantiatedOperation(), e);
    } catch (SequenceExecutionException e) {
      throw new BugInRandoopException("Error executing generated sequence", e);
    } catch (RandoopLoggingError e) {
      throw new BugInRandoopException("Logging error", e);
    }

    /* post generation */
    if (GenInputsAbstract.dont_output_tests) {
      return true;
    }

    JUnitCreator junitCreator =
        JUnitCreator.getTestCreator(
            junit_package_name,
            beforeAllFixtureBody,
            afterAllFixtureBody,
            beforeEachFixtureBody,
            afterEachFixtureBody);

    JavaFileWriter javaFileWriter = new JavaFileWriter(junit_output_dir);
    if (!GenInputsAbstract.no_error_revealing_tests) {
      CodeWriter codeWriter = javaFileWriter;
      if (GenInputsAbstract.minimize_error_test || GenInputsAbstract.stop_on_error_test) {
        codeWriter = new MinimizerWriter(javaFileWriter);
      }
      writeTestFiles(
          junitCreator,
          explorer.getErrorTestSequences(),
          codeWriter,
          GenInputsAbstract.error_test_basename,
          "Error-revealing");
    }

    if (!GenInputsAbstract.no_regression_tests) {
      final TestEnvironment testEnvironment = new TestEnvironment(classpath);
      String agentPathString = MethodReplacements.getAgentPath();
      String agentArgs = MethodReplacements.getAgentArgs();
      if (agentPathString != null && !agentPathString.isEmpty()) {
        Path agentPath = Paths.get(agentPathString);
        testEnvironment.setReplaceCallAgent(agentPath, agentArgs);
      }

      FailingTestFilter codeWriter = new FailingTestFilter(testEnvironment, javaFileWriter);
      writeTestFiles(
          junitCreator,
          explorer.getRegressionSequences(),
          codeWriter,
          GenInputsAbstract.regression_test_basename,
          "Regression");
    }

    if (GenInputsAbstract.progressdisplay) {
      System.out.printf("%nInvalid tests generated: %d%n", explorer.invalidSequenceCount);
    }

    if (this.sequenceCompileFailureCount > 0) {
      System.out.printf(
          "%nUncompilable sequences generated (count: %d). Please report.%n",
          this.sequenceCompileFailureCount);
    }

    // Operation history includes counts determined by getting regression sequences from explorer,
    // so dump after all done.
    explorer.getOperationHistory().outputTable();

    return true;
  }

  /**
   * Creates the test classes for the test sequences using the {@link JUnitCreator} and then writes
   * the files using the {@link CodeWriter}. Writes the test suite if {@link
   * GenInputsAbstract#junit_reflection_allowed} is true, or the test driver, otherwise.
   *
   * <p>Class names are numbered with {@code basename} as the prefix. The package for tests is
   * {@link GenInputsAbstract#junit_package_name}.
   *
   * @param junitCreator the {@link JUnitCreator} to create the test class source
   * @param testSequences a list of {@link ExecutableSequence} objects for test methods
   * @param codeWriter the {@link CodeWriter} to output the test classes
   * @param basename the prefix for the class name
   * @param testKind a {@code String} indicating the kind of tests for logging and error messages
   */
  private void writeTestFiles(
      JUnitCreator junitCreator,
      List<ExecutableSequence> testSequences,
      CodeWriter codeWriter,
      String basename,
      String testKind) {
    if (testSequences.isEmpty()) {
      if (GenInputsAbstract.progressdisplay) {
        System.out.printf("%nNo " + testKind.toLowerCase() + " tests to output%n");
      }
      return;
    }
    if (GenInputsAbstract.progressdisplay) {
      System.out.printf("%n%s test output:%n", testKind);
      System.out.printf("%s test count: %d%n", testKind, testSequences.size());
      System.out.printf("Writing JUnit tests...%n");
    }
    try {
      List<File> testFiles = new ArrayList<>();

      // Create and write test classes.
      LinkedHashMap<String, CompilationUnit> testMap =
          getTestASTMap(basename, testSequences, junitCreator);
      for (Map.Entry<String, CompilationUnit> entry : testMap.entrySet()) {
        String classname = entry.getKey();
        String classSource = entry.getValue().toString();
        testFiles.add(
            codeWriter.writeClassCode(
                GenInputsAbstract.junit_package_name, classname, classSource));
      }

      // Create and write suite or driver class.
      String driverName;
      String classSource;
      if (GenInputsAbstract.junit_reflection_allowed) {
        driverName = basename;
        classSource = junitCreator.createTestSuite(driverName, testMap.keySet());
      } else {
        driverName = basename + "Driver";
        classSource = junitCreator.createTestDriver(driverName, testMap.keySet());
      }
      testFiles.add(
          codeWriter.writeUnmodifiedClassCode(
              GenInputsAbstract.junit_package_name, driverName, classSource));
      if (GenInputsAbstract.progressdisplay) {
        System.out.println();
        for (File f : testFiles) {
          System.out.printf("Created file %s%n", f.getAbsolutePath());
        }
      }
    } catch (RandoopOutputException e) {
      System.out.printf("%nError writing %s tests%n", testKind.toLowerCase());
      e.printStackTrace(System.out);
      System.exit(1);
    }
  }

  /**
   * Create fixture code from {@link GenInputsAbstract#junit_after_all}, {@link
   * GenInputsAbstract#junit_after_each}, {@link GenInputsAbstract#junit_before_all}, and {@link
   * GenInputsAbstract#junit_before_each} and set fixture body variables.
   *
   * @return true if all fixtures were read without error, false, otherwise
   */
  private boolean getFixtureCode() {
    boolean badFixtureText = false;

    try {
      afterAllFixtureBody =
          JUnitCreator.parseFixture(getFileText(GenInputsAbstract.junit_after_all));
    } catch (ParseException e) {
      System.out.println("Error in after-all fixture text at token " + e.currentToken);
      badFixtureText = true;
    }
    try {
      afterEachFixtureBody =
          JUnitCreator.parseFixture(getFileText(GenInputsAbstract.junit_after_each));
    } catch (ParseException e) {
      System.out.println("Error in after-each fixture text at token " + e.currentToken);
      badFixtureText = true;
    }
    try {
      beforeAllFixtureBody =
          JUnitCreator.parseFixture(getFileText(GenInputsAbstract.junit_before_all));
    } catch (ParseException e) {
      System.out.println("Error in before-all fixture text at token " + e.currentToken);
      badFixtureText = true;
    }
    try {
      beforeEachFixtureBody =
          JUnitCreator.parseFixture(getFileText(GenInputsAbstract.junit_before_each));
    } catch (ParseException e) {
      System.out.println("Error in before-each fixture text at token " + e.currentToken);
      badFixtureText = true;
    }
    return !badFixtureText;
  }

  /**
   * Returns patterns read from the given file.
   *
   * @param file the file to read from, may be null
   * @return contents of the file, as a set of Patterns
   */
  private List<Pattern> readOmitMethods(File file) {
    List<Pattern> result = new ArrayList<>();
    // Read method omissions from user-provided file
    if (file != null) {
      try (EntryReader er = new EntryReader(file, "^#.*", null)) {
        for (String line : er) {
          String trimmed = line.trim();
          if (!trimmed.isEmpty()) {
            Pattern pattern = Pattern.compile(trimmed);
            result.add(pattern);
          }
        }
      } catch (IOException e) {
        System.out.println("Error reading omitmethods-list file " + file + ":");
        System.out.println(e.getMessage());
        System.exit(1);
      }
    }
    return result;
  }

  /**
   * Creates a list of signature strings (see {@link RawSignature#toString()} to a list of {@code
   * Pattern}.
   *
   * @param signatures the list of signature strings
   * @return the list of patterns for the signature strings
   */
  private List<Pattern> createPatternsFromSignatures(List<String> signatures) {
    List<Pattern> patterns = new ArrayList<>();
    for (String signatureString : signatures) {
      patterns.add(signatureToPattern(signatureString));
    }
    return patterns;
  }

  /**
   * Converts a signature string (see {@link RawSignature#toString()} to a {@code Pattern} that
   * matches that string.
   *
   * @param signatureString the string representation of a signature
   * @return the pattern to match {@code signatureString}
   */
  private Pattern signatureToPattern(String signatureString) {
    String patternString =
        signatureString
            .replaceAll(" ", "")
            .replaceAll("\\.", "\\\\.")
            .replaceAll("\\(", "\\\\(")
            .replaceAll("\\)", "\\\\)")
            .replaceAll("\\$", "\\\\$")
            .replaceAll("\\[", "\\\\[")
            .replaceAll("\\]", "\\\\]");
    return Pattern.compile(patternString);
  }

  /**
   * Handles the occurrence of a {@code SequenceExceptionError} that indicates a flaky test has been
   * found. Prints information to help user identify source of flakiness, including exception,
   * statement that threw the exception, the full sequence where exception was thrown, and the input
   * subsequence.
   *
   * @param explorer the test generator
   * @param e the sequence exception
   */
  private void handleFlakySequenceException(AbstractGenerator explorer, SequenceExceptionError e) {

    String msg =
        String.format(
            "%n%nERROR: Randoop stopped because of a flaky test.%n%n"
                + "This can happen when Randoop is run on methods that side-effect global state.%n"
                + "See the \"Randoop stopped because of a flaky test\" section of the user manual.%n");
    if (GenInputsAbstract.log == null) {
      msg += "For more details, rerun with logging turned on with --log=FILENAME.%n";
    } else {
      msg += "For more details, see the log at " + GenInputsAbstract.log + "%n";
    }
    System.out.printf(msg);

    if (Log.isLoggingOn()) {
      Sequence subsequence = e.getSubsequence();
      Log.log(msg);
      Log.log(String.format("%nException:%n  %s%n", e.getError()));
      Log.log(String.format("Statement:%n  %s%n", e.getStatement()));
      Log.log(String.format("Full sequence:%n%s%n", e.getSequence()));
      Log.log(String.format("Input subsequence:%n%s%n", subsequence.toCodeString()));

      /*
       * Get the set of operations executed since the first execution of the flaky subsequence
       */
      List<String> executedOperationTrace = new ArrayList<>();
      boolean flakySequenceFound = false;
      for (Sequence sequence : explorer.getAllSequences()) {
        // Look for occurrence of flaky sequence
        if (subsequence.equals(sequence)) {
          flakySequenceFound = true;
        }
        // Once flaky sequence found, collect the operations executed
        if (flakySequenceFound) {
          for (int i = 0; i < sequence.statements.size(); i++) {
            Operation operation = sequence.statements.get(i).getOperation();
            if (!operation.isNonreceivingValue()) {
              executedOperationTrace.add(operation.toString());
            }
          }
        }
      }

      if (!executedOperationTrace.isEmpty()) {
        Log.logLine("Operations performed since subsequence first executed:");
        for (String opName : executedOperationTrace) {
          Log.logLine(opName);
        }
      } else {
        System.err.printf(
            "Unable to find a previous occurrence of subsequence where exception was thrown:%n"
                + "  %s%n"
                + "Please submit an issue at https://github.com/randoop/randoop/issues/new%n",
            subsequence);
      }
    }
  }

  /**
   * Builds the test predicate that determines whether a particular sequence will be included in the
   * output based on command-line arguments.
   *
   * @param excludeSet the set of sequences to exclude
   * @param coveredClasses the list of classes to test for coverage
   * @param includePattern the pattern for method name inclusion
   * @return the predicate
   */
  public Predicate<ExecutableSequence> createTestOutputPredicate(
      Set<Sequence> excludeSet, Set<Class<?>> coveredClasses, Pattern includePattern) {
    if (GenInputsAbstract.dont_output_tests) {
      return new AlwaysFalse<>();
    }

    Predicate<ExecutableSequence> baseTest;
    // Base case: exclude sequences in excludeSet, keep everything else.
    // To exclude something else, add sequence to excludeSet.
    baseTest = new ExcludeTestPredicate(excludeSet);
    if (includePattern != null) {
      baseTest = baseTest.and(new IncludeTestPredicate(includePattern));
    }
    if (!coveredClasses.isEmpty()) {
      baseTest = baseTest.and(new IncludeIfCoversPredicate(coveredClasses));
    }

    // Use command-line arguments to determine which kinds of tests to output.
    Predicate<ExecutableSequence> checkTest;
    if (GenInputsAbstract.no_regression_tests && GenInputsAbstract.no_error_revealing_tests) {
      checkTest = new AlwaysFalse<>();
    } else if (GenInputsAbstract.no_regression_tests) {
      checkTest = new ErrorTestPredicate();
    } else if (GenInputsAbstract.no_error_revealing_tests) {
      checkTest = new RegressionTestPredicate();
    } else {
      checkTest = new ErrorTestPredicate().or(new RegressionTestPredicate());
    }

    Predicate<ExecutableSequence> isOutputTest = baseTest.and(checkTest);

    if (GenInputsAbstract.check_compilable) {
      JUnitCreator junitCreator =
          JUnitCreator.getTestCreator(
              junit_package_name,
              beforeAllFixtureBody,
              afterAllFixtureBody,
              beforeEachFixtureBody,
              afterEachFixtureBody);
      isOutputTest = isOutputTest.and(new CompilableTestPredicate(junitCreator, this));
    }

    return isOutputTest;
  }

  /**
   * Creates the JUnit test classes for the given sequences, in AST (abstract syntax tree) form.
   *
   * @param junitPrefix the class name prefix
   * @param sequences the sequences for test methods of the created test classes
   * @param junitCreator the JUnit creator to create the abstract syntax trees for the test classes
   * @return mapping from a class name to the abstract syntax tree for the class
   */
  private LinkedHashMap<String, CompilationUnit> getTestASTMap(
      String junitPrefix, List<ExecutableSequence> sequences, JUnitCreator junitCreator) {

    List<List<ExecutableSequence>> sequencePartition =
        CollectionsExt.formSublists(new ArrayList<>(sequences), testsperfile);

    LinkedHashMap<String, CompilationUnit> testMap = new LinkedHashMap<>();
    for (int i = 0; i < sequencePartition.size(); i++) {
      List<ExecutableSequence> partition = sequencePartition.get(i);
      String testClassName = junitPrefix + i;
      CompilationUnit classAST =
          junitCreator.createTestClass(testClassName, TEST_METHOD_NAME_PREFIX, partition);
      testMap.put(testClassName, classAST);
    }
    return testMap;
  }

  /**
   * Creates the test check generator for this run based on the command-line arguments. The goal of
   * the generator is to produce all appropriate checks for each sequence it is applied to. Validity
   * and contract checks are always needed to determine which sequences have invalid or error
   * behaviors, even if only regression tests are desired. So, this generator will always be built.
   * If in addition regression tests are to be generated, then the regression checks generator is
   * added.
   *
   * @param visibility the visibility predicate
   * @param contracts the contract checks
   * @param observerMap the map from types to observer methods
   * @return the {@code TestCheckGenerator} that reflects command line arguments.
   */
  public TestCheckGenerator createTestCheckGenerator(
      VisibilityPredicate visibility,
      ContractSet contracts,
      MultiMap<Type, TypedOperation> observerMap) {

    // Start with checking for invalid exceptions.
    ExceptionPredicate isInvalid = new ExceptionBehaviorPredicate(BehaviorType.INVALID);
    TestCheckGenerator testGen =
        new ValidityCheckingVisitor(isInvalid, !GenInputsAbstract.ignore_flaky_tests);

    // Extend with contract checker.
    ExceptionPredicate isError = new ExceptionBehaviorPredicate(BehaviorType.ERROR);
    ContractCheckingVisitor contractVisitor = new ContractCheckingVisitor(contracts, isError);
    testGen = new ExtendGenerator(testGen, contractVisitor);

    // And, generate regression tests, unless user says not to.
    if (!GenInputsAbstract.no_regression_tests) {
      ExceptionPredicate isExpected = new AlwaysFalseExceptionPredicate();
      boolean includeAssertions = true;
      if (GenInputsAbstract.no_regression_assertions) {
        includeAssertions = false;
      } else {
        isExpected = new ExceptionBehaviorPredicate(BehaviorType.EXPECTED);
      }
      ExpectedExceptionCheckGen expectation = new ExpectedExceptionCheckGen(visibility, isExpected);

      RegressionCaptureVisitor regressionVisitor =
          new RegressionCaptureVisitor(expectation, observerMap, visibility, includeAssertions);

      testGen = new ExtendGenerator(testGen, regressionVisitor);
    }
    return testGen;
  }

  /**
   * Print out usage error and stack trace and then exit.
   *
   * @param format the string format
   * @param args the arguments
   */
  private static void usage(String format, Object... args) {
    System.out.print("ERROR: ");
    System.out.printf(format, args);
    System.out.println();
    System.out.println(options.usage());
    System.exit(-1);
  }

  /**
   * Return the text of the given file, as a list of lines. Returns null if the {@code filename}
   * argument is null. Terminates execution if the {@code filename} file cannot be read.
   *
   * @param filename the file to read
   * @return the contents of {@code filename}, as a list of strings
   */
  private static List<String> getFileText(String filename) {
    if (filename == null) {
      return null;
    }

    try {
      return UtilMDE.fileLines(filename);
    } catch (IOException e) {
      System.err.println("Unable to read " + filename);
      System.exit(1);
      throw new Error("This can't happen.");
    }
  }

  public void countSequenceCompileFailure() {
    this.sequenceCompileFailureCount++;
  }
}
