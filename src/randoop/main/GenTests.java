package randoop.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import plume.EntryReader;
import plume.Option;
import plume.OptionGroup;
import plume.Options;
import plume.Options.ArgException;
import plume.SimpleLog;
import plume.Unpublicized;

import randoop.BugInRandoopException;
import randoop.CheckRep;
import randoop.CheckRepContract;
import randoop.ComponentManager;
import randoop.DummyVisitor;
import randoop.EqualsHashcode;
import randoop.EqualsReflexive;
import randoop.EqualsSymmetric;
import randoop.EqualsToNullRetFalse;
import randoop.ExecutionVisitor;
import randoop.JunitFileWriter;
import randoop.LiteralFileReader;
import randoop.MultiVisitor;
import randoop.ObjectContract;
import randoop.RandoopListenerManager;
import randoop.SeedSequences;
import randoop.experiments.CodeCoverageTracker;
import randoop.experiments.CovWitnessHelperVisitor;
import randoop.operation.ConstructorCall;
import randoop.operation.NonreceiverTerm;
import randoop.operation.Operation;
import randoop.operation.OperationParseException;
import randoop.operation.OperationParser;
import randoop.reflection.DefaultReflectionPredicate;
import randoop.reflection.NotPrivateVisibilityPredicate;
import randoop.reflection.OperationExtractor;
import randoop.reflection.PublicVisibilityPredicate;
import randoop.reflection.VisibilityPredicate;
import randoop.sequence.AbstractGenerator;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.ForwardGenerator;
import randoop.sequence.Sequence;
import randoop.test.ContractCheckingVisitor;
import randoop.test.ErrorTestPredicate;
import randoop.test.ExcludeTestPredicate;
import randoop.test.ExpectedExceptionCheckGen;
import randoop.test.ExtendGenerator;
import randoop.test.IncludeTestPredicate;
import randoop.test.RegressionCaptureVisitor;
import randoop.test.RegressionTestPredicate;
import randoop.test.TestCheckGenerator;
import randoop.test.ValidityCheckingVisitor;
import randoop.test.predicate.AlwaysFalseExceptionPredicate;
import randoop.test.predicate.ExceptionBehaviorPredicate;
import randoop.test.predicate.ExceptionPredicate;
import randoop.util.ClassFileConstants;
import randoop.util.CollectionsExt;
import randoop.util.Log;
import randoop.util.MultiMap;
import randoop.util.Randomness;
import randoop.util.ReflectionExecutor;
import randoop.util.predicate.AlwaysFalse;
import randoop.util.predicate.Predicate;

import cov.Branch;
import cov.Coverage;

public class GenTests extends GenInputsAbstract {

  private static final String command = "gentests";

  private static final String pitch = "Generates unit tests for a set of classes.";

  private static final String commandGrammar = "gentests OPTIONS";

  private static final String where = "At least one class is specified via `--testclass' or `--classlist'.";

  private static final String summary = "Attempts to generate JUnit tests that "
    + "capture the behavior of the classes under test and/or find contract violations. "
    + "Randoop generates tests using feedback-directed random test generation. ";

  private static final String input = "One or more names of classes to test. A class to test can be specified "
    + "via the `--testclass=<CLASSNAME>' or `--classlist=<FILENAME>' options.";

  private static final String output = "A JUnit test suite (as one or more Java source files). The "
    + "tests in the suite will pass when executed using the classes under test.";

  private static final String example = "java randoop.main.Main gentests --testclass=java.util.Collections "
    + " --testclass=java.util.TreeSet";

  private static final List<String> notes;

