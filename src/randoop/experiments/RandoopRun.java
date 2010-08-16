package randoop.experiments;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import randoop.Globals;


/**
 * Runs randoop experiments.
 *
 * NOTE ON UGLY HACK: Running randoop on javax.xml with the -offline flag
 * crashes randoop. Until this is fixed, method run(..) will not randoop-offline
 * experimment for package "javax.xml" (hard-wired).
 */
public class RandoopRun {

  private static int MAX_TRIES_TO_REPRODUCE_ISSTA06 = 5;

  private ExperimentBase base;

  // Input generation time (input to randoop).
  protected String time;
  // File containing the results of running randoop (statistics, input to randoop).
  protected String resultsFileOnline;
  // File containing the results of running randoop-offline (statistics, input to randoop).
  protected String resultsFileOffline;
  // Prefix to use when generating junit tests (input to randoop).
  protected String junitNamePrefixOnline;
  protected String junitNamePrefixOffline;
  // Randoop-generated files.
  protected List<String> junitFiles;

  private static boolean verbose = true;

  public RandoopRun(ExperimentBase base) {

    this.base = base;

    this.time = base.extraProperties.getProperty("TIME");

    this.resultsFileOffline = this.base.experimentName + ".offline.results.tex";
    this.resultsFileOnline = this.base.experimentName + ".results.tex";

    String packageNameUnderscore = this.base.experimentName.replace('.', '_');

    this.junitNamePrefixOnline = "TestOnline_" + packageNameUnderscore;
    this.junitNamePrefixOffline = "TestOffline_" + packageNameUnderscore;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder(this.base.toString());
    b.append("time:" + this.time + Globals.lineSep);
    b.append("resultsFileOnline:" + this.resultsFileOnline + Globals.lineSep);
    b.append("resultsFileOffline:" + this.resultsFileOffline + Globals.lineSep);
    b.append("junitNamePrefixOnline:" + this.junitNamePrefixOnline + Globals.lineSep);
    b.append("junitNamePrefixOffline:" + this.junitNamePrefixOffline + Globals.lineSep);
    b.append("junitFiles:" + this.junitFiles + Globals.lineSep);
    return b.toString();
  }

  private void clean(RunType runType) {
    System.out.println("========== Removing files from previous run of this experiment.");
    List<String> rm = new ArrayList<String>();
    rm.add("rm");
    if (runType == RunType.OFFLINE)
      rm.add(this.resultsFileOffline);
    else
      rm.add(this.resultsFileOnline);
    this.junitFiles = findGeneratedJunitFiles(runType);
    rm.addAll(this.junitFiles);
    if (verbose)
      ExperimentBase.printCommand(rm, true, true);
    Command.runCommandOKToFail(rm.toArray(new String[0]), "CLEAN", true, "", true);
  }

  public static enum RunType { OFFLINE, ONLINE }

  private void run(RunType runType, String resultsFile,
      MultiRunResults results, boolean reproduceISSTA06) throws IOException {

    System.out.println("========== RUNNING EXPERIMENT:");
    System.out.println(toString());

    int numTries = 0;
    boolean success = false;
    while (!success) {
      // Run randoop.
      clean(runType);
      try {
        callRandoop(runType);
      } catch (Command.KillBecauseTimeLimitExceed e) {
        System.out.println("Run of randoop terminated because it appears to be nonterminating.");
        numTries++;
        continue;
      }
      verifyResults(runType);
      printResultsToStdout(resultsFile);
      Properties p = new Properties();
      FileInputStream inputStream= null;
      try{
        inputStream = new FileInputStream(resultsFile);
        p.load(inputStream);
      } finally{
        if (inputStream != null)
          inputStream.close();
      }
      String experimentName =
        (runType == RunType.ONLINE ? "online" : "offline")
        + this.base.experimentName.replace(".", "");
      results.addRunResults(experimentName, p);
      if (reproduceISSTA06) {
        try {
          numTries++;
          ReproduceISSTA06.checkIfReproduced(runType, this.base.experimentName, p);
          success = true;
        } catch (ReproduceISSTA06Failure e) {
          System.out.println("Failed to reproduce experiment (try: " + numTries + " out of " + MAX_TRIES_TO_REPRODUCE_ISSTA06
              + ", message: " + e.getMessage());
          if (numTries == MAX_TRIES_TO_REPRODUCE_ISSTA06) {
            throw new RuntimeException("Failed to reproduce " + MAX_TRIES_TO_REPRODUCE_ISSTA06 + " times. This is the"
                + " lmit of tries.");
          }
        }
      } else {
        success = true;
      }
    }
  }

