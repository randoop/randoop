package randoop.experiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class RandoopOneClass {

  /**
   * Runs Randoop on a single class of a given subject program.
   * This command must be run from $RANDOOP_HOME/systemtests.
   * The command is run as follows:
   *
   *   java randoop.experiments.RandoopOneClass <subject-program-string>-<classname>
   *
   * The command assumes:
   *
   *   + The file experiments/<subject-program-string>.experiment exists and
   *     specifies a subject program property file.
   */
  public static void main(String[] args) throws IOException {

    // Parse experiment string.
    if (args.length != 1)
        throw new IllegalArgumentException("No experiment string specified.");
    String[] split = args[0].split("-");
    if (split.length != 2)
        throw new IllegalArgumentException("Invalid experiment string:" + args[0]);
    String expName = split[0];
    String className = split[1];
    if (expName.length() == 0)
        throw new IllegalArgumentException("Invalid experiment name (empty).");
    if (className.length() == 0)
        throw new IllegalArgumentException("Invalid class name (empty).");

    // Create experiment base from experiment name. The experiment file
    // is expected to be in experiments/<experiment-name>.exp
    String expFileName = "experiments/" + expName + ".experiment";
    File expFile = new File(expFileName);
    if (!expFile.exists())
      throw new IllegalArgumentException("Experiment file does not exist: " +
                                         expFile.getAbsolutePath());

    ExperimentBase exp = new ExperimentBase(expFile.getAbsolutePath());

    FileOutputStream fos = new FileOutputStream("log.txt");
    PrintStream err = new PrintStream(fos);

    System.out.println("========== Calling Randoop: " + exp.classDirAbs);
    List<String> randoop = new ArrayList<String>();
    randoop.add("java");
    randoop.add("-ea");
    randoop.add(Command.javaHeapSize);
    randoop.add("-classpath");
    randoop.add(exp.covInstSourcesDir + ":" + exp.classPath);
    randoop.add("randoop.main.Main");
    randoop.add("gentests");
    randoop.add("--check-object-contracts=false");
    randoop.add("--timelimit=10000"); // No time limit. We use plateau limit.
    randoop.add("--stop-when-plateau=10"); // Stop when 10 seconds pass with no coverage increase.
    randoop.add("--coverage-instrumented-classes=" + exp.covInstClassListFile);
    randoop.add("--testclass=" + className);
    randoop.add("--dont-output-tests");
    randoop.add("--forbid-null=true");
    randoop.add("--always-use-ints-as-objects=true");
    randoop.add("--helpers=true");
    randoop.add("--use-object-cache");
    randoop.add("--output-components=rp1." + args[0] + ".components.gz");
    randoop.add("--output-covmap=rp1." + args[0] + ".covmap.gz");
    ExperimentBase.printCommand(randoop, false, true);
    int retval = Command.exec(randoop.toArray(new String[0]), System.out,
        err, "", false, Integer.MAX_VALUE, null);
    if (retval != 0) {
      System.out.println("Command exited with error code " + retval);
      System.out.println("File log.txt contains output of stderr.");
      System.exit(1);
    }

  }

}