  static {

    notes = new ArrayList<String>();
    notes.add("Randoop executes the code under test, with no mechanisms to protect your system from harm resulting from arbitrary code execution. If random execution of your code could have undesirable effects (e.g. deletion of files, opening network connections, etc.) make sure you execute Randoop in a sandbox machine.");
    notes.add("Randoop will only use methods from the classes that you specify for testing. If Randoop is not generating tests for a particular method, make sure that you are including classes for the types that the method requires. Otherwise, Randoop may fail to generate tests due to missing input parameters.");
    notes.add("Randoop is designed to be deterministic when the code under test is itself deterministic. This means that two runs of Randoop will generate the same tests. To get variation across runs, use the --randomseed option.");

  }

  @OptionGroup(value="GenTests unpublicized options", unpublicized=true)
  @Unpublicized
  @Option("Signals that this is a run in the context of a system test. (Slower)")
  public static boolean system_test_run = false;

  public static SimpleLog progress = new SimpleLog (true);

  private static Options options = new Options(
      GenTests.class,
      GenInputsAbstract.class,
      ReflectionExecutor.class,
      ForwardGenerator.class,
      AbstractGenerator.class);

  public GenTests() {
    super(command, pitch, commandGrammar, where, summary, notes, input, output,
        example, options);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean handle(String[] args) throws RandoopTextuiException {

    try {
      String[] nonargs = options.parse(args);
      if (nonargs.length > 0)
        throw new ArgException("Unrecognized arguments: "
            + Arrays.toString(nonargs));
    } catch (ArgException ae) {
      usage ("while parsing command-line arguments: %s",
             ae.getMessage());
    }

    checkOptionsValid();

    Randomness.reset(randomseed);

    java.security.Policy policy = java.security.Policy.getPolicy();

    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.printf ("policy = %s%n", policy);
    }

    // If some properties were specified, set them
    for (String prop : GenInputsAbstract.system_props) {
      String[] pa = prop.split ("=", 2);
      if (pa.length != 2)
        usage ("invalid property definition: %s%n", prop);
      System.setProperty (pa[0], pa[1]);
    }

    // If an initializer method was specified, execute it
    executeInitializationRoutine(1);

    // Find classes to test.
    if (classlist == null && methodlist == null && testclass.size() == 0) {
      System.out.println("You must specify some classes or methods to test.");
      System.out.println("Use the --classlist, --testclass, or --methodlist options.");
      System.exit(1);
    }

    List<Class<?>> allClasses = findClassesFromArgs(options);
    
    // TODO include package in visibility, maybe not do "not private"
    VisibilityPredicate visibility;
    if (GenInputsAbstract.public_only) {
      visibility = new PublicVisibilityPredicate();
    } else {
      visibility = new NotPrivateVisibilityPredicate();
    }

    // Remove private (non-.isVisible) classes and abstract classes
    // and interfaces.
    List<Class<?>> classes = new ArrayList<>(allClasses.size());
    for (Class<?> c : allClasses) {
      if (Modifier.isAbstract (c.getModifiers()) && !c.isEnum()) {
        System.out.println("Ignoring abstract " + c + " specified via --classlist or --testclass.");
      } else if (! visibility.isVisible (c)) {
        System.out.println("Ignoring non-visible " + c + " specified via --classlist or --testclass.");
      } else {
        classes.add(c);
      }
    }

    // Make sure each of the classes is visible.  Should really make sure
    // there is at least one visible constructor/factory in each class as well.
    for (Class<?> c : classes) {
      if (! visibility.isVisible (c)) {
        throw new Error ("Specified class " + c + " is not visible");
      }
    }

    Set<String> omitFields = new HashSet<>();

    if (omit_field_list != null) {
      try (EntryReader er = new EntryReader(omit_field_list)) {
        for (String line : er) {
          omitFields.add(line);
        }
      } catch (IOException e1) {
        System.out.println("Error reading file " + omit_field_list);
        System.exit(2);
        throw new Error("Escaped exit after failing to read omit fields.");
      }
    }

    // Determine classes for which we compute coverage instrumentation
    // (using cov.Instrument), and omit all fields in any such class
    // to avoid having them manipulated by generated tests.
    List<Class<?>> covClasses = new ArrayList<>();
    if (coverage_instrumented_classes != null) {
      File covClassesFile = new File(coverage_instrumented_classes);
      try {
        covClasses = ClassReader.getClassesForFile(covClassesFile);
      } catch (IOException e) {
        throw new Error(e);
      }
      for (Class<?> cls : covClasses) {
        assert Coverage.isInstrumented(cls) : cls.toString();
        // System.out.println("Will track branch coverage for " + cls);
        omitFields.add(cls.getName() + "." + cov.Constants.BRANCHLINES);
        omitFields.add(cls.getName() + "." + cov.Constants.FALSE_BRANCHES);
        omitFields.add(cls.getName() + "." + cov.Constants.IS_INSTRUMENTED_FIELD);
        omitFields.add(cls.getName() + "." + cov.Constants.METHOD_ID_ANNOTATION);
        omitFields.add(cls.getName() + "." + cov.Constants.METHOD_ID_TO_BRANCHES);
        omitFields.add(cls.getName() + "." + cov.Constants.METHOD_LINE_SPANS_FIELD);
        omitFields.add(cls.getName() + "." + cov.Constants.SOURCE_FILE_NAME);
        omitFields.add(cls.getName() + "." + cov.Constants.TRUE_BRANCHES);
      }
    }
    
    CodeCoverageTracker covTracker = new CodeCoverageTracker(covClasses);
    RandoopListenerManager listenerMgr = new RandoopListenerManager();
    listenerMgr.addListener(covTracker);
    
    DefaultReflectionPredicate reflectionPredicate = new DefaultReflectionPredicate(omitmethods, omitFields, visibility);
    List<Operation> model = OperationExtractor.getOperations(classes, reflectionPredicate);

    // Always add Object constructor (it's often useful).
    ConstructorCall objectConstructor = null;
    try {
      objectConstructor = ConstructorCall.createConstructorCall(Object.class.getConstructor());
      if (!model.contains(objectConstructor))
        model.add(objectConstructor);
    } catch (Exception e) {
      throw new BugInRandoopException(e); // Should never reach here!
    }

    if (methodlist != null) {

      try (EntryReader rdr = new EntryReader(new File(methodlist), "^#.*", null)) {

        for (String line : rdr) {
          Operation op = OperationParser.parse(line);
          if (op.satisfies(reflectionPredicate) && !model.contains(op)) {
            model.add(op);
          }
        }

      } catch (IOException e) {
        System.out.println("Error while reading method list file " + methodlist);
        System.exit(1);
      } catch (OperationParseException e) {
        throw new Error(e);
      }

    }

    // Don't remove observers; they create useful values.

    if (model.size() == 0) {
      Log.out.println("There are no methods to test. Exiting.");
      System.exit(1);
    }
    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.println("PUBLIC MEMBERS=" + model.size());
    }

