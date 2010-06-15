package randoop.experiments;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class JCrasherRun {

  private String jcrasherOutputDir;
  private ExperimentBase base;

  public JCrasherRun(ExperimentBase base) {
    this.base = base;
    String packageNameUnderscore = this.base.experimentName.replace('.', '_');
    this.jcrasherOutputDir = "jcrasher_output_" + packageNameUnderscore;
  }

  private void callJcrasher(JCrasherResults results, boolean suppressNull) throws IOException {

    String finalOutputDir = this.jcrasherOutputDir + (suppressNull ? "suppressnull" : "");

    // Remove jcrasher files.
    List<String> rm = new ArrayList<String>();
    rm.add("rm");
    rm.add("-r");
    rm.add(finalOutputDir);
    Command.runCommandOKToFail(rm.toArray(new String[0]), "RM", true, "", true);

    // Jcrasher expects its output directory to exist.
    List<String> mkdir = new ArrayList<String>();
    mkdir.add("mkdir");
    mkdir.add(finalOutputDir);
    Command.runCommand(mkdir.toArray(new String[0]), "MKDIR", true, "", true);

    System.out.println("========== Running jcrasher.");
    List<String> jcrasher = new ArrayList<String>();
    jcrasher.add("java");
    jcrasher.add(Command.javaHeapSize);
    jcrasher.add("-classpath");
    jcrasher.add(this.base.classPath);
    jcrasher.add("edu.gatech.cc.jcrasher.JCrasher");
    if (suppressNull) {
      jcrasher.add("-suppressnull");
    }
    if (this.base.experimentName.startsWith("java.")) {
      // If we're testing java.*, we must
      // (1) tell Jcrasher to generate tests with a different package name,
      // otherwise jcrasher's testclasses runner...crashes.
      // (2) Since Jcrasher will create tests under a different pakcage name,
      // it shouldn't generate tests for anything other than public things,
      // otherwise generated tests will not compile.
      jcrasher.add("-prependpackage:jcrash");
      jcrasher.add("-publiconly");
    }
    jcrasher.add("-o");
    jcrasher.add(finalOutputDir);
    jcrasher.add("--verbose");
    jcrasher.add("--depth=1000");
    {
      BufferedReader reader= null;
      try{
        reader = new BufferedReader(new FileReader(this.base.targetClassListFile));
        String line = reader.readLine();
        while (line != null) {
          jcrasher.add(line.trim());
          line = reader.readLine();
        }
      } finally{
        if (reader != null)
          reader.close();
      }
    }
    ExperimentBase.printCommand(jcrasher, true, true);
    Command.runCommand(jcrasher.toArray(new String[0]), "JCRASHER", false, "", true);

    // Collect the names of all jcrasher-generated files
    List<String> jcrasherGeneratedJunitFiles;
    {
      jcrasherGeneratedJunitFiles = ExperimentBase.findJavaFilesRecursively(new File(finalOutputDir));
      System.out.println("Found " + jcrasherGeneratedJunitFiles.size() + " files.");
    }

    System.out.println("========== Compiling and running jcrasher-generated Junit tests.");
    List<String> compileJunit = new ArrayList<String>();
    compileJunit.add("javac");
    compileJunit.add("-J" + Command.javaHeapSize);
    compileJunit.add("-d");
    compileJunit.add(finalOutputDir);
    compileJunit.add("-classpath");
    compileJunit.add(finalOutputDir
        // + (this.jcrasherPackageToPrepend != null ? "/" + this.jcrasherPackageToPrepend : "")
        + ":" 
        + this.base.classPath);
    compileJunit.addAll(jcrasherGeneratedJunitFiles);
    ExperimentBase.printCommand(compileJunit, true, true);
    Command.runCommand(compileJunit.toArray(new String[0]), "COMPILE JCRASHER JUNIT", true, "", true);

    List<String> runJunit = new ArrayList<String>();
    runJunit.add("java");
    runJunit.add("-classpath");
    runJunit.add(finalOutputDir
        // + (this.jcrasherPackageToPrepend != null ? "/" + this.jcrasherPackageToPrepend : "")
        + ":" 
        + this.base.classPath);
    runJunit.add("edu.gatech.cc.junit.textui.RaGTestRunner");
    runJunit.add("-noreinit");
    runJunit.add("JUnitAll");
    ExperimentBase.printCommand(runJunit, true, true);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(bos);
    Command.exec(runJunit.toArray(new String[0]), printStream, printStream, "", true);
    String s = bos.toString();
    System.out.println(s);
    results.processJcrasherOutput(s);
  }


  private void run(JCrasherResults
      resultsRaw, JCrasherResults resultsSuppressNull) throws IOException {

    {
      long startJcrasher = System.currentTimeMillis();
      callJcrasher(resultsRaw, false);
      long endJcrasher = System.currentTimeMillis();
      System.out.println("JCrasher took " + (endJcrasher - startJcrasher)/1000);
    }

    {
      long startJcrasher = System.currentTimeMillis();
      callJcrasher(resultsSuppressNull, true);
      long endJcrasher = System.currentTimeMillis();
      System.out.println("JCrasher took " + (endJcrasher - startJcrasher)/1000);
    }

    System.out.println("==================== RESULTS SO FAR:");
    System.out.println(resultsRaw);
    System.out.println(resultsSuppressNull);

  }

  public static void main(String[] args) throws IOException     {

    List<JCrasherRun> jcrasherRuns = getJCrasherRuns(ExperimentBase.getExperimentBasesFromFiles(args));

    JCrasherResults resultsRaw = new JCrasherResults("resultsRaw");
    JCrasherResults resultsSuppressNull = new JCrasherResults("resultsSuppressNull");

    for (JCrasherRun run : jcrasherRuns) {
      run.run(resultsRaw, resultsSuppressNull);
    }
  }

  private static List<JCrasherRun> getJCrasherRuns(List<ExperimentBase> name) {
    List<JCrasherRun> retval = new ArrayList<JCrasherRun>();
    for (ExperimentBase base : name) {
      retval.add(new JCrasherRun(base));
    }
    return retval;
  }
}