  private void run(MultiRunResults onlineResults,
      MultiRunResults offlineResults,
      boolean reproduceISSTA06) throws IOException {

    System.out.println("========== RUNNING EXPERIMENT:");
    System.out.println(toString());

    run(RunType.ONLINE, this.resultsFileOnline, onlineResults, reproduceISSTA06);

    /* NOTE ON UGLY HACK: Running randoop on javax.xml with the -offline flag
     * crashes randoop. Until this is fixed, method run(..) will not randoop-offline
     * experimment for package "javax.xml" (hard-wired).
     */
    if (this.base.experimentName.equals("javax.xml")) {
      return;
    }
    run(RunType.OFFLINE, this.resultsFileOffline, offlineResults, reproduceISSTA06);
  }

  private void printResultsToStdout(String resultsFileName) throws IOException {
    System.out.println("========== Results");
    BufferedReader reader= null;
    try{
      reader = new BufferedReader(new FileReader(resultsFileName));
      String line = reader.readLine();
      while (line != null) {
        System.out.println(line);
        line = reader.readLine();
      }
    } finally{
      if (reader != null)
        reader.close();
    }
  }

  private void verifyResults(RunType runType) throws IOException {
    if (this.junitFiles.size() == 0)
      return;
    System.out.println("========== Compiling and running randoop-generated Junit tests.");
    List<String> compileJunit = new ArrayList<String>();
    compileJunit.add("javac");
    compileJunit.add("-J" + Command.javaHeapSize);
    compileJunit.add("-classpath");
    compileJunit.add(".:" + this.base.classPath);
    compileJunit.addAll(this.junitFiles);
    if (verbose)
      ExperimentBase.printCommand(compileJunit, true, true);
    Command.runCommand(compileJunit.toArray(new String[0]), "COMPILE JUNIT", true, "", true);

    List<String> runJunit = new ArrayList<String>();
    runJunit.add("java");
    compileJunit.add(Command.javaHeapSize);
    runJunit.add("-classpath");
    runJunit.add(".:" + this.base.classPath);
    if (runType == RunType.OFFLINE)
      runJunit.add(this.junitNamePrefixOffline);
    else
      runJunit.add(this.junitNamePrefixOnline);
    if (verbose)
      ExperimentBase.printCommand(runJunit, true, true);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(bos);
    Command.exec(runJunit.toArray(new String[0]), printStream, printStream, "", true);
    String s = bos.toString();
    try {
      Check_no_error_reports.check_no_error_reports(new StringReader(s));
    } catch (RuntimeException e) {
      System.out.println("WARNING! " + e.getMessage());
    }
  }