    // Initialize components.
    Set<Sequence> components = new LinkedHashSet<Sequence>();
    if (!componentfile_ser.isEmpty()) {
      for (String onefile : componentfile_ser) {
        try (ObjectInputStream objectos = 
            new ObjectInputStream(
                new GZIPInputStream(new FileInputStream(onefile)))) {
          Set<Sequence> seqset = (Set<Sequence>)objectos.readObject();
          if (!GenInputsAbstract.noprogressdisplay) {
            System.out.println("Adding " + seqset.size() + " component sequences from file " + onefile);
          }
          components.addAll(seqset);
        } catch (Exception e) {
          throw new Error(e);
        }
      }
    }
    if (!componentfile_txt.isEmpty()) {
      for (String onefile : componentfile_txt) {
        Set<Sequence> seqset = new LinkedHashSet<Sequence>();
        Sequence.readTextSequences(onefile, seqset);
        if (!GenInputsAbstract.noprogressdisplay) {
          System.out.println("Adding " + seqset.size() + " component sequences from file " + onefile);
        }
        components.addAll(seqset);
      }
    }

    // Add default seeds.
    components.addAll(SeedSequences.objectsToSeeds(SeedSequences.primitiveSeeds));

    // Add user-specified seeds.
    components.addAll(SeedSequences.getSeedsFromAnnotatedFields(classes.toArray(new Class<?>[0])));

