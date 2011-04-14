package randoop.experiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import plume.Options;
import plume.Options.ArgException;

public class Repro100 {

  private static ExperimentBase exp;

  private static PrintStream err;

  public static void main(String[] args2) throws IOException {

    // Parse options and ensure that a scratch directory was specified.
    Options options = new Options(RandoopAllClasses.class);
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
    // argument format: TTSS
    // where TT is the name of technique (om=random walk, fc=randoop, etc.)
    // SS is the name of experiment (jf=jfreechart, cc=commons collections, etc.)
    // N is a number specifying the random seed to use; can be more than one digit.
    String ttss = args[0];

    // Create experiment base from experiment name. The experiment file
    // is expected to be in experiments/<experiment-name>.exp
    String ss = ttss.substring(2, 4);
    String expFileName = "experiments/" + ss + ".experiment";
    File expFile = new File(expFileName);
    if (!expFile.exists()) {
      String msg = "Experiment file does not exist: " + expFile.getAbsolutePath();
      throw new IllegalArgumentException(msg);
    }

    exp = new ExperimentBase(expFile.getAbsolutePath());

    FileOutputStream fos = new FileOutputStream("log.txt");
    err = new PrintStream(fos);

    System.out.println("========== Calling Randoop: " + exp.classDirAbs);
    List<String> randoop = new ArrayList<String>();
    randoop.add("javac");
    randoop.add("-J" + Command.javaHeapSize);
    randoop.add("-classpath");
    randoop.add(exp.covInstSourcesDir + ":" + exp.classPath);
    randoop.add("randoop100/" + ttss + ".java");
    randoop.add("-d");
    randoop.add("randoop100");

    ExperimentBase.printCommand(randoop, false, true);
    int retval = Command.exec(randoop.toArray(new String[0]), System.out,
        err, "", false, Integer.MAX_VALUE, null);
    if (retval != 0) {
      System.out.println("Command exited with error code " + retval);
      System.out.println("File log.txt contains output of stderr.");
      System.exit(1);
    }

    System.out.println("========== Calling Randoop: " + exp.classDirAbs);
    randoop = new ArrayList<String>();
    randoop.add("java");
    randoop.add(Command.javaHeapSize);
    randoop.add("-classpath");
    randoop.add("randoop100:" + exp.covInstSourcesDir + ":" + exp.classPath);
    randoop.add(ttss);

    ExperimentBase.printCommand(randoop, false, true);
    retval = Command.exec(randoop.toArray(new String[0]), System.out,
        err, "", false, Integer.MAX_VALUE, null);
    if (retval != 0) {
      System.out.println("Command exited with error code " + retval);
      System.out.println("File log.txt contains output of stderr.");
      System.exit(1);
    }

  }
}
