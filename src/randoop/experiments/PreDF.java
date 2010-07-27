package randoop.experiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import plume.Option;
import plume.Options;
import plume.Options.ArgException;

/**
 * 
 * MIT-specific! Will probably not work outside CSAIL.
 * 
 * Calls Randoop, then determines frontier branches and creates input
 * files for DataFlow (one file per frontier branch).
 */
public class PreDF {

  @Option("(CAN REPEAT) Skip the given step")
  public static List<Integer> skip = new ArrayList<Integer>();

  private static ExperimentBase exp;

  private static String expName;

  private static PrintStream err;

  public static void main(String[] args2) throws IOException {

    // Parse options and ensure that a scratch directory was specified.
    Options options = new Options(PreDF.class);
    String[] args = null;
    try {
      args = options.parse(args2);
    } catch (ArgException e) {
      throw new Error(e);
    }

    // Parse experiment string.
    if (args.length != 1) {
      throw new IllegalArgumentException("No experiment string specified.");
    }
    expName = args[0];

    // Create experiment base from experiment name. The experiment file
    // is expected to be in experiments/<experiment-name>.exp
    String expFileName = "experiments/" + expName + ".experiment";
    File expFile = new File(expFileName);
    if (!expFile.exists()) {
      String msg = "Experiment file does not exist: " + expFile.getAbsolutePath();
      throw new IllegalArgumentException(msg);
    }

    exp = new ExperimentBase(expFile.getAbsolutePath());

    FileOutputStream fos = new FileOutputStream("log.txt");
    err = new PrintStream(fos);

    if (!skip.contains(1)) {
      callRandoop();
    }

    if (!skip.contains(2)) {
      createReport();
    }

    if (!skip.contains(3)) {
      computeFrontiers();
    }
  }

  private static void callRandoop() {
    System.out.println("========== Calling Randoop: " + exp.classDirAbs);
    List<String> randoop = new ArrayList<String>();
    randoop.add("java");
    randoop.add("-ea");
    randoop.add(Command.javaHeapSize);
    randoop.add("-classpath");
    randoop.add(exp.covInstSourcesDir + ":" + exp.classPath);
    randoop.add("randoop.main.Main");
    randoop.add("gentests");
    randoop.add("--dont-output-tests");
    randoop.add("--timelimit=1000000");
    randoop.add("--stop-when-plateau=50");
    randoop.add("--helpers=true");
    randoop.add("--coverage-instrumented-classes=" + exp.covInstClassListFile);
    randoop.add("--classlist=" + exp.targetClassListFile);
    if (exp.methodOmitPattern != null && !exp.methodOmitPattern.trim().equals("")) {
      randoop.add("--omitmethods=" + exp.methodOmitPattern);
    }
    randoop.add("--forbid-null=true");
    randoop.add("--stats-coverage=true");
    randoop.add("--usethreads=false");
    randoop.add("--alias-ratio=0.5");
    randoop.add("--use-object-cache");
    randoop.add("--maxsize=50");
    randoop.add("--output-covmap=" + expName + ".covmap.gz");
    randoop.add("--output_cov_witnesses=true");

    ExperimentBase.printCommand(randoop, false, true);
    int retval = Command.exec(randoop.toArray(new String[0]), System.out,
                              err, "", false, Integer.MAX_VALUE, null);
    if (retval != 0) {
      System.out.println("Command exited with error code " + retval);
      System.out.println("File log.txt contains output of stderr.");
      System.exit(1);
    }
  }

  private static void createReport() {

    System.out.println("========== Creating coverage report: " + exp.classDirAbs);

    List<String> report = new ArrayList<String>();
    report.add("java");
    report.add("-ea");
    report.add(Command.javaHeapSize);
    report.add("-classpath");
    report.add(exp.covInstSourcesDir + ":" + exp.classPath);
    report.add("randoop.experiments.CreateCovReport");
    report.add("--input-map=" + expName + ".covmap.gz");
    report.add("--input-cov-class-list=" + exp.covInstClassListFile);
    report.add("--output-report=" + expName + ".covreport.txt");

    ExperimentBase.printCommand(report, false, true);
    int retval = Command.exec(report.toArray(new String[0]), System.out,
        err, "", false, Integer.MAX_VALUE, null);
    if (retval != 0) {
      System.out.println("Command exited with error code " + retval);
      System.out.println("File log.txt contains output of stderr.");
      System.exit(1);
    }
  }

  private static void computeFrontiers() {

    System.out.println("========== Computing frontiers: " + exp.classDirAbs);

    List<String> frontiers = new ArrayList<String>();
    frontiers.add("java");
    frontiers.add("-ea");
    frontiers.add(Command.javaHeapSize);
    frontiers.add("-classpath");
    frontiers.add(exp.covInstSourcesDir + ":" + exp.classPath);
    frontiers.add("randoop.main.ComputeFrontierBranches");
    frontiers.add("--seqs-per-method=1000000");
    frontiers.add("--input-map=" + expName + ".covmap.gz");
    frontiers.add("--experiment=" + expName);
    ExperimentBase.printCommand(frontiers, false, true);
    int retval = Command.exec(frontiers.toArray(new String[0]), System.out,
        err, "", false, Integer.MAX_VALUE, null);
    if (retval != 0) {
      System.out.println("Command exited with error code " + retval);
      System.out.println("File log.txt contains output of stderr.");
      System.exit(1);
    }
  }

}