    ComponentManager componentMgr = null;
    if (components == null) {
      componentMgr = new ComponentManager(SeedSequences.defaultSeeds());
    } else {
      componentMgr = new ComponentManager(components);
    }

    addClassLiterals(componentMgr, allClasses);
    
    /////////////////////////////////////////
    // Create the generator for this session.
    AbstractGenerator explorer;
    explorer = new ForwardGenerator(
        model, 
        timelimit * 1000, 
        inputlimit, 
        outputlimit, 
        componentMgr, 
        null, 
        listenerMgr);
    /////////////////////////////////////////

    ////// setup for check generation 
    TestCheckGenerator testGen = createTestCheckGenerator(visibility, classes);
    
    // Define test predicate to decide which test sequences will be output
    Predicate<ExecutableSequence> isOutputTest = createTestOutputPredicate(objectConstructor);
    
    // list of visitors for collecting information from test sequences
    List<ExecutionVisitor> visitors = new ArrayList<ExecutionVisitor>();

    // setup coverage visitor if user says so
    if (GenInputsAbstract.output_cov_witnesses) {
      ExecutionVisitor covVisitor = new CovWitnessHelperVisitor(covTracker);
      visitors.add(covVisitor);
    }

    // Install any user-specified visitors.
    if (!GenInputsAbstract.visitor.isEmpty()) {
      for (String visitorClsName : GenInputsAbstract.visitor) {
        try {
          Class<ExecutionVisitor> cls =
            (Class<ExecutionVisitor>)Class.forName(visitorClsName);
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
    
    explorer.addTestPredicate(isOutputTest);
    explorer.addTestCheckGenerator(testGen);
    explorer.addExecutionVisitor(visitor);

    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.printf ("Explorer = %s\n", explorer);
    }
    
    // Generate tests
    explorer.explore();

   // once tests generated, 

    if (GenInputsAbstract.output_branches != null) {
      outputCoverageBranches(covTracker);
    }

    if (GenInputsAbstract.output_covmap != null) {
      outputObject(covTracker.branchesToCoveringSeqs, output_covmap);
    }

    if (GenInputsAbstract.output_components != null) {

      assert explorer instanceof ForwardGenerator;
      ForwardGenerator gen = (ForwardGenerator)explorer;

      // Output component sequences.
      System.out.print("Serializing component sequences...");
      Set<Sequence> componentset = gen.componentManager.getAllGeneratedSequences();
      System.out.println(" (" + componentset.size() + " components) ");
      outputObject(componentset, GenInputsAbstract.output_components);
    }

    if (GenInputsAbstract.dont_output_tests)
      return true;
  
    if (! GenInputsAbstract.no_error_revealing_tests) {
      List<ExecutableSequence> errorSequences = explorer.getErrorTestSequences();
      if (errorSequences.size() > 0) {
        if (! GenInputsAbstract.noprogressdisplay) {
          System.out.printf("%nError-revealing test output:%n");
        }
        outputTests(errorSequences, GenInputsAbstract.error_test_filename);
      } else {
        if (! GenInputsAbstract.noprogressdisplay) {
          System.out.printf("%nNo error-revealing tests to output%n");
        }
      }
    }
    
    if (! GenInputsAbstract.no_regression_tests) {
      List<ExecutableSequence> regressionSequences = explorer.getRegressionSequences();
      if (regressionSequences.size() > 0) {
        if (! GenInputsAbstract.noprogressdisplay) {
          System.out.printf("%nRegression test output:%n");
        }
        outputTests(regressionSequences, GenInputsAbstract.regression_test_filename);
      } else {
        if (! GenInputsAbstract.noprogressdisplay) {
          System.out.printf("No regression tests to output%n");
        }
      }
    }
    
    return true;
  }

  /**
   * Builds the test predicate that determines whether a particular sequence will
   * be included in the output based on command-line arguments.
   * 
   * @param objectConstructor  the constructor for the Object class
   * @return the predicate 
   */
  public Predicate<ExecutableSequence> createTestOutputPredicate(ConstructorCall objectConstructor) {
    Predicate<ExecutableSequence> isOutputTest;
    if (GenInputsAbstract.dont_output_tests) {
      isOutputTest = new AlwaysFalse<>();      
    } else {
      Predicate<ExecutableSequence> baseTest;
      // base case: exclude sequences with just "new Object()", keep everything else
      // to exclude something else, add sequence to excludeSet
      Sequence newObj = new Sequence().extend(objectConstructor);
      Set<Sequence> excludeSet = new LinkedHashSet<>();
      excludeSet.add(newObj);
      baseTest = new ExcludeTestPredicate(excludeSet);
      if (GenInputsAbstract.include_only_classes != null) { //keep only tests with test classes
        baseTest = baseTest.and(new IncludeTestPredicate(GenInputsAbstract.include_only_classes));
      }

      // Use arguments to determine which kinds of tests to output
      // Default is neither (e.g., no tests output)
      Predicate<ExecutableSequence> checkTest = new AlwaysFalse<>();

      // But, generate error-revealing tests if user says so
      if (! GenInputsAbstract.no_error_revealing_tests) {
        checkTest = new ErrorTestPredicate();
      }

      // And, generate regression tests, unless user says not to
      if (! GenInputsAbstract.no_regression_tests) {
        checkTest = checkTest.or(new RegressionTestPredicate());
      }
      isOutputTest = baseTest.and(checkTest);
    }
    return isOutputTest;
  }

  /**
   * Outputs JUnit tests for the sequence list. And, if the user indicates by
   * command-line argument {@link GenInputsAbstract#output_tests_serialized} 
   * also writes serialized tests.
   * 
   * @param sequences  the sequences to output
   * @param junitPrefix  the filename prefix for test output
   */
  private void outputTests(List<ExecutableSequence> sequences, String junitPrefix) {
    if (GenInputsAbstract.output_tests_serialized != null) {
      System.out.println("Serializing tests...");
      //using prefix as a suffix here because of path info in output_tests_serialized
      outputObject(sequences, output_tests_serialized + junitPrefix);
    }

    writeJUnitTests (junit_output_dir, sequences, null, junitPrefix);
  }
  
  /**
   * Manages output for serialized objects. 
   * 
   * @param obj  the object to serialize
   * @param filename  the file name for serialization
   */
  private void outputObject(Object obj, String filename) {
    try (ObjectOutputStream os = new ObjectOutputStream(
        new GZIPOutputStream(new FileOutputStream(filename)))) {
      os.writeObject(obj);
    } catch (FileNotFoundException e) {
      throw new Error("Unable to serialize object: " + e);
    } catch (IOException e) {
      throw new Error(e);
    } 
  }
  
  /**
   * Outputs coverage branches to the file {@link GenInputsAbstract#output_branches}.
   * Branches are sorted by their string representation.
   * 
   * @param covTracker  the coverage tracker with branches
   * @throws Error  if an {@link IOException} is thrown during output
   */
  private void outputCoverageBranches(CodeCoverageTracker covTracker) throws Error {
    Comparator<Branch> branchComparator = new Comparator<Branch>() {
      public int compare(Branch o1, Branch o2) {
        return o1.toString().compareTo(o2.toString());
      }
    };
    Set<Branch> branches = new TreeSet<Branch>(branchComparator);
    branches.addAll(covTracker.branchesCovered);
    // Create a file with branches, sorted by their string representation.
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(output_branches))){
      // Touch all covered branches (they may have been reset during generation).
      for (Branch b : branches) {
        writer.append(b.toString());
        writer.newLine();
      }
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  /**
   * Returns the list of contracts to be used in contract checking.
   * 
   * @param classes  the list of annotated classes for retrieving contracts
   * @return the list of {@code ObjectContract} objects for contract checking
   */
  private List<ObjectContract> getContracts(List<Class<?>> classes) {
    List<ObjectContract> contracts = new ArrayList<ObjectContract>();

    // Add any @CheckRep-annotated methods
    List<ObjectContract> checkRepContracts = getContractsFromAnnotations(classes);
    contracts.addAll(checkRepContracts);

    // Now add all of Randoop's default contracts.
    // Note: if you add to this list, also update the Javadoc for check_object_contracts.
    contracts.add(new EqualsReflexive());
    contracts.add(new EqualsSymmetric());
    contracts.add(new EqualsHashcode());
    contracts.add(new EqualsToNullRetFalse());
    return contracts;
  }

  /**
   * Creates the test check generator for this run based on the command-line
   * arguments. 
   * The goal of the generator is to produce all appropriate checks for each
   * sequence it is applied to. Validity and contract checks are always needed
   * to determine which sequences have invalid or error behaviors, even if only
   * regression tests are desired. So, this generator will always be built.
   * If in addition regression tests are to be generated, then the regression 
   * checks generator is added.
   * 
   * @param visibility  the visibility predicate
   * @param classes  the classes for obtaining contract checks
   * @return the {@code TestCheckGenerator} that reflects command line arguments.
   */
  public TestCheckGenerator createTestCheckGenerator(VisibilityPredicate visibility, List<Class<?>> classes) {
    
    // start with checking for invalid exceptions
    ExceptionPredicate isInvalid = new ExceptionBehaviorPredicate(BehaviorType.INVALID);
    TestCheckGenerator testGen = new ValidityCheckingVisitor(isInvalid);
    
    // extend with contract checker 
    List<ObjectContract> contracts = getContracts(classes);
    ExceptionPredicate isError = new ExceptionBehaviorPredicate(BehaviorType.ERROR);
    ContractCheckingVisitor contractVisitor = new ContractCheckingVisitor(contracts,isError);
    testGen = new ExtendGenerator(testGen, contractVisitor);
    
    // and, generate regression tests, unless user says not to
    if (! GenInputsAbstract.no_regression_tests) {
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
      regressionVisitor = new RegressionCaptureVisitor(expectation, includeAssertions);

      testGen = new ExtendGenerator(testGen, regressionVisitor);
    }
    return testGen;
  }
  
  
  /**
   * Adds literals to the component manager, by parsing any literals
   * files specified by the user.
   */
  private void addClassLiterals(ComponentManager compMgr, List<Class<?>> allClasses) {

    // Parameter check.
    boolean validMode = GenInputsAbstract.literals_level != ClassLiteralsMode.NONE;
    if (GenInputsAbstract.literals_file.size() > 0 && !validMode) {
      System.out.println("Invalid parameter combination: specified a class literal file but --use-class-literals=NONE");
      System.exit(1);
    }

    // Add a (1-element) sequence corresponding to each literal to the component manager.
    for (String filename : GenInputsAbstract.literals_file) {
      MultiMap<Class<?>, NonreceiverTerm> literalmap;
      if (filename.equals("CLASSES")) {
        Collection<ClassFileConstants.ConstantSet> css
          = new ArrayList<ClassFileConstants.ConstantSet>(allClasses.size());
        for (Class<?> clazz : allClasses) {
          css.add(ClassFileConstants.getConstants(clazz.getName()));
        }
        literalmap = ClassFileConstants.toMap(css);
      } else {
        literalmap = LiteralFileReader.parse(filename);
      }

      for (Class<?> cls : literalmap.keySet()) {
        Package pkg = (GenInputsAbstract.literals_level == ClassLiteralsMode.PACKAGE ? cls.getPackage() : null);
        for (NonreceiverTerm p : literalmap.getValues(cls)) {
          Sequence seq = Sequence.create(p);
          switch (GenInputsAbstract.literals_level) {
          case CLASS:
            compMgr.addClassLevelLiteral(cls, seq);
            break;
          case PACKAGE:
            assert pkg != null;
            compMgr.addPackageLevelLiteral(pkg, seq);
            break;
          case ALL:
            compMgr.addGeneratedSequence(seq);
            break;
          default:
            throw new Error("Unexpected error in GenTests -- please report at https://github.com/randoop/randoop/issues");
          }
        }
      }
    }
  }

  /**
   * Writes the sequences as JUnit files to the specified directory.
   *
   * @param output_dir string name of output directory.
   * @param seqList a list of sequences to write.
   * @param additionalJUnitClasses other classes to write (may be null).
   * @return list of files written.
   **/
  public static List<File> writeJUnitTests (String output_dir,
                                        List<ExecutableSequence> seqList,
                                        List<String> additionalJUnitClasses,
                                        String junitClassname) {

    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.printf("Writing %d junit tests%n", seqList.size());
    }

    List<File> files = new ArrayList<File>();

    if (seqList.size() > 0) {
      List<List<ExecutableSequence>> seqPartition =
          CollectionsExt.<ExecutableSequence>chunkUp(new ArrayList<ExecutableSequence> (seqList), testsperfile);

      JunitFileWriter jfw = new JunitFileWriter(output_dir, junit_package_name, junitClassname);

      files.addAll(jfw.writeJUnitTestFiles(seqPartition));

      if (GenInputsAbstract.junit_reflection_allowed) {
        files.add(jfw.writeSuiteFile(additionalJUnitClasses));
      } else {
        files.add(jfw.writeDriverFile());
      }
    } else { //preserves behavior from previous version
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
   * @param phase  the phase number passed to initialization routine
   */
  public static void executeInitializationRoutine (int phase) {

    if (GenInputsAbstract.init_routine == null)
      return;

    String full_name = GenInputsAbstract.init_routine;
    int lastdot = full_name.lastIndexOf(".");
    if (lastdot == -1)
      usage ("invalid init routine: %s\n", full_name);
    String classname = full_name.substring (0, lastdot);
    String methodname = full_name.substring (lastdot+1);
    methodname = methodname.replaceFirst ("[()]*$", "");
    System.out.printf ("%s - %s\n", classname, methodname);
    Class<?> iclass = null;
    try {
      iclass = Class.forName (classname);
    } catch (Exception e) {
      usage ("Can't load init class %s: %s", classname, e.getMessage());
    }
    Method imethod = null;
    try {
      imethod = iclass.getDeclaredMethod (methodname, int.class);
    } catch (Exception e) {
      usage ("Can't find init method %s: %s", methodname, e);
    }
    if (!Modifier.isStatic (imethod.getModifiers()))
      usage ("init method %s.%s must be static", classname, methodname);
    try {
      imethod.invoke (null, phase);
    } catch (Exception e) {
      usage (e, "problem executing init method %s.%s: %s",
             classname, methodname, e);
    }
  }

  /**
   * Read a list of sequences from a serialized file
   * @param filename is name of file containing sequences.
   * @return list of sequence objects read from file.
   */
  public static List<ExecutableSequence> read_sequences (String filename) {

    // Read the list of sequences from the serialized file
    List<ExecutableSequence> seqs = null;
    try {
      FileInputStream fileis = new FileInputStream(filename);
      ObjectInputStream objectis
        = new ObjectInputStream(new GZIPInputStream(fileis));
      @SuppressWarnings("unchecked")
      List<ExecutableSequence> seqs_tmp
        = (List<ExecutableSequence>) objectis.readObject();
      seqs = seqs_tmp;
      objectis.close();
      fileis.close();
    } catch (Exception e) {
        throw new Error(e);
    }

    return seqs;
  }

  /**
   * Write out a serialized file of sequences
   * @param seqs list of sequences to write.
   * @param outfile filename for output file.
   */
  public static void write_sequences (List<ExecutableSequence> seqs,
                                      String outfile) {

    // dump_seqs ("write_sequences", seqs);

    try {
      FileOutputStream fileos = new FileOutputStream(outfile);
      ObjectOutputStream objectos
        = new ObjectOutputStream(new GZIPOutputStream(fileos));
      System.out.printf (" Saving %d sequences to %s%n", seqs.size(), outfile);
      objectos.writeObject(seqs);
      objectos.close();
      fileos.close();
    } catch (Exception e) {
      throw new Error(e);
    }
    System.out.printf ("Finished saving sequences%n");
  }
  
  /** Print out usage error and stack trace and then exit **/
  static void usage (Throwable t, String format, Object... args) {

    System.out.print ("ERROR: ");
    System.out.printf (format, args);
    System.out.println();
    System.out.println (options.usage());
    if (t != null)
      t.printStackTrace();
    System.exit(-1);
  }

  static void usage (String format, Object ... args) {
    usage (null, format, args);
  }

  public static List<ObjectContract> getContractsFromAnnotations(List<Class<?>> classes) {

    List<ObjectContract> contractsFound = new ArrayList<ObjectContract>();

    for (Class<?> c : classes) {
      for (Method m : c.getDeclaredMethods()) {
        if (m.getAnnotation(CheckRep.class) != null) {

          // Check that method is an instance (not a static) method.
          if (Modifier.isStatic(m.getModifiers())) {
            String msg = "RANDOOP ANNOTATION ERROR: Expected @CheckRep-annotated method " + m.getName() + " in class "
            + m.getDeclaringClass() + " to be an instance method, but it is declared static.";
            throw new RuntimeException(msg);
          }

          // Check that method is public.
          if (!Modifier.isPublic(m.getModifiers())) {
            String msg = "RANDOOP ANNOTATION ERROR: Expected @CheckRep-annotated method " + m.getName() + " in class "
            + m.getDeclaringClass() + " to be declared public but it is not.";
            throw new RuntimeException(msg);
          }

          // Check that method takes no arguments.
          if (m.getParameterTypes().length > 0) {
            String msg = "RANDOOP ANNOTATION ERROR: Expected @CheckRep-annotated method " + m.getName() + " in class "
            + m.getDeclaringClass() + " to declare no parameters but it does (method signature:" + m.toString() + ").";
            throw new RuntimeException(msg);
          }

          // Check that method's return type is void.
          if (!(m.getReturnType().equals(boolean.class) || m.getReturnType().equals(void.class))) {
            String msg = "RANDOOP ANNOTATION ERROR: Expected @CheckRep-annotated method " + m.getName() + " in class "
            + m.getDeclaringClass() + " to have void or boolean return type but it does not (method signature:" + m.toString() + ").";
            throw new RuntimeException(msg);
          }

          printDetectedAnnotatedCheckRepMethod(m);
          contractsFound.add(new CheckRepContract(m));
        }
      }
    }
    return contractsFound;
  }

  private static void printDetectedAnnotatedCheckRepMethod(Method m) {
    String msg = "ANNOTATION: Detected @CheckRep-annotated method \""
      + m.toString() + "\". Will use it to check rep invariant of class "
      + m.getDeclaringClass().getCanonicalName() + " during generation.";
   System.out.println(msg);
  }

  public static void dump_seqs (String msg, List<ExecutableSequence> seqs) {

    if (false) {
      System.out.printf ("Sequences at %s\n", msg);
      for (int seq_no = 0; seq_no < seqs.size(); seq_no++)
        System.out.printf ("seq %d [%08X]:\n %s\n", seq_no,
                           seqs.get(seq_no).seq_id(),
                           seqs.get(seq_no).toCodeString());

    }
  }
}
