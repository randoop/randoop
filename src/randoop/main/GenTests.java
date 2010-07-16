package randoop.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import plume.Option;
import plume.Options;
import plume.SimpleLog;
import plume.Unpublicized;
import plume.Options.ArgException;
import randoop.AbstractGenerator;
import randoop.BugInRandoopException;
import randoop.Check;
import randoop.CheckRep;
import randoop.CheckRepContract;
import randoop.ComponentManager;
import randoop.ContractCheckingVisitor;
import randoop.EqualsHashcode;
import randoop.EqualsReflexive;
import randoop.EqualsSymmetric;
import randoop.EqualsToNullRetFalse;
import randoop.ExecutableSequence;
import randoop.ExecutionVisitor;
import randoop.ExpectedExceptionCheck;
import randoop.ForwardGenerator;
import randoop.Globals;
import randoop.JunitFileWriter;
import randoop.LiteralFileReader;
import randoop.NaiveRandomGenerator;
import randoop.ObjectContract;
import randoop.PrimitiveOrStringOrNullDecl;
import randoop.RConstructor;
import randoop.RMethod;
import randoop.RegressionCaptureVisitor;
import randoop.ReplayVisitor;
import randoop.SeedSequences;
import randoop.Sequence;
import randoop.SequenceGeneratorStats;
import randoop.StatementKind;
import randoop.Variable;
import randoop.runtime.ClosingStream;
import randoop.runtime.CreatedJUnitFile;
import randoop.runtime.IMessage;
import randoop.runtime.MessageSender;
import randoop.runtime.RandoopFinished;
import randoop.util.ClassFileConstants;
import randoop.util.DefaultReflectionFilter;
import randoop.util.Log;
import randoop.util.MultiMap;
import randoop.util.Randomness;
import randoop.util.Reflection;
import randoop.util.ReflectionExecutor;
import randoop.util.RunCmd;
import randoop.util.SerializationHelper;
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

  @Unpublicized
  @Option("Signals that this is a run in the context of a system test. (Slower)")
  public static boolean system_test_run = false;

  public static SimpleLog progress = new SimpleLog (true);

  private static Options options = new Options(
      Globals.class,
      GenTests.class,
      GenInputsAbstract.class,
      Log.class,
      ReflectionExecutor.class,
      ForwardGenerator.class,
      AbstractGenerator.class,
      SequenceGeneratorStats.class);

  public GenTests() {
    super(command, pitch, commandGrammar, where, summary, notes, input, output,
        example, options);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean handle(String[] args) throws RandoopTextuiException {

//     RandoopSecurityManager randoopSecurityManager = new RandoopSecurityManager(
//       RandoopSecurityManager.Status.OFF);
//     System.setSecurityManager(randoopSecurityManager);
    
    try {
      String[] nonargs = options.parse(args);
      if (nonargs.length > 0)
        throw new ArgException("Unrecognized arguments: "
            + Arrays.toString(nonargs));
    } catch (ArgException ae) {
      usage ("while parsing command-line arguments: %s",
             ae.getMessage());
    }

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
    execute_init_routine(1);

    // Find classes to test.
    if (classlist == null && methodlist == null && testclass.size() == 0) {
      System.out.println("You must specify some classes or methods to test.");
      System.out.println("Use the --classlist, --testclass, or --methodlist options.");
      System.exit(1);
    }
    
    MessageSender msgSender = null;
    if (comm_port > 0) {
      try {
        msgSender = new MessageSender(comm_port);
      } catch (IOException e) {
        System.out.println("Could not connect to port " + comm_port + " on local host");
        System.exit(1);
      }
    }
    
    List<Class<?>> allClasses = findClassesFromArgs(options);

    // Remove private (non-.isVisible) classes and abstract classes
    // and interfaces.
    List<Class<?>> classes = new ArrayList<Class<?>>(allClasses.size());
    for (Class<?> c : allClasses) {
      if (Reflection.isAbstract (c)) {
        System.out.println("Ignoring abstract " + c + " specified on command line.");
      } else if (! Reflection.isVisible (c)) {
        System.out.println("Ignoring non-visible " + c + " specified on command line.");
      } else {
        classes.add(c);
      }
    }

    // Make sure each of the classes is visible.  Should really make sure
    // there is at least one visible constructor/factory in each class as well.
    for (Class<?> c : classes) {
      if (!Reflection.isVisible (c)) {
        throw new Error ("Specified class " + c + " is not visible");
      }
    }
    List<StatementKind> model =
      Reflection.getStatements(classes, new DefaultReflectionFilter(omitmethods));

    // Always add Object constructor (it's often useful).
    RConstructor objectConstructor = null;
    try {
      objectConstructor = RConstructor.getRConstructor(Object.class.getConstructor());
      if (!model.contains(objectConstructor))
        model.add(objectConstructor);
    } catch (Exception e) {
      throw new BugInRandoopException(e); // Should never reach here!
    }

    if (methodlist != null) {
      Set<StatementKind> statements = new LinkedHashSet<StatementKind>();
      try {
        for (Member m : Reflection.loadMethodsAndCtorsFromFile(new File(methodlist))) {
          if (m instanceof Method) {
            statements.add(RMethod.getRMethod((Method)m));
          } else {
            assert m instanceof Constructor<?>;
            statements.add(RConstructor.getRConstructor((Constructor<?>)m));
          }
        }
      } catch (IOException e) {
        System.out.println("Error while reading method list file " + methodlist);
        System.exit(1);
      }
      for (StatementKind st : statements) {
        if (!model.contains(st))
          model.add(st);
      }
    }

    if (model.size() == 0) {
      Log.out.println("There are no methods to test. Exiting.");
      System.exit(1);
    }
    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.println("PUBLIC MEMBERS=" + model.size());
    }

    List<Class<?>> covClasses = new ArrayList<Class<?>>();
    if (coverage_instrumented_classes != null) {
      File covClassesFile = new File(coverage_instrumented_classes);
      try {
        covClasses = Reflection.loadClassesFromFile(covClassesFile);
      } catch (IOException e) {
        throw new Error(e);
      }
      for (Class<?> cls : covClasses) {
        assert Coverage.isInstrumented(cls) : cls.toString();
        // System.out.println("Will track branch coverage for " + cls);
      }
    }


    // Initialize components.
    Set<Sequence> components = new LinkedHashSet<Sequence>();
    if (!componentfile_ser.isEmpty()) {
      for (String onefile : componentfile_ser) {
        try {
          FileInputStream fileos = new FileInputStream(onefile);
          ObjectInputStream objectos = new ObjectInputStream(new GZIPInputStream(fileos));
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
    
    AbstractGenerator explorer = null;

    if (component_based) {
      explorer = new ForwardGenerator(
        model,
        covClasses,
        timelimit * 1000,
        inputlimit,
        componentMgr,
        msgSender,
        null);

    } else {

      // Generate inputs.
      explorer = new NaiveRandomGenerator(
          model,
          covClasses,
          timelimit * 1000,
          inputlimit,
          componentMgr,
          null);
    }
    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.printf ("Explorer = %s\n", explorer);
    }

    // Determine what visitors to install.
    // NOTE that order matters! Regression capture visitor
    // should come after contract-violating visitor.
    List<ExecutionVisitor> visitors = new ArrayList<ExecutionVisitor>();
    
    
    if (check_object_contracts) {
      List<ObjectContract> contracts = new ArrayList<ObjectContract>();

      // Add any @CheckRep-annotated methods and create visitors for them.
      List<ObjectContract> checkRepContracts = getContractsFromAnnotations(classes.toArray(new Class<?>[0]));
      contracts.addAll(checkRepContracts);
      
      // Now add all of Randoop's default contracts.
      contracts.add(new EqualsReflexive());
      contracts.add(new EqualsToNullRetFalse());
      contracts.add(new EqualsHashcode());
      contracts.add(new EqualsSymmetric());
      ContractCheckingVisitor contractVisitor = new ContractCheckingVisitor(contracts,
          GenInputsAbstract.offline ? false : true);
      visitors.add(contractVisitor);
    }
    visitors.add(new RegressionCaptureVisitor());

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

    explorer.executionVisitor.visitors.addAll(visitors);

    explorer.explore();

    if (explorer instanceof NaiveRandomGenerator) {
      System.out.println("*** NORMALS:" + NaiveRandomGenerator.normals);
      System.out.println("*** EXCEPTIONS:" + NaiveRandomGenerator.exceptions);
    }

    // Print branch coverage.
    System.out.println();

    if (output_branches != null) {
      Comparator<Branch> branchComparator = new Comparator<Branch>() {
        public int compare(Branch o1, Branch o2) {
          return o1.toString().compareTo(o2.toString());
        }
      };
      Set<Branch> branches = new TreeSet<Branch>(branchComparator);
      branches.addAll(explorer.stats.branchesCovered);
      // Create a file with branches, sorted by their string representation.
      BufferedWriter writer = null;
      try {
        writer = new BufferedWriter(new FileWriter(output_branches));
        // Touch all covered branches (they may have been reset during generation).
        for (Branch b : branches) {
          writer.append(b.toString());
          writer.newLine();
        }
        writer.close();
      } catch (IOException e) {
        throw new Error(e);
      }
    }

    if (output_covmap != null) {
      try {
        FileOutputStream fileos = new FileOutputStream(output_covmap);
        ObjectOutputStream objectos = new ObjectOutputStream(new GZIPOutputStream(fileos));
        objectos.writeObject(explorer.branchesToCoveringSeqs);
        objectos.close();
        fileos.close();
      } catch (Exception e) {
        throw new Error(e);
      }
    }


    if (output_components != null) {

      assert explorer instanceof ForwardGenerator;
      ForwardGenerator gen = (ForwardGenerator)explorer;

      // Output component sequences.
      System.out.print("Serializing component sequences...");
      try {
        FileOutputStream fileos = new FileOutputStream(output_components);
        ObjectOutputStream objectos = new ObjectOutputStream(new GZIPOutputStream(fileos));
        Set<Sequence> componentset = gen.componentManager.getAllGeneratedSequences();
        System.out.println(" (" + componentset.size() + " components) ");
        objectos.writeObject(componentset);
        objectos.close();
        fileos.close();
      } catch (Exception e) {
        throw new Error(e);
      }
    }

    if (output_stats != null) {
      SerializationHelper.writeSerialized(output_stats, explorer.stats);
    }

    if (dont_output_tests)
      return true;

    // Create JUnit files containing faults.
    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.println();
      System.out.print("Creating Junit tests (" + explorer.outSeqs.size() + " tests)...");
    }
    List<ExecutableSequence> sequences = new ArrayList<ExecutableSequence>();
    for (ExecutableSequence p : explorer.outSeqs) {
      sequences.add(p);
    }
    
    if (output_tests_serialized != null) {
      // Output executable sequences.
      System.out.println("Serializing tests...");
      try {
        
        FileOutputStream fileos = new FileOutputStream(output_tests_serialized);
        ObjectOutputStream objectos = new ObjectOutputStream(new GZIPOutputStream(fileos));
        objectos.writeObject(sequences);
        objectos.close();
        fileos.close();
        
        FileInputStream fileis = new FileInputStream(output_tests_serialized);
        ObjectInputStream objectis = new ObjectInputStream(new GZIPInputStream(fileis));
        List<ExecutableSequence> seqsfromfile = (List<ExecutableSequence>) objectis.readObject();
        assert seqsfromfile.size() == sequences.size();
        for (int i = 0 ; i < seqsfromfile.size() ; i++) {
          assert seqsfromfile.get(i).equals(sequences.get(i)) : seqsfromfile.get(i) + "@@@" + sequences.get(i);
          ExecutableSequence eseq = seqsfromfile.get(i);
          ReplayVisitor visitor = new ReplayVisitor();
          eseq.execute(visitor);
          assert eseq.getChecksResults().equals(sequences.get(i).getChecksResults()) :
            sequences.get(i).getChecksResults() + "\n" 
            + seqsfromfile.get(i) + "\n"
            + eseq.getChecksResults() + "\n" + sequences.get(i); 
          
        }
        fileis.close();
      } catch (Exception e) {
        throw new Error(e);
      }
    }

    // Remove any sequences that throw
    // randoop.util.ReflectionExecutor.TimeoutExceeded.
    {
      List<ExecutableSequence> non_timeout_seqs = new ArrayList<ExecutableSequence>();
      boolean keep = true;
      for (ExecutableSequence es : sequences) {
        for (int i = 0 ; i < es.sequence.size() ; i++) {
          List<Check> exObs = es.getChecks(i, ExpectedExceptionCheck.class);
          if (!exObs.isEmpty()) {
            assert exObs.size() == 1 : toString();
            ExpectedExceptionCheck eec = (ExpectedExceptionCheck) exObs.get(0);
            if (eec.get_value().equals("randoop.util.ReflectionExecutor.TimeoutExceeded")) {
              keep = false;
              break;
            }
          }
        }
        if (keep)
          non_timeout_seqs.add (es);
      }
      sequences = non_timeout_seqs;
    }

    // If specified, remove any sequences that don't include the target class
    // System.out.printf ("test_classes regex = %s%n",
    //                   GenInputsAbstract.test_classes);
    if (GenInputsAbstract.test_classes != null) {
      List<ExecutableSequence> tc_seqs = new ArrayList<ExecutableSequence>();
      for (ExecutableSequence es : sequences) {
        boolean keep = false;
        for (Variable v : es.sequence.getAllVariables()) {
          if (GenInputsAbstract.test_classes.matcher (v.getType().getName())
              .matches()) {
            keep = true;
            break;
          }
        }
        if (keep)
          tc_seqs.add (es);
      }
      sequences = tc_seqs;
      System.out.printf ("%n%d sequences include %s%n", sequences.size(),
                         GenInputsAbstract.test_classes);
    }

    // If specified remove any sequences that are used as inputs in other
    // tests.  These sequences are redundant.
    //
    // While we're at it, remove the useless sequence "new Object()".
    Sequence newObj = new Sequence().extend(objectConstructor);
    if (GenInputsAbstract.remove_subsequences) {
      List<ExecutableSequence> unique_seqs 
        = new ArrayList<ExecutableSequence>();
      Set<Sequence> subsumed_seqs = explorer.subsumed_sequences();
      for (ExecutableSequence es : sequences) {
        if (!subsumed_seqs.contains(es.sequence) && !es.sequence.equals(newObj)) {
          unique_seqs.add(es);
        }
      }
      if (!GenInputsAbstract.noprogressdisplay) {
        System.out.printf("%d subsumed tests removed%n", sequences.size() - unique_seqs.size());
      }
      sequences = unique_seqs;
    }

    // Generate checks from the exact sequences to be run in the
    // tests.  These checks may differ from the original checks
    // because of changes to the global state.
    File tmpfile = null;
    if (GenInputsAbstract.compare_checks) {
      try {
        tmpfile = File.createTempFile ("seqs", "gz");
      } catch (Exception e) {
        throw new Error ("can't create temp file", e);
      }
      write_junit_tests ("./before_clean", sequences, msgSender);
      write_sequences (sequences, tmpfile.getPath());
      generate_clean_checks (tmpfile.getPath());
    }

    // Run the tests a second time, looking for any different checks
    // This removes any checks whose values are not deterministic
    // (such as values dependent on the current date/time)
    if (GenInputsAbstract.compare_checks) {
      write_junit_tests ("./before_cmp", sequences, msgSender);
      remove_diff_checks (tmpfile.getPath());
      sequences = read_sequences (tmpfile.getPath());
    }

    // Write out junit tests
    if (GenInputsAbstract.outputlimit < sequences.size()) {
      List<ExecutableSequence> seqs = new ArrayList<ExecutableSequence>();
      for (int ii = 0; ii < GenInputsAbstract.outputlimit; ii++)
        seqs.add (sequences.get (ii));
      sequences = seqs;
    }
    write_junit_tests (junit_output_dir, sequences, msgSender);

    if (msgSender != null) {
      IMessage msg = new ClosingStream();
      msgSender.send(msg);
    }

    return true;
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
      MultiMap<Class<?>, PrimitiveOrStringOrNullDecl> literalmap;
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
        for (PrimitiveOrStringOrNullDecl p : literalmap.getValues(cls)) {
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
            throw new Error("This can't happen");
          }
        }
      }
    }
  }

  /**
   * Writes the sequences as junit files to the specified directory.
   *
   * msgSender can be null.
   **/
  public static void write_junit_tests (String output_dir,
                                        List<ExecutableSequence> seq,
                                        MessageSender msgSender) {

    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.printf("Writing %d junit tests%n", seq.size());
    }
    JunitFileWriter jfw
      = new JunitFileWriter(output_dir, junit_package_name,
                            junit_classname, testsperfile);
    List<File> files = jfw.createJunitFiles(seq);
    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.println();
    }
    
    for (File f : files) {
      if (!GenInputsAbstract.noprogressdisplay) {
        System.out.println("Created file: " + f.getAbsolutePath());
      }
      if (msgSender != null) {
        // TODO: This is unsafe. I'm just assuming that the list file written is the driver file
        boolean isDriver = files.lastIndexOf(f) == files.size() - 1;
        msgSender.send(new CreatedJUnitFile(f, isDriver));
      }
    }
  }

  /**
   * Execute the init routine (if user specified one)
   */
  public static void execute_init_routine (int phase) {

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

  /** Read a list of sequences from a serialized file **/
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

  /** Write out a serialized file of sequences **/
  public static void write_sequences (List<ExecutableSequence> seqs,
                                      String outfile) {
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

  /**
   * Run Randoop again and generate checks for the sequence.
   * This ensures that the checks match the state that will be
   * in the final tests (because the global state used to create the
   * checks will match that of the final tests)
   */
  public void generate_clean_checks (String outfile) {

    List<String> cmd = new ArrayList<String>();
    cmd.add ("java");
    cmd.add ("-ea");

    // Add a javaagent option if specified
    if (GenInputsAbstract.agent != null)
      cmd.add (GenInputsAbstract.agent);

    // Define any properties
    for (String prop : GenInputsAbstract.system_props) {
      cmd.add (String.format ("-D%s", prop));
    }

    cmd.add ("randoop.main.Main");
    cmd.add ("cleanobs");

    // Add applicable arguments from this call
    if (GenInputsAbstract.observers != null) {
      cmd.add ("--observers=" + GenInputsAbstract.observers.toString());
    }
    cmd.add (String.format("--usethreads=%b", ReflectionExecutor.usethreads));
    if (GenInputsAbstract.init_routine != null)
      cmd.add ("--init_routine=" + GenInputsAbstract.init_routine);
    cmd.add (String.format("--capture_output=%b",
                           GenInputsAbstract.capture_output));

    cmd.add (outfile);      List<ObjectContract> contracts = new ArrayList<ObjectContract>();

    cmd.add (outfile);
    String[] cmd_array = new String[cmd.size()];
    System.out.printf ("Executing command %s%n", cmd);
    RunCmd.run_cmd (cmd.toArray (cmd_array));
    System.out.printf ("Completed command%n");
  }

  /**
   * Runs Randoop again and generates new checks for the list of
   * sequences stored in seq_file.  Any checks that do not match
   * are presumed to be non-deteministic and are removed.  The resulting
   * sequence is written back into seq_file.
   *
   * This is run in a new JVM so that the initial global state for the
   * second run matches the initial global state for the first run.
   */
  public void remove_diff_checks (String seq_file) {

    List<String> cmd = new ArrayList<String>();
    cmd.add ("java");
    cmd.add ("-ea");

    // Add a javaagent option if specified
    if (GenInputsAbstract.agent != null)
      cmd.add (GenInputsAbstract.agent);

    // Define any properties
    for (String prop : GenInputsAbstract.system_props) {
      cmd.add (String.format ("-D%s", prop));
    }

    // Add memory size
    cmd.add (String.format ("-Xmx%dM", GenInputsAbstract.mem_megabytes));

    cmd.add ("randoop.main.Main");
    cmd.add ("rm-diff-obs");

    // Add applicable arguments from this call
    if (GenInputsAbstract.observers != null) {
      cmd.add ("--observers=" + GenInputsAbstract.observers.toString());
    }
    cmd.add (String.format("--usethreads=%b", ReflectionExecutor.usethreads));
    if (GenInputsAbstract.init_routine != null)
      cmd.add ("--init_routine=" + GenInputsAbstract.init_routine);
    cmd.add (String.format("--capture_output=%b",
                           GenInputsAbstract.capture_output));

    cmd.add (seq_file);
    cmd.add (seq_file);
    String[] cmd_array = new String[cmd.size()];
    progress.log ("Removing non-deterministic checks: executing "
                  + " command %s%n", cmd);
    RunCmd.run_cmd (cmd.toArray (cmd_array));
    progress.log ("Completed removal of non-deterministic checks");
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
  
  public static List<ObjectContract> getContractsFromAnnotations(Class<?>... classes) {

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
}
