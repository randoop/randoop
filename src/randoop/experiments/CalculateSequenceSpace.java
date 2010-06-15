package randoop.experiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class CalculateSequenceSpace {

  private static final int MAX_LENGTH = 2;

  /**
   * Compute the size of the sequence space for lengths 1, 2, ... MAX_LENGTH.
   * This command must be run from $RANDOOP_HOME/systemtests.
   * The command is run as follows:
   *
   *   java randoop.experiments.CalculateSequenceSpace <subject-program-string>
   *
   * The command assumes:
   *
   *   + The file experiments/<subject-program-string>.experiment exists and
   *     specifies a subject program property file.
   */
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

    FileOutputStream fos = new FileOutputStream("log.txt");
    PrintStream err = new PrintStream(fos);

    System.out.println("========== Calculating sequence space for " + exp.experimentName);
    for (int length = 1 ; length <= MAX_LENGTH ; length++) {
      System.out.println("Length " + length + ": ");
      List<String> randoop = new ArrayList<String>();
      randoop.add("java");
      randoop.add("-ea");
      randoop.add(Command.javaHeapSize);
      randoop.add("-classpath");
      randoop.add(exp.classPath);
      randoop.add("randoop.main.Main");
      randoop.add("gentests");
      randoop.add("--noprogressdisplay");
      randoop.add("--calc-sequence-space=" + length);
      randoop.add("--output-sequence-space=" + expName + "_" + length + ".sequencespace");
      randoop.add("--classlist=" + exp.targetClassListFile);
      
      ExperimentBase.printCommand(randoop, false, true);

      // ExperimentBase.printCommand(randoop, false, true);
      int retval = Command.exec(randoop.toArray(new String[0]), System.out,
          err, "", false, Integer.MAX_VALUE, null);
      if (retval != 0) {
        System.out.println("Command exited with error code " + retval);
        System.out.println("File log.txt contains output of stderr.");
        System.exit(1);
      }
    }
  }
}
