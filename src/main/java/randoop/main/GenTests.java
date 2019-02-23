package randoop.main;

import static randoop.reflection.VisibilityPredicate.IS_PUBLIC;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.BlockStmt;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.checkerframework.checker.signature.qual.ClassGetName;
import org.plumelib.options.Options;
import org.plumelib.options.Options.ArgException;
import org.plumelib.util.EntryReader;
import org.plumelib.util.UtilPlume;
import randoop.ExecutionVisitor;
import randoop.Globals;
import randoop.MethodReplacements;
import randoop.condition.RandoopSpecificationError;
import randoop.condition.SpecificationCollection;
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
import randoop.output.NameGenerator;
import randoop.output.RandoopOutputException;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.OperationModel;
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
import randoop.test.ContractCheckingGenerator;
import randoop.test.ContractSet;
import randoop.test.ErrorTestPredicate;
import randoop.test.ExcludeTestPredicate;
import randoop.test.ExpectedExceptionCheckGen;
import randoop.test.ExtendGenerator;
import randoop.test.IncludeIfCoversPredicate;
import randoop.test.IncludeTestPredicate;
import randoop.test.RegressionCaptureGenerator;
import randoop.test.RegressionTestPredicate;
import randoop.test.TestCheckGenerator;
import randoop.test.ValidityCheckingGenerator;
import randoop.types.ClassOrInterfaceType;
import randoop.types.Type;
import randoop.util.CollectionsExt;
import randoop.util.Log;
import randoop.util.MultiMap;
import randoop.util.Randomness;
import randoop.util.RandoopLoggingError;
import randoop.util.ReflectionExecutor;
import randoop.util.predicate.AlwaysFalse;