//   private static void combineCovMaps() {

//     System.out.println("========== Combining coverage maps: " + exp.classDirAbs);

//     List<String> combine = new ArrayList<String>();
//     combine.add("java");
//     combine.add("-ea");
//     combine.add(Command.javaHeapSize);
//     combine.add("-classpath");
//     combine.add(exp.covInstSourcesDir + ":" + exp.classPath);
//     combine.add("randoop.experiments.CombineCovMaps");
//     combine.add("--outputmap=" + expName + ".covmap.gz");
//     combine.add("--inputmap=" + expName + ".covmap.gz");

//     ExperimentBase.printCommand(combine, false, true);
//     int retval = Command.exec(combine.toArray(new String[0]), System.out,
//         err, "", false, Integer.MAX_VALUE, null);
//     if (retval != 0) {
//       System.out.println("Command exited with error code " + retval);
//       System.out.println("File log.txt contains output of stderr.");
//       System.exit(1);
//     }
//   }

//   private static void df() {

//     System.out.println("========== Calling DataFlow: " + exp.classDirAbs);
//     List<String> df = new ArrayList<String>();
//     df.add("java");
//     df.add("-ea");
//     df.add(Command.javaHeapSize);
//     df.add("-classpath");
//     df.add(exp.classPath);
//     df.add("randoop.main.DataFlow");
//     df.add("--scratchdir=" + expName + "-scratch");
//     df.add("--overwrite");
//     df.add(expName + ".frontiers.gz");
//     ExperimentBase.printCommand(df, false, true);
//     int retval = Command.exec(df.toArray(new String[0]), System.out,
//         err, "", false, Integer.MAX_VALUE, null);
//     if (retval != 0) {
//       System.out.println("Command exited with error code " + retval);
//       System.out.println("File log.txt contains output of stderr.");
//       System.exit(1);
//     }
//   }

//   private static void bdgen() {
//     System.out.println("========== Calling BDGen: " + exp.classDirAbs);
//     List<String> branchdir = new ArrayList<String>();
//     branchdir.add("java");
//     branchdir.add("-ea");
//     branchdir.add(Command.javaHeapSize);
//     branchdir.add("-classpath");
//     branchdir.add(exp.covInstSourcesDir + ":" + exp.classPath);
//     branchdir.add("randoop.main.GenBranchDir");

//     branchdir.add("--logfile=" + expName + ".bdgen.log.txt");
//     branchdir.add("--input-covinst-classes=" + exp.covInstClassListFile);
//     branchdir.add("--input-df-results=" + expName + ".frontiers.gz.output");


//     branchdir.add("--input-covmap=" + expName + ".covmap.gz");
//     // branchdir.add("--output-new-branches=" + expName + ".bdgencovmap.gz");

//     branchdir.add("--output-success-seqs=" + expName + ".successes.txt");
//     branchdir.add("--output-components-used=" + expName + ".componentsused.txt");
//     branchdir.add("--output-new-sequences=" + expName + ".bdgen.output.txt");

//     List<String> classNames = null;
//     try {
//       classNames = Files.readWhole(exp.targetClassListFile);
//     } catch (IOException e) {
//       throw new Error(e);
//     }
//     for (String cls : classNames)
//       branchdir.add("--input-components-ser=rp1." + expName + "-" + cls + ".components.gz");

//     ExperimentBase.printCommand(branchdir, false, true);
//     int retval = Command.exec(branchdir.toArray(new String[0]), System.out,
//         err, "", false, Integer.MAX_VALUE, null);
//     if (retval != 0) {
//       System.out.println("Command exited with error code " + retval);
//       System.out.println("File log.txt contains output of stderr.");
//       System.exit(1);
//     }
//   }

//   private static void randoop2() {
//     System.out.println("========== Calling Randoop post-BDGen: " + exp.classDirAbs);
//     List<String> randoop = new ArrayList<String>();
//     randoop.add("java");
//     randoop.add("-ea");
//     randoop.add(Command.javaHeapSize);
//     randoop.add("-classpath");
//     randoop.add(exp.covInstSourcesDir + ":" + exp.classPath);
//     randoop.add("randoop.main.Main");
//     randoop.add("gentests");
//     randoop.add("--check-object-contracts=false");
//     // No time limit. We use plateau limit.
//     randoop.add("--timelimit=10000");
//     randoop.add("--stop-when-plateau=10");
//     List<String> classNames;
//     try {
//       classNames = Files.readWhole(exp.targetClassListFile);
//     } catch (IOException e) {
//       throw new Error(e);
//     }
//     for (String className : classNames) {
//       randoop.add("--testclass=" + className);
//     }
//     randoop.add("--dont-output-tests");
//     randoop.add("--forbid-null=false");
//     randoop.add("--null-ratio=0.5");

//     randoop.add("--componentfile-txt=" + expName + ".bdgen.output.txt");

//     randoop.add("--output-stats=" + expName + ".stats2.ser");

//     randoop.add("--coverage-instrumented-classes=" + exp.covInstClassListFile);

//     ExperimentBase.printCommand(randoop, false, true);
//     int retval = Command.exec(randoop.toArray(new String[0]), System.out,
//         err, "", false, Integer.MAX_VALUE, null);
//     if (retval != 0) {
//       System.out.println("Command exited with error code " + retval);
//       System.out.println("File log.txt contains output of stderr.");
//       System.exit(1);
//     }
//   }
