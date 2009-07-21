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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import randoop.AbstractGenerator;
import randoop.BugInRandoopException;
import randoop.ContractCheckingVisitor;
import randoop.EnumGenCounter;
import randoop.EqualsHashcode;
import randoop.EqualsSymmetric;
import randoop.EqualsToItself;
import randoop.EqualsToNull;
import randoop.ExecutableSequence;
import randoop.ExecutionVisitor;
import randoop.ForwardGenerator;
import randoop.Globals;
import randoop.HashCodeReturnsNormally;
import randoop.JunitFileWriter;
import randoop.NaiveRandomGenerator;
import randoop.ObjectContract;
import randoop.RConstructor;
import randoop.RMethod;
import randoop.RegressionCaptureVisitor;
import randoop.SeedSequences;
import randoop.Sequence;
import randoop.SequenceCollection;
import randoop.SequenceGeneratorStats;
import randoop.StatementKind;
import randoop.ToStringReturnsNormally;
import randoop.util.DefaultReflectionFilter;
import randoop.util.Log;
import randoop.util.Randomness;
import randoop.util.RandoopSecurityManager;
import randoop.util.Reflection;
import randoop.util.ReflectionExecutor;
import randoop.util.SerializationHelper;
import utilpag.Invisible;
import utilpag.Option;
import utilpag.Options;
import utilpag.Options.ArgException;
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

  @Invisible
  @Option("Signals that this is a run in the context of a system test. (Slower)")
  public static boolean system_test_run = false;

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
      System.out
      .println("ERROR while parsing command-line arguments (will exit): "
          + ae.getMessage());
      System.exit(-1);
    }

    Randomness.reset(randomseed);

    // Find classes to test.
    if (classlist == null && methodlist == null && testclass.size() == 0) {
      System.out.println("You must specify some classes or methods to test.");
      System.out.println("Use the --classlist, --testclass, or --methodlist options.");
      System.exit(1);
    }
    List<Class<?>> classes = findClassesFromArgs(options);

    List<StatementKind> model =
      Reflection.getStatements(classes, new DefaultReflectionFilter(omitmethods));

    // Always add Object constructor (it's often useful).
    try {
      RConstructor cons = RConstructor.getRConstructor(Object.class.getConstructor());
      if (!model.contains(cons))
        model.add(cons);
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
    System.out.println("PUBLIC MEMBERS=" + model.size());

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
        System.out.println("Will track branch coverage for " + cls);
      }
    }


    // Initialize components.
    components = new SequenceCollection();
    if (!componentfile_ser.isEmpty()) {
      for (String onefile : componentfile_ser) {
        try {
          FileInputStream fileos = new FileInputStream(onefile);
          ObjectInputStream objectos = new ObjectInputStream(new GZIPInputStream(fileos));
          Set<Sequence> seqset = (Set<Sequence>)objectos.readObject();
          System.out.println("Adding " + seqset.size() + " component sequences from file "
              + onefile);
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
        System.out.println("Adding " + seqset.size() + " component sequences from file "
            + onefile);
        components.addAll(seqset);
      }
    }
    components.addAll(SeedSequences.objectsToSeeds(SeedSequences.primitiveSeeds));

    AbstractGenerator explorer = null;

    if (calc_sequence_space != null) {

      // This explorer calls System.exit(0) by itself
      // when it's done; the rest of the code after explore()
      // will not be executed.
      explorer = new EnumGenCounter(
          model,
          covClasses,
          Long.MAX_VALUE,
          Integer.MAX_VALUE,
          components);
      ((EnumGenCounter)explorer).LIMIT = calc_sequence_space;

    } else if (component_based) {
      explorer = new ForwardGenerator(
        model,
        covClasses,
        timelimit * 1000,
        inputlimit,
        components);

    } else {

      // Generate inputs.
      explorer = new NaiveRandomGenerator(
          model,
          covClasses,
          timelimit * 1000,
          inputlimit,
          components);
    }

    // Determine what visitors to install.
    // NOTE that order matters! Regression capture visitor
    // should come after contract-violating visitor.
    List<ExecutionVisitor> visitors = new ArrayList<ExecutionVisitor>();
    if (check_object_contracts) {
      List<ObjectContract> contracts = new ArrayList<ObjectContract>();
      contracts.add(new EqualsToItself());
      contracts.add(new EqualsToNull());
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
        Set<Sequence> components = gen.components.getAllSequences();
        System.out.println(" (" + components.size() + " components) ");
        objectos.writeObject(components);
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
    System.out.println();
    System.out.print("Creating Junit tests ("
        + explorer.stats.outSeqs.size() + " tests)...");
    List<ExecutableSequence> sequences = new ArrayList<ExecutableSequence>();
    for (ExecutableSequence p : explorer.stats.outSeqs) {
      sequences.add(p);
    }

    JunitFileWriter jfw = new JunitFileWriter(junit_output_dir, junit_package_name, junit_classname, testsperfile);
    List<File> files = jfw.createJunitFiles(sequences);
    System.out.println();
    for (File f : files) {
      System.out.println("Created file: " + f.getAbsolutePath());
    }

    return true;
  }
}
