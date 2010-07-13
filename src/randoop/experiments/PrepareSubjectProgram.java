package randoop.experiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import randoop.util.Files;

/**
 *
 * This command does a number of tasks to prepare a subject program
 * for experimentation. The tasks include:
 *
 *   + Compiling the subject program.
 *   + Instrumenting the subject program for coverage.
 *   + Compiling the instrumented version.
 *
 * WARNING! This script deletes files and directories.
 * However, they are all files that can be recreated.
 */
public class PrepareSubjectProgram {

  public static void main(String[] args) throws IOException {
    if (args.length != 1)
      throw new IllegalArgumentException("Must give a single argument.");
    ExperimentBase exp = new ExperimentBase("experiments/" + args[0] + ".experiment");
    compile(exp);
  }

  private static void compile(ExperimentBase exp) throws IOException {

    File sourceListingFile = new File("sources.txt");

    FileOutputStream fos = new FileOutputStream("log.txt");
    PrintStream err = new PrintStream(fos);


    System.out.println("========== Removing temporary files from previous run of this experiment.");
    List<String> rm = new ArrayList<String>();
    rm.add("rm");
    rm.add("-f");
    rm.add(exp.targetClassListFile);
    ExperimentBase.printCommand(rm, true, true);
    Command.runCommandOKToFail(rm.toArray(new String[0]), "CLEAN", true, "", true);


    System.out.println("========== Deleting .class files in " + exp.classDirAbs);
    List<String> delCmd = new ArrayList<String>();
    delCmd.add("find");
    delCmd.add(exp.classDirAbs);
    delCmd.add("-name");
    delCmd.add("*.class");
    delCmd.add("-delete");
    ExperimentBase.printCommand(delCmd, true, true);
    int retval = Command.exec(delCmd.toArray(new String[0]),
        new PrintStream(new FileOutputStream(sourceListingFile)),
        System.err, "", false, Integer.MAX_VALUE, null);
    if (retval != 0) {
      System.out.println("find command exited with error code " + retval);
      System.out.println("File log.txt contains output of stderr.");
      System.exit(1);
    }

    System.out.println("========== Creating list of .java files for experiment (stored in "
        + sourceListingFile + ")");
    List<String> findCmd = new ArrayList<String>();
    findCmd.add("find");
    findCmd.add(exp.classDirAbs);
    findCmd.add("-name");
    findCmd.add("*.java");
    ExperimentBase.printCommand(findCmd, true, true);
    retval = Command.exec(findCmd.toArray(new String[0]),
        new PrintStream(new FileOutputStream(sourceListingFile)),
        System.err, "", false, Integer.MAX_VALUE, null);
    if (retval != 0) {
      System.out.println("find command exited with error code " + retval);
      System.out.println("File log.txt contains output of stderr.");
      System.exit(1);
    }


    System.out.println("========== Compiling subject program: " + exp.experimentName);
    List<String> compile = new ArrayList<String>();
    compile.add("javac");
    compile.add("-J-Xmx1000m");
    compile.add("-classpath");
    compile.add(exp.classPath);
    compile.add("-g");
    compile.add("@" + sourceListingFile.getAbsolutePath());
    ExperimentBase.printCommand(compile, false, false);
    retval = Command.exec(compile.toArray(new String[0]), System.out, err, "", true,
        Integer.MAX_VALUE, null);
    if (retval != 0) {
      System.out.println("javac command exited with error code " + retval);
      System.out.println("File log.txt contains output of stderr.");
      System.exit(1);
    }

    System.out.println("========== Creating public top-level class name listing file: " + exp.targetClassListFile);
    if (exp.classOmitPattern.equals("")) {
      ClassListPrinter.findPublicTopLevelClasses(exp.targetClassListFile, exp.classDirAbs, "", new PublicTopLevelClassFilter(null));
    } else {
      ClassListPrinter.findPublicTopLevelClasses(exp.targetClassListFile, exp.classDirAbs, "", new PublicTopLevelClassFilter(exp.classOmitPattern));
    }

    File covDir = new File(exp.covInstSourcesDir);
    System.out.println("========== Removing directory for coverage-instrumented sources: " + covDir.getName());
    Files.deleteRecursive(covDir);

    System.out.println("========== Instrumenting subject program for coverage: " + exp.experimentName);
    List<String> instrument = new ArrayList<String>();
    instrument.add("java");
    instrument.add("-ea");
    instrument.add(Command.javaHeapSize);
    instrument.add("-classpath");
    instrument.add(exp.classPath);
    instrument.add("cov.Instrument");
    instrument.add("--destination=" + covDir.getAbsolutePath());
    instrument.add("--files=" + sourceListingFile);
    ExperimentBase.printCommand(instrument, true, false);
    retval = Command.exec(instrument.toArray(new String[0]), System.out, err, "", true,
        Integer.MAX_VALUE, null);
    if (retval != 0) {
      System.out.println("command exited with error code " + retval);
      System.out.println("File log.txt contains output of stderr.");
      System.exit(1);
    }

    System.out.println("========== Creating list of coverage-instrumented .java files for experiment (stored in "
        + sourceListingFile + ")");
    List<String> findInstrSources = new ArrayList<String>();
    findInstrSources.add("find");
    findInstrSources.add(covDir.getName());
    findInstrSources.add("-name");
    findInstrSources.add("*.java");
    ExperimentBase.printCommand(findInstrSources, true, true);
    retval = Command.exec(findInstrSources.toArray(new String[0]),
        new PrintStream(new FileOutputStream(sourceListingFile)),
        System.err, "", false, Integer.MAX_VALUE, null);
    if (retval != 0) {
      System.out.println("find command exited with error code " + retval);
      System.out.println("File log.txt contains output of stderr.");
      System.exit(1);
    }

    System.out.println("========== Compiling coverage-instrumented .java files: " + exp.experimentName);
    List<String> compileInstr = new ArrayList<String>();
    compileInstr.add("javac");
    compileInstr.add("-J" + Command.javaHeapSize);
    compileInstr.add("-classpath");
    compileInstr.add(exp.classPath);
    compileInstr.add("-g");
    compileInstr.add("@" + sourceListingFile.getAbsolutePath());
    ExperimentBase.printCommand(compileInstr, true, false);
    retval = Command.exec(compileInstr.toArray(new String[0]), System.out, err, "", true,
        Integer.MAX_VALUE, null);
    if (retval != 0) {
      System.out.println("javac command exited with error code " + retval);
      System.out.println("File log.txt contains output of stderr.");
      System.exit(1);
    }

    System.out.println("========== Creating coverage-instrumented class name listing file: " + exp.covInstClassListFile);
    ClassListPrinter.findPublicTopLevelClasses(exp.covInstClassListFile, exp.covInstSourcesDir, "", new CoverageInstrumentedClassFilter());

  }
}
