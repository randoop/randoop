package randoop.experiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import randoop.util.Files;

public class PrintStatsOneClassExp {

  public static void main(String[] args) throws IOException {

    // Parse experiment string.
    if (args.length != 1)
      throw new IllegalArgumentException("No experiment string specified.");
    String expName = args[0];

    // Create experiment base from experiment name. The experiment file
    // is expected to be in experiments/<experiment-name>.exp
    String expFileName = "experiments/" + expName + ".experiment";
    File expFile = new File(expFileName);
    if (!expFile.exists())
      throw new IllegalArgumentException("Experiment file does not exist: "
          + expFile.getAbsolutePath());

    ExperimentBase exp = new ExperimentBase(expFile.getAbsolutePath());

    List<String> targetClassNames = Files.readWhole(exp.targetClassListFile);

    FileOutputStream fos = new FileOutputStream("log.txt");
    PrintStream err = new PrintStream(fos);

    System.out.println("========== Calling Randoop: " + exp.classDirAbs);
    List<String> printstats = new ArrayList<String>();
    printstats.add("java");
    printstats.add("-ea");
    printstats.add("-classpath");
    printstats.add(exp.covInstSourcesDir + ":" + exp.classPath);
    printstats.add("randoop.main.PrintStats");
    printstats.add("--print-branches=" + expName + ".branches.txt");
    printstats.add("--cov-classes=" + exp.covInstClassListFile);
    for (String className : targetClassNames) {
      String serializedFileName = expName + "-" + className + ".stats.ser";
      printstats.add("--stats-file=" + serializedFileName);
    }

    ExperimentBase.printCommand(printstats, false, true);
    int retval = Command.exec(printstats.toArray(new String[0]), System.out,
        err, "", false, Integer.MAX_VALUE, null);
    if (retval != 0) {
      System.out.println("Command exited with error code " + retval);
      System.out.println("File log.txt contains output of stderr.");
      System.exit(1);
    }
  }

}
