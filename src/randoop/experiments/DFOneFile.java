package randoop.experiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class DFOneFile {


  /**
   * Runs DF on a single file (i.e. frontier branch).
   * This command must be run from $RANDOOP_HOME/systemtests.
   * The command is run as follows:
   *
   *   java randoop.experiments.DFOneFile <subject-program-string>-<df-input-file>
   *
   * The command assumes:
   *
   *   + The file experiments/<subject-program-string>.experiment exists and
   *     specifies a subject program property file.
   */
  public static void main(String[] args) throws IOException {

    // Parse experiment string.
    if (args.length != 1) throw new IllegalArgumentException("No experiment string specified.");
    String[] split = args[0].split("-");
    if (split.length != 2) throw new IllegalArgumentException("Invalid experiment string:" + args[0]);
    String expName = split[0];
    if (expName.length() == 0) throw new IllegalArgumentException("Invalid experiment name (empty).");
    String frontier = split[1];
    if (frontier.length() == 0) throw new IllegalArgumentException("Invalid input file name (empty).");

    // Create experiment base from experiment name. The experiment file
    // is expected to be in experiments/<experiment-name>.exp
    String expFileName = "experiments/" + expName + ".experiment";
    File expFile = new File(expFileName);
    if (!expFile.exists())
      throw new IllegalArgumentException("Experiment file does not exist: " + expFile.getAbsolutePath());

    ExperimentBase exp = new ExperimentBase(expFile.getAbsolutePath());

    FileOutputStream fos = new FileOutputStream("log.txt");
    PrintStream err = new PrintStream(fos);

    System.out.println("========== Calling DataFlow: " + exp.classDirAbs);
    List<String> randoop = new ArrayList<String>();
    randoop.add("java");
    randoop.add("-ea");
    randoop.add(Command.javaHeapSize);
    randoop.add("-classpath");
    randoop.add(exp.classPath);
    randoop.add("randoop.main.DataFlow");
    randoop.add("--scratchdir=" + frontier + "-scratch");
    randoop.add("--overwrite");
    randoop.add("--outputfile=" + frontier + ".dfout.gz");
    randoop.add(frontier);

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
