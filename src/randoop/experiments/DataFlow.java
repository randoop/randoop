package randoop.experiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class DataFlow {

  /**
   * This is a script we use to run research experiments.
   * 
   * Runs randoop.main.DataFlow on a given subject program.
   * 
   * This command must be run from $RANDOOP_HOME/systemtests.
   * The command is run as follows:
   * 
   *   java randoop.experiments.DataFlow <subject-program-string>-<suffix>
   *   
   * The command assumes two things:
   * 
   *   + The file experiments/<subject-program-string>.experiment exists and
   *     specifies a subject program property file.
   *     
   *   + The file <subject-program-string>-<suffix>.ser exists and contains
   *     the serialized results of a run of Randoop on classes in the subject
   *     program.
   *     
   *  The command reads in the property file and uses it to create the
   *  appropriate classpath. It invokes randoop.main.DataFlow with the
   *  given classpath and passes the serialized file as input.
   */
  public static void main(String[] args) throws IOException {

    // Parse experiment string.
    if (args.length != 1) throw new IllegalArgumentException("No experiment string specified.");
    String[] split = args[0].split("-");
    if (split.length != 2) throw new IllegalArgumentException("Invalid experiment string.");
    String expName = split[0];
    String className = split[1];
    if (expName.length() == 0) throw new IllegalArgumentException("Invalid experiment name (empty).");
    if (className.length() == 0) throw new IllegalArgumentException("Invalid class name (empty).");

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
    List<String> df = new ArrayList<String>();
    df.add("java");
    df.add("-ea");
    df.add(Command.javaHeapSize);
    df.add("-classpath");
    df.add(exp.classPath);
    df.add("randoop.main.DataFlow");
    df.add("--scratchdir=" + args[0] + "-scratch");
    df.add("--overwrite");
    df.add(args[0] + ".dfin.txt.gz");

    ExperimentBase.printCommand(df, false, true);
    int retval = Command.exec(df.toArray(new String[0]), System.out,
        err, "", false, Integer.MAX_VALUE, null);
    if (retval != 0) {
      System.out.println("Command exited with error code " + retval);
      System.out.println("File log.txt contains output of stderr.");
      System.exit(1);
    }
  }
}