  private void callRandoop(RunType runType) throws IOException {
    System.out.println("========== Running randoop.");
    List<String> randoop = new ArrayList<String>();
    randoop.add("java");
    randoop.add(Command.javaHeapSize);
    randoop.add("-classpath");
    randoop.add(this.base.classPath);
    randoop.add("randoop.main.Main");
    randoop.add("genfailures");
    randoop.add("--progressinterval=1");
    randoop.add("--minimize");
    // randoop.add("--log=temp.log");
    randoop.add("--track-coverage=cobertura.ser");
    randoop.add("--maxsize=50");
    randoop.add("--usethreads=false");
    randoop.add("--classlist=" + this.base.targetClassListFile);
    randoop.add("--timelimit=" + this.time);
    if (runType == RunType.OFFLINE) {
      randoop.add("--offline");
      randoop.add("--noredundancychecks");
      randoop.add("--experiment=" + this.resultsFileOffline);
      randoop.add("--junitclass=" + this.junitNamePrefixOffline);
    } else {
      randoop.add("--experiment=" + this.resultsFileOnline);
      randoop.add("--junitclass=" + this.junitNamePrefixOnline);
    }

    if (!this.base.methodOmitPattern.equals(""))
      randoop.add("--omitmethods=" + this.base.methodOmitPattern);
    if (verbose)
      ExperimentBase.printCommand(randoop, true, true);
    // ByteArrayOutputStream bos = new ByteArrayOutputStream();
    FileOutputStream fos = new FileOutputStream("temp.txt");
    PrintStream err = new PrintStream(fos);
    // Kill the process if randoop takes 100 seconds more to execute than the time limit
    // (this likely means it's definitely stuck).
    int killAfterMillis = Integer.parseInt(this.time) * 1000 + 100000;
    Command.exec(randoop.toArray(new String[0]), System.out, err, "RUN JOE", true, killAfterMillis, null);
    // Command.runCommand(randoop.toArray(new String[0]), "RUN JOE", true, "", true);

    this.junitFiles = findGeneratedJunitFiles(runType);
  }

  private List<String> findGeneratedJunitFiles(RunType runType) {
    File currentDir = new File(System.getProperty("user.dir"));
    List<String> retval = new ArrayList<String>();
    for (String fileName : currentDir.list()) {
      if (fileName.startsWith((runType == RunType.OFFLINE ? this.junitNamePrefixOffline : this.junitNamePrefixOnline)))
        retval.add(fileName);
    }
    return retval;
  }

  /**
   * args[0] is the name of the filename to write experiment results to.
   * The rest of the args are filenames containing experiment properties.
   */
  public static void main(String[] args) throws IOException     {

    List<String> args2 = new ArrayList<String>();
    String resultsFileName = null;
    boolean reproduceISSTA06 = false;
    for (String s : args) {
      if (s.equals("-verbose")) {
        verbose = true;
      } else  if (s.startsWith("-resultsfile:")) {
        resultsFileName = s.substring("-resultsfile:".length());
      } else if (s.equals(("-reproduceISSTA06"))) {
        reproduceISSTA06 = true;
      } else {
        args2.add(s);
      }
    }

    String[] experiments = new String[args2.size()];

    for (int i = 0 ; i < args2.size() ; i++) {
      experiments[i] = args2.get(i);
    }

    List<RandoopRun> randoopRuns = getRandoopRuns(ExperimentBase.getExperimentBasesFromFiles(experiments));

    MultiRunResults onlineResults = new MultiRunResults();
    MultiRunResults offlineResults = new MultiRunResults();

    for (RandoopRun run : randoopRuns) {
      run.run(onlineResults, offlineResults, reproduceISSTA06);
    }

    String onlineResultString = onlineResults.toString("online", MultiRunResults.OutputFormat.LATEX);
    String offlineResultString = offlineResults.toString("offline", MultiRunResults.OutputFormat.LATEX);

    // Write experiment results to file, if --resultsfile option was given.
    if (resultsFileName != null) {
      FileWriter writer = new FileWriter(resultsFileName);
      writer.write(onlineResultString + Globals.lineSep);
      writer.write(offlineResultString + Globals.lineSep);
      writer.flush();
      writer.close();
    }

    // Write experiment results to stdout.
    System.out.println(onlineResultString);
    System.out.println(offlineResultString);
  }

  private static List<RandoopRun> getRandoopRuns(List<ExperimentBase> name) {
    List<RandoopRun> retval = new ArrayList<RandoopRun>();
    for (ExperimentBase base : name) {
      retval.add(new RandoopRun(base));
    }
    return retval;
  }


}
