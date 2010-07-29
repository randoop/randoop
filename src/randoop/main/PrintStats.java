package randoop.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.SequenceGeneratorStats;
import randoop.util.Reflection;
import randoop.util.SerializationHelper;
import plume.Option;
import plume.Options;
import plume.Options.ArgException;
import cov.Branch;
import cov.Coverage;

/**
 * Usage: PrintStats COVCLASSES RESULT_1 ... RESULT_N
 * 
 * There must be at least one RESULT_i argument.
 * 
 * COVCLASSES is a text file with a class name in each line. The total branches
 * reported are the branches across these classes. These classes must be
 * instrumented for coverage.
 * 
 * RESULT_i is a file containing a single SequenceGeneratorStats object, created
 * by a run of Randoop. This object contains the covered branches during the
 * specific run of Randoop. The classes covered must be a subset of the classes
 * in COVCLASSES.
 * 
 */
public class PrintStats {
  
  @Option("If given, prints all the branches to the given file")
  public static String print_branches;

  @Option("(REQUIRED) Name of file containg a list of coverage-instrumented classes")
  public static String cov_classes;

  @Option("(AT LEAST ONCE REQUIRED) Name of file containg a serialized SequenceGeneratorStats object")
  public static List<String> stats_file = new ArrayList<String>();
  
  public static void main(String[] args) throws IOException {

    // Parse options and ensure that a scratch directory was specified.
    Options options = new Options(PrintStats.class);
    try {
      options.parse(args);
    } catch (ArgException e) {
      throw new Error(e);
    }
    if (cov_classes == null) {
      System.out.println("Missing required option --cov-classes=<filename>");
      System.exit(1);
    }
    if (stats_file.isEmpty()) {
      System.out.println("Missing required option --stats-file=<filename>");
      System.exit(1);
    }
    
    List<Class<?>> covClasses = null;
    try {
      covClasses  = Reflection.loadClassesFromFile(new File(cov_classes));
    } catch (IOException e) {
      throw new Error(e);
    }
    for (Class<?> cls : covClasses) {
      assert Coverage.isInstrumented(cls) : cls.toString();
      System.out.println("Will track branch coverage for " + cls);
    }

    List<SequenceGeneratorStats> runs = new ArrayList<SequenceGeneratorStats>();
    for (String stat : stats_file) {
      File resultsFile = new File(stat);
      if (!resultsFile.exists()) {
        System.out.println("DID NOT FIND FILE (WILL SKIP): " + resultsFile);
        continue;
      }
      SequenceGeneratorStats run = (SequenceGeneratorStats) SerializationHelper.readSerialized(resultsFile);
      runs.add(run);
    }

    Set<Branch> allBranches = new LinkedHashSet<Branch>();
    for (SequenceGeneratorStats in : runs) {
      allBranches.addAll(in.branchesCovered);
    }

    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter("cov.txt"));
      // Touch all covered branches (they may have been reset during generation).
      for (Branch br : allBranches) {
        Coverage.touch(br);
      }
      for (Class<?> cls : covClasses) {
//        System.out.println(cls);
        for (String s : Coverage.getCoverageAnnotatedSource(cls)) {
//          if (cls.getName().contains("SubList"))
//            System.out.println(s);
//          System.out.print(s.contains("__") ? "." : "");
//          System.out.print(s.contains("TF") ? "." : "");
//          System.out.print(s.contains("T_") ? "." : "");
//          System.out.print(s.contains("_F") ? "." : "");
          writer.append(s);
          writer.newLine();
        }
        // System.out.println();
      }
      writer.close();
    } catch (IOException e) {
      throw new Error(e);
    }
    System.out.println("BRANCHES COVERED: " + allBranches.size());
    
    if (print_branches != null) {
      System.out.print("Writing branches to file " + print_branches + "...");
      Branch.writeToFile(allBranches, print_branches, false);
      System.out.println("done.");
    }
  }

}
