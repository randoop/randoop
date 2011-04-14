package randoop.experiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import randoop.util.Files;

/**
 * Runs branch-directed generation on a given subject program.
 * This command must be run from $RANDOOP_HOME/systemtests.
 * The command is run as follows:
 * 
 *   java randoop.experiments.BranchDir <subject-program-string>-<classname>
 *   
 * The command assumes:
 * 
 *   + The file experiments/<subject-program-string>.experiment exists and
 *     specifies a subject program property file.
 *     
 *   + The file <subject-program-string>-<suffix>.ser.output exists and contains
 *     the serialized results of a run of DataFlow on classes in the subject
 *     program.
 *     
 *  The command reads in the property file and uses it to create the
 *  appropriate classpath. It invokes randoop.main.GenBranchDir
 *  with the given classpath and passes the serialized file as input.
 */

public class BranchDir {

  public static void main(String[] args) throws IOException {

    // Parse experiment string.
    if (args.length != 1) throw new IllegalArgumentException("No experiment string specified.");
    String expName = args[0];
    if (expName.length() == 0) throw new IllegalArgumentException("Invalid experiment name (empty).");

    // Create experiment base from experiment name. The experiment file
    // is expected to be in experiments/<experiment-name>.exp
    String expFileName = "experiments/" + expName + ".experiment";
    File expFile = new File(expFileName);
    if (!expFile.exists())
      throw new IllegalArgumentException("Experiment file does not exist: " + expFile.getAbsolutePath());

    ExperimentBase exp = new ExperimentBase(expFile.getAbsolutePath());

    List<String> targetClassNames = Files.readWhole(exp.targetClassListFile);


    FileOutputStream fos = new FileOutputStream("log.txt");
    PrintStream err = new PrintStream(fos);

    System.out.println("========== Calling branch-directed generator: " + exp.classDirAbs);
    List<String> branchdir = new ArrayList<String>();
    branchdir.add("java");
    branchdir.add("-ea");
    branchdir.add(Command.javaHeapSize);
    branchdir.add("-classpath");
    branchdir.add(exp.covInstSourcesDir + ":" + exp.classPath);
    branchdir.add("randoop.main.GenBranchDir");
    branchdir.add("--logfile=" + args[0] + ".bdgen.log.txt");
    branchdir.add("--outputfile=" + args[0] + ".bdgen.output.txt");
    branchdir.add("--covclasses=" + exp.covInstClassListFile);
    branchdir.add("--output-components-used=" + args[0] + ".componentsused.txt");
    branchdir.add("--output-success-seqs=" + args[0] + ".sucessses.txt");
    branchdir.add("--covered-branches=" + args[0] + ".branches.txt");
    branchdir.add("--print-branches=" + args[0] + ".bdgen.branches.txt");
    for (String className : targetClassNames) {
      String inputfile = expName + "-" + className + ".dfin.txt.gz.output";
      branchdir.add("--inputfile=" + inputfile);
      branchdir.add("--componentfile-ser=" + expName + "-" + className + ".dfin.txt.gz.components.ser.gz");
    }

    ExperimentBase.printCommand(branchdir, false, true);
    int retval = Command.exec(branchdir.toArray(new String[0]), System.out,
        err, "", false, Integer.MAX_VALUE, null);
    if (retval != 0) {
      System.out.println("Command exited with error code " + retval);
      System.out.println("File log.txt contains output of stderr.");
      System.exit(1);
    }
  }

}
