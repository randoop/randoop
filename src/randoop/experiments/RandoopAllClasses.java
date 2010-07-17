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
 * Runs Randoop on a all the classes (together) of a given subject program.
 * This command must be run from $RANDOOP_HOME/systemtests.
 * The command is run as follows:
 *
 *   java randoop.experiments.RandoopOneClass<?> <subject-program-string>
 *
 * The command assumes:
 *
 *   + The file experiments/<subject-program-string>.experiment exists and
 *     specifies a subject program property file.
 *
 *  The command reads in the property file and uses it to create the
 *  appropriate classpath and other parameters.
 */
public class RandoopAllClasses {

  public static String seed;

  @Option("If true, Randoop uses object-cache (together with component-based)")
  public static Boolean objectcache = null;

  @Option("If true, Randoop uses component-based generation")
  public static Boolean component_based = null;

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

    if (component_based == null) {
      throw new IllegalArgumentException("component-based option must be specfied.");
    }
    
    if (component_based) {
      if (objectcache == null) {
        throw new IllegalArgumentException("objectcache option must be specified.");
      }
    }

    // Parse experiment string.
    if (args.length != 1) {
      throw new IllegalArgumentException("No experiment string specified.");
    }
    // argument format: TTSS/TTSSN.data.gz
    // where TT is the name of technique (om=random walk, fc=randoop, etc.)
    // SS is the name of experiment (jf=jfreechart, cc=commons collections, etc.)
    // N is a number specifying the random seed to use; can be more than one digit.
    assert args[0].endsWith(".data.gz");
    String ttss = args[0].substring(0, 4);
    assert args[0].charAt(4) == '/';
    assert args[0].substring(5, 9).equals(ttss);
    seed = args[0].substring(9, args[0].length() - 8);

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
    randoop.add("java");
    randoop.add("-ea");
    randoop.add(Command.javaHeapSize);
    randoop.add("-classpath");
    randoop.add(exp.covInstSourcesDir + ":" + exp.classPath);
    randoop.add("randoop.main.Main");
    randoop.add("gentests");
    // randoop.add("--output-tests=fail");
    randoop.add("--timelimit=1000000");
    randoop.add("--stop-when-plateau=50");
    randoop.add("--helpers=true");
    randoop.add("--coverage-instrumented-classes=" + exp.covInstClassListFile);
    randoop.add("--classlist=" + exp.targetClassListFile);
    if (exp.methodOmitPattern != null && !exp.methodOmitPattern.trim().equals("")) {
      randoop.add("--omitmethods=" + exp.methodOmitPattern);
    }
    // randoop.add("--junit-classname=" + summary);
    randoop.add("--dont-output-tests");
    randoop.add("--forbid-null=true");
    randoop.add("--stats-coverage=true");
    randoop.add("--usethreads=false");
    randoop.add("--component-based=" + component_based);
    if (component_based) {
      randoop.add("--alias-ratio=0.5");
      if (objectcache) {
        randoop.add("--use-object-cache");
      }
    }
    randoop.add("--maxsize=50");
    randoop.add("--randomseed=" + seed);
    // randoop.add("--output-components=" + exp.experimentName + ".components.gz");
    // randoop.add("--output-covmap=" + exp.experimentName + ".covmap.gz");
    // randoop.add("--randoop-exp");
    // randoop.add("--dontexecute");
    randoop.add("--expfile="
                + ttss
                + "/"
                + ttss
                + seed
                + ".data");

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