/** Test generation. */
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

  private static Options options =
      new Options(
          GenTests.class,
          GenInputsAbstract.class,
          ReflectionExecutor.class,
          ForwardGenerator.class,
          AbstractGenerator.class);

  /** The count of sequences that failed to compile. */
  private int sequenceCompileFailureCount = 0;

  public GenTests() {
    super(command, pitch, commandGrammar, where, summary, notes, input, output, example, options);
  }

  @Override
  public boolean handle(String[] args) {

    try {
      String[] nonargs = options.parse(args);
      if (nonargs.length > 0) {
        throw new ArgException("Unrecognized command-line arguments: " + Arrays.toString(nonargs));
      }
    } catch (ArgException ae) {
      usage("While parsing command-line arguments: %s", ae.getMessage());
    }

    checkOptionsValid();

    Randomness.setSeed(randomseed);

    // java.security.Policy policy = java.security.Policy.getPolicy();

    // This is distracting to the user as the first thing shown, and is not very informative.
    // Reinstate it with a --verbose option.
    // if (GenInputsAbstract.progressdisplay) {
    //   System.out.printf("Using security policy %s%n", policy);
    // }

    // If some properties were specified, set them
    for (String prop : GenInputsAbstract.system_props) {
      String[] pa = prop.split("=", 2);
      if (pa.length != 2) {
        usage("invalid property definition: %s%n", prop);
      }
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
    Set<@ClassGetName String> classnames = GenInputsAbstract.getClassnamesFromArgs();

    // Get names of classes that must be covered by output tests
    @SuppressWarnings("signature") // TOOD: read from file, no guarantee strings are @ClassGetName
    Set<@ClassGetName String> coveredClassnames =
        GenInputsAbstract.getStringSetFromFile(require_covered_classes, "coverage class names");

    // Get names of fields to be omitted
    Set<String> omitFields = GenInputsAbstract.getStringSetFromFile(omit_field_list, "field list");
    omitFields.addAll(omit_field);

    VisibilityPredicate visibility;
    if (GenInputsAbstract.junit_package_name == null) {
      visibility = IS_PUBLIC;
    } else if (GenInputsAbstract.only_test_public_members) {
      visibility = IS_PUBLIC;
      if (GenInputsAbstract.junit_package_name != null) {
        System.out.println(
            "Not using package "
                + GenInputsAbstract.junit_package_name
                + " since --only-test-public-members is set");
      }
    } else {
      visibility =
          new VisibilityPredicate.PackageVisibilityPredicate(GenInputsAbstract.junit_package_name);
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

    /*
     * Setup pre/post/throws-conditions for operations.
     */
    if (GenInputsAbstract.use_jdk_specifications) {
      if (GenInputsAbstract.specifications == null) {
        GenInputsAbstract.specifications = new ArrayList<>();
      }
      GenInputsAbstract.specifications.addAll(getJDKSpecificationFiles());
    }
    SpecificationCollection operationSpecifications = null;
    try {
      operationSpecifications = SpecificationCollection.create(GenInputsAbstract.specifications);
    } catch (RandoopSpecificationError e) {
      System.out.println("Error in specifications: " + e.getMessage());
      System.exit(1);
    }

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
              GenInputsAbstract.literals_file,
              operationSpecifications);
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
        StringTokenizer tokenizer = new StringTokenizer(classpath, java.io.File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
          String classPathElt = tokenizer.nextToken();
          if (classPathElt.endsWith(".jar")) {
            String classFileName = e.className.replace(".", "/") + ".class";
            System.out.println("  " + classFileName + " in " + classPathElt);
          } else {
            String classFileName = e.className.replace(".", java.io.File.separator) + ".class";
            if (!classPathElt.endsWith(java.io.File.separator)) {
              classPathElt += java.io.File.separator;
            }
            System.out.println("  " + classPathElt + classFileName);
          }
        }
        System.out.println("Correct your classpath or the class name and re-run Randoop.");
      }
      System.exit(1);
    } catch (RandoopSpecificationError e) {
      System.out.printf("Error: %s%n", e.getMessage());
      System.exit(1);
    }
    assert operationModel != null;

    List<TypedOperation> operations = operationModel.getOperations();
    Set<ClassOrInterfaceType> classesUnderTest = operationModel.getClassTypes();

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
      System.out.printf("Error parsing observers: %s%n", e.getMessage());
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
    AbstractGenerator explorer =
        new ForwardGenerator(
            operations,
            observers,
            new GenInputsAbstract.Limits(),
            componentMgr,
            listenerMgr,
            classesUnderTest);

    // log setup. TODO: handle environment variables like other methods in TestUtils do.
    operationModel.log();
    if (GenInputsAbstract.operation_history_log != null) {
      TestUtils.setOperationLog(new PrintWriter(GenInputsAbstract.operation_history_log), explorer);
    }
    TestUtils.setSelectionLog(GenInputsAbstract.selection_log);

    // These two debugging lines make runNoOutputTest() fail:
    // operationModel.dumpModel(System.out);
    // System.out.println("isLoggingOn = " + Log.isLoggingOn());

    /*
     * Create the test check generator for the contracts and observers
     */
    ContractSet contracts = operationModel.getContracts();
    TestCheckGenerator testGen = createTestCheckGenerator(visibility, contracts, observerMap);
    explorer.setTestCheckGenerator(testGen);

    /*
     * Setup for test predicate
     */
    // Always exclude a singleton sequence with just new Object()
    TypedOperation objectConstructor;
    try {
      objectConstructor = TypedOperation.forConstructor(Object.class.getConstructor());
    } catch (NoSuchMethodException e) {
      throw new RandoopBug("failed to get Object constructor", e);
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

    explorer.setTestPredicate(isOutputTest);

    /*
     * Setup visitors
     */
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
          throw new RandoopBug("Error while loading visitor class " + visitorClsName, e);
        }
      }
    }
    explorer.setExecutionVisitor(visitors);

    // Diagnostic output
    if (GenInputsAbstract.progressdisplay) {
      System.out.printf("Explorer = %s%n", explorer);
    }
    // These two debugging lines make runNoOutputTest() fail:
    // operationModel.dumpModel(System.out);
    // System.out.println("isLoggingOn = " + Log.isLoggingOn());
    if (Log.isLoggingOn()) {
      Log.logPrintf("Initial sequences (seeds):%n");
      componentMgr.log();
    }

    // Generate tests
    try {
      explorer.createAndClassifySequences();
    } catch (SequenceExceptionError e) {

      printSequenceExceptionError(explorer, e);

      System.exit(1);
    } catch (RandoopInstantiationError e) {
      throw new RandoopBug("Error instantiating operation " + e.getOpName(), e);
    } catch (RandoopGenerationError e) {
      throw new RandoopBug("Error in generation with operation " + e.getInstantiatedOperation(), e);
    } catch (SequenceExecutionException e) {
      throw new RandoopBug("Error executing generated sequence", e);
    } catch (RandoopLoggingError e) {
      throw new RandoopBug("Logging error", e);
    }

    // post generation
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
      final TestEnvironment testEnvironment =
          new TestEnvironment(convertClasspathToAbsolute(classpath));
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
          "%nUncompilable sequences generated (count: %d).%n", this.sequenceCompileFailureCount);
      System.out.println("Please report at https://github.com/randoop/randoop/issues ,");
      System.out.println(
          "providing the information requested at https://randoop.github.io/randoop/manual/index.html#bug-reporting .");
    }

    // Operation history includes counts determined by getting regression sequences from explorer,
    // so dump after all done.
    explorer.getOperationHistory().outputTable();

    return true;
  }

  /**
   * Convert each element of the given classpath from a relative to an absolute path.
   *
   * @param classpath the classpath to replace
   * @return a version of classpath with relative paths replaced by absolute paths
   */
  private String convertClasspathToAbsolute(String classpath) {
    String[] relpaths = classpath.split(java.io.File.pathSeparator);
    int length = relpaths.length;
    String[] abspaths = new String[length];
    for (int i = 0; i < length; i++) {
      String rel = relpaths[i];
      String abs;
      if (rel.equals("")) {
        abs = rel;
      } else {
        abs = Paths.get(rel).toAbsolutePath().toString();
      }
      abspaths[i] = abs;
    }
    return UtilPlume.join(abspaths, java.io.File.pathSeparator);
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
      List<Path> testFiles = new ArrayList<>();

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
        classSource =
            junitCreator.createTestDriver(driverName, testMap.keySet(), testSequences.size());
      }
      testFiles.add(
          codeWriter.writeUnmodifiedClassCode(
              GenInputsAbstract.junit_package_name, driverName, classSource));
      if (GenInputsAbstract.progressdisplay) {
        System.out.println();
        for (Path f : testFiles) {
          System.out.printf("Created file %s%n", f.toAbsolutePath());
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
  private List<Pattern> readOmitMethods(Path file) {
    List<Pattern> result = new ArrayList<>();
    // Read method omissions from user-provided file
    if (file != null) {
      try (EntryReader er = new EntryReader(file.toFile(), "^#.*", null)) {
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
   * Prints information about a {@code SequenceExceptionError} that indicates a flaky test has been
   * found. Prints information to help user identify source of flakiness, including exception,
   * statement that threw the exception, the full sequence where exception was thrown, and the input
   * subsequence.
   *
   * @param explorer the test generator
   * @param e the sequence exception
   */
  private void printSequenceExceptionError(AbstractGenerator explorer, SequenceExceptionError e) {

    StringJoiner msg = new StringJoiner(Globals.lineSep);
    msg.add("");
    msg.add("");
    msg.add("ERROR: Randoop stopped because of a flaky test.");
    msg.add("");
    msg.add("This can happen when Randoop is run on methods that side-effect global state.");
    msg.add("It can also indicate a bug in Randoop.  For example, it is often a bug in");
    msg.add("Randoop if the Exception is ClassCastException and the Input Subsequence\"");
    msg.add("below compiles but does not run.");
    msg.add("See the below info and https://randoop.github.io/randoop/manual/#flaky-tests .");
    msg.add("");
    msg.add(String.format("Exception:%n  %s%n", e.getError()));
    msg.add(String.format("Statement:%n  %s%n", e.getStatement()));
    // No trailing newline needed.
    msg.add(String.format("Full sequence:%n%s", e.getSequence()));
    // No trailing newline needed.
    msg.add(String.format("Input subsequence:%n%s", e.getSubsequence().toCodeString()));

    Log.logPrintf("%s%n", msg);
    System.out.println(msg);

    if (GenInputsAbstract.log == null) {
      System.out.println("For more details, rerun with logging turned on with --log=FILENAME.");
    } else {
      System.out.println("For more details, see the log at " + GenInputsAbstract.log);
    }

    if (Log.isLoggingOn()) {

      Sequence subsequence = e.getSubsequence();

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
        Log.logPrintf("Operations performed since subsequence first executed:%n");
        for (String opName : executedOperationTrace) {
          Log.logPrintf("%s%n", opName);
        }
      } else {
        Log.logPrintf(
            "No previous occurrence of subsequence where exception was thrown:%n" + "%s%n"
            // + "Please submit an issue at https://github.com/randoop/randoop/issues/new%n"
            ,
            subsequence);
      }
      Log.logPrintf("%n");
      Log.logStackTrace(e);
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
   * @param classNamePrefix the class name prefix
   * @param sequences the sequences for test methods of the created test classes
   * @param junitCreator the JUnit creator to create the abstract syntax trees for the test classes
   * @return mapping from a class name to the abstract syntax tree for the class
   */
  private LinkedHashMap<String, CompilationUnit> getTestASTMap(
      String classNamePrefix, List<ExecutableSequence> sequences, JUnitCreator junitCreator) {

    LinkedHashMap<String, CompilationUnit> testMap = new LinkedHashMap<>();

    NameGenerator methodNameGenerator =
        new NameGenerator(TEST_METHOD_NAME_PREFIX, 1, sequences.size());
    List<List<ExecutableSequence>> sequencePartition =
        CollectionsExt.formSublists(new ArrayList<>(sequences), testsperfile);
    for (int i = 0; i < sequencePartition.size(); i++) {
      String testClassName = classNamePrefix + i;
      CompilationUnit classAST =
          junitCreator.createTestClass(testClassName, methodNameGenerator, sequences);
      testMap.put(testClassName, classAST);
    }
    return testMap;
  }

  /**
   * Creates the test check generator for this run based on the command-line arguments. The goal of
   * the generator is to produce all appropriate checks for each sequence it is applied to.
   *
   * <p>The generator always contains validity and contract checks. If regression tests are to be
   * generated, it also contains the regression checks generator.
   *
   * @param visibility the visibility predicate
   * @param contracts the contract checks
   * @param observerMap the map from types to observer methods
   * @return the {@code TestCheckGenerator} that reflects command line arguments
   */
  public static TestCheckGenerator createTestCheckGenerator(
      VisibilityPredicate visibility,
      ContractSet contracts,
      MultiMap<Type, TypedOperation> observerMap) {

    // Start with checking for invalid exceptions.
    TestCheckGenerator testGen =
        new ValidityCheckingGenerator(
            GenInputsAbstract.flaky_test_behavior == FlakyTestAction.HALT);

    // Extend with contract checker.
    ContractCheckingGenerator contractVisitor = new ContractCheckingGenerator(contracts);
    testGen = new ExtendGenerator(testGen, contractVisitor);

    // And, generate regression tests, unless user says not to.
    if (!GenInputsAbstract.no_regression_tests) {
      ExpectedExceptionCheckGen expectation = new ExpectedExceptionCheckGen(visibility);

      RegressionCaptureGenerator regressionVisitor =
          new RegressionCaptureGenerator(
              expectation, observerMap, visibility, !GenInputsAbstract.no_regression_assertions);

      testGen = new ExtendGenerator(testGen, regressionVisitor);
    }
    return testGen;
  }

  /**
   * Print message, then print usage information, then exit.
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
      return UtilPlume.fileLines(filename);
    } catch (IOException e) {
      System.err.println("Unable to read " + filename);
      System.exit(1);
      throw new Error("This can't happen.");
    }
  }

  /**
   * Returns the list of JDK specification files from the {@code specifications/jdk} resources
   * directory in the Randoop jar file.
   *
   * @throws randoop.main.RandoopBug if there is an error locating the specification files
   * @return the list of JDK specification files
   */
  private Collection<? extends Path> getJDKSpecificationFiles() {
    List<Path> fileList = new ArrayList<>();
    final String specificationDirectory = "/specifications/jdk/";
    Path directoryPath = getResourceDirectoryPath(specificationDirectory);

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath, "json")) {
      for (Path entry : stream) {
        fileList.add(entry);
      }
    } catch (IOException e) {
      throw new RandoopBug("Error reading JDK specification directory", e);
    }

    return fileList;
  }

  /**
   * Returns the path for the resource directory in the jar file.
   *
   * @param resourceDirectory the resource directory relative to the root of the jar file, should
   *     start with "/"
   * @throws randoop.main.RandoopBug if an error occurs when locating the directory
   * @return the {@code Path} for the resource directory
   */
  private Path getResourceDirectoryPath(String resourceDirectory) {
    URI directoryURI;
    try {
      directoryURI = GenTests.class.getResource(resourceDirectory).toURI();
    } catch (URISyntaxException e) {
      throw new RandoopBug("Error locating directory " + resourceDirectory, e);
    }

    FileSystem fileSystem = null;
    try {
      fileSystem = FileSystems.newFileSystem(directoryURI, Collections.<String, Object>emptyMap());
    } catch (IOException e) {
      throw new RandoopBug("Error locating directory " + resourceDirectory, e);
    }
    return fileSystem.getPath(resourceDirectory);
  }

  /** Increments the count of sequence compilation failures. */
  public void incrementSequenceCompileFailureCount() {
    this.sequenceCompileFailureCount++;
  }
}
